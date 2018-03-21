// HttpCacheControl.java
// $Id: HttpCacheControl.java,v 1.1 2010/06/15 12:19:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The parsed cache-control directive of HTTP/1.1
 * This object gives access to the parsed value of the cache-control
 * directive of HTTP/1.1, as defined in section 14.9 of the specification.
 */

public class HttpCacheControl extends BasicValue {
    private static final String EMPTY_LIST[] = { };

    private String  nocache[] = null ;
    private boolean nostore  = false;
    private int     maxage   = -1;
    private int     maxstale = -1;
    private int     minfresh = -1;
    private boolean onlyifcached = false;
    private boolean pub = false ;
    private String  priv[] = null ;
    private boolean notransform = false;
    private boolean mustrevalidate = false;
    private boolean proxyrevalidate = false;
    private int     s_maxage = -1;
    private Vector  extensions = null;

    private static final int NOCACHE = (1<<0);
    private static final int NOSTORE = (1<<1);
    private static final int MAXAGE  = (1<<2);
    private static final int MINVERS = (1<<3);
    private static final int ONLYIFCACHED = (1<<4);
    private static final int PUB = (1<<5);
    private static final int PRIV = (1<<6);
    private static final int NOTRANSFORM = (1<<7);
    private static final int MUSTREVALIDATE = (1<<8);
    private static final int PROXYREVALIDATE = (1<<9);
    private static final int MAXSTALE = (1<<10);
    private static final int MINFRESH = (1<<11);
    private static final int S_MAXAGE = (1<<12);

    private int defined = 0 ;

    /**
     * Check if the given field has been explictly set.
     * @param id The field identifier.
     * @return A boolean.
     */

    private final boolean checkDirective(int id) {
	return (defined & id) != 0;
    }

    /**
     * Mark the given field as being set.
     * @param id The field identifier.
     */

    private final void setDirective(int id) {
	defined |= id;
    }

    /**
     * Mark the given field as being unset.
     * @param id The field identifier.
     */

    private final void unsetDirective(int id) {
	defined &= (~id);
    }

    /**
     * Recompute the byte value for this header.
     */

    protected void updateByteValue() {
	// Dump the value, using my own version of the StringBuffer:
	HttpBuffer buf = new HttpBuffer();
	boolean    cnt = false;
	if (checkDirective(NOCACHE)) {
	    if ( nocache != null )
		buf.appendQuoted("no-cache", (byte) '=', nocache);
	    else
		buf.append("no-cache");
	    cnt = true;
	}
	if(checkDirective(NOSTORE) && nostore) {
	    buf.append("no-store");
	    cnt = true;
	}
	if (checkDirective(MAXAGE) && (maxage >= 0)) {
	    if ( cnt )
		buf.append(',');
	    buf.append("max-age", (byte) '=', maxage);
	    cnt = true;
	}
	if (checkDirective(ONLYIFCACHED) && onlyifcached) {
	    if ( cnt )
		buf.append(',');
	    buf.append("only-if-cached");
	    cnt = true;
	}
	if (checkDirective(PUB) && pub) {
	    if ( cnt )
		buf.append(',');
	    buf.append("public");
	    cnt = true;
	}
	if (checkDirective(PRIV) && (priv != null)) {
	    if ( cnt )
		buf.append(',');
	    buf.appendQuoted("private", (byte) '=', priv);
	    cnt = true;
	}
	if (checkDirective(NOTRANSFORM) && notransform) {
	    if ( cnt )
		buf.append(',');
	    buf.append("no-transform");
	    cnt = true;
	}
	if (checkDirective(MUSTREVALIDATE) && mustrevalidate) { 
	    if ( cnt )
		buf.append(',');
	    buf.append("must-revalidate");
	    cnt = true;
	}
	if (checkDirective(PROXYREVALIDATE) && proxyrevalidate) {
	    if ( cnt )
		buf.append(',');
	    buf.append("proxy-revalidate");
	    cnt = true;
	}
	if (checkDirective(MAXSTALE) && (maxstale >= 0)) {
	    if ( cnt )
		buf.append(',');
	    buf.append("max-stale", (byte) '=', maxstale);
	    cnt = true;
	}
	if (checkDirective(MINFRESH) && (minfresh >= 0)) {
	    if ( cnt )
		buf.append(',');
	    buf.append("min-fresh", (byte) '=', minfresh);
	    cnt = true;
	}
	if (checkDirective(S_MAXAGE) && (s_maxage >= 0)) {
	    if ( cnt )
		buf.append(',');
	    buf.append("s_maxage", (byte) '=', s_maxage);
	    cnt = true;
	}
	if (extensions != null) {
	    Enumeration e = extensions.elements();
	    while (e.hasMoreElements()) {
		if (cnt) {
		    buf.append(',');
		    cnt = true;
		}
		buf.append((String) e.nextElement());
	    }
	}
	// Keep track of the string, for potential reuse:
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    // Efficient regular expression is what I would do in C

    private static byte bnocache[] = { 
	(byte) 'n', (byte) 'o', (byte) '-', (byte) 'c', 
	(byte) 'a', (byte) 'c', (byte) 'h', (byte) 'e' 
    } ;
    private static byte bnostore[] = {
	(byte) 'n', (byte) 'o', (byte) '-', (byte) 's',
	(byte) 't', (byte) 'o', (byte) 'r', (byte) 'e'
    };
    private static final byte bmaxage[] = {
	(byte) 'm', (byte) 'a', (byte) 'x', (byte) '-',
	(byte) 'a', (byte) 'g', (byte) 'e'
    };
    private static final byte bs_maxage[] = {
	(byte) 's', (byte) '-', (byte) 'm', (byte) 'a',
	(byte) 'x', (byte) 'a', (byte) 'g', (byte) 'e'
    };
    private static final byte bmaxstale[] = {
	(byte) 'm', (byte) 'a', (byte) 'x', (byte) '-', 
	(byte) 's', (byte) 't', (byte) 'a', (byte) 'l',
	(byte) 'e'
    };
    private static final byte bminfresh[] = {
	(byte) 'm', (byte) 'i', (byte) 'n', (byte) '-',
	(byte) 'f', (byte) 'r', (byte) 'e', (byte) 's',
	(byte) 'h'
    };
    private static final byte bonlyifcached[] = {
	(byte) 'o', (byte) 'n', (byte) 'l', (byte) 'y', 
	(byte) '-', (byte) 'i', (byte) 'f', (byte) '-', 
	(byte) 'c', (byte) 'a', (byte) 'c', (byte) 'h', 
	(byte) 'e', (byte) 'd'
    };
    private static final byte bpublic[] = {
	(byte) 'p', (byte) 'u', (byte) 'b', (byte) 'l',
	(byte) 'i', (byte) 'c', 
    };
    private static final byte bprivate[] = {
	(byte) 'p', (byte) 'r', (byte) 'i', (byte) 'v',
	(byte) 'a', (byte) 't', (byte) 'e'
    };
    private static final byte bnotransform[] = {
	(byte) 'n', (byte) 'o', (byte) '-', (byte) 't',
	(byte) 'r', (byte) 'a', (byte) 'n', (byte) 's',
	(byte) 'f', (byte) 'o', (byte) 'r', (byte) 'm' 
    };
    private static final byte bmustrevalidate[] = {
	(byte) 'm', (byte) 'u', (byte) 's', (byte) 't',
	(byte) '-', (byte) 'r', (byte) 'e', (byte) 'v', 
	(byte) 'a', (byte) 'l', (byte) 'i', (byte) 'd', 
	(byte) 'a', (byte) 't', (byte) 'e'
    };
    private static final byte bproxyrevalidate[] = {
	(byte) 'p', (byte) 'r', (byte) 'o', (byte) 'x',
	(byte) 'y', (byte) '-', (byte) 'r', (byte) 'e',
	(byte) 'v', (byte) 'a', (byte) 'l', (byte) 'i', 
	(byte) 'd', (byte) 'a', (byte) 't', (byte) 'e'
    };

    // Parse a valued-directive
    private final void parseDirective(int ds, int de, ParseState pval)
	throws HttpParserException
    {
	if (HttpParser.compare(raw, ds, de, bmaxage) == 0) {
	    pval.ioff   = pval.start;
	    pval.bufend = pval.end;
	    maxage = HttpParser.parseInt(raw, pval);
	    setDirective(MAXAGE);
	} else if (HttpParser.compare(raw, ds, de, bmaxstale) == 0) {
	    pval.ioff   = pval.start;
	    pval.bufend = pval.end;
	    maxstale = HttpParser.parseInt(raw, pval);
	    setDirective(MAXSTALE);
	} else if (HttpParser.compare(raw, ds, de, bminfresh) == 0) {
	    pval.ioff   = pval.start;
	    pval.bufend = pval.end;
	    minfresh = HttpParser.parseInt(raw, pval);
	    setDirective(MINFRESH);
	} else if (HttpParser.compare(raw, ds, de, bs_maxage) == 0) {
	    pval.ioff   = pval.start;
	    pval.bufend = pval.end;
	    s_maxage = HttpParser.parseInt(raw, pval);
	    setDirective(S_MAXAGE);
	} else if (HttpParser.compare(raw, ds, de, bnocache) == 0) {
	    Vector fields = new Vector(8);
	    ParseState sp = new ParseState();
	    sp.ioff       = pval.start;
	    sp.bufend     = pval.end;
	    HttpParser.unquote(raw, sp);
	    while (HttpParser.nextItem(raw, sp) >= 0) {
		fields.addElement(new String(raw,0,sp.start,sp.end-sp.start));
		sp.prepare();
	    }
	    if (nocache == null) {
		nocache = new String[fields.size()];
		fields.copyInto(nocache);
	    } else {
		int num = fields.size();
		String t_nocache[] = new String[nocache.length+num];
		System.arraycopy(nocache, 0, t_nocache, 0, nocache.length);
		for (int i=0; i< num; i++) {
		    t_nocache[nocache.length+i] = (String) fields.elementAt(i);
		}
		nocache = t_nocache;
	    }
	    setDirective(NOCACHE);
	} else if (HttpParser.compare(raw, ds, de, bprivate) == 0) {
	    Vector fields = new Vector(8);
	    ParseState sp = new ParseState();
	    sp.ioff   = pval.start;
	    sp.bufend = pval.end;
	    HttpParser.unquote(raw, sp);
	    while (HttpParser.nextItem(raw, sp) >= 0) {
		fields.addElement(new String(raw,0,sp.start,sp.end-sp.start));
		sp.prepare();
	    }
	    if (priv == null) {
		priv = new String[fields.size()];
		fields.copyInto(priv);
	    } else {
		int num = fields.size();
		String t_priv[] = new String[priv.length+num];
		System.arraycopy(priv, 0, t_priv, 0, priv.length);
		for (int i=0; i< num; i++) {
		    t_priv[priv.length+i] = (String) fields.elementAt(i);
		}
		priv = t_priv;
	    }
	    setDirective(PRIV);
	} else {
	    if (extensions == null) {
		extensions = new Vector(4);
		extensions.add(new String(raw,0,ds,de-ds));
	    }
//	    error("Unknown directive "+new String(raw, 0, ds, de-ds));
	}
    }

    // Parse a boolean directive
    private final void parseDirective(int ds, int de)
	throws HttpParserException
    {
	if (HttpParser.compare(raw, ds, de, bnocache) == 0) {
	    setDirective(NOCACHE);
	    nocache = EMPTY_LIST;
	} else if (HttpParser.compare(raw, ds, de, bnostore) == 0) {
	    setDirective(NOSTORE);
	    nostore = true;
	} else if (HttpParser.compare(raw, ds, de, bonlyifcached) == 0) {
	    setDirective(ONLYIFCACHED);
	    onlyifcached = true;
	} else if (HttpParser.compare(raw, ds, de, bpublic) == 0) {
	    setDirective(PUB);
	    pub = true;
	} else if (HttpParser.compare(raw, ds, de, bprivate) == 0) {
	    setDirective(PRIV);
	    priv = EMPTY_LIST;
	} else if (HttpParser.compare(raw, ds, de, bnotransform) == 0) {
	    setDirective(NOTRANSFORM);
	    notransform = true;
	} else if (HttpParser.compare(raw, ds, de, bmustrevalidate) == 0) {
	    setDirective(MUSTREVALIDATE);
	    mustrevalidate = true;
	} else if (HttpParser.compare(raw, ds, de, bproxyrevalidate) == 0) {
	    setDirective(PROXYREVALIDATE);
	    proxyrevalidate = true;
	} else {
	    if (extensions == null) {
		extensions = new Vector(4);
		extensions.add(new String(raw,0,ds,de-ds));
	    }
//	    error("Unknown or invalid directive: "+new String(raw,0,ds,de));
	}
    }

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	// Parse the raw value, this is the right time to do it:
	ParseState ls = new ParseState(0);
	ls.ioff       = 0;
	ls.bufend     = raw.length;
	ls.separator  = (byte) ',';
	ls.spaceIsSep = false; // gift for broken implementations
	ParseState ld = new ParseState(0);
	ld.separator  = (byte) '=';
	ld.spaceIsSep = false; // gift for broken implementations
//	HttpParser.unquote(raw, ls); 
	while (HttpParser.nextItem(raw, ls) >= 0) {
	    ld.bufend = ls.end;
	    ld.ioff   = ls.start;
	    if ( HttpParser.nextItem(raw, ld) >= 0 ) {
		int dstart = ld.start;
		int dend   = ld.end;
		ld.prepare();
		if ( HttpParser.nextItem(raw, ld) >= 0 )
		    parseDirective(dstart, dend, ld);
		else
		    parseDirective(dstart, dend);
	    } else {
		parseDirective(ls.start, ls.end);
	    }
	    ls.prepare();
	}
    }
	
    /**
     * HeaderValue implementation - Get this header value.
     * @return Itself !
     */

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Get and test the no-cache directive setting.
     * @return A field-list as an array of String, or <strong>null</strong>
     * if the directive is undefined.
     */

    public String[] getNoCache() {
	validate();
	return checkDirective(NOCACHE) ? nocache : null;
    }

    /**
     * Set the no cache directive to the given list of fields.
     * @param fields The fields to set in the no-cache directive, encoded
     * as a String array (whose length can be <strong>0</strong>), or
     * <strong>null</strong> to reset the value.
     */

    public void setNoCache(String fields[]) {
	validate();
	if ( fields == null ) {
	    if ( checkDirective(NOCACHE) )
		invalidateByteValue();
	    unsetDirective(NOCACHE) ;
	} else {
	    setDirective(NOCACHE);
	    nocache = fields;
	    invalidateByteValue();
	}
    }

    /**
     * Set the <code>no-cache</code> directive globally.
     */

    public void setNoCache() {
	validate();
	setDirective(NOCACHE);
	invalidateByteValue();
    }

    /**
     * Add the given header name to the <code>no-cache</code> directive.
     * @param name The header name to add.
     */

    public void addNoCache(String name) {
	validate();
	// If no-cache is set globally, then skip...
	if ( checkDirective(NOCACHE) && (nocache.length == 0))
	    return;
	// Add or test for presence
	if ( nocache != null ) {
	    // Check for that header name presence:
	    for (int i = 0 ; i < nocache.length ; i++)
		if (nocache[i].equalsIgnoreCase(name))
		    return;
	    invalidateByteValue();
	    String newcache[] = new String[nocache.length+1];
	    System.arraycopy(nocache, 0, newcache, 0, nocache.length);
	    newcache[nocache.length] = name;
	    nocache = newcache;
	} else {
	    invalidateByteValue();
	    nocache    = new String[1];
	    nocache[0] = name;
	}
	setDirective(NOCACHE);
    }

    /**
     * Unset the <code>no-cache</code> directive.
     */

    public void unsetNoCache() {
	validate();
	if ( checkDirective(NOCACHE) ) {
	    invalidateByteValue();
	    unsetDirective(NOCACHE);
	    nocache = null;
	}
    }

    /**
     * Is the no-store flag set ?
     * @return A boolean.
     */

    public boolean checkNoStore() {
	validate();
	return checkDirective(NOSTORE) ? nostore : false;
    }

    /**
     * Set the <code>no-store</code> flag.
     * @param onoff The value for the no-store directive.
     */

    public void setNoStore(boolean onoff) {
	validate();
	if ( onoff == false ) {
	    if ( nostore ) {
		invalidateByteValue();
		nostore = false;
	    }
	    unsetDirective(NOSTORE);
	} else if ( ! nostore ) {
	    invalidateByteValue();
	    setDirective(NOSTORE);
	    nostore = true;
	}
    }

    /**
     * Get the max-age value defined by this cache control policy.
     * @return The max-age value, or <strong>-1</strong> if undefined.
     */

    public final int getMaxAge() {
	validate();
	return checkDirective(MAXAGE) ? maxage : -1;
    }

    /**
     * Set the max-age directive for this cache control.
     * @param age The max allowed age for the cache control policy, or
     * <strong>-1</strong> to reset value.
     */

    public void setMaxAge(int age) {
	validate();
	if ( age == -1 ) {
	    if ( checkDirective(MAXAGE) )
		invalidateByteValue();
	    maxage = -1;
	    unsetDirective(MAXAGE);
	} else {
	    if ((age != maxage) || ! checkDirective(MAXAGE))
		invalidateByteValue();
	    setDirective(MAXAGE);
	    maxage = age;
	}
    }

    /**
     * Get the s-maxage value defined by this cache control policy.
     * @return The s-maxage value, or <strong>-1</strong> if undefined.
     */
    public final int getSMaxAge() {
	validate();
	return checkDirective(S_MAXAGE) ? s_maxage : -1;
    }

    /**
     * Set the s_maxage directive for this cache control.
     * @param age The max allowed age for the cache control policy, or
     * <strong>-1</strong> to reset value.
     */
    public void setSMaxAge(int age) {
	validate();
	if ( age == -1 ) {
	    if ( checkDirective(S_MAXAGE) )
		invalidateByteValue();
	    s_maxage = -1;
	    unsetDirective(S_MAXAGE);
	} else {
	    if ((age != s_maxage) || ! checkDirective(S_MAXAGE))
		invalidateByteValue();
	    setDirective(S_MAXAGE);
	    s_maxage = age;
	}
    }

    /**
     * Get the max-stale value defined by this control object.
     * @return The max-stale value, or <strong>-1</strong> if undefined.
     */

    public int getMaxStale() {
	validate();
	return checkDirective(MAXSTALE) ? maxstale : -1;
    }

    /**
     * Set the max-stale directive value.
     * @param stale The max-stale value, or <strong>-1</strong> to reset value.
     */

    public void setMaxStale(int stale) {
	validate();
	if ( stale == -1 ) {
	    if ( checkDirective(MAXSTALE) )
		invalidateByteValue();
	    maxstale = -1;
	    unsetDirective(MAXSTALE);
	} else {
	    if ((stale != maxstale) || ! checkDirective(MAXSTALE))
		invalidateByteValue();
	    setDirective(MAXSTALE);
	    maxstale = stale;
	}
    }

    /**
     * Get the min-fresh directive value.
     * @param def The default value to reurn if undefined.
     */

    public int getMinFresh() {
	validate();
	return checkDirective(MINFRESH) ? minfresh : -1;
    }

    /**
     * Set the minfresh directive value.
     * @param fresh The new minfresh value, or <strong>-1</strong> to reset
     * value.
     */

    public void setMinFresh(int fresh) {
	validate();
	if ( fresh == -1 ) {
	    if ( checkDirective(MINFRESH) )
		invalidateByteValue();
	    minfresh = -1;
	    unsetDirective(MINFRESH);
	} else {
	    if ((fresh != minfresh) || ! checkDirective(MINFRESH))
		invalidateByteValue();
	    setDirective(MINFRESH);
	    minfresh = fresh;
	}
    }

    /**
     * Is the on-if-cached flag value set ?
     * @return A boolean.
     */

    public boolean checkOnlyIfCached() {
	validate();
	return onlyifcached;
    }

    /**
     * Set the only-if-cached directive.
     * @param onoff The boolean value for the directive.
     */

    public void setOnlyIfCached(boolean onoff) {
	validate();
	if ( ! onoff ) {
	    if ( onlyifcached ) {
		invalidateByteValue();
		onlyifcached = false;
	    }
	    unsetDirective(ONLYIFCACHED);
	} else if ( ! onlyifcached ) {
	    invalidateByteValue();
	    setDirective(ONLYIFCACHED);
	    onlyifcached = true;
	}
    }

    /**
     * Is the public flag set ?
     * @return A boolean.
     */

    public boolean checkPublic() {
	validate();
	return pub;
    }

    /**
     * Set the public directive.
     * @param onoff The public directive value.
     */

    public void setPublic(boolean onoff) {
	validate();
	if ( ! onoff ) {
	    if ( pub ) {
		invalidateByteValue();
		pub = false;
	    }
	    unsetDirective(PUB);
	} else if ( ! pub ) {
	    invalidateByteValue();
	    setDirective(PUB);
	    pub = true;
	}
    }

    /**
     * Check and get the private value.
     * @param def The default value if undefined.
     * @return A list of field-names, as a String array, or the provided
     * default value otherwise.
     */

    public String[] getPrivate() {
	validate();
	return (priv == null) ? null : priv;
    }

    /**
     * Set the private directive value.
     * @param priv The list of field-names as a String array.
     */

    public void setPrivate(String priv[]) {
	validate();
	invalidateByteValue();
	setDirective(PRIV);
	this.priv = priv;
    }

    /**
     * Unset the <code>private</code> directive.
     */

    public void unsetPrivate() {
	validate();
	if ( checkDirective(PRIV) )
	    invalidateByteValue();
	unsetDirective(PRIV);
	priv = null;
    }

    /**
     * Is the no-transform flag set ?
     * @return A boolean.
     */

    public boolean checkNoTransform() {
	validate();
	return notransform;
    }

    /**
     * Set the no-transform directive.
     * @param onoff The new boolean value for the no-transform directive.
     */

    public void setNoTransform(boolean onoff) {
	validate();
	if ( ! onoff ) {
	    if ( notransform ) {
		invalidateByteValue();
		notransform = false;
	    }
	    unsetDirective(NOTRANSFORM);
	} else if ( ! notransform ) {
	    invalidateByteValue();
	    setDirective(NOTRANSFORM);
	    notransform = true;
	}
    }

    /**
     * Is the must-revalidate flag set ?
     * @return A boolean.
     */

    public boolean checkMustRevalidate() {
	validate();
	return mustrevalidate;
    }

    /**
     * Set the must-revalidate directive.
     * @param onoff The new value for the must-revalidate directive.
     */

    public void setMustRevalidate(boolean onoff) {
	validate();
	if ( ! onoff ) {
	    if ( mustrevalidate ) {
		invalidateByteValue();
		mustrevalidate = false;
	    }
	    unsetDirective(MUSTREVALIDATE);
	} else if ( ! mustrevalidate ) {
	    invalidateByteValue();
	    setDirective(MUSTREVALIDATE);
	    mustrevalidate = true;
	}
    }

    /**
     * Is the proxy-revalidate flag set ?
     * @return A boolean.
     */

    public boolean checkProxyRevalidate() {
	validate();
	return proxyrevalidate;
    }

    /**
     * Set the proxy-revalidate directive.
     * @param onoff The new proxy-revalidate value.
     */

    public void setProxyRevalidate(boolean onoff) {
	validate();
	if ( ! onoff ) {
	    if ( proxyrevalidate ) {
		invalidateByteValue();
		proxyrevalidate = false;
	    }
	    unsetDirective(PROXYREVALIDATE);
	} else if ( ! proxyrevalidate ) {
	    invalidateByteValue();
	    setDirective(PROXYREVALIDATE);
	    proxyrevalidate = true;
	}
    }

    /**
     * Create a new empty HttpCacheControl object descriptor.
     * @param isValid A boolean indicating if this object will be filled in
     * by parsing a value, or is for internal purposes.
     */

    HttpCacheControl(boolean isValid) {
	this.raw     = null ;
	this.isValid = isValid;
    }

    /**
     * Create a new empty cache control object descriptor.
     * The value will be provided through parsing.
     */

    public HttpCacheControl() {
	this(false);
    }

}
