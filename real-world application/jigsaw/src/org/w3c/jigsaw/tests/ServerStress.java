// ServerStress.java
// $Id: ServerStress.java,v 1.1 2010/06/15 12:29:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.tests ;

import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Vector;

import org.w3c.tools.timers.EventHandler;
import org.w3c.tools.timers.EventManager;

class URLStat {
    URL  url    = null ;
    long time   = 0 ;
    int  count  = 0 ;
    int  size   = 0 ;
    int  failed = 0 ;

    public synchronized void update(int size, long time) {
	if ((this.size == 0) && (size > 0)) {
	    this.size = size ;
	    this.time = time ;
	} else if ( this.size != size ) {
	    failed++ ;
	} else {
	    this.time += time ;
	    this.count++ ;
	}
    }

    public synchronized void display(PrintStream out) {
	out.println(url.toExternalForm()
		    + ": s=" + size
		    + " t=" + (((double) time/1000.0) / ((double) count))
		    + " f=" + failed
		    + " t=" + time
		    + " c=" + count) ;
    }

    public URL getURL() {
	return url;
    }

    URLStat(URL url) {
	this.url   = url ;
	this.time  = 0 ;
	this.count = 0 ;
    }
	
}

class URLGetter extends Thread {
    Stresser s = null ;

    /**
     * Keep picking an URL until the stresser tells us to stop.
     * Each time a full URL has been fetched, notify the stresser, and try
     * continuing.
     */

    public void run() {
	URLStat u = null ;

	try {
	    while ((u = s.pickURL()) != null) {
		int  got      = 0 ;
		int  size     = 0 ;
		byte buffer[] = new byte[8192] ;
		
		long tstart = System.currentTimeMillis() ;
		try {
		    URLConnection conn = u.getURL().openConnection() ;
		    InputStream   in   = conn.getInputStream() ;
		    while ((got = in.read(buffer)) > 0) 
			size += got ;
		    in.close() ;
		} catch (Exception ex) {
		    size = -1 ;
		}
		s.notifyDone(u, size, System.currentTimeMillis() - tstart) ;
	    }
	} catch (Exception ex) {
	}
	s.notifyEnd(this) ;
    }

    URLGetter(Stresser s) {
	this.s = s ;
    }
}

class Stresser extends Thread implements EventHandler {
    private final boolean debug = false ;
    long      duration  = -1 ;
    int       parallel  = 10 ;
    Vector    stats     = null ;
    URLGetter getters[] = null ;
    boolean   done      = false ;

    EventManager manager = null ;

    /**
     * Statistics
     */
    public long total_request = 0 ;
    public long total_time    = 0 ;

    public synchronized void handleTimerEvent(Object data, long time) {
	if ( debug )
	    System.out.println("shuting down...") ;
	done = true ;
    }

    public synchronized void notifyDone(URLStat u, int size, long time) {
	u.update(size, time) ;
	total_request++ ;
    }

    public synchronized void notifyEnd(URLGetter getter) {
	parallel-- ;
	notifyAll() ;
    }

    public int url_counter = 0 ;

    public synchronized URLStat pickURL() {
	if ( done )
	    return null ;
	url_counter = (url_counter + 1) % stats.size() ;
	return (URLStat) stats.elementAt(url_counter) ;
    }

    public synchronized void waitForCompletion() {
	while ( parallel != 0 ) {
	    try {
		wait() ;
	    } catch (InterruptedException ex) {
	    }
	    if ( debug )
		System.out.println("waiting for "+parallel+" getters.");
	}
    }

    /**
     * Start all URL getters, until the duration has expired.
     */

    public void run() {
	long tstart = System.currentTimeMillis() ;
	// Start all getters:
	for (int i = 0 ; i < parallel ; i++) 
	    getters[i].start() ;
	// Wait for termination:
	waitForCompletion() ;
	// Display results:
	long tend = System.currentTimeMillis() ;
	System.out.println("Total request: "+total_request) ;
	System.out.println("Total time   : "+(tend-tstart)) ;
	System.out.println("Req/sec      : "
			   +(((double)total_request)/((double)(tend-tstart)))*1000);
	System.out.println("detailed results:") ;
	for (int i = 0 ; i < stats.size() ; i++) {
	    ((URLStat) stats.elementAt(i)).display(System.out) ;
	}
    }

    Stresser(Vector urls, long duration, int parallel) {
	// Initialize instance vars:
	this.duration = duration ;
	this.parallel = parallel ;
	// Create the getters:
	this.getters  = new URLGetter[parallel] ;
	for (int i = 0 ; i < parallel ; i++) 
	    getters[i] = new URLGetter(this) ;
	// Create the url stat objects:
	this.stats = new Vector(urls.size()) ;
	for (int i = 0 ; i < urls.size() ; i++) 
	    stats.addElement(new URLStat((URL) urls.elementAt(i))) ;
	// Create an event manager, and register for event at end of duration
	this.manager  = new EventManager();
	this.manager.setDaemon(true) ;
	this.manager.start() ;
	this.manager.registerTimer(duration, this, null) ;
    }

}

/**
 * Stress a web server for a given duration of time.
 * This program will run a constant rate of requests to a given server, 
 * for a given duration of time. 
 * <p><b>This test results should be taken with care</b>: they depend on a 
 * number of things, and usually run slower then C-written tests. They are
 * included in Jigsaw release for testing only (not for real benchmarking,
 * altough you are free to doi whatever you want with the numbers you get).
 */

public class ServerStress {

    public static void usage() {
	PrintStream o = System.out ;
	o.println("usage: ServerStress <options>") ;
	o.println("-d duration: duration of test in ms.") ;
	o.println("-p parallel: number of parallel requests to run.") ;
	o.println("-u url     : add URL to list of url to visit.");
	o.println("-f file    : add all URL in file (one per line)") ;
	System.exit(1) ;
    }

    public static void main (String args[]) {
	Vector urls = new Vector() ;		// List of urls to visit
	long   duration = 60000 ;		// Duration in ms.
	int    par      = 10 ;			// # of parallel reqs to run
	String filename = null ;

	// Parse the command line:
	for (int i = 0 ; i < args.length ; i++) {
	    if (args[i].equals("-d") && (i+1 < args.length)) {
		try {
		    duration = Integer.parseInt(args[++i]) ;
		} catch (Exception ex) {
		    usage() ;
		}
	    } else if (args[i].equals("-u") && (i+1 < args.length)) {
		try {
		    urls.addElement(new URL(args[++i])) ;
		} catch (Exception ex) {
		    usage() ;
		}
	    } else if (args[i].equals("-p") && (i+1 < args.length)) {
		try {
		    par = Integer.parseInt(args[++i]) ;
		} catch (Exception ex) {
		    usage() ;
		}
	    } else if (args[i].equals("-f") && (i+1 < args.length)) {
		filename = args[++i] ;
	    } else {
		usage() ;
	    }
	}
	// Update URL list with file if needed:
	if ( filename != null ) {
	    try {
		String      line = null ;
		DataInputStream in   = (new DataInputStream
					(new BufferedInputStream
					 (new FileInputStream(filename)))) ;
		while ((line = in.readLine()) != null) 
		    urls.addElement(new URL(line)) ;
		in.close() ;
	    } catch (Exception ex) {
		System.out.println("Unable to read URL file "+filename) ;
		ex.printStackTrace() ;
		System.exit(1) ;
	    }
	}
	// Build the stresser:
	Stresser s = new Stresser(urls, duration, par) ;
	s.start() ;
    }

}


