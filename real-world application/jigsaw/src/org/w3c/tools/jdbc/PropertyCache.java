// PropertyCache.java
// $Id: PropertyCache.java,v 1.1 2010/06/15 12:27:30 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc;

import java.util.Hashtable;
import java.util.Enumeration;
import java.beans.PropertyDescriptor;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PropertyCache {

    public static boolean debug = false;

    /**
     * The modified properties
     */
    protected static Hashtable properties = new Hashtable();

    protected static String getId(JdbcBeanInterface bean, String property) {
	StringBuffer buffer = 
	    new StringBuffer(String.valueOf(bean.hashCode()));
	buffer.append(".").append(property);
	return buffer.toString();
    }

    public static void addProperty(JdbcBeanInterface bean, 
				   String property, 
				   Object value) 
    {
	String id = getId(bean, property);
	if (id != null) {
	    if (debug) {
		System.out.println("add property in cache: "+id+" = "+value);
	    }
	    properties.put(id, value);
	}
    }

    public static Object getProperty(JdbcBeanInterface bean,
				     PropertyDescriptor pd) 
    {
	String id = getId(bean, pd.getName());
	if (id != null) {
	    return properties.get(id);
	}
	return null;
    }

    public static void removeProperties(JdbcBeanInterface bean) {
	Enumeration keys = properties.keys();
	StringBuffer buffer = 
	    new StringBuffer(String.valueOf(bean.hashCode()));
	buffer.append(".");
	String beankey = buffer.toString();
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    if (key.startsWith(beankey)) {
		if (debug) {
		    System.out.println("remove property from cache: "+beankey);
		}
		properties.remove(key);
	    }
	}
    }
}
