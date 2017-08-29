// FrameBrowser.java
// $Id: FrameBrowser.java,v 1.1 2010/06/15 12:25:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.plaf.basic.BasicTreeUI;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DnDConstants;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.widgets.EditableStringChoice;
import org.w3c.jigadmin.widgets.Icons;
import org.w3c.jigadmin.gui.Message;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.sorter.Sorter;

/**
 * A JTree used to manage frames.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class FrameBrowser extends ResourceTreeBrowser {

    /**
     * Constructor
     * @param root The resource node
     */
    protected FrameBrowser (RemoteResourceWrapperNode root) {
	super(root);
	setUI(new BasicTreeUI());
    }

    /**
     * Get a FrameBrowser.
     * @param rrw The resource
     * @param name The resource identifier.
     * @return a FrameBrowser instance
     */
    public static FrameBrowser getFrameBrowser(RemoteResourceWrapper rrw,
					       String name)
    {
	RemoteFrameWrapperNode rnode = new RemoteFrameWrapperNode(rrw, name);
	FrameBrowser           fb    = new FrameBrowser(rnode);
	return fb;
    }

    /**
     * Drop a frame.
     * @param cell The resource cell
     * @see org.w3c.jigadmin.editors.ResourceCell
     */
    protected boolean dropResource(ResourceCell cell)
    {
	RemoteResourceWrapper rrw = getSelectedResourceWrapper();
	if (cell.isFrame() || cell.isFilter()) {
	    try {
		TreePath path = getSelectionPath();
		addFrame(cell.toString(), rrw, path);
		return true;
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(this, ex);
		return false;
	    }
	} else {
	    return false;
	}
    }

    /**
     * Get the Panel used to add a new frame
     * @param title The title
     * @param rrw The wrapper of the father RemoteResource
     * @return a AddResourcePanel instance
     * @see org.w3c.jigadmin.editors.AddResourcePanel
     */
    protected AddResourcePanel getAddResourcePanel(String title, 
						   RemoteResourceWrapper rrw)
	throws RemoteAccessException
    {
	return new AddFramePanel(title, rrw, this);
    }

    private void performAddResourceToSelectedContainer() {
	popupAddResourceDialog("Add Frame", getSelectedResourceWrapper());
	if (resClassname != null) {
	    RemoteResourceWrapper rrw = getSelectedResourceWrapper();
	    try {
		addFrame(resClassname, rrw, getSelectionPath());
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(this, ex);
	    }
	}
    }

    /**
     * Add a frame to the selected container.
     */
    protected void addResourceToSelectedContainer() {
	RemoteResourceWrapper selected = getSelectedResourceWrapper();
	PropertyManager pm = PropertyManager.getPropertyManager();
	if (selected == null) {
	    JOptionPane.showMessageDialog(this, 
					  "No resource selected",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	} else if (! pm.isExtensible(selected)) {
	    JOptionPane.showMessageDialog(this, 
					  "The resource selected is not "+
					  "extensible.",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	} else {
	    Thread thread = new Thread() {
		public void run() {
		    performAddResourceToSelectedContainer();
		}
	    };
	    thread.start();
	}
    }

    /**
     * Add a frame to the resource wrapped.
     * @param classname The new frame class name
     * @param rrwf the Wrapper of the resource
     * @param fpath The path of the resource node
     */ 
    protected void addFrame(String classname, 
			    RemoteResourceWrapper rrwf,
			    TreePath path)
	throws RemoteAccessException
    { 
	RemoteResource newframe = 
	    rrwf.getResource().registerFrame(null, classname);
	RemoteResourceWrapper nrrw = 
	    new RemoteResourceWrapper(rrwf, newframe);
	RemoteFrameWrapperNode parent = 
	    (RemoteFrameWrapperNode)path.getLastPathComponent();
	RemoteFrameWrapperNode child = 
	    new RemoteFrameWrapperNode(parent, nrrw);
	((DefaultTreeModel)getModel()).insertNodeInto(child, parent, 0);
 	expandPath(path);
    }

    /**
     * Delete the frame wrapped by the given wrapper
     * @param rrw The RemoteResourceWrapper
     */    
    protected void deleteResource(RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	rrw.getFatherResource().unregisterFrame(rrw.getResource());
    }

    /**
     * Delete the frames associated to the selected nodes.
     * Display an error message if there is no node selected.
     */ 
    protected void deleteSelectedResources() {
	TreePath path[] = removeDescendants(getSelectionPaths());
	if (path == null)
	    return;
	if (path.length > 0) {
	    int result = 
		JOptionPane.showConfirmDialog(this, 
					      "Delete Selected Frame(s)?", 
					      "Delete Frame(s)", 
					      JOptionPane.YES_NO_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		for (int i = 0 ; i < path.length ; i++) {
		    RemoteResourceWrapper rrw = 
			getSelectedResourceWrapper(path[i]);
		    if (rrw == null)
			continue;
		    if (rrw != rootNode.getResourceWrapper()) {
			try {
			    deleteResource(rrw);
			} catch (RemoteAccessException ex) {
			    Message.showErrorMessage(this, ex);
			    continue;
			}
			MutableTreeNode node = 
			    (MutableTreeNode) path[i].getLastPathComponent();
			model.removeNodeFromParent(node);
		    } else {
			JOptionPane.showMessageDialog(this,
					      "You can't remove the "+
					      "resource here, please use "+
					      "the resource tree",
					      "Information",
					      JOptionPane.INFORMATION_MESSAGE);
		    }
		}
	    }
	}
    }

    /**
     * The popup menu action listener.
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    if (command.equals("del")) {
		deleteSelectedResources();
	    } else if (command.equals("add")) {
		addResourceToSelectedContainer();
	    } else if (command.equals("info")) {
		showReferenceDocumentation();
	    }
	}
    };	

    /**
     * Get the popup menu relative to the selected frame.
     * @param rrw the wrapper of the resource
     * @return a JPopupMenu instance
     */
    protected JPopupMenu getPopupMenu(RemoteResourceWrapper rrw) {
	boolean frame = rrw.getResource().isFrame();

	JPopupMenu popupMenu = new JPopupMenu();

	JMenuItem menuItem = new JMenuItem("Add frame", Icons.addIcon);
	menuItem.addActionListener(al);
	menuItem.setActionCommand("add");
	popupMenu.add(menuItem);

	if (frame) {
	    menuItem = new JMenuItem("Delete frame", Icons.deleteIcon);
	    menuItem.addActionListener(al);
	    menuItem.setActionCommand("del");
	    popupMenu.add(menuItem);
	}

	popupMenu.addSeparator();

	menuItem = new JMenuItem("Info", Icons.infoIcon);
	menuItem.addActionListener(al);
	menuItem.setActionCommand("info");
	popupMenu.add(menuItem);

	return popupMenu;
    }

    /**
     * A double click occured on the node with the given path.
     * @param path The path where the double click occured.
     */
    protected void doubleClick(TreePath path) {
	//nothing
    }
}
