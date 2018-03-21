// Message.java
// $Id: Message.java,v 1.1 2010/06/15 12:21:49 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui; 

import java.awt.Component;

import javax.swing.JOptionPane;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * Messages.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Message {

    /**
     * Show an error message
     * @param parent The parent Component
     * @param message The message to show
     * @param title The dialog title
     */
    public static void showErrorMessage(Component parent,
					String message,
					String title)
    {
	JOptionPane.showMessageDialog(parent, message, 
				      title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show an error message
     * @param rrw the RemoteResourceWrapper associated to this message
     * @param message The message to show
     * @param title The dialog title
     */
    public static void showErrorMessage(RemoteResourceWrapper rrw,
					String message,
					String title)
    {
	showErrorMessage(rrw.getServerBrowser(), message, title);

    }

    /**
     * Show an error message
     * @param parent The parent Component
     * @param ex the catched exception
     * @param title The dialog title
     */
    public static void showErrorMessage(Component parent,
					Exception ex,
					String title)
    {
	showErrorMessage(parent, ex.getMessage(), title);
    }

    /**
     * Show an error message
     * @param rrw the RemoteResourceWrapper associated to this message
     * @param ex the catched exception
     * @param title The dialog title
     */
    public static void showErrorMessage(RemoteResourceWrapper rrw,
					Exception ex,
					String title)
    {
	showErrorMessage(rrw.getServerBrowser(), ex.getMessage(), title);
    }

    /**
     * Show an error message
     * @param parent The parent Component
     * @param ex the catched exception
     */
    public static void showErrorMessage(Component parent,
					RemoteAccessException ex)
    {
	showErrorMessage(parent, ex, "Remote Access Error");
    }

    /**
     * Show an error message
     * @param rrw the RemoteResourceWrapper associated to this message
     * @param ex the catched exception
     */
    public static void showErrorMessage(RemoteResourceWrapper rrw,
					RemoteAccessException ex)
    {
	showErrorMessage(rrw.getServerBrowser(), ex, "Remote Access Error");
    }

    /**
     * Show an error message
     * @param parent The parent Component
     * @param ex the catched exception
     */
    public static void showErrorMessage(Component parent,
					Exception ex)
    {
	showErrorMessage(parent, ex, ex.getClass().getName());
    }

    /**
     * Show an error message
     * @param rrw the RemoteResourceWrapper associated to this message
     * @param ex the catched exception
     */
    public static void showErrorMessage(RemoteResourceWrapper rrw,
					Exception ex)
    {
	showErrorMessage(rrw.getServerBrowser(), ex, ex.getClass().getName());
    }

    /**
     * Show a message
     * @param parent The parent Component
     * @param message The message to show
     * @param title The dialog title
     */
    public static void showInformationMessage(Component parent,
					      String message,
					      String title)
    {
	
	JOptionPane.showMessageDialog(parent, message, 
				      title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show a message
     * @param rrw the RemoteResourceWrapper associated to this message
     * @param message The message to show
     * @param title The dialog title
     */
    public static void showInformationMessage(RemoteResourceWrapper rrw,
					      String message,
					      String title)
    {
	showInformationMessage(rrw.getServerBrowser(), message, title);
    }

    /**
     * Show a message
     * @param parent The parent Component
     * @param message The message to show
     */
    public static void showInformationMessage(Component parent,
					      String message)
    {
	JOptionPane.showMessageDialog(parent, message);
    }

}
