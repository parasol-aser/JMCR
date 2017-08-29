// AuthRealm.java
// $Id: AuthRealm.java,v 1.1 2010/06/15 12:28:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.auth;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

public class AuthRealm extends ExternalContainer {

    /**
     * Load the user having this name.
     * @param name The user's name.
     * @return An instance of AuthUser or <strong>null</strong> if not found.
     */

    public synchronized ResourceReference loadUser(String name) {
	return lookup(name);
    }

    /**
     * register this new user in the realm.
     * @param user  The new user.
     */

    public synchronized void registerUser(AuthUser user) {
	addResource(user, null);
    }

    public void registerResource(String name,
				 Resource resource,
				 Hashtable defs) 
    {
	if( resource instanceof AuthUser) {
	    registerUser(AuthUser.makeUser(resource,
					   name, 
					   new ResourceContext(getContext())));
	}
    }

    /**
     * Unregister a user from the realm.
     * @param name The user's name.
     * @exception org.w3c.tools.resources.MultipleLockException if someone
     * else has locked this user.
     */

    public synchronized void unregisterUser(String name) 
	throws MultipleLockException
    {
	delete(name);
    }

    /**
     * Enumerate this realm user's name.
     */

    public synchronized Enumeration enumerateUserNames() {
	return enumerateResourceIdentifiers();
    }

    /**
     * create a new empty realm.
     * @param name The name of the realm.
     * @param repository The file to use to store the realm database.
     */
    public static AuthRealm makeRealm(ResourceContext context, String name) {
	Hashtable defs = new Hashtable(3) ;
	defs.put(id, name) ;
	defs.put(co, context);
	AuthRealm realm = new AuthRealm(name, context) ;
	realm.initialize(defs) ;
	return realm ;
    }

    /**
     * create a new empty realm.
     * @param name The name of the realm.
     * @param repository The file to use to store the realm database.
     */
    public static AuthRealm makeRealm(Resource res,
				      ResourceContext context, 
				      String name) {
	AuthRealm realm = (AuthRealm) res;
	Hashtable defs = new Hashtable(3) ;
	defs.put(id, name) ;
	defs.put(co, context);
	realm.initialize(defs) ;
	return realm ;
    }

    /**
     * Save our store.
     */

    public synchronized void save() {

    }

    public File getRepository(ResourceContext context) {
	return new File(context.getServer().getAuthDirectory(),
			getIdentifier()+".db");
    }

    public AuthRealm(String id, ResourceContext context)  {
	super(id, context, false);
    }

    public AuthRealm() {
	super() ;
    }

}
