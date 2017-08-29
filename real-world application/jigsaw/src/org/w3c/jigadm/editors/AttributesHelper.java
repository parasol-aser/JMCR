// AttributeHelper.java
// $Id: AttributesHelper.java,v 1.1 2010/06/15 12:22:47 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Hashtable;
import java.util.Properties;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.serialization.AttributeDescription;

import org.w3c.tools.widgets.BorderPanel;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigadm.gui.ServerBrowser;

import org.w3c.jigadm.events.ResourceChangeEvent;

public class AttributesHelper extends ResourceHelper {

    class ButtonBarListener implements ActionListener {

	class Commiter extends Thread {
	    public void run() {
		try {
		    commitChanges();
		} catch (RemoteAccessException ex) {
		    errorPopup("RemoteAccessException",ex);
		}
	    }
	}

	public void actionPerformed(ActionEvent ae) {
	    if (ae.getActionCommand().equals("Reset"))
		resetChanges();
	    else if (ae.getActionCommand().equals(COMMIT_L)) {
		setMessage("Committing...");
		(new Commiter()).start();
		setMessage("Commit done.");
	    } 
	}
    }

    class MouseButtonListener extends MouseAdapter {

	public void mouseEntered(MouseEvent e) {
	    Component comp = e.getComponent();
	    if (comp instanceof Button) {
		String action = ((Button)comp).getActionCommand();
		if (action.equals(COMMIT_L)) {
		    setMessage("Commit the changes to the server.");
		} else if (action.equals(RESET_L)) {
		    setMessage("Reset changes");
		}
	    }
	}

	public void mouseExited(MouseEvent e) {
	    setMessage("");
	}
    }

    private RemoteResourceWrapper rrw = null;
    private AttributeDescription[] a = null;
    private AttributeEditor[] ae = null;
    private ScrollPane pwidget;
    private boolean initialized = false;

    protected static final String COMMIT_L = "Commit"; 
    protected static final String RESET_L = "Reset"; 

    Panel widget;
    Label message;

    public void setMessage(String msg) {
	message.setText(msg);
    }

    /**
     * Commit changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void commitChanges()
	throws RemoteAccessException
    {
	if(!initialized)
	    return;

	int num = 0;
        for(int i=0; i<ae.length; i++) {
            if (ae[i].hasChanged())
                num++;
        }
	boolean authorized;
        String s[] = new String[num];
	Object o[] = new Object[num];
        num = 0;
        for(int i=0; i<ae.length; i++) {
            if (ae[i].hasChanged()) {
                s[num] = a[i].getName();
		o[num] = ae[i].getValue();
		if(s[num].equals("identifier")) {
		    // should send an event
		    if(rrw.getBrowser() != null)
			rrw.getBrowser().renameNode(rrw, (String)o[num]);
		    processEvent(new ResourceChangeEvent(rrw,
							 "identifier",
							 null,
							 o[num]));
		}
		num++;
	    }
	}
	authorized = false;
	while(!authorized) {
	    try {
		authorized = true;
		rrw.getResource().setValues(s, o);
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    throw ex;
		}
	    } finally {
		if(!authorized) {
		    rrw.getBrowser().popupDialog("admin");
		}
	    }
	}
	clearChanged();
	// FIXME propagate event
    }
	

    public boolean hasChanged() {
	if(ae == null)
	    return false;
	boolean changed = false;
	for(int i=0; !changed && i<ae.length; i++) {
	    changed = ae[i].hasChanged();
	}
	return changed;
    }

    public void resetChanges() {
	if(ae == null)
	    return;

	for(int i=0; i<ae.length; i++) {
	    if(ae[i].hasChanged())
		ae[i].resetChanges();
	}
    }

    public void clearChanged() {
	if(ae == null)
	    return;
	for(int i=0; i<ae.length; i++) {
	    if (ae[i].hasChanged())
		ae[i].clearChanged();
	}
    }

    public Component getComponent() {
	return widget;
    }

    public final String getTitle() {
	return "Attribute";
    }

    public AttributesHelper() {
	widget = new Panel(new BorderLayout());
    }

    /**
     * initialize.
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper rrw, Properties pr)
	throws RemoteAccessException
    {
	if(initialized)
	    return;

	RemoteResource rr;
	AttributeDescription b[] = null;
	String s[] = null;
	int nbn = 0;
	boolean authorized;
	

	this.rrw = rrw;
	rr = rrw.getResource();
	authorized = false;
	while(!authorized) {
	    try {
		authorized = true;
		b = rr.getAttributes();
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    throw ex;
		}
	    } finally {
		if(!authorized) {
		    rrw.getBrowser().popupDialog("admin");
		}
	    }
	}
	// we select only the editable Attributes.
	for(int i=0; i<b.length; i++)
	    if(b[i] == null)
		nbn++;
	    else
		if(!b[i].getAttribute().checkFlag(Attribute.EDITABLE))
		    nbn++;
	a = new AttributeDescription[b.length-nbn];
	ae = new AttributeEditor[a.length];
	int j = 0;
	for(int i=0; i<b.length; i++) {
	    if(b[i] != null && 
	       b[i].getAttribute().checkFlag(Attribute.EDITABLE)) {
		a[j++] = b[i];
	    }
	}

	// add all the attribute editors

	Label l;
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	Panel p = new Panel(gbl);
	p.setForeground(new Color(0,0,128));
	pwidget = new ScrollPane();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0;
	gbc.weighty = 0;
	for(int i=0; i<a.length; i++) {
	    if(a[i] != null) {
		PropertyManager pm = PropertyManager.getPropertyManager();
		Properties attrProps = 
		    pm.getAttributeProperties(rrw, a[i].getAttribute());
		String labelText = (String) attrProps.get("label");
		if ( labelText == null )
		    labelText = a[i].getName();
		l = new Label(labelText, Label.RIGHT);
		ae[i] = AttributeEditorFactory.getEditor(rrw, 
							 a[i].getAttribute());
		authorized = false;
		while(!authorized) {
		    try {
			authorized = true;
			ae[i].initialize(rrw, a[i].getAttribute(), 
					 a[i].getValue(), attrProps);
		    } catch (RemoteAccessException ex) {
			if(ex.getMessage().equals("Unauthorized")) {
			    authorized = false;
			} else {
			    throw ex;
			}
		    } finally {
			if(!authorized) {
			    rrw.getBrowser().popupDialog("admin");
			}
		    }
		}
		gbc.gridwidth = 1;
		gbl.setConstraints(l, gbc);
		p.add(l);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(ae[i].getComponent(), gbc);
		p.add(ae[i].getComponent());
	    }
	}
	pwidget.add(p);
	widget.add("Center", pwidget);
	// Now add the reset/commit button bar

	Panel  toolpane= new Panel(new BorderLayout());
	Button commitb = new Button(COMMIT_L);
	Button resetb  = new Button(RESET_L);

	MouseButtonListener mbl = new MouseButtonListener();
	commitb.addMouseListener(mbl);
	resetb.addMouseListener(mbl);

	ButtonBarListener bbl = new ButtonBarListener();
	commitb.addActionListener(bbl);
	resetb.addActionListener(bbl);

	message = new Label("", Label.CENTER);
	message.setForeground(Color.white);
	message.setBackground(Color.gray);

	BorderPanel pmsg = new BorderPanel(BorderPanel.IN, 2);
	pmsg.setLayout(new BorderLayout());
	pmsg.add("Center", message);

	toolpane.add("West", commitb);
	toolpane.add("Center", pmsg);
	toolpane.add("East", resetb);

	widget.add("South", toolpane);
	// add information about the class of the resource edited

	String classes[] = {""};
	try {
	    classes = rr.getClassHierarchy();
	} catch (RemoteAccessException ex) {
	    // big trouble but it may be temporary and this information
	    // is not vital, so just warn
	    ex.printStackTrace();
	}
	l = new Label("Class: " + classes[0], Label.CENTER);
	l.setForeground(new Color(0,0,128));
	widget.add("North", l);
	initialized = true;
    }
}
