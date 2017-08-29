// HttpString.java
// $Id: HttpString.java,v 1.1 2010/06/15 12:19:54 smhuang Exp $$
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpString extends BasicValue {
    String value = null;

    protected void parse() {
	return;
    }

    protected void updateByteValue() {
	raw = new byte[value.length()];
	value.getBytes(0, raw.length, raw, 0);
	return ;
    }

    public Object getValue() {
	validate();
	if ( value == null )
	    value = new String(raw, 0, 0, raw.length);
	return value;
    }

    public void setValue(String value) {
	invalidateByteValue() ;
	this.value = value ;
	this.isValid = true ;
    }

    HttpString(boolean isValid, String value) {
	this.value = value;
	this.isValid = true ;
    }

    HttpString() {
	this.isValid = false;
    }
}
