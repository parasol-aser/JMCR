// RemoteFrameWrapperNode.java
// $Id: RemoteFrameWrapperNode.java,v 1.1 2010/06/15 12:25:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * The TreeNode for Frames
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class RemoteFrameWrapperNode extends RemoteResourceWrapperNode {

    /**
     * Get the pretty ResourceFrame name
     * @param frame The ResourceFrame
     * @param name The ResourceFrame name
     * @exception RemoteAccessException if a remote access error occurs.
     */
    protected static String getFrameName(RemoteResource frame, String name) 
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
    protected static String getFrameName(RemoteResource frame) 
	throws RemoteAccessException
    {
	String className = frame.getClassHierarchy()[0];
	String shortName = className.substring(className.lastIndexOf('.') + 1);
	String frameName = (String) frame.getValue("identifier");
	return shortName.concat(" (").concat(frameName).concat(")");
    }

    /**
     * Load the children of this node.
     */
    protected synchronized void loadChildren() {
	RemoteResource frames[] = null;

	children = new Vector();

	try {
	    if (rrw.getResource().isFramed()) 
		frames = rrw.getResource().getFrames();
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(rrw, ex);
	}
	if (frames == null)
	    return;
	for(int i = 0; i < frames.length; i++) {
	    RemoteResourceWrapper rrwf = 
		new RemoteResourceWrapper(rrw, frames[i]);
	    try {
		RemoteFrameWrapperNode node =
		    new RemoteFrameWrapperNode(this, rrwf, 
					       getFrameName(frames[i]));
		children.add(node);
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(rrw, ex);
	    }
	}
    }

    /**
     * Returns true if this node is allowed to have children.
     * @return true if this node allows children, else false
     */
    public boolean getAllowsChildren() {
	try {
	    return (rrw.getResource().isFramed());
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(rrw, ex);
	}
	return false;
    }

    /**
     * Constructor
     * @param parent The parent node
     * @param rrw The associated RemoteResourceWrapper
     * @param name The name of this node
     */
    protected RemoteFrameWrapperNode(RemoteResourceWrapperNode parent,
				     RemoteResourceWrapper rrw,
				     String name)
    {
	super(parent, rrw, name);
    }

    /**
     * Constructor
     * @param parent The parent node
     * @param rrw The associated RemoteResourceWrapper
     */
    protected RemoteFrameWrapperNode(RemoteResourceWrapperNode parent,
				     RemoteResourceWrapper rrw)
	throws RemoteAccessException
    {
	super(parent, rrw, getFrameName(rrw.getResource()));
    }

    /**
     * Constructor
     * @param rrw The associated RemoteResourceWrapper
     * @param name The name of this node
     */
    protected RemoteFrameWrapperNode(RemoteResourceWrapper rrw,
				     String name)
    {
	super(rrw, name);
    }

   

}
