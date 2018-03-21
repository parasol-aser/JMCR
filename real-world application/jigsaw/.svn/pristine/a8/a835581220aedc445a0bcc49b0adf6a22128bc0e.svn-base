// ContainerInterface.java
// $Id: ContainerInterface.java,v 1.1 2010/06/15 12:20:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.util.Enumeration;
import java.util.Hashtable;

public interface ContainerInterface {

  /**
   * Enumerate children resource identifiers.
   * @param all Should all resources be enumerated ? Resources are often
   * created on demand only, this flag allows the caller to tell the 
   * container about wether it is interested only in already created
   * resources, or in all resources (even the one that have not yet been
   * created).
   * @return An String enumeration, one element per child.
   */

  public Enumeration enumerateResourceIdentifiers(boolean all);

  /**
   * Lookup a children in the container.
   * @param name The name of the children to lookup.
   * the resource from its store.
   */

  public ResourceReference lookup(String name);

  /**
   * Remove a child resource from that container.
   * @param name The name of the child to remove.
   * @exception MultipleLockException If somone else has locked the 
   * resource.
   */

  public void delete(String name)
    throws MultipleLockException;

 

  /**
   * Initialize and register the given resource within that container.
   * @param name The identifier for the resource.
   * @param resource An unitialized resource instance.
   * @param defs A default set of init attribute values (may be
   * <strong>null</strong>).
   * @exception InvalidResourceException If an error occurs during the
   * registration.
   */

  public void registerResource(String name,
			       Resource resource,
			       Hashtable defs)
    throws InvalidResourceException;

}
