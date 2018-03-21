// IPTemplateAttributeEditor.java
// $Id: IPTextField.java,v 1.1 2010/06/15 12:20:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.util.EventObject;
import java.util.StringTokenizer;

/**
 * IPTextField :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class IPTextField extends Panel implements TextEditable {

  class IntegerListener extends KeyAdapter {

    IPTextField comp = null;

    // filter the non-numeric char
    public void keyPressed(KeyEvent ke) {
      if(ke.getKeyCode() == KeyEvent.VK_DELETE ||
	 ke.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
	 ke.getKeyCode() == KeyEvent.VK_UP ||
	 ke.getKeyCode() == KeyEvent.VK_DOWN ||
	 ke.getKeyCode() == KeyEvent.VK_LEFT ||
	 ke.getKeyCode() == KeyEvent.VK_RIGHT)
	return;
      //I'm sure of that
      TextField target = (TextField)ke.getComponent();
      if(ke.getKeyChar() == '*') {
	ke.consume();
	target.setText("*");
	requestFocusOnNextField(target);
      } else  if(ke.getKeyCode() == KeyEvent.VK_SPACE) {
	ke.consume();
	requestFocusOnNextField(target);
      } else  if(ke.getKeyCode() == KeyEvent.VK_TAB ||
		 ke.getKeyCode() == KeyEvent.VK_SPACE ||
		 ke.getKeyCode() == KeyEvent.VK_ENTER) {
	requestFocusOnNextField(target);
      } else  if(!(ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')) {
	ke.consume();
      }
    }

    void requestFocusOnNextField(TextField target) {
      if (target == comp.fields[0]) 
	comp.fields[1].requestFocus();
      else  if (target == comp.fields[1]) 
	comp.fields[2].requestFocus();
      else  if (target == comp.fields[2]) 
	comp.fields[3].requestFocus();
      else  if (target == comp.fields[3]) {
	comp.fireActionEvent();
	comp.fields[0].requestFocus();
	comp.fields[0].setText("");
	comp.fields[1].setText("");
	comp.fields[2].setText("");
	comp.fields[3].setText("");
      }
    }

    IntegerListener(IPTextField comp) {
      this.comp = comp;
    }

  }

 
  class IPListener implements TextListener {

    IPTextField comp = null;

    public void textValueChanged(TextEvent e) {
      // I'm sure of that
      TextField target = (TextField)e.getSource();
      String IP = target.getText();

      if (IP.length() < 3)
	return ;
      if (IP.length() == 4) {
	target.setText(IP.substring(3));
	return;
      }
	
      try {
	short ip = Short.parseShort(IP);
	if (ip > 255) {
	  target.setText("");
	  return ;
	}
      } catch (NumberFormatException ex) {
	target.setText("");
	return ;
      }
      if (target == comp.fields[0]) 
	comp.fields[1].requestFocus();
      else  if (target == comp.fields[1]) 
	comp.fields[2].requestFocus();
      else  if (target == comp.fields[2]) 
	comp.fields[3].requestFocus();
    }

    IPListener(IPTextField comp) {
      this.comp = comp;
    }
  }

  //////// The IPTextField itself

  protected TextField [] fields = null;
  transient ActionListener actionListener;
  String command = "";

  /**
   * Adds the specified action listener to recieve action events from 
   * this IPTextField. 
   * @param al - the action listener.
   */
  public synchronized void addActionListener(ActionListener al) {
    actionListener = AWTEventMulticaster.add(actionListener, al);
  }   

  /**
   * Removes the specified action listener so that it no longer receives 
   * action events from IPTextField. 
   * @param al - the action listener.
   */
  public synchronized void removeActionListener(ActionListener al) {
    actionListener = AWTEventMulticaster.remove(actionListener, al);
  }

  /**
   * fire a new ActionEvent and process it, if some listeners are listening
   */

  protected void fireActionEvent() {
    if(actionListener != null) {
      ActionEvent ae = new ActionEvent(this,
				       ActionEvent.ACTION_PERFORMED,
				       command);
      actionListener.actionPerformed(ae);
    }
  }

  private String getFieldValue(TextField field) {
    String svalue = field.getText();
    if (svalue == null)
      return "0";
    if (svalue.length() == 0)
      return "0";
    else
      return svalue;
  }

  /**
   * Gets the text that is presented by this IPTextField.
   */
  public String getText() {
    return (getFieldValue(fields[0])+
	    "."+
	    getFieldValue(fields[1])+
	    "."+
	    getFieldValue(fields[2])+
	    "."+
	    getFieldValue(fields[3]));
  }

  /**
   * Sets the text that is presented by this IPTextField to be the specified 
   * text. 
   * @param text - the new text
   */
  public void setText(String IPT) {
    StringTokenizer st    = new StringTokenizer(IPT, ".");
    int i = 0;
    while (i < 4 && st.hasMoreTokens())
      fields[i++].setText(st.nextToken());
  }

  /**
   * Check if the current text value and the default value are different.
   */
  public boolean updated() {
    return (! getText().equals("0.0.0.0"));
  }

  /**
   * Sets the text at its default value
   */
  public void setDefault() {
    fields[0].setText("");
    fields[1].setText("");
    fields[2].setText("");
    fields[3].setText("");
  }

  public Dimension getMinimumSize() {
    return new Dimension(4 * fields[0].getMinimumSize().width,
			 fields[0].getMinimumSize().height );
  }

  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  public IPTextField() {
    IntegerListener intlistener = new IntegerListener(this);
    IPListener iplistener = new IPListener(this);

    fields = new TextField[4];
    for (int i=0; i < fields.length; i++) {
      fields[i] = new TextField(3);
      fields[i].addTextListener(iplistener);
      fields[i].addKeyListener(intlistener);
    }
    setLayout( new GridLayout(1,4) );
    add(fields[0]);
    add(fields[1]);
    add(fields[2]);
    add(fields[3]);

  }

}
