// ResourceIndexer.java
// $Id: ResourceIndexer.java,v 1.1 2010/06/15 12:28:02 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.indexer ;

import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;

/**
 * Jigsaw indexer.
 * The indexer is an object that given some global configuration informations, 
 * tries to build default resources for files that the server doesn't know
 * about.
 * A ResourceIndexer <strong>must</strong> be a resource it is to be added
 * permanently to the IndexersCatalog.
 * @see IndexersCatalog
 */

public interface  ResourceIndexer {

    /**
     * When was this indexer configuration last modified.
     * @return The date at which that indexer was last modified, as 
     * a number of milliseconds since Java epoch.
     */

    abstract public long lastModified();

    /**
     * Try to create a resource for the given file.
     * This method makes its best efforts to try to build a default
     * resource out of a file. 
     * @param container The container making the call.
     * @param request The HTTP request that triggered the call to the indexer
     *    (may be <strong>null</strong>).
     * @param directory The directory the file is in.
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */

    abstract public Resource createResource(ContainerResource container,
					    RequestInterface request,
					    File directory,
					    String name,
					    Hashtable defs);

    /**
     * Get the name of the resource relative to the given filename.
     * @param name The name of the file.
     * @return a String, the resource name.
     */
    abstract public String getIndexedName(File directory, String name);

}
