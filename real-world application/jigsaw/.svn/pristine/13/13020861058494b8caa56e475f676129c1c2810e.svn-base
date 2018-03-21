// PostableFrame.java
// $Id: PostableFrame.java,v 1.2 2010/06/15 17:52:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames ;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Enumeration;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.www.mime.MimeType;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpTokenList;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.ClientException;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator ;

/**
 * Handle POST.
 */
public class PostableFrame extends HTTPFrame {

    private static MimeType type = MimeType.APPLICATION_X_WWW_FORM_URLENCODED ;
    /**
     * Attribute index - Should we override form values when multiple ?
     */
    protected static int ATTR_OVERIDE = -1 ;
    /**
     * Attribute index - Should we silently convert GET to POST methods ?
     */
    protected static int ATTR_CONVERT_GET = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.PostableFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The override attribute:
	a = new BooleanAttribute("override",
				 Boolean.FALSE,
				 Attribute.EDITABLE);
	ATTR_OVERIDE = AttributeRegistry.registerAttribute(cls, a) ;
	// The convert get attribute:
	a = new BooleanAttribute("convert-get",
				 Boolean.TRUE,
				 Attribute.EDITABLE) ;
	ATTR_CONVERT_GET = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the 'convert GET to POST' flag.
     */

    public boolean getConvertGetFlag() {
	return getBoolean(ATTR_CONVERT_GET, false) ;
    }

    /**
     * Get the 'override multiple form field value' flag.
     */

    public boolean getOverrideFlag() {
	return getBoolean(ATTR_OVERIDE, true) ;
    }

    /**
     * get the Allowed methods for this resource
     * @return an HttpTokenList
     */
    protected HttpTokenList getAllow() {
	if (allowed != null) {
	    return allowed;
	}
	int size = 5; // the default HEAD GET OPTIONS POST TRACE
	if (getPutableFlag()) {
	    size++;
	}
	if (getAllowDeleteFlag()) {
	    size++;
	}
	String allow_str[] = new String[size];
	int i = 0;
	if (getAllowDeleteFlag()) {
	    allow_str[i++] = "DELETE";
	}
	allow_str[i++] = "HEAD";
	allow_str[i++] = "GET";
	allow_str[i++] = "OPTIONS";
	allow_str[i++] = "POST";
	if (getPutableFlag()) {
	    allow_str[i++] = "PUT";
	}
	allow_str[i] = "TRACE";
	allowed = HttpFactory.makeStringList(allow_str);
	return allowed;
    }

    /**
     * Get this resource body.
     * If we are allowed to convert GET requests to POST, than we first
     * check to see if there is some search string in the request, and continue
     * with normal POST request processing.
     * <p>If there is no search string, or if we are not allowed to convert
     * GETs to POSTs, than we just invoke our <code>super</code> method,
     * which will perform the appropriate job.
     * @param request The request to handle.
     * @exception ProtocolException If request couldn't be processed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply get (Request request) 
	throws ProtocolException, ResourceException
    {
	// Check if we should handle it (is it a POST disguised in GET ?)
	if ((! getConvertGetFlag()) || ( ! request.hasState("query")))
	    return super.get (request) ;
	// Get the request entity, and decode it:
	String      query = request.getQueryString() ;
	InputStream in    = new StringBufferInputStream(query) ;
	URLDecoder  d     = new URLDecoder (in, getOverrideFlag()) ;
	try {
	    d.parse () ;
	} catch (URLDecoderException e) {
	    Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
	    error.setContent("Invalid request:unable to decode form data.");
	    throw new HTTPException (error) ;
	} catch (IOException e) {
	    Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
	    error.setContent("Invalid request: unable to read form data.");
	    throw new HTTPException (error) ;
	}
	return handle (request, d) ;
    }

    /**
     * Perform the post method.
     * @param request The request to handle.
     * @exception ProtocolException If request couldn't be processed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply post (Request request)
	throws ProtocolException, ResourceException
    {
	boolean mustURLDecode = false;
	// Check that we are dealing with an application/x-www-form-urlencoded:
	if (request.hasContentType()) {
	    mustURLDecode = (type.match(request.getContentType()) ==
					MimeType.MATCH_SPECIFIC_SUBTYPE);
	}
	// Get and decode the request entity:
	URLDecoder dec = null;
	if (mustURLDecode) {
	    try {
		InputStream in = request.getInputStream() ;
		// Notify the client we are willing to continue processing:
		Client client = request.getClient();
		// FIXME check expect
		if ( client != null ) {
		    client.sendContinue();
		}
		dec = new URLDecoder (in, getOverrideFlag()) ;
		dec.parse () ;
	    } catch (URLDecoderException e) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: unable to decode "+
				 "form data.") ;
		throw new HTTPException (error) ;
	    } catch (IOException ex) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: unable to read form data.");
		throw new ClientException(request.getClient(), ex) ;
	    }
	}
	// Handle the stuff:
	return handle (request, dec) ;
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
	// Now we just dump back the variables we got:
	HtmlGenerator g = new HtmlGenerator ("Form decoded values") ;
	if (data != null) {
	    Enumeration   e = data.keys() ;
	    g.append ("<p>List of variables and values:</p><ul>") ;
	    while ( e.hasMoreElements () ) {
		String name = (String) e.nextElement() ;
		g.append ("<li><em>"+
			  name+"</em> = <b>"+
			  data.getValue(name)+
			  "</b></li>");
	    }
	    g.append ("</ul>") ;
	} else {
	    g.append ("<p>Not with the application/x-www-form-urlencoded"
		      +" mime type");
	}
	Reply reply = request.makeReply(HTTP.OK) ;
	reply.setStream (g) ;
	return reply ;
    }
}
