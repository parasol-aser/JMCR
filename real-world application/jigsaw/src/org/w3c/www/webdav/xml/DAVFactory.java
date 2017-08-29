// DAVFactory.java
// $Id: DAVFactory.java,v 1.1 2010/06/15 12:25:59 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVFactory {
    
    protected static Element createDAVElement(Document doc, String name) {
	Element  el = doc.createElementNS(WEBDAV.NAMESPACE_URI, 
					  WEBDAV.NAMESPACE_PREFIX+":"+name);
	return el;
    }

    /**
     * Create a DAVNode
     * @param el the DOM Element
     * @return a DAVNode instance
     */
    public static DAVNode createDAVNode(Element el) {
	return new DAVNode(el);
    }

    /**
     * Create a DAVProperties node.
     * @param document the xml document
     * @return a DAVProperties instance
     */
    public static DAVProperties createProperties(Document document) {
	Element el = createDAVElement(document, DAVNode.PROP_NODE);
	return new DAVProperties(el);
    }

    /**
     * Create a DAVProperties node.
     * @param element the prop node
     * @return a DAVProperties instance
     */
    public static DAVProperties createProperties(Element el) {
	return new DAVProperties(el);
    }

    public static DAVPropStat createPropStat(String status, 
					     Document document) 
    {
	Element     el  = createDAVElement(document, DAVNode.PROPSTAT_NODE);
	DAVPropStat dps = new DAVPropStat(el);
	dps.setStatus(status);
	return dps;
    }

    public static DAVPropStat createPropStat(String status, 
					     String propname,
					     Document document) 
    {
	Element     el  = createDAVElement(document, DAVNode.PROPSTAT_NODE);
	DAVPropStat dps = new DAVPropStat(el);
	dps.setStatus(status);
	DAVProperties dp = createProperties(document);
	dp.addProperty(propname);
	dps.addDAVNode(dp);
	return dps;
    }

    public static DAVPropStat createPropStatNS(String status, 
					       Node node,
					       Document document) 
    {
	Element     el  = createDAVElement(document, DAVNode.PROPSTAT_NODE);
	DAVPropStat dps = new DAVPropStat(el);
	dps.setStatus(status);
	DAVProperties dp = createProperties(document);
	dp.addNodeNS(node);
	dps.addDAVNode(dp);
	return dps;
    }

    public static DAVPropStat createPropStat(String status, 
					     DAVProperties props,
					     Document document) 
    {
	Element     el  = createDAVElement(document, DAVNode.PROPSTAT_NODE);
	DAVPropStat dps = new DAVPropStat(el);
	dps.setStatus(status);
	dps.addDAVNode(props);
	return dps;
    }

    public static DAVResponse createResponse(String url, Document document) {
	Element     el = createDAVElement(document, DAVNode.RESPONSE_NODE);
	DAVResponse dr = new DAVResponse(el);
	dr.addDAVNode(DAVNode.HREF_NODE, url);
	return dr;
    }

    public static DAVResponse createResponse(String url, 
					     String status,
					     Document document) 
    {
	Element     el = createDAVElement(document, DAVNode.RESPONSE_NODE);
	DAVResponse dr = new DAVResponse(el);
	dr.addDAVNode(DAVNode.HREF_NODE, url);
	dr.addDAVNode(DAVNode.STATUS_NODE, status);
	return dr;
    }

    public static DAVResponse createResponse(String url, 
					     String status,
					     String description,
					     Document document) 
    {
	Element     el = createDAVElement(document, DAVNode.RESPONSE_NODE);
	DAVResponse dr = new DAVResponse(el);
	dr.addDAVNode(DAVNode.HREF_NODE, url);
	dr.addDAVNode(DAVNode.STATUS_NODE, status);
	dr.addDAVNode(DAVNode.RESPONSEDESC_NODE, description);
	return dr;
    }

    public static DAVResponse createPropStatResponse(String url,
						     String status,
						     DAVProperties props,
						     Document document)
    {
	DAVPropStat dps = createPropStat(status, props, document);
	Element     el  = createDAVElement(document, DAVNode.RESPONSE_NODE);
	DAVResponse dr  = new DAVResponse(el);
	dr.addDAVNode(DAVNode.HREF_NODE, url);
	dr.addDAVNode(dps);
	return dr;
    }

    public static DAVPropAction createPropAction(int type, 
						 DAVProperties props,
						 Document document)
    {
	Element el = null;
	if (type == DAVPropAction.SET) {
	    el = createDAVElement(document, DAVNode.SET_NODE);
	} else {
	    el = createDAVElement(document, DAVNode.REMOVE_NODE);
	}
	el.appendChild(props.getNode());
	return new DAVPropAction(el);
    }

    public static DAVPropertyUpdate createPropertyUpdate(DAVPropAction act[],
							 Document document)
    {
	Element el = createDAVElement(document, DAVNode.PROPERTYUPDATE_NODE);
	DAVPropertyUpdate dpu = new DAVPropertyUpdate(el);
	dpu.setActions(act);
	return dpu;
    }

    public static DAVPropertyUpdate createPropertyUpdate(DAVPropAction act,
							 Document document)
    {
	Element el = createDAVElement(document, DAVNode.PROPERTYUPDATE_NODE);
	DAVPropertyUpdate dpu = new DAVPropertyUpdate(el);
	DAVPropAction array[] = { act };
	dpu.setActions(array);
	return dpu;
    }

    public static DAVPropertyUpdate createPropertyUpdate(Element el,
							 DAVPropAction act)
    {
	DAVPropertyUpdate dpu = new DAVPropertyUpdate(el);
	DAVPropAction array[] = { act };
	dpu.setActions(array);
	return dpu;
    }

    public static DAVMultiStatus createMultiStatus(Element el) {
	return new DAVMultiStatus(el);
    }

}
