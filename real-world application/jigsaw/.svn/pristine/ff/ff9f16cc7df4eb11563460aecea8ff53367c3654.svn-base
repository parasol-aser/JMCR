// DAVProperties.java
// $Id: DAVProperties.java,v 1.1 2010/06/15 12:25:59 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;


/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVProperties extends DAVNode {

    public void addProperty(String name, String value) {
	addDAVNode(name, value);
    }

    public void addProperty(String name) {
	addDAVNode(name, null);
    }

    public Element[] getProperties() {
	return getChildrenElements();
    }
    
    public String[] getPropertyNames() {
	return getDAVNodeNames();
    }

    public String getProperty(String propname) {
	return getTextChildValue(propname);
    }

    public Date getCreationDate() {
	String creationdate = getTextChildValue(CREATIONDATE_NODE);
	if (creationdate != null) {
	    try {
		return DateParser.parse(creationdate);
	    } catch (InvalidDateException ex) {
		return null;
	    }
	}
	return null;
    }

    public String getDisplayName() {
	return getTextChildValue(DISPLAYNAME_NODE);
    }

    public String getContentLanguage() {
	return getTextChildValue(GETCONTENTLANGUAGE_NODE);
    }

    public String getContentType() {
	return getTextChildValue(GETCONTENTTYPE_NODE);
    }

    public int getContentLength() {
	String cl = getTextChildValue(GETCONTENTLENGTH_NODE);
	if (cl != null) {
	    try {
		return Integer.parseInt(cl);
	    } catch (Exception ex) {
		return -1;
	    }
	} else {
	    return -1;
	}
    }

    public String getETag() {
	return getTextChildValue(GETETAG_NODE);
    }

    public Date getLastModified() {
	String lastmodified = getTextChildValue(GETLASTMODIFIED_NODE);
	if (lastmodified != null) {
	    try {
		return DateParser.parse(lastmodified);
	    } catch (InvalidDateException ex) {
		return null;
	    }
	}
	return null;
    }

    public String getLockDiscovery() {
	return getTextChildValue(LOCKDISCOVERY_NODE);
    }

    public String getResourceType() {
	return getTextChildValue(RESOURCETYPE_NODE);
    }

    public DAVLink[] getSources() {
	Vector  vnodes  = getDAVElementsByTagName(SOURCE_NODE);
	int     len     = vnodes.size();
	DAVLink links[] = new DAVLink[len];
	for (int i = 0 ; i < len ; i++) { 
	    links[i] = new DAVLink((Element)vnodes.elementAt(i));
	}
	return links;
    }

    public DAVLockEntry[] getSupportedLocks() {
	Vector       vnodes  = getDAVElementsByTagName(SUPPORTEDLOCK_NODE);
	int          len     = vnodes.size();
	DAVLockEntry locks[] = new DAVLockEntry[len];
	for (int i = 0 ; i < len ; i++) { 
	    locks[i] = new DAVLockEntry((Element)vnodes.elementAt(i));
	}
	return locks;
    }

    public void setResourceType(String type) {
	Document doc   = element.getOwnerDocument();
	Node     rtn   = DAVNode.getDAVNode(element, RESOURCETYPE_NODE);
	Element  rtype = DAVFactory.createDAVElement(doc, RESOURCETYPE_NODE);
	if (type != null) {
	    DAVNode.addDAVNode(rtype, type, null);
	}
	if (rtn != null) {
	    element.replaceChild(rtype, rtn);
	} else {
	    element.appendChild(rtype);
	}
    }

    DAVProperties(Element element) {
	super(element);
    }

}
