// DnDTabbedPane.java
// $Id: DnDTabbedPane.java,v 1.1 2010/06/15 12:28:34 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.widgets;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JTabbedPane;

/**
 * A JTabbedPane for dnd (work arround for a bug in swing)
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DnDTabbedPane extends JTabbedPane {

    public DnDTabbedPane() {
	super();
    }

    public Component findComponentAt(int x, int y) {
	if (!contains(x, y)) {
	    return null;
	}
	int ncomponents = getComponentCount();
	for (int i = 0 ; i < ncomponents ; i++) {
	    Component comp = getComponentAt(i);
	    if (comp != null) {
		if (comp instanceof Container) {
		    if(comp.isVisible()) {
			comp = 
			    ((Container)comp).findComponentAt(x - comp.getX(),
							      y - comp.getY());
		    }
		} else {
		    comp = comp.getComponentAt(x - comp.getX(), 
					       y - comp.getY());
		}
		if (comp != null && comp.isVisible()) {
		    return comp;
		}
	    }
	}
	return this;
    }
}
