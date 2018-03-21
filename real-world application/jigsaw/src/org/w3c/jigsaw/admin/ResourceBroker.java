// ResourceBroker.java
// $Id: ResourceBroker.java,v 1.1 2010/06/15 12:24:26 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.admin;

import java.util.Hashtable;

import org.w3c.jigsaw.daemon.ServerHandlerManager;

import org.w3c.tools.resources.FramedResource;

/**
 * The server side resource broker.
 */

public class ResourceBroker extends FramedResource {

  public String getIdentifier() {
    return "ResourceBroker";
  }

  /**
   * A real funny way to create resources..
   * @param shm The server handler manager instance to administer.
   * @param server The AdminServer instance.
   * @param writer The encoder for the Admin protocol.
   */

  public ResourceBroker(ServerHandlerManager shm,
			AdminServer admin,
			AdminWriter writer) 
  {
    super();
    Hashtable defs = new Hashtable(3);
    defs.put("identifier", "broker-frame");
    registerFrame( new BrokerFrame(shm, admin, writer), defs);
  }
	
}
