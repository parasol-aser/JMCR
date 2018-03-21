// ZipFileResource.java
// $Id: ZipFileResource.java,v 1.2 2010/06/15 17:53:04 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.zip;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.StringAttribute;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ZipFileResource extends FileResource {

    /**
     * Attributes index - The filename attribute.
     */
    protected static int ATTR_ZIPFILE = -1 ;
    /**
     * Attribute index - The index for our entry path.
     */
    protected static int ATTR_ENTRYPATH = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.zip.ZipFileResource") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	// The zip file attribute.
	a = new FileAttribute("zipfile"
			      , null
			      , Attribute.COMPUTED) ;
	ATTR_ZIPFILE = AttributeRegistry.registerAttribute(cls, a) ;
	// the entry path attribute
	a = new StringAttribute("entrypath"
				, null
				, Attribute.COMPUTED) ;
	ATTR_ENTRYPATH = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get this zip file.
     */
    public synchronized File getFile() {
	return (File) getValue(ATTR_ZIPFILE, null);
    }

    public String getEntryPath() {
	return getString(ATTR_ENTRYPATH, null);
    }

    protected synchronized InputStream getInputStream() {
	try {
	    ZipFile zipfile = new ZipFile(getFile());
	    ZipEntry zentry = zipfile.getEntry(getEntryPath());
	    return new ZipInputStream(zipfile, zipfile.getInputStream(zentry));
	} catch (IOException ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    protected synchronized long getEntrySize() {
	ZipFile z = null;
	try {
	    z = new ZipFile(getFile());
	    ZipEntry entry = z.getEntry(getEntryPath());
	    if (entry != null)
		return entry.getSize();
	    else 
		return 0;
	} catch (IOException ex) {
	    return 0;
	} finally {
	    try { z.close(); } catch (Exception ex) {}
	}
    }

    protected synchronized boolean hasEntry() {
	ZipFile z = null;
	try {
	    z = new ZipFile(getFile());
	    return (z.getEntry(getEntryPath()) != null);
	} catch (IOException ex) {
	    return false;
	} finally {
	    try { z.close(); } catch (Exception ex) {}
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
	setValue(ATTR_FILE_LENGTH, new Integer((int)getEntrySize()));
	return ;
    }

    /**
     * Is that resource still wrapping an existing file ?
     * If the underlying file has disappeared <string> and if</strong> the
     * container directory is extensible, remove the resource.
     * @return A boolean.
     */

    public synchronized boolean verify() {
	File file = getFile();
	if ( ! file.exists() ) 
	    return false;
	return (hasEntry());
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
	throw new IOException("Can't modify the content of ZipFile");
    }

}
