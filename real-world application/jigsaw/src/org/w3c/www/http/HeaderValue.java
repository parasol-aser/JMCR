// HeaderValue.java
// $Id: HeaderValue.java,v 1.1 2010/06/15 12:19:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;

public interface HeaderValue {

    /**
     * Emit this header value to the given output stream.
     * @param out The output stream to emit this value to.
     * @exception IOException If some IO error occurs while emiting the value.
     */

    public void emit(OutputStream out) 
	throws IOException;

    /**
     * Add these bytes to the header raw value.
     * @param value The raw header value as a byte array.
     * @param off The beginning of the raw value in the above byte buffer.
     * @param len The length of the raw value in the above byte buffer.
     */

    public void addBytes(byte value[], int off, int len) ;

    /**
     * Reset the header byte value to the given byte array.
     * @param value The raw header value as a byte array.
     * @param off The beginning of the raw value in the above byte buffer.
     * @param len The length of the raw value in the above byte buffer.
     */

    public void setBytes(byte value[], int off, int len) ;

    /**
     * Get this header parsed value, in its native type.
     * HeaderValue implementors can be used as wrappers for the actual
     * parsed header value. In such case this method should return the wrapped
     * value (you would otherwise, probably want to return 
     * <strong>this</strong>).
     */

    public Object getValue() ;

    /**
     * Return the HTTP encoding for this header value.
     * This method is slow, and defeats nearly all the over-engeneered 
     * optimization of the HTTP parser.
     * @return A String representing the header value in a format compatible 
     * with HTTP.
     */
	
    public String toExternalForm() ;
	
     /**
      * Append this header byte value to the given buffer.
      * @param buf The buffer to append the byte value to.
      */

    public void appendValue(HttpBuffer buf);
	  
}
