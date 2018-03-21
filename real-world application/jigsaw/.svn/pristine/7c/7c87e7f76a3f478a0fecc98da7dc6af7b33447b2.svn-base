// HttpRequestMessage.java
// $Id: HttpRequestMessage.java,v 1.1 2010/06/15 12:19:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;

public class HttpRequestMessage extends HttpEntityMessage {
    // HTTP Request message well-known headers
    public static int H_ACCEPT              = 31;
    public static int H_ACCEPT_CHARSET      = 32;
    public static int H_ACCEPT_ENCODING     = 33;
    public static int H_ACCEPT_LANGUAGE     = 34;
    public static int H_AUTHORIZATION       = 35;
    public static int H_EXPECT              = 36;
    public static int H_FROM                = 37;
    public static int H_HOST                = 38;
    public static int H_IF_MODIFIED_SINCE   = 39;
    public static int H_IF_MATCH            = 40;
    public static int H_IF_NONE_MATCH       = 41;
    public static int H_IF_RANGE            = 42;
    public static int H_IF_UNMODIFIED_SINCE = 43;
    public static int H_MAX_FORWARDS        = 44;
    public static int H_PROXY_AUTHORIZATION = 45;
    public static int H_RANGE               = 46;
    public static int H_REFERER             = 47;
    public static int H_TE                  = 48;
    public static int H_USER_AGENT          = 49;

    static {
      registerHeader("Accept"
		     , "org.w3c.www.http.HttpAcceptList"
		     , H_ACCEPT);
      registerHeader("Accept-Charset"
		     , "org.w3c.www.http.HttpAcceptCharsetList"
		     , H_ACCEPT_CHARSET);
      registerHeader("Accept-Encoding"
		     , "org.w3c.www.http.HttpAcceptEncodingList"
		     , H_ACCEPT_ENCODING);
      registerHeader("Accept-Language"
		     , "org.w3c.www.http.HttpAcceptLanguageList"
		     , H_ACCEPT_LANGUAGE);
      registerHeader("Authorization"
		     , "org.w3c.www.http.HttpCredential"
		     , H_AUTHORIZATION);
      registerHeader("From"
		     , "org.w3c.www.http.HttpString"
		     , H_FROM);
      registerHeader("Host"
		     , "org.w3c.www.http.HttpString"
		     , H_HOST);
      registerHeader("If-Modified-Since"
		     , "org.w3c.www.http.HttpDate"
		     , H_IF_MODIFIED_SINCE);
      registerHeader("If-Match"
		     , "org.w3c.www.http.HttpEntityTagList"
		     , H_IF_MATCH);
      registerHeader("If-None-Match"
		     , "org.w3c.www.http.HttpEntityTagList"
		     , H_IF_NONE_MATCH);
      registerHeader("If-Range"
		     , "org.w3c.www.http.HttpEntityTag"
		     , H_IF_RANGE);
      registerHeader("If-Unmodified-Since"
		     , "org.w3c.www.http.HttpDate"
		     , H_IF_UNMODIFIED_SINCE);
      registerHeader("Max-Forwards"
		     , "org.w3c.www.http.HttpInteger"
		     , H_MAX_FORWARDS);
      registerHeader("Proxy-Authorization"
		     , "org.w3c.www.http.HttpCredential"
		     , H_PROXY_AUTHORIZATION);
      registerHeader("Range"
		     , "org.w3c.www.http.HttpRangeList"
		     , H_RANGE);
      registerHeader("Referer"
		     , "org.w3c.www.http.HttpString"
		     , H_REFERER);
      registerHeader("User-Agent"
		     , "org.w3c.www.http.HttpString"
		     , H_USER_AGENT);
      registerHeader("Expect"
		     , "org.w3c.www.http.HttpString"
		     , H_EXPECT);
      registerHeader("TE"
		     ,  "org.w3c.www.http.HttpAcceptEncodingList"
		     , H_TE);
    }

    /**
     * The method to execute on the target resource.
     */
    protected String method = "GET".intern();
    /**
     * The target resource, identified by its URL.
     */
    protected URL    url    = null;
    /**
     * The proxy to use for that request, if any.
     */
    protected URL proxy = null;

    protected String sProxy = null;

    /**
     * This message is about to be emited, emit the request-line first !
     * @param out The output stream to emit the request to.
     * @exception IOException If some IO error occured while emiting the
     * request.
     */

    protected void startEmit(OutputStream out, int what) 
	 throws IOException
    {
	if ((what & EMIT_HEADERS) != EMIT_HEADERS)
	    return ;
	// I am not sure (at all) whether this belongs here or in some subclass
	if ((major >= 1) && ! hasHeader(H_HOST)) {
	    String h = 
		((((url.getPort()==80) && 
		   url.getProtocol().equalsIgnoreCase("http")) || 
		  ((url.getPort()==443) && 
		   url.getProtocol().equalsIgnoreCase("https"))||
		  (url.getPort() == -1))
		 ? url.getHost()
		 : url.getHost() + ":" + url.getPort());
	    setHeaderValue(H_HOST, HttpFactory.makeString(h));
	}
	// Emit the request line:
	HttpBuffer buf = new HttpBuffer();
	buf.append(method);
	buf.append(' ');
	if ( proxy != null ) {
	    buf.append(url.toExternalForm());
	} else {
	    String sUrl = url.getFile();
	    // as to jdk1.4 getFile can be "" in that case
	    // we can use it when method = OPTIONS
	    if (sUrl.length() == 0) {
		if (method == HTTP.OPTIONS) {
		    buf.append('*');
		} else {
		    buf.append('/');
		}
	    } else {
		buf.append(sUrl);
	    }
	}
	buf.append(' ');
	buf.append(getVersion());
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
     * @return A boolean <strong>true</strong> if the MIME parser should stop
     * parsing, <strong>false</strong> otherwise.
     * @exception IOException If some IO error occured while reading the
     * stream.
     * @exception HttpParserException if parsing failed.
     */
    public boolean notifyBeginParsing(MimeParser parser)
	throws HttpParserException, IOException
    {
	// Append the whole reply line in some buffer:
	HttpBuffer buf = new HttpBuffer();
	int        ch  = parser.read();
	// A present for Netscape !
	while((ch == '\r') || (ch == '\n')) {
	    ch = parser.read();
	}
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
	    }
	    ch = parser.read();
	}
	// Parse the bufer into HTTP version and status code
	byte       line[] = buf.getByteCopy();
	ParseState ps     = new ParseState();
	ps.ioff      = 0;
	ps.bufend    = line.length;
	ps.separator = (byte) ' ';
	// Get the method name:
	if ( HttpParser.nextItem(line, ps) < 0 ) {
	    throw new RuntimeException("Bad request, no method !");
	}
	setMethod(ps.toString(line));
	// Get the URL path, or full URL
	if ( HttpParser.nextItem(line, ps) < 0 ) {
	    throw new RuntimeException("Bad request, no URL !");
	}
	setTarget(ps.toString(line));
	// Get the version numbers:
	HttpParser.skipSpaces(line, ps);
	if ( ps.ioff + 5 < ps.bufend ) {
	    ps.ioff += 5;
	    ps.separator = (byte) '.';
	    this.major = (short) HttpParser.parseInt(line, ps);
	    ps.prepare();
	    this.minor = (short) HttpParser.parseInt(line, ps);
	    return false;
	} else {
	    this.major = 0;
	    this.minor = 9;
	    return true;
	}
    }

    /**
     * All the headers have been parsed, take any appropriate actions.
     * Here we will verify that the request is HTTP/1.1 compliant
     * for the Host header.
     * @param parser The Mime parser.
     * @exception MimeParserException if the parsing failed
     * @exception IOException if an IO error occurs.
     */
    public void notifyEndParsing(MimeParser parser)
	 throws HttpParserException, IOException
    {
	if (major == 1 && minor == 1) {
	    if (getHost() == null) {
		throw new HttpParserException("missing Host header", this);
	    }
	}
    }

    // FIXME - I really mean FIXME

    String target = null;
    protected void setTarget(String target) {
	this.target = target;
    } 

    protected String getTarget() {
	return target;
    }

    /**
     * Get this request's method.
     * @return The request method, as a String.
     */

    public String getMethod() {
	return method;
    }

    /**
     * Set this request's method.
     * @param mth The request method.
     */

    public void setMethod(String method) {
	this.method = method.intern();
    }

    /**
     * Get this request's target URI.
     * This will only return the absolute path of the requested resource, even
     * if the actual request came with the full path as an URI.
     * @return An URL instance, or <strong>null</strong> if undefined.
     */

    public URL getURL() {
	return url;
    }

    /**
     * Set this request URI.
     * The provided URI should only include the absolute path of the target
     * request, see the <code>setHost</code> method for how to set the actual
     * host of the target resource.
     * @param url The target URL of the request, as an URL instance.
     */

    public void setURL(URL url) {
	this.url = url;
    }

    /**
     * Get the <code>min-fresh</code> directive value of the cache control
     * header.
     * @return The min-fresh value, as a number of seconds, or <strong>-1
     * </strong> if undefined.
     */

    public int getMinFresh() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? -1 : cc.getMinFresh();
    }

    /**
     * Set the <code>min-fresh</code> directive value of the cache control
     * header.
     * @param minfresh The min-fresh value, in seconds, or <strong>-1</strong>
     * to reset value.
     */

    public void setMinFresh(int minfresh) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    if ( minfresh == -1 ) {
		return;
	    }
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setMinFresh(minfresh);
    }

    /**
     * Get the <code>max-stale</code> directive value of the cache control
     * header.
     * @return The max-stale value, as a number of seconds, or <strong>-1
     * </strong> if undefined.
     */

    public int getMaxStale() {
	HttpCacheControl cc = getCacheControl();
	return (cc == null) ? -1 : cc.getMaxStale();
    }

    /**
     * Set the <code>max-stale</code> directive value.
     * @param maxstale A number of seconds giving the allowed drift for
     * a resource that is no more valid, or <strong>-1</strong> to reset
     * the value.
     */

    public void setMaxStale(int maxstale) {
	HttpCacheControl cc = getCacheControl();
	if ( cc == null ) {
	    if ( maxstale == -1 ) {
		return;
	    }
	    setCacheControl(cc = new HttpCacheControl(true));
	}
	cc.setMaxStale(maxstale);
    }

    // FIXME more cache control accessors

    /**
     * Get this request accept list.
     * @return A list of Accept clauses encoded as an array of HttpAccept
     * instances, or <strong>null</strong> if undefined.
     */

    public HttpAccept[] getAccept() {
	HeaderValue value = getHeaderValue(H_ACCEPT);
	return (value != null) ? (HttpAccept[]) value.getValue() : null;
    }

    /**
     * Set the list of accept clauses attached to this request.
     * @param accepts The list of accept clauses encoded as an array
     * of HttpAccept instances, or <strong>null</strong> to reset the value.
     */

    public void setAccept(HttpAccept accepts[]) {
	setHeaderValue(H_ACCEPT
		       , ((accepts == null)
			  ? null
			  : new HttpAcceptList(accepts)));
    }

    /**
     * Get the list of accepted charsets for this request.
     * @return The list of accepted languages encoded as an array of
     * instances of HttpAcceptCharset, or <strong>null</strong> if undefined.
     */

    public HttpAcceptCharset[] getAcceptCharset() {
	HeaderValue value = getHeaderValue(H_ACCEPT_CHARSET);
	return (value != null) ? (HttpAcceptCharset[]) value.getValue():null;
    }

    /**
     * Set the list of accepted charsets for this request.
     * @param charsets The list of accepted charsets, encoded as an array
     * of HttpAcceptCharset instances, or <strong>null</strong> to reset
     * the value.
     */

    public void setAcceptCharset(HttpAcceptCharset charsets[]) {
	setHeaderValue(H_ACCEPT_CHARSET
		       , ((charsets == null)
			  ? null
			  : new HttpAcceptCharsetList(charsets)));
    }

    /**
     * Get the list of accepted encodings.
     * @return A list of token describing the accepted encodings, or <strong>
     * null</strong> if undefined.
     */

    public HttpAcceptEncoding[] getAcceptEncoding() {
	HeaderValue value = getHeaderValue(H_ACCEPT_ENCODING);
	return (value != null) ? (HttpAcceptEncoding[]) value.getValue():null;
    }

    /**
     * Set the list of accepted encodings.
     * @param encodings The list of accepted encodings, as an array,
     * of HttpAcceptEncoding or <strong>null</strong> to reset the value.
     */

    public void setAcceptEncoding(HttpAcceptEncoding encoding[]) {
	setHeaderValue(H_ACCEPT_ENCODING
		       , ((encoding == null)
			  ? null
			  : new HttpAcceptEncodingList(encoding)));
    }

    /**
     * Get the list of accepted languages for this request.
     * @return The list of accepted languages encoded as an array of
     * instances of HttpAcceptLanguage, or <strong>null</strong> if
     * undefined.
     */

    public HttpAcceptLanguage[] getAcceptLanguage() {
	HeaderValue value = getHeaderValue(H_ACCEPT_LANGUAGE);
	return ((value != null) 
		? (HttpAcceptLanguage[]) value.getValue()
		: null);
    }

    /**
     * Set the list of accepted languages for this request.
     * @param langs The list of accepted languages, encoded as an array
     * of HttpAcceptLanguage instances, or <strong>null</strong> to reset
     * value.
     */

    public void setAcceptLanguage(HttpAcceptCharset langs[]) {
	setHeaderValue(H_ACCEPT_LANGUAGE
		       , ((langs == null) 
			  ? null 
			  : new HttpAcceptCharsetList(langs)));
    }

    /**
     * Get the authorization associated with this request.
     * @return An instance of HttpCredential of <strong>null</strong>
     * if undefined.
     */

    public HttpCredential getAuthorization() {
	HeaderValue value = getHeaderValue(H_AUTHORIZATION);
	return (value != null) ? (HttpCredential) value.getValue() : null;
    }

    /**
     * Set the authorization associated with this request.
     * @param credentials The credentials to attach to this request, or
     * <strong>null</strong> to reset the value.
     */

    public void setAuthorization(HttpCredential credentials) {
	setHeaderValue(H_AUTHORIZATION, credentials);
    }

    /**
     * Does this request has some specific authorization infos.
     * @return A boolean.
     */

    public boolean hasAuthorization() {
	return hasHeader(H_AUTHORIZATION);
    }

    /**
     * Get the originator (from header value) of the request.
     * @return The originator description, as a String, or <strong>null
     * </strong> if undefined.
     */

    public String getFrom() {
	HeaderValue value = getHeaderValue(H_FROM);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the originator of this request.
     * @param from The description of the originator, as an email address,
     * or <strong>null</strong> to reset the value.
     */

    public void setFrom(String from) {
	setHeaderValue(H_FROM
		       , ((from == null) 
			  ? null 
			  : new HttpString(true, from)));
    }

    /**
     * Get the host header.
     * @return The host header, encoded as a String, or <strong>null</strong>
     * if undefined.
     */

    public String getHost() {
	HeaderValue value = getHeaderValue(H_HOST);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the host header value.
     * @param host The String representing the target host of the request,
     * or <strong>null</strong> to reset the value.
     */

    public void setHost(String host) {
	setHeaderValue(H_HOST
		       , ((host == null) ? null : new HttpString(true, host)));
    }

    /**
     * Get the if-modified-since conditional.
     * @return A long, giving the If-Modified-Since date value as the number
     * of milliseconds since Java epoch, or <strong>-1</strong> if undefined.
     */

    public long getIfModifiedSince() {
	HeaderValue value = getHeaderValue(H_IF_MODIFIED_SINCE);
	return (value != null) ? ((Long) value.getValue()).longValue() : -1;
    }

    /**
     * Set the if-modified-since conditional.
     * @param ims The date of last modification, as the number of milliseconds
     * since Java epoch, or <strong>-1</strong> to reset the value.
     */

    public void setIfModifiedSince(long ims) {
	setHeaderValue(H_IF_MODIFIED_SINCE
		       , ((ims == -1) ? null : new HttpDate(true, ims)));
    }

    /**
     * Get the conditional matching set of entity tags.
     * @return An array of HttpEntityTag instances, or <strong>null</strong>
     * if undefined.
     */

    public HttpEntityTag[] getIfMatch() {
	HeaderValue value = getHeaderValue(H_IF_MATCH);
	return (value != null) ? (HttpEntityTag[]) value.getValue() : null;
    }

    /**
     * Set the conditional matching set of entity tags.
     * @param etags An array of HttpEntityTag, one per item in the set, or
     * <strong>null</strong> to reset the header value.
     */

    public void setIfMatch(HttpEntityTag etags[]) {
	setHeaderValue(H_IF_MATCH
		       , ((etags != null)
			  ? new HttpEntityTagList(etags)
			  : null));
    }

    /**
     * Get the conditional none matching entity tags.
     * @return An entity tag list, encoded as an array of HttpEntityTag, or
     * <strong>null</strong> if undefined.
     */

    public HttpEntityTag[] getIfNoneMatch() {
	HeaderValue value = getHeaderValue(H_IF_NONE_MATCH);
	return (value != null) ? (HttpEntityTag[]) value.getValue() : null;
    }

    /**
     * Set the conditional none matching entity tags.
     * @param etags An array of HttpEntityTag, one per item in the set,
     * or <strong>null</strong> to reset the value.
     */

    public void setIfNoneMatch(HttpEntityTag etags[]) {
	setHeaderValue(H_IF_NONE_MATCH
		       , ((etags == null)
			  ? null
			  : new HttpEntityTagList(etags)));
    }

    /**
     * Get the if-range conditional if any.
     * Warning: This API doesn't accept <code>If-Range</code> header that
     * contains date value (if you want to discuss why, send me email)
     * @return An HttpEntityTag instance, or <strong>null</strong> if 
     * that header is not defined.
     */

    public HttpEntityTag getIfRange() {
	HeaderValue value = getHeaderValue(H_IF_RANGE);
	return ((value != null) ? (HttpEntityTag) value : null);
    }

    /**
     * Set the if-range header value.
     * @param etag The contional etag, or <strong>null</strong> to reset
     * previous setting.
     */

    public void setIfRange(HttpEntityTag etag) {
	setHeaderValue(H_IF_RANGE, etag);
    }

    /**
     * Get the if unmodified since conditional date.
     * @return The date encoded as a long number of milliseconds since
     * Java runtime epoch, or <strong>-1</strong> if undefined.
     */

    public long getIfUnmodifiedSince() {
	HeaderValue value = getHeaderValue(H_IF_UNMODIFIED_SINCE);
	return (value != null) ? ((Long) value.getValue()).longValue() : -1;
    }

    /**
     * Set the if-unmodified-since conditional date.
     * @param date The date, encoded as the number of milliseconds since
     * Java epoch, or <strong>-1</strong> to reset value.
     */

    public void setIfUnmodifiedSince(long date) {
	if ( date == -1L ) {
	    setHeaderValue(H_IF_UNMODIFIED_SINCE, null);
	} else {
	    setHeaderValue(H_IF_UNMODIFIED_SINCE, new HttpDate(true, date));
	}
    }

    /**
     * Get the maximum allowed count of hops for the request.
     * @return An integer giving the number of hops, or <strong>-1</strong>
     * if undefined.
     */

    public int getMaxForwards() {
	HeaderValue value = getHeaderValue(H_MAX_FORWARDS);
	return (value != null) ? ((Integer) value.getValue()).intValue() : -1;
    }

    /**
     * Set the maximum allowed count of hops for that request.
     * @param hops The hops count, or <strong>-1</strong> to reset value.
     */

    public void setMaxForwards(int hops) {
	if ( hops == -1 ) {
	    setHeaderValue(H_MAX_FORWARDS, null);
	} else {
	    setHeaderValue(H_MAX_FORWARDS, new HttpInteger(true, hops));
	}
    }

    /**
     * Set the proxy authorization associated with that request.
     * @param credentials The credentials, or <strong>null</strong> to
     * reset the value.
     */

    public void setProxyAuthorization(HttpCredential credentials) {
	setHeaderValue(H_PROXY_AUTHORIZATION, credentials);
    }

    /**
     * Get the authorization associated with this request.
     * @return An instance of HttpCredential of <strong>null</strong>
     * if undefined.
     */

    public HttpCredential getProxyAuthorization() {
	HeaderValue value = getHeaderValue(H_PROXY_AUTHORIZATION);
	return (value != null) ? (HttpCredential) value.getValue() : null;
    }

    /**
     * Get the ranges queried by this request.
     * @return A list of ranges, encoded as an array of HttpRange instance
     * or <strong>null</strong> if undefined.
     */

    public HttpRange[] getRange() {
	HeaderValue value = getHeaderValue(H_RANGE);
	return (value != null) ? (HttpRange[]) value.getValue() : null;
    }

    /**
     * Set the ranges queried by this request.
     * @param ranges The list of ranges, encoded as an array of instances
     * of HttpRange, or <strong>null</strong> to reset the value.
     */

    public void setRange(HttpRange ranges[]) {
	setHeaderValue(H_RANGE
		       , ((ranges == null)
			  ? null
			  : new HttpRangeList(ranges)));
    }

    /**
     * Get the referer of the request.
     * @return A String encoding the referer (generally an URL), or
     * <strong>null</strong> if undefined.
     */

    public String getReferer() {
	HeaderValue value = getHeaderValue(H_REFERER);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the referer of this request.
     * @param referer The referer of the request, or <strong>null</strong>
     * to reset the value.
     */

    public void setReferer(String referer) {
	setHeaderValue(H_REFERER
		       , ((referer == null)
			  ? null
			  : new HttpString(true, referer)));
    }

    /**
     * Get the user agent String.
     * @return The user agent description, as a String, or <strong>null
     * </strong> if undefined.
     */

    public String getUserAgent() {
	HeaderValue value = getHeaderValue(H_USER_AGENT);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the user agent description header.
     * @param ua The description of the user agent emiting the request, or
     * <strong>null</strong> to reset the value.
     */

    public void setUserAgent(String ua) {
	setHeaderValue(H_USER_AGENT
		       , ((ua == null) ? null : new HttpString(true, ua)));
    }

    /**
     * Get this request's Expect header value.
     * @return This header as a String.
     */

    public String getExpect() {
	HeaderValue value = getHeaderValue(H_EXPECT);
	return (value != null) ? (String) value.getValue() : null;
	
    }

    /**
     * Set this request's Expect header.
     * @param exp The value of the header (ex: "100-continue".
     */

    public void setExpect(String expect) {
	setHeaderValue(H_EXPECT
		       , ((expect == null) 
			  ? null 
			  : new HttpString(true, expect)));
    }

    /**
     * Get the list of restricted transfer encodings.
     * @return A list of token describing the restreicted TE, or <strong>
     * null</strong> if undefined.
     */

    public HttpAcceptEncoding[] getTE() {
	HeaderValue value = getHeaderValue(H_TE);
	return (value != null) ? (HttpAcceptEncoding[]) value.getValue():null;
    }

    /**
     * Set the list of restricted transfer encodings
     * @param encodings The list of accepted encodings, as an array,
     * of HttpAcceptEncoding or <strong>null</strong> to reset the value.
     */

    public void setTE(HttpAcceptEncoding encoding[]) {
	setHeaderValue(H_TE
		       , ((encoding == null)
			  ? null
			  : new HttpAcceptEncodingList(encoding)));
    }

    /**
     * Set the proxy to use for that request.
     * @param proxy The proxy's URL, or <strong>null</strong> to reset value.
     */

    public void setProxy(URL proxy) {
	this.proxy  = proxy;
	this.sProxy = (proxy == null) ? null : proxy.toExternalForm();
    }

    /**
     * Get the proxy to use for that request.
     * @return The proxy's URL, or <strong>null</strong> if none is set.
     */

    public URL getProxy() {
	return proxy;
    }

    /**
     * Will this request use a proxy when executed ?
     * @return A boolean.
     */

    public boolean hasProxy() {
	return proxy != null;
    }

    public HttpRequestMessage(MimeParser parser) {
	super(parser);
    }

    public HttpRequestMessage() {
	super();
    }

    public static void main(String args[]) {
	try {
	    HttpRequestMessage r = new HttpRequestMessage();
	    // Set general request headers:
	    r.setURL(new URL("http://www.w3.org/"));
	    r.setHost("http://www.w3.org");
	    r.setFrom("abaird@w3.org");
	    r.setReferer("http://abaird.w3.org/");
	    // Set the cache control directive:
	    HttpCacheControl c = new HttpCacheControl(true);
	    c.setMaxAge(10);
	    c.setNoStore(true);
	    r.setCacheControl(c);
	    // Emit the request:
	    r.emit(System.out);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

}
