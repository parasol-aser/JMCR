// MICPProp.java
// $Id: MICPProp.java,v 1.2 2010/06/15 17:53:09 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.www.protocol.http.HttpManager;

public class MICPProp extends PropertySet {
    private static String title = "mICP properties";

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.www.protocol.http.micp.MICPProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// port on which we will listen
	a = new IntegerAttribute(MICPFilter.PORT_P
				 , new Integer(2005)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// the multicast address
	a = new StringAttribute(MICPFilter.ADDRESS_P
				, "224.0.2.67"
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
        // the timeout, in ms
	a = new IntegerAttribute(MICPFilter.TIMEOUT_P
				 , new Integer(300)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// disable cache
	a = new BooleanAttribute(MICPFilter.DISABLE_CACHE_P
				 , new Boolean(false)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// debug?
	a = new BooleanAttribute(MICPFilter.DEBUG_P
				 , new Boolean(false)
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

    public MICPProp(String name, httpd server) {
	super(name, server);
    }
}
