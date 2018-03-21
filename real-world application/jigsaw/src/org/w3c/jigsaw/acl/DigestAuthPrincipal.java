// DigestAuthPrincipal.java
// $Id: DigestAuthPrincipal.java,v 1.1 2010/06/15 12:22:07 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.w3c.jigsaw.http.Request;
import org.w3c.www.http.HttpCredential;
import org.w3c.util.StringUtils;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 * This algorithm used is based on RFC 2069
 */
public class DigestAuthPrincipal extends HTTPPrincipal  {

    protected
    String dac_user      = null;
    String dac_realm     = null;
    String dac_nonce     = null;
    String dac_uri       = null;
    String dac_response  = null;
    String dac_algorithm = null;
    String dac_method    = null;
    String nonce         = null;
    String old_nonce     = null;
    String algo          = null;
    boolean stale        = false;
    boolean no_user      = false;

    public boolean isStale() {
	return stale;
    }
    
    /**
     * Check that the challenge matches with the provided nonce
     * @return true if it matches
     */
    private boolean checkDigest2069(String username, String realm , 
				    String password, String nonce) {
	// check if the user knows the right passwd
	String a1, a2, ha1, ha2;
	StringBuffer sb = new StringBuffer(256);
	// a1 = unq(username-value) ":" unq(realm-value) ":" passwd
	sb.append(username).append(':').append(realm);
	sb.append(':').append(password);
	a1 = sb.toString();
	// A2       = Method ":" digest-uri-value
	sb = new StringBuffer(256);
	sb.append(dac_method).append(':').append(dac_uri);
	a2 = sb.toString();
	MessageDigest md = null;
	try {
	    md = MessageDigest.getInstance(this.algo);
	} catch (NoSuchAlgorithmException algex) {
	    // fatal error, can't authenticate
	    return false;
	}
	try {
	    md.update(a1.getBytes("ISO-8859-1"));
	    ha1 = StringUtils.toHexString(md.digest());
	    md.reset();
	    md.update(a2.getBytes("ISO-8859-1"));
	    ha2 = StringUtils.toHexString(md.digest());
	    md.reset();
	    String kd, hkd;
	    // KD( H(A1), unq(nonce-value) ":" H(A2)
	    sb = new StringBuffer(256);
	    sb.append(ha1).append(':').append(nonce).append(':').append(ha2);
	    kd = sb.toString();
	    md.update(kd.getBytes("ISO-8859-1"));
	    hkd = StringUtils.toHexString(md.digest());
	    return hkd.equals(dac_response);
	} catch (UnsupportedEncodingException ex) {
	    // in case iso-8859-1 is not known...
	}
	return false;
    }
    
    public boolean equals(Object another) {
	if (no_user)
	    return false;
	if (another instanceof AclPrincipal) {
	    AclPrincipal aclp = (AclPrincipal) another;
	    String username   = aclp.getName();
	    String realm      = aclp.getRealm();
	    String passwd     = aclp.getPassword();
	    
	    if (!dac_user.equals(username))
		return false;
	    if (!dac_realm.equals(realm))
		return false;
	    if (dac_algorithm != null && !dac_algorithm.equals(this.algo))
		return false;
	    // are we using the current nonce?
	    if (!dac_nonce.equals(this.nonce)) {
		// no, is it the old one?
		if (dac_nonce.equals(this.old_nonce)) {
		    // yes, does it matches?
		    if (checkDigest2069(username, realm, passwd, old_nonce)) {
			// it doesn't mean that we are validating an old nonce
			// but it is a trick, allowing two nonces at the same
			// time to populate "AuthenticationInfo" with the
			// next nonce.
			stale = true;
			return true;
		    }
		} else {
		    // reject but mark as atale if auth is ok with nonce.
		    if (checkDigest2069(username, realm, passwd, dac_nonce)) {
			stale = true;
		    }
		}
		return false;
	    }
	    return checkDigest2069(username, realm, passwd, nonce);
	} else if (another instanceof DigestAuthPrincipal) {
	    return false;
	}
	return false;
    }

    public String toString() {
	if (dac_user != null)
	    return dac_user;
	return "Digest";
    }

    public int hashCode() {
	if (dac_nonce != null)
	    return dac_nonce.hashCode();
	else return -1;
    }

    public String getName() {
	return dac_user;
    }

    public DigestAuthPrincipal(Request request, 
			       String nonce, 
			       String old_nonce,
			       String algo)
	throws InvalidAuthException
    {
	super(request);
	HttpCredential credential = (request.isProxy()
				     ? request.getProxyAuthorization()
				     : request.getAuthorization());
	if ((credential == null) ||
	    ( ! credential.getScheme().equalsIgnoreCase("Digest"))) {
	    no_user = true;
	} else {
	    no_user        = false;
	    dac_user       = credential.getAuthParameter("username");
	    dac_uri        = credential.getAuthParameter("uri");
	    dac_response   = credential.getAuthParameter("response");
	    dac_realm      = credential.getAuthParameter("realm");
	    dac_method     = request.getMethod();
	    dac_nonce      = credential.getAuthParameter("nonce");
	    this.nonce     = nonce;
	    this.old_nonce = old_nonce;
	    this.algo      = algo;
	    if (dac_user == null || dac_uri == null || dac_response == null ||
		dac_realm == null) {
		String msg = ("Invalid authentication header");
		throw new InvalidAuthException(msg);
	    }
	}
    }

    public DigestAuthPrincipal(Request request)
	throws InvalidAuthException
    {
	super(request);
	throw new InvalidAuthException("Bad call for authentification");
    }

    protected DigestAuthPrincipal(Request request, String internal)
	throws InvalidAuthException
    {
	super(request);
    }
}
