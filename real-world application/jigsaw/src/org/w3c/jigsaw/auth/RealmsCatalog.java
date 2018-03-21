// RealmsCatalog.java
// $Id: RealmsCatalog.java,v 1.1 2010/06/15 12:28:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.auth;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.File;
import java.io.PrintStream;

import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

public class RealmsCatalog extends ExternalContainer {

    protected String rep = null;

    /**
     * Load the given realm and return the AuthRealm instance.
     * @param name The realm identifier.
     */

    public synchronized ResourceReference loadRealm(String name) {
	return lookup(name);
    }

    /**
     * Enumerate the list of available realms.
     */

    public synchronized Enumeration enumerateRealmNames() {
	return enumerateResourceIdentifiers() ;
    }

    /**
     * register the given new realm.
     * @param realm The new realm to register.
     */

    public synchronized void registerRealm(AuthRealm realm) {
	System.out.println("register realm : "+realm.getIdentifier());
	addResource(realm, null) ;
    }

    public synchronized void registerRealm(String name) {
	registerRealm(AuthRealm.makeRealm(new ResourceContext(getContext()),
					  name ));
    }

    public void registerResource(String name,
				 Resource resource,
				 Hashtable defs) 
    {
	if ( resource instanceof AuthRealm) {
	    registerRealm(
			 AuthRealm.makeRealm(resource,
					     new ResourceContext(getContext()),
					     name ));
	}
    }

    /**
     * Unregister the given realm from the catalog.
     * @param name The name of the catalog.
     * @exception org.w3c.tools.resources.MultipleLockException if someone
     * else has locked this realm.
     */

    public synchronized void unregisterRealm(String name) 
	throws MultipleLockException
    {
	delete(name);
    }

    /**
     * Save the catalog back to disk.
     */

    public synchronized void save() {

    }

    public File getRepository(ResourceContext context) {
	return new File(context.getServer().getAuthDirectory(), rep);
    }

    public RealmsCatalog(ResourceContext context) {
	this(context, "realms.db");
    }

    public RealmsCatalog(ResourceContext context, String rep) {
	super();
	this.rep = (rep.endsWith(".db")) ? rep : rep + ".db";
	this.transientFlag = true;
	Hashtable h        = new Hashtable(3);
	h.put(id, "realms");
	h.put(co, context);
	initialize(h);
	context.setResourceReference( new DummyResourceReference(this));
    }
}
