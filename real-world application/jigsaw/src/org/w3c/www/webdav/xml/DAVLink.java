// DAVLink.java
// $Id: DAVLink.java,v 1.1 2010/06/15 12:26:01 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Vector;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVLink extends DAVNode {

    public String[] getSources() {
	return getMultipleTextChildValue(SRC_NODE);
    }

    public String[] getDestinations() {
	return getMultipleTextChildValue(DST_NODE);
    }

    DAVLink(Element element) {
	super(element);
    }
}
