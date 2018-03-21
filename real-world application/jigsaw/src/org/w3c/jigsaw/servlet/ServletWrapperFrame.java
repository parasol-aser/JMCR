// ServletWrapperFrame.java
// $Id: ServletWrapperFrame.java,v 1.1 2010/06/15 12:24:09 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * @author Alexandre Rafalovitch <alex@access.com.au>
 * @author Anselm Baird-Smith <abaird@w3.org>
 * @author Benoit Mahe <bmahe@w3.org>
 */

public class ServletWrapperFrame extends HTTPFrame {

    protected ServletWrapper wrapper = null;

    /**
     * Register our resource. Must be an instance of ServletWrapper.
     */
    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
	if (resource instanceof ServletWrapper)
	    wrapper = (ServletWrapper) resource;
    }

    /**
     * Create a reply to answer to request on this file.
     * This method will create a suitable reply (matching the given request)
     * and will set all its default header values to the appropriate 
     * values. The reply will not have LastModified field setted.
     * @param request The request to make a reply for.
     * @return An instance of Reply, suited to answer this request.
     */

    public Reply createDefaultReply(Request request, int status) {
	Reply reply = super.createDefaultReply(request, status);
	reply.setLastModified( -1 );
	return reply;
    }

    /**
     * Dispatch the give request to our servlet.
     * <p>If the servlet cannot be inititalized, we just throw an error message
     * otherwise, we just delegate that request processing to the underlying 
     * servlet instance.
     * @param request The request to be processed.
     * @exception ProtocolException If the wrapped servlet is not initialized.
     * @exception ResourceException If the resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req)
	throws ProtocolException, ResourceException
    {
	ReplyInterface repi = performFrames(req);
	if (repi != null)
	    return repi;

	if (! checkRequest(req))
	    return null;

	Request request = (Request) req;
	PipedInputStream pis = null;

	if (wrapper == null) {
	    Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent("Servlet Wrapper Frame not configured properly: "+
			     "must be attached to a ServletWrapper.");
	    throw new HTTPException(reply);
	}

	try {
	    wrapper.checkServlet();
	} catch (ClassNotFoundException ex) {
	    Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent("The server was unable to find the "+
			     "servlet class : "+ex.getMessage());
	    if ( wrapper.debug )
		ex.printStackTrace();
	    throw new HTTPException(reply);
	} catch (ServletException ex) {
	    Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent("The server was unable to initialize the "+
			     "servlet : "+ex.getMessage());
	    if ( wrapper.debug )
		ex.printStackTrace();
	    throw new HTTPException(reply);
	}

	// Check that the servlet has been initialized properly:
	if ( ! wrapper.isInited() ) {
	    Reply reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent("Servlet not configured properly");
	    throw new HTTPException(reply);
	} 
	// Dispatch the request:
	Reply reply = createDefaultReply(request, HTTP.OK);
	reply.setContentType(MimeType.TEXT_HTML);
	try {
	    if (request.hasState(JigsawHttpServletResponse.INCLUDED)) {
		wrapper.service(request, reply);
	    } else {
		pis = new PipedInputStream();
		request.setState(JigsawHttpServletResponse.STREAM, pis);
		PipedOutputStream pos = new PipedOutputStream(pis);
		reply.setState(JigsawHttpServletResponse.STREAM, pos);
		reply.setStream(pis);
		Object o = new Object();
		reply.setState(JigsawHttpServletResponse.MONITOR, o);
		// wait until the reply is constructed by the processing thread
		ServerInterface server = getServer();
		if (server instanceof httpd) {
		    synchronized (o) {
			wrapper.service(request, reply);
			o.wait((long)((httpd)server).getRequestTimeOut());
		    }
		    Object strm;
		    strm = reply.getState(JigsawHttpServletResponse.STREAM);
		    if (strm != null) {
			// it is a timeout
			try {
			    pis.close();
			    pos.close();
			} catch (IOException ex) {};
			if (strm instanceof PipedOutputStream) {
			    ServletWrapper.ServletRunner r;
			    r = (ServletWrapper.ServletRunner) 
				         reply.getState(ServletWrapper.RUNNER);
			    if (r != null) {
				r.signalTimeout();
			    }
			    throw new ServletException("Timed out");
			}
		    }
		} else {
		    synchronized (o) {
			wrapper.service(request, reply);
			o.wait();
		    }
		}
	    }
	} catch (UnavailableException uex) {
	     reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
	     if (uex.isPermanent()) {
		 reply.setContent("<h2>The servlet is permanently "+
				  "unavailable :</h2>"+
				  "Details: <b>"+uex.getMessage()+"</b>");
	     } else {
		 int delay = uex.getUnavailableSeconds();
		 if (delay > 0) {
		     reply.setRetryAfter(delay);
		     reply.setContent("<h2>The servlet is temporarily "+
				      "unavailable :</h2>"+
				      "Delay : "+delay+
				      " seconds<br><br>Details: <b>"+
				      uex.getMessage()+"</b>");
		 } else {
		     reply.setContent("<h2>The servlet is temporarily "+
				      "unavailable :</h2>"+
				      "Details: <b>"+uex.getMessage()+"</b>");
		 }
	     }
	     if (pis != null) {
		 try {
		     pis.close();
		 } catch (IOException ioex) {}
	     }
	} catch (Exception ex) {
	    if ( wrapper.debug )
		ex.printStackTrace();
	    reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent("Servlet has thrown exception:" + ex.toString());
	    if (pis != null) {
		try {
		    pis.close();
		} catch (IOException ioex) {}
	    }
	}
	if (reply != null) {
	    reply.setDynamic(true);
	}
	return reply;
    }

    /**
     * Jigsaw's lookup on servlets.
     * Once here, we have reached a leaf servlet (or at least the remaining
     * lookup is to be done at the servlet itself). We keep track of the
     * <em>path info</em> and mark that servlet as the target of request.
     * @param ls The lookup state.
     * @param lr The lookup result.
     * @exception ProtocolException If some error occurs.
     */

    protected boolean lookupOther(LookupState ls, LookupResult lr)
	throws ProtocolException
    {
	// Get the extra path information:
	String extraPath = ls.getRemainingPath(true);
	if ((extraPath == null) || extraPath.equals(""))
	    extraPath = "/";
	// Keep this path info into the request, if possible:
	Request request = (Request) ls.getRequest();
	if ( request != null ) {
	    if (request.getState(JigsawRequestDispatcher.PATH_INFO_P) == null)
		request.setState(JigsawRequestDispatcher.PATH_INFO_P, 
				 extraPath);
	}
	lr.setTarget(resource.getResourceReference());
	return super.lookupOther(ls, lr);
    }

}
