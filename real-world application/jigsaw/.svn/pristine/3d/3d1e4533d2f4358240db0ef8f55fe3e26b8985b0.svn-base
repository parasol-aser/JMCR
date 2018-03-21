// FakeComboBox.java
// $Id: FakeComboBox.java,v 1.1 2010/06/15 12:20:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;

import java.awt.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * FakeComboBox :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class FakeComboBox extends Panel implements ActionListener, 
                                                   ItemListener 
{

    class GoodList extends List {

	FakeComboBox parent = null;

	int max (int a, int b) {
	    return (( a < b) ? b : a);
	}

	public Dimension getMinimumSize() {
	    return new Dimension( max (parent.text.getSize().width,
				       super.getMinimumSize().width),
				  super.getMinimumSize().height );
	}

	public Dimension getPreferredSize() {
	    return getMinimumSize();
	}

	public void add(String item) { //FIXME
	    super.addItem(item);
	}

	GoodList (FakeComboBox parent, int nb) {
	    super(nb);
	    this.parent = parent;
	}

    }

    // The FakeComboBox itself

    protected TextField text     = null;
    protected ImageButton button = null; 
    protected int listSize = 0;
    protected GoodList list = null;
    protected Panel plist = null;
    transient ActionListener actionListener;
    private String command = "";

    // ItemListener

    public void itemStateChanged(ItemEvent e) {
	if (e.getStateChange() == ItemEvent.SELECTED) {
	    Integer idx = (Integer)e.getItem();
	    String key = list.getItem(idx.intValue());
	    if (key != null) {
		setText(key);
		fireActionEvent();
	    }
	    hidePopup();
	}
    }

    /**
     * Sets the action command String used when an ActionEvent is fired
     * @param command The command String
     */

    public void setActionCommand(String command) {
	this.command = command;
    }

    /**
     * Returns the action command String
     */

    public String getActionCommand() {
	return command; 
    }

    /**
     * Adds an action listener to this FakeComboBox
     * @param al The ActionListener
     */

    public synchronized void addActionListener(ActionListener al) {
	actionListener = AWTEventMulticaster.add(actionListener, al);
    }   

    /**
     * Removes an action listener to this FakeComboBox
     * @param al The ActionListener
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

    protected void hidePopup() {
	if (plist.isShowing()) {
	    plist.setVisible(false);
	    button.switchImage();
	}
    }

    protected void showPopup() {
	if (! plist.isShowing()) {
	    plist.setVisible(true);
	    button.switchImage();
	    updateParent();
	}
    }

    void updateParent() {
	Component parent = getParent();
	if (parent != null) 
	    parent.validate();
    }

    /**
     * ActionListener
     */

    public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	if ( command.equals("popup" ) ) {
	    if (plist.isShowing())
		hidePopup();
	    else
		showPopup();
	} else  if (evt.getSource() == text) {
	    fireActionEvent();
	}
    }

    public void setText(String text) {
	this.text.setText(text);
    }

    public String getText() {
	return this.text.getText();
    }

    public void add(String item) {
	list.add(item);
    }

    public FakeComboBox(int size, 
			int listSize, 
			boolean editable,
			String imagePath1,
			String imagePath2) 
    {
	super();
	list = new GoodList(this,listSize);
	list.addItemListener(this);
	Image down = Toolkit.getDefaultToolkit().getImage(imagePath1);
	Image left = Toolkit.getDefaultToolkit().getImage(imagePath2);

	setLayout(new BorderLayout());
	text = new TextField(size);
	text.setEditable(editable);
	text.addActionListener(this);
	button = new ImageButton(down, left);
	button.addActionListener( this );
	button.setActionCommand("popup");
	add(text,"Center");
	add(button,"East");
	plist = new Panel();
	plist.add(list);
	add(plist,"South");
	plist.setVisible(true);
    }

}


