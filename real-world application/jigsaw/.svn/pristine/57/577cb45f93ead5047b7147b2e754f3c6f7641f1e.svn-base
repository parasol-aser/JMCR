// HtmlStyle.java
// $Id: HtmlStyle.java,v 1.1 2010/06/15 12:29:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.html ;

import org.w3c.www.mime.MimeType;

/**
 * a sample Style class for HTML documents
 */

public class HtmlStyle {
    MimeType        type = MimeType.TEXT_CSS;
    StringBuffer    style = null;

    /**
     * set the type of style sheet used
     * @param style the sheet's MimeType.
     */

    public void setType(MimeType type)
    {
	this.type = type;
    }

    /**
     * append a string to the style buffer
     * @param str1 A string to be append
     */

    public void append(String str1)
    {
	if(style == null)
	    style = new StringBuffer(str1);
	else
	    style.append(str1);
    }

    /**
     * genereate a String representation that can be
     * append in a HTML document
     */

    public String toString()
    {
	if(style == null)
	    return "";
	return "<STYLE TYPE=\"" + type.toString() + "\">\n"
	       + style.toString() + "\n</STYLE>\n" ;
    }

    public HtmlStyle(MimeType type, String style)
    {
	setType(type);
	append(style);
    }

    public HtmlStyle(String style)
    {
	append(style);
    }
}
