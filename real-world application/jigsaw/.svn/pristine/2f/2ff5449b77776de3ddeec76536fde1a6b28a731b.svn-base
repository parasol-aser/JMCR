// InvalidStoreClassException.java
// $Id: InvalidStoreClassException.java,v 1.1 2010/06/15 12:25:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996-1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.store;

/**
 * A store is being loaded by a class who didn't write it.
 */

public class InvalidStoreClassException extends Exception {
    /**
     * The class willing to load that store.
     */
    protected String loader = null;
    /**
     * The class willing to unload that store.
     */
    protected String saver = null;

    /**
     * Get the loader's class name.
     * @return A String giving the full name of the loader's class.
     */

    public String getLoaderClassName() {
	return loader;
    }

    /**
     * Get the saver's class name.
     * @return A String giving the full name of the saver's class.
     */

    public String getSaverClassName() {
	return saver;
    }

    public InvalidStoreClassException(String loadingcls, String expectedcls) {
	super("invalid class: "+loadingcls+", supposed to be: "+expectedcls);
	this.loader = loadingcls;
	this.saver  = expectedcls;
    }

}
