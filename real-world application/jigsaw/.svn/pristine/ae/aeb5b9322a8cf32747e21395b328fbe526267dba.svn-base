// XMLSubsetReader.java
// $Id: XMLSubsetReader.java,v 1.1 2010/06/15 12:28:57 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.IOException;
import java.io.Reader;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;

import org.w3c.util.LookupTable;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ErrorHandler;
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
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLSubsetReader extends HandlerBase implements JigXML{

    Vector          tables           = null;
    LookupTable     table            = null;
    LookupTable     lookuptables[]   = null;
    String          attributes[]     = null;
    String          attribute        = null;
    boolean         readAttr         = false;
    int             level            = 0;
    int             len              = 0;

    Parser          parser           = null;
    Reader          reader           = null;
    boolean         isavalue         = false;

    public void startElement(String name, AttributeList attributes) 
	throws SAXException
    {
	String iname = name.intern();
	if (iname == iRESOURCE_TAG) {
	    if (level == 0) {
		table = new LookupTable(len);
	    }
	} else if (iname == iATTRIBUTE_TAG) {
	    if (level == 0) {
		String attribute = attributes.getValue(NAME_ATTR);
		readAttr = load(attribute);
	    }
	} else if (iname == iARRAY_TAG) {
	    level++;
	} else if (iname == iRESARRAY_TAG) {
	    level++;
	} else if (iname == iVALUE_TAG) {
	    isavalue = true;
	}
    }

    public void endElement(String name) 
	throws SAXException
    {
	String iname = name.intern();
	if (iname == iRESOURCE_TAG) {
	    if (level == 0) {
		tables.addElement(table);
	    }
	} else if (iname == iATTRIBUTE_TAG) {
	    if (level == 0) {
		readAttr = false;
	    }
	} else if (iname == iARRAY_TAG) {
	    level--;
	} else if (iname == iRESARRAY_TAG) {
	    level--;
	} else if (iname == iVALUE_TAG) {
	    isavalue = false;
	}
    }

    public void startDocument() 
	throws SAXException
    {
	tables = new Vector(10);
    }

    public void endDocument() 
	throws SAXException
    {
	lookuptables = new LookupTable[tables.size()];
	tables.copyInto(lookuptables);
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
	if ((level == 0) && readAttr) {
	    String value = new String(ch, start, length);
	    if (value.equals(NULL)) {
		table.put(attribute, value);
	    }
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
	parser.setDocumentHandler(this);
	parser.setErrorHandler(this);
	parser.parse(new InputSource(reader));
    }

    /**
     * Read a subset of the resource attributes. (excluding array and frames)
     * @return a LookupTable 
     *           <attribute_name (String), attribute_value (String)>
     */
    public LookupTable[] readAttributeTables() 
	throws IOException, SerializationException
    {
	try {
	    parse();
	} catch (SAXException ex) {
	    ex.printStackTrace();
	    return new LookupTable[0];
	}
	return lookuptables;
    }

    private boolean load(String attribute) {
	for (int i = 0 ; i < len ; i++) {
	    if (attributes[i].equals(attribute))
		return true;
	}
	return false;
    }

    public XMLSubsetReader(Reader reader, Parser parser, String attributes[]) {
	this.reader      = reader;
	this.parser      = parser;
	this.attributes  = attributes;
	this.len         = attributes.length;
    }

}
