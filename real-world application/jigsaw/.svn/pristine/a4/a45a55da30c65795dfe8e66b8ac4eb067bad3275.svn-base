// PutedEntry.java
// $Id: PutedEntry.java,v 1.2 2010/06/15 17:53:03 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.File;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.tools.sorter.Comparable;

import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

/**
 * We want free pickling, hence the super class.
 */

public class PutedEntry extends AttributeHolder implements Comparable {
    protected static int ATTR_AUTHOR   = -1;
    protected static int ATTR_URL      = -1;
    protected static int ATTR_FILENAME = -1;
    protected static int ATTR_TIME     = -1;

    static {
	Class c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigedit.filters.PutedEntry");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The author of change:
	a = new StringAttribute("author"
				, null
				, Attribute.EDITABLE);
	ATTR_AUTHOR = AttributeRegistry.registerAttribute(c, a);
	// The mandatory url:
	a = new StringAttribute("url"
				, null
				, Attribute.EDITABLE);
	ATTR_URL = AttributeRegistry.registerAttribute(c, a);
	// The optional absolute file name:
	a = new StringAttribute("filename"
				, null
				, Attribute.EDITABLE);
	ATTR_FILENAME = AttributeRegistry.registerAttribute(c, a);
	// The time of modification:
	a = new LongAttribute("time"
			      , null
			      , Attribute.EDITABLE);
	ATTR_TIME = AttributeRegistry.registerAttribute(c, a);
    }

    final String getAuthor() {
	return getString(ATTR_AUTHOR, null);
    }

    final String getURL() {
	return getString(ATTR_URL, null);
    }

    final String getFilename() {
	return getString(ATTR_FILENAME, null);
    }

    final long getTime() {
	return getLong(ATTR_TIME, -1);
    }

    public String getStringValue() {
	return Long.toString(getTime());
    }

    public boolean greaterThan(Comparable comp) {
	return (getStringValue().compareTo(comp.getStringValue()) > 0);
    }

    protected String getKey() {
	String key = getFilename();
	if ( key == null )
	    return getURL();
	return key;
    }

    synchronized void update(Request request) {
	String author = (String) request.getState(AuthFilter.STATE_AUTHUSER);
	if (author == null) {
	    author = "unknown";
	}
	long   time   = System.currentTimeMillis();
	setValue(ATTR_AUTHOR, author);
	setValue(ATTR_TIME, new Long(time));
    }

    static PutedEntry makeEntry(Request request) {
	ResourceReference rr = request.getTargetResource();
	Resource          r  = null;
	if (rr != null) {
	  try {
	    r = rr.lock();
	    // Build an entry:
	    PutedEntry e = new PutedEntry();
	    e.setValue(ATTR_URL, request.getURL().toExternalForm());
	    if ( r instanceof FileResource )
	      e.setValue(ATTR_FILENAME,
			 ((FileResource) r).getFile().getAbsolutePath());
	    // Update other infos:
	    e.update(request);
	    return e;
	  } catch (InvalidResourceException ex) {
	    return null;
	  } finally {
	    rr.unlock();
	  }
	}
	return null;
    }

}


