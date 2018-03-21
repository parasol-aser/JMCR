// NegotiatedFrame.java
// $Id: NegotiatedFrame.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.frames;

import java.io.PrintStream;

import java.util.Vector;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringArrayAttribute;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.jigsaw.html.HtmlGenerator ;

import org.w3c.www.mime.LanguageTag;
import org.w3c.www.mime.MimeType;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpAccept;
import org.w3c.www.http.HttpAcceptCharset;
import org.w3c.www.http.HttpAcceptEncoding;
import org.w3c.www.http.HttpAcceptLanguage;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpEntityTag;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInvalidValueException;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpTokenList;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;


/**
 * Content negotiation.
 */
public class NegotiatedFrame extends HTTPFrame {

    class VariantState {
	ResourceReference variant = null ;
	double qs      = 0.0 ;  // configured frame quality
	double qe      = 0.0 ;  // content encoding quality
	double qc      = 0.0 ;  // content charset quality
	double ql      = 0.0 ;  // content language quality
	double q       = 0.0 ;	// quality (mime type one)
	double Q       = 0.0 ;	// the big Q

	public String toString() {
	    try {
		Resource res = variant.unsafeLock();
		String name = (String) res.getIdentifier() ;
		if ( name == null )
		    name = "<noname>" ;
		return "[" + name 
		    + " qc=" + qc
		    + " qs=" + qs 
		    + " qe=" + qe
		    + " ql=" + ql
		    + " q =" + q
		    + " Q =" + getQ() 
		    +"]" ;
	    } catch (InvalidResourceException ex) {
		return "invalid";
	    } finally {
		variant.unlock();
	    }
	}

	void setCharsetQuality (double qc) {
	    this.qc = qc ;
	}

	double getCharsetQuality () {
	    return qc;
	}

	void setContentEncodingQuality (double qe) {
	    this.qe = qe ;
	}

	void setContentEncodingQuality (HttpAcceptEncoding e) {
	    this.qe = e.getQuality();
	}

	double getContentEncodingQuality () {
	    return qe ;
	}

	void setQuality (double q) {
	    this.q = q ;
	}

	void setQuality (HttpAccept a) {
	    q = a.getQuality() ;
	}

	void setLanguageQuality (double ql) {
	    this.ql = ql ;
	}

	void setLanguageQuality (HttpAcceptLanguage l) {
	    this.ql = l.getQuality() ;
	}

	double getLanguageQuality () {
	    return ql ;
	}

	ResourceReference getResource () {
	    return variant ;
	}

	double getQ() {
	    return qe * q * qs * ql * qc;
	}

	VariantState (ResourceReference variant, double qs) {
	    this.qs      = qs ;
	    this.variant = variant ;
	}
    }

    private static Class httpFrameClass = null;
    private static Class negotiatedFrameClass = null;

    static {
	try {
	    httpFrameClass = Class.forName("org.w3c.jigsaw.frames.HTTPFrame") ;
	} catch (Exception ex) {
	    throw new RuntimeException("No HTTPFrame class found.");
	}
	try {
	    negotiatedFrameClass = 
		    Class.forName("org.w3c.jigsaw.frames.NegotiatedFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    throw new RuntimeException("No NegotiatedFrame class found.");
	}
    }

    /**
     * Our Icon property.
     */
    public static String NEGOTIATED_ICON_P = 
	"org.w3c.jigsaw.frames.negotiated.icon";
    /**
     * Our default Icon
     */
    public static String DEFAULT_NEGOTIATED_ICON = "generic.gif";

    /**
     * our state
     */
    protected static String STATE_NEG = "org.w3c.jigsaw.frames.Negotiated";

    /**
     * Turn debugging on/off.
     */
    private static final boolean debug = false;
    /**
     * Minimum quality for a resource to be considered further.
     */
    private static final double REQUIRED_QUALITY = 0.0001 ;
    /**
     * The Vary header field for this resource
     */
    protected HttpTokenList vary = null;
    private boolean vary_done = false;
    /**
     * Attribute index - The set of names of variants.
     */
    protected static int ATTR_VARIANTS = -1 ;
    /**
     * Attribute index - Should the PUT needs to be strictly checked?
     */
    protected static int ATTR_PUT_POLICY = -1;
    /**
     * Attribute index - Should the variants to be strictly checked?
     * the variants should NEVER contain negotiated resources!
     */
    protected static int ATTR_PARANOID_VARIANT_CHECK = -1;

    static {
	Attribute   a = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.frames.NegotiatedFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The names of the varint we negotiate
	a = new StringArrayAttribute("variants"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_VARIANTS = AttributeRegistry.registerAttribute(cls, a) ;
	a = new BooleanAttribute("strict_put"
				 , new Boolean(true)
				 , Attribute.EDITABLE);
	ATTR_PUT_POLICY = AttributeRegistry.registerAttribute(cls, a) ;
	a = new BooleanAttribute("paranoid_variant_check"
				 , new Boolean(false)
				 , Attribute.EDITABLE);
	ATTR_PARANOID_VARIANT_CHECK = 
			    AttributeRegistry.registerAttribute(cls, a) ;
    }

    private boolean b_charset  = true;
    private boolean b_type     = true;
    private boolean b_language = true;
    private boolean b_encoding = true;


    public synchronized void setValue(int idx, Object value) {
	if ( idx == ATTR_VARIANTS ){
	    super.setValue(idx,checkVariants((String[])value));
	} else {
	    super.setValue(idx,value);
	}
    }

    private HttpTokenList getVary() {
	if (vary_done) {
	    return vary;
	}
	// ok let's do it!
	vary_done  = true;
	b_charset  = false;
	b_type     = false;
	b_language = false;
	b_encoding = false;

	ResourceReference variants[] = null;
	try {
	    variants = getVariantResources() ;
	} catch (ProtocolException ex) {};
	// no vary for so few variants!

	if ((variants == null) || (variants.length < 2)) {
	    return null;
	}
	synchronized (this) {
	    int num = 0;
	    HTTPFrame refframe = null;
	    FramedResource resource = null;
	    do {
		try {
		    resource = (FramedResource)variants[num].unsafeLock();
		    refframe = (HTTPFrame) resource.getFrame(httpFrameClass);
		} catch (InvalidResourceException ex) {
		    //ex.printStackTrace();
		    //FIXME
		} catch (Exception fex) {
		    fex.printStackTrace();
		} finally {
		    variants[num].unlock();
		}
		num++;
	    } while ((refframe == null) && (num < variants.length));
	    // not enough variants, abort
	    if (variants.length - num < 1)
		return null;
	
	    String language = refframe.getContentLanguage();
	    String encoding = refframe.getContentEncoding();
	    String charset  = refframe.getCharset();
	    MimeType mtype  = refframe.getContentType();
	    
	    HTTPFrame itsframe = null;
	    for (int i = num; i < variants.length; i++) {
		try {
		    resource = (FramedResource)variants[i].unsafeLock();
		    itsframe = (HTTPFrame) 
			               resource.unsafeGetFrame(httpFrameClass);
		    if (language != null) {
			if (!language.equals(itsframe.getContentLanguage())) {
			    b_language = true;
			}
		    } else {
			if (itsframe.getContentLanguage() != null) {
			    b_language = true;
			}
		    }
		    if (encoding != null) {
			if (!encoding.equals(itsframe.getContentEncoding())) {
			    b_encoding = true;
			}
		    } else {
			if (itsframe.getContentEncoding() != null) {
			    b_encoding = true;
			}
		    }
		    if (charset != null) {
			if (!charset.equals(itsframe.getCharset())) {
			    b_charset = true;
			}
		    } else {
			if (itsframe.getCharset() != null) {
			    b_charset = true;
			}
		    }
		    if (mtype != null) {
			MimeType o_type = itsframe.getContentType();
			if ((o_type != null) && (mtype.match(o_type) != 
					    MimeType.MATCH_SPECIFIC_SUBTYPE)) {
			    b_type = true;
			}
		    } else {
			if (itsframe.getContentType() != null) {
			    b_type = true;
			}
		    }
		} catch (InvalidResourceException ex) {
		    //ex.printStackTrace();
			//FIXME
		} finally {
		    variants[i].unlock();
		}
	    }
	    int vary_size = 0;
	    if (b_language) {
		vary_size++;
	    }
	    if (b_charset) {
		vary_size++;
	    }
	    if (b_encoding) {
		++vary_size;
	    }
	    if (b_type) {
		++vary_size;
	    }
	    
	    if (vary_size == 0) {
		return null;
	    }
	    String[] s_vary = new String[vary_size];
	    num = 0;
	    if (b_type) {
		s_vary[num++] = "Accept";
	    }
	    if (b_encoding) {
		s_vary[num++] = "Accept-Encoding";
	    }
	    if (b_language) {
		s_vary[num++] = "Accept-Language";
	    }
	    if (b_charset) {
		s_vary[num] = "Accept-Charset";
	    }
	    vary = HttpFactory.makeStringList(s_vary);
	}
	return vary;
    }

    public String getIcon() {
	String icon = super.getIcon();
	if (icon == null) {
	    icon = 
		getServer().getProperties().getString(NEGOTIATED_ICON_P,
						      DEFAULT_NEGOTIATED_ICON);
	    setValue(ATTR_ICON, icon);
	}
	return icon;
    }

    /**
     * Get the variant names.
     */
    public String[] getVariantNames() {
	return (String[]) getValue(ATTR_VARIANTS, null) ;
    }

    protected String[] checkVariants(String variants[]) {

	Vector checked = new Vector(variants.length) ;
	ResourceReference tmpres = null;
	ResourceReference r_parent   = resource.getParent() ;
	try {
	    DirectoryResource parent= (DirectoryResource)r_parent.unsafeLock();
	    for (int i = 0 ; i < variants.length ; i++) {
		tmpres = parent.lookup(variants[i]) ;
		if (tmpres != null) {
		    try {
			FramedResource resource = 
				    (FramedResource)tmpres.unsafeLock() ;
			NegotiatedFrame itsframe = (NegotiatedFrame) 
				    resource.getFrame(negotiatedFrameClass);
			if (itsframe == null) {
			    checked.addElement (variants[i]);
			}
		    } catch (InvalidResourceException ex) {
		    } finally {
			tmpres.unlock();
		    }
		} else {
		    checked.addElement (variants[i]);
		}
	    }
	} catch (InvalidResourceException ex) {
	    // throw new HTTPException("invalid parent for negotiation");
	} finally {
	    r_parent.unlock();
	}

	String[] variants_ok = new String[checked.size()];
	checked.copyInto(variants_ok);
    
	return variants_ok ;
    }

    public void setVariants(String variants[]) {
	setValue(ATTR_VARIANTS, variants);

	// invalidate the Vary header
	vary_done = false;
    }

    /**
     * get the "strictness" of the PUT checking
     */
    public boolean getPutPolicy() {
	Boolean val = (Boolean) getValue(ATTR_PUT_POLICY, null);
	if (val == null) // strict by default
	    return true;
	return val.booleanValue();
    }

    public void setPutPolicy(Boolean strict) {
	setValue(ATTR_PUT_POLICY, strict);
    }

    public void setPutPolicy(boolean strict) {
	setValue(ATTR_PUT_POLICY, new Boolean(strict));
    }

    /**
     * get the variant checking policy
     */
     public boolean getParanoidVariantCheck() {
	Boolean val = (Boolean) getValue(ATTR_PARANOID_VARIANT_CHECK, 
					 Boolean.FALSE); 
	return val.booleanValue(); 
    }

    public void setParanoidVariantCheck(boolean strict) {
	setValue(ATTR_PARANOID_VARIANT_CHECK, new Boolean(strict));
    }

    /**
     * Get the variant resources.
     * This is somehow broken, it shouldn't allocate the array of variants
     * on each call. However, don't forget that the list of variants can be
     * dynamically edited, this means that if we are to keep a cache of it 
     * (as would be the case if we kept the array of variants as instance var)
     * we should also take care of editing of attributes (possible, but I
     * just don't have enough lifes).
     * @return An array of ResourceReference, or <strong>null</strong>.
     * @exception ProtocolException If one of the variants doesn't exist.
     */

    public ResourceReference[] getVariantResources() 
	throws ProtocolException
    {

	// Get the variant names:
	String names[] = getVariantNames() ;
	if ( names == null )
	    return null ;
	
	int oldlength = names.length ;

	if (getParanoidVariantCheck()) {
	    names = checkVariants(names) ;
	}

	// Look them up in our parent directory:
	ResourceReference variants[] = new ResourceReference[names.length] ;
	ResourceReference r_parent   = resource.getParent() ;
	try {
	    DirectoryResource parent= (DirectoryResource)r_parent.unsafeLock();
	    int missing = 0;
	    for (int i = 0 ; i < names.length ; i++) {
		variants[i] = parent.lookup(names[i]) ;
		if (variants[i] == null)
		    missing++;
	    }
	    if (missing > 0) {
		int kept = names.length - missing;
		if (kept < 1)
		    return null;
		String newNames[] = new String[kept];
		int j = 0; int i = 0;
		while (i < variants.length) {
		    if (variants[i] != null) {
			newNames[j++] = names[i++];
		    } else {
			i++;
		    }
		}
		setVariants(newNames);
		//recompute Variant Resources
		return getVariantResources();
	    } else if (oldlength > names.length){
		setVariants(names);
	    }

	} catch (InvalidResourceException ex) {
	    throw new HTTPException("invalid parent for negotiation");
	} finally {
	    r_parent.unlock();
	}
	return variants ;
    }

    /**
     * Print the current negotiation state.
     * @param header The header to print first.
     * @param states The current negotiation states.
     */

    protected void printNegotiationState (String header, Vector states) {
	if ( debug ) {
	    System.out.println ("------" + header) ;
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		System.out.println (state) ;
	    }
	    System.out.println ("-----") ;
	}
    }

    /**
     * Negotiate among content encodings.
     * <p>BUG: This will work only for single encoded variants.
     * @param states The current negotiation states.
     * @param request The request to handle.
     * @return a boolean.
     * @exception ProtocolException If one of the variants doesn't exist.
     */

    protected boolean negotiateContentEncoding (Vector states,
						Request request) 
	throws ProtocolException
    {
	if ( !request.hasAcceptEncoding() || !b_encoding ) {
	    // All encodings accepted:
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		state.setContentEncodingQuality(1.0) ;
	    }
	} else {
	    HttpAcceptEncoding encodings[] = request.getAcceptEncoding() ;
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state    = (VariantState) states.elementAt(i) ;
		ResourceReference rr  = state.getResource();
		try {
		    FramedResource resource = (FramedResource)rr.unsafeLock() ;
		    HTTPFrame itsframe = 
			(HTTPFrame) resource.unsafeGetFrame(httpFrameClass);
		    if (itsframe != null) {
			String ve;
			ve = (String) itsframe.unsafeGetValue(
			                          ATTR_CONTENT_ENCODING, null);
			if ( ve == null ) {
			    ve = "identity"; // default encoding
			    state.setContentEncodingQuality (1.0) ;
			} else {
			    state.setContentEncodingQuality (0.01) ;
			}
			int jidx    = -1 ;
			for (int j = 0 ; j < encodings.length ; j++) {
			    if (encodings[j].getEncoding().equals(ve)) {
				jidx = j;
				break;
			    } 
			    if (encodings[j].getEncoding().equals("*"))
				jidx = j; // default '*' if no better match
			}
			if ( jidx >= 0 ) 
			    state.setContentEncodingQuality
				(encodings[jidx].getQuality() - jidx * 0.001) ;
		    }
		} catch (InvalidResourceException ex) {
		    
		} finally {
		    rr.unlock();
		}
	    }
	    // FIXME We should check here against unlegible variants as now
	}
	return false ;
    }

    /**
     * Negotiate on charsets.
     * The algorithm is described in RFC2616 (Obsoletes RFC2068)
     * @param states The current states of negotiation.
     * @param request The request to handle.
     */

    protected boolean negotiateCharsetQuality (Vector states
					       , Request request) {
	if ( !request.hasAcceptCharset() || !b_charset ) {
	    // All variants get a quality of 1.0
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		state.setCharsetQuality (1.0) ;
	    }
	} else {
	    // The browser has given some preferences:
	    HttpAcceptCharset charsets[] = request.getAcceptCharset() ;

	    for (int i = 0 ; i < states.size() ; i++ ) {
		VariantState state = (VariantState) states.elementAt(i) ;
		// Get the most specific match for this variant:
		ResourceReference rr = state.getResource();
		try {
		    FramedResource resource = (FramedResource)rr.unsafeLock() ;
		    HTTPFrame itsframe = 
			(HTTPFrame) resource.unsafeGetFrame(httpFrameClass);
		    if (itsframe != null) {
			MimeType vt;
			vt = (MimeType) itsframe.unsafeGetValue(
			                              ATTR_CONTENT_TYPE, null);
			String fcharset = vt.getParameterValue("charset");
			// if not defined in the frame, it must be the default
			if (fcharset == null) {
			    fcharset = "ISO-8859-1";
			}
			double qual = 0.0 ;
			boolean default_done = false;
			String charset;
			for (int j = 0 ; j < charsets.length ; j++) {
			    charset = charsets[j].getCharset();
			    if (charset.equals("*")) {
				default_done = true;
				if (qual == 0) {
				    qual = charsets[j].getQuality()-0.001*j;
				}
			    } else {
				if ( charset.equals("ISO-8859-1"))
				    default_done = true;
				if ( charset.equals(fcharset)) 
				    if (charsets[j].getQuality() > qual) {
					qual =charsets[j].getQuality()-0.001*j;
				    }
			    }
			}
			if (!default_done && fcharset.equals("ISO-8859-1"))
			    qual = 1.0 - 0.001*charsets.length;
			state.setCharsetQuality(qual) ;
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr.unlock();
		}
	    }
	}
	return false ;
    }

    /**
     * Negotiate among language qualities.
     * <p>BUG: This will only work for variants that have one language tag.
     * @param states The current states of negotiation.
     * @param request The request to handle.
     * @return a boolean.
     * @exception ProtocolException If one of the variants doesn't exist.
     */

    protected boolean negotiateLanguageQuality (Vector states
						, Request request) 
	throws ProtocolException
    {
	if ( !request.hasAcceptLanguage() || !b_language ) {
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		state.setLanguageQuality (1.0) ;
	    }
	} else {
	    HttpAcceptLanguage languages[] = request.getAcceptLanguage() ;
	    LanguageTag req_lang[] = new LanguageTag[languages.length];
	    for (int i = 0 ; i < languages.length ; i++) {
		req_lang[i] = new LanguageTag(languages[i].getLanguage());
	    }
	    boolean  varyLang    = false ;
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state    = (VariantState) states.elementAt(i) ;
		ResourceReference rr  = state.getResource();
		try {
		    FramedResource resource = (FramedResource)rr.unsafeLock() ;
		    HTTPFrame itsframe = 
			(HTTPFrame) resource.getFrame(httpFrameClass);
		    if (itsframe != null) {
			String lang;
			lang = (String) itsframe.unsafeGetValue(
			                          ATTR_CONTENT_LANGUAGE, null);
			if ( lang == null ) {
			    state.setLanguageQuality (-1.0) ;
			} else {
			    varyLang = true ;
			    LanguageTag ftag = new LanguageTag(lang);
			    int jmatch = -1 ;
			    int jidx   = -1 ;
			    for (int j = 0 ; j < languages.length ; j++) {
				int match = ftag.match(req_lang[j]);
				if ( match > jmatch ) {
				    jmatch = match ;
				    jidx   = j ;
				}
			    }
			    if ( jidx < 0 ) 
				state.setLanguageQuality(0.01) ;
			    else {
				// little hack for first
				state.setLanguageQuality (
				    languages[jidx].getQuality()-jidx*0.001) ;
			    }
			}
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr.unlock();
		}
	    }
	    if ( varyLang ) {
		for (int i = 0 ; i < states.size() ; i++) {
		    VariantState s = (VariantState) states.elementAt(i);
		    if ( s.getLanguageQuality() < 0 )
			s.setLanguageQuality (0.5) ;
		}
	    } else {
		for (int i = 0 ; i < states.size() ; i++) {
		    VariantState s = (VariantState) states.elementAt(i) ;
		    s.setLanguageQuality (1.0) ;
		}
	    }
	}
	return false ;
    }

    /**
     * Negotiate among content types.
     * @param states The current states of negotiation.
     * @param request The request to handle.
     * @return a boolean.
     * @exception ProtocolException If one of the variants doesn't exist.
     */

    protected boolean negotiateContentType (Vector states,
					    Request request) 
	throws ProtocolException
    {
	if ( !request.hasAccept() || !b_type ) {
	    // All variants get a quality of 1.0
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		state.setQuality (1.0) ;
	    }
	} else {
	    // The browser has given some preferences:
	    HttpAccept accepts[] = request.getAccept() ;

	    for (int i = 0 ; i < states.size() ; i++ ) {
		VariantState state = (VariantState) states.elementAt(i) ;
		// Get the most specific match for this variant:
		ResourceReference rr = state.getResource();
		try {
		    FramedResource resource = (FramedResource)rr.unsafeLock() ;
		    HTTPFrame itsframe = 
			(HTTPFrame) resource.unsafeGetFrame(httpFrameClass);
		    if (itsframe != null) {
			MimeType vt;
			vt = (MimeType) itsframe.unsafeGetValue(
			                              ATTR_CONTENT_TYPE, null);
			int jmatch = -1 ;
			int jidx   = -1 ;
			for (int j = 0 ; j < accepts.length ; j++) {
			    try {
				int match = vt.match(accepts[j].getMimeType());
				if ( match > jmatch ) {
				    jmatch = match ;
				    jidx   = j ;
				}
			    } catch (HttpInvalidValueException ivex) {
				// There is a bad acept header here
				// let's be cool and ignore it
				// FIXME we should answer with a Bad Request
			    }
			}
			if ( jidx < 0 )
			    state.setQuality (0.0) ;
			else 
			    state.setQuality(accepts[jidx].getQuality()
					     -jidx*0.001) ;
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr.unlock();
		}
	    }
	}
	return false ;
    }

    /**
     * Negotiate among the various variants for the Resource.
     * We made our best efforts to be as compliant as possible to the HTTP/1.0
     * content negotiation algorithm.
     * @param request the incomming request.
     * @return a RefourceReference instance.
     * @exception ProtocolException If one of the variants doesn't exist.
     */
    protected ResourceReference negotiate (Request request) 
	throws ProtocolException
    {
	// Check for zero or one variant:
	ResourceReference variants[] = getVariantResources() ;
	if (variants == null) {
	    try {
		getResource().delete();
	    } catch (MultipleLockException ex) {
		//will be deleted later...
	    } finally {
		Reply reply = request.makeReply(HTTP.NOT_FOUND);
		reply.setContent ("<h1>Document not found</h1>"+
				  "<p>The document "+request.getURL()+
				  " has no acceptable variants "+
				  "(probably deleted).");
		throw new HTTPException (reply);
	    }
	}
	if ( variants.length < 2 ) {
	    if ( variants.length == 0 ) {
		try {
		    getResource().delete();
		} catch (MultipleLockException ex) {
		    //will be deleted later...
		} finally {
		    Reply reply = request.makeReply(HTTP.NOT_FOUND);
		    reply.setContent ("<h1>Document not found</h1>"+
				      "<p>The document "+request.getURL()+
				      " has no acceptable variants "+
				      "(probably deleted).");
		    throw new HTTPException (reply);
		}
	    } else {
		return variants[0] ;
	    }
	}
	// Build a vector of variant negociation states, one per variants:
	Vector states = new Vector (variants.length) ;
	for (int i = 0 ; i < variants.length ; i++) {
	    double qs = 1.0 ;
	    try {
		FramedResource resource = 
		    (FramedResource) variants[i].unsafeLock() ;
		HTTPFrame itsframe = 
		    (HTTPFrame) resource.unsafeGetFrame(httpFrameClass);
		if (itsframe != null) {
		    if ( itsframe.unsafeDefinesAttribute (ATTR_QUALITY) )
			qs = itsframe.unsafeGetQuality() ;
		    if ( qs > REQUIRED_QUALITY )
			states.addElement(new VariantState (variants[i], qs)) ;
		}
	    } catch (InvalidResourceException ex) {
		//FIXME
	    } finally {
		variants[i].unlock();
	    }
	}
	// Content-encoding negociation:
	if ( debug ) {
	    printNegotiationState ("init:", states) ;
	}
	if ( negotiateContentEncoding (states, request) ) {
	    // Remains a single acceptable variant:
	    return ((VariantState) states.elementAt(0)).getResource() ;
	}
	if ( debug ) {
	    printNegotiationState ("encoding:", states) ;
	}
	// Charset quality negociation:
	if ( negotiateCharsetQuality (states, request) ) {
	    // Remains a single acceptable variant:
	    return ((VariantState) states.elementAt(0)).getResource() ;
	}
	if ( debug ) {
	    printNegotiationState ("charset:", states) ;
	}
	// Language quality negociation:
	if ( negotiateLanguageQuality (states, request) ) {
	    // Remains a single acceptable variant:
	    return ((VariantState) states.elementAt(0)).getResource() ;
	}
	if ( debug ) {
	    printNegotiationState ("language:", states) ;
	}
	// Content-type negociation:
	if ( negotiateContentType (states, request) ) {
	    // Remains a single acceptable variant:
	    return ((VariantState) states.elementAt(0)).getResource() ;
	}
	if ( debug ) {
	    printNegotiationState ("type:", states) ;
	}
	// If we reached this point, this means that multiple variants are 
	// acceptable at this point. Keep the ones that have the best quality.
	if ( debug ) {
	    printNegotiationState ("before Q selection:", states) ;
	}
	double qmax = REQUIRED_QUALITY ;

	for (int i=0; i< states.size() ; ) {
	    VariantState state = (VariantState) states.elementAt(i) ;
	    if ( state.getQ() > qmax ) {
		for (int j = i ; j > 0 ; j--)
		    states.removeElementAt(0) ;
		qmax = state.getQ() ;
		i = 1 ;
	    } else {
		if ( state.getQ() < qmax)
		    states.removeElementAt(i) ;
		else
		    i++;
	    }
	}
	if ( debug )
	    printNegotiationState ("After Q selection:", states) ;
	if ( qmax == REQUIRED_QUALITY ) {

	    Reply reply = request.makeReply(HTTP.NOT_ACCEPTABLE) ;
	     HtmlGenerator g = new HtmlGenerator("No acceptable");
	     g.append("<P>The resource cannot be served according to the "
		      + "headers sent</P>");
	     reply.setStream (g) ;
	    throw new HTTPException (reply) ;
	} else if ( states.size() == 1 ) {
	    return ((VariantState) states.elementAt(0)).getResource() ;
	} else {
	    // Respond with multiple choice (for the time being, there should
	    // be a parameter to decide what to do.
	    Reply reply = request.makeReply(HTTP.MULTIPLE_CHOICE) ;
	    HtmlGenerator g = new HtmlGenerator ("Multiple choice for "+
						 resource.getIdentifier()) ;
	    g.append ("<ul>") ;
	    for (int i = 0 ; i < states.size() ; i++) {
		VariantState state = (VariantState) states.elementAt(i) ;
		String name = null;
		ResourceReference rr = state.getResource();
		try {
		    name = rr.unsafeLock().getIdentifier();
		    g.append ("<li>" 
			      + "<a href=\"" + name + "\">" + name + "</a>"
			      + " Q= " + state.getQ()) ;
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr.unlock();
		}
	    }
	    reply.setStream (g) ;
	    reply.setHeaderValue(reply.H_VARY, getVary());
	    throw new HTTPException (reply) ;
	}
    }

   /**
     * "negotiate" for a PUT, the negotiation of a PUT should be 
     * different as we just want to match the desciption of the entity
     * and the available variants
     * @param request, the request to handle
     * @return a ResourceReference instance
     * @exception ProtocolException If negotiating among the resource variants 
     * failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    protected ResourceReference  negotiatePut(Request request) 
	throws ProtocolException, ResourceException
    {	
	// Check for zero or one variant:
	ResourceReference variants[] = getVariantResources() ;
	HTTPFrame itsframe;
	int nb_v;
	// zero, don't PUT on a negotiable resource! 
	if (variants == null ||  variants.length == 0) {
	    try {
		getResource().delete();
	    } catch (MultipleLockException ex) {
		//will be deleted later...
	    } finally {
		Reply reply = request.makeReply(HTTP.NOT_FOUND);
		reply.setContent ("<h1>Document not found</h1>"+
				  "<p>The document "+request.getURL()+
				  " has no acceptable variants "+
				  "(probably deleted).");
		throw new HTTPException (reply);
	    }
	}
	// negotiate etag
	HttpEntityTag etag = request.getETag();
	HttpEntityTag etags[] = request.getIfMatch();
	// gather the etags
	if (etags == null && etag != null) {
	    etags = new HttpEntityTag[1];
	    etags[0] = etag;
	} else if (etag != null) {
	    HttpEntityTag t_etags[] = new HttpEntityTag[etags.length+1];
	    System.arraycopy(etags, 0, t_etags, 0, etags.length);
	    t_etags[etags.length] = etag;
	    etags = t_etags;
	}

	if (etags != null) {
	    // yeah go for it!
	    FramedResource resource;
	    HttpEntityTag frametag;
	    for (int i = 0 ; i < variants.length ; i++) {
		try {
		    resource = (FramedResource)variants[i].unsafeLock() ;
		    itsframe = (HTTPFrame)resource.getFrame(httpFrameClass);
		    if (itsframe != null) {
			frametag = itsframe.getETag();
			if (frametag == null) {
			    continue;
			}
			// Do we have a winner?
			try {
			    for (int j=0; j<etags.length; j++)
				if(frametag.getTag().equals(etags[j].getTag()))
				    return variants[i];
			} catch (NullPointerException ex) {
			    // if the list of etag contains a null
			    // it should never happen and the try doesn't cost
			}
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    variants[i].unlock();
		}
	    }
	    // no matching variants...
	    Reply reply = request.makeReply(HTTP.NOT_FOUND);
	    reply.setContent ("<h1>Document not found</h1>"+
			      "<p>The document "+request.getURL()+
			      " has no acceptable variants "+
			      "according to the ETag sent");
	    throw new HTTPException (reply);
	}
	// if we are strict, don't go any further, etags
	// is the mandatory thing, otherwise PUT on the direct version
	if (getPutPolicy()) {
	    Reply reply = request.makeReply(HTTP.NOT_FOUND);
	    reply.setContent ("<h1>Document not found</h1>"+
			      "<p>The document "+request.getURL()+
			      " has no acceptable variants "+
			      " for a PUT, as no ETags were sent");
	    throw new HTTPException (reply);
	}
	// now filter out variants
	nb_v = variants.length;
	MimeType type = request.getContentType();
	String encodings[] = request.getContentEncoding();
	String languages[] = request.getContentLanguage();
	ResourceReference rr;

	if (type != null || encodings != null || languages != null) {
	    // the request is not too bad ;)
	    for (int i = 0 ; i < variants.length ; i++) {
		if (variants[i] == null)
		    continue;
		rr = variants[i];
		try {
		    resource = (FramedResource)rr.unsafeLock() ;
		    itsframe = (HTTPFrame)resource.getFrame(httpFrameClass);
		    if (itsframe == null) {
			nb_v--;
			variants[i] = null;
			continue;
		    }
		    // remove the non matching mime types
		    if (type != null) {
			MimeType fmt = itsframe.getContentType();
			if (fmt == null || (fmt.match(type) != 
					    MimeType.MATCH_SPECIFIC_SUBTYPE)) {
			    nb_v--;
			    variants[i] = null;
			    continue;
			}
		    }
		    // remove the non matching languages
		    if (languages != null) {
			String language = itsframe.getContentLanguage();
			nb_v--;
			variants[i] = null;
			if (language == null) {
			    continue;
			}
			for (int j=0; j<languages.length; j++) {
			    if (language.equals(languages[j])) {
				nb_v++;
				variants[i] = rr;
				break;
			    }
			}
		    }
		    // remove the non matching encodings		    
		    if (encodings != null) {
			String encoding = itsframe.getContentEncoding();
			nb_v--;
			variants[i] = null;
			if (encoding == null) {
			    continue;
			}
			for (int j=0; j<encodings.length; j++) {
			    if (encoding.equals(languages[j])) {
				nb_v++;
				variants[i] = rr;
				break;
			    }
			}
		    } 
		} catch (InvalidResourceException ex) {
		    //FIXME
		} finally {
		    rr.unlock();
		}
	    }
	    // a winner!
	    if (nb_v == 1) {
		for (int i=0; i< variants.length; i++) {
		    if (variants[i] != null)
			return variants[i];
		}
	    }
	    // no document matching
	    if (nb_v <= 0 ) {
		Reply reply = request.makeReply(HTTP.NOT_FOUND);
		reply.setContent ("<h1>Document not found</h1>"+
				  "<p>The document "+request.getURL()+
				  " has no acceptable variants "+
				  " for a PUT");
		throw new HTTPException (reply);
	    }
	}
	// now we have multiple choice :(
	String name;
	Reply reply = request.makeReply(HTTP.MULTIPLE_CHOICE) ;
	HtmlGenerator g = new HtmlGenerator ("Multiple choice for "+
					     resource.getIdentifier()) ;
	g.append ("<ul>") ;
	for (int i = 0 ; i < variants.length ; i++) {
	    if (variants[i] != null) {
		try {
		    name = variants[i].unsafeLock().getIdentifier();
		    g.append ("<li>" 
			      + "<a href=\"" + name + "\">" +name+ "</a>");
		} catch (InvalidResourceException ex) {
		    //FIXME (this should NOT happen :) )
		} finally {
		    variants[i].unlock();
		}
	    }
	}
	reply.setStream (g) ;
	reply.setHeaderValue(reply.H_VARY, getVary());
	throw new HTTPException (reply) ;
    }

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }

    /**
     * Lookup the target resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	if ( ls.isDirectory() ) { // we are not a directory, bail out
	    Request req = (Request)ls.getRequest();
	    String locstate = (String)req.getState(STATE_CONTENT_LOCATION);
	    if (locstate == null) {
		lr.setTarget(null);
		return true;
	    }
	}
	ResourceFrame frames[] = getFrames();
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if (frames[i] == null) {
		    continue;
		}
		if (frames[i].lookup(ls,lr)) {
		    return true;
		}
	    }
	}
	if ( ls.hasMoreComponents() ) {
	    // We are not a container resource, and we don't have children:
	    lr.setTarget(null);
	    return false;
	} else {
	    //we are done! try to find the negotiated one...
	    RequestInterface reqi = ls.getRequest();
	    ResourceReference selected;
	    Request request = (Request) reqi;
	    // Run content negotiation now:
	    // The PUT is special, we negotiate with ETag (if present) or
	    // using the metainformation about the PUT entity.
	    String method = request.getMethod ();
	    try {
		if (method.equals("PUT")) {
		    selected = negotiatePut(request);
		} else {
		    selected = negotiate(request);
		}
	    } catch (ResourceException ex) {
		// the failure will be processed in perform
		return false;
	    }
	    if (selected != null) {
		try {
		    FramedResource resource = 
			(FramedResource) selected.unsafeLock();
		    resource.lookup(ls, lr);
		} catch (InvalidResourceException ex) {
		    // the failure will be processed in perform
		} finally {
		    selected.unlock();
		}
	    }
	    request.setState(STATE_NEG, selected);
	    // fake now, we handle the process to have a two-level processing
	    // just to add the Vary: header
	    lr.setTarget(getResourceReference());
	    return true;
	}
    }

    /**
     * Perform an HTTP request.
     * Negotiate among the variants, the best variant according to the request
     * fields, and make this elected variant serve the request.
     * @param request The request to handle.
     * @exception ProtocolException If negotiating among the resource variants 
     * failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req)
	throws ProtocolException, ResourceException
    {
	ReplyInterface repi = performFrames(req);
	if (repi != null)
	    return repi;

	if (! checkRequest(req))
	    return null;

	Request request = (Request) req;
	ResourceReference selected;
	// get the right resources
	if (request.hasState(STATE_NEG)) {
	    selected = (ResourceReference) request.getState(STATE_NEG);
	} else {
	    String method = request.getMethod ();
	    if (method.equals("PUT")) {
		selected = negotiatePut(request);
	    } else {
		selected = negotiate(request);
	    }
	}

	// This should never happen: either the negotiation succeed, or the
	// negotiate method should return an error.
	if ( selected == null ) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("Error negotiating among resource's variants.");
	    throw new HTTPException(error) ;
	}

	try {
	    FramedResource resource = (FramedResource) selected.unsafeLock();
	    Reply reply = (Reply)resource.perform(request) ;
	    reply.setHeaderValue(reply.H_VARY, getVary());
	    HTTPFrame itsframe = 
		(HTTPFrame) resource.unsafeGetFrame(httpFrameClass);
	    if (itsframe != null) {
		reply.setContentLocation(
				  itsframe.getURL(request).toExternalForm()) ;
		return reply;
	    }
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("Error negotiating : "+
			     "selected resource has no HTTPFrame");
	    throw new HTTPException(error) ;
	} catch (InvalidResourceException ex) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("Error negotiating : Invalid selected resource");
	    throw new HTTPException(error) ;
	} finally {
	    selected.unlock();
	}
    }
}
