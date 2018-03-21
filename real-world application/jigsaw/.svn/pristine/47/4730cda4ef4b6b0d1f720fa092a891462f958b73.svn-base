// ServerHandler.java
// $Id: ServerHandler.java,v 1.1 2010/06/15 12:26:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.daemon;

import java.net.InetAddress;

import org.w3c.util.ObservableProperties;

import org.w3c.tools.resources.ResourceReference;

/**
 * A ServerHandler is a class that handles an accepting socket.
 */

public interface ServerHandler {

    /**
     * Get this server identifier.
     * @return A String identifying this server context.
     */

    public String getIdentifier();

    /**
     * Log an error into this server's error log.
     * @param msg The message to log.
     */

    public void errlog(String msg) ;

    /**
     * Log a normal message into this server's log.
     * @param msg The message to log.
     */

    public void log(String msg) ;

    /**
     * Emit a debugging trace on behalf of this server.
     * @param msg The trace to emit.
     */

    public void trace(String msg);

    /**
     * Get the network addresses on which this server is listening.
     * @return The InetAddress this server is listening to.
     */

    public InetAddress getInetAddress();

    /**
     * Get the root, configuration resource for that server.
     * @return A ContainerResource instance.
     */

    public ResourceReference getConfigResource();

    /**
     * Clone this server handler, and custmozie it with the given properties.
     * <p>Once cloned, the new server is assumed to be running happily,
     * as if it had been initialized.
     * @param shm The global server handler manager.
     * @param identifier The new ServerHandler identifier.
     * @param props The properties that overide part of the configuration of
     * the cloned server.
     * @return A newly created server, <em>sharing</em> the configuration
     * of the cloned server, except for the config options defined by
     * the given property set.
     * @exception ServerHandlerInitException if initialization failed.
     */

    public ServerHandler clone(ServerHandlerManager shm
			       , String identifier
			       , ObservableProperties props)
	throws ServerHandlerInitException;

    /**
     * Initialize the server from the given set of properties.
     * This method is called by the ServerManager instance, when launching
     * the appropriate servers.
     * <p>A Server instance that has initialize itself successfully is
     * considered to be running.
     * @param shm The global server handler manager.
     * @param identifier A String identifying the server.
     * @param props The property set this server should use to initialize
     * itself.
     * @exception ServerHandlerInitException if initialization failed.
     */

    public void initialize(ServerHandlerManager shm
			   , String id
			   , ObservableProperties props)
	throws ServerHandlerInitException;

   
    /**
     * Start the server, after everything has been initialized.
     * itself.
     * @exception ServerHandlerInitException if initialization failed.
     */
    public void start()
	throws ServerHandlerInitException;

   

    /**
     * Shutdown this server handler.
     * This is a synchronous method, that will return only once the server
     * has been shutdown entirely (all the resources it uses have been 
     * released).
     * <p>This server handler clones are considered shutdown too.
     */

    public void shutdown();

}
