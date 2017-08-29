// PropertyManager.java
// $Id: PropertyManager.java,v 1.1 2010/06/15 12:26:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.w3c.jigsaw.admin.RemoteAccessException;
import org.w3c.jigsaw.admin.RemoteResource;

import org.w3c.tools.resources.Attribute;

class ResourceClassProperties {

  // The class we're handling the configuration for
  String entryPath = null;
  // The zip file this classes configuration is at
  ZipFile zip = null;
  // The class editor properties
  Properties editorProperties = null;
  // The set of registered helpers for that editor's class
  String helperClasses[] = new String[0];
  // The helpers properties (map help classes to properties)
  Hashtable helperProperties = null;
  // The attribute properties
  Hashtable attributeProperties = null;

  /**
   * Parse | separated property value into a String array.
   * @param p Properties to look into.
   * @param name The property to look for and parse.
   * @return A non-null but potentially zero-length string array.
   */

  static String[] getStringArray(Properties p, String name) {
    String v = (String) p.get(name);
    if ( v == null )
      return new String[0];
    // Parse the property value:
    StringTokenizer st    = new StringTokenizer(v, "|");
    int             len   = st.countTokens();
    String          ret[] = new String[len];
    for (int i = 0 ; i < ret.length ; i++) {
      ret[i] = st.nextToken();
    }
    return ret;
	
  }

  /**
   * Does this class config allows for helper aggregation.
   * If <strong>true</strong> this means that the GUI should instantiate
   * all inherited helpers to construct the resource editor.
   * @return A boolean.
   */

  boolean aggregateHelpers() {
    if ( editorProperties == null )
      return true;
    String s = (String) editorProperties.get("aggregateHelpers");
    return (s != null) && s.equalsIgnoreCase("true");
  }

  /**
   * Does this class config allows for attribute aggregation.
   * If <strong>true</strong> this means that the GUI should instantiate
   * an editor for all inherited attributes.
   * @return A boolean.
   */

  boolean aggregateAttributes() {
    if ( editorProperties == null )
      return true;
    String s = (String) editorProperties.get("aggregateAttributes");
    return (s != null) && s.equalsIgnoreCase("true");
  }

  /**
   * Should the config aggregate specific attribute editor properties.
   * @return A boolean.
   */

  boolean aggregateAttributeProperties() {
    return false;
  }

  /**
   * Utility - Load a directory of prop files into given hashtable.
   * @param into Hashtable to fill in with Properties instance.
   * @param dir Directory to load.
   */

  void loadPropertyDirectory(Hashtable into, ZipFile zip, String dir) {
    try {
      Enumeration entries = zip.entries();
      while( entries.hasMoreElements() ) {
	ZipEntry entry = (ZipEntry) entries.nextElement();
	String   name  = entry.getName();
	if (name.startsWith(dir) && name.endsWith(".p")) {
	  Properties  p  = new Properties();
	  InputStream in = (new BufferedInputStream
			    (zip.getInputStream(entry)));
	  p.load(in);
	  in.close();
	  // Register them into the hashtable:
	  into.put(name.substring(dir.length()+1, name.length()-2), p);
	}
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Initialize that class config.
   * This method will load the resource editor properties, than the helper
   * specific properties, and finally the attribute properties.
   */

  void initialize() {
    // Load the editor properties:
    try {
      ZipEntry propentry = zip.getEntry(entryPath + "properties");
      if (propentry != null) {
	InputStream in = (new BufferedInputStream
			  (zip.getInputStream(propentry)));
	editorProperties = new Properties();
	editorProperties.load(in);
	in.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    // From the editor properties, get the set of helpers:
    if ( editorProperties != null )
      helperClasses = getStringArray(editorProperties, "helpers");
    // Load the helpers specific properties:
    helperProperties = new Hashtable(11);
    loadPropertyDirectory(helperProperties, zip, entryPath + "helpers");
    // Load the specific attribute editor properties:
    attributeProperties = new Hashtable(11);
    loadPropertyDirectory(attributeProperties, zip,entryPath + "attrs");
  }

  String[] getHelperClasses() {
    return helperClasses;
  }

   
  Properties getHelperProperties(String clsname) {
    Properties p = (Properties) helperProperties.get(clsname);
    return p;
  }

  Properties getAttributeProperties(String name) {
    Properties p = (Properties) attributeProperties.get(name);
    return p;
  }

  Properties getEditorProperties() {
    return editorProperties;
  }

  ResourceClassProperties(String entryPath, ZipFile zip) {
    this.entryPath = entryPath;
    this.zip       = zip;
    initialize();
  }
}

public class PropertyManager {

  /**
   * The root for the property files.
   */
  protected File root    = null;
  protected File zipfile = null;
  /**
   * The set of resource classes we know about.
   * Maps class names to ResourceClassProperty
   */
  protected Hashtable classProperties = null;
  /**
    /**
     * the mapping of icon names to icon locations
     */
  protected Properties iconProperties = null;

  /**
   * the hashtable of mime types
   */
  protected Hashtable mimeTypes = null;

  private boolean inited = false;

  protected static Properties merge(Properties into
				    , Properties source
				    , boolean overide) {
    Properties  p = (Properties) into.clone();
    Enumeration e = source.keys();
    while ( e.hasMoreElements() ) {
      Object key = e.nextElement();
      if ((! overide) && into.get(key) != null)
	continue;
      Object val = source.get(key);
      p.put(key, val);
    }
    return p;
  }

  protected static Properties merge(Properties into, Properties source) {
    return merge(into, source, true);
  }

  /**
   * Load the properties from the root directory.
   */

  protected void initialize() {
    ZipFile zip = null;
    try {
      zip = new ZipFile(this.zipfile);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    Enumeration entries = zip.entries();
    classProperties = new Hashtable(11);

    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      String entry_name = entry.getName();
      if (entry_name.equals( "icons"+ File.separator)) //reserved
	continue;
      // Skip non directory entries
      if (! entry.isDirectory() )
	continue;
      // only first level sub directories
      if (entry_name.indexOf(File.separator) == 
	  entry_name.lastIndexOf(File.separator)) {
	ResourceClassProperties rcp = null;
	rcp = new ResourceClassProperties(entry_name, zip);
	classProperties.put(entry_name.substring(0, entry_name.length()-1 ),
			    rcp);
      }
    }
    // and now the icons mapping
    try {
      ZipEntry icons = zip.getEntry("icons.p");
      InputStream in = (new BufferedInputStream
			(zip.getInputStream(icons)));
      iconProperties = new Properties();
      iconProperties.load(in);
      in.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    // and now the mime types
    mimeTypes = new Hashtable(11);
    try {
      ZipEntry mime = zip.getEntry("mimetypes.p");
      InputStream in = (new BufferedInputStream
			(zip.getInputStream(mime)));
      Properties p = new Properties();
      p.load(in);
      in.close();
      String[] major = ResourceClassProperties.getStringArray(p,"Types");
      String[] minor;
      for(int i=0; i<major.length; i++) {
	minor = ResourceClassProperties.getStringArray(p, major[i]);
	mimeTypes.put(major[i], minor);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    try {
      zip.close(); //FIXME: sure?
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    inited = true;
  }

  /**
   * Get the best matching resource class properties for given class.
   * @param classes The class names we're looking a macth for.
   * @return A ResourceClassProperties instance.
   */

  protected ResourceClassProperties 
  getResourceClassProperties(String names[], int from) {
    // Look along the hierarchy for a class editor:
    ResourceClassProperties rcp = null;
    for (int i = from ; i < names.length ; i++) {
      rcp = (ResourceClassProperties) classProperties.get(names[i]);
      if ( rcp != null)
	break;
    }
    if ( rcp == null )
      throw new RuntimeException("configuration error, no editor for: "+
				 names[0]);	
    return rcp;
  }

  protected
  ResourceClassProperties getResourceClassProperties(String names[]) {
    return getResourceClassProperties(names, 0);
  }

  protected int
  findResourceClassProperties(String classes[], int offset) {
    // Look along the hierarchy for a class editor:
    ResourceClassProperties rcp = null;
    for (int i = offset ; i < classes.length ; i++) {
      rcp = (ResourceClassProperties) classProperties.get(classes[i]);
      if ( rcp != null )
	return i;
    }
    return -1;
  }

  protected int findResourceClassProperties(String classes[]) {
    return findResourceClassProperties(classes, 0);
  }

  protected String[] getClassHierarchy(RemoteResourceWrapper rrw) {
    try {
      String classes[] = rrw.getResource().getClassHierarchy();
      return classes;
    } catch (RemoteAccessException ex) {
      ex.printStackTrace();
    }
    return new String[0];
  }

  protected Properties getDefaultHelperProperties(String clsname) {
    ResourceClassProperties rcp = null;
    rcp = (ResourceClassProperties) classProperties.get("defaults");
    if ( rcp != null )
      return rcp.getHelperProperties(clsname);
    return null;
  }

  protected Properties getDefaultAttributeProperties(String clsname) {
    ResourceClassProperties rcp = null;
    rcp = (ResourceClassProperties) classProperties.get("defaults");
    if ( rcp != null )
      return rcp.getAttributeProperties(clsname);
    return null;
  }

  /**
   * Get any properties for the editor of the given remote resource instance.
   * @return A Properties instance.
   */

  public Properties getEditorProperties(RemoteResourceWrapper rrw) {
    String classes[] = getClassHierarchy(rrw);
    return getResourceClassProperties(classes).getEditorProperties();
  }

  /**
   * Get the list of helpers to be created for the given remote resource.
   * @return A set of helper class names (as an array).
   */

  public String[] getHelperClasses(RemoteResourceWrapper rrw) {
    String classes[] = getClassHierarchy(rrw);
    // Load in the most specific helpers:
    int slot = findResourceClassProperties(classes);
    if ( slot < 0 )
      return new String[0];
    ResourceClassProperties rcp = getResourceClassProperties(classes);
    Vector vhelpers = new Vector();
    vhelpers.addElement(rcp.getHelperClasses());
    // Look up the hierarchy, while allowed:
    while ( rcp.aggregateHelpers() ) {
      if ( ++slot >= classes.length )
	break;
      if ((slot = findResourceClassProperties(classes, slot)) < 0)
	break;
      rcp  = getResourceClassProperties(classes, slot);
      vhelpers.addElement(rcp.getHelperClasses());
    }
    // Convert the vector of String arrays into a single String array:
    if ( vhelpers.size() == 1 ) 
      return (String[]) vhelpers.elementAt(0);
    int sz = 0;
    int n  = 0;
    for (int i = 0 ; i < vhelpers.size() ; i++)
      sz += ((String[]) vhelpers.elementAt(i)).length;
    String helpers[] = new String[sz];
    for (int i = 0 ; i < vhelpers.size() ; i++) {
      String s[] = (String[]) vhelpers.elementAt(i);
      for (int j = 0 ; j < s.length ; j++)
	helpers[n++] = s[j];
    }
    return helpers;
  }

  /**
   * Get the properties for the helper of a given remote resource.
   * @param rr The remote resource being edited.
   * @param helperClass Class of the helper about to be created.
   * @return An instance of Properties.
   */

  public Properties getHelperProperties(RemoteResourceWrapper rrw
					, String helperClass) {
    String classes[] = getClassHierarchy(rrw);
    int slot = findResourceClassProperties(classes);
    if ( slot < 0 )
      return new Properties();
    ResourceClassProperties rcp = getResourceClassProperties(classes);
    // Properties p = getDefaultProperties(helperClass);
    Properties p = rcp.getHelperProperties(helperClass);
    while ( rcp.aggregateHelpers() ) {
      if ( ++slot >= classes.length )
	break;
      if ((slot = findResourceClassProperties(classes, slot)) < 0)
	break;
      rcp  = getResourceClassProperties(classes, slot);
      Properties more = rcp.getHelperProperties(helperClass);
      if ( more != null )
	p = (p != null) ? merge(p, more, false) : more;
    }
    return (p == null) ? new Properties() : p;
  }

  /**
   * Get the properties for the attribute editor of a given remote resource.
   * @param rr The remote resource being edited.
   * @param attr The attribute being edited.
   */

  public Properties getAttributeProperties(RemoteResourceWrapper rrw
					   , Attribute attr) {
    String classes[] = getClassHierarchy(rrw);
    String attrClass = attr.getClass().getName();
    int slot = findResourceClassProperties(classes);
    if ( slot < 0 )
      return new Properties();
    ResourceClassProperties rcp = getResourceClassProperties(classes);
    Properties p = rcp.getAttributeProperties(attr.getName());
    if ( p == null )
      p = new Properties();
    Properties defs = getDefaultAttributeProperties(attrClass);
    if ( defs != null ) 
      p = merge(defs, p);
    while ( rcp.aggregateAttributeProperties() ) {
      if ( ++slot >= classes.length )
	break;
      if ((slot = findResourceClassProperties(classes, slot)) < 0 )
	break;
      rcp  = getResourceClassProperties(classes, slot);	    
      Properties more = rcp.getAttributeProperties(attr.getName());
      if ( more != null )
	p = merge(more, p);
    }
    return p;
  }

  /**
   * get the icon name resolved from its description
   * @param name a String 
   */

  public String getIconLocation(String name) {
    String rname = iconProperties.getProperty(name);
    if (rname != null) {
      boolean rel; 
      rel = iconProperties.getProperty("relative").equals("true");
      return (rel) ? root.getAbsolutePath() +
	File.separator + "icons" + File.separator + rname : rname;
    }
    return null;
  }

  /**
   * get the hashtable of mimetypes
   */

  public Hashtable getMimeTypes() {
    return mimeTypes;
  }

  public static String ROOT_P = "jigadmRoot";

  public static PropertyManager propertyManager = null;

  public static PropertyManager getPropertyManager() {
    if ( propertyManager == null ) {
      Properties p = System.getProperties();
      String base = p.getProperty(ROOT_P);
      if (base == null) {
	propertyManager = new PropertyManager(
			  new File(p.getProperty("user.dir") + File.separator +
				   "config" + File.separator + "jigadm.zip"),
			  new File(p.getProperty("user.dir") + File.separator +
				   "config"));
      }
      else
	propertyManager = new PropertyManager(
		          new File(base + File.separator +"config" +
				   File.separator + "jigadm.zip"),
		          new File(base + File.separator +"config"));
    }
    return propertyManager;
  }

  public PropertyManager(File zipfile, File root) {
    this.root    = root;
    this.zipfile = zipfile;
    initialize();
    if ( ! this.inited ) {
      // The one who wrote that code has already booked a special
      // place in hell
      System.out.println("PropertyManager: unable to initialize.");
      System.out.println("\tCouldn't find mandatory properties in \""+
			 root+"\" use the \"-root\" option or run "+
			 "jigadmin from Jigsaw root directory.");
      System.exit(1);
    }
  }
}


