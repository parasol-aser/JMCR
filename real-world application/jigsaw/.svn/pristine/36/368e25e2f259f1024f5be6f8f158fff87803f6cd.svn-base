// SelectFileEditor.java
// $Id: SelectFileEditor.java,v 1.1 2010/06/15 12:22:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;

import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

public class SelectFileEditor extends AttributeEditor {

    class FComponent extends Panel implements ActionListener {
	SelectFileEditor editor = null;
	TextField text = null;
	Button select = null;

	public String getFile() {
	    return text.getText();
	}

	public void setFile(String file) {
	    text.setText(file);
	}

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		if (command.equals("select")) {
		    FileDialog fileD = new FileDialog(new Frame(),
						      "Select a file");
		    //FIXME
		    String file = getFile();
		    String dir = null;
		    if (file != null) {
			dir = new File(file).getParent();
			fileD.setDirectory(dir);
		    }
		    fileD.show();
		    file = fileD.getFile();
		    dir = fileD.getDirectory();
		    String selected = null;
		    if ((file != null) && (file.length() > 0)) {
			selected = (new File(dir, file)).getAbsolutePath();
			setFile(selected);
		    }
		}
	    }
	}

	FComponent(SelectFileEditor editor) {
	    this.editor = editor;
	    this.text = new TextField();
	    this.select = new Button("Select");
	    select.setActionCommand("select");
	    select.addActionListener(this);
	    setLayout( new BorderLayout());
	    add(text,"Center");
	    add(select,"East");
	}
    }

    private String origs;
    protected FComponent widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return !origs.equals(widget.getFile());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origs = widget.getFile();
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	widget.setFile(origs);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	return widget.getFile();
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o) {
	widget.setFile(o.toString());
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */

    public Component getComponent() {
	return widget;
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void initialize(RemoteResourceWrapper w, Attribute a,  Object o,
			   Properties p)
	throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    String v = null;
	    // FIXME
	    v = (String) r.getValue(a.getName());
	   
	    if(v == null)
		if(a.getDefault() != null)
		    v = a.getDefault().toString();
	    if ( v != null ) {
		origs = v;
		widget.setFile(origs);
	    } 
	} else {
	    origs = o.toString();
	}
	widget.setFile(origs);
    }

    public void createComponent() {
	widget = new FComponent(this);
    }

    public SelectFileEditor() {
	createComponent();
	origs = "";
    }

}
