// DAVMultiStatus.java
// $Id: DAVMultiStatus.java,v 1.1 2010/06/15 12:26:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVMultiStatus extends DAVNode {
    
    public DAVResponse[] getDAVResponses() {
	Vector      list          = getDAVElementsByTagName(RESPONSE_NODE);
	int         len           = list.size();
	DAVResponse responses[]   = new DAVResponse[len];
	for (int i = 0 ; i < len ; i++) {
	    responses[i] = new DAVResponse((Element)list.elementAt(i));
	}
	return responses;
    }

    public String getResponseDescription() {
	Node node = getDAVNode(RESPONSEDESC_NODE);
	if (node != null) {
	    return getTextChildValue(node);
	}
	return null;
    }

    DAVMultiStatus(Element element) {
	super(element);
    }

}
