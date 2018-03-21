// $Id: CvsAddException.java,v 1.1 2010/06/15 12:28:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs2;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CvsAddException extends CvsException {

    CvsAddException(String filename) {
	super(filename);
    }

}
