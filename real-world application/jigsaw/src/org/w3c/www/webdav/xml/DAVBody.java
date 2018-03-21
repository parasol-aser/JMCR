// DAVBody.java
// $Id: DAVBody.java,v 1.1 2010/06/15 12:26:02 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.io.InputStream;
import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DocumentImpl;

import org.w3c.www.webdav.WEBDAV;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVBody implements ErrorHandler {

    protected static DOMParser         parser  = null;

    protected static DOMImplementation domimpl = null;

    private static boolean setValidation    = false; //defaults
    private static boolean setNameSpaces    = true;
    private static boolean setSchemaSupport = true;
    private static boolean setDeferredDOM   = true;

    static {
	parser  = new DOMParser();
	domimpl = (new DocumentImpl()).getImplementation();
	try {
	    parser.setFeature(
                     "http://apache.org/xml/features/dom/defer-node-expansion",
		     setDeferredDOM);
	    parser.setFeature(
		     "http://xml.org/sax/features/validation", 
		     setValidation);
	    parser.setFeature(
		     "http://xml.org/sax/features/namespaces",
		     setNameSpaces);
	    parser.setFeature(
		     "http://apache.org/xml/features/validation/schema",
		     setSchemaSupport);
	} catch (Exception ex) {
            ex.printStackTrace(System.err);
        }	
    }

    protected Document document = null;

    public static synchronized Document getDocument(InputStream in, 
						    ErrorHandler handler) 
	throws IOException, SAXException
    {
	parser.setErrorHandler(handler);
	InputSource source = new InputSource(in);
	parser.parse(source);
	return parser.getDocument();
    }

    //
    // XML part
    //

    public DAVMultiStatus getMultiStatus() {
	Node n = DAVNode.getDAVNode(document, DAVNode.MULTISTATUS_NODE);
	if (n != null) {
	    return new DAVMultiStatus((Element)n);
	}
	return null;
    }

    public DAVPropertyBehavior getPropertyBehavior() {
	Node n = DAVNode.getDAVNode(document, DAVNode.PROPERTYBEHAVIOR_NODE);
	if (n != null) {
	    return new DAVPropertyBehavior((Element)n);
	}
	return null;
    }

    public DAVPropertyUpdate getPropertyUpdate() {
	Node n = DAVNode.getDAVNode(document, DAVNode.PROPERTYUPDATE_NODE);
	if (n != null) {
	    return new DAVPropertyUpdate((Element)n);
	}
	return null;
    }

    public DAVPropFind getPropFind() {
	Node n = DAVNode.getDAVNode(document, DAVNode.PROPFIND_NODE);
	if (n != null) {
	    return new DAVPropFind((Element)n);
	}
	return null;
    }

    public DAVActiveLock getActiveLock() {
	Node n = DAVNode.getDAVNode(document, DAVNode.ACTIVELOCK_NODE);
	if (n != null) {
	    return new DAVActiveLock((Element)n);
	}
	return null;
    }

    public DAVLockInfo getLockInfo() {
	Node n = DAVNode.getDAVNode(document, DAVNode.LOCKINFO_NODE);
	if (n != null) {
	    return new DAVLockInfo((Element)n);
	}
	return null;
    }

    //
    // creation
    //

    public static Document createDocument(String root) {
	Document newdoc = 
	    domimpl.createDocument(WEBDAV.NAMESPACE_URI, 
				   WEBDAV.NAMESPACE_PREFIX+":"+root,
				   null);
	Element rootel = newdoc.getDocumentElement();
	try {
	    rootel.setAttribute("xmlns:"+WEBDAV.NAMESPACE_PREFIX, 
				WEBDAV.NAMESPACE_URI);
	} catch (DOMException ex) {
	    ex.printStackTrace();
	}
	return newdoc;
    }

    public static Document createDocumentNS(String root, 
					    String ns, 
					    String prefix) 
    {
	Document newdoc = createDocument(root);
	Element  rootel = newdoc.getDocumentElement();
	try {
	    rootel.setAttribute("xmlns:"+prefix, ns);
	} catch (DOMException ex) {
	    ex.printStackTrace();
	}
	return newdoc;
    }

    // Error Handler

    /**
     * Warning
     */
    public void warning(SAXParseException ex) {
        System.err.println("[Warning] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /**
     * Error. 
     */
    public void error(SAXParseException ex) {
        System.err.println("[Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /** 
     * Fatal error. 
     */
    public void fatalError(SAXParseException ex) 
	throws SAXException 
    {
        System.err.println("[Fatal Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
        throw ex;
    }

    //
    // Private methods
    //

    /** 
     * Returns a string of the location. 
     */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) 
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    }


    /**
     * Constructor
     */
    public DAVBody(InputStream in) 
	throws IOException, SAXException
    {
	this.document = getDocument(in, this); 
    }

}
