// ServerList.java
// $Id: ServerList.java,v 1.1 2010/06/15 12:28:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui.slist;

import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.AbstractButton;

import javax.swing.event.EventListenerList;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.widgets.Utilities;

/**
 * Manage a list of Jigsaw server.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServerList extends JPanel {

    ServerListModelInterface model        = null;
    EventListenerList        listenerList = null;

    ImageIcon icon = null;

    /**
     * Our internal ActionListener.
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    fireServerSelectedEvent(e.getActionCommand());
	}
    };

    /**
     * Get the Server List Model.
     * @return a ServerListModelInterface instance.
     */
    public ServerListModelInterface getModel() {
	return model;
    }

    /**
     * Add a ServerListListener.
     * @param listener the ServerListListener to add.
     */
    public void addServerListListener(ServerListListener listener) {
	listenerList.add(ServerListListener.class, listener);
    }

    /**
     * Remove a ServerListListener.
     * @param listener the ServerListListener to remove.
     */
    public void removeServerListListener(ServerListListener listener) {
	listenerList.remove(ServerListListener.class, listener);
    }

    /**
     * Fire a ServerSelectedEvent for the given server name.
     * @param name the server name.
     */
    protected void fireServerSelectedEvent(String name) {
	RemoteResourceWrapper server = null;
	server = model.getServer(name);
	Object listeners[] = listenerList.getListenerList();
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == ServerListListener.class) {
		((ServerListListener)
		 listeners[i+1]).serverSelected(name, server);
	    }
	}
    }

    /**
     * Build the interface.
     */
    protected void build() {
	String  servers[] = model.getServers();
	int     len       = servers.length;
	JButton b         = null;
	setLayout(new GridLayout(len, 1));

	for (int i = 0 ; i < len ; i++) {
	    b = new JButton(servers[i], icon);
	    b.setVerticalTextPosition(AbstractButton.BOTTOM);
	    b.setHorizontalTextPosition(AbstractButton.CENTER);
	    b.setActionCommand(servers[i]);
	    b.addActionListener(al);
	    b.setMargin(Utilities.insets2);
	    b.setToolTipText("Load or reload the configuration of "+
			     servers[i]);
	    //b.setBorder(BorderFactory.createRaisedBevelBorder());
	    add(b);
	}
    }

    /**
     * Constructor.
     * @param model The Server list model.
     */
    public ServerList(ServerListModelInterface model) {
	this(model, null);
    }

    /**
     * Constructor.
     * @param model The Server list model.
     * @param icon. The server icon.
     */
    public ServerList(ServerListModelInterface model, ImageIcon icon) {
	this.icon    = icon;
	this.model   = model;
	listenerList = new EventListenerList();
	build();
    }

    /**
     * Constructor.
     * @param root The root RemoteResourceWrapper
     */
    public ServerList(RemoteResourceWrapper root) 
	throws RemoteAccessException
    {
	this(root, null);
    }

    /**
     * Constructor.
     * @param root The root RemoteResourceWrapper
     * @param icon. The server icon.
     */
    public ServerList(RemoteResourceWrapper root, ImageIcon icon) 
	throws RemoteAccessException
    {
	this.icon    = icon;
	this.model   = new ServerListModel(root);
	listenerList = new EventListenerList();
	build();
    }

}
