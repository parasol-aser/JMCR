// ConfigResource.java
// $Id: ConfigResource.java,v 1.1 2010/06/15 12:22:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;

class ConfigResourceEnumeration implements Enumeration {

    private static final String ids[] = {
	ConfigResource.SPACE_NAME,
	"properties",
	"indexers",
	"realms",
	"control"
    };
    int idx = 0;

    public boolean hasMoreElements() {
	return idx < ids.length;
    }

    public Object nextElement() {
	if ( idx >= ids.length )
	    throw new NoSuchElementException("config resource enumeration");
	return ids[idx++];
    }

    ConfigResourceEnumeration() {
	this.idx = 0;
    }
}

class PropertySetEnumeration implements Enumeration {
    Enumeration e = null;

    public boolean hasMoreElements() {
	return e.hasMoreElements();
    }

    public Object nextElement() {
	return ((Resource) e.nextElement()).getIdentifier();
    }

    protected PropertySetEnumeration(Enumeration e) {
	this.e = e;
    }

}

class PropertiesConfig extends AbstractContainer {
    httpd server = null;

    public void registerResource(String n, Resource c, Hashtable d) {
	throw new RuntimeException("static container");
    }

    public ResourceReference createDefaultResource(String name) {
	throw new RuntimeException("static container");
    }

    public void delete(String name) {
	throw new RuntimeException("static container");
    }	

    public Enumeration enumerateResourceIdentifiers(boolean all) {
	return new PropertySetEnumeration(server.enumeratePropertySet());
    }

    public ResourceReference lookup(String name) {
	return new DummyResourceReference(server.getPropertySet(name));
    }

    protected PropertiesConfig(httpd server) {
	this.server = server;
    }

}

public class ConfigResource extends AbstractContainer {

    public final static String SPACE_NAME = "docs_space";

    protected httpd server = null;
    protected ResourceReference propConfig = null;
    protected ResourceReference realmConfig = null;
    protected ResourceReference controlConfig = null;
    protected ResourceReference indexers = null;

    public void registerResource(String n, Resource c, Hashtable d) {
	throw new RuntimeException("static container");
    }

    public ResourceReference createDefaultResource(String name) {
	throw new RuntimeException("static container");
    }

    public void delete(String name) {
	throw new RuntimeException("static container");
    }	

    public Enumeration enumerateResourceIdentifiers(boolean all) {
	return new ConfigResourceEnumeration();
    }

    public ResourceReference lookup(String name) {
	if ( name.equals(SPACE_NAME) ) {
	    return server.getEditRoot();
	} else if ( name.equals("properties") ) {
	    return propConfig;
	} else if ( name.equals("indexers") ) {
	    return indexers;
	} else if ( name.equals("realms" ) ) {
	    return realmConfig;
	} else if ( name.equals("control") ) {
	    return controlConfig;
	}
	return null;
    }

    public ConfigResource(httpd server) {
	this.server = server;
	this.propConfig = 
	    new DummyResourceReference(new PropertiesConfig(server));
	this.realmConfig = 
	    new DummyResourceReference(server.getRealmsCatalog());
	this.controlConfig = 
	    new DummyResourceReference(new ControlResource(server));
	this.indexers = 
	    new DummyResourceReference( server.getIndexersCatalog() );
    }

}
