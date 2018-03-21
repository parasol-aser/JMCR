// PutListResource.java
// $Id: PutListResource.java,v 1.2 2010/06/15 17:53:04 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import java.net.URL;

import org.w3c.util.IO;
import org.w3c.util.ObservableProperties;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.tools.resources.serialization.Serializer;
import org.w3c.tools.resources.ProtocolException;

import org.w3c.cvs2.CVS;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;
import org.w3c.cvs2.UncheckedOutException;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HttpRequestMessage;

public class PutListResource extends FramedResource {

    protected static final boolean debug = true;

    /**
     * status: File published
     */
    public static final int FILE_PB = 1;

    /**
     * status: File unchanged
     */
    public static final int FILE_UC = 2;

    /**
     * status: File merged
     */
    public static final int FILE_MG = 3;

    /**
     * status: conflict
     */
    public static final int FILE_CF = 4;

    /**
     * status: deleted
     */
    public static final int FILE_DEL = 5;

    /**
     * Attribute index - The file used to store the modification list.
     */
    protected static int ATTR_FILE = -1;
    /**
     * Attribute index - The user's local space.
     */
    protected static int ATTR_SPACE = -1;
    /**
     * Attribute index - The web server public space.
     */
    protected static int ATTR_ROOT = -1;
    /**
     * Attribute index - The auto publish flag
     */    
    protected static int ATTR_AUTO_PUBLISH = -1;
    /**
     * Attribute index - The auto delete flag
     */    
    protected static int ATTR_AUTO_DELETE = -1;
    /**
     * Attribute index - The max number of published entries stored
     */
    protected static int ATTR_MAX_PUBLISHED = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigedit.filters.PutListResource");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the file attribute:
	a = new FileAttribute("file"
			      , null
			      , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_FILE = AttributeRegistry.registerAttribute(c, a);
	// Register the space attribute:
	a = new FileAttribute("space"
			      , null
			      , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_SPACE = AttributeRegistry.registerAttribute(c, a);
	// Register the server root:
	a = new FileAttribute("root"
			      , null
			      , Attribute.EDITABLE| Attribute.MANDATORY);
	ATTR_ROOT = AttributeRegistry.registerAttribute(c, a);
	// Register the auto publish flag
	a = new BooleanAttribute("auto-publish",
				 Boolean.FALSE,
				 Attribute.EDITABLE);
	ATTR_AUTO_PUBLISH = AttributeRegistry.registerAttribute(c, a);
	// Register the auto delete flag
	a = new BooleanAttribute("auto-delete",
				 Boolean.FALSE,
				 Attribute.EDITABLE);
	ATTR_AUTO_DELETE = AttributeRegistry.registerAttribute(c, a);
	// Register the max number of published entries stored
	a = new IntegerAttribute("max-published",
				 new Integer(10),
				 Attribute.EDITABLE);
	ATTR_MAX_PUBLISHED = AttributeRegistry.registerAttribute(c, a);
    }

    protected static Serializer serializer = null;

    static {
	serializer = 
	    new org.w3c.tools.resources.serialization.xml.XMLSerializer();
    }

    /**
     * Get our auto publish flag.
     * @return a boolean.
     */
    public boolean getAutoPublishFlag() {
	return getBoolean(ATTR_AUTO_PUBLISH, false);
    }

    /**
     * Enable or disable the auto publish feature.
     * @param onoff if onoff is true auto publish is enable.
     */
    protected void setAutoPublish(boolean onoff) {
	setValue(ATTR_AUTO_PUBLISH, new Boolean(onoff));
    }

    /**
     * Get our auto delete flag.
     * @return a boolean.
     */
    public boolean getAutoDeleteFlag() {
	return getBoolean(ATTR_AUTO_DELETE, false);
    }

    /**
     * Enable or disable the auto delete feature.
     * @param onoff if onoff is true auto publish is enable.
     */
    protected void setAutoDelete(boolean onoff) {
	setValue(ATTR_AUTO_DELETE, new Boolean(onoff));
    }

    /**
     * Get the max number of published entries stored in the putlist.
     * @return an int.
     */
    public int getMaxPublishedEntryStored() {
	return getInt(ATTR_MAX_PUBLISHED, 10);
    }

    /**
     * Set the max number of published entries stored in the putlist.
     * @param max This number.
     */
    protected void setMaxPublishedEntryStored(int max) {
	setValue(ATTR_MAX_PUBLISHED, new Integer(max));
    }

    /**
     * Known entries.
     */
    private Hashtable entries = null;

    /**
     * Known "todelete" entries.
     */
    private Hashtable dentries = null;

    /**
     * Published
     */
    private Hashtable published = null;

    /**
     * Our server context properties.
     */
    ObservableProperties props = null;

    /**
     * Get the modified entries.
     * @return an enumeration of PutedEntry
     * @see PutedEntry
     */
    protected Enumeration getEntries() {
	return entries.elements();
    }

    /**
     * Get the modified entries keys
     * @return an enumeration of String
     */
    protected Enumeration getEntriesKeys() {
	return entries.keys();
    }

    /**
     * Get the modified entry relative to the given key.
     * @param key The key relative to the PutedEntry
     * @return a PutedEntry
     * @see PutedEntry
     */
    protected PutedEntry getEntry(String key) {
	return (PutedEntry) entries.get(key);
    }

    /**
     * Add an entry into the putlist
     * @param e the entry to add
     */
    protected void addEntry(PutedEntry e) {
	entries.put(e.getKey(), e);
    }

    /**
     * Remove a modified entry from the putlist
     * @param key the key of the entry to remove
     */
    protected void removeEntry(String key) {
	entries.remove(key);
    }

    /**
     * Get the deleted entries.
     * @return an enumeration of DeletedEntry
     * @see DeletedEntry
     */
    protected Enumeration getDelEntries() {
	return dentries.elements();
    }

    /**
     * Get the deleted entries keys
     * @return an enumeration of String
     */
    protected Enumeration getDelEntriesKeys() {
	return dentries.keys();
    }

    /**
     * Get the deleted entry relative to the given key.
     * @param key The key relative to the DeletedEntry
     * @return a DeletedEntry
     * @see DeletedEntry
     */
    protected DeletedEntry getDelEntry(String key) {
	return (DeletedEntry) dentries.get(key);
    }

    /**
     * Add an entry into the putlist
     * @param e the entry to add
     */
    protected void addDelEntry(DeletedEntry e) {
	dentries.put(e.getKey(), e);
    }

    /**
     * Remove a deleted entry from the putlist
     * @param key the key of the entry to remove
     */
    protected void removeDelEntry(String key) {
	dentries.remove(key);
    }

    //---

    /**
     * Get the published entries.
     * @return an enumeration of PutedEntry
     * @see PutedEntry
     */
    protected Enumeration getPublishedEntries() {
	return published.elements();
    }

    /**
     * Remove the oldest published entry from the putlist.
     */
    protected void removeOldestPublishedEntry() {
	//Could be optimized, but it is significant?
	Enumeration penum = published.elements();
	PutedEntry oldest = null;
	PutedEntry current = null;

	if (!penum.hasMoreElements()) {
	    return;
	}
	oldest = (PutedEntry) penum.nextElement();
	while (penum.hasMoreElements()) {
	    current = (PutedEntry) penum.nextElement();
	    if (current.getTime() < oldest.getTime())
		oldest = current;
	}
	published.remove(oldest.getKey());
    }

    /**
     * Add a published entry into the putlist.
     * @param e The published entry to add.
     * @see PutedEntry
     */
    protected synchronized void addPubEntry(PutedEntry e) {
	while (published.size() >= getMaxPublishedEntryStored())
	    removeOldestPublishedEntry();
	published.put(e.getKey(), e);
    }

    /**
     * Remove a published entry from the putlist
     * @param key the key of the entry to remove
     */
    protected synchronized void removePubEntry(String key) {
	published.remove(key);
    }

    /**
     * Compute the path of the public file for the given local file.
     * This method uses the <em>space</em> and <em>root</em> attributes
     * to translate the path of the given file from the user's local space
     * to the public (server) space.
     * @return A File instance, or <strong>null</strong>.
     */
    protected File getServerFile(File file) {
	String fpath  = file.getAbsolutePath();
	String fspace = getCvsSpace().getAbsolutePath();
	if ( ! fpath.startsWith(fspace) )
	    return null;
	return new File(getRoot(), fpath.substring(fspace.length()));
    }

    /**
     * Get the file to use to store the edited list of files.
     * @return The file.
     */
    public File getFile() {
	return (File) getValue(ATTR_FILE, null);
    }

    /**
     * Get the file to use to store the edited list of published files.
     * @return The file.
     */
    public File getPubFile() {
        File file = getFile();
	if (file != null)
  	    return new File(file+".pub");
	else
	  return null;
    }

    /**
     * Get the file to use to store the edited list of deleted files.
     * @return The file.
     */
    public File getDelFile() {
        File file = getFile();
	if (file != null)
  	    return new File(file+".del");
	else
	  return null;
    }

    /**
     * Get the root directory of the public server to update.
     * @return The root directory of the public server space, supposed to
     * be controled by CVS.
     */
    public File getRoot() {
	return (File) getValue(ATTR_ROOT, null);
    }

    /**
     * Get this user's local CVS space root directory.
     * @return The usre's root of the CVS local space, assumed to be 
     * under CVS control.
     */
    public File getCvsSpace() {
	return (File) getValue(ATTR_SPACE, null);
    }

    protected synchronized void write(File file, Enumeration genum) {
	if (file == null)
	    return;
	File backup = null;
	// Save old version if available:
	if ( file.exists() ) {
	    backup = new File(file+".bak");
	    if ( backup.exists() )
		backup.delete();
	    file.renameTo(backup);
	}
	try {
	    Vector v = new Vector(10);
	    while (genum.hasMoreElements()) {
		v.addElement(genum.nextElement());
	    }
	    AttributeHolder holders[] = new AttributeHolder[v.size()];
	    v.copyInto(holders);
	    Writer writer = new BufferedWriter(new FileWriter(file));
	    serializer.writeResources(holders, writer);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    if ( backup != null )
		backup.renameTo(file);
	}
    }

    /**
     * Dump the current list of edited files back to disk.
     */
    protected synchronized void writeList() {
	write(getFile(), entries.elements());
    }

    /**
     * Dump the current list of published files back to disk.
     */
    protected synchronized void writePubList() {
	write(getPubFile(), published.elements());
    }

    /**
     * Dump the current list of [ublished files back to disk.
     */
    protected synchronized void writeDelList() {
	write(getDelFile(), getDelEntries());
    }

    protected synchronized void removeUnconfirmedDelEntries() {
	Enumeration denum = getDelEntries();
	while (denum.hasMoreElements()) {
	    DeletedEntry e = (DeletedEntry) denum.nextElement();
	    if (! e.isConfirmed()) {
		removeDelEntry(e.getKey());
	    }
	}
    }

    /**
     * Restore the list from the file.
     */
    protected synchronized void readList() {
	File file = getFile();
	try {
	    Reader reader = new BufferedReader(new FileReader(file));
	    AttributeHolder holders[] =
		serializer.readAttributeHolders(reader);
	    for (int i = 0 ; i < holders.length ; i++)
		addEntry((PutedEntry)holders[i]);
	} catch (Exception ex) {
	    // FIXME
	    ex.printStackTrace();
	}
    }

    /**
     * Restore the published list from the file.
     */
    protected synchronized void readPubList() {
	File file = getPubFile();
	try {
	    Reader reader = new BufferedReader(new FileReader(file));
	    AttributeHolder holders[] =
		serializer.readAttributeHolders(reader);
	    for (int i = 0 ; i < holders.length ; i++)
		addPubEntry((PutedEntry)holders[i]);
	} catch (Exception ex) {
	    // FIXME
	    ex.printStackTrace();
	}
    }

    /**
     * Restore the deleted list from the file.
     */
    protected synchronized void readDelList() {
	File file = getDelFile();
	try {
	    Reader reader = new BufferedReader(new FileReader(file));
	    AttributeHolder holders[] =
		serializer.readAttributeHolders(reader);
	    for (int i = 0 ; i < holders.length ; i++) {
		DeletedEntry e = (DeletedEntry) holders[i];
		e.confirm();
		addDelEntry(e);
	    }
	} catch (Exception ex) {
	    // FIXME
	    ex.printStackTrace();
	}
    }

    protected PutedEntry lookupEntry(Request request) {
	ResourceReference rr = request.getTargetResource();
	String            k  = request.getURL().toExternalForm();
	Resource          r  = null;
	if (rr != null) {
	  try {
	    r = rr.lock();
	    if ( r instanceof FileResource )
	      k = ((FileResource) r).getFile().getAbsolutePath().toString();
	  } catch (InvalidResourceException ex) {
	    // continue
	  } finally {
	    rr.unlock();
	  }
	}
	return (PutedEntry) entries.get(k);
    }

    protected DeletedEntry lookupDelEntry(Request request) {
	ResourceReference rr = request.getTargetResource();
	String            k  = request.getURL().toExternalForm();
	Resource          r  = null;
	if (rr != null) {
	  try {
	    r = rr.lock();
	    if ( r instanceof FileResource )
	      k = ((FileResource) r).getFile().getAbsolutePath().toString();
	  } catch (InvalidResourceException ex) {
	    // continue
	  } finally {
	    rr.unlock();
	  }
	}
	return (DeletedEntry) dentries.get(k);
    }

    /**
     * Register the given request, which must has a PUT method.
     * @param file The modified file.
     */
    public synchronized int registerRequest(Request request) {
	PutedEntry e = lookupEntry(request);
	if ( e == null ) {
	    e = PutedEntry.makeEntry(request);
	    addEntry(e);
	} else {
	    e.update(request);
	} 
	if (getAutoPublishFlag()) {
	    return publish(e);
	} else {
	    return FILE_UC;
	}
    }

    public synchronized void registerDeleteRequest(Request request) {
	DeletedEntry e = (DeletedEntry) DeletedEntry.makeEntry(request);
	addDelEntry(e);
    }

    public synchronized int confirmDelete(Request request) {
	DeletedEntry e = lookupDelEntry(request);
	if (e != null) {
	    e.confirm();
	    if (getAutoDeleteFlag())
		return delete(e);
	}
	return FILE_UC;
    }

    /**
     * Delete the file relative to the given entry.
     * @param entry The DeletedEntry.
     * @return FILE_UC, FILE_DEL
     */
    protected int delete(DeletedEntry de) {
	File file  = new File(de.getFilename());
	File sfile = getServerFile(file);
	if (sfile.exists()) {
	    if (debug)
		System.out.println("Deleting : "+sfile);
	    sfile.delete();
	    removeDelEntry(de.getKey());
	    return FILE_DEL;
	} else {
	    if (debug)
		System.out.println("Nothing to delete : "+sfile);
	    removeDelEntry(de.getKey());
	    return FILE_UC;
	}
    }

    /**
     * Publish the file relative to the given entry.
     * @param entry The PutedEntry.
     * @return FILE_UC, FILE_CF, FILE_PB, FILE_MG
     */
    protected int publish(PutedEntry pe) {
	File file  = new File(pe.getFilename());
	File sfile = getServerFile(file); 
	int status = FILE_UC;

	try {
	    // First step: does the private version needs commit ?
	    File         d  = new File(file.getParent());
	    CvsDirectory c  = CvsDirectory.getManager(d, props);
	    if ( c.status(file.getName()) == CVS.FILE_M ) {
		String author = pe.getAuthor();
		String env [] = { "USER="+author , 
				  "LOGNAME="+author };
		String msg    = ((author != null)
				 ? "Published by "+author+" through Jigsaw"
				 : "Published through Jigsaw");
		c.commit(file.getName(), msg, env);
	    } else if ( debug ) {
		System.out.println("PutList: no commit needed on "+
				   file.getAbsolutePath()+
				   " st="+c.status(file.getName()));
	    }
	    // Second step: publish
	    File sd = new File(sfile.getParent());
	    try {
		CvsDirectory sc = 
		    CvsDirectory.getManager(sd, props);
		String filename = sfile.getName();
		int cvs_status = sc.status(filename);
		if (debug) {
		    System.out.println("publishing "+
				       CvsDirectory.statusToString(cvs_status)+
				       " file : "+filename);
		}
		if (cvs_status == CVS.FILE_C) {
		    //conflict! we try to merge
		    //create a backup file
		    File backup = new File(sfile.getParent(), filename+".bak");
		    try {
			org.w3c.util.IO.copy(sfile, backup);
			//try to merge
			sc.update(filename);
			cvs_status = sc.status(filename);
			if (cvs_status == CVS.FILE_M) {
			    //merge done, so commit.
			    String author = pe.getAuthor();
			    String env [] = { "USER="+author , 
					      "LOGNAME="+author };
			    String msg    = ((author != null)
					     ? "Merged by "+author+
					     " through Jigsaw"
					     : "Merged through Jigsaw");
			    sc.commit(filename, msg, env);
			    //done so delete backup file
			    backup.delete();
			    status = FILE_MG;
			} else if (cvs_status == CVS.FILE_C) {
			    //merge failed
			    sfile.delete();
			    backup.renameTo(sfile);
			    status = FILE_CF;
			}
		    } catch (IOException ex) {
			ex.printStackTrace();
			status = FILE_CF;
		    }
		} else if (cvs_status != CVS.FILE_OK) {
		    sc.update(filename);
		    status = FILE_PB;
		} else if ( debug ) {
		    System.out.println("PutList: no update needed on "+
				      sfile.getAbsolutePath()+
				      " st="+
				      CvsDirectory.statusToString(cvs_status));
		}
	    } catch (UncheckedOutException ex) {
		// perform a get from root
		File root = new File(getRoot().getAbsolutePath());
		CvsDirectory sc = CvsDirectory.getManager(root, props);
		String fpath  = file.getAbsolutePath();
		String fspace = getCvsSpace().getAbsolutePath();
		String path   = fpath.substring(fspace.length()+1);
		sc.get(path);
		status = FILE_PB;
	    }
	    // Last step: remove published entries:
	    entries.remove(pe.getKey());
	    // publication time
	    pe.setValue(PutedEntry.ATTR_TIME, 
			new Long(System.currentTimeMillis()));
	    addPubEntry(pe);
	} catch (CvsException ex) {
	    ex.printStackTrace();
	}
	return status;
    }

    public synchronized void notifyUnload() {
	writeList();
	writePubList();
	writeDelList();
	super.notifyUnload();
    }

    public void initialize(Object values[]) {
	super.initialize(values);
	// Prepare empty entry list:
	File file = getFile();
	if ((file != null) && file.exists())
	    readList();
	File pub = getPubFile();
	if ((pub != null) && pub.exists())
	    readPubList();
	File del = getDelFile();
	if ((del != null) && del.exists())
	    readDelList();
	// Get the server properties:
	this.props = getServer().getProperties();
	try {
	  registerFrameIfNone("org.w3c.jigedit.filters.PutListFrame",
			      "putlist-frame");
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
    }

    public PutListResource() {
	super();
	this.entries = new Hashtable(11);
	this.dentries = new Hashtable(11);
	this.published = new Hashtable(11);
    }

}
