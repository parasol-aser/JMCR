// DAVStateToken.java
// $Id: DAVStateToken.java,v 1.1 2010/06/15 12:27:43 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVStateToken {

    boolean isnot = false;
    String  token = null;

    public boolean isNot() {
	return isnot;
    }

    public String getStateToken() {
	return token;
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	if (isnot) {
	    buffer.append("Not ");
	}
	buffer.append("<").append(token).append(">");
	return buffer.toString();
    }

    public DAVStateToken(String token, boolean isnot) {
	this.token = token;
	this.isnot = isnot;
    }
    
    
}
