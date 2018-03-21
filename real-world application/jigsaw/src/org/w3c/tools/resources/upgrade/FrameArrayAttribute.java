
// FrameArrayAttribute.java
// $Id: FrameArrayAttribute.java,v 1.1 2010/06/15 12:22:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.UnknownFrame;

public class FrameArrayAttribute extends Attribute {

    public boolean checkValue(Object value) {
	return value instanceof ResourceFrame[];
    }

    public int getPickleLength(Object value) {
	throw new RuntimeException("unused for upgrade");
    }

    public void pickle(DataOutputStream out, Object obj)
	throws IOException
    {
	throw new RuntimeException("unused for upgrade");
    }

    public Object unpickle(DataInputStream in) 
	throws IOException
    {
	int cnt = in.readInt();
	if ( cnt == 0 )
	    return null;
	ResourceFrame frames[] = new ResourceFrame[cnt];
 	for (int i = 0 ; i < cnt ; i++) {
	    try {
		frames[i] = (ResourceFrame) Upgrader.readResource(in);
	    } catch (UpgradeException ex) {
		frames[i] = new UnknownFrame();
	    }
	}
	return frames;
    }

    public FrameArrayAttribute(String name, ResourceFrame def[],
                               Integer flags) {
	super(name, def, flags);
    }

}
