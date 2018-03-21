// EditableSlider.java
// $Id: EditableSlider.java,v 1.1 2010/06/15 12:28:32 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.widgets;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 * A JSlider associated to an IntegerTextField
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 * @see org.w3c.jigadmin.widgets.IntegerTextField
 */
public class EditableSlider extends JPanel {

    ChangeListener cl = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	    if (e.getSource() == slider) {
		String newvalue = String.valueOf(slider.getValue());
		if (! newvalue.equals(textfield.getText()))
		    textfield.setText(newvalue);
	    }
	}
    };

    DocumentListener dl = new DocumentListener() {
	private void update() {
	    try {
		int newvalue = Integer.parseInt(textfield.getText());
		int min, max, step, nbtick, newnbtick, newstep;
		min = slider.getMinimum();
		max = slider.getMaximum();
		if (extensible) {
		    if (newvalue < min) {
			slider.removeChangeListener(cl);
			step = slider.getMajorTickSpacing();
			nbtick = (max - min) / step;
			newnbtick = (max - newvalue) / step;
			newstep = (max - newvalue) / nbtick;
			if (newnbtick != nbtick) {
			    slider.setMajorTickSpacing(newstep);
			}
			slider.setMinimum(newvalue);
			slider.addChangeListener(cl);
		    } else {
			if (newvalue > max) {
			    slider.removeChangeListener(cl);
			    step = slider.getMajorTickSpacing();
			    nbtick = (max - min) / step;
			    newnbtick = (newvalue - min) / step;
			    newstep = (newvalue - min) / nbtick;
			    if (newnbtick != nbtick) {
				slider.setMajorTickSpacing(newstep);
			    }
			    slider.setMaximum(newvalue);
			    slider.addChangeListener(cl);
			}
		    }
		    slider.setValue(newvalue);
		} else {
		    if ( (newvalue >= min) && (newvalue <= max)) {
			slider.setValue(newvalue);
		    }
		}
	    } catch (NumberFormatException ex) {
		//nothing
	    }
	}

	public void insertUpdate(DocumentEvent e) {
	    update();
	}

	public void changedUpdate(DocumentEvent e) {
	    update();
	}

	public void removeUpdate(DocumentEvent e) {
	    update();
	}
    };

    protected JSlider          slider     = null;
    protected IntegerTextField textfield  = null;
    private   boolean          extensible = true;

    public int getValue() {
	return slider.getValue();
    }

    public void setValue(int value) {
	slider.setValue(value);
    }

    public JSlider getSlider() {
	return slider;
    }

    public EditableSlider(int min, int max, int step, int value) {
	this(min, max, step, value, true);
    }

    public EditableSlider(int min, int max, int step,
			  int value, boolean extensible) {
	super(new FlowLayout());

	if (value < min) {
	    slider = new JSlider(value + (value - min), max, value);
	} else if (value > max) {
	    slider = new JSlider(min, value + (value - max), value);
	} else {
	    slider = new JSlider(min, max, value);
	}
	slider.setMajorTickSpacing(step);
	slider.setPaintLabels(true);
	slider.setPaintTicks(true);
	slider.setPaintTrack(true);
	slider.setSnapToTicks(false);
	
	textfield = new IntegerTextField(8);
	textfield.setText(String.valueOf(value));
	textfield.setBorder(BorderFactory.createLoweredBevelBorder());
	
	slider.addChangeListener(cl);
	textfield.getDocument().addDocumentListener(dl);

	add(textfield, BorderLayout.WEST);
	add(slider, BorderLayout.CENTER);
    }

}
