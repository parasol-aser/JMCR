// LogFilter.java
// $Id: LogFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.tools.resources.ProtocolException;

/**
 * This filter provides a very flexible logger.
 * It is not designed as a logger, in order to be plugable only on
 * a sub-tree of the URL space (a logger would log all site accesses).
 * It provides as much details as you want, and uses a very simple format:
 * each log entry (or <em>record</em> is made of several lines having
 * the folowing format:
 * <pre>variable=value</pre>
 * A record starts with the special <code>url</code> variable value which
 * provides the requested URL. The for each header that is to be logged,
 * a variable is added in the record, prefixed by its <em>scope</em>. The
 * scope can be either:
 * <dl><dt>request<dd> to specify a request header,
 * <dt>reply<dd> to specify a reply header,
 * <dt>server<dd> to specify global server samples.
 * </dl>
 * As an example, if you configure that filter to log the request's referer
 * and the reply content length, a sample record will look like:
 * <pre>
 * url=http://www.w3.org/pub/WWW/Jigsaw/
 * request.referer=http://www.w3.org/pub/WWW
 * reply.content-length=10
 * </pre>
 */

public class LogFilter extends ResourceFilter {
    /**
     * Name of the state that when set on the request will prevent logging.
     */
    public static final 
	String DONT_LOG = "org.w3c.jigsaw.filters.LogFilter.nolog";

    /**
     * Attribute index - The HTTP request headers to dump
     */
    protected static int ATTR_REQUEST_HEADERS = -1;
    /**
     * Attribute index - The HTTP reply headers to dump
     */
    protected static int ATTR_REPLY_HEADERS = -1;
    /**
     * Attribute index - The log file to use to emit log record.
     */
    protected static int ATTR_LOGFILE = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.filters.LogFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the request headers to be logged:
	a = new StringArrayAttribute("request-headers"
				     , null
				     , Attribute.EDITABLE);
	ATTR_REQUEST_HEADERS = AttributeRegistry.registerAttribute(c, a);
	// Register the reply headers to be logged:
	a = new StringArrayAttribute("reply-headers"
				     , null
				     , Attribute.EDITABLE);
	ATTR_REPLY_HEADERS = AttributeRegistry.registerAttribute(c, a);
	// Register the log file name:
	a = new FileAttribute("logfile"
			      , null
			      , Attribute.EDITABLE);
	ATTR_LOGFILE = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Compiled index of the request headers to dump.
     */
    protected HeaderDescription reqheaders[] = null;
    /**
     * Compiled index of the reply headers to dump.
     */
    protected HeaderDescription repheaders[] = null;
    /**
     * Open log descriptor, to write to the log.
     */
    protected RandomAccessFile log = null;

    /**
     * Compile the given set of header names into header indexes.
     * @param kind An instance of the class whose headers are to be dumped.
     * @param headers The name of headers to compile.
     * @return An array of header description, which will allow fast fetch
     * of header values.
     */

    protected HeaderDescription[] compileHeaders(HttpMessage kind
						 , String headers[]) {
	Enumeration       e   = kind.enumerateHeaderDescriptions(true);
	HeaderDescription r[] = new HeaderDescription[headers.length];
	while ( e.hasMoreElements() ) {
	    HeaderDescription d = (HeaderDescription) e.nextElement();
	    if ( d == null )
		continue;
	    for (int i = 0 ; i < headers.length ; i++) {
		if (headers[i].equals(d.getName())) {
		    r[i] = d;
		    break;
		}
	    }
	}
	return r;
    }

    /**
     * Write the given string to the log file.
     * @param record The string to write.
     */

    protected synchronized void writelog(String record) {
	try {
	    if ( log != null ) 
		log.writeBytes(record);
	} catch (IOException ex) {
	    FramedResource target = (FramedResource) getTargetResource();
	    if (target != null) {
		String msg = ("IO error while writing to log file \""+
			      ex.getMessage() + "\".");
		target.getServer().errlog(this, msg);
	    }
	}
    }

    /**
     * Open the log stream, and make it available through <code>log</code>.
     * If opening the stream failed, an appropriate error message is emitted
     * and <code>log</code> remains set to <strong>null</strong>. If a log
     * stream was already opened, it is first closed.
     */

    protected synchronized void openLog() {
	// Close first if needed:
	if ( log != null ) {
	    try {
		log.close();
	    } catch (IOException ex) {
	    }
	    log = null;
	}
	// Try opening the log:
	File logfile = getLogfile();
	try {
	    if (logfile != null) {
		log = new RandomAccessFile(logfile, "rw");
		log.seek(log.length());
	    }
	} catch (Exception ex) {
	}
	// If failed, emit error message:
	if ( log == null ) {
	    FramedResource target = (FramedResource) getTargetResource();
	    if (target != null) {
		String msg = ("unable to open log file \""+
			      logfile
			      + "\".");
		target.getServer().errlog(this, msg);
	    }
	}
    }

    /**
     * Get the log file.
     * @return A File instance, or <strong>null</strong> if not set.
     */

    public File getLogfile() {
	return (File) getValue(ATTR_LOGFILE, null);
    }

    /**
     * Get the list of request headers to dump.
     * @return An array of String containing the name of headers to dump, or
     * <strong>null</strong> if undefined.
     */

    public String[] getRequestHeaders() {
	return (String[]) getValue(ATTR_REQUEST_HEADERS, null);
    }

    /**
     * Get the list of reply headers to dump.
     * @return An array of String containing the name of headers to dump,
     * or <strong>null</strong> if undefined.
     */

    public String[] getReplyHeaders() {
	return (String[]) getValue(ATTR_REPLY_HEADERS, null);
    }

    /**
     * Traop setValue calls.
     * We maintain a compiled version of both the <code>request-headers</code>
     * and the <code>reply-headers</code> attributes, make sure they stay in 
     * sync even when modified.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_REQUEST_HEADERS ) {
	    synchronized(this) {
		reqheaders = compileHeaders(new HttpRequestMessage()
					    , (String[]) value);
	    }
	} else if (idx == ATTR_REPLY_HEADERS ) {
	    synchronized(this) {
		repheaders = compileHeaders(new HttpReplyMessage()
					    , (String[]) value);
	    }
	} else if (idx == ATTR_LOGFILE ) {
	    openLog();
	}
    }

    /**
     * Log the given request/reply transaction.
     * Dump a record for the given transaction.
     * @param request The request to log.
     * @param reply It's associated reply.
     */

    protected void log(Request request, Reply reply) {
	StringBuffer sb = new StringBuffer();
	// Start a new log record:
	sb.append("url=");
	sb.append(request.getURL().toString());
	sb.append('\n');
	// Then log all request headers:
	if ( reqheaders != null ) {
	    for (int i = 0 ; i < reqheaders.length ; i++) {
		HeaderDescription d = reqheaders[i];
		if ( d == null )
		    continue;
		HeaderValue v = request.getHeaderValue(d);
		if ( v != null ) {
		    sb.append("request.");
		    sb.append(d.getName());
		    sb.append('=');
		    sb.append(v.toString());
		    sb.append('\n');
		}
	    }
	}
	// The log all reply headers:
	if ( repheaders != null ) {
	    for (int i = 0 ; i < repheaders.length ; i++) {
		HeaderDescription d = repheaders[i];
		if ( d == null )
		    continue;
		HeaderValue v = reply.getHeaderValue(d);
		if ( v != null ) {
		    sb.append("reply.");
		    sb.append(d.getName());
		    sb.append('=');
		    sb.append(v.toString());
		    sb.append('\n');
		}
	    }
	}
	writelog(sb.toString());
    }

    /**
     * Log the request.
     * @param request The request that has been handled.
     * @param reply It's associated reply.
     * @exception org.w3c.tools.resources.ProtocolException 
     * If processing should be interrupted, because an abnormal situation 
     * occured. 
     */

    public ReplyInterface outgoingFilter(RequestInterface req, 
					 ReplyInterface rep)
	throws ProtocolException 
    {
	Reply   reply   = (Reply) rep;
	Request request = (Request) req;
	if ( ! reply.hasState(DONT_LOG) )
	    log(request, reply);
	return null;
    }

    /**
     * Initialize the filter.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	// Open the log file, ready for working:
	if ( log == null ) 
	    openLog();
	// Compile request and reply headers to dump:
	if ( reqheaders == null ) {
	    String headers[] = getRequestHeaders();
	    if ( headers != null )
		reqheaders = compileHeaders(new HttpRequestMessage(), headers);
	}
	if ( repheaders == null ) {
	    String headers[] = getReplyHeaders();
	    if ( headers != null )
		repheaders = compileHeaders(new HttpReplyMessage(), headers);
	}
    }

}
