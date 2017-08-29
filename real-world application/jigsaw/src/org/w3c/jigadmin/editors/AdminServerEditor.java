// AdminServerEditor.java
// $Id: AdminServerEditor.java,v 1.1 2010/06/15 12:25:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import java.util.Properties;
import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigadmin.events.ResourceActionEvent;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.widgets.Utilities;

/**
 * The admin server editor
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AdminServerEditor extends ServerEditor 
    implements ServerEditorInterface, ResourceActionListener
{

    protected final static String CONTROL_NAME = "control";
    protected final static String REALMS_NAME  = "realms";

    private RemoteResource[] controls = null;

    /**
     * initialize the server helpers. There is only one helper for the
     * Admin server, the ControlServerHelper
     * @exception RemoteAccessException if a remote error occurs
     */
    protected void initializeServerHelpers()
	throws RemoteAccessException
    {
	shelpers = new ServerHelperInterface[2]; //control + realms
	//control
	RemoteResourceWrapper rrw = server.getChildResource(CONTROL_NAME);
	shelpers[0] = ServerHelperFactory.getServerHelper(CONTROL_NAME, rrw);
	ControlServerHelper control = (ControlServerHelper) shelpers[0];
	control.setResOpEnabled(false);
	control.setSaveToolTipText("Save all servers configuration");
	control.setStopToolTipText("Stop all servers");
	control.addResourceActionListener(this);
	//realms
	rrw = server.getChildResource(REALMS_NAME);
	shelpers[1] = ServerHelperFactory.getServerHelper(REALMS_NAME, rrw);
    }

    /**
     * Get the control resource of all administrated servers.
     * @return a RemoteResource Array
     * @exception RemoteAccessException if a remote error occurs
     */
    protected RemoteResource[] getControls() 
	throws RemoteAccessException
    {
	if (controls == null) {
	    RemoteResource admin = server.getResource();
	    String names[] = admin.enumerateResourceIdentifiers();
	    Vector vcontrols = new Vector(2);
	    for (int i = 0 ; i < names.length ; i++) {
		if ((! names[i].equals("control")) && 
		    (! names[i].equals("realms"))) {
		    RemoteResource srr = admin.loadResource(names[i]);
		    //load the control node
		    RemoteResource control = srr.loadResource("control");
		    vcontrols.addElement(control);
		}
	    }
	    controls = new RemoteResource[vcontrols.size()];
	    vcontrols.copyInto(controls);
	}
	return controls;
    }

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e) {
	switch (e.getResourceActionCommand()) 
	    {
	    case ResourceActionEvent.SAVE_EVENT:
		try {
		    RemoteResource ctrls[] = getControls();
		    for (int i = 0 ; i < ctrls.length ; i++)
			ctrls[i].loadResource("save");
		} catch (RemoteAccessException ex) {
		    Message.showErrorMessage(server, ex);
		}
		break;
	    case ResourceActionEvent.STOP_EVENT:
		try {
		    RemoteResource ctrls[] = getControls();
		    for (int i = 0 ; i < ctrls.length ; i++)
			ctrls[i].loadResource("stop");
		} catch (RemoteAccessException ex) {
		    Message.showErrorMessage(server, ex);
		}
		break;
	    default:
		//nothing to do
	    }
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
	super.initialize(name, rrw, p);
	//must be built at init time
	setServer(rrw);
    }

    /**
     * Constructor.
     */
    public AdminServerEditor() {
	//for newInstance
    }

}
