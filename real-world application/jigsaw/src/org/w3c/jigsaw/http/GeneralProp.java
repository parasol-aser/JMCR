// GeneralProp.java
// $Id: GeneralProp.java,v 1.2 2010/06/15 17:52:57 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.config.PropertySet;

/**
 * A wrapper class to give access to editable properties through a resource.
 * This class allows to reuse entirely the generic resource editor to
 * edit the properties of the server.
 */

class GeneralProp extends PropertySet {
    private static final String title = "General properties";

    /*
     * Attribute index - the URL rewriting patterns
     */
    protected static int ATTR_PORT = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.http.GeneralProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The server name:
	a = new StringAttribute(httpd.SERVER_SOFTWARE_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Is the file system case sensitive ?
	a = new BooleanAttribute(httpd.FS_SENSITIVITY
				 , null
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The server root directory:
	a = new FileAttribute(httpd.ROOT_P
			      , null
			      , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The full host name:
	a = new StringAttribute(httpd.HOST_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The port number:
	a = new IntegerAttribute(httpd.PORT_P
				 , null
				 , Attribute.EDITABLE);
	ATTR_PORT = AttributeRegistry.registerAttribute(c, a);
	// The root resource identifier:
	a = new StringAttribute(httpd.ROOT_NAME_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The list of public methods:
	a = new StringArrayAttribute(httpd.PUBLIC_P
				     , null
				     , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The trace flag:
	a = new BooleanAttribute(httpd.TRACE_P
				 , null
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The documentation path:
	a = new StringAttribute(httpd.DOCURL_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The Chekpoint Resource URL:
	a = new StringAttribute(httpd.CHECKURL_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	//The Trash directory
	a = new StringAttribute(httpd.TRASHDIR_P,
				null,
				Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	//The serializer class
	a = new StringAttribute(httpd.SERIALIZER_CLASS_P,
				null,
				Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The URI display toggle when an error occur
	a = new BooleanAttribute(httpd.DISPLAY_URL_ON_ERROR_P,
				 new Boolean(false),
				 Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// The URI display toggle when an error occur
	a = new BooleanAttribute(httpd.LENIENT_P,
				 new Boolean(true),
				 Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * This property set's title.
     * @return A String encoded title.
     */

    public String getTitle() {
	return title;
    }

    /**
     * Set value forwards the effectation to the properties.
     * @param idx The attribute (property in that case) being set.
     * @param value The new value for that property.
     */

    public synchronized void setValue(int idx, Object value) {
	// Check access (we don't care about side effects)
	super.setValue(idx, value);
    }

    GeneralProp(String name, httpd server) {
	super(name, server);
    }
}


