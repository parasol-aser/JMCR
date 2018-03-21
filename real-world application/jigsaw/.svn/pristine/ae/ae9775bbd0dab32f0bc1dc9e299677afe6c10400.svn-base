// SlideDoubleAttributeEditor.java
// $Id: SlideDoubleAttributeEditor.java,v 1.1 2010/06/15 12:22:40 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors;

import java.awt.Color;
import java.awt.Component;

import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.Slider;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

/**
 * SlideDoubleAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class SlideDoubleAttributeEditor extends AttributeEditor {

    /**
     * The slider's max bound property.
     */
    public static final String MAX_P = "slider.max";

    /**
     * The slider's min bound property.
     */
    public static final String MIN_P = "slider.min";

    /**
     * The slider's step property.
     */
    public static final String STEP_P = "slider.step";
    /**
     * The slider border property
     */
    public static final String BORDER_P = "slider.border";

    private double origs;
    Slider widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */
    public boolean hasChanged(){
	return (origs != widget.getValue());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */
    public void clearChanged(){
	origs = (double)widget.getValue();
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges(){
	widget.setValue(origs);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue(){
	return new Double(widget.getValue());
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o){
	widget.setValue(((Double)o).doubleValue());
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public Component getComponent() {
	return widget;
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper w, Attribute a, Object o,
			   Properties p)
	throws RemoteAccessException
    {
	if (p != null) {
	    double min; double max; double step;

	    try { 
		min = 
		    Double.valueOf(p.getProperty(MIN_P,"0.0")).doubleValue(); 
	    } catch (NumberFormatException ex) { min = 0.0; }

	    try { 
		max = 
		    Double.valueOf(p.getProperty(MAX_P,
						 "100.0")).doubleValue(); 
	    } catch (NumberFormatException ex) { max = 100.0; }

	    try { 
		step = Double.valueOf(p.getProperty(STEP_P,
						    "5.0")).doubleValue(); 
	    } catch (NumberFormatException ex) { step = 5.0; }
	
	    widget.initialize(min,max,step,
			      p.getProperty(BORDER_P,"false").equals("true"));

	} else {
	    widget.initialize(0.0,100.0,5.0);
	}

	RemoteResource r = w.getResource();
	if(o == null) {
	    Double v = null;
	    // FIXME
	    v = (Double) r.getValue(a.getName());
	   
	    if(v == null)
		if(a.getDefault() != null)
		    v = (Double)a.getDefault();
	    if ( v != null ) {
		origs = v.doubleValue();
		widget.setValue(origs);
	    } 
	} else {
	    origs = ((Double)o).doubleValue();
	}
	widget.setValue(origs);
    }

    public SlideDoubleAttributeEditor () {
	widget = new Slider(4, false);
	widget.setColor(Color.lightGray);
    }

}
