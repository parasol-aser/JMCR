// ServerBrowser.java
// $Id: ServerBrowser.java,v 1.1 2010/06/15 12:27:17 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.gui ;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Properties;

import java.io.PrintStream;

import java.net.URL;

import org.w3c.jigadm.PropertyManager;
import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.AdminContext;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.sorter.Sorter;

import org.w3c.tools.widgets.MessagePopup;
import org.w3c.tools.widgets.NodeHandler;
import org.w3c.tools.widgets.TreeBrowser;
import org.w3c.tools.widgets.TreeNode;

class UrlPopup extends Frame implements ActionListener {
  ServerBrowser browser = null;
  Frame frame           = null;
  TextField url         = null;
  Button okB            = null; 
  Button cancelB        = null;
  boolean keepOnCancel  = true;

    private class Openner extends Thread {
	ServerBrowser browser = null;
	AdminContext  ac      = null;

	public void run() {
	    browser.open(ac);
	}

	private Openner(ServerBrowser browser,
			AdminContext ac) {
	    this.browser = browser;
	    this.ac = ac;
	}
    }

    private String getAdminUrl() {
	String adminUrl = url.getText();
	if (adminUrl.length() < 1)
	    return null;
	else  if (! adminUrl.startsWith("http")) {
	    adminUrl = "http://"+adminUrl;
	}
	return adminUrl;
    }

    public void actionPerformed(ActionEvent evt) {
	Object target = evt.getSource();
	if ((target == url) || (target == okB)) {
	    String adminUrl = getAdminUrl();
	    if (adminUrl != null) {
		AdminContext ac = null;
		try {
		    ac = new AdminContext(new URL(adminUrl));
		} catch (RemoteAccessException ex) {
		    browser.errorPopup("RemoteAccessException", ex);
		    //	  (new MessagePopup("RemoteAccessException : "+
		    //			    ex.getMessage())).show();
		    ex.printStackTrace();
		    return;
		} catch (java.net.MalformedURLException ex) {
		    browser.errorPopup("MalformedURLException", ex);
		    //	(new MessagePopup("MalformedURL : "+adminUrl)).show();
		    return;
		}
		dispose();
		frame.setTitle("Server Browser: " +adminUrl);
		frame.show();
		( new Openner(browser,ac)).start();
	    }
	} else  if (target == cancelB) {
	    if (! keepOnCancel) {
		frame.dispose();
		WindowCloser.windows--;
		if (WindowCloser.windows < 0) {
		    dispose();
		    System.exit(0);
		}
	    }
	    dispose();
	}
    }

    /**
     * create the URL popup
     */

    UrlPopup(String title, 
	     ServerBrowser browser, 
	     Frame frame, 
	     boolean keepOnCancel) {
	    super(title);
	    setBackground(Color.lightGray);
	    this.browser = browser;
	    this.frame = frame;
	    this.keepOnCancel = keepOnCancel;
	    Label label = new Label("Location :");
	    url = new TextField(20);
	    url.addActionListener(this);
	    okB = new Button("Ok");
	    okB.addActionListener(this);
	    cancelB = new Button("Cancel");
	    cancelB.addActionListener(this);

	    GridBagLayout gbl = new GridBagLayout();
	    GridBagConstraints gbc = new GridBagConstraints();

	    setLayout(gbl);
	    gbc.insets = new Insets(10,0,10,0);
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.gridwidth = GridBagConstraints.RELATIVE;
	    gbl.setConstraints(label,gbc);
	    add(label);

	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbl.setConstraints(url,gbc);
	    add(url);

	    gbc.gridwidth = GridBagConstraints.RELATIVE;
	    gbc.anchor = GridBagConstraints.EAST;
	    gbl.setConstraints(okB,gbc);
	    add(okB);

	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbc.anchor = GridBagConstraints.WEST;
	    gbl.setConstraints(cancelB,gbc);
	    add(cancelB);

	    pack();
	    show();

	    url.requestFocus();
    }
}

class WindowCloser extends WindowAdapter {

    protected static int windows = 0;

    Frame window = null;

    public void windowClosing(WindowEvent e) {
	if (e.getWindow() == window) {
	    window.setVisible(false);
	    window.dispose();
	    windows--;
	    if (windows < 0)
		System.exit(0);
	}
    }

    WindowCloser(Frame window) {
	this.window = window;
    }

}

class ServerMenu extends MenuBar implements ActionListener {

  ServerBrowser browser = null;

    public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	if (command.equals("open")) {
	    new UrlPopup("Open new Admin-server", browser, 
			 (Frame)getParent(), true);
	} else  if(command.equals("new")) {
	    try { 
		Frame f = new Frame("New Server Browser");
		f.setBackground(Color.lightGray);
		Panel editor = new Panel();
		Panel newbrowser = new Panel(new BorderLayout());
		TreeListener tl = new TreeListener(editor);
		ServerBrowser sb = null;
		sb = new ServerBrowser(f,tl);
		Scrollbar sv = new Scrollbar(Scrollbar.VERTICAL);
		sb.setVerticalScrollbar(sv);
		Scrollbar sh = new Scrollbar(Scrollbar.HORIZONTAL);
		sb.setHorizontalScrollbar(sh);
		newbrowser.add("Center", sb);
		newbrowser.add("East", sv);
		newbrowser.add("South", sh);
		editor.setLayout(new BorderLayout());
		// Add a menubar
		ServerMenu menu = new ServerMenu(sb);
		GridLayout g = new GridLayout(1, 2) ;
		f.setLayout(g);
		f.setMenuBar(menu);
		f.add(newbrowser);
		f.add(editor);
		f.setSize(new Dimension(850,600));
		WindowCloser.windows++;
		f.addWindowListener(new WindowCloser(f));
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }  
	} else  if(command.equals("close")) {
	    Frame cont = (Frame)getParent();
	    cont.setVisible(false);
	    cont.dispose();
	    WindowCloser.windows--;
	    if (WindowCloser.windows < 0)
		System.exit(0);
	} else  if(command.equals("quit")) {
	    Frame cont = (Frame)getParent();
	    cont.setVisible(false);
	    cont.dispose();
	    WindowCloser.windows = 0;
	    System.exit(0);
	}
    }

    ServerMenu(ServerBrowser browser) {
	super();
	this.browser = browser;
	Menu server = new Menu("Admin-Server");
	add(server);
	MenuItem open = new MenuItem("Open");
	open.setActionCommand("open");
	open.addActionListener(this);
	server.add( open );
	MenuItem newOpen = new MenuItem("Open in new window");
	newOpen.setActionCommand("new");
	newOpen.addActionListener(this);
	server.add( newOpen );
	server.addSeparator();
	MenuItem close = new MenuItem("Close window");
	close.setActionCommand("close");
	close.addActionListener(this);
	server.add( close );
	MenuItem quit = new MenuItem("Exit");
	quit.setActionCommand("quit");
	quit.addActionListener(this);
	server.add( quit );
    }
}

public class ServerBrowser extends TreeBrowser implements NodeHandler {

    class Expander extends Thread {
	TreeBrowser browser;
	TreeNode nd;

        public void run() {
	    if(getLock()) {
		notifyExpander(browser, nd);
		unlock();
	    }
	}

        Expander(TreeBrowser browser, TreeNode nd) {
	    this.browser = browser;
	    this.nd = nd;
	}
    }

    public static final boolean debug = false;
    Image diricon = null;
    Image diropenedicon = null;
    Image fileicon = null;
    AdminContext admin = null;
    TreeListener tl = null;
    TreeNode lastn = null;
    RemoteResourceWrapper rootResource;
    boolean locked;
    private Frame popup = null;

    protected void errorPopup(String name, Exception ex) {
      (new MessagePopup(name+" : "+ex.getMessage())).show();
    }

    /**
     * gets a lock to avoid adding node while removing other nodes
     * it sets also the Cursor to WAIT_CURSOR
     */

    protected synchronized boolean getLock() {
	if(locked)
	    return false;
	setCursor(Frame.WAIT_CURSOR);
	locked = true;
	return true;
    }

    /**
     * release the lock and sets the Cursor to the default
     */

    protected synchronized void unlock() {
	locked = false;
	setCursor(Frame.DEFAULT_CURSOR);
    }

    // get rid of the Auth Popup

    protected void dispose(boolean Ok) {
      if (! Ok) {
	Frame f = getFrame(this);
	if (f != null)
	  f.dispose();
	WindowCloser.windows--;
	popup.dispose();
	if (WindowCloser.windows < 0)
	  System.exit(0);
      } else  if (popup != null) {
	popup.dispose();
	popup = null;
      }
    } 

    // pops up a new Auth popup

    public void popupDialog(String name) {
	if(popup == null) {
	    AuthPopup ap = new AuthPopup(this, name);
	    popup = new Frame("Authorization for jigadm");
	    popup.setBackground(Color.lightGray);
	    popup.setSize(new Dimension(300, 200));
	    popup.setLayout(new BorderLayout());
	    popup.add("Center", ap);
	    popup.show();
	    ap.user.requestFocus();
	    while(!ap.waitForCompletion());
	}
    }    

    /**
     * give the Root Resource of the browser
     */

    public RemoteResourceWrapper getRootWrapper() {
	return rootResource;
    }

    public void renameNode(RemoteResourceWrapper rw, String label) {
        try {
	  if (rw.getResource().isContainer()) {
	    (new MessagePopup("WARNING: you have changed the identifier. "+
			      "To access the sons, close and reopen the node")
	      ).show();
	  }
	} catch (RemoteAccessException ex) {
	  // ??
	}
	TreeNode tn = getNode(rw);
	// if it is a visible node, change the label and repaint
	if (tn != null) {
	    tn.setLabel(label);
	    repaint();
	}
    }

    public void removeNode(RemoteResourceWrapper rw) {
	if(getLock()) {
	  if (getNode(rw) != null) {
	    removeBranch(getNode(rw));
	    tl.nodeRemoved(rw);
	  }
	  unlock();
	  repaint();
	}
    }

    public void insertNode(RemoteResourceWrapper father, 
			   RemoteResourceWrapper son,
			   String name) {
	TreeNode fatherNode;
	boolean ic = false;

	if(father == null)
	    System.out.println("Error null father");
	fatherNode = getNode(father);
	if(fatherNode.getChildren() == TreeNode.NOCHILD)
	    return;
	
	if (fatherNode == null)
	    return; // this should never happen, but...
	try {
	    ic = son.getResource().isContainer();
	} catch (RemoteAccessException ex) {
	    // fancy thing
	    errorPopup("RemoteAccessException",ex);
	}

	if(ic)
	    insert(fatherNode, son, this, name, diricon);
	else
	    insert(fatherNode, son, this, name, fileicon);
 	repaint();
    }

    protected RemoteResourceWrapper getResources(
	RemoteResourceWrapper rw,String name) {

	RemoteResource resource = null;
	if(rw != null) {
	    try {
		resource = rw.getResource().loadResource(name);
	    } catch (RemoteAccessException ex) {
	      errorPopup("RemoteAccessException", ex);
	      ex.printStackTrace();
	    }
	}
	return new RemoteResourceWrapper(rw, resource, this);
    }

    private final Image getImage(String name) {
	Image img;
	img = Toolkit.getDefaultToolkit().getImage(name);
	return img;
    }

    private final Frame getFrame(Component c) {
	while(! (c instanceof Frame)) {
	    c = c.getParent();
	    if (c == null)
	      return null;
	}
	return (Frame)c;
    }
    /*   private final*/ public void setCursor(int cursor) {
	getFrame(this).setCursor(new Cursor(cursor));
	Toolkit.getDefaultToolkit().sync();
    }

    protected void open(AdminContext ac) {
	RemoteResource rr = null;
	admin   = ac;
	locked = false;
	boolean authorized = false;

	if (rootResource != null)
	    removeNode(rootResource);
	tl.focusChanged( null );
	while (!authorized) {
	    try {
		authorized = true;
		ac.initialize();
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    ex.printStackTrace();
		}
	    } finally {
		if(!authorized) {
		    popupDialog("admin");
		}
	    }
	}
	try {
	    rr = ac.getAdminResource();
	} catch (RemoteAccessException ex) { 
	    errorPopup("RemoteAccessException", ex);
	    //( new MessagePopup("RemoteAccessException : "+
	    //ex.getMessage())).show();
	    return;
	}
	rootResource = new RemoteResourceWrapper(rr, this);
	initialize(rootResource, "Root", this, diricon);
	tl.focusChanged(rootResource);
	repaint();
    }

    public ServerBrowser(Frame frame, TreeListener tl) {
	PropertyManager pm = PropertyManager.getPropertyManager();
	this.tl = tl;

	diricon = getImage(pm.getIconLocation("smalldir"));
	fileicon = getImage(pm.getIconLocation("smallfile"));
	diropenedicon = getImage(pm.getIconLocation("smalldiropened"));

	new UrlPopup("Open Admin Server : ", this, frame, false);
    }

    public ServerBrowser(AdminContext ac, TreeListener tl) {
	boolean authorized = false;
	RemoteResource rr = null;
	PropertyManager pm = PropertyManager.getPropertyManager();
	admin   = ac;
	this.tl = tl;

	locked = false;
	diricon = getImage(pm.getIconLocation("smalldir"));
	fileicon = getImage(pm.getIconLocation("smallfile"));
	diropenedicon = getImage(pm.getIconLocation("smalldiropened"));
	while (!authorized) {
	    try {
		authorized = true;
		ac.initialize();
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    ex.printStackTrace();
		}
	    } finally {
		if(!authorized) {
		    popupDialog("admin");
		}
	    }
	}
	try {
	    rr = ac.getAdminResource();
	} catch (RemoteAccessException ex) { 
	    // Unable to connect for whatever reason... exit!
	    ex.printStackTrace();
	    System.exit(0);
	}
	rootResource = new RemoteResourceWrapper(rr, this);
	initialize(rootResource, "Root", this, diricon);
    }

    public void notifySelect(TreeBrowser browser, TreeNode nd) {
	if(tl != null) {
	    tl.editedChanged(this, (RemoteResourceWrapper)nd.getItem());
	}
	browser.unselect(lastn);
 	browser.select(nd);
	browser.repaint();
	lastn = nd;
   }

   /**
    * Handles Select notifications.
    *
    * we simply select the node and redraw the browser.
    */    
    public void notifyExecute(TreeBrowser browser, TreeNode node) {
	if(tl != null) {
	    tl.focusChanged((RemoteResourceWrapper)node.getItem());
	}
	if(!node.equals(lastn)) {
	    browser.unselect(lastn);
	    lastn = null;
	}
	browser.repaint();
    }

    public void notifyExpand(TreeBrowser browser, TreeNode nd) {
	(new Expander(browser, nd)).start();
    }

   /**
    * Handles Expand notifications
    *
    * if the node is a directory, we list its content and insert the
    * directories and files in the browser.
    */
    public void notifyExpander(TreeBrowser browser, TreeNode nd) {
	if(tl != null) {
	    tl.focusChanged((RemoteResourceWrapper)nd.getItem());
	}
	RemoteResourceWrapper rrw = null;
	RemoteResource rr = null;
	boolean ic = false;
	boolean authorized;

	rrw = (RemoteResourceWrapper)nd.getItem();
	if(rrw == null)
	    return;
	rr = rrw.getResource();
	try {
	    ic = rr.isContainer();
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
	if(ic) {
	    String names[] = null;
	    setCursor(Frame.WAIT_CURSOR);
	    authorized = false;
	    while(!authorized) {
		authorized = true;
		try {
		    names = rr.enumerateResourceIdentifiers();
		} catch (RemoteAccessException ex) {
		    if( ex.getMessage().equals("Unauthorized")) {
			authorized = false;
		    } else {
		      names = new String[0];
		      errorPopup("RemoteAccessException", ex);
		      ex.printStackTrace();
		    }
		} finally {
		    if(!authorized) {
			popupDialog("admin");
		    }
		}
	    }
	    Sorter.sortStringArray(names, true);
	    if (debug)
		System.out.println("Found "+names.length+" identifiers");
            for(int i = 0; i <names.length; i++) {
		boolean nic = false;
		RemoteResourceWrapper nrrw = getResources(rrw, names[i]);
		RemoteResource nrr = nrrw.getResource();
		try {
		    nic = nrr.isContainer();
		} catch(Exception ex) {
		    ex.printStackTrace();
		}
		if(nic) {
		    browser.insert(nd, nrrw, this, names[i], diricon);
		}
		else {
		    browser.insert(nd, nrrw, this, names[i], fileicon);
		}
	    }
	    if(!nd.equals(lastn)) {
		browser.unselect(lastn);
		lastn = null;
	    }
	    setCursor(Frame.DEFAULT_CURSOR);
	    browser.repaint();
	}
	if(isDirectory(this, nd))
	   nd.setIcon(diropenedicon);
    }

    public boolean isDirectory(TreeBrowser browser, TreeNode nd) {
	RemoteResourceWrapper rrw = null;
	boolean ic = false;
	rrw = (RemoteResourceWrapper)nd.getItem();
	if(rrw == null)
	    return false;
	try {
	    return rrw.getResource().isContainer();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return false;
    }

   /**
    * Handles Collapse notifications
    *
    * we simply collapse the given node and repaint the browser.
    */    
    public void notifyCollapse(TreeBrowser browser, TreeNode node) {
	if(getLock()) {
	    if(tl != null) {
		tl.focusChanged((RemoteResourceWrapper)node.getItem());
	    }
	    browser.collapse(node);
	    if(!node.equals(lastn)) {
		browser.unselect(lastn);
		lastn = null;
	    }
	    unlock();
	    browser.repaint();
	    node.setIcon(diricon);
	}
    }

    static public void main(String args[]) {
	String baseURL=null;
	String jigadmRoot = null;
	
	for (int i = 0 ; i < args.length ; i++) {
	    if (args[i].equals("-root")) {
		Properties p = System.getProperties();
		jigadmRoot = args[++i];
		p.put(PropertyManager.ROOT_P, jigadmRoot);
		System.setProperties(p);
	    }
	    baseURL = args[i];
	}
	try {
	    Frame f = new Frame("Server Browser: " + baseURL);
	    f.setBackground(Color.lightGray);
	    Panel editor = new Panel();
	    Panel browser = new Panel(new BorderLayout());
	    TreeListener tl = new TreeListener(editor);
	    ServerBrowser sb = null;
	    Scrollbar sv = new Scrollbar(Scrollbar.VERTICAL);
	    Scrollbar sh = new Scrollbar(Scrollbar.HORIZONTAL);
	    try {
	      AdminContext ac = new AdminContext(new URL(baseURL));
	      sb = new ServerBrowser(ac, tl);
	      sb.setVerticalScrollbar(sv);
	      sb.setHorizontalScrollbar(sh);
	      browser.add("Center", sb);
	      browser.add("East", sv);
	      browser.add("South", sh);
	      editor.setLayout(new BorderLayout());
	      ServerMenu menu = new ServerMenu(sb);

  	      GridLayout g = new GridLayout(1, 2) ;
  	      f.setLayout(g);
  	      f.setMenuBar(menu);
  	      f.add(browser);
  	      f.add(editor);	      
  	      f.setSize(new Dimension(850,600));
	      f.addWindowListener(new WindowCloser(f));
  	      f.show();

	    } catch (java.net.MalformedURLException ex) {
	      sb = new ServerBrowser(f,tl);
	      sb.setVerticalScrollbar(sv);
	      sb.setHorizontalScrollbar(sh);
 	      browser.add("Center", sb);
	      browser.add("East", sv);
	      browser.add("South", sh);
	      editor.setLayout(new BorderLayout());
	      ServerMenu menu = new ServerMenu(sb);
	      GridLayout g = new GridLayout(1, 2) ;
	      f.setLayout(g);
	      f.setMenuBar(menu);
	      f.add(browser);
	      f.add(editor);
	      f.setSize(new Dimension(850,600));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}


