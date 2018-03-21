// HttpEntityTag.java
// $Id: HttpEntityTag.java,v 1.1 2010/06/15 12:19:52 smhuang Exp $$
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package  org.w3c.www.http;

public class HttpEntityTag extends BasicValue {
    boolean weak = false;
    String  tag  = null;

    HttpEntityTagList list = null;

    /**
     * parse the byte value as an entity tag.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	ParseState ps = new ParseState();
	ps.ioff      = roff;
	ps.bufend    = rlen;
	ps.separator = (byte) '/';
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid entity tag.");
	if ((raw[ps.start] == 'W') || (raw[ps.start] == 'w')) {
	    weak = true;
	    ps.prepare();
	    if ( HttpParser.nextItem(raw, ps) < 0 )
		error("Invalid weak entity tag.");
	    ps.ioff   = ps.start;
	    ps.bufend = ps.end;
	    HttpParser.unquote(raw, ps);
	    tag = ps.toString(raw);
	} else {
	    weak = false;
	    ps.ioff   = ps.start;
	    ps.bufend = ps.end;
	    HttpParser.unquote(raw, ps);
	    tag  = ps.toString(raw);
	}
    }

    protected void invalidateByteValue() {
	super.invalidateByteValue();
	if ( list != null )
	    list.invalidateByteValue();
    }

    /**
     * Update the byte value to reflect any changes in the parsed value.
     */

    protected void updateByteValue() {
	validate();
	HttpBuffer buf = new HttpBuffer(20) ;
	if ( weak ) {
	    buf.append('W');
	    buf.append('/');
	}
	buf.appendQuoted(tag);
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	return this;
    }

    /**
     * Is this a weak entity tag ?
     * @return A boolean <strong>true</strong> if the tag is weak.
     */

    public boolean isWeak() {
	validate();
	return weak;
    }

    /**
     * Set this tag weakness.
     * @param onoff A boolean, tag is weak if <strong>true</strong>.
     */

    public void setWeak(boolean onoff) {
	if ( onoff != weak )
	    invalidateByteValue();
	weak = onoff;
    }

    /**
     * Get this tag value.
     * @return A String giving the entity tag value.
     */

    public String getTag() {
	validate();
	return tag;
    }

    /**
     * Set this tag value.
     * @param tag The new tag value.
     */

    public void setTag(String tag) {
	if ( ! tag.equals(this.tag) )
	    invalidateByteValue();
	this.tag = tag;
    }

    HttpEntityTag(HttpEntityTagList list, byte raw[], int roff, int rlen) {
	this.list    = list;
	this.raw     = raw;
	this.isValid = false;
	this.roff    = roff;
	this.rlen    = rlen;
    }

    HttpEntityTag(boolean isValid, boolean weak, String tag) {
	this.isValid = isValid;
	this.weak    = weak;
	this.tag     = tag;
    }

    public HttpEntityTag() {
	this.isValid = false;
    }

}
