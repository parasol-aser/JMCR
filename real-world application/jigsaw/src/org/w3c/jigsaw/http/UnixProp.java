// UnixProp.java
// $Id: UnixProp.java,v 1.2 2010/06/15 17:52:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.config.PropertySet;

/**
 * A wrapper class to give access to editable properties through a resource.
 * This class wraps UNIX specific properties.
 */

class UnixProp extends PropertySet {
    private static String title = "Unix properties";

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.http.UnixProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Group
	a = new StringAttribute(httpd.SERVER_GROUP_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// User
	a = new IntegerAttribute(httpd.SERVER_USER_P
				 , null
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */

    public String getTitle() {
	return title;
    }

    UnixProp(String name, httpd server) {
	super(name, server);
    }
}


