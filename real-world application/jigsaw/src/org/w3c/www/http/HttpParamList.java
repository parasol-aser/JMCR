// HttpParamList.java
// $Id: HttpParamList.java,v 1.1 2010/06/15 12:19:49 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import org.w3c.util.ArrayDictionary;

/**
 * This class parses name->value pairs, used in Authentication Info
 */

public class HttpParamList extends BasicValue {
    ArrayDictionary params = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse() 
	throws HttpParserException
    {
	ParseState ps = new ParseState(roff, rlen);

	this.params = new ArrayDictionary(4, 4);
	ps.prepare();

	// Parameters parsing
	ParseState it = new ParseState();
	it.separator  = (byte) '=';
	ps.separator = (byte) ',';
	while (HttpParser.nextItem(raw, ps) >= 0 ) {
	    // Get the param name:
	    it.prepare(ps);
	    if (HttpParser.nextItem(raw, it) < 0)
		error("Invalid param list: bad param name.");
	    String key = it.toString(raw, true);
	    // Get the param value:
	    it.prepare();
	    if ( HttpParser.nextItem(raw, it) < 0)
		error("Invalid param list: no param value.");
	    it.ioff = it.start;
	    HttpParser.unquote(raw, it);
	    params.put(key, it.toString(raw));
	    ps.prepare();
	}
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer();
	int len = params.size();
	for (int i = 0 ; len > 0 ; i++) {
	    String key = (String) params.keyAt(i);
	    if ( key == null )
		continue;
	    buf.appendQuoted(key, (byte)'=', (String)params.elementAt(i));
	    len--;
	    if (len > 0) {
		buf.append((byte)',');
	    }
	}
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Get a parameter.
     * @param name The name of the parameter to fetch.
     * @return The String value, or <strong>null</strong> if undefined.
     */

    public String getParameter(String name) {
	validate();
	return (params == null) ? null : (String) params.get(name);
    }

    /**
     * Set an authentication parameter.
     * @param name The name of the authentication parameter.
     * @param value The value of the authentication parameter.
     */

    public void setParameter(String name, String value) {
	validate();
	if ( params == null )
	    params = new ArrayDictionary(4, 4);
	params.put(name, value);
    }

    public HttpParamList(boolean isValid) {
	this.isValid = isValid;
    }

    public HttpParamList() {
	this.isValid = false;
    }
}


