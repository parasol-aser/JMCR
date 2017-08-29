// FramedResource.java
// $Id: FramedResource.java,v 1.2 2010/06/15 17:52:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.EventObject;
import java.util.Hashtable;

import java.io.PrintStream;

import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.tools.resources.event.AttributeChangedListener;
import org.w3c.tools.resources.event.Events;
import org.w3c.tools.resources.event.FrameEvent;
import org.w3c.tools.resources.event.FrameEventListener;
import org.w3c.tools.resources.event.ResourceEvent;
import org.w3c.tools.resources.event.ResourceEventMulticaster;
import org.w3c.tools.resources.event.ResourceEventQueue;
import org.w3c.tools.resources.event.StructureChangedEvent;
import org.w3c.tools.resources.event.StructureChangedListener;

/**
 * A FramedResource manage frames which are called during the
 * lookup and the perform.
 */
public class FramedResource extends Resource 
                            implements FrameEventListener
{

    /**
     * The ResourceReference of frames.
     */
    class FrameReference implements ResourceReference {

	Class             frameClass = null;
	String            identifier = null;
	ResourceReference framedr    = null;

	int lockCount = 0;

	public void updateContext(ResourceContext ctxt) {
	    //nothing to do
	}

	public int nbLock() {
	    return lockCount;
	}

	/**
	 * Lock the refered resource in memory.
	 * @return A real pointer to the resource.
	 */

	public Resource lock()
	    throws InvalidResourceException 
	{
	    FramedResource res = (FramedResource)framedr.lock();
	    lockCount++;
	    return res.getFrame(frameClass, identifier);
	}

	/**
	 * Lock the refered resource in memory.
	 * @return A real pointer to the resource.
	 */

	public Resource unsafeLock()
	    throws InvalidResourceException 
	{
	    FramedResource res = (FramedResource)framedr.lock();
	    lockCount++;
	    return res.unsafeGetFrame(frameClass, identifier);
	}

	/**
	 * Unlock that resource from memory.
	 */

	public void unlock() {
	    framedr.unlock();
	    lockCount--;
	}

	/**
	 * Is that resource reference locked ?
	 */

	public boolean isLocked() {
	    return lockCount != 0;
	}

	FrameReference (ResourceFrame rframe, ResourceReference framedr) {
	    this.frameClass = rframe.getClass();
	    this.framedr    = framedr;
	    this.identifier = rframe.getIdentifier();
	}
    }

    /**
     * Debug flag
     */
    protected final boolean debugEvent = false;

    /**
     * Do we handle events?
     */
    protected boolean event_disabled = false;

    /**
     * Our frames references.
     */
    protected Hashtable framesRef = null; //<ResourceFrame, Reference>

    /**
     * Our AttributeChangedListener.
     */
    protected AttributeChangedListener attrListener = null;

    /**
     * Our StructureChangedListener.
     */
    protected StructureChangedListener structListener = null;

    protected void disableEvent() {
	event_disabled = true;
    }

    protected void enableEvent() {
	event_disabled = false;
    }

    protected boolean eventDisabled() {
	return event_disabled;
    }

    /**
     * Attribute index - The object identifier.
     */
    protected static int ATTR_OID = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	// Get a pointer to our class:
	try {
	    cls = Class.forName("org.w3c.tools.resources.FramedResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The object identifier, *should* be uniq (see below)
	a = new IntegerAttribute("oid",
				 null,
				 Attribute.COMPUTED);
	ATTR_OID = AttributeRegistry.registerAttribute(cls, a);
    }

    public Object getClone(Object values[]) {
	FramedResource clone   = (FramedResource) super.getClone(values);
	clone.framesRef      = new Hashtable(3);
	return clone;
    }

    /**
     * Get this resource's object identifier.
     * An object identifier is to be used specifically in etags. It's purpose
     * is to uniquify the etag of a resource. It's computed as a random number
     *, on demand only.
     * @return A uniq object identifier for that resource, as an inteeger.
     */
    public int getOid() {
	int oid = getInt(ATTR_OID, -1);
	if ( oid == -1 ) {
	    double d = Math.random() * ((double) Integer.MAX_VALUE);
	    setInt(ATTR_OID, oid = (int) d);
	}
	return oid;
    }

    protected void displayEvent(FramedResource fr, EventObject evt) {
	System.out.println(">>> ["+fr.getIdentifier()+"] has receive "+evt);
    }

    /**
     * This handles the <code>FRAME_ADDED</code> kind of events.
     * @param evt The FrameEvent.
     */

    public void frameAdded(FrameEvent evt) {
	if (debugEvent) 
	    displayEvent( this, evt );
	if (! isUnloaded()) 
	    markModified();
    }

    /**
     * This handles the <code>FRAME_MODIFIED</code> kind of events.
     * @param evt The event describing the change.
     */

    public void frameModified(FrameEvent evt) {
	if (debugEvent) 
	    displayEvent( this, evt );
	if (! isUnloaded()) 
	    markModified();
    }

    /**
     * A frame is about to be removed
     * This handles the <code>FRAME_REMOVED</code> kind of events.
     * @param evt The event describing the change.
     */

    public void frameRemoved(FrameEvent evt) {
	if (debugEvent) 
	    displayEvent( this, evt );
	if (! isUnloaded()) 
	    markModified();
    }

    /**
     * Initialize and attach a new ResourceFrame to that resource.
     * @param frame An uninitialized ResourceFrame instance.
     * @param defs A default set of attribute values.
     */

    public void registerFrame(ResourceFrame frame, Hashtable defs) {
	super.registerFrame(frame,defs);
	frame.addFrameEventListener(this);
	addAttributeChangedListener(frame);
	frame.registerResource(this);
    }

    /**
     * Register a new ResourceFrame if none (from the same class) has been 
     * registered.
     * @param classname The ResourceFrame class
     * @param identifier The ResourceFrame identifier
     * @exception ClassNotFoundException if the class can't be found
     * @exception IllegalAccessException if the class or initializer is not 
     * accessible
     * @exception InstantiationException if the class can't be instanciated
     * @exception ClassCastException if the class is not a ResourceFrame
     */
    protected void registerFrameIfNone(String classname, String identifier) 
	throws ClassNotFoundException,
	       IllegalAccessException,
	       InstantiationException,
	       ClassCastException
    {
	Class frameclass = 
	    Class.forName(classname);
    //Added by Jeff Huang
    //TODO: FIXIT
	ResourceFrame frame = getFrame(frameclass);
	if (frame == null) {
	    Hashtable defs = new Hashtable(3);
	    defs.put(id , identifier);
	    registerFrame( (ResourceFrame)frameclass.newInstance() , defs );
	}
    }
				     

    /**
     * Unregister a resource frame from the given resource.
     * @param frame The frame to unregister from the resource.
     */

    public synchronized void unregisterFrame(ResourceFrame frame) {
	super.unregisterFrame(frame);
	frame.unregisterResource(this);
	frame.removeFrameEventListener(this);
	removeAttributeChangedListener(frame);
    }

    private ResourceReference[] getReferenceArray(ResourceFrame[] frames) {
	if (frames == null)
	    return null;
	ResourceReference[] refs = new ResourceReference[frames.length];
	ResourceReference rr = null;
	for (int i=0 ; i < frames.length ; i++) {
	    rr = (ResourceReference)framesRef.get(frames[i]);
	    if (rr == null) {
		rr = (ResourceReference) 
		    new FrameReference(frames[i], 
				       getResourceReference());
		framesRef.put(frames[i],rr);
	    }
	    refs[i] = rr;
	}
	return refs;
    }

    /**
     * Collect all frames references.
     * @return An array of ResourceReference, containing a set of 
     * FrameReference instances or <strong>null</strong> if no resource
     * frame is available.
     */
    public synchronized ResourceReference[] getFramesReference() {
	return getReferenceArray(getFrames());
    }

    /**
     * Collect any frame reference pointing to an instance of the given class.
     * @param cls The class of frames we are looking for.
     * @return An array of ResourceReference, containing a set of 
     * FrameReference pointing to instances of the given class, or 
     * <strong>null</strong> if no resource frame is available.
     */
    public synchronized ResourceReference[] collectFramesReference(Class c) {
	return getReferenceArray(collectFrames(c));
    }

    /**
     * Get the first occurence of a frame of the given class.
     * @param cls The class of te frame to look for.
     * @return A ResourceReference instance, or <strong>null</strong>.
     */
    public synchronized ResourceReference getFrameReference(Class c) {
	ResourceFrame     frame = getFrame(c);
	if (frame == null)
	    return null;
	ResourceReference  rr = 
	    (ResourceReference)framesRef.get(frame);
	if (rr == null) {
	    rr = (ResourceReference) 
		new FrameReference(frame, 
				   getResourceReference());
	    framesRef.put(frame,rr);
	}
	return rr;
    }

    /**
     * Get The FrameReference of the given frame, or <strong>null</strong>
     * if the frame is not registered.
     * @param frame The ResourceFrame.
     * @return A ResourceReference instance.
     */
    public synchronized 
	ResourceReference getFrameReference(ResourceFrame frame) {
	ResourceReference rr = 
	    (ResourceReference)framesRef.get(frame);
	if (rr == null) {
	    rr = (ResourceReference) 
		new FrameReference(frame, 
				   getResourceReference());
	    framesRef.put(frame,rr);
	}
	return rr;
    }

    /**
     * Get the frame of the given class and identifier.
     * @param cls The class of frames we are looking for.
     * @param identifier the frame identifier
     * @return a ResourceFrame instance of <strong>null</strong>
     */
    public synchronized ResourceFrame getFrame(Class c, String identifier) {
	ResourceFrame frames[] = collectFrames(c);
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		ResourceFrame fr = frames[i];
		if (fr.getIdentifier().equals(identifier))
		    return fr;
	    }
	}
	return null;
    }

    /**
     * Get the frame of the given class and identifier.
     * @param cls The class of frames we are looking for.
     * @param identifier the frame identifier
     * @return a ResourceFrame instance of <strong>null</strong>
     */
    ResourceFrame unsafeGetFrame(Class c, String identifier) {
	ResourceFrame frames[] = collectFrames(c);
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		ResourceFrame fr = frames[i];
		if (fr.getIdentifier().equals(identifier))
		    return fr;
	    }
	}
	return null;
    }

    /**
     * Get the frame of the given class.
     * @param classname the class name
     * @return a ResourceFrame instance of null.
     */
    public synchronized ResourceFrame getFrame(String classname) {
	try {
	    Class c = Class.forName(classname);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    return getFrame(c);
	} catch (Exception ex) {
	    return null;
	}
    }

    /**
     * (AWT Like), dspatch the Event to all our listeners.
     * @param evt The resourceEvent to dispatch.
     */
    public void processEvent(ResourceEvent evt) {
	if (evt instanceof StructureChangedEvent) {
	    fireStructureChangedEvent((StructureChangedEvent)evt);
	} else if (evt instanceof AttributeChangedEvent) {
	    fireAttributeChangeEvent((AttributeChangedEvent)evt);
	}
    }

    /**
     * Post an Event in the Event Queue.
     * @param evt The Event to post.
     */
    public void postEvent(ResourceEvent evt) {
	if (eventDisabled())
	    return;
	ResourceSpace space = getSpace();
	if (space != null) 
	    space.getEventQueue().sendEvent(evt);
    }

    /**
     * Add an attribute change listener.
     * @param l The new attribute change listener.
     */

    public void addAttributeChangedListener(AttributeChangedListener l) {
	attrListener = ResourceEventMulticaster.add(attrListener, l);
    }

    /**
     * Remove an attribute change listener.
     * @param l The listener to remove.
     */

    public void removeAttributeChangedListener(AttributeChangedListener l) {
	attrListener = ResourceEventMulticaster.remove(attrListener, l);
    }

    /**
     * post an attribute change event. Actually this kind of event should 
     * not be posted. So fire them!
     * @param idx The index of the attribute that has changed.
     * @param newvalue The new value for that attribute.
     */

    protected void postAttributeChangeEvent(int idx, Object newvalue) {
	if (eventDisabled())
	    return;
	if (( attrListener != null ) && (getResourceReference() != null)) {
	    AttributeChangedEvent evt = 
		new AttributeChangedEvent(getResourceReference(),
					  attributes[idx],
					  newvalue);
	    fireAttributeChangeEvent(evt);
	}
    }

    /**
     * Fire an attribute change event.
     * @param evt the AttributeChangedEvent to fire.
     */
    protected void fireAttributeChangeEvent(AttributeChangedEvent evt) {
	if ( attrListener != null )
	    attrListener.attributeChanged(evt);
    }

    /**
     * Add a structure change listener.
     * @param l The new structure change listener.
     */

    public void addStructureChangedListener(StructureChangedListener l) {
	structListener = ResourceEventMulticaster.add(structListener, l);
    }

    /**
     * Remove a structure change listener.
     * @param l The listener to remove.
     */

    public void removeStructureChangedListener(StructureChangedListener l) {
	structListener = ResourceEventMulticaster.remove(structListener, l);
    }

    /**
     * post an structure change event.
     * @param rr the ResourceReference of the source.
     * @param type The type of the event.
     */
    protected void postStructureChangedEvent(ResourceReference rr, int type) {
	if ((structListener != null) && (rr != null)) {
	    StructureChangedEvent evt = 
		new StructureChangedEvent(rr, type);
	    postEvent(evt);
	}
    }

    /**
     * post an structure change event.
     * @param type The type of the event.
     */
    protected void postStructureChangedEvent(int type) {
	if ((structListener != null) && (getResourceReference() != null)) {
	    StructureChangedEvent evt = 
		new StructureChangedEvent(getResourceReference(), type);
	    postEvent(evt);
	}
    }

    /**
     * Fire an structure change event.
     * @param type The type of the event.
     */
    protected void fireStructureChangedEvent(int type) {
	if (structListener != null) {
	    ResourceReference resref = unsafeGetResourceReference();
	    if (resref != null) {
		StructureChangedEvent evt = 
		    new StructureChangedEvent(resref, type);
		fireStructureChangedEvent(evt);
	    }
	}
    }

    /**
     * Fire an structure change event.
     * @param evt the StructureChangedEvent to fire.
     */
    protected void fireStructureChangedEvent(StructureChangedEvent evt) {
	if (structListener != null) {
	    int type = evt.getID();
	    switch (type) {
	    case Events.RESOURCE_MODIFIED :
		structListener.resourceModified(evt);
		break;
	    case Events.RESOURCE_CREATED :
		structListener.resourceCreated(evt);
		break;
	    case Events.RESOURCE_REMOVED :
		structListener.resourceRemoved(evt);
		break;
	    case Events.RESOURCE_UNLOADED :
		structListener.resourceUnloaded(evt);
		break;
	    }
	}
    }

    /**
     * This resource is being unloaded.
     * The resource is being unloaded from memory, perform any additional
     * cleanup required.
     */
    public void notifyUnload() {
	//
	// direct notification
	//
	ResourceFrame frames[] = unsafeGetFrames();
	if ( frames != null ) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( frames[i] == null )
		    continue;
		frames[i].notifyUnload();
	    }
	}
	fireStructureChangedEvent(Events.RESOURCE_UNLOADED);
	super.notifyUnload();
    }

    /**
     * Delete this Resource instance, and remove it from its store.
     * This method will erase definitely this resource, for ever, by removing
     * it from its resource store (when doable).
     * @exception MultipleLockException if someone has locked this resource.
     */
    public synchronized void delete() 
	throws MultipleLockException 
    {
	disableEvent();
	// fire and not post because we don't want this resource
	// to be locked() during the delete.
	fireStructureChangedEvent(Events.RESOURCE_REMOVED);
	ResourceFrame frames[] = getFrames();
	if ( frames != null ) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( frames[i] == null )
		    continue;
		frames[i].removeFrameEventListener(this);
		this.removeAttributeChangedListener(frames[i]);
		frames[i].unregisterResource(this);
	    }
	}
	try {
	    super.delete();
	} catch (MultipleLockException ex) {
	    enableEvent();
	    throw ex;
	}
    }

    /**
     * Mark this resource as having been modified.
     */
    public void markModified() {
	super.markModified();
	postStructureChangedEvent(Events.RESOURCE_MODIFIED);
    }

    /**
     * Set some of this resource attribute. We overide setValue to post
     * events.
     */
    public synchronized void setValue(int idx, Object value) {
	super.setValue(idx, value) ;
	if (idx != ATTR_LAST_MODIFIED) {
	    postAttributeChangeEvent(idx, value);
	    postStructureChangedEvent(Events.RESOURCE_MODIFIED);
	}
    }

    /**
     * Set a value, without posting event.
     * @param idx The attribute index, in the list of attributes advertized by 
     * the resource.
     * @param value The new value for this attribute.
     */
    public synchronized void setSilentValue(int idx, Object value) {
	disableEvent();
	super.setValue(idx, value);
	enableEvent();
    }

    /**
     * Set a value, without posting event.
     * @param name The attribute name.
     * @param value The new value for the attribute.
     */
    public synchronized void setSilentValue(String name, Object value) {
	disableEvent();
	super.setValue(name, value);
	enableEvent();
    }

    /**
     * Lookup the target resource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	ResourceFrame frames[] = getFrames();
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if (frames[i] == null)
		    continue;
		if (frames[i].lookup(ls,lr))
		    return true;
	    }
	}
	if ( ls.hasMoreComponents() ) {
	    // We are not a container resource, and we don't have children:
	    lr.setTarget(null);
	    return false;
	} else {
	    //we are done!
	    lr.setTarget(getResourceReference());
	    return true;
	}
    }

    /**
     * Perform the request on all the frames of that resource. The
     * Reply returned is the first non-null reply.
     * @param request A RequestInterface instance.
     * @return A ReplyInterface instance.
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */
    protected ReplyInterface performFrames(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	ResourceFrame frames[] = getFrames();
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if (frames[i] == null)
		    continue;
		ReplyInterface reply  = frames[i].perform(request);
		if (reply != null)
		    return reply;
	    }
	}
	return null;
    }

    /**
     * Perform the request.
     * @return a ReplyInterface instance
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */
    public ReplyInterface perform(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	return performFrames(request);
    }

    /**
     * Initialize the frames of that framed resource.
     * @param values Default attribute values.
     */

    public void initialize(Object values[]) {
	this.attrListener   = null;
	this.structListener = null;
	disableEvent();
	super.initialize(values);
	// Initialize the frames if any.
	ResourceFrame frames[] = getFrames();
	if ( frames != null ) {
	    this.framesRef = new Hashtable(Math.max(frames.length, 1));
	    Hashtable defs = new Hashtable(3);
	    for (int i = 0 ; i < frames.length ; i++) {
		if ( frames[i] == null )
		    continue;
		frames[i].registerResource(this);
		frames[i].initialize(defs);
		frames[i].addFrameEventListener(this);
		this.addAttributeChangedListener(frames[i]);
	    }
	} else {
	    this.framesRef = new Hashtable(3);
	}
	enableEvent();
    }

}
