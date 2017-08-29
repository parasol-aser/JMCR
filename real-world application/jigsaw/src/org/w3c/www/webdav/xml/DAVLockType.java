// DAVLockType.java
// $Id: DAVLockType.java,v 1.1 2010/06/15 12:26:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVLockType extends DAVNode {
    
    public final static short UNDEFINED = 0;
    public final static short WRITE     = 1;

    public short getType() {
	if (getDAVNode(WRITE_NODE) != null) {
	    return WRITE;
	} else {
	    return UNDEFINED;
	}
    }

    DAVLockType(Element element) {
	super(element);
    }
}
