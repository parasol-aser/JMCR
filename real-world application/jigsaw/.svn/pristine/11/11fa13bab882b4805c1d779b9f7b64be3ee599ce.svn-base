// MuxHttpHandler.java
// $Id: MuxHttpHandler.java,v 1.1 2010/06/15 12:24:58 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.mux;

import java.io.IOException;

import org.w3c.util.ThreadCache;

import org.w3c.www.mux.MuxProtocolHandler;
import org.w3c.www.mux.MuxSession;

import org.w3c.jigsaw.http.httpd;

public class MuxHttpHandler implements MuxProtocolHandler {
    protected httpd         server  = null;
    protected int           cid     = -1;

    protected int clientcount = 0;
    protected int maxclient   = 50;

    protected ThreadCache   threadcache = null;

    protected MuxClient freelist = null;

    private final synchronized MuxClient createClient() {
	clientcount++;
	return new MuxClient(server, this, ++cid);
    }

    protected synchronized void markIdle(MuxClient client) {
	client.next = freelist;
	freelist    = client;
	notifyAll();
    }

    protected synchronized MuxClient getClient() {
	MuxClient client = null;
	while ( true ) {
	    if ( freelist != null ) {
		// Free client already available:
		client   = freelist;
		freelist = client.next;
		break;
	    } else if ( clientcount+1 < maxclient ) {
		// We're allowed to create new clients
		client = createClient();
		break;
	    } else {
		// Wait for a free client
		try {
		    wait();
		} catch (InterruptedException ex) {
		}
	    }
	}
	return client;
    }

    public void initialize(MuxSession session) 
	throws IOException
    {
	// Find an idle MuxClient, bind and run it:
	MuxClient client = getClient();
	client.bind(session);
	threadcache.getThread(client, true);
    }

    public MuxHttpHandler(httpd server) {
	this.server      = server;
	this.threadcache = new ThreadCache("mux-clients");
	this.threadcache.setCachesize(10);
	this.threadcache.setThreadPriority(server.getClientThreadPriority());
	this.threadcache.initialize();
    }

}
