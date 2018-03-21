// BasicCommand.java
// $Id: BasicCommand.java,v 1.1 2010/06/15 12:21:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HTTP ;

/**
 * This class just adds some convenience functions for commands.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public abstract class BasicCommand implements Command {
    protected static final String STATE_IF_MODIFIED_SINCE =
	"org.w3c.jigsaw.ssi.BasicCommand.If-Modified-Since" ;

    protected void handleSimpleIMS(Request request,
				   Reply reply)
    {
	long ims = request.getIfModifiedSince() ;
	if(ims == -1) {
	    Long IMS = (Long)
		request.getState(STATE_IF_MODIFIED_SINCE) ;
	    if(IMS != null) ims = IMS.longValue() ;
	}
	if(ims != -1) {
	    reply.setStatus(HTTP.NOT_MODIFIED) ;
	    reply.setLastModified(ims) ;
	}
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }
}
