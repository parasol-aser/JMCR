// ServerBrowser.java
// $Id: ServerBrowser.java,v 1.1 2010/06/15 12:21:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui; 

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Properties;

import java.net.URL;
import java.net.MalformedURLException;

import org.w3c.jigsaw.admin.AdminContext;
import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.gui.Message;
import org.w3c.jigadmin.gui.slist.ServerList;
import org.w3c.jigadmin.gui.slist.ServerListModel;
import org.w3c.jigadmin.gui.slist.ServerListListener;
import org.w3c.jigadmin.editors.ServerEditorInterface;
import org.w3c.jigadmin.editors.ServerEditorFactory;
import org.w3c.jigadmin.editors.FramedResourceHelper;
import org.w3c.jigadmin.widgets.DnDJPanel;
import org.w3c.jigadmin.widgets.Icons;

class WindowCloser extends WindowAdapter {

    protected static int windows = 0;

    public synchronized static void close(JFrame frame) {
	windows--;
	frame.setVisible(false);
	frame.dispose();
	if (windows < 0)
	    System.exit(0);
    }

    public void windowClosing(WindowEvent e) {
	close((JFrame)e.getWindow());
    }

}

class ServerMenu extends JMenuBar implements ActionListener {

    ServerBrowser browser = null;

    protected String getAdminURL() {
	String url = JOptionPane.showInputDialog(this, 
						 "Admin server URL",
						 "Open",
						 JOptionPane.PLAIN_MESSAGE);
	if ((url != null) && (! url.startsWith("http://")))
	    url = "http://"+url;
	return url;
    }

    public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	if (command.equals("open")) {
	    String adminurl = getAdminURL();
	    if (adminurl == null)
		return;
	    try {
		URL url = new URL(adminurl);
		browser.open(url);
	    } catch (MalformedURLException ex) {
		JOptionPane.showMessageDialog(this, 
					      adminurl+" is not a valid URL",
					      "Invalid URL",
					      JOptionPane.ERROR_MESSAGE);
	    }
	} else if(command.equals("new")) {
	    String adminurl = getAdminURL();
	    if (adminurl == null)
		return;
	    URL url = null;
	    try {
		url = new URL(adminurl);
	    } catch (MalformedURLException ex) {
		JOptionPane.showMessageDialog(this, 
					      adminurl+" is not a valid URL",
					      "Invalid URL",
					      JOptionPane.ERROR_MESSAGE);
	    }
	    JFrame        frame = new JFrame("Server Browser: " + adminurl);
	    ServerBrowser sb    = new ServerBrowser(frame);
	    frame.getContentPane().add(sb, BorderLayout.CENTER);
	    frame.setVisible(true);
	    sb.open(url);
	    WindowCloser.windows++;
	} else if(command.equals("close")) {
	    JFrame cont = browser.frame;
	    cont.setVisible(false);
	    cont.dispose();
	    WindowCloser.windows--;
	    if (WindowCloser.windows < 0)
		System.exit(0);
	} else if(command.equals("quit")) {
	    JFrame cont = browser.frame;
	    cont.setVisible(false);
	    cont.dispose();
	    WindowCloser.windows = 0;
	    System.exit(0);
	} else if (command.equals("about")) {
	    AboutJigAdmin.show(this);
	}
    }

    ServerMenu(ServerBrowser browser) {
	super();
	this.browser = browser;
	JMenu server = new JMenu("JigAdmin");
	add(server);
	JMenuItem about = new JMenuItem("About JigAdmin");
	about.setActionCommand("about");
	about.addActionListener(this);
	server.add( about );
	server.addSeparator();
	JMenuItem open = new JMenuItem("Open");
	open.setActionCommand("open");
	open.addActionListener(this);
	server.add( open );
	JMenuItem newOpen = new JMenuItem("Open in new window");
	newOpen.setActionCommand("new");
	newOpen.addActionListener(this);
	server.add( newOpen );
	server.addSeparator();
	JMenuItem close = new JMenuItem("Close window");
	close.setActionCommand("close");
	close.addActionListener(this);
	server.add( close );
	JMenuItem quit = new JMenuItem("Exit");
	quit.setActionCommand("quit");
	quit.addActionListener(this);
	server.add( quit );
    }
}

/**
 * The ServerBrowser.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */

public class ServerBrowser extends JPanel implements ServerListListener {

    private class Openner extends Thread {

	URL adminURL = null;

	public void run() {
	    setAdminURL(adminURL);
	}

	private Openner(URL adminURL) {
	    this.adminURL = adminURL;
	}
    }

    protected JFrame     frame       = null;
    protected JDialog    popup       = null;
    protected JPanel     serverPanel = null;
    protected ServerList serverList  = null;

    private AdminContext admin = null;

    RemoteResourceWrapper rootResource = null;

    /**
     * Constructor.
     * @param frame the JFrame containing ServerBrowser
     * @param ac The Admin context
     * @see org.w3c.jigsaw.admin.AdminContext
     */
    public ServerBrowser(JFrame frame, AdminContext ac) {
	//set our variables
	this.frame = frame;
	this.admin = ac;
	//update our frame.
	frame.addWindowListener(new WindowCloser());
	frame.setJMenuBar(this.getMenuBar());
	frame.setSize(800, 600);
	//initialization
	initialize();
	//build the interface
	build();
    }

    /**
     * Constructor.
     * @param frame the JFrame containing ServerBrowser
     */
    protected ServerBrowser(JFrame frame) {
	this.frame = frame;
	frame.addWindowListener(new WindowCloser());
	frame.setJMenuBar(this.getMenuBar());
	frame.setSize(800, 600);
    }

    /**
     * Get the frame of ServerBrowser.
     * @return a JFrame instance
     */
    public JFrame getFrame() {
	return frame;
    }

    /**
     * Open the given URL (in another thread)
     * @param admin the URL of the admin server.
     */
    protected void open(URL admin) {
	(new Openner(admin)).start();
    }

    /**
     * Set the admin server URL.
     * @param adminurl The admin server URL.
     */
    protected void setAdminURL(URL adminurl) {
	try {
	    setCursor(Cursor.WAIT_CURSOR);
	    frame.invalidate();
	    this.removeAll();
	    this.admin = new AdminContext(adminurl);
	    initialize(); 
	    build();
	    RemoteResourceWrapper rrw = serverList.getModel().
		getServer(ServerListModel.ADMIN_SERVER_NAME);
	    serverSelected(ServerListModel.ADMIN_SERVER_NAME, rrw);
	    frame.validate();
	    setCursor(Cursor.DEFAULT_CURSOR);
	    frame.setTitle("Server Browser : "+adminurl);
	} catch (RemoteAccessException ex) {
	    JOptionPane.showMessageDialog(this, 
					  ex.getMessage(),
					  "RemoteAccessException",
					  JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * Initialize SErverBrowser.
     */
    protected void initialize() {
	//check authorization
	boolean        authorized = false;
	RemoteResource rr         = null;
	while (!authorized) {
	    try {
		authorized = true;
		admin.initialize();
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    Message.showErrorMessage(this, ex);
		}
	    } finally {
		if(!authorized) {
		    popupPasswdDialog("admin");
		}
	    }
	}
	try {
	    rr = admin.getAdminResource();
	} catch (RemoteAccessException ex) { 
	    // Unable to connect for whatever reason... exit!
	    ex.printStackTrace();
	    System.exit(0);
	}
	rootResource = new RemoteResourceWrapper(rr, this);
	//build interface
    }

    /**
     * Build the interface.
     */
    protected void build() {
	serverPanel = new DnDJPanel(new CardLayout());
	serverPanel.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
	serverList = getServerList();

	if (serverList == null)
	    return;
	serverList.addServerListListener(this);

	String servers[] = serverList.getModel().getServers();
	for (int i = 0 ; i < servers.length ; i++) {
	    String server = servers[i];
	    RemoteResourceWrapper rrw = 
		serverList.getModel().getServer(server);
	    //no way to get the component from the cardLayout :(
	    ServerEditorInterface editor = 
		ServerEditorFactory.getServerEditor(server, this, rrw);
	    serverPanel.add(server, editor.getComponent());
	}

	JPanel serverListPanel = new JPanel( new BorderLayout() );
	serverListPanel.add(serverList, BorderLayout.NORTH);
	serverListPanel.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));

	setLayout(new BorderLayout());
	add(serverListPanel, BorderLayout.WEST);
	add(serverPanel, BorderLayout.CENTER);
	setBorder(BorderFactory.createLoweredBevelBorder());
    }

    /**
     * Show the configuration of the given server
     * @param name The server name
     * @param rrw The server RemoteResourceWrapper
     */
    public void serverSelected(String name, RemoteResourceWrapper rrw) {
	setCursor(Cursor.WAIT_CURSOR);
	((CardLayout)serverPanel.getLayout()).show(serverPanel, name);
	ServerEditorFactory.updateServerEditor(name, this, rrw);
	setCursor(Cursor.DEFAULT_CURSOR);
    }

    /**
     * Get the ServerList.
     * @return a ServerList instance
     * @see org.w3c.jigadmin.gui.slist.ServerList
     */
    protected ServerList getServerList() {
	boolean    authorized = false;
	ServerList list       = null;

	while (! authorized) {
	    try {
		authorized = true;
		list = new ServerList(getRootWrapper(), Icons.serverIcon);
	    } catch (RemoteAccessException ex) {
		if(ex.getMessage().equals("Unauthorized")) {
		    authorized = false;
		} else {
		    Message.showErrorMessage(this, ex);
		}
	    } finally {
		if(!authorized) {
		    popupPasswdDialog("admin");
		}
	    }
	}
	return list;
    }

    /**
     * give the Root Resource of the browser
     */
    public RemoteResourceWrapper getRootWrapper() {
	return rootResource;
    }

    /**
     * Should I retry?
     * @param ex the RemoteAccessException that occured
     * @return a boolean
     */
    public boolean shouldRetry(RemoteAccessException ex) {
	if(ex.getMessage().equals("Unauthorized")) {
	    popupPasswdDialog("admin");
	    return true;
	} else {
	    Message.showErrorMessage(this, ex);
	    return false;
	}
    }

    /**
     * Set The Cursor.
     * @param cursor The Cursor type
     */
    public void setCursor(int cursor) {
	frame.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    /**
     * Popup a dialog that allows the user to enter his username/password.
     * @param name the realm name
     */
    public void popupPasswdDialog(String name) {
	if (popup == null) {
	    AuthPanel ap   = new AuthPanel(this, name);
	    popup = new JDialog(frame, "Authorization for JigAdmin", false);
	    Container cont = popup.getContentPane();
	    cont.setLayout(new GridLayout(1,1));
	    cont.add(ap);
	    popup.setSize(new Dimension(300, 220));
	    popup.show();
	    ap.getFocus();
	    while(! ap.waitForCompletion());
	}
    }

    /**
     * Popup a dialog that allows the user to edit some resource properties.
     * @param rrw the RemoteResourceWrapper of the resource to edit.
     */
    public void popupResource(RemoteResourceWrapper rrw) {
	try {
	    setCursor(Cursor.WAIT_CURSOR);
	    JDialog popres = new JDialog(frame, "Edit Resource", false);
	    FramedResourceHelper helper = new FramedResourceHelper();
	    popres.setJMenuBar(helper.getMenuBar(popres));
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    Properties props = pm.getEditorProperties(rrw);
	    helper.initialize(rrw, props);
	    Container cont = popres.getContentPane();

	    cont.setLayout(new GridLayout(1,1));
	    cont.add(helper.getComponent());
	    popres.setSize(new Dimension(500, 550));
	    popres.setLocationRelativeTo(this);
	    popres.show();
	    setCursor(Cursor.DEFAULT_CURSOR);
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Dispose the popupPasswdDialog and close the frame.
     * @param oK if true dispose omly the dialog.
     */
    protected void dispose(boolean Ok) {
	if (! Ok) {
	    WindowCloser.close(frame);
	    popup.dispose();
	    popup = null;
	} else if (popup != null) {
	    popup.dispose();
	    popup = null;
	}
    }

    /**
     * Returns the AdminContext.
     * @return an AdminContext instance
     * @see org.w3c.jigsaw.admin.AdminContext
     */
    protected AdminContext getAdminContext() {
	return admin;
    }

    /**
     * Get the ServerBrowser MenuBar.
     * @return a JMenuBar instance.
     */
    public JMenuBar getMenuBar() {
	return new ServerMenu(this);
    }

    public static void unBoldSpecificFonts() {
	Font f = new Font("Dialog",Font.PLAIN,12); 

	UIManager.put("Button.font",f); 
	UIManager.put("CheckBox.font",f); 
	UIManager.put("CheckBoxMenuItem.font",f); 
	UIManager.put("ComboBox.font",f); 
	UIManager.put("DesktopIcon.font",f); 

	UIManager.put("InternalFrame.font",f); 
	UIManager.put("InternalFrame.titleFont",f); 
	UIManager.put("Label.font",f); 
	UIManager.put("Menu.font",f); 
	UIManager.put("MenuBar.font",f); 

	UIManager.put("MenuItem.font",f); 
	UIManager.put("ProgressBar.font",f); 
	UIManager.put("RadioButton.font",f); 
	UIManager.put("RadioButtonMenuItem.font",f); 
	UIManager.put("TabbedPane.font",f); 

	UIManager.put("TitledBorder.font",f); 
	UIManager.put("ToggleButton.font",f); 
	UIManager.put("ToolBar.font",f); 
    } 

    /**
     * Run ServerBrowser.
     */
    public static void main(String args[]) {
	String baseURL=null;
	String jigadmRoot = null;
	
	for (int i = 0 ; i < args.length ; i++) {
	    if (args[i].equals("-root")) {
		Properties p = System.getProperties();
		jigadmRoot = args[++i];
		p.put(PropertyManager.ROOT_P, jigadmRoot);
		System.setProperties(p);
	    } else {
		baseURL = args[i];
	    }
	}
	if (baseURL == null)
	    baseURL = "http://localhost:8009/";
	URL bu = null; 
	try {
	    bu = new URL (baseURL);
	    if (bu.getFile().length() == 0) {
		bu = new URL(bu, "/");
	    }
	} catch (MalformedURLException ex) {
	    System.err.println("Invalid URL : "+baseURL);
	}
	if (bu != null) {
	    try {
		unBoldSpecificFonts();
		AdminContext  ac    = new AdminContext(bu);
		JFrame        frame = new JFrame("Server Browser: " + baseURL);
		ServerBrowser sb    = new ServerBrowser(frame, ac);
		frame.getContentPane().add(sb, BorderLayout.CENTER);
		frame.setVisible(true);
	    } catch (RemoteAccessException raex) {
		raex.printStackTrace();
	    }
	}
    }
}
