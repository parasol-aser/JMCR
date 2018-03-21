// TextEditable.java
// $Id: TextEditable.java,v 1.1 2010/06/15 12:20:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.event.ActionListener;

/**
 * Editable interface
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public interface TextEditable {

  /**
   * Sets the text that is presented by this interface to be the specified 
   * text. 
   * @param text - the new text
   */
  public void setText(String text);

  /**
   * Gets the text that is presented by this interface.
   */
  public String getText();

  /**
   * Check if the current text value and the default value are different.
   */
  public boolean updated();

  /**
   * Sets the text at its default value
   */
  public void setDefault();

  /**
   * Adds the specified action listener to recieve action events from 
   * this interface. 
   * @param al - the action listener.
   */
  public void addActionListener(ActionListener al);

  /**
   * Removes the specified action listener so that it no longer receives 
   * action events from interface. 
   * @param al - the action listener.
   */
  public void removeActionListener(ActionListener al);

}
