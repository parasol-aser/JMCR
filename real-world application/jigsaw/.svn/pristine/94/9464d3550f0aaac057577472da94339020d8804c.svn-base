// StepIntegerAttributeEditor.java
// $Id: StepIntegerAttributeEditor.java,v 1.1 2010/06/15 12:22:43 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

public class StepIntegerAttributeEditor extends AttributeEditor {

    class StepListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
	    if(ae.getActionCommand().equals("+"))
		incr();
	    else
		decr();
	}
    }

    private int origi = 0;
    private int curri;
    private Label l;
    private Button p, m;
    Panel   widget;

    protected void incr() {
	curri++;
	l.setText((new Integer(curri)).toString());
    }

    protected void decr() {
	--curri;
	l.setText((new Integer(curri)).toString());
    }

    public boolean hasChanged() {
	return (origi != curri);
    }

    public void clearChanged() {
	origi = curri;
    }

    public void resetChanges() {
	curri = origi;
	l.setText((new Integer(curri)).toString());
    }

    public Object getValue() {
	return new Integer(curri);
    }

    public void setValue(Object o) {
	if(o instanceof Integer) {
	    curri = ((Integer)o).intValue();
	    l.setText(((Integer)o).toString());
	}
    }

    public Component getComponent() {
	return widget;
    }

    public StepIntegerAttributeEditor() {
	StepListener sl = new StepListener();
	widget = new Panel(new BorderLayout());
	l = new Label();
	l.setAlignment(Label.CENTER);
	widget.add("Center", l);
	p = new Button("+");
	p.addActionListener(sl);
	widget.add("East", p);
	m = new Button("-");
	m.addActionListener(sl);
	widget.add("West", m);
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
	    Integer i = null;
	    // FIXME
	    i = (Integer) r.getValue(a.getName());

	    if(i == null)
		if(a.getDefault() != null)
		    i = (Integer) a.getDefault();
	    if ( i != null ) {
		origi = i.intValue();
		l.setText(i.toString());
	    }
	    curri = origi;
	} else {
	    if(o instanceof Integer)
		origi = ((Integer)o).intValue();
	}
	l.setText((new Integer(origi)).toString());
	curri = origi;
    }
}


