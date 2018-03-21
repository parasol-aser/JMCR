// SimpleCacheSerializer.java
// $Id: SimpleCacheSerializer.java,v 1.1 2010/06/15 12:25:08 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.protocol.http.cache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Vector;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.serialization.SerializationException;
import org.w3c.tools.resources.serialization.xml.XMLSerializer;

import org.w3c.util.LookupTable;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class SimpleCacheSerializer extends CacheSerializer {

    protected XMLSerializer serializer = null;

    /**
     * Save a Generation, using a specified writer
     * @param generation, a CacheGeneration, the generation to be saved
     * @param writer, a Writer, the writer used to serialize this generation
     */
    public void writeGeneration(CacheGeneration generation, Writer writer) 
	throws IOException
    {
	synchronized(generation) {
	    _writeGeneration(generation, writer);
	}
    }

    /**
     * No synchronization on generation
     */
    private void _writeGeneration(CacheGeneration generation, Writer writer) 
	throws IOException
    {
	Enumeration cgs = generation.getCachedResources();
	Vector v = new Vector(10);
	while(cgs.hasMoreElements()) {
	    v.addElement(cgs.nextElement());
	}
	CachedResource crs[] = new CachedResource[v.size()];
	v.copyInto(crs);
	try {
	    serializer.writeResources(crs, writer);
	} catch (SerializationException ex) {
	    String msg = "XML Serialization, write failed : "+ex.getMessage();
	    throw new IOException(msg);
	}
    }

    /**
     * Save the list of generations (except the 'description' Generation)
     * @param store, the Store to be dumped
     * @param writer a Writer, used to dump 
     */
    public void writeGenerationList(CacheStore store) 
	throws IOException
    {
	synchronized(store) {
	    CacheGeneration cg = store.getMRUGeneration();
	    while ((cg != null) && (! cg.isSaved())) {
		Writer writer = 
		    new BufferedWriter(new FileWriter(cg.getGenerationFile()));
		_writeGeneration(cg, writer);
		cg = store.getNextGeneration(cg);
	    }
	}
    }

    /**
     * Read a Generation, using a specified reader
     * @param generation, a CacheGeneration, the generation to be loaded 
     * (or updated)
     * @param reader, the Reader used to read this generation
     */
    public CacheGeneration readGeneration(CacheGeneration generation,
					  Reader reader)
	throws IOException
    {
	synchronized(generation) {
	    generation.clean();
	    AttributeHolder ah[] = null;
	    try {
		ah = serializer.readAttributeHolders(reader);
	    } catch (SerializationException ex) {
		String msg = "XML Serialization, load failed : "+
		    ex.getMessage();
		throw new IOException(msg);
	    }
	    for (int i = 0 ; i < ah.length ; i++) {
		generation.loadCachedResource((CachedResource)ah[i]);
	    }
	    generation.setLoaded(true);
	    generation.setSaved(true);
	    return generation;
	}
    }

    /**
     * Read a Generation containing only CachedResourceDescription, 
     * using a specified reader.
     * @param generation, a CacheGeneration, the generation to be 'loaded'
     * @param reader, the Reader used to read this generation
     */
    public CacheGeneration readDescription(CacheGeneration generation,
					   Reader reader)
	throws IOException			   
    {
	LookupTable tables[] = null;
	try {
	    tables = serializer.readAttributes(reader, 
					       CachedResource.ATTR_DESCR);
	} catch (SerializationException ex) {
	    String msg = "XML Serialization, load failed : "+
		ex.getMessage();
	    throw new IOException(msg);
	}
	synchronized(generation) {
	    generation.setDescription(tables);
	}
	return generation;
    }

    /**
     * Save an Attribute Holder
     * @param holder, the attribute holder
     * @param writer, a Writer, the writer used to serialize this generation
     */
    public void write(AttributeHolder holder, Writer writer)
	throws IOException
    {
	AttributeHolder holders[] = { holder };
	try {
	    serializer.writeResources(holders, writer);
	} catch (SerializationException ex) {
	    String msg = "XML Serialization, save failed : "+
		ex.getMessage();
	    throw new IOException(msg);
	}
    }	

    /**
     * Read an Attribute Holder
     * @param holder, the attribute holder
     * @param reader, a Reader, the reader used to read the holder
     */
    public AttributeHolder read(Reader reader)
	throws IOException
    {
	try {
	    AttributeHolder holders[] = 
		serializer.readAttributeHolders(reader);
	    if ((holders != null) && (holders.length > 0)) {
		return holders[0];
	    } else {
		throw new IOException("empty!");
	    }
	} catch (SerializationException ex) {
	    String msg = "XML Serialization, load failed : "+
		ex.getMessage();
	    throw new IOException(msg);
	}
    }

    public SimpleCacheSerializer() {
	this.serializer = new XMLSerializer();
    }
}
