// Request.java
// $Id: Request.java,v 1.1 2010/06/15 12:21:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import org.w3c.www.mime.MimeParser;

import org.w3c.www.http.ChunkedInputStream;
import org.w3c.www.http.ContentLengthInputStream;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpParserException;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.tools.codec.Base64Encoder;

/**
 * this class extends HttpRequestMessage to cope with HTTP request.
 * One subtely here: note how each field acessor <em>never</em> throws an
 * exception, but rather is provided with a default value: this is in the hope
 * that sometime, HTTP will not require all the parsing it requires right now.
 */

public class Request extends HttpRequestMessage 
    implements RequestInterface
{
    /**
     * The URL that means <strong>*</strong> for an <code>OPTIONS</code>
     * method.
     */
    public static URL THE_SERVER = null;

    /**
     * the state of original URL
     */
    public static final String ORIG_URL_STATE = 
                                         "org.w3c.jigsaw.http.Request.origurl";

    static {
	try {
	    THE_SERVER = new URL("http://your.url.unknown");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    protected Client      client  = null;
    protected MimeParser  parser  = null;
    protected InputStream in      = null;
    protected boolean     keepcon = true;
    boolean is_proxy = false;

    public void setState(String name, String state) {
	super.setState(name, state);
    }

    /**
     * Fix the target URL of the request, this is the only good time to do so.
     * @param parser The MimeParser
     * @exception HttpParserException if parsing failed.
     * @exception IOException if an IO error occurs.
     */

    public void notifyEndParsing(MimeParser parser)
	throws HttpParserException, IOException
	{
	    super.notifyEndParsing(parser);
	    String target = getTarget();
	    String url    = null;
	    // Get rid of the nasty cases(there is a place in hell for someone)
	    if ( target.equals("*") ) {
		setURL(THE_SERVER);
		return;
	    }
	    // Is this a full http URL:
	    int    at    = -1;
	    int    colon = target.indexOf(':');
	    String proto = (colon != -1) ? target.substring(0, colon) : null;
	    if ((proto != null) &&
		(proto.equals("http") || proto.equals("ftp"))) {
		// Good we have a full URL:
		try {
		    // hugly hack, bug in URL for urls like:
		    // http://user:passwd@host:port/file
		    if ((at = target.indexOf('@',6)) != -1) {
			String auth = target.substring(colon+3,at);
			int    sep  = -1;
			if ((auth.indexOf('/') == -1) &&
 			    ((sep = auth.indexOf(':')) != -1)) {
			    if (! hasAuthorization()) {
				String username = auth.substring(0,sep);
				String password = auth.substring(sep+1);
				HttpCredential credential =
				    HttpFactory.makeCredential("Basic");
				Base64Encoder encoder = 
				    new Base64Encoder(username+":"+password);
				credential.setAuthParameter("cookie", 
 						     encoder.processString());
				setAuthorization(credential);
			    }
			    setURL(new URL(proto+
					   "://"+target.substring(at+1)));
			} else {
			    setURL(new URL(target));
			}
		    } else {
			setURL(new URL(target));
		    }
		} catch (Exception ex) {
		    throw new HttpParserException("Bogus URL ["+url+"]", this);
		}
	    } else {
		try {
		    // Do we have a valid host header ?
		    String host = getHost();
		    if ( host == null ) {
			// If this claims to be 1.1, tell him he's wrong:
			if ((major == 1) && (minor >= 1))
			    throw new HttpParserException("No Host Header");
			httpd server = getClient().getServer();
			setURL(new URL("http"
				       , server.getHost(), server.getPort()
				       , target));
		    } else {
			int ic = host.indexOf(':');
			if ( ic < 0 ) {
			    setURL(new URL("http", host, target));
			} else {
			    setURL(new URL("http"
					   , host.substring(0, ic)
					   , Integer.parseInt(
					       host.substring(ic+1))
					   , target));
			}
		    }
		} catch (Exception ex) {
		    throw new HttpParserException("Bogus URL ["+url+"]", this);
		}
	    }
	}

    // FIXME
    // This guy should also check that the (optional) request stream has been
    // exhausted.

    public boolean canKeepConnection() {
	// HTTP/0.9 doesn't know about keeping connections alive:
	if (( ! keepcon) || (major < 1))
	    return false;
	if ( minor >= 1 ) 
	    // HTTP/1.1 keeps connections alive by default
	    return hasConnection("close") ? false : true;
	// For HTTP/1.0 check the [proxy] connection header:
	if ( is_proxy )
	    return hasProxyConnection("keep-alive");
	else
	    return hasConnection("keep-alive");
    }

    private ResourceReference target_resource = null;
    protected void setTargetResource(ResourceReference resource) {
	target_resource = resource;
    }

    /**
     * Get this request target resource.
     * @return An instance of HTTPResource, or <strong>null</strong> if
     * not found.
     */

    public ResourceReference getTargetResource() {
	return target_resource;
    }

    public void setProxy(boolean onoff) {
	is_proxy = onoff;
    }

    public boolean isProxy() {
	return is_proxy;
    }

    public String getURLPath() {
	return url.getFile();
    }

    public void setURLPath(String path) {
	try {
	    url = new URL(url, path);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public boolean hasContentLength() {
	return hasHeader(H_CONTENT_LENGTH);
    }

    public boolean hasContentType() {
	return hasHeader(H_CONTENT_TYPE);
    }

    public boolean hasAccept() {
	return hasHeader(H_ACCEPT);
    }

    public boolean hasAcceptCharset() {
	return hasHeader(H_ACCEPT_CHARSET);
    }

    public boolean hasAcceptEncoding() {
	return hasHeader(H_ACCEPT_ENCODING);
    }

    public boolean hasAcceptLanguage() {
	return hasHeader(H_ACCEPT_LANGUAGE);
    }

    public boolean hasAuthorization() {
	return hasHeader(H_AUTHORIZATION);
    }

    public boolean hasProxyAuthorization() {
	return hasHeader(H_PROXY_AUTHORIZATION);
    }

    public String getQueryString() {
	return (String) getState("query");
    }

    public boolean hasQueryString() {
	return hasState("query");
    }

    protected boolean internal = false;
    public boolean isInternal() {
	return internal;
    }

    public void setInternal(boolean onoff) {
	this.internal = onoff;
    }

    protected Request original = null;
    public Request getOriginal() {
	return original == null ? this : original ;
    }

    protected ResourceFilter filters[] = null;
    protected int        infilters = -1;
    protected void setFilters(ResourceFilter filters[], int infilters) {
	this.filters   = filters;
	this.infilters = infilters;
    }

    /**
     * Clone this request, in order to launch an internal request.
     * This method can be used to run a request in some given context, defined
     * by an original request. It will preserve all the original information
     * (such as authentication, etc), and will provide a <em>clone</em> of
     * the original request.
     * <p>The original request and its clone differ in the following way:
     * <ul>
     * <li>The clone is marked as <em>internal</em>, which can be tested
     * by the <code>isInternal</code> method.
     * <li>The clone will keep a pointer to the first request that was 
     * cloned. This original request can be accessed by the <code>getOriginal
     * </code> method.
     * </ul>
     * <p>To run an internal request, the caller can then use the <code>
     * org.w3c.jigsaw.http.httpd</code> <code>perform</code> method.
     * @return A fresh Request instance, marked as internal.
     */

    public HttpMessage getClone() {
	Request cl = (Request) super.getClone();
	cl.internal = true;
	if ( cl.original == null )
	    cl.original = this;
	return cl;
    }

    /**
     * Get this reply entity body.
     * The reply entity body is returned as an InputStream, that the caller
     * has to read to actually get the bytes of the content.
     * @return An InputStream instance. If the reply has no body, the returned
     * input stream will just return <strong>-1</strong> on first read.
     */

    public InputStream getInputStream()
	throws IOException
	{
	    if ( in != null )
		return in;
	    // Find out which method is used to the length:
	    // first, chunked
	    String te[] = getTransferEncoding() ;
	    if ( te != null ) {
		for (int i = 0 ; i < te.length ; i++) {
		    if (te[i].equals("chunked")) 
			in = new ChunkedInputStream(
			    parser.getInputStream());
		}
	    }
	    // if not, content-length
	    int len = getContentLength();
	    if ( (in == null) && (len >= 0) ) {
		in = new ContentLengthInputStream(parser.getInputStream(),len);
	    }
	    // Handle broken HTTP/1.0 request
	    // It is mandatory for 1.1 requests to have been handled above.
	    if ((major == 1) && (minor == 0) && (in == null)) {
		String m = getMethod();
		if (m.equals("POST") || m.equals("PUT")) {
		    keepcon = false;
		    in      = parser.getInputStream();
		}
	    }
	    return in;
	}

    /**
     * Unescape a HTTP escaped string
     * @param s The string to be unescaped
     * @return the unescaped string.
     */

    public static String unescape (String s) {
	StringBuffer sbuf = new StringBuffer () ;
	int l  = s.length() ;
	int ch = -1 ;
	for (int i = 0 ; i < l ; i++) {
	    switch (ch = s.charAt(i)) {
	    case '%':
		ch = s.charAt (++i) ;
		int hb = (Character.isDigit ((char) ch) 
			  ? ch - '0'
			  : 10+Character.toLowerCase ((char) ch)-'a') & 0xF ;
		ch = s.charAt (++i) ;
		int lb = (Character.isDigit ((char) ch)
			  ? ch - '0'
			  : 10+Character.toLowerCase ((char) ch)-'a') & 0xF ;
		sbuf.append ((char) ((hb << 4) | lb)) ;
		break ;
	    case '+':
		sbuf.append (' ') ;
		break ;
	    default:
		sbuf.append ((char) ch) ;
	    }
	}
	return sbuf.toString() ;
    }

    public ReplyInterface makeBadRequestReply() {
	return makeReply(HTTP.BAD_REQUEST);
    }

    /**
     * Make an empty Reply object matching this request version.
     * @param status The status of the reply.
     */

    public Reply makeReply(int status) {
	Reply reply = new Reply(client
				, this
				, getMajorVersion()
				, getMinorVersion()
				, status);
	if ((filters != null) && (infilters > 0))
	    reply.setFilters(filters, infilters);
	return reply;
    }

    /**
     * skip the body
     */
    public void skipBody() {
	// don't skip when there is a 100-Continue
	if (getExpect() != null)
	    return;
	try {
	    InputStream is = getInputStream();
	    int avail = is.available();
	    
	    while (avail > 0) {
		is.skip(avail);
		avail = is.available();
	    }
	} catch (Exception ex) {// nothing to skip 
	}
    }

    /**
     * Get the client of this request.
     */

    public Client getClient() {
	return client ;
    }

    public Request (Client client, MimeParser parser) {
	super (parser);
	this.parser = parser;
	this.client = client ;
    }


    /**
     * Set this reply entity body.
	 * @param is the InputStream instance. 
	 * USE CAREFULLY : need to be thread-safe
     */
    public void setStream(InputStream is){
	if (is != null)
	    this.in = is ;
    }
}
