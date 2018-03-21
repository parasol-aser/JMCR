// RemoteResourceWrapperNode.java
// $Id: RemoteResourceWrapperNode.java,v 1.1 2010/06/15 12:25:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors; 

import java.awt.Cursor;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.MutableTreeNode;

import java.util.Enumeration;
import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.sorter.Sorter;

import org.w3c.util.ArrayEnumeration;

/**
 * The TreeNode for Resources
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class RemoteResourceWrapperNode 
    implements MutableTreeNode, RemoteNode
{

    protected RemoteResourceWrapper     rrw        = null;
    protected RemoteResourceWrapperNode parent     = null;
    protected String                    name       = null;
    protected Vector                    children   = null;

    /**
     * Load the children of this node.
     */
    protected synchronized void loadChildren() {
	String                names[] = null;
	RemoteResourceWrapper child   = null;
	
	children = new Vector();

	try {
	    names = rrw.getResource().enumerateResourceIdentifiers();
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(rrw, ex);
	}
	if (names == null)
	    return;
	Sorter.sortStringArray(names, true);
	for (int i = 0 ; i < names.length ; i++) {
	    try {
		child = rrw.getChildResource(names[i]);
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(rrw, ex, names[i]);
	    }
	    if (child != null) {
		RemoteResourceWrapperNode node = 
		    new RemoteResourceWrapperNode(this, child, names[i]);
		children.add(node);
	    }
	}
    }

    //RemoteNode part
    /**
     * Invoked whenever this node is about to be expanded.
     */    
    public void nodeWillExpand() {
	children = null;
    }

    /**
     * Invoked whenever this node is about to be collapsed.
     */
    public void nodeWillCollapse() {
	//children = null;
    }

    /**
     * Get the associated RemoteResourceWrapper.
     * @return the associated RemoteResourceWrapper
     */
    public RemoteResourceWrapper getResourceWrapper() {
	return rrw;
    }

    /**
     * Load the children if needed.
     */
    protected void acquireChildren() {
	rrw.getServerBrowser().setCursor(Cursor.WAIT_CURSOR);
	if (children == null)
	    loadChildren();
	rrw.getServerBrowser().setCursor(Cursor.DEFAULT_CURSOR);
    }

    //TreeNode part

    /**
     * Returns the child TreeNode at index childIndex.
     * @param childIndex the index of the child to return
     * @return a TreeNode instance
     */
    public TreeNode getChildAt(int childIndex) {
	acquireChildren();
	return (TreeNode)children.elementAt(childIndex);
    }

    /**
     * Returns the number of children TreeNodes the receiver contains.
     * @return the number of children TreeNodes the receiver contains
     */
    public int getChildCount() {
	acquireChildren();
	return children.size();
    }

    /**
     * Returns the parent TreeNode of the receiver.
     * @return a TreeNode
     */
    public TreeNode getParent() {
	return parent;
    }

    /**
     * Returns the index of node in the receivers children. If the receiver 
     * does not contain node, -1 will be returned.
     * @return an int.
     */
    public int getIndex(TreeNode node) {
	acquireChildren();
	return children.indexOf(node);
    }

    /**
     * Returns true if the receiver allows children.
     * @return an int.
     */
    public boolean getAllowsChildren() {
	try {
	    return rrw.getResource().isContainer();
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(rrw, ex);
	}
	return false;
    }

    /**
     * Returns true if the receiver is a leaf.
     * @return a boolean
     */ 
    public boolean isLeaf() {
	return (! getAllowsChildren());
    }

    /**
     * Returns the children of the reciever as an Enumeration.
     * @return an Enumeration
     */
    public Enumeration children() {
	acquireChildren();
	return children.elements();
    }

    //MutableTreeNode part

    /**
     * Adds child to the receiver at index. child will be messaged
     * with setParent.
     * @param child the child to add.
     * @param index the index of the new child.
     */
    public void insert(MutableTreeNode child, int index) {
	acquireChildren();
	children.insertElementAt(child, index);
    }

    /**
     * Removes the child at index from the receiver.
     * @param the index of the child to remove.
     */
    public void remove(int index) {
	acquireChildren();
	children.remove(index);
    }

    /**
     * Removes node from the receiver. setParent will be messaged on node
     * @param node the node to remove
     */
    public void remove(MutableTreeNode node) {
	children.remove(node);
    }

    /**
     * Resets the user object of the receiver to object.
     * @param object the new user object, actually the new identifier.
     */    
    public void setUserObject(Object object) {
	if (object instanceof String) {
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    if (pm.isEditable(rrw)) {
		try {
		    rrw.getResource().setValue("identifier", (String)object);
		    name = (String)object;
		} catch (RemoteAccessException ex) {
		    Message.showErrorMessage(rrw, ex);
		}
	    }
	}
    }

    /**
     * Removes the receiver from its parent.
     */
    public void removeFromParent() {
	if (parent != null)
	    parent.remove(this);
    }

    /**
     * Sets the parent of the receiver to newParent.
     * @param newParent the new parent.
     */
    public void setParent(MutableTreeNode newParent) {
	this.parent = (RemoteResourceWrapperNode)newParent;
    }

    /**
     * Return the string reoresentation of this node.
     * @return its name
     */
    public String toString() {
	return name;
    }

    /**
     * Constructor
     * @param parent The parent node
     * @param rrw The associated RemoteResourceWrapper
     * @param name The name of this node
     */
    protected RemoteResourceWrapperNode(RemoteResourceWrapperNode parent,
					RemoteResourceWrapper rrw,
					String name) 
    {
	this.parent = parent;
	this.name   = name;
	this.rrw    = rrw;

    }

    /**
     * Constructor
     * @param rrw The associated RemoteResourceWrapper
     * @param name The name of this node
     */
    protected RemoteResourceWrapperNode(RemoteResourceWrapper rrw, 
					String name)
    {
	this.rrw   = rrw;
	this.name  = name;
    }

}
