// BasicValue.java
// $Id: BasicValue.java,v 1.1 2010/06/15 12:19:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BasicValue implements HeaderValue, Cloneable {
    /**
     * The header value, as a byte array, if available.
     */
    protected byte raw[] = null;
    /**
     * The offset of the value in the above buffer, in case the buffer is
     * shared.
     */
    protected int roff = -1;
    /**
     * The length of the byte value in case the above buffer is shared.
     */
    protected int rlen = -1;
    /**
     * Are the parsed values up to date with the lastly set unparsed value ?
     */
    protected boolean isValid = false;

    /**
     * Parse this header value into its various components.
     * @exception HttpParserException if unable to parse.
     */

    abstract protected void parse()
	throws HttpParserException;

    /**
     * Update the RFC822 compatible header value for this object.
     */

    abstract protected void updateByteValue();

    /**
     * Compute the new RFC822 compatible representation of this header value.
     * If our value is up to date, we just return, otherwise, the abstract
     * <code>updateByteValue</code> is called to perform the job.
     */

    protected final void checkByteValue() {
	if ( raw == null ) {
	    updateByteValue();
	    roff = 0;
	    rlen = raw.length;
	}
    }

    /**
     * Validate the parsed value according to the last set raw value.
     * This will trigger the header value parsing, if it is required at this
     * point.
     * @exception HttpInvalidValueException If the value couldn't be parsed 
     * properly.
     */

    protected final void validate()
	throws HttpInvalidValueException
    {
	if ( isValid )
	    return;
	try {
	    parse();
	} catch (HttpParserException ex) {
	    throw new HttpInvalidValueException(ex.getMessage());
	}
	isValid = true;
    }

    /**
     * Invalidate the current byte value for this header, if any.
     */

    protected void invalidateByteValue() {
	raw = null;
    }

    /**
     * Emit a parsing error.
     * @param msg The error message.
     * @exception HttpParserException If the parsing failed.
     */

    protected void error(String msg) 
	throws HttpParserException
    {
	throw new HttpParserException(msg);
    }

    /**
     * Append this header value to the given output buffer.
     * @return The header value as a byte array.
     */

    public void appendValue(HttpBuffer buf) {
	checkByteValue();
	buf.append(raw, roff, rlen);
    }

    /**
     * Return a String encoding this header value in an HTTP compatible way.
     * @return A String.
     */

    public String toExternalForm() {
	checkByteValue();
	return new String(raw, 0, roff, rlen-roff);
    }

    /**
     * Print this header value as it would be emited.
     * @return A String representation of this header value.
     */

    public String toString() {
	return toExternalForm();
    }

    /**
     * HeaderValue implementation - Emit this header value to the given output
     * stream.
     * @param out The output stream to emit the header value to.
     * @exception IOException If some IO error occured.
     */

    public void emit(OutputStream out) 
	throws IOException
    {
	checkByteValue();
	out.write(raw);
    }

    /**
     * HeaderValue implementation - Add these bytes to the header raw value.
     * @param buf The byte buffer containing some part of the header value.
     * @param off The offset of the header value in above buffer.
     * @param len The length of the header value in above buffer.
     */

    public void addBytes(byte buf[], int off, int len) {
	if ( raw != null ) {
	    int  nl   = len + rlen;
	    byte nr[] = new byte[nl+1];
	    System.arraycopy(raw, roff, nr, 0, rlen);
	    nr[rlen] = (byte) ',';
	    System.arraycopy(buf, off, nr, rlen+1, len);
	    raw  = nr;
	} else {
	    raw = new byte[len];
	    System.arraycopy(buf, off, raw, 0, len);
	}
	roff    = 0;
	rlen    = raw.length;
	isValid = false;
    }

    /**
     * HeaderValue implementation - Reset the header byte value.
     * @param buf The byte buffer containing some part of the header value.
     * @param off The offset of the header value in above buffer.
     * @param len The length of the header value in above buffer.
     */

    public void setBytes(byte buf[], int off, int len) {
	raw = new byte[len];
	System.arraycopy(buf, off, raw, 0, len);
	roff = 0;
	rlen = raw.length;
	isValid = false;
    }

    /**
     * Set this Header Value by parsing the given String.
     * @param strval The String value for that object.
     * @return Itself.
     */

    public void setString(String strval) {
	int slen = strval.length();
	raw      = new byte[slen];
	roff     = 0;
	rlen     = slen;
	strval.getBytes(0, slen, raw, 0);
	isValid  = false;
    }

    /**
     * HeaderValue implemenntation - Get this header value.
     * @return An object representing the parsed value for this header.
     */

    abstract public Object getValue() ;

    // needs to define it as this is an abstract class
    protected Object clone()
	throws CloneNotSupportedException 
    {
	return super.clone();
    }

    public BasicValue() {
	isValid = false;
    }

}
