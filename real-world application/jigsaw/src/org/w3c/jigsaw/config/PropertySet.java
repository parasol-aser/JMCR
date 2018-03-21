// PropertySet.java
// $Id: PropertySet.java,v 1.2 2010/06/15 17:53:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.config ;

import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.ClassAttribute;
import org.w3c.tools.resources.DoubleAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FrameArrayAttribute;
import org.w3c.tools.resources.IllegalAttributeAccess;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.util.ObservableProperties;

import org.w3c.jigsaw.http.httpd;

public class PropertySet extends Resource {
    protected httpd   server         = null;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.config.PropertySet");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Get this property set title.
     * @return A String encoding the title of the property set.
     */

    public String getTitle() {
	return getIdentifier()+" property set.";
    }

    /**
     * Get this resource's help url.
     * @return An URL, encoded as a String, or <strong>null</strong> if not
     * available.
     */

    public String getHelpURL() {
	String docurl = server.getDocumentationURL();
	if ( docurl == null )
	    return null;
	return docurl + "/" + getClass().getName() + ".html";
    }

    /**
     * Get the help URL for that resource's attribute.
     * @param topic The topic (can be an attribute name, or a property, etc).
     * @return A String encoded URL, or <strong>null</strong>.
     */

    public String getHelpURL(String topic) {
	String docurl = server.getDocumentationURL();
	if ( docurl == null )
	    return null;
	Class defines = AttributeRegistry.getAttributeClass(getClass(), topic);
	if ( defines != null ) 
	    return docurl + "/" + defines.getName() + ".html";
	return null;
    }

    /**
     * Set value forwards the effectation to the properties.
     * @param idx The attribute (property in that case) being set.
     * @param value The new value for that property.
     */

    public synchronized void setValue(int idx, Object value) {
	// Check access (we don't care about side effects)
	super.setValue(idx, value);
	if ( idx > ATTR_LAST_MODIFIED ) {
	    Attribute a = attributes[idx];
	    if ( value == null ) {
		server.getProperties().remove(a.getName());
	    } else {
		if ( ! server.getProperties().putValue(a.getName()
						       , a.stringify(value)) )
		    throw new IllegalAttributeAccess(this
						     , getAttributes()[idx]
						     , value);
	    }
	}
    }

    protected Object convertingGet(httpd s, Attribute a, Object def) {
	ObservableProperties p = s.getProperties();
	def = (def == null) ? a.getDefault() : def;
	if ( a instanceof FileAttribute ) {
	    return p.getFile(a.getName(), (File) def);
	} else if ( a instanceof StringAttribute ) {
	    return p.getString(a.getName(), (String) def);
	} else if ( a instanceof IntegerAttribute ) {
	    int d = (def == null) ? -1 : ((Integer) def).intValue();
	    int i = p.getInteger(a.getName(), d);
	    return new Integer(i); 
	} else if ( a instanceof LongAttribute ) {
	    long d = (def == null) ? -1 : ((Long) def).longValue();
	    long l = p.getLong(a.getName(), d);
	    return new Long(l); 
	} else if ( a instanceof BooleanAttribute ) {
	    boolean b = p.getBoolean(a.getName(), (def == Boolean.TRUE));
	    return b ? Boolean.TRUE : Boolean.FALSE;
	} else if ( a instanceof ClassAttribute ) {
	    try {
		String cn = p.getString(a.getName(), null);
		if ( cn == null )
		    return def;
		return Class.forName(cn);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		throw new RuntimeException("Invalid class name.");
	    }
	} else if ( a instanceof StringArrayAttribute ) {
	    return p.getStringArray(a.getName(), null);
	} else if ( a instanceof DoubleAttribute ) {
	    double d = ((def == null) 
			? Double.NaN 
			: ((Double) def).doubleValue());
	    d = p.getDouble(a.getName(), d);
	    return (d == Double.NaN) ? def : new Double(d);
	} else if ( a instanceof FrameArrayAttribute ) {
	    return null; // ugly hack FIXME
	} else {
	    throw new RuntimeException("// FIXME !!!");
	}
    }

    public Object getValue(int idx, Object def) {
	// Check access (again we don't care about side effectes)
	if ( idx <= ATTR_LAST_MODIFIED )
	    return super.getValue(idx, def);
	return convertingGet(server, attributes[idx], def);
    }

    public Object unsafeGetValue(int idx, Object def) {
	// Check access (again we don't care about side effectes)
	if ( idx <= ATTR_LAST_MODIFIED )
	    return super.unsafeGetValue(idx, def);
	return convertingGet(server, attributes[idx], def);
    }   

    public PropertySet(String name, httpd server) {
	super();
	this.server = server;
	setValue(ATTR_IDENTIFIER, name);
    }

    public void initialize(Object values[]) {
	super.initialize(values);
	attributes[ATTR_IDENTIFIER] = new StringAttribute("identifier"
							  , null
							  , 0);
    }
}
