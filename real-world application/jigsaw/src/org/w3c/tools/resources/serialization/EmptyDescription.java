// EmptyDescription.java
// $Id: EmptyDescription.java,v 1.1 2010/06/15 12:28:13 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class EmptyDescription extends ResourceDescription {

    /**
     * Constructor. build the description as an empty description
     * only classname and identifeir are provided.
     * @param classname the resource class name
     */
    public EmptyDescription(String classname, String identifier) {
	super(classname);
	this.identifier = identifier;
	this.classes    = null;
	this.attributes = null;
    }

}
