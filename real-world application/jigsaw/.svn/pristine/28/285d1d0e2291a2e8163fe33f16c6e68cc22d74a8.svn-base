// Resource.java
// $Id: Resource.java,v 1.2 2010/06/15 17:52:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Hashtable;
import java.util.Vector;

import java.lang.ArrayIndexOutOfBoundsException;

/**
 * The resource class describes an object, accessible through the server.
 * Resource objects are required to have the following properties: 
 * <ul>
 * <li>They must be persistent (their life-time can span multiple server 
 * life-time).
 * <li>They must be editable, so that one can change some of their aspects
 * (such as any associated attribute).
 * <li>They must be self-described: each resource must now what kind
 * of attribute it <em>understands</em>.
 * <li>They must be able to update themselves: some of the meta-information
 * associated with a resource may require lot of CPU to compute. 
 * <li>They must implement some name-service policy.
 * </ul>
 * <p>These resource objects do not define how they are accessed. See the
 * sub-classes for specific accesses. 
 */

public class Resource extends AttributeHolder {
    private static final boolean debugunload = false;

    /**
     * Attribute index - The index of the resource store entry attribute.
     */
    protected static int ATTR_STORE_ENTRY = -1;
    /**
     * Attribute index - The index for the identifier attribute.
     */
    protected static int ATTR_IDENTIFIER = -1 ;
    /**
     * Attribute index - Associated resource frames
     */
    protected static int ATTR_RESOURCE_FRAMES = -1;
    /**
     * Attribute index - The index for our parent attribute.
     */
    protected static int ATTR_PARENT = -1 ;
    /**
     * Attribute index - The hierarchical context of the resource.
     */
    protected static int ATTR_CONTEXT = -1;
    /**
     * Attribute index - The index for our URL attribute.
     */
    protected static int ATTR_URL = -1;
    /**
     * Attribute index - The index for the last-modified attribute.
     */
    protected static int ATTR_LAST_MODIFIED = -1 ;
    /**
     * Attribute index - The help URL for this resource (Ref doc)
     */
    protected static int ATTR_HELP_URL = -1 ;

    public static String id = "identifier".intern();
    public static String co = "context".intern();

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our own class:
	try {
	    cls  = Class.forName("org.w3c.tools.resources.Resource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// Our parent resource (the one that created us)
	a = new ObjectAttribute("parent",
				"org.w3c.tools.resources.Resource",
				null,
				Attribute.COMPUTED|Attribute.DONTSAVE);
	ATTR_PARENT = AttributeRegistry.registerAttribute(cls, a) ;
        // Our runtime context
	a = new ObjectAttribute("context",
				"org.w3c.tools.resources.ResourceContext",
				null,
				Attribute.COMPUTED|Attribute.DONTSAVE);
	ATTR_CONTEXT = AttributeRegistry.registerAttribute(cls, a) ;
	// The resource store entry for that resource:
	a = new ObjectAttribute("store-entry"
				, "java.lang.Object"
				, null
				, Attribute.DONTSAVE);
	ATTR_STORE_ENTRY = AttributeRegistry.registerAttribute(cls, a);
	// The identifier attribute:
	a = new StringAttribute("identifier"
				, null
				, Attribute.MANDATORY|Attribute.EDITABLE);
	ATTR_IDENTIFIER = AttributeRegistry.registerAttribute(cls, a);
	// The frames associated to that resource:
	a = new FrameArrayAttribute("frames"
				    , null
				    , Attribute.COMPUTED);
	ATTR_RESOURCE_FRAMES = AttributeRegistry.registerAttribute(cls, a);
	// Our URL
	a = new StringAttribute("url",
				null,
				Attribute.COMPUTED|Attribute.DONTSAVE);
	ATTR_URL = AttributeRegistry.registerAttribute(cls, a) ;
	// The last modified attribute:
	a = new DateAttribute("last-modified",
			      null,
			      Attribute.COMPUTED|Attribute.EDITABLE) ;
	ATTR_LAST_MODIFIED = AttributeRegistry.registerAttribute(cls,a);
	// The help url attribute
	a = new StringAttribute("help-url",
				null,
				Attribute.COMPUTED);
	ATTR_HELP_URL = AttributeRegistry.registerAttribute(cls,a);
    }

    public Object getClone(Object values[]) {
	// The frame attribute needs one more level of cloning:
	ResourceFrame f[] = (ResourceFrame[]) values[ATTR_RESOURCE_FRAMES];
	if ( f != null ) {
	    ResourceFrame c[] = new ResourceFrame[f.length] ;
	    for (int i = 0 ; i < f.length ; i++) {
		if ( f[i] == null )
		    c[i] = null;
		else
		    c[i] = (ResourceFrame) f[i].getClone();
	    }
	    values[ATTR_RESOURCE_FRAMES] = c;
	}
	return super.getClone(values);
    }

    /**
     * Get this resource parent resource.
     * The parent of a resource can be either <strong>null</strong> if it is
     * the server root resource, or any Resource.
     * @return An instance of ResourceReference, or <strong>null</strong>
     */
    public ResourceReference getParent() {
	ResourceContext context = unsafeGetContext();
	if (context == null) //are we external?
	    return null;
	return context.getContainer();
    }

    /**
     * Get the file part of the URL this resource is attached to.
     * @return An URL object specifying the location in the information 
     *    space of this resource.
     */
    public String getURLPath() {
	return getString(ATTR_URL, null) ;
    }


    /**
     * Get the file part of the URL this resource is attached to.
     * @return An URL object specifying the location in the information 
     *    space of this resource.
     */
    public String unsafeGetURLPath() {
	return unsafeGetString(ATTR_URL, null) ;
    }

    /**
     * Get the server this resource is served by.
     * @return The first instance of Jigsaw this resource was attached to.
     */
    public ServerInterface getServer() {
	return ((ResourceContext) 
		              unsafeGetValue(ATTR_CONTEXT, null)).getServer();
    }

    /**
     * Get this resource's help url.
     * @return An URL, encoded as a String, or <strong>null</strong> if not
     * available.
     */
    public String getHelpURL() {
	return getString(ATTR_HELP_URL, null);
    }

    private String computeHelpUrl() {
	try {
	    StringBuffer sb = new StringBuffer(128);
	    sb.append(getServer().getDocumentationURL());
	    sb.append('/');
	    sb.append(getClass().getName());
	    sb.append(".html");
	    return sb.toString().intern();
	} catch (Exception ex) {
	    return null;
	}
	
    }

    synchronized public Object getValue (int idx, Object def) {
	if ((idx == ATTR_HELP_URL) && (values[ATTR_HELP_URL] == null))
	    values[ATTR_HELP_URL] = computeHelpUrl();
	return super.getValue(idx, def);
    }
    
    public Object unsafeGetValue (int idx, Object def) {
	if ((idx == ATTR_HELP_URL) && (values[ATTR_HELP_URL] == null))
	    values[ATTR_HELP_URL] = computeHelpUrl();
	return super.unsafeGetValue(idx, def);
    }

    /**
     * Get the help URL for that resource's topic.
     * @param topic The topic you want help for.
     * @return A String encoded URL, or <strong>null</strong> if none
     * was found.
     */
    public String getHelpURL(String topics) {
	return null;
    }

    /**
     * Get the hierarchical context for that resource.
     * @return A ResourceContext instance, guaranteed not to be <strong>null
     * </strong>
     */
    protected ResourceContext getContext() {
	return (ResourceContext) getValue(ATTR_CONTEXT, null);
    }

    protected ResourceContext unsafeGetContext() {
	return (ResourceContext) unsafeGetValue(ATTR_CONTEXT, null);
    }

    /**
     * Set the given context as the current context of this resource.
     * @param context The new context.
     */
    protected void setContext(ResourceContext context) {
	context.setResourceReference(getResourceReference());
	setValue(ATTR_CONTEXT, context);
    }

    /**
     * Set the given context as the current context of this resource.
     * @param context The new context.
     * @param keepmodules If true the new context will have the same
     * modules than the old one.
     */
    protected void setContext(ResourceContext context, boolean keepmodules) {
	context.setResourceReference(getResourceReference());
	if (keepmodules) {
	    ResourceContext ctxt = getContext();
	    if (ctxt != null)
		context.modules = ctxt.modules;
	}
	setValue(ATTR_CONTEXT, context);
    }

    /**
     * Get the store entry for that resource.
     * Only the resource store in charge of this resource knows about the
     * type of the resulting object. Buy declaring the class of that object
     * private, the resource store can assume some private access to it.
     * @return A java Object instance, or <strong>null</strong> if no 
     * store entry is attached to that resource.
     */

    public Object getStoreEntry() {
	return getValue(ATTR_STORE_ENTRY, null);
    }

    /**
     * Get this resource identifier.
     * @return The String value for the identifier.
     */
    public String getIdentifier() {
	return getString(ATTR_IDENTIFIER, null) ;
    }

    /**
     * Get this resource identifier.
     * @return The String value for the identifier.
     */
    public String unsafeGetIdentifier() {
	return unsafeGetString(ATTR_IDENTIFIER, null) ;
    }

    /**
     * Get the space entry for that resource. This Object is use to
     * retrieve the resource in the resource space.
     * @return A spaceEntry instance.
     */
    protected SpaceEntry getSpaceEntry() {
	ResourceReference rr = getParent();
	if (rr != null) {
	    try {
		ContainerResource cont = (ContainerResource) rr.lock();
		return new SpaceEntryImpl(cont);
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	}
	return null;
    }

    /**
     * Get the ResourceSpace where this resource is stored.
     * @return A ResourceSpace instance.
     */
    protected ResourceSpace getSpace() {
	ResourceContext context = unsafeGetContext();
	if (context == null) {
	    context = getContext();
	}
	if (context != null) {
	    return context.getSpace();
	}
	return null;
    }

    /**
     * Get the ResourceReference of that resource. ResourceReference is the
     * only public way to access a resource.
     * @return a ResourceReference instance.
     */
    public ResourceReference getResourceReference() {
	ResourceContext context = getContext();
	if (context != null)
	    return context.getResourceReference();
	return null;
    }

    /**
     * Get the ResourceReference of that resource. ResourceReference is the
     * only public way to access a resource.
     * @return a ResourceReference instance.
     */
    public ResourceReference unsafeGetResourceReference() {
	ResourceContext context = unsafeGetContext();
	if (context != null)
	    return context.getResourceReference();
	return null;
    }

    /**
     * Initialize and attach a new ResourceFrame to that resource.
     * @param frame An uninitialized ResourceFrame instance.
     * @param defs A default set of attribute values.
     */
    public void registerFrame(ResourceFrame frame, Hashtable defs) {
	synchronized (this) {
	    ResourceFrame frames[] = null;
	    frames = (ResourceFrame[]) getValue(ATTR_RESOURCE_FRAMES, null);
	    // Initialize the frame with given default attributes:
	    if ( defs.get(id) == null ) {
		String fname = "frame-"+((frames == null) ? 0 : frames.length);
		defs.put(id, fname.intern());
	    }
	    frame.initialize(defs);
	    // Look for a free slot frame:
	    if ( frames == null ) {
		frames    = new ResourceFrame[1];
		frames[0] = frame;
	    } else {
		int slot = -1;
		// Look for a free slot:
		for (int i = 0 ; i < frames.length ; i++) {
		    if ( frames[i] == null ) {
			slot = i;
			break;
		    }
		}
		if ( slot >= 0 ) {
		    // Free slot available:
		    frames[slot] = frame;
		} else {
		    // Resize frames:
		    ResourceFrame nf[] = new ResourceFrame[frames.length+1];
		    System.arraycopy(frames, 0, nf, 0, frames.length);
		    nf[frames.length] = frame;
		    frames = nf;
		}
	    }
	    // Set the frames:
	    setValue(ATTR_RESOURCE_FRAMES, frames);
	}
    }

    /**
     * Unregister a resource frame from the given resource.
     * @param frame The frame to unregister from the resource.
     */
    public synchronized void unregisterFrame(ResourceFrame frame) {
	ResourceFrame frames[] = null;
	frames = (ResourceFrame[]) getValue(ATTR_RESOURCE_FRAMES, null);
	if ( frames != null ) {
	    ResourceFrame f[] = new ResourceFrame[frames.length-1];
	    int j=0;
	    for (int i = 0; i < frames.length ; i++) {
		if ( frames[i] == frame ) {
		    // got it, copy the end of the array
		    System.arraycopy(frames, i+1, f, j, frames.length-i-1);
		    setValue(ATTR_RESOURCE_FRAMES, f);
		    return;
		} else {
		    try {
			f[j++] = frames[i];
		    } catch (ArrayIndexOutOfBoundsException ex) {
			return; // no modifications, return
		    }
		}
	    }
	}
    }

    /**
     * Collect all frames.
     * @return An array of ResourceFrame, containing a set of frames instances
     * or <strong>null</strong> if no resource frame is available.
     */
    public synchronized ResourceFrame[] getFrames() {
	return (ResourceFrame[]) getValue(ATTR_RESOURCE_FRAMES, null);
    }

    /**
     * Collect all frames.
     * @return An array of ResourceFrame, containing a set of frames instances
     * or <strong>null</strong> if no resource frame is available.
     */
    public ResourceFrame[] unsafeGetFrames() {
	return (ResourceFrame[]) unsafeGetValue(ATTR_RESOURCE_FRAMES, null);
    }

    /**
     * Collect any frame that's an instance of the given class.
     * @param cls The class of frames we are looking for.
     * @return An array of ResourceFrame, containing a set of frames instances
     * of the given class, or <strong>null</strong> if no resource frame is
     * available.
     */
    public synchronized ResourceFrame[] collectFrames(Class c) {
	ResourceFrame frames[] = null;
	frames = (ResourceFrame[]) getValue(ATTR_RESOURCE_FRAMES, null);
	if ( frames != null ) {
	    Vector v = new Vector(frames.length);
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( c.isInstance(frames[i]) )
		    v.addElement(frames[i]);
	    }
	    int sz = v.size();
	    if ( sz > 0 ) {
		ResourceFrame ret[] = new ResourceFrame[sz];
		v.copyInto(ret);
		return ret;
	    }
	}
	return null;
    }


    /**
     * Collect any frame that's an instance of the given class.
     * @param cls The class of frames we are looking for.
     * @return An array of ResourceFrame, containing a set of frames instances
     * of the given class, or <strong>null</strong> if no resource frame is
     * available.
     */
    ResourceFrame[] unsafeCollectFrames(Class c) {
	ResourceFrame frames[] = null;
	frames = (ResourceFrame[]) unsafeGetValue(ATTR_RESOURCE_FRAMES, null);
	if ( frames != null ) {
	    Vector v = new Vector(frames.length);
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( c.isInstance(frames[i]) )
		    v.addElement(frames[i]);
	    }
	    int sz = v.size();
	    if ( sz > 0 ) {
		ResourceFrame ret[] = new ResourceFrame[sz];
		v.copyInto(ret);
		return ret;
	    }
	}
	return null;
    }

    /**
     * Get the first occurence of a frame of the given class.
     * @param cls The class of te frame to look for.
     * @return A ResourceFrame instance, or <strong>null</strong>.
     */
    public synchronized ResourceFrame getFrame(Class c) {
	ResourceFrame frames[] = null;
	frames = (ResourceFrame[]) getValue(ATTR_RESOURCE_FRAMES, null);
	if ( frames != null ) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( c.isInstance(frames[i]) )
		    return frames[i];
	    }
	}
	return null;
    }
	
    /**
     * Get the first occurence of a frame of the given class.
     * @param cls The class of te frame to look for.
     * @return A ResourceFrame instance, or <strong>null</strong>.
     */
    public ResourceFrame unsafeGetFrame(Class c) {
	ResourceFrame frames[] = null;
	frames = (ResourceFrame[]) unsafeGetValue(ATTR_RESOURCE_FRAMES, null);
	if ( frames != null ) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( c.isInstance(frames[i]) )
		    return frames[i];
	    }
	}
	return null;
    }    

    /**
     * Get an attached frame attribute value.
     * This method really is a short-hand that combines a <code>getFrame</code>
     * and <code>getValue</code> method call.
     * @param cls The class of the frame we want the value of.
     * @param idx The attribute index.
     * @param def The default value (if the attribute value isn't defined).
     * @return The attribute value as an Object instance, or the provided
     * default value if the attribute value isn't defined.
     */
    public synchronized Object getValue(Class c, int idx, Object def) {
	ResourceFrame frame = getFrame(c);
	if ( frame != null )
	    return frame.getValue(idx, def);
	throw new IllegalAttributeAccess(this, idx);
    }

    /**
     * Set an attached frame attribute value.
     * This method really is a short-hand that combines a <code>getFrame</code>
     * and a <code>setValue</code> method call.
     * @param cls The class of the frame we want to modify.
     * @param idx The attribute to modify.
     * @param val The new attribute value.
     */
    public synchronized void setValue(Class c, int idx, Object val) {
	ResourceFrame frame = getFrame(c);
	if ( frame != null ) {
	    frame.setValue(idx, val);
	    markModified();
	    return;
	}
	throw new IllegalAttributeAccess(this, idx);
    }

    /**
     * Get this resource last modification time.
     * @return A long giving the date of the last modification time, or
     *    <strong>-1</strong> if undefined.
     */
    public long getLastModified() {
	return getLong(ATTR_LAST_MODIFIED, (long) -1) ;
    }

    /**
     * Mark this resource as having been modified.
     */
    public void markModified() {
	ResourceSpace space = getSpace();
	if ((space != null) && (getSpaceEntry() != null)) {
	    synchronized (this) {
		space.markModified(getSpaceEntry(), this);
	    }
	}
	super.setValue(ATTR_LAST_MODIFIED, 
		       new Long(System.currentTimeMillis()));
    }

    /**
     * We overide setValue, to mark the resource as modified.
     * @param idx The index of the attribute to modify.
     * @param value The new attribute value.
     */
    public void setValue(int idx, Object value) {
	// Changing the identifier of a resource needs some special tricks:
	if ( idx == ATTR_IDENTIFIER ) {
	    String oldid = getIdentifier();
	    try {
		super.setValue(idx, value);
	    } catch (IllegalAttributeAccess ex) {
		// We were not able to change the identifier, rethrow
		throw ex;
	    }
	    // Change was successfull, update the resource space:
	    if (getSpaceEntry() != null) {
		ResourceSpace space = getSpace();
		space.renameResource(getSpaceEntry(), oldid, (String) value);
	    }
	    //modify the URL path
	    ResourceReference rr = getParent();
	    if (rr != null) {
		try {
		    Resource r = rr.lock();
		    setValue(ATTR_URL,
			     r.getURLPath()+
			     java.net.URLEncoder.encode((String) value));
		} catch(Exception ex) {
		    ex.printStackTrace();
		} finally {
		    rr.unlock();
		}
	    }
	    markModified();
	    return;
	}
	// Normal setValue, but markModified before leaving:
	super.setValue(idx, value) ;
	if (( ! attributes[idx].checkFlag(Attribute.DONTSAVE) ) &&
	    ( idx != ATTR_LAST_MODIFIED))
	    markModified() ;
    }

    /**
     * Is that resource willing to be unloaded.
     * This method is a bit tricky to implement. The guideline is that you
     * should not dynamically change the returned value (since you can't 
     * control what happens between a call to that method and a call to
     * the <code>notifyUnload</code> method).
     * <p>Returning <strong>false</strong> should never be needed, except
     * for very strange resources.
     * @return A boolean <strong>true</strong> if the resource can be unloaded
     * <strong>false</strong> otherwise.
     */

    public boolean acceptUnload() {
	if ( debugunload ) {
	    try {
		System.out.println(getValue("url","<??>")+": acceptUnload");
	    } catch (IllegalAttributeAccess ex) {
	    }
	}
	return true;
    }

    /**
     * This resource is being unloaded.
     * The resource is being unloaded from memory, perform any additional
     * cleanup required.
     */
    public void notifyUnload() {
	if ( debugunload ) {
	    try {
		System.out.println(getValue("url","<??>")+": unloaded.");
	    } catch (IllegalAttributeAccess ex) {
	    }
	}
	values = null ;
    }

    /**
     * unloaded?
     * @return true if the resource has been unloaded.
     */
    public boolean isUnloaded() {
	return (values == null);
    }

    /**
     * The web admin wants us to update any out of date attribute.
     */
    public void updateAttributes() {
	return ;
    }

    /**
     * Check if this resource is loked more than one time.
     * @exception MultipleLockException is thrown if true.
     */
    protected void checkMultipleLock(ResourceReference rr) 
	throws MultipleLockException
    {
	if (rr.nbLock() > 1)
	    throw new MultipleLockException(rr.nbLock(), this, "can't delete");
    }

    /**
     * Delete this Resource instance, and remove it from its store.
     * This method will erase definitely this resource, for ever, by removing
     * it from its resource store (when doable).
     * @exception MultipleLockException if someone else has locked this 
     * resource
     */
    public synchronized void delete() 
	throws MultipleLockException
    {
	ResourceSpace space = getSpace();

	if ((space != null) && (getSpaceEntry() != null)) {
	    ResourceReference self = getResourceReference();
	    if (self != null) {
		synchronized (self) {
		    checkMultipleLock(self);
		    space.deleteResource(getSpaceEntry(), this);
		}
	    } else {
		space.deleteResource(getSpaceEntry(), this);
	    }
	}
    }

    public boolean isInitialized() {
	return (values != null);
    }

    /**
     * Set the values. (MUST be called before initialize).
     * @param defs The Hashtable containing the values.
     */
    public void pickleValues(Hashtable defs) {
	Object nvalues[] = new Object[attributes.length];
	for (int i = 0 ; i < nvalues.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    if ((i == ATTR_HELP_URL) && (def != null) 
		&& (def instanceof String)) {
		nvalues[i] = ((String) def).intern();
	    } else {	    
		nvalues[i] = def ;
	    }	    
	}
	this.values = nvalues ;
    }

   /**
     * Initialization method for attribute holders.
     * This method allows to initialize an attribute holder by providing
     * its attributes values through a Hashtable mapping attribute names
     * to attribute values.
     * @param defs The Hashtable containing the default values.
     */

    public void initialize(Hashtable defs) {
	Object values[] = ((this.values == null)
			   ? new Object[attributes.length] 
			   : this.values);
	for (int i = 0 ; i < values.length ; i++) {
	    String attrname = attributes[i].getName() ;
	    Object def      = defs.get(attrname) ;
	    if ( values[i] == null ) {
		// for help_url, we can save lots of space by using 
		// String.intern()
		if ((i == ATTR_HELP_URL) && (def != null) 
		        && (def instanceof String)) {
		    values[i] = ((String) def).intern();
		} else {	    
		    values[i] = def ;
		}
	    }
	}
	initialize(values) ;
    }

    public void initialize(Object values[]) {
	super.initialize(values);
    }

    /**
     * Create an empty resource instance.
     * Initialize the instance attributes description, and its values.
     */
    public Resource() {
	super() ;
    }
}
