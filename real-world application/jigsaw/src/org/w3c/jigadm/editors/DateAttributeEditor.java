// DateAttributeEditor.java
// $Id: DateAttributeEditor.java,v 1.1 2010/06/15 12:22:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.ImageButton;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

public class DateAttributeEditor extends AttributeEditor {

    /** 
     * an inner ActionListener for the '+' and '-' Buttons
     */

    class DateActionListener implements ActionListener {
	int field = 0;
	
        public void actionPerformed(ActionEvent ae) {
	    if(ae.getActionCommand().equals("+"))
		updateValue(field, true);
	    else
		updateValue(field, false);
	}

      DateActionListener(int f) {
	    field = f;
	}
    }

    private Calendar c;
    private Date origd;
    Date currd;
    Panel widget;
    private Label h, min, s, d, m, y;
    private static final String smonth[] = {"Jan", "Feb", "Mar", "Apr",
					    "May", "Jun", "Jul", "Aug",
					    "Sep", "Oct", "Nov", "Dec" };

    /**
     * reset the strings in the Textfields according to the new date
     */

    private void updateFields() {
	h.setText((new Integer(c.get(Calendar.HOUR_OF_DAY))).toString());
	min.setText((new Integer(c.get(Calendar.MINUTE))).toString());
	s.setText((new Integer(c.get(Calendar.SECOND))).toString());
	d.setText((new Integer(c.get(Calendar.DAY_OF_MONTH))).toString());
	m.setText(smonth[c.get(Calendar.MONTH)]);
	y.setText((new Integer(c.get(Calendar.YEAR))).toString());
    }

    /**
     * update the new date value, according to the field defined in
     * the Calendar
     * @see Calendar
     * @param field the field of the Calendar to be modified
     * @param plus a boolean which determine the change sign
     */

    protected void updateValue(int field, boolean plus) {
	c.setTime(currd);
	c.roll(field, plus);
	currd = c.getTime();
	// jdk 1.1 kludge
	c.setTime(currd);
	updateFields();
    }

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return (!origd.equals(currd));
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origd = currd;
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	currd = origd;
	c.setTime(currd);
	updateFields();
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	return new Long(currd.getTime());
    }

    /**
     * Add a Listener to this editor.
     * @param el a listener
     */

    public void setValue(Object o) {
	if(o instanceof Date) {
	    currd = (Date)o;
	    c.setTime(currd);
	    updateFields();
	}
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */

    public Component getComponent() {
	return widget;
    }

   public DateAttributeEditor() {
       ImageButton pl, mi;
       Panel p, arrows;
       DateActionListener dae;
       PropertyManager pm = PropertyManager.getPropertyManager();

       Image up = Toolkit.getDefaultToolkit().getImage(
	   pm.getIconLocation("pup"));
       Image down = Toolkit.getDefaultToolkit().getImage(
	   pm.getIconLocation("pdown"));

       widget = new Panel(new GridLayout(2,1,1,1));

       Panel time = new BorderPanel(BorderPanel.IN);
       time.setLayout(new GridLayout(1,3));

       Panel date = new BorderPanel(BorderPanel.IN);
       date.setLayout(new GridLayout(1,3));

       // add the "hour" panel
       h = new Label();
       h.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.HOUR_OF_DAY);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(h, "Center");
       p.add(arrows, "East");
       time.add(p);

       // add the "min" panel
       min = new Label();
       min.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.MINUTE);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(min, "Center");
       p.add(arrows, "East");
       time.add(p);

       s = new Label();
       s.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.SECOND);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(s, "Center");
       p.add(arrows, "East");
       time.add(p);

       // add the "day" panel
       d = new Label();
       d.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.DAY_OF_MONTH);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(d, "Center");
       p.add(arrows, "East");
       date.add(p);

       // then the "Month" panel
       m = new Label();
       m.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.MONTH);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(m, "Center");
       p.add(arrows, "East");
       date.add(p);

       // then the "Year" panel
       y = new Label();
       y.setAlignment(Label.CENTER);
       dae = new DateActionListener(Calendar.YEAR);

       pl = new ImageButton(up);
       pl.addActionListener(dae);
       pl.setActionCommand("+");

       mi = new ImageButton(down);
       mi.addActionListener(dae);
       mi.setActionCommand("-");

       arrows = new Panel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new Panel(new BorderLayout());
       p.add(y, "Center");
       p.add(arrows, "East");
       date.add(p);

       widget.add(time);
       widget.add(date);

       c = Calendar.getInstance();
   }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.     
     */

   public void initialize(RemoteResourceWrapper w, Attribute a,  Object o,
			  Properties p)
       throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    Date d = null;
	    try {
		d = new Date(((Long)r.getValue(a.getName())).longValue());
		
		if(d == null)
		    if(a.getDefault() != null)
			d = new Date(((Long) a.getDefault()).longValue());
	    } catch (Exception ex) {
		// a fancy error?
	    }
	    if ( d != null ) {
		origd = d;
		c.setTime(d);
		updateFields();
	    } else {
	 	origd = new Date();
	    }
	    currd = origd;
	} else {
	    if(o instanceof Long) {
		origd = new Date(((Long)o).longValue());
		c.setTime(origd);
	    }
	}
	updateFields();
	currd = origd;
    }
}


