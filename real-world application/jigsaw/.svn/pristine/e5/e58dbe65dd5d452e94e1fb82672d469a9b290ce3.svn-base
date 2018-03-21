// MuxClientFactory.java
// $Id: MuxClientFactory.java,v 1.1 2010/06/15 12:24:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.mux;

import java.io.IOException;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

import org.w3c.www.mux.MuxProtocolHandler;
import org.w3c.www.mux.MuxStream;
import org.w3c.www.mux.SampleMuxHandler;

import org.w3c.jigsaw.http.ClientFactory;
import org.w3c.jigsaw.http.httpd;

public class MuxClientFactory implements ClientFactory {
    public static final int HTTP_PORT = 80;

    httpd              server  = null;
    int                cid     = -1;
    SampleMuxHandler   handler = null;
    MuxProtocolHandler httphandler = null;

    public void shutdown(boolean force) {
	System.out.println("Shutdown: not implemented.");
    }

    /**
     * Handle that new incomming connection.
     * Wrap the given socket into a MuxStream, the rest is handled magically.
     * @param socket The newly accepted socket.
     */

    public void handleConnection(Socket socket) {
	try {
	    new MuxStream(true
			  , handler
			  , socket);
	} catch (IOException ex) {
	    server.errlog(getClass().getName()
			  +": rejected newly accepted connection (IOerror): "
			  + ex.getMessage());
	}
    }

    /**
     * Create a suitable server socket for our server context.
     * @return A ServerSocket instance.
     * @exception IOException If some IO error occured while creating the
     * server socket.
     */

    public ServerSocket createServerSocket() 
	throws IOException
    {
	return new ServerSocket(server.getPort(), 128);
    }

    /**
     * Initialize the MUX client factory.
     * @param server The server context in which this factory is to run.
     */

    public void initialize(httpd server) {
	// Initialize the server itself:
	this.server  = server;
	// Configure the mux stream handler:
	handler     = (SampleMuxHandler) SampleMuxHandler.getStreamHandler();
	httphandler = new MuxHttpHandler(server);
	handler.registerHandler(HTTP_PORT, httphandler);
    }

    public MuxClientFactory() {
	super();
    }

}
