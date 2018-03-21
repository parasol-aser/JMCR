// MiniBrowser.java
// $Id: MiniBrowser.java,v 1.1 2010/06/15 12:25:51 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Vector;
import java.util.NoSuchElementException;

import java.io.IOException;

import org.w3c.jigadmin.widgets.ClosableFrame;

import org.w3c.tools.widgets.Utilities;

/**
 * A mini HTML browser.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class MiniBrowser extends ClosableFrame {

    private static MiniBrowser mbrowser = null;

    private Vector prevurls = null;
    private Vector nexturls = null;

    protected final static String NEXT_AC = "next";
    protected final static String PREV_AC = "prev";
    protected final static String TEXT_AC = "text";

    protected JLabel      statusBar = null;
    protected JTextField  urlField  = null;
    protected JEditorPane editor    = null;
    protected JButton     next_B    = null;
    protected JButton     prev_B    = null;

    protected History history    = null;
    protected URL     currentURL = null;

    /**
     * The browser history.
     */
    class History {
	private Vector prevurls = null;
	private Vector nexturls = null;

	protected synchronized void add(Object obj) {
	    prevurls.addElement(obj);
	    nexturls.clear();
	    prev_B.setEnabled(true);
	    next_B.setEnabled(false);
	}

	protected synchronized Object getPrev(Object current) 
	    throws NoSuchElementException
	{
	    Object prev = prevurls.lastElement();
	    prevurls.removeElementAt(prevurls.size()-1);
	    nexturls.addElement(current);
	    if (prevurls.size() > 0)
		prev_B.setEnabled(true);
	    else
		prev_B.setEnabled(false);
	    next_B.setEnabled(true);
	    return prev;
	}

	protected synchronized Object getNext(Object current) 
	    throws NoSuchElementException
	{
	    Object next = nexturls.lastElement();
	    nexturls.removeElementAt(nexturls.size()-1);
	    prevurls.addElement(current);
	    prev_B.setEnabled(true);
	    if (nexturls.size() > 0)
		next_B.setEnabled(true);
	    else
		next_B.setEnabled(false);
	    return next;
	}

	History(){
	    prevurls = new Vector(10);
	    nexturls = new Vector(10);
	}
    }

    /**
     * Our internal HyperLinkListener.
     */
    HyperlinkListener hll = new HyperlinkListener() {
	public void hyperlinkUpdate(HyperlinkEvent he) {
	    HyperlinkEvent.EventType type = he.getEventType();

	    if (type == HyperlinkEvent.EventType.ENTERED) {
		editor.setCursor(Cursor.
				 getPredefinedCursor(Cursor.HAND_CURSOR));
		statusBar.setText(he.getURL().toString());
	    }
	    else if (type == HyperlinkEvent.EventType.EXITED) {
		editor.setCursor(Cursor.getDefaultCursor());
		statusBar.setText(" ");
	    }
	    else {
		try {
		    if (currentURL != null)
		        history.add(currentURL);
		    setPage(he.getURL());
		    if (urlField != null) {
			urlField.setText(he.getURL().toString());
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    };

    /**
     * Our internal ActionListener.
     */
    ActionListener al = new ActionListener() {
	public void actionPerformed(ActionEvent ae) {
	    String command = ae.getActionCommand();
	    if (command.equals(PREV_AC)) {
		try {
		    setPage((URL)history.getPrev(currentURL));
		} catch (NoSuchElementException ex) {
		} catch (IOException io) {
		    io.printStackTrace();
		}
	    } else if (command.equals(NEXT_AC)) {
		try {
		    setPage((URL)history.getNext(currentURL));
		} catch (NoSuchElementException ex) {
		} catch (IOException io) {
		    io.printStackTrace();
		}
	    } else if (command.equals(TEXT_AC)) {
		
		history.add(currentURL);
		try {
		    setPage(urlField.getText());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
    };

    protected void build(String url) {
	Container cont = getContentPane();
	cont.setLayout(new BorderLayout());

	prev_B = new JButton("Prev");
	prev_B.setActionCommand(PREV_AC);
	prev_B.addActionListener(al);
	prev_B.setEnabled(false);
	next_B = new JButton("Next");
	next_B.setActionCommand(NEXT_AC);
	next_B.addActionListener(al);
	next_B.setEnabled(false);
	JPanel pb = new JPanel(new GridLayout(1,2));
	pb.add(prev_B);
	pb.add(next_B);

	urlField = new JTextField(40);
	urlField.setActionCommand(TEXT_AC);
	urlField.addActionListener(al);
	urlField.setBorder(BorderFactory.createLoweredBevelBorder());
	JLabel location = new JLabel("  Location:");
	JPanel urlp = new JPanel(new BorderLayout());
	urlp.add(location, BorderLayout.WEST);
	urlp.add(urlField, BorderLayout.CENTER);

	JPanel bar = new JPanel(new BorderLayout());
	bar.add(pb, BorderLayout.WEST);
	bar.add(urlp, BorderLayout.CENTER);
	bar.setBorder(BorderFactory.createRaisedBevelBorder());

	statusBar = new JLabel(url);
	statusBar.setFont(Utilities.smallFont);
	statusBar.setBorder(BorderFactory.createRaisedBevelBorder());

	editor = new JEditorPane();
	editor.setEditable(false);
	editor.setEditorKitForContentType("text/html", new MyHTMLEditorKit());
	editor.addHyperlinkListener(hll);

	cont.add(bar, BorderLayout.NORTH);
	cont.add(new JScrollPane(editor), BorderLayout.CENTER);
	cont.add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Constructor.
     * @param url The url to browse
     * @param title the frame Title
     * @exception MalformedURLException if the URL is an invalid URL.
     * @exception IOException if an IO error occurs.
     */
    public MiniBrowser(String url, String title) 
	throws MalformedURLException, IOException
    {
	super(title);
	history = new History();
	build(url);
	setPage(url);
    }

    /**
     * Constructor.
     * @param title the frame Title
     * @exception MalformedURLException if the URL is an invalid URL.
     * @exception IOException if an IO error occurs.
     */
    public MiniBrowser(String title)
	throws MalformedURLException, IOException
    {
	super(title);
	history = new History();
	build("");
    }

    /**
     * The frame has been closed.
     */
    protected void close() {
	mbrowser = null;
    }

    /**
     * Set the current URL.
     * @param url the new current url.
     * @exception MalformedURLException if the URL is an invalid URL.
     * @exception IOException if an IO error occurs.
     */
    public void setPage(String url)
	throws MalformedURLException, IOException
    {
	currentURL = new URL(url);
	editor.setPage(currentURL);
	urlField.setText(url);
    }

    /**
     * Set the current URL.
     * @param url the new current url.
     * @exception IOException if an IO error occurs.
     */
    public void setPage(URL url)
	throws IOException
    {
	currentURL = url;
	editor.setPage(url);
	urlField.setText(url.toExternalForm());
    }

    /**
     * Show the current MiniBrowser for the given URL.
     * @param url the new current url.
     * @param title the frame title.
     * @exception MalformedURLException if the URL is an invalid URL.
     * @exception IOException if an IO error occurs.
     */
    public static void showDocumentationURL(String url, String title) 
	throws MalformedURLException, IOException
    {
	if (mbrowser == null) {
	    mbrowser = new MiniBrowser(url, title);
	    mbrowser.setSize(600, 600);
	    mbrowser.setVisible(true);
	} else {
	    mbrowser.history.add(mbrowser.currentURL);
	    mbrowser.setPage(url);
	}
    }

    public static void main(String args[]) {
	try {

	    MiniBrowser browser = null;
	    if (args.length > 0) 
		browser = new MiniBrowser(args[0], "Mini Browser");
	    else
		browser = new MiniBrowser("Mini Browser");

	    browser.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });

	    browser.setSize(600, 600);
	    browser.setVisible(true);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

}
