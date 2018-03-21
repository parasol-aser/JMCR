// TEFilter.java
// $Id: TEFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.util.zip.DeflaterOutputStream;
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

import org.w3c.www.http.HttpAcceptEncoding;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

/**
 * This filter will compress the content of replies using GZIP or whatever
 * encoding scheme requested in the TE: header of the request.
 * Compression is done <em>on the fly</em>. This assumes that you're really
 * on a slow link, where you have lots of CPU, but not much bandwidth.
 * <p>A nifty usage for that filter, is to plug it on top of a
 * <code>org.w3c.jigsaw.proxy.ProxyFrame</code>, in which case it
 * will encode the data when it flies out of the proxy.
 */

public class TEFilter extends ResourceFilter {
    /**
     * Attribute index - List of MIME type that we can compress
     */
    protected static int ATTR_MIME_TYPES = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.TEFilter");
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

    // grumble... DeflateInputStream with a compression/decompression
    // flag would have been better in the core API ;)
    private class DataMover extends Thread {
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

	DataMover(InputStream in, OutputStream out) {
	    this.in  = in;
	    this.out = out;
	    setName("DataMover");
	    start();
	}
    }

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

    protected double getCompressibilityFactor(Reply reply) {
        // Match possible mime types:
	MimeType t[]     = getMimeTypes();
	if ( t != null ) {
	    for (int i = 0 ; i < t.length ; i++) {
		if ( t[i] == null )
		    continue;
		if ( t[i].match(reply.getContentType()) > 0 ) {
		    String enc[] = reply.getContentEncoding();
		    if (enc != null ) {
			for (int j=0; j< enc.length; j++) {
			    if ((enc[j].indexOf("gzip") >= 0) ||
				(enc[j].indexOf("deflate") >= 0) ||
				(enc[j].indexOf("compress") >= 0)) {
				// no more compression
				return 0.001;
			    }
			}
		    }
		    return 1.0;
		}
	    }
	}
	return 0.001; // minimal value
    }

    protected void doEncoding(HttpAcceptEncoding enc, Reply reply) {
        // Anything to compress ?
	if ( ! reply.hasStream() )
	    return;
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
	    return;	
	InputStream orig_is = reply.openStream();
	// do the gzip encoding
	if (enc.getEncoding().equals("gzip")) {
	    try {
		PipedOutputStream gzpout = new PipedOutputStream();
		PipedInputStream  gzpin  = new PipedInputStream(gzpout);
		new DataMover(reply.openStream()
			      , new GZIPOutputStream(gzpout));
		reply.setStream(gzpin);
	    } catch (IOException ex) {
		ex.printStackTrace();
		reply.setStream(orig_is);
		return;
	    }
	    reply.addTransferEncoding("gzip");
	    reply.setContentLength(-1);
	} else if (enc.getEncoding().equals("deflate")) {
	    // do the deflate encoding
	    try {
		PipedOutputStream zpout = new PipedOutputStream();
		PipedInputStream  zpin  = new PipedInputStream(zpout);
		new DataMover(reply.openStream()
			      , new DeflaterOutputStream(zpout));
		reply.setStream(zpin);
	    } catch (IOException ex) {
		ex.printStackTrace();
		reply.setStream(orig_is);
		return;
	    }
	    reply.addTransferEncoding("deflate");
	    reply.setContentLength(-1);
	}
	return;
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
      HttpAcceptEncoding encs[] = request.getTE();
      String trenc[] = reply.getTransferEncoding();
      
      // Anything to compress ?
      if ( ! reply.hasStream() )
	  return null;
      
      if (trenc != null) {
	  // don't mess with already encoded stuff
	  // otherwise we have to dechunk/rechunk and it would be painful
	  return null;
      }

      if (encs != null) { // identity and chucked always ok
	  double max = -1.0;
	  double identity = 1.0; // some dummy default values
	  double chunked = 1.0;
	  double comp_factor = getCompressibilityFactor(reply);
	  HttpAcceptEncoding best = null;
	  for (int i = 0 ; i < encs.length ; i++) {
	      if (encs[i].getEncoding().equals("identity")) {
		  identity = encs[i].getQuality();
		  continue;
	      } else if (encs[i].getEncoding().equals("chunked")) {
		  chunked = encs[i].getQuality();
		  continue;
	      } else if (encs[i].getEncoding().equals("trailers")) {
		  // means that the client understand trailers.. check 
		  // that with pending trailers impl
		  // req.setTrailerOk();
		  continue;
	      }
	      if (encs[i].getQuality() * comp_factor > max) {
		  best = encs[i];
		  max = encs[i].getQuality() * comp_factor;
		  if (max == 1.0) // can't be better
		      break;
	      }
	  }
	  if (best != null && (max >= identity)) {
	      doEncoding(best, reply);
	  } else {
	      if (identity > 0) {
		  if (chunked > identity)
		      reply.setContentLength(-1);
	      } else {
		  // spec says: chunked always acceptable
		  reply.setContentLength(-1);
	      }
	  }
      }
      return null;
    }
}
