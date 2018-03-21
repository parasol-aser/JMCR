// AuthUser.java
// $Id: AuthUser.java,v 1.2 2010/06/15 17:53:03 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.auth ;

import java.util.Hashtable;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

/**
 * The basic description of a user.
 * A user is defined by the following set of attributes: its name, its email
 * adress, some comments. Than it can have either an IP adress, and/or 
 * a password.
 * <p>If an IP adress is provided, the user will be authentified by its
 * incoming connection IP address. Moreover, if a password is provided, 
 * before being authentified, the client will be challenged for it.
 * <p>Finally a user can be registered in any number of groups.
 */

public class AuthUser extends Resource {
    /**
     * Attribute index - The email adress of the user.
     */
    protected static int ATTR_EMAIL = -1 ;
    /**
     * Attribute index - The comments for this user.
     */
    protected static int ATTR_COMMENTS = -1 ;
    /**
     * Attribute index - The IP adress of the user.
     */
    protected static int ATTR_IPADDR = -1 ;
    /**
     * Attribute index - The optional password for the user.
     */
    protected static int ATTR_PASSWORD = -1 ;
    /**
     * Attribute index - The list of groups this user belongs to.
     */
    protected static int ATTR_GROUPS = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;

	try {
	    cls = Class.forName("org.w3c.jigsaw.auth.AuthUser") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The user email address
	a = new StringAttribute("email"
				, null
				, Attribute.EDITABLE) ;
	ATTR_EMAIL = AttributeRegistry.registerAttribute(cls, a) ;
	// The comments for the user
	a = new StringAttribute("comments"
				, null
				, Attribute.EDITABLE);
	ATTR_COMMENTS = AttributeRegistry.registerAttribute(cls, a) ;
	// The IP address of the user (optional)
	a = new IPTemplatesAttribute("ipaddress"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_IPADDR = AttributeRegistry.registerAttribute(cls, a) ;
	// The password for the user
	a = new PasswordAttribute("password"
				  , null
				  , Attribute.EDITABLE) ;
	ATTR_PASSWORD = AttributeRegistry.registerAttribute(cls, a) ;
	// The groups the user belong to.
	a = new StringArrayAttribute("groups"
				     , null
				     , Attribute.EDITABLE) ;
	ATTR_GROUPS = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get this user's name.
     * We use the resource identifier as the user name here.
     */

    public String getName() {
	return getIdentifier() ;
    }

    /**
     * Get the user email address.
     */

    public String getEmail() {
	return (String) getValue(ATTR_EMAIL, null) ;
    }

    /**
     * Get the user associated comments.
     */

    public String getComments() {
	return (String) getValue(ATTR_COMMENTS, null) ;
    }

    /**
     * Get the user IP templates.
     */

    public short[][] getIPTemplates() {
	return (short[][]) getValue(ATTR_IPADDR, null) ;
    }

    /**
     * Get the user password.
     */

    public String getPassword() {
	return (String) getValue(ATTR_PASSWORD, null) ;
    }

    /**
     * Set a new password for this user.
     * @param passwd The new user's password.
     */

    public void setPassword(String passwd) {
	setString(ATTR_PASSWORD, passwd);
    }

    /**
     * Get the user groups.
     */

    public String[] getGroups() {
	return (String[]) getValue(ATTR_GROUPS, null) ;
    }

    /**
     * Create a new user.
     * @param name The user's name.
     */

    public static AuthUser makeUser(String name, ResourceContext context) {
	Hashtable defs = new Hashtable(3) ;
	defs.put(id, name) ;
	defs.put(co, context);
	AuthUser user = new AuthUser() ;
	user.initialize(defs) ;
	return user ;
    }

    /**
     * Create a new user.
     * @param name The user's name.
     */
    public static AuthUser makeUser(Resource res,
				    String name, 
				    ResourceContext context) 
    {
	Hashtable defs = new Hashtable(3) ;
	defs.put(id, name) ;
	defs.put(co, context);
	AuthUser user = (AuthUser) res ;
	user.initialize(defs) ;
	return user ;
    }

    public AuthUser() {
    }

}


