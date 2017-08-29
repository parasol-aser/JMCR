// PasswordAttribute.java
// $Id: PasswordAttribute.java,v 1.1 2010/06/15 12:28:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.auth ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.StringAttribute;

public class PasswordAttribute extends StringAttribute {

    public PasswordAttribute(String name, String password, int flags) {
	super(name, password, flags);
	this.type = "java.lang.String".intern();
    }

    public PasswordAttribute() {
	super();
    }

}
