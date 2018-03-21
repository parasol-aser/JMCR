// SimplePrincipal.java
// $Id: SimplePrincipal.java,v 1.1 2010/06/15 12:22:06 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.security.Principal;
import java.util.Hashtable;
import java.net.InetAddress;

/**
 * The most simple principal, it takes only the realm name, username 
 * and passwd as arguments
 */
public class SimplePrincipal implements AclPrincipal {
    protected String    name      = null;
    protected String    password  = null;
    protected String    realm     = null;
    protected Hashtable values    = null;

    public boolean equals(Object another) {
	if (another instanceof SimplePrincipal) {
	    return toString().equals(another.toString());
	} else {
	    return another.equals(this);
	}
    }

    public String toString() {
	if (password == null)
	    return name;
	else
	    return name+":"+password;
    }

    public int hashCode() {
	return toString().hashCode();
    }

    public String getName() {
	return name;
    }

    public String getRealm() {
	return realm;
    }

    public String getPassword() {
	return password;
    }

    public void setValue(String name, Object value) {
	values.put(name, value);
    }

    public Object getValue(String name) {
	return values.get(name);
    }

    public boolean matchIP(InetAddress adr) {
	return false;
    }

    public SimplePrincipal(String name, String password, String realm) {
	this.name      = name;
	this.password  = password;
	this.realm     = realm;
	this.values    = new Hashtable();
    }
}
