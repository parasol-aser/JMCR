// ProxyExtFrame.java
// $Id: ProxyExtFrame.java,v 1.1 2010/06/15 12:27:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy;

import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpExtList;
import org.w3c.www.http.HttpExt;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ProxyExtFrame extends ProxyFrame {

    /**
     * Perform the given proxied request.
     * @param request The request to perform.
     * @return A Reply instance.
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     * @exception org.w3c.tools.resources.ResourceException if the resource
     * got a fatal error.
     */

    public ReplyInterface perform(RequestInterface ri) 
	throws org.w3c.tools.resources.ProtocolException,
	       org.w3c.tools.resources.ResourceException
    {
	Request     request  = (Request) ri;
	Reply       reply    = null;
	Reply       extReply = null;
	HttpExtList cman     = request.getHttpCManExtDecl();
	HttpExtList copt     = request.getHttpCOptExtDecl();
	HttpExtList man      = request.getHttpManExtDecl();

	if ((cman != null) || (copt != null)) {
	    extReply = applyExtensions(request, cman, copt);
	    if (extReply != null)
		return extReply;
	    if ((cman != null) && (man == null)) {
		//strip the M-
		String method = request.getMethod();
		if (method.startsWith("M-"))
		    request.setMethod(method.substring(2));
	    }
	}
	//Send the request, and get the reply
	reply = (Reply) super.perform(ri);

	if ((cman != null) || (copt != null))
	    return applyExtensions( reply, cman, copt);
	return reply;
    }

    /**
     * Apply extension to the request.
     * @param request the incomming request
     * @param cman The Mandatory hop-by-hop extension declaration list
     * @param cman The optionnal hop-by-hop extension declaration list
     * @return a Reply instance or null if processing the request must continue
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     */
    public Reply applyExtensions(Request request, 
				 HttpExtList cman, 
				 HttpExtList copt) 
	throws org.w3c.tools.resources.ProtocolException
    {
	if (cman != null) {
	    Reply error = request.makeReply(HTTP.NOT_EXTENDED) ;
	    HtmlGenerator content = new HtmlGenerator("Error");
	    content.append("<h1>Mandatory extension(s) not supported:",
			   "</h1><p>\n");
	    content.append("<ul>\n");
	    HttpExt exts[] = cman.getHttpExts();
	    for (int i=0 ; i < exts.length ; i++)
		content.append("<li> "+exts[i].getName()+"\n");
	    content.append("</ul>\n");
	    error.setStream(content);
	    return error;
	}
	return null;
    }

    /**
     * Apply extension to the reply.
     * @param request the reply
     * @param cman The Mandatory hop-by-hop extension declaration list
     * @param cman The optionnal hop-by-hop extension declaration list
     * @return a Reply instance.
     * @exception org.w3c.tools.resources.ProtocolException if processing
     * the request failed.
     */
    public Reply applyExtensions(Reply reply,
				 HttpExtList cman, 
				 HttpExtList copt) 
	throws org.w3c.tools.resources.ProtocolException
    {
	return reply;
    }

}
