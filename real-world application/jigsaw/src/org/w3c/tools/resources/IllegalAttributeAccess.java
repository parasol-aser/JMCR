// IllegalAttributeAccess.java
// $Id: IllegalAttributeAccess.java,v 1.1 2010/06/15 12:20:24 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic exception for illegal attribute access.
 * Depending on this parameter this exception can indicate:
 * <ul>
 * <li>That an attribute can't be set to a given value.
 * <li>That the attribute isn't defined for the given resource.
 * </ul>
 */

public class IllegalAttributeAccess extends RuntimeException {
    AttributeHolder holder    = null ;
    Attribute       attribute = null ;
    int             idx       = -1 ;
    Object          value     = null ;
    String          accessor  = null ;

    /**
     * This attribute isn't defined by the given holder.
     * @param holder The holder that got the exception.
     * @param attr The unknown atribute.
     */

    public IllegalAttributeAccess(AttributeHolder holder, Attribute attr) {
	super("Unknown attribute " + attr.getName()) ;
	this.holder  = holder ;
	this.attribute = attribute ;
    }

    /**
     * This attribute index isn't valid for the given holder.
     * @param holder The holder that got the exception.
     * @param idx The erred index.
     */

    public IllegalAttributeAccess(AttributeHolder holder, int idx) {
	super("Invalid attribute index "+idx) ;
	this.holder = holder ;
	this.idx      = idx ;
    }

    /**
     * The proposed value for the attribute doesn't match the expected type.
     * @param holder The holder that got the exception.
     * @param attribute The attribute that you were trying to set.
     * @param value The erred value.
     */

    public IllegalAttributeAccess(AttributeHolder holder
				  , Attribute attr
				  , Object value) {
	super("Illegal attribute value " 
	      + ((value == null) ? "null" : value.toString() )
	      + " for " + attr.getName()) ;
	this.holder  = holder ;
	this.attribute = attr ;
	this.value     = value ;
    }

    /**
     * Invalid access to an attribute value.
     * You used an invalid specific accessor to get the value of an attribute.
     * @param holder The holder that got the exception.
     * @param attr The attribute that was accessed.
     * @param accessor The name of the invalid accessor used.
     */

    public IllegalAttributeAccess(AttributeHolder holder
				  , Attribute attr
				  , String accessor) {
	super("Illegal access " + accessor + " to get " +attr.getName()) ;
	this.holder  = holder ;
	this.attribute = attribute ;
	this.accessor  = accessor ;
    }
	
    /**
     * Invalid access to an attribute.
     * @param golder The attribute holder.
     * @param name The name of the attribute that wan't found.
     */

    public IllegalAttributeAccess(AttributeHolder holder, String name) {
	super("Illegal attribute name "+ name) ;
	this.holder = holder ;
    }

}
