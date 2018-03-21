// RemoteResource.java
// $Id: RemoteResource.java,v 1.1 2010/06/15 12:24:28 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.net.URL;

import org.w3c.tools.resources.serialization.AttributeDescription;

/**
 * The client side view of a server-side resource.
 * The whole servers state are exported through resources, which allows
 * the administration application to discover and query it using a 
 * homogeneous interface.
 * All methods will throw a <code>RemoteAccessException</code> in case of
 * network failure.
 * @see ResourceBroker
 */

public interface RemoteResource {

    /**
     * Get the target resource class hierarchy.
     * This method will return the class hierarchy as an array of String. The
     * first string in the array is the name of the resource class itself, the
     * last string will always be <em>java.lang.Object</em>.
     * @return A String array givimg the target resource's class description.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public String[] getClassHierarchy()
	throws RemoteAccessException;

    /**
     * Delete that resource, and detach it from its container.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public void delete()
	throws RemoteAccessException;

    /**
     * Reindex the resource's children if this resource is a DirectoryResource.
     * @param rec recursivly?
     * @exception RemoteAccessException If it's not a DirectoryResource
     */
    public void reindex(boolean rec)
	throws RemoteAccessException;

    /**
     * Get the target resource list of attributes.
     * This method returns the target resource attributes description. The
     * resulting array contains instances of the Attribute class, one item
     * per described attributes.
     * <p>Even though this returns all the attribute resources, only the
     * ones that are advertized as being editable can be set through this
     * interface.
     * @return An array of Attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public AttributeDescription[] getAttributes()
	throws RemoteAccessException;

    /**
     * @param name The attribute whose value is to be fetched, encoded as
     * its name.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public Object getValue(String attr)
	throws RemoteAccessException;

    /**
     * @param attrs The (ordered) set of attributes whose value is to be
     * fetched.
     * @return An (ordered) set of values, one per queried attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public Object[] getValues(String attrs[])
	throws RemoteAccessException;

    /**
     * @param attr The attribute to set, encoded as it's name.
     * @param value The new value for that attribute.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public void setValue(String attr, Object value)
	throws RemoteAccessException;

    /**
     * Set a set of attribute values in one shot.
     * This method guarantees that either all setting is done, or none of
     * them are.
     * @param attrs The (ordered) list of attribute to set, encoded as their
     * names.
     * @param values The (ordered) list of values, for each of the above
     * attributes.
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public void setValues(String attrs[], Object values[])
	throws RemoteAccessException;

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public boolean isContainer()
	throws RemoteAccessException;

    public boolean isDirectoryResource()
	throws RemoteAccessException;

    public boolean isIndexersCatalog()
	throws RemoteAccessException;

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public String[] enumerateResourceIdentifiers()
	throws RemoteAccessException;

    /**
     * @exception RemoteAccessException If somenetwork failure occured.
     */

    public RemoteResource loadResource(String identifier)
	throws RemoteAccessException;

    /**
     * Register a new resource within this container.
     * @param id The identifier of the resource to be created.
     * @param classname The name of the class of the resource to be added.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public RemoteResource registerResource(String id, String classname) 
	throws RemoteAccessException;

    /**
     * Is this resource a framed resource ?
     * @return A boolean, <strong>true</strong> if the resource is framed
     * and it currently has some frames attached, <strong>false</strong>
     * otherwise.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public boolean isFramed() 
	throws RemoteAccessException;

    public boolean isFrame();

    /**
     * Get the frames attached to that resource.
     * Each frame is itself a resource, so it is returned as an instance of
     * a remote resource.
     * @return A (posssibly <strong>null</strong>) array of frames attached
     * to that resource.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public RemoteResource[] getFrames()
	throws RemoteAccessException;

    /**
     * Unregister a given frame from that resource.
     * @param filter The frame to unregister.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void unregisterFrame(RemoteResource frame)
	throws RemoteAccessException;

    /**
     * Attach a new frame to that resource.
     * @param identifier The name for this frame (if any).
     * @param clsname The name of the frame's class.
     * @return A remote handle to the (remotely) created frame instance.
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public RemoteResource registerFrame(String identifier, String classname)
	throws RemoteAccessException;

    /**
     * Is this resource a filtered resource ?
     * @return A boolean, <strong>true</strong> if the resource is filtered
     * and it currently has some filters attached, <strong>false</strong>
     * otherwise.
     */

    public void updateURL(URL parentURL);

}
