// StringChoiceEditor.java
// $Id: StringChoiceEditor.java,v 1.2 2010/06/15 17:52:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes;

import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.TextListener;
import java.awt.event.TextEvent;
import java.awt.event.ItemEvent;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;
import org.w3c.jigadm.editors.EditorFeeder;
import org.w3c.jigadm.editors.EditorModifier;

import org.w3c.jigadmin.widgets.StringChoice;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

/**
 * An editor for StringChoice attributes.  
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class StringChoiceEditor extends AttributeEditor {

    class StringChoiceComponent extends StringChoice 
	implements DocumentListener
    {

	EditorFeeder       feeder   = null;
	EditorModifier     modifier = null;
	StringChoiceEditor editor   = null;

	//DocumentListener
	public void insertUpdate(DocumentEvent e) {
	    editor.setModified();
	}

	//DocumentListener
	public void changedUpdate(DocumentEvent e) {
	    editor.setModified();
	}

	//DocumentListener
	public void removeUpdate(DocumentEvent e) {
	    editor.setModified();
	}

	protected void setTextInternal(String stext) {
	    if (modifier != null)
		setText(modifier.modify(stext));
	    else
		setText(stext);
	}

	StringChoiceComponent(StringChoiceEditor editor,
			      String selected,
			      EditorFeeder feeder,
			      EditorModifier modifier) 
	{
	    super();
	    this.editor = editor;
	    this.feeder = feeder;
	    this.modifier = modifier;
	    String items[] = feeder.getDefaultItems();
	    super.initialize(items);
	    setText(selected);
	    addDocumentListener(this);
	}
    }

    /**
     * Properties - The feeder's class name.
     */
    public static final String FEEDER_CLASS_P = "feeder.class";
    public static final String MODIFIER_CLASS_P = "modifier.class";

    protected boolean hasChanged = false;
    protected String oldvalue    = null;
    protected StringChoiceComponent comp = null;

    protected Dimension getPopupSize() {
	return new Dimension(400,160);
    }

    protected void createComponent(EditorFeeder feeder,
				   EditorModifier modifier,
				   String selected) 
    {
	if ( comp == null ) 
	    comp = new StringChoiceComponent(this, selected, feeder, modifier);
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
	String ct = comp.getText();
	if ((ct != null) && (ct.length() > 0)) {
	    return ct;
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
	EditorFeeder   feeder        = null;
	EditorModifier modifier      = null;
	String         feederClass   = null;
	String         modifierClass = null;

	feederClass = (String)p.get(FEEDER_CLASS_P);
	if ( feederClass == null )
	    throw new RuntimeException("StringChoiceEditor mis-configuration:"+
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
	    throw new RuntimeException("StringChoiceEditor mis-configured: "+
				       " unable to instantiate "+
				       feederClass +".");
	}

	modifierClass = (String)p.get(MODIFIER_CLASS_P);
	if (modifierClass != null) {
	    try {
		Class cm = Class.forName(modifierClass);
		modifier = (EditorModifier) cm.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new RuntimeException("SelectEditor mis-configured: "+
					   " unable to instantiate "+
					   modifierClass +".");
	    }
	}
	createComponent(feeder, modifier, (String) o);
	oldvalue = (String) o;
    }

    public StringChoiceEditor() {
	super();
    }
}
