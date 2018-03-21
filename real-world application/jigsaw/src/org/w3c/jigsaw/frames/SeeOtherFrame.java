// SeeOtherFrame.java
// $Id: SeeOtherFrame.java,v 1.2 2010/06/15 17:52:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames ;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpReplyMessage;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
/**
 * generates a 303 See Other reply on a POST
 * client should do the redirect with a GET
 */

public class SeeOtherFrame extends PostableFrame {

    protected static int ATTR_TARGET_URL = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.SeeOtherFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The override attribute:
	a = new StringAttribute("target-url",
				null,
				Attribute.EDITABLE);
	ATTR_TARGET_URL = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Handle the form submission, after posted data parsing.
     * <p>This method ought to be abstract, but for reasonable reason, it
     * will just dump (parsed) the form content back to the client, so that it
     * can be used for debugging.
     * @param request The request proper.
     * @param data The parsed data content.
     * @exception ProtocolException If form data processing failed.
     * @see org.w3c.jigsaw.forms.URLDecoder
     */

    public Reply handle (Request request, URLDecoder data)
	throws ProtocolException 
    {
	Reply reply = request.makeReply(HTTP.SEE_OTHER);
	URL loc = null;
	String target = (String) getValue(ATTR_TARGET_URL, null) ;
	if (target == null) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    HtmlGenerator g = new HtmlGenerator("Error");
	    g.append("The target RelocateResource doesn't define the"
		     + " relocation location. The server is "
		     + " misconfigured.") ;
	    error.setStream(g);
	    return error ;
	}
	try {
	    loc = new URL(getURL(request), target);
	} catch (MalformedURLException ex) {
	    // still not well configured :)
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    HtmlGenerator g = new HtmlGenerator("Error");
	    g.append("The target RelocateResource doesn't define the"
		     + " relocation location. The server is "
		     + " misconfigured.") ;
	    error.setStream(g);
	    return error ;
	}
	reply.setLocation(loc);
	HtmlGenerator g = new HtmlGenerator("Moved");
	g.append("<P>You should see the following resource, with a GET"+
		 ", click on the link if your"
		 + " browser doesn't support automatic redirection<BR>"+
		 "<A HREF=\""+loc.toExternalForm()+"\">"+
		 loc.toExternalForm()+"</A>");
	reply.setStream(g);
	return reply ;
    }
}
