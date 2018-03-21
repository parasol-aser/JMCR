// Discard.java
// $Id: Discard.java,v 1.1 2010/06/15 12:30:01 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.www.mux.MuxProtocolHandler;
import org.w3c.www.mux.MuxSession;

/**
 * The <em>discard</em> protocol handler.
 */

public class Discard extends Thread implements MuxProtocolHandler {
    /**
     * Debug flag.
     */
    private static final boolean debug = true;
    /**
     * Trace flag (will emit a line per discarded packet)
     */
    private static final boolean trace = false;
    InputStream  in      = null;
    OutputStream out     = null;
    MuxSession   session = null;

    /**
     * Run the <em>discard</em> protocol, can't be easier !
     */

    public void run() {
	byte buffer[] = new byte[1024];
	int  got      = -1;
	try {
	    long tstart = System.currentTimeMillis();
	    int  recv   = 0;
	    int  nread  = 0;   
	    while ((got = in.read(buffer, 0, buffer.length)) > 0) {
		recv += got;
		nread++;
		if ( trace )
		    System.out.println("discard: "+nread+" "+got+" bytes.");
	    }
	    long tend = System.currentTimeMillis();
	    if ( debug ) {
		System.out.println("discard: recv="+recv
				   + ", reads="+nread
				   + ", bytes/read="
				   + (recv/nread)
				   + ", bytes/sec="
				   + (recv/((tend-tstart)/1000))+".");
	    }
	    session.shutdown();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Initialize the <em>echo</em> protocol on that session.
     * @param session The session to use to speak that protocol.
     */

    public void initialize(MuxSession session) 
	throws IOException
    {
	this.in      = session.getInputStream();
	this.out     = session.getOutputStream();
	this.session = session;
	start();
    }

    /**
     * Default public constructor.
     */

    public Discard() {
	super();
	setName("discard");
    }

}


