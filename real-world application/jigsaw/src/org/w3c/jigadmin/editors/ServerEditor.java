// ServerEditor.java
// $Id: ServerEditor.java,v 1.1 2010/06/15 12:25:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import java.util.Properties;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.widgets.DnDTabbedPane;
import org.w3c.jigadmin.events.ResourceActionSource;
import org.w3c.jigadmin.events.ResourceActionListener;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.widgets.Utilities;
import org.w3c.tools.sorter.Sorter;

/**
 * The server editor.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServerEditor extends JPanel implements ServerEditorInterface {

    private class Retryer extends Thread {
	
	RemoteAccessException ex = null;

	public void run() {
	    while (server.getServerBrowser().shouldRetry(ex)) {
		try {
		    initializeServerHelpers();
		    break;
		} catch (RemoteAccessException ex2) {
		    ex = ex2;
		}
	    }
	    build();
	}

	private Retryer(RemoteAccessException ex) {
	    this.ex = ex;
	}
    }

    protected RemoteResourceWrapper server     = null;
    protected String                name       = null;
    protected ServerHelperInterface shelpers[] = null;

    /**
     * Reload the server configuration.
     * @param server the new server wrapper
     */
    public void setServer(RemoteResourceWrapper server) {
	this.server = server;
	try {
	    initializeServerHelpers();
	    build();
	} catch (RemoteAccessException ex) {
	    (new Retryer(ex)).start();
	}
    }

    /**
     * Get the component.
     * @return a Component
     */
    public Component getComponent() {
	return this;
    }

    /**
     * initialize the server helpers.
     * @exception RemoteAccessException if a remote error occurs
     */
    protected void initializeServerHelpers()
	throws RemoteAccessException
    {
	shelpers = null;
	//use ServerHelperFactory....
	RemoteResource rr = server.getResource();
	String names[] = rr.enumerateResourceIdentifiers();
	Sorter.sortStringArray(names, true);
	shelpers = new ServerHelperInterface[names.length];
	for (int i = 0 ; i < names.length ; i++) {
	    RemoteResourceWrapper rrw = server.getChildResource(names[i]);
	    shelpers[i] = ServerHelperFactory.getServerHelper(names[i], rrw);
	}

	for (int i = 0 ; i < shelpers.length ; i++) {
	    if (shelpers[i] instanceof ResourceActionSource) {
		ResourceActionSource source = 
		    (ResourceActionSource)shelpers[i]; 
		for (int j = 0 ; j < shelpers.length ; j++) {
		    if (j != i) {
			if (shelpers[j] instanceof ResourceActionListener) {
			    ResourceActionListener listener =
				(ResourceActionListener) shelpers[j];
			    source.addResourceActionListener(listener);
			}
		    }
		}
	    }
	}
    }

    /**
     * Build the interface.
     */
    protected void build() {
	removeAll();
	invalidate();
	setLayout(new BorderLayout());
	TitledBorder border = BorderFactory.createTitledBorder(name);
	border.setTitleFont(Utilities.reallyBigBoldFont);
	setBorder(border);
	//tabbedpane
	JTabbedPane tabbedPane = new DnDTabbedPane();
	if (shelpers == null)
	    return;

	for (int i = 0 ; i < shelpers.length ; i++) {
	    if (shelpers[i] instanceof ChangeListener)
		tabbedPane.addChangeListener((ChangeListener)shelpers[i]);

	    if (shelpers[i] instanceof ControlServerHelper) {
		//add it on top of the panel
		add(shelpers[i].getComponent(), BorderLayout.NORTH);
	    } else {
		String name = shelpers[i].getName().replace('_',' ');
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		String helperName = new String(chars);
		tabbedPane.addTab(helperName, null, 
				  shelpers[i].getComponent(),
				  shelpers[i].getToolTip());
	    }
	}
	tabbedPane.setSelectedIndex(0);
	add(tabbedPane, BorderLayout.CENTER);
	validate();
    }

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */ 
    public void initialize(String name,
			   RemoteResourceWrapper rrw, 
			   Properties p) 
    {
	this.server = rrw;
	this.name   = name;
    }

    public ServerEditor() {
	//for new Isntance
    }

   
    
}
