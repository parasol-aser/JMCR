// ExtFrame.java
// $Id: ExtFrame.java,v 1.1 2010/06/15 12:29:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.tests;

import java.util.Dictionary;
import java.util.Enumeration;
import java.io.InputStream;

import org.w3c.jigsaw.frames.HTTPExtFrame;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpExtList;
import org.w3c.www.http.HttpExt;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ExtFrame extends HTTPExtFrame {

    static {
	String classname = "org.w3c.jigsaw.tests.ExtFrame";
	registerExtension("http://www.w3.org/exts/test", "GET", classname);
	registerExtension("http://www.w3.org/exts/test", "HEAD", classname);
	registerExtension("http://www.w3.org/exts/test2", "GET", classname);
	registerExtension("http://www.w3.org/exts/test2", "HEAD", classname);
    }

    /**
     * Perform a GET.
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     * @exception org.w3c.tools.resources.ResourceException if the resource
     * got a fatal error.
     */
    public Reply get(Request request)
	throws ProtocolException, ResourceException
    {
	HttpExtList extl       = null;
	HttpExtList reply_extl = null;
	Reply reply            = super.get(request);

	//Mandatory End-to-End
	extl = request.getHttpManExtDecl();
	if (extl != null) {
	    reply_extl = new HttpExtList(extl);
	    reply.setHttpManExtDecl(reply_extl);
	    HttpExt exts[]  = extl.getHttpExts();
	    HttpExt rexts[] = reply_extl.getHttpExts();
	    for (int i=0; i < exts.length; i++) {
		HttpExt     ext         = exts[i];
		Dictionary  headers     = request.getExtensionHeaders(ext);
		Enumeration e           = headers.keys();
		String      resp_header = "";
		while (e.hasMoreElements()) {
		    String name = (String) e.nextElement();
		    HeaderValue value = (HeaderValue)headers.get(name);
		    resp_header+="["+name+": "+value.toExternalForm()+"] ";
		}
		reply.setExtensionHeader(rexts[i], "receipt", resp_header);
	    }
	    if (rexts.length > 0)
		reply.setEnd2EndExtensionAcknowledgmentHeader();
	}

	//Mandatory Hop-by-Hop
	extl = request.getHttpCManExtDecl();
	if (extl != null) {
	    reply_extl = new HttpExtList(extl);
	    reply.setHttpCManExtDecl(reply_extl);
	    HttpExt exts[]  = extl.getHttpExts();
	    HttpExt rexts[] = reply_extl.getHttpExts();
	    for (int i=0; i < exts.length; i++) {
		HttpExt     ext         = exts[i];
		Dictionary  headers     = request.getExtensionHeaders(ext);
		Enumeration e           = headers.keys();
		String      resp_header = "";
		while (e.hasMoreElements()) {
		    String name = (String) e.nextElement();
		    HeaderValue value = (HeaderValue)headers.get(name);
		    resp_header+="["+name+": "+value.toExternalForm()+"] ";
		}
		reply.setExtensionHeader(rexts[i], "receipt", resp_header);
	    }
	    if (rexts.length > 0)
		reply.setHopByHopExtensionAcknowledgmentHeader();
	}

	//Optionnal End-to-End
	extl = request.getHttpOptExtDecl();
	if (extl != null) {
	    reply_extl = new HttpExtList(extl);
	    reply.setHttpOptExtDecl(reply_extl);
	    HttpExt exts[]  = extl.getHttpExts();
	    HttpExt rexts[] = reply_extl.getHttpExts();
	    for (int i=0; i < exts.length; i++) {
		HttpExt     ext         = exts[i];
		Dictionary  headers     = request.getExtensionHeaders(ext);
		Enumeration e           = headers.keys();
		String      resp_header = "";
		while (e.hasMoreElements()) {
		    String name = (String) e.nextElement();
		    HeaderValue value = (HeaderValue)headers.get(name);
		    resp_header+="["+name+": "+value.toExternalForm()+"] ";
		}
		reply.setExtensionHeader(rexts[i], "receipt", resp_header);
	    }
	}

	//Optionnal Hop-by-Hop
	extl = request.getHttpCOptExtDecl();
	if (extl != null) {
	    reply_extl = new HttpExtList(extl);
	    reply.setHttpCOptExtDecl(reply_extl);
	    HttpExt exts[]  = extl.getHttpExts();
	    HttpExt rexts[] = reply_extl.getHttpExts();
	    for (int i=0; i < exts.length; i++) {
		HttpExt     ext         = exts[i];
		Dictionary  headers     = request.getExtensionHeaders(ext);
		Enumeration e           = headers.keys();
		String      resp_header = "";
		while (e.hasMoreElements()) {
		    String name = (String) e.nextElement();
		    HeaderValue value = (HeaderValue)headers.get(name);
		    resp_header+="["+name+": "+value.toExternalForm()+"] ";
		}
		reply.setExtensionHeader(rexts[i], "receipt", resp_header);
	    }
	}

	return reply;
    }

    /**
     * Perform a HEAD.
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     * @exception org.w3c.tools.resources.ResourceException if the resource
     * got a fatal error.
     */
    public Reply head(Request request) 
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	reply = get(request) ;
	reply.setStream((InputStream) null);
	return reply;
    }

}
