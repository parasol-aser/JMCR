// ContentTypeIndexer.java
// $Id: ContentTypeIndexer.java,v 1.2 2010/06/15 17:53:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.indexer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import java.io.File;

import org.w3c.tools.resources.indexer.SampleResourceIndexer;
import org.w3c.tools.resources.indexer.TemplateContainer;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;

import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

class ContentTypesIndexerEnumeration implements Enumeration {
    private static final String list[] = {
	"directories".intern(),
	"extensions".intern(),
	"content-types".intern()
    };

    int idx = 0;

    public boolean hasMoreElements() {
	return idx < list.length;
    }

    public Object nextElement() {
	if ( idx >= list.length )
	    throw new NoSuchElementException("ContentTypesIndexer enum");
	return list[idx++];
    }

    ContentTypesIndexerEnumeration() {
	this.idx = 0;
    }

}

public class ContentTypeIndexer extends SampleResourceIndexer {
    // usual debug stuff
    private static final boolean debug = false;

    protected ResourceReference contentTypes  = null;

    public Enumeration enumerateResourceIdentifiers(boolean all) {
	return new ContentTypesIndexerEnumeration();
    }

    protected synchronized ResourceReference getContentTypes() {
	if ( contentTypes == null ) {
	    String typesidxid = getIdentifier()+"-c";
	    contentTypes = new DummyResourceReference(
			      new TemplateContainer(
				    new ResourceContext(getContext()),
				    typesidxid+".db"));
	}
	return contentTypes;
    }

    public ResourceReference lookup(String name) {
	if ( name.equals("directories") ) {
	    return getDirectories();
	} else if ( name.equals("extensions") ) {
	    return getExtensions();
	} else if ( name.equals("content-types") ) {
	    return getContentTypes();
	}  
	return null;
    }

    /**
     * Delete this indexer.
     * @exception MultipleLockException if someone else has locked the resource
     */
    public synchronized void delete() 
	throws MultipleLockException
    {
	DummyResourceReference rr = 
	    (DummyResourceReference) getContentTypes();
	try {
	    Resource r = rr.lock();
	    r.delete();
	} catch (InvalidResourceException ex) {
	} finally {
	    rr.invalidate();
	    rr.unlock();
	}
	super.delete();
    }

    /*
     * Load an content-type descriptor.
     * @param type The name of the content-type.
     * @return An instance of Extension, or <strong>null</strong>.
     */

    public synchronized ResourceReference loadContentType (String type) {
	MimeType mtype = null;
	
	try {
	    mtype = new MimeType(type);
	} catch (MimeTypeFormatException ex) {
	    return null;
	}
	ResourceReference rr = getContentTypes();
	try {
	    TemplateContainer types = (TemplateContainer) rr.lock();
	    Enumeration e = types.enumerateResourceIdentifiers(true);
	    String key = "*:*"; // if there is no better match...
	    int match = -1;
	    int cmatch;
	    while (e.hasMoreElements()) {
		String ext_entry = (String)e.nextElement();
		MimeType entry_type = null;
		try {
		    entry_type = new MimeType(ext_entry.replace(':','/'));
		} catch (MimeTypeFormatException mex) {
		    // ok there is a configuration problem here
		    continue;
		}
		cmatch = mtype.match(entry_type);
		if (cmatch > match) {
		    match = cmatch;
		    key = ext_entry;
		    if (match == MimeType.MATCH_SPECIFIC_SUBTYPE)
			break; // exact match exit loop asap
		}
	    }
	    return types.lookup(key);
	} catch (InvalidResourceException ex) {
	    String msg = ("[resource indexer]: content-types \""+
			  type+
			  "\" couldn't be restored ("+ex.getMessage()+")");
	    getContext().getServer().errlog(msg);
	    return null;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Create a default file resource for this file (that exists).
     * @param directory The directory of the file.
     * @param name The name of the file.
     * @param defs A set of default attribute values.
     * @return An instance of Resource, or <strong>null</strong> if
     *    we were unable to create it.
     */

    protected Resource createFileResource(File directory,
					  RequestInterface req,
					  String name,
					  Hashtable defs) 
    {
	ResourceReference rr = null;
	FramedResource template = null;
	
	//try with content type in request.
	if (req instanceof Request) {
	    Request request = (Request) req;
	    MimeType type = request.getContentType();
	    if (type != null)
		rr = loadContentType(type.toString());
	    if (rr != null) {
		String s_dir = "directory".intern();
		String s_ide = "identifier".intern();
		String s_fil = "filename".intern();
		String s_con = "context".intern();
		if ( defs == null )
		    defs = new Hashtable(5) ;
		if ( defs.get(s_dir) == null )
		    defs.put(s_dir, directory) ;
		if ( defs.get(s_ide) == null )
		    defs.put(s_ide, name) ;
		if ( defs.get(s_con) == null )
		    defs.put(s_con, getContext());
		//read attribute from request
		try {
		    template = (FramedResource) rr.lock();
		    // Create, initialize and return the new resouce
		    try {
			FramedResource resource = null;
			resource = (FramedResource) template.getClone(defs);
			// Now update HTTPFrame from request
			String contentLanguage[] = 
			    request.getContentLanguage();
			String contentEncoding[] = 
			    request.getContentEncoding();
			//get the HTTPFrame.
			Class httpFrameClass = 
			    Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
		    //Added by Jeff Huang
		    //TODO: FIXIT
			HTTPFrame hframe = (HTTPFrame)
			    resource.getFrame(httpFrameClass);
			if (hframe != null) {
			    if ((contentLanguage != null) && 
				(contentLanguage.length > 0))
				hframe.setValue("content-language",
						contentLanguage[0]);
			    if ((contentEncoding != null) &&
				(contentEncoding.length > 0))
				hframe.setValue("content-encoding",
						contentEncoding[0]);
			}
			return resource;
		    } catch (Exception ex) {
			ex.printStackTrace() ;
		    }
		} catch (InvalidResourceException ex) {
		    ex.printStackTrace();
		} finally {
		    rr.unlock();
		}
	    }
	}
	// nothing found with this content-type, try usual indexing
	return super.createFileResource(directory, req, name, defs);
    }
}
