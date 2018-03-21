// MimeTypeAttributeEditor.java
// $Id: MimeTypeAttributeEditor.java,v 1.1 2010/06/15 12:22:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.ClosableFrame;

import org.w3c.www.mime.MimeType;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * MimeTypeAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class MimeTypeAttributeEditor extends AttributeEditor {

    class MimeTypeAttributePopup extends ClosableFrame 
	implements ActionListener 
    {

	protected TextField mimetype = null;
	protected MimeTypeAttributeComponent parent = null;

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		if (command.equals("update")) {
		    parent.setText(mimetype.getText());
		    setVisible(false);
		} else  if (command.equals("cancel")) {
		    close();
		} else 
		    mimetype.setText(command);
	    }
	}

	protected void close() {
	    setVisible(false);
	    mimetype.setText("");
	}

	private void addMenuListener(MenuItem item, String action) {
	    item.addActionListener(this);
	    item.setActionCommand(action);
	}

	MimeTypeAttributePopup(MimeTypeAttributeComponent parent) {
	    this.parent = parent;

	    GridBagLayout layout = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.BOTH;
	    c.insets = new Insets(5,5,5,5);
	    setLayout(layout);

	    // MENU
	    MenuBar menubar = new MenuBar();
	    MenuItem item;
	    Menu menu = new Menu("MimeTypes");
	    Hashtable mimeTypes;
	    Enumeration e;
	    String minor[];

	    mimeTypes = PropertyManager.getPropertyManager().getMimeTypes();
	    e = mimeTypes.keys();

	    while(e.hasMoreElements()) {
		String major = (String)e.nextElement();
		Menu imenu = new Menu(major);
		imenu.addActionListener(this);
		minor = (String[]) mimeTypes.get(major);
		for(int i=0; i<minor.length; i++) {
		    item = new MenuItem(minor[i]);
		    addMenuListener(item, major + "/" + minor[i]);
		    imenu.add(item);
		}
		menu.add(imenu);
	    }
	    menubar.add(menu);

	    c.gridwidth = GridBagConstraints.RELATIVE;
	    Label label = new Label("Mime Type : ");
	    layout.setConstraints(label,c);
	    add(label);

	    c.gridwidth = GridBagConstraints.REMAINDER;    
	    mimetype = new TextField(20);
	    layout.setConstraints(mimetype,c);
	    add(mimetype);

	    Button okB = new  Button("Ok");
	    okB.setActionCommand("update");
	    okB.addActionListener(this);
	    Button cancelB = new Button("Cancel");
	    cancelB.setActionCommand("cancel");
	    cancelB.addActionListener(this);

	    Panel p = new Panel();
	    GridBagLayout playout = new GridBagLayout();
	    p.setLayout( playout);

	    c.fill = GridBagConstraints.NONE;
	    c.anchor=GridBagConstraints.EAST;
	    c.gridwidth = GridBagConstraints.RELATIVE;
	    playout.setConstraints(okB,c);
	    p.add(okB);

	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.anchor=GridBagConstraints.WEST;
	    playout.setConstraints(cancelB,c);
	    p.add(cancelB);

	    c.fill = GridBagConstraints.NONE;
	    c.anchor=GridBagConstraints.CENTER;
	    c.gridwidth = 2;
	    layout.setConstraints(p,c);
	    add(p);

	    setMenuBar(menubar);

	    setSize(300,150);
	}

    }

    class MimeTypeAttributeComponent extends Panel 
	implements ActionListener,
	TextListener 
    {

	protected TextField type = null;
	protected MimeTypeAttributePopup popup = null;
	protected MimeTypeAttributeEditor editor = null;

	public void textValueChanged(TextEvent e) {
	    setModified();
	}

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		if (command.equals("edit")) {
		    if (popup == null)
			popup = new MimeTypeAttributePopup(this);
		    popup.show();
		}
	    }
	}

	public String getText() {
	    return type.getText();
	}

	public void setText(String text) {
	    type.setText(text);
	    editor.setModified();
	}

	MimeTypeAttributeComponent (MimeTypeAttributeEditor editor,
				    String type)
	{
	    super();
	    this.editor = editor;
	    this.type = new TextField(20);
	    this.type.setText(type);
	    this.type.addTextListener(this);
	    Button editB = new Button("Change");
	    editB.setActionCommand("edit");
	    editB.addActionListener(this);
	    setLayout( new BorderLayout());
	    add(this.type,"West");
	    add(editB,"Center");
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
	    ex.printStackTrace();
	}
	// not reached
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
