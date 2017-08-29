// MimeTypeAttribute.java
// $Id: MimeTypeAttribute.java,v 1.1 2010/06/15 12:24:16 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.SimpleAttribute;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;

/**
 * The generic class of Mime type attributes.
 */

public class MimeTypeAttribute extends SimpleAttribute {

    /**
     * Is the given object a valid MimeTypeAttribute value ?
     * @param obj The object to test.
     * @return A boolean <strong>true</strong> if okay.
     */

    public boolean checkValue(Object obj) {
	return (obj instanceof MimeType) || (obj == null) ;
    }

    public String pickle(Object obj) {
	return ((MimeType)obj).toString();
    }

    public Object unpickle (String value) {
	try {
	    return new MimeType(value);
	} catch (MimeTypeFormatException ex) {
	    return null;
	}
    }

    public MimeTypeAttribute(String name, Object def, int flags) {
	super(name, def, flags) ;
	this.type = "org.w3c.www.mime.MimeType".intern();
    }

    public MimeTypeAttribute() {
	super() ;
    }
}
