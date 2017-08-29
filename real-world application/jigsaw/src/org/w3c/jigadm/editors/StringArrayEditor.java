// StringArrayEditor.java
// $Id: StringArrayEditor.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Properties;

import org.w3c.tools.resources.Attribute;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.ClosableFrame;
import org.w3c.tools.widgets.ImageButton;
import org.w3c.tools.widgets.ListEditor;
import org.w3c.tools.widgets.TextEditable;

/**
 * An editor for StringArray attributes.  
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class StringArrayEditor extends AttributeEditor {

    class EditStringArrayPopup extends ClosableFrame 
	implements ActionListener 
    {

	protected StringArrayComponent parent = null;
	protected EditorFeeder feeder         = null;
	protected String selected []             = null;
	protected List witems                    = null;
	protected Panel items                    = null;
	protected Panel pitems                   = null;
	protected ImageButton waddItem           = null;
	protected ImageButton wdelItem           = null;
	protected List wselected                 = null;
	protected Panel pselected                = null;
	protected TextEditable newItem           = null;		      
	protected boolean modified               = false;

	/**
	 * ActionListsner implementation - One of our button was fired.
	 * @param evt The ActionEvent.
	 */

	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    if ( command.equals("add" ) ) {
		if (newItem.updated()) {
		    modified = true;
		    wselected.addItem(newItem.getText());
		}
		newItem.setDefault();
		// Add witems selection to wselected:
		int isels[] = witems.getSelectedIndexes();
		if ((isels != null) && (isels.length > 0)) {
		    // Wait until processing done to remove items...
		    modified = true;
		    for (int i = 0 ; i < isels.length ; i++) {
			String item = witems.getItem(isels[i]);
			wselected.addItem(item);
			witems.deselect(isels[i]);
		    }
		    for (int i = 0 ; i < isels.length ; i++)
			witems.delItem(isels[i]-i);
		}
	    } else  if ( command.equals("del" ) ) {
		int isels[] = wselected.getSelectedIndexes();
		if ((isels != null) && (isels.length > 0)) {
		    // Wait until processing done to remove items...
		    modified = true;
		    for (int i = 0 ; i < isels.length ; i++) {
			String item = wselected.getItem(isels[i]);
			witems.addItem(item);
			wselected.deselect(isels[i]);
		    }
		    for (int i = 0 ; i < isels.length ; i++) 
			wselected.delItem(isels[i]-i);
		}
	    } else  if ( command.equals("update" ) ) {
		if (modified) {
		    parent.setSelectedItems(wselected.getItems());
		    parent.setModified();
		}
		setVisible(false);
	    } else  if ( command.equals("cancel" ) ) {
		close();
	    } else  if ( evt.getSource().equals(newItem)) {
		if (newItem.updated()) {
		    modified = true;
		    wselected.addItem(newItem.getText());
		}
		newItem.setDefault();
		wselected.requestFocus();
	    }
	}
	
	protected void close() {
	    modified = false;
	    setVisible(false);
	}

	/**
	 * Create the list of possible items, querying the feeder:
	 * @param feeder The one that knows about default items.
	 */

	protected void createDefaultItems(EditorFeeder feeder) {
	    this.witems = new List(4, false);
	    witems.setBackground(Color.white);
	    witems.setMultipleMode(true);
	    // Feed that list:
	    String items[] = feeder.getDefaultItems();
	    if (items != null) {
		for (int i = 0 ; i < items.length ; i++) 
		    if ( items[i] != null )
			witems.addItem(items[i]);
	    }
	}

	protected void setDefaultItems(EditorFeeder feeder) {
	    witems.removeAll();
	    String items[] = feeder.getDefaultItems();
	    if (items != null) {
		for (int i = 0 ; i < items.length ; i++) 
		    if ( items[i] != null )
			witems.addItem(items[i]);
	    }
	}

	protected void createSelectedItems() {
	    this.wselected = new List(7, false);
	    wselected.setMultipleMode(true);
	    wselected.setBackground(Color.white);
	}

	protected void setSelectedItems(String selected[]) {
	    // Remove any prev set items:
	    wselected.removeAll();
	    // Refill the list:
	    if ( selected != null ) {
		for (int i = 0 ; i < selected.length ; i++) 
		    if ( selected[i] != null )
			wselected.addItem(selected[i]);
	    }
	}

	protected String[] getSelectedItems() {
	    return wselected.getItems();
	}  

	protected void updateSize() {
	    setSize(parent.editor.getPopupSize());
	}
						      
	public void start(String selected []) {
	    modified = false;
	    setSelectedItems(selected);
	    setDefaultItems(feeder);
	}

	public EditStringArrayPopup(StringArrayComponent parent,
				    EditorFeeder feeder,
				    String selected [],
				    String title)
	{
	    super(title);

	    PropertyManager pm = PropertyManager.getPropertyManager();
	    Image left = Toolkit.getDefaultToolkit().getImage(
				      pm.getIconLocation("shadowleft"));

	    Image right = Toolkit.getDefaultToolkit().getImage(
				       pm.getIconLocation("shadowright"));
	    this.selected  = selected;
	    this.parent   = parent;
	    this.feeder   = feeder;
	    this.newItem = parent.editor.getTextEditor();
	    createDefaultItems(feeder);
	    createSelectedItems();
	    setSelectedItems(selected);

	    //center
	    waddItem = new ImageButton(left);
	    waddItem.setActionCommand("add");
	    waddItem.addActionListener(this);
	    Panel paddItem = new Panel();
	    paddItem.add(waddItem);

	    wdelItem = new ImageButton(right);
	    wdelItem.setActionCommand("del");
	    wdelItem.addActionListener(this);    
	    Panel pdelItem = new Panel();
	    pdelItem.add(wdelItem, "Center");

	    Button Ok = new Button("Ok");
	    Ok.setActionCommand("update");
	    Ok.addActionListener(this);
	    Button Cancel = new Button("Cancel");
	    Cancel.setActionCommand("cancel");
	    Cancel.addActionListener(this);

	    Panel pselected = new Panel(new BorderLayout(3,3));
	    Panel psel = new Panel();
	    psel.add(new Label("Selection"));
	    pselected.add(psel,"North");
	    pselected.add(wselected,"Center");

	    Panel items = new Panel(new BorderLayout(3,3));
	    newItem.addActionListener(this);
	    items.add((Component)newItem,"North");
	    items.add(witems, "Center");

	    Panel arrows = new BorderPanel(BorderPanel.IN);
	    arrows.add(pdelItem);
	    arrows.add(paddItem);

	    BorderPanel lists = new BorderPanel(BorderPanel.IN);
	    lists.setLayout(new GridLayout(1,2,20,5));
	    lists.add(pselected);
	    lists.add(items);

	    Panel buttons = new Panel(new GridLayout(1,2,5,5));
	    buttons.add(Ok);
	    buttons.add(Cancel);
	    Panel pbuttons = new BorderPanel(BorderPanel.IN);
	    pbuttons.add(buttons);

	    BorderPanel mainp = new BorderPanel(BorderPanel.OUT,5);
	    mainp.setLayout(new BorderLayout());
	    mainp.setInsets(new Insets(10,10,10,10));
	    mainp.add(arrows,"North");
	    mainp.add(lists,"Center");
	    mainp.add(pbuttons,"South");

	    setLayout(new BorderLayout());
	    add(mainp);
	    updateSize();
	}
    }

    class StringArrayComponent extends ListEditor {

	protected StringArrayEditor editor     = null;
	protected EditStringArrayPopup   popup    = null;
	protected String selected []              = null;
	protected EditorFeeder feeder  = null;

	protected void edit() {
	    if (popup == null)
		popup = new EditStringArrayPopup(this,
						 feeder,
						 getSelectedItems(),
						 "Edit");
	    else
		popup.start(getSelectedItems());
	    popup.show();
	    popup.toFront();

	}

	public void setModified() {
	    editor.setModified();
	}

	protected void setSelectedItems(String selected[]) {
	    // Remove any prev set items:
	    list.removeAll();
	    // Refill the list:
	    if ( selected != null ) {
		for (int i = 0 ; i < selected.length ; i++) 
		    if ( selected[i] != null )
			list.addItem(selected[i]);
	    }
	}

	protected String[] getSelectedItems() {
	    return list.getItems();
	}

	StringArrayComponent (StringArrayEditor editor,
			      String selected[],
			      EditorFeeder feeder)
	{
	    super(5,true);
	    this.editor = editor;
	    this.selected = selected;
	    this.feeder  = feeder;
	    setSelectedItems(selected);
	}

    }

    class TextEditor extends TextField implements TextEditable { 

	public boolean updated() {
	    return ( (getText().length() > 0) &&
		     (! getText().equals("")) );
	}

	public void setDefault() {
	    setText("");
	}

	TextEditor(int nb) {
	    super(nb);
	}

    }

    // The StringArrayEditor itself

    /**
     * Properties - The feeder's class name.
     */
    public static final String FEEDER_CLASS_P = "feeder.class";

    protected boolean hasChanged = false;
    protected String oldvalue[]  = null;
    protected StringArrayComponent comp = null;

    protected TextEditable getTextEditor() {
	return new TextEditor(15);
    }

    protected Dimension getPopupSize() {
	return new Dimension(350,250);
    }

    protected void createComponent(EditorFeeder feeder,
				   String selected[]) {
	if ( comp == null ) 
	    comp = new StringArrayComponent(this,
					    selected,
					    feeder);

    }

    protected void setModified() {
	hasChanged = true;
    }

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */
    public boolean hasChanged() {
	return hasChanged;
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */
    public void clearChanged() {
	hasChanged = false;
    }

    /**
     * reset the changes (if any)
     */
    public void resetChanges() {
	hasChanged = false;
	comp.setSelectedItems(oldvalue);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */
    public Object getValue() {
	return comp.getSelectedItems();    
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */
    public void setValue(Object o) {
	this.oldvalue = (String[]) o;
	comp.setSelectedItems(oldvalue);
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public Component getComponent() {
	return comp;
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(RemoteResourceWrapper w
			   , Attribute a
			   , Object o
			   , Properties p) 
	throws RemoteAccessException
    {
	// Get the feeder class fromproperties:
	EditorFeeder feeder      = null;
	String       feederClass = null;

	feederClass = (String)p.get(FEEDER_CLASS_P);
	if ( feederClass == null )
	    throw new RuntimeException("StringArrayEditor mis-configuration: "+
				       FEEDER_CLASS_P + " property undefined.");
	try {
	    Class c = Class.forName(feederClass);
	    feeder  = (EditorFeeder) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    feeder.initialize(w,p);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("StringArrayEditor mis-configured: "+
				       " unable to instantiate "+
				       feederClass +".");
	}
	createComponent(feeder, (String[]) o);
	oldvalue = (String[]) o;
    }

    public StringArrayEditor() {
	super();
    }

}


