// CvsEntry.java
// $Id: CvsEntry.java,v 1.1 2010/06/15 12:28:47 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

import java.io.File;

class CvsEntry {
    /**
     * Is that entry a directory ?
     */
    boolean isdir = false;
    /**
     * The file or directory this entry describes.
     */
    protected String name = null;

    protected String revision = null;
    /**
     * the sticy options
     */
    protected String sticky_options = null;
    /**
     * The most recent CVS status obtained for this entry.
     */
    protected int status = -1;
    /**
     * The time at which we last updated the status.
     */
    protected long timestamp = -1;
    /**
     * Our CVS directory manager.
     */
    protected CvsDirectory cvs = null;
    /**
     * Our underlying file.
     */
    protected File file = null;

    /**
     * Set this entry current status.
     * @param timestamp Date at which the CVS command was initiated.
     * @param status The new status for this entry.
     */

    protected synchronized void setStatus(long timestamp, int status) {
	this.timestamp = Math.min(file.lastModified(), timestamp);
	this.status    = status;
    }

    /**
     * Get this entry status.
     * @return An integer describing the CVS status of that entry.
     */

    protected synchronized int getStatus() {
	return status;
    }

    protected synchronized void setRevision(String revision) {
        this.revision = revision;
    }

    protected synchronized String  getRevision() {
        return revision;
    }  

    protected synchronized String getStickyOptions() {
	return sticky_options;
    }

    protected synchronized void setStickyOptions(String st_opt) {
	sticky_options = st_opt;
    }

    /**
     * Does this entry needs updating ?
     * This method checks the current timestamp for that entry against the
     * last modified date of the file to check if it needs a status update.
     * It also check the repository file stamp.
     * @return A boolean, <strong>true</strong> if the some cvs command 
     * required.
     */

    protected synchronized boolean needsUpdate() {
	File dirrep = cvs.computeRepositoryDirectory(cvs.getDirectory());
	if (dirrep == null)
	  return (timestamp < file.lastModified());

	File filrep = new File(dirrep, file.getName()+",v");
	return (((filrep != null) && filrep.exists())
		? ((timestamp < filrep.lastModified()) 
		   || (timestamp < file.lastModified()))
		: (timestamp < file.lastModified()));
    }

    CvsEntry(CvsDirectory cvs
	     , long timestamp
	     , String name
	     , boolean isdir
	     , int status) {
	this.cvs       = cvs;
	this.timestamp = timestamp;
	this.file      = new File(cvs.getDirectory(), name);
	this.name      = name;
	this.isdir     = isdir;
	this.status    = status;
    }

  
}
