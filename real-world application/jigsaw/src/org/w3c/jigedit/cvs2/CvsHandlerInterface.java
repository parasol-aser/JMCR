// CvsHandlerInterface.java
// $Id: CvsHandlerInterface.java,v 1.1 2010/06/15 12:26:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs2 ;

import org.w3c.jigsaw.http.Request;

import org.w3c.tools.resources.ProtocolException;

public interface CvsHandlerInterface {

    /**
     * Perform action on the given cvs entry, on behalf of the given client.
     * @param request The request to handle.
     * @param action The action to perform.
     * @param regexp The regular expression
     * @param comment Some comments describing your changes.
     * @exception ProtocolException If the action couldn't be performed.
     */    
    public void perform (Request request, String action, 
			 String regexp, String comment)
	throws ProtocolException;

    /**
     * Perform action on the given cvs entry, on behalf of the given client.
     * @param request The request to handle.
     * @param action The action to perform.
     * @param regexp The regular expression
     * @exception ProtocolException If the action couldn't be performed.
     */    
    public void perform (Request request, String action, String regexp)
	throws ProtocolException;

    /**
     * Perform action on the given cvs entry, on behalf of the given client.
     * @param request The request to handle.
     * @param action The action to perform.
     * @param entry The entry to act on.
     * @param direntries the directories to act on
     * @exception ProtocolException If the action couldn't be performed.
     */

    public void perform (Request request
			 , String action
			 , String names[]
			 , String revs[])
	throws ProtocolException ;

    /**
     * Perform action on the given cvs entry, on behalf of the given client.
     * @param request The request that triggered the processing.
     * @param action The action to perform.
     * @param entry The entry to act on.
     * @param direntries the directories to act on
     * @param comment Some comments describing your changes.
     * @exception ProtocolException If the action couldn't be performed.
     */

    public void perform (Request request
			 , String action
			 , String names[]
			 , String revs[]
			 , String comment)
	throws ProtocolException ;

}
