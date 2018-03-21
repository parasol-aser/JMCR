// XMLProperties.java
// $Id: XMLProperties.java,v 1.2 2010/06/15 17:53:11 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.util;

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.AttributeList;
import org.xml.sax.Parser;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Enumeration;

/**
 * The <code>Properties</code> class represents a persistent set of 
 * properties. The <code>Properties</code> can be saved to a stream or 
 * loaded from a stream. Each key and its corresponding value in the 
 * property list is a string.
 * <p>
 * A property list can contain another property list as its "defaults"; this 
 * second property list is searched if the property key is not found in the
 * original property list. Because Properties inherits from Hashtable, the 
 * put and putAll methods can be applied to a Properties object. Their use
 *  is strongly discouraged as they allow the caller to insert entries whose
 * keys or values are not Strings. The setProperty method should be used 
 * instead. If the store or save method is called on a "compromised" 
 * Properties object that contains a non-String key or value, the call will 
 * fail.
 * <p>
 * This is a special implementation for XML :
 * <pre>
 *   &lt;properties>
 *      &lt;key name="My_key1">My_Value1&lt;/key>
 *      &lt;key name="My_key2">My_Value2&lt;/key>
 *   &lt;/properties>
 * </pre>
 * @version $Revision: 1.2 $
 * @author  Philippe Le Hégaret (plh@w3.org)
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLProperties extends Properties {

    public static final String PARSER_P = "com.jclark.xml.sax.Driver";

    public boolean debug = false;

    class XMLParser implements DocumentHandler {

	final int IN_NOTHING  = 0;
	final int IN_DOCUMENT = 1;
	final int IN_KEY      = 2;
	int              state       = IN_NOTHING;

	String       key;
	StringBuffer value;

	Parser       parser;

	XMLParser(InputStream in) 
	    throws IOException, SAXException 
	{
	    state = IN_NOTHING;
	    value = new StringBuffer();
	    try {
		parser = getParser();
		parser.setDocumentHandler(this);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new SAXException("can't create parser ");
	    }
	    parser.parse(new InputSource(in));
	}

	public void startElement(String name, AttributeList atts)
	    throws SAXException 
	{
	    if (state == IN_NOTHING) {
		if (name.equals("properties")) {
		    state = IN_DOCUMENT;
		} else {
		    throw new SAXException("attempt to find root properties");
		}
	    } else if (state == IN_DOCUMENT) {
		if (name.equals("key")) {
		    state = IN_KEY;
		    key = atts.getValue("name");
		
		    if (key == null) {
			throw new SAXException("no name for key "+atts);
		    }
		} else {
		    throw new SAXException("attempt to find keys");
		}
	    } else {
		throw new SAXException("invalid element "+name);
	    }
	}

	public void endElement(String name) 
	    throws SAXException 
	{
	    if (state == IN_KEY) {
		setProperty(key, value.toString());
		if (debug) {
		    System.out.print("<key name=\""+key+"\">");
		    System.out.println(value.toString()+"</key>\n");
		}
		state = IN_DOCUMENT;
		name = null;
		value = new StringBuffer();
	    } else if (state == IN_DOCUMENT) {
		state = IN_NOTHING;
	    }
	}

	public void characters(char ch[], int start, int length)
	    throws SAXException 
	{
	    if (state == IN_KEY) {
		compute(ch, start, length);
	    }
	}

	public void ignorableWhitespace(char ch[], int start, int length)
	    throws SAXException 
	{
	    // nothing to do
	}

	public void startDocument() 
	    throws SAXException 
	{
	    // nothing to do
	}

	public void endDocument() 
	    throws SAXException 
	{
	    // nothing to do
	}

	public void processingInstruction(String target, String data)
	    throws SAXException 
	{
	    // nothing to do
	}

	public void setDocumentLocator(Locator locator) {
	    // nothing to do
	}

	private void compute(char[] ch, int start, int length) {
	    int st = start;
	    int len = length-1;
	    while (st < length 
		   && ((ch[st] == '\n') || (ch[st] == '\t') || (ch[st] == ' ')
		       || (ch[st] == '\r'))) {
		st++;
	    }
	    while (len > 0
		   && ((ch[len] == '\n') 
		       || (ch[len] == '\t') 
		       || (ch[len] == ' ')
		       || (ch[len] == '\r'))) {
		len--;
	    }
	
	    while (st <= len) {
		value.append(ch[st]);
		st++;
	    }
	}
    } //XMLParser

    private Class parser_class = null;

    /**
     * Reads a property list from an input stream.
     * @param      in   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     * input stream.
     * @since   JDK1.0
     */
    public synchronized void load(InputStream in) 
	throws IOException 
    {
	XMLParser p = null;
	try {
	    p = new XMLParser(in);
	} catch (SAXException e) {
	    throw new IOException(e.getMessage());
	}
    }

    /**
     * Reads a property list from an input stream. This method try to load
     * properties with super.load() if the XMLParser failed. Use this method
     * to translate an Property set to an XML Property set.
     * @param      file the properties file.
     * @exception  IOException  if an error occurred when reading from the
     * input stream.
     * @since   JDK1.0
     */
    public synchronized void load(File file) 
	throws IOException 
    {
	InputStream in = new BufferedInputStream(new FileInputStream(file));
	XMLParser p = null;
	try {
	    p = new XMLParser(in);
	} catch (SAXException e) {
	    try {
		in = new BufferedInputStream(new FileInputStream(file));
		super.load(in);
		in.close();
	    } catch (IOException ex) {
		throw new IOException(e.getMessage());
	    }
	}
    }

    /**
     * Calls the <code>store(OutputStream out, String header)</code> method
     * and suppresses IOExceptions that were thrown.
     *
     * @deprecated This method does not throw an IOException if an I/O error
     * occurs while saving the property list.  As of JDK 1.2, the preferred
     * way to save a properties list is via the <code>store(OutputStream out,
     * String header)</code> method.
     *
     * @param   out      an output stream.
     * @param   header   a description of the property list.
     * @exception  ClassCastException  if this <code>Properties</code> object
     * contains any keys or values that are not <code>Strings</code>.
     */
    public synchronized void save(OutputStream out, String header) {
	try {
	    store(out, header);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    /**
     * Writes this property list (key and element pairs) in this
     * <code>Properties</code> table to the output stream in a format suitable
     * for loading into a <code>Properties</code> table using the
     * <code>load</code> method.
     * <p>
     * After the entries have been written, the output stream is flushed.  The
     * output stream remains open after this method returns.
     *
     * @param   out      an output stream.
     * @param   header   a description of the property list.
     * @exception  ClassCastException  if this <code>Properties</code> object
     * contains any keys or values that are not <code>Strings</code>.
     */
    public synchronized void store(OutputStream out, String header) 
	throws IOException
    {
	PrintWriter wout = new PrintWriter(out);
	    wout.println("<?xml version='1.0'?>");
	if (header != null) {
	    wout.println("<!--" + header + "-->");
	}
	
	wout.print("<properties>");
	for (Enumeration e = keys() ; e.hasMoreElements() ;) {
	    String key = (String)e.nextElement();
	    String val = (String)get(key);
	    wout.print("\n <key name=\"" + key + "\">");
	    wout.print(encode(val));
	    wout.print("</key>");
	}
	wout.print("\n</properties>");
	wout.flush();
    }

    protected StringBuffer encode(String string) {
	int          len    = string.length();
	StringBuffer buffer = new StringBuffer(len);
	char         c;

	for (int i = 0 ; i < len ; i++) {
	    switch (c = string.charAt(i)) 
		{
		case '&':
		    buffer.append("&amp;");
		    break;
		case '<':
		    buffer.append("&lt;");
		    break;
		case '>':
		    buffer.append("&gt;");
		    break;
		default:
		    buffer.append(c);
		}
	}

	return buffer;
    }

    private Class getParserClass() 
	throws ClassNotFoundException
    {
	if (parser_class == null)
	    parser_class = Class.forName(PARSER_P);
    //Added by Jeff Huang
    //TODO: FIXIT
	return parser_class;
    } 

    private Parser getParser() {
        try {
            return (Parser) getParserClass().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to intantiate : "+
				       PARSER_P);
        }
    }

    /**
     * Creates an empty property list with no default values.
     */
    public XMLProperties() {
	super();
    }

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param   defaults   the defaults.
     */
    public XMLProperties(Properties defaults) {
	super(defaults);
    }

    /**
     * Creates an empty property list with the specified defaults.
     * @param  parser the XML Parser classname (default is PARSER_P)
     * @param  defaults   the defaults.
     */
    public XMLProperties(String parser, Properties defaults) {
	super(defaults);
	try {
	    parser_class = Class.forName(parser);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (ClassNotFoundException ex) {
	    System.err.println("Unable to instanciate parser class: "+parser);
	    System.err.println("Using default parser.");
	}
    }

    public void setDebug(boolean debug) {
	this.debug = debug;
    }

}
