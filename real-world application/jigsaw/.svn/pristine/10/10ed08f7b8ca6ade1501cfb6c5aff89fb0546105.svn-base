// ClassAttribute.java
// $Id: ClassAttribute.java,v 1.2 2010/06/15 17:53:11 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an ClassAttribute.
 */

public class ClassAttribute extends Attribute {

    /**
     * Make a String out of a ClassAttribute value.
     * The default <code>toString</code> method on classes doesn't work
     * for that purpose, since it will preceed the class name with
     * a <strong>class</strong> keyword.
     * @return The String name of the class.
     */

    public String stringify(Object value) {
	return ((value instanceof Class) 
		? ((Class) value).getName()
		: "<unknown-class>");
    }

    /**
     * Is the given object a valid ClassAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Class);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	String str    = ((Class) value).getName();
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
     * Pickle an integer to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object c) 
	throws IOException
    {
	out.writeUTF(((Class) c).getName()) ;
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of Integer.
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (DataInputStream in) 
	throws IOException
    {
	String clsname = in.readUTF() ;
	try {
	    return Class.forName(clsname);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    throw new IOException ("Unable to restore class "
				   + clsname
				   + ": "
				   + ex.getMessage()) ;
	}
    }

    public ClassAttribute(String name, Class def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Class";
    }

}
