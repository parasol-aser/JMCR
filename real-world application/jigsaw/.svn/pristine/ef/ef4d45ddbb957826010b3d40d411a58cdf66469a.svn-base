// CookieFilter.java
// $Id: CookieFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;

import org.w3c.www.http.HttpCookie;
import org.w3c.www.http.HttpCookieList;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpSetCookie;
import org.w3c.www.http.HttpSetCookieList;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * A demo for how to use cookies from Jigsaw.
 */

public class CookieFilter extends ResourceFilter {
    public static final 
	String NAME = "org.w3c.jigsaw.filters.counter";

    /**
     * Attribute index - The duration of the cookie.
     */
    protected static int ATTR_COOKIE_MAXAGE = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.CookieFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	a = new IntegerAttribute("cookie-maxage"
				 , new Integer(20)
				 , Attribute.EDITABLE);
	ATTR_COOKIE_MAXAGE = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get the cookie's allowed max age.
     * @return The max allowed age in seconds.
     */

    public int getCookieMaxAge() {
	return getInt(ATTR_COOKIE_MAXAGE, 20);
    }

    /**
     * @return A Reply instance, if the filter did know how to answer
     * the request without further processing, <strong>null</strong> 
     * otherwise. 
     * @exception org.w3c.tools.resources.ProtocolException 
     * If processing should be interrupted,
     * because an abnormal situation occured. 
     */ 
    public ReplyInterface ingoingFilter(RequestInterface request) 
	throws ProtocolException
    {
	return null;
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
	int count = 1;
	HttpCookieList cookies = request.getCookie();
	// Display and get available count:
	if ( cookies != null ) {
	    HttpCookie c = cookies.getCookie(NAME);
	    if ( c != null ) {
		System.out.println("cookie-count="+c.getValue());
		try {
		    count = Integer.parseInt(c.getValue())+1;
		} catch (Exception ex) {
		}
	    }
	}
	String strcount = Integer.toString(count);
	// Set cookie with next value:
	FramedResource frame = (FramedResource) getResource();
	if (frame instanceof HTTPFrame) {
	    HTTPFrame target = (HTTPFrame) frame;
	    HttpSetCookieList setcookies = HttpFactory.makeSetCookieList(null);
	    HttpSetCookie     setcookie  = setcookies.addSetCookie(NAME,
								   strcount);
	    setcookie.setMaxAge(getCookieMaxAge());
	    URL url = target.getURL(request);
	    setcookie.setPath(url.getFile());
	    setcookie.setDomain(url.getHost());
	    reply.setSetCookie(setcookies);
	    reply.addNoCache("Set-Cookie");
	}
	return null;
    }

}


