// FrameBrowser.java
// $Id: FrameBrowser.java,v 1.1 2010/06/15 12:22:41 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;

import java.io.PrintStream;

import java.net.URL;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.gui.ServerBrowser;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.widgets.MessagePopup;
import org.w3c.tools.widgets.NodeHandler;
import org.w3c.tools.widgets.TreeBrowser;
import org.w3c.tools.widgets.TreeNode;

public class FrameBrowser extends TreeBrowser implements NodeHandler {

    class Expander extends Thread {
	TreeBrowser browser;
	TreeNode nd;

	public void run() {
	    if(getLock()) {
		notifyExpander(browser, nd);
		unlock();
	    }
	}

	Expander(TreeBrowser browser, TreeNode nd) {
	    this.browser = browser;
	    this.nd = nd;
	}
    }

    public static final boolean debug = false;
    Image frameopened = null;
    Image frameicon = null;
    ServerBrowser serverBrowser = null;
    FrameTreeListener tl = null;
    TreeNode lastn = null;
    RemoteResourceWrapper rootResource;
    boolean locked;

    protected void errorPopup(String name, Exception ex) {
	(new MessagePopup(name+" : "+ex.getMessage())).show();
    }

    private Dimension preferredSize = null;

    public Dimension getPreferredSize() {
	if (preferredSize == null) {
	    preferredSize = new Dimension(120,100);
	}      
	return preferredSize;
    }

    private final Frame getFrame(Component c) {
	while(! (c instanceof Frame)) {
	    c = c.getParent();
	    if (c == null)
		return null;
	}
	return (Frame)c;
    }

    public void setCursor(int cursor) {
	getFrame(serverBrowser).setCursor(new Cursor(cursor));
	Toolkit.getDefaultToolkit().sync();
    }

    /**
     * gets a lock to avoid adding node while removing other nodes
     * it sets also the Cursor to WAIT_CURSOR
     */

    protected synchronized boolean getLock() {
	if(locked)
	    return false;
	setCursor(Frame.WAIT_CURSOR);
	locked = true;
	return true;
    }

    /**
     * release the lock and sets the Cursor to the default
     */

    protected synchronized void unlock() {
	locked = false;
	setCursor(Frame.DEFAULT_CURSOR);
    }

    /**
     * give the Root Resource of the browser
     */

    public RemoteResourceWrapper getRootWrapper() {
	return rootResource;
    }

    public void renameNode(RemoteResourceWrapper rw, String label) {
	TreeNode tn = getNode(rw);
	// if it is a visible node, change the label and repaint
	if (tn != null) {
	    try {
		if (rw != rootResource)
		    label = getFrameName(rw.getResource(), label);
	    } catch (RemoteAccessException ex) {
		//nothing
	    }
	    tn.setLabel(label);
	    repaint();
	}
    }

    public void removeNode(RemoteResourceWrapper rw) {
	if(getLock()) {
	    if (getNode(rw) != null) {
		removeBranch(getNode(rw));
		tl.nodeRemoved(rw);
	    }
	    unlock();
	    repaint();
	}
    }

    public void insertNode(RemoteResourceWrapper father, 
			   RemoteResourceWrapper son)
    {
	RemoteResource newFrame = son.getResource();
	String name = null;
	try {
	    name = getFrameName(newFrame);
	} catch (RemoteAccessException ex) {
	    // fancy thing
	    errorPopup("RemoteAccessException",ex);
	}
	insertNode(father, son, name);
    }  

    public void insertNode(RemoteResourceWrapper father, 
			   RemoteResourceWrapper son,
			   String name) {
	TreeNode fatherNode;
	boolean ic = false;

	if(father == null)
	    System.out.println("Error null father");
	fatherNode = getNode(father);
	if(fatherNode.getChildren() == TreeNode.NOCHILD)
	    return;
	
	if (fatherNode == null)
	    return; // this should never happen, but...
	try {
	    ic = son.getResource().isContainer();
	} catch (RemoteAccessException ex) {
	    // fancy thing
	    errorPopup("RemoteAccessException",ex);
	}
	insert(fatherNode, son, this, name, frameicon);
	repaint();
    }

    protected RemoteResourceWrapper getResources(RemoteResourceWrapper rw,
						 String name) 
    {
	RemoteResource resource = null;
	if(rw != null) {
	    try {
		resource = rw.getResource().loadResource(name);
	    } catch (RemoteAccessException ex) {
		errorPopup("RemoteAccessException", ex);
		ex.printStackTrace();
	    }
	}
	return new RemoteResourceWrapper(rw, resource, serverBrowser);
    }

    private final Image getImage(String name) {
	Image img;
	img = Toolkit.getDefaultToolkit().getImage(name);
	return img;
    }

    public FrameBrowser(FrameTreeListener tl,
			RemoteResourceWrapper rrw) 
    {
	boolean authorized = false;
	RemoteResource rr = null;
	PropertyManager pm = PropertyManager.getPropertyManager();
	serverBrowser = rrw.getBrowser();
	this.tl = tl;

	locked = false;
	frameicon = getImage(pm.getIconLocation("frame"));
	frameopened = getImage(pm.getIconLocation("frameopened"));
	
	rootResource = rrw;

	String name = null;
	while (!authorized) {
	    try {
		authorized = true;
		rr = rrw.getResource();
		name = (String) rr.getValue("identifier");
	    } catch (RemoteAccessException ex) {
		if( ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    errorPopup("Can't read resource identifier",ex);
		}
	    } finally {
		if(!authorized) {
		    serverBrowser.popupDialog("admin");
		}
	    }
	}
	if (name != null)
	    initialize(rootResource, name, this, frameicon);
    }

    public void notifySelect(TreeBrowser browser, TreeNode nd) {
	if(tl != null) {
	    tl.editedChanged(this, (RemoteResourceWrapper)nd.getItem());
	}
	browser.unselect(lastn);
	browser.select(nd);
	browser.repaint();
	lastn = nd;
    }

    /**
     * Handles Select notifications.
     *
     * we simply select the node and redraw the browser.
     */    
    public void notifyExecute(TreeBrowser browser, TreeNode node) {
	if(tl != null) {
	    tl.focusChanged((RemoteResourceWrapper)node.getItem());
	}
	if(!node.equals(lastn)) {
	    browser.unselect(lastn);
	    lastn = null;
	}
	browser.repaint();
    }

    public void notifyExpand(TreeBrowser browser, TreeNode nd) {
	(new Expander(browser, nd)).start();
    }

    public boolean isDirectory(TreeBrowser browser, TreeNode nd) {
	//FIXME ??
	return true;
    }

    /**
     * Get the pretty ResourceFrame name
     * @param frame The ResourceFrame
     * @param name The ResourceFrame name
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public String getFrameName(RemoteResource frame, String name) 
	throws RemoteAccessException
    {
	String className = frame.getClassHierarchy()[0];
	String shortName = className.substring(className.lastIndexOf('.') + 1);
	return shortName.concat(" (").concat(name).concat(")");
    }

    /**
     * Get the pretty ResourceFrame name
     * @param frame The ResourceFrame
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public String getFrameName(RemoteResource frame) 
	throws RemoteAccessException
    {
	String className = frame.getClassHierarchy()[0];
	String shortName = className.substring(className.lastIndexOf('.') + 1);
	String frameName = (String) frame.getValue("identifier");
	return shortName.concat(" (").concat(frameName).concat(")");
    }

    /**
     * Handles Expand notifications
     *
     * if the node is a directory, we list its content and insert the
     * directories and files in the browser.
     */
    public void notifyExpander(TreeBrowser browser, TreeNode nd) {
	boolean authorized = true; 
	if(tl != null) {
	    tl.focusChanged((RemoteResourceWrapper)nd.getItem());
	}
	RemoteResourceWrapper rrw = null;
	RemoteResource rr = null;
	rrw = (RemoteResourceWrapper)nd.getItem();
	if(rrw == null)
	    return;
	rr = rrw.getResource();
	setCursor(Frame.WAIT_CURSOR);
	try {
	    RemoteResource frames[] = rr.getFrames();
	    if (frames != null) {
		if (debug)
		    System.out.println("Found "+frames.length+" identifiers");
		for(int i = 0; i <frames.length; i++) {
		    RemoteResourceWrapper frw = 
			new RemoteResourceWrapper(rrw, frames[i], serverBrowser);
		    browser.insert(nd, frw, this, getFrameName(frames[i]), frameicon);
		}
	    }
	} catch (RemoteAccessException ex) {
	    if( ex.getMessage().equals("Unauthorized")) {
		authorized = false;
	    } else {
		errorPopup("RemoteAccessException", ex);
		ex.printStackTrace();
	    }
	} finally {
	    if(!authorized) {
		serverBrowser.popupDialog("admin");
	    }
	}
	setCursor(Frame.DEFAULT_CURSOR);
	nd.setIcon(frameopened);
	browser.repaint();
    }

    /**
     * Handles Collapse notifications
     *
     * we simply collapse the given node and repaint the browser.
     */    
    public void notifyCollapse(TreeBrowser browser, TreeNode node) {
	if(getLock()) {
	    if(tl != null) {
		tl.focusChanged((RemoteResourceWrapper)node.getItem());
	    }
	    browser.collapse(node);
	    if(!node.equals(lastn)) {
		browser.unselect(lastn);
		lastn = null;
	    }
	    unlock();
	    browser.repaint();
	    node.setIcon(frameicon);
	}
    }

}
