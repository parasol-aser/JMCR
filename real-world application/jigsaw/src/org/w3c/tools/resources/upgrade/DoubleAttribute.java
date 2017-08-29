// DoubleAttribute.java
// $Id: DoubleAttribute.java,v 1.1 2010/06/15 12:22:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an DoubleAttribute.
 */

public class DoubleAttribute extends Attribute {

    /**
     * Is the given object a valid DoubleAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     * @exception IllegalAttributeAccess If the provided value doesn't pass the
     *    test.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Double) ;
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	return 8;
    }

    /**
     * Pickle an double to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public final void pickle(DataOutputStream out, Object d) 
	throws IOException
    {
	out.writeDouble(((Double) d).doubleValue()) ;
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of Double.
     * @exception IOException If some IO error occured.
     */

    public final Object unpickle (DataInputStream in) 
	throws IOException
    {
	return new Double(in.readDouble()) ;
    }

    /**
     * Create a description for a generic Double attribute.
     * @param name The attribute name.
     * @param def The default value for these attributes.
     * @param flags The associated flags.
     */

    public DoubleAttribute(String name, Double def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Double";
    }

}


