// SelectFileEditor.java
// $Id: SelectFileEditor.java,v 1.1 2010/06/15 12:20:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes ;

import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.BorderFactory;

import java.io.File;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

public class SelectFileEditor extends AttributeEditor {

    class FComponent extends JPanel implements ActionListener {
	JTextField text = null;
	JButton select = null;

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
		    JFileChooser chooser = null;
		    String file = getFile();
		    String dir = null;
		    if (file != null) {
			dir = new File(file).getParent();
			chooser = new JFileChooser(dir);
		    } else {
			chooser = new JFileChooser();
		    }
		    chooser.setFileSelectionMode(
					 JFileChooser.FILES_AND_DIRECTORIES);
		    int returnVal = chooser.showOpenDialog(this);
		    if (returnVal ==  JFileChooser.APPROVE_OPTION) {
			File selected = chooser.getSelectedFile();
			setFile(selected.getAbsolutePath());
		    }
		}
	    }
	}

	FComponent() {
	    this.text = new JTextField();
	    text.setBorder(BorderFactory.createLoweredBevelBorder());
	    this.select = new JButton("Select");
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
	widget = new FComponent();
    }

    public SelectFileEditor() {
	createComponent();
	origs = "";
    }

}
