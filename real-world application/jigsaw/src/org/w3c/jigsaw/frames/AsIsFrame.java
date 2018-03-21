// AsIsFrame.java
// $Id: AsIsFrame.java,v 1.1 2010/06/15 12:24:18 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

import org.w3c.tools.resources.event.AttributeChangedEvent;

import org.w3c.util.CountInputStream;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeParserException;

import org.w3c.www.http.ByteRangeOutputStream;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderDescription;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.MimeParserReplyFactory;
import org.w3c.www.http.HttpContentRange;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpRange;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpFactory;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

/**
 * Read the HTTP reply directly from a file. Like in Apache mod_asis
 * http://httpd.apache.org/docs/mod/mod_asis.html
 * 
 */
public class AsIsFrame extends HTTPFrame {
    
    // the file offset
    int foffset = -1;
    HttpReplyMessage asisreply = null;

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
	super.attributeChanged(evt);
	asisreply = null;
    }

    /**
     * shadows the call from HTTPFrame
     * It updates the cached headers for the automagic reply generation
     */
    protected void updateCachedHeaders() {
	super.updateCachedHeaders();
	if (asisreply == null) {
	    if (fresource != null) {
		// read the reply from the file stream
		FileInputStream fis = null;
		try {
		    fis = new FileInputStream(fresource.getFile());
		} catch (FileNotFoundException fex) {
		    // should never happen
		}
		CountInputStream cis = new CountInputStream(fis);
		MimeParserReplyFactory repfact = new MimeParserReplyFactory();
		MimeParser mp = new MimeParser(cis, repfact);
		try {
		    asisreply = (HttpReplyMessage) mp.parse();
		} catch (MimeParserException mex) {
		    // probably a "normal" file, serve it as-is...
		    return;
		} catch (IOException ioex) {
		    // silently fail, iot will fail later anyway
		    return;
		}
		// update the offset and the Content-Length
		foffset = (int) cis.getBytesRead();
		try {
		    cis.close();
		} catch (IOException ioex) {};
		int cl = fresource.getFileLength();
		cl -= foffset;
		setValue(ATTR_CONTENT_LENGTH, new Integer(cl));
		contentlength = HttpFactory.makeInteger(cl);
		// reset the md5, as we don't yet compute it
		md5Digest = null;
	    }
	}
    }

    /**
     * Create a reply to answer to request on this file.
     * This method will create a suitable reply (matching the given request)
     * and will set all its default header values to the appropriate 
     * values.
     * The Status is ignored as it is superceded byt the Asis file
     * @param request The request to make a reply for.
     * @return An instance of Reply, suited to answer this request.
     */
    public Reply createDefaultReply(Request request, int status) {
	if (asisreply == null) {
	    updateCachedHeaders();
	}
	if (asisreply == null) {
	    // not parsed, try to serve it as a normal file
	    return super.createDefaultReply(request, status);
	}
	// create the reply with the parsed status
	Reply reply = super.createDefaultReply(request, asisreply.getStatus());
	reply.setReason(asisreply.getReason());
	// and update all defined headers
	Enumeration e = asisreply.enumerateHeaderDescriptions();
	while ( e.hasMoreElements() ) {
	    HeaderDescription d = (HeaderDescription) e.nextElement();
	    HeaderValue       v = asisreply.getHeaderValue(d);
	    if ( v != null )
		reply.setHeaderValue(d, v);
	}
	return reply;
    }

    /**
     * Create the reply relative to the given file.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply createFileReply(Request request) 
	throws ProtocolException, ResourceException
    {
	File file = fresource.getFile() ;
	Reply reply = null;
	// Won't check for range request, as we don't control the status
	// Default to full reply then
	reply = createDefaultReply(request, HTTP.OK) ;
	try { 
	    FileInputStream fis = new FileInputStream(file);
	    // and escape the headers
	    fis.skip(foffset);
	    reply.setStream(fis);
	} catch (IOException ex) {
	    Reply error = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
	    error.setContent(ex.getMessage());
	    return error;
	}
	return reply ;
    }
}
