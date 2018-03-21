// ConnectionProp.java
// $Id: SocketConnectionProp.java,v 1.2 2010/06/15 17:53:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http.socket;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.config.PropertySet;

public class SocketConnectionProp extends PropertySet {
    private static String title = "Socket connection properties";

    static {
	Class     c = null;
	Attribute a = null;
	
	try {
          c = Class.forName("org.w3c.jigsaw.http.socket.SocketConnectionProp");
  	    //Added by Jeff Huang
  	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Minimum number of free spare threads
	a = new IntegerAttribute(SocketClientFactory.MINSPARE_FREE_P
			       , new Integer(SocketClientFactory.MINSPARE_FREE)
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Maximum number of free spare threads
	a = new IntegerAttribute(SocketClientFactory.MAXSPARE_FREE_P
			       , new Integer(SocketClientFactory.MAXSPARE_FREE)
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Maximum number of idle threads:
	a = new IntegerAttribute(SocketClientFactory.MAXSPARE_IDLE_P
			       , new Integer(SocketClientFactory.MAXSPARE_IDLE)
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Maximum number of simultaneous connections:
	a = new IntegerAttribute(SocketClientFactory.MAXCLIENTS_P
			       , new Integer(SocketClientFactory.MAXCLIENTS)
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Idle timeout for thread cache
	a = new IntegerAttribute(SocketClientFactory.IDLETO_P
			       , new Integer(SocketClientFactory.IDLETO)
			       , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
	// Max number of threads
	a = new IntegerAttribute(SocketClientFactory.MAXTHREADS_P
				 , new Integer(SocketClientFactory.MAXTHREADS)
				 , Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
        // Specific address to bidn the server socket
	a = new StringAttribute(SocketClientFactory.BINDADDR_P
				, null
				, Attribute.EDITABLE);
	AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * Get this property set title.
     * @return A String giving the title for this property set.
     */

    public String getTitle() {
	return title;
    }

    SocketConnectionProp(String name, httpd server) {
	super(name, server);
    }
}
