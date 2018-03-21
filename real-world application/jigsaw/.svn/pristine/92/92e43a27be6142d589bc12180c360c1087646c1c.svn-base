// ControlHelper.java
// $Id: ControlHelper.java,v 1.1 2010/06/15 12:22:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EventObject;
import java.util.Properties;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.ImageButton;

public class ControlHelper extends ResourceHelper {

    class ControlListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
	    if(ae.getSource() instanceof ImageButton) {
		executeAction(ae.getActionCommand());
	    }
	}
    }

    class MouseButtonListener extends MouseAdapter {
	
	public void mouseEntered(MouseEvent e) {
	    Component comp = e.getComponent();
	    if (comp instanceof ImageButton) {
		String action = ((ImageButton)comp).getActionCommand();

		if (action.equals(CHECKPOINT_L)) {
		    setMessage("Start the Checkpoint resource.");
		} else if (action.equals(SAVE_L)) {
		    setMessage("Save the current configuration.");
		} else if (action.equals(STOP_L)) {
		    setMessage("Stop the server.");
		} else if (action.equals(RESTART_L)) {
		    setMessage("Restart the server "+
			       "(doesn't work yet).");
		}
	    }
	}

	public void mouseExited(MouseEvent e) {
	    setMessage("");
	}

    }

    protected static final String CHECKPOINT_L = "Checkpoint";
    protected static final String SAVE_L       = "Save";
    protected static final String STOP_L       = "Stop";
    protected static final String RESTART_L    = "Restart";

    private   RemoteResourceWrapper rrw          = null;
    private   boolean               initialized  = false;
    protected Panel                 widget       = null;
    protected Label                 controlLabel = null;

    public void commitChanges() {
    }

    public boolean hasChanged() {
	return false;
    }

    public void resetChanges() {
    }

    public void clearChanged() {
    }

    public Component getComponent() {
	return widget;
    }

    public final String getTitle() {
	return "Control";
    }

    public void setMessage(String msg) {
	controlLabel.setText(msg);
    }

    public ControlHelper() {
	widget = new BorderPanel(BorderPanel.LOWERED,2);
	widget.setLayout(new BorderLayout());
    }

    protected void executeAction(String action) {
	try {
	    setMessage(action+" ...");
	    rrw.getResource().loadResource(action);
	    setMessage(action+" done.");
	} catch (Exception ex) {
	    ex.printStackTrace();
	    errorPopup("Error",ex);
	}
    }

    protected Image getIcon(PropertyManager pm, String name) {
	return Toolkit.getDefaultToolkit().getImage(pm.getIconLocation(name));
    }

    public void initControlPanel() {
	Image checkpoint_im, save_im, stop_im, restart_im;
	ImageButton ib;
	Panel tfp;
	Label l;
	MouseButtonListener mbl = new MouseButtonListener();
	ControlListener cl = new ControlListener();
	Panel ControlPanel = new Panel(new BorderLayout());
	PropertyManager pm = PropertyManager.getPropertyManager();
	ScrollPane fsp = new ScrollPane();
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	Panel fspp = new Panel(gbl);
	gbc.insets = new Insets(5,0,0,0);
	fsp.add(fspp);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;

	checkpoint_im = getIcon(pm, "checkpoint");
	save_im       = getIcon(pm, "save");
	stop_im       = getIcon(pm, "stop");
	restart_im    = getIcon(pm, "restart");

	// Checkpoint
	ib = new ImageButton(checkpoint_im, CHECKPOINT_L);
	ib.addActionListener(cl);
	ib.addMouseListener(mbl);
	gbc.gridwidth = 1;
	gbl.setConstraints(ib, gbc);
	fspp.add(ib);    
	l = new Label(CHECKPOINT_L);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(l, gbc);
	fspp.add(l);

	//Save
	ib = new ImageButton(save_im, SAVE_L);
	ib.addActionListener(cl);
	ib.addMouseListener(mbl);
	gbc.gridwidth = 1;
	gbl.setConstraints(ib, gbc);
	fspp.add(ib);
		
	l = new Label(SAVE_L);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(l, gbc);
	fspp.add(l);

	//Stop
	ib = new ImageButton(stop_im, STOP_L);
	ib.addActionListener(cl);
	ib.addMouseListener(mbl);
	gbc.gridwidth = 1;
	gbl.setConstraints(ib, gbc);
	fspp.add(ib);
		
	l = new Label(STOP_L);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(l, gbc);
	fspp.add(l);

	//Restart
	ib = new ImageButton(restart_im, RESTART_L);
	ib.addActionListener(cl);
	ib.addMouseListener(mbl);
	gbc.gridwidth = 1;
	gbl.setConstraints(ib, gbc);
	fspp.add(ib);
		
	l = new Label(RESTART_L);
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(l, gbc);
	fspp.add(l);
	    
	ControlPanel.add("Center", fspp);
	widget.add("Center", ControlPanel);
	controlLabel = new Label("", Label.CENTER);
	controlLabel.setBackground(Color.gray);
	controlLabel.setForeground(Color.white);
	BorderPanel bpcl = new BorderPanel(BorderPanel.IN,2);
	bpcl.setLayout(new BorderLayout());
	bpcl.add(controlLabel, "Center");
	widget.add("South", bpcl);
	widget.validate();
	widget.setVisible(true);
    }

	

    /**
     * initialize the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties pr)
	throws RemoteAccessException
    {
	this.rrw = rrw;
	if(!initialized)
	    initialized = true;
	else
	    return;
	initControlPanel();
    }
}


