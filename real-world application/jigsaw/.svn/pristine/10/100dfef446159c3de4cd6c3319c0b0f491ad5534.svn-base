// LabelCheckBox.java
// $Id: LabelCheckbox.java,v 1.1 2010/06/15 12:20:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.widgets;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.LayoutManager;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class LabelCheckbox extends BorderPanel implements ItemListener {

    Label     label    = null;
    Checkbox  checkbox = null;
    String    strue    = "on";
    String    sfalse   = "off";
    Dimension size     = null;

    public void itemStateChanged(ItemEvent e) {
	switch (e.getStateChange()) {
	case ItemEvent.SELECTED:
	    setState(true);
	    break;
	case ItemEvent.DESELECTED:
	    setState(false);
	    break;
	default:

	}
    }

    String getString(boolean check) {
	return (check ? strue : sfalse);
    }

    public void setState(boolean state) {
	checkbox.setState(state);
	label.setText(getString(state));
	//	remove(label);
	//	label = new Label(getString(state));
	//	add(label);
    }

    public boolean getState() {
	return checkbox.getState();
    }

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox(int type, int thickness ) {
        super(type, thickness);
	init();
    }    

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox(int type) {
        super(type);
	init();
    }

    /**
     * Create a new LabelCheckbox
     */
    public LabelCheckbox() {
        super(IN);
	init();
    }    

    public Dimension getPreferredSize() {
	return size;
    }

    public Dimension getMinimumSize() {
	return size;
    }

    public Dimension getSize() {
	return size;
    }

    private void init() {
	setLayout( new BorderLayout());
	label    = new Label(getString(true));
	checkbox = new Checkbox();
	checkbox.setState(true);
	checkbox.addItemListener(this);
	add(checkbox,"West");
	add(label,"Center");
	size = new Dimension(75,30);
    }

}
