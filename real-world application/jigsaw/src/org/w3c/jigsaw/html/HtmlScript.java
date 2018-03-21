// HtmlScript.java
// $Id: HtmlScript.java,v 1.1 2010/06/15 12:29:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.html ;

/**
 * a sample Script class for HTML documents
 */

public class HtmlScript {
    String          lang = null;
    StringBuffer    script = null;

    /**
     * set the type of script used
     * @param script the type
     */

    public void setLanguage(String lang)
    {
	this.lang = lang;
    }

    /**
     * append a string to the script buffer
     * @param str1 A string to be append
     */

    public void append(String str1)
    {
	if(script == null)
	    script = new StringBuffer(str1);
	else
	    script.append(str1);
    }

    /**
     * genereate a String representation that can be
     * append in a HTML document
     */

    public String toString()
    {
	if(script == null)
	    return "";
	return "<SCRIPT LANGUAGE=\"" + lang.toString() + "\">\n"
	       + script.toString() + "\n</SCRIPT>\n" ;
    }

    public HtmlScript(String language, String script)
    {
	setLanguage(language);
	append(script);
    }

    public HtmlScript(String script)
    {
	append(script);
    }
}
