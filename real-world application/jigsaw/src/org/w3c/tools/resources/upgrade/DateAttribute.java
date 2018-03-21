// DateAttribute.java
// $Id: DateAttribute.java,v 1.1 2010/06/15 12:22:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.util.Date;

public class DateAttribute extends LongAttribute {

    public DateAttribute(String name, Long def, Integer flags) {
	// ugly hack, the constructor already knows that we are using Long ;)
	super ( name, def, flags);
	this.type = "java.util.Date";
    }
}
