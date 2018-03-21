// HttpSetCookie.java
// $Id: HttpSetCookie.java,v 1.1 2010/06/15 12:19:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpSetCookie {
    /**
     * The cookie's name.
     */
    protected String name = null;
    /*
     * The cookie's value.
     */
    protected String value = null;
    /**
     * The cookie's comment.
     */
    protected String comment = null;
    /**
     * The cookie's domain name.
     */
    protected String domain = null;
    /**
     * The cookie's max age.
     */
    protected int maxage = -1;
    /**
     * The cookie's associated path.
     */
    protected String path = null;
    /**
     * Does this cookie requires special security care by client ?
     */
    protected boolean security = false;
    /**
     * This cookie's version.
     */
    protected int version = 1;

    /**
     * Get this SetCookie attached comment.
     * @return A String describing the reason for this SetCookie, or
     * <strong>null</strong>.
     */

    public String getComment() {
	return comment;
    }

    /**
     * Set this SetCookie attached comment.
     * @param comment A String giving the comments attached to that SetCookie,
     * or <strong>null</strong> to reset the value.
     */

    public void setComment(String comment) {
	this.comment = comment;
    }

    /**
     * Get the domain to which this cookie applies.
     * @return The string encoding of the domain to which this cookie applies
     * or <strong>null</strong> if undefined.
     */

    public String getDomain() {
	return domain;
    }

    /**
     * Define the domain for this SetCookie.
     * @param domain The String encoded domain name to which this cookie will
     * apply.
     */

    public void setDomain(String domain) {
	this.domain = domain;
    }

    /**
     * Get the max age value for this cookie.
     * @return A integer number of seconds giving the maximum age allowed for
     * this cookie, or <strong>-1</strong> if undefined.
     */

    public int getMaxAge() {
	return maxage;
    }

    /**
     * Set the max age value for this cookie.
     * @param maxage The max age value for this cookie, given as a number of
     * seconds, or <strong>-1</strong> to reset value.
     */

    public void setMaxAge(int maxage) {
	this.maxage = maxage;
    }

    /**
     * Get the path in which this cookie will apply.
     * @return The path to which this cookie applies, encoded as a String, or
     * <strong>null</strong> if undefined.
     */

    public String getPath() {
	return path;
    }

    /**
     * Set the path to which this SetCookie applies.
     * @param path The path, encoded as a String.
     */

    public void setPath(String path) {
	this.path = path;
    }

    /**
     * Get the security requirement atttached to that cookie.
     * @return A boolean, <strong>true</strong> if the cookie requires
     * special care from the client, <strong>false</strong> otherwise.
     */

    public boolean getSecurity() {
	return security;
    }

    /**
     * Mark/unmark this SetCookie as requiring special security when emited
     * back by the client.
     * @param onoff A boolean, <stronmg>true</strong> if the cookie that
     * results should be emited with special care, <strong>false</strong>
     * otherwise.
     */

    public void setSecurity(boolean onoff) {
	this.security = onoff;
    }

    /**
     * Get the SetCookie version number.
     * @return An integer giving the version number of this cookie.
     */

    public int getVersion() {
	return version;
    }

    /**
     * Set the SetCookie version number.
     * @param version The version number of this SetCookie.
     */

    public void setVersion(int version) {
	this.version = version;
    }

    /**
     * Set this SetCookie name.
     * @param name The name of the cookie, encoded as a String.
     */

    public void setName(String name) {
	this.name = name;
    }

    /**
     * Get this SetCookie cookie's name.
     * @return A String giving the cookie's name.
     */

    public String getName() {
	return name;
    }

    /**
     * Set this SetCookie cookie's value.
     * @param value The value, encoded as a printable String, or <strong>
     * null</strong> to reset the value.
     */

    public void setValue(String value) {
	this.value = value;
    }

    /**
     * Get the value associated with this SetCookie.
     * @return The value, encoded as a String, or <strong>null</strong> if
     * undefined.
     */

    public String getValue() {
	return value;
    }

    public String toString() {
	String cookie = name+"="+value;
	if (comment != null)
	    cookie = cookie+"; comment="+comment;
	if (maxage != -1)
	    cookie = cookie+"; maxage="+maxage;
	if (domain != null)
	    cookie = cookie+"; domain="+domain;
	if (path != null)
	    cookie = cookie+"; path="+path;
	if (security)
	    cookie = cookie+"; secure";
	return cookie;
    }

    public HttpSetCookie(boolean isValid, String name, String value) {
	this.name  = name;
	this.value = value;
    }

    public HttpSetCookie(String name, String value) {
	this.name  = name;
	this.value = value;
    }

    public HttpSetCookie() {
    }
}


