// DAVEntityTag.java
// $Id: DAVEntityTag.java,v 1.1 2010/06/15 12:27:43 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import org.w3c.www.http.HttpEntityTag;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVEntityTag extends HttpEntityTag {

    boolean isnot = false;

    public boolean isNot() {
	return isnot;
    }

    public String toString() {
	if (isnot) {
	    return "Not "+toExternalForm();
	} else {
	    return toExternalForm();
	}
    }

    public DAVEntityTag(byte raw[], int off, int len, boolean isnot) {
	super();
	addBytes(raw, off, len);
	this.isnot = isnot;
    }
    
}
