// ControlServerHelper.java
// $Id: ControlServerHelper.java,v 1.1 2010/06/15 12:25:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Cursor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.JButton;

import java.util.Properties;
import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.events.ResourceActionEvent;
import org.w3c.jigadmin.events.ResourceActionSource;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigadmin.widgets.Icons;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.widgets.Utilities;

/**
 * The server helper for the control resource.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ControlServerHelper extends JToolBar 
    implements ServerHelperInterface, ResourceActionSource
{
    protected RemoteResourceWrapper control = null;

    protected Vector listeners = null;

    protected String name    = null;
    protected String tooltip = null;

    protected final static String SAVE_A = "save";
    protected final static String STOP_A = "stop";
    protected final static String HELP_A = "help";
    protected final static String DELE_A = "del";
    protected final static String ADD_A  = "add";
    protected final static String REIN_A = "reindx";
    protected final static String REFE_A = "reference";
    protected final static String EDIT_A = "edit";

    protected String saveBTT = null;
    protected String stopBTT = null;
    protected String helpBTT = null;
    protected String deleBTT = null;
    protected String addBTT  = null;
    protected String reinBTT = null;
    protected String refeBTT = null;
    protected String editBTT = null;

    boolean res_op_enabled = true;
    boolean built          = false;

    /**
     * Our internal ActionListener.
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
	    String command = ae.getActionCommand();
	    control.getServerBrowser().setCursor(Cursor.WAIT_CURSOR);
	    if (command.equals(HELP_A)) {
		try {
		    String url = (String)
		    control.getResource().getValue("help-url");
		    MiniBrowser.showDocumentationURL(url, "Documentation");
		} catch (RemoteAccessException ex) {
		    ex.printStackTrace();
		} catch (Exception ex) {
		}
	    } else if (command.equals(DELE_A)) {
		fireResourceEvent(ResourceActionEvent.DELETE_EVENT);
	    } else if (command.equals(ADD_A)) {
		fireResourceEvent(ResourceActionEvent.ADD_EVENT);
	    } else if (command.equals(STOP_A)) {
		fireResourceEvent(ResourceActionEvent.STOP_EVENT);
		try {
		    control.getChildResource(ae.getActionCommand());
		} catch (RemoteAccessException ex) {
		    Message.showErrorMessage(ControlServerHelper.this, ex);
		}
	    } else if (command.equals(SAVE_A)) {
		fireResourceEvent(ResourceActionEvent.SAVE_EVENT);
		try {
		    control.getChildResource(ae.getActionCommand());
		} catch (RemoteAccessException ex) {
		    Message.showErrorMessage(ControlServerHelper.this, ex);
		}
	    } else if (command.equals(REIN_A)) {
		fireResourceEvent(ResourceActionEvent.REINDEX_EVENT);
	    } else if (command.equals(REFE_A)) {
		fireResourceEvent(ResourceActionEvent.REFERENCE_EVENT);
	    } else if (command.equals(EDIT_A)) {
		fireResourceEvent(ResourceActionEvent.EDIT_EVENT);
	    }
	    control.getServerBrowser().setCursor(Cursor.DEFAULT_CURSOR);
	}
    };

    /**
     * Add a ResourceActionListener.
     * @param listener the ResourceActionListener to add
     */    
    public void addResourceActionListener(ResourceActionListener listener) {
	if (listeners == null)
	    listeners = new Vector(1);
	listeners.addElement(listener);
    }

    /**
     * Remove a ResourceActionListener.
     * @param listener the ResourceActionListener to remove
     */
    public void removeResourceActionListener(ResourceActionListener listener) {
	if (listeners == null)
	    return;
       listeners.removeElement(listener);
    }

    /**
     * Fire a resource event.
     * @param type the resource event type
     * @see org.w3c.jigadmin.event.ResourceActionEvent
     */
    protected void fireResourceEvent(int type) {
	if (listeners == null)
	    return;
	ResourceActionEvent ev = 
	    new ResourceActionEvent(this, type);
	for (int i = 0; i < listeners.size(); i++) 
	    ((ResourceActionListener)
	     listeners.elementAt(i)).resourceActionPerformed(ev);
    }

    /**
     * Enable or disable the resource operations.
     * @param onoff a boolean.
     */
    public void setResOpEnabled(boolean onoff) {
	res_op_enabled = onoff;
    }

    /**
     * Set the tooltip for the save button.
     * @param tooltip the tooltip
     */
    public void setSaveToolTipText(String tooltip) {
	saveBTT = tooltip;
    }

    /**
     * Set the tooltip for the stop button.
     * @param tooltip the tooltip
     */
    public void setStopToolTipText(String tooltip) {
	stopBTT = tooltip;
    }

    /**
     * Set the tooltip for the help button.
     * @param tooltip the tooltip
     */
    public void setHelpToolTipText(String tooltip) {
	helpBTT = tooltip;
    }

    /**
     * Set the tooltip for the delete button.
     * @param tooltip the tooltip
     */
    public void setDeleteToolTipText(String tooltip) {
	deleBTT = tooltip;
    }

    /**
     * Set the tooltip for the add button.
     * @param tooltip the tooltip
     */
    public void setAddToolTipText(String tooltip) {
	addBTT = tooltip;
    }

    /**
     * Set the tooltip for the reindex button.
     * @param tooltip the tooltip
     */
    public void setReindexToolTipText(String tooltip) {
	reinBTT = tooltip;
    }

    /**
     * Set the tooltip for the information button.
     * @param tooltip the tooltip
     */
    public void setReferenceToolTipText(String tooltip) {
	refeBTT = tooltip;
    }

    /**
     * Set the tooltip for the edit button.
     * @param tooltip the tooltip
     */
    public void setEditToolTipText(String tooltip) {
	editBTT = tooltip;
    }

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */ 
    public void initialize(String name, 
			   RemoteResourceWrapper rrw, 
			   Properties p)
    {
	this.control = rrw;
	this.name    = name;
	this.tooltip = (String) p.get(TOOLTIP_P);
    }

    /**
     * Build the interface.
     */
    protected void build() {
	JButton saveB = (JButton) add( new JButton(Icons.saveIcon) );
	if (saveBTT == null)
	    saveBTT = "Save the configuration";
	saveB.setToolTipText(saveBTT);
	saveB.setMargin(Utilities.insets0);
	saveB.setActionCommand(SAVE_A);
	saveB.addActionListener(al);

	addSeparator(Utilities.dim3_3);

	JButton stopB = (JButton) add( new JButton(Icons.stopIcon) );
	if (stopBTT == null)
	    stopBTT = "Stop the server";
	stopB.setToolTipText(stopBTT);
	stopB.setMargin(Utilities.insets0);
	stopB.setActionCommand(STOP_A);
	stopB.addActionListener(al);

	if (res_op_enabled) {
	    addSeparator(Utilities.dim10_10);
	    JButton reinB = (JButton) add( new JButton(Icons.reindexIcon) );
	    if (reinBTT == null)
		reinBTT = "Reindex children of selected Container(s)";
	    reinB.setToolTipText(reinBTT);
	    reinB.setMargin(Utilities.insets0);
	    reinB.setActionCommand(REIN_A);
	    reinB.addActionListener(al);

	    addSeparator(Utilities.dim3_3);

	    JButton addB = (JButton) add( new JButton(Icons.addIcon) );
	    if (addBTT == null)
		addBTT = "Add a resource to the selected Container";
	    addB.setToolTipText(addBTT);
	    addB.setMargin(Utilities.insets0);
	    addB.setActionCommand(ADD_A);
	    addB.addActionListener(al);

	    addSeparator(Utilities.dim3_3);

	    JButton deleB = (JButton) add( new JButton(Icons.deleteIcon) );
	    if (deleBTT == null)
		deleBTT = "Delete selected resources";
	    deleB.setToolTipText(deleBTT);
	    deleB.setMargin(Utilities.insets0);
	    deleB.setActionCommand(DELE_A);
	    deleB.addActionListener(al);

	    addSeparator(Utilities.dim3_3);

	    JButton editB = (JButton) add( new JButton(Icons.editIcon) );
	    if (editBTT == null)
		editBTT = "Edit selected resource";
	    editB.setToolTipText(editBTT);
	    editB.setMargin(Utilities.insets0);
	    editB.setActionCommand(EDIT_A);
	    editB.addActionListener(al);

	    addSeparator(Utilities.dim10_10);

	    JButton refeB = (JButton) add( new JButton(Icons.infoIcon) );
	    if (refeBTT == null)
		refeBTT = "Show reference documentation of selected resource";
	    refeB.setToolTipText(refeBTT);
	    refeB.setMargin(Utilities.insets0);
	    refeB.setActionCommand(REFE_A);
	    refeB.addActionListener(al);

	    addSeparator(Utilities.dim3_3);

	    JButton helpB = (JButton) add( new JButton(Icons.helpIcon) );
	    if (helpBTT == null)
		helpBTT = "Show documentation";
	    helpB.setToolTipText(helpBTT);
	    helpB.setMargin(Utilities.insets0);
	    helpB.setActionCommand(HELP_A);
	    helpB.addActionListener(al);
	}

	putClientProperty( "JToolBar.isRollover", Boolean.FALSE );
	built = true;
    }

    /**
     * Get the helper name.
     * @return a String instance
     */
    public String getName() {
	return name;
    }

    /**
     * Get the helper tooltip
     * @return a String
     */      
    public String getToolTip() {
	return tooltip;
    }

    /**
     * Get the helper Component
     * @return a Component instance
     */  
    public Component getComponent() {
	if (! built)
	    build();
	return this;
    }

    /**
     * Constructor.
     */
    public ControlServerHelper() {
	super(HORIZONTAL);
	//new instance
    }

}
