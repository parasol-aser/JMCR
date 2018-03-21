// DoubleTextField.java
// $Id: DoubleTextField.java,v 1.1 2010/06/15 12:28:33 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.widgets;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

/**
 * A TextField that accepts only num char and '.'
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DoubleTextField extends JTextField {

    protected void processComponentKeyEvent(KeyEvent ke) {
	if(ke.getKeyCode() == KeyEvent.VK_DELETE ||
	       ke.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
	       ke.getKeyCode() == KeyEvent.VK_UP ||
	       ke.getKeyCode() == KeyEvent.VK_DOWN ||
	       ke.getKeyCode() == KeyEvent.VK_LEFT ||
	       ke.getKeyCode() == KeyEvent.VK_RIGHT)
	    super.processComponentKeyEvent(ke);
	if(!(ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9') &&
	   (ke.getKeyChar() != '.') && ke.getKeyChar() != ',') {
	    ke.consume();
	} else {
	    super.processComponentKeyEvent(ke);
	}
    }

}
