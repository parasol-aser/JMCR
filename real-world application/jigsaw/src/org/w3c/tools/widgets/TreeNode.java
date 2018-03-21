// TreeNode.java
// $Id: TreeNode.java,v 1.1 2010/06/15 12:20:36 smhuang Exp $
// Author: Jean-Michel.Leon@sophia.inria.fr
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Image;

/**
 * The representation of a node of a TreeBrowser.
 *
 * A TreeNode is used internally by the TreeBrowser to store informations
 * related to a node.
 *
 * It is also given as parameter in the notifications the TreeBrowser send to
 * handlers.
 *
 * @see org.w3c.tools.widgets.TreeBrowser
 * @see org.w3c.tools.widgets.NodeHandler
 */
public class TreeNode {

     public static final int NOCHILD = -1;

    Object item;
    String label ;
    Image icon;
    NodeHandler handler = null ;
    int level;
    int children = NOCHILD;
    boolean selected = false;

   
    TreeNode(Object item, String label,
	     NodeHandler handler, Image icon, int level) {
	this.item = item;
	this.label = label;
	this.icon = icon;
	this.level = level;
	this.handler = handler;
    }

   
   /**
    * Gets the item.
    */   
    public Object getItem() {
	return item;
    }

   /**
    * Gets the label.
    *
    * @see #setLabel
    */    
    public String getLabel() {
	return label;
    }

   /**
    * Gets the current Icon.
    *
    * @see #setIcon
    */
    public Image getIcon() {
	return icon;
    }

   /**
    * Gets the handler.
    */
    public NodeHandler getHandler() {
	return handler;
    }

   /**
    * Gets the children
    */
    public int getChildren() {
 	return children;
    }

   /**
    * Checks if the Node is selected.
    */
    public boolean isSelected() {
	return selected;
    }

   /**
    * Sets the icon.
    *
    * @see #getIcon
    */
    public void setIcon(Image i) {
	icon = i;
    }

   /**
    * Sets the label.
    *
    * @see #getLabel
    */
    public void setLabel(String l) {
	label = l;
    }

    /**
     * Sets the children
     *
     * @see #getChildren
     */
     public void setChildren(int children) {
	this.children = children;
     }
}
