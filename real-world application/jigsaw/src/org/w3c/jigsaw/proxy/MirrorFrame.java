// MirrorFrame.java
// $Id: MirrorFrame.java,v 1.2 2010/06/15 17:53:06 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy ;

import java.net.URL;

import java.io.IOException;

import org.w3c.www.http.HTTP;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

public class MirrorFrame extends ForwardFrame {
    /**
     * Attribute index - The site we are mirroring.
     */
    protected static int ATTR_MIRRORS = -1;
    /**
     * Attribute index - Do we mirror from root or relative
     */
    protected static int ATTR_PARTIAL = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.proxy.MirrorFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the mirrored site attribute:
	a = new StringAttribute("mirrors"
				, null
				, Attribute.EDITABLE);
	ATTR_MIRRORS = AttributeRegistry.registerAttribute(c, a);
	// Do we allow sub mirroring (aka parts of the site)
	// and not from root
	a = new BooleanAttribute("partial"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_PARTIAL = AttributeRegistry.registerAttribute(c, a);
    }
    
    protected URL mirrors = null;
    public static final String MIRROR_PATH = "MIRROR_PATH";
	
    /**
     * Get the mirrors site attribute value.
     * @return The String encoded URL of the site we are mirroring here.
     */

    public String getMirrors() {
	return getString(ATTR_MIRRORS, null);
    }

    /**
     * Get the mirrors site attribute value.
     * @return The String encoded URL of the site we are mirroring here.
     */

    public boolean isPartialMirroring() {
	return getBoolean(ATTR_PARTIAL, false);
    }

   /**
     * Catch assignment to the mirror attribute, to update our cached URL.
     * @param idx The slot to set.
     * @param value It's new value.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_MIRRORS ) {
	    try {
		mirrors = new URL(getMirrors());
	    } catch (Exception ex) {
		mirrors = null;
	    }
	}
    }

    /**
     * @param request the incomming request
     * @param rep the client reply
     * @return A Reply instance
     * @exception HTTPException if processing the request failed.
     * @exception IOException if an IO error occurs.
     */
    protected Reply dupReply(Request request
			     , org.w3c.www.protocol.http.Reply rep) 
	throws HTTPException, IOException
    {
	Reply reply = super.dupReply(request, rep);
	// Tweak redirections ! Wow this is getting real nifty :-)
	switch(reply.getStatus()) {
	  case HTTP.MOVED_PERMANENTLY:
	  case HTTP.TEMPORARY_REDIRECT:
	  case HTTP.FOUND:
	  case HTTP.SEE_OTHER:
	      // Have fun !
	      String location = rep.getLocation();
	      if ((mirrors != null) && (location != null)) {
		  try {
		      URL uloc = new URL(request.getURL(), location);
		      URL loc  = getURL(request);
		      URL fake = null;
		      if (isPartialMirroring()) {
			  fake = new URL(request.getURL().getProtocol()
					 , loc.getHost()
					 , loc.getPort()
					 , getURLPath()+uloc.getFile());
		      } else if (location.startsWith(mirrors.toString())) {
			  fake = new URL(request.getURL().getProtocol()
					 , loc.getHost()
					 , loc.getPort()
					 , uloc.getFile());
		      } else {
			  fake = uloc;
		      }
		      if (fake != null) {
			  reply.setLocation(fake);
		      }
		  } catch (Exception ex) {
		  }
	      }
	}
	return reply;
    }

    /**
     * @param request the incomming request
     * @return A client Request instance.
     * @exception HTTPException if processing the request failed.
     * @exception IOException if an IO error occurs.
     */
    protected org.w3c.www.protocol.http.Request dupRequest(Request request) 
	throws HTTPException, IOException
    {
	org.w3c.www.protocol.http.Request req = super.dupRequest(request);
	// Tweak the URL :-)
	if (isPartialMirroring()) {
	    String requrl = request.getURL().getFile();
	    String respath = getURLPath();
	    if (requrl.startsWith(respath)) {
		String nurl = requrl.substring(respath.length());
		req.setURL(new URL(mirrors, nurl));
	    } else {
		req.setURL(new URL(mirrors, requrl));
	    }
	} else {
	    req.setURL(new URL(mirrors, request.getURL().getFile()));
	}
	return req;
    }
    
    /**
     * Lookup for a mirrored  resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception org.w3c.tools.resources.ProtocolException If an error 
     * relative to the protocol occurs
     */
    public boolean lookupOther(LookupState ls, LookupResult lr)
	throws org.w3c.tools.resources.ProtocolException 
    {
	// Get the full URL from the request:
	Request request = (Request) ls.getRequest();
	URL     url     = request.getURL();

	if ( ls.isInternal() )
	    return super.lookupOther(ls, lr);
	if ( mirrors != null ) {
	    request.setProxy(true);
	    lr.setTarget(this.getResource().getResourceReference());
	    return true;
	} 
	// Emit a not found:
	Reply error = request.makeReply(HTTP.NOT_FOUND);
	if (request.getMethod().equals("GET"))
	    error.setContent("Target resource not found.");
	lr.setTarget(null);
	lr.setReply(error);
	return true;
    }

    public void initialize(Object values[]) {
	super.initialize(values);
	String strmirrors = getMirrors();
	try {
	    mirrors = new URL(strmirrors);
	} catch (Exception ex) {
	    mirrors = null;
	}
    }
}
