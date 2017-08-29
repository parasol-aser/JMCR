// JigsawHttpServletReponse.java
// $Id: JigsawHttpServletResponse.java,v 1.1 2010/06/15 12:24:11 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Locale;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;
import org.w3c.www.mime.Utils;

import org.w3c.www.http.BasicValue;
import org.w3c.www.http.HttpAcceptCharsetList;
import org.w3c.www.http.HttpAcceptEncodingList;
import org.w3c.www.http.HttpAcceptLanguageList;
import org.w3c.www.http.HttpAcceptList;
import org.w3c.www.http.HttpCookie;
import org.w3c.www.http.HttpCookieList;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpEntityTagList;
import org.w3c.www.http.HttpExt;
import org.w3c.www.http.HttpExtList;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpParamList;
import org.w3c.www.http.HttpRangeList;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpSetCookie;
import org.w3c.www.http.HttpSetCookieList;
import org.w3c.www.http.HttpString;
import org.w3c.www.http.HttpTokenList;
import org.w3c.www.http.HttpWarningList;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HeaderValue;

/**
 * @author Alexandre Rafalovitch <alex@access.com.au>
 * @author Anselm Baird-Smith <abaird@w3.org>
 * @author Benoît Mahé (bmahe@w3.org)
 * @author Roland Mainz (Roland.Mainz@informatik.med.uni-giessen.de)
 */

public class JigsawHttpServletResponse implements HttpServletResponse {

    public final static String CHARSET_PARAMETER = "charset";

    public final static int DEFAULT_BUFFER_SIZE   = 8 * 1024; // 8KB buffer
    public final static int MIN_BUFFER_SIZE       = 4 * 1024; // 4KB buffer

    private final static int STATE_INITIAL        = 0;
    private final static int STATE_HEADERS_DONE   = 1;
    private final static int STATE_ALL_DONE       = 2;

    private final static int STREAM_STATE_INITIAL = 0;
    private final static int STREAM_WRITER_USED   = 1;
    private final static int OUTPUT_STREAM_USED   = 2;

    private int stream_state = STREAM_STATE_INITIAL;

    private JigsawServletOutputStream output = null;
    private PrintWriter               writer = null;

    private MimeTypeFormatException setContentTypeException = null;

    // servlet has set a fixed content length or not, 
    // and cut (see flushStream) any data which are too much here...
    private final static int CALC_CONTENT_LENGTH = -1; 
    private              int fixedContentLength  = CALC_CONTENT_LENGTH;

    // Our Locale
    protected Locale locale;

    /**
     * Our temp stream.
     */
    protected ByteArrayOutputStream out = null;
    protected PipedOutputStream pout = null;

    protected JigsawHttpServletRequest jrequest = null;

    protected int buffer_size;

    protected void setServletRequest(JigsawHttpServletRequest jrequest) {
	this.jrequest = jrequest;
    }

    public static final 
	String INCLUDED = "org.w3c.jigsaw.servlet.included";
    public static final 
	String STREAM = "org.w3c.jigsaw.servlet.stream";
    public static final 
	String MONITOR = "org.w3c.jigsaw.servlet.monitor";

    int   state = STATE_INITIAL;
    Reply reply = null;
    Request request = null;

    /**
     * Sets the content length for this response. 
     * @param len - the content length 
     */
    public void setContentLength(int i) {
	fixedContentLength = i;
	reply.setContentLength(i);
    }

    /**
     * Sets the content type for this response. This type may later be 
     * implicitly modified by addition of properties such as the MIME
     * charset=<value> if the service finds it necessary, and the appropriate
     * media type property has not been set.
     * <p>This response property may only be assigned one time. If a writer 
     * is to be used to write a text response, this method must be
     * called before the method getWriter. If an output stream will be used 
     * to write a response, this method must be called before the
     * output stream is used to write response data. 
     * @param spec - the content's MIME type 
     * @see JigsawHttpServletResponse#getOutputStream
     * @see JigsawHttpServletResponse#getWriter
     */    
    public void setContentType(String spec) {
	try {
	    MimeType type= new MimeType(spec);
	    reply.setContentType(type);
	    setContentTypeException = null;
	} catch(MimeTypeFormatException ex) {
	    //store exception
	    setContentTypeException = ex;
	}
    }

    protected boolean isStreamObtained() {
	return (stream_state != STREAM_STATE_INITIAL);
    }

    protected Reply getReply() {
	return reply;
    }

    /**
     * Returns an output stream for writing binary response data.
     * @return A ServletOutputStream
     * @exception IOException if an I/O exception has occurred 
     * @exception IllegalStateException if getWriter has been called on this 
     * same request. 
     * @see JigsawHttpServletResponse#getWriter
     */    
    public synchronized ServletOutputStream getOutputStream()
	throws IOException
    {
	if (stream_state == STREAM_WRITER_USED) {
	    // obviously output is not null, but it doesn't cost...
	    if ((output != null) && output.isCommitted()) {
		throw new IllegalStateException("Writer used");
	    }
	    output.reset();
	}
	stream_state = OUTPUT_STREAM_USED;
	return getJigsawOutputStream(false);
    }

    /** 
     * returns the raw output stream regardless of what happened before
     * used for internal operation (e.g. writing an exception trailer).
     * @return an underlying output stream if available (even when a writer is
     * used)
     */
    
    protected synchronized OutputStream getRawOutputStream() {
	return output;
    }

    synchronized ServletOutputStream getJigsawOutputStream(boolean writerUsed)
	throws IOException
    {
	if ( output != null )
	    return output;

	if (state == STATE_ALL_DONE) {
	    throw new IOException("Processing finished");
	}
	// any exception during setContentType ?    
	if( setContentTypeException != null ) {
	    // "wrap" the exception from setContentType in an IOException
	    throw new IOException("Illegal Content Type: "+
				  setContentTypeException.toString());
	}

	if( request.hasState(INCLUDED) ) {
	    out = new ByteArrayOutputStream();
	    output = new JigsawServletOutputStream(this, 
						   new DataOutputStream(out),
						   buffer_size,
						   writerUsed);
	    reply.setState(STREAM, new Object());
	} else {
	    if (reply.hasState(STREAM)) {
		try {
		    pout = (PipedOutputStream) reply.getState(STREAM);
		    DataOutputStream dos = new DataOutputStream(pout);
		    output = new JigsawServletOutputStream(this, 
							   dos,
							   buffer_size,
							   writerUsed);
		    reply.setState(STREAM, null);
		} catch (ClassCastException ex) {
		    // it is null, no OutputStream -> redirect done
		    // we will eat this anyway
		    output = new JigsawServletOutputStream(this, 
							   reply, 
							   buffer_size,
							   writerUsed);
		    reply.setState(STREAM, new Object());
		}
	    } else {
		output = new JigsawServletOutputStream(this, 
						       reply, 
						       buffer_size,
						       writerUsed);
		reply.setState(STREAM, new Object());
	    }
	}
	return output;
    }

    synchronized void notifyClient() {
	Object o = reply.getState(MONITOR);
	if (o != null) {
	    synchronized (o) {
		o.notifyAll();
	    }
	}
    }

    /**
     * Sets the status code and message for this response. If the field had
     * already been set, the new value overwrites the previous one. The message
     * is sent as the body of an HTML page, which is returned to the user to
     * describe the problem. The page is sent with a default HTML header; the
     * message is enclosed in simple body tags (<body></body>).
     * @param i - the status code 
     * @param reason - the status message
     * @deprecated since jsdk2.1
     */
    public void setStatus(int i, String reason) {
	reply.setStatus(i);
	reply.setReason(reason);
    }

    /**
     * Sets the status code for this response. This method is used to set the
     * return status code when there is no error (for example, for the status
     * codes SC_OK or SC_MOVED_TEMPORARILY). If there is an error, the 
     * sendError method should be used instead.
     * @param i - the status code 
     * @see JigsawHttpServletResponse#sendError
     */
    public void setStatus(int i) {
	setStatus(i, reply.getStandardReason(i));
    }

    /**
     * Adds a field to the response header with the given name and value. If
     * the field had already been set, the new value overwrites the previous
     * one. The containsHeader method can be used to test for the presence of a
     * header before setting its value.
     * @param name - the name of the header field 
     * @param value - the header field's value 
     * @see JigsawHttpServletResponse#containsHeader
     */
    public void setHeader(String name, String value) {
	reply.setValue(name, value);
    }

    /**
     * Adds a field to the response header with the given name and integer
     * value. If the field had already been set, the new value overwrites the
     * previous one. The containsHeader method can be used to test for the
     * presence of a header before setting its value.
     * @param name - the name of the header field 
     * @param value - the header field's integer value 
     * @see JigsawHttpServletResponse#containsHeader
     */
    public void setIntHeader(String name, int value) {
	setHeader(name, String.valueOf(value));
    }

    /**
     * Adds a field to the response header with the given name and date-valued
     * field. The date is specified in terms of milliseconds since the epoch. 
     * If the date field had already been set, the new value overwrites the
     * previous one. The containsHeader method can be used to test for the
     * presence of a header before setting its value.
     * @param name - the name of the header field 
     * @param value - the header field's date value 
     * @see JigsawHttpServletResponse#containsHeader 
     */
    public void setDateHeader(String name, long date) {
	setHeader(name, HttpFactory.makeDate(date).toExternalForm());
    }

    public void unsetHeader(String name) {
	setHeader(name, null);
    }

    /**
     * Sends an error response to the client using the specified status code
     * and descriptive message. If setStatus has previously been called, it is
     * reset to the error status code. The message is sent as the body of an
     * HTML page, which is returned to the user to describe the problem. The
     * page is sent with a default HTML header; the message is enclosed in
     * simple body tags (<body></body>).
     * @param sc - the status code 
     * @param msg - the detail message 
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void sendError(int i, String msg) 
	throws IOException
    {
	if (isStreamObtained() && (output != null) && output.isCommitted()) {
	    throw new IOException("Reply already started in servlet");
	}
	if (output != null) {
	    output.reset();
	}
	setStatus(i);
	reply.setContent(msg);
	state = STATE_ALL_DONE;
	reply.setState(STREAM, new Object());
	notifyClient();
    }

    /**
     * Sends an error response to the client using the specified status 
     * code and a default message. 
     * @param sc - the status code 
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void sendError(int i)
        throws IOException
    {
	sendError(i, reply.getStandardReason(i));
    }

    /**
     * Sends a temporary redirect response to the client using the specified
     *  redirect location URL. The URL must be absolute (for example, 
     * https://hostname/path/file.html). Relative URLs are not permitted here. 
     * @param url - the redirect location URL 
     * @exception IOException If an I/O error has occurred. 
     */    
    public synchronized void sendRedirect(String url)
        throws IOException
    {
	URL loc = null;
	if (isStreamObtained() && (output != null) && output.isCommitted()) {
	    throw new IOException("Reply already started in servlet");
	}
	if (output != null) {
	    output.reset();
	}
	try {
	    String requri = jrequest.getRequestURI();
	    URL    requrl = request.getURL();
	    loc = new URL(requrl.getProtocol(), 
			  requrl.getHost(), 
			  requrl.getPort(),
			  requri);
	    loc = new URL(loc, url);
//  Removed (netscape doesn't know SEE OTHER! sig)
// 	    if (jrequest.getMethod().equalsIgnoreCase("POST") &&
// 		jrequest.getProtocol().equals("HTTP/1.1")) {
// 		setStatus(SC_SEE_OTHER);
// 	    } else {
		setStatus(SC_MOVED_TEMPORARILY);
//	    }
	    reply.setLocation(loc);
	    HtmlGenerator g = new HtmlGenerator("Moved");
	    g.append("<P>This resource has moved, click on the link if your"
		     + " browser doesn't support automatic redirection<BR>"+
		     "<A HREF=\""+loc.toExternalForm()+"\">"+
		     loc.toExternalForm()+"</A>");
	    reply.setStream(g);
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    state = STATE_ALL_DONE;
	    reply.setState(STREAM, new Object());
	    notifyClient();
	}
    }

    /**
     * Checks whether the response message header has a field with the
     * specified name. 
     * @param name - the header field name 
     * @return true if the response message header has a field with the 
     * specified name; false otherwise
     */
    public boolean containsHeader(String header) {
	return reply.hasHeader(header);
    }

    /**
     * Adds the specified cookie to the response. It can be called multiple 
     * times to set more than one cookie. 
     * @param cookie - the Cookie to return to the client 
     */
    public void addCookie(Cookie cookie) {
	HttpSetCookieList clist = reply.getSetCookie();
	if (clist == null) {
	    HttpSetCookie cookies [] = new HttpSetCookie[1];
	    cookies[0] = convertCookie(cookie);
	    clist = new HttpSetCookieList(cookies);
	} else {
	    clist.addSetCookie(convertCookie(cookie));
	}
	reply.setSetCookie(clist);
    }

    private HttpSetCookie convertCookie(Cookie cookie) {
	HttpSetCookie scookie = new HttpSetCookie(true, 
						  cookie.getName(),
						  cookie.getValue());
	scookie.setComment(cookie.getComment());
	scookie.setDomain(cookie.getDomain());
	scookie.setMaxAge(cookie.getMaxAge());
	scookie.setPath(cookie.getPath());
	scookie.setSecurity(cookie.getSecure());
	scookie.setVersion(cookie.getVersion());
	return scookie;
    }

    /**
     * Encodes the specified URL for use in the sendRedirect method or, if 
     * encoding is not needed, returns the URL unchanged. The implementation 
     * of this method should include the logic to determine whether the 
     * session ID needs to be encoded in the URL.
     * Because the rules for making this determination differ from those used
     * to decide whether to encode a normal link, this method is seperate from
     * the encodeUrl method.
     * <p>All URLs sent to the HttpServletResponse.sendRedirect method should
     * be run through this method. Otherwise, URL rewriting canont be used 
     * with browsers which do not support cookies. 
     * @param url - the url to be encoded. 
     * @return the encoded URL if encoding is needed; the unchanged URL 
     * otherwise. 
     * @deprecated since jsdk2.1
     * @see JigsawHttpServletResponse#sendRedirect
     * @see JigsawHttpServletResponse#encodeUrl
     */
    public String encodeRedirectUrl(String url) {
	try {
	    URL redirect = new URL(url);
	    URL requested = new URL(jrequest.getRequestURI());
	    if ( redirect.getHost().equals(requested.getHost()) &&
		 redirect.getPort() == requested.getPort())
		return encodeUrl(url);
	} catch (MalformedURLException ex) {
	    //error so return url.
	    return url;
	}
	return url;
    }

        /**
     * Encodes the specified URL for use in the sendRedirect method or, if 
     * encoding is not needed, returns the URL unchanged. The implementation 
     * of this method should include the logic to determine whether the 
     * session ID needs to be encoded in the URL.
     * Because the rules for making this determination differ from those used
     * to decide whether to encode a normal link, this method is seperate from
     * the encodeUrl method.
     * <p>All URLs sent to the HttpServletResponse.sendRedirect method should
     * be run through this method. Otherwise, URL rewriting canont be used 
     * with browsers which do not support cookies. 
     * @param url - the url to be encoded. 
     * @return the encoded URL if encoding is needed; the unchanged URL 
     * otherwise. 
     * @see JigsawHttpServletResponse#sendRedirect
     * @see JigsawHttpServletResponse#encodeUrl
     */
    public String encodeRedirectURL(String url) {
	return encodeRedirectUrl(url);
    }

    /**
     * Encodes the specified URL by including the session ID in it, or, if 
     * encoding is not needed, returns the URL unchanged. The implementation of
     * this method should include the logic to determine whether the session ID
     * needs to be encoded in the URL. For example, if the browser supports
     * cookies, or session tracking is turned off, URL encoding is unnecessary.
     * <p>All URLs emitted by a Servlet should be run through this method. 
     * Otherwise, URL rewriting cannot be used with browsers which do not 
     * support cookies.
     * @param url - the url to be encoded. 
     * @return the encoded URL if encoding is needed; the unchanged URL 
     * otherwise. 
     * @deprecated since jsdk2.1
     */
    public String encodeUrl(String url) {
	if (! jrequest.isRequestedSessionIdFromCookie()) {
	    url = url + ((url.indexOf("?") != -1) ? "&" : "?")+
		jrequest.getCookieName()+"="+
		jrequest.getSession(true).getId();
	}
	return url;
    }

    /**
     * Encodes the specified URL by including the session ID in it, or, if 
     * encoding is not needed, returns the URL unchanged. The implementation of
     * this method should include the logic to determine whether the session ID
     * needs to be encoded in the URL. For example, if the browser supports
     * cookies, or session tracking is turned off, URL encoding is unnecessary.
     * <p>All URLs emitted by a Servlet should be run through this method. 
     * Otherwise, URL rewriting cannot be used with browsers which do not 
     * support cookies.
     * @param url - the url to be encoded. 
     * @return the encoded URL if encoding is needed; the unchanged URL 
     * otherwise. 
     */
    public String encodeURL(String url) {
	return encodeUrl(url);
    }

    /**
     * Return the Charset parameter of content type
     * @return A String instance
     */
    public String getCharacterEncoding() {
	org.w3c.www.mime.MimeType type = reply.getContentType();
	if ((type != null) && (type.hasParameter(CHARSET_PARAMETER))) {
	    return type.getParameterValue(CHARSET_PARAMETER);
	}
	// as specified in the javadoc, default to iso-8859-1
	return "ISO-8859-1".intern();
    }

    /**
     * Returns a print writer for writing formatted text responses. 
     * The MIME type of the response will be modified, if necessary, to
     * reflect the character encoding used, through the charset=... property. 
     * This means that the content type must be set before calling this 
     * method. 
     * @exception UnsupportedEncodingException if no such encoding can be 
     * provided 
     * @exception IllegalStateException if getOutputStream has been called 
     * on this same request.
     * @exception IOException on other errors. 
     * @see JigsawHttpServletResponse#getOutputStream
     * @see JigsawHttpServletResponse#setContentType 
     */    
    public synchronized PrintWriter getWriter() 
	throws IOException, UnsupportedEncodingException
    {
	if (stream_state == OUTPUT_STREAM_USED) {
	    // obviously output is not null, but it doesn't cost...
	    if ((output != null) && output.isCommitted()) {
		throw new IllegalStateException("Output stream used");
	    } 
	    output.reset();
	}
		
	stream_state = STREAM_WRITER_USED;

	if (writer == null) {
	    writer = new PrintWriter(
			 new OutputStreamWriter(getJigsawOutputStream(true), 
						getCharacterEncoding()));
	}
	return writer;
    }

    /**
     * Flush the output stream.
     * @param close Close the stream if true.
     * @exception IOException if an IO error occurs.
     */
    protected synchronized void flushStream(boolean close) 
	throws IOException
    {
	if (state == STATE_ALL_DONE) {
	    return;
	}
	int writeLength;

	if (stream_state == OUTPUT_STREAM_USED) {
	    output.flush();
	} else if (stream_state == STREAM_WRITER_USED) {
	    writer.flush();
	    if (close) {
		output.realFlush();
	    } else {
		output.flush();
	    }
	} else {
	    // force flush even if no stream are openned
	    getWriter();
	    writer.flush();
	    output.realFlush();
	}

	if (request.hasState(INCLUDED)) {
	    if (out == null)
		return;

	    if( fixedContentLength != CALC_CONTENT_LENGTH ) {
		writeLength = (out.size() < fixedContentLength) 
		    ? (out.size())
		    : (fixedContentLength);
	    } else {
		writeLength = out.size();
	    }
	    reply.setContentLength(writeLength);
	    
	    OutputStream rout = reply.getOutputStream(false);
	    byte content[] = out.toByteArray();
	    if (close)
		out.close();
	    else 
		out.reset();
	    rout.write(content);
	    rout.flush();
	} else {
	    if ((pout != null) && close) {
		pout.flush();
		pout.close();
	    }
	}
    }

    // 2.2

    /**
     * Sets the preferred buffer size for the body of the response.  
     * The servlet container will use a buffer at least as large as 
     * the size requested.  The actual buffer size used can be found
     * using <code>getBufferSize</code>.
     *
     * <p>A larger buffer allows more content to be written before anything is
     * actually sent, thus providing the servlet with more time to set
     * appropriate status codes and headers.  A smaller buffer decreases 
     * server memory load and allows the client to start receiving data more
     * quickly.
     *
     * <p>This method must be called before any response body content is
     * written; if content has been written, this method throws an 
     * <code>IllegalStateException</code>.
     * @param size the preferred buffer size
     * @exception IllegalStateException	if this method is called after
     * content has been written
     * @see #getBufferSize
     * @see #flushBuffer
     * @see #isCommitted
     * @see #reset
     */
    public void setBufferSize(int size) {
	if (stream_state != STREAM_STATE_INITIAL) {
	    throw new IllegalStateException("Stream already initialized");
	}
	buffer_size = size < MIN_BUFFER_SIZE ? MIN_BUFFER_SIZE : size;
    }

    /**
     * Returns the actual buffer size used for the response.  If no buffering
     * is used, this method returns 0.
     * @return the actual buffer size used
     * @see #setBufferSize
     * @see #flushBuffer
     * @see #isCommitted
     * @see #reset
     */
    public int getBufferSize() {
	return buffer_size;
    }

    /**
     * Forces any content in the buffer to be written to the client.  A call
     * to this method automatically commits the response, meaning the status 
     * code and headers will be written.
     * @see #setBufferSize
     * @see #getBufferSize
     * @see #isCommitted
     * @see #reset
     */
    public void flushBuffer() 
	throws IOException
    {
	if (output != null) {
	    if (stream_state == STREAM_WRITER_USED) {
		writer.flush();
	    }
	    output.flush();
	}
    }

    /**
     * Returns a boolean indicating if the response has been
     * committed.  A commited response has already had its status 
     * code and headers written.
     * @return a boolean indicating if the response has been
     * committed
     * @see #setBufferSize
     * @see #getBufferSize
     * @see #flushBuffer
     * @see #reset
     */
    public boolean isCommitted() {
	if (output != null) {
	    return output.isCommitted();
	} else {
	    return false;
	}
    }

    /**
     * Clears any data that exists in the buffer as well as the status code and
     * headers.  If the response has been committed, this method throws an 
     * <code>IllegalStateException</code>.
     * @exception IllegalStateException  if the response has already been
     *                                   committed
     * @see #setBufferSize
     * @see #getBufferSize
     * @see #flushBuffer
     * @see #isCommitted
     */
    public void reset() {
	// FIXME needs to clear headers and status
	if (output != null) {
	    if (stream_state == STREAM_WRITER_USED) {
		writer.flush();
	    }
	    output.reset();
	}
    }

    /**
     * Clears any data that exists in the buffer but not status code and 
     * headers.  If the response has been committed, this method throws an 
     * <code>IllegalStateException</code>.
     * @exception IllegalStateException  if the response has already been
     *                                   committed
     * @see #setBufferSize
     * @see #getBufferSize
     * @see #flushBuffer
     * @see #isCommitted
     */
    public void resetBuffer() {
	if (output != null) {
	    if (stream_state == STREAM_WRITER_USED) {
		writer.flush();
	    }
	    output.reset();
	}
    }

    /**
     * Sets the locale of the response, setting the headers (including the
     * Content-Type's charset) as appropriate.  This method should be called
     * before a call to {@link #getWriter}.  By default, the response locale
     * is the default locale for the server.
     * @param loc  the locale of the response
     * @see #getLocale
     */
    public void setLocale(Locale locale) {
	if (locale == null) {
	    return;
	}
	this.locale = locale;

	// content language
	String lang = locale.getLanguage();
	if (lang.length() > 0) {
	    String array[] = new String[1];
	    array[0] = lang;
	    reply.setContentLanguage(array);
	}
	
	// content type (charset)
	String charset = org.w3c.www.mime.Utils.getCharset(locale);
	if (charset != null) {
	    MimeType contentType = reply.getContentType();
	    // override charset
	    contentType.setParameter(CHARSET_PARAMETER, charset);
	}
    }

    /**
     * Returns the locale assigned to the response.
     * @see #setLocale
     */
    public Locale getLocale() {
	return locale;
    }

    public void addHeader(String name, String value) {
	String      lname = name.toLowerCase();
	HeaderValue hvalue = reply.getHeaderValue(lname);
	//
	// Horrible, Shame on us, I hate that
	//
	if (hvalue == null) {
	    setHeader(name, value);
	} else if (hvalue instanceof HttpAcceptCharsetList) {
	    HttpAcceptCharsetList acl = (HttpAcceptCharsetList) hvalue;
	    acl.addCharset(HttpFactory.parseAcceptCharset(value));
	} else if (hvalue instanceof HttpAcceptEncodingList) {
	    HttpAcceptEncodingList ael = (HttpAcceptEncodingList) hvalue;
	    ael.addEncoding(HttpFactory.parseAcceptEncoding(value));
	} else if (hvalue instanceof HttpAcceptLanguageList) {
	    HttpAcceptLanguageList all = (HttpAcceptLanguageList) hvalue;
	    all.addLanguage(HttpFactory.parseAcceptLanguage(value));
	} else if (hvalue instanceof HttpAcceptList) {
	    HttpAcceptList al = (HttpAcceptList) hvalue;
	    al.addAccept(HttpFactory.parseAccept(value));
	} else if (hvalue instanceof HttpEntityTagList) {
	    HttpEntityTagList etl = (HttpEntityTagList) hvalue;
	    etl.addTag(HttpFactory.parseETag(value));
	} else if (hvalue instanceof HttpExtList) {
	    HttpExtList el = (HttpExtList) hvalue;
	    el.addHttpExt(new HttpExt(value, false));
	} else if (hvalue instanceof HttpCookieList) {
	    // shouldn't be used, but who knows?
	    HttpCookieList cl  = (HttpCookieList) hvalue;
	    HttpCookieList ncl = HttpFactory.parseCookieList(value);
	    HttpCookie scookies[] = ncl.getCookies();
	    for (int i = 0 ; i < scookies.length ; i++) {
		HttpCookie cookie = scookies[i];
		cl.addCookie(cookie.getName(), cookie.getValue());
	    }
	} else if (hvalue instanceof HttpParamList) {
	    int idx = value.indexOf('=');
	    if (idx != -1) {
		String pname  = value.substring(0, idx);
		String pvalue = value.substring(idx+1);
		HttpParamList pl = (HttpParamList) hvalue;
		pl.setParameter(pname, pvalue);
	    }
	} else if (hvalue instanceof HttpRangeList) {
	    HttpRangeList rl = (HttpRangeList) hvalue;
	    rl.addRange(HttpFactory.parseRange(value));
	} else if (hvalue instanceof HttpSetCookieList) {
	    HttpSetCookieList scl = (HttpSetCookieList) hvalue;
	    HttpSetCookieList nscl = HttpFactory.parseSetCookieList(value);
	    HttpSetCookie scookies[] = nscl.getSetCookies();
	    for (int i = 0 ; i < scookies.length ; i++) {
		scl.addSetCookie(scookies[i]);
	    }
	} else if (hvalue instanceof HttpTokenList) {
	    ((HttpTokenList) hvalue).addToken(value, true);
	} else if (hvalue instanceof HttpWarningList) {
	    HttpWarningList wl = (HttpWarningList) hvalue;
	    wl.addWarning(HttpFactory.parseWarning(value));
	} else if (hvalue instanceof HttpString) {
	    // this is the default type for unkown header
	    // we don't know what it is, so just append
	    HttpString s = (HttpString) hvalue;
	    String string = (String) s.getValue();
	    s.setValue(string+", "+value);
	} else {
	    // not compliant with HTTP/1.1, override
	    setHeader(name, value);
	}
    }

    public void addDateHeader(String name, long date) {
	addHeader(name, HttpFactory.makeDate(date).toExternalForm());
    }

    public void addIntHeader(String name, int value) {
	addHeader(name, String.valueOf(value));
    }

    JigsawHttpServletResponse(Request request, Reply reply) {
	this.request     = request;
	this.reply       = reply;
	this.buffer_size = DEFAULT_BUFFER_SIZE;
    }

}
