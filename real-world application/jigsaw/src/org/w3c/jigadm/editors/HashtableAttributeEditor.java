// HashtableAttributeEditor.java
// $Id: HashtableAttributeEditor.java,v 1.1 2010/06/15 12:22:40 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;

import java.awt.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Enumeration;
import java.util.EventObject;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.tools.widgets.ClosableFrame;
import org.w3c.tools.widgets.ListEditor;

import org.w3c.util.ArrayDictionary;

/**
 * HashtableAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class HashtableAttributeEditor extends AttributeEditor {

    class HashtableAttributePopup extends ClosableFrame 
	implements ActionListener,
	           ItemListener
    {

	protected HashtableAttributeComponent parent = null;
	protected ArrayDictionary table                    = null;
	protected List keys                          = null;
	protected TextField tkey                     = null;
	protected TextField tvalue                   = null;
	protected boolean modified                   = false;
	protected String selectedKey                 = null;

	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		Integer idx = (Integer)e.getItem();
		String key = keys.getItem(idx.intValue());
		if (key != null) {
		    String value = (String)table.get(key);
		    tkey.setText(key);
		    selectedKey = key;
		    tvalue.setText(value);
		    tkey.requestFocus();
		}
	    }
	}

	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    if ( command.equals("add" ) ) {
		String key = tkey.getText();
		String value = tvalue.getText();
		if (key.length() > 0) {
		    table.put(key,value);
		    updateKeys();
		    modified = true;
		}
	    } else  if ( command.equals("replace" ) ) {
		if (selectedKey != null)
		    table.remove(selectedKey);
		String key = tkey.getText();
		String value = tvalue.getText();
		if (key.length() > 0) {
		    table.put(key,value);
		    updateKeys();
		    modified = true;
		}
	    } else  if( command.equals("del" ) ) {
		int isels[] = keys.getSelectedIndexes();
		if ((isels != null) && (isels.length > 0)) {
		    modified = true;
		    for (int i = 0 ; i < isels.length ; i++) {
			String key = keys.getItem(isels[i]);
			table.remove(key);
			keys.deselect(isels[i]);
		    }
		    for (int i = 0 ; i < isels.length ; i++)
			keys.delItem(isels[i]-i);
		}
		tkey.setText("");
		tvalue.setText("");
	    } else  if( command.equals("update" ) ) {
		if (modified) {
		    parent.setTable(table);
		    parent.setModified();
		    modified = false;
		}
		tkey.setText("");
		tvalue.setText("");      
		setVisible(false);
	    } else  if( command.equals("cancel" ) ) {
		close();
	    } else  if( evt.getSource() == tkey ) {
		tvalue.requestFocus();
	    } else  if( evt.getSource() == tvalue ) {
		String key = tkey.getText();
		String value = tvalue.getText();
		if (key.length() > 0) {
		    table.put(key,value);
		    updateKeys();
		    modified = true;
		}
		tkey.requestFocus();
	    }
	}

	protected void close() {
	    modified = false;
	    tkey.setText("");
	    tvalue.setText("");      
	    setVisible(false);
	}

	protected void updateKeys() {
	    tkey.setText("");
	    tvalue.setText("");
	    keys.removeAll();
	    Enumeration e = table.keys();
	    while (e.hasMoreElements())
		keys.addItem((String)e.nextElement());
	}

	protected void updateSize() {
	    setSize(350,230);
	}

	public void start(ArrayDictionary table) {
	    this.table  = table;
	    updateKeys();
	    selectedKey = null;
	    modified = false;
	}

	HashtableAttributePopup(HashtableAttributeComponent parent,
				ArrayDictionary table,
				String title) 
	{
	    super(title);
	    this.parent = parent;
	    this.table  = table;

	    GridBagLayout layout = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.NONE;
	    c.insets = new Insets(5,5,5,5);
	    setLayout(layout);

	    Label lkey = new Label("Key");
	    Label lvalue = new Label("Value");

	    tkey = new TextField(15);
	    tkey.addActionListener(this);
	    tvalue = new TextField(15);
	    tvalue.addActionListener(this);

	    keys = new List(5,true);
	    keys.addItemListener(this);

	    Button addB = new Button("add");
	    addB.setActionCommand("add");
	    addB.addActionListener(this);

	    Button removeB = new Button("Remove");
	    removeB.setActionCommand("del");
	    removeB.addActionListener(this);

	    Button replaceB = new Button("Replace");
	    replaceB.setActionCommand("replace");
	    replaceB.addActionListener(this);

	    Button okB = new Button("Ok");
	    okB.setActionCommand("update");
	    okB.addActionListener(this);

	    Button cancelB = new Button("Cancel");
	    cancelB.setActionCommand("cancel");
	    cancelB.addActionListener(this);

	    c.fill = GridBagConstraints.NONE;
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    c.anchor = GridBagConstraints.CENTER;
	    layout.setConstraints(lkey,c);
	    add(lkey);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.anchor = GridBagConstraints.CENTER;
	    layout.setConstraints(lvalue,c);
	    add(lvalue);

	    c.fill = GridBagConstraints.BOTH;
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    layout.setConstraints(tkey,c);
	    add(tkey);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(tvalue,c);
	    add(tvalue);

	    c.gridwidth = GridBagConstraints.RELATIVE;
	    //    c.gridheight = 2;
	    layout.setConstraints(keys,c);
	    add(keys);

	    c.fill = GridBagConstraints.NONE;

	    Panel control = new Panel( new BorderLayout());
	    control.add(addB, "North");
	    control.add(replaceB, "Center");
	    control.add(removeB,"South");
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(control,c);
	    add(control);

	    c.fill = GridBagConstraints.BOTH;

	    c.gridwidth = GridBagConstraints.RELATIVE;
	    layout.setConstraints(okB,c);
	    add(okB);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(cancelB,c);
	    add(cancelB);

	    updateSize();
	    updateKeys();
	}

    }

    class HashtableAttributeComponent extends ListEditor {

	protected HashtableAttributeEditor editor = null;
	protected HashtableAttributePopup  popup  = null;
	protected ArrayDictionary hashtable             = null;

	protected void edit() {
	    if (popup == null)   
		popup = new HashtableAttributePopup(this,
					    (ArrayDictionary)hashtable.clone(),
					    "Edit");
	    else 
		popup.start((ArrayDictionary)hashtable.clone());
	    popup.show();
	    popup.toFront();
	}

	protected void setModified() {
	    editor.setModified();
	}

	protected void setTable(ArrayDictionary table) {
	    if (table == null) {
		hashtable = new ArrayDictionary(5);
		return;
	    }
	    hashtable = table;
	    Enumeration keys = table.keys();
	    list.removeAll();
	    while (keys.hasMoreElements())
		list.addItem((String)keys.nextElement());
	}

	protected ArrayDictionary getTable() {
	    return hashtable;
	} 

	HashtableAttributeComponent(HashtableAttributeEditor editor,
				    ArrayDictionary table) 
	{
	    super(3,false);
	    this.editor = editor;
	    setTable(table);
	} 
			      

    }

    // The HashtableAttributeEditor itself

    protected boolean hasChanged               = false;
    protected HashtableAttributeComponent comp = null;
    protected ArrayDictionary oldValue               = null;

    /**
     * get the Component created by the editor.
     * @return a Component
     */  
    public Component getComponent() {
	return comp;
    }

    protected void createComponent(ArrayDictionary table) {
	if (comp == null)
	    comp = new HashtableAttributeComponent(this,table);
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
	comp.setTable(oldValue);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue() {
	return comp.getTable();
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o) {
	this.oldValue = (ArrayDictionary)o;
	comp.setTable(oldValue);
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */  
    public void initialize(RemoteResourceWrapper w,
			   Attribute a,
			   Object o,
			   Properties p) 
	throws RemoteAccessException
    {
	createComponent((ArrayDictionary)o);
	oldValue = (ArrayDictionary)o;
    }

    public HashtableAttributeEditor() {
	super();
    }

}
