// UpgradeException.java
// $Id: UpgradeException.java,v 1.1 2010/06/15 12:22:53 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.upgrade; 

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class UpgradeException extends Exception {

    public UpgradeException(String message) {
	super(message);
    }

}
