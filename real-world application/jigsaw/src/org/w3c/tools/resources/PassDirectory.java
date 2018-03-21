// PassDirectory.java
// $Id: PassDirectory.java,v 1.2 2010/06/15 17:53:00 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.File;

/**
 * A directory resource able to pass (jump) into some non-related directory.
 * This resource provides something similar to the PASSS rule of the CERN
 * server: it allows you to export a directory that is not strictly under 
 * the server physicall space (e.g. none of its parent directory is the server
 * space directory).
 * <p>Using this resource means that your exported space is no more
 * restrained to the server space directory, which can be considered as 
 * a security problem.
 */
public class PassDirectory extends DirectoryResource {

    /**
     * Attribute index - The target physicall directory of this resource.
     */
    protected static int ATTR_PASSTARGET = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;

	// Get a pointer to our class.
	try {
	    cls = Class.forName("org.w3c.tools.resources.PassDirectory") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The directory attribute.
	a = new FileAttribute("pass-target"
			      , null
			      , Attribute.EDITABLE);
	ATTR_PASSTARGET = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Catch side-effects on pass-target, to absolutize it.
     * @param idx The attribute to set.
     * @param value The new value.
     */

    public void setValue(int idx, Object value) {
	if ( (idx == ATTR_IDENTIFIER) || (idx == ATTR_PASSTARGET)) {
	    try {
		deleteChildren();
	    } catch (MultipleLockException ex) {
		//nothing to do
	    }
	}
	super.setValue(idx, value);
	if ( idx == ATTR_IDENTIFIER ) {
	    ResourceReference rr = getParent();
	    if (rr != null) {
		try {
		    Resource parent = rr.lock();
		    if (parent.definesAttribute("directory")) {
			File pdir = (File) parent.getValue("directory", null);
			if ( pdir != null ) {
			    // Compute and set our directory attribute:
			    File dir = new File(pdir, getIdentifier()) ;
			    super.setValue(ATTR_DIRECTORY, dir) ;
			}
		    }
		} catch (InvalidResourceException ex) {
	  
		} finally {
		    rr.unlock();
		}
	    }
	    values[ATTR_PASSTARGET] = null;
	    values[ATTR_DIRSTAMP]   = new Long(-1);
	} else if ( idx == ATTR_PASSTARGET ) {
	    File file = (File) value;
	    if ( ! file.isAbsolute() ) {
		// Make it absolute, relative to the server space.
		File abs = new File(getServer().getRootDirectory()
				    , file.toString());
		values[ATTR_PASSTARGET] = abs;
		values[ATTR_DIRECTORY]  = abs;
	    } else {
		values[ATTR_PASSTARGET] = value;
		values[ATTR_DIRECTORY]  = value;
	    }
	    values[ATTR_DIRSTAMP] = new Long(-1);
	}
    }

    /**
     * The getDirectory method now returns the pass-directory.
     * @return The pass target location.
     */

    public File getDirectory() {
	File dir = (File) getValue(ATTR_PASSTARGET, null) ;
	if (dir == null)
	    dir = super.getDirectory();
	return dir;
    }

    /**
     * Make the directory attribute default to the target location.
     * This is required for classes that rely on the directory attribute to
     * compute their own attributes.
     * @param values The values we should initialized from.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	disableEvent();
	File target = getDirectory();
	if ( target != null ) 
	    setValue(ATTR_DIRECTORY, target);
	enableEvent();
    }

}
