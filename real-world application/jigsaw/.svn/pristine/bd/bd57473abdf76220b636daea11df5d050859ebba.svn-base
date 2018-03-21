// DAVNode.java
// $Id: DAVNode.java,v 1.1 2010/06/15 12:26:01 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVNode {

    //
    // XML Elements
    //
    public static final String ACTIVELOCK_NODE         = "activelock";
    public static final String LOCKSCOPE_NODE          = "lockscope";
    public static final String LOCKTYPE_NODE           = "locktype";
    public static final String DEPTH_NODE              = "depth";
    public static final String OWNER_NODE              = "owner";
    public static final String TIMEOUT_NODE            = "timeout";
    public static final String LOCKTOKEN_NODE          = "locktoken";
    public static final String LOCKENTRY_NODE          = "lockentry";
    public static final String LOCKINFO_NODE           = "lockinfo";
    public static final String WRITE_NODE              = "write";
    public static final String EXCLUSIVE_NODE          = "exclusive";
    public static final String SHARED_NODE             = "shared";

    public static final String HREF_NODE               = "href";

    public static final String LINK_NODE               = "link";
    public static final String SRC_NODE                = "src";
    public static final String DST_NODE                = "dst";

    public static final String MULTISTATUS_NODE        = "multistatus";
    public static final String RESPONSE_NODE           = "response";
    public static final String PROPSTAT_NODE           = "propstat";
    public static final String RESPONSEDESC_NODE       = "responsedescription";
    public static final String STATUS_NODE             = "status";

    public static final String PROP_NODE               = "prop";

    public static final String PROPERTYBEHAVIOR_NODE   = "propertybehavior";
    public static final String OMIT_NODE               = "omit";
    public static final String KEEPALIVE_NODE          = "keepalive";

    public static final String PROPERTYUPDATE_NODE     = "propertyupdate";
    public static final String REMOVE_NODE             = "remove";
    public static final String SET_NODE                = "set";

    public static final String PROPFIND_NODE           = "propfind";
    public static final String ALLPROP_NODE            = "allprop";
    public static final String PROPNAME_NODE           = "propname";

    public static final String COLLECTION_NODE         = "collection";

    //
    // Property Elements
    //
    public static final String CREATIONDATE_NODE       = "creationdate";
    public static final String DISPLAYNAME_NODE        = "displayname";
    public static final String GETCONTENTLANGUAGE_NODE = "getcontentlanguage";
    public static final String GETCONTENTLENGTH_NODE   = "getcontentlength";
    public static final String GETCONTENTTYPE_NODE     = "getcontenttype";
    public static final String GETETAG_NODE            = "getetag";
    public static final String GETLASTMODIFIED_NODE    = "getlastmodified";
    public static final String LOCKDISCOVERY_NODE      = "lockdiscovery";
    public static final String RESOURCETYPE_NODE       = "resourcetype";
    public static final String SOURCE_NODE             = "source";
    public static final String SUPPORTEDLOCK_NODE      = "supportedlock";
    // 
    // Non std properties, per 
    // http://greenbytes.de/tech/webdav/draft-hopmann-collection-props-00.txt
    public static final String ISCOLLECTION_NODE       = "iscollection";
    public static final String ISSHARED_NODE           = "isshared";
    public static final String ISHIDDEN_NODE           = "ishidden";
    public static final String ISFOLDER_NODE           = "isfolder";

    //
    // Our wrapped XML Element
    //
    protected Element element = null;

    //
    // General methods
    //

    /**
     * Get the first node of the parent matchind the given tagname and
     * namespace.
     * @param parent the parent node
     * @param tagname the tagname to find
     * @param ns the namespace
     * @return a Node instance of null
     */
    public static Node getNodeNS(Node parent, String tagname, String ns) {
	Node current = parent.getFirstChild();
	while (current != null) {
	    if ((current.getNodeType() == current.ELEMENT_NODE) &&
		(current.getLocalName().equals(tagname))) {
		if (ns != null) { 
		    String cns = current.getNamespaceURI();
		    if ((cns != null) && (cns.equals(ns))) {
			return current;
		    }
		} else {
		    return current;
		}
	    }
	    current = current.getNextSibling();
	}
	return null;
    }

    /**
     * Add the given node to the children list of the parent node and
     * Add the Namespace definition if needed (work arround of xmlserializer
     * bug)
     * @param parent the parent node
     * @param child the new child
     */
    public static void addNodeNS(Node parent, Node child) {
	Document doc    = parent.getOwnerDocument();
	String   ns     = child.getNamespaceURI();
	String   prefix = child.getPrefix();
	if (ns != null) {
	    if (prefix != null) {
		Element rootel = doc.getDocumentElement();
		String arg = "xmlns:"+prefix;
		if (rootel.getAttribute(arg).equals("")) {
		    rootel.setAttribute(arg, ns);
		}
	    } else if (ns.equals(WEBDAV.NAMESPACE_URI)) {
		setPrefix(child, WEBDAV.NAMESPACE_PREFIX, ns);
	    }
	}
	parent.appendChild(child);
    }

    public static void exportChildren(Document newdoc, 
				      Node newparent,
				      Node node,
				      boolean deep) 
	throws DOMException
    {
	Node current = node.getFirstChild();
	while (current != null) {
	    if (current.getNodeType() == current.ELEMENT_NODE) {
		Node newnode = newdoc.importNode(current, deep);
		addNodeNS(newparent, newnode);
	    }
	    current = current.getNextSibling();
	}
    }

    public static Node importNode(Document newdoc, 
				  Element parent,
				  Node node, 
				  boolean deep) {
	Element rootel  = newdoc.getDocumentElement();
	Node    newnode = newdoc.importNode(node, deep);
	String ns = newnode.getNamespaceURI();
	if (ns != null) {
	    // check for declaration
	    String prefix = newnode.getPrefix();
	    if (prefix != null) {
		String arg = "xmlns:"+prefix;
		String ons = rootel.getAttribute(arg);
		if (ons.equals("")) {
		    rootel.setAttribute(arg, ns);
		} else if (! ons.equals(ns)) {
		    // change prefix
		    prefix = "D"+prefix;
		    arg = "xmlns:"+prefix;
		    while ((ons = rootel.getAttribute(arg)) != null) {
			prefix = "D"+prefix;
			arg = "xmlns:"+prefix;
		    }
		    rootel.setAttribute(arg, ns);
		    setPrefix(newnode, prefix, ns);
		}
	    } else if (ns.equals(WEBDAV.NAMESPACE_URI)) {
		// add DAV prefix
		 setPrefix(newnode, WEBDAV.NAMESPACE_PREFIX, ns);
	    }
	}
	parent.appendChild(newnode);
	return newnode;
    }

    private static void setPrefix(Node node, String prefix, String ns) {
	String nns = node.getNamespaceURI();
	if ((nns != null) && (nns.equals(ns))) {
	    node.setPrefix(prefix);
	}
	Node current = node.getFirstChild();
	while (current != null) {
	    if (current.getNodeType() == current.ELEMENT_NODE) {
		setPrefix(current, prefix, ns);
	    }
	    current = current.getNextSibling();
	}
    }

    /**
     * Get our Node
     * @return a Node instance
     */
    public Node getNode() {
	return element;
    }

    /**
     * Get all the element that are chidren of the current node.
     * @return a array of Element
     */
    public Element[] getChildrenElements() {
	Vector v       = new Vector();
	Node   current = element.getFirstChild();
	while (current != null) {
	    if (current.getNodeType() == current.ELEMENT_NODE) {
		v.addElement((Element)current);
	    }
	    current = current.getNextSibling();
	}
	Element elements[] = new Element[v.size()];
	v.copyInto(elements);
	return elements;
    }    

    /**
     * Get the first node matching the given name and the given namespace
     * @param tagname the node name
     * @param ns the namespace
     * @return a Node instance or null
     */
    public Node getNodeNS(String tagname, String ns) {
	return getNodeNS(element, tagname, ns);
    }

    /**
     * Get the value of the fist child TEXT node.
     * @return a String
     */
    public String getTextValue() {
	return getTextChildValue(element);
    }

    /**
     * Get the value of the first child text node (if any)
     * @param node the parent node (can be null)
     * @return a String instance or null
     */
    public String getTextChildValue(Node node) {
	if (node == null) {
	    return null;
	}
	Node  current = node.getFirstChild();
	while (current != null) {
	    if (current.getNodeType() == current.TEXT_NODE) {
		return current.getNodeValue();
	    }
	    current = current.getNextSibling();
	}
	return null;
    }

    /**
     * Get the value of the first child text node (if any)
     * @param tagname the parent node name
     * @param ns the namespace to match
     * @return a String instance or null
     */
    public String getTextChildValueNS(String tagname, String ns) {
	return getTextChildValue(getNodeNS(tagname, ns));
    }

    /**
     * Add this node to our children
     * @param node the new node
     */
    public void addNode(Node node) {
	element.appendChild(node);
    }

    /**
     * Add the given node to our children list.
     * Add the Namespace definition if needed (work arround of xmlserializer
     * bug)
     * @param parent the parent node
     * @param child the new child
     */
    public void addNodeNS(Node node) {
	addNodeNS(element, node);
    }

    //
    // DAV specific methods (ns="DAV:")
    //

    /**
     * Get the list of children element that match the given tagname 
     * (and the DAV namespace)
     * @param node the parent node
     * @param tagname the tagname to match
     * @return a Vector instance
     */
    public static Vector getDAVElementsByTagName(Node node, String tagname) {
	Vector v       = new Vector();
	Node   current = node.getFirstChild();
	while (current != null) {
	    if ((current.getNodeType() == current.ELEMENT_NODE) &&
		(current.getLocalName().equals(tagname)) &&
		(current.getNamespaceURI() != null) &&
		(current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
		v.addElement(current);
	    }
	    current = current.getNextSibling();
	}
	return v;
    }

    /**
     * Get the first child element that match the given tagname 
     * (and the DAV namespace)
     * @param node the parent node
     * @param tagname the tagname to match
     * @return a Vector instance
     */
    public static Node getDAVNode(Node node, String tagname) {
	Node current = node.getFirstChild();
	while (current != null) {
	    if ((current.getNodeType() == current.ELEMENT_NODE) &&
		(current.getLocalName().equals(tagname)) &&
		(current.getNamespaceURI() != null) &&
		(current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
		return current;
	    }
	    current = current.getNextSibling();
	}
	return null;
    }

    /**
     * Create a new node and add it to the parent children list.
     * @param parent the parent node
     * @param name the tagname of the new node
     * @param textvalue the nodevalue of the TextNode (child of the new node)
     * @return the newly added Element
     */
    public static Element addDAVNode(Node parent, 
				     String name, 
				     String textvalue)
	throws DOMException
    {
	Document doc = parent.getOwnerDocument();
	Element  el  = doc.createElementNS(WEBDAV.NAMESPACE_URI, 
					   WEBDAV.NAMESPACE_PREFIX+":"+name);
	if (textvalue != null) {
	    el.appendChild(doc.createTextNode(textvalue));
	}
	parent.appendChild(el);
	return el;
    }

    /**
     * Get the text value of all our "DAV:" children matching the given
     * tagname with a text value available.
     * @param node the parent node
     * @param tagname the tagname to search
     * @return a String array
     */
    public static String[] getMultipleTextChildValue(Node node, 
						     String tagname) 
    {
	Vector v       = new Vector();
	Node   current = node.getFirstChild();
	while (current != null) {
	    if ((current.getNodeType() == current.TEXT_NODE) && 
		(current.getLocalName().equals(tagname)) && 
		(current.getNamespaceURI() != null) &&
		(current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
		v.addElement(current.getNodeValue());
	    }
	    current = current.getNextSibling();
	}
	String array[] = new String[v.size()];
	v.copyInto(array);
	return array;
    }

    /**
     * Just for child, not the all tree.
     * @param tagname the tagname
     * @return a Vector of Node.
     */
    public Vector getDAVElementsByTagName(String tagname) {
	return getDAVElementsByTagName(element, tagname);
    }

    /**
     * Get the first node matching the given name
     * @param tagname the node name
     * @return a Node instance or null
     */
    public Node getDAVNode(String tagname) {
	return getDAVNode(element, tagname);
    }

    /**
     * Get the value of the first "DAV:" child text node (if any)
     * @param tagname the parent node name
     * @return a String instance or null
     */
    public String getTextChildValue(String tagname) {
	return getTextChildValue(getDAVNode(tagname));
    }

    /**
     * Get the text value of all our "DAV:" children matching the given
     * tagname with a text value available.
     * @param tagname the tagname to search
     * @return a String array
     */
    public String[] getMultipleTextChildValue(String tagname) {
	return getMultipleTextChildValue(element, tagname);
    }

    // cached value
    String nodenames[] = null;

    /**
     * Get the tagnames of all our DAV children
     * @return a String array
     */
    public String[] getDAVNodeNames() {
	if (nodenames == null) {
	    Node  current = element.getFirstChild();
	    Vector   v    = new Vector();
	    while (current != null) {
		if ((current.getNodeType() == current.ELEMENT_NODE) &&
		    (current.getNamespaceURI() != null) &&
		    (current.getNamespaceURI().equals(WEBDAV.NAMESPACE_URI))) {
		    v.addElement(current.getLocalName());
		}
		current = current.getNextSibling();
	    }
	    nodenames = new String[v.size()];
	    v.copyInto(nodenames);
	}
	return nodenames;
    }

    //
    // Creation
    //

    /**
     * Create a new node.
     * @param name the tagname of the new node
     * @param textvalue the nodevalue of the TextNode (child of the new node)
     * @return the newly added Element
     */
    public Element addDAVNode(String name, String textvalue) 
	throws DOMException
    {
	return addDAVNode(element, name, textvalue);
    }

    /**
     * Add the given node the our children list
     * @param node the new child
     */
    public void addDAVNode(DAVNode node) {
	element.appendChild(node.getNode());
    }

    /**
     * Add the given nodes the our children list
     * @param nodes the new children
     */
    public void addDAVNodes(DAVNode nodes[]) {
	if (nodes != null) {
	    int len = nodes.length;
	    for (int i = 0 ; i < len ; i++) {
		element.appendChild(nodes[i].getNode());
	    }
	}
    }

    public boolean equals(DAVNode node) {
	return (element == node.getNode());
    }

    /**
     * Constructor
     */
    DAVNode(Element element) {
	this.element = element;
    }

}
