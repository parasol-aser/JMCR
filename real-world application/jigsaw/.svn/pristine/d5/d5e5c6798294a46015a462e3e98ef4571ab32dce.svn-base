// ControlResource.java
// $Id: ControlResource.java,v 1.1 2010/06/15 12:22:01 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;

import org.w3c.util.ObservableProperties;

import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

class ControlResourceEnumeration implements Enumeration {
    private static final String ids[] = {
	"checkpoint",
	"save",
	"restart",
	"shutdown"
    };
    int idx = 0;

    public boolean hasMoreElements() {
	return false;
    }

    public Object nextElement() {
//	if ( idx >= ids.length )
	throw new NoSuchElementException("control resource enumeration");
//	return ids[idx++];
    }

    ControlResourceEnumeration() {
	this.idx = 0;
    }
}

public class ControlResource extends AbstractContainer {
    protected httpd server = null;

    protected ResourceReference self = null;

    public void registerResource(String n, Resource c, Hashtable d) {
	throw new RuntimeException("static container");
    }
			       
    public void delete(String name) {
	throw new RuntimeException("static container");
    }	

    public ResourceReference createDefaultResource(String name) {
	throw new RuntimeException("static container");
    }	

    protected void saveProperties() {
	ObservableProperties props = server.getProperties();
	File propfile = new File(props.getString(httpd.PROPS_P, null));
	// Did we guessed were the place to save the property file ?
	if ( propfile == null) {
	    throw new RuntimeException("Unable to save properties: property "+
				       httpd.PROPS_P+" undefined.");
	} else {
	    try {
		FileOutputStream fout = new FileOutputStream(propfile);
		server.getProperties().store (fout, "Jigsaw written") ;
		fout.close() ;
	    } catch (IOException ex) {
		// FIXME
	    }
	}  
	server.errlog ("Properties " + propfile + " have been saved.");
    }

    public ResourceReference lookup(String name) {
	if ( name.equalsIgnoreCase("checkpoint") ) {
	    server.startCheckpoint();
	} else if ( name.equalsIgnoreCase("save") ) {
	    saveProperties();
	    server.checkpoint();
	} else if ( name.equalsIgnoreCase("restart") ) {
	    server.restart();
	} else if ( name.equalsIgnoreCase("stop") ) {
	    server.shutdown();
	}
	if (self == null)
	    self = new DummyResourceReference(this);
	return self;
    }

    /**
     * Get the server this resource is served by.
     * @return The first instance of Jigsaw this resource was attached to.
     */
    public ServerInterface getServer() {
	return server;
    }

    private String computeHelpUrl() {
	try {
	    URL url = new URL(getServer().getDocumentationURL());
	    URL docurl = new URL(url.getProtocol(),
				 url.getHost(),
				 url.getPort(),
				 "/Doc/Overview.html");
	    return docurl.toExternalForm();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    synchronized public Object getValue (int idx, Object def) {
	if ((idx == ATTR_HELP_URL) && (values[ATTR_HELP_URL] == null))
	    values[ATTR_HELP_URL] = computeHelpUrl();
	return super.getValue(idx, def);
    }

    public Enumeration enumerateResourceIdentifiers(boolean all) {
	return new ControlResourceEnumeration();
    }

    public ControlResource(httpd server) {
	this.server = server;
    }

   
}


