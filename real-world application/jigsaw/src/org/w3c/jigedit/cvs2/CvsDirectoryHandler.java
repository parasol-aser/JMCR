// CvsDirectoryHandler.java
// $Id: CvsDirectoryHandler.java,v 1.1 2010/06/15 12:26:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

//
// FIXME add extra environment parameter to all public methods
// witch run cvs.
//

package org.w3c.jigedit.cvs2 ;

import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.jigsaw.html.HtmlGenerator ;

public class CvsDirectoryHandler implements CvsHandlerInterface {
    private static final boolean debug = false;

    CvsDirectory cvs = null ;

    protected Reply badAction(Request request, String action) {
	Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
	HtmlGenerator g = CvsFrame.getHtmlGenerator ("Bad CVS command") ;
	g.append ("<center>");
	g.append ("[ <A HREF=\"./CVS\">Back</A> ]<hr noshade width=\"80%\">");
	g.append ("<p>Your command "
		  + "<strong>"+ action + "</strong>"
		  + " wasn't undesrtood.<p>");
	g.append ("<hr noshade width=\"80%\"></center>");
	error.setStream (g) ;
	return error;
    }

    /**
     * Perform the action on the entity matching the given regexp
     * @param request The request that triggered this method call.
     * @param action The action to perform.
     * @param regexp The regular expression to match
     * @param comment A string of comments describing the change.
     * @exception ProtocolException If running the action failed.
     */
    public void perform (Request request, String action, 
			 String regexp, String comment)
	throws ProtocolException
    {
	try {
	    String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
	    String env[] = {"USER="+u , "LOGNAME="+u };
	    if ( action.equals ("add") ) {
		cvs.addRegexp(regexp, env);
	    } else if ( action.equals ("update") ) {
		cvs.updateRegexp(regexp);
	    } else if ( action.equals("commit") ) {
		String jcomment = null;
		if (comment != null) {
		    jcomment = ((u!=null) ? "("+u+") "+comment : comment);
		} else {
		    jcomment = ((u != null)
				? "("+u+") changed through Jigsaw, no comments"
				: "changed through Jigsaw, no comments");
		}
		cvs.commitRegexp(regexp, jcomment, env);
	    } else if (action.equals("addcom")) {
		cvs.addRegexp(regexp, env);
		String jcomment = null;
		if (comment != null) {
		    jcomment = ((u!=null) ? "("+u+") "+comment : comment);
		} else {
		    jcomment = ((u != null)
				? "("+u+") changed through Jigsaw, no comments"
				: "changed through Jigsaw, no comments");
		}
		cvs.commitRegexp(regexp, jcomment, env);
	    } else {
		throw new HTTPException (badAction(request, action)) ;
	    }
	} catch (CvsException ex) {
	    String msg = action+" in "+cvs.getDirectory()+" failed";
	    throw new HTTPException(CvsFrame.error(request, msg, ex));
	}
    }

    /**
     * Perform the action on the entity matching the given regexp
     * @param request The request that triggered this method call.
     * @param action The action to perform.
     * @param regexp The regular expression to match
     * @exception ProtocolException If running the action failed.
     */
    public void perform (Request request, String action, String regexp)
	throws ProtocolException
    {
	perform(request, action, regexp, null);
    }

    /**
     * Perform the action on the given entity.
     * @param request The request that triggered this method call.
     * @param action The action to perform.
     * @param names Name of files to apply the action to.
     * @exception ProtocolException If running the action failed.
     */

    public void perform (Request request, String action, 
			 String names[], String revs[])
	throws ProtocolException
    {
	perform(request, action, names, revs, null);
    }

    /**
     * Perform the action on the given entity.
     * @param action The action to perform.
     * @param names The names on which the action should be performed.
     * @param comment A string of comments describing the change.
     * @exception ProtocolException If running the action failed.
     */

    public void perform (Request request, 
			 String action, 
			 String names[],
			 String revs[],
			 String comment) 
	throws ProtocolException
    {
	String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
	String env[] = {"USER="+u , "LOGNAME="+u };
	String jcomment = null;
	if (comment != null) {
	    jcomment = ((u!=null) ? "("+u+") "+comment : comment);
	} else {
	    jcomment = ((u != null)
			? "("+u+") changed through Jigsaw, no comments"
			: "changed through Jigsaw, no comments");
	}

	if (debug)
	    for(int i = 0 ; i < names.length ; i++)
		System.out.println("*** perform "+action+" on "+names[i]);
	try {
	    if ( action.equals ("add") ) {
		cvs.add(names, env) ;
	    } else if ( action.equals ("revert") ) {
		String msg = ((u!=null) ? "("+u+") "+comment : comment);
		for (int i=0; i<names.length; i++)
		    cvs.revert(names[i], revs[i], msg, env);
	    } else if ( action.equals ("remove") ) {
		cvs.remove(names, jcomment, env);
	    } else if ( action.equals("update") ) {
		cvs.update(names) ;
	    } else if ( action.equals ("commit") ) {
		cvs.commit(names, jcomment, env);
	    } else if ( action.equals ("addcom") ) {
		cvs.add(names, env) ;
		cvs.commit(names, jcomment, env);
	    } else {
		throw new HTTPException (badAction(request, action)) ;
	    }
	} catch (CvsException ex) {
	    String msg = action+" in "+cvs.getDirectory()+" failed";
	    throw new HTTPException(CvsFrame.error(request, msg, ex));
	}
    }

    CvsDirectoryHandler(CvsDirectory cvs) {
	super() ;
	this.cvs = cvs ;
    }

}
