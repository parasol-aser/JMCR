// SegmentArrayAttribute.java
// $Id: SegmentArrayAttribute.java,v 1.1 2010/06/15 12:26:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi ;

import org.w3c.tools.resources.ArrayAttribute;
import org.w3c.tools.resources.Attribute;

import org.w3c.util.CountOutputStream;

/**
 * Attribute used to make the document segment information persistent.
 * @author Antonio Ramirez <anto@mit.edu>
 */ 
public class SegmentArrayAttribute extends ArrayAttribute {

    public boolean checkValue(Object value)
    {
	return (value instanceof Segment[] || value == null);
    }

    /**
     * Unpickle an attribute array from a string array.
     * @param array the String array
     * @return a Object array
     */
    public Object unpickle(String array[]) {
	Segment segs[] = new Segment [array.length] ;
	for (int i = 0 ; i < array.length ; i++) 
	    segs[i] = Segment.unpickle(array[i]) ;
	return segs;
    }

    /**
     * Pickle an attribute array into a String array.
     * @param array the attribute array
     * @return a String array
     */
    public String[] pickle(Object obj) {
	Segment[] segs = (Segment[]) obj ;
	String strings[] = new String[segs.length];
	for (int i = 0 ; i < segs.length ; i++) {
	    strings[i] = segs[i].pickle();
	}
	return strings;
    }

    public SegmentArrayAttribute(String name, Segment[] def, int flags)
    {
	super(name,def,flags) ;
	this.type = "[Lorg.w3c.jigsaw.ssi.Segment;";
    }

    public SegmentArrayAttribute() {
	super();
	this.type = "[Lorg.w3c.jigsaw.ssi.Segment;";
    }

    public String stringify(Object value)
    {
	Segment[] segs = (Segment[]) value ;
	StringBuffer buf = new StringBuffer(160) ;
	buf.append('[') ;
	for(int i=0;i<segs.length;i++) {
	    buf.append(segs[i].toString()) ;
	    buf.append(' ') ;
	}
	buf.append(']') ;
	return buf.toString() ;
    }
}
