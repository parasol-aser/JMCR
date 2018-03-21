// RemoteNode.java
// $Id: RemoteNode.java,v 1.1 2010/06/15 12:25:50 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

/**
 * The interface for remote nodes.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface RemoteNode {

    /**
     * Invoked whenever this node is about to be expanded.
     */  
    public void nodeWillExpand();

    /**
     * Invoked whenever this node is about to be collapsed.
     */
    public void nodeWillCollapse();

}
