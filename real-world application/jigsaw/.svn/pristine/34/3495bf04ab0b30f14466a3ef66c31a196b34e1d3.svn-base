// HttpExt.java
// $Id: HttpExt.java,v 1.1 2010/06/15 12:19:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class HttpExt {

    protected String    name      = null;
    protected String    ns        = null;
    protected Hashtable exts      = null;
    protected boolean   generated = true;
    protected boolean   headers   = false;

    protected void setName(String name) {
	this.name = name;
    }

    /**
     * Get the http extension declaration name.
     * @return a String instance
     */
    public String getName() {
	return name;
    }

    protected void setNamespace(String ns) {
	this.ns = ns;
        this.headers = true;
    }

    /**
     * Get the http extension declaration namespace.
     * @return a String instance
     */
    public String getNamespace() {
	return ns;
    }

    /**
     * Does this extension needs specific headers?
     * @return a boolean.
     */
    public boolean needsHeaders() {
	return headers;
    }

    /**
     * Add an http extension declaration <token/value>
     * @param name the token name.
     * @param value the value.     
     */
    public void addDeclExt(String token, String value) {
	exts.put(token, value);
    }

    /**
     * Get an http extension declaration token value.
     * @param name the token name.
     * @return a String instance
     */
    public String getDeclExt(String name) {
	return (String)exts.get(name);
    }

    /**
     * Get all http extension declaration <token/value>
     * @return an Enumeration instance
     */
    public Enumeration getDeclExtNames() {
	return exts.keys();
    }

    protected String getRealHeader(String header) {
	return ns+header;
    }

    public String toString() {
	String string = "\""+name+"\" ; ns="+ns;
	Enumeration e = exts.keys();
	while (e.hasMoreElements()) {
	    String tok = (String) e.nextElement();
	    String val = (String) exts.get(tok);
	    string += ("; "+tok+"="+val);
	}
	return string;
    }

    protected boolean isGenerated() {
	return generated;
    }

    /**
     * Constructor, for User
     * @param name the Http extension declaration name 
     * @param header Does this extension needs specific headers?
     * (absoluteURI or field-name)
     */
    public HttpExt(String name, boolean headers) {
	this.generated = false;
	this.name      = name;
	this.exts      = new Hashtable(3);
	this.headers   = headers;
    }

    /**
     * Constructor, for User
     * @param old the old Http extension declaration  
     * If you want to reply the same extension, use this
     * contructor.
     */
    public HttpExt(HttpExt old) {
	this.generated = false;
	this.name      = old.name;
	this.exts      = new Hashtable(3);
	this.headers   = old.headers;
    }

    protected HttpExt() {
	this.generated = true;
	this.headers   = false;
	this.exts      = new Hashtable(3);
    }
}
