// DAVPropStat.java
// $Id: DAVPropStat.java,v 1.1 2010/06/15 12:25:59 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVPropStat extends DAVNode {

    public String getStatus() {
	return getTextChildValue(STATUS_NODE);
    }

    public void setStatus(String line) {
	addDAVNode(STATUS_NODE, line);
    }

    public String getResponseDescription() {
	return getTextChildValue(RESPONSEDESC_NODE);
    }

    public DAVProperties getProps() {
	Node node = getDAVNode(PROP_NODE);
	if (node != null) {
	    return new DAVProperties((Element)node);
	}
	return null;
    }

    DAVPropStat(Element element) {
	super(element);
    }
    
}
