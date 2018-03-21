// GhostResourceIndexer.java
// $Id: GhostResourceIndexer.java,v 1.1 2010/06/15 12:28:02 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.indexer;

import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;

public class GhostResourceIndexer extends SampleResourceIndexer {

    /**
     * Try to create a resource for the given file.
     * This method makes its best efforts to try to build a default
     * resource out of a file. 
     * @param directory The directory the file is in.
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */

    public Resource createResource(ContainerResource container
				   , RequestInterface request
				   , File directory
				   , String name
				   , Hashtable defs) {
	File file = new File(directory, name) ;
	// Okay, dispatch on wether it is a file or a directory.
	Resource result = null;
	if ( file.isDirectory() )
	    result = createDirectoryResource(directory, request, name, defs) ;
	else
	    result = createFileResource(directory, request, name, defs) ;

	if (result == null)
	    result = createVirtualResource(directory, request, name, defs) ;
	if ( result != null )
	    return result;
	// Try the super indexer if available:
	String superIndexer = getSuperIndexer();
	if ( superIndexer == null )
	    return null;
	IndexerModule m = null;
	m = (IndexerModule) getContext().getModule(IndexerModule.NAME);
	ResourceReference rri = m.getIndexer(superIndexer);
	if (rri == null)
	    return null;
	try {
	    ResourceIndexer p = (ResourceIndexer)rri.lock();
	    return ((p != null) 
		    ? p.createResource(container, request, directory, 
				       name, defs)
		    : null);
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rri.unlock();
	}
    }

}
