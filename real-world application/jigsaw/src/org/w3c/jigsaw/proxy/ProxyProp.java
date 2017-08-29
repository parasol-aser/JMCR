// ProxyProp.java
// $Id: ProxyProp.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.proxy;

import java.util.Enumeration;

import java.io.File;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.config.PropertySet;

import org.w3c.util.ObservableProperties;

import org.w3c.www.protocol.http.micp.MICPProp;

import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheStore;

class ProxyProp extends PropertySet {
    private static String title = "Proxy properties";

    private static String MICP_PROP_NAME       = "micp";
    private static String CACHE_PROP_NAME      = "cache";
    private static String PROXY_DISP_PROP_NAME = "dispatcher";

    protected static int ATTR_FILTERS = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.proxy.ProxyProp");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the maximum number of allowed connections:
	a = new IntegerAttribute(HttpManager.CONN_MAX_P
				 , new Integer(20)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Register the timeout on the client socket
	a = new IntegerAttribute(HttpManager.TIMEOUT_P
				 , new Integer(300000) // default 5mn
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Register the connection timeout on the client socket
	a = new IntegerAttribute(HttpManager.CONN_TIMEOUT_P
				 , new Integer(1000) // default 1s
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
        // Register the proxy set property
	a = new BooleanAttribute(HttpManager.PROXY_SET_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Register the proxy host:
	a = new StringAttribute(HttpManager.PROXY_HOST_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Register the proxy port:
	a = new IntegerAttribute(HttpManager.PROXY_PORT_P
				 , new Integer(80)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
        // Register the lenient parsing mode property
	a = new BooleanAttribute(HttpManager.LENIENT_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Register the filters property
	a = new StringArrayAttribute(HttpManager.FILTERS_PROP_P
				     , null
				     , Attribute.EDITABLE);
	ATTR_FILTERS = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
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

    protected String[] getFilters() {
	return (String[]) getValue(ATTR_FILTERS, null);
    }

    protected void initializeFiltersProps() {
	String flt[] = getFilters();
	if (flt == null)
	    return;
	// FIXME! shouldget the name of the property set associated
	// to the filter, and verify if it is present or not
	// if not, register the property set
	PropertySet p = null;
	for(int i=0; i<flt.length; i++) {
	    if(flt[i].equals("org.w3c.www.protocol.http.micp.MICPFilter")){
		if (server.getPropertySet(MICP_PROP_NAME) == null) {
		    p = new MICPProp(MICP_PROP_NAME, server);
		    server.registerPropertySet(p);
		}
	    } else if (flt[i].equals(
		       "org.w3c.www.protocol.http.cache.CacheFilter")) {
		if (server.getPropertySet(CACHE_PROP_NAME) == null) {
		    p = new CacheProp(CACHE_PROP_NAME, server);
		    server.registerPropertySet(p);
		    ObservableProperties props = server.getProperties();
		    File c = 
			props.getFile(CacheStore.CACHE_DIRECTORY_P, null);
		    if ( c == null ) {
			c = new File(server.getConfigDirectory(), 
				     "cache");
			props.putValue(CacheStore.CACHE_DIRECTORY_P,
				       c.getAbsolutePath());
		    }
		}
	    } else if (flt[i].equals(
		   "org.w3c.www.protocol.http.proxy.ProxyDispatcher")) {
		//add ProxyDispatcher PropertySet...
		if (server.getPropertySet(PROXY_DISP_PROP_NAME) == null) {
		    p = new ProxyDispatcherProp(PROXY_DISP_PROP_NAME, server);
		    server.registerPropertySet(p);
		}
	    }
	}
    }

    public synchronized void setValue(int idx, Object value) {
	// Check access (we don't care about side effects)
	super.setValue(idx, value);
	if ( idx == ATTR_FILTERS )
	    initializeFiltersProps();
    }

    ProxyProp(String name, httpd server) {
	super(name, server);
	initializeFiltersProps();
    }
}
