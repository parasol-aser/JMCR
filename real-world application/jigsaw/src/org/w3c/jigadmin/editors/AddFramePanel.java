// AddFramePanel.java
// $Id: AddFramePanel.java,v 1.1 2010/06/15 12:25:52 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors; 

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Hashtable;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;

import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * A widget used to select a frame class.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AddFramePanel extends AddResourcePanel {

    /**
     * Get a list of resources that we can add to the given 
     * RemoteResourceWrapper.
     * @param rrw The RemoteResourceWrapper
     * @return a Hashtable instance containing the list of resource that can
     * be added to the given RemoteResource.
     * @exception RemoteAccessException is some remote error occurs
     */
    protected Hashtable getResources(RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	PropertyManager pm = PropertyManager.getPropertyManager();
	return pm.getFrames();
    }

    /**
     * Build the interface.
     * @param rrw The RemoteResourceWrapper of the resource where we are going
     * to add a frame.
     * @param title the title of the panel.
     * @exception RemoteAccessException is some remote error occurs
     */
    protected void build(RemoteResourceWrapper rrw, String title) 
	throws RemoteAccessException
    {
	/**
	 * Our internal ActionListener
	 */
	ActionListener afpal = new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		if( ae.getActionCommand().equals("Ok")) {
		    if(! classSC.getText().equals("")) {
			browser.setResourceToAdd(classSC.getText(), null);
			browser.disposeAddResourcePopup();
			done();
		    } else {
			classSC.requestFocus();
		    }
		} else if ( ae.getActionCommand().equals("Cancel")) {
		    browser.setResourceToAdd(null, null);
		    browser.disposeAddResourcePopup();
		    done();
		}
	    }
	};

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	JLabel l; JButton b;
	JPanel p = new JPanel(gbl);

	initializeStringChoice(rrw);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	gbc.insets = new Insets(0, 0, 10, 0);
	mgbc.fill = GridBagConstraints.NONE;
	mgbc.weightx = 0;
	mgbc.weighty = 0;
	mgbc.insets = new Insets(0, 10, 16, 5);
	setLayout(mgbl);

	l = new JLabel("Class name: ", JLabel.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(classSC, gbc);
	p.add(classSC);

	mgbc.gridwidth = GridBagConstraints.REMAINDER;
	mgbl.setConstraints(p, mgbc);
	add(p);
	// and now the usual button bar
	p = new JPanel(new GridLayout(1, 2, 20, 20));
	b = new JButton("Ok");
	b.addActionListener(afpal);
	p.add(b);
	b = new JButton("Cancel");
	b.addActionListener(afpal);
	p.add(b);
	mgbl.setConstraints(p, mgbc);
	add(p);
	setBorder(BorderFactory.createTitledBorder(title));
    }

    /**
     * Constructor.
     * @param title The widget title
     * @param  rrw The RemoteResourceWrapper of the resource where we are going
     * to add a frame.
     * @param browser the ResourceTreeBrowser
     * @exception RemoteAccessException is some remote error occurs
     */
    protected AddFramePanel(String title, 
			    RemoteResourceWrapper rrw,
			    ResourceTreeBrowser browser) 
	throws RemoteAccessException
    {
	super(title, rrw, browser);
    }

}
