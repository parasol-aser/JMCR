// HTTPFrame.java
// $Id: HTTPFrame.java,v 1.1 2010/06/15 12:24:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.tools.codec.Base64Encoder;
import org.w3c.tools.sorter.Sorter;
import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.DoubleAttribute;
import org.w3c.tools.resources.StringAttribute;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.ProtocolFrame;
import org.w3c.tools.resources.ProtocolFrame;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.ClientException;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.HTTPException;

import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.html.HtmlLink;
import org.w3c.www.mime.MimeType;
import org.w3c.www.http.ByteRangeOutputStream;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpContentRange;
import org.w3c.www.http.HttpDate;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInteger;
import org.w3c.www.http.HttpMimeType;
import org.w3c.www.http.HttpRange;
import org.w3c.www.http.HttpString;
import org.w3c.www.http.HttpTokenList;

import org.w3c.tools.crypt.Md5;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * Default class to handle the HTTP protocol, manage FileResource and
 * DirectoryResource.
 */
public class HTTPFrame extends ProtocolFrame {

    public static final 
    String STATE_CONTENT_LOCATION = "org.w3c.jigsaw.frames.HTTPFrame.cl";

    private static final boolean debug = false;

    /**
     * Condition check return code - Condition existed but failed.
     */
    public static final int COND_FAILED = 1;
    /**
     * Condition check return code - Condition existed and succeeded.
     */
    public static final int COND_OK = 2;
    /**
     * Condition check return code - Condition existed and succeeded 
     *                               but is a weak validation.
     */
    public static final int COND_WEAK = 3;    

    private   static HttpTokenList _accept_ranges  = null;
    static {
	String accept_ranges[] = { "bytes" };
	_accept_ranges = HttpFactory.makeStringList(accept_ranges);
    }
    /**
     * Methods allowed by instances of that class in particular:
     */
    protected        HttpTokenList  allowed = null;

    /**
     * Attributes index - The index for the quality attribute.
     */
    protected static int ATTR_QUALITY = -1 ;
    /**
     * Attribute index - The index for the title attribute.
     */
    protected static int ATTR_TITLE = -1 ;
    /**
     * Attribute index - The index for the content languages attribute.
     */
    protected static int ATTR_CONTENT_LANGUAGE = -1 ;
    /**
     * Attribute index - The index for the content encodings attribute.
     */
    protected static int ATTR_CONTENT_ENCODING = -1 ;
    /**
     * Attribute index - The index for the content type attribute.
     */
    protected static int ATTR_CONTENT_TYPE = -1 ;
    /**
     * Attribute index - The index for the charset attribute.
     */
    protected static int ATTR_CHARSET = -1 ;
    /**
     * Attribute index - The index for the content length attribute.
     */
    protected static int ATTR_CONTENT_LENGTH = -1 ;
    /**
     * Attribute index - The icon (if any) associated to the resource.
     */
    protected static int ATTR_ICON = -1 ;
    /**
     * Attribute index - Max age: the maximum drift allowed from reality.
     */
    protected static int ATTR_MAXAGE = -1 ;
    /**
     * Attribute index - Send MD5 Digest: the md5 digest of the resource sent
     */
    protected static int ATTR_MD5 = -1;
    /**
     * Attribute index - delete allowed for the associated resource ?
     */
    protected static int ATTR_ALLOW_DEL = -1;

    //
    // Attribute relative to FileResource
    //

    /**
     * Attribute index - Do we allow PUT method on this file.
     */
    protected static int ATTR_PUTABLE = -1 ;

    //
    // Attribute relative to DirectoryResource
    //

    /**
     * Attribute index - The index for our relocate attribute.
     */
    protected static int ATTR_RELOCATE = -1 ;
    /**
     * Attribute index - our index resource name.
     */
    protected static int ATTR_INDEX = -1 ;
    /**
     * Attribute index - our indexes resource name.
     */
    protected static int ATTR_INDEXES = -1 ;
    /**
     * Attribute index - The icon directory to use in dir listing.
     */
    protected static int ATTR_ICONDIR = -1 ;
    /**
     * Attribute index - Allow the GNN browse method.
     */
    protected static int ATTR_BROWSABLE = -1 ;
    /**
     * Attribute index - Style sheet for directory listing
     */
    protected static int ATTR_STYLE_LINK = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;

	// Get a pointer to our class:
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.HTTPFrame") ;
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The quality attribute:
	a = new DoubleAttribute("quality"
				, new Double(1.0) 
				, Attribute.EDITABLE);
	ATTR_QUALITY = AttributeRegistry.registerAttribute(cls, a) ;
	// The title attribute:
	a = new StringAttribute("title"
				, null
				, Attribute.EDITABLE) ;
	ATTR_TITLE = AttributeRegistry.registerAttribute(cls, a) ;
	// The content language attribute:
	a = new LanguageAttribute("content-language"
				  , null
				  , Attribute.EDITABLE) ;
	ATTR_CONTENT_LANGUAGE = AttributeRegistry.registerAttribute(cls,a);
	// The content encoding attribute:
	a = new EncodingAttribute("content-encoding"
				  , null
				  , Attribute.EDITABLE) ;
	ATTR_CONTENT_ENCODING = AttributeRegistry.registerAttribute(cls,a);
	// The content type attribute:
	a = new MimeTypeAttribute("content-type"
				  , null
				  , Attribute.EDITABLE) ;
	ATTR_CONTENT_TYPE = AttributeRegistry.registerAttribute(cls,a);
	// The Charset attribute:
	a = new StringAttribute("charset"
				, null
				, Attribute.EDITABLE) ;
	ATTR_CHARSET = AttributeRegistry.registerAttribute(cls,a);
        // The content length attribute:
	a = new IntegerAttribute("content-length"
				 , null
				 , Attribute.COMPUTED);
	ATTR_CONTENT_LENGTH = AttributeRegistry.registerAttribute(cls,a);
	// The icon attribute:
	a = new StringAttribute("icon"
				, null
				, Attribute.EDITABLE) ;
	ATTR_ICON = AttributeRegistry.registerAttribute(cls, a) ;
	// The max age attribute (in ms)
	a = new LongAttribute("maxage"
			      , null
			      , Attribute.EDITABLE) ;
	ATTR_MAXAGE = AttributeRegistry.registerAttribute(cls, a) ;
	// Should we send MD5 digest?
	a = new BooleanAttribute("send-md5"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_MD5 = AttributeRegistry.registerAttribute(cls, a) ;
	// delete allowed for the associated resource ?
	a = new BooleanAttribute("allow-delete"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_ALLOW_DEL = AttributeRegistry.registerAttribute(cls, a) ;

	//
	// Attribute relative to a FileResource
	//

	// The putable flag:
	a = new BooleanAttribute("putable"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_PUTABLE = AttributeRegistry.registerAttribute(cls, a) ;

	//
	// Attribute relative to a DirectoryResource
	//

	//Should we relocate invalid request to this directory ?
	a = new BooleanAttribute("relocate"
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	ATTR_RELOCATE = AttributeRegistry.registerAttribute(cls, a) ;
	// Our index resource name (optional).
	a = new StringAttribute("index"
				, null
				, Attribute.EDITABLE) ;
	ATTR_INDEX = AttributeRegistry.registerAttribute(cls, a) ;
	// Our indexes resource name 
	a = new StringArrayAttribute("indexes"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_INDEXES = AttributeRegistry.registerAttribute(cls, a) ;
	// Our icon directory.
	a = new StringAttribute("icondir"
				, null
				, Attribute.EDITABLE) ;
	ATTR_ICONDIR = AttributeRegistry.registerAttribute(cls,a);
	// The browsable flag:
	a = new BooleanAttribute("browsable"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_BROWSABLE = AttributeRegistry.registerAttribute(cls, a) ;
	// The style sheet attribute:
	a = new StringAttribute("style-sheet-link"
				, null
				, Attribute.EDITABLE) ;
	ATTR_STYLE_LINK = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * The associated DirectoryResource (if any)
     */
    protected DirectoryResource dresource  = null;

    /**
     * The associated FileResource (if any)
     */
    protected FileResource      fresource  = null;

    /**
     * Register this frame to the given resource.
     * @param resource The resource associated with this frame.
     */
    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	if (resource instanceof FileResource)
	    fresource = (FileResource) resource;
	else if (resource instanceof DirectoryResource)
	    dresource = (DirectoryResource) resource;
    }

    /**
     * Get the associated FileResource (if any)
     * @return a FileResource instance or <strong>null</strong>
     * if no FileResource is associated with this frame.
     */
    public FileResource getFileResource() {
	return fresource;
    }

    /**
     * Get the associated DirectoryResource (if any)
     * @return a DirectoryResource instance or <strong>null</strong>
     * if no DirectoryResource is associated with this frame.
     */
    public DirectoryResource getDirectoryResource() {
	return dresource;
    }

    /**
     * use this one instead of registerResource if the resource type 
     * doesn't matter or if this is not a file or a directory resource.
     * In subclasses you should have to do that:
     * <pre>
     *  public void registerResource(FramedResource resource) {
     *   super.registerOtherResource(resource);
     *  }
     * </pre>
     * @param the resource to register.
     */
    public void registerOtherResource(FramedResource resource) {
	super.registerResource(resource);
	dresource = null;
	fresource = null;
    }

    // The HTTPResource keeps a cache of ready to use Http values. This 
    // allows to save converting to/from wire rep these objects. Not 
    // much CPU time, but also memory is spared.
    HttpMimeType  contenttype     = null;
    HttpInteger   contentlength   = null;
    HttpDate      lastmodified    = null;
    HttpTokenList contentencoding = null;
    HttpTokenList contentlanguage = null;

    // The Http entity tag for this resource (for FileResource only)
    HttpEntityTag etag   = null;
    // the MD5 digest for this resource (for FileResource only)
    HttpString md5Digest = null;

    /**
     * Get this resource's help url.
     * @return An URL, encoded as a String, or <strong>null</strong> if not
     * available.
     */

    public String getHelpURL() {
	httpd server = (httpd) getServer();
	if ( server == null ) 
	    return null;
	String docurl = server.getDocumentationURL();
	if ( docurl == null )
	    return null;
	return docurl + "/" + getClass().getName() + ".html";
    }

    /**
     * Get the help URL for that resource's attribute.
     * @param topic The topic (can be an attribute name, or a property, etc).
     * @return A String encoded URL, or <strong>null</strong>.
     */

    public String getHelpURL(String topic) {
	httpd server = (httpd) getServer();
	if ( server == null ) 
	    return null;
	String docurl = server.getDocumentationURL();
	if ( docurl == null )
	    return null;
	Class defines = AttributeRegistry.getAttributeClass(getClass(), topic);
	if ( defines != null ) 
	    return docurl + "/" + defines.getName() + ".html";
	return null;
    }

    /** 
     * give the md5 digest from cache or calculate it
     * @return the HttpString version of the digest
     */

    private HttpString getMd5Digest() {
	if (md5Digest != null)
	    return md5Digest;
	// not found, compute it if necessary!
	Resource r = getResource();
	if (r instanceof FileResource) {
	    try {
		Md5 md5 = new Md5 (
		    new FileInputStream(((FileResource)r).getFile()));
		String s = null;
		try {
		    byte b[] = md5.getDigest();
		    Base64Encoder b64;
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    b64 = new Base64Encoder(new ByteArrayInputStream(b), bos);
		    b64.process();
		    s = bos.toString();
		    md5Digest = HttpFactory.makeString(s);
		} catch (Exception mdex) {
		    // error, set it to null
		    md5Digest = null;
		}
		return md5Digest;
	    } catch (FileNotFoundException ex) {
		// silent fail
		md5Digest = null;
	    }
	}
	return null;
    }

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
	super.attributeChanged(evt);
	String name = evt.getAttribute().getName();
	if (name.equals("file-stamp")) {
	    etag = null;
	    lastmodified = null;
	    md5Digest = null;
	} else if (name.equals("file-length")) {
	    setValue(ATTR_CONTENT_LENGTH, evt.getNewValue());
	} else if (name.equals("last-modified")) {
	    setValue(ATTR_LAST_MODIFIED, evt.getNewValue());
	} else {
	    lastmodified = null;
	}
    }

    /**
     * Catch setValue, to maintain cached header values correctness.
     * @param idx The index of the attribute to be set.
     * @param value The new value for the attribute.
     */

    public synchronized void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (idx == ATTR_CONTENT_TYPE) {
	    contenttype = null;
	} else if (idx == ATTR_CHARSET) {
	    contenttype = null;
	} else if (idx == ATTR_CONTENT_LENGTH) {
	    contentlength = null;
	} else if ( idx == ATTR_CONTENT_ENCODING ) {
	    contentencoding = null;
	} else if ( idx == ATTR_CONTENT_LANGUAGE ) {
	    contentlanguage = null;
	} else if ( idx == ATTR_PUTABLE) {
	    allowed = null;
	} else if (idx == ATTR_ALLOW_DEL) {
	    allowed = null;
	} else if (idx == ATTR_MD5) {
	    md5Digest = null; // reset the digest state
	}
	// Any attribute setting modifies the last modified time:
	lastmodified = null;
    }

    /**
     * Get the full URL for that resource.
     * @return An URL instance.
     */
    public URL getURL(Request request) {
	try {
	    return new URL(request.getURL(), resource.unsafeGetURLPath());
	} catch (MalformedURLException ex) {
	    throw new RuntimeException("unable to build "+
				       getURLPath()+
				       " full URL, from server "+
				       getServer().getURL());
	}
    }

    /**
     * Get this resource quality.
     * @return The resource quality, or some negative value if not defined.
     */

    public double getQuality() {
	return getDouble(ATTR_QUALITY, -1.0) ;
    }

    /**
     * Get this resource quality.
     * @return The resource quality, or some negative value if not defined.
     */

    public double unsafeGetQuality() {
	return unsafeGetDouble(ATTR_QUALITY, -1.0) ;
    }

    /**
     * Get this resource title.
     * @return This resource's title, or <strong>null</strong> if not 
     *    defined.
     */

    public String getTitle() {
	return getString(ATTR_TITLE, null) ;
    }

    /**
     * Get this resource content language.
     * Language are stored as a comma separated String of tokens.
     * @return A comma separated string of language tokens, or
     *    <strong>null</strong> if undefined.
     */

    public String getContentLanguage() {
	return (String) getValue(ATTR_CONTENT_LANGUAGE, null) ;
    } 

    /**
     * Get this resource content encoding.
     * The content encoding of a resource is stored as a comma separated
     * list of tokens (as decribed in the Content_encoding header of the
     * HTTP specification, and in the order they should appear in the header).
     * @return A string of comma separated encoding tokens, or
     *    <strong>null</strong> if not defined.
     */

    public String getContentEncoding() {
	String def = (String) attributes[ATTR_CONTENT_ENCODING].getDefault();
	String s =  (String) getString (ATTR_CONTENT_ENCODING, def) ;
	return (String) getString (ATTR_CONTENT_ENCODING, def) ;
    }

    /**
     * Get this resource charset.
     * @return A String, or <strong>null</strong> if not
     *    defined.
     */
    public String getCharset() {
	return (String) getValue(ATTR_CHARSET, null);
    }

    /**
     * Get this resource content type.
     * @return An instance of MIMEType, or <strong>null</strong> if not
     *    defined.
     */
    public MimeType getContentType() {
	return (MimeType) getValue(ATTR_CONTENT_TYPE, null);
    }

    /**
     * Compute the ETag string
     * @return a string or null if not applicable
     */ 
    public String computeETag() {
	String etag_s = null;
	if (fresource != null) {
	    long lstamp = fresource.getFileStamp();
	    if ( lstamp >= 0L ) {
		StringBuffer sb = new StringBuffer(32);
		sb.append(Integer.toString(getOid(), 32));
		sb.append(':');
		sb.append(Long.toString(lstamp, 32));
		etag_s = sb.toString();
	    }
	} 
	return etag_s;
    }

    /**
     * Get this resource Etag
     * @return an instance of HttpEntityTag, or <strong>null</strong> if not
     *    defined.
     */

    public HttpEntityTag getETag() {
	if (etag == null) {
	    String etag_s = computeETag();
	    // no luck, exit
	    if (etag_s == null) {
		return null;
	    }
	    etag = HttpFactory.makeETag(false, etag_s);
	}
	return etag;
    }

    /**
     * Get this resource content length.
     * @return The resource content length, or <strong>-1</strong> if not
     *    defined.
     */

    public int getContentLength() {
	return getInt(ATTR_CONTENT_LENGTH, -1) ;
    }

    /**
     * Get this resource's icon.
     */

    public String getIcon() {
	return getString(ATTR_ICON, null) ;
    }

    /**
     * Get this resource's max age.
     * The max age of a resource indicates how much drift is allowed between
     * the physicall version of the resource, and any in-memory cached version
     * of it.
     * <p>The max age attribute is a long number giving the number of 
     * milliseconds of allowed drift.
     */

    public long getMaxAge() {
	return getLong(ATTR_MAXAGE, (long) -1) ;
    }

    //
    // Relative to FileResource ...
    //

    /**
     * Does this resource support byte ranges.
     */
    protected boolean acceptRanges = false;

    /**
     * Get the PUT'able flag (are we allow to PUT to the resource ?)
     */
    public boolean getPutableFlag() {
	return getBoolean(ATTR_PUTABLE, false) ;
    }

    /**
     * Do we send the MD5 digest?
     */
    public boolean getMD5Flag() {
	return getBoolean(ATTR_MD5, false) ;
    }

    /**
     * delete allowed for the associated resource ?
     */
    public boolean getAllowDeleteFlag() {
	return getBoolean(ATTR_ALLOW_DEL, false);
    } 

    /**
     * get the Allowed methods for this resource
     * @return an HttpTokenList
     */
    protected HttpTokenList getAllow() {
	if (allowed != null) {
	    return allowed;
	}
	int size = 4; // the default HEAD GET OPTIONS TRACE
	if (getPutableFlag()) {
	    size++;
	}
	if (getAllowDeleteFlag()) {
	    size++;
	}
	String allow_str[] = new String[size];
	int i = 0;
	if (getAllowDeleteFlag()) {
	    allow_str[i++] = "DELETE";
	}
	allow_str[i++] = "HEAD";
	allow_str[i++] = "GET";
	allow_str[i++] = "OPTIONS";
	if (getPutableFlag()) {
	    allow_str[i++] = "PUT";
	}
	allow_str[i] = "TRACE";
	allowed = HttpFactory.makeStringList(allow_str);
	return allowed;
    }

    /**
     * handles a Range Request
     * @param request, the request
     * @param r, the HttpRange
     * @return a Reply if range is valid, or null if there is a change in the
     * resource, or if the HttpRange is not valid ( 4-2, for example).
     * @exception ProtocolException If processsing the request failed.
     */

    public Reply handleRangeRequest(Request request, HttpRange r) 
	throws ProtocolException
    {
	// Should we check against a IfRange header ?
	HttpEntityTag t = request.getIfRange();

	if ( t != null ) {
	    if (t.isWeak() || ! t.getTag().equals(etag.getTag()))
		return null;
	}
	// Check the range:
	int cl = getContentLength();
	int fb = r.getFirstPosition();
	int lb = r.getLastPosition();
	int sz;

	if (fb > cl-1) { // first byte already out of range
	    HttpContentRange cr = HttpFactory.makeContentRange("bytes", 0,
							       cl - 1, cl);
	    Reply rr;
	    rr = createDefaultReply(request, 
				    HTTP.REQUESTED_RANGE_NOT_SATISFIABLE);
	    rr.setContentLength(-1);
	    rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
	    if (getMD5Flag()) 
		rr.setContentMD5(null);
	    return rr;
	}

	if ((fb < 0) && (lb >= 0)) { // ex: bytes=-20 final 20 bytes
	    if (lb >= cl)   // cut the end
		lb = cl;
	    sz = lb;
	    fb = cl - lb;
	    lb = cl - 1;
	} else if (lb < 0) {  // ex: bytes=10- the last size - 10
	    lb = cl-1;
	    sz = lb-fb+1;
	} else {              // ex: bytes=10-20
	    if (lb >= cl)  // cut the end
		lb = cl-1;
	    sz = lb-fb+1;
	}
	if ((fb < 0) || (lb < 0) || (fb <= lb)) {
	    HttpContentRange cr = null;
	    fb = (fb < 0) ? 0 : fb;
	    lb = ((lb > cl) || (lb < 0)) ? cl : lb;
	    cr = HttpFactory.makeContentRange("bytes", fb, lb, cl);
	    // Emit reply:
	    Reply rr = createDefaultReply(request, HTTP.PARTIAL_CONTENT);
	    // FIXME check for MD5 of only the subpart
	    try { // create the MD5 for the subpart
		if (getMD5Flag()) {
		    String s = null;
		    try {
			ByteRangeOutputStream br;
			br = new ByteRangeOutputStream(fresource.getFile(),
						       fb, lb+1);
			Md5 md5 = new Md5 (br);
			byte b[] = md5.getDigest();
			Base64Encoder b64;
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			b64 = new Base64Encoder(new ByteArrayInputStream(b),
						bs);
			b64.process();
			s = bs.toString();
		    } catch (Exception md_ex) {
			// default to null, no action here then
		    }
		    if (s == null)
			rr.setContentMD5(null); 
		    else 
			rr.setContentMD5(s);
		}
      		rr.setContentLength(sz);
		rr.setHeaderValue(rr.H_CONTENT_RANGE, cr);
		rr.setStream(new ByteRangeOutputStream(fresource.getFile(),
						       fb,
						       lb+1));
		return rr;
	    } catch (IOException ex) {
	    }
	} 
	return null;
    }

    //
    // Relative to DirectoryResource ...
    //

    /**
     * Get this class browsable flag.
     */
    public boolean getBrowsableFlag() {
	return getBoolean (ATTR_BROWSABLE, false) ;
    }

    /**
     * Get this frame style sheet link
     */
    public String getStyleSheetURL() {
	return getString (ATTR_STYLE_LINK, null);
    }

    /**
     * Our current (cached) directory listing.
     */
    protected HtmlGenerator listing = null ;
    /**
     * The time at which we generated the directory index.
     */
    protected long listing_stamp = -1 ;

    private String getUnextendedName(String name) {
	int strlen = name.length() ;
	for (int i = 0 ; i < strlen ; i++) {
	    // FIXME: Should use the system props to get the right sep
	    if ( name.charAt(i) == '.' ) {
		if ( i == 0 )
		    return null ;
		return name.substring(0, i) ;
	    }
	}
	return null ;
    }

    /**
     * Get the optional icon directory.
     */
    public String getIconDirectory() { 
	return getString(ATTR_ICONDIR, "/icons") ;
    }

    /**
     * Should we relocate invalid requests to this directory.
     * @return A boolean <strong>true</strong> if we should relocate.
     */
    public boolean getRelocateFlag() {
	return getBoolean(ATTR_RELOCATE, true) ;
    }

    /**
     * Get the optional main index name for this directory listing.
     * @return The name of the resource responsible to list that container.
     */
    public String getIndex() {
	return (String) getValue(ATTR_INDEX, null) ;
    }

    /**
     * Get the optional index name array for this directory listing.
     * @return The index name array (including the main index)
     * @see #getIndex
     */
    public String[] getIndexes() {
	String mainIndex = getIndex();
	if (mainIndex != null) {
	    String indexes[] = (String[]) getValue(ATTR_INDEXES, null);
	    if (indexes != null) {
		int    len          = indexes.length + 1;
		String mergeIndex[] = new String[len];
		mergeIndex[0] = mainIndex;
		System.arraycopy(indexes, 0, mergeIndex, 1, len-1);
		return mergeIndex;
	    } else {
		indexes = new String[1];
		indexes[0] = mainIndex;
		return indexes;
	    }
	} else {
	    return (String[]) getValue(ATTR_INDEXES, null) ;
	}
    }

    /**
     * Add our own Style Sheet to the HtmlGenerator.
     * @param g The HtmlGenerator.
     */
    public void addStyleSheet(HtmlGenerator g) {
	// Add style link
	String css_url = getStyleSheetURL();
	if (css_url != null) {
	    g.addLink( new HtmlLink("STYLESHEET", css_url));
	}
    }

    /**
     * Get ContainerResource listing
     * @param refresh should we refresh the listing?
     * @return a boolean (true if refreshed)
     */ 
    public boolean computeContainerListing(boolean refresh) {
	ContainerResource cresource = (ContainerResource)resource;
	synchronized (cresource) {
	    if ((refresh) ||
	        (listing == null) || 
		(cresource.getLastModified() > listing_stamp) || 
		(getLastModified() > listing_stamp)) {
	    
		Class http_class = null;
		try {
		    http_class = 
			    Class.forName("org.w3c.jigsaw.frames.HTTPFrame");
		} catch (ClassNotFoundException ex) {
			http_class = null;
		}

		Enumeration   e     = cresource.enumerateResourceIdentifiers();
		Vector    resources = Sorter.sortStringEnumeration(e) ;
		HtmlGenerator g     = 
		      new HtmlGenerator("Index of "+cresource.getIdentifier());
		// Add style link
		addStyleSheet(g);
		g.append("<h1>"+cresource.getIdentifier()+"</h1>");
		// Link to the parent, when possible:
		if ( cresource.getParent() != null ) {
		    g.append("<p><a href=\"..\">Parent</a><br>");
		}
	    // List the children:
		for (int i = 0 ; i < resources.size() ; i++) {
		    String            name = (String) resources.elementAt(i);
		    ResourceReference rr   = null;
		    long              size = -1;
		    rr = cresource.lookup(name);
		    FramedResource resource = null;
		    if (rr != null) {
			try {
			    resource = (FramedResource) rr.unsafeLock();
			    // remove manually deleted FileResources
			    if( resource instanceof FileResource ) {
			        FileResource fr = (FileResource)resource;
				if( !fr.getFile().exists() ) {
				    try {
					fr.delete();
				    } catch (MultipleLockException ex) {};
				    continue;
				} else {
					size = fr.getFile().length();
				}
			    }
			    HTTPFrame itsframe = null;
			    if (http_class != null)
				itsframe = 
				    (HTTPFrame) resource.getFrame(http_class);
			    if (itsframe != null) {
				// Icon first, if available
				String icon = itsframe.getIcon() ;
				if ( icon != null ) {
				    g.append("<img src=\""+
					     getIconDirectory() +"/"+ icon+
					     "\" alt=\"" + icon + "\">");
				}
				// Resource's name with link:
				if (resource instanceof ContainerInterface) {
				    g.append("<a href=\"" 
					     , URLEncoder.encode(name)
					     , "/\">"+name+"</a>");
				} else {
				    g.append("<a href=\"" 
					     , URLEncoder.encode(name)
					     , "\">"+name+"</a>");
				}
				// resource's title, if any:
				String title = itsframe.getTitle();
				if ( title != null ) {
				    g.append(" "+title);
				}
				//size (if any)
				if (size != -1) {
				    String s = null;
				    if (size > 1023) {
					s = " ["+(size/1024)+" KB]";
				    } else {
					s = " ["+size+" bytes]";
				    }
				    g.append(s);
				}
				g.append("<br>\n");
			    } else {
				// Resource's name with link:
				g.append(name+
					 " (<i>Not available via HTTP.</i>)");
				g.append("<br>\n");
			    }
			} catch (InvalidResourceException ex) {
			    g.append(name+
				  " cannot be loaded (server misconfigured)");
			    g.append("<br>\n");
			    continue;
			} finally { 
			    rr.unlock();
			}
		    }
		}
		g.close() ;
		listing_stamp = getLastModified() ;
		listing       = g ;
		return true;
	    }
	}
	return false;
    }

    /**
     * Reply with an HTML doc listing the resources of this container.
     * This function takes special care not to regenerate a listing
     * when one is available. It also caches the date of the
     * listing, so that it can win big with NOT_MODIFIED.
     * <p>Using a modem, I know that each place I can reply with an 
     * NOT_MODIFIED, <strong>is</strong> a big win.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply getDirectoryListing(Request request)
	throws ProtocolException, ResourceException
    {
	if (! (resource instanceof ContainerResource)) {
	    throw new ResourceException("this frame is not attached to a "+
					"ContainerResource. ("+
					resource.getIdentifier()+")");
	}
	// delete us if the directory was deleted
	boolean refresh = false;
	if (dresource != null) {
	    synchronized (dresource) {
	        if (! dresource.getDirectory().exists()) {
	   	    //delete us and emit an error
		    String msg = dresource.getIdentifier()+
		        ": deleted, removing the DirectoryResource";
		    getServer().errlog(dresource, msg);
		    try {
		        dresource.delete();
		    } catch (MultipleLockException ex) {
		    }
		    // Emit an error back:
		    Reply error = request.makeReply(HTTP.NOT_FOUND) ;
		    error.setContent ("<h1>Document not found</h1>"+
			   	      "<p>The document "+
				      request.getURL()+
				      " is indexed but not available."+
				      "<p>The server is misconfigured.") ;
		    throw new HTTPException (error) ;
		}
		refresh = 
			(dresource.getDirectory().lastModified() > 
			                                       listing_stamp);
	    }
	}
	if ((! computeContainerListing(refresh)) && 
	    ( checkIfModifiedSince(request) == COND_FAILED )) {
	    // Is it an IMS request ?
	    Reply reply = createDefaultReply(request, HTTP.NOT_MODIFIED) ;
	    return reply;
	}
	// New content or need update:
	Reply reply = createDefaultReply(request, HTTP.OK) ;
	reply.setLastModified(listing_stamp) ;
	reply.setStream(listing) ;
	// check MD5
	return reply ;
    }

    //
    // Commom part.
    //

    /**
     * Update the cached headers value.
     * Each resource maintains a set of cached values for headers, this
     * allows for a nice sped-up in headers marshalling, which - as the 
     * complexity of the protocol increases - becomes a bottleneck.
     */

    protected void updateCachedHeaders() {
	// Precompute a set of header values to keep by:
	if ( contenttype == null ) {
	    String charset = getCharset();
	    if (charset == null)
		contenttype = HttpFactory.makeMimeType(getContentType());
	    else {
		MimeType ctype = getContentType().getClone();
		ctype.addParameter("charset", charset);
		contenttype = HttpFactory.makeMimeType(ctype);
	    }
	}
	if (contentlength == null) {
	    int cl = -1;
	    if (fresource != null) 
		cl = fresource.getFileLength();
	    if ( cl >= 0 ) {
		if (cl != getInt(ATTR_CONTENT_LENGTH, -1)) {
		    setValue(ATTR_CONTENT_LENGTH, new Integer(cl));
		}
		contentlength = HttpFactory.makeInteger(cl);
	    }
	}
	if ( lastmodified == null ) {
	    long lm = getLastModified();
	    if ( lm > 0 )
		lastmodified = HttpFactory.makeDate(lm);
	}
	if (definesAttribute(ATTR_CONTENT_ENCODING) &&(contentencoding==null))
	    contentencoding = HttpFactory.makeStringList(getContentEncoding());
	if (definesAttribute(ATTR_CONTENT_LANGUAGE) &&(contentlanguage==null))
	    contentlanguage = HttpFactory.makeStringList(getContentLanguage());

	if (fresource != null) {
	    // We only take care of etag here:
	    if ( etag == null ) {
		getETag();
	    }
	    if (getMD5Flag() && (md5Digest == null)) {
		getMd5Digest();
	    }
	}
    }

    /**
     * Create a reply to answer to request on this file.
     * This method will create a suitable reply (matching the given request)
     * and will set all its default header values to the appropriate 
     * values.
     * @param request The request to make a reply for.
     * @return An instance of Reply, suited to answer this request.
     */

    public Reply createDefaultReply(Request request, int status) {
	Reply reply = request.makeReply(status);
	updateCachedHeaders();
	if ( status != HTTP.NOT_MODIFIED ) {
	    if ( contentlength != null )
		reply.setHeaderValue(Reply.H_CONTENT_LENGTH, contentlength);
	    if ( contenttype != null )
		reply.setHeaderValue(Reply.H_CONTENT_TYPE, contenttype);
	    if ( lastmodified != null )
		reply.setHeaderValue(Reply.H_LAST_MODIFIED, lastmodified);
	    if ( contentencoding != null ) {
		reply.setHeaderValue(Reply.H_CONTENT_ENCODING,contentencoding);
	    }
	    if ( contentlanguage != null )
		reply.setHeaderValue(Reply.H_CONTENT_LANGUAGE,contentlanguage);

	}
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

	if (fresource != null) {
	    // Set the entity tag:
	    if ( getETag() != null ) {
		reply.setETag(etag);
	    }
	    if ( status != HTTP.NOT_MODIFIED ) {
		if ( acceptRanges ) {
		    reply.setHeaderValue(reply.H_ACCEPT_RANGES,_accept_ranges);
		}
		if ( getMD5Flag()) {
		    reply.setHeaderValue(reply.H_CONTENT_MD5, getMd5Digest());
		}
	    }
	}
	return reply;
    }

    /**
     * Check the <code>If-Match</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */

    public int checkIfMatch(Request request) {
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

    public int checkIfNoneMatch(Request request) {
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
     * Check the <code>If-Modified-Since</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */

    public int checkIfModifiedSince(Request request) {
	// Check for an If-Modified-Since conditional:
	long ims = request.getIfModifiedSince();
	if (dresource != null) {
	    if (ims >= 0) {
		if (listing_stamp > 0) {
		    long s_listing_stamp = listing_stamp / 1000;
		    long s_ims = ims / 1000;
		    if (s_listing_stamp < s_ims) {
			return COND_FAILED;
		    } else if (s_listing_stamp == s_ims) {
			return COND_WEAK;
		    }
		    return COND_OK;
		}
	    }
	} else if (fresource != null) {
	    long cmt = getLastModified();
	    if ( ims >= 0 ) {
		if (cmt > 0) {
		    long s_cmt = cmt / 1000;
		    long s_ims = ims / 1000;		    
		    if (s_cmt < s_ims) {
			return COND_FAILED;
		    } else if (s_cmt == s_ims) {
			return COND_WEAK;
		    }
		    return COND_OK;
		}
	    }
	}
	return 0;
    }

    /**
     * Check the <code>If-Unmodified-Since</code> condition of that request.
     * @param request The request to check.
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */

    public int checkIfUnmodifiedSince(Request request) {
	if (fresource != null) {
	    // Check for an If-Unmodified-Since conditional:
	    long iums = request.getIfUnmodifiedSince();
	    long cmt = getLastModified();
	    if ( iums >= 0 ) 
		return ((cmt > 0) && (cmt - 1000) >= iums) 
		    ? COND_FAILED : COND_OK;
	}
	return 0;
    }

    /**
     * Check the <code>Expect</code> condition of that request
     * @param request The request to check.
     * @return A boolean <code>true</code> if the requirement is known
     */

    public boolean checkExpect(Request request) {
	// crude for now as we only support 100-continue
	// so FIXME for a more evolved version of this.
	String exp = request.getExpect();
	if (exp != null) {
	    if (!exp.equalsIgnoreCase(HTTP.HTTP_100_CONTINUE)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * check the validators namely LMT/Etags according to rfc2616 rules
     * @return An integer, either <code>COND_FAILED</cond> if condition
     * was checked, but failed, <code>COND_OK</code> if condition was checked
     * and succeeded, or <strong>0</strong> if the condition was not checked
     * at all (eg because the resource or the request didn't support it).
     */
    public int checkValidators(Request request) {
	int v_inm = checkIfNoneMatch(request);
	int v_ims = checkIfModifiedSince(request);

	if ((v_inm == COND_OK) || (v_ims == COND_OK)) {
	    return COND_OK;
	}
	if ((v_inm == COND_FAILED) || (v_ims == COND_FAILED)) {
	    return COND_FAILED;
	}
	if ((v_inm == COND_WEAK) || (v_ims == COND_WEAK)) {
	    return COND_FAILED;
	}
	return 0;
    }

     /**
     * Lookup the target resource. Lookup filters and then resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @see org.w3c.tools.resources.ResourceFrame#lookupFilters
     * @see #lookupResource
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	RequestInterface req = ls.getRequest(); 
	if (! checkRequest(req)) 
	    return false;
	if (lookupFilters(ls,lr))
	    return true;
	return lookupResource(ls,lr);
    }

     /**
     * Lookup the target resource (dispath to more specific lookup methods).
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @see #lookupDirectory
     * @see #lookupFile
     * @see #lookupOther
     */
    protected boolean lookupResource(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	if (fresource != null) {
	    return lookupFile(ls,lr);
	} else if (dresource != null) {
	    return lookupDirectory(ls,lr);
	} else {
	    return lookupOther(ls,lr);
	}
    }

    /**
     * Lookup the target resource when associated with a DirectoryResource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol
     * occurs
     */
    protected boolean lookupDirectory(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	// Give a chance to our super-class to run its own lookup scheme:
	// do we have to create a resource (PUT) ?
	if ((ls.hasMoreComponents()) && getPutableFlag()) {
	    Request request = (Request) ls.getRequest() ;
	    if ((request == null) || request.getMethod().equals("PUT")) {
		// We might well want to create a resource:
		String            name = ls.peekNextComponent() ;
		ResourceReference rr   = dresource.lookup(name);
		if ((rr == null) && (dresource.getExtensibleFlag())) {
		    if (ls.countRemainingComponents() == 1)
			rr = dresource.createResource(name, request);
		    else
			rr = dresource.createDirectoryResource(name);
		    if (rr == null) {
			Reply error = 
			    request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE);
			error.setContent(
			    "Failed to create resource "+
			    name +" : "+
			    "Unable to create the appropriate file:"+
			    request.getURLPath()+
			    " this media type is not supported");
			throw new HTTPException (error);
		    }
		} else if (rr == null) {
		    Reply error = request.makeReply(HTTP.FORBIDDEN) ;
		    error.setContent("You are not allowed to create resource "+
				     name +" : "+
				     dresource.getIdentifier()+
				     " is not extensible.");
		    throw new HTTPException (error);
		}
	    }
	}
	if ( super.lookup(ls, lr) ) {
	    if ( ! ls.isDirectory() && ! ls.isInternal() ) {
		// The directory lookup URL doesn't end with a slash:
		Request request = (Request)ls.getRequest() ;
		if ( request == null ) {
		    lr.setTarget(null);
		    return true;
		}
		URL url = null;
		try {
		    if ((request != null ) && 
			request.hasState(Request.ORIG_URL_STATE)) {
			URL oldurl;
			oldurl = (URL)request.getState(Request.ORIG_URL_STATE);
			url = new URL(oldurl, oldurl.getFile() + "/");
		    } else {
			url = (ls.hasRequest() 
			       ? getURL(request)
			       : new URL(getServer().getURL(), 
					 resource.getURLPath()));
		    }
		} catch (MalformedURLException ex) {
		    getServer().errlog(this, "unable to build full URL.");
		    throw new HTTPException("Internal server error");
		}
		String msg = "Invalid requested URL: the directory resource "+
		    " you are trying to reach is available only through "+
		    " its full URL: <a href=\""+
		    url + "\">" + url + "</a>.";
		if ( getRelocateFlag() ) {
		    // Emit an error (with reloc if allowed)
		    Reply reloc = request.makeReply(HTTP.FOUND);
		    reloc.setContent(msg) ;
		    reloc.setLocation(url);
		    lr.setTarget(null);
		    lr.setReply(reloc);
		    return true;
		} else {
		    Reply error = request.makeReply(HTTP.NOT_FOUND) ;
		    error.setContent(msg) ;
		    lr.setTarget(null);
		    lr.setReply(error);
		    return true;
		}
	    } else if ( ! ls.isInternal() ) {
		Request request = (Request)ls.getRequest() ;
		request.setState(STATE_CONTENT_LOCATION, "true");
		// return the index file.
		String indexes[] = getIndexes();
		if (indexes != null) {
		    for (int i = 0 ; i < indexes.length ; i++) {
			String index = indexes[i];
			if ( index != null && index.length() > 0) {
			    DirectoryResource dir = 
				(DirectoryResource) resource;
			    ResourceReference rr = dir.lookup(index);
			    if (rr != null) {
				try {
				    FramedResource rindex = 
					(FramedResource) rr.lock();
				    return rindex.lookup(ls,lr);
				} catch (InvalidResourceException ex) {
				} finally {
				    rr.unlock();
				}
			    }
			}
		    }	
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * Lookup the target resource when associated with a FileResource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupFile(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	return super.lookup(ls,lr);
    }

    /**
     * Lookup the target resource when associated with an unknown resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupOther(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	return super.lookup(ls,lr);
    }

    /**
     * Check the request.
     * @param request the incomming request.
     * @return true if the request is an HTTP Request. 
     */
    public boolean checkRequest(RequestInterface request) {
	return ((request == null) 
		? true 
		:(request instanceof org.w3c.jigsaw.http.Request));
    }

    /**
     * Perform the request on all the frames of that resource. The
     * Reply returned is the first non-null reply.
     * @param request A RequestInterface instance.
     * @return A ReplyInterface instance.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */
    protected ReplyInterface performFrames(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	return super.performFrames(request);
    }

    /**
     * Perform the request
     * @param req The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	ReplyInterface repi = super.perform(req);
	if (repi != null)
	    return repi;

	if (! checkRequest(req))
	    return null;

	Reply  reply  = null;
	Request request = (Request) req;
	String method = request.getMethod () ;
	// Perform the request:
	if ( method.equals("GET") ) {
	    reply = get(request) ;
	} else if ( method.equals("HEAD") ) {
	    reply = head(request) ;
	} else if ( method.equals("POST") ) {
	    reply = post(request) ;
	} else if ( method.equals("PUT") ) {
	    reply = put(request) ;
	} else if ( method.equals("OPTIONS") ) {
	    reply = options(request);
	} else if ( method.equals("DELETE") ) {
	    reply = delete(request) ;
	} else if ( method.equals("LINK") ) {
	    reply = link(request) ;
	} else if ( method.equals("UNLINK") ) {
	    reply = unlink(request) ;
	} else if ( method.equals("TRACE") ) {
	    reply = trace(request) ;
	} else {
	    reply = extended(request) ;
	}
	return reply;
    }

    /**
     * The default GET method.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply get(Request request)
	throws ProtocolException, ResourceException
    {
	if (dresource != null) {
	    // we manage a DirectoryResource
	    return getDirectoryResource(request) ;
	} else if (fresource != null) {
	    // we manage a FileResource
	    return getFileResource(request);
	} else {
	    return getOtherResource(request);
	}
    }

    /**
     * The default GET method for other king of associated resource
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getOtherResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (resource instanceof ContainerResource) {
	    return getDirectoryResource(request);
	} else {
	    // we don't manage this kind of resource
	    Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	    error.setContent("Method GET not implemented.") ;
	    throw new HTTPException (error) ;
	}
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
	// Check for a range request:
	HttpRange ranges[] = request.getRange();
	if ((ranges != null) && (ranges.length == 1)) {
	    Reply rangereply = handleRangeRequest(request, ranges[0]);
	    if ( rangereply != null )
		return rangereply;
	}
	// Default to full reply:
	reply = createDefaultReply(request, HTTP.OK) ;
	try { 
	    reply.setStream(new FileInputStream(file));
	} catch (IOException ex) {
	    Reply error = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
	    error.setContent("Error while accessing filesystem");
	    return error;
	}
	return reply ;
    }

    /**
     * Alway throws an HTTPException
     */
    protected Reply deleteMe(Request request) 
	throws HTTPException
    {
	// Delete the resource if parent is extensible:
	boolean shrinkable = false;
	ResourceReference rr = fresource.getParent();
	ResourceReference rrtemp = null;
	Resource p = null;
	while ( true ) {
	    try {
		if (rr == null)
		    break;
		p = rr.lock();
		if (p instanceof DirectoryResource) {
		    shrinkable = 
			((DirectoryResource)p).getShrinkableFlag();
		    break;
		}
		rrtemp = p.getParent();
	    } catch (InvalidResourceException ex) {
		break;
	    } finally {
		if (rr != null)
		    rr.unlock();
	    }
	    rr = rrtemp;
	}
	if (shrinkable) {
	    // The resource is indexed but has no file, emit an error
	    String msg = fresource.getFile()+
		": deleted, removing the FileResource.";
	    getServer().errlog(fresource, msg);
	    try {
		fresource.delete();
	    } catch (MultipleLockException ex) {
		Reply error = request.makeReply(HTTP.GONE) ;
		error.setContentMD5(null); // FIXME must compute it!
		error.setContent ("<h1>Document Gone</h1>"+
				  "<p>The document "+
				  request.getURL()+
				  " is indexed but no longer available.</p>"+
				  "<p>"+ex.getMessage()+"</p>");
		throw new HTTPException (error) ;
	    }
	}
	// Emit an error back:
	Reply error = request.makeReply(HTTP.GONE) ;
	error.setContentMD5(null);
	error.setContent ("<h1>Document Gone</h1>"+
			  "<p>The document "+
			  request.getURL()+
			  " is indexed but no longer available.</p>");
	return error;
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
	if (!checkExpect(request)) {
	    reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	File file = fresource.getFile() ;
	fresource.checkContent();
	updateCachedHeaders();
	// Check validators:
	int cim = checkIfMatch(request);
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
	if (checkValidators(request) == COND_FAILED) {
	    reply = createDefaultReply(request, HTTP.NOT_MODIFIED);
	    return reply;
	}
	// Does this file really exists, if so send it back
	if ( file.exists() ) {
	    reply = createFileReply(request);
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
	    return reply;
	} else {
	    return deleteMe(request);
	}
    }

    /**
     * Perform a GET for the associated DirectoryResource.
     * @param request the incomming request.
     * @return A Reply instance.
     * @exception ProtocolException if request processing failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply getDirectoryResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	String index = getIndex();
	if ( index != null && index.length() > 0 ) {
	    if (index.equals("*forbid*")) {
		Reply rep = request.makeReply(HTTP.FORBIDDEN);
		rep.setContent("<h1>Forbidden</h1>"+
			       "The directory resource "+ request.getURL() +
			       " cannot be browsed");
		return rep;
	   } 
	}
	return getDirectoryListing(request) ;
    }
    /**
     * The default HEAD method replies does a GET and removes entity.
     * @param request The request to handle.
     * @exception ProtocolException Always thrown, to return a NOT_IMPLEMENTED
     * error.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply head(Request request)
	throws ProtocolException, ResourceException
    {
	if (dresource != null) {
	    return headDirectoryResource(request);
	} else if (fresource != null) {
	    return headFileResource(request);
	} else {
	    return headOtherResource(request);
	}
    }

    /**
     * Perform a HEAD request for the associated resource.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply headOtherResource(Request request) 
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	reply = getOtherResource(request) ;
	reply.setStream((InputStream) null);
	return reply;
    }

    /**
     * Perform a HEAD request for the associated DirectoryResource.
     * @param request the incomming request.
     * @return A Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply headDirectoryResource(Request request) 
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	reply = getDirectoryResource(request) ;
	reply.setStream((InputStream) null);
	return reply;
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
	if (!checkExpect(request)) {
	    reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	fresource.checkContent();
	updateCachedHeaders();
	// Conditional check:
	int cim = checkIfMatch(request);
	if ((cim == COND_FAILED) || (cim == COND_WEAK)) {
	    Reply r = request.makeReply(HTTP.PRECONDITION_FAILED);
	    r.setContent("Pre-conditions failed.");
	    return r;
	}
	if ( checkIfUnmodifiedSince(request) == COND_FAILED ) {
	    Reply r = request.makeReply(HTTP.PRECONDITION_FAILED);
	    r.setContent("Pre-conditions failed.");
	    return r;
	}
	if ( checkValidators(request) == COND_FAILED) {
	    return createDefaultReply(request, HTTP.NOT_MODIFIED);
	}
	if (! fresource.getFile().exists()) {
	    return deleteMe(request);
	}
	reply = createDefaultReply(request, HTTP.OK);
	if (request.hasState(STATE_CONTENT_LOCATION))
	    reply.setContentLocation(getURL(request).toExternalForm());
	return reply;
	
    }

    /**
     * The default POST method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException Always thrown, to return a NOT_IMPLEMENTED
     *    error.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply post(Request request)
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	Reply error = request.makeReply(HTTP.NOT_ALLOWED) ;
	error.setHeaderValue(Reply.H_ALLOW, getAllow());
	error.setContent("Method POST not allowed on this resource.") ;
	throw new HTTPException (error) ;
    }

    /**
     * The default PUT method.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply put(Request request)
	throws ProtocolException, ResourceException
    {
	if (fresource != null) {
	    return putFileResource(request);
	} else {
	    return putOtherResource(request);
	}
    }

    /**
     * Always throw a ProtocolException.
     * @param request The incmming request.
     * @return a Reply instance.
     * @exception ProtocolException (Always thrown).
     */
    protected Reply putOtherResource(Request request) 
	throws ProtocolException
    {
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method PUT not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * Change the content of the associated FileResource.
     * @param request The incomming request.
     * @exception org.w3c.tools.resources.ProtocolException if a protocol 
     * error occurs
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply putFileResource(Request request)
	throws ProtocolException, ResourceException
    {
	Reply reply = null;
	int status = HTTP.OK;

	if (!checkExpect(request)) {
	    reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	fresource.checkContent();
	updateCachedHeaders();
	// Is this resource writable ?
	if ( ! getPutableFlag() ) {
	    Reply error = request.makeReply(HTTP.NOT_ALLOWED) ;
	    error.setHeaderValue(Reply.H_ALLOW, getAllow());
	    error.setContent("Method PUT not allowed.") ;
	    throw new HTTPException (error) ;
	}
	// Check validators:
	int cim = checkIfMatch(request);	
	if ((cim == COND_FAILED) || (cim == COND_WEAK)
	    || (checkIfNoneMatch(request) == COND_FAILED)
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
	    Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED);
	    error.setContent("partial PUT not supported.");
	    throw new HTTPException(error);
	}
// REMOVED as it is impossile for clients to behave properly.
// Of course it is fare more unsafe now, but it was too impractical IRL.
	// Check that if some type is provided it doesn't conflict:
//	if ( request.hasContentType() ) {
//	    MimeType rtype = request.getContentType() ;
//	    MimeType type  = getContentType() ;
//	    if ( type == null ) {
//		setValue (ATTR_CONTENT_TYPE, rtype) ;
//	    } else if ( (rtype.match (type) < 0 ) && !rtype.equiv(type) ) {
//		if (debug) {
//		    System.out.println("No match between: ["+
//				       rtype.toString()+"] and ["+
//				       type.toString()+"]");
//		}
//		Reply error = request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE) ;
//		error.setContent ("<p>Invalid content type: "+rtype.toString()
//				  + " is not matching resource MIME type: "
//				  +type.toString());
//		throw new HTTPException (error) ;
//	    }
//	}
	// Write the body back to the file:
	try {
	    // We are about to accept the put, notify client before continuing
	    Client client = request.getClient();
	    if ( client != null  && request.getExpect() != null ) {
		// FIXME we should check for "100-continue" explicitely
		client.sendContinue();
	    }
	    if ( fresource.newContent(request.getInputStream()) )
		status = HTTP.CREATED;
	    else
		status = HTTP.NO_CONTENT;
	} catch (IOException ex) {
	    if (debug) 
		ex.printStackTrace();
	    // so we have a problem replacing or creating the content
	    // it is then a configuration problem (access right for the 
	    // underlying fle resource for example...
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setReason("File Access Error");
	    error.setContent("<p>Unable to save " + request.getURL() 
			     +" due to IO problems"); 
	    throw new HTTPException (error) ;	    
	}
	if ( status == HTTP.CREATED ) {
	    reply = request.makeReply(status);
	    reply.setContent("<P>Resource succesfully created");
	    if (request.hasState(STATE_CONTENT_LOCATION))
		reply.setContentLocation(getURL(request).toExternalForm());
            // Henrik's fix, create the Etag on 201
	    if (fresource != null) {
		// We only take car eof etag here:
		if ( etag == null ) {
		    reply.setETag(getETag());
		}
	    }
	    reply.setLocation(getURL(request));
	    reply.setContent ("<p>Entity body saved succesfully !") ;
	} else {
	    reply = createDefaultReply(request, status);
	}
	return reply ;
    }

    /**
     * The default OPTIONS method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException In case of errors.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply options(Request request)
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	Reply reply = createDefaultReply(request, HTTP.OK);
	// set size to 0 according to rfc2616 9.2
	reply.setContentLength(0);
	// Removed unused headers:
	reply.setContentType(null);
	// Add the allow header:
	reply.setHeaderValue(Reply.H_ALLOW, getAllow());
	return reply;
    }

    /**
     * The default DELETE method, actually the resource (file, directory)
     * is moved into the trash directory which is not accessible via HTTP.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply delete(Request request)
	throws ProtocolException, ResourceException
    {
	if (getAllowDeleteFlag()) {
	    if (dresource != null) {
		// we manage a DirectoryResource
		return deleteDirectoryResource(request) ;
	    } else if (fresource != null) {
		// we manage a FileResource
		return deleteFileResource(request);
	    } else {
		return deleteOtherResource(request);
	    }
	} else {
	    Reply error = request.makeReply(HTTP.NOT_ALLOWED) ;
	    error.setContent("Method DELETE not allowed.") ;
	    error.setHeaderValue(Reply.H_ALLOW, getAllow());
	    throw new HTTPException (error) ;
	}
    }

    protected File computeTrashFile(File file) {
	File trashdir = new File(getServer().getTrashDirectory());
	if (! trashdir.exists()) {
	    trashdir.mkdirs();
	}
	String filename = file.getName();
	File trashfile = new File(trashdir, filename);
	int nb = 1;
	while(trashfile.exists()) {
	    trashfile = new File(trashdir, filename+"."+nb);
	    nb++;
	}
	return trashfile;
    }

    protected File computeTrashDir(File dir) {
	return computeTrashFile(dir);
    }

    /**
     * Perform a DELETE for the associated DirectoryResource.
     * @param request the incomming request.
     * @return A Reply instance.
     * @exception ProtocolException if request processing failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply deleteDirectoryResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	File olddir = dresource.getDirectory();
	File newdir = computeTrashDir(olddir);
	try {
	    //delete the FileResource
	    dresource.delete();
	} catch (MultipleLockException ex) {
	    Reply error = request.makeReply(HTTP.FORBIDDEN);
	    error.setContent("Can't delete resource: "+
			     resource.getIdentifier()+
			     " is locked. Try again later.");
	    throw new HTTPException(error);
	}
	olddir.renameTo(newdir);
	return request.makeReply(HTTP.NO_CONTENT);
    }

    /**
     * Perform a DELETE for the associated FileResource.
     * @param request the incomming request.
     * @return A Reply instance.
     * @exception ProtocolException if request processing failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply deleteFileResource(Request request) 
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	File oldfile = fresource.getFile();
	File newfile = computeTrashFile(oldfile);
	try {
	    //delete the FileResource
	    fresource.delete();
	} catch (MultipleLockException ex) {
	    Reply error = request.makeReply(HTTP.FORBIDDEN);
	    error.setContent("Can't delete resource: "+
			     resource.getIdentifier()+
			     " is locked. Try again later.");
	    throw new HTTPException(error);
	}
	oldfile.renameTo(newfile);
	return request.makeReply(HTTP.NO_CONTENT);
    }

    /**
     * Perform a DELETE for the associated resource.
     * @param request the incomming request.
     * @return A Reply instance.
     * @exception ProtocolException if request processing failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    protected Reply deleteOtherResource(Request request) 
	throws ProtocolException, ResourceException
    {
	// we don't manage this kind of resource
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method DELETE not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * The default LINK method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException Always thrown, to return a NOT_IMPLEMENTED
     * error.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply link(Request request) 
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method LINK not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * The default UNLINK method replies with a not implemented.
     * @param request The request to handle.
     * @exception ProtocolException Always thrown, to return a NOT_IMPLEMENTED
     *    error.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply unlink(Request request)
	throws ProtocolException, ResourceException
    {
	if (!checkExpect(request)) {
	    Reply reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
	Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method UNLINK not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * The default TRACE method replies with a not implemented
     * @param request The request to handle.
     * @exception HTTPException In case of errors.
     * @exception ClientException If the client instance controling the
     * request processing got a fatal error.
     */

    public Reply trace(Request request)
        throws HTTPException, ClientException
    {
	Reply reply = null;
	if (!checkExpect(request)) {
	    reply = createDefaultReply(request, HTTP.EXPECTATION_FAILED);
	    reply.setContent("The requested expectation could not be"+
			     " met by the resource");
	    return reply;
	}
        reply = createDefaultReply(request, HTTP.OK);
	reply.setNoCache(); // don't cache this
        reply.setMaxAge(-1); // 
	reply.setContentMD5(null);
        // Dump the request as the body
        // Removed unused headers:
	// FIXME should be something else for chuncked stream
	ByteArrayOutputStream ba = new ByteArrayOutputStream();
	try {
	    reply.setContentType(new MimeType("message/http"));
	    request.dump(ba);
	    reply.setContentLength(ba.size());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	reply.setStream(new ByteArrayInputStream(ba.toByteArray()));
	return reply;
    }

    public Reply extended(Request request) 
    	throws ProtocolException, ResourceException
    {
	String method = request.getMethod() ;
	Reply error   = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	error.setContent("Method "+method+" not implemented.") ;
	throw new HTTPException (error) ;
    }

    /**
     * The Browse Mime type.
     */
    protected static MimeType  browsetype = null;

    /**
     * Get the Browse Mime type.
     */

    protected synchronized MimeType getBrowseType() {
	if ( browsetype == null ) {
	    try {
		browsetype = new MimeType("application/x-navibrowse");
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
	return browsetype;
    }


    /**
     * A present to GNNPress users !
     * This method implements the <code>BROWSE</code> method that
     * AOL press (or GNN press, or whatever its last name is) expects.
     * @param request The request to process.
     * @exception ProtocolException If some error occurs.
     * @return A Reply instance.
     */

    public Reply browse (Request request) 
	throws ProtocolException
    {
	if (dresource == null) {
	    Reply error = request.makeReply(HTTP.NOT_IMPLEMENTED) ;
	    error.setContent("Method "+request.getMethod()+
			     " not implemented.") ;
	    throw new HTTPException (error) ;
	}

	Enumeration  e         = dresource.enumerateResourceIdentifiers() ;
	Vector       resources = Sorter.sortStringEnumeration(e) ;
	int          rsize     = ((resources == null) ? 0 : resources.size()) ;
	StringBuffer sb        = new StringBuffer() ;

	// As we have enumerated all resources, just looking the store is ok
	for (int i = 0 ; i < rsize ; i++) {
	    String            rname = (String) resources.elementAt(i) ;
	    ResourceReference rr    = null;
	    Resource          r     = null;
	    try {
		rr = dresource.lookup(rname) ;
		r = rr.lock();
		// may throw InvalidResourceException

		if ( r instanceof DirectoryResource ) {
		    sb.append("application/x-navidir "+
			      rname+
			      "\r\n") ;
		} else {
		    HTTPFrame itsframe = 
			(HTTPFrame) resource.getFrame(getClass());
		    if (itsframe != null) {
			sb.append(itsframe.getContentType().toString()+
				  " "+
				  rname+
				  "\r\n") ;
		    }
		}
	    } catch (InvalidResourceException ex) {
		continue;
	    } finally {
		rr.unlock();
	    }
	}
	Reply reply = request.makeReply(HTTP.OK) ;
	reply.addPragma("no-cache");
	reply.setNoCache();
	reply.setContent(sb.toString()) ;
	reply.setContentType(getBrowseType());
	return reply ;
    }

    /**
     * Set the values. (MUST be called before initialize).
     * @param defs The Hashtable containing the values.
     */
    public void pickleValues(Hashtable defs) {
	Object nvalues[] = new Object[attributes.length];
	for (int i = 0 ; i < nvalues.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    // note that protocol frames have usually names in the
	    // frame-XX serie, so it is valid to save (big) on this.
	    if ((def != null) && ((i == ATTR_HELP_URL) ||
				  (i == ATTR_IDENTIFIER) ||
				  (i == ATTR_TITLE) ||
				  (i == ATTR_CHARSET) ||
				  (i == ATTR_CONTENT_LANGUAGE) ||
				  (i == ATTR_CONTENT_ENCODING) ||
				  (i == ATTR_ICON) ||
				  (i == ATTR_INDEX) ||
				  (i == ATTR_ICONDIR) ||
				  (i == ATTR_STYLE_LINK))
		&& (def instanceof String)) {
		nvalues[i] = ((String) def).intern();
	    } else {	    
		nvalues[i] = def ;
	    }
	}
	this.values = nvalues ;
    }

    /**
     * Initialization method for attribute holders.
     * This method allows to initialize an attribute holder by providing
     * its attributes values through a Hashtable mapping attribute names
     * to attribute values.
     * @param defs The Hashtable containing the default values.
     */

    public void initialize(Hashtable defs) {
	Object values[] = ((this.values == null)
			   ? new Object[attributes.length] 
			   : this.values);
	for (int i = 0 ; i < values.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    if ( values[i] == null ) {
		// for help_url, we can save lots of space by using 
		// String.intern()
		// Those attribute are not, in practise, very variables
		// So it is a valid optimisation to intern them.
		if ((def != null) && ((i == ATTR_HELP_URL) ||
				      (i == ATTR_IDENTIFIER) ||
				      (i == ATTR_TITLE) ||
				      (i == ATTR_CHARSET) ||
				      (i == ATTR_CONTENT_LANGUAGE) ||
				      (i == ATTR_CONTENT_ENCODING) ||
				      (i == ATTR_ICON) ||
				      (i == ATTR_INDEX) ||
				      (i == ATTR_ICONDIR) ||
				      (i == ATTR_STYLE_LINK))
		        && (def instanceof String)) {
		    values[i] = ((String) def).intern();
		} else {	    
		    values[i] = def ;
		}
	    }
	}
	initialize(values) ;
    }
}
