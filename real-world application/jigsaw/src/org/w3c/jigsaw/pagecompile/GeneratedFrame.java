// GeneratedFrame.java
// $Id: GeneratedFrame.java,v 1.1 2010/06/15 12:29:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pagecompile; 

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.frames.PostableFrame;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceException;

import org.w3c.www.http.HTTP;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public abstract class GeneratedFrame extends PostableFrame {
    /**
     * Get the 'convert GET to POST' flag.
     * Always return false in GeneratedFrame, could be overriden.
     * @return a boolean.
     */
    public boolean getConvertGetFlag() {
	return false;
    }

    /**
     * Perform the request
     * @param req The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	try {
	    return super.perform(req);
	} catch (ProtocolException ex) {
	    throw ex;
	} catch (ResourceException ex) {
	    throw ex;
	} catch (Exception ex) {
	    Request request = (Request) req;
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    PageCompileOutputStream err = new PageCompileOutputStream();
	    PrintWriter writer = new PrintWriter(err);
	    writer.print("The generated frame at\n\n"+
			 request.getURL()+"\n\n"+
			 "reported this exception : \n\n"+ex.getMessage()+
			 "\n\nStack trace : \n\n");
	    ex.printStackTrace(writer);
	    writer.flush();
	    writer.close();
	    error.setStream(err.getInputStream());
	    error.setContentLength(err.size());
	    error.setContentType(MimeType.TEXT_PLAIN);
	    return error;
	}
    }

    /**
     * The default GET method.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply get(Request request)
	throws ProtocolException, ResourceException
    {
	if (getConvertGetFlag() && request.hasState("query")) {
	    String      query = request.getQueryString() ;
	    InputStream in    = new StringBufferInputStream(query) ;
	    URLDecoder  d     = new URLDecoder (in, getOverrideFlag()) ;
	    try {
		d.parse () ;
	    } catch (URLDecoderException e) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: "+
				 "unable to decode form data.");
		throw new HTTPException (error) ;
	    } catch (IOException e) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: unable to read form data.");
		throw new HTTPException (error) ;
	    }
	    return handle (request, d) ;
	}
	Reply reply = createDefaultReply(request, HTTP.OK);
	PageCompileOutputStream out = new PageCompileOutputStream();
	try {
	    get(request, reply, out);
	} catch (IOException ex) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    PageCompileOutputStream err = new PageCompileOutputStream();
	    PrintWriter writer = new PrintWriter(err);
	    writer.print("The generated frame at\n\n"+
			 request.getURL()+"\n\n"+
			 "reported this exception : \n\n"+ex.getMessage()+
			 "\n\nStack trace : \n\n");
	    ex.printStackTrace(writer);
	    writer.flush();
	    writer.close();
	    error.setStream(err.getInputStream());
	    error.setContentLength(err.size());
	    error.setContentType(MimeType.TEXT_PLAIN);
	    return error;
	}
	reply.setStream(out.getInputStream());
	reply.setContentLength(out.size());
	return reply;
    }

    /**
     * Handle the form submission, after posted data parsing.
     * This methos always return "Method POST not allowed".
     * @param request The request proper.
     * @param reply The reply.
     * @param data The parsed data content.
     * @param out the output stream.
     * @exception ProtocolException If form data processing failed.
     * @exception IOException If an IO error occurs.
     * @see org.w3c.jigsaw.forms.URLDecoder
     */ 
    protected void post (Request request, 
			 Reply reply,
			 URLDecoder data, 
			 PageCompileOutputStream out) 
	throws ProtocolException, IOException
    {
	Reply error = request.makeReply(HTTP.NOT_ALLOWED) ;
	if ( allowed != null )
	    error.setHeaderValue(Reply.H_ALLOW, allowed);
	error.setContent("Method POST not allowed on this resource.") ;
	throw new HTTPException (error) ;
    }

    /**
     * Handle the form submission, after posted data parsing.
     * @param request The request proper.
     * @param data The parsed data content.
     * @exception ProtocolException If form data processing failed.
     * @see org.w3c.jigsaw.forms.URLDecoder
     */

    public Reply handle (Request request, URLDecoder data)
	throws ProtocolException 
    {
	PageCompileOutputStream out = new PageCompileOutputStream();
	Reply reply = createDefaultReply(request, HTTP.OK);

	try {
	    post(request, reply, data, out);
	} catch (IOException ex) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    PageCompileOutputStream err = new PageCompileOutputStream();
	    PrintWriter writer = new PrintWriter(err);
	    writer.print("The generated frame at\n\n"+
			 request.getURL()+"\n\n"+
			 "reported this exception : \n\n"+ex.getMessage()+
			 "\n\nStack trace : \n\n");
	    ex.printStackTrace(writer);
	    writer.flush();
	    writer.close();
	    error.setStream(err.getInputStream());
	    error.setContentLength(err.size());
	    error.setContentType(MimeType.TEXT_PLAIN);
	    return error;
	}
	reply.setStream(out.getInputStream());
	reply.setContentLength(out.size());
	return reply;
    }

    /**
     * All java code extracted between <java type=code> and </java> from 
     * the jhtml page will be put in this method body.
     * @param request the incomming request.
     * @param reply the reply.
     * @param out the output stream.
     * @exception IOException if an IO error occurs.
     */
    abstract protected void get(Request request, 
				Reply reply,
				PageCompileOutputStream out) 
	throws IOException;
}
