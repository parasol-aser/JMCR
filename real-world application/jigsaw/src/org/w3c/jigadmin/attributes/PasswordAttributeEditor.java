// PasswordAttributeEditor.java
// $Id: PasswordAttributeEditor.java,v 1.1 2010/06/15 12:20:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes ;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BorderFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

class PasswordEditor extends JPanel {

    protected PasswordAttributeEditor pae;
    protected JPasswordField passwd;
    protected JPasswordField verify;
    protected String orig;

    private PasswordAttributeEditor getEditor() {
	return pae;
    }

    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
	    if( ae.getActionCommand().equals("Ok") || 
		ae.getSource().equals(verify)) {
		    String pass  = new String(passwd.getPassword());
		    String verif = new String(verify.getPassword());
		    if(pass.equals(verif) && (! pass.equals(""))) {
			getEditor().setValue(pass);
			getEditor().dispose();
		    } else {
			// popup an Error? FIXME
			passwd.requestFocus();
		    }
		} else if ( ae.getActionCommand().equals("Cancel")) {
		    getEditor().dispose();
		} else if(ae.getSource().equals(passwd)) {
		    verify.requestFocus();
		} 
	}
    };

    public PasswordEditor (PasswordAttributeEditor pae, String name) {

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	JLabel l;
	JButton b;
	JPanel p = new JPanel(gbl);

	this.pae = pae;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	mgbc.fill = GridBagConstraints.NONE;
	mgbc.weightx = 0;
	mgbc.weighty = 0;
	mgbc.insets = new Insets(16, 10, 16, 5);
	setLayout(mgbl);
	passwd = new JPasswordField(10);
	passwd.setBorder(BorderFactory.createLoweredBevelBorder());
	passwd.addActionListener(al);
	verify = new JPasswordField(10);
	verify.setBorder(BorderFactory.createLoweredBevelBorder());
	verify.addActionListener(al);

	// Construct the first block with the labels and textfields
	if (name != null)
	    setBorder(BorderFactory.createTitledBorder("User: "+name));

	l = new JLabel("Password: ", JLabel.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(passwd, gbc);
	p.add(passwd);

	l = new JLabel("Verify: ", JLabel.RIGHT);
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
	p = new JPanel(new GridLayout(1, 2, 20, 20));
	b = new JButton("Ok");
	b.addActionListener(al);
	p.add(b);
	b = new JButton("Cancel");
	b.addActionListener(al);
	p.add(b);
	mgbl.setConstraints(p, mgbc);
	add(p);
    }

    public PasswordEditor (PasswordAttributeEditor pae) {
	this(pae, null);
    }
}

public class PasswordAttributeEditor extends AttributeEditor {

    protected String  name   = null;
    protected JButton widget = null;
    protected JFrame  frame  = null;

    private String  origs   = null;
    private String  current = null;
    private JDialog popup   = null;

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
	    popup = new JDialog(frame, "Jigsaw Password Editor", false);
	    Container cont = popup.getContentPane();
	    cont.setLayout(new BorderLayout());
	    cont.add("Center", pe);
	    popup.setSize(new Dimension(300, 200));
	    popup.setLocationRelativeTo(widget);
	    popup.show();
	    pe.passwd.requestFocus();
	}
    }

    protected void setText(String s) {
	if(s.equals("")) {
	    widget.setText("WARNING: No password, click to edit");
	} else {
	    char c[] = new char[s.length()];
	    for(int i=0; i<s.length(); i++)
		c[i] = '*';
	    widget.setText(new String(c));
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
	setText(current);
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public void resetChanges() {
	current = new String(origs);
	setText(current);
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public Object getValue() {
	if ((current != null) && (current.length() > 0)) {
	    return current;
	}
	return null;
    }

    /**
     * @see org.w3c.jigadm.editors.AttributeEditorInterface
     */

    public void setValue(Object o) {
	current = o.toString();
	setText(current);
    }

    /*
     * @see org.w3c.jigadm.editors.AttributeEditor
     */

    public Component getComponent() {
	return widget;
    }

    public PasswordAttributeEditor() {
	widget = new JButton();
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
	frame = ((org.w3c.jigadmin.RemoteResourceWrapper)
		 w).getServerBrowser().getFrame();
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
	setText(origs);
	
	ActionListener al = new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		popupDialog();
	    }
	};
	widget.addActionListener(al);
    }
}
