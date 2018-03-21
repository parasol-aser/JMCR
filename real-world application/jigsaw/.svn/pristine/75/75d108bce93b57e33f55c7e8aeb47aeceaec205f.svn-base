// CCPPWarning.java
// $Id: CCPPWarning.java,v 1.1 2010/06/15 12:28:21 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ccpp;

import java.util.Vector;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CCPPWarning {

    Vector warnings = null;

    public static final String CCPPWARNING_STATE =
	"org.w3c.jigsaw.ccpp.ccppwarning";

    protected String computeWarning(int warning, String reference) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(String.valueOf(warning)).append(" ");
	buffer.append(reference).append(" ");
	buffer.append("\"").append(CCPPRequest.getStandardWarning(warning));
	buffer.append("\"");
	return buffer.toString();
    }

    public void addWarning(int warning, String reference) {
	String token = computeWarning(warning, reference);
	warnings.addElement(token);
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0 ; i < warnings.size() ; i++) {
	    if (i != 0) {
		buffer.append(", ");
	    }
	    buffer.append(warnings.elementAt(i));
	}
	return buffer.toString();
    }

    public CCPPWarning() {
	warnings = new Vector();
    }

   
}


