// CachedResource.java
// $Id: CachedResource.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache;

import java.io.File;

import org.w3c.util.ArrayDictionary;
import org.w3c.util.LRUAble;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.DateAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.PropertiesAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HttpEntityTag;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

import org.w3c.www.mime.MimeType;

import org.w3c.jigsaw.frames.MimeTypeAttribute;

public abstract class CachedResource extends AttributeHolder 
    implements TeeMonitor, LRUAble {

    /**
     * Condition check return code - Condition existed but failed.
     */
    public static final int COND_FAILED = 1;
    /**
     * Condition check return code - Condition existed and succeeded.
     */
    public static final int COND_OK = 2;

    /**
     * The download state of the resource, currently not loaded
     */
    public static final int STATE_NOT_LOADED = 0;

    /**
     * The download state of the resource, complete content
     */
    public static final int STATE_LOAD_COMPLETE = 1;

    /**
     * The download state of the resource, partial content
     */
    public static final int STATE_LOAD_PARTIAL = 2;

    /**
     * The download state of the resource, unknown, probably an HTTP/1.0
     * reply without the Content-Length.
     */
    public static final int STATE_LOAD_UNKNOWN = 3;
    /**
     * The download state of the resource, erroneous, something weird 
     * happened! but at least we know that :)
     */
    public static final int STATE_LOAD_ERROR = 4;

    /**
     * Attribute index - The identifier
     */
    protected static int ATTR_IDENTIFIER = -1;    
    /**
     * Attribute index - The resource content length.
     */
    protected static int ATTR_CONTENT_LENGTH = -1;
    /**
     * Attribute index - The resource current length.
     */
    protected static int ATTR_CURRENT_LENGTH = -1;
    /**
     * Attribute index - The file
     */
    protected static int ATTR_FILE = -1;

    /**
     * Attribute name - The resource content length.
     */
    protected static final String NAME_CONTENT_LENGTH = "content-length";

    /**
     * Attribute name -  The resource current length
     */
    protected static final String NAME_CURRENT_LENGTH = "current-length";

    /**
     * Attribute name - The identifier
     */
    protected static final String NAME_IDENTIFIER     = "id";

    /**
     * Attribute name - The identifier
     */
    protected static final String NAME_FILE           = "file";

    /**
     * Attribute index - The download state
     */
    protected static int ATTR_LOAD_STATE = -1;  
    /**
     * Attribute index - The entity tag (if any) associated with the resource.
     */
    protected static int ATTR_ETAG = -1;
    /**
     * Attribute index - The reply status.
     */
    protected static int ATTR_STATUS = -1;
    /**
     * Attribute index - The Last modified of this resource
     */
    protected static int ATTR_REPLY_LAST_MODIFIED = -1;
    /**
     * Attribute index - The Date of the resource
     */
    protected static int ATTR_DATE = -1;
    /**
     * Attribute index - The Content MD5 of the resource
     */
    protected static int ATTR_CONTENT_MD5 = -1;
    /**
     * Attribute index - The Content Encoding of the resource
     */
    protected static int ATTR_CONTENT_ENCODING = -1;
    /**
     * Attribute index - The Content Language of the resource
     */
    protected static int ATTR_CONTENT_LANGUAGE = -1;
    /**
     * Attribute index - The Location of this resource
     */
    protected static int ATTR_LOCATION = -1;
    /**
     * Attribute index - The Vary of this resource
     */
    protected static int ATTR_VARY = -1;
    /**
     * Attribute index - The extra headers attribute.
     */
    protected static int ATTR_EXTRA_HEADERS = -1;
    /**
     * Attribute index - The request headers used for content negotiation
     *                   as set by the reply Vary header.
     */
    protected static int ATTR_CONNEG_HEADERS = -1;

    static {
	Attribute a = null;
	Class     c = null;
	try {
	    c = Class.forName(
		"org.w3c.www.protocol.http.cache.CachedResource");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}    
	// This resource ntity tag:
	a = new StringAttribute(NAME_IDENTIFIER
				, null
				, Attribute.COMPUTED);
	ATTR_IDENTIFIER = AttributeRegistry.registerAttribute(c, a);
	// Declare the content length attribuite:
	a = new IntegerAttribute(NAME_CONTENT_LENGTH
				 , null
				 , Attribute.COMPUTED);
	ATTR_CONTENT_LENGTH = AttributeRegistry.registerAttribute(c, a);
	// Declare the currentlength attribuite:
	a = new IntegerAttribute(NAME_CURRENT_LENGTH
				 , null
				 , Attribute.COMPUTED);
	ATTR_CURRENT_LENGTH = AttributeRegistry.registerAttribute(c, a);
	// Declare the file attribute
	a = new FileAttribute(NAME_FILE
			      , null
			      , Attribute.COMPUTED);
	ATTR_FILE = AttributeRegistry.registerAttribute(c, a);
	// Declare the download state value
	a = new IntegerAttribute("load-state"
				 , null
				 , Attribute.COMPUTED);
	ATTR_LOAD_STATE = AttributeRegistry.registerAttribute(c, a);
	// This resource entity tag:
	a = new StringAttribute("etag"
				, null
				, Attribute.COMPUTED);
	ATTR_ETAG = AttributeRegistry.registerAttribute(c, a);
	// Declare the status attribute:
	a = new IntegerAttribute("status"
				 , null
				 , Attribute.COMPUTED);
	ATTR_STATUS = AttributeRegistry.registerAttribute(c, a);
	// The last modified attribute:
	a = new DateAttribute("reply-last-modified",
			      null,
			      Attribute.COMPUTED|Attribute.EDITABLE) ;
	ATTR_REPLY_LAST_MODIFIED = AttributeRegistry.registerAttribute(c,a);
	// The last modified attribute:
	a = new DateAttribute("reply-date",
			      null,
			      Attribute.COMPUTED|Attribute.EDITABLE) ;
	ATTR_DATE = AttributeRegistry.registerAttribute(c,a);
	// This resource content-md5
	a = new StringAttribute("content-md5"
				, null
				, Attribute.COMPUTED);
	ATTR_CONTENT_MD5 = AttributeRegistry.registerAttribute(c, a);
	// This resource content encoding
	a = new StringArrayAttribute("content-encoding"
				     , null
				     , Attribute.COMPUTED);
	ATTR_CONTENT_ENCODING = AttributeRegistry.registerAttribute(c, a);
	// This resource content-language
	a = new StringArrayAttribute("content-language"
				     , null
				     , Attribute.COMPUTED);
	ATTR_CONTENT_LANGUAGE = AttributeRegistry.registerAttribute(c, a);
	// This resource location
	a = new StringAttribute("location"
				, null
				, Attribute.COMPUTED);
	ATTR_LOCATION = AttributeRegistry.registerAttribute(c, a);
	// This resource location
	a = new StringArrayAttribute("vary"
				     , null
				     , Attribute.COMPUTED);
	ATTR_VARY = AttributeRegistry.registerAttribute(c, a);
	// The extra headers attribute:
	a = new PropertiesAttribute("headers"
				    , null
				    , Attribute.COMPUTED);
	ATTR_EXTRA_HEADERS = AttributeRegistry.registerAttribute(c, a);
	// The extra headers attribute:
	a = new PropertiesAttribute("conneg"
				    , null
				    , Attribute.COMPUTED);
	ATTR_CONNEG_HEADERS = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The minimal attribute set used to describe a cachedresource without
     * loading it entirely.
     */
    protected static String ATTR_DESCR[] = { 
	NAME_IDENTIFIER, 
	NAME_CURRENT_LENGTH,
	NAME_FILE
    };

    // should we revalidate next time?
    protected boolean invalidated = false;
    // our generation
    protected CacheGeneration generation = null;
    // Cached Entity tag HTTP list value for this resource.
    HttpEntityTag etags[] = null;
    // the extra headers
    protected ArrayDictionary a = null;
    // are we uploading or not?
    protected boolean uploading = false;

    public void notifyTeeFailure(int size) {};
    public void notifyTeeSuccess(int size) {};

    // the filter that is using this resource
    protected CacheFilter filter;
    /**
     * LRU management - previous entry.
     */
    protected LRUAble prev = null;
    /**
     * LRU management - next entry.
     */
    protected LRUAble next = null;

    /**
     * LRU management - Get next node.
     * @return A CvsDirectory instance.
     */

    public LRUAble getNext() {
	return next;
    }

    /**
     * LRU management - Get previous node.
     * @return A CvsDirectory instance.
     */

    public LRUAble getPrev() {
	return prev;
    }

    /**
     * LRU management - Set next node.
     * @return A CvsDirectory instance.
     */

    public synchronized void setNext(LRUAble next) {
	this.next = next;
    }

    /**
     * LRU management - Set previous node.
     * @return A CvsDirectory instance.
     */

    public synchronized void setPrev(LRUAble prev) {
	this.prev = prev;
    }

    /**
     * overrides the default setValue to invalidate the ETag
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_ETAG )
	    etags = null;
    }

    /**
     * returns the current age of this cached resource
     * @return an integer, the current age in seconds
     */
    public abstract int getCurrentAge();

    /**
     * returns the current freshness lifetime of this resource
     * @return a long, the freshness lifetime, in seconds
     */
    public abstract int getFreshnessLifetime();

    /**
     * This methods return the CachedResource matching this request
     * it allows lookup in the cache for alternatives
     * @return a CachedResource depending on the request
     */
    public CachedResource lookupResource(Request request) {
	return this;
    }

    /**
     * Get this cached entry identifier
     * @return a String, usually the URL of the resource
     */
    public String getIdentifier() {
	return (String) getValue(ATTR_IDENTIFIER, null);
    }

    /**
     * Get this cached entry content length.
     * @return An integer, giving the content length, or <strong>-1</strong>
     * if undefined.
     */
    public int getContentLength() {
	return getInt(ATTR_CONTENT_LENGTH, -1);
    }

    /**
     * Set the content length of that cached entry.
     * @param length The new content length of that entry.
     */
    public void setContentLength(int length) {
	setInt(ATTR_CONTENT_LENGTH, length);
    }

    /**
     * Get this cached entry current content length.
     * @return An integer, giving the current content length, or 
     * <strong>-1</strong> if undefined
     */
    public int getCurrentLength() {
	return getInt(ATTR_CURRENT_LENGTH, -1);
    }

    /**
     * Set the current length of that cached entry.
     * @param length The current length of that entry.
     */
    public void setCurrentLength(int length) {
	setInt(ATTR_CURRENT_LENGTH, length);
    }

    /**
     * Get the load state value
     * @return an integer, as defined in CachedResource
     * @see org.w3c.www.protocol.http.cache.CachedResource
     * The default is STATE_NOT_LOADED
     */
    public int getLoadState() {
	return getInt(ATTR_LOAD_STATE, STATE_NOT_LOADED);
    }

    /**
     * Set the loading state of this resource
     * @param an integer, one of the state defined in CachedResource
     * @see org.w3c.www.protocol.http.cache.CachedResource
     */
    public void setLoadState(int state) {
	setInt(ATTR_LOAD_STATE, state);
    }

    /**
     * Get the HTTP status of that cached entry.
     * @return An integer HTTP status code, or <strong>-1</strong> if 
     * undefined.
     */
    public int getStatus() {
	return getInt(ATTR_STATUS, -1);
    }

    /**
     * Set the reply status for that entry.
     * @param status The HTTP status code of that entry, or <strong>-1</strong>
     * to undefine the previous setting.
     */
    public void setStatus(int status) {
	setInt(ATTR_STATUS, status);
    }

    /**
     * Get this Cached Resource last modification time.
     * @return A long giving the date of the last modification time, or
     *    <strong>-1</strong> if undefined.
     */
    public long getLastModified() {
	return getLong(ATTR_REPLY_LAST_MODIFIED, (long) -1) ;
    }

    /**
     * Set the last modified time of that cached entry.
     * @param lastmodified The last modification date as a number of 
     * milliseconds since Java epoch, or <strong>-1</strong> to undefine
     * previous setting.
     */
    public void setLastModified(long lastmodified) {
	setLong(ATTR_REPLY_LAST_MODIFIED, lastmodified);
    }

    /**
     * Get the Content-Type of the cached resource of <code>null</code> if
     * there is no mime type (it should NEVER happen!)
     * @return a MimeType
     */
    public abstract MimeType getContentType();

    /**
     * Set the Content-Type of this cached resource
     * @param a MimeType, the mime type of this resource
     */
    public abstract void setContentType(MimeType type);

    /**
     * Get state of the resource, did someone ask for revalidation for
     * the next request?
     * @return a boolean, <code>true</code> if it will.
     * -1</strong> if undefined.
     */
    public boolean getWillRevalidate() {
	return invalidated;
    }

    /**
     * Set this cached entry revalidate-on-next-request flag
     * @param validate, a boolean, <code>true</code> if it will be revalidated
     * next time.
     */
    public void setWillRevalidate(boolean invalidated) {
	this.invalidated = invalidated;
    }

    /**
     * Get this date, as a long
     * @return a long, the date
     * if undefined.
     */
    public long getDate() {
	return getLong(ATTR_DATE, -1);
    }

    /**
     * Set the content length of that cached entry.
     * @param length The new content length of that entry.
     */
    public void setDate(long date) {
	setLong(ATTR_DATE, date);
    }

    /**
     * Set the cached file
     * @param file
     */
    public void setFile(File file) {
	setValue(ATTR_FILE, file);
    }

    /**
     * Get the cached File.
     * @return a File instance
     */
    public File getFile() {
	return (File)getValue(ATTR_FILE, null);
    }

    /**
     * Get the entity tag associated with that cached entry.
     * @return The String encoded entity tag, or <strong>null</strong> if 
     * undefined.
     */
    public String getETag() {
	return getString(ATTR_ETAG, null);
    }

    /**
     * Associate an entity tag with that cached enrty.
     * @param etag The entity tag of the entry, or <strong>null</strong>
     * to reset the value.
     */
    public void setETag(String etag) {
	setValue(ATTR_ETAG, etag);
    }

    /**
     * Get the Content-MD5 associated with that cached entry.
     * @return The String encoded Content-MD5, or <strong>null</strong> if 
     * undefined.
     */
    public String getContentMD5() {
	return getString(ATTR_CONTENT_MD5, null);
    }

    /**
     * Associate a Content-MD5 with that cached enrty.
     * @param sum, the md5 sum as a string, see RFC2616, 
     * or <strong>null</strong>
     * to reset the value.
     */
    public void setContentMD5(String sum) {
	setValue(ATTR_CONTENT_MD5, sum);
    }

    /**
     * Get the Content-Encoding associated with that cached entry.
     * @return The String Content-Encoding, or <strong>null</strong> if 
     * undefined.
     */
    public String[] getContentEncoding() {
	return (String[]) getValue(ATTR_CONTENT_ENCODING, null);
    }

    /**
     * Associate a Content-Encoding with that cached enrty.
     * @param sum, the encoding as a string,
     * or <strong>null</strong>
     * to reset the value.
     */
    public void setContentEncoding(String[] sum) {
	setValue(ATTR_CONTENT_ENCODING, sum);
    }

    /**
     * Get the Content-Language associated with that cached entry.
     * @return The String Content-Language, or <strong>null</strong> if 
     * undefined.
     */
    public String[] getContentLanguage() {
	return (String[]) getValue(ATTR_CONTENT_LANGUAGE, null);
    }

    /**
     * Associate a Content-Language with that cached enrty.
     * @param sum, the encoding as a string,
     * or <strong>null</strong>
     * to reset the value.
     */
    public void setContentLanguage(String[] language) {
	setValue(ATTR_CONTENT_LANGUAGE, language);
    }

    /**
     * Get the Vary associated with that cached entry.
     * @return The String array of Vary, or <strong>null</strong> if 
     * undefined.
     */
    public String[] getVary() {
	return (String[]) getValue(ATTR_VARY, null);
    }

    /**
     * Associate a Vary with that cached enrty.
     * @param sum, the header involved in the variant check as a string array,
     * or <strong>null</strong>
     * to reset the value.
     */
    public void setVary(String[] vary) {
	setValue(ATTR_VARY, vary);
    }

    /**
     * Get the extra headers stored for that resource.
     * @return An ArrayDictionary with the extra headers, or <strong>null
     * </strong> if undefined.
     */
    public ArrayDictionary getExtraHeaders() {
	return (ArrayDictionary) getValue(ATTR_EXTRA_HEADERS, null);
    }

    /**
     * Set a new set of extra headers for that resource.
     * @param headers The new set of headers.
     */
    public void setExtraHeaders(ArrayDictionary a) {
	setValue(ATTR_EXTRA_HEADERS, a);
    }

    /**
     * Get the extra headers stored for that resource.
     * @return An ArrayDictionary with the extra headers, or <strong>null
     * </strong> if undefined.
     */
    public ArrayDictionary getConnegHeaders() {
	return (ArrayDictionary) getValue(ATTR_CONNEG_HEADERS, null);
    }

    /**
     * Set a new set of extra headers for that resource.
     * @param headers The new set of headers.
     */
    public void setConnegHeaders(ArrayDictionary a) {
	setValue(ATTR_CONNEG_HEADERS, a);
    }

    /**
     * Delete this resource (and its associated file).
     * @return the number of bytes saved.
     */
    public long delete() {
	File file = getFile();
	if (file != null)
	    file.delete();
	return getCurrentLength();
    }

    /**
     * This cached entry has been checked valid, perform given request.
     * @param request The request to perform.
     * @return An Reply instance.
     * @exception HttpException If something went wrong.
     */
    public abstract Reply perform(Request request)
	throws HttpException;

    /**
     * This cached entry needs revalidation, it will modify the 
     * request to do that.
     */
    public abstract Request setRequestRevalidation(Request request);

}
