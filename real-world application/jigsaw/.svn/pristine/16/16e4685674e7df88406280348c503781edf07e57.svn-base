// HttpMessage.java
// $Id: HttpMessage.java,v 1.1 2010/06/15 12:19:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.util.ArrayDictionary;
import org.w3c.util.EmptyEnumeration;

import org.w3c.www.mime.MimeHeaderHolder;
import org.w3c.www.mime.MimeParser;

/**
 * The basic class for all HTTP messages, as define in the HTTP spec.
 * This class is the base class for a number of other classes, including
 * both the ingoing/outgoing requests and replies.
 */

public class HttpMessage implements MimeHeaderHolder, Cloneable, HTTP {

    // FIXME doc
    public final static int EMIT_HEADERS = (1<<0);
    public final static int EMIT_BODY    = (1<<2);
    public final static int EMIT_FOOTERS = (1<<3);
    public final static int EMIT_ALL     = ((1<<0)|(1<<1)|(1<<2));

    // HTTP message well-known headers
    public static int H_CACHE_CONTROL 	  = 0;
    public static int H_CONNECTION    	  = 1;
    public static int H_PROXY_CONNECTION  = 2;
    public static int H_DATE          	  = 3;
    public static int H_PRAGMA        	  = 4;
    public static int H_TRANSFER_ENCODING = 5;
    public static int H_UPGRADE           = 6;
    public static int H_VIA               = 7;

    public static int H_PROTOCOL          = 8;
    public static int H_PROTOCOL_REQUEST  = 9;
    public static int H_PROTOCOL_INFO     = 10;
    public static int H_PROTOCOL_QUERY    = 11;

    public static int H_SET_COOKIE        = 12;
    public static int H_COOKIE            = 13;

    public static int H_TRAILER           = 14;

    public static int H_MAN_EXT           = 15;
    public static int H_OPT_EXT           = 16;
    public static int H_CMAN_EXT          = 17;
    public static int H_COPT_EXT          = 18;

    public static int MAX_HEADERS = 61;

    /**
     * The header value factory.
     */
    protected static Hashtable factory = new Hashtable(23) ;

    /**
     * The header value repository.
     * At this time, I am using this quite inefficient scheme, but the API
     * have been carefully designed to enable a more efficient implementation.
     */
    protected Dictionary headers = null ;
    /**
     * The major version of this message, according to HTTP specs.
     */
    protected short major = 1;
    /**
     * The minoir version of this message, according to HTTP specs.
     */
    protected short minor = 1;
    /**
     * The date at which this message was last emitted.
     */
    protected long emitdate = -1;
    /**
     * The state dictionary.
     */
    protected ArrayDictionary state = null;
    /**
     * The extension namespace counter.
     */
    private int extCurrentNamespace = 10;

    // FIXME
    protected 
    HeaderValue values[] = null;

    protected static
    HeaderDescription descriptors[] = new HeaderDescription[MAX_HEADERS];

    // Initialize the global header factory

    protected final static void registerHeader(String name, String cls) {
	HeaderDescription d = new HeaderDescription(name, cls);
	factory.put(d.getName(), d);
    }

    protected final static void registerHeader(String name
					       , String c
					       , int i) {
	HeaderDescription d = new HeaderDescription(name, c, i);
	descriptors[i] = d;
	factory.put(d.getName(), d);
    }

    static {
	registerHeader("Cache-Control"
		       , "org.w3c.www.http.HttpCacheControl"
		       , H_CACHE_CONTROL);
	registerHeader("Connection"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_CONNECTION);
	registerHeader("Proxy-Connection"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_PROXY_CONNECTION);
	registerHeader("Date"
		       , "org.w3c.www.http.HttpDate"
		       , H_DATE);
	registerHeader("Pragma"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_PRAGMA);
	registerHeader("Transfer-Encoding"
		       ,"org.w3c.www.http.HttpTokenList"
		       , H_TRANSFER_ENCODING);
	registerHeader("Upgrade"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_UPGRADE);
	registerHeader("Via"
		       , "org.w3c.www.http.HttpCaseTokenList"
		       , H_VIA);
	registerHeader("Trailer"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_TRAILER);
	registerHeader("Protocol"
		       , "org.w3c.www.http.HttpBag"
		       , H_PROTOCOL);
	registerHeader("Protocol-Request"
		       , "org.w3c.www.http.HttpBag"
		       , H_PROTOCOL_REQUEST);
	registerHeader("Protocol-Query"
		       , "org.w3c.www.http.HttpBag"
		       , H_PROTOCOL_QUERY);
	registerHeader("Protocol-Info"
		       , "org.w3c.www.http.HttpBag"
		       , H_PROTOCOL_INFO);
	registerHeader("Set-Cookie"
		       , "org.w3c.www.http.HttpSetCookieList"
		       , H_SET_COOKIE);
	registerHeader("Cookie"
		       , "org.w3c.www.http.HttpCookieList"
		       , H_COOKIE);
	registerHeader("Man",
		       "org.w3c.www.http.HttpExtList",
		       H_MAN_EXT);
	registerHeader("Opt",
		       "org.w3c.www.http.HttpExtList",
		       H_OPT_EXT);
	registerHeader("C-Man",
		       "org.w3c.www.http.HttpExtList",
		       H_CMAN_EXT);
	registerHeader("C-Opt",
		       "org.w3c.www.http.HttpExtList",
		       H_COPT_EXT);
    }

    /**
     * Get a header value, given its name.
     * @param name The name of the field whose value is to be fetched.
     * @param def The default value if the field is undefined.
     */

    public HeaderValue getHeaderValue(String name, HeaderValue def) {
	// Check the description, for fast access:
	HeaderValue       v = null;
	HeaderDescription d = (HeaderDescription) factory.get(name);
	if ( d != null  && d.offset >= 0) {
	    v = values[d.offset];
	} else if ( headers != null ) {
	    v = (HeaderValue) headers.get(name);
	}
	return v == null ? def : v ;
    }

    /**
     * Get a header value by name.
     * @param name The header's name.
     * @return The value of the header, as a String, or 
     * <strong>null</strong>
     * if undefined.
     */

    public final HeaderValue getHeaderValue(String name) {
	return getHeaderValue(name.toLowerCase(), null);
    }

    /**
     * Fast access to header value. 
     * <p>This method provides a very fast access to pre-defined header
     * values. You can use it on all headers that have an access token.
     * @param idx The token of the header to access.
     * @return An instance of <code>HeaderValue</code> or <strong>null
     * </strong> if undefined.
     */

    public final HeaderValue getHeaderValue(int idx) {
	return values[idx];
    }

    /**
     * Set a header value.
     * @param name The name of the header to define.
     * @param value It's HeaderValue.
     */

    public void setHeaderValue(String name, HeaderValue value) {
	String lname = name.toLowerCase();
	HeaderDescription d = (HeaderDescription) factory.get(lname);
	if(d != null && d.offset >= 0) {
	    values[d.offset] = value ;
	} else {
	    if ( headers == null )
		headers = new ArrayDictionary(5, 5);
	    headers.put(lname, value);
	}
    }

    /**
     * Get a header value, keyed by it's header description.
     * This is usefull when enumerating headers, by the mean of
     * <code>enumerateHeaderDescriptions</code>.
     * @param d The header description.
     * @return A HeaderValue instance, if the header is defined,
     * <strong>null</strong> otherwise.
     */

    public HeaderValue getHeaderValue(HeaderDescription d) {
	// Try to be as fast as possible:
	if ( d.offset >= 0 ) {
	    return values[d.offset];
	} else {
	    return ((headers != null) 
		    ? (HeaderValue) headers.get(d.getName()) 
		    : null);
	}
    }

    /**
     * Set a header value, keyed by it's header description.
     * @param d The header description.
     * @param v The HeaderValue instance, or <strong>null</strong> to
     * reset the header value.
     */

    public void setHeaderValue(HeaderDescription d, HeaderValue v) {
	if ( d.offset >= 0 ) {
	    values[d.offset] = v;
	} else {
	    if ( headers == null )
		headers = new ArrayDictionary(5, 5);
	    headers.put(d.getName(), v);
	}
    }

    /**
     * Fast write accessor to headers.
     * <p> This method provides a very fast write access to header
     * values. It can be used with any of the headers that have
     * a pre-defined access token.
     * @param idx The access token of the header's to write to.
     * @param value The new header value.
     */

    public final void setHeaderValue(int idx, HeaderValue value) {
	values[idx] = value;
    }

    /**
     * Remove a header, by name.
     * @param name The name of the header to remove.
     */

    public void removeHeader(String name) {
	String lname = name.toLowerCase();
	HeaderDescription d = (HeaderDescription) factory.get(lname);
	if ( d != null ) {
	    if ( d.offset >= 0 ) {
		values[d.offset] = null;
	    } else if ( headers != null ) {
		headers.remove(lname);
	    }
	}
    }

    /**
     * Remove a header, by address.
     * A fast version of the above.
     * @param idx The index of the header to remove.
     */

    public final void removeHeader(int idx) {
	if ((idx >= 0) && (idx < MAX_HEADERS))
	    values[idx] = null;
    }

    /**
     * Enumerate all the available headers for that message.
     * This method returns an enumeration of HeaderDescription instances,
     * which you can then use to access most efficiently header values.
     * @param all If <strong>true</strong> the enumeration will cover
     * all headers (even the ones that are not defined for that message)
     * otherwise, it will cover only defined headers.
     * @return An enumeration.
     */

    public Enumeration enumerateHeaderDescriptions(boolean all) {
	return new headerEnumerator(this, all);
    }

    /**
     * Enumerate all the headers defined for that message.
     * This method returns an enumeration of HeaderDescription instances,
     * which you can then use to access most efficiently header values.
     * @return An enumeration.
     */

    public Enumeration enumerateHeaderDescriptions() {
	return new headerEnumerator(this, false);
    }

    /**
     * State management - Add a piece of state to this request.
     * If the piece of state already exists, it is overriden by the new 
     * value.
     * @param name The name of the piece of state to define.
     * @param value It's corresponding value, or <strong>null</strong> to
     * reset the value.
     */

    public void setState(String name, Object value) {
	if ( value != null ) {
	    if ( state == null )
		state = new ArrayDictionary(4, 4);
	    state.put(name, value);
	} else if ( state != null ) {
	    state.remove(name);
	}
    }

    /**
     * State management - Lookup the value of a state on this request.
     * @param name The name of the piece of state to look for.
     * @return An object, if the piece of state is defined, <strong>null
     * </strong> otherwise.
     */

    public Object getState(String name) {
	return (state == null) ? null : state.get(name);
    }

    /**
     * State management - Remove a piece of state from this request.
     * @param name The name of the piece of state to remove.
     */

    public void delState(String name) {
	if ( state != null )
	    state.remove(name);
    }

    /**
     * State management - Is the given state defined for the message ?
     * @return A boolean <strong>true</strong> if the state is defined, 
     * <strong>false</strong> otherwise.
     */

    public boolean hasState(String name) {
	return (state != null) && (state.get(name) != null);
    }

    /**
     * Get an enumeration of the states names.
     * @return an Enumeration
     */
    public Enumeration getStateNames() {
	return ((state != null) ? state.keys() : new EmptyEnumeration());
    }

    /**
     * Get a clone of this HTTP message.
     * @return An HttpMessage, of the class of the message receiver.
     */

    public HttpMessage getClone() {
	try {
	    // Start by a std clone:
	    HttpMessage cl =  (HttpMessage) clone();
	    // Then all tables:
	    cl.values  = new HeaderValue[MAX_HEADERS];
	    System.arraycopy(values, 0, cl.values, 0, MAX_HEADERS);
	    if ( cl.headers != null ) 
		cl.headers = ((ArrayDictionary) 
			      ((ArrayDictionary) headers).clone());
	    if ( cl.state != null )
		cl.state = (ArrayDictionary) state.clone();
	    return cl;
	} catch (Exception ex) {
	    throw new RuntimeException ("Clone not supported !");
	}
    }

    /**
     * Get a clone of this HTTP message.
     * It is a semi-deep clone, not a complete one.
     * @return An HttpMessage, of the class of the message receiver.
     */

    public HttpMessage getDeeperClone() {
	try {
	    // Start by a std clone:
	    HttpMessage cl =  (HttpMessage) clone();
	    // Then all tables:
	    cl.values  = new HeaderValue[MAX_HEADERS];
	    for (int i=0; i< MAX_HEADERS; i++) {
		if (values[i] != null) {
		    cl.values[i] = (HeaderValue)
			                      ((BasicValue) values[i]).clone();
		}
	    }
	    // that _should_ be ok.
	    if ( cl.headers != null ) 
		cl.headers = ((ArrayDictionary) 
			      ((ArrayDictionary) headers).clone());
	    // FIXME need to do a better cloning of that.
	    if ( cl.state != null )
		cl.state = (ArrayDictionary) state.clone();
	    return cl;
	} catch (Exception ex) {
	    throw new RuntimeException ("Clone not supported !");
	}
    }

    /**
     * Get a header field value as a String.
     * @param name The name of the header.
     * @return A String giving the header value, or <strong>null</strong>
     *    if undefined.
     */

    public String getValue(String name) {
	HeaderValue value = getHeaderValue(name);
	return (value != null) ? value.toExternalForm() : null;
    }

    /**
     * Get a header field value as a String.
     * @param d The header description.
     * @return The String value for the given header, or <strong>null</strong>
     * if undefined.
     */

    public String getValue(HeaderDescription d) {
	HeaderValue v = getHeaderValue(d);
	return (v == null) ? null : v.toString();
    }

    /**
     * Define a new header field.
     * @param name The name of the header to be defined or reset.
     * @param value It's String value, or <strong>null</strong> to reset
     * the value.
     */

    public void setValue(String name, String strval) {
	String            lname = name.toLowerCase();
	HeaderDescription d     = null;
	// If reset ing value:
	if ( strval == null ) {
	    d = (HeaderDescription) factory.get(lname);
	    if ( d != null ) {
		if ( d.offset >= 0 ) {
		    values[d.offset] = null;
		} else if ( headers != null ) {
		    headers.remove(lname);
		}
	    }
	    return;
	}
	// Get or create the appropriate value holder:
	HeaderValue value = getHeaderValue(lname);
	if ( value == null ) {
	    if ( d == null )
		d = (HeaderDescription) factory.get(lname);
	    if ( d == null ) {
		registerHeader(name, "org.w3c.www.http.HttpString");
		value = new HttpString();
	    } else {
		value = d.getHolder();
	    }
	} 
	// Set (or reset) the byte value:
	byte bval[] = new byte[strval.length()];
	strval.getBytes(0, bval.length, bval, 0);
	value.setBytes(bval, 0, bval.length);
	// Register this new header:
	setHeaderValue(lname, value);
    }

    /**
     * Probe this message for a defined header.
     * @param name The name of the header to check.
     * @return <strong>true</strong> if the header is defined, <strong>
     *    false</strong> otherwise.
     */

    public boolean hasHeader(String name) {
	return getHeaderValue(name) != null;
    }

    /**
     * Probe this message for a defined header, fast access !
     * @param idx The index of the well-known header to check.
     * @return <strong>true</strong> if the header is defined, <strong>
     *    false</strong> otherwise.
     */

    public boolean hasHeader(int idx) {
	return values[idx] != null;
    }

    /**
     * MimeHeaderHolder implementation - The MIME parser callback.
     * This method is called if the HttpMessage is created by parsing 
     * an input stream. Each time the MIME parser detects a new header
     * field, it calls back this method.
     * @param name The name of the header that has been encountered.
     * @param buf The buffer containing the header value.
     * @param off The offset of the header value in the above buffer.
     * @param len The length of the header value in the above buffer.
     */

    public void notifyHeader(String name, byte buf[], int off, int len) {
	// Get the header value repository, or create a new one:
	String      lname = name.toLowerCase();
	HeaderValue value = getHeaderValue(lname);
	if ( value == null ) {
	    HeaderDescription d = (HeaderDescription) factory.get(lname);
	    if ( d == null ) {
		// Slow header access anyway:
		value = new HttpString();
		if ( headers == null )
		    headers = new ArrayDictionary(5, 5);
		registerHeader(name, "org.w3c.www.http.HttpString");
		headers.put(lname, value);
	    } else {
		value = d.getHolder();
		if (d.offset >= 0) {
		    values[d.offset] = value;
		} else {
		    if ( headers == null )
			headers = new ArrayDictionary(5, 5);
		    headers.put(lname, value);
		}
	    }
	}
	// Notify the header value of the newly received bunch of bytes:
	value.addBytes(buf, off, len);
    }

    /**
     * MimeHeaderHolder implementation - HTTP message about to be parsed.
     * No further action is required at this point (we do not distinguish 
     * between request or reply here). The MIME parsing is to continue normally
     * so we return <strong>false</strong>.
     * @return Always <strong>false</strong> to conotinue the MIME parsing.
     * @exception HttpParserException if parsing failed.
     * @exception IOException if an IO error occurs.
     */

    public boolean notifyBeginParsing(MimeParser parser) 
	 throws HttpParserException, IOException
    {
	return false;
    }

    /**
     * MimeHeaderHolder implementation - HTTP message parsing done.
     * Nothing special to be done here, return straight.
     * @exception HttpParserException if parsing failed.
     * @exception IOException if an IO error occurs.
     */

    public void notifyEndParsing(MimeParser parser) 
	 throws HttpParserException, IOException
    {
	return;
    }

    /**
     * This message is about to be emited.
     * Take any appropriate actions.
     * @exception IOException if an IO error occurs.
     */

    protected void startEmit(OutputStream out, int what) 
	throws IOException
    {
	return ;
    }

    /**
     * This message has been emited.
     * Take any appropriate action.
     * @exception IOException if an IO error occurs.
     */

    protected void endEmit(OutputStream out, int what)
	throws IOException
    {
	return ;
    }

    /**
     * Emit the headers.
     * @exception IOException if an IO error occurs.
     */
    protected void emitHeaders(OutputStream out, int what) 
	throws IOException
    {
	if ((what & EMIT_HEADERS) != EMIT_HEADERS) {
	    return;
	}	
	// If no date is set, then it's time to set it ourself:
	emitdate = System.currentTimeMillis();
	if ( ! hasHeader(H_DATE) ) {
	    setHeaderValue(H_DATE, new HttpDate(true, emitdate));
	}
	//Update the Connection Header for hop by hop extensions
	String oldconn[] = getConnection();
	HttpTokenList connection   = null;
	if (oldconn != null) {
	    connection = new HttpTokenList(oldconn);
	} else {
	    connection = new HttpTokenList(new String[0]);
	}
	HttpExtList extl = getHttpCManExtDecl();
	if (extl != null) {
	    connection.addToken("C-Man", false);
	    HttpExt exts[] = extl.getHttpExts();
	    for (int i=0 ; i < exts.length ; i++) {
		HttpExt     ext     = exts[i];
		Dictionary  headers = getExtensionHeaders(ext);
		Enumeration henum   = headers.keys();
		while (henum.hasMoreElements()) {
		    String header = 
			ext.getRealHeader((String) henum.nextElement());
		    connection.addToken(header, false);
		}
	    }
	}
	extl = getHttpCOptExtDecl();
	if (extl != null) {
	    connection.addToken("C-Opt", false);
	    HttpExt exts[] = extl.getHttpExts();
	    for (int i=0 ; i < exts.length ; i++) {
		HttpExt     ext     = exts[i];
		Dictionary  headers = getExtensionHeaders(ext);
		Enumeration henum   = headers.keys();
		while (henum.hasMoreElements()) {
		    String header = 
			ext.getRealHeader((String) henum.nextElement());
		    connection.addToken(header, false);
		}
	    }
	}

	if (hasHeader("C-Ext"))
	    connection.addToken("C-Ext", false);

	String tokens[] = (String[])connection.getValue();
	if ((tokens != null) && (tokens.length > 0))
	    setHeaderValue(H_CONNECTION, connection);
	
	//update cache control for Ext header
	if (hasHeader("Ext"))
	    addNoCache("Ext");

	// Emit well-known headers first:
	for (int i = 0 ; i < MAX_HEADERS ; i++) {
	    HeaderDescription d = descriptors[i];
	    HeaderValue       v = values[i];
	    if (v instanceof HttpSetCookieList) {
		// ugly hack :(
		HttpSetCookieList hscl = (HttpSetCookieList) v;
		if (hscl.isOriginal()) {
		    int nbc = hscl.length();
		    if (nbc == 0) { //the ugliest hack I have ever wrote ;-)
			out.write(d.getTitle());
			out.write(':'); out.write(' ');
			v.emit(out);
			out.write('\r'); out.write('\n');
		    } else {
			for (int j = 0 ; j < nbc ; j++) {
			    out.write(d.getTitle());
			    out.write(':'); out.write(' ');
			    hscl.emitCookie(out, j);
			    out.write('\r'); out.write('\n');
			}
		    }
		} else {
		    int rcs = hscl.copyLength();
		    for (int j = 0 ; j < rcs ; j++) {
			out.write(d.getTitle());
			out.write(':'); out.write(' ');
			hscl.emitCopyCookie(out, j);
			out.write('\r'); out.write('\n');
		    }
		}
	    } else if ( v != null ) {
		out.write(d.getTitle());
		out.write(':'); out.write(' ');
		v.emit(out);
		out.write('\r'); out.write('\n');
	    }
	}
	// Emit extension headers:
	if ( headers != null ) {
	    Enumeration e = headers.keys();
	    while ( e.hasMoreElements() ) {
		String            n = (String) e.nextElement();
		HeaderDescription d = (HeaderDescription) factory.get(n);
		HeaderValue       v = (HeaderValue) headers.get(n);
		if ( v != null ) {
		    out.write(d.getTitle());
		    out.write(':'); out.write(' ');
		    v.emit(out);
		    out.write('\r'); out.write('\n');
		}
	    }
	}
	out.write('\r'); out.write('\n');
    }

    public void dump(OutputStream out) {
	try {
	    emitHeaders(out, EMIT_HEADERS);
	} catch (Exception ex) {
	}
    }

    /**
     * Emit this message to the given output stream.
     * This methods emits the given message to the stream, after invoking
     * the <code>startEmit</code> method. Once the whole message has been 
     * emited, the <code>endEmit</code> method is called back.
     * @param out The output stream to emit the message to.
     * @exception IOException If the message couldn't be emited to the
     * given stream, due to IO errors.
     */

    public void emit(OutputStream out) 
	throws IOException
    {
	startEmit(out, EMIT_ALL) ;
	if ( major > 0 )
	    emitHeaders(out, EMIT_ALL);
	endEmit(out, EMIT_ALL) ;
    }
    /**
     * @param out The output stream to emit the message to. 
     * @param what (fixme doc)
     * @exception IOException If the message couldn't be emited to the 
     * given stream, due to IO errors. 
     */ 
    public void emit(OutputStream out, int what) 
	throws IOException
    {
	startEmit(out, what);
	emitHeaders(out, what);
	endEmit(out, what);
    }

    /**
     * Header accessor - set the cache control associated with the message.
     * This method should not be used in general, it's much more preferable
     * to use the various cache control accessors available.
     * @param control The cache control policy, or <strong>null</strong>
     * to reset the value.
     */

    public void setCacheControl(HttpCacheControl control) {
	setHeaderValue(H_CACHE_CONTROL, control);
    }

    /**
     * Header accessor - get the cache control policy.
     * @return The current cache control policy, or <strong>null</strong>
     *     if undefined.
     */

    public HttpCacheControl getCacheControl() {
	HeaderValue value = getHeaderValue(H_CACHE_CONTROL);
	return (value != null) ? (HttpCacheControl) value.getValue() : null;
    }

    /**
     * Set the <code>max-age</code> value of the associated cache control.
     * This method hides as much as possible, the difference between 
     * HTTP/1.1 max-age, and HTTP/1.0 expires headers. It will set only 
     * the appropriate one.
     * @param maxage The max-age value, or <strong>-1</strong> to reset the 
     * value.
     */

    public void setMaxAge(int maxage) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    if ( maxage == -1 )
		return ;
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setMaxAge(maxage);
    }

    /**
     * Get the <code>max-age</code> value for the current cache control.
     * @return The max age value, as an integer, or <strong>-1</strong> if
     * undefined.
     */

    public int getMaxAge() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? -1 : cc.getMaxAge();
    }

    /**
     * Set the <code>s-maxage</code> value of the associated cache control.
     * Set the s-maxage value on an HTTP/1.1 reply
     * @param maxage The max-age value in seconds, or <strong>-1</strong>
     * to reset the value.
     */

    public void setSMaxAge(int smaxage) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setSMaxAge(smaxage);
    }

    /**
     * Get the <code>s-maxage</code> value for the current cache control.
     * @return The s-maxage value, as an integer (seconds), or 
     * <strong>-1</strong> if undefined.
     */
    public int getSMaxAge() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? -1 : cc.getSMaxAge();
    }

    /**
     * Get the <code>no-cache</code> directive of the cache control header.
     * @return A list of token (potentially empty) encoded as an array of
     * String (with 0 length if empty), or <strong>null</strong> if
     * undefined.
     */

    public String[] getNoCache() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? null : cc.getNoCache();
    }

    /**
     * Set the <code>no-cache</code> directive of the cache control header.
     * @param nocache A list of headers name encoded as an array of String
     * (of length possibly <strong>0</strong>), or <strong>null</strong>
     * to reset the value.
     */

    public void setNoCache(String nocache[]) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    if ( nocache == null )
		return;
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setNoCache(nocache);
    }

    /**
     * Set the <code>no-cache</code> directive globally.
     */

    public void setNoCache() {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) 
	    setCacheControl(cc = new HttpCacheControl(true));
	cc.setNoCache();
    }

    /**
     * Add the given header name to the <code>no-cache</code> directive.
     * @param name The header name to add there.
     */

    public void addNoCache(String name) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null )
	    setCacheControl(cc = new HttpCacheControl(true));
	cc.addNoCache(name);
    }

    /**
     * Check the <code>no-store</code> directive of the cache control header.
     * @return A boolean <strong>true</strong> if set, <strong>false</strong>
     * otherwise.
     */

    public boolean checkNoStore() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? false : cc.checkNoStore();
    }

    /**
     * Set the <code>no-store</code> directive.
     * @param onoff Turn it on or off.
     */

    public void setNoStore(boolean onoff) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    if ( ! onoff )
		return;
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setNoStore(onoff);
    }

    /**
     * Check the <code>only-if-cached</code> directive.
     * @return A boolean, <strong>true</strong> if the directive is set,
     * <strong>false</strong> otherwise.
     */

    public boolean checkOnlyIfCached() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? false : cc.checkOnlyIfCached();
    }

    /**
     * Set the <code>only-if-cached</code> directive.
     * @param onoff Turn it on or off.
     */

    public void setOnlyIfCached(boolean onoff) {
	HttpCacheControl cc = getCacheControl();
	if (cc == null) {
	    if ( ! onoff )
		return;
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setOnlyIfCached(onoff);
    }

    // FIXME add all the other cache control directive

    /**
     * Header accessor - set the connection header value.
     * @param tokens The connection tokens as a String array, or <strong>null
     * </strong> to reset the value.
     */

    public void setConnection(String tokens[]) {
	setHeaderValue(H_CONNECTION
		       , ((tokens == null)
			  ? null
			  : new HttpTokenList(tokens)));
    }

    /**
     * Header accessor - get the connection header value.
     * @return The tokens of the connection header, as a String array, or
     * <strong>null</strong> if undefined.
     */

    public String[] getConnection() {
	HeaderValue value = getHeaderValue(H_CONNECTION);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Add the given header name to the <code>Connection</code> header.
     * @param name The name of the header to add to the <code>Connection</code>
     * header.
     */

    public void addConnection(String name) {
	HttpTokenList list = (HttpTokenList) getHeaderValue(H_CONNECTION);
	if ( list == null ) {
	    String sList[] = new String[1];
	    sList[0]       = name.toLowerCase();
	    setHeaderValue(H_CONNECTION, new HttpTokenList(sList));
	} else {
	    list.addToken(name, false);
	}
    }

    /**
     * Does the connection header include the given token ?
     * @return A boolean.
     */

    public boolean hasConnection(String tok) {
	HttpTokenList list = (HttpTokenList) getHeaderValue(H_CONNECTION);
	return (list == null) ? false : list.hasToken(tok, false);
    }

    /**
     * Header accessor - set the proxy connection header value.
     * @param tokens The connection tokens as a String array, or
     * <strong>null</strong> to reset the value.
     */

    public void setProxyConnection(String tokens[]) {
	setHeaderValue(H_PROXY_CONNECTION
		       , ((tokens == null)
			  ? null
			  : new HttpTokenList(tokens)));
    }

    /**
     * Add the given header name to the <code>Proxy-Connection</code> header.
     * @param name The name of the header to add to the 
     * <code>Proxy-Connection</code> header.
     */

    public void addProxyConnection(String name) {
	HttpTokenList list = (HttpTokenList)getHeaderValue(H_PROXY_CONNECTION);
	if ( list == null ) {
	    String sList[] = new String[1];
	    sList[0]       = name.toLowerCase();
	    setHeaderValue(H_PROXY_CONNECTION, new HttpTokenList(sList));
	} else {
	    list.addToken(name, false);
	}
    }

    /**
     * Header accessor - get the proxy connection header value.
     * @return The tokens of the connection header, as a String array,
     * or <strong>null</strong> if undefined.
     */

    public String[] getProxyConnection() {
	HeaderValue value = getHeaderValue(H_PROXY_CONNECTION);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Does the proxy connection header defines the given token.
     * @param tok The token to check for.
     * @return A boolean.
     */

    public boolean hasProxyConnection(String tok) {
	HttpTokenList l = (HttpTokenList) getHeaderValue(H_PROXY_CONNECTION);
	return (l == null) ? false : l.hasToken(tok, false);
    }

    /**
     * Header accessor - set the date of this message.
     * @param date The date of the message, following Java runtime conventions
     * (number of milliseconds since epoch), or <strong>-1</strong> to
     * reset the value.
     */

    public void setDate(long date) {
	setHeaderValue(H_DATE
		       , ((date == -1) ? null : new HttpDate(true, date)));
    }

    /**
     * Header accessor - get the date of this message.
     * @return A long giving the date of this message, following the Java 
     *    runtime convention (milliseconds since epoch), or <strong>-1</strong>
     *    if undefined.
     */

    public long getDate() {
	HeaderValue date = getHeaderValue(H_DATE);
	return (date != null) ? ((Long) date.getValue()).longValue() : -1;
    }

    /**
     * Header accessor - set the pragmas applicable to this message.
     * @param tokens The pragma tokens as a String array, or <strong>null
     * </strong> to reset the value.
     */

    public void setPragma(String tokens[]) {
	setHeaderValue(H_PRAGMA
		       , ((tokens == null)
			  ? null
			  : new HttpTokenList(tokens)));
    }

    /**
     * Header accessor - get the pragmas applicable to this message.
     * @return The pragma tokens applicable to this message, encoded
     *    as a String array, or <strong>null</strong> if undefined.
     */

    public String[] getPragma() {
	HeaderValue value = getHeaderValue(H_PRAGMA);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Header accessor - Check for a given pragma.
     * @param pragma The pragma to check for.
     * @return A boolean <strong>true</strong> if this pragma is set, 
     * <strong>false</strong> otherwise.
     */

    public boolean hasPragma(String pragma) {
	HttpTokenList list = (HttpTokenList) getHeaderValue(H_PRAGMA);
	return (list != null) ? list.hasToken(pragma, false) : false;
    }

    /**
     * Add the given directive to the <code>Pragma</code> header.
     * @param name The name of the directive to add to the <code>Pragma</code>
     * header.
     */

    public void addPragma(String name) {
	HttpTokenList list = (HttpTokenList) getHeaderValue(H_PRAGMA);
	if ( list == null ) {
	    String sList[] = new String[1];
	    sList[0]       = name.toLowerCase();
	    setHeaderValue(H_PRAGMA, new HttpTokenList(sList));
	} else {
	    list.addToken(name, false);
	}
    }

    /**
     * Header accessor - set the transfer encoding for this message.
     * This just sets the transfer encoding, it is up to the rest of the
     * application to make sure that the encoding is actually applied 
     * at emiting time.
     * @param tokens The transfer encoding tokens as a String array, or
     * <strong>null</strong> to reset the value.
     */

    public void setTransferEncoding(String tokens[]) {
	setHeaderValue(H_TRANSFER_ENCODING
		       , ((tokens == null)
			  ? null
			  : new HttpTokenList(tokens)));
    }

    /**
     * Add an encoding token to the given reply stream (ie the body).
     * @param name The name of the encoding to add.
     */

    public void addTransferEncoding(String name) {
	HttpTokenList l = (HttpTokenList) getHeaderValue(H_TRANSFER_ENCODING);
	if ( l == null ) {
	    String sList[] = new String[1];
	    sList[0]       = name.toLowerCase();
	    setHeaderValue(H_TRANSFER_ENCODING, new HttpTokenList(sList));
	} else {
	    l.addToken(name, false);
	}
    }

    /**
     * Header accessor - get the transfer encoding applying to this message.
     * @return The list of encoding tokens, as a String array, or <strong>
     *    null</strong> if undefined.
     */

    public String[] getTransferEncoding() {
	HeaderValue value = getHeaderValue(H_TRANSFER_ENCODING);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Header accessor - Check for a given transfer encoding.
     * @param encoding The pragma to check for.
     * @return A boolean <strong>true</strong> if this encoding is set, 
     * <strong>false</strong> otherwise.
     */

    public boolean hasTransferEncoding(String encoding) {
	HttpTokenList l = (HttpTokenList) getHeaderValue(H_TRANSFER_ENCODING);
	return (l != null) ? l.hasToken(encoding, false) : false;
    }

    /**
     * Header accessor - set the upgrade header of this message.
     * @param products An array of products you want this message to carry
     * or <strong>null</strong> to reset the value.
     */

    public void setUpgrade(String products[]) {
	setHeaderValue(H_UPGRADE
		       , ((products == null)
			  ? null
			  : new HttpTokenList(products)));
    }

    /**
     * Header accessor - get the upgrade header of this message.
     * @return A list of products, encoded as an array of String
     *    or <strong>null</strong> if undefined.
     */

    public String[] getUpgrade() {
	HeaderValue value = getHeaderValue(H_UPGRADE);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Header accessor - set the Via header of this message.
     * @param vias The hops to be placed in the <code>Via</code> header, or
     * <strong>null</strong> to reset the value.
     */

    public void setVia(String vias[]) {
	setHeaderValue(H_VIA
		       , ((vias == null) ? null : new HttpTokenList(vias)));
    }

    /**
     * Header accessor - get the via header of this message.
     * @return A Via array describing each hop of the message, or <strong>
     *    null</strong> if undefined.
     */

    public String[] getVia() {
	HeaderValue value = getHeaderValue(H_VIA);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Add a via clause to the via header.
     * @param via The new via clause.
     */

    public void addVia(String via) {
	HttpTokenList list = (HttpTokenList) getHeaderValue(H_VIA);
	if ( list == null ) {
	    String sList[] = new String[1];
	    sList[0]       = via;
	    setVia(sList);
	} else {
	    list.addToken(via, true);
	}
    }

    /**
     * Get the set of protocol extensions that have been applied to that
     * that message.
     * @return A bag containing the description of the protocol extensions
     * applied to that message, or <strong>null</strong>.
     */

    public HttpBag getProtocol() {
	HeaderValue value = getHeaderValue(H_PROTOCOL);
	return (value != null) ? (HttpBag) value.getValue() : null;
    }

    /**
     * Set the protocol extensions applied to that message.
     * @param protocols A bag instance, describing the protocol extensions
     * applied to the message, or <strong>null</strong> to reset previous
     * value.
     */

    public void setProtocol(HttpBag bag) {
	setHeaderValue(H_PROTOCOL, bag);
    }

    /**
     * Get the set of protocol extensions requested by this message.
     * @return A bag containing the description of the protocol extensions
     * requested by this message, or <strong>null</strong>.
     */

    public HttpBag getProtocolRequest() {
	HeaderValue value = getHeaderValue(H_PROTOCOL_REQUEST);
	return (value != null) ? (HttpBag) value.getValue() : null;
    }

    /**
     * Set the protocol extensions required by this message.
     * @param protocols A bag instance, describing the protocol extensions
     * required by the message, or <strong>null</strong> to reset previous
     * value.
     */

    public void setProtocolRequest(HttpBag bag) {
	setHeaderValue(H_PROTOCOL_REQUEST, bag);
    }

    /**
     * Get the protocol extensions informations carried by this message.
     * @return A bag containing the description of the protocol extensions
     * informations carried by that message, or <strong>null</strong>.
     */

    public HttpBag getProtocolInfo() {
	HeaderValue value = getHeaderValue(H_PROTOCOL_INFO);
	return (value != null) ? (HttpBag) value.getValue() : null;
    }

    /**
     * Attach protocol extensions informations to that message.
     * @param protocols A bag instance, describing the protocol extensions
     * informations to attach to the message, or <strong>null</strong> to 
     * reset previous value.
     */

    public void setProtocolInfo(HttpBag bag) {
	setHeaderValue(H_PROTOCOL_INFO, bag);
    }

    /**
     * Get the set of protocol extensions that are queried through this
     * message.
     * @return A bag containing the description of the protocol extensions
     * queried by that message, or <strong>null</strong>.
     */

    public HttpBag getProtocolQuery() {
	HeaderValue value = getHeaderValue(H_PROTOCOL_QUERY);
	return (value != null) ? (HttpBag) value.getValue() : null;
    }

    /**
     * Set the protocol extensions queried by that message.
     * @param protocols A bag instance, describing the protocol extensions
     * queried by the message, or <strong>null</strong> to reset previous
     * value.
     */

    public void setProtocolQuery(HttpBag bag) {
	setHeaderValue(H_PROTOCOL_QUERY, bag);
    }

    /**
     * Get this message trailer
     * @return A list of encoding tokens, encoded as a String array, or 
     *    <strong>null</strong> if undefined.
     */

    public String[] getTrailer() {
	HeaderValue value = getHeaderValue(H_TRAILER);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set this message trailer
     * @param encodings A list of encoding tokens, encoded as a String array
     * or <strong>null</strong> to reset the value.
     */

    public void setTrailer(String trailers[]) {
	setHeaderValue(H_TRAILER
		       , ((trailers == null)
			  ? null
			  : new HttpTokenList(trailers)));
    }

    /**
     * Get the value of the SetCookie header.
     * @return AN HttpSetCookie instance, or <strong>null</strong> if 
     * undefined.
     */

    public HttpSetCookieList getSetCookie() {
	HeaderValue value = getHeaderValue(H_SET_COOKIE);
	return (value != null) ? (HttpSetCookieList) value.getValue() : null;
    }

    /**
     * Set the value of the Set-Cookie header.
     * @param setcookies The HttpSetCookie value.
     */

    public void setSetCookie(HttpSetCookieList setcookie) {
	setHeaderValue(H_SET_COOKIE, setcookie);
    }

    /**
     * Get the cookies attached to that message.
     * @return An instance of HttpCookie holding the list of available 
     * cookies, or <strong>null</strong> if undefined.
     */

    public HttpCookieList getCookie() {
	HeaderValue value = getHeaderValue(H_COOKIE);
	return (value != null) ? (HttpCookieList) value.getValue() : null;
    }

    /**
     * Set the cookies attached to this message.
     * @param cookies The HttpCookie instance describing the cookies, or
     * <strong>null</strong> to reset value.
     */

    public void setCookie(HttpCookieList cookie) {
	setHeaderValue(H_COOKIE, cookie);
    }

    /**
     * Get the String identifying the HTTP version used for this message.
     * @return A String identifying the protocol version.
     */

    public String getVersion() {
	switch(major) {
	  case 0:
	    switch(minor) {
	      case 9:
		return "HTTP/0.9";
	    }
	    break;
	  case 1:
	    switch(minor) {
	      case 0:
		return "HTTP/1.0";
	      case 1:
		return "HTTP/1.1";
	    }
	}
	return "HTTP/"+major+"."+minor;
    }

    /**
     * Get the major version number of this message.
     * This method returns the major version that the caller should use to
     * <em>drive</em> message processing. It <em>may not</em> match the
     * version number actually emitted on the wire, which is computed
     * by the API itself.
     * @return A ninteger giving the major version number.
     */

    public short getMajorVersion() {
	return major;
    }

    /**
     * Get the minor version number of this message.
     * This method returns the minor version that the caller should use
     * to drive message processing. It <em>may not</em> match the
     * minor version number emitted on the wire, which is computed by the
     * API itself.
     * @return An integer giving the minor version number.
     */

    public short getMinorVersion() {
	return minor;
    }

    /**
     * Get the date at which this message was last emitted, if ever it was.
     * @return The date, in milliseconds since Java epoch at which this message
     * was emitted, or <strong>-1</strong> if the message was never emitted.
     */

    public long getEmitDate() {
	return emitdate;
    }

    /**
     * Get an Http Extension Listfrom the following list:
     * <ul>
     * <li>Mandatory (End-to-End) Extensions
     * <li>Mandatory (Hop-by-Hop) Extensions
     * <li>Optionnal (End-to-End) Extensions
     * <li>Optionnal (hop-by-Hop) Extensions
     * </ul>
     * @param ext the Extension id
     * @return a HttpExtList containing the extension declaration (or null)
     */
    public HttpExtList getExtList(String id) {
	HttpExtList extlist = getHttpManExtDecl();
	HttpExt ext = (extlist != null ? extlist.getHttpExt(id) : null);
	if (ext == null) {
	    extlist = getHttpCManExtDecl();
	    ext = (extlist != null ? extlist.getHttpExt(id) : null);
	    if (ext == null) {
		extlist = getHttpOptExtDecl();
		ext = (extlist != null ? extlist.getHttpExt(id) : null);
		if (ext == null) {
		    extlist = getHttpCOptExtDecl();
		    ext = (extlist != null ? extlist.getHttpExt(id) : null);
		    if (ext == null) {
			extlist = null;
		    }
		}
	    }
	}
	return extlist;
    }

    /**
     * get the String value of the given header relative to the given 
     * Extension. This method search the header in the following declarations:
     * <ul>
     * <li>Mandatory (End-to-End) Extensions
     * <li>Mandatory (Hop-by-Hop) Extensions
     * <li>Optionnal (End-to-End) Extensions
     * <li>Optionnal (hop-by-Hop) Extensions
     * </ul>
     * @param ext the extension
     * @param header the extension header
     * @return a String (or null)
     */
    public String getExtHeader(String ext, String header) {
	String value = getManExtHeader(ext, header);
	if (value == null) {
	    value = getCManExtHeader(ext, header);
	    if (value == null) {
		value = getOptExtHeader(ext, header);
		if (value == null) {
		    value = getCOptExtHeader(ext, header);
		}
	    }
	}
	return value;
    }

    /**
     * get the String value of the given header relative to the given 
     * Extension. This method search the header in the following declarations:
     * <ul>
     * <li>Mandatory (End-to-End) Extensions
     * </ul>
     * @param ext the extension
     * @param header the extension header
     * @return a String (or null)
     */
    public String getManExtHeader(String ext, String header) {
	return getExtHeader(getHttpManExtDecl(), ext, header);
    }

    /**
     * get the String value of the given header relative to the given 
     * Extension. This method search the header in the following declarations:
     * <ul>
     * <li>Optionnal (End-to-End) Extensions
     * </ul>
     * @param ext the extension
     * @param header the extension header
     * @return a String (or null)
     */
    public String getOptExtHeader(String ext, String header) {
	return getExtHeader(getHttpOptExtDecl(), ext, header);
    }

    /**
     * get the String value of the given header relative to the given 
     * Extension. This method search the header in the following declarations:
     * <ul>
     * <li>Mandatory (Hop-by-Hop) Extensions
     * </ul>
     * @param ext the extension
     * @param header the extension header
     * @return a String (or null)
     */
    public String getCManExtHeader(String ext, String header) {
	return getExtHeader(getHttpCManExtDecl(), ext, header);
    }

    /**
     * get the String value of the given header relative to the given 
     * Extension. This method search the header in the following declarations:
     * <ul>
     * <li>Optionnal (hop-by-Hop) Extensions
     * </ul>
     * @param ext the extension
     * @param header the extension header
     * @return a String (or null)
     */
    public String getCOptExtHeader(String ext, String header) {
	return getExtHeader(getHttpCOptExtDecl(), ext, header);
    }

    private String getExtHeader(HttpExtList list, String ext, String header) 
    {
	if (list == null) {
	    return null;
	}
	HttpExt extheader = list.getHttpExt(ext);
	if (extheader != null) {
	    String realheader = extheader.getNamespace()+"-"+header;
	    return getValue(realheader);
	}
	return null;
    }

    /**
     * Get The Mandatory (End-to-End) Extension declaration list.
     * @return a HttpExtList instance or null;
     */
    public HttpExtList getHttpManExtDecl() {
	HeaderValue value = getHeaderValue(H_MAN_EXT);
	HttpExtList list  = null;
	if (value != null) {
	    list = (HttpExtList) value.getValue();
	    if (list != null) {
		list.setManOptFlag(HttpExtList.MAN);
	    }
	}
	return list;
    }

    /**
     * Set The Mandatory (End-to-End) Extension declaration list.
     * @param exts the extension declaration list.
     */
    public void setHttpManExtDecl(HttpExtList exts) {
	exts.setManOptFlag(HttpExtList.MAN);
	setNamespaces(exts);
	setHeaderValue(H_MAN_EXT, exts);
    }

    /**
     * Get The Mandatory (Hop-by-Hop) Extension declaration list.
     * @return a HttpExtList instance or null;     
     */
    public HttpExtList getHttpCManExtDecl() {
	HeaderValue value = getHeaderValue(H_CMAN_EXT);
	HttpExtList list  = null;
	if (value != null) {
	    list  = (HttpExtList) value.getValue();
	    if (list != null) {
		list.setManOptFlag(HttpExtList.CMAN);
	    }
	}
	return list;
    }

    /**
     * Set the Extension declaration.
     * WARNING: The ManOpt flag of exts must have been set.
     * @param exts the extension declaration list.
     */
    public void setHttpExtDecl(HttpExtList exts) {
	setNamespaces(exts);
	switch (exts.getManOptFlag()) 
	    {
	    case HttpExtList.MAN:
		setHeaderValue(H_MAN_EXT, exts);
		break;
	    case HttpExtList.CMAN:
		setHeaderValue(H_CMAN_EXT, exts);
		break;
	    case HttpExtList.OPT:
		setHeaderValue(H_OPT_EXT, exts);
		break;
	    case HttpExtList.COPT:
		setHeaderValue(H_COPT_EXT, exts);
		break;
	    default:
		setHeaderValue(H_MAN_EXT, exts);
	    }
    }

    /**
     * Set The Mandatory (Hop-by-Hop) Extension declaration list.
     * @param exts the extension declaration list.
     */
    public void setHttpCManExtDecl(HttpExtList exts) {
	exts.setManOptFlag(HttpExtList.CMAN);
	setNamespaces(exts);
	setHeaderValue(H_CMAN_EXT, exts);
    }

    /**
     * Get The Optionnal (End-to-End) Extension declaration list.
     * @return a HttpExtList instance or null;
     */
    public HttpExtList getHttpOptExtDecl() {
	HeaderValue value = getHeaderValue(H_OPT_EXT);
	HttpExtList list  = null;
	if (value != null) {
	    list = (HttpExtList) value.getValue();
	    if (list != null) {
		list.setManOptFlag(HttpExtList.OPT);
	    }
	}
	return list;
    }

    /**
     * Set The Optional (End-to-End) Extension declaration list.
     * @param exts the extension declaration list.
     */
    public void setHttpOptExtDecl(HttpExtList exts) {
	exts.setManOptFlag(HttpExtList.OPT);
	setNamespaces(exts);
	setHeaderValue(H_OPT_EXT, exts);
    }

    /**
     * Get The Optionnal (Hop-by-Hop) Extension declaration list.
     * @return a HttpExtList instance or null;     
     */
    public HttpExtList getHttpCOptExtDecl() {
	HeaderValue value = getHeaderValue(H_COPT_EXT);
	HttpExtList list  = null;
	if (value != null) {
	    list = (HttpExtList) value.getValue();
	    if (list != null) {
		list.setManOptFlag(HttpExtList.COPT);
	    }
	}
	return list;
    }

    /**
     * Set The Optional (Hop-by-Hop) Extension declaration list.
     * @param exts the extension declaration list.
     */
    public void setHttpCOptExtDecl(HttpExtList exts) {
	exts.setManOptFlag(HttpExtList.COPT);
	setNamespaces(exts);
	setHeaderValue(H_COPT_EXT, exts);
    }

    protected void setNamespaces(HttpExtList extl) {
	HttpExt exts[] = extl.getHttpExts();
	for (int i = 0 ; i < exts.length ; i++) {
	    if (exts[i].isGenerated())
		throw new HttpExtException("This extension Object is already "+
					   "associated to an HttpMessage: "+
					   exts[i].getName());
	    if (exts[i].needsHeaders())
		exts[i].setNamespace(generateExtNamespace());
	}
    }

    /**
     * Get the headers relative to the given Http Extension declaration.
     * @param ext the HttpExt
     * @return a Dictionnary of <String, HeaderValue>
     */
    public Dictionary getExtensionHeaders(HttpExt ext) {
	ArrayDictionary extheaders = new ArrayDictionary(5, 5);
	if (headers != null) {
	    Enumeration     henum      = headers.keys();
	    String          namespace  = ext.getNamespace();

	    if (namespace == null) {
		return extheaders;
	    }

	    namespace += "-";
	    
	    while (henum.hasMoreElements()) {
		String name = (String) henum.nextElement();
		if (name.startsWith(namespace)) {
		    //remove namespace
		    String realname = name.substring(3);
		    extheaders.put(realname, headers.get(name));
		}
	    }
	}
	return extheaders;
    }

    /**
     * Set an extension header relative to the given extension declaration.
     * @param ext The extension declaration
     * @param name the header name
     * @param value the header value
     */
    public synchronized void setExtensionHeader(HttpExt ext, 
						String name, 
						String value) 
    {
	if (ext.isGenerated())
	    throw new HttpExtException("This extension Object is already "+
				       "associated to an HttpMessage: "+
				       ext.getName());
	String namespace = ext.getNamespace();
	if (namespace == null) {
	    namespace = generateExtNamespace();
	    ext.setNamespace(namespace);
	}
	setValue(namespace+"-"+name, value);
    }

    /**
     * get a new namespace.
     * @return an int.
     */
    protected synchronized String generateExtNamespace() {
	return String.valueOf(extCurrentNamespace++);
    }

    public void setEnd2EndExtensionAcknowledgmentHeader() {
	setValue("Ext","");
    }

    public void setHopByHopExtensionAcknowledgmentHeader() {
	setValue("C-Ext","");
    }

    public final static void registerExtHeader(String name, String cls) {
	registerHeader(name, cls);
    }

    public HttpMessage(MimeParser parser) {
	// FIXME
	this.values  = new HeaderValue[MAX_HEADERS];
    }

    public HttpMessage() {
	// FIXME
	this.values  = new HeaderValue[MAX_HEADERS];
    }

	
}
