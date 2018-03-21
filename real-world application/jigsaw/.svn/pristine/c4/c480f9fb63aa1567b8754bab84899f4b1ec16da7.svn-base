// DAVIf.java
// $Id: DAVIf.java,v 1.1 2010/06/15 12:27:41 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import java.util.LinkedList;
import java.util.ListIterator;

import org.w3c.www.http.HttpInvalidValueException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVIf {
    
    private String taggedResource = null;

    private LinkedList lists = null; // List of list

    public void addList(byte raw[], int start, int end) 
	throws HttpInvalidValueException
    {
	// stuff like <...> [...] Not <...> [...]
	LinkedList list  = new LinkedList();
	boolean    isnot = false;
	for (int i = start; i < end ; i++) {
	    int idx = i + 1;
	    switch (raw[i]) 
		{
		case (byte) '<':
		    // State Token
		    while ((raw[i] != (byte)'>') && (i <= end)) { i++; }
		    String state = new String(raw, idx, i-idx);
		    DAVStateToken token = new DAVStateToken(state, isnot);
		    list.add(token);
		    isnot = false;
		    break;
		case (byte) '[':
		    // ETag
		    while ((raw[i] != (byte)']') && (i <= end)) { i++; }
		    DAVEntityTag etag = 
			new DAVEntityTag(raw, idx, i-idx, isnot);
		    list.add(etag);
		    isnot = false;
		    break;
		case (byte) 'N':
		    // Not
		    if (i > end - 2) {
			throw new HttpInvalidValueException("Invalid header");
		    }
		    isnot = ((raw[i+1] == (byte)'o') &&
			     (raw[i+2] == (byte)'t'));
		    i += 2;
		    break;
		case (byte) ' ':
		case (byte) '\t':
		    //skip spaces
		    break;
		default:
		    // error
		    String msg = "got '"+(char)raw[i]+"'";
		    throw new HttpInvalidValueException("Invalid header: "+
							msg);
		}
	}
	lists.add(list);
    }

    public boolean hasResource() {
	return (taggedResource != null);
    }

    public String getResource() {
	return taggedResource;
    }

    public ListIterator getTokenListIterator() {
	return lists.listIterator(0);
    }

    DAVIf() {
	this.lists = new LinkedList();
    }

    DAVIf (String resource) 
	throws HttpInvalidValueException
    {
	this.taggedResource = DAVParser.decodeURL(resource);
	this.lists = new LinkedList();
    }

    /**
     * Public Constructor
     * @param resource the tagged resource (can be null)
     * @param taglist a List of DAVStateToken and/or DAVEntityTag
     * @see DAVStateToken
     * @see DAVEntityTag
     */
    public DAVIf(String resource, LinkedList taglist) {
	this.lists          = taglist;
	this.taggedResource = resource;
    }
    

}
