// PropertyFeeder.java
// $Id: RealmUsersFeeder.java,v 1.1 2010/06/15 12:22:48 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import java.util.Properties;

/**
 * RealmUsersFeeder :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class RealmUsersFeeder implements EditorFeeder {

    RemoteResourceWrapper rrw;
    Properties p;

    public String[] getDefaultItems() {
	return getStringArray(rrw, p);
    }

    protected String [] getStringArray(RemoteResourceWrapper rrw, 
				       Properties p) 
    {
	RemoteResourceWrapper w      = rrw;
	RemoteResource        rm     = w.getResource();
	RemoteResource        target = null;
	String                realm  = null;
	try {
	    realm = (String)rm.getValue("realm");
	} catch (RemoteAccessException ex) {
	    ex.printStackTrace();
	}
	if (realm == null)
	    return new String[0];

	do {
	    w = w.getFatherWrapper();
	    if (w != null) {
		rm = w.getResource();
		try {
		    if ((rm.getClassHierarchy())[0].equals
			("org.w3c.jigsaw.http.ConfigResource"))
			target = rm.loadResource("realms");
		} catch (RemoteAccessException ex) {
		    ex.printStackTrace();
		}
	    }
	    else 
		return new String[0];
	} while (target == null);
	
	try {
	    target = target.loadResource(realm);
	} catch (RemoteAccessException ex) {
	    target = null;
	}
	if (target == null)
	    return new String[0];
	try {
	    return target.enumerateResourceIdentifiers();
	} catch (RemoteAccessException ex) {
	    ex.printStackTrace();
	    return new String[0];
	}
    }

    public void initialize (RemoteResourceWrapper rrw, Properties p) {
	this.rrw = rrw;
	this.p   = p;
    } 

}
