// HttpCookie.java
// $Id: HttpCookie.java,v 1.1 2010/06/15 12:19:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpCookie {
    /**
     * The path in which this cookie applies.
     */
    protected String path = null;
    /**
     * The domain in which this cookie applies.
     */
    protected String domain = null;
    /**
     * This cookie's value.
     */
    protected String value = null;
    /**
     * This cookie's name.
     */
    protected String name = null;
    /**
     * Set this cookie's version.
     */
    protected int version = 1;
    /**
     * Set the security flag
     */
    protected boolean secure = false;

    /**
     * Get the security flag
     * @return true if it's a secured cookie
     */
    public boolean getSecurity() {
	return secure;
    }

    public void setSecurity(boolean secure) {
	this.secure = secure;
    }

    /**
     * Get the path to which this cookie applies.
     * @return The path encoded as a String, or <strong>null</strong> if not
     * defined.
     */

    public String getPath() {
	return path;
    }

    /**
     * Set the path in which this cookie applies.
     * @param path The path to which this cookie applies, or <strong>null
     * </strong> to reset it.
     */

    public void setPath(String path) {
	this.path = path;
    }

    /**
     * Get the domain in which this cookie applies.
     * @return The domain in which this cookie applies, encoded as a String,
     * or <strong>null</strong> if undefined.
     */

    public String getDomain() {
	return domain;
    }

    /**
     * Set the domain in which this cookie applies.
     * @param domain The domain in which the cookie applies, or <strong>
     * null</strong> to reset it.
     */

    public void setDomain(String domain) {
	this.domain = domain;
    }

    /**
     * Get this cookie's version.
     * @return An integer, giving the cookie's version.
     */

    public int getVersion() {
	return version;
    }

    /**
     * Set this cookie's version.
     * @param version An integer indicating the version of this cookie.
     */

    public void setVersion(int version) {
	this.version = version;
    }

    /**
     * Get this cookie's name.
     * @return The name of the cookie, or <strong>null</strong> if undefined.
     */

    public String getName() {
	return name;
    }

    /**
     * Set this cookie's name.
     * @param name The cookie's name, or <strong>null</strong> to reset the
     * value.
     */

    public void setName(String name) {
	this.name = name;
    }

    /**
     * Get this cookie's value.
     * @return The value, encoded as a String, or <strong>nullM</strong> if
     * no value defined.
     */

    public String getValue() {
	return value;
    }

    /**
     * Set this cookie's value.
     * @param value The String encoded value, or <strong>null</strong> to
     * reset the value.
     */

    public void setValue(String value) {
	this.value = value;
    }

    public String toString() {
	return name+"="+value;
    }

    HttpCookie(boolean isValid, String name, String value) {
	this.name  = name;
	this.value = value;
    }

    public HttpCookie() {
    }

}


