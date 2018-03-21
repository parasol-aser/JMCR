// ClientFactory.java
// $Id: ClientFactory.java,v 1.1 2010/06/15 12:21:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

public interface ClientFactory {

    /**
     * Shutdown this client factory.
     * @param force If <strong>true</strong>, force the shutdown, and stop
     * all operations in progress.
     */

    public void shutdown(boolean force);

    /**
     * Handle the given, new connection.
     * @param socket The newly accepted connection.
     */

    public void handleConnection(Socket socket);

    /**
     * Initialize that client factory.
     * Initialize this new client factory, and create the <em>server 
     * socket</em> (ie the socket that will be used to accept new connections.
     * @param server The httpd instance to be used as the context for
     * this client factory.
     */

    public void initialize(httpd server);

    /**
     * Create the server socket for this client factory.
     * This method is always called <em>after</em> the initialize method
     * of the client factory is done.
     * @return A server socket instance. 
     * @exception IOException If some IO error occurs while creating the
     * server socket.
     */

    public ServerSocket createServerSocket() 
	throws IOException;

}
