// AuthPopup.java
// $Id: AuthPopup.java,v 1.1 2010/06/15 12:27:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.gui;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;

import org.w3c.tools.codec.Base64Encoder;

import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;

class AuthPopup extends Panel implements ActionListener {

    protected ServerBrowser sb;
    protected TextField user;
    protected TextField passwd;
    protected String orig;
    protected Image img;
    protected boolean ok;

    protected synchronized void done() {
	ok = true;
	notifyAll();
    }

    public void actionPerformed(ActionEvent ae) {
	if( ae.getActionCommand().equals("Ok") || 
	    ae.getSource().equals(passwd)) {
	    if(!user.getText().equals("")) {
		HttpCredential credential;
		credential = HttpFactory.makeCredential("Basic");
		Base64Encoder encoder = new Base64Encoder(user.getText()
							  +":"
                                                          +passwd.getText());
		credential.setAuthParameter("cookie", encoder.processString());
		sb.admin.setCredential(credential);
		sb.dispose(true);
		done();
	    } else {
		// popup an Error? FIXME
		user.requestFocus();
	    }
	} else if ( ae.getActionCommand().equals("Cancel")) {
            sb.dispose(false);
	} else if(ae.getSource().equals(user)) {
	    passwd.requestFocus();
	} 
    }

    public synchronized boolean waitForCompletion() {
	try {
	    wait();
	} catch (InterruptedException ex) {
	}
	return ok;
    }

    public AuthPopup (ServerBrowser sb, String name) {
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	Label l;
	Button b;
	Panel p = new Panel(gbl);

	ok = false;
	this.sb = sb;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	mgbc.fill = GridBagConstraints.NONE;
	mgbc.weightx = 0;
	mgbc.weighty = 0;
	mgbc.insets = new Insets(16, 10, 16, 5);
	setLayout(mgbl);
	user = new TextField(10);
	user.addActionListener(this);
	passwd = new TextField(10);
	passwd.setEchoChar('*');
	passwd.addActionListener(this);

	// Construct the first block with the labels and textfields
	if (name != null) {
	    l = new Label("Realm: ", Label.RIGHT);
	    gbc.gridwidth = 1;
	    gbl.setConstraints(l, gbc);
	    p.add(l);
	    l = new Label(name);
	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbl.setConstraints(l, gbc);
	    p.add(l);
	}
	l = new Label("User: ", Label.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(user, gbc);
	p.add(user);

	l = new Label("Password: ", Label.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(passwd, gbc);
	p.add(passwd);
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

    public AuthPopup (ServerBrowser sb) {
	this(sb, null);
    }
}
