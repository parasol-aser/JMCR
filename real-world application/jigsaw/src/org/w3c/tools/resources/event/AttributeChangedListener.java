// AttributeChangedListener.java
// $Id: AttributeChangedListener.java,v 1.1 2010/06/15 12:26:41 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.event;

import java.util.EventListener;

public interface AttributeChangedListener extends EventListener {

  /**
   * Gets called when a property changes.
   * @param evt The AttributeChangeEvent describing the change.
   */

  public void attributeChanged(AttributeChangedEvent evt);

}
