// ServletDirectoryFrame.java
// $Id: ServletDirectoryFrame.java,v 1.1 2010/06/15 12:24:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.File;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.w3c.jigsaw.http.httpd;
import org.w3c.util.ObservableProperties;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.jigsaw.frames.HTTPFrame;

import javax.servlet.http.HttpSessionContext;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 *  @author Benoit Mahe <bmahe@w3.org>
 */

public class ServletDirectoryFrame extends HTTPFrame {

    /**
     * The servlet Context.
     */
    protected ServletContext servletContext = null;

    /**
     * The Session Context.
     */
    protected HttpSessionContext sessionContext = null;

    /**
     * Register the resource and add ServletProperties in httpd.
     * @param resource The resource to register.
     */
    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	if (getServletProps() == null ) {
	    synchronized (this.getClass()) {
		httpd s = (httpd) getServer();
		if ( s != null ) {
		    // Register the property sheet if not done yet:
		    ObservableProperties props = s.getProperties() ;
		    s.registerPropertySet(new ServletProps(s));
		}
	    }
	}
    }

    /**
     * ServletContext implementation - Lookup a given servlet.
     */
    public Servlet getServlet(String name) {
	if (dresource != null) {
	    ResourceReference rr = dresource.lookup(name);
	    if (rr != null) {
		try {
		    Resource resource = rr.lock();
		    if (resource instanceof ServletWrapper)
			return ((ServletWrapper) resource).getServlet();
		} catch (InvalidResourceException ex) {
		    return null;
		} finally {
		    rr.unlock();
		}
	    }
	}
	return null;
    }

    /**
     * Lookup a given servlet without accessing it.
     * @return true if and only if loading was successful
     */
    public boolean isServletLoaded(String name) {
	if (dresource != null) {
	    ResourceReference rr = dresource.lookup(name);
	    if (rr != null) {
		try {
		    Resource resource = rr.lock();
		    if (resource instanceof ServletWrapper)
			return ((ServletWrapper) resource).isServletLoaded();
		} catch (InvalidResourceException ex) {
		    return false;
		} finally {
		    rr.unlock();
		}
	    }
	}
	return false;
    }

    /**
     * ServletContext implementation - Enumerate all servlets within context.
     */

    public Enumeration getServlets() {
	if (dresource != null)
	    return new ServletEnumeration(this, 
				  dresource.enumerateResourceIdentifiers());
	else
	    return new ServletEnumeration(this, null);
    }

    /**
     * ServletContext implementation - Enumerate all servlets names
     * within context.
     */

    public Enumeration getServletNames() {
	if (dresource != null)
	    return new ServletNamesEnumeration(this, 
			       dresource.enumerateResourceIdentifiers());
	else
	    return new ServletNamesEnumeration(this, null);
    }

    /**
     * ServletContext implementation - Get server informations.
     */

    public String getServerInfo() {
	return getServer().getSoftware();
    }

    /**
     * ServletContext implementation - Get an attribute value.
     * We map this into the ServletWrapper attributes, without
     * support for name clashes though.
     * @param name The attribute name.
     */

    public Object getAttribute(String name) {
	if ( definesAttribute(name) )
	    return getValue(name, null);
	else if (resource.definesAttribute(name))
	    return resource.getValue(name, null);
	return null;
    }

    protected HttpSessionContext getHttpSessionContext() {
	if (sessionContext == null) {
	    ServletProps sprops = getServletProps();
	    if (sprops != null)
		 sessionContext = sprops.getSessionContext();
	}
	return (HttpSessionContext)sessionContext;
    }

    protected ServletProps getServletProps() {
	httpd server = (httpd) getServer();
	return (ServletProps)
	    server.getPropertySet(ServletProps.SERVLET_PROPS_NAME);
    }

    protected ServletContext getServletContext() {
	if (servletContext == null) {
	    servletContext = new JigsawServletContext(getFrameReference(),
						 getServer().getProperties());
	    File tmp = new File(getServer().getTempDirectory(), 
		       String.valueOf(resource.getURLPath().hashCode()));
	    tmp.mkdirs();
	    servletContext.setAttribute(JigsawServletContext.TEMPDIR_P, tmp);
	}
	return (ServletContext) servletContext;
    }

    /**
     * We add a <em>context</em> attribute to all our children.
     * The <em>context</em> attribute is any object implementing the
     * ServletContext interface.
     */

    protected void updateDefaultChildAttributes(Hashtable attrs) {
	attrs.put("servlet-context", getServletContext());
	attrs.put("session-context", getHttpSessionContext());
    }

}
