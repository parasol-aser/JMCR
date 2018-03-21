// NoCacheFilter.java
// $Id: NoCacheFilter.java,v 1.1 2010/06/15 12:24:58 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.Request;

/**
 * Transform a Pragma: no-cache into a Cache-Control: max-age=0
 * It is just here to fix some client brokenness.
 */

public class NoCacheFilter extends ResourceFilter {

    /**
     * Modify the request to transform a Pragma: no-cache into a 
     * Cache-Control: max-age=0
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong> 
     * otherwise. 
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */ 
    public ReplyInterface ingoingFilter(RequestInterface request) 
	throws ProtocolException
    {
	Request req = (Request) request;
	if (req.hasPragma("no-cache")) {
	    String pragmas[] = req.getPragma();
	    if (pragmas.length == 1) {
		req.setPragma(null);
	    } else {
		// remove the no-cache
		String newpragmas[] = new String[pragmas.length - 1];
		for (int i=0, j=0; i<pragmas.length; i++) {
		    if (pragmas[i].equals("no-cache"))
			continue;
		    newpragmas[j++] = pragmas[i];
		}
		req.setPragma(newpragmas);
	    }
	    req.setMaxAge(0);
	}
	return null;
    }

    /**
     * The outgoing filter decorates the reply appropriately.
     * @param request The original request.
     * @param reply The originial reply.
     * @return Always <strong>null</strong>.
     * @exception ProtocolException If processing should be interrupted,
     * because an abnormal situation occured. 
     */

    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep) 
	throws ProtocolException
    {
	return null;
    }
}
