// FrameArrayAttribute.java
// $Id: FrameArrayAttribute.java,v 1.1 2010/06/15 12:20:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

public class FrameArrayAttribute extends Attribute {

    public boolean checkValue(Object value) {
	return value instanceof ResourceFrame[];
    }

    public String stringify(Object value) {
	throw new RuntimeException("Can't stringify FrameArrayAttribute");
    }

    public FrameArrayAttribute(String name, ResourceFrame def[], int flags) {
	super(name, def, flags);
	this.type="[Lorg.w3c.tools.resources.ResourceFrame;".intern();
    }

    public FrameArrayAttribute() {
	super();
	this.type="[Lorg.w3c.tools.resources.ResourceFrame;".intern();
    }

}
