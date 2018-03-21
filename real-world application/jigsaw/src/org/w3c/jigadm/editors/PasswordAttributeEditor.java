// PasswordAttributeEditor.java
// $Id: PasswordAttributeEditor.java,v 1.1 2010/06/15 12:22:44 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

class PasswordEditor extends Panel implements ActionListener {

    protected PasswordAttributeEditor pae;
    protected TextField passwd;
    protected TextField verify;
    protected String orig;
    protected Image img;

    public void actionPerformed(ActionEvent ae) {
	if( ae.getActionCommand().equals("Ok") || 
	    ae.getSource().equals(verify)) {
	    if(passwd.getText().equals(verify.getText())
	       && !passwd.getText().equals("")) {
		pae.setValue(passwd.getText());
		pae.dispose();
	    } else {
		// popup an Error? FIXME
		passwd.requestFocus();
	    }
	} else if ( ae.getActionCommand().equals("Cancel")) {
	    pae.dispose();
	} else if(ae.getSource().equals(passwd)) {
	    verify.requestFocus();
	} 
    }

    public PasswordEditor (PasswordAttributeEditor pae, String name) {
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	Label l;
	Button b;
	Panel p = new Panel(gbl);

	this.pae = pae;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	mgbc.fill = GridBagConstraints.NONE;
	mgbc.weightx = 0;
	mgbc.weighty = 0;
	mgbc.insets = new Insets(16, 10, 16, 5);
	setLayout(mgbl);
	passwd = new TextField(10);
	passwd.setEchoChar('*');
	passwd.addActionListener(this);
	verify = new TextField(10);
	verify.setEchoChar('*');
	verify.addActionListener(this);

	// Construct the first block with the labels and textfields
	if (name != null) {
	    l = new Label("User: ", Label.RIGHT);
	    gbc.gridwidth = 1;
	    gbl.setConstraints(l, gbc);
	    p.add(l);
	    l = new Label(name);
	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbl.setConstraints(l, gbc);
	    p.add(l);
	}
	l = new Label("Password: ", Label.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(passwd, gbc);
	p.add(passwd);

	l = new Label("Verify: ", Label.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(verify, gbc);
	p.add(verify);
	mgbc.gridwidth = GridBagConstraints.REMAINDER;
	mgbl.setConstraints(p, mgbc);
	add(p);
	
	// and now the usual button bar
	p = new Panel(new GridLayout(1, 2, 20, 20));
	b = new Button("Ok");
	b.addActionListener(this);
	p.add(b);
	b = new Button("Cancel");
	b.addActionListener(this);
	p.add(b);
	mgbl.setConstraints(p, mgbc);
	add(p);
    }

    public PasswordEditor (PasswordAttributeEditor pae) {
	this(pae, null);
    }
}

public class PasswordAttributeEditor extends AttributeEditor {

  class PasswordEditorListener implements ActionListener {
	
        public void actionPerformed(ActionEvent ae) {
	    popupDialog();
	}

    }

    protected String name = null;
    private String origs;
    private String current;
    Button  widget;
    private Frame popup = null;

    // get rid of the password editor

    protected void dispose() {
	if(popup != null) {
	    popup.dispose();
	    popup = null;
	}
    }

    // pops up a new editor

    protected void popupDialog() {
	if(popup == null) {
	    PasswordEditor pe = new PasswordEditor(this, name);
	    popup = new Frame("Jigsaw Password Editor");
	    popup.setBackground(Color.lightGray);
	    popup.setSize(new Dimension(300, 200));
	    popup.setLayout(new BorderLayout());
	    popup.add("Center", pe);
	    popup.show();
	    pe.passwd.requestFocus();
	}
    }

    protected void setLabel(String s) {
	if(s.equals("")) {
	    widget.setLabel("WARNING: No password, click to edit");
	} else {
	    char c[] = new char[s.length()];
	    for(int i=0; i<s.length(); i++)
		c[i] = '*';
	    widget.setLabel(new String(c));
	}
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public boolean hasChanged() {
	return !origs.equals(current);
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public void clearChanged() {
	origs = new String(current);
	setLabel(current);
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public void resetChanges() {
	current = new String(origs);
	setLabel(current);
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public Object getValue() {
	return current;
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public void setValue(Object o) {
	current = o.toString();
	setLabel(current);
    }

    /*
     * @see org.w3c.jigadm.editors.AttributeEditor
     */

    public Component getComponent() {
	return widget;
    }

    public PasswordAttributeEditor() {
	widget = new Button();
	origs = "";
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
	name = (String) r.getValue("identifier");
	if(o == null) {
	    String v = null;
	    v = (String) r.getValue(a.getName());
	    if(v == null)
		if(a.getDefault() != null)
		    v = a.getDefault().toString();
	    if ( v != null ) {
		origs = v;
	    } 
	} else {
	    origs = o.toString();
	}
	current = origs;
	setLabel(origs);
	widget.addActionListener(new PasswordEditorListener());
    }
}
