// ResourceShadower.java
// $Id: ResourceShadower.java,v 1.1 2010/06/15 12:20:16 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * This interface describe the <em>proxy</em> pattern.
 * Resource can be proxied: a given resource can act as if it was some other
 * resource; this interface describe how to access the proxy resource 
 * attributes in such cases.
 */

public interface ResourceShadower {

    /**
     * Get the resource shadowed by this object.
     * @return A Resource instance, or <strong>null</strong>
     */

    public Resource getTargetResource() ;

    /**
     * Get the list of attributes shadowed byt htis shadowing resource.
     * @return The attribute list of the shadowed object.
     */

    public Attribute[] getTargetAttributes() ;

    /**
     * Get a shadowed attribute value.
     * @param idx The index of the shadowed attribute.
     * @param def The default return value (if no shadow value defined).
     * @return The shadowed attribute value, of the provided default.
     */

    public Object getTargetValue(int idx, Object def) ;

    /**
     * Get a shadowed attribute value (by name).
     * @param name The name of the shadowed attribute.
     * @param def The default return value (if no shadow value defined).
     * @return The shadowed attribute value, of the provided default.
     */

    public Object getTargetValue(String name, Object def) ;

    /**
     * Set a shadowed attribute value.
     * @param idx The index of the attribute to set.
     * @param value Its new value.
     */

    public void setTargetValue(int idx, Object value) ;

    /**
     * Set a shadowed attribute value by name.
     * @param name The name of the shadowed attribute.
     * @param value Its new value.
     */

    public void setTargetValue(String name, Object def) ;

    /**
     * Does this shadow object defines the given attribute.
     * @param idx The index of the shadowed attribute to test.
     */

    public boolean definesTargetAttribute(int idx) ;

    /**
     * Does this shadow object defines the given attribute (by name).
     * @param name The name of the target attribute.
     * @return A boolean <strong>true</strong> if attribute is defined.
     */

    public boolean definesTargetAttribute(String name) ;

}
