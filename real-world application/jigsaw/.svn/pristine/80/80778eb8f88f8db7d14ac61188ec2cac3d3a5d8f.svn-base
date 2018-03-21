// DAVIfList.java
// $Id: DAVIfList.java,v 1.1 2010/06/15 12:27:43 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import java.util.Vector;
import java.util.LinkedList;
import java.util.ListIterator;

import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVIfList extends BasicValue {

    public static final boolean debug = false;

    DAVIf davifs[] = null;

    boolean tagged = false;

    protected void parse() {
	Vector vdavifs = new Vector();

	if (debug) {
	    System.out.println("PARSING IF HEADER");
	}
	// for end of list ( ... )
	ParseState list = new ParseState(0, raw.length);
	list.separator  = (byte) ')';
	list.spaceIsSep = false;
	// for beginning of list
	ParseState blist = new ParseState(0, 0);
	blist.separator  = (byte) '(';
	blist.spaceIsSep = false;
	// for state token <...>
	ParseState st = new ParseState(0, 0);
	st.separator = (byte) '>';
	st.spaceIsSep = false;

	DAVIf davif = null;
	if (DAVParser.nextItem(raw, list) >= 0) {
	    if (DAVParser.startsWith(raw, list, '<')) {
		tagged = true;
		do {
		    if (DAVParser.startsWith(raw, list, '<')) {
			davif = null;
		    }
		    blist.prepare(list);
		    while (DAVParser.nextItem(raw, blist) >= 0) {
			// the tagged resource
			if ((DAVParser.startsWith(raw, blist, '<')) &&
			    (davif == null)) 
			{
			    davif = new DAVIf(blist.toString(raw));
			    vdavifs.addElement(davif);
			    if (debug) {
				System.out.println("Res : "+
						   davif.getResource());
			    }
			    //list.start = st.end;
			} else {
			    // another list for our tagged resource
			    if (debug) {
				String slist = blist.toString(raw);
				System.out.println("LIST : "+slist);
			    }
			    davif.addList(raw, blist.start, blist.end);
			}
		    }
		} while (DAVParser.nextItem(raw, list) >= 0);
	    } else {
		// (... ... ...) (... ... ...)
		tagged = false;
		davif  = new DAVIf();
		vdavifs.addElement(davif);
		do {
		    blist.prepare(list);
		    while (DAVParser.nextItem(raw, blist) >= 0) {
			if (debug) {
			    System.out.println(blist.toString(raw));
			}
			davif.addList(raw, blist.start, blist.end);
		    }
		} while (DAVParser.nextItem(raw, list) >= 0);
	    }
	}
	// Ok good
	davifs = new DAVIf[vdavifs.size()];
	vdavifs.copyInto(davifs);
    }

    protected void updateByteValue() {
	if (davifs != null) {
	    StringBuffer buf = new StringBuffer();
	    for (int i = 0; i < davifs.length ; i++) {
		DAVIf davif = davifs[i];
		if (davif.hasResource()) {
		    buf.append("<").append(davif.getResource()).append("> ");
		}
		ListIterator iterator = davif.getTokenListIterator();
		while (iterator.hasNext()) {
		    LinkedList   list = (LinkedList) iterator.next();
		    ListIterator it   = list.listIterator(0);
		    buf.append("(");
		    while (it.hasNext()) {
			Object token = (String) it.next();
			String item  = token.toString();
			if (it.hasNext()) {
			    buf.append(item).append(" ");
			} else {
			    buf.append(item);
			}
		    }
		    buf.append(")");
		}
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

    public Object getValue() {
	validate();
	return davifs;
    }

    public boolean isTaggedList() {
	return tagged;
    }

    public DAVIfList() {
	this.isValid = false;
    }
    
}
