// JigsawRequestDispatcher.java
// $Id: JigsawRequestDispatcher.java,v 1.1 2010/06/15 12:24:10 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletOutputStream;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;

import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ProtocolException;

import org.w3c.www.http.HTTP;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JigsawRequestDispatcher implements RequestDispatcher {

    public static final String REQUEST_URI_P = 
	"javax.servlet.include.request_uri";
    public static final String CONTEXT_PATH_P = 
	"javax.servlet.include.context_path";
    public static final String SERVLET_PATH_P = 
	"javax.servlet.include.servlet_path";
    public static final String PATH_INFO_P = 
	"javax.servlet.include.path_info";
    public static final String QUERY_STRING_P = 
	"javax.servlet.include.query_string";

    private httpd  server  = null;
    private String urlpath = null;

    public void forward(ServletRequest request,	ServletResponse response)
	throws ServletException, IOException
    {
	JigsawHttpServletResponse jres = (JigsawHttpServletResponse) response;
	JigsawHttpServletRequest  jreq = (JigsawHttpServletRequest) request;
	if (jres.isStreamObtained())
	    throw new IllegalStateException("Can't Forward! OutputStream or "+
					 "Writer has allready been obtained.");
	Request req  = (Request)jreq.getRequest().getClone();
	String  host = req.getHost(); 
	try {
	    //update URL...
	    if (host == null) {
		req.setURL(new URL(server.getURL(), urlpath));
	    } else {
		req.setURL(new URL(server.getURL().getProtocol(), host, 
				   urlpath));
	    }
	} catch (MalformedURLException ex) {
	    //should not occurs
	}
	
	//do nothing more with this reply
	jres.getReply().setStatus(HTTP.DONE);

	Reply reply = null;
	try {
	    reply = (Reply) server.perform(req);
	} catch (ResourceException ex) {
	    reply = req.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent(ex.getMessage());
	} catch (ProtocolException pex) {
	    if (pex.hasReply())
		reply = (Reply) pex.getReply();
	    else {
		reply = req.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		reply.setContent(pex.getMessage());
	    }
	}
	//copy reply into response...
	if (reply.hasStream()) {
	    jres.getReply().setStatus(reply.getStatus());
	    InputStream is = reply.openStream();
	    try {
		ServletOutputStream out = jres.getOutputStream();
		byte buffer[] = new byte[512];
		int len = -1;
		while((len = is.read(buffer, 0, 512)) != -1)
		    out.write(buffer, 0, len);
	    } catch (IllegalStateException ex) {
		Writer writer = jres.getWriter();
		Reader reader = new InputStreamReader(is);
		char buffer[] = new char[512];
		int len = -1;
		while((len = reader.read(buffer, 0, 512)) != -1)
		    writer.write(buffer, 0, len);
	    }
	}
    }

    public void include(ServletRequest request,	ServletResponse response)
	throws ServletException, IOException
    {
	JigsawHttpServletResponse jres = (JigsawHttpServletResponse) response;
	JigsawHttpServletRequest  jreq = (JigsawHttpServletRequest) request;

	Request req  = (Request)jreq.getRequest().getClone();
 	String  host = req.getHost(); 

	try {
	    //update URL...
	    if (host == null)
		req.setURL(new URL(server.getURL(), urlpath));
	    else
		req.setURL(new URL(server.getURL().getProtocol(), host, 
				   urlpath));
	} catch (MalformedURLException ex) {
	    //should not occurs
	}

	jres.flushStream(false);

	Reply reply = null;
	try {
	    //req.setState(CONTEXT_PATH_P, jreq.getContextPath());
	    req.setState(REQUEST_URI_P, jreq.getRequestURI());
	    req.setState(SERVLET_PATH_P, jreq.getServletPath());
	    req.setState(PATH_INFO_P, jreq.getPathInfo());
	    req.setState(QUERY_STRING_P, jreq.getQueryString());
	    req.setState(JigsawHttpServletResponse.INCLUDED, Boolean.TRUE);
	    reply = (Reply) server.perform(req);
	} catch (ResourceException ex) {
	    reply = req.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    reply.setContent(ex.getMessage());
	} catch (ProtocolException pex) {
	    if (pex.hasReply())
		reply = (Reply) pex.getReply();
	    else {
		reply = req.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		reply.setContent(pex.getMessage());
	    }
	}

	if (reply.hasStream()) {
	    InputStream is = reply.openStream();
	    try {
		ServletOutputStream out = jres.getOutputStream();
		byte buffer[] = new byte[512];
		int len = -1;
		while((len = is.read(buffer, 0, 512)) != -1)
		    out.write(buffer, 0, len);
	    } catch (IllegalStateException ex) {
		Writer writer = jres.getWriter();
		Reader reader = new InputStreamReader(is);
		char buffer[] = new char[512];
		int len = -1;
		while((len = reader.read(buffer, 0, 512)) != -1)
		    writer.write(buffer, 0, len);
	    }
	}
    }

    protected JigsawRequestDispatcher(String urlpath, httpd server) {
	this.server  = server;
	this.urlpath = urlpath;
    }

    /**
     * Get the appropriate dispatcher
     * @param name The servlet name
     * @param rr the ServletContainer (ServletDirectoryFrame) reference
     * @param server the HTTP server
     * @return the RequestDispatcher
     */
    public static RequestDispatcher getRequestDispatcher(String name,
							 ResourceReference rr,
							 httpd server)
    {
	try {
	    Resource res = rr.lock();
	    if (! (res instanceof ServletDirectoryFrame)) {
		throw new IllegalArgumentException("Not a servlet container!");
	    }
	    ServletDirectoryFrame sdf = (ServletDirectoryFrame) res;
	    // if (sdf.getServlet(name) != null) {
	    if (sdf.isServletLoaded(name)) {
		// servlet exists, so return the dispatcher
		String urlpath = sdf.getResource().getURLPath();
		urlpath = 
		    urlpath.endsWith("/") ? urlpath+name : urlpath+"/"+name;
		return new JigsawRequestDispatcher(urlpath, server);
	    }
	} catch(InvalidResourceException ex) {
	    return null;
	} finally {
	    rr.unlock();
	}
	return null;
    }

    /**
     * Get the appropriate dispatcher
     * @param urlpath the servlet URI
     * @param server the HTTP server
     * @param rr the ServletContainer reference or a servlet reference 
     * (Just used for Virtual Host)
     * @return the RequestDispatcher
     */
    public static RequestDispatcher getRequestDispatcher(String urlpath,
							 httpd server,
							 ResourceReference rr) 
    {
	ResourceReference rr_root = null;
	FramedResource    root = null;
	rr_root = JigsawServletContext.getLocalRoot(server.getRootReference(),
						    rr);
	try {
	    root = (FramedResource) rr_root.lock();
	    // Do the lookup:
	    ResourceReference r_target = null;
	    try {
		LookupState  ls = new LookupState(urlpath);
		LookupResult lr = new LookupResult(rr_root);
		root.lookup(ls, lr);
		r_target = lr.getTarget();
	    } catch (Exception ex) {
		r_target = null;
	    }
	    if (r_target == null)
		return null;
	    //there is a resource, return the dispatcher...
	    return new JigsawRequestDispatcher(urlpath, server);
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rr_root.unlock(); 
	}
    }

}
