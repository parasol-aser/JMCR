// AutoLookupDirectory.java
// $Id: AutoLookupDirectory.java,v 1.2 2010/06/15 17:53:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.resources ;

import java.io.File;

import org.w3c.cvs2.CVS;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.http.Request;

import org.w3c.jigedit.cvs2.CvsModule;
import org.w3c.jigedit.cvs2.CvsRootDirectory;

/**
 * A special version of DirectoryResource that can fetch a file
 * from CVS directly if it is not already here.
 * It can alos do an automatic update, depending on a flag
 */

public class AutoLookupDirectory extends CvsRootDirectory {

    private CvsDirectory cvs = null ;

    /**
     * Attribute index, tell if we must update the resource everytime it is
     * acceded (not recommended as it generates many cvs commands)
     */

    private static int ATTR_AUTOUPDATE = -1;

    /**
     * Attribute index, tell if we must add into cvs new puted directories.
     */
    private static int ATTR_EXTENSIBLE = -1;

    static {
	Attribute   a = null ;
	Class     cls = null;

	try {
	    cls = 
	      Class.forName("org.w3c.jigedit.resources.AutoLookupDirectory") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The browsable flag:
	a = new BooleanAttribute("autoupdate",
				 Boolean.FALSE,
				 Attribute.EDITABLE) ;
	ATTR_AUTOUPDATE = AttributeRegistry.registerAttribute(cls, a) ;
        // The browsable flag:
	a = new BooleanAttribute("cvs-extensible",
				 Boolean.FALSE,
				 Attribute.EDITABLE) ;
	ATTR_EXTENSIBLE = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * Get the appropriate CVS manager for the directory we handle.
     * @return A CvsDirectory instance.
     * @exception CvsException If we couldn't get the manager.
     */

    protected synchronized CvsDirectory getCvsManager() 
	throws CvsException
    {
	if ( cvs == null ) {
	    cvs = CvsModule.getCvsManager(getDirectory(),
					  getContext(),
					  getServer().getProperties());
	}
	return cvs;
    }

	    
    /**
     * tell if we must always do an update.
     */

    public boolean isAutoUpdatable() {
	return getBoolean(ATTR_AUTOUPDATE, false);
    }

    /**
     * tell if we must add in cvs the new puted documents.
     */
    public boolean isCvsExtensible() {
	return getBoolean(ATTR_EXTENSIBLE, false);
    }

    /**
     * Create a DirectoryResource and the physical directory too.
     * Add the new directory in the CVS repository.
     * @param name the name of the resource.
     * @return A ResourceReference instance.
     */
    public ResourceReference createDirectoryResource(String name) {
	ResourceReference newdir = super.createDirectoryResource(name);
	if ((newdir != null) && isCvsExtensible()) {
	    String names[] = new String[1];
	    names[0] = name;
	    try {
		getCvsManager().add(names);
	    } catch (CvsException ex) {
		getServer().errlog(this, ex.getMessage());
	    }
	}
	return newdir;
    }

    /**
     * Lookup the next component of this lookup state in here.
     * @param ls The current lookup state.
     * @param lr The lookup result under construction.
     * @exception ProtocolException If an error occurs.
     * @return A boolean, <strong>true</strong> if lookup has completed, 
     * <strong>false</strong> if it should be continued by the caller.
     */
    public boolean lookup(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	Request request = (Request) ls.getRequest();
	if ((request != null) && (request.getMethod().equals("PUT"))) {
	    if ( ls.hasMoreComponents() ) {
		String name = null;
		try {
		    name = ls.peekNextComponent();
		    File dir = new File(getDirectory(), name);
		    if (dir.isDirectory()) {
			CvsDirectory cvs = getCvsManager();
			if (cvs.getDirectoryStatus(name, false) == CVS.DIR_Q) {
			    String names[] = { name };
			    cvs.add(names);
			}
		    }
		} catch (CvsException ex) {
		    String msg = "cvs add \""+name+"\" failed.";
		    getServer().errlog(this, msg);
		}
	    }
	}
	return super.lookup(ls,lr);
    }

    /**
     * Lookup the resource having the given name in this directory.
     * if the resource is not present, it will try to fetch it from
     * the Cvs repository.
     * @param name The name of the resource.
     * @return A ResourceReference instance, or <strong>null</strong>.
     */

    public ResourceReference lookup(String name) {
	ResourceReference rr = super.lookup(name);
	if (rr == null) {
	    // This may be an unchecked out directory:
	    try {
		CvsDirectory cvs = getCvsManager();
		if ( cvs.getDirectoryStatus(name) == CVS.DIR_NCO )
		    cvs.updateDirectory(name);
	    } catch (CvsException ex) {
		String msg = "cvs update -d \""+name+"\" failed.";
		getServer().errlog(this, msg);
		return null;
	    }
	    // Checking out the directory succeeded, retry lookup:
	    return super.lookup(name);
	} else {
	    return rr;
	}
    }

}
