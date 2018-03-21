// DirectoryListerFrame.java
// $Id: DirectoryListerFrame.java,v 1.2 2010/06/15 17:53:06 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources ;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.www.http.HTTP;

/**
 * Emit the content of its parent directory.
 */
public class DirectoryListerFrame extends HTTPFrame {

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }

    /**
     * The default GET method for other king of associated resource
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getOtherResource (Request request)
	throws ProtocolException, ResourceException
    {
	ResourceReference rr = getResource().getParent();
	if (rr != null) {
	    try {
		FramedResource p = (FramedResource)rr.unsafeLock();
		// synchronize here to create locks in the right order
		synchronized (p) { 
		    // verify
		    Class http_class = null;
		    try {
			http_class = 
			    Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    } catch (ClassNotFoundException ex) {
			throw new ResourceException(ex.getMessage());
		    }
		    ResourceReference rrf = p.getFrameReference(http_class);
		    if (rrf == null) {
			throw new ResourceException("DirectoryResource has "+
						    "no HTTPFrame");
		    }
		    try { 
			HTTPFrame httpframe = (HTTPFrame) rrf.unsafeLock();
			if (p instanceof DirectoryResource) 
			    return httpframe.getDirectoryListing(request);
			else 
			    return httpframe.get(request);
		    } catch (InvalidResourceException ex) {
			throw new ResourceException(
			    "Invalid parent frame (lister):"+
			    ex.getMessage());
		    } finally {
			rrf.unlock();
		    }
		}
	    } catch (InvalidResourceException ex) {
		throw new ResourceException("Invalid parent (lister):"+
					    ex.getMessage());
	    } finally {
		rr.unlock();
	    }
	} else {
	    return createDefaultReply(request, HTTP.NO_CONTENT) ;
	}
    }
}
