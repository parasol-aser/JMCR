// HttpReplyMessage.java
// $Id: HttpReplyMessage.java,v 1.1 2010/06/15 12:19:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import org.w3c.www.mime.MimeParser;

public class HttpReplyMessage extends HttpEntityMessage {
    // HTTP Reply message well-known headers
    public static int H_ACCEPT_RANGES       = 50;
    public static int H_AGE                 = 51;
    public static int H_LOCATION            = 52;
    public static int H_PROXY_AUTHENTICATE  = 53;
    public static int H_PUBLIC              = 54;
    public static int H_RETRY_AFTER         = 55;
    public static int H_SERVER              = 56;
    public static int H_VARY                = 57;
    public static int H_WARNING             = 58;
    public static int H_WWW_AUTHENTICATE    = 59;
    public static int H_AUTHENTICATION_INFO = 60;

    static {
	registerHeader("Accept-Ranges"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_ACCEPT_RANGES);
	registerHeader("Age"
		       , "org.w3c.www.http.HttpInteger"
		       , H_AGE);
	registerHeader("Location"
		       , "org.w3c.www.http.HttpString"
		       , H_LOCATION);
	registerHeader("Proxy-Authenticate"
		       , "org.w3c.www.http.HttpChallenge"
		       , H_PROXY_AUTHENTICATE);
	registerHeader("Public"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_PUBLIC);
	// String as it can be a number or a date...
	registerHeader("Retry-After"
		       , "org.w3c.www.http.HttpString"
		       , H_RETRY_AFTER);
	registerHeader("Server"
		       , "org.w3c.www.http.HttpString"
		       , H_SERVER);
	registerHeader("Trailer"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_TRAILER);
	registerHeader("Vary"
		       , "org.w3c.www.http.HttpCaseTokenList"
		       , H_VARY);
	registerHeader("Warning"
		       , "org.w3c.www.http.HttpWarningList"
		       , H_WARNING);
	registerHeader("WWW-Authenticate"
		       , "org.w3c.www.http.HttpChallenge"
		       , H_WWW_AUTHENTICATE);
	registerHeader("Authentication-Info"
		       , "org.w3c.www.http.HttpParamList"
		       , H_AUTHENTICATION_INFO);
    }

    /**
     * The status associated with this reply.
     */
    protected int status = -1;
    /**
     * The reason phrase.
     */
    protected String reason = null; 

    /**
     * Emit the status line before emiting the actual reply headers.
     * @param out The output stream to emit the reply to.
     */

    protected void startEmit(OutputStream out, int what) 
	 throws IOException
    {
	if ((what & EMIT_HEADERS) != EMIT_HEADERS)
	    return ;
	if ( major < 1 )
	    return;
	HttpBuffer buf = new HttpBuffer(64);
	buf.append(HTTP.byteArrayVersion);
	buf.append(' ');
	buf.appendInt(status);
	buf.append(' ');
	buf.append((reason != null) ? reason : "Unknown Status Code");
	buf.append('\r');
	buf.append('\n');
	buf.emit(out);
    }

    public void dump(OutputStream out) {
	// Dump the reply status line first, and then the headers
	try {
	    startEmit(out, EMIT_HEADERS);
	} catch (Exception ex) {
	}
	super.dump(out);
    }

    /**
     * MimeHeaderHolder implementation - Begining of reply parsing.
     * If we can determine that this reply version number is less then
     * 1.0, then we skip the header parsing by returning <strong>true</strong>
     * to the MIME parser.
     * <p>Otherwise, we parse the status line, and return <strong>false
     * </strong> to make the MIME parser continue.
     * @return A boolean <strong>true</strong> if the MIME parser should stop
     * parsing, <strong>false</strong> otherwise.
     * @exception IOException If some IO error occured while reading the 
     * stream.
     * @exception HttpParserException if parsing failed.
     */
    public boolean notifyBeginParsing(MimeParser parser) 
	 throws HttpParserException, IOException
    {
	if ( major <= 0 ) 
	    return true;
	// Append the whole reply line in some buffer:
	HttpBuffer buf = new HttpBuffer();
	int        ch  = parser.read();
	int        len = 0;

	// Skip leading CRLF, a present to Netscape
	while((ch == '\r') || (ch =='\n'))
	    ch = parser.read();
    loop:
	while (true) {
	    switch(ch) {
	      case -1:
		  throw new HttpParserException("End Of File");
	      case '\r':
		if ((ch = parser.read()) != '\n')
		    parser.unread(ch);
		break loop;
	      case '\n':
		break loop;
	      default:
		buf.append(ch);
		len++;
	    }
	    // That's a guard against very poor HTTP
	    if ( len > 16*1024 )
		throw new HttpParserException("Invalid HTTP");
	    ch = parser.read();
	}
	// Parse the bufer into HTTP version and status code
	byte       line[] = buf.getByteCopy();
	ParseState ps     = new ParseState();
	ps.ioff      = 0;
	ps.bufend    = line.length;
	ps.separator = (byte) ' ';

	if ( HttpParser.nextItem(line, ps) < 0 ) {
	    // invalid, it should be an HTTP/0.9 reply...
	    // FIXME check this...
	    this.major = 0;
	    this.minor = 9;
	    this.status = 200;
	    return true;
//	    throw new RuntimeException("Bad reply: invalid status line ["
//				       + new String(line, 0, 0, line.length)
//				       + "]");
	}
	// Parse the reply version:
	if ((line.length >= 4) && line[4] == (byte) '/' ) {
	    // A present to broken NCSA servers around
	    ParseState item = new ParseState();
	    item.ioff   = ps.start+5;
	    item.bufend = ps.end;
	    this.major = (short) HttpParser.parseInt(line, item);
	    item.prepare();
	    item.ioff++;
	    this.minor = (short) HttpParser.parseInt(line, item);
	} else {
	    this.major = 1;
	    this.minor = 0;
	}
	// Parse the status code:
	ps.prepare();
	this.status = HttpParser.parseInt(line, ps);
	// The rest of the sentence if the reason phrase:
	ps.prepare();
	HttpParser.skipSpaces(line, ps);
	this.reason = new String(line, 0, ps.ioff, line.length-ps.ioff);
	return false;
    }

    /**
     * Get the standard HTTP reason phrase for the given status code.
     * @param status The given status code.
     * @return A String giving the standard reason phrase, or
     * <strong>null</strong> if the status doesn't match any knowned error.
     */

    public String getStandardReason(int status) {
	int category = status / 100;
	int catcode  = status % 100;
	switch(category) {
	  case 1:
	      if ((catcode >= 0) && (catcode < msg_100.length))
		  return HTTP.msg_100[catcode];
	      break;
	  case 2:
	      if ((catcode >= 0) && (catcode < msg_200.length))
		  return HTTP.msg_200[catcode];
	      break;
	  case 3:
	      if ((catcode >= 0) && (catcode < msg_300.length))
		  return HTTP.msg_300[catcode];
	      break;
	  case 4:
	      if ((catcode >= 0) && (catcode < msg_400.length))
		  return HTTP.msg_400[catcode];
	      break;
	  case 5:
	      if ((catcode >= 0) && (catcode < msg_500.length))
		  return HTTP.msg_500[catcode];
	      break;
	}
	return null;
    }

    /**
     * Get this reply status code.
     * @return An integer, giving the reply status code.
     */

    public int getStatus() {
	return status;
    }

    /**
     * Set this reply status code.
     * This will also set the reply reason, to the default HTTP/1.1 reason
     * phrase.
     * @param status The status code for this reply.
     */

    public void setStatus(int status) {
	if ((status != this.status) || (reason == null))
	    this.reason = getStandardReason(status);
	this.status = status;
    }

    /**
     * Get the reason phrase for this reply.
     * @return A String encoded reason phrase.
     */

    public String getReason() {
	return reason;
    }

    /**
     * Set the reason phrase of this reply.
     * @param reason The reason phrase for this reply.
     */

    public void setReason(String reason) {
	this.reason = reason;
    }

    /**
     * Get the <code>private</code> directive of the cache control header.
     * @return A list of fields (potentially empty) encoded as an array of
     * String (with 0 length if empty), or <strong>null</strong> if
     * undefined.
     */

    public String[] getPrivate() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? null : cc.getPrivate();
    }

    /**
     * Check the <code>public</code> directive of the cache control header.
     * @return A boolean <strong>true</strong> if set, <strong>false</strong>
     * otherwise.
     */

    public boolean checkPublic() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? false : cc.checkPublic();
    }

    /**
     * Set the <code>public</code> cache control directive.
     * @param onoff Set it on or off.
     */

    public void setPublic(boolean onoff) {
	HttpCacheControl cc = getCacheControl();
	if (onoff && (cc == null)) 
	    setCacheControl(cc = new HttpCacheControl(true));
	else
	    return;
	cc.setPublic(onoff);
    }

    /**
     * Check the <code>proxy-revalidate</code> directive of the cache control
     * header.
     * @return A boolean <strong>true</strong> if set, <strong>false</strong>
     * otherwise.
     */

    public boolean checkProxyRevalidate() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? false : cc.checkProxyRevalidate();
    }

    /**
     * Set the <code>proxy-revalidate</code> cache control directive.
     * @param onoff Set it on or off.
     */

    public void setProxyRevalidate(boolean onoff) {
	HttpCacheControl cc = getCacheControl();
	if (onoff && (cc == null)) 
	    setCacheControl(cc = new HttpCacheControl(true));
	else
	    return;
	cc.setProxyRevalidate(onoff);
    }

    /**
     * Check the <code>must-revalidate</code> directive of the cache control
     * header.
     * @return A boolean <strong>true</strong> if set, <strong>false</strong>
     * otherwise.
     */

    public boolean checkMustRevalidate() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? false : cc.checkMustRevalidate();
    }

    /**
     * Set the <code>must-revalidate</code> cache control directive.
     * @param onoff Set it on or off.
     */

    public void setMustRevalidate(boolean onoff) {
	HttpCacheControl cc = getCacheControl();
	if (onoff && (cc == null)) 
	    setCacheControl(cc = new HttpCacheControl(true));
	else
	    return;
	cc.setMustRevalidate(onoff);
    }

    /**
     * Get the list of accepted ranges.
     * @return The list of units in which range requests are accepted, encoded
     * as an array of String, or <strong>null</strong> if undefined.
     */

    public String[] getAcceptRanges() {
	HeaderValue value = getHeaderValue(H_ACCEPT_RANGES);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set the list of units in which range requests are accepted.
     * @param units The list of units, encoded as a String array, or
     * <strong>null</strong> to reset the value.
     */

    public void setAcceptRanges(String units[]) {
	setHeaderValue(H_ACCEPT_RANGES
		       , ((units == null) ? null : new HttpTokenList(units)));
    }

    /**
     * Get the age of the attached entity.
     * @return An integer giving the age as a number of seconds, or 
     * <strong>-1</strong> if undefined.
     */

    public int getAge() {
	HeaderValue value = getHeaderValue(H_AGE);
	try {
	    return (value != null) ? 
		((Integer) value.getValue()).intValue() :-1;
	} catch (HttpInvalidValueException hpx) {
	    if (hpx.getMessage().indexOf("overflow") != -1) {
		return Integer.MAX_VALUE;
	    }
	}
	return -1;
    }

    /**
     * Set the age of the attached entity.
     * @param age The age of the attached entity as a number of seconds,
     * or <strong>null</strong> to reset the value.
     */

    public void setAge(int age) {
	setHeaderValue(H_AGE
		       , ((age == -1) ? null : new HttpInteger(true, age)));
    }

    /**
     * Get the location of the reply.
     * The location header field keeps track of where to relocate clients
     * if needed.
     * @return The location encoded as a String.
     */

    public String getLocation() {
	HeaderValue value = getHeaderValue(H_LOCATION);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the location value of the reply.
     * @param location The location an URL instance, or <strong>null</strong>
     * to reset the value.
     */

    public void setLocation(URL location) {
	setHeaderValue(H_LOCATION
		       , ((location == null)
			  ? null
			  : new HttpString(true, location.toExternalForm())));
    }

     /**
      * Set the location value of the reply.
      * @param location The location a String, or <strong>null</strong> to 
      * reset the value.
      */

     public void setLocation(String location) {
	 setHeaderValue(H_LOCATION
			, ((location == null)
			   ? null
			   : new HttpString(true, location)));
     }

   
    /**
     * Get the proxy authentication challenge from this reply.
     * @return An instance of HttpChallenge, or <strong>null</strong> 
     * if undefined.
     */

    public HttpChallenge getProxyAuthenticate() {
	HeaderValue value = getHeaderValue(H_PROXY_AUTHENTICATE);
	return (value == null) ? null : (HttpChallenge) value.getValue();
    }

    /**
     * Set thye proxy authentication challenge on this reply.
     * @param challenge The challenge to set, or <strong>null</strong>
     * to reset the value.
     */

    public void setProxyAuthenticate(HttpChallenge challenge) {
	setHeaderValue(H_PROXY_AUTHENTICATE, challenge);
    }

    /**
     * Get the list of publicly allowed methods on queried resource.
     * @return The list of methods, encoded as a String array, or <strong>
     * null</strong> if undefined.
     */

    public String[] getPublic() {
	HeaderValue value = getHeaderValue(H_PUBLIC);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set the list of allowed method on queried resource.
     * @param mth The list of public methods, encoded as a String array, or
     * <strong>null</strong> to reset the value.
     */

    public void setPublic(String mth[]) {
	setHeaderValue(H_PUBLIC
		       , ((mth == null) 
			  ? null
			  : new HttpTokenList(mth)));
    }

    /**
     * Get the description of the server that generated this reply.
     * @return A String giving the description, or <strong>null</strong>
     * if undefined.
     */

    public String getServer() {
	HeaderValue value = getHeaderValue(H_SERVER);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the description of the server.
     * @param server The String decribing the server, or <strong>null</strong>
     * to reset the value.
     */

    public void setServer(String server) {
	setHeaderValue(H_SERVER
		       , ((server == null) 
			  ? null 
			  : new HttpString(true, server)));
    }

    /**
     * set the retry after as a delay
     * @param an integer representing the delay, in seconds
     */

    public void setRetryAfter(int delay) {
	setHeaderValue(H_RETRY_AFTER,
		       ((delay == -1) ? null :
			new HttpString(true, 
				       ((new Integer(delay)).toString()))));
    }

    /**
     * set the retry after as a date
     * @param long the date as a long
     */

    public void setRetryAfter(long date) {
	if (date == -1) {
	    setHeaderValue(H_RETRY_AFTER, null);
	} else {
	    HttpDate d = new HttpDate(true, date);
	    setHeaderValue(H_RETRY_AFTER,
			   new HttpString(true, d.toExternalForm()));
	}
    }

    /**
     * get the RetryAfter as a date
     * @return  A long giving the date as the number of milliseconds since the
     * Java epoch, or <strong>-1</strong> if undefined.
     */

    // FIXME Implementation

    /**
     * Get the vary header value.
     * @return A list of field-names on which the negotiated resource vary,
     * or a list containing only <code>*</code> (if varies on all headers),
     * or <strong>null</strong> if undefined.
     */

    public String[] getVary() {
	HeaderValue value = getHeaderValue(H_VARY);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set the vary header value.
     * @param varies The list of headers on which this resource varies, or
     * <strong>null</strong> to reset the value.
     */

    public void setVary(String varies[]) {
	setHeaderValue(H_VARY
		       , ((varies == null) 
			  ? null 
			  : new HttpCaseTokenList(varies)));
    }

    /**
     * Get the list of warnings attached to this reply.
     * @return An array of HttpWarning, or <strong>null</strong> if
     * undefined.
     */

    public HttpWarning[] getWarning() {
	HeaderValue value = getHeaderValue(H_WARNING);
	return (value != null) ? (HttpWarning[]) value.getValue() : null;
    }

    /**
     * Set the warning list attached to this reply.
     * @param warnings An array of warnings to attach to the given reply,
     * or <strong>null</strong> to reset the value.
     */

    public void setWarning(HttpWarning warnings[]) {
	setHeaderValue(H_WARNING
		       , ((warnings == null)
			  ? null
			  : new HttpWarningList(warnings)));
    }

    /**
     * Add a warning to this reply message.
     * @param warning The warning to add.
     */

    public void addWarning(HttpWarning warning) {
	HttpWarningList wl = (HttpWarningList) getHeaderValue(H_WARNING);
	if ( wl == null ) {
	    wl = new HttpWarningList(warning);
	    setHeaderValue(H_WARNING, wl);
	} else {
	    wl.addWarning(warning);
	}
    }

    /**
     * Get the challenge attached to this reply.
     * @return An instance of HttpChallenge, or <strong>null</strong> if
     * undefined.
     */

    public HttpChallenge getWWWAuthenticate() {
	HeaderValue value = getHeaderValue(H_WWW_AUTHENTICATE);
	return (value != null) ? (HttpChallenge) value.getValue() : null;
    }

    /**
     * Attach a challenge to this reply.
     * @param challenge The challenge to be attached to the reply, or
     * <strong>null</strong> to reset the value.
     */

    public void setWWWAuthenticate(HttpChallenge challenge) {
	setHeaderValue(H_WWW_AUTHENTICATE, challenge);
    }

    /**
     * Get the Authentication Info (see digest auth) attached to this reply.
     * @return An instance of HttpParamList, or <strong>null</strong> if
     * undefined.
     */

    public HttpParamList getAuthenticationInfo() {
	HeaderValue value = getHeaderValue(H_AUTHENTICATION_INFO);
	return (value != null) ? (HttpParamList) value.getValue() : null;
    }

    /**
     * Attach Authentication Info to this reply.
     * @param plist, the parameter list to be attached to the reply, or
     * <strong>null</strong> to reset the value.
     */

    public void setAuthenticationInfo(HttpParamList plist) {
	setHeaderValue(H_AUTHENTICATION_INFO, plist);
    }

    /**
     * Add the given parameter/value pair to the Authentication Info
     * header
     * @param String the name of the value
     * @param String the value
     */

    public void addAuthenticationInfo(String name, String value) {
	HttpParamList hpl = getAuthenticationInfo();
	if (hpl == null) {
	    setAuthenticationInfo(hpl = new HttpParamList(true));
	}
	hpl.setParameter(name, value);
    }
	

    public HttpReplyMessage(MimeParser parser) {
	super(parser);
    }

    public HttpReplyMessage() {
	super();
    }

}
