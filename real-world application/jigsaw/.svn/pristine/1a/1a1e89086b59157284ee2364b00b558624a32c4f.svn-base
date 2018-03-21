// IndexFeeder.java
// $Id: IndexFeeder.java,v 1.1 2010/06/15 12:22:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class IndexFeeder implements EditorFeeder {

    public static final String FILTER_P    = "feeder.filter";
    public static final String FORBID_RULE = "*forbid*";

    String[] sdefault = { FORBID_RULE };
    String[] extentions = null;
    String[] s = null;

    public String[] getDefaultItems() {
	return s;
    }

    protected boolean match(String s, String[] extensions) {
	for (int i=0 ; i < extensions.length ; i++) {
	    if (s.endsWith("."+extensions[i]))
		return true;
	}
	return false;
    }

    protected String[] filter(String[] children, String[] extensions) {
	if (extensions == null)
	    return children;
	Vector V = new Vector(12);
	V.addElement(FORBID_RULE);
	for (int i=0 ; i < children.length ; i++) {
	    if (match(children[i], extensions))
		V.addElement(children[i]);
	}
	String filtered[] = new String[V.size()];
	V.copyInto(filtered);
	return filtered;
    }

    protected RemoteResource getResource(RemoteResourceWrapper rrw,
					 Properties p) 
    {
	return rrw.getResource();
    }

    protected String [] getStringArray(RemoteResourceWrapper rrw,
				       Properties p) 
    {
	String extensions[] = null;
	String exts         = (String)p.get(FILTER_P);

	if (exts != null) {
	    StringTokenizer st    = new StringTokenizer(exts, "|");
	    int             len   = st.countTokens();
	    extensions = new String[len];
	    for (int i = 0 ; i < extensions.length ; i++) {
		extensions[i] = st.nextToken();
	    }
	}
	try {
	    RemoteResource rm = getResource(rrw, p);
	    if (rm.isContainer()) {
		return filter(rm.enumerateResourceIdentifiers(),
			      extensions);
	    } else if (rm.isFrame()) {
		//are we a frame?
		rm = rrw.getFatherResource();
		if ((rm != null) && (rm.isContainer()))
		    return filter(rm.enumerateResourceIdentifiers(),
				  extensions);
	    }
	} catch (RemoteAccessException ex) {
	    //nothing to do
	}
	return new String[0];
    }

    public void initialize (RemoteResourceWrapper rrw, Properties p) {
	s = getStringArray(rrw, p);
	if (s.length == 0)
	    s = sdefault;
    } 
}
