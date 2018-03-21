// CvsDirectory.java
// $Id: CvsDirectory.java,v 1.1 2010/06/15 12:28:49 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.util.AsyncLRUList;
import org.w3c.util.LRUAble;
import org.w3c.util.LRUList;

//
// FIXME add extra environment parameter to all public methods
// witch run cvs.
//

public class CvsDirectory implements LRUAble, CVS {
    /**
     * Property giving the path of the cvs binary.
     * This property should be set to the absolute path to the cvs command
     * in your local environment.
     * <p>This property defaults to <code>/usr/local/bin/cvs</code>.
     */
    public static final String CVSPATH_P = "org.w3c.cvs.path" ;
    /**
     * Property giving your CVS repository.
     * This property should be set to the absolute path of your repository.
     * <p>This property defaults to <code>/afs/w3.org/pub/WWW</code>.
     */
    public static final String CVSROOT_P = "org.w3c.cvs.root" ;
    /**
     * Property giving the path of the cvswrapper.
     * Because CVS can't run without being in the right directory, this
     * classes use a shell script wrapper to issue cvs commands, that will
     * change directory appropriately.
     * <p>You should have gotten this wrapper in the distribution 
     * <code>bin</code> directory.
     * <p>This property defaults to 
     * <code>/afs/w3.org/usr/abaird/Jigsaw/bin/cvs_wrapper</code>.
     */
    public final static String CVSWRAP_P = "org.w3c.cvs.wrapper" ;

    /**
     * The default CVS path.
     */
    public final static String cvspath_def 
        = "/usr/local/bin/cvs" ;
    /**
     * The default CVS root path.
     */
    public final static String cvsroot_def 
        = "/afs/w3.org/CVS-Repository";
    /**
     * The default CVS wrapper path.
     */
    public final static String cvswrap_def
        = "/afs/w3.org/usr/abaird/Jigsaw/bin/cvs_wrapper";

    /**
     * Our cache of existing CVS managers.
     * Maps absolute directory names to appropriate CvsDirectory instance.
     */
    protected static Hashtable cache = new Hashtable(23);
    /**
     * All CVS entries are LRU maintained too.
     */
    protected static LRUList lru = new AsyncLRUList();
    /**
     * Our recommended cache size.
     */
    protected static int cachesize = 32;

    /**
     * LRU management - previous entry.
     */
    protected LRUAble prev = null;
    /**
     * LRU management - next entry.
     */
    protected LRUAble next = null;
    /**
     * The time at which we last checked the cvs status of that directory.
     */
    protected long cvscheck_stamp = -1;
    /**
     * The time at which we last examined the repository for this directory.
     */
    protected long cvsrep_stamp = -1;
    /**
     * Has this directory manager been cleaned up (removed from cache).
     */
    protected boolean clean = true;
    /**
     * The directory we manage.
     */
    protected File directory = null;
    /**
     * The corresponding repository directory (when available).
     */
    protected File repdir = null;
    /**
     * Known CVS entries (files)
     */
    protected Hashtable entries = null;
    /**
     * Our associated CvsRunner.
     */
    protected CvsRunner runner = null;
    /**
     * The properties we use for initialization.
     */
    public Properties props = null;

    String cvspath      = cvspath_def ;
    String cvsroot      = cvsroot_def ;
    String cvswrapper[] = { cvswrap_def } ;

   
    /**
     * This one is the third copy of the same piece of code (!)
     * Parse the given prop value into an array of | separated components.
     * @param propval The property value (may be <strong>null</strong>).
     * @param def The default value (if undefined).
     * @return A String array, or <strong>null</strong>.
     */

    private static String[] parseArrayProperty(String propval) {
	if ( propval == null )
	    return null;
	// Parse the property value:
	StringTokenizer st    = new StringTokenizer(propval, "|");
	int             len   = st.countTokens();
	String          ret[] = new String[len];
	for (int i = 0 ; i < ret.length ; i++) {
	    ret[i] = st.nextToken();
	}
	return ret;
    }

    File computeRepositoryDirectory(File dir) {
	File rep = new File(new File(dir, "CVS"), "Repository");
	File ret = null;
	if ( ! rep.exists() )
	    return null;
	try {
	    DataInputStream in = new DataInputStream(new FileInputStream(rep));
	    String          nm = in.readLine();
	    if ( nm.startsWith("/") )
		ret = new File(nm);
	    else
		ret = new File(cvsroot, nm);
	    in.close();
	} catch (Exception ex) {
	    return null;
	}
	return ret;
    }

    protected String[] getCvsWrapper() {
	return cvswrapper;
    }

    protected String[] getCvsDefaults() {
	String ret[] = new String[6];
	ret[0] = "-directory";
	ret[1] = getDirectory().getAbsolutePath();
	ret[2] = cvspath;
	ret[3] = "-q";
	ret[4] = "-d";
	ret[5] = cvsroot;
	return ret;
    }

    protected synchronized void createFileEntry(long timestamp
						, String name
						, int status) {
	if ( entries == null )
	    entries = new Hashtable(13);
	CvsEntry entry = new CvsEntry(this, timestamp, name, false, status);
	entries.put(name, entry);
    }

    protected synchronized void createDirectoryEntry(long timestamp
						     , String name
						     , int status) {
	if ( entries == null ) 
	    entries = new Hashtable(13);
	CvsEntry entry = new CvsEntry(this, timestamp, name, true, status);
	entries.put(name, entry);
    }

    public static Properties defprops = null;

    /**
     * Get a CvsDirectory.
     * @param directory The CVS directory.
     * @param props The cvs properties.
     * @param cvspath The absolute path of the cvs program.
     * @param cvsroot The absolute path of the CVS repository. 
     * @param cvswrap The absolute path of the cvs wrapper program
     * @return A CvsDirectory instance.
     * @exception CvsException If some initialisation failed
     */
    public static synchronized CvsDirectory getManager(File directory,
						       Properties props,
						       String cvspath,
						       String cvsroot,
						       String cvswrap[])
	throws CvsException
    {
	// Initialize default properties if not done yet:
	if ( defprops == null )
	    defprops = System.getProperties();
	String abspath = directory.getAbsolutePath();
	// Check the cache first:
	CvsDirectory cvs = (CvsDirectory) cache.get(abspath);
	if ( cvs != null ) {
	    cvs.cacheLoaded();
	    return cvs;
	}
	// Create a new cache entry for that directory and add to cache:
	cvs = new CvsDirectory(directory
			       , ((props == null) ? defprops : props)
			       , cvspath
			       , cvsroot
			       , cvswrap);
	if ( cache.size() >= cachesize )
	    cacheUnload();
	cache.put(abspath, cvs);
	lru.toHead(cvs);
	return cvs;
    }

    /**
     * Get a CvsDirectory.
     * @param directory The CVS directory
     * @return A CvsDirectory instance.
     * @exception CvsException If some initialisation failed
     */
    public static CvsDirectory getManager(File directory) 
	throws CvsException
    {
	return getManager(directory, null, null, null, null);
    }

    /**
     * @param directory The CVS directory.
     * @param props The cvs properties.
     * @return A CvsDirectory instance.
     * @exception CvsException If some initialisation failed
     */
    public static CvsDirectory getManager(File directory , Properties props) 
	throws CvsException
    {
	return getManager(directory, props, null, null, null);
    }

    /**
     * @param father The father CvsDirectory.
     * @param directory The CVS directory.
     * @return A CvsDirectory instance.
     * @exception CvsException If some initialisation failed
     */
    protected static CvsDirectory getManager(CvsDirectory father, File dir) 
	throws CvsException
    {
	return getManager(dir, father.props, null, null, null);
    }

    /**
     * LRU management - Get next node.
     * @return A CvsDirectory instance.
     */

    public LRUAble getNext() {
	return next;
    }

    /**
     * LRU management - Get previous node.
     * @return A CvsDirectory instance.
     */

    public LRUAble getPrev() {
	return prev;
    }

    /**
     * LRU management - Set next node.
     * @return A CvsDirectory instance.
     */

    public void setNext(LRUAble next) {
	this.next = next;
    }

    /**
     * LRU management - Set previous node.
     * @return A CvsDirectory instance.
     */

    public void setPrev(LRUAble prev) {
	this.prev = prev;
    }

    public static String statusToString(int st) {
	return (((st > 0) && (st < status.length))
		? status[st]
		: "unknown");
    }

    /**
     * This directory manager is being fetched from the cache.
     * Perform any action before we return it back to the user.
     * @exception CvsException If some CVS action fails during 
     * reinitialization.
     */

    protected void cacheLoaded() 
	throws CvsException
    {
	lru.toHead(this);
    }

    /**
     * Remove a directory manager from the cache.
     * Clear al references to other objects in order to free up memory
     * even if the caller maintains a pointer to the manager.
     */

    protected static synchronized void cacheUnload() {
	// Pick the manager to remove:
	CvsDirectory cvs = (CvsDirectory) lru.removeTail();
	// Clean it up:
	if ( cvs != null ) {
	    cvs.entries        = null;
	    cvs.clean          = true;
	    cvs.cvsrep_stamp   = -1;
	    cvs.cvscheck_stamp = -1;
	    cache.remove(cvs.getDirectory().getAbsolutePath());
	}
    }

    /**
     * Look for a file entry.
     * @param filename The name of the entry to look for.
     */

    protected CvsEntry getFileEntry(String filename) {
	return (entries != null) ? (CvsEntry) entries.get(filename) : null ;
    }

    protected void removeFileEntry(String filename) {
	if (entries != null) entries.remove(filename);
    }

    /**
     * Look for a sub-directory entry.
     * @param filename The name of the entry to look for.
     */

    protected CvsEntry getDirectoryEntry(String filename) {
	return (entries != null) ? (CvsEntry) entries.get(filename) : null ;
    }

    /**
     * Refresh the file entries for that directory.
     * @exception CvsException If the underlying CVS command failed.
     */

    public void refresh() 
	throws CvsException
    {
	synchronized(this) {
	    LoadUpdateHandler handler = new LoadUpdateHandler(this);
	    CvsStatusHandler  statush = new CvsStatusHandler(this);
	    try {
		entries = null; //FIXME other effect?
		runner.cvsLoad(this, handler, statush);
	    } catch (CvsException ex) {
		clean   = true;
		entries = null;
		throw ex;
	    }
	    clean = false;
	    cvscheck_stamp = getDirectory().lastModified();
	    handler.notifyEnd();
	    //must be called after UpdateHandler
	    statush.notifyEnd();
	}
	lru.toHead(this);
    }

    /**
     * Refresh the file entries for that filename.
     * @exception CvsException If the underlying CVS command failed.
     */
    protected void refresh(String filename) 
	throws CvsException
    {
	synchronized(this) {
	    LoadUpdateHandler handler = new LoadUpdateHandler(this);
	    CvsStatusHandler  statush = new CvsStatusHandler(this);

	    runner.cvsLoad(this, filename, handler, statush);

	    handler.notifyEnd();
	    //must be called after UpdateHandler
	    statush.notifyEnd();
	}
	lru.toHead(this);
    }

    /**
     * Refresh the file entry status for that filename.
     * @exception CvsException If the underlying CVS command failed.
     */
    protected void refreshStatus(String filename) 
	throws CvsException
    {
	synchronized(this) {
	    LoadUpdateHandler handler = new LoadUpdateHandler(this);
	    runner.cvsLoad(this, filename, handler);
	    handler.notifyEnd();
	}
	lru.toHead(this);
    }

    /**
     * Refresh the file entry revision number for that filename.
     * @exception CvsException If the underlying CVS command failed.
     */
    protected void refreshRevision(String filename) 
	throws CvsException
    {
	synchronized(this) {
	    CvsStatusHandler statush = new CvsStatusHandler(this);
	    runner.cvsStatus(this, filename, statush);
	    statush.notifyEnd();
	}
    }
	
    /**
     * This directory manager is about to be used, check it.
     * @exception CvsException If some CVS error occurs in that process.
     */

    protected synchronized void checkUse()
	throws CvsException
    {
	if ( clean ) {
	    // This has been cleaned up some times ago, restore:
	    if ( cache.size() >= cachesize )
		cacheUnload();
	    cacheLoaded();
	    cache.put(getDirectory().getAbsolutePath(), this);
	    clean = false;
	} 
	// Check if update is needed:
	if ( needsUpdate() ) 
	    refresh();
    }

    /**
     * That file in the directory is about to be used, check its status.
     * @param filename The name of the entry that is about to be used.
     * @exception CvsException If a CVS error occurs.
     */

    protected synchronized void checkUse(String filename)
	throws CvsException
    {
	CvsEntry entry = getFileEntry(filename);
	if ((entry == null) || entry.needsUpdate())
	    refresh();
    }

    /**
     * This directory manager is about to be used for sub-directories.
     * @exception CvsException If a CVS error occurs.
     */
    protected synchronized void checkDirectoryUse()
	throws CvsException
    {
	//test new thing
	//runner.cvsUpdateDirectories(this, null);

	// Can we gain access to the repository ?
	if (repdir == null) {
	    if ((repdir = computeRepositoryDirectory(getDirectory())) == null)
		throw new CvsException("Repository not accessible.");
	}
	// Are we uptodate against repository ?
	if ( cvsrep_stamp < repdir.lastModified() ) {
	    long stamp = System.currentTimeMillis();
	    // Get all subdirs from repository:
	    String subdirs[] = repdir.list(new DirectoryFilter());
	    if ( subdirs != null ) {
		for (int i = 0 ; i < subdirs.length ; i++) {
		    File subdir = new File(getDirectory(), subdirs[i]);
		    if ( subdir.exists() )
			createDirectoryEntry(stamp, subdirs[i], DIR_CO);
		    else
			createDirectoryEntry(stamp, subdirs[i], DIR_NCO);
		}
	    }
	    // Check against all local sub-directories:
	    subdirs = getDirectory().list(new DirectoryFilter());
	    if ( subdirs != null ) {
		for (int i = 0 ; i < subdirs.length ; i++) {
		    if (getDirectoryEntry(subdirs[i]) == null)
			createDirectoryEntry(stamp, subdirs[i], DIR_Q);
		}
	    }
	    cvsrep_stamp = stamp;
	}
    }

    /**
     * This directory manager is about to be used for the given sub-directory.
     * (Really really faster than checkDirectoryUse())
     * @exception CvsException If a CVS error occurs.
     */
    protected synchronized void checkDirectoryUse(String subdir) 
    	throws CvsException
    {
	// Can we gain access to the repository ?
	if (repdir == null) {
	    if ((repdir = computeRepositoryDirectory(getDirectory())) == null)
		throw new CvsException("Repository not accessible.");
	}
	// Are we uptodate against repository ?
	if ( cvsrep_stamp < repdir.lastModified() ) {
	    long stamp = System.currentTimeMillis();
	    File dir   = new File (repdir, subdir);
	    if (dir.exists()) {
		File subd = new File(getDirectory(), subdir);
		if ( subd.exists() )
		    createDirectoryEntry(stamp, subdir, DIR_CO);
		else
		    createDirectoryEntry(stamp, subdir, DIR_NCO);
	    } else {
		createDirectoryEntry(stamp, subdir, DIR_Q);
	    }
	}
    }

    /**
     * Does this directory needs some CVS update ?
     */

    protected boolean needsUpdate() {
	// Has the directory changed since we last visited it ?
	if (cvscheck_stamp < directory.lastModified()) {
	    return true;
	}
	// Has any entry changed since its last status ?
	if ( entries != null ) {
	    Enumeration e = entries.elements();
	    while ( e.hasMoreElements() ) {
		CvsEntry entry = (CvsEntry) e.nextElement();
		if ( entry.needsUpdate() ) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * List all available file entries in that directory.
     * This method will list all possible files:
     * <ul><li>The ones that exist but are not in the repository.
     * <li>The ones that are in the repository but not in local space.
     * <li>All the other ones.
     * </ul>
     * @return An enumeration listing zero or more String instances.
     * @exception CvsException If some CVS error occured while examining
     * the cvs status of entries.
     */

    public Enumeration listFiles()
	throws CvsException
    {
	lru.toHead(this);
	checkUse();
	return new FileEnumeration(entries);
    }

    /**
     * List available sub-directories of that directory.
     * This method will list of possible directories. If access to the
     * repository is permitted, it will look into that to get a list of
     * unchecked out directories, otherwise, it will just list the 
     * checked out ones.
     * @return An enumeration listing zero or more File instances.
     * @exception CvsException If some CVS error occured while examining
     * the cvs status of entries.
     */

    public Enumeration listDirectories() 
	throws CvsException
    {
	lru.toHead(this);
	checkDirectoryUse();
	return new DirectoryEnumeration(entries);
    }

    /**
     * Get the status of a file entry.
     * @param filename The file whose status is to be fetched.
     * @param refresh Should we refresh the status?
     * @return A integer status indicating the CVS status of that file within
     * the repository.
     * @exception CvsException If some CVS error occured while examining
     * the cvs status of entries.
     */
    public int status(String filename, boolean refresh) 
	throws CvsException
    {
	if (refresh)
	    refreshStatus(filename);
	return status(filename);
    }

    /**
     * Get the status of a file entry.
     * @param filename The file whose status is to be fetched.
     * @return A integer status indicating the CVS status of that file within
     * the repository.
     * @exception CvsException If some CVS error occured while examining
     * the cvs status of entries.
     */
    public int status(String filename) 
	throws CvsException
    {
	lru.toHead(this);
	checkUse(filename);
	CvsEntry entry = 
	    (entries != null) ? (CvsEntry) entries.get(filename) : null;
	if ( entry == null ) {
	    //let's make a try with the filename in update
	    refresh(filename);
	    entry = 
		(entries != null) ? (CvsEntry) entries.get(filename) : null;
	    if ( entry == null )
		throw new CvsException(filename+": no such entry.");
	}
	return entry.getStatus();
    }

    /**
     * Get the revision number of the given file.
     * @param filename The File name
     * @return A String instance
     * @exception CvsException if some CVS errors occurs
     */
    public String revision(String filename)
	throws CvsException
    {
	lru.toHead(this);
	checkUse(filename);
	CvsEntry entry = 
	    (entries != null) ? (CvsEntry) entries.get(filename) : null;
	if ( entry == null ) {
	    //let's make a try with the filename in update
	    refresh(filename);
	    entry = 
		(entries != null) ? (CvsEntry) entries.get(filename) : null;
	    if ( entry == null )
		throw new CvsException(filename+": no such entry.");
	} else if (( entry.getRevision() == null) && 
		   (entry.getStatus() != FILE_Q)) {
	    refreshRevision(filename);
	}
	return entry.getRevision();
    }

    /**
     * Get the Sticky Options of the given file.
     * @param filename The File name
     * @return A String instance
     * @exception CvsException if some CVS errors occurs
     */
    public String stickyOptions(String filename)
	throws CvsException
    {
	lru.toHead(this);
	checkUse(filename);
	CvsEntry entry = 
	    (entries != null) ? (CvsEntry) entries.get(filename) : null;
	if ( entry == null ) {
	    //let's make a try with the filename in update
	    refresh(filename);
	    entry = 
		(entries != null) ? (CvsEntry) entries.get(filename) : null;
	    if ( entry == null )
		throw new CvsException(filename+": no such entry.");
	} else if (( entry.getRevision() == null) && 
		   (entry.getStatus() != FILE_Q)) {
	    refreshRevision(filename);
	}
	return entry.getStickyOptions();
    }    


    /**
     * Update these files from the repository.
     * @param files The name of the files to update (as a String array).
     * @exception CvsException If CVS process failed.
     */

    public void update(String files[]) 
	throws CvsException
    {
	lru.toHead(this);
	// No need to checkUse here:
	CvsUpdateHandler handler = new CvsUpdateHandler(this);
	runner.cvsUpdate(this, files, handler);
    }

    /**
     * Update this file from the repository.
     * @param file The name of the file to update (as a String).
     * @exception CvsException If CVS process failed.
     */
    public void update(String file) 
	throws CvsException
    {
	String files[] = new String[1];
	files[0] = file;
	update(files);
    }

    /**
     * Update all that directory's content. (not recursivly).
     * @exception CvsException If some CVS error occured during update.
     */

    public void update() 
	throws CvsException
    {
	lru.toHead(this);
	CvsUpdateHandler handler = new CvsUpdateHandler(this);
	runner.cvsUpdate(this, handler);
    }

    /**
     * Update thes files matching the given regular expression  from the 
     * repository.
     * @param files The name of the files to update (as a String array).
     * @exception CvsException If CVS process failed.
     */
    public void updateRegexp(String regexp) 
	throws CvsException
    {
	update(regexp);
    }

    /**
     * Perform a cvs get
     * @param path the file (or directory) path
     * @exception CvsException If CVS process failed.
     */
    public void get(String path) 
	throws CvsException 
    {
	lru.toHead(this);
	runner.cvsGet(this, path);
    }

    /**
     * Commit pending actions on given file.
     * @param names The name of the files to commit.
     * @param msg The associated message.
     * @param env The extra env to use during commit.
     * @exception CvsException If some error occured during the CVS process.
     */
    public void commit(String names[], String msg, String env[])
	throws CvsException
    {
	lru.toHead(this);
	// We don't really need to check use here.
	CvsCommitHandler handler = new CvsCommitHandler(this);
	runner.cvsCommit(this, names, msg, handler, env);
    }

    /**
     * Commit pending actions on given file.
     * @param file The file to commit.
     * @param msg The associated message.
     * @param env The extra env to use during commit.
     * @exception CvsException If some error occured during the CVS process.
     */
    public void commit(String file, String msg, String env[]) 
	throws CvsException
    {
        lru.toHead(this);
	String names[] = new String[1];
	names[0] = file;
	commit(names, msg, env);
    }

    /**
     * Commit pending actions on given file.
     * @param names The name of the files to commit.
     * @param msg The associated message.
     * @exception CvsException If some error occured during the CVS process.
     */
    public void commit(String names[], String msg)
	throws CvsException
    {
	lru.toHead(this);
	// We don't really need to check use here.
	CvsCommitHandler handler = new CvsCommitHandler(this);
	runner.cvsCommit(this, names, msg, handler);
    }

    /**
     * Commit pending actions on given file.
     * @param file The file to commit.
     * @param msg The associated message.
     * @exception CvsException If some error occured during the CVS process.
     */
    public void commit(String file, String msg) 
	throws CvsException
    {
	String names[] = new String[1];
	names[0] = file;
	commit(names, msg);
    }

    /**
     * Commit pending actions to all that directory content.
     * @param msg The associated message.
     * @exception CvsException If some CVS error occurs during the CVS process.
     */

    public void commit(String msg)
	throws CvsException
    {
	lru.toHead(this);
	// No need to checkUse
	CvsCommitHandler handler = new CvsCommitHandler(this);
	runner.cvsCommit(this, msg, handler);
    }

    /**
     * Commit pending actions to all that directory content.
     * @param msg The associated message.
     * @param env The extra environment.
     * @exception CvsException If some CVS error occurs during the CVS process.
     */

    public void commit(String msg, String env[])
	throws CvsException
    {
	lru.toHead(this);
	// No need to checkUse
	CvsCommitHandler handler = new CvsCommitHandler(this);
	runner.cvsCommit(this, msg, handler, env);
    }

    /**
     * Commit pending actions on files matching the given regular expression.
     * @param regexp The regular expresion. 
     * @param msg The associated message.
     * @param env The extra env to use during commit.
     * @exception CvsException If some error occured during the CVS process.
     */
    public void commitRegexp(String regexp, String comment, String env[]) 
	throws CvsException
    {
	commit(regexp, comment, env);
    }

    /**
     * Revert the file, make the given revision the current one.
     * <UL><LI>First remove the file</LI>
     * <LI>perform a cvs update -p -r <revision></LI>
     * @param filename The name of the file to revert.
     * @param revision The revision number to get.
     * @param msg The associated message.
     * @param env The extra environment.
     * @exception CvsException If some CVS error occurs during the CVS process.
     */
    public void revert(String filename,
		       String revision,
		       String msg,
		       String env[])
        throws CvsException
    {
	File file = new File( getDirectory(), filename);
	if (!file.exists())
	    throw new CvsException("the file "+file+" can't be reverted : "+
				   "it doesn't exists!");
	// 1: remove the file
	file.delete();
	String names[] = new String[1];
	names[0] = filename;
	// 2: revert 
	runner.cvsRevert(this, filename, revision, file, env);
	// done!
    }

    /**
     * Revert the file, make the given revision the current one.
     * <UL><LI>First remove the file</LI>
     * <LI>perform a cvs update -p -r <revision></LI>
     * @param filename The name of the file to revert.
     * @param revision The revision number to get.
     * @param out The output stream. (reverted file will be written in)
     * @param env The extra environment.
     * @exception CvsException If some CVS error occurs during the CVS process.
     */
    public void revert(String filename, 
		       OutputStream out, 
		       String revision,
		       String env[]) 
	throws CvsException    
    {
	runner.cvsRevert(this, filename, revision, out, env);
    }

    /**
     * Get the log associated to the given file.
     * @param filename The name of the file whose log is to be fetched.
     * @exception CvsException If some CVS error occurs in the process.
     */

    public String log(String filename)
	throws CvsException
    {
	lru.toHead(this);
	// No need to checkUse here
	return runner.cvsLog(this, filename);
    }

    /**
     * Get the diff of given file against repository.
     * @param filename The name of the file to diff.
     * @return The diffs has a String, or <strong>null</strong> if the file
     * is in sync with the repository.
     * @exception CvsException If some CVS exception occurs within the
     * process.
     */

    public String diff(String filename)
	throws CvsException
    {
	lru.toHead(this);
	checkUse(filename);
	CvsEntry entry = getFileEntry(filename);
	// Spare a cvs command when possible:
	if ((entry != null) 
	    && ( ! entry.needsUpdate())
	    && (entry.getStatus() == FILE_OK))
	    return null;
	// Have to run the command:
	return runner.cvsDiff(this, filename);
    }

    /**
     * Add the given file to the CVS repository.
     * The addition will have to be commited through a commit of the same
     * file before taking effect.
     * @param names The name of the files to add.
     * @exception CvsException If some CVS error occurs during the process.
     */

    public void add(String names[]) 
	throws CvsException
    {
	add(names, null);
    }

    /**
     * Add the given file to the CVS repository.
     * The addition will have to be commited through a commit of the same
     * file before taking effect.
     * @param names The name of the files to add.
     * @param env The extra env to use during the process.
     * @exception CvsException If some CVS error occurs during the process.
     */
    public void add(String names[], String env[]) 
	throws CvsException
    {
	lru.toHead(this);
	// No need to checkUse here:
	long stamp = System.currentTimeMillis();
	runner.cvsAdd(this, names, env);
	// Update all files status:
	for (int i = 0 ; i < names.length; i++) {
	    CvsEntry entry = getFileEntry(names[i]);
	    if ( entry != null )
		entry.setStatus(stamp, FILE_A);
	    else
		createFileEntry(stamp, names[i], FILE_A);
	}      
    } 

    /**
     * Add the file matching the given regular expression to the CVS 
     * repository.
     * The addition will have to be commited through a commit of the same
     * file before taking effect.
     * @param regexp The regular expression.
     * @param env The extra env to use during the process.
     * @exception CvsException If some CVS error occurs during the process.
     */
    public void addRegexp(String regexp, String env[]) 
	throws CvsException
    {
	lru.toHead(this);
	String files[] = new String[1];
	files[0] = regexp;
	runner.cvsAdd(this, files, env);
	refresh();
    }

    /**
     * Remove the given file from the repository.
     * @param names The files to be removed.
     * @exception CvsException If some CVS error occurs during that process.
     */

    public void remove(String names[], String msg, String env[])
	throws CvsException
    {
	lru.toHead(this);
	// No need to check use here:
	long stamp = System.currentTimeMillis();
	runner.cvsRemove(this, names);
	//commit the remove
	commit(names, msg, env);
	// Update all files status:
	for (int i = 0 ; i < names.length; i++) {
	    removeFileEntry(names[i]);
	}
// 	for (int i = 0 ; i < names.length; i++) {
// 	    CvsEntry entry = getFileEntry(names[i]);
// 	    if ( entry != null )
// 		entry.setStatus(stamp, FILE_R);
// 	    else
// 		createFileEntry(stamp, names[i], FILE_R);
// 	}
    }

    /**
     * Perform a RCS request.
     * @param command the rcs command splitted in a string array.
     * @exception CvsException If some CVS error occurs during that process.
     */
    public void admin(String command[]) 
	throws CvsException
    {
	lru.toHead(this);
	runner.cvsAdmin(this, command);
    }

    /**
     * Display the status of the entries in that directory.
     * @param prt A print stream to print to.
     */

    public void display(PrintStream prt) {
	lru.toHead(this);
	try {
	    Enumeration  files = listFiles();
	    while ( files.hasMoreElements() ) {
		String name = (String) files.nextElement();
		int    stat = status(name);
		prt.println(name+": "+status[stat]);
	    }
	} catch (CvsException ex) {
	    prt.println("*** CVS Error: "+ex.getMessage());
	}
    }

    /**
     * Get a sub-directory status.
     * @param subdir The name of the subdirectory.
     * @return An integer CVS status for the given sub-directory.
     * @exception CvsException if the CVS process failed.
     */

    public int getDirectoryStatus(String subdir)
	throws CvsException
    {
	return getDirectoryStatus(subdir, true);
    }

    /**
     * Get a sub-directory status.
     * @param subdir The name of the subdirectory.
     * @param update If true update all directory entries.
     * @return An integer CVS status for the given sub-directory.
     * @exception CvsException if the CVS process failed.
     */
    public int getDirectoryStatus(String subdir, boolean update) 
    	throws CvsException
    {
	lru.toHead(this);
	if (update)
	    checkDirectoryUse();
	else
	    checkDirectoryUse(subdir);
	CvsEntry entry = getDirectoryEntry(subdir);
	return (entry == null) ? DIR_Q : entry.getStatus();
    }

    /**
     * Check out, or update a sub-directory.
     * @param subdir The sub directory to update.
     * @exception CvsException If the CVS process failed.
     */

    public void updateDirectory(String subdir) 
	throws CvsException
    {
	lru.toHead(this);
	// Check use of the directories:
	checkDirectoryUse();
	// Run the command:
	CvsEntry entry = getDirectoryEntry(subdir);
	if ( entry == null )
	    throw new CvsException("Unknown subdirectory "+subdir);
	DirectoryUpdateHandler handler = new DirectoryUpdateHandler(this);
	runner.cvsUpdateDirectory(this, entry.file, handler);
	// Mark the new entry state:
	entry.setStatus(handler.stamp , DIR_CO);
    }

    /**
     * Get the directory controlled by this manager.
     * @return A File instance locating the directory.
     */

    public File getDirectory() {
	return directory;
    }

    /**
     * Create a new CVS manager for the given directory.
     * @param directory The directory for which a manager is to be created.
     * @param props The cvs properties.
     * @param cvspath The absolute path of the cvs program.
     * @param cvsroot The absolute path of the CVS repository. 
     * @param cvswrap The absolute path of the cvs wrapper program
     * @exception CvsException If some initialisation failed
     */

    protected CvsDirectory(File directory
			   , Properties props
			   , String cvspath
			   , String cvsroot
			   , String cvswrap[])
	throws CvsException
    {
	if ( ! directory.exists() )
	    throw new UncheckedOutException("Unchecked out directory: "
					    + directory.getAbsolutePath());
	this.props          = props;
	this.cvspath        = ((cvspath == null)
			       ? props.getProperty(CVSPATH_P, cvspath_def)
			       : cvspath);
	this.cvsroot        = ((cvsroot == null)
			       ? props.getProperty(CVSROOT_P, cvsroot_def)
			       : cvsroot);
	this.cvswrapper     = ((cvswrap == null) 
			       ? parseArrayProperty(props.getProperty(
								  CVSWRAP_P,
								  cvswrap_def))
			       : cvswrap);
	this.directory      = directory;
	this.cvscheck_stamp = -1;
	this.runner         = new CvsRunner();
    }

    public static void usage() {
	System.out.println("CvsDirectory <dir> [command] [files]");
	System.exit(1);
    }

    public static void main(String args[]) {
	String command  = null;
	String params[] = null;
	File   target   = null;

	// Parse command line:
	switch (args.length) {
	case 0:
	    usage();
	    break;
	case 1:
	    target = new File(args[0]);
	    break;
	case 2:
	    target  = new File(args[0]);
	    command = args[1];
	    break;
	default:
	    target  = new File(args[0]);
	    command = args[1];
	    params  = new String[args.length-2];
	    System.arraycopy(args, 2, params, 0, args.length-2);
	    break;
	}
	// Load the directory and perform command:
	try {
	    CvsDirectory cvs = CvsDirectory.getManager(new File(args[0]));
	    if ( command == null ) {
		cvs.display(System.out);
	    } else if ( command.equals("update") ) {
		if ( params != null )
		    cvs.update(params);
		else
		    cvs.update();
	    } else if ( command.equals("status") ) {
		if ( params.length != 1 )
		    usage();
		System.out.println(status[cvs.status(params[0])]);
	    } else if ( command.equals("diff") ) {
		if ( params.length != 1 )
		    usage();
		System.out.println(cvs.diff(params[0]));
	    } else if ( command.equals("log") ) {
		if ( params.length != 1 )
		    usage();
		System.out.println(cvs.log(params[0]));
	    } else if ( command.equals("add") ) {
		if ( params.length == 0 )
		    usage();
		cvs.add(params);
	    } else if ( command.equals("listdir") ) {
		Enumeration e = cvs.listDirectories();
		while ( e.hasMoreElements() ) {
		    String subdir = (String) e.nextElement();
		    System.out.println(subdir+
				       ": "+
				       status[cvs.getDirectoryStatus(subdir)]);
		}
	    } else if ( command.equals("updatedir") ) {
		if ( params.length == 0 )
		    usage();
		cvs.updateDirectory(params[0]);
	    }  else {
		System.err.println("Unknown command ["+command+"]");
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

}
