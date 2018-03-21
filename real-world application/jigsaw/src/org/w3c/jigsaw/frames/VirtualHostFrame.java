// VirtualHostFrame.java
// $Id: VirtualHostFrame.java,v 1.2 2010/06/15 17:52:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.jigsaw.http.Request;

/**
 * For Virtual Hosting.
 */
public class VirtualHostFrame extends HTTPFrame {

    /**
     * Attribute index - The default root (for unknown hosts)
     */
    protected static int ATTR_FOLLOWUP = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.frames.VirtualHostFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register our default root:
	a = new StringAttribute("followup"
				, null
				, Attribute.EDITABLE);
	ATTR_FOLLOWUP = AttributeRegistry.registerAttribute(c, a);
    }

    protected ResourceReference followup = null;

    /**
     * Get the name of the resource used as a followup.
     * @return A String giving the name of the resource to be used as the
     * default.
     */

    public String getFollowup() {
	return getString(ATTR_FOLLOWUP, null);
    }

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }

    /**
     * Lookup the followup resource.
     * @return The loaded resource for the current followup.
     */

    public synchronized ResourceReference lookupFollowup() {
	if ( followup == null ) {
	    String name  = getFollowup();
	    if ( name != null ) {
		followup = getServer().loadRoot(name);
	    }
	    if ( followup == null ) {
		getServer().errlog(getIdentifier()
				   + "[" + getClass().getName() + "]: "
				   + "unable to restore \"" + name + "\" "
				   + " from root store.");
	    }
	}
	return followup;
    }

    /**
     * Lookup the target resource when associated with an unknown resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupOther(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	// Try to lookup on the host header:
	ResourceReference vrroot = null;
	ContainerResource root = null;
	
	root = (ContainerResource)getResource();
	Request r = (Request)ls.getRequest();
	if ( r != null ) {
	    String host = r.getURL().getHost();
	    String protocol = r.getURL().getProtocol();
	    if (host == null) {
		host = r.getHost();
		if ( host != null ) {
		    // must strip the port if different from 80!
		    if (host.endsWith(":80") && protocol.equals("http")) {
			host = host.substring(0, host.lastIndexOf(":80"));
		    }
		    // and the same for https (443)
		    if (host.endsWith(":443") && protocol.equals("https")) {
			host = host.substring(0, host.lastIndexOf(":443"));
		    }
		}
	    } else {
		int port = r.getURL().getPort();
		if (port != -1) {
		    if ( (protocol.equals("http") && (port != 80)) ||
			 (protocol.equals("https") && (port != 443)) ) {
			host = host + ":" + port;
		    }
		}
	    }
	    if (host != null) {
		vrroot = root.lookup(host.toLowerCase());
	    }
	}
	if ( vrroot == null ) {
	    vrroot  = lookupFollowup();
	}
	// Check for what we got:
	if (vrroot == null) {
	    return super.lookupOther(ls, lr);
	}
	try {
	    lr.setTarget(vrroot);
	    FramedResource resource = (FramedResource) vrroot.lock();
	    boolean done = 
	      (resource != null ) ? resource.lookup(ls, lr) : false;
	    if (! done) {
		lr.setTarget(null);
	    }
	    // because the vroot eats the lookup state components
	    // we have to return true.
	    // Should not be continued by the caller.
	    return true;
	} catch (InvalidResourceException ex) {
	    return false;
	} finally {
	    vrroot.unlock();
	}
    }
}
