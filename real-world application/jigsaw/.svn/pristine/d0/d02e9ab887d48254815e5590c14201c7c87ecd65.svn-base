// MyHTMLEditorKit.java
// $Id: MyHTMLEditorKit.java,v 1.1 2010/06/15 12:25:52 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.MouseInputAdapter;

import java.awt.event.MouseEvent;
import java.awt.Point;

import java.io.Serializable;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * A special HTMLEditorKit that handle mouse event.
 */
public class MyHTMLEditorKit extends HTMLEditorKit {

    public static final int JUMP = 0;
    public static final int MOVE = 1;

    LinkController myController = new LinkController();

    /**
     * Called when the kit is being installed into the a JEditorPane.
     * @param c the JEditorPane.
     */
    public void install(JEditorPane c) {
	c.addMouseListener(myController);
	c.addMouseMotionListener(myController);
    }

    /**
     * Our MouseListener.
     */
    public static class LinkController extends MouseInputAdapter 
	implements Serializable 
    {

	URL currentUrl = null;

	public void mouseClicked(MouseEvent e) {
	    JEditorPane editor = (JEditorPane) e.getSource();

	    if (! editor.isEditable()) {
		Point pt = new Point(e.getX(), e.getY());
		try {
		    int pos = editor.viewToModel(pt);
		    if (pos >= 0) {
			activateLink(pos, editor, JUMP);
		    }
		} catch (IllegalArgumentException iae) {}
	    }
	}

	public void mouseMoved(MouseEvent e) {
	    JEditorPane editor = (JEditorPane) e.getSource();

	    if (! editor.isEditable()) {
		Point pt = new Point(e.getX(), e.getY());
		try {
		    int pos = editor.viewToModel(pt);
		    if (pos >= 0) {
			activateLink(pos, editor, MOVE);
		    }
		} catch (IllegalArgumentException iae) {}
	    }
	}

	protected void activateLink(int pos, JEditorPane html, int type) {
	    Document doc = html.getDocument();
	    if (doc instanceof HTMLDocument) {
		HTMLDocument hdoc = (HTMLDocument) doc;
		Element e = hdoc.getCharacterElement(pos);
		AttributeSet a = e.getAttributes();
		AttributeSet anchor = 
		    (AttributeSet) a.getAttribute(HTML.Tag.A);
		String href = (anchor != null) ? 
		    (String) anchor.getAttribute(HTML.Attribute.HREF) : null;
		boolean shouldExit = false;

		HyperlinkEvent linkEvent = null;
		if (href != null) {
		    URL u;
		    try {
			u = new URL(hdoc.getBase(), href);
		    } catch (MalformedURLException m) {
			u = null;
		    }

		    if ((type == MOVE) && (!u.equals(currentUrl))) {
			linkEvent =  new HyperlinkEvent(html, 
					HyperlinkEvent.EventType.ENTERED, 
							u, href);
			currentUrl = u;
		    }
		    else if (type == JUMP) {
			linkEvent = new HyperlinkEvent(html, 
				       HyperlinkEvent.EventType.ACTIVATED, 
						       u, href);
			shouldExit = true;
		    }
		    else {
			return;
		    }
		    html.fireHyperlinkUpdate(linkEvent);
		}
		else if (currentUrl != null) {
		    shouldExit = true;
		}
		if (shouldExit) {
		    linkEvent = new HyperlinkEvent(html,
					   HyperlinkEvent.EventType.EXITED,
						   currentUrl, null);
		    html.fireHyperlinkUpdate(linkEvent);
		    currentUrl = null;
		}
	    }
	}
    }
}
