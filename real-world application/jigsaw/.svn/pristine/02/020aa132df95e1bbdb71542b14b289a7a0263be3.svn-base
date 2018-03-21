// CvsDAVFileFrame.java
// $Id: CvsDAVDirectoryFrame.java,v 1.2 2010/06/15 17:53:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.webdav.frames ;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.webdav.DAVFrame;
import org.w3c.jigsaw.webdav.DAVRequest;
import org.w3c.www.webdav.WEBDAV;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.www.mime.MimeType;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.cvs2.CVS;
import org.w3c.cvs2.CvsAddException;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;
import org.w3c.cvs2.UpToDateCheckFailedException;

import org.w3c.jigedit.cvs2.CvsFrame;
import org.w3c.jigedit.cvs2.CvsModule;

/**
 * This subclass of HTTPFrame check cvs before performing a PUT request.
 * If a CVS directory exists<BR>
 * <ul>
 * If the resource file exists<BR>
 * <ul> 
 * If resource file not up to date Fail.<BR>
 * Else perform PUT and commit it into cvs.<BR>
 * </ul>
 * Else perform PUT, add and commit it into cvs.<BR>
 * </ul>
 * Else perform PUT.
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class CvsDAVDirectoryFrame extends DAVFrame {

    public static final boolean debug = true;

    /**
     * Attribute index, tell if we must update the resource everytime it is
     * acceded (not recommended as it generates many cvs commands)
     */
    private static int ATTR_AUTOUPDATE = -1;

    static {
	Attribute   a = null ;
	Class     cls = null;

	try {
	    cls = Class.forName("org.w3c.jigedit.webdav.frames"
				+".CvsDAVDirectoryFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The browsable flag:
	a = new BooleanAttribute("autoupdate",
				 Boolean.FALSE,
				 Attribute.EDITABLE) ;
	ATTR_AUTOUPDATE = AttributeRegistry.registerAttribute(cls, a) ;
    }

    protected static Reply error(Request request,
				 int status,
				 String title,
				 String msg) 
    {
	Reply error = request.makeReply(status);
	HtmlGenerator g = CvsFrame.getHtmlGenerator(title);
	g.append("<span class=\"title\">",title,"</span>\n");
	g.append("<p>",msg);
	error.setStream(g);
	return error;
    }

    protected File resDirectory = null;

    protected File getResourceDirectory() {
	if (resDirectory == null) {
	    FramedResource fr = getResource();
	    if (fr instanceof DirectoryResource) {
		resDirectory = ((DirectoryResource) fr).getDirectory();
	    }
	}
	return resDirectory;
    }
    /**
     * tell if we must always do an update.
     */
    public boolean isAutoUpdatable() {
	return getBoolean(ATTR_AUTOUPDATE, false);
    }


    protected synchronized CvsDirectory getCvsManager() 
	throws CvsException
    {
	return CvsModule.getCvsManager(getResourceDirectory(), 
				       getContext(),
				       getServer().getProperties());
    }

    protected boolean checkCvsManager() {
	try {
	    return (getCvsManager() != null);
	} catch (CvsException ex) {
	    return false;
	}
    }

    /**
     * @exception CvsException if the CVS process failed
     */
    protected void add(Request request) 
	throws CvsException
    {
	String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
	String env[] = {"USER="+u , "LOGNAME="+u };
	String names [] = null;
	MimeType mtype;
        mtype = request.getContentType();
	if (mtype != null && 
	    (mtype.match(MimeType.TEXT) != MimeType.MATCH_SUBTYPE) &&
	    (mtype.match(MimeType.APPLICATION_XHTML_XML) != 
	                    MimeType.MATCH_SPECIFIC_SUBTYPE)) {
	    names = new String[2];
	    names[0] = "-kb";
	    names[1] = getFileResource().getFile().getName();
	} else {
	    names = new String[1];
	    names[0] = getFileResource().getFile().getName();
	}
	CvsDirectory cvsdir = null;
	cvsdir = getCvsManager();
	cvsdir.add(names, env);
    }

    /**
     * @exception CvsException if the CVS process failed
     */
    protected void commit(Request request) 
	throws CvsException
    {
	commit (request, "Changed through Jigsaw.");
    }

    /**
     * @exception CvsException if the CVS process failed
     */
    protected void commit(Request request, String msg) 
	throws CvsException
    {
	String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
	String env[] = {"USER="+u , "LOGNAME="+u };
	String comment = "("+u+") "+msg;
	CvsDirectory cvsdir = null;
	cvsdir = getCvsManager();
	cvsdir.commit(getFileResource().getFile().getName(), comment, env);
    }

    /**
     * @exception CvsException if the CVS process failed
     */
    protected void update()
	throws CvsException
    {
	CvsDirectory cvsdir = getCvsManager();
	cvsdir.update(getFileResource().getFile().getName());
    }

    /**
     * @exception CvsException if the CVS process failed
     */
    protected int status()
	throws CvsException
    {
	CvsDirectory cvsdir = getCvsManager();
	return cvsdir.status(getFileResource().getFile().getName());
    }

    protected String statusToString(int status) {
	return CvsDirectory.statusToString(status);
    }

    /**
     * Handle the MKCOL request.
     * @param request the WEBDAV request
     * @return a Reply instance
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */
    public Reply mkcol(DAVRequest request)
	throws ProtocolException, ResourceException
    {
	Reply rep = super.mkcol(request);
	if (rep.getStatus() == HTTP.CREATED) {
	    // we created a new dir, time to add it in cvs
	    String names[] = new String[1];
	    // as the result is HTTP.CREATED, we know for sure that dresource
	    // exists, no need to check.
	    String newcol = (String) request.getState(REMAINING_PATH);
	    names[0] = newcol;
	    try {
		getCvsManager().add(names);
	    } catch (CvsException ex) {
		getServer().errlog(this, ex.getMessage());
	    }
	}
	return rep;
    }

    /**
     * The WEBDAV DELETE method, actually the resource (file, directory)
     * is moved into the trash directory which is not accessible via HTTP.
     * @param request The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public Reply delete(Request request)
	throws ProtocolException, ResourceException
    {
	/**
	 * We are currently refusing to handle direct DELETE requests (but
	 * honor internal requests following other actions, like a MOVE
	 * because is is quite dangerous with graphical WebDAV clients
	 * and because there is no clean way to remove directories from a CVS
	 * repository 
	 */
	String method = request.getMethod();
	if (method.equals("DELETE")) {
	    Reply error = request.makeReply(HTTP.FORBIDDEN);
	    error.setContent("DELETE is forbidden on CVS controlled"+
			     " directories in this version of JigEdit");
	    throw new HTTPException (error);
	} 
	return super.delete(request);
    }
    
    private void updateStates(DAVRequest request) {
	if (getCurrentLockDepth() == WEBDAV.DEPTH_INFINITY) { 
	    // propagate the lock
	    request.setState(LOCKED_REREFENCE, getResourceReference());
	    request.setState(LOCK_OWNER, getCurrentLockOwner(null));
	    request.setState(LOCK_TOKEN, getCurrentLockToken(null));
	    request.setState(LOCK_USERNAME, getCurrentLockUsername(null));
	    request.setState(LOCK_EXPIRE, 
			     new Long(getTokenExpirationDate(null)));
	    request.setState(LOCK_TIMEOUT, getValue(ATTR_LOCK_TIMEOUT, 
						    DEFAULT_LOCK_TIMEOUT));
	}
    }

    /**
     * Lookup the target resource when associated with a DirectoryResource.
     * @param ls The current lookup state
     * @param lr The result
     * @return true if lookup is done.
     * @exception ProtocolException If an error relative to the protocol
     * occurs
     */
    protected boolean lookupDirectory(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	// handle PUT and MKCOL
	// refresh the timeout value
	DAVRequest request = (DAVRequest) ls.getRequest();
	if (request == null) {
	    return super.lookupDirectory(ls, lr);
	}
	if (isLocked(null)) {
	    updateStates(request);
	}
	if (ls.hasMoreComponents()) {
	    if (request.getMethod().equals("PUT")) {
		String            name = ls.peekNextComponent() ;
		ResourceReference rr   = dresource.lookup(name);
		if ((rr == null) && 
		    dresource.getExtensibleFlag() &&
		    getPutableFlag()) { 
		    // the resource doesn't exists
		    if (ls.countRemainingComponents() == 1) {
			rr = dresource.createResource(name, request);
		    } else {
			rr = dresource.createDirectoryResource(name);
		    }
		    if (rr == null) {
			Reply error = 
			    request.makeReply(HTTP.UNSUPPORTED_MEDIA_TYPE);
			error.setContent(
			    "Failed to create resource "+
			    name +" : "+
			    "Unable to create the appropriate file:"+
			    request.getURLPath()+
			    " this media type is not supported");
			throw new HTTPException (error);
		    }
		} else if (rr == null) {
		    Reply error = request.makeReply(HTTP.FORBIDDEN) ;
		    error.setContent("You are not allowed to create resource "+
				     name +" : "+
				     dresource.getIdentifier()+
				     " is not extensible.");
		    throw new HTTPException (error);
		}
	    } else if (request.getMethod().equals("MKCOL")) {
		String            name = ls.peekNextComponent() ;
		ResourceReference rr   = dresource.lookup(name);
		if (rr == null) {
		    if (ls.countRemainingComponents() == 1) {
			request.setState(REMAINING_PATH, name);
			return true;
		    } else {
			Reply error = request.makeReply(HTTP.CONFLICT) ;
			error.setContent("Can't create "+
					 ls.getRemainingPath(true));
			throw new HTTPException (error);
		    }
		}
	    }
	}
	// normal lookup
	if ( super.lookupOther(ls, lr) ) {
	    if ( ! ls.isDirectory() && ! ls.isInternal() ) {
		// The directory lookup URL doesn't end with a slash:
		if ( request == null ) {
		    lr.setTarget(null);
		    return true;
		} else if (! acceptRedirect(request)) {
		    return true;
		}
		URL url = null;
		try {
		    if ((request != null ) && 
			request.hasState(Request.ORIG_URL_STATE)) {
			URL oldurl;
			oldurl = (URL)request.getState(Request.ORIG_URL_STATE);
			url = new URL(oldurl, oldurl.getFile() + "/");
		    } else {
			url = (ls.hasRequest() 
			       ? getURL(request)
			       : new URL(getServer().getURL(), 
					 resource.getURLPath()));
		    }
		} catch (MalformedURLException ex) {
		    getServer().errlog(this, "unable to build full URL.");
		    throw new HTTPException("Internal server error");
		}
		String msg = "Invalid requested URL: the directory resource "+
		    " you are trying to reach is available only through "+
		    " its full URL: <a href=\""+
		    url + "\">" + url + "</a>.";
		if ( getRelocateFlag() ) {
		    // Emit an error (with reloc if allowed)
		    Reply reloc = request.makeReply(HTTP.FOUND);
		    reloc.setContent(msg) ;
		    reloc.setLocation(url);
		    lr.setTarget(null);
		    lr.setReply(reloc);
		    return true;
		} else {
		    Reply error = request.makeReply(HTTP.NOT_FOUND) ;
		    error.setContent(msg) ;
		    lr.setTarget(null);
		    lr.setReply(error);
		    return true;
		}
	    } else if ( ! ls.isInternal() && acceptRedirect(request) ) {
		request.setState(STATE_CONTENT_LOCATION, "true");
		// return the index file.
		String indexes[] = getIndexes();
		if (indexes != null) {
		    for (int i = 0 ; i < indexes.length ; i++) {
			String index = indexes[i];
			if ( index != null && index.length() > 0) {
			    DirectoryResource dir = 
				(DirectoryResource) resource;
			    ResourceReference rr = dir.lookup(index);
			    if (rr != null) {
				try {
				    FramedResource rindex = 
					(FramedResource) rr.lock();
				    return rindex.lookup(ls,lr);
				} catch (InvalidResourceException ex) {
				} finally {
				    rr.unlock();
				}
			    }
			}
		    }	
		}
	    }
	    return true;
	}
	return false;
    }

}
