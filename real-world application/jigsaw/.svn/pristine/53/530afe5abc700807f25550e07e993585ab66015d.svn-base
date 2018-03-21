// SecurityLevel.java
// $Id: SecurityLevel.java,v 1.1 2010/06/15 12:22:07 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.w3c.jigsaw.auth.AuthFilter;
import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Reply;
import org.w3c.www.http.HttpChallenge;
import org.w3c.www.http.HttpFactory;
import org.w3c.tools.resources.FramedResource;
import org.w3c.util.StringUtils;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class SecurityLevel {

    private int           level     = -1;
    private long          prev_date = 0;
    private String        nonce     = null;
    private String        old_nonce = null;
    private AclFilter     filter    = null;
    private HttpChallenge challenge = null;
    private boolean       lenient   = false;

    /**
     * Get the principal relative to the request and the securityLevel.
     * @param request the incomming request.
     * @param securityLevel the Security Level.
     * @return a Principal instance.
     */
    public Principal getPrincipal(Request request, String algo) {
	Date d;
	switch (level) 
	    {
	    case 0:
		try {
		    return new BasicAuthPrincipal(request, lenient);
		} catch (InvalidAuthException ex) {
		    return new HTTPPrincipal(request, lenient);
		}
	    case 1:
		d = new Date();
		if ((d.getTime() - prev_date) / 1000 > filter.getNonceTTL()) {
			// synchronized to avoid bursts changes
		    synchronized (this) {
			if ( (d.getTime() - prev_date) / 1000 >
			     filter.getNonceTTL()) {	
			    prev_date = d.getTime();
			    updateNonce();
			}
		    }
		}
		try {
		    return new DigestAuthPrincipal(request, 
						   nonce, 
						   old_nonce,
						   algo);
		} catch (InvalidAuthException ex) {
		    return new HTTPPrincipal(request);
		}
	    case 2:
	    default:
		d = new Date();
		if ((d.getTime() - prev_date) / 1000 > filter.getNonceTTL()) {
		    // synchronized to avoid bursts changes
		    synchronized (this) {
			if ( (d.getTime() - prev_date) / 1000 >
			     filter.getNonceTTL()) {	
			    prev_date = d.getTime();
			    updateNonce();
			}
		    }
		}
		try {
		    return new DigestQopAuthPrincipal(request, 
						      nonce, 
						      old_nonce,
						      algo);
		} catch (InvalidAuthException ex) {
		    try {
			return new DigestAuthPrincipal(request, 
						       nonce, 
						       old_nonce,
						       algo);
		    } catch (InvalidAuthException iex) {
			return new HTTPPrincipal(request);
		    }
		}
	    }
    }

    /**
     * Get the HTTP challenge relative to the given security level.
     * @param name the challenge name
     * @param securityLevel the security level
     * @return a HttpChallenge instance.
     */
    public HttpChallenge getChallenge(String name, Principal principal) {
	switch (level) 
	    {
	    case 0:
		challenge.setAuthParameter("realm", name);
		return challenge;
	    case 1:
		if (principal instanceof DigestAuthPrincipal) {
		    DigestAuthPrincipal dap = (DigestAuthPrincipal)principal;
		    HttpChallenge new_c;
		    if (dap != null && dap.isStale()) {
			new_c = challenge.getClone();
			if (new_c != null)
			    new_c.setAuthParameter("stale","true", false);
			else
			    new_c = challenge;
		    } else {
			new_c = challenge;
		    }
		    new_c.setAuthParameter("realm", name);
		    return new_c;
		} else {
		    challenge.setAuthParameter("realm", name);
		    return challenge;
		}
	    case 2:
	    default:
		if (principal instanceof DigestQopAuthPrincipal) {
		    DigestQopAuthPrincipal dap;
		    dap = (DigestQopAuthPrincipal)principal;
		    HttpChallenge new_c;
		    if (dap != null && dap.isStale()) {
			new_c = challenge.getClone();
			if (new_c != null) {
			    new_c.setAuthParameter("stale","true", false);
			} else {
			    new_c = challenge;
			}
		    } else {
			new_c = challenge;
		    }
		    new_c.setAuthParameter("realm", name);
		    Request req = dap.getRequest();
		    String opaque = null;
		    try {
			MessageDigest md = 
			    MessageDigest.getInstance(filter.getAlgorithm());
			md.update(req.getMethod().getBytes());
			md.update(nonce.getBytes());
			byte b[] = md.digest();
			opaque = StringUtils.toHexString(b);
		    } catch (NoSuchAlgorithmException algex) {
			opaque="op"+nonce;
		    }
		    new_c.setAuthParameter("opaque", opaque);
		    return new_c;
		} else {
		    challenge.setAuthParameter("realm", name);
		    return challenge;
		}
	    }
    }

    public void updateRequestStates(Request request, Principal principal) {
	switch (level) 
	    {
	    case 0:
		request.setState(AuthFilter.STATE_AUTHUSER, 
				 principal.getName());
		request.setState(AuthFilter.STATE_AUTHTYPE, "Basic");
		break;
	    case 1:
	    case 2:
	    default:
		request.setState(AuthFilter.STATE_AUTHUSER, 
				 principal.getName());
		request.setState(AuthFilter.STATE_AUTHTYPE, "Digest");
		request.setState(AuthFilter.STATE_AUTHCONTEXT, principal);
	    }
    }
						       

    public void updateReply(Reply reply, Request request) {
	switch (level) 
	    {
	    case 0:
		break;
	    case 1:
	    case 2:
	    default:
		if (request.hasState(AuthFilter.STATE_AUTHCONTEXT)) {
		    DigestAuthPrincipal dap;
		    dap = (DigestAuthPrincipal)
			request.getState(AuthFilter.STATE_AUTHCONTEXT);
		    if (dap.isStale()) {
			reply.addAuthenticationInfo("nextnonce", nonce);
		    }
		}
	    }
    }

    private synchronized void updateNonce() {
	FramedResource fr = filter.getResource();
	if (fr instanceof HTTPFrame) {
	    HTTPFrame htf = (HTTPFrame) fr;
	    try {
		MessageDigest md = 
		    MessageDigest.getInstance(filter.getAlgorithm());
		md.update((new Date()).toString().getBytes());
		try {
		    md.update(htf.getETag().getTag().getBytes());
		} catch (Exception ex) {
		    // hum... try without it
		    md.update(htf.getURLPath().getBytes());
		}
		byte b[] = md.digest();
		if (nonce != null) 
		    old_nonce = nonce;
		nonce = StringUtils.toHexString(b);
		challenge.setAuthParameter("nonce", nonce);
	    } catch (NoSuchAlgorithmException algex) {
		// bad algorithm, prevent access by firing an error
	    }
	}
    }

    private SecurityLevel(AclFilter filter) {
	String algorithm = null;
	this.level   = filter.getSecurityLevel();
	this.filter  = filter;
	this.lenient = filter.isLenient();
	switch (level) 
	    {
	    case 0:
		challenge = HttpFactory.makeChallenge("Basic");
		challenge.setAuthParameter("realm", "");
		break;
	    case 1:
		challenge = HttpFactory.makeChallenge("Digest");
		//temporary hack for amaya
		challenge.setAuthParameter("realm", "");
		challenge.setAuthParameter("domain", 
					   filter.getResource().getURLPath());
		algorithm = filter.getAlgorithm();
		if (!algorithm.equalsIgnoreCase("md5")) {
		    challenge.setAuthParameter("algorithm", algorithm, false);
		}
		updateNonce();
		break;
	    case 2:
	    default:
		challenge = HttpFactory.makeChallenge("Digest");
		//temporary hack for amaya
		challenge.setAuthParameter("realm", "");
		challenge.setAuthParameter("domain", 
					   filter.getResource().getURLPath());
		algorithm = filter.getAlgorithm();
		challenge.setAuthParameter("algorithm", algorithm, false);
		challenge.setAuthParameter("qop", "auth");
		updateNonce();	    
	    }
	
    }

    static public SecurityLevel getSecurityLevel(AclFilter filter) {
	return new SecurityLevel(filter);
    }

}
