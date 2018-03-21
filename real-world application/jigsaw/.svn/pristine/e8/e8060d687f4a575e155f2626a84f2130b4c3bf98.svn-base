// AuthPanel.java
// $Id: AuthPanel.java,v 1.1 2010/06/15 12:21:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui; 

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;

import org.w3c.tools.codec.Base64Encoder;

/**
 * The Authentication dialog.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AuthPanel extends JPanel {

    protected boolean        ok;
    protected ServerBrowser  sb;
    protected JTextField     user;
    protected JPasswordField passwd;

    /**
     * Constructor.
     * @param sb The ServerBrowser
     * @param name the realm name.
     */
    protected AuthPanel(ServerBrowser sb, String name) {
	this.sb = sb;
	this.ok = false;

	ActionListener al = new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		if( ae.getActionCommand().equals("Ok") || 
		    ae.getSource().equals(passwd)) 
		{
		    if(! user.getText().equals("")) {
			HttpCredential credential;
			credential = HttpFactory.makeCredential("Basic");
			String password = new String(passwd.getPassword());
			Base64Encoder encoder = 
			new Base64Encoder(user.getText()
					  +":"
					  +password);
			credential.setAuthParameter("cookie", 
						    encoder.processString());
			AuthPanel.this.sb.getAdminContext().setCredential(
								 credential);
			AuthPanel.this.sb.dispose(true);
			done();
		    } else {
			user.requestFocus();
		    }
		} else if ( ae.getActionCommand().equals("Cancel")) {
		    AuthPanel.this.sb.dispose(false);
		    done();
		} else if(ae.getSource().equals(user)) {
		    passwd.requestFocus();
		}
	    }
	};

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	JLabel l; JButton b;
	JPanel p = new JPanel(gbl);

	user   = new JTextField(10);

	passwd = new JPasswordField(10);
	passwd.addActionListener(al);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	mgbc.fill = GridBagConstraints.NONE;
	mgbc.weightx = 0;
	mgbc.weighty = 0;
	mgbc.insets = new Insets(16, 10, 16, 5);
	setLayout(mgbl);

	l = new JLabel("User name: ", JLabel.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(user, gbc);
	p.add(user);

	l = new JLabel("Password: ", JLabel.RIGHT);
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
	p = new JPanel(new GridLayout(1, 2, 20, 20));
	b = new JButton("Ok");
	b.addActionListener(al);
	p.add(b);
	b = new JButton("Cancel");
	b.addActionListener(al);
	p.add(b);
	mgbl.setConstraints(p, mgbc);
	add(p);
	if (name != null)
	    setBorder(BorderFactory.createTitledBorder("Realm: "+name));
	else
	    setBorder(BorderFactory.createTitledBorder("Authentication"));
    }

    /**
     * Request the focus on the user TextField.
     */
    protected void getFocus() {
	user.requestFocus();
    }

    /**
     * Notify All thread that the username/password are entered.
     */
    protected synchronized void done() {
	ok = true;
	notifyAll();
    }

    /**
     * Wait until it's done.
     */
    public synchronized boolean waitForCompletion() {
	try { 
	    wait(); 
	} catch (InterruptedException ex) {
	    //nothing to do
	}
	return ok;
    }

}
