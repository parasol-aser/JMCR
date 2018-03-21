// ServletEnumeration.java
// $Id: ServletEnumeration.java,v 1.1 2010/06/15 12:24:12 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.servlet.Servlet;

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 */

public class ServletEnumeration implements Enumeration {
    Enumeration           children = null;
    Servlet               next     = null;
    ServletDirectoryFrame dir      = null;

    ServletEnumeration(ServletDirectoryFrame dir, Enumeration children) {
	this.dir      = dir;
	this.children = children; //was null ???
    }

    private final synchronized Servlet computeNext() {
	if ( next != null )
	    return next;
	while ( children.hasMoreElements() ) {
	    next = dir.getServlet((String) children.nextElement());
	    if ( next != null )
		return next;
	}
	return null;
    }
				  
	    
    public synchronized boolean hasMoreElements() {
	return (next != null) || ((next = computeNext()) != null);
    }

    public synchronized Object nextElement() {
	if ( next == null ) 
	    next = computeNext();
	if ( next != null ) {
	    Object ret = next;
	    next = null;
	    return ret;
	}  else {
	    throw new NoSuchElementException("NextElement");
	}
    }
}
