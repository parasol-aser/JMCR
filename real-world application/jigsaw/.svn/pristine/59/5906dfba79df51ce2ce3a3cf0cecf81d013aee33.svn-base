// NodeHandler.java
// $Id: NodeHandler.java,v 1.1 2010/06/15 12:20:35 smhuang Exp $
// Author: Jean-Michel.Leon@sophia.inria.fr
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

/**
 * The interface to be implemented by nodes.
 * What is a node is application dependent, however, the informations the
 * browser needs in order to be able do display nodes are obtained through 
 * this interface.
 *
 * @see TreeBrowser
 */

public interface NodeHandler {
	
   /**
    * Notifies that a node has to be selected.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifySelect(TreeBrowser browser, TreeNode node) ;
	
   /**
    * Notifies that a node has to be expanded.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyExpand(TreeBrowser browser, TreeNode node) ;

   /**
    * Notifies that a node has to be collapsed.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyCollapse(TreeBrowser browser, TreeNode node) ;

   /**
    * Notifies that a node has to be executed.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public void notifyExecute(TreeBrowser browser, TreeNode node) ;

   /**
    * Checks if the node is a directory.
    *
    * @param browser the TreeBrowser sending the notification.
    */
    public boolean isDirectory(TreeBrowser browser, TreeNode node) ;

}


