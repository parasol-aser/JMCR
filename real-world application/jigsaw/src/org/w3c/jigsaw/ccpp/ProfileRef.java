// ProfileRef.java
// $Id: ProfileRef.java,v 1.1 2010/06/15 12:28:22 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ccpp;

/**
 * A Profile reference 
 * (syntax described at http://www.w3.org/1999/06/NOTE-CCPPexchange-19990624)
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ProfileRef {

    int     number   = -1;
    boolean isDiff   = false;
    String  diffname = null;
    String  ref      = null;
    String  uri      = null;
    boolean parsed   = false;

    /**
     * Get the unparsed profile reference (used for error message)
     * @return a String;
     */
    public String getUnparsedRef() {
	return ref;
    }

    /**
     * Is this Profile reference an absolute URI?
     * @return a boolean
     */
    public boolean isURI() {
	return !isDiff;
    }

    /**
     * Get the URI
     * @return a String (or null if this reference is not an URI)
     * @exception InvalidProfileException if the profile reference is not valid
     */
    public String getURI() 
	throws InvalidProfileException
    {
	parse();
	return uri;
    }

    /**
     * Is this Profile reference a profile diff name?
     * @return a boolean
     */
    public boolean isDiffName() {
	return isDiff;
    }

    /**
     * Get the diff number
     * @return a integer (or -1 if this reference is not a profile diff name)
     * @exception InvalidProfileException if the profile reference is not valid
     */
    public int getDiffNumber() 
	throws InvalidProfileException
    {
	parse();
	return number;
    }

    /**
     * Get the profile diff name
     * @return a String (or null if this reference is not a profile diff name)
     * @exception InvalidProfileException if the profile reference is not valid
     */
    public String getDiffName() 
	throws InvalidProfileException
    {
	parse();
	return diffname;
    }

    protected void parse() 
	throws InvalidProfileException
    {
	if (! parsed) {
	    if (isDiff) {
		int idx = ref.indexOf('-');
		if (idx != -1) {
		    this.diffname = ref.substring(idx+1);
		    String snum   = ref.substring(0, idx);
		    try {
			number = Integer.parseInt(snum);
		    } catch (NumberFormatException ex) {
			number = -1;
		    }
		} else {
		    throw new InvalidProfileException(ref);
		}
	    } else {
		uri = ref;
	    }
	    parsed = true;
	}
    }

    /**
     * Constructor
     * @param ref the raw profile reference. ie :
     * <ul>
     * <li> "http://www.aaa.com/hw"
     * <li> "1-uKhjE/AEeeMzFSejsYshHg=="
     * </ul>
     */
    public ProfileRef(String ref) {
	this.ref    = ref;
	this.isDiff = !(ref.indexOf(':') != -1);
    }

}
