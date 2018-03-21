// DaemonProperties.java
// $Id: DaemonProperties.java,v 1.1 2010/06/15 12:26:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.daemon;

import java.util.Hashtable;
import java.util.Properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.w3c.util.ObservableProperties;

/**
 * Subclass of Properties, to deal with daemon specific usage of them.
 * This class refines the basic Properties class, in order to tune them for
 * ServerHandler specific needs:
 * <ul>
 * <li>Properties can be observed through the PropertyMonitoring interface,
 * <li>Properties are multiplexed according to the server identifier. Two
 * servers can rely on the same set of properties, but each of them can
 * have its own property value. Eg the org.w3c.jigsaw.http package host
 * property can be set fro server1 to host1 and for server2 to host2. 
 * This is done by defining server1.org.w3c.jigsaw.http.host 
 * and server2.org.w3c.jigsaw.http.host
 * </ul>
 * <p>Each property can be monitored, to allow for dynamic reconfiguration of
 * the server.
 * @see org.w3c.util.PropertyMonitoring
 */

public class DaemonProperties {
    /**
     * The set of loaded properties set.
     */
    protected Hashtable propspace = new Hashtable(5);
    /**
     * The global set of properties (inherited by all spaces).
     */
    protected Properties globprops = null;
    /**
     * Our base config directory.
     */
    protected File configdir = null;

    /**
     * Extend a property space.
     * @param id The identifier of the property set to extend.
     * @param in The input stream containing Java properties to add.
     * @exception IOException If the input stream couldn't be read.
     */

    public ObservableProperties loadPropertySpace(String id, InputStream in)
	throws IOException
    {
	ObservableProperties p = (ObservableProperties) propspace.get(id);
	if ( p == null ) {
	    p = new ObservableProperties(globprops);
	    propspace.put(id, p);
	}
	p.load(in);
	return p;
    }

    /**
     * Load in the default properties for the given space.
     * The file from which properties are loaded is kept itself as the
     * <code>org.w3c.jigsaw.propfile</code> property.
     * @param id The identifier of the property set to load.
     * @exception FileNotFoundException If the default property file wasn't 
     * found.
     * @exception IOException If default property file couldn't be read.
     */

    public ObservableProperties loadPropertySpace(String id)
	throws IOException, FileNotFoundException
    {
	File                 file = new File(configdir, id+".props");
	ObservableProperties p    = null;
	p = loadPropertySpace(id, (new BufferedInputStream
				   (new FileInputStream(file))));
	p.put(org.w3c.jigsaw.http.httpd.PROPS_P, file.getAbsolutePath());
	return p;
    }

    /**
     * Get the properties for the given space.
     * @param id The identifier for a property set space.
     * @return An ObservableProperties instance, or <strong>null</strong>.
     */

    public ObservableProperties getPropertySpace(String id) {
	return (ObservableProperties) propspace.get(id);
    }

    // FIXME
    public void save() {
	System.out.println("DaemonProperties.save: not implemented !");
    }

    // FIXME 
    public void savePropertySpace(String id) {
	System.out.println("DaemonProperties.save ["+id+"]; not implemented!");
    }

    // FIXME doc
    public void load(InputStream in) 
	throws IOException
    {
	if ( globprops == null )
	    globprops = new Properties();
	globprops.load(in);
    }

    // FIXME doc
    public String getProperty(String name) {
	return (globprops != null) ? globprops.getProperty(name) : null;
    }

    public String getString(String name, String def) {
	return (globprops != null) ? globprops.getProperty(name, def) : def;
    }

    /**
     * @param props The global properties to use in all spaces.
     */

    public DaemonProperties (File configdir, Properties props) {
	this.configdir = configdir;
	this.globprops = props;
    }

}

 
