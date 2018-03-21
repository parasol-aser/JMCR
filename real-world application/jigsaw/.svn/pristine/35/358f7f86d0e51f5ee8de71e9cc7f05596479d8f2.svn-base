// ProxyDispatcherProp.java
// $Id: ProxyDispatcherProp.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy;

import java.io.File;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.jigsaw.http.httpd;

import org.w3c.www.protocol.http.proxy.ProxyDispatcher;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ProxyDispatcherProp extends PropertySet {
    private static String title = "Proxy dispatcher properties";

    protected static int ATTR_RULES = -1;

    protected static int ATTR_DEBUG = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName(
			"org.w3c.jigsaw.proxy.ProxyDispatcherProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	
	//Our proxy rules attribute:
	a = new StringAttribute(ProxyDispatcher.RULE_P,
				null,
				Attribute.EDITABLE);
	ATTR_RULES = AttributeRegistry.registerAttribute(c, a);
	//Our debug Attribute
	a = new BooleanAttribute(ProxyDispatcher.DEBUG_P,
				 Boolean.FALSE,
				 Attribute.EDITABLE);
	ATTR_DEBUG = AttributeRegistry.registerAttribute(c, a);
	//Our debug Attribute
	a = new BooleanAttribute(ProxyDispatcher.CHECK_RULES_LAST_MODIFIED_P,
				 Boolean.FALSE,
				 Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */
    public String getTitle() {
	return title;
    }

    private String getDefaultRulesLocation() {
	File config = server.getConfigDirectory();
	String loc = config.getAbsolutePath()+"/proxy.rls";
	return loc;
    }

    public String getRulesLocation() {
	return (String) getValue(ATTR_RULES, null);
    }

    public boolean getDegugFlag() {
	return getBoolean(ATTR_DEBUG, false);
    }

    public ProxyDispatcherProp(String name, httpd server) {
	super(name, server);
	if (getRulesLocation() == null)
	    setValue(ATTR_RULES, getDefaultRulesLocation());
    }
}
