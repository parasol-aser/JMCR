// AdminWriter.java 
// $Id: AdminWriter.java,v 1.1 2010/06/15 12:24:27 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.serialization.ResourceDescription;
import org.w3c.tools.resources.serialization.SerializationException;
import org.w3c.tools.resources.serialization.Serializer;

class AdminWriter implements AdminProtocol {

    /**
     * Our serializer.
     */
    protected Serializer serializer = null;

    /**
     * Write the given resource to the given output stream.
     * @param out The object output stream to write to.
     * @param resource The resource to write
     * @exception IOException If something went wrong.
     */

    protected void writeResource(Resource resource, OutputStream out) 
	throws IOException, AdminProtocolException
    {
	try {
	    Resource resources[] = { resource };
	    Writer writer = new OutputStreamWriter( out, "UTF-8" );
	    serializer.writeResourceDescriptions(resources, writer);
	} catch (SerializationException ex) {
	    throw new AdminProtocolException("Unable to serialize resource :"+
					     ex.getMessage());
	}
    }

    /**
     * Write the given resource to the given output stream.
     * @param out The object output stream to write to.
     * @param description The resource description to write
     * @exception IOException If something went wrong.
     */

    protected void  writeResourceDescription(ResourceDescription description,
					     OutputStream out) 
	throws IOException, AdminProtocolException
    {
	try {
	    ResourceDescription descrs[] = { description };
	    Writer writer = new OutputStreamWriter( out, "UTF-8");
	    serializer.writeResourceDescriptions(descrs, writer);
	} catch (SerializationException ex) {
	    throw new AdminProtocolException("Unable to serialize resource :"+
					     ex.getMessage());
	}
    }

    AdminWriter() {
	this.serializer = 
	    new org.w3c.tools.resources.serialization.xml.XMLSerializer();
    }

}
