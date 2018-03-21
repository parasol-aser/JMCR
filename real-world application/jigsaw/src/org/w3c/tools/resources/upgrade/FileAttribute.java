// FileAttribute.java
// $Id: FileAttribute.java,v 1.1 2010/06/15 12:22:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * The generic description of an FileAttribute.
 */

public class FileAttribute extends Attribute {

    /**
     * Is the given object a valid FileAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof File);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	String str    = ((File) value).getPath();
	int    strlen = str.length() ;
	int    utflen = 0 ;

	for (int i = 0 ; i < strlen ; i++) {
	    int c = str.charAt(i);
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		utflen++;
	    } else if (c > 0x07FF) {
		utflen += 3;
	    } else {
		utflen += 2;
	    }
	}
	return utflen + 2 ;
    }

    /**
     * Pickle an File to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object f) 
	throws IOException
    {
	if (f instanceof String)
	    out.writeUTF(f.toString());
	else
	    out.writeUTF(((File) f).getPath()) ;
    }

    /**
     * Unpickle an File from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of File.
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (DataInputStream in) 
	throws IOException
    {
	return new File(in.readUTF()) ;
    }

    public FileAttribute(String name, File def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.io.File";
    }

}
