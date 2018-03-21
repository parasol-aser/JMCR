// XMLDescrReader.java
// $Id: XMLDescrReader.java,v 1.2 2010/06/15 17:53:01 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.IOException;
import java.io.Reader;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.UnknownResource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.serialization.SerializationException;
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.EmptyDescription;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLDescrReader extends HandlerBase implements JigXML{



    Parser              parser           = null;
    ResourceDescription resources[]      = null;

    Reader          reader           = null;
    Stack           resourceSetStack = null;
    Stack           resourceStack    = null;
    Stack           defsStack        = null;
    Stack           classes          = null;
    Stack           interfaces       = null;
    Stack           FAStack          = null;
    Vector          children         = null;
    SimpleAttribute currentS         = null;
    ArrayAttribute  currentA         = null;
    int             length           = -1;
    boolean         isavalue         = false;
    String          array[]          = null;
    int             arrayidx         = -1;
    StringBuffer    characters       = null;
    String          charvalue        = null;

    public void startElement(String name, AttributeList attributes) 
	throws SAXException
    {
	endCharacters();
	String iname = name.intern();
	if (iname == iINHERIT_TAG) {
	    Vector vclasses = (Vector)classes.peek();
	    vclasses.addElement(attributes.getValue(CLASS_ATTR));
	} else if (iname == iIMPLEMENTS_TAG) {
	    Vector vinterfaces = (Vector)interfaces.peek();
	    vinterfaces.addElement(attributes.getValue(CLASS_ATTR));
	} else if (iname == iCHILDREN_TAG) {
	    length   = Integer.parseInt(attributes.getValue(LENGTH_ATTR));
	    array    = new String[length];
	    arrayidx = 0;
	} else if (iname == iDESCR_TAG) {
	    String resourceclass = attributes.getValue(CLASS_ATTR);
	    String identifier    = attributes.getValue(NAME_ATTR);
	    if (identifier.equals(NULL))
		identifier = null;
	    resourceStack.push(new EmptyDescription(resourceclass,
						    identifier));
	    
	} else if (iname == iRESOURCE_TAG) {
	    String resourceclass = attributes.getValue(CLASS_ATTR);
	    resourceStack.push(new ResourceDescription(resourceclass));
	    defsStack.push(new Vector(10));
	    Vector vcls = new Vector(8);
	    vcls.addElement(resourceclass);
	    classes.push(vcls);
	    interfaces.push(new Vector(8));
	} else if (iname == iATTRIBUTE_TAG) {
	    String attrclass = attributes.getValue(CLASS_ATTR);
	    try {
		    //Added by Jeff Huang
		    //TODO: FIXIT
		Class c = Class.forName(attrclass);
		currentS = (SimpleAttribute) c.newInstance();
		currentS.setName(attributes.getValue(NAME_ATTR));
		currentS.setFlag(attributes.getValue(FLAG_ATTR));
	    } catch (Exception ex) {
		ex.printStackTrace();
		currentS = null;
	    }
	} else if (iname == iARRAY_TAG) {
	    String attrclass = attributes.getValue(CLASS_ATTR);
	    try {
		Class c  = Class.forName(attrclass);
		currentA = (ArrayAttribute) c.newInstance();
		currentA.setName(attributes.getValue(NAME_ATTR));
		currentA.setFlag(attributes.getValue(FLAG_ATTR));
		length   = Integer.parseInt(attributes.getValue(LENGTH_ATTR));
		array    = new String[length];
		arrayidx = 0;
	    } catch (Exception ex) {
		ex.printStackTrace();
		currentA = null;
	    }
	} else if (iname == iRESARRAY_TAG) {
	    resourceSetStack.push(new Vector(10));
	    String attrclass = attributes.getValue(CLASS_ATTR);
	    try {
		Class     c    = Class.forName(attrclass);
		Attribute attr = (Attribute)c.newInstance();
		attr.setName(attributes.getValue(NAME_ATTR));
		FAStack.push(attr);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new SAXException("Unable to create an instance of "+
				       attrclass);
	    }
	} else if ((iname == iCHILD_TAG) || (iname == iVALUE_TAG)) {
	    isavalue = true;
	}
    }

    public void endElement(String name) 
	throws SAXException
    {
	endCharacters();
	String iname = name.intern();
	if (iname == iRESOURCE_TAG) {
	    ResourceDescription res = (ResourceDescription)resourceStack.pop();
	    res.setAttributeDescriptions((Vector)defsStack.pop());
	    Vector vresources = (Vector)resourceSetStack.peek();
	    vresources.addElement(res);
	    Vector vclasses = (Vector)classes.pop();
	    res.setClassHierarchy(vclasses);
	    Vector vinterfaces = (Vector)interfaces.pop();
	    res.setInterfaces(vinterfaces);
	} else if (iname == iCHILDREN_TAG) {
	    ResourceDescription res = 
		(ResourceDescription)resourceStack.peek();
	    res.setChildren(array);
	} else if (iname == iDESCR_TAG) {
	    ResourceDescription res = (ResourceDescription)resourceStack.pop();
	    Vector vresources = (Vector)resourceSetStack.peek();
	    vresources.addElement(res);
	} else if (iname == iATTRIBUTE_TAG) {
	    currentS = null;
	} else if (iname == iARRAY_TAG) {
	    AttributeDescription ad = 
		new AttributeDescription(currentA, currentA.unpickle(array));
	    Vector attrs = (Vector)defsStack.peek();
	    attrs.addElement(ad);
	    currentA = null;
	} else if (iname == iRESARRAY_TAG) {
	    Vector vframes = (Vector) resourceSetStack.pop();
	    ResourceDescription frames[] = 
		new ResourceDescription[vframes.size()];
	    vframes.copyInto(frames);
	    AttributeDescription ad = 
		new AttributeDescription((Attribute)FAStack.pop(),
					 frames);
	    Vector attrs = (Vector)defsStack.peek();
	    attrs.addElement(ad);
	} else if ((iname== iCHILD_TAG) || (iname == iVALUE_TAG)) {
	    isavalue = false;
	}
    }

    public void startDocument() 
	throws SAXException
    {
	defsStack        = new Stack();
	classes          = new Stack();
	interfaces       = new Stack();
	FAStack          = new Stack();
	resourceStack    = new Stack();
	resourceSetStack = new Stack();
	resourceSetStack.push(new Vector(10));
    }

    public void endDocument() 
	throws SAXException
    {
	Vector vresources = (Vector) resourceSetStack.pop();
	resources = new ResourceDescription[vresources.size()];
	vresources.copyInto(resources);
	try {
	    reader.close();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }
	 
    public void characters(char ch[],
			   int start,
			   int length) 
	throws SAXException
    {
	if (charvalue == null) {
	    charvalue = new String(ch, start, length);
	} else if (length > 0) {
	    if (characters == null) {
		characters = new StringBuffer(charvalue);
	    }
	    characters.append(ch, start, length);
	}
    }

    private void endCharacters() {
	String value;
	if (charvalue == null) {
	    return;
	}
	if (characters == null) {
	    value = charvalue;
	} else {
	    value = characters.toString();
	}
	characters = null;
	charvalue  = null;
	if (currentS != null) {
	    AttributeDescription ad = null;
	    if (value.equals(NULL))
		ad = new AttributeDescription(currentS, null);
	    else
		ad = new AttributeDescription(currentS, 
					      currentS.unpickle(value));
	    Vector attrs = (Vector)defsStack.peek();
	    attrs.addElement(ad);
	} else if (isavalue) {
	    array[arrayidx++] = value;
	}
    }

    public void warning(SAXParseException e) 
	throws SAXException
    {
	System.out.println("WARNING in element "+e.getPublicId());
	System.out.println("Sys  : "+e.getSystemId());
	System.out.println("Line : "+e.getLineNumber());
	System.out.println("Col  : "+e.getColumnNumber());
	e.printStackTrace();
    }

    public void error(SAXParseException e) 
	throws SAXException
    {
	System.out.println("ERROR in element "+e.getPublicId());
	System.out.println("Sys  : "+e.getSystemId());
	System.out.println("Line : "+e.getLineNumber());
	System.out.println("Col  : "+e.getColumnNumber());
	e.printStackTrace();
    }

    public void fatalError(SAXParseException e) 
	throws SAXException
    {
	System.out.println("FATAL ERROR in element "+e.getPublicId());
	System.out.println("Sys  : "+e.getSystemId());
	System.out.println("Line : "+e.getLineNumber());
	System.out.println("Col  : "+e.getColumnNumber());
	e.printStackTrace();
    }

    protected void parse() 
	throws SAXException, IOException
    {
	try {
	    parser.setDocumentHandler(this);
	    parser.setErrorHandler(this);
	    parser.parse(new InputSource(reader));
	} catch (IOException ex) {
	    try { reader.close(); } catch (IOException ioex) {}
	    throw ex;
	} catch (SAXException sex) {
	    try { reader.close(); } catch (IOException ioex) {}
	    throw sex;
	}
    }

    public ResourceDescription[] readResourceDescriptions() 
	throws IOException, SerializationException
    {
	try {
	    parse();
	} catch (SAXException ex) {
	    ex.printStackTrace();
	    return new ResourceDescription[0];
	}
	
	return resources;
    }

    public XMLDescrReader(Reader reader, Parser parser) {
	this.reader = reader;
	this.parser = parser;
    }

}
