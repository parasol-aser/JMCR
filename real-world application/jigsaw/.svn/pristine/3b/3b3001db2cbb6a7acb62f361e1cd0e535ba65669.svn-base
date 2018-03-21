// BasicAuthprincipal.java
// $Id: BasicAuthPrincipal.java,v 1.1 2010/06/15 12:22:07 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import org.w3c.jigsaw.http.Request;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64FormatException;
import org.w3c.www.http.HttpCredential;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class BasicAuthPrincipal extends HTTPPrincipal {

    // original name is there to cope with a MS weirdness on MiniRedir
    protected String      origname = null;
    protected String      name     = null;
    protected String      password = null;
    protected String      cookie   = null;

    protected String getCookie() {
	return cookie;
    }

    public boolean equals(Object another) {
	if (another instanceof AclPrincipal) {
	    AclPrincipal aclp = (AclPrincipal) another;
	    if (aclp.matchIP(getInetAddress())) {
		if (aclp.getPassword() != null) {
		    return ((name != null) &&
			    (password != null) &&
			    name.equals(aclp.getName()) &&
			    password.equals(aclp.getPassword()));
		} else {
		    return true;
		}
	    } else {
		return ((name != null) &&
			(password != null) &&
			name.equals(aclp.getName()) &&
			password.equals(aclp.getPassword()));
	    }
	} else {
	    return toString().equals(another.toString());
	}
    }

    public String toString() {
	if (name == null)
	    return super.toString();
	return name+":"+password;
    }

    public int hashCode() {
	return toString().hashCode();
    }

    public String getName() {
	return name;
    }

    public String getOriginalName() {
	return (origname == null) ? name : origname;
    }

    public BasicAuthPrincipal(Request request) 
	throws InvalidAuthException
    {
	this(request, false);
    }

    public BasicAuthPrincipal(Request request, boolean lenient) 
	throws InvalidAuthException
    {
	super(request, lenient);
	HttpCredential credential = null;
	credential = (request.isProxy()
		      ? request.getProxyAuthorization()
		      : request.getAuthorization());
	if (credential == null) {
	    this.name     = null;
	    this.password = null;
	} else if ( ! credential.getScheme().equalsIgnoreCase("Basic") ) {
	    String msg = ("Invalid authentication scheme \""
			  + credential.getScheme()
			  + " expecting \"Basic\"");
	    throw new InvalidAuthException (msg) ;
	} else {
	    // Decode the credentials:
	    String decoded = null ;
	    this.cookie    = credential.getAuthParameter("cookie");
	    try {
		Base64Decoder b  = new Base64Decoder (cookie) ;
		decoded          = b.processString() ;
	    } catch (Base64FormatException e) {
		String msg = "Invalid BASE64 encoding of credentials." ;
		throw new InvalidAuthException (msg) ;
	    }
	    // Get user and password:
	    origname = null;
	    int icolon = decoded.indexOf (':') ;
	    if ( (icolon > 0) && (icolon+1 < decoded.length()) ) {
		// ok, parse was find, check user:
		if (lenient) {
		    String _name = decoded.substring (0, icolon) ;
		    int _slashIdx = _name.lastIndexOf('\\');
		    if ( _slashIdx != -1) {
			this.origname = _name;
			this.name = _name.substring(_slashIdx+1);
		    } else {
			this.name = _name;
		    }
		} else {
		    this.name     = decoded.substring (0, icolon) ;
		}
		this.password = decoded.substring (icolon+1) ;
	    } else {
		String msg = "Invalid credentials syntax in " + decoded ;
		throw new InvalidAuthException (msg) ;
	    }
	}
    }
}
