// SimpleGrepFIlter.java
// $Id: SimpleGrepFilter.java,v 1.2 2010/06/15 17:53:03 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.www.mime.MimeType;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

public class SimpleGrepFilter extends ResourceFilter {

    class ByteArrayComp {
	byte[] tab    = null;
	String string = null;
	int    idx    = -1;
	int    length = -1;

	boolean matchNow(byte c) {
	    if (tab[idx++] == c) {
		return (idx == length);
	    } else {
		idx = 0;
		return false;
	    }
	}

	void reset() {
	    idx = 0;
	}

	String getString() {
	    return string;
	}

	ByteArrayComp(String string) {
	    tab         = string.getBytes();
	    idx         = 0;
	    length      = tab.length;
	    this.string = string;
	}
    }

    protected ByteArrayComp[] forbiddenBytes = null;

    /**
     * Attribute index - The strings to grep.
     */
    protected static int ATTR_FORBIDSTRING_ARRAY = -1;

    /**
     * Attribute index - The url to redirect.
     */
    protected static int ATTR_REDIRECT = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigedit.filters.SimpleGrepFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	a = new StringArrayAttribute("forbidden-strings"
				     , null
				     , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_FORBIDSTRING_ARRAY = AttributeRegistry.registerAttribute(c, a);

	a = new StringAttribute("redirect-url"
				, null
				, Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_REDIRECT = AttributeRegistry.registerAttribute(c, a);
    }

    protected String[] getForbiddenStrings() {
	return (String[]) getValue(ATTR_FORBIDSTRING_ARRAY, null);
    }

    protected String getRedirectURL() {
	return (String) getValue(ATTR_REDIRECT, null);
    }

    protected ByteArrayComp[] getForbiddenBytes() {
	if (forbiddenBytes == null ) {
	    String[] fstrings   = getForbiddenStrings();
	    forbiddenBytes = new ByteArrayComp[fstrings.length];
	    for (int i = 0 ; i < fstrings.length; i++)
		forbiddenBytes[i] = new ByteArrayComp(fstrings[i]);
	}
	return forbiddenBytes;
    }

    /**
     * Catch assignements to the forbidden strings attribute.
     * <p>When a change to that attribute is detected, the cached value
     * are updated.
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (idx == ATTR_FORBIDSTRING_ARRAY) {
	    forbiddenBytes = null;
	}
    }

    /**
     * Searh for a forbidden string in given stream.
     * @param in the InputStream
     * @return The String found or <strong>null</strong> if none
     * was found.
     */
    protected String searchForbiddenStrings(InputStream in) {
	if (getForbiddenStrings() == null)
	    return null;
	try {
	    ByteArrayComp comp[] = getForbiddenBytes();
	    int len = in.available();
	    int c;
	    in.mark(len);
	    int baclen = comp.length;

	    for (int j = 0; j < baclen; j++)
		comp[j].reset();

	    while ((c = in.read()) != -1) {
		for (int i = 0; i < baclen; i++) {
		    if (comp[i].matchNow((byte)c)) {
			in.reset();
			return comp[i].getString();
		    }
		}
	    }	    
	    in.reset();
	    return null;
	} catch (IOException ex) {
	    return null;
	}
    }

    /**
     * Search the forbidden string in the body, if found return
     * an ACCES FORBIDDEN Reply.
     * @param request The request that is about to be processsed.
     */

    public ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	if(request.getMethod().equals("PUT")) {
	    try {
		MimeType req_mt = request.getContentType();
		if (req_mt.match(MimeType.TEXT) == MimeType.NO_MATCH)
		    return null;
	    } catch (NullPointerException ex) {
		// no Content-Type sent! check anyway
	    }

	    InputStream in = null;
	    try {
		in = request.getInputStream();
		if ( in == null ) {
		    return null;
		}
	    } catch (IOException ex) {
		return null;
	    }
	    // verify that the target resource is putable
	    ResourceReference rr = request.getTargetResource();
	    if (rr != null) {
		try {
		    FramedResource target = (FramedResource) rr.lock();
		    HTTPFrame frame = null;
		    try {
			frame = (HTTPFrame) target.getFrame( 
			   Class.forName("org.w3c.jigsaw.frames.HTTPFrame"));
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    } catch (ClassNotFoundException cex) {
			cex.printStackTrace();
			//big big problem ...
		    }
		    if (frame == null) // can't be putable
			return null;
		    // now we can verify if the target resource is putable
		    if (! frame.getPutableFlag()) {
			return null;
		    }
		    // and that the PUT can happen (taken from putFileResource
		    int cim = frame.checkIfMatch(request);
		    if ((cim == HTTPFrame.COND_FAILED)
			|| (cim == HTTPFrame.COND_WEAK)
			|| (frame.checkIfNoneMatch(request) == 
			    HTTPFrame.COND_FAILED)
			|| (frame.checkIfModifiedSince(request) == 
			    HTTPFrame.COND_FAILED)
			|| (frame.checkIfUnmodifiedSince(request) == 
			    HTTPFrame.COND_FAILED)) {
			Reply r = request.makeReply(HTTP.PRECONDITION_FAILED);
			r.setContent("Pre-condition failed.");
			return r;
		    }
		} catch (InvalidResourceException ex) {
		    ex.printStackTrace();
		    // problem ...
		} finally {
		    rr.unlock();
		}
	    }
	    String expect = request.getExpect();
	    if (expect != null) {
		if (expect.startsWith("100")) { // expect 100?
		    Client client = request.getClient();
		    if (client != null) {
			try {
			    client.sendContinue();
			} catch (java.io.IOException ex) {
			    return null;
			}
		    }
		}
	    }
	    String found = searchForbiddenStrings(in);
	    if (found != null) {
		Reply error = request.makeReply(HTTP.FORBIDDEN);
		error.setReason("the string \""+found+"\" is forbidden.");
		error.setContent ("<p>the string \""+found+
				  "\" is forbidden.</p><br> click "+
				  "<A HREF=\""+getRedirectURL()+"\">here</A>"+
				  " for explaination.");
		return error;
	    }
	    return null;
	} else 
	    return null;
    }

}


