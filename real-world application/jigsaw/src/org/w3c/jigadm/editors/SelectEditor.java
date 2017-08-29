// SelectEditor.java
// $Id: SelectEditor.java,v 1.2 2010/06/15 17:52:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.util.Hashtable;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.ClosableFrame;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * SelectEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class SelectEditor extends AttributeEditor {

    class SelectPopup extends ClosableFrame implements ItemListener {

	SelectComponent parent   = null;
	java.awt.List   list     = null; 
	EditorFeeder    feeder   = null;
	EditorModifier  modifier = null;

	// ItemListener
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		String selected = (String)
		    list.getItem(((Integer)e.getItem()).intValue());
		if (modifier != null)
		    selected = modifier.modify(selected);
		parent.setText(selected);
		setVisible(false);
	    }
	}

	protected void setDefaultItems() {
	    list.removeAll();
	    String items[] = feeder.getDefaultItems();
	    if (items != null) {
		for (int i = 0 ; i < items.length ; i++) 
		    if ( items[i] != null )
			list.addItem(items[i]);
	    }
	}

        protected void close() {
	    setVisible(false);
	}

	SelectPopup(SelectComponent parent, 
		    EditorFeeder feeder,
		    EditorModifier modifier) 
	{
	    super("Select");
	    this.parent   = parent;
	    this.feeder   = feeder;
	    this.modifier = modifier;
	    setLayout(new BorderLayout());
	    list = new java.awt.List(20);
	    list.addItemListener(this);
	    setDefaultItems();
	    add(list);
	    setSize(170,300);
	}
    }

    class SelectComponent extends Panel implements ActionListener, 
						   TextListener 
    {

	protected TextField    selected = null;
	protected SelectPopup  popup    = null;
	protected SelectEditor editor   = null;
 	EditorFeeder   feeder   = null;
	EditorModifier modifier = null;

	public void textValueChanged(TextEvent e) {
	    setModified();
	}

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		if (command.equals("edit")) {
		    if (popup == null)
			popup = new SelectPopup(this, feeder, modifier);
		    popup.show();
		}
	    }
	}

	public String getText() {
	    return selected.getText();
	}

	public void setText(String text) {
	    selected.setText(text);
	    editor.setModified();
	}

	SelectComponent (SelectEditor editor,
			 EditorFeeder feeder,
			 EditorModifier modifier,
			 String selected)
	{
	    super();
	    this.feeder = feeder;
	    this.modifier = modifier;
	    this.editor = editor;
	    this.selected = new TextField(20);
	    this.selected.setText(selected);
	    this.selected.addTextListener(this);
	    Button editB = new Button("Change");
	    editB.setActionCommand("edit");
	    editB.addActionListener(this);
	    setLayout( new BorderLayout());
	    add(this.selected,"West");
	    add(editB,"Center");
	}

    }

    // The SelectEditor itself

    /**
     * Properties - The feeder's class name.
     */
    public static final String FEEDER_CLASS_P = "feeder.class";
    public static final String MODIFIER_CLASS_P = "modifier.class";

    protected SelectComponent comp = null;
    protected boolean hasChanged = false;
    protected String oldvalue  = null;

    protected void createComponent( EditorFeeder feeder,
				    EditorModifier modifier,
				    String selected) 
    {
	if ( comp == null ) 
	    comp = new SelectComponent(this, feeder, modifier, selected);
    }

    protected void setModified() {
	hasChanged = true;
    }

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */
    public boolean hasChanged() {
	return hasChanged;
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */  
    public void clearChanged() {
	hasChanged = false;
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges() {
	hasChanged = false;
	comp.setText(oldvalue);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue() {
	try {
	    return comp.getText();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */  
    public void setValue(Object o) {
	this.oldvalue = (String) o;
	comp.setText(oldvalue);
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public Component getComponent() {
	return comp;
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper w
			   , Attribute a
			   , Object o
			   , Properties p) 
	throws RemoteAccessException
    {
	// Get the feeder class fromproperties:
	EditorFeeder   feeder      = null;
	EditorModifier modifier    = null;
	String         feederClass = null;

	feederClass = (String)p.get(FEEDER_CLASS_P);
	if ( feederClass == null )
	    throw new RuntimeException("SelectEditor mis-configuration: "+
				       FEEDER_CLASS_P +
				       " property undefined.");
	try {
	    Class c = Class.forName(feederClass);
	    feeder  = (EditorFeeder) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    feeder.initialize(w,p);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("SelectEditor mis-configured: "+
				       " unable to instantiate "+
				       feederClass +".");
	}

	String modifierClass = (String)p.get(MODIFIER_CLASS_P);
	if (modifierClass != null) {
	    try {
		Class cm = Class.forName(modifierClass);
		modifier  = (EditorModifier) cm.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new RuntimeException("SelectEditor mis-configured: "+
					   " unable to instantiate "+
					   modifierClass +".");
	    }
	}

	String selected = (String)o;
	createComponent(feeder, modifier, selected);
	if (selected != null)
	    oldvalue = new String(selected);
    }

}
