// HttpBuffer.java
// $Id: HttpBuffer.java,v 1.1 2010/06/15 12:19:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A cool StringBuffer like class, for converting header values to String.
 * Note that for good reasons, this class is <em>not</em> public.
 */

class HttpBuffer {
    private static final int INIT_SIZE = 128 ;

    byte    buf[] = null ;
    int     len   = 0;
    byte    sep   = (byte) 0;

    final void append(byte b) {
    	ensureCapacity(1);
    	buf[len++] = b;
    }

    final void append(char ch) {
    	append((byte) ch);
    }

    final void append(int i) {
	append((byte) i);
    }

    final void appendLong(long i) {
	appendLong(i, -1, (byte) 0);
    }

    final void appendLong(long i, int padlen, byte pad) {
	boolean neg = (i < 0);
	int hackpos = len;
	// Emit number in reverse order:
	if ( ! neg )
	    i = -i;
	while (i <= -10) {
	    append((byte) ('0'-(i%10))); padlen--;
	    i = i / 10;
	}
	append((byte) ('0'-i)); padlen--;
	if ( neg ) {
	    append((byte) '-'); padlen--;
	}
	while ( --padlen >= 0 ) 
	    append(pad);
	// Reverse byte order
	int cnt = (len-hackpos) / 2 ;
	int j   = len-1;
	while ( --cnt >= 0 ) {
	    int pos = hackpos+len-j-1;
	    byte tmp = buf[j];
	    buf[j]   = buf[pos] ;
	    buf[pos] = tmp;
	    j--;
	}
    }

    final void appendInt(int i) {
	appendLong(i, -1, (byte) 0);
    }

    final void appendInt(int i, int padlen, byte pad) {
	appendLong(i, padlen, pad);
    }

    final void ensureCapacity(int sz) {
	int req = len + sz ;
	if ( req >= buf.length ) {
	    int nsz = buf.length << 1;
	    if ( nsz < req )
		nsz = req + 1;
	    byte nb[] = new byte[nsz];
	    System.arraycopy(buf, 0, nb, 0, len);
	    buf = nb;
	}
	return;
    }

    void append(byte b[], int o, int l) {
	ensureCapacity(l);
	System.arraycopy(b, o, buf, len, l);
	len += l;
    }

    final void append(byte b[]) {
	append(b, 0, b.length);
    }

    void append(String str) {
	int l = str.length();
	ensureCapacity(l);
	str.getBytes(0, l, buf, len);
	len += l;
    }

    void appendQuoted(String str) {
	append((byte) '"');
	append(str);
	append((byte) '"');
    }
	
    void append(String name, byte sep, String value) {
	append(name);
	append(sep);
	append(value);
    }

    void append(String name, byte sep, int value) {
	append(name);
	append(sep);
	appendInt(value);
    }

    void appendQuoted(String name, byte sep, String value) {
	append(name);
	append(sep);
	append('"');
	append(value);
	append('"');
    }

    void appendQuoted(String name, byte sep, String values[]) {
	append(name);
	if ( values.length > 0 ) {
	    append(sep);
	    append((byte) '"');
	    for (int i = 0 ; i < values.length ; i++) {
		if ( i > 0 )
		    append(',');
		append(values[i]);
	    }
	    append((byte) '"');
	}
    }

    void append(String name, byte sep, String values[]) {
	append(name);
	if ( values.length > 0 ) {
	    append(sep);
	    for (int i = 0 ; i < values.length ; i++) {
		if ( i > 0 )
		    append(',');
		append(values[i]);
	    }
	}
    }

    void append(double d) {
    	append(Double.toString(d));
    }

    public String toString() {
	return new String(buf, 0, 0, len);
    }

    /**
     * Get a copy of the current byte buffer.
     */

    public byte[] getByteCopy() {
	byte v[] = new byte[len];
	System.arraycopy(buf, 0, v, 0, len);
	return v;
    }

    public final byte[] getBytes() {
	return buf;
    }

    public final int length() {
	return len;
    }

    public final void reset() {
	len = 0;
    }

    /**
     * Emit the content of this byte buffer to the given output stream.
     * @param out The output stream to emit the content to.
     * @exception IOException If sone IO error occurs during emitting.
     */

    public final void emit(OutputStream out) 
	 throws IOException
    {
	if (out == null) {
	    throw new IOException("outputstream not existent");
	}
	out.write(buf, 0, len);
    }

    HttpBuffer() {
	this.buf = new byte[INIT_SIZE];
    }

    HttpBuffer(int size) {
	this.buf = new byte[size];
    }
}
