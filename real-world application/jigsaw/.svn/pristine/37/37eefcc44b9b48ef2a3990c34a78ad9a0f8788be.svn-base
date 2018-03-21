// Serializer.java
// $Id: Serializer.java,v 1.1 2010/06/15 12:28:13 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization; 

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import org.w3c.util.LookupTable;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.AttributeHolder;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface Serializer {

    /**
     * Write the resource descriptions using the given writer.
     * @param descr the resource descriptions array
     * @param writer the writer
     */
    public void writeResourceDescriptions(ResourceDescription descr[],
					  Writer writer)
	throws IOException, SerializationException;

    /**
     * Write the resource descriptions using the given writer.
     * @param descr the resource descriptions array
     * @param writer the writer
     */
    public void writeResourceDescriptions(Resource descr[],
					  Writer writer)
	throws IOException, SerializationException;

    /**
     * Write the resources using the given writer.
     * @param descr the resource array
     * @param writer the writer
     */
    public void writeResources(AttributeHolder holders[], Writer writer)
	throws IOException, SerializationException;

    /**
     * Read the resource descriptions using the given reader.
     * @param writer the reader
     * @return a ResourceDescription array
     */
    public ResourceDescription[] readResourceDescriptions(Reader reader) 
	throws IOException, SerializationException;

    /**
     * Read the resources using the given reader.
     * @param writer the reader
     * @return a Resources array
     */
    public Resource[] readResources(Reader reader)
	throws IOException, SerializationException;

    /**
     * Read the attribute holders using the given reader.
     * @param writer the reader
     * @return a Resources array
     */
    public AttributeHolder[] readAttributeHolders(Reader reader) 
	throws IOException, SerializationException;

    /**
     * Load only some attributes
     * @param attributes the attribute names array.
     */
    public LookupTable[] readAttributes(Reader reader, String attributes[]) 
    	throws IOException, SerializationException;
}
