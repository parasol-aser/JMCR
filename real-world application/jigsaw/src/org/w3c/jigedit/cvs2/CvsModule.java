// CvsModule.java
// $Id: CvsModule.java,v 1.1 2010/06/15 12:26:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs2;

import java.util.Hashtable;
import java.util.Properties;

import java.io.File;

import org.w3c.tools.resources.ResourceContext;

import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CvsModule {

    /**
     * Name of the module
     */
    public static final String MODULE_NAME    = "org.w3c.jigedit.cvsmodule";

    /**
     * Name of the CVS Root value.
     */
    public static final String CVSROOT = "org.w3c.jigedit.cvsmodule.root";

    /**
     * Set a value.
     * @param ctxt the resource context
     * @param name The value name.
     * @param value The value to record.
     * @return an Object.
     */
    public static void setValue ( ResourceContext ctxt, 
				  String name, 
				  Object value ) 
    {
	Hashtable values = (Hashtable) ctxt.getModule(MODULE_NAME, false);
	if (values == null) {
	    values = new Hashtable(1);
	    ctxt.registerModule(MODULE_NAME, values);
	}
	values.put(name, value);
    }

    /**
     * Get a value.
     * @param ctxt the resource context
     * @param name The value name.
     * @return an Object.
     */
    public static Object getValue( ResourceContext ctxt, String name ) {
	Hashtable values = (Hashtable) ctxt.getModule(MODULE_NAME);
	if (values == null)
	    return null;
	return values.get(name);
    }

    public static synchronized CvsDirectory getCvsManager(File directory,
							  ResourceContext ctxt,
							  Properties props )
	throws CvsException
    {
	CvsDirectory cvs = null;
	String cvsroot = (String) getValue(ctxt, CVSROOT);
	if (cvsroot != null) 
	    cvs = CvsDirectory.getManager(directory,
					  props,
					  null,
					  cvsroot,
					  null);
	else
	    cvs = CvsDirectory.getManager(directory, props);
	return cvs;
    }
							  

    private CvsModule() {
    }

}
