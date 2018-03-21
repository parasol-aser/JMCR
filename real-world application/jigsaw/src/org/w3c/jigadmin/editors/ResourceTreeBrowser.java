// ResourceTreeBrowser.java
// $Id: ResourceTreeBrowser.java,v 1.1 2010/06/15 12:25:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JTree;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DnDConstants;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.events.ResourceActionListener;
import org.w3c.jigadmin.events.ResourceActionEvent;
import org.w3c.jigadmin.widgets.Icons;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

/**
 * A JTree used to manage RemoteResource.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ResourceTreeBrowser extends JTree 
    implements DropTargetListener, ResourceActionListener
{

    public static final String DELETE_RESOURCE_AC = "delres";

    protected RemoteResourceWrapperNode rootNode      = null;
    protected String                    resIdentifier = null;
    protected String                    resClassname  = null;
    protected JDialog                   popup         = null;

    private boolean isDragging = false;

    /**
     * Our TreeWillExpandListener
     */
    TreeWillExpandListener twel = new TreeWillExpandListener() {
	public void treeWillExpand(TreeExpansionEvent event)
	throws ExpandVetoException
	{
	    TreeNode node = 
	    (TreeNode)event.getPath().getLastPathComponent();
	    ((RemoteNode)node).nodeWillExpand();
	    ((DefaultTreeModel)getModel()).reload(node);
	}

	public synchronized void treeWillCollapse(TreeExpansionEvent event)
	throws ExpandVetoException
	{
	    TreeNode node = 
	    (TreeNode)event.getPath().getLastPathComponent();
	    ((RemoteNode)node).nodeWillCollapse();
	    ((DefaultTreeModel)getModel()).reload(node);
	}
    };

    /**
     * Our ActionListener
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    if (command.equals(DELETE_RESOURCE_AC)) {
		//delete the selected resource
		deleteSelectedResources();
	    }
	}
    };
    /**
     * Our MouseListener
     */
    MouseAdapter mouseAdapter = new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	    int selRow = getRowForLocation(e.getX(), e.getY());
	    TreePath selPath = getPathForLocation(e.getX(), e.getY());
	    if(selRow != -1) {
		if(e.getClickCount() == 1) {
		    simpleClick(selPath);
		}
		else if(e.getClickCount() == 2) {
		    doubleClick(selPath);
		}
	    }
	}

	public void mousePressed(MouseEvent e) {
	    maybeShowPopup(e);
	}
	    
	public void mouseReleased(MouseEvent e) {
	    maybeShowPopup(e);
	}
	    
	private void maybeShowPopup(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		RemoteResourceWrapper rrw = getSelectedResourceWrapper();
		if (rrw != null)
		getPopupMenu(rrw).show(e.getComponent(), 
				       e.getX(), e.getY());
	    }
	}
    };

    //DropTargetListener

    DropTarget dropTarget;

    /**
     * Is the mouse dragging something on the resource tree?
     * @return a boolean
     */
    public boolean isDragging() {
	return isDragging;
    }

    /**
     * a Drag operation has encountered the DropTarget
     */
    public void dragEnter (DropTargetDragEvent dropTargetDragEvent) {
	dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
	isDragging = true;
    }

    /**
     * The Drag operation has departed the DropTarget without dropping.
     */
    public void dragExit (DropTargetEvent dropTargetEvent) {
	isDragging = false;
    }

    /**
     * a Drag operation is ongoing on the DropTarget
     */
    public void dragOver (DropTargetDragEvent dropTargetDragEvent) {
	Point location = dropTargetDragEvent.getLocation();
	TreePath path = getClosestPathForLocation(location.x,
						  location.y);
	setSelectionPath(path);
    }

    /**
     * The user as modified the current drop gesture
     */
    public void dropActionChanged (DropTargetDragEvent dropTargetDragEvent) {
    }

    /**
     * The Drag operation has terminated with a Drop on this DropTarget
     */
    public synchronized void drop (DropTargetDropEvent dropTargetDropEvent) {
	isDragging = false;
	Transferable tr  = dropTargetDropEvent.getTransferable();
	DataFlavor required = TransferableResourceCell.RESOURCE_CELL_FLAVOR;
	try {
	    if (tr.isDataFlavorSupported(required)) {
		dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
		ResourceCell cell = (ResourceCell)tr.getTransferData(required);
		dropTargetDropEvent.dropComplete(dropResource(cell));
	    } else {
		dropTargetDropEvent.rejectDrop();
	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	    dropTargetDropEvent.rejectDrop();
	} catch (UnsupportedFlavorException ufe) {
	    ufe.printStackTrace();
	    dropTargetDropEvent.rejectDrop();
	}
    }

    /**
     * Drop a resource.
     * @param cell The resource cell
     * @see org.w3c.jigadmin.editors.ResourceCell
     */
    protected boolean dropResource(ResourceCell cell) {
	RemoteResourceWrapper rrw = getSelectedResourceWrapper();
	PropertyManager pm = PropertyManager.getPropertyManager();
	try {
	    if (! pm.isExtensible(rrw)) {
		return false;
	    } else if ((cell.isFrame() || cell.isFilter()) && 
		       (pm.isEditable(rrw))) {
		//popupresource
		popupResource(rrw);
		return true;
	    } else if (((cell.isContainer() || cell.isResource()) && 
			(! rrw.getResource().isIndexersCatalog())) 
		       ||
		       (rrw.getResource().isIndexersCatalog() && 
			cell.isIndexer())) {
		//must be droppped on a Container
		if (rrw.getResource().isContainer()) {
		    //add the resource
		    String identifier = getIdentifier(cell, rrw);
		    if (identifier != null) {
			TreePath path = getSelectionPath();
			addResource(identifier, cell.toString(), 
				    rrw, path);
			return true;
		    } 
		}
	    }
	    return false;
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	    return false;
	}
    }

    //End of DropTargetListener

    /**
     * A resource action occured.
     * @param e the ResourceActionEvent
     */
    public void resourceActionPerformed(ResourceActionEvent e) {
	int cmd = e.getResourceActionCommand();
	if (isShowing()) {
	    if (cmd == ResourceActionEvent.DELETE_EVENT) {
		deleteSelectedResources();
	    } else if (cmd == ResourceActionEvent.REINDEX_EVENT) {
		reindexSelectedResources(false); //FIXME
	    } else if (cmd == ResourceActionEvent.REFERENCE_EVENT) {
		showReferenceDocumentation();
	    } else if (cmd == ResourceActionEvent.ADD_EVENT) {
		addResourceToSelectedContainer();
	    } else if (cmd == ResourceActionEvent.EDIT_EVENT) {
		doubleClick(getSelectionPath());
	    }
	}
    }

    /**
     * Add a resource to the resource wrapped.
     * @param identifier The new resource identifier
     * @param classname The new resource class name
     * @param rrwf the Wrapper of the father resource
     * @param fpath The path of the father node
     */
    protected void addResource(String identifier, String classname,
			       RemoteResourceWrapper rrwf, TreePath fpath) 
	throws RemoteAccessException
    {
	RemoteResource rc = 
	    rrwf.getResource().registerResource(identifier, classname);
	RemoteResourceWrapper nrrw = 
	    new RemoteResourceWrapper(rrwf, rc);
	RemoteResourceWrapperNode parent = 
	    (RemoteResourceWrapperNode)fpath.getLastPathComponent();
	RemoteResourceWrapperNode child = 
	    new RemoteResourceWrapperNode(parent, nrrw, identifier);
	((DefaultTreeModel)getModel()).insertNodeInto(child, parent, 0);
	expandPath(fpath);
    }

    /**
     * Add a resource to the selected container.
     * @param classname the resource class name
     * @param identifier the resource identifier
     */
    protected void addResourceToSelectedContainer(String classname,
						  String identifier)
	throws RemoteAccessException				  
    {
	RemoteResourceWrapper rrw = getSelectedResourceWrapper();
	if (rrw == null)
	    return;
	addResource(identifier, classname, rrw, getSelectionPath());
    }

    /**
     * Get (compute) the resource identifier of the dropped resource.
     * @param cell the ResourceCell dropped
     * @param rrw the RemoteResourceWrapper of the father
     * @return a String instance
     * @see org.w3c.jigadmin.editors.ResourceCell
     */
    protected String getIdentifier(ResourceCell cell, 
				   RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	String id = cell.toString();
	id = id.substring(id.lastIndexOf('.')+1);
	String names[] = rrw.getResource().enumerateResourceIdentifiers();
	String identifier = id;
	int cpt = 0; 
	int i   = 0;
    loop:
	while (i < names.length) {
	    if (names[i].equals(identifier)) {
		identifier = id+String.valueOf(++cpt);
		i = 0;
		continue loop;
	    }
	    i++;
	}
	return identifier;
    }

    /**
     * Get the RemoteResourceWrapper associated to the selected node.
     * @return a RemoteResourceWrapper
     */
    protected RemoteResourceWrapper getSelectedResourceWrapper() {
	RemoteResourceWrapperNode node = 
	    (RemoteResourceWrapperNode)getLastSelectedPathComponent();
	if (node == null)
	    return null;
	return node.getResourceWrapper();
    }

    /**
     * Get the RemoteResourceWrapper associated to the selected node.
     * @param path the selected path
     * @return a RemoteResourceWrapper
     */
    protected RemoteResourceWrapper getSelectedResourceWrapper(TreePath path) {
	if (path == null)
	    return null;
	RemoteResourceWrapperNode node = 
	    (RemoteResourceWrapperNode) path.getLastPathComponent();
	return node.getResourceWrapper();
    }

    /**
     * Get the Panel used to add a new resource
     * @param title The title
     * @param rrw The wrapper of the father RemoteResource
     * @return a AddResourcePanel instance
     * @see org.w3c.jigadmin.editors.AddResourcePanel
     */
    protected AddResourcePanel getAddResourcePanel(String title, 
						   RemoteResourceWrapper rrw)
	throws RemoteAccessException
    {
	return new AddResourcePanel(title, rrw, this);
    }

    /**
     * Popup a "add resource" Dialog
     * @param title the popup title
     * @param rrw The wrapper of the father RemoteResource
     */
    protected void popupAddResourceDialog(String title, 
					  RemoteResourceWrapper rrw) 
    {
	try {
	    AddResourcePanel arp = getAddResourcePanel(title, rrw);
	    JFrame frame = 
		rootNode.
		getResourceWrapper().getServerBrowser().getFrame();
	    popup = new JDialog(frame, title, false);
	    Container cont = popup.getContentPane();
	    cont.setLayout(new GridLayout(1,1));
	    cont.add(arp);
	    popup.setSize(new Dimension(600, 220));
	    popup.show();
	    arp.getFocus();
	    while(! arp.waitForCompletion());
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Dispose the "add resource" popup
     */
    protected void disposeAddResourcePopup() {
	if (popup != null) {
	    popup.dispose();
	    popup = null;
	}
    }

    /**
     * Specify some properties of the resource to add
     * @param classnema the new resource class name
     * @param identifier the new resource identifier
     */
    protected void setResourceToAdd(String classname, String identifier) {
	this.resClassname  = classname;
	this.resIdentifier = identifier;
    }

    private void performAddResourceToSelectedContainer() {
	popupAddResourceDialog("Add Resource", getSelectedResourceWrapper());
	if ((resIdentifier != null) && (resClassname != null)) {
	    try {
		addResourceToSelectedContainer(resClassname, 
					       resIdentifier);
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(this, ex);
	    }
	}
    }

    /**
     * Add a (new) resource to the container associated to the
     * selected node.
     */
    protected void addResourceToSelectedContainer() {
	RemoteResourceWrapper selected = getSelectedResourceWrapper();
	PropertyManager pm = PropertyManager.getPropertyManager();
	try {
	    if (selected == null) {
		JOptionPane.showMessageDialog(this, 
					      "No Container selected",
					      "Error",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    } else if (! pm.isExtensible(selected)) {
		JOptionPane.showMessageDialog(this, 
					      "The resource selected is not "+
					      "extensible.",
					      "Error",
					      JOptionPane.ERROR_MESSAGE);
	    }else if (selected.getResource().isContainer()) {
		Thread thread = new Thread() {
		    public void run() {
			performAddResourceToSelectedContainer();
		    }
		};
		thread.start();
	    } else {
		JOptionPane.showMessageDialog(this, 
					      "The resource selected is not "+
					      "container.",
					      "Error",
					      JOptionPane.ERROR_MESSAGE);
	    }
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Filter the TreePath array. Remove all nodes that have one of their
     * parent in this array.
     * @param paths the TreePath array
     * @return the filtered array
     */
    protected TreePath[] removeDescendants(TreePath[] paths) {
	if (paths == null)
	    return null;
	Vector newpaths = new Vector();
	for (int i = 0 ; i < paths.length ; i++) {
	    TreePath currentp = paths[i];
	    boolean hasParent = false;
	    for (int j = 0 ; j < paths.length ; j++) {
		if ((!(j == i)) && (paths[j].isDescendant(currentp)))
		    hasParent = true;
	    }
	    if (! hasParent)
		newpaths.addElement(currentp);
	}
	TreePath[] filteredPath = new TreePath[newpaths.size()];
	newpaths.copyInto(filteredPath);
	return filteredPath;
    }

    /**
     * Reindex the containers associated to the selected nodes.
     * Display an error message (dialog) if there is no node selected or
     * if one of the selected resource is not a ResourceContainer.
     * @param rec recursivly?
     */
    protected void reindexSelectedResources(boolean rec) {
	TreePath path[] = removeDescendants(getSelectionPaths());
	if (path == null) {
	    JOptionPane.showMessageDialog(this, 
					  "No Container selected",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
	if (path.length > 0) {
	    int result = 
		JOptionPane.showConfirmDialog(this, 
					      "Reindex selected resource(s)?", 
					      "Reindex Resource(s)", 
					      JOptionPane.YES_NO_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		for (int i = 0 ; i < path.length ; i++) {
		    RemoteResourceWrapper rrw = 
			getSelectedResourceWrapper(path[i]);
		    if (rrw == null)
			continue;
		    try {
			reindexResource(rrw, rec);
		    } catch (RemoteAccessException ex) {
			Message.showErrorMessage(this, ex);
			continue;
		    }
		}
	    }
	}
    }

    /**
     * Reindex the container wrapped by the given wrapper.
     * Display an error message (dialog) if the resource
     * is not a ResourceContainer.
     * @param rrw the RemoteResourceWrapper
     * @param rec recursivly?
     * @exception RemoteAccessException if a Remote Error occurs
     */
    protected void reindexResource(RemoteResourceWrapper rrw, boolean rec) 
	throws RemoteAccessException
    {
	RemoteResource rr = rrw.getResource();
	if (rr.isContainer()) {
	    rr.reindex(rec);
	} else {
	    JOptionPane.
		showMessageDialog(this, 
				  rr.getValue("identifier")+
				  " is not a container.",
				  "Error",
				  JOptionPane.ERROR_MESSAGE);
	}
    }
	

    /**
     * Delete the resources associated to the selected nodes.
     * Display an error message if there is no node selected or if
     * the resource is not editable.
     */
    protected void deleteSelectedResources() {
	PropertyManager pm = PropertyManager.getPropertyManager();
	TreePath path[] = removeDescendants(getSelectionPaths());
	if (path == null) {
	    JOptionPane.showMessageDialog(this, 
					  "No Resource selected",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
	if (path.length > 0) {
	    int result = 
		JOptionPane.showConfirmDialog(this, 
					      "Delete selected resource(s)?", 
					      "Delete Resource(s)", 
					      JOptionPane.YES_NO_OPTION);
	    if (result == JOptionPane.YES_OPTION) {
		DefaultTreeModel model = (DefaultTreeModel) getModel();
		for (int i = 0 ; i < path.length ; i++) {
		    RemoteResourceWrapper rrw = 
			getSelectedResourceWrapper(path[i]);
		    if (rrw == null)
			continue;
		    try {
			if (pm.isEditable(rrw)) {
			    deleteResource(rrw);
			    MutableTreeNode node = (MutableTreeNode) 
				path[i].getLastPathComponent();
			    model.removeNodeFromParent(node);
			} else {
			    String name = (String)
				rrw.getResource().getValue("identifier");
			    Message.showInformationMessage(this, name+
							   " is not editable");
			}
		    } catch (RemoteAccessException ex) {
			Message.showErrorMessage(this, ex);
			continue;
		    }
		}
	    }
	}
    }

    /**
     * Delete the resource wrapped by the given wrapper
     * @param rrw The RemoteResourceWrapper
     */
    protected void deleteResource(RemoteResourceWrapper rrw) 
	throws RemoteAccessException
    {
	rrw.getResource().delete();
    }

    /**
     * Display (in another frame) the reference documentation relative
     * to the resource associated to the selected node.
     * Display en error message (dialog) if no node is selected.
     */
    protected void showReferenceDocumentation() {
	try {
	    RemoteResourceWrapper selected = 
		getSelectedResourceWrapper();
	    if (selected == null) {
		JOptionPane.showMessageDialog(this, 
					      "No Resource selected",
					      "Error",
					      JOptionPane.ERROR_MESSAGE);
		return;
	    } else {
		String url = (String)
		    selected.getResource().getValue("help-url");
		MiniBrowser.showDocumentationURL(url, 
						 "Reference documentation");
	    }
	} catch (RemoteAccessException rae) {
	    Message.showErrorMessage(this, rae);
	} catch (Exception ex) {

	}
    }

    /**
     * A simle click occured on the node with the given path.
     * @param path The path where the click occured.
     */
    protected void simpleClick(TreePath path) {
    }

    /**
     * A double click occured on the node with the given path.
     * @param path The path where the double click occured.
     */
    protected void doubleClick(TreePath path) {
	if (path == null)
	    return;
	RemoteResourceWrapperNode node = 
	    (RemoteResourceWrapperNode) path.getLastPathComponent();
	RemoteResourceWrapper rrw = node.getResourceWrapper();
	PropertyManager pm = PropertyManager.getPropertyManager();
	if (pm.isEditable(rrw))
	    popupResource(node.getResourceWrapper());
	else {
	    try {
		String name = (String)
		    rrw.getResource().getValue("identifier");
		Message.showInformationMessage(this, name+" is not editable");
	    } catch (RemoteAccessException ex) {
		Message.showErrorMessage(this, ex);
	    }
	}
    }

    /**
     * Popup a dialog where the user can edit the resource properties.
     * @param rrw the wrapper if the resource to edit.
     */
    protected void popupResource(RemoteResourceWrapper rrw) {
	rrw.getServerBrowser().popupResource(rrw);
    }

    /**
     * Set the cursor.
     * @param a cursor type
     */ 
    protected void setCursor(int cursor) {
	rootNode.getResourceWrapper().getServerBrowser().setCursor(cursor);
    }

   
    /**
     * The popup menu action listener.
     */
    ActionListener pmal = new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	    setCursor(Cursor.WAIT_CURSOR);
	    String command = evt.getActionCommand();
	    if (command.equals("del")) {
		deleteSelectedResources();
	    } else if (command.equals("add")) {
		addResourceToSelectedContainer();
	    } else if (command.equals("reindex")) {
		reindexSelectedResources(true);
	    } else if (command.equals("reindex-locally")) {
		reindexSelectedResources(false);
	    } else if (command.equals("info")) {
		showReferenceDocumentation();
	    } else if (command.equals("edit")) {
		doubleClick(getSelectionPath());
	    }
	    setCursor(Cursor.DEFAULT_CURSOR);
	}
    };

    /**
     * Get the popup menu relative to the selected resource.
     * @param rrw the wrapper of the resource
     * @return a JPopupMenu instance
     */
    protected JPopupMenu getPopupMenu(RemoteResourceWrapper rrw) {

	JPopupMenu popupMenu = new JPopupMenu();

	boolean container  = false;
	boolean editable   = false;

	try {
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    container = rrw.getResource().isContainer();
	    editable = pm.isEditable(rrw);
	} catch (RemoteAccessException ex) {
	    container = false;
	}

	JMenuItem menuItem = null;

	if (container) {
	    menuItem = new JMenuItem("Reindex", Icons.reindexIcon);
	    menuItem.addActionListener(pmal);
	    menuItem.setActionCommand("reindex");
	    popupMenu.add(menuItem);

	    menuItem = new JMenuItem("Reindex Locally", Icons.reindexIcon);
	    menuItem.addActionListener(pmal);
	    menuItem.setActionCommand("reindex-locally");
	    popupMenu.add(menuItem);
	    
	    menuItem = new JMenuItem("Add resource", Icons.addIcon);
	    menuItem.addActionListener(pmal);
	    menuItem.setActionCommand("add");
	    popupMenu.add(menuItem);
	}

	if (editable) {
	    menuItem = new JMenuItem("Delete resource", Icons.deleteIcon);
	    menuItem.addActionListener(pmal);
	    menuItem.setActionCommand("del");
	    popupMenu.add(menuItem);

	    menuItem = new JMenuItem("Edit resource", Icons.editIcon);
	    menuItem.addActionListener(pmal);
	    menuItem.setActionCommand("edit");
	    popupMenu.add(menuItem);

	    popupMenu.addSeparator();
	}

	menuItem = new JMenuItem("Info", Icons.infoIcon);
	menuItem.addActionListener(pmal);
	menuItem.setActionCommand("info");
	popupMenu.add(menuItem);

	return popupMenu;
    }

    /**
     * Constructor
     * @param root The root node
     */
    protected ResourceTreeBrowser(RemoteResourceWrapperNode root) {
	super(root);
	this.rootNode = root;
	dropTarget = new DropTarget (this, this);
	setEditable(true);
	setLargeModel(true);
	setScrollsOnExpand(true);
	setUI(new ResourceTreeUI());
	addTreeWillExpandListener(twel);
	KeyStroke delK = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
	registerKeyboardAction(al, DELETE_RESOURCE_AC, delK, WHEN_FOCUSED);
	addMouseListener(mouseAdapter);
    }

    /**
     * Get a ResourceTreeBrowser.
     * @param rrw The root resource
     * @param rootName The root identifier.
     * @return a ResourceTreeBrowser instance
     */
    public static 
	ResourceTreeBrowser getResourceTreeBrowser(RemoteResourceWrapper rrw,
						   String rootName)
    {
	RemoteResourceWrapperNode rnode = 
	    new RemoteResourceWrapperNode(rrw, rootName);
	ResourceTreeBrowser treebrowser = new ResourceTreeBrowser(rnode);
	return treebrowser;
    }
}
