// ResourceFrame.java
// $Id: ResourceFrame.java,v 1.2 2010/06/15 17:52:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Hashtable;

import org.w3c.tools.resources.event.AttributeChangedEvent;
import org.w3c.tools.resources.event.AttributeChangedListener;
import org.w3c.tools.resources.event.Events;
import org.w3c.tools.resources.event.FrameEvent;
import org.w3c.tools.resources.event.FrameEventListener;
import org.w3c.tools.resources.event.ResourceEvent;
import org.w3c.tools.resources.event.ResourceEventMulticaster;

/**
 * The resource frame class. A ResourceFrame can be attached to a
 * resource.
 */
public class ResourceFrame extends FramedResource 
    implements AttributeChangedListener
{
    /**
     * The special class of filter.
     */
    protected static Class filterClass = null;

    /**
     * Our FrameEventListener.
     */
    protected transient FrameEventListener frameListener = null;

    /**
     * Our target resource.
     */
    protected FramedResource resource = null;

    static {
	try {
	    filterClass = 
		Class.forName("org.w3c.tools.resources.ResourceFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    throw new RuntimeException("No ResourceFilter class found.");
	}
    }

    protected boolean eventDisabled() {
	if (resource != null)
	    return (event_disabled || resource.eventDisabled());
	return event_disabled;
    }

    /**
     * Get the file part of the URL this resource is attached to.
     * @return An URL object specifying the location in the information 
     *    space of this resource.
     */
    public String getURLPath() {
	return getString(ATTR_URL, getResource().getURLPath()) ;
    }

    /**
     * Get the space entry for that resource. This Object is use to
     * retrieve the resource in the resource space.
     * A ResourceFrame has no SpaceEntry.
     * @return always null.
     */
    protected SpaceEntry getSpaceEntry() {
	return null;
    }

    private ResourceReference self = null;
    /**
     * Get The FrameReference of this frame, or <strong>null</strong>
     * if this frame is not registered.
     * @return A ResourceReference instance.
     */
    public ResourceReference getFrameReference() {
	if ((self == null) &&  (resource != null)) {
	    self = resource.getFrameReference(this);
	}
	return self;
    }

    public ResourceReference getResourceReference() {
	return getFrameReference();
    }

    /**
     * If our target resource has some children, we could have
     * some attribute to give to them.
     * @param attrs A Hashtable.
     */
    protected void updateDefaultChildAttributes(Hashtable attrs) {
	//nothing here
    }

    /**
     * Check if this kind of request can be perform by this resource.
     * @param request A RequestInterface instance
     * @return a boolean.
     */
    public boolean checkRequest(RequestInterface request) {
	return true;
    }

    /**
     * Perform the request
     * @param request the incomming request
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */ 
    public ReplyInterface perform(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	return super.perform(request);
    }

    /**
     * lookup only filters.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupFilters(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	ResourceFilter filters[] = getFilters();
	if ( filters != null ) {
	    // Mark filters, for them to be called at outgoing time:
	    lr.addFilters(filters);
	    // Some clever filter around ?
	    for (int i = 0 ; i < filters.length ; i++) {
		if ( filters[i] == null )
		    continue;
		if ( filters[i].lookup(ls, lr) )
		    return true;
	    }
	}
	return false;
    }

    /**
     * lookup frames excluding filters.
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    protected boolean lookupFrames(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	ResourceFrame frames[] = getFrames();
	if (frames != null) {
	    for (int i = 0 ; i < frames.length ; i++) {
		if ((frames[i] == null) ||
		    (frames[i] instanceof ResourceFilter))
		    continue;
		if (frames[i].lookup(ls,lr))
		    return true;
	    }
	}
	return false;
    }

    /**
     * Lookup the target resource.
     * @param ls The current lookup state
     * @param lr The result
     * @exception ProtocolException If an error relative to the protocol occurs
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
	{
	    if (lookupFrames(ls,lr))
		return true;

	    if ( ls.hasMoreComponents() ) {
		// We are not a container resource, and we don't have children:
		lr.setTarget(null);
		return false;
	    } else {
		// We are done !
		lr.setTarget(resource.getResourceReference());
		return true;
	    }
	}

    public void processEvent(ResourceEvent evt) {
	if (evt instanceof FrameEvent) {
	    fireFrameEvent((FrameEvent)evt);
	} else if (evt instanceof AttributeChangedEvent) {
	    fireAttributeChangeEvent((AttributeChangedEvent)evt);
	}
    }

    /**
     * Add a frame event listener.
     * @param l The new frame event listener.
     */

    public void addFrameEventListener(FrameEventListener l) {
	frameListener = ResourceEventMulticaster.add(frameListener, l);
    }

    /**
     * Remove a frame event listener.
     * @param l The listener to remove.
     */

    public void removeFrameEventListener (FrameEventListener l) {
	frameListener = ResourceEventMulticaster.remove(frameListener, l);
    }

    /**
     * Post a frameEvent.
     * @param the frame event type.
     */
    protected void postFrameEvent(int type) {
	if (frameListener != null) {
	    FrameEvent evt = new FrameEvent(this, type);
	    postEvent(evt);
	}
    }

    /**
     * Fire a frameEvent.
     * @param the frame event type.
     */
    protected void fireFrameEvent(FrameEvent evt) {
	if (frameListener != null) {
	    int type = evt.getID();
	    switch (type) {
	    case Events.FRAME_ADDED :
		frameListener.frameAdded(evt);
		break;
	    case Events.FRAME_MODIFIED :
		frameListener.frameModified(evt);
		break;
	    case Events.FRAME_REMOVED :
		frameListener.frameRemoved(evt);
		break;
	    }
	}
    }

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
	if (debugEvent)
	    displayEvent( this, evt );
	if (! isUnloaded())
	    setValue(ATTR_LAST_MODIFIED, new Long(System.currentTimeMillis()));
    }

    /**
     * This handles the <code>FRAME_MODIFIED</code> kind of events.
     * @param evt The event describing the change.
     */

    public void frameModified(FrameEvent evt) {
	if (debugEvent)
	    displayEvent( this, evt );
	if (! isUnloaded()) {
	    markModified();
	    postFrameEvent(evt.getID());
	}
    }

    /**
     * We overide setValue, to fire event.
     * @param idx The index of the attribute to modify.
     * @param value The new attribute value.
     */
    public synchronized void setValue(int idx, Object value) {
	super.setValue(idx,value);
	if (idx != ATTR_LAST_MODIFIED)
	    postFrameEvent(Events.FRAME_MODIFIED); 
    }

    /**
     * Get the target resource.
     * @return a resource instance.
     */
    public FramedResource getResource() {
	return resource;
    }

    /**
     * Register a target resource. Called after initialize,
     * set the context. getServer() can be call only after
     * this method call.
     * @parame resource The resource to register.
     */ 
    public void registerResource(FramedResource resource) {
	this.resource = resource;
	postFrameEvent(Events.FRAME_ADDED);
	setValue(ATTR_CONTEXT, resource.getContext());
    }

    /**
     * Register a target resource.
     * @parame resource The resource to register.
     */ 
    public void unregisterResource(Resource resource) {
	//FIXME (can we have more than one resource? )
	this.resource = null;
	postFrameEvent(Events.FRAME_REMOVED);
    }

    //
    // Filtered part
    //

    /**
     * Get our whole list of filters.
     */

    public synchronized ResourceFilter[] getFilters() {
	ResourceFrame frames[] = collectFrames(filterClass);
	if ( frames != null ) {
	    // FIXME Normally a simple cast should suffice (?)
	    ResourceFilter f[] = new ResourceFilter[frames.length];
	    for (int i = 0 ; i < frames.length ; i++)
		f[i] = (ResourceFilter) frames[i];
	    return f;
	}
	return null; 
    }

    /**
     * Get the list of filters of this class.
     * @param cls The class of filters requested.
     * @return An array of filters, which are instances of the given class.
     */

    public synchronized ResourceFilter[] getFilters(Class cls) {
	ResourceFrame frames[] = collectFrames(cls);
	if ( frames != null ) {
	    // FIXME Normally a simple cast should suffice (?)
	    ResourceFilter f[] = new ResourceFilter[frames.length];
	    for (int i = 0 ; i < frames.length ; i++)
		f[i] = (ResourceFilter) frames[i];
	    return f;
	}
	return null;
    }

}
