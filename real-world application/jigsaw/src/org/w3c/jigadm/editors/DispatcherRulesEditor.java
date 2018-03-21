// DispatcherRulesEditor.java
// $Id: DispatcherRulesEditor.java,v 1.1 2010/06/15 12:22:48 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Window;

import java.awt.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Enumeration;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.tools.resources.Attribute;

import org.w3c.tools.widgets.AnswerListener;
import org.w3c.tools.widgets.BorderPanel;
import org.w3c.tools.widgets.ClosableFrame;
import org.w3c.tools.widgets.MessagePopup;
import org.w3c.tools.widgets.PasswordPopup;
import org.w3c.tools.widgets.QuestionPopup;

import org.w3c.tools.sorter.Sorter;

import org.w3c.tools.codec.Base64Encoder;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.www.protocol.http.proxy.Rule;
import org.w3c.www.protocol.http.proxy.RuleNode;
import org.w3c.www.protocol.http.proxy.RuleParser;
import org.w3c.www.protocol.http.proxy.RuleParserException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DispatcherRulesEditor extends AttributeEditor {

    class RulesEditorFrame extends ClosableFrame implements ActionListener,
	                                                    ItemListener,
                                                            AnswerListener
    {

	class EditorMenu extends MenuBar implements ActionListener {
	    RulesEditorFrame frame = null;
	    
	    public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (command.equals("save")) {
		    frame.save();
		} else if (command.equals("quit")) {
		    frame.quit();
		} else if (command.equals("add")) {
		    frame.add();
		} else if (command.equals("replace")) {
		    frame.replace();
		} else if (command.equals("remove")) {
		    frame.remove();
		}
	    }

	    EditorMenu(RulesEditorFrame frame) {
		this.frame = frame;

		//menu file
		Menu file = new Menu("File");
		this.add(file);

		MenuItem save = new MenuItem("Save Rules");
		save.setActionCommand("save");
		save.addActionListener(this);
		file.add( save );

		MenuItem quit = new MenuItem("Quit");
		quit.setActionCommand("quit");
		quit.addActionListener(this);
		file.add( quit );

		//menu rules
		Menu rules = new Menu("Rule");
		this.add(rules);

		MenuItem add = new MenuItem("Add Rule");
		add.setActionCommand("add");
		add.addActionListener(this);
		rules.add( add );

		MenuItem replace = new MenuItem("Replace Rule");
		replace.setActionCommand("replace");
		replace.addActionListener(this);
		rules.add( replace );

		MenuItem remove = new MenuItem("Remove Rule");
		remove.setActionCommand("remove");
		remove.addActionListener(this);
		rules.add( remove );

	    }
	}

	protected Label     location   = null;
	protected List      lrules     = null;
	protected TextField thost      = null;
	protected TextField trule      = null;
	protected Choice    crule      = null;
	protected Choice    cargs      = null;
	protected TextField targs      = null;
	protected Button    removeB    = null;
	protected Button    addB       = null;
	protected Button    replaceB   = null;

	protected QuestionPopup exitAnyway = null;
	
	protected Hashtable hargs      = null; //<args, Boolean.TRUE>

	protected RulesEditor editor   = null;
	protected int         selected = -1;

	protected boolean     modified = false;

	/**
	 * ActionListsner implementation - One of our button was fired.
	 * @param evt The ActionEvent.
	 */

	public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();

	    if ( command.equals("add" ) ) {
		add();
	    } else if ( command.equals("remove" ) ) {
		remove();
	    } else if ( command.equals("replace" ) ) {
		replace();
	    } else if ( command.equals("save" ) ) {
		save();
	    } else if ( command.equals("dismiss" ) ) {
		quit();
	    }
	}

	protected void error(String msg) {
	    (new MessagePopup("Error: "+msg)).show();
	}

	protected void error(Exception ex) {
	    error(ex.getMessage());
	}

	protected void error(String msg, Exception ex) {
	    error(msg+": "+ex.getMessage());
	}

	protected void msg(String msg) {
	    (new MessagePopup(msg)).show();
	}

	protected void msg(String type, String msg) {
	    msg(type+": "+msg);
	}

	protected String getAuthFromDialog() {
	    PasswordPopup pp = new PasswordPopup();
	    Frame popup = new Frame("Authorization required");
	    popup.setBackground(Color.lightGray);
	    popup.setSize(new Dimension(300, 200));
	    popup.setLayout(new BorderLayout());
	    popup.add("Center", pp);
	    popup.show();
	    pp.init();
	    while(!pp.waitForCompletion());
	    popup.setVisible(false);
	    if (pp.canceled())
		return null;
	    Base64Encoder encoder = 
		new Base64Encoder(pp.getUserName()+":"+pp.getPassword());
	    popup.dispose();
	    return encoder.processString();
	}

	protected void add() {
	    Rule added = null;
	    try {
		String tokens[] = getTokens();
		if (tokens != null)
		    added = editor.addRule(tokens);
	    } catch (RuleParserException ex) {
		error(ex);
	    }
	    //add at the end of the displayed list
	    if (added != null) {
		lrules.addItem(added.toString());
		updateArgsList(added.getRuleArgs());
		setModified(true);
	    }
	}

	protected void remove() {
	    if (selected != -1) {
		editor.removeRule(selected);
		lrules.remove(selected);
		setModified(true);
	    } else {
		error("No rule selected");
	    }
	}

	protected void replace() {
	    if (selected == -1) {
		error("No rule selected");
		return;
	    }
	    Rule replaced = null;
	    try {
		String tokens[] = getTokens();
		if (tokens != null)
		    replaced = editor.replaceRule(tokens, selected);
	    } catch (RuleParserException ex) {
		error(ex);
	    }
	    if (replaced != null) {
		lrules.replaceItem(replaced.toString(), selected);
		setModified(true);
	    }
	}

	protected void save() {
	    editor.save();
	    setModified(false);
	}

	protected void close() {
	    quit();
	}

	protected void quit() {
	    if (getModified()) {
		if (exitAnyway == null) {
		    exitAnyway = 
			new QuestionPopup(this, 
					  "Rules Modified, Quit anyway?");
		    exitAnyway.registerAnswerListener(this);
		} 
		exitAnyway.show();
	    } else {
		setVisible(false);
	    }
	}

	protected String[] getTokens() {
	    String rule = trule.getText();
	    String host = thost.getText();
	    if ((rule == null) || (rule.length() == 0)) {
		error("You must specify a rule");
		return null;
	    }
	    if ((host == null) || (host.length() == 0)) {
		error("You must specify a host");
		return null;
	    }
	    Vector vtokens = new Vector(3);
	    vtokens.addElement(host);
	    vtokens.addElement(rule);

	    String args = targs.getText();
	    if (args != null) {
		StringTokenizer st = new StringTokenizer(args);
		while (st.hasMoreTokens())
		    vtokens.addElement(st.nextToken());
	    }
	    String tokens[] = new String[vtokens.size()];
	    vtokens.copyInto(tokens);
	    return tokens;
	}
	
	protected void setModified(boolean onoff) {
	    this.modified = onoff;
	}

	protected boolean getModified() {
	    return modified;
	}

	/**
	 * ItemListener implementation - a rule type was selected.
	 */
	public void itemStateChanged(ItemEvent e) {
	    Object source = e.getSource();
	    if (source == lrules) {
		this.selected = ((Integer) e.getItem()).intValue();
		showRule(selected);
	    } else if (source == crule) {
		String rule = (String)e.getItem();
		trule.setText(rule);
		if (rule.equals("direct") || rule.equals("forbid")) {
		    targs.setEditable(false);
		    targs.setText("");
		} else {
		    targs.setEditable(true);
		}
	    } else if (source == cargs) {
		String arg = (String)e.getItem();
		if (targs.isEditable())
		    targs.setText(arg);
	    }
	}

	/**
	 * AnswerListener implemetation - yes
	 */
	public void questionAnswered (Object source, int response) {
	    if (source == exitAnyway) {
		exitAnyway.setVisible(false);
		if (response == YES)
		    setVisible(false);
	    }
	}

	protected void showRule(int idx) {
	    Rule rule = editor.getRule(idx);
	    thost.setText(rule.getHost());
	    trule.setText(rule.getRuleName());
	    String args = rule.getRuleArgs();
	    if (args != null) {
		targs.setText(args);
		targs.setEditable(true);
	    } else {
		targs.setEditable(false);
		targs.setText("");
	    }
	}
	

	protected void createRulesChoice() {
	    crule = new Choice();
	    String names[] = Rule.getRulesName();
	    for (int i=0; i < names.length; i++) {
		crule.add(names[i]);
	    }
	    crule.addItemListener(this);
	}

	protected void createArgsChoice() {
	    cargs = new Choice();
	    hargs = new Hashtable(10);
	    cargs.addItemListener(this);
	}

	protected void updateArgsList(String args) {
	    if ((args != null) && (hargs.get(args) == null)) {
		hargs.put(args, Boolean.TRUE);
		cargs.add(args);
	    }
	}

	protected void update() {
	    int       size  = editor.getSize();
	    Rule      rule  = null;

	    setModified(false);
	    selected = -1;
	    if (lrules != null) {
		lrules.removeAll();
		for (int i = 0; i < size; i++) {
		    rule = editor.getRule(i);
		    lrules.addItem(rule.toString(),i);
		    updateArgsList(rule.getRuleArgs());
		}
	    }
	    if (location != null)
		location.setText("Rules location: "+
				 editor.component.getRulesLocation());
	}

	RulesEditorFrame(RulesEditor editor) {
	    super("Proxy Dispatcher Rules Editor");

	    this.editor = editor;
	    lrules = new List(20, false);
	    lrules.setBackground(Color.white);
	    lrules.addItemListener(this);
	    createRulesChoice();
	    createArgsChoice();
	    update();
	    //Menu Bar
	    EditorMenu menu = new EditorMenu(this);
	    setMenuBar(menu);

	    //rules list
	    BorderPanel plrules = new BorderPanel(BorderPanel.OUT, 5);
	    plrules.setLayout(new BorderLayout());
	    plrules.add(lrules);

	    
	    thost = new TextField(20);
	    trule = new TextField(15);
	    trule.setEditable(false);

	    BorderPanel ptrule = new BorderPanel(BorderPanel.IN, 2);
	    ptrule.setLayout(new GridLayout(1,1));
	    ptrule.add(trule);

	    targs = new TextField(30);

	    // BAR 0
	    Panel plabel = new Panel();
	    plabel.add(new Label("Host: "));
	    Panel pcrule = new Panel(new BorderLayout());
	    pcrule.add(new Label("Rule: "), "West");
	    pcrule.add(crule, "Center");
	    Panel pcargs = new Panel(new BorderLayout());
	    pcargs.add(new Label("Args: "), "West");
	    pcargs.add(cargs, "Center");

	    BorderPanel phost = new BorderPanel(BorderPanel.IN,2);
	    phost.setLayout(new GridLayout(2,1));
	    phost.add(plabel);
	    phost.add(thost);

	    BorderPanel prule = new BorderPanel(BorderPanel.IN,2);
	    prule.setLayout(new GridLayout(2,1));
	    prule.add(pcrule);
	    prule.add(trule);

	    BorderPanel pargs = new BorderPanel(BorderPanel.IN,2);
	    pargs.setLayout(new GridLayout(2,1));
	    pargs.add(pcargs);
	    pargs.add(targs);

	    BorderPanel bar0 = new BorderPanel(BorderPanel.OUT,5);
	    bar0.setLayout(new GridLayout(1,3));
	    bar0.add(phost);
	    bar0.add(prule);
	    bar0.add(pargs);

	    // BAR 2
	    addB     = new Button("Add Rule");
	    addB.setActionCommand("add");
	    addB.addActionListener(this);

	    replaceB = new Button("Replace Rule");
	    replaceB.setActionCommand("replace");
	    replaceB.addActionListener(this);

	    removeB  = new Button("Remove Rule");
	    removeB.setActionCommand("remove");
	    removeB.addActionListener(this);

	    Panel bar2 = new Panel(new GridLayout(1,3));
	    bar2.add(addB);
	    bar2.add(replaceB);
	    bar2.add(removeB);

	    //Label location
	    location = new Label("Rules location: "+
				 editor.component.getRulesLocation());
	    BorderPanel ploc = new BorderPanel(BorderPanel.RAISED, 1);
	    ploc.setLayout(new FlowLayout());
	    ploc.add(location);

	    //Bar 2 & 3
	    BorderPanel bar2_3 = new BorderPanel(BorderPanel.OUT, 5);
	    bar2_3.setLayout(new GridLayout(2,1));
 	    bar2_3.add(bar2);
 	    bar2_3.add(ploc);
	    
	    //subpanel
	    Panel subPanel = new Panel(new BorderLayout());
	    subPanel.add(plrules,"Center");
	    subPanel.add(bar0, "South");

	    add(subPanel, "Center");
	    add(bar2_3, "South");
	    
	    setSize(800,600);
	}
    }

    class RulesEditor {

	class Saver extends Thread {
	    RulesEditor editor = null;
	    
	    public void run() {
		editor.saveRules();
	    }

	    Saver(RulesEditor editor) {
		this.editor = editor;
	    }
	    
	}

	protected URL                   rulesUrl  = null;
	protected File                  rulesFile = null;
	protected RulesEditorFrame      gui       = null;
	protected HttpURLConnection     con       = null;
	protected String                auth      = null;
	protected DispatcherComponent   component = null;

	/**
	 * The current set of rules.
	 */
	protected Vector rules   = null; //<Rule>

	protected void setURL(URL url) {
	    this.rulesFile = null;
	    this.rulesUrl  = url;
	    parse();
	}

	protected void setAuthorization(String authorization) {
	    this.auth = authorization;
	}

	protected String getAuthorization() {
	    return auth;
	}

	protected boolean hasAuthorization() {
	    return (auth != null);
	}

	protected void setFile(File file) {
	    this.rulesUrl  = null;
	    this.rulesFile = file;
	    parse();
	}

	protected DataOutputStream getRulesOutputStream() 
	    throws IOException
	{
	    DataOutputStream out = null;
	    if (rulesUrl != null) {
		con = (HttpURLConnection) rulesUrl.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("content-type","text/plain");
		if (hasAuthorization())
		    con.setRequestProperty("Authorization","Basic "+
					   getAuthorization());
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setAllowUserInteraction(true);
		out = new DataOutputStream(con.getOutputStream());
	    } else {
		out = new DataOutputStream(
		          new BufferedOutputStream(
			      new FileOutputStream(rulesFile)));
	    }
	    return out;
	}

	protected boolean closeRulesOutputStream(DataOutputStream out) 
	    throws IOException
	{
	    out.flush();
	    out.close();
	    if ((rulesUrl != null) && (con != null)) {
		con.getHeaderField(0);
		int code = con.getResponseCode();
		switch (code) {
		case HttpURLConnection.HTTP_UNAUTHORIZED:
		    //ask for a username/password
		    String encoded = gui.getAuthFromDialog();
		    if (encoded == null)
			return true;
		    setAuthorization(encoded);
		    return false;
		case HttpURLConnection.HTTP_CREATED:
		case HttpURLConnection.HTTP_OK:
		case HttpURLConnection.HTTP_NO_CONTENT:
		    gui.msg("Proxy dispatcher rules saved.");
		    return true;
		default:
		    gui.msg("HTTP error",con.getResponseMessage());
		    return true;
		}
	    } else {
		gui.msg("Proxy dispatcher rules saved.");
		return true;
	    }
	}

	/**
	 * return the inputStream or null if the rules file doesn't exists.
	 * @return an InputStream.
	 */
	protected InputStream getRulesInputStream() 
	    throws IOException
	{
	    InputStream in = null;
	    if (rulesUrl != null)
		in = rulesUrl.openStream();
	    else if (rulesFile.exists()) {
		in = new BufferedInputStream(
		         new FileInputStream(rulesFile));
	    }
	    return in;
	}

	protected void save() {
	    (new Saver(this)).start();
	}

	protected void saveRules() {
	    DataOutputStream out = null;
	    try {
		do {
		    out = getRulesOutputStream();
		    if (rules != null) {
			out.writeBytes("#\n# Generated by proxy "+
				       "dispatcher rules editor.\n#\n");
			//write content here
			for (int i=0; i < rules.size(); i++)
			    ((Rule)rules.elementAt(i)).writeRule(out);
		    }
		} while (! closeRulesOutputStream(out));
	    } catch (Exception ex) {
		gui.error("Unable to save rules",ex);
	    }
	}

	protected void parse() {
	    InputStream in = null;
	    // Try opening the rule file as a URL:
	    try {
		in = getRulesInputStream();
	    } catch (Exception ex) {
		gui.error("Unable to open input stream");
		return;
	    }
	    try {
		if (in != null) {
		    RuleParser parser = new RuleParser(in);
		    RuleNode   nroot  = parser.parse();
		    rules = generateRuleVector(nroot);
		} else {
		    rules = new Vector(); //empty
		}
	    } catch (Exception ex) {
		gui.error("Rules parser error",ex);
	    }
	}

	protected Vector generateRuleVector(RuleNode root) {
	    Vector vrules = new Vector(20);
	    collectRules(root, vrules);
	    Rule srules [] = new Rule[vrules.size()];
	    vrules.copyInto(srules);
	    srules = (Rule[]) Sorter.sortComparableArray(srules, true);
	    for (int i=0; i < srules.length; i++)
		vrules.setElementAt(srules[i], i);
	    return vrules;
	}

	protected void collectRules(RuleNode root, Vector vrules) {
	    Hashtable childrens = root.getChildren();
	    if (childrens != null) {
		Enumeration childenum = childrens.keys();
		while (childenum.hasMoreElements()) {
		    RuleNode rnode = 
			(RuleNode) childrens.get((String) childenum.nextElement());
		    Rule rule = rnode.getRule();
		    if (rule != null)
			vrules.addElement(rule);
		    collectRules(rnode, vrules);
		}
	    }
	}

	/**
	 * Add a Rule to the end of the rules array.
	 * @param tokens a tokens array, according to the Rule specification.
	 * @return the new added rule.
	 * @exception RuleParserException if the rule can't be created.
	 */
	protected Rule addRule(String tokens[]) 
	    throws RuleParserException
	{
	    Rule newRule = null;
	    newRule = Rule.createRule(tokens, 1, tokens.length);
	    if (newRule != null) {
		if (rules == null)
		    rules = new Vector(10);
		rules.addElement(newRule);
	    }
	    return newRule;
	}
	
	/**
	 * Replace the Rule at the specified index .
	 * @param tokens a tokens array, according to the Rule specification.
	 * @param idx the index.
	 * @return the new added rule.
	 * @exception RuleParserException if the rule can't be created.
	 */
	protected Rule replaceRule(String tokens[], int idx) 
	    throws RuleParserException
	{
	    Rule newRule = null;
	    newRule = Rule.createRule(tokens, 1, tokens.length);
	    if (newRule != null) {
		if (rules == null)
		    rules = new Vector(10);
		rules.setElementAt(newRule, idx);
	    }
	    return newRule;
	}

	/**
	 * Deletes the rule at the specified index. Each rule with an index 
	 * greater or equal to the specified index is shifted downward to 
	 * have an index one smaller than the value it had previously.
	 * @param idx the rule index.
	 */
	protected void removeRule(int idx) {
	    rules.removeElementAt(idx);
	}

	protected Rule getRule(int idx) {
	    return (Rule)rules.elementAt(idx);
	}

	/**
	 * return the number of rules.
	 */
	protected int getSize() {
	    if (rules == null)
		return 0;
	    return rules.size();
	}

	public void show() {
	    gui.update();
	    gui.show();
	}

	RulesEditor(DispatcherComponent comp, URL url) {
	    this.component = comp;
	    this.rulesUrl = url;
	    parse();
	    this.gui = new RulesEditorFrame(this);
	}

	RulesEditor(DispatcherComponent comp, File file) {
	    this.component = comp;
	    this.rulesFile = file;
	    parse();
	    this.gui = new RulesEditorFrame(this);
	}
    }

    /**
     * The TextField+Button component
     */
    class DispatcherComponent extends BorderPanel implements ActionListener {
	protected RulesEditor           reditor    = null;
	protected TextField             locationEd = null;
	protected DispatcherRulesEditor editor     = null;

	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command != null) {
		String loc = getRulesLocation();
		if ((loc != null) && (loc.length() > 0)) {
		    try {
			URL url = new URL(loc);
			if (url.getProtocol().equalsIgnoreCase("file")) {
			    File file = new File(url.getFile());
			    if (reditor == null)
				reditor = new RulesEditor(this, file);
			    else
				reditor.setFile(file);
			} else {
			    if (reditor == null)
				reditor = new RulesEditor(this, url);
			    else 
				reditor.setURL(url);
			}
		    } catch (MalformedURLException ex) {
			//shoud be a file
			File file = new File(loc);
			if (reditor == null)
			    reditor = new RulesEditor(this, file);
			else
			    reditor.setFile(file);
		    }
		    reditor.show();
		}
	    }
	}
	
	public String getRulesLocation() {
	    return locationEd.getText();
	}

	public void setRulesLocation(String loc) {
	    locationEd.setText(loc);
	}

	DispatcherComponent (DispatcherRulesEditor parent, String location) {
	    super(IN, 2);
	    this.editor = parent;
	    this.locationEd  = new TextField(20);
	    if (location != null)
		this.locationEd.setText(location);
	    Button editB = new Button("Edit Rules");
	    editB.setActionCommand("edit");
	    editB.addActionListener(this);
	    setLayout( new BorderLayout());
	    add(locationEd,"Center");
	    add(editB,"East");
	}
    }

    //
    // DispatcherRulesEditor 
    //

    private   DispatcherComponent widget     = null;
    protected String              origs      = null;

    protected void createComponent(String location) {
	widget = new DispatcherComponent(this, location);
    }

    /**
     * get the Component created by the editor.
     * @return a Component
     */
    public Component getComponent() {
	return widget;
    }

    /**
     * Tells if the edited value has changed
     * @return true if the value changed.
     */

    public boolean hasChanged() {
	return !origs.equals(widget.getRulesLocation());
    }

    /**
     * set the current value to be the original value, ie: changed
     * must return <strong>false</strong> after a reset.
     */

    public void clearChanged() {
	origs = widget.getRulesLocation();
    }

    /**
     * reset the changes (if any)
     */

    public void resetChanges() {
	widget.setRulesLocation(origs);
    }

    /**
     * Get the current value of the edited value
     * @return an object or <strong>null</strong> if the object was not
     * initialized
     */

    public Object getValue() {
	return widget.getRulesLocation();
    }

    /**
     * Set the value of the edited value
     * @param o the new value.
     */

    public void setValue(Object o) {
	widget.setRulesLocation(o.toString());
    }

    /**
     * Initialize the editor
     * @param w the ResourceWrapper father of the attribute
     * @param a the Attribute we are editing
     * @param o the value of the above attribute
     * @param p some Properties, used to fine-tune the editor
     * @exception RemoteAccessException if a remote access error occurs.
     */

    public void initialize(RemoteResourceWrapper w, Attribute a,  Object o,
			   Properties p)
	throws RemoteAccessException
    {
	RemoteResource r = w.getResource();
	if(o == null) {
	    String v = null;
	    // FIXME
	    v = (String) r.getValue(a.getName());
	   
	    if(v == null)
		if(a.getDefault() != null)
		    v = a.getDefault().toString();
	    if ( v != null ) {
		origs = v;
	    } 
	} else {
	    origs = o.toString();
	}
	createComponent(origs);
    }

    public DispatcherRulesEditor() {
	origs = "";
    }

}
