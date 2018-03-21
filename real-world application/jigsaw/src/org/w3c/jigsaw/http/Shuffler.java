// Shuffler.java
// $Id: Shuffler.java,v 1.1 2010/06/15 12:21:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Vector ;

/**
 * This describes the message structure that we exchange with the shuffler.
 * The equivalent C structure is defined in ShufflerProtocol.h
 */

class ShuffleMessage {
    byte op ;
    int  id ;
    int  length ;
}

/**
 * Manage the queue of pending shuffler requests.
 * This object manages the queue of pending shuffler requests. It life goes
 * like this: wait for something to be put on the queue, once the queue is
 * not empty, send some request to the underlying <em>shuffler</em> process
 * and try getting <strong>DONE</strong> messages from the shuffler.
 * For each message got, find back the appropriate handler and terminate it.
 */

class ShufflerThread extends Thread {
    private static final boolean debug = true ;

    Shuffler s ;
    Vector   q ;

    /**
     * Add the given handler in our wait queue.
     * @param h The handler to wait for.
     */

    synchronized void registerHandler (ShuffleHandler h) {
	q.addElement (h) ;
	notifyAll () ;
    }

    /**
     * Process the given shuffler process message.
     * This method can emit a RuntimeException if some internal state becomes
     * inconsistent. This is typically the case if we can find back a request
     * from the queue.
     * @param msg The (process) shuffler message to handle.
     */

    synchronized void processMessage (ShuffleMessage msg) {
	int id = msg.id ;
	// FIXME: use some thing better than liner lookup here ?
	for (int i = 0 ; i < q.size() ; i++) {
	    ShuffleHandler h = (ShuffleHandler) q.elementAt (i) ;
	    if ( h.id == id ) {
		q.removeElementAt(i) ;
		h.done(msg.length) ;
		return ;
	    }
	}
	for (int i = 0 ; i < q.size() ; i++)
	    System.out.println ("waiting for : " + q.elementAt(i)) ;
	throw new RuntimeException (this.getClass().getName()
				    + ": received unexpected id " + id) ;
    }

    /**
     * Block the thread until we get some pending shuffles to wait for.
     */

    synchronized void waitForHandlers () {
	while (q.size() == 0) {
	    try {
		wait () ;
	    } catch (InterruptedException e) {
	    }
	}
    }

    public void run () {
	while ( true ) {
	    waitForHandlers() ;
	    ShuffleMessage msg = s.getNextMessage () ;
	    processMessage (msg) ;
	}
    }

    ShufflerThread (Shuffler s) {
	this.s = s ;
	this.q = new Vector() ;
	setPriority (9) ;
	setName ("ShufflerThread") ;
	setDaemon (true) ;
    }

}

/**
 * Objects describing pending shuffle requests.
 */

class ShuffleHandler {
    FileDescriptor in       = null ;
    FileDescriptor out      = null ;
    boolean        doneflag = false ;
    int            id       = -1 ;
    int            length   = -1 ;

    /**
     * Notify that this shuffle handle is now completed.
     */

    synchronized void done (int length) {
	this.length   = length ;
	this.doneflag = true ;
	notifyAll () ;
    }	

    /**
     * Wait for this shuffle completion.
     * This method blocks the calling thread until the shuffle is completed.
     */

    synchronized int waitForCompletion () {
	while ( ! doneflag ) {
	    try {
		wait() ;
	    } catch (InterruptedException e) {
	    }
	}
	return length ;
    }

    /**
     * Print a ShuffleHandler (for debugging).
     */

    public String toString() {
	return id + " " + doneflag ;
    }

    ShuffleHandler (FileDescriptor in, FileDescriptor out) {
	this.in  = in ;
	this.out = out ;
    }
}

/**
 * This class implements both a nice hack and some magic.
 * It uses an underlying <em>shuffler</em> process to speed up the sending
 * of big data files back to the client.
 * <p>The protocol between the server and the shuffler is quite simple, one
 * byte indicate the operation, which takes as argument two file descriptors.
 */

public class Shuffler {
    /**
     * The property giving the path of the shuffler server.
     * The shuffler server is an optional server helper, that deals with
     * serving resource contents. When resource contents can be efficiently
     * messaged between process boundaries (eg using sendmsg), the shuffler
     * server takes over the task of sending resource's content back to the 
     * client. This property gives the path of the shuffler server binary 
     * program.
     */
    public static final String SHUFFLER_P = "org.w3c.jigsaw.shuffler" ;

    private static Process shuffler = null ;
    private static boolean inited   = false ;
    private ShufflerThread waiter   = null ;
    private int fd = -1 ;

    private native int initialize (String path) ;
    private native synchronized int shuffle (ShuffleHandler h) ;
    private native int getNextMessage (ShuffleMessage msg) ;

    ShuffleMessage getNextMessage() {
	ShuffleMessage msg   = new ShuffleMessage () ;
	int duration = 2 ;

	while ( true ) {
	    int ecode = getNextMessage (msg) ;

	    if ( duration < 250 )
		duration = (duration << 1) ;
	    if ( ecode > 0 ) {
		return msg ;
	    } else if ( ecode == 0 ) {
		// yield and retyr (yes, this *is* pooling :=(
		try {
		    Thread.sleep (duration) ;
		} catch (InterruptedException e) {
		}
		Thread.yield() ;
	    } else if ( ecode < 0 ) {
		String m = (this.getClass().getName()
			    + "[getNextMessage]: failed (e="+ecode+")") ;
		throw new RuntimeException (m) ;
	    }
	}
    }

    /**
     * Initialize this class.
     * This deserve a special method, since we want any exception to be 
     * caught when invoking theinstance constructor.
     * <p>This method tries to launch the shuffler process automatically.
     * @param path The driectory for UNIX socket bindings.
     * @return A boolean <strong>true</strong> is every thing went fine, 
     *    <strong>false</strong> otherwise.
     */

    private synchronized boolean classInitialize (String path) {
	File socket = new File (path + "/shuffler") ;
	// Delete any old socket for this shuffler:
	socket.delete() ;
	// Load the native code:
	Runtime.getRuntime().loadLibrary ("Shuffle") ;
	inited = true ;
	// Get the shuffler binary path:
	String shuffler_bin = System.getProperty (SHUFFLER_P) ;
	if ( shuffler_bin == null )
	    return false ;
	// Run it:
	try {
	    String args[] = new String[2] ;
	    args[0]  = shuffler_bin ;
	    args[1]  = path + "/shuffler" ;
// This is intended for debug only, it makes the shuffler emit traces
// in the provided (-log) log file
//	    args[2] = "-v";
//	    args[3] = "-log";
//	    args[4] = "/nfs/usr/abaird/Jigsaw/logs/shuffler";
	    shuffler = Runtime.getRuntime().exec (args) ;
	} catch (Exception e) {
	    throw new RuntimeException (this.getClass().getName()
					+ "[classInitialize]: "
					+ "unable to launch shuffler.") ;
	}
	// Wait for the shuffler to create its listening socket:
	int timeout = 10000 ;
	while ( (timeout > 0) && (! socket.exists()) ) {
	    timeout -= 500 ;
	    try {
		Thread.sleep (500) ;
	    } catch (InterruptedException e) {
	    }
	}
	if ( ! socket.exists() ) {
	    throw new RuntimeException (this.getClass().getName()
					+ "[classInitialize]: "
					+ " didn't create its socket.");
	}
	return true ;
    }

    private int shuffle (FileDescriptor in, FileDescriptor out) 
	throws IOException
    {
	ShuffleHandler handle = new ShuffleHandler (in, out) ;
	// WARNING: code below shouldn't be changed it contains black magic
	// The thing is that the shuffler is really fast, and can even
	// finish its job before the waiter gets a chance to register the
	// appropriate identifier (so it gets a reply for an identifier it
	// doesn't know about).
	// Synchronizing the waiter makes the 'shuffle' + 'register' 
	// operations atomic with regard to the waiter thread, which is
	// what we want.
	// If you have understood above comment, then there is no more black
	// magic for you below.
	synchronized (waiter) {
	    if ( (handle.id = shuffle (handle)) < 0 )
		throw new IOException (this.getClass().getName() 
				       + " unable to shuffle !") ;
	    waiter.registerHandler (handle) ;
	}
	return handle.waitForCompletion () ;
    }

    /**
     * Shuffle the given rteply body to the given client.
     * This methods tries to outout the given reply 
     */

    public int shuffle (Client client, Reply reply)
	throws IOException
    {
	FileDescriptor in = reply.getInputFileDescriptor () ;
	if ( in == null )
	    return -1 ;
	// client.flushOutput() ;
	// FileDescriptor out  = client.getOutputFileDescriptor() ;
	// int written = shuffle (in, out) ;
	// if ( written < 0 )
	//     throw new IOException ("Shuffler failure.") ;
	// return written ;
	return -1;
    }

    public synchronized void shutdown() {
	// Kill the shuffler process (if needed) and waitfor its completion
	if ( shuffler != null ) {
	    shuffler.destroy() ;
	    while ( true ) {
		try {
		    shuffler.waitFor() ;
		    break ;
		} catch (InterruptedException ex) {
		}
	    }
	    shuffler = null ;
	}
	// Un-initialize, stop the waiter thread:
	inited   = false ;
	waiter.stop() ;
	waiter = null ;
    }

    /**
     * Create a new data shuffler.
     * The path identifies the directory in which UNIX socket will get bind.
     * This should be an absloute path, eg <code>/tmp/shuffler</code>.
     * @param path The path to the server.
     */

    public Shuffler (String path) {
	if ( ((! inited) && ( ! classInitialize (path)))
	     || (initialize (path) < 0) ) {
	    throw new RuntimeException (this.getClass().getName()
					+ ": unable to connect to shuffler "
					+ path) ;
	}
	this.waiter = new ShufflerThread (this) ;
	this.waiter.start() ;
    }

    // testing only

    public static void main (String args[])
	throws FileNotFoundException, IOException
    {
	Shuffler s = new Shuffler(args[0]) ;
	FileInputStream f = new FileInputStream ("from") ;
	FileOutputStream t = new FileOutputStream ("to") ;
	s.shuffle (f.getFD(), t.getFD()) ;
	f.close() ;
	t.close() ;
    }
}


