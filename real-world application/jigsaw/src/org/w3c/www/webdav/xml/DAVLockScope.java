// DAVLockScope.java
// $Id: DAVLockScope.java,v 1.1 2010/06/15 12:26:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVLockScope extends DAVNode {
    
    public static final short UNDEFINED = 0;
    public static final short EXCLUSIVE = 1;
    public static final short SHARED    = 1;

    public short getLockScope() {
	if (getDAVNode(EXCLUSIVE_NODE) != null) {
	    return EXCLUSIVE;
	} else if (getDAVNode(SHARED_NODE) != null) {
	    return SHARED;
	} else {
	    return UNDEFINED;
	}
    }

    DAVLockScope(Element element) {
	super(element);
    }

}
