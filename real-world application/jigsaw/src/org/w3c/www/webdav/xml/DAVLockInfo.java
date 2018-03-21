// DAVLockInfo.java
// $Id: DAVLockInfo.java,v 1.1 2010/06/15 12:26:02 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVLockInfo extends DAVLockEntry {
    
    public Node getOwner() {
	return getDAVNode(OWNER_NODE);
    }

    DAVLockInfo(Element element) {
	super(element);
    }

}
