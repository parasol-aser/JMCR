// HtmlGenerator.java
// $Id: HtmlGenerator.java,v 1.1 2010/06/15 12:29:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.html ;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.Vector;

import org.w3c.www.mime.MimeType;

class HtmlHead {

    String       title    = null;
    String       base     = null;
    Vector       links    = null;
    HtmlStyle    style    = null;
    HtmlScript   script   = null;
    StringBuffer meta     = null ; // FIXME

    /**
     * @return the current HtmlStyle (if any)
     */

    public HtmlStyle getStyle() {
	return style;
    }

    /**
     * Create and add/replace a new style in the head
     * @param type, the MimeType of the style
     * @param style, the full text of the style sheet
     */

    public void addStyle(MimeType type, String style) {
	this.style = new HtmlStyle(type, style);
    }

    /**
     * Add or append style to the current Style
     * @param style, the full text of the style sheet to append
     */

    public void addStyle(String style) {
	if(this.style == null)
	    this.style = new HtmlStyle(style);
	else
	    this.style.append(style);
    }

    /**
     * Create and add/replace a new script in the head
     * @param lang, the scripting language
     * @param style, the full text (or a part) of the script
     */
    public void addScript(String lang, String script) {
	this.script = new HtmlScript(lang, script);
    }

    /**
     * Add or append script to the current Style
     * @param script, the full text (or a part) of the script.
     */

    public void addScript(String script) {
	if (this.script == null)
	    this.script = new HtmlScript(script);
	else
	    this.script.append(script);
    }

    /**
     * set the HREF part of the BASE element
     */

    public void addBase(String base) {
	this.base = base;
    }

    /**
     * Add a link to the head
     * @param link an HtmlLink
     */

    public void addLink(HtmlLink link) {
	if(links == null)
	    links = new Vector(4);
	links.addElement(link);
    }

    /** Add some meta-http-equiv information 
     * @param name, the name of the meta-http 
     * @param value, the name of the meta-http
     */

    public void addMeta (String name, String value) {
	if ( meta == null ) {
	     meta = new StringBuffer ("<meta http-equiv=\""
                                      + name
				      + "\" content=\""
                                      + value +"\">") ;
	} else {
	    meta.append ("<meta http-equiv=\""
                         + name
			 + "\" content=\""
                         + value
			 + "\">") ;
	}
    }

    /**
     * generate a String format of the HEAD element that can
     * be inserted in a HTML document
     */

    public String toString() {
	StringBuffer strlink = new StringBuffer("");
	if(links != null)
	{
	    for(int i=0; i<links.size(); i++)
		strlink.append(links.elementAt(i).toString());
	}
	return "<head>\n <title>" + title + "</title>\n"
	       + ((meta != null) ? meta.toString() : "")
	       + ((base != null) ? " <base href=\"" + base + "\">\n":"")
               + strlink.toString() 
	       + ((script != null) ? script.toString() : "")
               + ((style != null) ? style.toString() : "")
               + "</head>" ;
    }

    public HtmlHead(String title) {
	this.title = title;
    }
}
	
/**
 * A simple HTML generator.
 * This class implements an HTML generator that allows to output dynamic
 * HTML content out.
 */

public class HtmlGenerator {
    private static MimeType defaultType = MimeType.TEXT_HTML;

    HtmlHead     head     = null ;
    StringBuffer body     = null ;
    String       content  = null ;	// content once closed.
    boolean      bodytag  = true ;       // frameset hack
    String       encoding = null ;      // the encoding, default is ISO8859_1
    private MimeType type = null ;

    /**
     * Get this stream MIME type.
     * This defaults to <strong>text/html</strong>.
     */

    public MimeType getMimeType () {
	return (type == null) ? defaultType : type;
    }

    /**
     * Don't emit body tag. This is usefull in conjunction with the FRAMESET
     * tag, that requires that no BODY tag be emited.
     * @param value If <strong>true</strong>, a BODY tag will be emited.
     */

    public void emitBODYTag (boolean value) {
	this.bodytag = value ;
    }

    /**
     * Append the given string, escaping all special characters. This can be 
     * used only if you know that the string you are inserting doesn't contain
     * HTML tags
     */

    public void appendAndEscape (String content) {
	for (int i = 0 ; i < content.length() ; i++) {
	    char ch = content.charAt(i) ;
	    switch (ch) {
	      case '<': body.append ("&lt;") ; break ;
	      case '>': body.append ("&gt;") ; break ;
	      case '&': body.append ("&amp;") ; break ;
	      default:  body.append (ch) ; break;
	    }
        }
    }

    /**
     * Add a Base element to the head
     * @param the href part of the BASE element
     */

    public void addBase(String base) {
	head.addBase(base);
    }

    /**
     * Add style to this html page
     * @see org.w3c.jigsaw.html.HtmlStyle
     */

    public void addStyle(String style) {
	head.addStyle(style);
    }

    /**
     * Add style to this html page
     * @see org.w3c.jigsaw.html.HtmlStyle
     */

     public void addStyle(MimeType type, String style) {
	head.addStyle(type, style);
    }

    /**
     * Add script to this html page
     * @see org.w3c.jigsaw.html.HtmlScript
     */

    public void addScript(String script) {
	head.addScript(script);
    }

    /**
     * Add script to this html page
     * @see org.w3c.jigsaw.html.HtmlScript
     */

     public void addScript(String lang, String script) {
	head.addScript(lang, script);
    } 

    /**
     * Add a link to the head of this html document
     * @param link the link
     */

    public void addLink(HtmlLink link) {
	head.addLink(link);
    }

    /**
     * Append the given string to the document body.
     * @param The HTML string to append.
     */

    public void append (String content) {
	body.append (content) ;
    }

    /**
     * Append the two strings to the document body.
     * <code>append("x"+"y");</code> is equivalent but slower than
     * <code>append("x", "y");</code>.
     * @param str1 The first string.
     * @param str2 The second string.
     */

    public void append(String str1, String str2) {
	body.append(str1) ;
	body.append(str2) ;
    }

    public void append(String s1, String s2, String s3) {
	body.append(s1);
	body.append(s2);
	body.append(s3);
    }

    /**
     * Get the length of this html document
     * @return the length in bytes of the document
     */

    public int length () {
	if ( content == null )
	    close () ;
	return content.length() ;
    }

    /**
     * Close the given document: its composition is now finished.
     * @return	The content length for this document.
     */

    public void close () {
	if ( content != null )
	    return ;
	content="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 "
	    +"Transitional//EN\"\n"
            +"               \"http://www.w3.org/TR/html4/loose.dtd\">\n"
	    +"<html>\n"
	    + head.toString() + "\n"
	    + (bodytag 
	       ? ("<body>\n" + body.toString() + "</body>\n")
	       : body.toString())
	    + "</html>" ;
	return ;
    }

    /**
     * adds a htt-equiv meta tag to the head of the document
     * @param name the name of the pseudo http tag
     * @param value, the string value of the pseudo header
     */

    public void addMeta (String name, String value) {
	head.addMeta(name, value);
    }

    /**
     * @deprecated
     * @see addMeta
     */

    public void meta (String name, String value) {
	addMeta(name, value);
    }    

    /**
     * Get the input string for reading the document.
     * @return An input stream to get the generated document from.
     */

    public InputStream getInputStream () {
	close() ;
	try {
	    return new ByteArrayInputStream (content.getBytes(encoding));
	} catch (UnsupportedEncodingException ex) {
	    throw new RuntimeException (this.getClass().getName() + 
					"[getInputStream] Unable to convert" +
					"properly char to bytes");
	}
    }
	

    /**
     * create the HTML generator with a specific encoding
     * @param the title, a String
     * @param the encoding used, also a String
     */
    public HtmlGenerator (String title, String encoding) {
	this.head = new HtmlHead(title); 
	this.body = new StringBuffer() ;
	this.encoding = encoding;
	String param[] = new String[1];
	String value[] = new String[1];
	param[0] = "charset";
	// a translation table may be needed there
	value[0] = encoding;
	this.type = new MimeType("text", "html", param, value);
    }
    /**
     * create the HTML generator with the default HTML encoding "ISO8859_1"
     * @param title, the document title
     */
    public HtmlGenerator (String title) {
	this(title, "ISO-8859-1");
    }
}
