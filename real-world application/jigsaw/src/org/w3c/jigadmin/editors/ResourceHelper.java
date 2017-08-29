// ResourceHelper.java
// $Id: ResourceHelper.java,v 1.1 2010/06/15 12:25:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors ;

import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

import org.w3c.jigadmin.gui.Message;

abstract public class ResourceHelper 
    extends org.w3c.jigadm.editors.ResourceHelper 
{

    /**
     * Show an error message.
     * @param msg the error message.
     * @param ex an Exception instance.
     */
    protected void errorPopup(String msg, Exception ex) {
	Message.showErrorMessage(getComponent(), ex, msg);
    }

    /**
     * Show a message.
     * @param msg the message.
     */
    protected void msgPopup(String msg) {
	Message.showInformationMessage(getComponent(), msg);
    }

}
