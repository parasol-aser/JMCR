// ResourceContext.java
// $Id: ResourceContext.java,v 1.1 2010/06/15 12:20:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

import java.util.Hashtable;

/**
 * The resource context.
 */
public class ResourceContext {

    /**
     * debug flag
     */
    public static boolean debug = false;

    /**
     * Our parent context, if any.
     */
    protected ResourceContext parent = null;
    /**
     * The set of registered modules.
     */
    public Hashtable modules = null;

    /**
     * Our Resource Space.
     */
    protected ResourceSpace space = null;

    /**
     * The server of that resource context.
     */
    protected ServerInterface server = null;

    /**
     * The ResourceReference of our resource.
     */
    protected ResourceReference reference = null;

    /**
     * The ResourceReference of the resource container.
     */
    protected ResourceReference container = null;

    /**
     * Get the container of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference getContainer() {
	return container;
    }

    /**
     * Get the ResourceReference of the resource.
     * @return a ResourceReference instance.
     */
    public ResourceReference getResourceReference() {
	return reference;
    }

    /**
     * Set the ResourceReference of the resource.
     * @param reference The ResourceReference to set.
     */
    public void setResourceReference(ResourceReference reference) {
	this.reference = reference;
	reference.updateContext(this);
    }

    /**
     * Get our Resource Space.
     * @return A ResourceSpace instance.
     */
    public ResourceSpace getSpace() {
	return space;
    }

    /**
     * Set the Resource Space.
     * @param space Our Resource Space.
     */
    public void setSpace(ResourceSpace space) {
	this.space = space;
    }

    /**
     * Get the server this context is attached to.
     * @return An ServerInterface instance 
     * (guaranteed not to be <strong>null</strong>.)
     */

    public ServerInterface getServer() {
	return server;
    }

    /**
     * Get that context's ancestor.
     * @return A ResourceContext instance, or <strong>null</strong>.
     */

    public ResourceContext getParent() {
	return parent;
    }

    /**
     * Register a module within that resource context.
     * @param name The module's name.
     * @param impl The module's implementation.
     */

    public synchronized void registerModule(String name, Object impl) {
	if ( modules == null )
	    modules = new Hashtable(7);
	modules.put(name, impl);
    }

    /**
     * Lookup a module within that resource context.
     * @param name Name of the module to look for.
     * @param inherited Also look within the contexts hierarchy for an 
     * inherited module having that name.
     */

    public Object getModule(String name, boolean inherited) {
	Object impl = ((modules == null) ? null : modules.get(name));
	if (inherited && (parent != null) && (impl == null))
	    impl = parent.getModule(name, true);
	return impl;
    }

    /**
     * Lookup a module within that context, and up the hierarchy of contexts.
     * @param name The module's name.
     * @return An object <em>implementing</em> that module.
     */

    public Object getModule(String name) {
	return getModule(name, true);
    }

    public String toString() {
	String tostring = "\nResourceContext : ";
	if (parent == null)
	    tostring += "\n\tparent    : null";
	tostring += "\n\tcontainer : "+container;
	tostring += "\n\tspace     : "+space;
	tostring += "\n\tserver    : "+server;
	return tostring;
    }

    /**
     * Create a ResourceContext from a container.
     * @param container The resource container.
     */
    public ResourceContext(ContainerResource container)
    {
	this.parent    = container.getContext();
	this.container = container.getResourceReference();
	this.space     = (parent != null) ? parent.space : null;
	this.server    = (parent != null) ? parent.server : null;
	if ((this.container == null) && debug) {
	    System.out.println("[1] container has no Reference");
	    org.w3c.util.Trace.showTrace();
	}
    }

    /**
     * Create a ResourceContext from a container ResourceReference.
     * @param container The resource reference of the container. 
     * Must be an instance of ContainerResource.
     */
    public ResourceContext(ResourceReference rr_container) {
	try {
	    Resource res = rr_container.lock();
	    this.parent    = res.getContext();
	    this.container = rr_container;
	    this.space     = (parent != null) ? parent.space : null;
	    this.server    = (parent != null) ? parent.server : null;
	} catch (InvalidResourceException ex) {
	    //should be valid
	    ex.printStackTrace();
	} finally {
	    rr_container.unlock();
	}
    }

    /**
     * Create a ResourceContext from the parent context.
     * @param parent The parent resource context.
     */
    public ResourceContext(ResourceContext parent) 
    {
	this.parent    = parent;
	this.container = parent.getResourceReference();
	this.space     = parent.space;
	this.server    = parent.server;
	if ((this.container == null) && debug) {
	    System.out.println("[2] parent context has no reference");
	    org.w3c.util.Trace.showTrace();
	}
    }

    /**
     * Create a ResourceContext.
     * @param space The ResourceSpace.
     * @param server The server.
     */
    public ResourceContext(ResourceSpace space, ServerInterface server)
    {
	this.server    = server;
	this.space     = space;
	this.container = null;
	this.parent    = null;
    }

    /**
     * Create a ResourceContext.
     * @param server The server.
     */
    public ResourceContext(ServerInterface server)
    {
	this.server    = server;
	this.space     = null;
	this.container = null;
	this.parent    = null;
    }

}
