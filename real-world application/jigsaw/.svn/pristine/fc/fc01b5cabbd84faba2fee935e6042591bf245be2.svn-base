// ResourceEditor.java
// $Id: ResourceEditor.java,v 1.1 2010/06/15 12:22:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EventObject;
import java.util.Properties;
import java.util.Vector;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.events.ResourceChangeEvent;
import org.w3c.jigadm.events.ResourceListener;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.MessagePopup;

public class ResourceEditor implements ResourceEditorInterface {

    public static final String UNREMOVABLE_P = "unremovable";

    class Alert extends Canvas {

	Image alert = null;
	int width  = 0;
	int height = 0;

	public Dimension getMinimumSize() {
	    return new Dimension(width+8, height+8);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(width+8, height+8);
	}

	public boolean imageUpdate(Image img, int flaginfo,
				   int x, int y, int width, int height) {
	    initSize();
	    Container parent = getParent();
	    if(parent != null) 
		parent.doLayout();
	    return super.imageUpdate(img, flaginfo, x, y, width, height);
	}

	private void initSize() {
	    if (alert != null) {
		width = alert.getWidth(this);
		height = alert.getHeight(this);
	    }
	}

	public void paint(Graphics g) {
	    if (alert != null)
		g.drawImage(alert, 0, 0, this);
	}

	Alert() {
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    String alertPath = pm.getIconLocation("alert");
	    if (alertPath != null) {
		alert = Toolkit.getDefaultToolkit().getImage(alertPath);
		prepareImage(alert,this);
	    }
	    initSize();
	}
    }

    class ButtonBarListener implements ActionListener {

 	class Switcher extends Thread {
	    Button b;

            public void run() {
		try {
		    switchHelper(b);
	   	} catch (RemoteAccessException ex) {
		    // do fancy things FIXME
		}
	    }
	    
	    Switcher(Button b) {
		this.b = b;
	    }
	}

        class Deleter extends Thread {
            public void run() {
		delete();
	    }
	}

        class Reindexer extends Thread {
            public void run() {
		reindex();
	    }
	}

        public void actionPerformed(ActionEvent ae) {
	    if (ae.getActionCommand().equals(DELETE_L)) {
		(new Deleter()).start();
	    } else if(ae.getActionCommand().equals(REINDEX_L)) {
		setMessage("Reindexation started...");
		(new Reindexer()).start();
		setMessage("Reindexation done.");
	    } else {
		(new Switcher((Button)ae.getSource())).start();
	    }
	}
    }

    class MouseButtonListener extends MouseAdapter {

	public void mouseEntered(MouseEvent e) {
	    Component comp = e.getComponent();
	    if (comp instanceof Button) {
		String action = ((Button)comp).getActionCommand();
		if (action.equals(DELETE_L)) {
		    setMessage("Delete this resource!");
		} else if (action.equals(REINDEX_L)) {
		    setMessage("Reindex the children.");
		}
	    }
	}

	public void mouseExited(MouseEvent e) {
	    setMessage("");
	}
    }

    protected Vector rls = null;

    Panel target = null;
    Panel buttonp = null;
    Panel editorp = null;
    Button commitb = null;
    Button resetb;
    RemoteResource rr = null;
    RemoteResourceWrapper rrw = null;
    ResourceHelper rh[] = null;
    protected Button b[] = null;
    protected Label message = null;
    Properties p;

    protected static final String DELETE_L  = "Delete";
    protected static final String REINDEX_L = "Reindex";

    public void setMessage(String msg) {
	message.setText(msg);
    }

    public void commitChanges()
	throws RemoteAccessException
    {
	boolean authorized;
	for(int i=0; i<rh.length; i++) {
	    if(rh[i].hasChanged()) {
		authorized = false;
		while (!authorized) {
		    try {
			authorized = true;
			rh[i].commitChanges();
		    } catch (RemoteAccessException ex) {
			if( ex.getMessage().equals("Unauthorized")) {
			    authorized = false;
			} else {
			    (new MessagePopup("Exception : ",
					      ex.getMessage())).show();
			    throw ex; // another reason, throw it
			}
		    } finally {
			if(!authorized) {
			    rrw.getBrowser().popupDialog("admin");
			}
		    }
		}
	    }
	}
    }

    public void resetChanges()
	throws RemoteAccessException
    {
	for(int i=0; i<rh.length; i++) {
	    if(rh[i].hasChanged()) {
		rh[i].resetChanges();
	    }
	}
    }

    protected void switchHelper(Button toggled)
	throws RemoteAccessException
    {
	for(int i=0; i< b.length; i++) {
	    boolean authorized;
	    if( toggled.equals(b[i])) {
	        PropertyManager pm = PropertyManager.getPropertyManager();
		Properties props = 
		    pm.getHelperProperties(rrw, rh[i].getClass().getName());
		authorized = false;
		while (!authorized) {
		    try {
			authorized = true;
			rh[i].initialize(rrw, props);
		    } catch (RemoteAccessException ex) {
			if( ex.getMessage().equals("Unauthorized")) {
			    authorized = false;
			} else {
			    (new MessagePopup("Exception : ",
					      ex.getMessage())).show();
			    throw ex; // another reason, throw it
			}
		    } finally {
			if(!authorized) {
			    rrw.getBrowser().popupDialog("admin");
			}
		    }
		}
		editorp.setVisible(false);
		editorp.removeAll();
		editorp.add("Center", rh[i].getComponent());
		editorp.validate();
		editorp.setVisible(true);
	    }
	}
    }

    protected void reindex() {
	boolean authorized = false;
	while (!authorized) {
	    try { 
		authorized = true;
		rrw.getResource().reindex(true);
	    } catch (RemoteAccessException ex) {
		if( ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    (new MessagePopup("Exception : ",
				      ex.getMessage())).show();
		    // must throw a fancy error
		}
	    } finally {
		if(!authorized) {
		    rrw.getBrowser().popupDialog("admin");
		}
	    }
	}
    }

    protected void delete() {
	boolean authorized = false;
	while (!authorized) {
	    try { 
		authorized = true;
		rrw.getResource().delete();
	    } catch (RemoteAccessException ex) {
		if( ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    (new MessagePopup("Exception : ",
				      ex.getMessage())).show();
		    // must throw a fancy error
		}
	    } finally {
		if(!authorized) {
		    rrw.getBrowser().popupDialog("admin");
		}
	    }
	}
	processEvent(new ResourceChangeEvent(rrw.getFatherWrapper(),
					     "deleted", rrw, null));
    }

    public boolean hasChanged() {
	for(int i=0; i<rh.length; i++) {
	    if(rh[i].hasChanged()) {
		return true;
	    }
	}
	return false;
    }

    public void clearChanged() {
	for(int i=0; i<rh.length; i++) {
	    if(rh[i].hasChanged()) {
		rh[i].clearChanged();
	    }
	}
    }

    public RemoteResource getValue() {
	return rr;
    }

    public synchronized void addResourceListener(ResourceListener rl) {
	if (rls == null)
	    rls = new Vector(2);
	rls.addElement(rl);
    }

    public synchronized void removeResourceListener(ResourceListener rl) {
	if ( rls != null ) 
	    rls.removeElement(rl);
    }

    protected void processEvent(EventObject eo) {
	Vector rls = null;
	ResourceListener rl;
	synchronized(this) {
	    if((this.rls != null) && (eo instanceof ResourceChangeEvent )) {
		rls = (Vector) this.rls.clone();
	    } else {
		return;
	    }
	}
	for(int i=0; i<rls.size(); i++) {
	    rl = (ResourceListener) rls.elementAt(i);
	    rl.resourceChanged((ResourceChangeEvent)eo);
	}
    }

    /**
     * initialize this editor
     * @param rrw The RemoteResourceWrapper
     * @param pr The properties
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties p)
	throws RemoteAccessException
    {
	int num = 0;
	this.rrw = rrw;
	rr = rrw.getResource();
	ButtonBarListener bbl = new ButtonBarListener();
	this.p = p;
	target.removeAll();
	BorderPanel ptarget = new BorderPanel(BorderPanel.OUT, 2);
	ptarget.setLayout(new BorderLayout());
	rh = ResourceHelperFactory.getHelpers(rrw);

	PropertyManager pm = PropertyManager.getPropertyManager();
	Properties props = pm.getEditorProperties(rrw);
	if ((props == null) ||
	    (props.getProperty(UNREMOVABLE_P,
			       "false").equalsIgnoreCase("false")))
	    {
		MouseButtonListener mbl = new MouseButtonListener();
		Panel pan = new Panel(new BorderLayout());
		Button deleteb = new Button(DELETE_L);
		deleteb.addActionListener(bbl);
		deleteb.addMouseListener(mbl);
		pan.add("West", deleteb);
		
		message = new Label("", Label.CENTER);
		message.setBackground(Color.gray);
		message.setForeground(Color.white);

		BorderPanel pmsg = new BorderPanel(BorderPanel.IN, 2);
		pmsg.setLayout(new BorderLayout());
		pmsg.add(message);
		pan.add("Center", pmsg);

		if (rr.isDirectoryResource()) {
		    Button reindexb = new Button(REINDEX_L);
		    reindexb.addActionListener(bbl);
		    reindexb.addMouseListener(mbl);
		    pan.add("East", reindexb);
		}
		//target.setVisible(false);
		ptarget.add("South", pan);
	    }
	if(rh != null) {
	    //buttonp = new Panel(new GridLayout(1, rh.length+1));
	    buttonp = new BorderPanel(BorderPanel.OUT, 1);
	    buttonp.setLayout(new GridLayout(1, rh.length+1));
	    b = new Button[rh.length];
	    for(int i=0; i<rh.length; i++) {
		b[i] = new Button(rh[i].getTitle());
		buttonp.add(b[i]);
		b[i].addActionListener(bbl);
		if (rh[i].getTitle().equalsIgnoreCase("shortcut"))
		    num = i;
	    }
	    editorp = new Panel(new BorderLayout());
	    //target.setVisible(false);
	    ptarget.add("North", buttonp);
	    ptarget.add("Center", editorp);
	    target.add("Center", ptarget);
	    target.validate();
	    target.setVisible(true);
	    switchHelper(b[num]);
	} else {
	    target.add("Center", ptarget);
	    target.validate();
	    target.setVisible(true);
	}
    }

    public ResourceEditor(Panel p) {
	target = p;
    }
}


