// HttpEntityMessage.java
// $Id: HttpEntityMessage.java,v 1.1 2010/06/15 12:19:52 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import org.w3c.www.mime.MimeParser;
import org.w3c.www.mime.MimeType;

public class HttpEntityMessage extends HttpMessage {

    // HTTP Entity message well-known headers
    public static int H_ALLOW               = 19;
    public static int H_CONTENT_LENGTH      = 20;
    public static int H_CONTENT_BASE        = 21;
    public static int H_CONTENT_ENCODING    = 22;
    public static int H_CONTENT_LANGUAGE    = 23;
    public static int H_CONTENT_LOCATION    = 24;
    public static int H_CONTENT_MD5         = 25;
    public static int H_CONTENT_RANGE       = 26;
    public static int H_CONTENT_TYPE        = 27;
    public static int H_ETAG                = 28;
    public static int H_EXPIRES             = 29;
    public static int H_LAST_MODIFIED       = 30;

    static {
	registerHeader("Allow"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_ALLOW);
	registerHeader("Content-Length"
		       , "org.w3c.www.http.HttpInteger"
		       , H_CONTENT_LENGTH);
	registerHeader("Content-Base"
		       , "org.w3c.www.http.HttpString"
		       , H_CONTENT_BASE);
	registerHeader("Content-Encoding"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_CONTENT_ENCODING);
	registerHeader("Content-Language"
		       , "org.w3c.www.http.HttpTokenList"
		       , H_CONTENT_LANGUAGE);
	registerHeader("Content-Location"
		       , "org.w3c.www.http.HttpString"
		       , H_CONTENT_LOCATION);
	registerHeader("Content-Md5"
		       , "org.w3c.www.http.HttpString"
		       , H_CONTENT_MD5);
	registerHeader("Content-Range"
		       , "org.w3c.www.http.HttpContentRange"
		       , H_CONTENT_RANGE);
	registerHeader("Content-Type"
		       , "org.w3c.www.http.HttpMimeType"
		       , H_CONTENT_TYPE);
	registerHeader("Etag"
		       , "org.w3c.www.http.HttpEntityTag"
		       , H_ETAG);
	registerHeader("Expires"
		       , "org.w3c.www.http.HttpDate"
		       , H_EXPIRES);
	registerHeader("Last-Modified"
		       , "org.w3c.www.http.HttpDate"
		       , H_LAST_MODIFIED);
    }

    /**
     * Get the message's entity allowed methods.
     * @return The list of allowed methods, encoded as a String array, or
     * <strong>null</strong> if undefined.
     */

    public String[] getAllow() {
	HeaderValue value = getHeaderValue(H_ALLOW);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set this message's entity allowed methods.
     * @param mth A list of allowed methods, encoded as a String array, or
     * <strong>null</strong> to reset the value.
     */

    public void setAllow(String mth[]) {
	setHeaderValue(H_ALLOW
		       , ((mth == null) ? null : new HttpTokenList(mth)));
    }

    /**
     * Get this message entity base.
     * @return A String encoding the content base, or <strong>null</strong>
     *    if undefined.
     */

    public String getContentBase() {
	HeaderValue value = getHeaderValue(H_CONTENT_BASE);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set this message entity content base.
     * @param base The base for the entity, encoded as a String, or
     * <strong>null</strong> to reset the value.
     */

    public void setContentBase(String base) {
	setHeaderValue(H_CONTENT_BASE
		       , ((base == null) ? null : new HttpString(true, base)));
    }

    /**
     * Get this message entity encoding.
     * @return A list of encoding tokens, encoded as a String array, or 
     *    <strong>null</strong> if undefined.
     */

    public String[] getContentEncoding() {
	HeaderValue value = getHeaderValue(H_CONTENT_ENCODING);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set this message entity content encoding.
     * @param encodings A list of encoding tokens, encoded as a String array
     * or <strong>null</strong> to reset the value.
     */

    public void setContentEncoding(String encodings[]) {
	setHeaderValue(H_CONTENT_ENCODING
		       , ((encodings == null)
			  ? null
			  : new HttpTokenList(encodings)));
    }

    /**
     * Add an encoding token to the given reply stream (ie the body).
     * @param name The name of the encoding to add.
     */

    public void addContentEncoding(String name) {
	HttpTokenList l = (HttpTokenList) getHeaderValue(H_CONTENT_ENCODING);
	if ( l == null ) {
	    String sList[] = new String[1];
	    sList[0]       = name.toLowerCase();
	    setHeaderValue(H_CONTENT_ENCODING, new HttpTokenList(sList));
	} else {
	    l.addToken(name, false);
	}
    }

    /**
     * Get this message entity content language.
     * @return A list of languages token, encoded as a String arry, or
     *    <strong>null</strong> if undefined.
     */

    public String[] getContentLanguage() {
	HeaderValue value = getHeaderValue(H_CONTENT_LANGUAGE);
	return (value != null) ? (String[]) value.getValue() : null;
    }

    /**
     * Set this message entity content language.
     * @param languages The language tokens for this entity, encoded as
     *    a String array, or <strong>null</strong> to reset the value.
     */

    public void setContentLanguage(String languages[]) {
	setHeaderValue(H_CONTENT_LANGUAGE
		       , ((languages == null)
			  ? null
			  : new HttpTokenList(languages)));
    }

    /**
     * Get the content length of the message.
     * @return The content length for the message entity, or <strong>-1
     *    </strong> if undefined.
     */

    public int getContentLength() {
	HeaderValue value = getHeaderValue(H_CONTENT_LENGTH);
	return (value != null) ? ((Integer) value.getValue()).intValue() : -1;
    }

    /**
     * Set this message entity content-length.
     * @param length The new content length for this message, or
     * <strong>-1</strong> to reset the value.
     */

    public void setContentLength(int length) {
	if ( length < 0 )
	    setHeaderValue(H_CONTENT_LENGTH, null);
	else
	    setHeaderValue(H_CONTENT_LENGTH, new HttpInteger(true, length));
    }

    /**
     * Get the attached entity's content location.
     * @return A String encoded value of the content location, or <strong>
     *    null</strong> if undefined.
     */

    public String getContentLocation() {
	HeaderValue value = getHeaderValue(H_CONTENT_LOCATION);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the entity's content location.
     * @param location The String encoding the content location for the 
     *    attached entity, or <strong>null</strong> to reset the value.
     */

    public void setContentLocation(String location) {
	setHeaderValue(H_CONTENT_LOCATION
		       , ((location == null) 
			  ? null
			  : new HttpString(true, location)));
    }

    /**
     * Get the entity's content MD5 checksum.
     * @return A String giving the base64 encoded MD% checksun on the
     * entity body, or <strong>null</strong> if undefined.
     */

    public String getContentMD5() {
	HeaderValue value = getHeaderValue(H_CONTENT_MD5);
	return (value != null) ? (String) value.getValue() : null;
    }

    /**
     * Set the entity's content MD5 checksum.
     * @param md5 The new base64 encoded checksum for the entity body, or
     * <strong>null</strong> to reset the value.
     */

    public void setContentMD5(String md5) {
	setHeaderValue(H_CONTENT_MD5 
		       , ((md5 == null) ? null : new HttpString(true, md5)));
    }

    /**
     * Get the range descriptor of the attached entity.
     * @return An HttpRange instance, describing the part of the full
     * entity body being transmited, or <strong>null</strong> if undefined.
     */

    public HttpContentRange getContentRange() {
	HeaderValue value = getHeaderValue(H_CONTENT_RANGE);
	return (value != null) ? (HttpContentRange) value.getValue() : null;
    }

    /**
     * Is this entity only a partial entity ?
     * @return A boolean, indicating if a <code>Content-Range</code> header
     * was present.
     */

    public boolean hasContentRange() {
	return hasHeader(H_CONTENT_RANGE);
    }

    /**
     * Get the entity MIME type.
     * @return An HttpMimeType object describing the entity's type, or 
     * <strong>null</strong> if udefined.
     */

    public MimeType getContentType() {
	HeaderValue value = getHeaderValue(H_CONTENT_TYPE);
	return (value != null) ? (MimeType) value.getValue() : null;
    }

    /**
     * Set the entity MIME type.
     * @param type The entity MIME type, or <strong>null</strong> to unset
     * the entity's content type.
     */

    public void setContentType(MimeType type) {
	setHeaderValue(H_CONTENT_TYPE
		       , (type == null) ? null : new HttpMimeType(true, type));
    }

    /**
     * Get this entity tag.
     * @return An HttpEntityTag instance describing this entity tag.
     */

    public HttpEntityTag getETag() {
	HeaderValue value = getHeaderValue(H_ETAG);
	return (value != null) ? (HttpEntityTag) value.getValue() : null;
    }

    /**
     * Set this entity tag.
     * @param tag The new entity tag, or <strong>null</strong> to reset the
     * value.
     */

    public void setETag(HttpEntityTag tag) {
	setHeaderValue(H_ETAG, tag);
    }

    /**
     * Get this message's entity expires date.
     * @return A long giving the date as the number of milliseconds since the
     * Java epoch, or <strong>-1</strong> if undefined.
     */

    public long getExpires() {
	HeaderValue value = getHeaderValue(H_EXPIRES);
	return (value != null) ? ((Long) value.getValue()).longValue() : -1;
    }

    /**
     * Se the message's associated entity exxpires date.
     * @param date The date as the number of milliseconds since Java epoch,
     * or <strong>-1</strong> to reset the value.
     */

    public void setExpires(long date) {
	setHeaderValue(H_EXPIRES
		       , ((date == -1) ? null : new HttpDate(true, date)));
    }

    /**
     * Get the message's associated entity last modified time.
     * @return A long giving the date as the number of milliseconds since
     * Java epoch, or  <strong>-1</strong> if undefined.
     */

    public long getLastModified() {
	HeaderValue value = getHeaderValue(H_LAST_MODIFIED);
	return (value != null) ? ((Long) value.getValue()).longValue() : -1;
    }

    /**
     * Set the message's entity header last-modified time.
     * @param date The date of last modification, as the number of milliseconds
     * since Java epoch, or <strong>-1</strong> to reset the value.
     */

    public void setLastModified(long date) {
	setHeaderValue(H_LAST_MODIFIED
		       , ((date == -1) ? null : new HttpDate(true, date)));
    }

    public HttpEntityMessage(MimeParser parser) {
	super(parser);
    }

    public HttpEntityMessage() {
	super();
    }

}
