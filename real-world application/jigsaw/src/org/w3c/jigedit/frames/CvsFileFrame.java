// CvsFileFrame.java
// $Id: CvsFileFrame.java,v 1.2 2010/06/15 17:53:11 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.frames ;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityTag;
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

public class CvsFileFrame extends HTTPFrame {

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
	    cls = 
	      Class.forName("org.w3c.jigedit.frames.CvsFileFrame") ;
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

    /**
     * tell if we must always do an update.
     */
    public boolean isAutoUpdatable() {
	return getBoolean(ATTR_AUTOUPDATE, false);
    }

    protected File resDirectory = null;

    protected synchronized File getResourceDirectory() {
	if (resDirectory == null) {
	    ResourceReference rr = getFileResource().getParent();
	    ResourceReference rrtemp = null;
	    Resource p = null;
	    while ( true ) {
		try {
		    if (rr == null)
			return null;
		    p = rr.lock();
		    if (p instanceof DirectoryResource) {
			resDirectory = ((DirectoryResource)p).getDirectory();
			break;
		    }
		    rrtemp = p.getParent();
		} catch (InvalidResourceException ex) {
		    return null;
		} finally {
		    if (rr != null)
			rr.unlock();
		}
		rr = rrtemp;
	    }
	}
	return resDirectory;
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
     * Get this resource Etag string, it will be computed using
     * FileETag MTime Size directive like in Apache, this will fit some
     * needs for our own server farm, it won't hurt anyway/
     * @return a string or null if not applicable
     */ 
    public String computeETag() {
	String etag_s = null;
	if (fresource != null) {
	    long lstamp = fresource.getFileStamp();
	    long lsize = (long) fresource.getFileLength();
	    if ( lstamp >= 0L ) {
		etag_s = Long.toHexString(lsize) + "-" 
		    + Long.toHexString(lstamp);
	    }
	}
	return etag_s;
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

    protected File getBackupFile() {
	File file = getFileResource().getFile();
	return new File(file.getParent(), file.getName()+".bak");
    }

    /**
     * Change the content of the associated FileResource.
     * +cvs action (commit, update)
     * @param request The incomming request.
     * @exception org.w3c.tools.resources.ProtocolException 
     * if a protocol error occurs
     * @exception org.w3c.tools.resources.ResourceException 
     * if a server error occurs
     */
    protected Reply putFileResource(Request request)
	throws ProtocolException, ResourceException
    {
	if (debug)
	    System.out.println("Put on "+getResource().getIdentifier()+"...");
	if (getResourceDirectory() != null) {
	    if (checkCvsManager()) {
		File resfile = getFileResource().getFile();
		int cvs_status = -1;
		try {
		    cvs_status = status();
		} catch (CvsException ex) {
		    //file not on disk yet, let's try to add this file..
		    cvs_status = CVS.FILE_Q;
		}
		if (debug)
		    System.out.println("... with a cvs status equals to : "+
				       statusToString(cvs_status));
		if (cvs_status == CVS.FILE_C) {
		    String msg = "Conflict between working "+
			"revision and repository revision.<br> "+
			"Please update the file manually or use the CVS form.";
		    return error( request,
				  HTTP.CONFLICT,
				  "Conflict",
				  msg );
		} else if (cvs_status == CVS.FILE_NCO) {
		    String msg = "File already in the repository "+
			"(added independently by second party).<br>"+
			"Please update the file manually or use the CVS form.";
		    return error( request,
				  HTTP.CONFLICT,
				  "Conflict",
				  msg );
		} else if (resfile.exists() && (cvs_status != CVS.FILE_Q)) {
		    File backup = getBackupFile();
		    try {
			if (cvs_status ==  CVS.FILE_OK) {
			    // Write the file to web space
			    //save the current file
			    try {
				org.w3c.util.IO.copy(resfile, backup);
			    } catch (IOException ex) {
				//not exactly what we want but, it ~works
				backup.delete();
				resfile.renameTo(backup);
			    }
			    //perform the put
			    Reply r = super.putFileResource(request);
			    int rstatus = r.getStatus();
			    // if OK
			    if ((rstatus / 100 ) == 2 ) {
				// Do a CVS commit with user name
				commit(request);
			    }
			    //well done, remove the backup file now
			    backup.delete();
			    // update the file attrs
			    getFileResource().checkContent();
			    //return the reply.
			    if ((rstatus / 100 ) == 2 ) {
				return createDefaultReply(request,
							  r.getStatus());
			    } else {
				return r;
			    }
			} else {
			    // fail
			    String msg = 
				"File is not up to date, "+
				"please update the file manually "+
				"or use the CVS form.";
			    return error( request, 
					  HTTP.CONFLICT,
					  "Error",
					  msg );
			}
		    } catch (UpToDateCheckFailedException utd_ex) {
			resfile.delete();
			//restore the backup file
			backup.renameTo(resfile);
			//send error
			String msg =  utd_ex.getFilename()+
			    " is not up to date, "+
			    "please update the file manually "+
			    "or use the CVS form.";
			return error( request,
				      HTTP.CONFLICT,
				      "Error",
				      msg );
		    } catch (CvsException ex) {
			backup.delete();
			// fail too 
			String msg = "CvsException : "+ex.getMessage();
			getServer().errlog(getIdentifier()+" : "
					   +ex.getMessage());
			return error( request, 
				      HTTP.SERVICE_UNAVAILABLE,
				      "Error",
				      msg );
		    }
		} else {
		    // Write the file to web space
		    Reply r = super.putFileResource(request);
		    try {
			//  Do a CVS add with user name
			add(request);
			//  Do a CVS commit with user name
			commit(request);
			getFileResource().checkContent();
		    } catch (CvsAddException ex) {
			String msg = "Cvs add failed : <br>'"+
			    ex.getMessage()+
			    "'<br>please update the file manually "+
			    "or use the CVS form";
			return error( request, 
				      HTTP.SERVICE_UNAVAILABLE,
				      "Error",
				      msg );
		    } catch (CvsException ex) {
			ex.printStackTrace();
			String msg = "Problem during cvs process : "+
			    ex.getMessage();
			getServer().errlog(getIdentifier()+" : "+msg);
			return error( request, 
				      HTTP.INTERNAL_SERVER_ERROR,
				      "Internal Server Error",
				      msg );
		    }
		    if (r.getStatus() == HTTP.CREATED) {
			Reply reply = request.makeReply(r.getStatus());
			reply.setContent("<P>Resource succesfully created");
			if (request.hasState(STATE_CONTENT_LOCATION))
			    reply.setContentLocation(
				getURL(request).toExternalForm());
			r = createDefaultReply(request, HTTP.CREATED);
			reply.setETag(r.getETag());
			return reply;
		    }
		    return createDefaultReply(request, r.getStatus());
		}
	    } else {
		return super.putFileResource(request);
	    }
	} else {
	    String msg = "Server misconfigured : "+
		"unable to find resource directory";
	    getServer().errlog(getIdentifier()+" : "+msg);
	    return error( request, 
			  HTTP.INTERNAL_SERVER_ERROR,
			  "Internal Server Error",
			  msg );
	}
    }

    /**
     * The DELETE method delete the file and perform a cvs remove.
     * @param request The request to handle.
     * @exception ProtocolException if a protocol error occurs 
     * @exception ResourceException If the resource got a fatal error.
     */

    protected Reply deleteFileResource(Request request)
	throws ProtocolException, ResourceException
    {
	//Get the user name
	String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
	String env[] = {"USER="+u , "LOGNAME="+u };
	//delete the file
	fresource.getFile().delete();
	//cvs remove
	String names[] = new String[1];
	
	names[0] = fresource.getFilename();
	if (names[0] == null)
	    names[0] = fresource.getIdentifier();
	try {
	    if (getCvsManager().status(names[0]) != CVS.FILE_Q) {
		getCvsManager().remove(names, 
				       "Deleted via HTTP delete method.", 
				       env);
	    }
	} catch (CvsException cvs_ex) {
	    String msg="Cvs remove failed : "+cvs_ex.getMessage();
	    getServer().errlog(this, msg);
	}
	try {
	    //delete the FileResource
	    fresource.delete();
	} catch (MultipleLockException ex) {
	    String msg = "Can't delete resource: "+resource.getIdentifier()+
		" is locked. Try again later.";
	    Reply error = error( request, 
				 HTTP.FORBIDDEN,
				 "Error",
				 msg );
	    throw new HTTPException(error);
	}
	//well done
	return request.makeReply(HTTP.NO_CONTENT) ;
    }

    /**
     * Perform a cvs update before perform a GET,HEAD,POST request.
     * @param req The request to handle.
     * @exception ProtocolException If processsing the request failed.
     * @exception ResourceException If the resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	if (! checkRequest(req))
	    return null;

	String method = ((Request)req).getMethod () ;
	if (( method.equals("GET") ||  method.equals("HEAD") ||
	      method.equals("POST") ||  method.equals("PUT")) && 
	    isAutoUpdatable() && (fresource != null)) {
	    
	    try {
		update();
	    } catch (CvsException ex) {
		String msg = "cvs update \""+
		    getFileResource().getFile().getName()+
		    "\" failed.";
		getServer().errlog(this, msg);
	    }
	}
	return super.perform(req);
    }
}
