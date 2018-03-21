// ResourceIndexFeeder.java
// $Id: ResourceIndexFeeder.java,v 1.1 2010/06/15 12:25:55 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.util.Properties;

import org.w3c.jigadm.RemoteResourceWrapper;
import org.w3c.jigadm.editors.HttpServerResourceFeeder;
import org.w3c.jigadm.editors.IndexFeeder;

import org.w3c.jigsaw.admin.RemoteResource;
import org.w3c.jigsaw.admin.RemoteAccessException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ResourceIndexFeeder extends IndexFeeder {

    protected RemoteResource getResource(RemoteResourceWrapper rrw,
					 Properties p)
    {
	String name = (String)p.get(HttpServerResourceFeeder.RESOURCE_P);
	if (name == null)
	    return null;
	RemoteResourceWrapper w = rrw;
	RemoteResource rm = w.getResource();
	RemoteResource target = null;
	do {
	    w = w.getFatherWrapper();
	    if (w != null) {
		rm = w.getResource();
		try {
		    if ((rm.getClassHierarchy())[0].equals
			("org.w3c.jigsaw.http.ConfigResource"))
			target = rm.loadResource(name);
		} catch (RemoteAccessException ex) {
		    ex.printStackTrace();
		}
	    } else {
		return null;
	    }
	} while (target == null);
	return target;
    }

}
