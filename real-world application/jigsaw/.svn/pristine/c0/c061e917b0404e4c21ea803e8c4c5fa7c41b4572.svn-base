// PropertyFeeder.java
// $Id: HttpServerResourceFeeder.java,v 1.1 2010/06/15 12:22:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.RemoteResourceWrapper;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import java.util.Properties;

/**
 * HttpServerResourceFeeder :
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class HttpServerResourceFeeder implements EditorFeeder {

  public static final String RESOURCE_P = "feeder.resource";

  String[] s = null;

  public String[] getDefaultItems() {
    return s;
  }

  protected String [] getStringArray(RemoteResourceWrapper rrw, Properties p) {
    String name = (String)p.get(RESOURCE_P);
    if (name == null)
      return new String[0];
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
      }
      else 
	return new String[0];
    } while (target == null);
    try {
      return target.enumerateResourceIdentifiers();
    } catch (RemoteAccessException ex) {
      ex.printStackTrace();
      return new String[0];
    }
  }

  public void initialize (RemoteResourceWrapper rrw, Properties p) {
    s = getStringArray(rrw, p);
  } 

}
