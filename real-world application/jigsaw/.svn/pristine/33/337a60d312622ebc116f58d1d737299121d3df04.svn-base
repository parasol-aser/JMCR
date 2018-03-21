// PrincipalImpl.java
// $Id: PrincipalImpl.java,v 1.1 2010/06/15 12:24:11 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.servlet;

import java.security.Principal;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class PrincipalImpl implements Principal {

    protected String name;

    /**
     * Compares this principal to the specified object.  Returns true
     * if the object passed in matches the principal represented by
     * the implementation of this interface.
     *
     * @param another principal to compare with.
     *
     * @return true if the principal passed in is the same as that
     * encapsulated by this principal, and false otherwise.
     */
    public boolean equals(Object another) {
	return ((Principal)another).getName().equalsIgnoreCase(name);
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return a string representation of this principal.
     */
    public String toString() {
	return name;
    }

    /**
     * Returns a hashcode for this principal.
     *
     * @return a hashcode for this principal.
     */
    public int hashCode() {
	return name.hashCode();
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
	return name;
    }

    public PrincipalImpl(String name) {
	this.name = name;
    }

}
