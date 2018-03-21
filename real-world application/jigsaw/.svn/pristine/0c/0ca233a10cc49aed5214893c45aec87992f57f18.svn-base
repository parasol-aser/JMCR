// MessagePopup.java
// $Id: MessagePopup.java,v 1.1 2010/06/15 12:20:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A very simple popup displaying a message.
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class MessagePopup implements ActionListener {

    Frame frame    = null;
    Label msg      = null;
    Button okB     = null;

    public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() == okB)
	    frame.dispose();
    }

    /**
     * show the popup.
     */
    public void show() {
	frame.show();
    }

    /**
     * build a Message popup.
     * @param message, The message to display
     */
    public MessagePopup(String message) {
	this("",message);
    }

    /**
     * build a Message popup.
     * @param title, the title.
     * @param message, The message to display
     */
    public MessagePopup(String title, String message) {
	msg   = new Label(message);
	BorderPanel pmsg = new BorderPanel(BorderPanel.IN, 2);
	pmsg.setLayout(new FlowLayout());
	pmsg.add(msg);
	okB   = new Button("Ok");
	okB.addActionListener(this);
	frame = new Frame(title);
	frame.setBackground(Color.lightGray);
	frame.setLayout( new BorderLayout());
	frame.add(pmsg,"North");
	Panel p = new Panel();
	p.add(okB);
	frame.add(p,"South");
	frame.pack();
    }

}
