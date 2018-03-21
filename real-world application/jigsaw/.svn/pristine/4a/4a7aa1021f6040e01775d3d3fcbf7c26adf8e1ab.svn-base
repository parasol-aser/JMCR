// Upgrader.java
// $Id: Upgrader.java,v 1.2 2010/06/15 17:53:10 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UTFDataFormatException;
import java.io.Writer;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import java.lang.reflect.Constructor;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.daemon.ServerHandlerManager;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.UnknownResource;
import org.w3c.tools.resources.store.ResourceStoreManager;
import org.w3c.tools.resources.serialization.Serializer;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Upgrader {

    public static final boolean debug = true;

    Serializer serializer = null;
    String     server     = null;
    File       config     = null;
    File       props      = null;

    public static void usage() {
	System.err.println("Usage : java "+
			   "org.w3c.tools.resources.upgrade.Upgrader "+
			   "<Jigsaw Install Directory>\n");
	System.exit(-1);
    }

    /**
     * Get the index of the given attribute in the given attribute array
     * @param name, the attribute name.
     * @param attributes, the atribute array.
     * @return an int
     */ 
    public static int lookupAttribute(String name, 
			       org.w3c.tools.resources.Attribute attributes[]) 
    {
	for (int i = 0 ; i < attributes.length ; i++) {
	    if ( name.equals(attributes[i].getName()) )
		return i;
	}
	return -1 ;
    }

    /**
     * Get the old attribute relative to the given new kind of attribute.
     * @param attr the new attribute
     * @return the old attribute
     */
    public static 
	Attribute getOldAttribute(org.w3c.tools.resources.Attribute attr)
	throws UpgradeException
    {
	String classname = attr.getClass().getName();
	int idx = classname.lastIndexOf('.');
	String newclassname = 
	    "org.w3c.tools.resources.upgrade."+classname.substring(idx+1);
	
	try {
	    Class c = Class.forName(newclassname);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    Class params[] = new Class[3];
	    params[0] = Class.forName("java.lang.String");
	    if (attr.getType() == null)
		throw new UpgradeException("*** ERROR : no type defined for "+
					   attr.getName());
	    // welcome to the land of ugly hacks
	    if (attr.getType().equals("java.util.Date"))
		params[1] = Class.forName("java.lang.Long");
	    else
		params[1] = Class.forName(attr.getType());
	    params[2] = Class.forName("java.lang.Integer");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    Constructor cons = c.getConstructor(params);
	    Object initargs[] = { attr.getName(), 
				  attr.getDefault(), 
				  new Integer(1) } ;
	    return (Attribute)cons.newInstance(initargs);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException("unable to instanciate :"+newclassname);
	}
    }

    public static void unpickleProlog(DataInputStream in)
	throws IOException
    {
	in.readInt();
	in.readUTF();
	in.readInt();
    }

    public static void readIndex(DataInputStream in)
	throws IOException
    {
	int count = in.readInt() ;
	for (int i = 0 ; i < count ; i++) {
	    in.readInt();
	    in.readInt();
	    in.readUTF();
	}
    }

    /**
     * load a resoure from the given DataInputStream.
     * @param in the DataInputStream
     * @return a Resource instance
     */
    public static Resource readResource(DataInputStream in)
	throws IOException, UpgradeException
    {
	String classname = null;
	try {
	    classname = in.readUTF();
	} catch (EOFException ex) {
	    //no more resources
	    return null;
	} catch (UTFDataFormatException utfex) {
	    //silent...
	    return null;
	}

	if (classname.startsWith("org.w3c.jigsaw.map")) {
	    System.out.println("\n*** WARNING : "+classname+
			       " is no more supported.");
	    return new UnknownResource();
	}

	try {
	    Class     c      = Class.forName(classname);
	    Resource  res    = (Resource)c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    Hashtable values = new Hashtable(10);
	    org.w3c.tools.resources.Attribute attrs[] = 
	       org.w3c.tools.resources.AttributeRegistry.getClassAttributes(c);

	    boolean slowpickle = ! in.readBoolean();
	    if (slowpickle) {
		String name = null;
		while ( ! (name = in.readUTF()).equals("") ) {
		    int ai = lookupAttribute(name, attrs);
		    int as = ((int) in.readShort() & 0xffff);
		    if ( ai >= 0 ) {
			Attribute oldattr = getOldAttribute(attrs[ai]);
			Object value = oldattr.unpickle(in);
			if (value != null)
			    values.put(name, value);
		    } else {
			in.skip(as);
		    }
		}
	    } else {
		for (int i = 0 ; i < attrs.length ; i++) {
		    if ( in.readBoolean() ) {
			Attribute oldattr = getOldAttribute(attrs[i]);
			Object value = oldattr.unpickle(in);
			if (value != null)
			    values.put(attrs[i].getName(), value);
		    }
		}
	    }
	    res.pickleValues(values);
	    return res;
	} catch (UTFDataFormatException utfex) {
	    //silent...
	    return null;
	} catch (ClassNotFoundException cnfex) {
	    System.out.println("\n*** ERROR loading "+classname);
	    System.out.println("*** Class not found : "+cnfex.getMessage());
	    return null;
	} catch (IllegalAccessException iaex) {
	    System.out.println("\n*** ERROR loading "+classname);
	    System.out.println("*** "+iaex.getMessage());
	    return null;
	} catch (InstantiationException iex) {
	    System.out.println("\n*** ERROR loading "+classname);
	    System.out.println("*** "+iex.getMessage());
	    return null;
	} catch (NoClassDefFoundError err) {
	    System.out.println("\n*** ERROR loading "+classname);
	    System.out.println("*** Class not found : "+err.getMessage());
	    return null;
	}
    }

    /**
     * Read the resource from a repository.
     * @param rep the repository (a File)
     * @return a Resource array
     */
    public static Resource[] readRepository(File rep) 
	throws FileNotFoundException, IOException, UpgradeException
    {
	DataInputStream in = null;
	in = new DataInputStream
	    (new BufferedInputStream 
	     (new FileInputStream(rep)));
	//first unpickle prolog
	unpickleProlog(in);
	//read the index
	readIndex(in);
	//read the resources
	Vector   vres = new Vector(10);
	Resource res  = null;
	do {
	    res = readResource(in);
	    if (res != null)
		vres.addElement(res);
	} while (res != null);
	Resource resources[] = new Resource[vres.size()];
	vres.copyInto(resources);
	return resources;
    }

    /**
     * update the index file (in stores)
     * @exception IOException if an IO error occurs
     */
    public void updateEntriesIndex(File storedir) 
	throws IOException, UpgradeException
    {
	if (storedir.isDirectory()) {
	    FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
		    return ((name.indexOf("-index") != -1) &&
			    (! name.endsWith(".bak")) &&
			    (! name.endsWith(".xml")));
		}
	    };
	    File files[] = storedir.listFiles(filter);
	    if ((files == null) || (files.length == 0)) {
		String msg = "\n*** ERROR : unable to find index file.\n"+
		    "*** Please check that \""+storedir.getAbsolutePath()+
		    "\" is the Jigsaw store directory";
		throw new UpgradeException(msg);
	    } else if (files.length > 1) {
		String msg = "\n*** ERROR: several index files found, "+
		    "please delete or move the invalid index files : ";
		for (int i = 0 ; i < files.length ; i++)
		    msg += "\n\t"+files[i].getName();
		throw new UpgradeException(msg);
	    } else {
		//good!
		File oldIndexFile = files[0];
		System.out.print("Found index file \""+
				 oldIndexFile.getName()+"\", upgrading...");
		File newIndexFile = new File(storedir,
					     oldIndexFile.getName()+".xml");
		ResourceStoreManager.updateEntriesIndex(oldIndexFile,
							newIndexFile,
							serializer);
		System.out.println(" done.");
	    }
	} else if (storedir.exists()) {
	    throw new UpgradeException("\n*** ERROR : \""+
				       storedir.getAbsolutePath()+
				       "\" is not a directory.");
	} else {
	    throw new UpgradeException("\n*** ERROR : \""+
				       storedir.getAbsolutePath()+
				       "\" doesn't exists.");
	}
    }

    /**
     * Create the subdirectories under /stores.
     */
    protected boolean createSubDirs(File storedir) {
	for (int i = 0 ; i < ResourceStoreManager.SUBDIRS ; i++) {
	    File subd = new File(storedir, Integer.toString(i));
	    if ((! subd.exists()) && ! subd.mkdirs())
		return false;
	}
	return true;
    }

    /**
     * Get the new store location.
     * @param oldstore the Old store.
     * @return a File instance
     */
    protected File getNewStore(File storedir, File oldstore) {
	String rep = oldstore.getName();
	if (rep.equals("root.idx")) {
	    return new File(storedir, "root.xml");
	} else {
	    int number = Integer.parseInt(rep.substring(3));
	    return new File(storedir, 
			    (number%ResourceStoreManager.SUBDIRS)+"/"+rep);
	}
    }

    /**
     * Upgrade the stores (st-xxx)  under the given store directory.
     * @param storedir the store directory
     * @param filter the filter to use to list the stores
     */
    protected void upgradeStores(File storedir, FilenameFilter filter) 
	throws UpgradeException
    {
	File stores[] = storedir.listFiles(filter);
	if (stores == null) {
	    System.err.println("No store files found!");
	    return;
	}
	System.out.print("Upgrading "+stores.length+" store files");
	for (int i = 0 ; i < stores.length ; i++) {
	    try {
		Resource resources[] = readRepository(stores[i]);
		File newstore = getNewStore(storedir, stores[i]);
		Writer writer = new BufferedWriter(new FileWriter(newstore));
		serializer.writeResources(resources, writer);
		System.out.print(".");
	    } catch (Exception ex) {
		System.err.println("\n*** ERROR : unable to upgrade "+
				   stores[i].getName());
		ex.printStackTrace();
	    }
	}
	System.out.println(" done.");
    }

    /**
     * Upgrade the stores (.db) under the given store directory.
     * @param storedir the store directory
     * @param filter the filter to use to list the stores
     */
    protected void upgradeDB(File storedir, FilenameFilter filter) {
	File stores[] = storedir.listFiles(filter);
	if (stores == null) {
	    System.err.println("No store files found!");
	    return;
	}
	System.out.print("Upgrading "+stores.length+" store files...");
	for (int i = 0 ; i < stores.length ; i++) {
	    try {
		Resource resources[] = readRepository(stores[i]);
		Writer writer = new BufferedWriter(new FileWriter(stores[i]));
		serializer.writeResources(resources, writer);
		System.out.print(".");
	    } catch (Exception ex) {
		System.err.println("\n*** ERROR : unable to upgrade "+
				   stores[i].getName());
		ex.printStackTrace();
	    }
	}
	System.out.println(" done.");
    }

    /**
     * backup the stores under the given store directory.
     * @param storedir the store directory
     * @param filter the filter to use to list the stores
     */
    protected void backupStores(File storedir, FilenameFilter filter) 
	throws IOException
    {
	File backupdir = new File(storedir, "backup");
	backupdir.mkdirs();
	System.out.print("Doing backup, copying store files in \""+
			 backupdir.getAbsolutePath()+"\"...");
	File files[] = storedir.listFiles(filter);
	for (int i = 0 ; i < files.length ; i++) {
	    File backuped = new File(files[i].getParentFile(),
				     "backup/"+files[i].getName());
	    org.w3c.util.IO.copy(files[i], backuped);
	}
	System.out.println(" done.");
    }

    /**
     * clean the stores under the given store directory.
     * @param storedir the store directory
     * @param filter the filter to use to list the stores
     */
    protected void cleanStores(File storedir, FilenameFilter filter) 
	throws IOException
    {
	System.out.print("Cleaning stores in \""+storedir.getAbsolutePath()+
			 "\"...");
	File files[] = storedir.listFiles(filter);
	for (int i = 0 ; i < files.length ; i++) {
	    files[i].delete();
	}
	System.out.println(" done.");
    }

    /**
     * Update the properties to add the serializer class in it.
     */
    protected void updateProperties(int version) {
	System.out.println("\nUpgrading properties files...");
	System.out.print("Updating \""+props.getName()+"\"...");
	Properties p = loadProps(props);
	p.put(httpd.SERIALIZER_CLASS_P, 
	      "org.w3c.tools.resources.serialization.xml.XMLSerializer");
	p.put(httpd.VERSCOUNT_P, String.valueOf(version));
	saveProps(p, props);
	System.out.println(" done.");
    }

    private Properties loadProps(File file) {
	Properties p = new Properties();
	FileInputStream in = null;
	try {
	    in = new FileInputStream (file);
	    p.load( in );
	} catch (Exception ex) {
	    System.err.println("*** ERROR : "+ex.getMessage());
	} finally { 
	    try { in.close(); } catch (Exception e) {}
	}
	return p;
    }

    private void saveProps(Properties p, File file) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream (file);
	    p.store( out, "Updated by Upgrader");
	} catch (Exception ex) {
	    System.err.println("*** ERROR : "+ex.getMessage());
	} finally {
	    try { out.close(); } catch (Exception e) {}
	}
    }

    protected void checkDir(File dir) 
	throws UpgradeException
    {
	if (! dir.exists()) {
	    String msg = "*** ERROR : \""+dir.getAbsolutePath()+
		"\" doesn't exists, please check that \""+
		dir.getParentFile().getParent()+
		"\" is the Jigsaw Install Directory.";
	    throw new UpgradeException(msg);
	} else if (! dir.isDirectory()) {
	    String msg = "*** ERROR : \""+dir.getAbsolutePath()+
		"\" is not a directory, please check that \""+
		dir.getParentFile().getParent()+
		"\" is the Jigsaw Install Directory.";
	    throw new UpgradeException(msg);
	}
    }

    /**
     * The big stuff, upgrade the configuration in XML
     * <ol>
     * <li> Upgrade the resource stores
     * <li> Upgrade the indexers (if any)
     * <li> Upgrade the realms (if any)
     * <li> Upgrade the properties files
     * </ol>
     * @exception IOException if an IO error occurs
     */
    public void upgrade(int version) 
	throws IOException, UpgradeException
    {
	checkDir(config);

	//
	// config/stores
	//

	File storedir = new File(config, "stores");
	checkDir(storedir);
	System.out.println("Upgrading stores in \""+
			   storedir.getAbsolutePath()+"\"...");
	updateEntriesIndex(storedir);
	System.out.print("Creating subdirectories... ");
	createSubDirs(storedir);
	System.out.println("done.");
	//store files
	FilenameFilter storefilter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return ((name.startsWith("st-") || 
			 name.equals("root.idx")) &&
			(! name.endsWith(".bak")));
	    }
	};
	FilenameFilter cleanfilter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return (name.startsWith("st-") || 
			name.equals("root.idx") ||
			name.equals("root.idx.bak") ||
			name.equals("state") ||
			name.endsWith("-index") ||
			name.endsWith("-index.bak"));
	    }
	};

	backupStores(storedir, cleanfilter);
	upgradeStores(storedir, storefilter);
	cleanStores(storedir, cleanfilter);

	//
	// config/auth
	//
	    
	storefilter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return name.endsWith(".db");
	    }
	};
	cleanfilter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		return name.endsWith(".bak");
	    }
	};

	try {
	    File authdir = new File(config, "auth");
	    checkDir(authdir);
	    System.out.println("\nUpgrading realms in \""+
			       authdir.getAbsolutePath()+"\"...");
	    backupStores(authdir, storefilter);
	    upgradeDB(authdir, storefilter);
	    cleanStores(authdir, cleanfilter);
	} catch (UpgradeException uex) {
	    //silent
	}

	//
	// config/idexers
	//

	try {
	    File indexers = new File(config, "indexers");
	    checkDir(indexers);
	    System.out.println("\nUpgrading indexers in \""+
			       indexers.getAbsolutePath()+"\"...");
	    backupStores(indexers, storefilter);
	    upgradeDB(indexers, storefilter);
	    cleanStores(indexers, cleanfilter);
	} catch (UpgradeException uex) {
	    //silent
	}

	updateProperties(version);

    }

    /**
     * Constructor
     * @param server the server name
     * @param config the server config directory
     * @param props the server properties file
     * @param serializer the XML Serializer
     */
    public Upgrader (String server, 
		     File config, 
		     File props,
		     Serializer serializer) 
    {
	this.server     = server;
	this.config     = config;
	this.props      = props;
	this.serializer = serializer;
    }

}
