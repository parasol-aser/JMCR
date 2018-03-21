// JigEditAclFilter.java
// $Id: JigEditAclFilter.java,v 1.2 2010/06/15 17:53:03 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters ;

import java.net.*;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.*;

import org.w3c.jigsaw.acl.*;
import org.w3c.jigsaw.auth.IPMatcher;
import org.w3c.jigsaw.auth.IPTemplatesAttribute;

import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.Client;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JigEditAclFilter extends AclFilter {
    
    /**
     * Attribute Index - Secure ip array
     */
    public static int ATTR_SECURE_IPS = -1;

    static {
	Attribute a = null ;
	Class     c = null ;
	try {
	    c = Class.forName("org.w3c.jigedit.filters.JigEditAclFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	a = new IPTemplatesAttribute("secure-ips"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_SECURE_IPS = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The IPMatcher to match secure IP 
     */
    protected IPMatcher ipmatcher = null ;

    protected IPMatcher getIPMatcher() {
	if (ipmatcher == null) {
	    ipmatcher = new IPMatcher();
	    short[][] ips = getSecureIPs();
	    if ( ips != null ) {
		for (int i = 0 ; i < ips.length ; i++) 
		    ipmatcher.add(ips[i], Boolean.TRUE) ;
	    }
	}
	return ipmatcher;
    }

    /**
     * Get the secure ip address
     */
    public short[][] getSecureIPs() {
	return (short[][]) getValue(ATTR_SECURE_IPS, null) ;
    }

    /**
     * Catch set value on the filter, to maintain cached values.
     */
     public void setValue(int idx, Object value) {
	 super.setValue(idx, value);
	if ( idx == ATTR_SECURE_IPS ) {
	    ipmatcher = null;
	}
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
	Client      client      = request.getClient();
	InetAddress inetaddress = client.getInetAddress();
	IPMatcher   ipmatcher   = getIPMatcher();
	System.out.println(">>> "+inetaddress);
	if (ipmatcher.lookup(inetaddress) != null) {
	    // secure ip, basic auth
	    setValue(ATTR_SECURITY_LEVEL, new Integer(0));
	} else {
	    // unsecure ip, digest auth
	    setValue(ATTR_SECURITY_LEVEL, new Integer(1));
	}
	super.authenticate(request, acls);
    }
    
}
