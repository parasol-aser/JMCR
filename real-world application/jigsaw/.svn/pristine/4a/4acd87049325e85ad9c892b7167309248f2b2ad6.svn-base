// HTTPPermission.java
// $Id: HTTPPermission.java,v 1.1 2010/06/15 12:22:07 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.security.acl.Permission;

import org.w3c.jigsaw.http.Request;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class HTTPPermission implements Permission {

    protected String method = null;

    protected String getMethod() {
	return method;
    }

    public HTTPPermission(Request request) {
	this.method = request.getMethod();
    }

    public boolean equals(Object another) {
	if (another instanceof HTTPPermission) {
	    return method.equals(((HTTPPermission)another).getMethod());
	} else {
	    return method.equals(another.toString());
	}
    }

    public String toString() {
	return method+" permission";
    }

}
