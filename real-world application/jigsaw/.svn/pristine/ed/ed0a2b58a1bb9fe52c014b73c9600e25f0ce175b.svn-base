// StringChoice.java
// $Id: StringChoice.java,v 1.1 2010/06/15 12:20:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.TextComponent;
import java.awt.TextField;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextListener;


/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

public class StringChoice extends BorderPanel {

    class IListener implements ItemListener {

	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		text.setText((String)e.getItem());
	    }
	}
	
    }

    public static final int ONE_LINE  = 1;
    public static final int TWO_LINES = 2;

    private Choice    choice = null;
    private TextField text   = null;

    public synchronized void addActionListener(ActionListener l) {
	text.addActionListener(l);
    }

    public synchronized void removeActionListener(ActionListener l) {
	text.removeActionListener(l);
    }

    public synchronized void addTextListener(TextListener l) {
	text.addTextListener(l);
    }

    public void removeTextListener(TextListener l) {
	text.removeTextListener(l);
    }

    public synchronized void addItemListener(ItemListener l) {
	choice.addItemListener(l);
    }

    public synchronized void removeItemListener(ItemListener l) {
	choice.removeItemListener(l);
    }

    public void addItem(String item) {
	choice.addItem(item);
    }

    public void addItems(String items[]) {
	for (int i = 0 ; i < items.length ; i++) {
	    if (items[i] != null)
		addItem(items[i]);
	}
    }

    public synchronized void select(String str) {
	choice.select(str);
    }

    public synchronized void remove(String item) {
	choice.remove(item);
    }

    public void removeAll() {
	choice.removeAll();
    }

    public void setText(String stext) {
	text.setText(stext);
    }

    public String getText() {
	return text.getText();
    }

    private void build(int type) {
	text = new TextField(20);
	choice = new Choice();
	choice.addItemListener( new IListener() );
	switch(type) {
	case ONE_LINE:
	    setLayout( new BorderLayout(5,5));
	    add(text,"West");
	    add(choice,"Center");
	    break;
	case TWO_LINES:
	default:
	    setLayout(new GridLayout(2,1,5,5));
	    add(choice);
	    add(text);
	    break;
	}
    }

    /**
     * Get a "one line" String choice with no border.
     */
    public StringChoice() {
	super(IN,0);
	build(ONE_LINE);
    }

    /**
     * Get a StringChoice widget.
     * @param type The posisionning (ONE_LINE, TWO_LINES)
     */
    public StringChoice(int type) {
	super(IN, 0);
	build(type);
    }

    /**
     * Get a StringChoice widget.
     * @param type The posisionning (ONE_LINE, TWO_LINES)
     * @param border The border type (SOLID, RAISED, LOWERED, IN, OUT)
     */
    public StringChoice(int type, int border) {
	super(border);
	build(type);
    }

    /**
     * Get a StringChoice widget.
     * @param type The posisionning (ONE_LINE, TWO_LINES)
     * @param border The border type (SOLID, RAISED, LOWERED, IN, OUT)
     * @param thickness The border's thickness.
     */
    public StringChoice(int type, int border, int thickness) {
	super(border, thickness);
	build(type);
    }
}
