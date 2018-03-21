// JpegComFrame.java
// $Id: JpegComFrame.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpAccept;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInvalidValueException;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.ClientException;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.tools.jpeg.JpegHeaders;
import org.w3c.jigsaw.resources.ImageFileResource;

/**
 * This class will read the comments from a jpeg file and return it
 * depending on the Accept: header
 */

public class JpegComFrame extends HTTPFrame {
    public static final boolean debug = false;
    /**
     * Attribute index - The comment content type
     */
    protected static int ATTR_COM_TYPE = -1 ;

    static {
	Attribute   a = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.JpegComFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The comment content type
	a = new MimeTypeAttribute("comment-type",
				  null,
				  Attribute.EDITABLE) ;
	ATTR_COM_TYPE = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * the static String of the Vary ehader to be added
     */
    protected static String[] vary = { "Accept" };

    /**
     * get the content type of the comment embedded in the picture
     * @return a MimeType, or null if undefined
     */
    public MimeType getCommentType() {
	return (MimeType)getValue(ATTR_COM_TYPE, null);
    }

    /**
     * The comment entity tag
     */
    protected HttpEntityTag cometag = null;

    /**
     * The comment.
     */
    protected String comment = null;

    /**
     * Extract the comment from the jpeg image.
     * @return the comment
     */
    protected String getMetadata() {
	if (fresource == null)
	    return null;
	File file = fresource.getFile();
	if (file.exists()) {
	    String comments[] = null;
	    try {
		JpegHeaders headers = new JpegHeaders(file);
		comments = headers.getComments();
	    } catch (Exception ex) {
		ex.printStackTrace();
		return "unable to get comment: "+ex.getMessage();
	    }
	    comment = "";
	    for (int i = 0 ; i < comments.length ; i++)
		comment += comments[i];
	    if (comment.equals(""))
		comment = "no comment";
	}
	return comment;
    }

    /**
     * Get the comment Etag
     * @return an instance of HttpEntityTag, or <strong>null</strong> if not
     *    defined.
     */

    public HttpEntityTag getComETag() {
	if (cometag == null) {
	    String etag_s = null;
	    if (fresource != null) {
		long lstamp = fresource.getFileStamp()+1;
		if ( lstamp >= 0L ) {
		    String soid  = Integer.toString(getOid(), 32);
		    String stamp = Long.toString(lstamp, 32);
		    etag_s = Integer.toString(getOid(), 32)+":"
			+ Long.toString(lstamp, 32);
		}
	    }
	    cometag = HttpFactory.makeETag(false, etag_s);
	}
	return cometag;
    }

    /**
     * Update the cached headers value.
     * Each resource maintains a set of cached values for headers, this
     * allows for a nice sped-up in headers marshalling, which - as the 
     * complexity of the protocol increases - becomes a bottleneck.
     */

    protected void updateCachedHeaders() {
	super.updateCachedHeaders();
	if (comment == null) {
	    comment = getMetadata();
	}
    }

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
	super.attributeChanged(evt);
	String name = evt.getAttribute().getName();
	if ((name.equals("file-stamp")) || (name.equals("file-stamp")))
	    comment = null;
    }

    public Reply createCommentReply(Request request, int status) {
	Reply reply = request.makeReply(status);
	updateCachedHeaders();
	reply.setContent(comment);
	reply.setContentType(getCommentType());
	reply.setVary(vary);
	if ( lastmodified != null )
	    reply.setHeaderValue(Reply.H_LAST_MODIFIED, lastmodified);
	if ( contentencoding != null )
	    reply.setHeaderValue(Reply.H_CONTENT_ENCODING,contentencoding);
	if ( contentlanguage != null )
	    reply.setHeaderValue(Reply.H_CONTENT_LANGUAGE,contentlanguage);
	long maxage = getMaxAge();
	if ( maxage >= 0 ) {
	    if (reply.getMajorVersion() >= 1 ) {
		if (reply.getMinorVersion() >= 1) {
		    reply.setMaxAge((int) (maxage / 1000));
		} 
		// If max-age is zero, say what you mean:
		long expires = (System.currentTimeMillis()
				+ ((maxage == 0) ? -1000 : maxage));
		reply.setExpires(expires);
	    }
	}
	// Set the date of the reply (round it to secs):
	reply.setDate((System.currentTimeMillis() / 1000L) * 1000L);
	reply.setETag(getComETag());
	String commenttype = getCommentType().toString();
	reply.setContentLocation(getURL(request).toExternalForm() 
				 + ";" + URLEncoder.encode(commenttype));
	return reply;
    }

    public Reply createCommentReply(Request request) {
	return createCommentReply(request, HTTP.OK);
    }

    /**
     * Check the <code>If-Match</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */

    public int checkIfMatch(Request request, HttpEntityTag etag) {
	if (fresource != null) {
	    HttpEntityTag tags[] = request.getIfMatch();
	    if ( tags != null ) {
		// Good, real validators in use:
		if ( etag != null ) {
		    // Note: if etag is null this means that the resource has 
		    // changed and has not been even emited since then...
		    for (int i = 0 ; i < tags.length ; i++) {
			HttpEntityTag t = tags[i];
			if (t.getTag().equals(etag.getTag())) {
			    if (t.isWeak() || etag.isWeak()) {
				return COND_WEAK;
			    } else {
				return COND_OK;
			    }
			}
		    }
		}
		return COND_FAILED;
	    }
	}
	return 0;
    }

    /**
     * Check the <code>If-None-Match</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */

    public int checkIfNoneMatch(Request request, HttpEntityTag etag) {
	if (fresource != null) {
	    // Check for an If-None-Match conditional:
	    HttpEntityTag tags[] = request.getIfNoneMatch();
	    if ( tags != null ) {
		if ( etag == null ) {
		    return COND_OK;
		}
		int status = COND_OK;
		for (int i = 0 ; i < tags.length ; i++) {
		    HttpEntityTag t = tags[i];
		    if (t.getTag().equals(etag.getTag())) {
			if (t.isWeak() || etag.isWeak()) {
			    status = COND_WEAK;
			} else {
			    return COND_FAILED;
			}
		    }
		    if (t.getTag().equals("*")) {
			if (fresource != null) {
			    File f = fresource.getFile();
			    if (f.exists()) {
				return COND_FAILED;
			    }
			} else {
			    return COND_FAILED;
			}
		    }
		}
		return status;
	    }
	}
	return 0;
    }

    /**
     * check the validators namely LMT/Etags according to rfc2616 rules
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */
    public int checkValidators(Request request, HttpEntityTag etag) {
	int v_inm = checkIfNoneMatch(request, etag);
	int v_ims = checkIfModifiedSince(request);

	if ((v_inm == COND_OK) || (v_ims == COND_OK)) {
	    return COND_OK;
	}
	if ((v_inm == COND_FAILED) || (v_ims == COND_FAILED)) {
	    return COND_FAILED;
	}
	if ((v_inm == COND_WEAK) || (v_ims == COND_WEAK)) {
	    return COND_OK;
	}
	return 0;
    }

    /**
     * Negotiate.
     * @param request the incomming request.
     * @return true if the client wants the comment, false if the client 
     * wants the image.
     */
    protected boolean negotiate(Request request)
    	throws ProtocolException
    {
	if ( ! request.hasAccept() ) {
	    //return the image
	    return false;
	} else {
	    // The browser has given some preferences:
	    HttpAccept accepts[] = request.getAccept() ;
	    
	    //two content types image/jpeg and comment-type
	    HttpAccept imgAccept = 
		getMatchingAccept(accepts, getContentType());
	    HttpAccept comAccept = 
		getMatchingAccept(accepts, getCommentType());
	    
	    if ((imgAccept != null) &&  (comAccept != null)) {
		// go for best MIME match first
		int matchImg = getContentType().match(imgAccept.getMimeType());
		int matchCom = getCommentType().match(comAccept.getMimeType());

		if (matchImg == matchCom) {
		    // equals, use quality
		    return (imgAccept.getQuality() < comAccept.getQuality());
		} else {
		    return (matchImg < matchCom);
		}
	    } else if (comAccept != null)
		return true;
	    else
		return false;
	}
    }

    protected HttpAccept getMatchingAccept(HttpAccept accepts[],
					   MimeType mime) 
    {
	int jmatch = -1 ;
	int jidx   = -1 ;
	for (int i = 0 ; i < accepts.length ; i++) {
	    try {
		int match = mime.match(accepts[i].getMimeType());
		if ( match > jmatch ) {
		    jmatch = match ;
		    jidx   = i ;
		}
	    } catch (HttpInvalidValueException ivex) {
		// There is a bad acept header here
		// let's be cool and ignore it
		// FIXME we should answer with a Bad Request
	    }
	}
	if (jidx < 0)
	    return null;
	return accepts[jidx];
    }

    /**
     * Perform a HEAD request for the associated FileResource.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply headFileResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (fresource == null) 
	    throw new ResourceException("this frame is not attached to a "+
					    "FileResource. ("+
					    resource.getIdentifier()+")");
	Reply reply = null;
	fresource.checkContent();
	updateCachedHeaders();
	// hack, if ;text/html is there,
	// it will be added at first place of the accept 
	String param = null;
	String sfile = request.getURL().getFile();
	int pos = sfile.indexOf(';');
	if (pos != -1) {
	    param = (String) request.getState("type");
	}
	if (param != null) {
	    HttpAccept acc[] = request.getAccept();
	    HttpAccept newacc[] = null;
	    if (acc != null) {
		newacc = new HttpAccept[acc.length+1];
		System.arraycopy(acc, 0, newacc, 1, acc.length);
	    } else {
		newacc = new HttpAccept[1];
	    }
	    try {
		newacc[0] = HttpFactory.makeAccept(new MimeType(param), 1.1);
		request.setAccept(newacc);
	    } catch (MimeTypeFormatException ex) {
		// not a valid mime type... maybe something else, do not care
	    }
	}
	boolean commentOnly = negotiate(request);
	HttpEntityTag etag = null;
	if (commentOnly)
	    etag = getComETag();
	else
	    etag = getETag();
	// Check validators:
	int cim = checkIfMatch(request, etag);
	if ((cim == COND_FAILED) || (cim == COND_WEAK)) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if ( checkIfUnmodifiedSince(request) == COND_FAILED ) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if (checkValidators(request, etag) == COND_FAILED) {
	    reply = createDefaultReply(request, HTTP.NOT_MODIFIED);
	    reply.setETag(etag);
	    reply.setContentMD5(null);
	    return reply;
	}	
	if (! fresource.getFile().exists()) {
	    return deleteMe(request);
	} else {
	    if (commentOnly) {
		reply = createCommentReply(request);
		reply.setStream((InputStream) null);
	    } else {
		reply = createDefaultReply(request, HTTP.OK);
		reply.setVary(vary);
	    }
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
	    return reply;
	}
    }

    /**
     * Get for FileResource
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getFileResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (fresource == null) 
	    throw new ResourceException("this frame is not attached to a "+
					"FileResource. ("+
					resource.getIdentifier()+")");
	Reply reply = null;
	File file = fresource.getFile() ;
	fresource.checkContent();
	updateCachedHeaders();
	String param = null;
	String sfile = request.getURL().getFile();
	int pos = sfile.indexOf(';');
	if (pos != -1) {
	    param = (String) request.getState("type");
	}
	if (param != null) {
	    HttpAccept acc[] = request.getAccept();
	    HttpAccept newacc[] = null;
	    if (acc != null) {
		newacc = new HttpAccept[acc.length+1];
		System.arraycopy(acc, 0, newacc, 1, acc.length);
	    } else {
		newacc = new HttpAccept[1];
	    }
	    try {
		newacc[0] = HttpFactory.makeAccept(new MimeType(param), 1.1);
		request.setAccept(newacc);
	    } catch (MimeTypeFormatException ex) {
		// not a valid mime type... maybe something else, do not care
	    }
	}
	boolean commentOnly = negotiate(request);
	HttpEntityTag etag = null;
	if (commentOnly)
	    etag = getComETag();
	else
	    etag = getETag();
	// Check validators:
	int cim = checkIfMatch(request, etag);
	if ((cim == COND_FAILED) || (cim == COND_WEAK)) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if ( checkIfUnmodifiedSince(request) == COND_FAILED ) {
	    reply = request.makeReply(HTTP.PRECONDITION_FAILED);
	    reply.setContent("Pre-conditions failed.");
	    reply.setContentMD5(null);
	    return reply;
	}
	if ( checkValidators(request, etag) == COND_FAILED ) {
	    reply = createDefaultReply(request, HTTP.NOT_MODIFIED);
	    reply.setETag(etag);
	    reply.setContentMD5(null);
	    return reply;
	}
	// Does this file really exists, if so send it back
	if ( file.exists() ) {
	    if (commentOnly) {
		reply = createCommentReply(request);
	    } else {
		reply = createFileReply(request);
	    }
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
	    return reply;
	} else {
	    return deleteMe(request);
	}
    }

    /**
     * Allow PUT based only on ETags, otherwise PUT is done on the image itself
     * @see HTTPFrame.putFileResource
     */
    protected Reply putFileResource(Request request)
	throws ProtocolException, ResourceException
    {
	// check if it is the right resource below!
	if (!(fresource instanceof ImageFileResource)) {
	    return super.putFileResource(request);
	}
	Reply reply = null;
	int status = HTTP.OK;
	fresource.checkContent();
	updateCachedHeaders();
	// Is this resource writable ?
	if ( ! getPutableFlag() ) {
	    Reply error = request.makeReply(HTTP.NOT_ALLOWED) ;
	    error.setContent("Method PUT not allowed.") ;
	    throw new HTTPException (error) ;
	}
	HttpEntityTag etag = getComETag();
	// no IfMatch, or no matching ETag, maybe a PUT on the image
	int cim = checkIfMatch(request, etag);
	if ((request.getIfMatch() == null) || 
	    (cim == COND_FAILED) || (cim == COND_WEAK)) {
	    return super.putFileResource(request);
	}
	// check all the others validator

	// Check remaining validators (checking if-none-match is lame
	// as we already require the If-Match
	if ((checkIfNoneMatch(request, etag) == COND_FAILED)
	    || (checkIfModifiedSince(request) == COND_FAILED)
	    || (checkIfUnmodifiedSince(request) == COND_FAILED)) {
	    Reply r = request.makeReply(HTTP.PRECONDITION_FAILED);
	    r.setContent("Pre-condition failed.");
	    return r;
	}
	// Check the request:
	InputStream in = null;
	try {
	    in = request.getInputStream();
	    if ( in == null ) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent ("<p>Request doesn't have a valid content.");
		throw new HTTPException (error) ;
	    }
	} catch (IOException ex) {
	    throw new ClientException(request.getClient(), ex);
	}
	// We do not support (for the time being) put with ranges:
	if ( request.hasContentRange() ) {
	    Reply error = request.makeReply(HTTP.BAD_REQUEST);
	    error.setContent("partial PUT not supported.");
	    throw new HTTPException(error);
	}
	// Check that if some type is provided it doesn't conflict:
	if ( request.hasContentType() ) {
	    MimeType rtype = request.getContentType() ;
	    MimeType type  = getCommentType() ;
	    if ( type == null ) {
		setValue (ATTR_CONTENT_TYPE, rtype) ;
	    } else if ( rtype.match (type) < 0 ) {
		if (debug) {
		    System.out.println("No match between: ["+
				       rtype.toString()+"] and ["+
				       type.toString()+"]");
		}
		Reply error = request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE) ;
		error.setContent ("<p>Invalid content type: "+type.toString());
		throw new HTTPException (error) ;
	    }
	}
	ImageFileResource ifresource = (ImageFileResource) fresource;
	// Write the body back to the file:
	try {
	    // We are about to accept the put, notify client before continuing
	    Client client = request.getClient();
	    if ( client != null  && request.getExpect() != null ) {
		client.sendContinue();
	    }
	    if ( ifresource.newMetadataContent(request.getInputStream()) )
		status = HTTP.CREATED;
	    else
		status = HTTP.NO_CONTENT;
	} catch (IOException ex) {
	    throw new ClientException(request.getClient(), ex);
	}
	if ( status == HTTP.CREATED ) {
	    reply = createCommentReply(request, status);
	    reply.setContent("<P>Resource succesfully created");
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
            // Henrik's fix, create the Etag on 201
	    if (fresource != null) {
		// We only take car eof etag here:
		if ( etag == null ) {
		    reply.setETag(getComETag());
		}
	    }
	    reply.setLocation(getURL(request));
	    reply.setContent ("<p>Entity body saved succesfully !") ;
	} else {
	    reply = createCommentReply(request, status);
	}
	return reply ;
    }	
}
