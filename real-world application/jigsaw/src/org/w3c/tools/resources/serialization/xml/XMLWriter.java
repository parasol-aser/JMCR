// XMLWriter.java
// $Id: XMLWriter.java,v 1.1 2010/06/15 12:28:56 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.FrameArrayAttribute;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLWriter implements JigXML {

    protected Writer writer = null;
    protected int    level  = 0;

    protected static String header ="<?xml version='1.0' encoding='UTF-8'?>\n<"
                                    +JXML_TAG+" version=\""+version+
                                    "\" xmlns=\""+ns+"\">\n";
    
    protected static char[] whitebuf = { ' ',' ',' ',' ',' ',' ',' ',' '};


    protected void indent()
	throws IOException
    {
	if (level > 0) {
	    int i = level;
	    while (i > 8) {
		writer.write(whitebuf);
		i = i - 8;
	    }
	    writer.write(whitebuf, 0, i);
	}
    }

    protected void startDocument() 
	throws IOException
    {
//	writer.write("<?xml version='1.0' encoding='UTF-8'?>\n<"
//		     +JXML_TAG+" version=\""+version+"\" xmlns=\""+
//		     ns+"\">\n");
	writer.write(header);
    }

    protected void closeDocument() 
	throws IOException
    {
	writer.write("</");
	writer.write(JXML_TAG);
	writer.write(">\n");
	writer.close();
    }

    protected void closeResource()
	throws IOException
    {
	writer.write("</");
	writer.write(RESOURCE_TAG);
	writer.write(">\n");
    }

    public XMLWriter(Writer writer) {
	this.writer = writer;
    }

    /**
     * & => &amp; < => &lt;
     */
    public static String encode(String string) {
	int          len    = string.length();
	StringBuffer buffer = new StringBuffer(len+16);
	char         c;
	String       s = null;

	synchronized (buffer) {
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
		case '"':
		    buffer.append("&quot;");
		    break;
		default:
		    buffer.append(c);
		}
	    }
	    s = buffer.toString();
	}
	return s;
    }

}
