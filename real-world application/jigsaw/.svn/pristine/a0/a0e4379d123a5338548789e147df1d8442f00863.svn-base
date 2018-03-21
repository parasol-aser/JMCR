// SpaceServerHelper.java
// $Id: SpaceServerHelper.java,v 1.1 2010/06/15 12:25:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Container;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.widgets.DraggableList;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigadmin.events.ResourceActionEvent;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.sorter.Sorter;

/**
 * Server Helper for resources space.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class SpaceServerHelper extends JDesktopPane
    implements ServerHelperInterface, ResourceActionListener
{

    protected String                name          = null;
    protected String                tooltip       = null;
    protected RemoteResourceWrapper root          = null;
    protected ResourceTreeBrowser   tree          = null;
    protected JTabbedPane           tabbedPane    = null;
    protected JScrollPane           treeview      = null;
    protected JInternalFrame        iframe        = null;

    /**
     * Initialize this editor.
     * @param name the editor name
     * @param rrw the RemoteResourceWrapper wrapping the editor node.
     * @param p the editor properties
     */ 
    public void initialize(String name, 
			   RemoteResourceWrapper rrw,
			   Properties p)
    {
	this.name    = name;
	this.root    = rrw;
	this.tooltip = (String) p.get(TOOLTIP_P);
	build();
    }

    /**
     * Get a draggable JList of ResourceCell
     * @param cells a Vector of ResourceCell
     * @return A DraggableList instance
     */    
    protected DraggableList getResourceList(Vector cells) {
	DraggableList list = new DraggableList(cells);
	list.setCellRenderer(new ResourceCellRenderer());
	return list;
    }

    /**
     * Get the Internal Frame containing frame & resource lists
     * @return A JInternalFrame instance
     */    
    protected JInternalFrame getInternalFrame() {
	PropertyManager pm = PropertyManager.getPropertyManager();
	JInternalFrame frame = new JInternalFrame("Available Resources", 
						  true, //resizable
						  false, //closable
						  true, //maximizable
						  true);//iconifiable
	tabbedPane = new JTabbedPane();
	//Resource list
	Hashtable resources = pm.getResources();
	Enumeration e = 
	    (Sorter.sortStringEnumeration(resources.keys())).elements();
	Vector cells = new Vector(10);
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    ResourceCell cell = 
		new ResourceCell(name, (String)resources.get(name));
	    cells.addElement(cell);
	}
	DraggableList list = getResourceList(cells);
	JScrollPane scroll = new JScrollPane(list);
	tabbedPane.addTab("Resources", null, scroll, "Resources available");
	//frames
	resources = pm.getFrames();
	e = (Sorter.sortStringEnumeration(resources.keys())).elements();
	cells = new Vector(10);
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    ResourceCell cell = 
		new ResourceCell(name, (String)resources.get(name));
	    cells.addElement(cell);
	}
	list = getResourceList(cells);
	scroll = new JScrollPane(list);
	tabbedPane.addTab("Frames", null, scroll, 
			  "Frames and Filters available");
	frame.getContentPane().add(tabbedPane);
	frame.setSize(350, 410);
	return frame;
    }

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e) {
	tree.resourceActionPerformed(e);
    }

    /**
     * Return a new ResourceTreeBrowser.
     * @return a ResourceTreeBrowser instance
     */
    protected ResourceTreeBrowser getTreeBrowser() {
	return ResourceTreeBrowser.getResourceTreeBrowser(root, "Root");
    }

    /**
     * Build the interface
     */
    protected void build() {
	setOpaque(false);
	tree = getTreeBrowser();
	treeview = new JScrollPane(tree);
	add(treeview, JLayeredPane.FRAME_CONTENT_LAYER);
 	treeview.setLocation(10,10);
	treeview.setBorder(
			BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	iframe = getInternalFrame();
	add(iframe, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Update the layout in the JDesktopPane
     * @param width The JDesktopPane width
     * @param height  The JDesktopPane height
     */
    protected void updateLayout(int width, int height) {
	int treewidth = 5*width/12;
	treeview.setLocation(10,10);
	treeview.setSize(treewidth, height-20);
	int x = iframe.getLocation().x;
	int minx = 20+treewidth;
	if (x < minx)
	    iframe.setLocation(minx, 10);
    }

    /**
     * Moves and resizes this component. The new location of the top-left
     * corner is specified by x and y, and the new size is specified by 
     * width and height.
     * @param x The new x-coordinate of this component.
     * @param y The new y-coordinate of this component.
     * @param width The new width of this component.
     * @param height The new height of this component.
     */    
    public void setBounds(int x,
			  int y,
			  int width,
			  int height) {
	updateLayout(width, height);
	super.setBounds(x,y,width, height);
    }

    /**
     * Get the helper name.
     * @return a String instance
     */
    public String getName() {
	return name;
    }

    /**
     * Get the helper tooltip
     * @return a String
     */   
    public String getToolTip() {
	return tooltip;
    }

    /**
     * Get the Component.
     * @return a Component instance
     */
    public Component getComponent() {
	return this;
    }

    /**
     * Constructor.
     */
    public SpaceServerHelper() {
	//new instance
    }

}
