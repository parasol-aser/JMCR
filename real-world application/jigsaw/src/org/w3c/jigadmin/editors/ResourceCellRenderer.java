// ResourceCellRenderer.java
// $Id: ResourceCellRenderer.java,v 1.1 2010/06/15 12:25:55 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigadmin.editors;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JScrollPane;

import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Color;

import org.w3c.jigadmin.widgets.Icons;

import org.w3c.tools.widgets.Utilities;

public class ResourceCellRenderer extends JLabel 
    implements ListCellRenderer 
{

    public ResourceCellRenderer() {
	setOpaque(true);
	setFont(Utilities.defaultFont);
    }
	
    // This is the only method defined by ListCellRenderer.  We just
    // reconfigure the Jlabel each time we're called.

    public Component getListCellRendererComponent(JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus)
    {
	String s = value.toString();
	setText(s);

	if (value instanceof ResourceCell) {
	    ResourceCell cell = (ResourceCell) value;
	    if (cell.isContainer() || cell.isIndexer())
		setIcon(Icons.dirIcon);
	    else if (cell.isFrame())
		setIcon(Icons.frameIcon);
	    else if (cell.isFilter())
		setIcon(Icons.filterIcon);
	    else if (cell.isMetaDataFrame())
		setIcon(Icons.metaDataIcon);
	    else
		setIcon(Icons.resIcon);
	}
	setBackground(isSelected ? Color.blue : Color.white);
	setForeground(isSelected ? Color.white : Color.black);
	return this;
    }
}
