// MuxTest.java
// $Id: MuxTest.java,v 1.1 2010/06/15 12:24:57 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

import org.w3c.www.mux.MuxSession;
import org.w3c.www.mux.MuxStream;
import org.w3c.www.mux.MuxStreamHandler;
import org.w3c.www.mux.SampleMuxHandler;

class DiscardServer extends Thread {
    ServerSocket     socket  = null;
    MuxStreamHandler handler = null;
    int              port    = -1;

    public void run() {
	try {
	    while(true) {
		System.out.println("DiscardServer@localhost:"+port+" accept.");
		Socket client = socket.accept();
		new MuxStream(true, handler, client);
	    }
	} catch (Exception ex) {
	    System.out.println("DiscardServer: fatal error !");
	    ex.printStackTrace();
	}
    }

    public DiscardServer(int port)
	throws IOException
    {
	this.handler = SampleMuxHandler.getStreamHandler();
	this.socket  = new ServerSocket(port);
	this.port    = port;
	setName("DiscardServer");
	this.start();
    }

}

class MuxTestClient extends Thread {
    static Socket    socket = null;
    static MuxStream stream = null;

    int count = 0;
    int bufsize = 0;
    byte buffer[] = null;
    int total = 0;
    InputStream in = null;
    OutputStream out = null;

    public static void makeClient(String host, int port
				  , int bufsize, int count) 
	throws IOException
    {
	if ( socket == null ) {
	    socket = new Socket(host, port);
	    stream = new MuxStream(false, null, socket);
	}
	new MuxTestClient(stream.connect(9), bufsize, count);
    }

    public void run() {
	System.out.println(this+" bs="+bufsize+", cn="+count);
	try {
	    long start = System.currentTimeMillis();
	    while (--count >= 0) 
		out.write(buffer, 0, buffer.length);
	    out.close();
	    out.flush();
	    long end = System.currentTimeMillis();
	    System.out.println(this
			       + ": emited "+total+" bytes "
			       + " in "+(end-start)+" ms.");
	} catch (Exception ex) {
	    System.out.println(this+": failed");
	    ex.printStackTrace();
	}
    }

    MuxTestClient(MuxSession session, int bufsize, int count) 
	throws IOException
    {
	this.in      = session.getInputStream();
	this.out     = session.getOutputStream();
	this.bufsize = bufsize;
	this.count   = count;
	this.buffer  = new byte[bufsize];
	this.total   = bufsize*count;
	setName("client-"+session.getIdentifier());
	start();
    }

}

public class MuxTest {

    public static void usage() {
	System.out.println("-h host -p port -t bufsize count>*");
	System.out.println("-s (server) -p port");
    }

    public static void main(String args[]) {
	boolean server = false;
	String host = null;
	int port = -1;

	for (int i = 0 ; i < args.length ; i++) {
	    if ( args[i].equals("-s") ) {
		server = true;
	    } else if (args[i].equals("-h")) {
		host = args[++i];
	    } else if (args[i].equals("-p")) {
		try {
		    port = Integer.parseInt(args[++i]);
		} catch (Exception ex) {
		    usage();
		}
	    } else if (args[i].equals("-t")) {
		if ((port < 0) || (host == null))
		    usage();
		try {
		    int bufsize = Integer.parseInt(args[++i]);
		    int count   = Integer.parseInt(args[++i]);
		    MuxTestClient.makeClient(host, port, bufsize, count);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	if ( server ) {
	    try {
		new DiscardServer(port);
	    } catch(Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

}
