// HashtableAttributeEditor.java
// $Id: HashtableAttributeEditor.java,v 1.1 2010/06/15 12:20:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

import org.w3c.jigadmin.widgets.ClosableDialog;
import org.w3c.jigadmin.widgets.ListEditor;

import org.w3c.util.ArrayDictionary;

/**
 * HashtableAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class HashtableAttributeEditor extends AttributeEditor {

    class HashtableAttributePopup extends ClosableDialog {	

	protected HashtableAttributeComponent parent      = null;
	protected ArrayDictionary             table       = null;
	protected Vector                      listdata    = null;
	protected JList                       keys        = null;
	protected JTextField                  tkey        = null;
	protected JTextField                  tvalue      = null;
	protected boolean                     modified    = false;
	protected String                      selectedKey = null;
	

	ListSelectionListener lsl = new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if (! e.getValueIsAdjusting()) {
		    String key = (String)keys.getSelectedValue();
		    if (key != null) {
			String value = (String)table.get(key);
			tkey.setText(key);
			selectedKey = key;
			tvalue.setText(value);
			tkey.requestFocus();
		    }
		}
	    }
	};

	ActionListener al = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if ( command.equals("add" ) ) {
		    String key = tkey.getText();
		    String value = tvalue.getText();
		    if (key.length() > 0) {
			table.put(key,value);
			listdata.addElement(key);
			updateKeys();
			modified = true;
		    }
		} else if ( command.equals("replace" ) ) {
		    if (selectedKey != null) {
		        table.remove(selectedKey);
			listdata.removeElement(selectedKey);
		    }
		    String key = tkey.getText();
		    String value = tvalue.getText();
		    if (key.length() > 0) {
			table.put(key,value);
			listdata.addElement(key);
			updateKeys();
			modified = true;
		    }
		} else if( command.equals("del" ) ) {
		    Object sels[] = keys.getSelectedValues();
		    for (int i = 0 ; i < sels.length ; i++) {
			table.remove((String)sels[i]);
			listdata.removeElement((String)sels[i]);
		    }
		    updateKeys();
		    modified = true;
		} else if( command.equals("update" ) ) {
		    if (modified) {
			parent.setTable(table);
			parent.setModified();
			modified = false;
		    }
		    tkey.setText("");
		    tvalue.setText("");      
		    close();
		} else if( command.equals("cancel" ) ) {
		    close();
		} else if( evt.getSource() == tkey ) {
		    tvalue.requestFocus();
		} else if( evt.getSource() == tvalue ) {
		    String key = tkey.getText();
		    String value = tvalue.getText();
		    if (key.length() > 0) {
			table.put(key,value);
			listdata.addElement(key);
			updateKeys();
			modified = true;
		    }
		    tkey.requestFocus();
		}
	    }
	};

	protected void close() {
	    modified = false;
	    tkey.setText("");
	    tvalue.setText("");      
	    setVisible(false);
	    dispose();
	}

	protected void updateKeys() {
	    tkey.setText("");
	    tvalue.setText("");
	    keys.setListData(listdata);
	}

	protected void updateSize() {
	    setSize(350,230);
	}

	HashtableAttributePopup(HashtableAttributeComponent parent,
				ArrayDictionary table,
				Vector listdata,
				String title) 
	{
	    super(HashtableAttributeEditor.this.frame, title, false);
	    this.parent   = parent;
	    this.table    = table;
	    this.listdata = listdata;

	    Container cont = getContentPane();

	    GridBagLayout layout = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.NONE;
	    c.insets = new Insets(5,5,5,5);
	    cont.setLayout(layout);

	    JLabel lkey = new JLabel("Key");
	    JLabel lvalue = new JLabel("Value");

	    tkey = new JTextField(15);
	    tkey.addActionListener(al);
	    tkey.setBorder(BorderFactory.createLoweredBevelBorder());
	    tvalue = new JTextField(15);
	    tvalue.addActionListener(al);
	    tvalue.setBorder(BorderFactory.createLoweredBevelBorder());

	    keys = new JList();
	    keys.addListSelectionListener(lsl);
	    keys.setBorder(BorderFactory.createLoweredBevelBorder());

	    JButton addB = new JButton("add");
	    addB.setActionCommand("add");
	    addB.addActionListener(al);

	    JButton removeB = new JButton("Remove");
	    removeB.setActionCommand("del");
	    removeB.addActionListener(al);

	    JButton replaceB = new JButton("Replace");
	    replaceB.setActionCommand("replace");
	    replaceB.addActionListener(al);

	    JButton okB = new JButton("Ok");
	    okB.setActionCommand("update");
	    okB.addActionListener(al);

	    JButton cancelB = new JButton("Cancel");
	    cancelB.setActionCommand("cancel");
	    cancelB.addActionListener(al);

	    c.fill = GridBagConstraints.NONE;
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    c.anchor = GridBagConstraints.CENTER;
	    layout.setConstraints(lkey,c);
	    cont.add(lkey);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.anchor = GridBagConstraints.CENTER;
	    layout.setConstraints(lvalue,c);
	    cont.add(lvalue);

	    c.fill = GridBagConstraints.BOTH;
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    layout.setConstraints(tkey,c);
	    cont.add(tkey);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(tvalue,c);
	    cont.add(tvalue);

	    c.gridwidth = GridBagConstraints.RELATIVE;
	    //    c.gridheight = 2;
	    layout.setConstraints(keys,c);
	    cont.add(keys);

	    c.fill = GridBagConstraints.NONE;

	    JPanel control = new JPanel( new BorderLayout());
	    control.add(addB, "North");
	    control.add(replaceB, "Center");
	    control.add(removeB,"South");
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(control,c);
	    cont.add(control);

	    c.fill = GridBagConstraints.BOTH;

	    c.gridwidth = GridBagConstraints.RELATIVE;
	    layout.setConstraints(okB,c);
	    cont.add(okB);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    layout.setConstraints(cancelB,c);
	    cont.add(cancelB);

	    updateSize();
	    updateKeys();
	}

    }

    class HashtableAttributeComponent extends ListEditor {

	protected HashtableAttributeEditor editor    = null;
	protected ArrayDictionary          hashtable = null;
	protected Vector                   listdata  = null;

	protected void edit() {
	    ArrayDictionary table = (ArrayDictionary)hashtable.clone();
	    Vector          list  = (Vector)listdata.clone();

	    HashtableAttributePopup popup = 
		new HashtableAttributePopup(this, table, list, "Edit");
	    popup.setLocationRelativeTo(this);
	    popup.show();
	    popup.toFront();
	}

	protected void setModified() {
	    editor.setModified();
	}

	protected void setTable(ArrayDictionary table) {
	    if (table == null) {
		hashtable = new ArrayDictionary(5);
		listdata = new Vector();
		return;
	    }
	    hashtable = table;
	    Enumeration keys = table.keys();
	    //list.removeAll();
	    listdata = new Vector();
	    while (keys.hasMoreElements())
		listdata.addElement((String)keys.nextElement());
	    list.setListData(listdata);
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

    protected boolean                     hasChanged = false;
    protected HashtableAttributeComponent comp       = null;
    protected ArrayDictionary             oldValue   = null;
    protected JFrame                      frame      = null;

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
	ArrayDictionary ad = comp.getTable();
	if ((ad != null) && (ad.size() > 0)) {
	    return ad;
	}
	return null;
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
	this.frame = ((org.w3c.jigadmin.RemoteResourceWrapper)
		      w).getServerBrowser().getFrame();
	createComponent((ArrayDictionary)o);
	oldValue = (ArrayDictionary)o;
    }

    public HashtableAttributeEditor() {
	super();
    }

}
