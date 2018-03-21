// EditableStringChoice.java
// $Id: EditableStringChoice.java,v 1.1 2010/06/15 12:28:34 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.widgets;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentListener;

/**
 * An editable JComboBox.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

public class EditableStringChoice extends JPanel {

    private JComboBox  combo = null;

    public synchronized void addItemListener(ItemListener l) {
	combo.addItemListener(l);
    }

    public synchronized void removeItemListener(ItemListener l) {
	combo.removeItemListener(l);
    }

    public void addItem(Object item) {
	combo.addItem(item);
    }

    public void addItems(Object items[]) {
	for (int i = 0 ; i < items.length ; i++) {
	    if (items[i] != null)
		addItem(items[i]);
	}
    }

    public synchronized void select(String str) {
	combo.setSelectedItem(str);
    }

    public synchronized void remove(String item) {
	combo.removeItem(item);
    }

    public void removeAll() {
	combo.removeAll();
    }

    public String getText() {
	return combo.getSelectedItem().toString();
    }

    public void setRenderer(ListCellRenderer aRenderer) {
	combo.setRenderer(aRenderer);
    }

    public void setMaximumRowCount(int count) {
	combo.setMaximumRowCount(count);
    }

    public void initialize(Object items[]) {

	combo = new JComboBox(items);
	combo.setEditable(true);

	setLayout(new BorderLayout(5,5));
	add(combo, BorderLayout.CENTER);
    }

    /**
     * Get a "one line" String choice with no border.
     */
    public EditableStringChoice() {
	super();
    }

}
