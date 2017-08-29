// IntegerAttribute.java
// $Id: IntegerAttribute.java,v 1.1 2010/06/15 12:22:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an IntegerAttribute.
 */

public class IntegerAttribute extends Attribute {

    /**
     * Is the given object a valid IntegerAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Integer) || (obj == null) ;
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	return 4;
    }

    /**
     * Pickle an integer to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object i) 
	throws IOException
    {
	out.writeInt(((Integer) i).intValue()) ;
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
	return new Integer(in.readInt()) ;
    }

    public IntegerAttribute(String name, Integer def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Integer";
    }

}
