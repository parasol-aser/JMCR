// FileAttribute.java
// $Id: FileAttribute.java,v 1.1 2010/06/15 12:20:24 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.File;

/**
 * The generic description of an FileAttribute.
 */

public class FileAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid FileAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof File);
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     */

    public String pickle(Object obj) {
	if (obj instanceof String)
	    return (String) obj;
	else
	    return ((File) obj).getPath();
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     */

    public Object unpickle (String value) {
	return new File(value);
    }

    public FileAttribute(String name, File def, int flags) {
	super(name, def, flags) ;
	this.type = "java.io.File".intern();
    }

    public FileAttribute() {
	super() ;
    }

}
