// AclFilter.java
// $Id: AclFilter.java,v 1.2 2010/06/15 17:53:02 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.acl;

import java.security.Principal;
import java.security.acl.Permission;
import java.security.acl.Acl;

import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpChallenge;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AclFilter extends ResourceFilter {

    /**
     * Attribute index - Security level 0=Basic 1=Digest...
     */
    protected static int ATTR_SECURITY_LEVEL = -1;
    /**
     * Attribute index - The algorithm used
     */
    protected static int ATTR_ALGORITHM = -1 ;
    /**
     * Attribute index - The nonce time to live (in seconds)
     */
    protected static int ATTR_NONCE_TTL = -1 ;
    /**
     * Attribute index - And or Or for multiple Acls
     */
    protected static int ATTR_STRICT_ACL_MERGE_POLICY = -1;
    /**
     * Attribute index - Is caching allowed by a shared cache ?
     */
    protected static int ATTR_SHARED_CACHABILITY = -1;
    /**
     * Attribute index - Is caching allowed in private cache ?
     */
    protected static int ATTR_PRIVATE_CACHABILITY = -1;
    /**
     * Attribute index - Is public caching of protected documents allowed ?
     */
    protected static int ATTR_PUBLIC_CACHABILITY = -1;
    /**
     * Attribute index - Do we enable workarounds ?
     */
    protected static int ATTR_LENIENT = -1;

    /**
     * The JAcl class.
     */
    protected static Class JAcl_class = null;

    protected SecurityLevel security = null;

    static {
	Attribute a   = null ;
	Class     c = null ;
	try {
	    c          = Class.forName("org.w3c.jigsaw.acl.AclFilter");
	    JAcl_class = Class.forName("org.w3c.jigsaw.acl.JAcl");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// security level
	a = new IntegerAttribute("security-level",
				 new Integer(0),
				 Attribute.EDITABLE);
	ATTR_SECURITY_LEVEL = AttributeRegistry.registerAttribute(c, a);
	// do we use a strict acl merge policy
	a = new BooleanAttribute("strict-acl-merge-policy"
				 , Boolean.TRUE
				 , Attribute.EDITABLE);
	ATTR_STRICT_ACL_MERGE_POLICY = 
	    AttributeRegistry.registerAttribute(c, a);
	// The algorithm used for digest and checksum
	a = new StringAttribute("algorithm"
				, null
				, Attribute.EDITABLE);
	ATTR_ALGORITHM = AttributeRegistry.registerAttribute(c, a) ;
	a = new IntegerAttribute("nonce_ttl"
				 , new Integer(300)
				 , Attribute.EDITABLE);
	ATTR_NONCE_TTL = AttributeRegistry.registerAttribute(c, a) ;
	// Can protected documents be saved in shared cache ?
	a = new BooleanAttribute("shared-cachability"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_SHARED_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
	// Can protected documents be shared in private cache ?
	a = new BooleanAttribute("private-cachability"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_PRIVATE_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
	// Can protected documents be publicly cached ?
	a = new BooleanAttribute("public-cachability"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_PUBLIC_CACHABILITY = AttributeRegistry.registerAttribute(c, a);
	// Are we lenient in the way we check things ?
	// related to enabling/disabling workarounds because of some
	// implementations
	a = new BooleanAttribute("lenient"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_LENIENT = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get the security level.
     * @return an integer;
     */ 
    public int getSecurityLevel() {
	return getInt(ATTR_SECURITY_LEVEL, 0);
    }

    /**
     * Get the algorithm used
     */
    public String getAlgorithm() {
	return (String) getValue(ATTR_ALGORITHM, "MD5") ;
    }

    public int getNonceTTL() {
	return getInt(ATTR_NONCE_TTL, 300);
    }

    /**
     * Returns true if we have a strict acl merge policy.
     * @return a boolean.
     */
    public boolean isStrictAclMergePolicy() {
	return getBoolean(ATTR_STRICT_ACL_MERGE_POLICY, true);
    }

    /**
     * Is this document publicly cachable ?
     * @return A boolean.
     */
    public boolean getPublicCachability() {
	return getBoolean(ATTR_PUBLIC_CACHABILITY, false);
    }

    /**
     * Is this document cachable in private caches ?
     * @return A boolean.
     */
    public boolean getPrivateCachability() {
	return getBoolean(ATTR_PRIVATE_CACHABILITY, false);
    }

    /**
     * Is this document cachable in shared caches ?
     * @return A boolean.
     */
    public boolean getSharedCachability() {
	return getBoolean(ATTR_SHARED_CACHABILITY, false);
    }

    /**
     * Are we lenient in the way we check things? 
     * can be read as "Do we enable workarounds for broken implementations?"
     * @return A boolean.
     */
    public boolean isLenient() {
	return getBoolean(ATTR_LENIENT, false);
    }

    protected JAcl[] getAcls() {
	ResourceFrame frames[] = collectFrames(JAcl_class);
	JAcl acls[] = new  JAcl[frames.length];
	for (int i = 0 ; i < frames.length ; i++)
	    acls[i] = (JAcl) frames[i];
	return acls;
    }

    /**
     * Authenticate the given request for the given client. 
     * This method is invoked prior to any request handling on its target
     * entity. If the used authentication method allows so, AuthFilters 
     * should set the <strong>authuser</strong> attribute of the request.
     * @param request The request.
     * @exception ProtocolException If authentication failed.
     */

    public boolean lookup(LookupState ls, LookupResult lr)
        throws ProtocolException
    {
	JAcl acls[] = getAcls();
	if ((acls == null) || (ls.getRequest() == null)) {
	    return false;
	}
	authenticate((Request)ls.getRequest(),  acls);
	return false;
    }

    /**
     * Authenticate the given request.
     * @param request The request to be authentified.
     * @param acls The Access Control List array.
     * @exception org.w3c.tools.resources.ProtocolException if authentication
     * failed
     */
    protected void authenticate(Request request, JAcl acls[]) 
    	throws ProtocolException 
    {
	Permission perm       = new HTTPPermission(request);
	Principal  princ      = security.getPrincipal(request, getAlgorithm());
	boolean    strict     = isStrictAclMergePolicy();
	boolean    authorized = false;
	Acl        acl        = acls[0];

	if (princ != null) {
	    if (strict) {
		authorized = true;
		for (int i = 0 ; i < acls.length ; i++) {
		    boolean check = acls[i].checkPermission(princ, perm);
		    if (!check) {
			authorized = false;
			acl        = acls[i];
			break;
		    } 
		}
	    } else {
		for (int i = 0 ; i < acls.length ; i++) {
		    boolean check = acls[i].checkPermission(princ, perm);
		    if (check) {
			authorized = true;
			break;
		    }
		}
	    }
	}
	if (!authorized) {
	    HttpChallenge challenge = 
		security.getChallenge(acl.getName(), princ);
	    Reply e = null;
	    if ( request.isProxy() ) {
		e = request.makeReply(HTTP.PROXY_AUTH_REQUIRED);
		e.setProxyAuthenticate(challenge);
	    } else {
		e = request.makeReply(HTTP.UNAUTHORIZED);
		e.setWWWAuthenticate(challenge);
	    }
	    HtmlGenerator g = new HtmlGenerator("Unauthorized");
	    g.append ("<h1>Unauthorized access</h1>"+
		      "<p>You are denied access to this resource.");
	    e.setStream(g);
	    request.skipBody();
	    throw new HTTPException (e);
	} else {
	    security.updateRequestStates(request, princ);
	}
    }

    /**
     * Add the appropriate cache control directives on the way back.
     * @param request The request that has been processed.
     * @param reply The original reply.
     * @return Always <strong>null</strong>.
     */
    public ReplyInterface outgoingFilter(RequestInterface request,
					 ReplyInterface reply) 
    {
      Reply rep = (Reply) reply;
	if ( getPrivateCachability() ) {
	    rep.setMustRevalidate(true);
	} else if ( getSharedCachability() ) {
	    rep.setProxyRevalidate(true);
	} else if ( getPublicCachability() ) {
	    rep.setPublic(true);
	}
	security.updateReply(rep, (Request) request);
	return null;
    }

    /**
     * Catch set value to maintain cached values.
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ((idx == ATTR_SECURITY_LEVEL) || (idx == ATTR_ALGORITHM )
	    || (idx == ATTR_LENIENT )) {
	    security = SecurityLevel.getSecurityLevel(this);
	}
    }

    /**
     * Initialize the filter.
     */
    public void initialize(Object values[]) {
	super.initialize(values) ;
	security = SecurityLevel.getSecurityLevel(this);
    }
}
