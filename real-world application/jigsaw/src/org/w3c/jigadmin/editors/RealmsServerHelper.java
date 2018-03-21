// RealmsServerHelper.java
// $Id: RealmsServerHelper.java,v 1.1 2010/06/15 12:25:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Properties;
import java.util.Vector;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.Message;

import org.w3c.jigsaw.admin.RemoteAccessException;

import org.w3c.tools.sorter.Sorter;
import org.w3c.tools.widgets.Utilities;

/**
 * The server helper dedicated to the Realms
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class RealmsServerHelper extends JPanel 
    implements ServerHelperInterface
{
    protected final static String ADD_USER_AC  = "add_user";
    protected final static String DEL_USER_AC  = "del_user";
    protected final static String DEL_REALM_AC = "del_realm";

    protected String                name     = null;
    protected String                tooltip  = null;
    protected RemoteResourceWrapper root     = null;
    protected RemoteResourceWrapper realmrrw = null;
    protected RemoteResourceWrapper userrrw  = null;
    protected Vector                realms   = null;
    protected Vector                users    = null;

    protected JComboBox  combo      = null;
    protected JPanel     usersPanel = null;
    protected JPanel     userPanel  = null;
    protected JList      usersList  = null;
    protected JTextField userT      = null;

    /**
     * Our internal ActionListener
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    Object obj = e.getSource();
	    if (obj == combo) {
		String realm = (String)combo.getSelectedItem();
		if (realms.contains(realm))
		selectRealm(realm);
		else if (realm.length() > 0)
		addRealm(realm);
	    } else if (e.getActionCommand().equals(ADD_USER_AC) ||
		       (obj == userT)) 
	    {
		String user = userT.getText();
		if ((user.length() > 0) && (! users.contains(user)))
		addUser(user);
	    } else if (e.getActionCommand().equals(DEL_USER_AC)) {
		Thread thread = new Thread() {
		    public void run() {
			deleteCurrentUser();
		    }
		};
		thread.start();
	    } else if (e.getActionCommand().equals(DEL_REALM_AC)) {
		Thread thread = new Thread() {
		    public void run() {
			deleteCurrentRealm();
		    }
		};
		thread.start();
	    }
	}
    };

    /**
     * Our internal ListSelectionListener
     */
    ListSelectionListener lsl = new ListSelectionListener() {
	public void valueChanged(ListSelectionEvent e) {
	    if (! e.getValueIsAdjusting()) {
		int idx = usersList.getSelectedIndex();
		selectUser((String)users.elementAt(idx));
	    }
	}    
    };

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
	this.root    = rrw;
	this.name    = name;
	this.tooltip = (String) p.get(TOOLTIP_P);
	build();
    }

    /**
     * Build the interface
     */
    protected void build() {
	removeAll();
	String srealms[] = null;
	try {
	    srealms = root.getResource().enumerateResourceIdentifiers();
	} catch (RemoteAccessException ex) {
	    while (root.getServerBrowser().shouldRetry(ex)) {
		try {
		    srealms = 
			root.getResource().enumerateResourceIdentifiers();
		    break;
		} catch (RemoteAccessException ex2) {
		    ex = ex2;
		}
	    }
	}
	if (srealms == null)
	    return;

	Sorter.sortStringArray(srealms, true);
	realms = new Vector(srealms.length);
	for (int i = 0 ; i < srealms.length; i++) 
	    realms.addElement(srealms[i]);

	setLayout(new BorderLayout());

	JPanel comboPanel = new JPanel();
	combo = new JComboBox(realms);
	combo.setEditable(true);
	combo.addActionListener(al);

	JLabel realmLabel1 = new JLabel("Enter the realm name or");
        JLabel realmLabel2 = new JLabel("select one from the list:");
        realmLabel1.setAlignmentX(CENTER_ALIGNMENT);
        realmLabel2.setAlignmentX(CENTER_ALIGNMENT);

	comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
        comboPanel.add(realmLabel1);
        comboPanel.add(realmLabel2);
        comboPanel.add(combo);
	TitledBorder border = BorderFactory.createTitledBorder("Realms");
	border.setTitleFont(Utilities.mediumBoldFont);
	comboPanel.setBorder(border);

	usersPanel = new JPanel(new BorderLayout(4,4));
	border = BorderFactory.createTitledBorder("Realm");
	border.setTitleFont(Utilities.mediumBoldFont);
	usersPanel.setBorder(border);

	JPanel realmsPanel = new JPanel(new BorderLayout());
	realmsPanel.add(comboPanel, BorderLayout.NORTH);
	realmsPanel.add(usersPanel, BorderLayout.CENTER);

	userPanel = new JPanel(new BorderLayout());
	initUserPanel();

	add(realmsPanel, BorderLayout.WEST);
	add(userPanel, BorderLayout.CENTER);
    }

    /**
     * Initialize the User Panel.
     */
    protected void initUserPanel() {
	userPanel.removeAll();
	userPanel.invalidate();
	TitledBorder border = BorderFactory.createTitledBorder("User");
	border.setTitleFont(Utilities.mediumBoldFont);
	userPanel.setBorder(border);
	userPanel.validate();
    }

    /**
     * Select (and display) the given realm.
     * @param realm the Realm to select
     */
    protected void selectRealm(String realm) {
	initUserPanel();
	usersPanel.removeAll();
	users = null;
	realmrrw = null;

	try {
	    realmrrw = root.getChildResource(realm);
	} catch (RemoteAccessException ex) {
	    while (root.getServerBrowser().shouldRetry(ex)) {
		try {
		    realmrrw = root.getChildResource(realm);
		    break;
		} catch (RemoteAccessException ex2) {
		    ex = ex2;
		}
	    }
	}
	
	if (realmrrw == null)
	    return;

	try {
	    String susers[] = 
		realmrrw.getResource().enumerateResourceIdentifiers();
	    Sorter.sortStringArray(susers, true);
	    users = new Vector(susers.length);
	    for (int i = 0 ; i < susers.length; i++) 
		users.addElement(susers[i]);
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}

	if (users == null)
	    return;

	//graphics...
	usersPanel.invalidate();
	if (usersList != null)
	    usersList.removeListSelectionListener(lsl);
	usersList = new JList(users);
	usersList.addListSelectionListener(lsl);
	usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	usersList.setBorder(BorderFactory.createLoweredBevelBorder());

	JButton addUserB = new JButton("Add user");
	addUserB.setMargin(Utilities.insets0);
	addUserB.setActionCommand(ADD_USER_AC);
	addUserB.addActionListener(al);
	
	JButton delRealmB = new JButton("Delete "+realm);
	delRealmB.setMargin(Utilities.insets0);
	delRealmB.setActionCommand(DEL_REALM_AC);
	delRealmB.addActionListener(al);

	userT = new JTextField(8);
	userT.addActionListener(al);

	JPanel p1 = new JPanel(new BorderLayout(1,4));
	p1.add(userT, BorderLayout.WEST);
	p1.add(addUserB, BorderLayout.CENTER);
	//p1.setBorder(BorderFactory.createEtchedBorder());

	JPanel p2 = new JPanel(new GridLayout(2,1));
	p2.add(p1);
	p2.add(delRealmB);

	TitledBorder border = 
	    BorderFactory.createTitledBorder("Realm: "+realm);
	border.setTitleFont(Utilities.mediumBoldFont);
	usersPanel.setBorder(border);
	usersPanel.add(new JScrollPane(usersList), BorderLayout.CENTER);
	usersPanel.add(p2, BorderLayout.SOUTH);
	usersPanel.validate();
    }

    /**
     * Select (and display) the given user.
     * @param realm the User to select
     */
    protected void selectUser(String user) {
	try {
	    if (realmrrw == null) //no realm selected
		return;
	    root.getServerBrowser().setCursor(Cursor.WAIT_CURSOR);
	    userrrw = realmrrw.getChildResource(user);
	    userPanel.removeAll();
	    userPanel.invalidate();
	    TitledBorder border = 
		BorderFactory.createTitledBorder("User: "+user);
	    border.setTitleFont(Utilities.mediumBoldFont);
	    userPanel.setBorder(border);
	    AttributesHelper helper = new AttributesHelper();
	    PropertyManager pm = PropertyManager.getPropertyManager();
	    Properties props = pm.getEditorProperties(userrrw);
	    helper.initialize(userrrw, props);

	    JButton delUserB = new JButton("Delete user "+user);
	    delUserB.setMargin(Utilities.insets0);
	    delUserB.setActionCommand(DEL_USER_AC);
	    delUserB.addActionListener(al);

	    userPanel.add(helper.getComponent(), BorderLayout.CENTER);
	    userPanel.add(delUserB, BorderLayout.SOUTH);
	    userPanel.validate();
	    root.getServerBrowser().setCursor(Cursor.DEFAULT_CURSOR);
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Add a user to the current selected realm.
     * @param user the user name
     */
    protected void addUser(String user) {
	try {
	    if (realmrrw == null)
		return;
	    realmrrw.getResource().
		registerResource(user,
				 "org.w3c.jigsaw.auth.AuthUser");
	    selectRealm((String)combo.getSelectedItem());
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Delete the selected user.
     */
    protected void deleteCurrentUser() {
	if (userrrw == null)
	    return;
	try {
	    userrrw.getResource().delete();
	    selectRealm((String)combo.getSelectedItem());
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Create a new Realm.
     * @param realm The name of the new realm.
     */
    protected void addRealm(String realm) {
	try {
	    root.getResource().
		registerResource(realm,
				 "org.w3c.jigsaw.auth.AuthRealm");
	    combo.invalidate();
	    combo.addItem(realm);
	    combo.validate();
	    selectRealm(realm);
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
    }

    /**
     * Delete the current realm.
     */
    protected void deleteCurrentRealm() {
	if (realmrrw == null) //no realm selected
	    return;
	try {
	    realmrrw.getResource().delete();
	    invalidate();
	    build();
	    validate();
	} catch (RemoteAccessException ex) {
	    Message.showErrorMessage(this, ex);
	}
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
    public RealmsServerHelper() {
    }

}
