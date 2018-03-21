// ResourceReference.java
// $Id: ResourceReference.java,v 1.1 2010/06/15 12:20:25 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources;

/**
 * Resolving a resource provides back a resource reference.
 * Resource <em>references</em> are the basis for handling an eventual
 * cache between the store of the resource and its memory image (ie
 * the resource instance).
 * <p>All resource spaces must provide some notion of resource reference.
 * <p>A typical access to a resource looks like:
 * <pre>
 * ResourceReference rr = space.lookup("/x/y/z");
 * try {
 *    Resource r = rr.lock();
 *    // Fiddle with the resource:
 *    r.setValue("foo", new Integer(10));
 * } catch (InvalidResourceException e) {
 *   System.out.println(e.getMessage());
 * } finally {
 *    // Make sure to unlock the reference:
 *    rr.unlock();
 * </pre>
 */

public interface ResourceReference {

  /**
   * Lock the refered resource in memory.
   * @return A real pointer to the resource.
   * @exception InvalidResourceException is thrown if the resource is
   * invalid (has been deleted or everything else).
   */
  public Resource lock()
    throws InvalidResourceException;

  /**
   * Lock the refered resource in memory.
   * NOTE, this version is NOT using a synchronized call and therefore
   * must be used with extreme caution. Basically it must be used only
   * when a backward locking is needed
   * @return A real pointer to the resource.
   * @exception InvalidResourceException is thrown if the resource is
   * invalid (has been deleted or everything else).
   */
  public Resource unsafeLock()
    throws InvalidResourceException;

  /**
   * Unlock that resource from memory.
   */
  public void unlock();

  /**
   * Is that resource reference locked ?
   * @return a boolean.
   */
  public boolean isLocked();

  /**
   * How many locks?
   * @return an int.
   */
  public int nbLock();

  /**
   * update the cached context of that reference.
   * @param ctxt the new ResourceContext.
   */
  public void updateContext(ResourceContext ctxt);

}
