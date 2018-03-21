// XMLReader.java
// $Id: XMLReader.java,v 1.2 2010/06/15 17:53:01 smhuang Exp $
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
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.UnknownResource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.serialization.SerializationException;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLReader extends HandlerBase implements JigXML{

    Parser          parser           = null;
    AttributeHolder holders[]        = null;
    Reader          reader           = null;
    Stack           resourceSetStack = null;
    Stack           resourceStack    = null;
    Stack           defsStack        = null;
    SimpleAttribute currentS         = null;
    ArrayAttribute  currentA         = null;
    Stack           faNameStack      = null;
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
	if (iname == iRESOURCE_TAG) {
	    String resourceclass = attributes.getValue(CLASS_ATTR);
	    try {
		    //Added by Jeff Huang
		    //TODO: FIXIT
		Class c = Class.forName(resourceclass);
		resourceStack.push(c.newInstance());
	    } catch (Exception ex) {
		UnknownResource unknown = new UnknownResource();
		resourceStack.push(unknown);
	    }
	    Hashtable defs = new Hashtable(5);
	    defsStack.push(defs);
	} else if (iname == iATTRIBUTE_TAG) {
	    String attrclass = attributes.getValue(CLASS_ATTR);
	    try {
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
	    faNameStack.push(attributes.getValue(NAME_ATTR));
	} else if (iname == iVALUE_TAG) {
	    isavalue = true;
	}
    }

    public void endElement(String name) 
	throws SAXException
    {
	endCharacters();
	String iname = name.intern();
	if (iname == iRESOURCE_TAG) {
	    AttributeHolder res = (AttributeHolder)resourceStack.pop();
	    res.pickleValues((Hashtable)defsStack.pop());
	    Vector vresources = (Vector)resourceSetStack.peek();
	    vresources.addElement(res);
	} else if (iname == iATTRIBUTE_TAG) {
	    currentS = null;
	} else if (iname == iARRAY_TAG) {
	    Hashtable defs = (Hashtable)defsStack.peek();
	    Object value = currentA.unpickle(array);
	    if (value != null)
		defs.put(currentA.getName(), value);
	    currentA = null;
	} else if (iname == iRESARRAY_TAG) {
	    Hashtable defs = (Hashtable)defsStack.peek();
	    Vector vframes = (Vector) resourceSetStack.pop();
	    ResourceFrame frames[] = new ResourceFrame[vframes.size()];
	    vframes.copyInto(frames);
	    defs.put((String)faNameStack.pop(), frames);
	} else if (iname == iVALUE_TAG) {
	    isavalue = false;
	}
    }

    public void startDocument() 
	throws SAXException
    {
	defsStack        = new Stack();
	faNameStack      = new Stack();
	resourceStack    = new Stack();
	resourceSetStack = new Stack();
	resourceSetStack.push(new Vector(10));
    }

    public void endDocument() 
	throws SAXException
    {
	Vector vresources = (Vector) resourceSetStack.pop();
	holders = new AttributeHolder[vresources.size()];
	vresources.copyInto(holders);
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
	    Hashtable defs = (Hashtable)defsStack.peek();
	    if (! value.equals(NULL)) {
		defs.put(currentS.getName(), currentS.unpickle(value));
	    }
	} else if ((currentA != null) && (isavalue)) {
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
//	} catch (IOException ex) {
//	    try { reader.close(); } catch (IOException ioex) {}
//	    throw ex;
//	} catch (SAXException sex) {
//	    try { reader.close(); } catch (IOException ioex) {}
//	    throw sex;
	} finally {
	    try { reader.close(); } catch (IOException ioex) {}
	}
    }

    public Resource[] readResources() 
	throws IOException, SerializationException
    {
	try {
	    parse();
	} catch (SAXException ex) {
	    ex.printStackTrace();
	    return new Resource[0];
	}
	
	int      len   = holders.length;
	Resource crs[] = new Resource[len];
	for (int i = 0 ; i < len ; i++)
	    crs[i] = (Resource) holders[i];
	return crs;
    }

    public AttributeHolder[] readAttributeHolders() 
	throws IOException, SerializationException
    {
	try {
	    parse();
	} catch (SAXException ex) {
	    ex.printStackTrace();
	    return new AttributeHolder[0];
	}

	return holders;
    }

    public XMLReader(Reader reader, Parser parser) {
	this.reader = reader;
	this.parser = parser;
    }

}
