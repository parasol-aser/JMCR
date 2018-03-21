// FramedResourceHelper.java
// $Id: FramedResourceHelper.java,v 1.1 2010/06/15 12:25:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Properties;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.widgets.Icons;

import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * The resource editor.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class FramedResourceHelper extends ResourceHelper {

    /**
     * Our MenuBar.
     */
    class ResourceMenu extends JMenuBar implements ActionListener {
	
	private Window parent = null;

	final static String CLOSE_AC = "quit";
	final static String DEL_AC   = "del";
	final static String ADD_AC   = "add";
	final static String DOC_AC   = "doc";

	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    if (command.equals(CLOSE_AC)) {
		parent.dispose();
	    } else if ((command.equals(DOC_AC)) && (selected_rrw != null)) {
		try {
		    String url = (String)
			selected_rrw.getResource().getValue("help-url");
		    showReference(url);
		} catch (RemoteAccessException ex) {
		    ex.printStackTrace();
		}
	    } else if ((command.equals(DEL_AC)) && (selected_rrw != null)) {
		browser.deleteSelectedResources();
	    } else if ((command.equals(ADD_AC)) && (selected_rrw != null)) {
  		browser.addResourceToSelectedContainer();
	    }
	}

	protected void showReference(String url) {
	    try {
		MiniBrowser.showDocumentationURL(url, 
						 "Reference documentation");
	    } catch (Exception ex) {
		Message.showErrorMessage(this, ex);
	    }
	}

	ResourceMenu(Window parent) {
	    super();
	    this.parent = parent;
	    JMenu resource = new JMenu("Resource");
	    add(resource);

	    JMenuItem add = 
		new JMenuItem("Add frame to selected resource/frame", 
			      Icons.addIcon);
	    add.setActionCommand(ADD_AC);
	    add.addActionListener(this);
	    resource.add( add );

	    JMenuItem del = 
		new JMenuItem("Delete selected frame(s)", Icons.deleteIcon);
	    del.setActionCommand(DEL_AC);
	    del.addActionListener(this);
	    resource.add( del );

	    resource.addSeparator();

	    JMenuItem quit = new JMenuItem("Close Resource window",
					   Icons.closeIcon);
	    quit.setActionCommand(CLOSE_AC);
	    quit.addActionListener(this);
	    resource.add( quit );

	    JMenu help = new JMenu("Help");
	    //setHelpMenu not yet implemented (FIXME)
	    add(help);
	    JMenuItem ref  = new JMenuItem("Show reference documentation",
					   Icons.infoIcon);
	    ref.setActionCommand(DOC_AC);
	    ref.addActionListener(this);
	    help.add(ref);
	}
	
    }

    protected String                name         = null;
    protected JPanel                comp         = null;
    protected JPanel                attrs        = null;
    protected RemoteResourceWrapper rrw          = null;
    protected RemoteResourceWrapper selected_rrw = null;
    protected FrameBrowser          browser      = null;

    /**
     * Our internal TreeSelectionListener
     */
    TreeSelectionListener tsl = new TreeSelectionListener() {
	RemoteResourceWrapper current_rrw = null;

	public void valueChanged(TreeSelectionEvent e) {
	    if (e.isAddedPath()) {
		if (! browser.isDragging()) {
		    RemoteFrameWrapperNode node = 
		    (RemoteFrameWrapperNode)e.getPath().getLastPathComponent();
		    RemoteResourceWrapper rrw = node.getResourceWrapper();
		    current_rrw = rrw;
		    Thread updater = new Thread() {
			public void run() {
			    updateAttrs(current_rrw);
			}
		    };
		    updater.start();
		    //updateAttrs(rrw);
		} else {
		    attrs.invalidate();
		    attrs.removeAll();
		    attrs.add(new JLabel(" ", JLabel.CENTER));
		    attrs.validate();
		}
	    } else {
		selected_rrw = null;
		attrs.invalidate();
		attrs.removeAll();
		attrs.add(new JLabel("no frame selected", JLabel.CENTER));
		attrs.validate();
	    }
	}
    };

    /**
     * Get the helper title.
     * @return a String
     */
    public String getTitle() {
	return "Frames";
    }

    /**
     * Get the heper Component
     * @return a Component
     */
    public Component getComponent() {
	return comp;
    }

    /**
     * tells if the edited resource in the helper has changed
     * @return <strong>true</strong> if the values changed.
     * to get more informations about what has changed, you can use the 
     * three methods below.
     */
    public boolean hasChanged() {
	return false;
    }

    /**
     * set the current resource to be the original resource (ie: the
     * hasChanged() method must return <strong>false</false> now.
     * to do a "fine tuned" reset, use one of the three following method.
     */
    public void clearChanged() {
	
    }

    /**
     * commit the changes (if any)
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void commitChanges()
	throws RemoteAccessException
    {

    }

    /**
     * undo the not-yet-commited changes
     */
    public void resetChanges() {

    }

    /**
     * Update the AttributesHelper
     * @param rrw the RemoteResourceWrapper of the RemoteReource to edit.
     */
    protected void updateAttrs(RemoteResourceWrapper rrw) {
	selected_rrw = rrw;
	attrs.invalidate();
	attrs.removeAll();
	AttributesHelper helper = new AttributesHelper();
	try {
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    Properties props = pm.getEditorProperties(rrw);
	    helper.initialize(rrw, props);
	    attrs.add(helper.getComponent());
	} catch (RemoteAccessException ex) {
	    ex.printStackTrace();
	}
	attrs.validate();
    }

    /**
     * initialize the helper
     * @param r the ResourceWrapper containing the Resource edited with 
     * this helper
     * @param p some Properties, used to fine-tune the helper
     * @exception RemoteAccessException if a remote access error occurs.
     */
    public void initialize(org.w3c.jigadm.RemoteResourceWrapper rw, 
			   Properties p)
	throws RemoteAccessException 
    {
	boolean framed = false;
	rrw = (RemoteResourceWrapper) rw;
	try {
	    name = (String)rrw.getResource().getValue("identifier");
	    framed = rrw.getResource().isFramed();
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(rrw, ex);
	}
	if (name == null) {
	    comp = new JPanel();
	    return;
	}
	build(framed);
    }

    /**
     * Get the dedicated MenuBar
     * @param parent the Window parent.
     */
    public JMenuBar getMenuBar(Window parent) {
	return new ResourceMenu(parent);
    }

    /**
     * Build the interface
     * @param framed is the resource a FramedResource or not?
     */
    protected void build(boolean framed) {
	comp = new JPanel(new GridLayout(1,1));
	attrs = new JPanel(new GridLayout(1,1));
	attrs.setBorder(BorderFactory.createTitledBorder("Attributes"));

	if (framed) {
	    browser = FrameBrowser.getFrameBrowser(rrw, name);
	    browser.addTreeSelectionListener(this.tsl);
	    browser.setSelectionRow(0);
	    browser.setBorder(BorderFactory.createTitledBorder("Frames"));
	    browser.setSize(new Dimension(30,30));
	    JScrollPane scroll = new JScrollPane(browser);
	    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					      true,
					      scroll,
					      attrs);
	    split.setDividerLocation(100);
	    comp.add(split);
	} else {
	    comp.add(attrs);
	    updateAttrs(rrw);
	}
    }

}
