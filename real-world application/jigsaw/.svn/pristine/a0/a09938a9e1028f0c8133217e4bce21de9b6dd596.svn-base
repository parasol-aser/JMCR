// Reply.java
// $Id: Reply.java,v 1.1 2010/06/15 12:25:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeType;

import org.w3c.www.http.ChunkedInputStream;
import org.w3c.www.http.ContentLengthInputStream;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpMimeType;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpStreamObserver;

public class Reply extends HttpReplyMessage {
    protected static HttpMimeType  DEFAULT_TYPE = null;

    static {
	DEFAULT_TYPE = HttpFactory.makeMimeType(MimeType.TEXT_HTML);
    }

    MimeParser parser = null;
    HttpStreamObserver observer = null;

    public boolean keepsAlive() {
	if ( major >= 1 ) {
	    // HTTP/1.1 is quite cool, in some sense
  	    if ( minor >= 1 ) 
	        return ((getContentLength() >= 0) 
                         || hasTransferEncoding("chunked"));
	    // HTTP/1.0 tries to be as cool, with no success
	    return (hasConnection("keep-alive") 
		    || hasProxyConnection("keep-alive"));
	}
	return false;
    }

    /**
     * Set an stream observer on the reply entity stream.
     * This method should be called <em>before</em> any caller gets
     * a chance to execute <code>getInputStream</code>. It is needed
     * for HttpServer instances to be notified when the stream becomes
     * available for the next request.
     */

    protected void setStreamObserver(HttpStreamObserver observer) {
	this.observer = observer;
    }

    /**
     * Notify this reply that is has been built to answer given request.
     * Perform has many ugly hack HTTP/1.1 requires. 
     * @param request The request that is answered by this reply.
     */

    protected void matchesRequest(Request request) {
	String mth = request.getMethod();
	if ( mth.equals("HEAD") /* || mth.equals("OPTIONS") */)
	    setStream(null);
    }

    protected InputStream input        = null;
    protected boolean     definesInput = false;

    /**
     * Set this reply's input stream.
     * @param input The stream to read the reply's entity from.
     */

    public void setStream(InputStream input) {
	this.input        = input;
	this.definesInput = true;
    }

    /**
     * Get this reply entity body.
     * The reply entity body is returned as an InputStream, that the caller
     * has to read to actually get the bytes of the content.
     * @return An InputStream instance. If the reply has no body, the 
     * returned input stream will just return <strong>-1</strong> on
     * first read.
     */

    public InputStream getInputStream()
	throws IOException
    {
	if ( definesInput )
	    return input;
	// Check the status code, there are some special cases floating around
	switch(getStatus()) {
	  case HTTP.NOT_MODIFIED:
	  case HTTP.NO_CONTENT:
	      return null;
	}
	// First, do we have chunked encoding:
	if ( hasTransferEncoding("chunked") ) {
	    definesInput = true;
	    input = new ChunkedInputStream(observer, parser.getInputStream());
	    return input;
	}
	// Find out if there is a content length
	int len = getContentLength();
	if ( len >= 0 ) {
	    input = new ContentLengthInputStream(observer
						 , parser.getInputStream()
						 , len);
	    definesInput = true;
	    return input;
	}
	// Everything has failed, we assume the connection will close:
	if ( observer != null )
	    observer.notifyFailure(parser.getInputStream());
	return parser.getInputStream();
    }

    /**
     * Does this reply has an associated entity stream ?
     * @return A boolean, <strong>true</strong> if the reply has an entity
     * stream, <strong>false</strong> otherwise.
     */

    public boolean hasInputStream() 
	throws IOException
    {
	return getInputStream() != null;
    }

    /**
     * Set this reply content.
     * This method allows to set the reply content to a simple String instance.
     * @param msg The reply content.
     */

    public void setContent (String msg) {
	if ( ! hasHeader(H_CONTENT_TYPE) )
	    setHeaderValue(H_CONTENT_TYPE, DEFAULT_TYPE) ;
	setContentLength (msg.length()) ;
	setStream(new StringBufferInputStream(msg));
    }

    protected Reply(short major, short minor, int status) {
	this.major = major;
	this.minor = minor;
	this.setStatus(status);
    }

    protected Reply(MimeParser parser) {
	super(parser);
	this.parser = parser;
    }

}
