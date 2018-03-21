// BooleanAttribute.java
// $Id: BooleanAttribute.java,v 1.1 2010/06/15 12:22:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The generic description of an BooleanAttribute.
 */

public class BooleanAttribute extends Attribute {

    /**
     * Is the given object a valid BooleanAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof Boolean);
    }

    /**
     * Get the number of bytes required to save that attribute value.
     * @param The value about to be pickled.
     * @return The number of bytes needed to pickle that value.
     */

    public final int getPickleLength(Object value) {
	return 1;
    }

    /**
     * Pickle an boolean to the given output stream.
     * @param out The output stream to pickle to.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public void pickle(DataOutputStream out, Object b) 
	throws IOException
    {
	out.writeBoolean(((Boolean) b).booleanValue()) ;
    }

    /**
     * Unpickle an boolean from the given input stream.
     * @param in The input stream to unpickle from.
     * @return An instance of Boolean.
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (DataInputStream in) 
	throws IOException
    {
	return (in.readBoolean()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public BooleanAttribute(String name, Boolean def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.Boolean";
    }

}
