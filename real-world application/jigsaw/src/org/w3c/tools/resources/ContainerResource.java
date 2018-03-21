// ContainerResource.java
// $Id: ContainerResource.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.tools.resources.event.Events;
import org.w3c.tools.resources.event.StructureChangedEvent;
import org.w3c.tools.resources.store.ResourceStoreManager;

/**
 * This resource manage children resources.
 */
public class ContainerResource extends AbstractContainer {

    public static boolean debug = false;

    /**
     * Attribute index - The index of the resource key.
     */
    protected static int ATTR_KEY = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our own class:
	try {
	    cls  = Class.forName("org.w3c.tools.resources.ContainerResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The identifier attribute:
	a = new IntegerAttribute("key",
				 null,
				 Attribute.COMPUTED);
	ATTR_KEY = AttributeRegistry.registerAttribute(cls, a);
    }

    public Object getClone(Object values[]) {
	values[ATTR_KEY] = null;
	return super.getClone(values);
    }

    /**
     * Get the container Key. This key must be unique and unchanged
     * during the container life.
     * @return a String instance.
     */
    public Integer getKey() {
	Integer key = (Integer) unsafeGetValue(ATTR_KEY, null);
	if (key == null) {
	    key = new Integer(getIdentifier().hashCode() ^ 
			      (new Date().hashCode()));
	    if (debug) {
		System.out.println("*** new key is: " + key);
	    }
	    ResourceStoreManager rsm = getServer().getResourceStoreManager();
	    while (!rsm.checkKey(key)) {
//		key = new Integer (key.intValue() ^ (int) Math.random());
		key = new Integer ((int)((1.9 *(Math.random()-0.5)) * 
					 Integer.MAX_VALUE));

		if (debug) {
		    System.out.println("*** updated key is: " + key);
		}
	    }
	    setValue(ATTR_KEY, key);
	}
	return key;
    }

    protected SpaceEntry getSpaceEntry() {
	ResourceReference rr = getParent();
	if (rr == null) //I'm root or external!!
	    return new SpaceEntryImpl(this);
	try {
	    //FIXME sure that is a containerResource?
	    ContainerResource cont = (ContainerResource) rr.lock();
	    return new SpaceEntryImpl(cont);
	} catch (InvalidResourceException ex) {
	    System.out.println(ex.getMessage());
	    ex.printStackTrace();
	    return null;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Get the SpaceEntry of our children resources.
     * @return A SpaceEntry instance.
     */
    protected SpaceEntry getChildrenSpaceEntry() {
	return new SpaceEntryImpl( this );
    }

    /**
     * This handles the <code>RESOURCE_MODIFIED</code> kind of events.
     * @param evt The StructureChangeEvent.
     */
    public void resourceModified(StructureChangedEvent evt) {
	if (! isUnloaded())
	    super.resourceModified(evt);
    }

    /**
     * A new resource has been created in some space.
     * This handles the <code>RESOURCE_CREATED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceCreated(StructureChangedEvent evt) {
	if (! isUnloaded())
	    super.resourceCreated(evt);
    }

    /**
     * A resource is about to be removed
     * This handles the <code>RESOURCE_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */
    public void resourceRemoved(StructureChangedEvent evt) {
  	if (! isUnloaded())
	    super.resourceRemoved(evt);
    }

    /**
     * Update default child attributes.
     * A parent can often pass default attribute values to its children,
     * such as a pointer to itself (the <em>parent</em> attribute).
     * <p>This is the method to overide when you want your container
     * to provide these kinds of attributes. By default this method will set
     * the following attributes:
     * <dl><dt>name<dd>The name of the child (it's identifier) - 
     * String instance.
     * <dt>parent<dd>The parent of the child (ie ourself here) - 
     * a ContainerResource instance.
     * <dt>url<dd>If a <em>identifier</em> attribute is defined, that
     * attribute is set to the full URL path of the children.
     * </dl>
     */

    protected ResourceContext updateDefaultChildAttributes(Hashtable attrs) {
	ResourceContext context = super.updateDefaultChildAttributes(attrs);
	if (context == null) {
	    context = new ResourceContext(getContext());
	    attrs.put(co, context) ;
	}
	String name = (String) attrs.get(id);
	if ( name != null ) {
	    StringBuffer sb = new StringBuffer(128);
	    sb.append(getURLPath());
	    sb.append(java.net.URLEncoder.encode(name));
	    attrs.put(ur, sb.toString());
	}
	return context;
    }

    /**
     * Enumerate children resource identifiers.
     * @param all Should all resources be enumerated ? Resources are often
     * created on demand only, this flag allows the caller to tell the 
     * container about wether it is interested only in already created
     * resources, or in all resources (even the one that have not yet been
     * created).
     * @return An String enumeration, one element per child.
     */

    public synchronized Enumeration enumerateResourceIdentifiers(boolean all)
    {
	ResourceSpace space = getSpace();
	acquireChildren();
	return space.enumerateResourceIdentifiers( getChildrenSpaceEntry() );
    }

    /**
     * Create a default child resource in that container.
     * This method is called by the editor to add a default resource
     * in the container under the given name. The meaning of <em>default</em>
     * is left up to the container here.
     * @param name The identifier for the new resource.
     */
    public  ResourceReference createDefaultResource(String name) {
	return null;
    }

    /**
     * Get the number of matching character (case sensitive).
     * ex getMatchingCharsCount("index.html", "Index.html") = 10.
     * @param s1 the first string.
     * @param s2 the second string.
     * @return -1 if s1 and s2 are not equals (ignoring case), 
     * otherwise the number of matching character (case sensitive).
     */
    protected int getMatchingCharsCount(String s1, String s2) {
	int len      = -1;
	int matching = 0;
	if (s1 == null || s2 == null || ((len = s1.length()) != s2.length()))
	    return -1;

	for (int i=0; i<len; i++) {
	    char c1 = s1.charAt(i);
	    char c2 = s2.charAt(i);
	    if (c1 == c2) {
		matching++;
		continue;
	    }
	    c1 = Character.toUpperCase(c1);
	    c2 = Character.toUpperCase(c2);
	    if (c1 != c2)
		return -1;
	}
	return matching;
    }

    /**
     * Lookup a children in the container.
     * @param name The name of the children to lookup.
     */
    public ResourceReference lookup(String name) {
	acquireChildren();
	SpaceEntry sp = getChildrenSpaceEntry();
	ResourceSpace space = getSpace();
	ResourceReference rr = internalLookup(name, sp, space);
	
	if ((rr == null) && (! getServer().checkFileSystemSensitivity())) {
	    Enumeration children = space.enumerateResourceIdentifiers(sp);
	    //look for possible matching identifier
	    int    max      = -1;
	    String realname = null;
	    while (children.hasMoreElements()) {
		String child = (String)children.nextElement();
		int matching = getMatchingCharsCount(name, child);
		if (matching > max) {
		    max      = matching;
		    realname = child;
		}
	    }
	    if (realname != null)
		rr = internalLookup(realname, sp, space);
	}
	return rr;
    }

    protected ResourceReference internalLookup(String name,
					       SpaceEntry sp,
					       ResourceSpace space) 
    {
	ResourceReference rr = space.lookupResource(sp, name);
	if (rr != null) 
	    return rr;
	synchronized (this) {
	    rr = space.lookupResource(sp, name);
	    if (rr != null) 
		return rr;
	    Hashtable defs = new Hashtable(5) ;
	    defs.put(id, name);
	    ResourceContext context = updateDefaultChildAttributes(defs);
	    rr = space.loadResource(sp, name, defs);
	    if (rr != null) {
		context.setResourceReference(rr);
		try {
		    Resource resource = rr.lock();
		    if (resource instanceof FramedResource) {
			FramedResource fres = (FramedResource) resource;
			fres.addStructureChangedListener(this);
			// send event
		    }
		} catch (InvalidResourceException ex) {
		    // nothing here
		} finally {
		    rr.unlock();
		}
	    }
	}
	return rr;
    }

    /**
     * Lookup the next component of this lookup state in here.
     * @param ls The current lookup state.
     * @param lr The lookup result under construction.
     * @exception ProtocolException If an error occurs.
     * @return A boolean, <strong>true</strong> if lookup has completed, 
     * <strong>false</strong> if it should be continued by the caller.
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	// Give a chance to our super-class to run its own lookup scheme:
	if ( super.lookup(ls, lr) )
	    return true;
	// Perform our own lookup on the next component:
	String name = ls.getNextComponent() ;
	ResourceReference rr = null;
	rr = lookup(name);
	if (rr == null) {
	    lr.setTarget(null);
	    return false;
	}
	try {
	    lr.setTarget(rr);
	    FramedResource resource = (FramedResource) rr.lock();
	    return (resource != null ) ? resource.lookup(ls, lr) : false;
	} catch (InvalidResourceException ex) {
	    return false;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Remove a child resource from that container.
     * @param name The name of the child to remove.
     * @exception MultipleLockException If somone else has locked the 
     * resource.
     */

    public void delete(String name) 
	throws MultipleLockException
    {
	ResourceReference rr = null;
	rr = lookup(name);

	if (rr != null) {
	    try {
		synchronized (rr) {
		    Resource resource = rr.lock();
		    if (resource instanceof FramedResource)
			((FramedResource)resource).
			    removeStructureChangedListener(this);
		    resource.delete();
		}
	    } catch (InvalidResourceException ex) {
		// FIXME ??
	    } finally {
		rr.unlock();
	    }
	}
    }

    /**
     * Delete that container and its children if children is true
     * @exception MultipleLockException If somone else has locked one 
     * of the resource child.
     */
    public synchronized void replace(DirectoryResource newdir) 
	throws MultipleLockException
    {
	Enumeration       e        = enumerateResourceIdentifiers();
	ResourceReference rr       = null;
	Resource          resource = null;
	while (e.hasMoreElements()) {
	    rr = lookup((String) e.nextElement());
	    if (rr != null) {
		try {
		    resource = rr.lock();
		    ResourceContext ctxt = new ResourceContext(
			newdir.getContext());
		    resource.setContext(ctxt, true);
		    if (resource instanceof FramedResource) {
			((FramedResource)resource).
			    removeStructureChangedListener(this);
			((FramedResource)resource).
			    addStructureChangedListener(newdir);
		    }
		} catch (InvalidResourceException ex) {
		    // do nothing , continue
		} finally {
		    rr.unlock();
		}
	    }
	}
	super.delete();
    }

    /**
     * Delete that resource container.
     * @exception MultipleLockException If somone else has locked the 
     * resource.
     */
    public synchronized void delete() 
	throws MultipleLockException
    {
	disableEvent();
	ResourceSpace space = getSpace();
	//delete our children
	acquireChildren();
	deleteChildren();
	disableEvent();
	SpaceEntry sentry = getChildrenSpaceEntry();
	//delete myself
	super.delete();
	space.deleteChildren(sentry);
    }

    protected synchronized void deleteChildren() 
	throws MultipleLockException
    {
	disableEvent();
	acquireChildren();
	Enumeration e = enumerateResourceIdentifiers();
	while (e.hasMoreElements())
	    delete((String)e.nextElement());
	enableEvent();
    }

    /**
     * This resource is being unloaded.
     * The resource is being unloaded from memory, perform any additional
     * cleanup required.
     */
    public void notifyUnload() {
	super.notifyUnload();
	// anything else?
    }

    protected boolean acquired = false;

    /**
     * Acquire our children. Must be called before all child manipulation.
     */
    protected synchronized void acquireChildren() {
	if (!acquired) {
	    ResourceSpace space = getSpace();
	    space.acquireChildren( getChildrenSpaceEntry() );
	    acquired = true;
	}
    }

    /**
     * Add an initialized resource into this store container instance.
     * @param resource The resource to be added to the store.
     */

    protected synchronized ResourceReference addResource(Resource resource, 
							 Hashtable defs) {
	acquireChildren();
	ResourceReference rr = getSpace().addResource(getChildrenSpaceEntry() ,
						      resource, 
						      defs);
	resource.getContext().setResourceReference(rr);
	if (resource instanceof FramedResource) {
	    FramedResource fres = (FramedResource) resource;
	    fres.addStructureChangedListener(this);
	}
	markModified() ;
	postStructureChangedEvent(rr, Events.RESOURCE_CREATED);
	return rr;
    }

    /**
     * Initialize and register the given resource within that container.
     * @param name The identifier for the resource.
     * @param resource An unitialized resource instance.
     * @param defs A default set of init attribute values (may be
     * <strong>null</strong>).
     * @exception InvalidResourceException If an error occurs during
     * the registration.
     */

    public void registerResource(String name,
				 Resource resource,
				 Hashtable defs)
	throws InvalidResourceException
    {
	acquireChildren();
	// Create a default set of attributes:
	if ( defs == null )
	    defs = new Hashtable(4) ;
	defs.put(id, name);
	ResourceContext context =  updateDefaultChildAttributes(defs);
	if (context != null) {
	    resource.initialize(defs);
	    ResourceReference rr;
	    rr = getSpace().addResource(getChildrenSpaceEntry(),
					resource, 
					defs);
	    context.setResourceReference(rr);
	    if (resource instanceof FramedResource) {
		FramedResource fres = (FramedResource) resource;
		fres.addStructureChangedListener(this);
		// send event
	    }
	    markModified();
	    postStructureChangedEvent(rr, Events.RESOURCE_CREATED);
	} else {
	    throw new InvalidResourceException(getIdentifier(),
					       name,
					       "unable to get context");
	}
    }

    /**
     * Initialize ourself.
     * As we are a container resource that really contains something, we make
     * sure our URL ends properly with a slash.
     * @param values Our default attribute values.
     */
    public void initialize(Object values[]) {
	super.initialize(values);
	disableEvent();
	// If my URL doesn't end with a slah, correct it:
	String url = getURLPath() ;
	if ((url != null) && ! url.endsWith("/") )
	    setValue(ATTR_URL, url+"/") ;
	acquired = false;
	enableEvent();
    }
}


