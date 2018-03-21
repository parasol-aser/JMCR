// XMLResourceWriter.java
// $Id: XMLResourceWriter.java,v 1.1 2010/06/15 12:28:57 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import java.util.Vector;
import java.util.Enumeration;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;
import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.FrameArrayAttribute;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLResourceWriter extends XMLWriter implements JigXML {

    protected void startResource(AttributeHolder res) 
	throws IOException
    {
	String classname = res.getClass().getName();
	writer.write('<');
	writer.write(RESOURCE_TAG);
	writer.write(' ');
	writer.write(CLASS_ATTR);
	writer.write("='");
	writer.write(classname);
	writer.write("'>\n");
    }
    
    protected void startResourceDescr(AttributeHolder res) 
	throws IOException
    {
	String classname = res.getClass().getName();
	writer.write('<');
	writer.write(RESOURCE_TAG);
	writer.write(' ');
	writer.write(CLASS_ATTR);
	writer.write("='");
	writer.write(classname);
	writer.write("'>\n");
	Vector interfaces = new Vector(10);
	Class  c          = res.getClass();
	Class  intfs[]    = c.getInterfaces();
	if ( intfs != null ) {
	    for (int i = 0 ; i < intfs.length ; i++) 
		interfaces.addElement(intfs[i].getName());
	}
	writeInherit(c.getSuperclass(), interfaces);
	level++;
	for (int i = 0 ; i < interfaces.size() ; i++) {
	    String s = (String) interfaces.elementAt(i);
	    indent();
	    writer.write('<');
	    writer.write(IMPLEMENTS_TAG);
	    writer.write(' ');
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(s);
	    writer.write("'/>\n");
	}
	level--;
	writeChildren(res);
    }

    protected void writeInherit(Class c, Vector interfaces)
	throws IOException
    {
	if (c != null) {
	    level++;
	    indent();
	    writer.write('<');
	    writer.write(INHERIT_TAG);
	    writer.write(' ');
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(c.getName());
	    writer.write("'>\n");
	    Class  intfs[]    = c.getInterfaces();
	    if ( intfs != null ) {
		for (int i = 0 ; i < intfs.length ; i++) 
		    interfaces.addElement(intfs[i].getName());
	    }
	    writeInherit(c.getSuperclass(), interfaces);
	    indent();
	    writer.write("</");
	    writer.write(INHERIT_TAG);
	    writer.write(">\n");
	    level--;
	}
    }

    protected void writeChildren(AttributeHolder res) 
	throws IOException
    {
	if (res instanceof ContainerInterface) {
	    Vector      vids  = new Vector();
	    Enumeration e = 
		((ContainerInterface)res).enumerateResourceIdentifiers(true);
	    while (e.hasMoreElements()) {
		vids.addElement((String)e.nextElement());
	    }
	    int len = vids.size();
	    level++;
	    indent();
	    writer.write('<');
	    writer.write(CHILDREN_TAG);
	    writer.write(' ');
	    writer.write(LENGTH_ATTR);
	    writer.write("='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    level++;
	    for (int i = 0 ; i < len ; i++) {
		indent();
		writer.write('<');
		writer.write(CHILD_TAG);
		writer.write('>');
		writer.write(encode((String)vids.elementAt(i)));
		writer.write("</");
		writer.write(CHILD_TAG);
		writer.write(">\n");
	    }
	    level--;
	    indent();
	    writer.write("</");
	    writer.write(CHILDREN_TAG);
	    writer.write(">\n");
	    level--;
	}
    }

    protected void writeAttribute(Attribute attr, Object value, boolean descr)
	throws IOException
    {
	level++;
	if (attr instanceof SimpleAttribute) {
	    indent();
	    writer.write('<');
	    writer.write(ATTRIBUTE_TAG);
	    writer.write(' ');
	    writer.write(NAME_ATTR);
	    writer.write("='");
	    writer.write(attr.getName());
	    writer.write("' ");
	    writer.write(FLAG_ATTR);
	    writer.write("='");
	    writer.write(attr.getFlag());
	    writer.write("' ");
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(attr.getClass().getName());
	    writer.write("'>");
	    if (value == null) {
		writer.write(NULL);
	    } else { 
		String pickled = ((SimpleAttribute)attr).pickle(value);
		writer.write(encode(pickled));
	    }
	    writer.write("</");
	    writer.write(ATTRIBUTE_TAG);
	    writer.write(">\n");
	} else if (attr instanceof ArrayAttribute) {
	    indent(); 
	    writer.write('<');
	    writer.write(ARRAY_TAG);
	    writer.write(' ');
	    writer.write(NAME_ATTR);
	    writer.write("='");
	    writer.write(attr.getName());
	    writer.write("' ");
	    writer.write(FLAG_ATTR);
	    writer.write("='");
	    writer.write(attr.getFlag());
	    writer.write("' ");
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(attr.getClass().getName());
	    writer.write("' ");
	    String values[] = null;
	    if (value == null)
		values = new String[0];
	    else
		values = ((ArrayAttribute)attr).pickle(value);
	    int len = values.length;
	    writer.write(LENGTH_ATTR);
	    writer.write("='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    level++;
	    String rval;
	    for (int i = 0 ; i < len ; i++) {
		indent(); 
		writer.write('<');
		writer.write(VALUE_TAG);
		writer.write('>');
		rval = values[i];
		if (rval == null) {
		    writer.write(NULL);
		} else {
		    writer.write(encode(rval));
		}
		writer.write("</");
		writer.write(VALUE_TAG);
		writer.write(">\n");
	    }
	    level--;
	    indent(); 
	    writer.write("</");
	    writer.write(ARRAY_TAG);
	    writer.write(">\n");
	} else if (attr instanceof FrameArrayAttribute) {
	    indent();
	    writer.write('<');
	    writer.write(RESARRAY_TAG);
	    writer.write(' ');
	    writer.write(NAME_ATTR);
	    writer.write("='");
	    writer.write(attr.getName());
	    writer.write("' ");
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(attr.getClass().getName());
	    writer.write("' ");
	    ResourceFrame frames[] = null;
	    if (value == null)
		frames = new ResourceFrame[0];
	    else
		frames = (ResourceFrame[]) value;
	    int len = frames.length;
	    writer.write(LENGTH_ATTR);
	    writer.write("='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    if (descr) {
		for (int i = 0 ; i < len ; i++) {
		    writeResourceDescription(frames[i]);
		}
	    } else {
		for (int i = 0 ; i < len ; i++) {
		    writeResource(frames[i]);
		}
	    }
	    indent();
	    writer.write("</");
	    writer.write(RESARRAY_TAG);
	    writer.write(">\n");
	}
	level--;
    }

    public void writeResourceDescription(AttributeHolder holder) 
	throws IOException
    {
	level++;
	indent();
	startResourceDescr(holder);
	Attribute attrs  [] = holder.getAttributes();
	for (int j = 0 ; j < attrs.length ; j++) {
	    Object value = holder.unsafeGetValue(j, null);
	    //if (value != null)
	    if (! attrs[j].checkFlag(Attribute.DONTSAVE))
		writeAttribute(attrs[j], value, true);
	}
	indent();
	closeResource();
	level--;
    }

    public void writeResource(AttributeHolder holder) 
	throws IOException
    {
	level++;
	indent();
	startResource(holder);
	Attribute attrs  [] = holder.getAttributes();
	for (int j = 0 ; j < attrs.length ; j++) {
	    Object value = holder.unsafeGetValue(j, null);
	    //if (value != null)
	    if (! attrs[j].checkFlag(Attribute.DONTSAVE))
		writeAttribute(attrs[j], value, false);
	}
	indent();
	closeResource();
	level--;
    }

    public XMLResourceWriter(Writer writer) {
	super(writer);
    }

}
