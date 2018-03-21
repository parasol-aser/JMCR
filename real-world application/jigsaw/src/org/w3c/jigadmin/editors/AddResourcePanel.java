// AddResourcePanel.java
// $Id: AddResourcePanel.java,v 1.1 2010/06/15 12:25:50 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JTextField;
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

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.widgets.EditableStringChoice;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.sorter.Sorter;

/**
 * A widget used to select a resource class and its identifier.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AddResourcePanel extends JPanel {

    protected boolean ok;

    protected EditableStringChoice classSC;

    protected JTextField identifierTF;

    protected ResourceTreeBrowser browser = null;

    /**
     * Our internal ActionListener
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
	    if( ae.getActionCommand().equals("Ok") || 
		ae.getSource().equals(identifierTF)) {
		    if(! classSC.getText().equals("")) {
			if (! identifierTF.getText().equals("")) {
			    browser.setResourceToAdd(classSC.getText(), 
						     identifierTF.getText());
			    browser.disposeAddResourcePopup();
			    done();
			} else {
			    identifierTF.requestFocus();
			}
		    } else {
			classSC.requestFocus();
		    }
		} else if ( ae.getActionCommand().equals("Cancel")) {
		    browser.setResourceToAdd(null, null);
		    browser.disposeAddResourcePopup();
		    done();
		} else if(ae.getSource().equals(classSC)) {
		    identifierTF.requestFocus();
		}
	}
    };

    /**
     * Get a list of resources that we can add to the container wrapped by
     * the given RemoteResourceWrapper.
     * @param rrw The RemoteResourceWrapper
     * @return a Hashtable instance containing the list of resource that can
     * be added to the given RemoteResource.
     * @exception RemoteAccessException is some remote error occurs
     */
    protected Hashtable getResources(RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	PropertyManager pm = PropertyManager.getPropertyManager();
	RemoteResource rr = rrw.getResource();
	if (rr.isIndexersCatalog())
	    return pm.getIndexers();
	else
	    return pm.getResources();
    }

    /**
     * Initialize the StringChoice.
     * @param rrw The RemoteResourceWrapper of the container where we are going
     * to add a resource.
     * @exception RemoteAccessException is some remote error occurs
     */
    protected void initializeStringChoice(RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	classSC = new EditableStringChoice();
	Hashtable resources = getResources(rrw);
	Enumeration e = 
	    (Sorter.sortStringEnumeration(resources.keys()))
	    .elements();
	Vector cells = new Vector(10);
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    ResourceCell cell = 
		new ResourceCell(name, (String)resources.get(name));
	    cells.addElement(cell);
	}

	Object items[] = new Object[cells.size()];
	cells.copyInto(items);
	classSC.initialize(items);
	classSC.setRenderer(new ResourceCellRenderer());
	classSC.setMaximumRowCount(5);
    }
	
    /**
     * Build the interface.
     * @param rrw The RemoteResourceWrapper of the container where we are going
     * to add a resource.
     * @param title the title of the panel.
     * @exception RemoteAccessException is some remote error occurs
     */
    protected void build(RemoteResourceWrapper rrw, String title) 
	throws RemoteAccessException
    {
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout mgbl = new GridBagLayout();
	GridBagConstraints mgbc = new GridBagConstraints();
	JLabel l; JButton b;
	JPanel p = new JPanel(gbl);

	initializeStringChoice(rrw);

	identifierTF = new JTextField(25);
	identifierTF.addActionListener(al);
	identifierTF.setBorder(BorderFactory.createLoweredBevelBorder());

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

	l = new JLabel("Identifier: ", JLabel.RIGHT);
	gbc.gridwidth = 1;
	gbl.setConstraints(l, gbc);
	p.add(l);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(identifierTF, gbc);
	p.add(identifierTF);
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
	setBorder(BorderFactory.createTitledBorder(title));
    }

    /**
     * Constructor.
     * @param title The widget title
     * @param  rrw The RemoteResourceWrapper of the container where we are 
     * going to add a resource.
     * @param browser the ResourceTreeBrowser
     * @exception RemoteAccessException is some remote error occurs
     */
    protected AddResourcePanel(String title, 
			       RemoteResourceWrapper rrw,
			       ResourceTreeBrowser browser) 
	throws RemoteAccessException
    {
	this.ok      = false;
	this.browser = browser;
	build(rrw, title);
    }

    /**
     * request the focus for the class combobox.
     */
    protected void getFocus() {
	classSC.requestFocus();
    }

    /**
     * NotifyAll that the resource class and its identifier are selected.
     */
    protected synchronized void done() {
	ok = true;
	notifyAll();
    }

    /**
     * Wait until the resource class and its identifier are selected.
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
