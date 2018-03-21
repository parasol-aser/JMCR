// RemoteServletWrapper.java
// $Id: RemoteServletWrapper.java,v 1.2 2010/06/15 17:52:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import javax.servlet.ServletException;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 */

public class RemoteServletWrapper extends ServletWrapper {
    private static final boolean debug = false;

    /**
     * Attribute index - The servlet content base.
     */
    protected static int ATTR_SERVLET_BASE = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.servlet.RemoteServletWrapper");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the servlet base URL:
	a = new StringAttribute("servlet-base"
				, null
				, Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_SERVLET_BASE = AttributeRegistry.registerAttribute(c, a);
    }
    
    /**
     * The ServletLoader instance for loading that servlet.
     */
    protected ServletLoader loader = null;

    /**
     * Check the servlet class, ans try to initialize it.
     * @exception ClassNotFoundException if servlet class can't be found.
     * @exception ServletException if servlet can't be initialized.
     */
    protected void checkServlet() 
	throws ClassNotFoundException, ServletException
    {
        synchronized(servletPool) { 
            // synchronization for pool access added, tk, 21.10.2001
  	    if (! inited) {
		inited = launchServlet();
            }
        }
    }

    /** 
     * Get or create a suitable ServletLoader instance to load that servlet.
     * @return A ServletLoader instance.
     */

    protected synchronized ServletLoader getServletLoader() {
	if ( loader == null ) {
	    loader = new ServletLoader(this);
	}
	return loader;
    }

    /**
     * Get the remote servlet URL base.
     * @return The String encoded base URL for that servlet, or <strong>null
     * </strong> if undefined.
     */

    public String getServletBase() {
	return getString(ATTR_SERVLET_BASE, null);
    }

    public void setValue(int idx, Object value) {
	super.setValueOfSuperClass(idx, value);
	try {
	    // synchronization for pool access added, tk, 21.10.2001
	    synchronized(servletPool) {
  	        if ((idx == ATTR_SERVLET_CLASS) && (value != null)) {
		    inited = launchServlet();
		}
	        if ((idx == ATTR_SERVLET_BASE) && (value != null)) {
		    inited = launchServlet();
		}
            }
	} catch (Exception ex) {
	    String msg = ("unable to set servlet class \""+
			  getServletClass()+
			  "\" : "+
			  ex.getMessage());
	    getServer().errlog(msg);
	}
    }

    /**
     * Initialize the servlet.
     * @exception ClassNotFoundException if servlet class can't be found.
     * @exception ServletException if servlet can't be initialized.
     */
    protected boolean launchServlet() 
	throws ClassNotFoundException, ServletException
    {
	if ( debug ) {
	    System.out.println("Launching servlet: "+getServletClass());
	}
	// Get and check the servlet class:
	// if ( servlet != null )
	destroyServlet();
        if (inited) { 
	    String msg = "relaunching servlet failed due to incomplete \""
		+ getServletClass() + "\" cleanup.";
	    getServer().errlog(this, msg); 
	    return false;
        } else {
	    // Load appropriate servlet class:
	    Class c = null;
	    try {
		// Load the servlet class through the loader:
		c = getServletLoader().loadClass(getServletClass(), true);
	    } catch (ClassFormatError er) {
		String msg = ("class \""+getServletClass()+"\" loaded from "
			      + getServletBase() + ", invalid format.");
		if ( debug ) {
		    er.printStackTrace();
		}
		getServer().errlog(this, msg);
	    } catch (ClassNotFoundException ex) {
		String msg = ("class \""+getServletClass()+"\" loaded from "
			      + getServletBase() + ", not found.");
		if ( debug ) {
		    ex.printStackTrace();
		}
		getServer().errlog(this, msg);
	    } 
	    return (c != null) ? launchServlet(c) : false;
        }
    }
}
