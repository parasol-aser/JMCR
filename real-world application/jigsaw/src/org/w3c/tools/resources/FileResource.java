// FileResource.java
// $Id: FileResource.java,v 1.2 2010/06/15 17:52:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple file resource.
 */
public class FileResource extends FramedResource {

    /**
     * Attributes index - The filename attribute.
     */
    protected static int ATTR_FILENAME = -1 ;

    /**
     * Attribute index - The date at which we last checked the file content.
     */
    protected static int ATTR_FILESTAMP = -1 ;

    /**
     * Attribute index - The index for the content length attribute.
     */
    protected static int ATTR_FILE_LENGTH = -1 ;

    /**
     * Attribute index - The index for the backup flag
     */
    protected static int ATTR_FILE_BACKUP = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.tools.resources.FileResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	// The filename attribute.
	a = new FilenameAttribute("filename"
				  , null
				  , Attribute.EDITABLE) ;
	ATTR_FILENAME = AttributeRegistry.registerAttribute(cls, a) ;
	// The file stamp attribute
	a = new DateAttribute("file-stamp"
			      , new Long(-1) 
			      , Attribute.COMPUTED) ;
	ATTR_FILESTAMP = AttributeRegistry.registerAttribute(cls, a) ;
	// The file length attribute:
	a = new IntegerAttribute("file-length"
				 , null
				 , Attribute.COMPUTED);
	ATTR_FILE_LENGTH = AttributeRegistry.registerAttribute(cls,a);
	// The backup attribute:
	a = new BooleanAttribute("backup"
				 , Boolean.FALSE
				 , Attribute.EDITABLE);
	ATTR_FILE_BACKUP = AttributeRegistry.registerAttribute(cls,a);
    }

    /**
     * The file we refer to.
     * This is a cached version of some attributes, so we need to override
     * the setValue method in order to be able to catch any changes to it.
     */
    protected File file = null ;

    /**
     * Get this resource filename attribute.
     */
    public String getFilename() {
	return (String) getValue(ATTR_FILENAME, null);
    }

    /**
     * Get this file length
     */
    public int getFileLength() {
	return ((Integer) getValue(ATTR_FILE_LENGTH, 
				   new Integer(0))).intValue();
    }

    /**
     * Get the date at which we last examined the file.
     */

    public long getFileStamp() {
	return getLong(ATTR_FILESTAMP, (long) -1) ;
    }

    /**
     * Get the backup flag, create a backup file when content change
     * if true.
     */

    public boolean getBackupFlag() {
	return getBoolean(ATTR_FILE_BACKUP, false) ;
    }

    /**
     * Get the name of the backup file for this resource.
     * @return A File object suitable to receive the backup version of this
     *    file.
     */

    public File getBackupFile() {
	File   file = getFile() ;
	String name = file.getName() ;
	return new File(file.getParent(), name+"~") ;
    }

    /**
     * Save the given stream as the underlying file content.
     * This method preserve the old file version in a <code>~</code> file.
     * @param in The input stream to use as the resource entity.
     * @return A boolean, <strong>true</strong> if the resource was just
     * created, <strong>false</strong> otherwise.
     * @exception IOException If dumping the content failed.
     */

    public synchronized boolean newContent(InputStream in) 
	throws IOException
    {
	File   file     = getFile() ;
	boolean created = (! file.exists() || (file.length() == 0));
	String name     = file.getName() ;
	File   temp     = new File(file.getParent(), "#"+name+"#") ;
	String iomsg    = null ;
	// We are not catching IO exceptions here, except to remove temp:
	try {
	    FileOutputStream fout  = new FileOutputStream(temp) ;
	    byte             buf[] = new byte[4096] ;
	    for (int got = 0 ; (got = in.read(buf)) > 0 ; )
		fout.write(buf, 0, got) ;
	    fout.close() ;
	} catch (IOException ex) {
	    iomsg = ex.getMessage() ;
	} finally {
	    if ( iomsg != null ) {
		temp.delete() ;
		throw new IOException(iomsg) ;
	    } else {
		if (getBackupFlag()) {
		    File backup = getBackupFile();
		    if (backup.exists())
			backup.delete();
		    file.renameTo(getBackupFile()) ;
		}
		// with some OSes, rename doesn't overwrite so...
		if (file.exists()) 
		    file.delete();
		temp.renameTo(file) ;
		// update our attributes for this new content:
		updateFileAttributes() ;
	    }
	}
	return created;
    }

    /**
     * Check this file content, and update attributes if needed.
     * This method is normally called before any perform request is done, so
     * that we make sure that all meta-informations is up to date before
     * handling a request.
     * @return The time of the last update to the resource.
     */

    public long checkContent() {
	File file = getFile() ;
	// Has this resource changed since last queried ? 
	long lmt = file.lastModified() ;
	long cmt = getFileStamp() ;
	if ((cmt < 0) || (cmt < lmt)) {
	    updateFileAttributes() ;
	    return getLastModified() ;
	} else if (getFileLength() != (int)file.length()) {
	    updateFileAttributes() ;
	    return getLastModified() ;
	} else {
	    return cmt;
	}
    }

    /**
     * Set some of this resource attribute.
     * We just catch here any write access to the filename's, to update 
     * our cache file object.
     */

    public synchronized void setValue(int idx, Object value) {
	if (idx == ATTR_IDENTIFIER) {
	    String oldid = getIdentifier();
	    if (getFilename() == null) {
		File oldfile = getFile(oldid);
		if ((oldfile != null) && (oldfile.exists()))
		    super.setValue(ATTR_FILENAME, oldid);
	    }
	}
	super.setValue(idx, value) ;
	if ((idx == ATTR_FILENAME) || (idx == ATTR_IDENTIFIER))
	    file = null;
    }

    private synchronized File getFile(String name) {
	ResourceReference rr = getParent();
	ResourceReference rrtemp = null;
	Resource p = this;
	while ( true ) {
	    try {
		if (rr == null)
		    return null;
		p = rr.lock();
		if (p instanceof DirectoryResource) {
		    DirectoryResource dr = (DirectoryResource) p;
		    return new File(dr.unsafeGetDirectory(), name);
		}
		rrtemp = p.getParent();
	    } catch (InvalidResourceException ex) {
		ex.printStackTrace();
		return null;
	    } finally {
		if (rr != null)
		    rr.unlock();
	    }
	    rr = rrtemp;
	}
    }

    /**
     * Get this file resource file name.
     * @return a File instance.
     * @exception InvalidParentException If no parent is available, 
     * and then the FileReource is unable to get its file.
     */

    public synchronized File getFile() {
	// Have we already computed this ?
	if ( file == null ) {
	    // Get the file name:
	    String name = getFilename() ;
	    if ( name == null )
		name = getIdentifier() ;
	    // Get the file directory:
	    ResourceReference rr = getParent();
	    ResourceReference rrtemp = null;
	    Resource p = this;
	    while ( true ) {
		try {
		    if (rr == null)
			throw new InvalidParentException(p.getIdentifier()+
						   " can't find his parent, "+
						   "context : "+
						   p.unsafeGetContext());
		    p = rr.lock();
		    if (p instanceof DirectoryResource) {
			DirectoryResource dr = (DirectoryResource) p;
			file = new File(dr.unsafeGetDirectory(), name);
			return file;
		    }
		    rrtemp = p.getParent();
		} catch (InvalidResourceException ex) {
		    ex.printStackTrace();
		    return null;
		} finally {
		    if (rr != null)
			rr.unlock();
		}
		rr = rrtemp;
	    }
	}
	return file ;
    }

    /**
     * Is that resource still wrapping an existing file ?
     * If the underlying file has disappeared <string> and if</strong> the
     * container directory is extensible, remove the resource.
     * @return true if that resource is wrapping an existing file
     * @exception org.w3c.tools.resources.MultipleLockException When the 
     * resource try to delete itself (because there is no more file)
     */

    public synchronized boolean verify() 
	throws MultipleLockException
    {
	File file = getFile();
	if ( ! file.exists() ) {
	    // Is the parent extensible:
	    ResourceReference rr = getParent();
	    ResourceReference rrtemp = null;
	    Resource p = null;

	    while ( true ) {
		try {
		    if (rr == null)
			return false;
		    p = rr.lock();
		    if (p instanceof DirectoryResource) {
			DirectoryResource d = (DirectoryResource) p;
			if ( ! d.unsafeGetShrinkableFlag() ) 
			    return false;
			else {
			    // Emit an error message, and delete the resource:
			    String msg = file+
				": deleted, removing the FileResource.";
			    getServer().errlog(this, msg);
			    delete();
			    return false;
			}
		    }
		    rrtemp = p.getParent();
		} catch (InvalidResourceException ex) {
		    return false;
		} finally {
		    if (rr != null)
			rr.unlock();
		}
		rr = rrtemp;
	    }
	} else {
	    return true;
	}
    }

    /**
     * Update the file related attributes.
     * The file we serve has changed since the last time we checked it, if
     * any of the attribute values depend on the file content, this is the
     * appropriate place to recompute them.
     */

    public void updateFileAttributes() {
	File file = getFile() ;
	setValue(ATTR_FILESTAMP, new Long(file.lastModified()));
	setValue(ATTR_FILE_LENGTH, new Integer((int)file.length()));
	return ;
    }

    /**
     * Update our computed attributes.
     */

    public void updateAttributes() {
	long fstamp = getFile().lastModified() ;
	long stamp  = getLong(ATTR_FILESTAMP, -1) ;
	// should be stamp < fstamp, but it avoid clocks weirdness
	if ((stamp < 0) || (stamp != fstamp)) 
	    updateFileAttributes() ;
    }

    /**
     * Initialize the FileResource instance.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	disableEvent();
	// If we have a filename attribute, update url:
	String filename = getFilename();
	if ( filename != null ) {
	    ResourceReference rr = getParent();
	    if (rr != null) {
		try {
		    Resource parent = rr.unsafeLock();
		    setValue(ATTR_URL, parent.unsafeGetURLPath()+
			     java.net.URLEncoder.encode(getIdentifier()));
		} catch (InvalidResourceException ex) {
		    //FIXME 
		} finally {
		    rr.unlock();
		}
	    }
	}
	enableEvent();
    }
}
