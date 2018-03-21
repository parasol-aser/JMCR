// ServerInterface.java
// $Id: ServerInterface.java,v 1.1 2010/06/15 12:20:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.net.URL;
import java.io.File;
import org.w3c.util.ObservableProperties;
import org.w3c.jigsaw.daemon.ServerHandler;
import org.w3c.tools.resources.indexer.IndexersCatalog;
import org.w3c.tools.resources.store.ResourceStoreManager;

//FIXME ServerHandler should be in this package
public interface ServerInterface extends ServerHandler {

    /**
     * Another nice way of reporting errors from a Resource.
     * @param from The resource that trigered the error.
     * @param msg The error message.
     */
    public void errlog(Resource from, String msg);

    /**
     * Lookup in the root entry for some resource.
     * @param name The name of the resource to lookup in the root entry.
     * @return The loaded resource, or <strong>null</strong>.
     */
    public ResourceReference loadResource(String name);

    /**
     * Checkpoint all cached data, by saving them to disk.
     */
    public void checkpoint();

    /**
     * Dynamically change the root resource for the server.
     * This is kind a dangerous operation !
     * @param name The name of the new root resource, to be found in the
     * root entry.
     * @return The new installed root resource, or <strong>null</strong>
     * if we couldn't load the given resource.
     */
    public ResourceReference loadRoot(String name);

    /** 
     * Get this server properties.
     */
    public ObservableProperties getProperties();

    /**
     * Is the underlying file-system case sensitive ?
     * @return A boolean, <strong>true</strong> if file system is case 
     * sensitive, <strong>false</strong> otherwise.
     */
    public boolean checkFileSystemSensitivity();

    /**
     * Get the full URL of Jigsaw's documentation.
     * @return A String encoded URL.
     */
    public String getDocumentationURL();

    /**
     * Get the tracsh directory
     */
    public String getTrashDirectory();

    /**
     * Get the client's debug flags from the properties.
     */
    public boolean getClientDebug();

    /**
     * Does this server wants clients to try keeping connections alive ?
     */
    public boolean getClientKeepConnection();

    /**
     * Get the request allowed time slice from the properties.
     */
    public int getRequestTimeOut();

    /**
     * Get the connection allowed idle time from the properties.
     */
    public int getConnectionTimeOut();

    /**
     * Get the client's threads priority from the properties.
     */
    public int getClientThreadPriority();

    /**
     * Get the client's buffer size.
     */
    public int getClientBufferSize();

    /**
     * Get this server host name.
     */
    public String getHost();

    /**
     * Get this server port number.
     */
    public int getPort ();

    /**
     * Get the server current root resource.
     */
    public FramedResource getRoot();

    /**
     * Get the server URL.
     */
    public URL getURL();

    /**
     * Get the server software string.
     */
    public String getSoftware();

    /**
     * Get the server local port
     */
    public int getLocalPort();

    /**
     * Get this server root directory.
     */
    public File getRootDirectory();

    /**
     * Get this server config directory.
     */
    public File getConfigDirectory();

    /**
     * Get this server authentication directory.
     */
    public File getAuthDirectory();

    /**
     * Get this server store directory.
     */
    public File getStoreDirectory();

    public File getIndexerDirectory();

    /**
     * Get temp directory
     */
    public File getTempDirectory();

    public IndexersCatalog getIndexersCatalog();

    /**
     * Get this server resource space.
     */
    public ResourceSpace getResourceSpace();

    /**
     * Get the default resource context for that server.
     */
    public ResourceContext getDefaultContext();

    /**
     * Get the Resource store manager of this server
     */
    public ResourceStoreManager getResourceStoreManager();
    
    /**
     * Perform the given request on behalf of this server.
     * @param request The request to perform.
     * @return A non-null Reply instance.
     * @exception ProtocolException If some error occurs during processing the
     * request.
     * @exception ResourceException If some error not relative to the 
     * protocol occurs.
     */
    public ReplyInterface perform(RequestInterface request)
	throws ProtocolException, ResourceException;

}
