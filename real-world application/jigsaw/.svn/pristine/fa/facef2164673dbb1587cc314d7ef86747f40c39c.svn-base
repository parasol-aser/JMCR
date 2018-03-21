// HtmlLink.java
// $Id: HtmlLink.java,v 1.1 2010/06/15 12:29:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.html ;

import org.w3c.www.mime.MimeType;
/*
 * The link element contained in the HEAD of an html document
 */

public class HtmlLink {
    String href  = null;
    String rel   = null;
    String rev   = null;
    String title = null;
    String type  = null;

    /**
     * genereate a String representation that can be
     * append in a HTML document
     */

    public String toString()
    {
	return "<LINK" 
	       + ((href != null) ? " HREF=\""+href+"\"" : "")
	       + ((rel != null) ? " REL=\""+rel+"\"" : "")
	       + ((rev != null) ? " REV=\""+rel+"\"" : "")
	       + ((type != null) ? " type=\""+type+"\"" : "")
	       + ((title != null) ? " TITLE=\""+rel+"\"" : "")
	       + ">\n";
    }

    /**
     * Create a link with only REL and HREF part
     * @param rel, String part of rel
     * @param href, String form of an URL or relative URL
     */

    public HtmlLink(String rel, String href)
    {
	this.rel  = rel;
	this.href = href;
    }

    /**
     * Create a link with only REL, HREF, and type part
     * @param rel, String part of rel
     * @param href, String form of an URL or relative URL
     * @param type, mimetype
     */

    public HtmlLink(String rel, String href, MimeType type)
    {
	this.rel  = rel;
	this.href = href;
	this.type = type.toString();
    }

   
    /**
     * Create a link with only HREF part (not very useful)
     * @param href, String form of an URL or relative URL
     */

    public HtmlLink(String href)
    {
	this.href = href;
    }

    /**
     * Create a link with only REL and HREF part
     * @param rev, String definition of rev
     * @param rel, String definition of rel
     * @param href, String form of an URL or relative URL
     */

    public HtmlLink(String rev, String rel, String href)
    {
	this.rev  = rev;
	this.rel  = rel;
	this.href = href;	
    }

    /**
     * Create a link with only REV, REL, HREF, and type part
     * @param rev, String definition of rev
     * @param rel, String part of rel
     * @param href, String form of an URL or relative URL
     * @param type, mimetype
     */

    public HtmlLink(String rev, String rel, String href, MimeType type)
    {
	this.rev  = rev;
	this.rel  = rel;
	this.href = href;
	this.type = type.toString();
    }

     /**
     * Create a complete link 
     * @param rev, String definition of rev
     * @param rel, String definition of rel
     * @param href, String form of an URL or relative URL
     * @param title, String title
     */

    public HtmlLink(String rev, String rel, String href, String title)
    {
	this.rev   = rev;
	this.rel   = rel;
	this.href  = href;	
	this.title = title;
    }   

    /**
     * Create a link with  REV, REL, HREF, type and title
     * @param rev, String definition of rev
     * @param rel, String part of rel
     * @param href, String form of an URL or relative URL
     * @param type, mimetype
     * @param title, String title
     */

    public HtmlLink(String rev, 
		    String rel, 
		    String href, 
		    MimeType type, 
		    String title)
    {
	this.rev   = rev;
	this.rel   = rel;
	this.href  = href;
	this.type  = type.toString();
	this.title = title;
    }
}
