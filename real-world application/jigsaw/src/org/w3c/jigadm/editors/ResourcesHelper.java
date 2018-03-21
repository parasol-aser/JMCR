// ResourcesHelper.java
// $Id: ResourcesHelper.java,v 1.1 2010/06/15 12:22:40 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextComponent;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.FakeComboBox;

public class ResourcesHelper extends ResourceHelper {

    class AddResourceListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
	    addResource();
	}
    }

    RemoteResourceWrapper   rrw = null;
    RemoteResource rr = null;
    RemoteResource[] child = null;
    private boolean initialized = false;
    Properties prop;
    FakeComboBox combo;
    TextField tf;
    Panel widget;
    Panel addPanel = null;

    protected void addResource() {
	if(tf.getText().length() > 0) {
	    RemoteResource nrr;
	    String selected = combo.getText();
	    if (selected != null)
	      if (selected.length() > 0) {
		try {
		    nrr = rrw.getResource().
			              registerResource(tf.getText(),
						       selected);
		} catch (RemoteAccessException ex) {
		    // Add a fancy error
		    return;
		}
		RemoteResourceWrapper nrrw;
		nrrw = new RemoteResourceWrapper(rrw, nrr, rrw.getBrowser());
		rrw.getBrowser().insertNode(rrw, nrrw, tf.getText());
	    }
	}
    }

    protected RemoteResourceWrapper getWrapper() {
	return rrw;
    }

    public Component getComponent() {
	return widget;
    }

    /**
     * commit changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void commitChanges()
	throws RemoteAccessException
    {
	if(!initialized)
	    return;
	return;
    }

    public boolean hasChanged() {    
	return false;
    }

    public void resetChanges() {
    }

    public void clearChanged() {
    }

    public final String getTitle () {
	return "Resources";
    }

    public ResourcesHelper() {
	widget = new Panel();
    }

    protected void initAddPanel(Properties config) {
	if(addPanel == null) {
	    Panel tfp;
	    addPanel = new Panel(new BorderLayout());
	
	    String af = (String) config.get(
		"org.w3c.jigadm.editors.resource.resources");
	    if ( af == null )
		return;
	    StringTokenizer st = new StringTokenizer(af, "|");
	    ScrollPane fsp = new ScrollPane();
	    GridBagLayout fgbl = new GridBagLayout();
	    GridBagConstraints fgbc = new GridBagConstraints();
	    Panel fspp = new Panel (fgbl);
	    fsp.add(fspp);
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    String downPath = pm.getIconLocation("down");
	    String leftPath = pm.getIconLocation("left");
	    combo = new FakeComboBox(35,7,true,downPath,leftPath); 
	    while(st.hasMoreTokens())
	      combo.add(st.nextToken().trim());
	    fspp.add(combo);
	    addPanel.add("Center", fsp);
	    Button newb     = new Button("Add Resource");
	    newb.addActionListener(new AddResourceListener());
	    addPanel.add("South", newb);
	    tfp = new Panel(new GridLayout(1, 2));
	    tf = new TextField();
	    tfp.add(new Label("identifier"));
	    tfp.add(tf);
	    addPanel.add("North", tfp);
	}
	widget.add("Center", addPanel);
	
    }

    /**
     * initialize this helper
     * @param rrw The RemoteResourceWrapper
     * @param pr The properties
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties pr)
	throws RemoteAccessException
    {
	if(!initialized)
	    initialized = true;
	else
	    return;	
	
	this.rrw = rrw;
	rr = rrw.getResource();
	widget.setLayout(new BorderLayout());
	initAddPanel(pr);
    }
}
