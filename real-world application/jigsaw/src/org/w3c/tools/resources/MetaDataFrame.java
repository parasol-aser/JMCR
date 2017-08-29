// MetaDataFrame.java
// $Id: MetaDataFrame.java,v 1.1 2010/06/15 12:20:18 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

public class MetaDataFrame extends ResourceFrame {

    /**
     * Get our target resource.
     */
    public Resource getTargetResource() {
	Resource target = (Resource) getResource();
	while (target instanceof ResourceFrame) {
	    target = ((ResourceFrame)target).getResource();
	}
	return target;
    }

    /**
     * Perform the request, return null in MetaDataFrame.
     * @param request the incomming request
     * @exception ProtocolException If an error relative to the protocol occurs
     * @exception ResourceException If an error not relative to the 
     * protocol occurs
     */ 
    public ReplyInterface perform(RequestInterface request) 
	throws ProtocolException, ResourceException
    {
	return null;
    }
}
