// XMLDescrWriter.java
// $Id: XMLDescrWriter.java,v 1.1 2010/06/15 12:28:58 smhuang Exp $
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
import org.w3c.tools.resources.serialization.AttributeDescription;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.EmptyDescription;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class XMLDescrWriter extends XMLWriter implements JigXML {

    protected void startResource(ResourceDescription res) 
	throws IOException
    {
	String classname = res.getClassName();
	writer.write('<');
	writer.write(RESOURCE_TAG);
	writer.write(' ');
	writer.write(CLASS_ATTR);
	writer.write("='");
	writer.write(classname);
	writer.write("'>\n");
	writeInherit(res.getClassHierarchy(), 1);
	writeInterfaces(res.getInterfaces());
    }

    protected void startDescription(ResourceDescription res) 
	throws IOException
    {
	String classname = res.getClassName();
	writer.write('<');
	writer.write(DESCR_TAG);
	writer.write(" ");
	writer.write(CLASS_ATTR);
	writer.write("='");
	writer.write(classname);
	String id = res.getIdentifier();
	if (id == null)
	    id = NULL;
	writer.write("' ");
	writer.write(NAME_ATTR);
	writer.write("='");
	writer.write(id);
	writer.write("'>\n");
    }

    protected void closeDescription() 
	throws IOException
    {
	writer.write("</");
	writer.write(DESCR_TAG);
	writer.write(">\n");
    }

    protected void writeInherit(String classes[], int idx)
	throws IOException
    {
	if (idx < classes.length) {
	    level++;
	    indent();
	    writer.write('<');	    
	    writer.write(INHERIT_TAG);
	    writer.write(' ');
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(classes[idx]);
	    writer.write("'>\n");
	    writeInherit(classes, ++idx);
	    indent();
	    writer.write("</");
	    writer.write(INHERIT_TAG);
	    writer.write(">\n");
	    level--;
	}
    }
    
    protected void writeInterfaces(String classes[]) 
	throws IOException
    {
	level++;
	for (int i = 0 ; i < classes.length ; i++) {
	    indent();
	    writer.write('<');
	    writer.write(IMPLEMENTS_TAG);
	    writer.write(' ');
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(classes[i]);
	    writer.write("'/>\n");
	}
	level--;
    }

    protected void writeAttributeDescription(AttributeDescription descr)
	throws IOException
    {
	level++;
	String    classname = descr.getClassName();
	Attribute attr      = descr.getAttribute();
	Object    value     = descr.getValue();
	if (attr instanceof SimpleAttribute) {
	    indent();
	    writer.write("<"+ATTRIBUTE_TAG+" "+NAME_ATTR+"='");
	    writer.write(descr.getName());
	    writer.write("' "+FLAG_ATTR+"='");
	    writer.write(attr.getFlag());
	    writer.write("' "+CLASS_ATTR+"='");
	    writer.write(classname);
	    if (value == null) {
		writer.write("'>"+NULL);
	    } else {
		writer.write("'>");
		writer.write(encode(((SimpleAttribute)attr).pickle(value)));
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
	    writer.write(descr.getName());
	    writer.write("' ");
	    writer.write(FLAG_ATTR);
	    writer.write("='");
	    writer.write(attr.getFlag());
	    writer.write("' ");
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(classname);
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
	    for (int i = 0 ; i < len ; i++) {
		indent(); 
		writer.write('<');
		writer.write(VALUE_TAG);
		writer.write('>');
		writer.write(encode(values[i]));
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
	    writer.write(descr.getName());
	    writer.write("' ");
	    writer.write(CLASS_ATTR);
	    writer.write("='");
	    writer.write(classname);
	    writer.write("' ");
	    ResourceDescription frames[] = null;
	    if (value == null)
		frames = new ResourceDescription[0];
	    else
		frames = (ResourceDescription[]) value;
	    int len = frames.length;
	    writer.write(LENGTH_ATTR);
	    writer.write("='");
	    writer.write(String.valueOf(len));
	    writer.write("'>\n");
	    for (int i = 0 ; i < len ; i++) {
		writeResourceDescription(frames[i]);
	    }
	    indent();
	    writer.write("</");
	    writer.write(RESARRAY_TAG);
	    writer.write(">\n");
	}
	level--;
    }

    public void writeResourceDescription(ResourceDescription resource) 
	throws IOException
    {
	level++;
	indent();
	if (resource instanceof EmptyDescription) {
	    startDescription(resource);
	    indent();
	    closeDescription();
	} else {
	    startResource(resource);
	    AttributeDescription attrs [] = 
		resource.getAttributeDescriptions();
	    for (int j = 0 ; j < attrs.length ; j++)
		writeAttributeDescription(attrs[j]);
	    indent();
	    closeResource();
	}
	level--;
    }

    public XMLDescrWriter(Writer writer) {
	super(writer);
    }

}
