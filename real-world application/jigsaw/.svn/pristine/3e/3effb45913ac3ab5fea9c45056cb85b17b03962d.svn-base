// AuthUserPrincipal.java
// $Id: AuthUserPrincipal.java,v 1.1 2010/06/15 12:22:08 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.net.InetAddress;
import java.security.Principal;
import java.util.Hashtable;

import org.w3c.jigsaw.auth.AuthUser;
import org.w3c.jigsaw.auth.IPMatcher;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AuthUserPrincipal implements AclPrincipal {

    protected String    name      = null;
    protected String    password  = null;
    protected String    realm     = null;
    protected Hashtable values    = null;
    protected IPMatcher ipmatcher = null;

    public boolean equals(Object another) {
	if (another instanceof AuthUserPrincipal) {
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
	return (ipmatcher.lookup(adr) == Boolean.TRUE);
    }

    public AuthUserPrincipal(AuthUser user, String realm) {
	this.name      = user.getName();
	this.password  = user.getPassword();
	this.realm     = realm;
	this.ipmatcher = new IPMatcher();
	this.values    = new Hashtable();

	short ips[][]  = user.getIPTemplates();
	if ( ips != null ) {
	    for (int i = 0 ; i < ips.length ; i++) 
		ipmatcher.add(ips[i], Boolean.TRUE);
	}
    }

   
}


