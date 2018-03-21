// StringChoice.java
// $Id: StringChoice.java,v 1.1 2010/06/15 12:28:32 smhuang Exp $
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
import javax.swing.event.DocumentListener;

/**
 * String choice widget.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class StringChoice extends JPanel {

    private JComboBox  combo = null;
    private JTextField text   = null;

    ItemListener il = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		setTextInternal((String)e.getItem());
	    }
	} 
    };

    public synchronized void addActionListener(ActionListener l) {
	text.addActionListener(l);
    }

    public synchronized void removeActionListener(ActionListener l) {
	text.removeActionListener(l);
    }

    public synchronized void addDocumentListener(DocumentListener l) {
	text.getDocument().addDocumentListener(l);
    }

    public void removeDocumentListener(DocumentListener l) {
	text.getDocument().removeDocumentListener(l);
    }

    public synchronized void addItemListener(ItemListener l) {
	combo.addItemListener(l);
    }

    public synchronized void removeItemListener(ItemListener l) {
	combo.removeItemListener(l);
    }

    public void addItem(String item) {
	combo.addItem(item);
    }

    public void addItems(String items[]) {
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

    public void setText(String stext) {
	text.setText(stext);
    }

    public String getText() {
	return text.getText();
    }

    protected void setTextInternal(String stext) {
	text.setText(stext);
    }

    public void initialize(String items[]) {
	text = new JTextField(10);
	text.setBorder(BorderFactory.createLoweredBevelBorder());

	combo = new JComboBox(items);
	combo.addItemListener(il);

	setLayout(new BorderLayout(5,5));

	add(text, BorderLayout.WEST);
	add(combo, BorderLayout.CENTER);
    }

    /**
     * Get a "one line" String choice with no border.
     */
    public StringChoice() {
	super();
    }

}
