// LoggingProp.java
// $Id: LoggingProp.java,v 1.2 2010/06/15 17:52:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ClassAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;

/**
 * A wrapper class to give access to editable properties through a resource.
 * This class allows to reuse entirely the generic resource editor to
 * edit the properties of the server.
 */

class LoggingProp extends PropertySet {
    private static String title = "Logging properties";

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.http.LoggingProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The class of the logger:
	a = new ClassAttribute(httpd.LOGGER_P
			       , null
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The error log file:
	a = new FileAttribute(CommonLogger.ERRLOGNAME_P
			      , null
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The normal log:
	a = new FileAttribute(CommonLogger.LOGNAME_P
			      , null
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The trace log:
	a = new FileAttribute(CommonLogger.TRACELOGNAME_P
			      , null
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The log dir:
	a = new FileAttribute(CommonLogger.LOGDIRNAME_P
			      , null
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The rotation level
	a = new IntegerAttribute(CommonLogger.ROTATE_LEVEL_P
				 , new Integer(0)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
    	// The log buffer size:
	a = new IntegerAttribute(CommonLogger.BUFSIZE_P
				 , new Integer(8192)
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

    LoggingProp(String name, httpd server) {
	super(name, server);
    }
}


