// Echo.java
// $Id: Echo.java,v 1.1 2010/06/15 12:30:02 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mux.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.www.mux.MuxProtocolHandler;
import org.w3c.www.mux.MuxSession;

/**
 * The <em>echo</em> protocol handler.
 */

public class Echo extends Thread implements MuxProtocolHandler {
    InputStream  in      = null;
    OutputStream out     = null;
    MuxSession   session = null;

    /**
     * Run that protocol, can't be easier !
     */

    public void run() {
	byte buffer[] = new byte[1024];
	int  got      = -1;
	try {
	    while ((got = in.read(buffer, 0, buffer.length)) > 0) {
		out.write(buffer, 0, got);
		out.flush();
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

    public Echo() {
	super();
	setName("echo");
    }

}


