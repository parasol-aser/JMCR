// MimeTypeAttributeEditor.java
// $Id: MimeTypeAttributeEditor.java,v 1.1 2010/06/15 12:20:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.widgets.ClosableFrame;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.resources.Attribute;

import org.w3c.www.mime.MimeType;

/**
 * MimeTypeAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class MimeTypeAttributeEditor extends AttributeEditor {

    class MimeTypeAttributeComponent extends JPanel 
	implements ActionListener, DocumentListener 
    {

	protected JTextField              type   = null;
	protected MimeTypeAttributeEditor editor = null;

	//DocumentListener
	public void insertUpdate(DocumentEvent e) {
	    setModified();
	}

	//DocumentListener
	public void changedUpdate(DocumentEvent e) {
	    setModified();
	}

	//DocumentListener
	public void removeUpdate(DocumentEvent e) {
	    setModified();
	}

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		type.setText(command);
	    }
	}

	public String getText() {
	    return type.getText();
	}

	public void setText(String text) {
	    type.setText(text);
	    editor.setModified();
	}

	private void addMenuListener(JMenuItem item, String action) {
	    item.addActionListener(this);
	    item.setActionCommand(action);
	}

	protected void build(String type) {
	    JMenuBar    menubar   = null;
	    JMenu       menu      = null;
	    JMenuItem   item      = null;
	    Hashtable   mimeTypes = null;
	    Enumeration e         = null;
	    String      minor[]   = null;

	    this.type = new JTextField(20);
	    this.type.setBorder(BorderFactory.createLoweredBevelBorder());
	    this.type.setText(type);
	    this.type.getDocument().addDocumentListener(this);
	    //build the menu
	    menubar = new JMenuBar();
	    menubar.setBorderPainted(false);
	    menu    = new JMenu("MimeTypes");
	    menubar.setAlignmentX(LEFT_ALIGNMENT);
	    menubar.setAlignmentY(TOP_ALIGNMENT);

	    mimeTypes = PropertyManager.getPropertyManager().getMimeTypes();
	    e = mimeTypes.keys();

	    while(e.hasMoreElements()) {
		String major = (String)e.nextElement();
		JMenu imenu = new JMenu(major);
		imenu.addActionListener(this);
		minor = (String[]) mimeTypes.get(major);
		for(int i = 0 ; i < minor.length ; i++) {
		    item = new JMenuItem(minor[i]);
		    addMenuListener(item, major + "/" + minor[i]);
		    imenu.add(item);
		}
		menu.add(imenu);
	    }
	    menubar.add(menu);
	    //end
	    setLayout(new BorderLayout());
	    add(this.type,BorderLayout.WEST);
	    add(menubar,BorderLayout.CENTER);
	}

	MimeTypeAttributeComponent (MimeTypeAttributeEditor editor,
				    String type)
	{
	    super();
	    this.editor = editor;
	    build(type);
	}

    }

    // The MimeTypeAttributeEditor itself

    protected MimeTypeAttributeComponent comp = null;
    protected boolean hasChanged = false;
    protected String oldvalue  = null;

    protected void createComponent(String type) {
	if ( comp == null ) 
	    comp = new MimeTypeAttributeComponent(this,type);
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
	    return new MimeType(comp.getText());
	} catch (Exception ex) {
	    // no mime type then!
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
	MimeType type = (MimeType)o;
	if (o == null) {
	    oldvalue = "*none*";
	    createComponent(oldvalue);;
	} else {
	    createComponent(type.toString());
	    oldvalue = type.toString();
	}
    }

}
