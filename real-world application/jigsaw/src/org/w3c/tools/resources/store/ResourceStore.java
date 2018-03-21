// ResourceStore.java
// $Id: ResourceStore.java,v 1.1 2010/06/15 12:25:24 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store ;

import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;

import org.w3c.tools.resources.serialization.Serializer;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.File;

/**
 * A resource store implements persistency for a set of resources.
 * A resource store may implement a number of strategies along different
 * axis, for example: 
 * <ul>
 * <li>It may connect to some database to get the resource attributes. 
 * <li>It may restrict the classes of the resource it handles (for security
 *      reasons), by using a specific class loader.
 * <li>It may implement some caching scheme, to keep memory requirements low.
 * <li>It may be distributed (eg using a ResourceStoreStub in the client, but
 * providing access to a server-side resource store.).
 * </ul>
 */

public interface ResourceStore {

    /**
     * Get the version of that resource store.
     * Version numbers are used to distinguish between pickling format. 
     * A resource store implementator has the duty of bumping the returned
     * number whenever it's archiving format changes.
     * Resource stores that relies on some external archiving mechanisms
     * (such as a database), may return a constant.
     * @return An integer version number.
     */

    public int getVersion();

    /**
     * Get the identifier for that store.
     * @return A uniq store identifier, as a String.
     */

    public String getIdentifier();

     /**
     * Restore the resource whose name is given.
     * This method doesn't assume that the resource will actually be restored,
     * it can be kept in a cache by the ResourceStore object, and the cached 
     * version of the resource may be returned.
     * @param identifier The identifier of the resource to restore.
     * @param defs Default attribute values. If the resource needs to be
     *     restored from its pickled version, this Hashtable provides
     *     a set of default values for some of the attributes.
     * @return A Resource instance, or <strong>null</strong>.
     * @exception InvalidResourceException If the resource could not
     * be restored from the store.
     */

    public Resource loadResource(String identifier, Hashtable defs)
	throws InvalidResourceException;

    /**
     * Get this resource, but only if already loaded.
     * The resource store may (recommended) maintain a cache of the resource
     * it loads from its store. If the resource having this identifier 
     * has already been loaded, return it, otherwise, return
     * <strong>null</strong>.
     * @param identifier The resource identifier.
     * @return A Resource instance, or <strong>null</strong>.
     */

    public Resource lookupResource(String identifier) ;

    /**
     * Stabilize the given resource.
     * @param resource The resource to save.
     */

    public void saveResource(Resource resource);

    /**
     * Add this resource to this resource store.
     * @param resource The resource to be added.
     */

    public void addResource(Resource resource) ;

    /**
     * Remove this resource from the repository.
     * @param identifier The identifier of the resource to be removed.
     */

    public void removeResource(String identifier) ;

    /**
     * Rename a given resource.
     * @param oldid The olde resource identifier.
     * @param newid The new resource identifier.
     */

    public void renameResource(String oldid, String newid);

    /**
     * Mark this resource as modified.
     * @param resource The resource that has changed (and will have to be
     * pickled some time latter).
     */

    public void markModified(Resource resource);

    /**
     * Can this resource store be unloaded now ?
     * This method gets called by the ResourceStoreManager before calling
     * the <code>shutdown</code> method, when possible. An implementation
     * of that method is responsible for checking the <code>acceptUnload
     * </code> method of all its loaded resource before returning 
     * <strong>true</strong>, meaning that the resource store can be unloaded.
     * @return A boolean <strong>true</strong> if the resource store can be
     * unloaded.
     */

    public abstract boolean acceptUnload();

    /**
     * Shutdown this store.
     */

    public void shutdown() ;

    /**
     * Save this store.
     */

    public void save() ;

    /**
     * Enumerate all the resources saved in this store.
     * @return An enumeration of Strings, giving the identifier for all 
     *     the resources that this store knows about.
     */

    public Enumeration enumerateResourceIdentifiers() ;

    /**
     * Check for the existence of a resource in this store.
     * @param identifier The identifier of the resource to check.
     * @return A boolean <strong>true</strong> if the resource exists
     *    in this store, <strong>false</strong> otherwise.
     */

    public boolean hasResource(String identifier) ;

    /**
     * This resource store is being built, initialize it with the given arg.
     * @param manager The ResourceStoreManager instance that asks yourself
     * to initialize.
     * @param token The resource store manager key to that resource store, 
     * this token should be used when calling methods from the manager that
     * are to act on yourself.
     * @param repository A file, giving the location of the associated 
     *    repository.
     */

    public void initialize(ResourceStoreManager manager,
			   Object token,
			   File repository,
			   Serializer serializer);

}
