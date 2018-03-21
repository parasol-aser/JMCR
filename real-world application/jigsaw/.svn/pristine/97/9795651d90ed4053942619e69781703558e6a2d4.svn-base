// TidyPutFilter.java
// $Id: TidyPutFilter.java,v 1.2 2010/06/15 17:53:03 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.PrintWriter;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.BooleanAttribute;

import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpWarning;
import org.w3c.www.http.HttpFactory;


import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.tidy.Tidy;
import org.w3c.tidy.Configuration;


public class TidyPutFilter extends ResourceFilter {

	
    /** attribute index */
    protected static int ATTR_VALID_STRICT = -1;
    
    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigedit.filters.TidyPutFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}

	a = new BooleanAttribute("valid-strict"
				 , Boolean.FALSE
				 , Attribute.EDITABLE); 
	ATTR_VALID_STRICT = AttributeRegistry.registerAttribute(c, a); 
    }

    HttpWarning hw = null;
    MimeType xhtml_mt = null;
   
   boolean isXhtml = false ;
   int tidyCharEncoding = Configuration.LATIN1;

    protected boolean getValidStrict(){
	return getBoolean(ATTR_VALID_STRICT, false);
    }
	

    public ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;

	if(request.getMethod().equals("PUT")) {
	    try {
		MimeType req_mt = request.getContentType();
		if (xhtml_mt == null){
		    xhtml_mt = new MimeType("application","xhtml+xml");
		}
		if (req_mt.match(xhtml_mt) != MimeType.NO_MATCH) {
		    isXhtml = true ;
		} else {
		    isXhtml = false ;
		}
		if (req_mt.match(MimeType.TEXT_HTML) == MimeType.NO_MATCH
		    && isXhtml == false){
		    return null;
		}
		if (req_mt.hasParameter("charset")){
		    String charset = req_mt.getParameterValue("charset"); 
		    if (charset.equalsIgnoreCase("iso-2022-jp")){
			tidyCharEncoding = Configuration.ISO2022 ;
		    }
		    if (charset.equalsIgnoreCase("us-ascii")){
			tidyCharEncoding = Configuration.ASCII ;
		    }
		    if (charset.equalsIgnoreCase("iso-8859-1")){
			tidyCharEncoding = Configuration.LATIN1 ;
		    }
		    if (charset.equalsIgnoreCase("utf8")){
			tidyCharEncoding = Configuration.UTF8 ;
		    }
		    if (charset.equalsIgnoreCase("macroman")){
			tidyCharEncoding = Configuration.MACROMAN ;
		    }
		}
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

	    try {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		if (isXhtml) {
		    tidy.setXHTML(true);
		} else {
		    tidy.setXHTML(false);
		}
		tidy.setErrout(new PrintWriter(System.err));
		in.mark(65536000);


//		System.out.println(tidyCharEncoding);
		tidy.setCharEncoding(tidyCharEncoding);
/*		tidy.setIndentContent(false);
		tidy.setIndentAttributes(false);
		tidy.setSmartIndent(false);
*/
		tidy.parse(in,bout);

		if (tidy.getParseErrors() != 0){
		    //System.out.println("too many errors, bailing out") ;
		    in.reset();
		}

		// Need to call tidy in other thread, because of Piped streams.
		
		byte[] bufout = bout.toByteArray();
	
		ByteArrayInputStream tmpbin = new ByteArrayInputStream(bufout);

		request.setContentLength(bufout.length);
		if (bufout.length != 0 ) {
		    // tidy is happy let's apply transformation
		    request.setStream(tmpbin);
		    // add state to set warnings on the way back
		    request.setState("tidy", "ok");
		} else {
		    if (getValidStrict() == true ){
			// tidy failed and a file MUST validate -> refuse put
			Reply reply = request.makeReply(HTTP.FORBIDDEN) ;
			HtmlGenerator g = new HtmlGenerator("Not Acceptable");
			g.append("<p>This HTML code does not validate. Valid"
				 + "code is required here</p>"
				 + "<p>Warnings: " + tidy.getParseWarnings() 
				   + " Errors: "+tidy.getParseErrors()+"</p>");

			reply.setStream(g);
			return reply;
		    } else {
			return null;
		    }
		}
	    } catch (Exception ex){
		ex.printStackTrace();
		// problem
	    }
	    return null;
	} else {
	    return null;
	}
    }

    /**
     * @param request The original request.
     * @param reply It's original reply. 
     * @return A Reply instance, or <strong>null</strong> if processing 
     * should continue normally. 
     * @exception ProtocolException If processing should be interrupted,  
     * because an abnormal situation occured. 
     */
    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep) 
	throws ProtocolException
    {
	Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	
	if (request.hasState("tidy")) {
	    if (hw == null) {
		hw = 
		  HttpFactory.makeWarning(HttpWarning.TRANSFORMATION_APPLIED);
		hw.setAgent("Jigsaw");
		hw.setText("Body modified for HTML conformance using JTidy");
		reply.addWarning(hw);
	    }
	}
	return null;
    }
}
