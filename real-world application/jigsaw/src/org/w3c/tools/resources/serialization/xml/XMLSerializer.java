// XMLSerializer.java
// $Id: XMLSerializer.java,v 1.2 2010/06/15 17:53:01 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import org.w3c.util.LookupTable;

import org.xml.sax.Parser;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.SerializationException;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLSerializer implements Serializer, JigXML {


    public static final String PARSER_P1 = "com.bluecast.xml.Piccolo";
    public static final String PARSER_P2 = "com.jclark.xml.sax.Driver";
    public static final String PARSER_P3 = 
                                         "org.apache.xerces.parsers.SAXParser";

    protected Class parser_class = null;
    protected String parser_name = null;

    /**
     * Write the resource descriptions using the given writer.
     * @param descr the resource descriptions array
     * @param writer the writer
     */
    public void writeResourceDescriptions(ResourceDescription descr[],
					  Writer writer)
	throws IOException, SerializationException
    {
	XMLDescrWriter xmlwriter = null;
	try {
	    xmlwriter = new XMLDescrWriter(writer);
	    //XML headers
	    xmlwriter.startDocument();
	    //dump the resources in XML...
	    for (int i = 0 ; i < descr.length ; i++) {
		xmlwriter.writeResourceDescription(descr[i]);
	    }
	} finally {
	    if (xmlwriter != null) {
		xmlwriter.closeDocument();
	    }
	}
    }

    /**
     * Write the resource descriptions using the given writer.
     * @param descr the resource array
     * @param writer the writer
     */
    public void writeResourceDescriptions(Resource descr[],
					  Writer writer)
	throws IOException, SerializationException
    {
	XMLResourceWriter xmlwriter = null;
	try {
	    xmlwriter = new XMLResourceWriter(writer);
	    //XML headers
	    xmlwriter.startDocument();
	    //dump the resources in XML...
	    for (int i = 0 ; i < descr.length ; i++) {
		xmlwriter.writeResourceDescription(descr[i]);
	    }
	} finally {
	    if (xmlwriter != null) {
		xmlwriter.closeDocument();
	    }
	}
    }
    /**
     * Write the resources using the given writer.
     * @param descr the resource array
     * @param writer the writer
     */
    public void writeResources(AttributeHolder holders[], Writer writer) 
    	throws IOException, SerializationException
    {
	XMLResourceWriter xmlwriter = null;
	try {
	    xmlwriter = new XMLResourceWriter(writer);
	    //XML headers
	    xmlwriter.startDocument();
	    //dump the holders in XML...
	    for (int i = 0 ; i < holders.length ; i++) {
		xmlwriter.writeResource(holders[i]);
	    }
	    //close the writer;
	} finally {
	    if (xmlwriter != null) {
		xmlwriter.closeDocument();
	    }
	}
    }

    protected Parser getParser() 
	throws SerializationException
    {
	try {
	    return (Parser) parser_class.newInstance();
	} catch (Exception ex) {
	    throw new SerializationException("Unable to intantiate : "+
					     parser_name);
	}
    }

    /**
     * Read the resource descriptions using the given reader.
     * @param writer the reader
     * @return a ResourceDescription array
     */
    public ResourceDescription[] readResourceDescriptions(Reader reader) 
	throws IOException, SerializationException
    {
	Parser         parser    = getParser();
	XMLDescrReader xmlreader = new XMLDescrReader(reader, parser);
	return xmlreader.readResourceDescriptions();
    }

    /**
     * Read the resources using the given reader.
     * @param writer the reader
     * @return a Resources array
     */
    public Resource[] readResources(Reader reader) 
	throws IOException, SerializationException
    {
	Parser    parser    = getParser();
	XMLReader xmlreader = new XMLReader(reader, parser);
	return xmlreader.readResources();
    }

    /**
     * Read the attribute holders using the given reader.
     * @param writer the reader
     * @return a Resources array
     */
    public AttributeHolder[] readAttributeHolders(Reader reader) 
	throws IOException, SerializationException
    {
	Parser    parser    = getParser();
	XMLReader xmlreader = new XMLReader(reader, parser);
	return xmlreader.readAttributeHolders();
    } 

    /**
     * Load only some attributes
     * @param attributes the attribute names array.
     */
    public LookupTable[] readAttributes(Reader reader, String attributes[]) 
    	throws IOException, SerializationException
    {
	Parser parser = getParser();
	XMLSubsetReader xmlreader = 
	    new XMLSubsetReader(reader, parser, attributes);
	return xmlreader.readAttributeTables();
    }

    public XMLSerializer() {
	try {
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    parser_class = Class.forName(PARSER_P1);
	    parser_name = PARSER_P1;
	} catch (ClassNotFoundException pex) {
	    try {
		parser_class = Class.forName(PARSER_P2);
		parser_name = PARSER_P2;
	    } catch (ClassNotFoundException ppex) {	
		try {
		    parser_class = Class.forName(PARSER_P3);
		    parser_name = PARSER_P3;
		} catch (ClassNotFoundException pppex) {
		    parser_class = null;
		}
	    }
	}
    }
}
