// DuplicateNameException.java
// $Id: DuplicateNameException.java,v 1.1 2010/06/15 12:20:20 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String name) {
	super(name);
    }

    public String getName() {
	return getMessage();
    }

}
