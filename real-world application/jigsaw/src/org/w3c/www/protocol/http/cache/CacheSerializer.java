// CacheSerializer.java
// $Id: CacheSerializer.java,v 1.1 2010/06/15 12:25:10 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.io.IOException;
import java.io.Writer;
import java.io.Reader;

import org.w3c.tools.resources.AttributeHolder;

public abstract class CacheSerializer {

    /**
     * Save a Generation, using a specified writer
     * @param generation, a CacheGeneration, the generation to be saved
     * @param writer, a Writer, the writer used to serialize this generation
     */
    public abstract void writeGeneration(CacheGeneration generation,
					 Writer writer)
	throws IOException;			 

    /**
     * Save the list of generations (except the 'description' Generation)
     * @param store, the Store to be dumped
     * @param writer a Writer, used to dump 
     */
    public abstract void writeGenerationList(CacheStore store)
	throws IOException;

    /**
     * Read a Generation, using a specified reader
     * @param generation, a CacheGeneration, the generation to be saved
     * @param reader, the Reader used to read this generation
     */
    public abstract CacheGeneration readGeneration(CacheGeneration generation,
						   Reader reader)
	throws IOException;		   

    /**
     * Read a Generation containing only CachedResourceDescription, 
     * using a specified reader.
     * @param generation, a CacheGeneration, the generation to be 'loaded'
     * @param reader, the Reader used to read this generation
     */
    public abstract CacheGeneration readDescription(CacheGeneration generation,
						    Reader reader)
	throws IOException;

    /**
     * Save an Attribute Holder
     * @param holder, the attribute holder
     * @param writer, a Writer, the writer used to serialize this generation
     */
    public abstract void write(AttributeHolder holder,
			       Writer writer)
	throws IOException;	

    /**
     * Read an Attribute Holder
     * @param holder, the attribute holder
     * @param reader, a Reader, the reader used to read the holder
     */
    public abstract AttributeHolder read(Reader reader)
	throws IOException;	

}
