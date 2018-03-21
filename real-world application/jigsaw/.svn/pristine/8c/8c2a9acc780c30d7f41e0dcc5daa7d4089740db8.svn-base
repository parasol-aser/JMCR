// DAVStatusURIList.java
// $Id: DAVStatusURIList.java,v 1.1 2010/06/15 12:27:42 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import java.util.Vector;

import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVStatusURIList extends BasicValue {

    public static final boolean debug = false;
    
    DAVStatusURI statusURIs[] = null;

    protected void parse() {
	Vector vstatus = new Vector();
	if (debug) {
	    System.out.println("PARSING STATUS URI HEADER");
	}
	ParseState list = new ParseState(0, raw.length);
	list.separator  = (byte) ')';
	list.spaceIsSep = false;
	ParseState blist = new ParseState(0, 0);
	blist.separator  = (byte) '(';
	blist.spaceIsSep = false;
	DAVStatusURI dsu = null;
	while (DAVParser.nextItem(raw, list) >= 0) {
	    blist.prepare(list);
	    while (DAVParser.nextItem(raw, blist) >= 0) {
		dsu = new DAVStatusURI(raw, blist.start, blist.end);
		addStatusURI(dsu);
	    }
	}
    }

    protected void updateByteValue() {
	if (statusURIs != null) {
	    StringBuffer buf = new StringBuffer();
	    for (int i = 0; i < statusURIs.length ; i++) {
		DAVStatusURI dsu = statusURIs[i];
		buf.append("(").append(dsu.getStatus()).append(" ");
		buf.append("<").append(dsu.getURI()).append(">) ");
	    }
	    raw  = buf.toString().getBytes();
	    roff = 0;
	    rlen = raw.length;
	} else {
	    raw  = new byte[0];
	    roff = 0;
	    rlen = 0;
	}
    }

    public void addStatusURI(DAVStatusURI su) {
	if ( statusURIs == null ) {
	    statusURIs    = new DAVStatusURI[1];
	    statusURIs[0] = su;
	} else {
	    int len = statusURIs.length;
	    DAVStatusURI nsu[] = new DAVStatusURI[len+1];
	    System.arraycopy(statusURIs, 0, nsu, 0, len);
	    nsu[len]   = su;
	    statusURIs = nsu;
	}
    }

    public Object getValue() {
	validate();
	return statusURIs;
    }

    /**
     * Don't use this constructor
     */
    public DAVStatusURIList() {
	this.isValid = false;
    }

    public DAVStatusURIList(DAVStatusURI dsu[]) {
	this.isValid    = true;
	this.statusURIs = dsu;
    }

    public DAVStatusURIList(DAVStatusURI dsu) {
	this.isValid    = true;
	this.statusURIs = new DAVStatusURI[1];
	statusURIs[0] = dsu;
    }
    
}
