// SampleResourceIndexer.java
// $Id: SampleResourceIndexer.java,v 1.2 2010/06/15 17:53:01 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.indexer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import java.io.File;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.ContainerInterface;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringArrayAttribute;
import org.w3c.tools.resources.StringAttribute;

class SampleIndexerEnumeration implements Enumeration {
    private static final String list[] = {
	"directories".intern(),
	"extensions".intern()
    };

    int idx = 0;

    public boolean hasMoreElements() {
	return idx < list.length;
    }

    public Object nextElement() {
	if ( idx >= list.length )
	    throw new NoSuchElementException("SampleResourceIndexer enum");
	return list[idx++];
    }

    SampleIndexerEnumeration() {
	this.idx = 0;
    }

}

/**
 * A container for directories and templates.
 */

public class SampleResourceIndexer extends Resource
    implements ContainerInterface, ResourceIndexer
{
    private static final boolean debug = false;
    protected static final boolean extCaseSensitive = false;
    protected static final String  defname          = "*default*";
 
   private static final String harmfulNames[] = {
	"aux".intern(),
	"con".intern()
   };
    
    static public boolean isWinPlatform = (File.pathSeparatorChar == ';');
    /**
     * Attribute index - the super indexer, if any.
     */
    protected static int ATTR_SUPER_INDEXER = -1;

    /**
     * Attribute index - the super indexer, if any.
     */
    protected static int ATTR_NOT_INDEXED = -1;

    static {
	Attribute a = null;
	Class     c = 
	    org.w3c.tools.resources.indexer.SampleResourceIndexer.class;
	// Our super indexer:
	a = new StringAttribute("super-indexer"
				, null
				, Attribute.EDITABLE);
	ATTR_SUPER_INDEXER = AttributeRegistry.registerAttribute(c, a);
	a = new StringArrayAttribute("not-indexed-names"
				     , null
				     , Attribute.EDITABLE);
	ATTR_NOT_INDEXED = AttributeRegistry.registerAttribute(c, a);
    }

    protected ResourceReference directories = null;
    protected ResourceReference extensions  = null;
    protected ResourceReference contentTypes  = null;

    protected synchronized ResourceReference getDirectories() {
	if ( directories == null ) {
	    String diridxid = getIdentifier()+"-d";
	    directories = new DummyResourceReference(
				     new TemplateContainer(
					   new ResourceContext(getContext()), 
							   diridxid+".db"));
	}
	return directories;
    }

    protected synchronized ResourceReference getExtensions() {
	if ( extensions == null ) {
	    String extidxid = getIdentifier()+"-e";
	    extensions = new DummyResourceReference(
				    new TemplateContainer(
					  new ResourceContext(getContext()), 
							  extidxid+".db"));
	}
	return extensions;
    }

    public long lastModified() {
	return getLong(ATTR_LAST_MODIFIED, -1);
    }

    public String getSuperIndexer() {
	return getString(ATTR_SUPER_INDEXER, null);
    }

    public Enumeration enumerateResourceIdentifiers(boolean all) {
	return new SampleIndexerEnumeration();
    }

    public ResourceReference lookup(String name) {
	if ( name.equals("directories") ) {
	    return getDirectories();
	} else if ( name.equals("extensions") ) {
	    return getExtensions();
	}  
	return null;
    }

    /**
     * Delete this inexer
     * @exception org.w3c.tools.resources.MultipleLockException if someone
     * else has locked the indexer.
     */
    public synchronized void delete() 
	throws MultipleLockException
    {
	// Remove the two stores we are responsible for:
	DummyResourceReference rr = (DummyResourceReference) getExtensions();
	try {
	    Resource r = rr.lock();
	    r.delete();
	} catch (InvalidResourceException ex) {
	} finally {
	    rr.invalidate();
	    rr.unlock();
	}

	rr = (DummyResourceReference) getDirectories();
	try {
	    Resource r = rr.lock();
	    r.delete();
	} catch (InvalidResourceException ex) {
	} finally {
	    rr.invalidate();
	    rr.unlock();
	}

	super.delete();
    }

    public void delete(String name) {
	throw new RuntimeException("static container");
    }

    public void registerResource(String name,
				 Resource resource,
				 Hashtable defs) {
	throw new RuntimeException("static container");
    }

    /*
     * Load an extension descriptor.
     * @param ext The name of the extension.
     * @return An instance of Extension, or <strong>null</strong>.
     */

    public synchronized ResourceReference loadExtension (String name) {
	ResourceReference rr = getExtensions();
	ResourceReference ext = null;
	try {
	    TemplateContainer exts = (TemplateContainer) rr.lock();
	    // try with exact and if it fails, try to with lower case
	    ext = exts.lookup(name);
	    if (ext == null && !extCaseSensitive) 
		return exts.lookup(name.toLowerCase());
	    return ext;
	} catch (InvalidResourceException ex) {
	    String msg = ("[resource indexer]: extensions \""+
			  name+
			  "\" couldn't be restored ("+ex.getMessage()+")");
	    getContext().getServer().errlog(msg);
	    return null;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Return the class (if any) that our store defines for given extension.
     * @param ext The extension we want a class for.
     * @return A Class instance, or <strong>null</strong>.
     */

    protected ResourceReference getTemplateFor(String ext) {
	ResourceReference rr = loadExtension(ext) ;
	if (rr != null) {
	    try {
		Resource template = rr.lock();
		if (template != null) {
		    Resource check = new Resource();
		    if (template.getClass() == check.getClass())
			return null;
		    else return rr;
		}
		return null;
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	} 
	return null;
    }

    /**
     * Merge the attributes this extension defines, with the provided ones.
     * @param attrs The attributes we want to fill with default values.
     * @param ext The extension name.
     * @param into The already built set of default values.
     * @return A Hashtable, containing the augmented set of default attribute
     *    values.
     */

    protected Hashtable mergeDefaultAttributes(Resource template,
					       String ext,
					       Hashtable into) {
	Attribute    attrs[] = template.getAttributes();
	ResourceReference rr = loadExtension(ext) ;
	if (rr != null) {
	    try {
		Resource e       = rr.lock() ;
		if ( e != null ) {
		    for (int i = 0 ; i < attrs.length ; i++) {
			if ( ! template.definesAttribute(i) ) {
			    int idx = e.lookupAttribute(attrs[i].getName());
			    if ( idx >= 0 ) {
				Object value = e.getValue(idx, null);
				if ( value != null )
				    into.put(attrs[i].getName(), value) ;
			    }
			}
		    }
		}
		return into ;
	    } catch (InvalidResourceException ex) {
		return null;
	    } finally {
		rr.unlock();
	    }
	}
	return null;
    }

    /**
     * Merge the attributes this extension defines, with the provided ones.
     * @param origFrame The original frame
     * @param ext The extension name
     * @param into The ResourceReference of the frame to be merged
     */

    protected void mergeFrameAttributes(ResourceFrame origFrame, String ext,
					ResourceReference frameref) {
	int idx;
	Object oldval, newval;
	String atname;

	try {
	    Resource frame = frameref.lock();
	    Attribute attrs[] = frame.getAttributes();
	    for (int i = 0 ; i < attrs.length ; i++) {
		atname = attrs[i].getName();
		try {
		    oldval = origFrame.getValue(atname, null);
		    if (oldval == null) { // not defined, try to merge
			try {
			    origFrame.setValue(atname,
					       frame.getValue(atname, null));
			} catch (Exception ex) {
			    // undefined value, will stay null
			}
		    } else if (atname.equals("quality")) {
			// small hack here, quality factor are merged
			// should be removed by a quality per encoding
			Double d = (Double)frame.getValue(atname, null);
			if (d != null) {
			    d = new Double(d.doubleValue() *
					   ((Double) oldval).doubleValue());
			    origFrame.setValue(atname, d);
			}
		    }
		} catch (Exception undefined) {
		    // attribute is NOT defined
		}
	    }
	} catch (InvalidResourceException ex) {
	    ex.printStackTrace();
	} finally {
	    frameref.unlock();
	}
    }	    

    /**
     * Get this name's extensions.
     * @param name The file name.
     * @return An array of string, giving ach (parsed) extension, or
     *    <strong>null</strong> if none was found.
     */

    private final static String noextension[] = { "*noextension*" } ;

    protected String[] getFileExtensions(String name) {
	Vector items = new Vector() ;
	int dpos     = name.indexOf ('.') ;
	
	if ( dpos > 0 ) {
	    int pos = dpos+1 ;
	    while ( (dpos = name.indexOf ('.', pos)) != -1 ) {
		// Skip empty extension:
		if ( dpos == pos+1 ) { 
		    pos++ ;
		    continue ;
		}
		// Else add new extension:
		items.addElement (name.substring(pos, dpos)) ;
		pos = dpos + 1;
	    }
	    if ( pos < name.length() )
		items.addElement (name.substring(pos)) ;
	    String exts[] = new String[items.size()] ;
	    items.copyInto (exts) ;
	    return exts ;
	} else {
	    // That file has no extensions, we'll use '.' as its extension
	    return noextension;
	}
    }

    /**
     * Create a default file resource for this file (that exists).
     * @param directory The directory of the file.
     * @param name The name of the file.
     * @param defs A set of default attribute values.
     * @return An instance of Resource, or <strong>null</strong> if
     *    we were unable to create it.
     */

    protected Resource createFileResource(File directory,
					  RequestInterface req,
					  String name,
					  Hashtable defs) 
    {
	ResourceReference rr = null;
	FramedResource template = null;
	Resource       newres = null;
	Class proto = null;
	try {
	    proto = Class.forName("org.w3c.tools.resources.ProtocolFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    // fatal error!
	    return null;
	}
	// Check that at least one class is defined for all the extensions:
	String exts[] = getFileExtensions(name) ;
	if ( exts == null )
	    return null ;
	for (int i = exts.length-1 ; i >= 0 ; i--) {
	    rr = getTemplateFor(exts[i]) ;
	    if ( rr != null )
		break ;
	}
	if ( rr == null ) {
	    // Look for a default template:
	    if ((rr = loadExtension(defname)) == null)
		return null ;
	}
	// Create the runtime-time default values for attributes.
	if ( defs == null ) {
	    defs = new Hashtable(5) ;
	}
	String s_dir = "directory".intern();
	String s_ide = "identifier".intern();
	String s_fil = "filename".intern();
	String s_con = "context".intern();

	if ( defs.get(s_dir) == null ) {
	    defs.put(s_dir, directory) ;
	}
	if ( defs.get(s_ide) == null ) {
	    defs.put(s_ide, getIndexedFileName(name)) ;
	} else {
	    defs.put(s_ide, getIndexedFileName((String)defs.get(s_ide))) ;
	}
	if ( defs.get(s_fil) == null) {
	    defs.put(s_fil, name) ;
	}
	if ( defs.get(s_con) == null ) {
	    defs.put(s_con, getContext());
	}
	try {
	    template = (FramedResource) rr.lock();
	    if (exts != null) {
		// Merge with values defined by the extension:
		for (int i = exts.length ; --i >= 0 ; ) 
		    mergeDefaultAttributes(template, exts[i], defs) ;
	    }
	    // Create, initialize and return the new resouce
	    try {
		newres = (FramedResource) template.getClone(defs);
	    } catch (Exception ex) {
		ex.printStackTrace() ;
		return null ;
	    }
	} catch (InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    rr.unlock();
	}
		// clone has been done, merge frames now
	if (exts != null) {
	    ResourceFrame rf[] = newres.collectFrames(proto);
	    if (rf != null) {
		for (int j=0; j < rf.length; j++) {
		    for (int i = exts.length-1 ; i >= 0 ; i--) {
			rr = getTemplateFor(exts[i]) ;
			if ( rr != null ) {
			    FramedResource fr = null;
			    try {
				fr = (FramedResource) rr.lock();
				ResourceReference trr = null;
				trr = fr.getFrameReference(proto);
				if (trr != null) {
				    mergeFrameAttributes(rf[j], exts[i], trr);
				}
			    } catch (InvalidResourceException iex) {
				iex.printStackTrace();
				return null;
			    } finally {
				rr.unlock();
			    }  
			}
		    }
		}
	    }
	}
	return newres;
    }

    /**
     * Load a given directory template from the store.
     * @param name The name of the template to load.
     * @return An instance of ResourceReference, or <strong>null</strong>.
     */

    public synchronized ResourceReference loadDirectory(String name) {
	ResourceReference rr = getDirectories();
	try {
	    TemplateContainer dirs = (TemplateContainer) rr.lock();
	    return dirs.lookup(name);
	} catch (InvalidResourceException ex) {
	    // Emit an error message, and remove it !
	    String msg = ("[resource indexer]: directory template \""+
			  name + "\" couldn't be restored. It has "+
			  "been removed.");
	    getContext().getServer().errlog(msg);
	    return null;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Create a default container resource for this directory (that exists).
     * @param directory The parent directory.
     * @param req the request that triggered this creation
     * @param name The name of its sub-directory to index.
     * @param defaults A set of default atribute values.
     * @return A Resource  instance, or <strong>null</strong> if
     *    the indexer was unable to build a default resource for the directory.
     */

    protected Resource createDirectoryResource(File directory,
					       RequestInterface req,
					       String name,
					       Hashtable defs) 
    {
	// Lookup the directory path, for an existing template.
	File         dir      = new File(directory, name) ;
	Resource dirtempl = null;
	ResourceReference rr = null;
	
	rr = loadDirectory(name);
	// If no template available, default to a raw DirectoryResource
	if ((rr == null) && ((rr=loadDirectory(defname)) == null))
	    return null;
	try {
	    dirtempl = rr.lock();
	    // Clone the appropriate template:
	    if ( defs == null ) {
		defs = new Hashtable(3);
	    }
	    String s_dir = "directory".intern();
	    String s_ide = "identifier".intern();	    
	    if ( defs.get(s_dir) == null ) {
		defs.put(s_dir, directory) ;
	    }
	    if ( defs.get(s_ide) == null ) {
		defs.put(s_ide, getIndexedDirName(name)) ;
	    } else {
		defs.put(s_ide, getIndexedDirName((String)defs.get(s_ide))) ;
	    }
	    //FIXME context ???
	    //      if ( defs.get("context") == null )
	    //	defs.put("context", getContext());
	    try {
		return (Resource) dirtempl.getClone(defs);
	    } catch (Exception ex) {
		ex.printStackTrace() ;
		return null ;
	    }
	} catch (InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    rr.unlock();
	}
    }

    /**
     * Try to create a virtual resource if the real (physical) resource
     * is not there.
     * @param directory The directory the file is in.
     * @param req the request that triggered this creation 
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */

    protected Resource createVirtualResource( File directory,
					      RequestInterface req,
					      String name,
					      Hashtable defs) 
    {
	ResourceReference rr = null;
	Resource dirtempl = null;
	
	rr = loadDirectory(name);
	if (rr != null) {
	    try {
		dirtempl = rr.lock();
		String classname = dirtempl.getClass().getName().intern();
		String idr = 
		    "org.w3c.jigsaw.resources.DirectoryResource".intern();
		if (classname == idr) {
		    File file = new File(directory, name) ;
		    // check in this case that we will have a special
		    // configuration ONLY for a real directory
		    if (!file.exists()) {
			return null;
		    }
		    if (!file.isDirectory()) {
		        return null;
		    }
		}
		String ifr =
		    "org.w3c.tools.resources.FileResource".intern();  
		if (classname == ifr) {
		    File file = new File(directory, name) ;
		    // check that we won't override a bad resource type
		    if (!file.exists()) {
		        return null;
		    }
		    if (file.isDirectory()) {
			return null;
		    }
		}
		if ( defs == null ) {
		    defs = new Hashtable(4);
		}
		String s_dir = "directory".intern();
		String s_ide = "identifier".intern();
		String s_con = "context".intern();		
		if ( defs.get(s_dir) == null ) {
		    defs.put(s_dir, directory) ;
		}
		if ( defs.get(s_ide) == null ) {
		    defs.put(s_ide, name) ;
		}
		if ( defs.get(s_con) == null ) {
		    defs.put(s_con, getContext());
		}
		try {
		    return (Resource) dirtempl.getClone(defs);
		} catch (Exception ex) {
		    ex.printStackTrace() ;
		    return null ;
		}
	    } catch (InvalidResourceException ex) {
		ex.printStackTrace();
		return null;
	    } finally {
		rr.unlock();
	    }
	}
	return null;
    }

    /**
     * Try to create a resource for the given file.
     * This method makes its best efforts to try to build a default
     * resource out of a file. 
     * @param directory The directory the file is in.
     * @param name The name of the file.
     * @param defs Any default attribute values that should be provided
     *    to the created resource at initialization time.
     * @return A Resource instance, or <strong>null</strong> if the given
     *    file can't be truned into a resource given our configuration
     *    database.
     */

    public Resource createResource(ContainerResource container,
				   RequestInterface request,
				   File directory,
				   String name,
				   Hashtable defs) 
    {
	if (isWinPlatform) {
	    for (int i=0; i < harmfulNames.length; i++) {
		if (name.equalsIgnoreCase(harmfulNames[i])) {
		    return null;
		}
	    }
	}
	// if it matches the Not-Indexed list, then exit
	String[] removed = (String[]) getValue(ATTR_NOT_INDEXED, null);
	if (removed != null) {
	    for (int i=0; i < removed.length; i++) {
		if (name.equals(removed[i])) {
		    return null;
		}
	    }
	}
	// Does this file exists ?
	File file = new File(directory, name) ;
	Resource result = null;
	result = createVirtualResource(directory, request, name, defs);
	if (result != null) {
	    return result;
	}
	if (!file.exists()) {
	    return null;
	}
	// Okay, dispatch on wether it is a file or a directory.
	if ( file.isDirectory() ) {
	    result = createDirectoryResource(directory, request, name, defs) ;
	} else if ( file.isFile() ) {
	    result = createFileResource(directory, request, name, defs) ;
	} else {
	    // not a directory and not a real file, perhaps something not
	    // really wanted
	    return null;
	}
	if ( result != null )
	    return result;
	// Try the super indexer if available:
	String superIndexer = getSuperIndexer();
	if ( superIndexer == null )
	    return null;
	IndexerModule m = null;
	m = (IndexerModule) getContext().getModule(IndexerModule.NAME);
	ResourceReference rri = m.getIndexer(superIndexer);
	if (rri == null)
	    return null;
	try {
	    ResourceIndexer p = (ResourceIndexer)rri.lock();
	    return ((p != null) 
		    ? p.createResource(container, request, directory, 
				       name, defs)
		    : null);
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rri.unlock();
	}
    }

    /**
     * Get the name of the resource relative to the given filename.
     * @param name The name of the file.
     * @return a String, the resource name.
     */
    public String getIndexedName(File directory, String name) {
	if (isWinPlatform) {
	    for (int i=0; i < harmfulNames.length; i++) {
		if (name.equalsIgnoreCase(harmfulNames[i])) {
		    return null;
		}
	    }
	}	
	File file = new File(directory, name);
	if (! file.exists())
	    return null;
	if (file.isDirectory())
	    return getIndexedDirName(name);
	//make sure that we will index this file (or directory)
	String            exts[] = getFileExtensions(name) ;
	ResourceReference rr     = null;
	if ( exts == null )
	    return null;
	for (int i = exts.length-1 ; i >= 0 ; i--) {
	    rr = getTemplateFor(exts[i]) ;
	    if ( rr != null )
		break ;
	}
	if (rr != null)
	    return getIndexedFileName(name);
	//try the super indexer
	String superIndexer = getSuperIndexer();
	if ( superIndexer == null )
	    return null;
	IndexerModule m = null;
	m = (IndexerModule) getContext().getModule(IndexerModule.NAME);
	ResourceReference rri = m.getIndexer(superIndexer);
	if (rri == null)
	    return null;
	try {
	    ResourceIndexer p = (ResourceIndexer)rri.lock();
	    return ((p != null) 
		    ? p.getIndexedName(directory, name)
		    : null);
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rri.unlock();
	}
    }

    protected String getIndexedFileName(String name) {
	return name;
    }

    protected String getIndexedDirName(String name) {
	return name;
    }

   
    // FIXME tests
    public SampleResourceIndexer(ResourceContext ctxt) {
	Hashtable init = new Hashtable(3);
	String s_ide = "identifier".intern();
	String s_con = "context".intern();	
	init.put(s_con, ctxt);
	init.put(s_ide, "default");
	initialize(init);
    }

    public SampleResourceIndexer() {
	super();
    }
}
