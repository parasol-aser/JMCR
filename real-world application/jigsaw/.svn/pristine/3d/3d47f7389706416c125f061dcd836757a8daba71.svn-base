// HTTPExtFrame.java
// $Id: HTTPExtFrame.java,v 1.1 2010/06/15 12:24:16 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.frames; 

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpExtList;
import org.w3c.www.http.HttpExt;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class HTTPExtFrame extends HTTPFrame {

    /**
     * Supported extensions 
     */
    protected static Hashtable  extensions = null;

    /**
     * Register a protocol extension name.
     * @param name the extension name (absoluteURI or field-name);
     * @param methods the allowed methods (an array of String)
     * @param classname the associated frame classname.
     */
    protected final static void registerExtension(String name, 
						  String methods[],
						  String classname) 
    {
	for (int i = 0 ; i < methods.length ; i++) {
	    registerExtension(name, methods[i], classname);
	}
    }

    /**
     * Register a protocol extension name.
     * @param name the extension name (absoluteURI or field-name);
     * @param method the allowed method
     * @param classname the associated frame classname.
     */
    protected final static void registerExtension(String name, 
						  String method,
						  String classname) 
    {
	if (extensions == null)
	    extensions = new Hashtable(2);

	String    idx     = classname+"."+method;
	Hashtable methods = (Hashtable)extensions.get(idx);

	if (methods == null) {
	    methods = new Hashtable(2);
	    extensions.put(idx, methods);
	}
	methods.put(name, Boolean.TRUE);
    }

    /**
     * Check if an extension is known.
     * @param name the extension name
     * @return a boolean
     */
    protected final static boolean supportedExtension(String name, 
						      String method,
						      Object obj)
    {
	if (extensions != null) {
	    String    idx     = obj.getClass().getName()+"."+method;
	    Hashtable methods = (Hashtable)extensions.get(idx);
	    if (methods != null)
		return (methods.get(name) != null);
	}
	return false;
    }

    /**
     * The handler for unknown method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply extended(Request request)
	throws ProtocolException, ResourceException
    {
	String method = request.getMethod() ;
	Reply  reply  = null;

	if ((method != null) && method.equals("BROWSE") && getBrowsableFlag())
	    return browse(request) ;

	if ((method != null) && (method.startsWith("M-"))) {
	    //HTTP Extension Framework
	    if ( method.equals("M-GET") ) {
		checkMandatoryExtension(request, method);
		reply = get(request) ;
	    } else if ( method.equals("M-HEAD") ) {
		checkMandatoryExtension(request, method);
		reply = head(request) ;
	    } else if ( method.equals("M-POST") ) {
		checkMandatoryExtension(request, method);
		reply = post(request) ;
	    } else if ( method.equals("M-PUT") ) {
		checkMandatoryExtension(request, method);
		reply = put(request) ;
	    } else if ( method.equals("M-OPTIONS") ) {
		checkMandatoryExtension(request, method);
		reply = options(request);
	    } else if ( method.equals("M-DELETE") ) {
		checkMandatoryExtension(request, method);
		reply = delete(request) ;
	    } else if ( method.equals("M-LINK") ) {
		checkMandatoryExtension(request, method);
		reply = link(request) ;
	    } else if ( method.equals("M-UNLINK") ) {
		checkMandatoryExtension(request, method);
		reply = unlink(request) ;
	    } else if ( method.equals("M-TRACE") ) {
		checkMandatoryExtension(request, method);
		reply = trace(request) ;
	    } else {
		reply = mextended(request) ;
	    }
	    return acknowledgeExtension(request, reply);
	}

	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method "+method+" not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * Set the Ext and/or C-Ext Header if necessary.
     * @param request the incomming request.
     * @param reply the reply
     * @return the acknowledged reply instance
     */
    protected Reply acknowledgeExtension(Request request, Reply reply) {
	return reply;
    }

    public Reply mextended(Request request) 
    	throws ProtocolException, ResourceException
    {
	String method = request.getMethod() ;
	Reply error   = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method "+method+" not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * Check if the mandatory extensions are supported.
     * @param request The request to handle.
     * @exception ProtocolException thrown when there is one (or more) 
     * unsupported extensions or if there is no extension in the M-XXX
     * method.
     */
    public void checkMandatoryExtension(Request request, String mmethod)
    	throws ProtocolException
    {
	HttpExtList extl         = request.getHttpManExtDecl();
	HttpExtList cextl        = request.getHttpCManExtDecl();
	HttpExt     exts[]       = null;
	HttpExt     cexts[]      = null;
	Vector      notsupported = new Vector(2);
	//remove the M-
	String      method       = mmethod.substring(2);

	if ((extl == null) && (cextl == null)) {
	    Reply error   = request.makeReply(HTTP.NOT_EXTENDED) ;
	    error.setContent("Mandatory extension expected.");
	    throw new HTTPException(error);
	} 

	int nb_extensions = 0;

	if (extl != null) {
	    exts          = extl.getHttpExts();
	    int len       = exts.length;
	    nb_extensions += len;
	    for (int i=0 ; i < len ; i++) {
		if (! supportedExtension(exts[i].getName(), method, this))
		    notsupported.addElement(exts[i].getName());
	    }
	}

	if (cextl != null) {
	    cexts         = cextl.getHttpExts();
	    int len       = cexts.length;
	    nb_extensions += len;
	    for (int i=0 ; i < cexts.length ; i++) {
		if (! supportedExtension(cexts[i].getName(), method, this))
		    notsupported.addElement(cexts[i].getName());
	    }
	}
	
	if (nb_extensions == 0) {
	    Reply error   = request.makeReply(HTTP.NOT_EXTENDED) ;
	    error.setContent("Mandatory extension expected.");
	    throw new HTTPException(error);
	}
		
	int len = -1;
	if ((len = notsupported.size()) > 0) {
	    Reply error   = request.makeReply(HTTP.NOT_EXTENDED) ;
	    HtmlGenerator content = new HtmlGenerator("Error");
	    content.append("<h1>Mandatory extension(s) not supported:",
			   "</h1><p>\n");
	    content.append("<ul>\n");
	    for (int i = 0; i < len; i++)
		content.append("<li> "+(String)notsupported.elementAt(i)+"\n");
	    content.append("</ul>\n");
	    error.setStream(content);
	    throw new HTTPException(error);
	}
    }

}
