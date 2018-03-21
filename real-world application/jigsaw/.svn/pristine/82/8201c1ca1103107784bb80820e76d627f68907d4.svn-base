// ZipDirectoryResource.java
// $Id: ZipDirectoryResource.java,v 1.2 2010/06/15 17:53:04 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.zip;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.File;
import java.io.IOException;

import org.w3c.util.EmptyEnumeration;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ResourceSpace;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.tools.resources.indexer.ResourceIndexer;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ZipDirectoryResource extends DirectoryResource {

    /**
     * Attributes index - The filename attribute.
     */
    protected static int ATTR_ZIPFILE = -1 ;
    /**
     * Attribute index - The index for our entry path.
     */
    protected static int ATTR_ENTRYPATH = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class.
	try {
	    cls = Class.forName("org.w3c.jigsaw.zip.ZipDirectoryResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The zip file attribute.
	a = new FileAttribute("zipfile"
			      , null
			      , Attribute.COMPUTED) ;
	ATTR_ZIPFILE = AttributeRegistry.registerAttribute(cls, a) ;
	// the entry path attribute
	a = new StringAttribute("entrypath"
				, null
				, Attribute.COMPUTED) ;
	ATTR_ENTRYPATH = AttributeRegistry.registerAttribute(cls, a) ;
    }

    protected Hashtable directories = new Hashtable(3);

    /**
     * Get this zip file.
     */
    public synchronized File getZipFile() {
	return (File) getValue(ATTR_ZIPFILE, getDirectory());
    }


    public String getEntryPath() {
	return getString(ATTR_ENTRYPATH, null);
    }

    /**
     * Enumerate all available children resource identifiers. 
     * This method <em>requires</em> that we create all our pending resources.
     * @return An enumeration of all our resources.
     */
    protected synchronized Enumeration enumerateAllResourceIdentifiers() {
	return enumerateResourceIdentifiers(true);
    }

    /**
     * Reindex recursivly all the resources from this DirectoryResource.
     */
    public synchronized void reindex() {
	//no reindex here
    }

    /**
     * Initialize and register a new resource into this directory.
     * @param resource The uninitialized resource to be added.
     */
    protected ResourceContext updateDefaultChildAttributes(Hashtable attrs) {
	//fixme
	attrs.put("zipfile", getZipFile());
	String entrypath = null;
	if (getEntryPath() != null) {
	    entrypath = getEntryPath()+"/"+((String) attrs.get(id));
	} else {
	    entrypath = (String) attrs.get(id);
	}
	attrs.put("entrypath", entrypath);
	return super.updateDefaultChildAttributes(attrs);
    }

    /**
     * Enumerate all available children resource identifiers. 
     * This method <em>requires</em> that we create all our pending resources
     * if we are in the extensible mode...too bad !
     * @return An enumeration of all our resources.
     */
    public synchronized Enumeration enumerateResourceIdentifiers(boolean all) {
	File zipfile = getZipFile();
	ZipFile zip = null;
	Hashtable lookuped = new Hashtable(20);
	try {
	    zip = new ZipFile(zipfile);
	} catch (Exception ex) {
	    return new EmptyEnumeration();
	}
	Enumeration entries    = zip.entries();
	ZipEntry    entry      = null;
	String      entry_name = null;
	int         idx        = -1;
	String      entry_path = getEntryPath();
	if (entry_path == null) {
	    while (entries.hasMoreElements()) {
		entry = (ZipEntry) entries.nextElement();
		entry_name = entry.getName();
		idx = entry_name.indexOf('/');
		if (idx != -1) {
		    entry_name = entry_name.substring(0,idx);
		    directories.put(entry_name, Boolean.TRUE);
		}
		if (lookuped.get(entry_name) != null)
		    continue;
		lookuped.put(entry_name, Boolean.TRUE);
		if (lookup(entry_name) == null) {
		    createDefaultResource(entry_name);
		}
	    }
	} else {
	    int startidx = entry_path.length();
	    while (entries.hasMoreElements()) {
		entry = (ZipEntry) entries.nextElement();
		entry_name = entry.getName();
		if (! entry_name.startsWith(entry_path))
		    continue;
		// +1 remove "/"
		if (entry_name.length() > startidx+1)
		    entry_name = entry_name.substring(startidx+1);
		else continue;
		idx = entry_name.indexOf('/');
		if (idx != -1) {
		    entry_name = entry_name.substring(0,idx);
		    if (entry_name.length() == 0)
			continue;
		    directories.put(entry_name, Boolean.TRUE);
		} 
		if (lookuped.get(entry_name) != null)
		    continue;
		lookuped.put(entry_name, Boolean.TRUE);
		if (lookup(entry_name) == null) {
		    createDefaultResource(entry_name);
		}
	    }
	}
	try { zip.close(); } catch (Exception ex) {}
	ResourceSpace space = getSpace();
	acquireChildren();
	return space.enumerateResourceIdentifiers( getChildrenSpaceEntry() );
    }

    protected boolean entryExists(String name) {
	File zipfile = getZipFile();
	ZipFile zip = null;
	try {
	    zip = new ZipFile(zipfile);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return false;
	}
	String entry_path = getEntryPath();
	String full_path = null;
	if (entry_path != null) {
	    if (entry_path.endsWith("/"))
		full_path = entry_path  + name;
	    else
		full_path = entry_path + "/" + name;
	} else {
	    full_path = name;
	}

	try {
	    return ((zip.getEntry(full_path) != null) ||
		    (zip.getEntry(full_path+"/") != null));
	} finally {
	    try { zip.close(); } catch (Exception ex) {}
	}
    }

    /**
     * Index a Resource. Call the indexer.
     * @param name The name of the resource to index.
     * @param defs The defaults attributes.
     * @param req The protocol request.
     * @return A resource instance.
     * @see org.w3c.tools.resources.indexer.SampleResourceIndexer
     */
    protected Resource index(String name, 
			     Hashtable defs, 
			     RequestInterface req) 
    {
	if (! entryExists(name))
	    return null;
	// Prepare a set of default parameters for the resource:
	defs.put(id, name);
	updateDefaultChildAttributes(defs);
	ResourceContext context = getContext();
	// Try to get the indexer to create the resource:
	Resource    resource = null;
	ResourceReference rr_indexer  = null;
	ResourceReference rr_lastidx  = null;
	while ( context != null ) {
	    // Lookup for next indexer in hierarchy:
	    do {
		rr_indexer = getIndexer(context);
		context = context.getParent();
	    } while ((rr_indexer == rr_lastidx) && (context != null));
	    // Is this a usefull indexer ?
	    if ((rr_lastidx = rr_indexer) != null ) {
		try {
		    ResourceIndexer indexer = 
			(ResourceIndexer)rr_indexer.lock();
		    if (! (indexer instanceof ZipIndexer))
			return null;
		    ZipIndexer zindexer = (ZipIndexer) indexer;
		    if (directories.get(name) != null) {
			resource = 
			    zindexer.createDirectoryResource(getDirectory(),
							     name,
							     defs);
		    } else {
			resource = zindexer.createFileResource(getDirectory(),
							       name,
							       defs);
		    }
		    if ( resource != null ) 
			break;
		} catch (InvalidResourceException ex) {
		    resource = null;
		} finally {
		    rr_indexer.unlock();
		}
	    }
	}
	return resource;
    }

    /**
     * Create a Resource and the physical file too.
     * @param name the name of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference createResource(String name) {
	return null;
    }

    /**
     * Create a DirectoryResource and the physical directory too.
     * @param name the name of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference createDirectoryResource(String name) {
	return null;
    }

    public void initialize(Object values[]) {
	super.initialize(values);
	disableEvent();
	if (getZipFile() != null)
	    setValue(ATTR_DIRECTORY, getZipFile()) ;
	enableEvent();
    }



public synchronized boolean hasEntry() {
        ZipFile z = null;
        try {
            z = new ZipFile(getZipFile());
            return (z.getEntry(getEntryPath()) != null);
        } catch (IOException ex) {
            return false;
        } finally {
            try { z.close(); } catch (Exception ex) {}
        }
    }


}
