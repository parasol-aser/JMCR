// DAVPropertyUpdate.java
// $Id: DAVPropertyUpdate.java,v 1.1 2010/06/15 12:26:01 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVPropertyUpdate extends DAVNode {

    public DAVPropAction[] getActions() {
	Vector v       = new Vector();
	Node   current = element.getFirstChild();
	while (current != null) {
	    if ((current.getNodeType() == current.ELEMENT_NODE) &&
		(current.getLocalName().equals(SET_NODE) ||
		 current.getLocalName().equals(REMOVE_NODE)) && 
		(current.getNamespaceURI() != null) &&
		(current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
		v.addElement(new DAVPropAction((Element)current));
	    }
	    current = current.getNextSibling();
	}
	DAVPropAction dpa[] = new DAVPropAction[v.size()];
	v.copyInto(dpa);
	return dpa;
    }

    public void setActions(DAVPropAction actions[]) {
	int len = actions.length;
	for (int i = 0 ; i < len ; i++) {
	    element.appendChild(actions[i].getNode());
	}
    }

    DAVPropertyUpdate(Element element) {
	super(element);
    }

}

