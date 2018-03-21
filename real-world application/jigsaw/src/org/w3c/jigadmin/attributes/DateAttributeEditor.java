// DateAttributeEditor.java
// $Id: DateAttributeEditor.java,v 1.1 2010/06/15 12:20:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.attributes ;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

import java.util.Properties;
import java.util.Date;
import java.util.Calendar;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadmin.widgets.Icons;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.AttributeEditor;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.widgets.Utilities;

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
    JPanel widget;
    private JLabel h, min, s, d, m, y;
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

    private JButton getUpButton(DateActionListener dae) {
	JButton up = new JButton(Icons.arrowUpIcon);
	up.addActionListener(dae);
	up.setActionCommand("+");
	up.setMargin(Utilities.insets0);
	return up;
    }

    private JButton getDownButton(DateActionListener dae) {
	JButton down = new JButton(Icons.arrowDownIcon);
	down.addActionListener(dae);
	down.setActionCommand("-");
	down.setMargin(Utilities.insets0);
	return down;
    }

    private JLabel getDateLabel() {
	JLabel label = new JLabel(".");
	label.setHorizontalAlignment(JLabel.CENTER);
	return label;
    }

    public DateAttributeEditor() {
       JButton pl, mi;
       JPanel p, arrows;
       DateActionListener dae;

       widget = new JPanel(new GridLayout(2,1,1,1));

       JPanel time = new JPanel(new GridLayout(1,3));
       time.setBorder(BorderFactory.createEtchedBorder());

       JPanel date = new JPanel(new GridLayout(1,3));
       date.setBorder(BorderFactory.createEtchedBorder());

       //TIME
       // add the "hour" panel
       h = getDateLabel();

       dae = new DateActionListener(Calendar.HOUR_OF_DAY);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
       p.add(h, "Center");
       p.add(arrows, "East");
       time.add(p);

       // add the "min" panel
       min = getDateLabel();

       dae = new DateActionListener(Calendar.MINUTE);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
       p.add(min, "Center");
       p.add(arrows, "East");
       time.add(p);

       s = getDateLabel();

       dae = new DateActionListener(Calendar.SECOND);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
       p.add(s, "Center");
       p.add(arrows, "East");
       time.add(p);

       //DATE
       // add the "day" panel
       d = getDateLabel();

       dae = new DateActionListener(Calendar.DAY_OF_MONTH);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
       p.add(d, "Center");
       p.add(arrows, "East");
       date.add(p);

       // then the "Month" panel
       m = getDateLabel();

       dae = new DateActionListener(Calendar.MONTH);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
       p.add(m, "Center");
       p.add(arrows, "East");
       date.add(p);

       // then the "Year" panel
       y = getDateLabel();

       dae = new DateActionListener(Calendar.YEAR);
       pl = getUpButton(dae);
       mi = getDownButton(dae);

       arrows = new JPanel(new GridLayout(2,1));
       arrows.add(pl);
       arrows.add(mi);
       p = new JPanel(new BorderLayout());
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


