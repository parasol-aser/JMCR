// GZIPFilter.java
// $Id: GZIPFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT, Keio and ERCIM, 1996, 2003
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.util.zip.GZIPOutputStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringArrayAttribute;

import org.w3c.www.mime.MimeType;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

class GZIPDataMover extends Thread {
    InputStream in = null;
    OutputStream out = null;

    public void run() {
	try {
	    byte buf[] = new byte[1024];
	    int  got   = -1;
	    while ((got = in.read(buf)) >= 0) 
		out.write(buf, 0, got);
	} catch (IOException ex) {
	    ex.printStackTrace();
	} finally {
	    try { in.close(); } catch (Exception ex) {};
	    try { out.close(); } catch (Exception ex) {} ;
	}
    }

    GZIPDataMover(InputStream in, OutputStream out) {
	this.in  = in;
	this.out = out;
	setName("GZIPDataMover");
	start();
    }
}

/**
 * This filter will compress the content of replies using GZIP.
 * Compression is done <em>on the fly</em>. This assumes that you're really
 * on a slow link, where you have lots of CPU, but not much bandwidth.
 * <p>NOTE that as it change the Content-Encoding of the served content,
 * it MUST NOT be used on a proxy or a proxy/cache..
 */

public class GZIPFilter extends ResourceFilter {
    /**
     * Attribute index - List of MIME type that we can compress
     */
    protected static int ATTR_MIME_TYPES = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.GZIPFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the MIME types attribute:
	a = new StringArrayAttribute("mime-types"
				     , null
				     , Attribute.EDITABLE);
	ATTR_MIME_TYPES = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The set of MIME types we are allowed to compress.
     */
    protected MimeType types[] = null;

    /**
     * Catch the setting of mime types to compress.
     * @param idx The attribute being set.
     * @param val The new attribute value.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_MIME_TYPES ) {
	    synchronized (this) {
		types = null;
	    }
	}
    }

    
    /**
     * Get the set of MIME types to match:
     * @return An array of MimeType instances.
     */

    public synchronized MimeType[] getMimeTypes() {
	if ( types == null ) {
	    String strtypes[] = (String[]) getValue(ATTR_MIME_TYPES, null);
	    if ( strtypes == null )
		return null;
	    types = new MimeType[strtypes.length];
	    for (int i = 0 ; i < types.length ; i++) {
		try {
		    types[i] = new MimeType(strtypes[i]);
		} catch (Exception ex) {
		    types[i] = null;
		}
	    }
	}
	return types;
    }

    /**
     * @param request The original request.
     * @param reply It's original reply. 
     * @return A Reply instance, or <strong>null</strong> if processing 
     * should continue normally. 
     * @exception ProtocolException If processing should be interrupted, 
     * because an abnormal situation occured. 
     */
    public ReplyInterface outgoingFilter(RequestInterface req,
					 ReplyInterface rep) 
	throws ProtocolException
    {
	Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	// Anything to compress ?
	if ( ! reply.hasStream() ) {
	    return null;
	}
	// if there is already a Content-Encoding, skip this
	if (reply.getContentEncoding() != null) {
	    return null;
	}
	// Match possible mime types:
	MimeType t[]     = getMimeTypes();
	boolean  matched = false;
	if ( t != null ) {
	    for (int i = 0 ; i < t.length ; i++) {
		if ( t[i] == null )
		    continue;
		if ( t[i].match(reply.getContentType()) > 0 ) {
		    matched = true;
		    break;
		}
	    }
	}
	if ( ! matched ) 
	    return null;
	// Compress:
	try {
	    PipedOutputStream pout = new PipedOutputStream();
	    PipedInputStream  pin  = new PipedInputStream(pout);
	    new GZIPDataMover(reply.openStream()
			      , new GZIPOutputStream(pout));
	    reply.addContentEncoding("gzip");
	    reply.setContentLength(-1);
	    reply.setStream(pin);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

}
