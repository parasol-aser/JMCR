// ZipFrame.java
// $Id: ZipFrame.java,v 1.2 2010/06/15 17:53:04 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.zip;

import java.util.Vector;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.File;
import java.net.URLEncoder;
import org.w3c.tools.sorter.Sorter;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HTTP;
import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ZipFrame extends HTTPFrame {

    protected ZipFileResource zipfresource = null;

    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	if (resource instanceof ZipFileResource)
	    zipfresource = (ZipFileResource) resource;
    }

    /**
     * Create the reply relative to the given file.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception org.w3c.tools.resources.ProtocolException if processing 
     * the request failed.
     * @exception org.w3c.tools.resources.ResourceException if the resource
     * got a fatal error.
     */
    protected Reply createFileReply(Request request) 
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	if (zipfresource == null) {
	    throw new ResourceException("this frame is not attached to a "+
					"ZipFileResource. ("+
					resource.getIdentifier()+")");
	}
	// Default to full reply:
	reply = createDefaultReply(request, HTTP.OK) ;
	InputStream in = zipfresource.getInputStream();
	if (in != null)
	    reply.setStream(in);
	return reply ;
    }

    /**
     * Get for FileResource
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getFileResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (fresource == null) 
	    throw new ResourceException("this frame is not attached to a "+
					"FileResource. ("+
					resource.getIdentifier()+")");
	Reply reply = null;
	File file = fresource.getFile() ;
	fresource.checkContent();
	updateCachedHeaders();
	// Check validators:
	int cim = checkIfMatch(request);
	if ((cim == COND_FAILED) || (cim == COND_WEAK)) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if ( checkIfUnmodifiedSince(request) == COND_FAILED ) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if ( checkValidators(request) == COND_FAILED) {
	    return createDefaultReply(request, HTTP.NOT_MODIFIED);
	}	
	// Does this file really exists, if so send it back
	if ( zipfresource.hasEntry()) {
	    reply = createFileReply(request);
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
	    return reply;
	} else {
	    return deleteMe(request);
	}
    }







    /**
     * Get ContainerResource listing
     * @param refresh should we refresh the listing?
     * @return a boolean (true if refreshed)
     */ 
    public synchronized boolean computeContainerListing(boolean refresh) {
	ContainerResource cresource = (ContainerResource)resource;
	if ((refresh) ||
	    (listing == null) || 
	    (cresource.getLastModified() > listing_stamp) || 
	    (getLastModified() > listing_stamp)) {
	    
	    Class http_class = null;
	    try {
		http_class = Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (ClassNotFoundException ex) {
		http_class = null;
	    }

	    Enumeration   e         = cresource.enumerateResourceIdentifiers();
	    Vector        resources = Sorter.sortStringEnumeration(e) ;
	    HtmlGenerator g         = 
		new HtmlGenerator("Index of "+cresource.getIdentifier());
	    // Add style link
	    addStyleSheet(g);
	    g.append("<h1>"+cresource.getIdentifier()+"</h1>");
	    // Link to the parent, when possible:
	    if ( cresource.getParent() != null ) {
		g.append("<p><a href=\"..\">Parent</a><br>");
	    }
	    // List the children:
	    for (int i = 0 ; i < resources.size() ; i++) {
		String            name = (String) resources.elementAt(i);
		ResourceReference rr   = null;
		long              size = -1;
		rr = cresource.lookup(name);
		FramedResource resource = null;
		if (rr != null) {
		    try {
			resource = (FramedResource) rr.lock();
			// remove manually deleted FileResources
			if( resource instanceof ZipFileResource ) {
			    ZipFileResource zfr = (ZipFileResource)resource;
			    if( ! zfr.hasEntry() ) {
				try {
				    zfr.delete();
				} catch (MultipleLockException ex) {};
				continue;
			    } else {
				size = zfr.getEntrySize();
			    }
			}
			// remove manually deleted DirectoryResources
			if( resource instanceof ZipDirectoryResource ) {
			    ZipDirectoryResource zdr = (ZipDirectoryResource)resource;
			    if( ! zdr.hasEntry() ) {
				try {
				    zdr.delete();
				} catch (MultipleLockException ex) {};
				continue;
			    }
			}
			HTTPFrame itsframe = null;
			if (http_class != null) {
			    itsframe = 
				 (HTTPFrame) resource.getFrame(http_class); 
			}
			if (itsframe != null) {
			    // Icon first, if available
			    String icon = itsframe.getIcon() ;
			    if ( icon != null ) 
				g.append("<img src=\""+
					 getIconDirectory() +"/" + icon+
					 "\" alt=\"" + icon + "\">");
			    // Resource's name with link:
			    if (resource instanceof ContainerInterface)
				g.append("<a href=\"" 
					 , URLEncoder.encode(name)
					 , "/\">"+name+"</a>");
			    else
				g.append("<a href=\"" 
					 , URLEncoder.encode(name)
					 , "\">"+name+"</a>");
			    // resource's title, if any:
			    String title = itsframe.getTitle();
			    if ( title != null )
				g.append(" "+title);
			    //size (if any)
			    if (size != -1) {
				String s = null;
				if (size > 1023) {
				    s = " ["+(size/1024)+" Kb]";
				} else {
				    s = " ["+size+" bytes]";
				}
				g.append(s);
			    }
			    g.append("<br>\n");
			} else {
			    // Resource's name with link:
			    g.append(name+" (<i>Not available via HTTP.</i>)");
			    g.append("<br>\n");
			}
		    } catch (InvalidResourceException ex) {
			g.append(name+
				 " cannot be loaded (server misconfigured)");
			g.append("<br>\n");
			continue;
		    } finally { 
			rr.unlock();
		    }
		}
	    }
	    g.close() ;
	    listing_stamp = getLastModified() ;
	    listing       = g ;
	    return true;
	}
	return false;
    }


    

    /**
     * The default PUT method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException Always thrown, to return a NOT_IMPLEMENTED
     * error.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply put(Request request)
	throws ProtocolException, ResourceException
    {
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method PUT not implemented for zipped document") ;
	throw new HTTPException (error) ;
    }







    
}
