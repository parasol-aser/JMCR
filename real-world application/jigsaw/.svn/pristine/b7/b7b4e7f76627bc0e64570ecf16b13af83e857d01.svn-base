// HttpMuxConnection.java
// $Id: HttpMuxConnection.java,v 1.1 2010/06/15 12:25:14 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.io.IOException;

import java.net.Socket;

import org.w3c.www.mux.MuxSession;
import org.w3c.www.mux.MuxStream;
import org.w3c.www.mux.MuxStreamHandler;

class HttpMuxConnection extends HttpConnection {
    Socket        socket = null;
    MuxStream     stream = null;
    HttpMuxServer server = null;
    int refcount = 0;

    protected synchronized boolean incrUseCount() {
	boolean ret = (refcount == 0);
	refcount++;
	return ret;
    }

    protected synchronized boolean decrUseCount() {
	return (--refcount == 0);
    }

    public void close() {
	try {
	    stream.shutdown(true);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    public final MuxSession connect(int protid) 
	throws IOException
    {
	return stream.connect(protid);
    }

    HttpMuxConnection(HttpMuxServer server, String host, int port) 
	throws IOException
    {
	this.server = server;
	this.socket = new Socket(host, port);
	this.stream = new MuxStream(false, null, socket);
    }

}
