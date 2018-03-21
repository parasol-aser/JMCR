// ListEditor.java
// $Id: ListEditor.java,v 1.1 2010/06/15 12:20:38 smhuang Exp $
// Author: bmahe@sophia.inria.fr
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ListEditor :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public abstract class ListEditor extends BorderPanel 
                                 implements ActionListener 
{

    protected List   list       = null;
    protected Button editButton = null;

    protected abstract void edit();

    /**
     * ActionListsner implementation - One of our button was fired.
     * @param evt The ActionEvent.
     */

    public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	if ( command.equals("edit" ) )
	    edit();
    }

    public ListEditor() {
	this(5,false);
    }

    public ListEditor(int nbVisible, boolean multiple) {
	super(IN, 2);
	setInsets(new Insets(4,4,4,4));
	editButton = new Button ("Edit");
	editButton.setActionCommand("edit");
	editButton.addActionListener(this);
	list = new List(nbVisible, multiple);

	setLayout(new BorderLayout());
	add(list, "Center");
	add(editButton,"East");
    }

}
