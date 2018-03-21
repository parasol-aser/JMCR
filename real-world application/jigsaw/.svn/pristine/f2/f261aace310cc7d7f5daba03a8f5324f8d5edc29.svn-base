// HTTPPrincipal.java
// $Id: HTTPPrincipal.java,v 1.1 2010/06/15 12:22:06 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.net.InetAddress;
import java.security.Principal;

import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

/**
 * This class implements the most basic HTTP principal, allowing
 * you to check the IP of the request only
 */

public class HTTPPrincipal implements Principal {

    protected Request     request = null;
    protected boolean     lenient = false;

    protected Request getRequest() {
	return request;
    }

    protected InetAddress getInetAddress() {
	return request.getClient().getInetAddress();
    }

    public boolean equals(Object another) {
	return false;
    }

    public String getName() {
	return null;
    }

    public String getOriginalName() {
	return null;
    }

    public String getRealm() {
	return null;
    }

    public String toString() {
	return request.getClient().getInetAddress().toString();
    }

    public HTTPPrincipal(Request request) {
	this.request = request;
    }

    public HTTPPrincipal(Request request, boolean lenient) {
	this.lenient = lenient;
	this.request = request;
    }
}
