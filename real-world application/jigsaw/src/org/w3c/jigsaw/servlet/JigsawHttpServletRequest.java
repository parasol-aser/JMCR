// JigsawHttpServletRequest.java
// $Id: JigsawHttpServletRequest.java,v 1.1 2010/06/15 12:24:13 smhuang Exp $
// (c) COPYRIGHT MIT, ERCIM and Keio, 1996-2004.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import java.security.Principal;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.w3c.util.ArrayEnumeration;
import org.w3c.util.EmptyEnumeration;
import org.w3c.util.ObservableProperties;

import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;
import org.w3c.jigsaw.resources.VirtualHostResource;

import org.w3c.www.http.ContentLengthInputStream;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpAcceptLanguage;
import org.w3c.www.http.HttpCookie;
import org.w3c.www.http.HttpCookieList;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.www.mime.MimeType;

import org.w3c.jigsaw.auth.AuthFilter; // for auth infos access

import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

class HeaderNames implements Enumeration {
    // The HeaderDescription enumeration
    Enumeration e = null;

    public boolean hasMoreElements() {
	return e.hasMoreElements();
    }

    public Object nextElement() {
	HeaderDescription d = (HeaderDescription) e.nextElement();
	return d.getName();
    }

    HeaderNames(Enumeration e) {
	this.e = e ;
    }

}

/**
 *  @author Alexandre Rafalovitch <alex@access.com.au>
 *  @author Anselm Baird-Smith <abaird@w3.org>
 *  @author Benoit Mahe <bmahe@sophia.inria.fr>
 *  @author Yves Lafon <ylafon@w3.org>
 */

public class JigsawHttpServletRequest implements HttpServletRequest {

    // for null encoding
    private static final String nullEnc = "null".intern();
    /**
     * The InputStream state codes.
     */

    /**
     * The initial state of the request InputStream 
     */
    private final static int STREAM_STATE_INITIAL = 0;

    /**
     * One reader has been created and is probably used.
     */
    private final static int STREAM_READER_USED = 1;

    /**
     * The input stream is used
     */
    private final static int INPUT_STREAM_USED = 2;

    /**
     * The inputstream state
     */
    private int stream_state = STREAM_STATE_INITIAL;

    public final static 
	String STATE_PARAMETERS = "org.w3c.jigsaw.servlet.stateParam";

    private static MimeType type = MimeType.APPLICATION_X_WWW_FORM_URLENCODED ;
    /** 
     * The initial request.
     */
    private Request request = null;
    /**
     * The attached servlet.
     */
    private Servlet servlet = null;
    /**
     * The attached servlet context.
     */
    private JigsawServletContext servletContext = null;
    /**
     * The lazyly computed queryParameters hashtable.
     */
    private Hashtable queryParameters = null;
    protected JigsawHttpServletResponse response = null;

    protected JigsawHttpSession httpSession = null;

    protected JigsawHttpSessionContext sessionContext = null;

    protected String requestedSessionID = null;

    protected String encoding = null;

    private Hashtable convertParameters(Hashtable source) {
	if (source != null) {
	    Enumeration e = source.keys();
	    while (e.hasMoreElements()) {
		Object name = e.nextElement();
		Object value = source.get(name);
		if (value instanceof String) {
		    String _newval[] = new String[1];
		    _newval[0] = (String) value;
		    source.put(name, _newval);
		}
	    }
	    return source;
	}
	return null;
    }

    private Hashtable mergeParameters(Hashtable source, Hashtable dest) {
	if (source == null)
	    return dest;
	if (dest != null) {
	    Enumeration e = source.keys();
	    while (e.hasMoreElements()) {
		String name = (String)e.nextElement();
		Object value = dest.get(name);
		if (value == null)
		    dest.put(name, source.get(name));
		else  if (value instanceof String[]) {
		    String oldValues [] = (String[])value;
		    String newValues [] = new String[oldValues.length+1];
		    System.arraycopy(oldValues,0,newValues,0,
				     oldValues.length);
		    newValues[oldValues.length] = (String)source.get(name);
		    dest.put(name,newValues);
		} else {
		    String newValues [] = new String[2];
		    newValues[0] = (String)source.get(name);
		    newValues[1] = (String)value;
		    dest.put(name,newValues);
		}
	    }
	    return dest;
	} else {
	    return source;
	}
    }

    private synchronized void prepareQueryParameters() {
	if( queryParameters != null )
	    return;
	Hashtable postParameters = null;
	// What kinf of parameters are we expecting ?
	if ( request.getMethod().equals("POST") ) {
	    // POSTed parameters, check content type:
	    if ((! request.hasContentType())
		|| (type.match(request.getContentType()) < 0) ) 
		return;
	    postParameters = new Hashtable(2);
	    // Get and decode the request entity:
	    URLDecoder dec = null;
	    try {
		Reader in = getReader() ;
		// Notify the client that we are willing to continue
		String exp = request.getExpect();
		if (exp != null && (exp.equalsIgnoreCase("100-continue"))) {
		    Client client = request.getClient();
		    if ( client != null ) {
			client.sendContinue();
		    }
		}
		String encoding = getCharacterEncoding();
		if (encoding == null) {
		    dec = new URLDecoder (in, false, "8859_1");
		} else {
		    dec = new URLDecoder (in, false, getCharacterEncoding());
		}
		postParameters = dec.parse() ;
	    } catch (URLDecoderException e) {
		postParameters = null;
	    } catch (IOException ex) {
		postParameters = null;
	    }
	}
	// URL encoded parameters:
	String query = getQueryString();
	if (query != null) {
	    Reader qis = null;
	    qis = new StringReader(query);
	    try {
		URLDecoder dec;
		String encoding = getCharacterEncoding();
		if (encoding == null) {
		    dec = new URLDecoder (qis, false, "8859_1");
		} else {
		    dec = new URLDecoder (qis, false, getCharacterEncoding());
		}
		queryParameters = dec.parse();
	    } catch (Exception ex) {
		throw new RuntimeException("Java implementation bug.");
	    }
	}
	queryParameters = mergeParameters(postParameters, queryParameters);
	// state parameters
	Hashtable param = (Hashtable)request.getState(STATE_PARAMETERS);
	queryParameters = mergeParameters(param, queryParameters);
	convertParameters(queryParameters);
    }

    protected String getURLParameter(String name) {
	Hashtable urlParameters = null;
	String query = getQueryString();
	if (query != null) {
	    Reader qis = new StringReader(query);
	    try {
		String encoding = getCharacterEncoding();
		if (encoding == null) {
		    urlParameters = new URLDecoder(qis,false,"8859_1").parse();
		} else {
		    urlParameters = new URLDecoder(qis,false,encoding).parse();
		}
		return (String) urlParameters.get(name);
	    } catch (Exception ex) {
		throw new RuntimeException("Java implementation bug.");
	    }
	}
	return null;
    }

    /**
     * Return the Charset parameter of content type
     * @return A String instance
     */
    public String getCharacterEncoding() {
	if (encoding == null) {
	    org.w3c.www.mime.MimeType type = request.getContentType();
	    if ((type != null) && (type.hasParameter("charset"))) {
		encoding = type.getParameterValue("charset");
	    } else {
		encoding = nullEnc;
	    }
	}
	if (encoding == nullEnc) {
	    return null;
	}
	return encoding;
    }

    /**
     * Overrides the name of the character encoding used in the body of this
     * request
     * ServletRequest implementation - version 2.3
     * @param enc, a <code>String</code> specifying the encoding String
     */
    public void setCharacterEncoding(String enc) 
	throws java.io.UnsupportedEncodingException
    {
	// a hack to see if the character encoding is supported 
	InputStreamReader isr = new InputStreamReader(new PipedInputStream(),
						      enc);
	encoding = enc;
    }

    /**
     * ServletRequest implementation - Get the length of request data.
     * @return An int, or <strong>-1</strong>.
     */

    public int getContentLength() {
	return request.getContentLength();
    }

    /**
     * ServletRequest implementation - Get the type of the request's body.
     * @return A String encoded mime type, or <strong>null</strong>.
     */

    public String getContentType() {
	org.w3c.www.mime.MimeType t = request.getContentType();
	return (t == null) ? null : t.toString();
    }

    /**
     * ServletRequest implementation - Get the protocol of that request.
     * @return A String encoded version of the protocol.
     */

    public String getProtocol() {
	return request.getVersion();
    }

    protected httpd getServer() {
	return request.getClient().getServer();
    }

   /**
    * ServletRequest implementation - Get the name of queried server.
    * @return Name of server, as a String.
    */

   public String getServerName() {
       String host = request.getHost();
       if (host != null) {
	   int idx = host.lastIndexOf(':');
	   if (idx != -1) {
	       return host.substring(0, host.lastIndexOf(':'));
	   } else {
	       return host;
	   }
       } else {
	   return getServer().getHost();
       }
   }

   /**
    * ServletRequest implementation - Get the port of queried server.
    * @return A port number (int).
    */

   public int getServerPort() {
       if (request.isProxy()) {
	   String host = request.getHost();
	   if (host != null) {
	       int idx = host.lastIndexOf(':');
	       if (idx == -1)
		   return 80;
	       return Integer.parseInt(host.substring(idx+1));
	   }
       }
       return getServer().getLocalPort();
   }

   /**
    * ServletRequest implementation - Get the IP address of requests's sender.
    * @return Numeric IP address, as a String.
    */

    public String getRemoteAddr() {
	return request.getClient().getInetAddress().getHostAddress();
    }

    /**
     * ServletRequest implementation - FQDN of request's sender.
     * @return Name of client's machine (FQDN).
     */

    public String getRemoteHost() {
	return request.getClient().getInetAddress().getHostName();
    }

    /**
     * ServletRequest implementation - Get real path.
     * Jigsaw realy has no notion of <em>translation</em> stricto
     * sensu (it has much better in fact ;-). This is a pain here.
     * @return the real path.
     * @deprecated since jsdk1.2
     */

    public String getRealPath(String name) {
	httpd             server  = getServer();
	ResourceReference rr_root = server.getRootReference();
	return JigsawServletContext.getRealPath(name, 
						rr_root, 
						request.getTargetResource());
    }

    protected ServletInputStream is = null;

    /**
     * Returns an input stream for reading binary data in the request body. 
     * @exception IllegalStateException if getReader has been called on 
     * this same request. 
     * @exception IOException on other I/O related errors. 
     * @see JigsawHttpServletRequest#getReader
     */    
    public ServletInputStream getInputStream()
	throws IOException
    {
	if (stream_state == STREAM_READER_USED)
	    throw new IllegalStateException("Reader used");
	stream_state = INPUT_STREAM_USED;
	return getJigsawInputStream();
    }

    /**
     * @exception IOException if an IO error occurs
     */ 
    protected ServletInputStream getJigsawInputStream()
	throws IOException
    {
	// If alredy computed return:
	if ( is != null )
	    return is;
	// Built it:
	InputStream stream = null;
	if ((stream = request.getInputStream()) == null) {
	    stream = new ContentLengthInputStream(null, 0);
	}
	return is = new JigsawServletInputStream(stream);
    }

    /**
     * ServletRequest implementation - Get a parameter value.
     * @return The String encoded value for the parameter.
     */

    public String getParameter(String name) {
	prepareQueryParameters();
	if ( queryParameters != null ) {
	    Object value = queryParameters.get(name);
	    if (value != null) { 
		return ((String[])value)[0];
	    }		
	} 
	return null;
    }

    /**
     * ServletRequest implementation - Get a parameter value. (v2.3)
     * @return a Map of the parameters in this request
     */
    public Map getParameterMap() {
	prepareQueryParameters();
	return queryParameters;
    }

    /**
     * ServletRequest implementation - Get the parameters value.
     * @return The String array encoded value for the parameter.
     */

    public String[] getParameterValues(String parameter) {
	Vector V = new Vector(23);
	prepareQueryParameters();
	if (queryParameters == null) {
	    return null;
	}
	Object value = queryParameters.get(parameter);
	if (value == null) {
	    return null;
	}
	return (String[])value;
    }

    /**
     * ServletRequest implementation - List available parameters.
     * @return An enumeration of parameter names.
     */

    public Enumeration getParameterNames() {
	prepareQueryParameters();
	return ((queryParameters == null)
		? new EmptyEnumeration()
		: queryParameters.keys());
    }

    /**
     * ServletRequest implementation - Get an attribute of the request.
     * This closely match Jigsaw's notion of request state.
     * @param name The name of the attribute.
     * @return An object that gives the value of the attribute.
     */

    public Object getAttribute(String name) {
	return request.getState(name);
    }

    public void setAttribute(String name, Object object) {
	request.setState(name, object);
    }

    /**
     * Removes an attribute from this request.  This method is not
     * generally needed as attributes only persist as long as the request
     * is being handled.
     *
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     * @param name a <code>String</code> specifying 
     * the name of the attribute to remove
     */
    public void removeAttribute(String name) {
	request.delState(name);
    }

    public Enumeration getAttributeNames() {
	return request.getStateNames();
    }

    /**
     * Returns the preferred <code>Locale</code> that the client will 
     * accept content in, based on the Accept-Language header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns the default locale for the server.
     *
     * @return the preferred <code>Locale</code> for the client
     */
    public Locale getLocale() {
	return (Locale)getLocales().nextElement();
    }

    /**
     * Returns an <code>Enumeration</code> of <code>Locale</code> objects
     * indicating, in decreasing order starting with the preferred locale, the
     * locales that are acceptable to the client based on the Accept-Language
     * header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns an <code>Enumeration</code> containing one 
     * <code>Locale</code>, the default locale for the server.
     *
     * @return an <code>Enumeration</code> of preferred 
     * <code>Locale</code> objects for the client
     */
    public Enumeration getLocales() {
	HttpAcceptLanguage languages[] = request.getAcceptLanguage();
	if (languages == null) {
	    Vector def = new Vector();
            def.addElement(Locale.getDefault());
            return def.elements();
	}

	//LinkedList is better, but we must be JDK1.1 compliant
	Vector locales = new Vector(); 

	for (int i = 0 ; i < languages.length ; i++) {
	    HttpAcceptLanguage language = languages[i];
	    double quality = language.getQuality();
	    String lang    = language.getLanguage();
	    String country = "";
	    int    idx     = lang.indexOf('-');
	    if (idx > -1) {
		country = lang.substring(idx + 1).trim();
		lang    = lang.substring(0, idx).trim();
	    }
	    // insert the Locale in ordered list
	    int     qidx = 0;
	    int     size = locales.size();
	    if (size > 0) {
		QLocale ql   = (QLocale) locales.firstElement();
		while ((qidx < size) && (ql.getLanguageQuality() >= quality)) {
		    try {
			ql = (QLocale) locales.elementAt(++qidx);
		    } catch (ArrayIndexOutOfBoundsException ex) {
			//end of vector, so append
		    }
		}
		locales.insertElementAt(new QLocale(lang, country, quality),
					qidx);
	    } else {
		locales.addElement(new QLocale(lang, country, quality));
	    }
	}
	// because Locale is final :(
	int    size    = locales.size(); 
	Vector vlocale = new Vector(size);
	for (int i = 0 ; i < size ; i ++) {
	    vlocale.addElement(((QLocale)locales.elementAt(i)).getLocale());
	}
	return vlocale.elements();
    }

    /**
     * Returns a boolean indicating whether this request was made using a
     * secure channel, such as HTTPS.
     *
     * @return a boolean indicating if the request was made using a
     * secure channel
     */

    public boolean isSecure() {
	// only https secure?
	return (request.getURL().getProtocol().equalsIgnoreCase("https"));
    }

    /**
     * HttpServletRequest implementation - Get the request's method.
     * @return A String instance.
     */

    public  String getMethod() {
	return request.getMethod();
    }

    /**
     * HttpServletRequest implementation - Get the request's path info.
     * @return A String instance or <strong>null</strong>.
     */

    public  String getPathInfo() {
	if (request.hasState(JigsawRequestDispatcher.PATH_INFO_P)) {
	    String pathinfo = 
		(String) request.getState(JigsawRequestDispatcher.PATH_INFO_P);
	    return (pathinfo.equals("/")) ? null : pathinfo;
	}
	return null;
    }

    /**
     * HttpServletRequest implementation - Get the request's path translated.
     * @return A String instance or <strong>null</strong>.
     */

    public  String getPathTranslated() {
	String pathinfo = getPathInfo();
	if ( pathinfo != null )
	    return getRealPath(pathinfo);
	return null;
    }

    /**
     * Returns the portion of the request URI that indicates the context
     * of the request.  The context path always comes first in a request
     * URI.  The path starts with a "/" character but does not end with a "/"
     * character.  For servlets in the default (root) context, this method
     * returns "".
     * @return a <code>String</code> specifying the portion of the request 
     * URI that indicates the context of the request
     */
    public String getContextPath() {
	return "";
    }

    public boolean hasQueryString() {
	if (request.hasQueryString()) {
	    return true;
	} else {
	    return request.hasState(JigsawRequestDispatcher.QUERY_STRING_P);
	}
    }

    /**
     * HttpServletRequest implementation - Get the request's query string.
     * @return A String instance or <strong>null</strong>.
     */
    public String getQueryString() {
	if (request.hasQueryString()) {
	    return request.getQueryString();
	} else if (request.hasState(JigsawRequestDispatcher.QUERY_STRING_P)) {
	    return (String) 
		request.getState(JigsawRequestDispatcher.QUERY_STRING_P);
	}
	return null;
    }

    /**
     * HttpServletRequest implementation - Get the request's user (if any).
     * @return A String instance or <strong>null</strong>.
     */

    public String getRemoteUser() {
	return (String) request.getState(AuthFilter.STATE_AUTHUSER);
    }

    /**
     * Returns a boolean indicating whether the authenticated user is included
     * in the specified logical "role".  Roles and role membership can be
     * defined using deployment descriptors.  If the user has not been
     * authenticated, the method returns <code>false</code>.
     *
     * @param role a <code>String</code> specifying the name of the role
     * @return a <code>boolean</code> indicating whether the user making this
     * request belongs to a given role; <code>false</code> if the user has not
     * been authenticated
     */

    public boolean isUserInRole(String role) {
	throw new RuntimeException("Not Yet Implemented");
    }

    /**
     * Returns a <code>java.security.Principal</code> object containing
     * the name of the current authenticated user. If the user has not been
     * authenticated, the method returns <code>null</code>.
     *
     * @return a <code>java.security.Principal</code> containing
     * the name of the user making this request; <code>null</code> if the 
     * user has not been authenticated
     */
    public Principal getUserPrincipal() {
	return new PrincipalImpl(getRemoteUser());
    }

    /**
     * HttpServletRequest implementation - Get the request's auth method.
     * @return A String instance or <strong>null</strong>.
     */

    public String getAuthType() {
	return (String) request.getState(AuthFilter.STATE_AUTHTYPE);
    }

    /**
     * HttpServletRequest implementation - Get a request header as a String.
     * @return A String instance or <strong>null</strong>.
     */

    public String getHeader(String name) {
	return request.getValue(name);
    }

    /**
     * Returns all the values of the specified request header
     * as an <code>Enumeration</code> of <code>String</code> objects.
     *
     * <p>Some headers, such as <code>Accept-Language</code> can be sent
     * by clients as several headers each with a different value rather than
     * sending the header as a comma separated list.
     *
     * <p>WARNING, this can't happen with Jigsaw, all multiple values are
     * grouped in one, and only one, header. So, this method always return 
     * ONE header value.
     *
     * <p>If the request did not include any headers
     * of the specified name, this method returns an empty
     * <code>Enumeration</code>.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name a <code>String</code> specifying the header name
     * @return a <code>Enumeration</code> containing the values of the 
     * requested header, or <code>null</code> if the request does not
     * have any headers of that name
     */			

    public Enumeration getHeaders(String name) {
	String value = getHeader(name);
	String array[] = { value };
	return new ArrayEnumeration(array);
    }

    /**
     * HttpServletRequest implementation - Get a request header as an int.
     * @return An int, or <strong>-1</strong>.
     */

    public int getIntHeader(String name) {
	HeaderValue v = request.getHeaderValue(name);
	if ( v != null ) {
	    Object o = v.getValue();
	    if ((o != null) && (o instanceof Integer))
		return ((Integer) o).intValue();
	}
	return -1;
    }

    /**
     * HttpServletRequest implementation - Get a request header as an date.
     * @return An long (as a number of milliseconds), or <strong>-1</strong>.
     */

    public long getDateHeader(String name) {
	HeaderValue v = request.getHeaderValue(name, null);
	if ( v != null ) {
	    Object o = v.getValue();
	    if ((o != null) && (o instanceof Long)) 
		return ((Long) o).longValue();
	}
	return (long) -1;
    }

    /**
     * HttpServletRequest implementation - Get a all header names.
     * @return An enumeration.
     */

    public Enumeration getHeaderNames() {
	return new HeaderNames(request.enumerateHeaderDescriptions());
    }

    /**
     * Gets, from the first line of the HTTP request, 
     * the part of this request's URI that is to the left of any query string.
     */
    public String getRequestURI() {
	String uri = null;
	if (request.hasState(JigsawRequestDispatcher.REQUEST_URI_P)) {
	    uri = (String)
		request.getState(JigsawRequestDispatcher.REQUEST_URI_P);
	    try {
		URL u = new URL(request.getURL(), uri);
		uri = u.getFile();
	    } catch (MalformedURLException muex) {}
	} else {
	    //fixme test
	    if (request.isProxy()) {
		uri = request.getURL().toExternalForm();
	    } else {
		uri = request.getURLPath();
	    }
	    if (hasQueryString()) {
		String query = getQueryString();
		int idx = uri.lastIndexOf(query);
		uri = uri.substring(0, idx-1);
	    }
	}
	return uri;
    }

    /**
     * Gets, from the first line of the HTTP request, 
     * the part of this request's URI that is to the left of any query string.
     */
    public StringBuffer getRequestURL() {
	String uri = null;
	if (request.hasState(JigsawRequestDispatcher.REQUEST_URI_P)) {
	    uri = (String)
		request.getState(JigsawRequestDispatcher.REQUEST_URI_P);
	    try {
		URL u = new URL(request.getURL(), uri);
		uri = u.toExternalForm();
	    } catch (MalformedURLException muex) {}
	} else {
	    uri = request.getURL().toExternalForm();
	    if (hasQueryString()) {
		String query = getQueryString();
		int idx = uri.lastIndexOf(query);
		uri = uri.substring(0, idx-1);
	    }
	}
	return new StringBuffer(uri);
    }

    /**
     * Returns a {@link RequestDispatcher} object that acts as a wrapper for
     * the resource located at the given path.  
     * A <code>RequestDispatcher</code> object can be used to forward
     * a request to the resource or to include the resource in a response.
     * The resource can be dynamic or static.
     *
     * <p>The pathname specified may be relative, although it cannot extend
     * outside the current servlet context.  If the path begins with 
     * a "/" it is interpreted as relative to the current context root.  
     * This method returns <code>null</code> if the servlet container
     * cannot return a <code>RequestDispatcher</code>.
     *
     * <p>The difference between this method and {@link
     * ServletContext#getRequestDispatcher} is that this method can take a
     * relative path.
     *
     * @param path a <code>String</code> specifying the pathname
     * to the resource
     * @return a <code>RequestDispatcher</code> object that acts as a 
     * wrapper for the resource at the specified path
     * @see RequestDispatcher
     * @see ServletContext#getRequestDispatcher
     */
    public RequestDispatcher getRequestDispatcher(String path) {
	if (path == null) {
	    throw new IllegalArgumentException("null");
	}
	String            urlpath = null;
	ResourceReference rr      = request.getTargetResource();
	if (! path.startsWith("/")) {
	    String uri = null;
	    try {
		ResourceReference rrp = rr.lock().getParent();
		try {
		    Resource r = rrp.lock();
		    uri = r.getURLPath();
		} catch (InvalidResourceException irex) {
		    return null;
		} finally {
		    rrp.unlock();
		}
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	    urlpath = ( uri.endsWith("/") ? uri+path : uri+"/"+path );
	} else {
	    urlpath = path;
	}
	return JigsawRequestDispatcher.getRequestDispatcher(urlpath, 
							    getServer(),
							    rr);
    }

    /**
     * Gets the part of this request's URI that refers to the servlet 
     * being invoked. Analogous to the CGI variable SCRIPT_NAME. 
     */
    public String getServletPath() {
	if (request.hasState(JigsawRequestDispatcher.SERVLET_PATH_P)) {
	    return (String)
		request.getState(JigsawRequestDispatcher.SERVLET_PATH_P);
	} else {
	    ResourceReference rr = request.getTargetResource();
	    try {
		return rr.lock().getURLPath();
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	}
    }

    /**
     * @return the scheme of the URL used in this request, for example "http",
     * "https", or "ftp". Different schemes have different rules
     * for constructing URLs, as noted in RFC 1738. The URL used to create 
     * a request may be reconstructed using this scheme, the server name 
     * and port, and additional information such as URIs.
     */
    public String getScheme() {
	return request.getURL().getProtocol();
    }

    /**
     * Gets the array of cookies found in this request.
     * @return the array of cookies found in this request or
     * <strong>null</strong> if there is no cookie.
     */
    public Cookie[] getCookies() {
	HttpCookieList cookielist = request.getCookie();
	Cookie[] Scookies = null;
	if (cookielist != null) {
	    HttpCookie[] cookies = cookielist.getCookies();
	    Scookies = new Cookie[cookies.length];
	    for (int i = 0 ; i < cookies.length ; i++ ) {
		Scookies[i] = convertCookie(cookies[i]);
	    }
	}
	return Scookies;
    }

    protected Cookie convertCookie(HttpCookie httpCookie) {
	Cookie cookie = new Cookie(httpCookie.getName(),
				   httpCookie.getValue());
	String val = null;
	if ((val = httpCookie.getDomain()) != null)
	    cookie.setDomain(val);
	if ((val = httpCookie.getPath()) != null)
	    cookie.setPath(val);
	cookie.setVersion(httpCookie.getVersion());
	return cookie;
    }

    protected String getRequestedSessionIdFromCookie() {
	HttpCookieList cookielist = request.getCookie();
	if (cookielist != null) {
	    HttpCookie httpCookie = 
		request.getCookie().getCookie(getCookieName());
	    if (httpCookie != null)
		return httpCookie.getValue();
	}
	return null;
    }

    protected String getRequestedSessionIdFromURL() {
	return getURLParameter(getCookieName());
    }

    /**
     * Gets the session id specified with this request. This may differ 
     * from the actual session id. For example, if the request specified an
     * id for an invalid session, then this will get a new session with a 
     * new id. 
     * @return the session id specified by this request, or null if the 
     * request did not specify a session id.
     */
    public String getRequestedSessionId() {
	if (requestedSessionID == null) {
	    requestedSessionID = getRequestedSessionIdFromCookie();
	    if (requestedSessionID == null)
		requestedSessionID = getRequestedSessionIdFromURL();
	}
	return requestedSessionID;
    }

    protected synchronized JigsawHttpSessionContext getSessionContext() {
	return sessionContext;
    }

    /**
     * Gets the current valid session associated with this request, if create
     * is false or, if necessary, creates a new session for the request, if 
     * create is true. 
     * @return the session associated with this request or null if create 
     * was false and no valid session is associated with this request. 
     */
    public HttpSession getSession(boolean create) {
	if (httpSession == null) {
	    httpSession = (JigsawHttpSession)
		getSession(getRequestedSessionId());
	    if (httpSession != null) // the client join the session
		httpSession.setNoMoreNew();
	}
	if (httpSession == null & create) {
	    httpSession = new JigsawHttpSession(getSessionContext(), 
						servletContext,
						createCookie());
	    response.addCookie(httpSession.getCookie());
	} else if (httpSession != null) {
	    httpSession.setLastAccessedTime();
	    if (! httpSession.isValid()) {
		httpSession = new JigsawHttpSession(getSessionContext(), 
						    servletContext,
						    createCookie());
		response.addCookie(httpSession.getCookie());
	    }
	}
	return httpSession;
    }

    /**
     * Gets the current valid session associated with this request.
     * @return the session associated with this request.
     */
    public HttpSession getSession() {
	return getSession(true);
    }

    protected String getCookieName() {
	 ObservableProperties props =
	     request.getClient().getServer().getProperties();
	 return props.getString(ServletProps.SERVLET_COOKIE_NAME,
				ServletProps.DEFAULT_COOKIE_NAME);
    }

    protected Cookie createCookie() {
	ObservableProperties props = 
	    request.getClient().getServer().getProperties();
	String name     = props.getString(ServletProps.SERVLET_COOKIE_NAME,
					  ServletProps.DEFAULT_COOKIE_NAME);
	String path     = props.getString(ServletProps.SERVLET_COOKIE_PATH,
					  "/");
	String domain   = props.getString(ServletProps.SERVLET_COOKIE_DOMAIN,
					  null);
	String comment  = props.getString(ServletProps.SERVLET_COOKIE_COMMENT,
					  null);
	int maxage      = props.getInteger(ServletProps.SERVLET_COOKIE_MAXAGE,
					   -1);
	boolean secure  = props.getBoolean(ServletProps.SERVLET_COOKIE_SECURE,
					   isSecure());

	Cookie cookie = new Cookie(name, null);
	cookie.setPath(path);
	cookie.setMaxAge(maxage);
	if ((comment != null) && (comment.length() > 0))
	    cookie.setComment(comment);
	if ((domain != null) && (domain.length() > 0))
	    cookie.setDomain(domain);
	cookie.setSecure(secure);
	return cookie;
    }

    protected HttpSession getSession(String sessionId) {
	if (sessionId != null)
	    return getSessionContext().getSession(sessionId);
	return null;
    }

    /**
     * Checks whether this request is associated with a session that is valid 
     * in the current session context. If it is not valid, the requested
     * session will never be returned from the getSession method. 
     * @return true if this request is assocated with a session that is valid 
     * in the current session context. 
     */
    public boolean isRequestedSessionIdValid() {
	JigsawHttpSession session = (JigsawHttpSession) 
	    getSession(getRequestedSessionId());
	if (session == null)
	    return false;
	return (session.isValid());
    }

    /**
     * Checks whether the session id specified by this request came in as 
     * a cookie. (The requested session may not be one returned by the 
     * getSession method.)
     * @return true if the session id specified by this request came 
     * in as a cookie; false otherwise 
     */
    public boolean isRequestedSessionIdFromCookie() {
	return (getRequestedSessionIdFromCookie() != null);
    }

    /**
     * Checks whether the session id specified by this request came in as 
     * part of the URL. (The requested session may not be the one returned 
     * by the getSession method.)
     * @return true if the session id specified by the request for this 
     * session came in as part of the URL; false otherwise
     * @deprecated since jsdk2.1
     */
    public boolean isRequestedSessionIdFromUrl() {
	return (getRequestedSessionIdFromURL() != null);
    }

    /**
     * Checks whether the session id specified by this request came in as 
     * part of the URL. (The requested session may not be the one returned 
     * by the getSession method.)
     * @return true if the session id specified by the request for this 
     * session came in as part of the URL; false otherwise
     */
    public boolean isRequestedSessionIdFromURL() {
	return (getRequestedSessionIdFromURL() != null);
    }

    protected BufferedReader reader = null;

    /**
     * Returns a buffered reader for reading text in the request body. 
     * This translates character set encodings as appropriate.
     * @exception UnsupportedEncodingException if the character set encoding 
     * is unsupported, so the text can't be correctly decoded. 
     * @exception IllegalStateException if getInputStream has been called on 
     * this same request. 
     * @exception IOException on other I/O related errors. 
     * @see JigsawHttpServletRequest#getInputStream 
     */
    public BufferedReader getReader()
	throws IOException
    {
	if (stream_state == INPUT_STREAM_USED)
	    throw new IllegalStateException("Input Stream used");
	stream_state = STREAM_READER_USED;
	if (reader == null) {
	    InputStream is = getJigsawInputStream();
	    String enc = getCharacterEncoding();
	    if (enc != null) {
		InputStreamReader isr = null;
		try {
		    isr = new InputStreamReader(is, enc);
		} catch (UnsupportedEncodingException ex) {
		    // not a valid encoding, skip it
		    isr = null;
		}
		if (isr != null)
		    reader = new BufferedReader(isr);
		else
		    reader = new BufferedReader(new InputStreamReader(is));
	    } else {
		reader = new BufferedReader(new InputStreamReader(is));	
	    }
	}
	return reader;
    }

    /**
     * Get the wrapped Jigsaw Request.
     * @return the request
     */
    protected Request getRequest() {
	return request;
    }

    JigsawHttpServletRequest(Servlet servlet, 
			     JigsawServletContext servletContext,
			     Request request, 
			     JigsawHttpServletResponse response,
			     JigsawHttpSessionContext sessionContext) {
	this.servlet        = servlet;
	this.servletContext = servletContext;
	this.request        = request;
	this.response       = response;
	this.sessionContext = sessionContext;
    }

}
