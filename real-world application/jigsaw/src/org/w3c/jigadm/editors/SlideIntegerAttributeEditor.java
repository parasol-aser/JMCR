// SlideIntegerAttributeEditor.java
// $Id: SlideIntegerAttributeEditor.java,v 1.1 2010/06/15 12:22:48 smhuang Exp $
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
 * SlideIntegerAttributeEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class SlideIntegerAttributeEditor extends AttributeEditor {

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

    private int origs;
    Slider widget;

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */
    public boolean hasChanged(){
	return (origs != (int)widget.getValue());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */
    public void clearChanged(){
	origs = (int)widget.getValue();
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges(){
	widget.setValue((long)origs);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue(){
	return new Integer((int)widget.getValue());
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o){
	widget.setValue((long)((Integer)o).intValue());
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
	    long min; long max; long step;

	    try { min = Long.parseLong(p.getProperty(MIN_P,"0")); } 
	    catch (NumberFormatException ex) { min = 0; }

	    try { max = Long.parseLong(p.getProperty(MAX_P,"100")); } 
	    catch (NumberFormatException ex) { max = 100; }

	    try { step = Long.parseLong(p.getProperty(STEP_P,"5")); }
	    catch (NumberFormatException ex) { step = 5; }

     
 
	    widget.initialize(min,max,step,
			      p.getProperty(BORDER_P,"false").equals("true"));

	} else {
	    widget.initialize((long)0,(long)100,(long)5);
	}
	RemoteResource r = w.getResource();
	if(o == null) {
	    Integer v = null;
	    // FIXME
	    v = (Integer) r.getValue(a.getName());
	   
	    if(v == null)
		if(a.getDefault() != null)
		    v = (Integer)a.getDefault();
	    if ( v != null ) {
		origs = v.intValue();
		widget.setValue((long)origs);
	    } 
	} else {
	    origs = ((Integer)o).intValue();
	}
	widget.setValue((long)origs);
    }

    public SlideIntegerAttributeEditor () {
	widget = new Slider(4, false);
	widget.setColor(Color.lightGray);
    }

}
