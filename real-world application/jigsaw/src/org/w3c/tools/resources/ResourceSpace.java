// ResourceSpace.java
// $Id: ResourceSpace.java,v 1.1 2010/06/15 12:20:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.event.ResourceEventQueue;

public interface ResourceSpace {

  public ResourceEventQueue getEventQueue();

  /**
   * Shutdown this resource space.
   * Go through all entries, and shut them down.
   */
  public void shutdown();

  /**
   * Checkpoint all modified resource, by saving them to disk.
   */
  public void checkpoint();

  /**
   * Restore the resource whose name is given from the root.
   * @param identifier The identifier of the resource to restore.
   * @param defs Default attribute values.
   */ 
  public ResourceReference loadRootResource(String identifier,
					    Hashtable defs);

  /**
   * Lookup this resource.
   * @param sentry The resource space entry.
   * @param identifier The resource identifier.
   * @return A Resource instance, or <strong>null</strong> if either the
   *    resource doesn't exist, or it isn't loaded yet.
   */
  public ResourceReference lookupResource(SpaceEntry sentry, 
					  String identifier);

  /**
   * Restore the resource whose name is given.
   * @param sentry The resource space entry.
   * @param identifier The identifier of the resource to restore.
   * @param defs Default attribute values.
   */
  public ResourceReference loadResource(SpaceEntry sentry, 
					String identifier,
					Hashtable defs);

  /**
   * Add this resource to the space.
   * @param sentry The resource space entry.
   * @param resource The resource to add.
   * @param defs Default attribute values.
   */
  public ResourceReference addResource(SpaceEntry sentry,
				       Resource resource,
				       Hashtable defs);

  /**
   * Save this resource to the space.
   * @param sentry The resource space entry.
   * @param resource The resource to save.
   */
  public void saveResource(SpaceEntry sentry,
			   Resource resource);

  /**
   * Mark the given resource as being modified.
   * @param sentry The resource space entry.
   * @param resource The resource to mark as modified.
   */
  public void markModified(SpaceEntry sentry,
			   Resource resource);

  /**
   * Rename a resource in this resource space.
   * @param sentry The resource space entry.
   * @param oldid The old resorce identifier.
   * @param newid The new resorce identifier.
   */
  public void renameResource(SpaceEntry sentry,
			     String oldid,
			     String newid);

  /**
   * Delete this resource from the space.
   * @param sentry The resource space entry.
   * @param resource The resource to delete.
   */
  public void deleteResource(SpaceEntry sentry,
			     Resource resource);

  /**
   * Delete all the children of resource indentified by its
   * space entry.
   * @param sentry The resource space entry
   */
  public void deleteChildren(SpaceEntry sentry);

  /**
   * Save all the children of the resource indentified by its
   * spaec entry.
   * @param sentry The resource space entry
   */  
  public void saveChildren(SpaceEntry sentry);

  /**
   * Acquire the children of the resource.
   * @param sentry The resource space entry.
   */
  public void acquireChildren(SpaceEntry sentry);

  /**
   * acquire children from an external file.
   * @param sentry The resource space entry.
   * @param repository The file used to store children.
   */
  public void acquireChildren(SpaceEntry sentry,
			      File repository,
			      boolean transientFlag);

  /**
   * Enumerate the name (ie identifiers) of the resource children
   * identified by its space entry.
   * @param sentry The space entry.
   * @return An enumeration, providing one element per child, which is
   * the name of the child, as a String.
   */
  public 
  Enumeration enumerateResourceIdentifiers(SpaceEntry sentry);

}
