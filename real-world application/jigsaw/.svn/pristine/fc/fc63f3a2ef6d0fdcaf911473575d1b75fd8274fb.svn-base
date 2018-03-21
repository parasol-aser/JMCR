// PutListResource.java
// $Id: PutListFrame.java,v 1.1 2010/06/15 12:27:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.filters;

import java.io.File;
import java.io.PrintStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import java.net.URL;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceException;

import org.w3c.tools.resources.ProtocolException;

import org.w3c.cvs2.CVS;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.forms.URLDecoder;

import org.w3c.jigsaw.html.HtmlGenerator;

public class PutListFrame extends PostableFrame {

    PutListResource putlist = null;

    public void registerResource(FramedResource resource) {
	super.registerResource(resource);
	if (resource instanceof PutListResource) {
	    putlist = (PutListResource) resource;
	}
    }

    /**
     * perform the request.
     * @param req the incomming request.
     * @exception org.w3c.tools.resources.ProtocolException if a protocol 
     * error occurs
     * @exception org.w3c.tools.resources.ResourceException if a server 
     * error occurs
     */
    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException
    {
	if (putlist == null) {
	    Request request = (Request) req;
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("The PutListFrame must be associated "+
			     "with a PutListResource only!!");
	    throw new HTTPException(error);
	}
	return super.perform(req);
    }

    protected HtmlGenerator getHtmlGenerator(String title) {
	HtmlGenerator g = new HtmlGenerator(title);
	addStyleSheet(g);
	return g;
    }

    /**
     * Dump the list of modified files.
     * @param request The request to handle.
     * @return A Reply instance.
     * @exception org.w3c.tools.resources.ProtocolException if a protocol 
     * error occurs
     * @exception org.w3c.tools.resources.ResourceException if a server 
     * error occurs
     */

    public Reply get(Request request) 
	throws ProtocolException, ResourceException
    {
	HtmlGenerator g = getHtmlGenerator("Modified files");
	g.append("<h1>List of recently published files</h1>\n");
	Enumeration penum = putlist.getPublishedEntries();
	if (! penum.hasMoreElements())
	    g.append("<center><b>No recently published file</b></center>\n");
	g.append("<ul>\n");
	
	Vector sorted = 
	    org.w3c.tools.sorter.Sorter.sortComparableEnumeration(penum);

	for (int i=0; i < sorted.size(); i++) {
	    PutedEntry e      = (PutedEntry) sorted.elementAt(i);
	    String     url    = e.getURL();
	    g.append("<li><a href=\"",
		     url,
		     "\">"+url+"</a>");
	    g.append("<br>Published by <em>"+e.getAuthor()+"</em> on <strong>",
		     new Date(e.getTime()).toString(),
		     "</strong>.</li>\n");
	}

	g.append("</ul>\n");

	penum = putlist.getEntries();
	if (penum.hasMoreElements()) {
	    g.append("<h1>List of modified files</h1>\n");
	    g.append("<form action=\""+request.getURL()+
		     "\" method=\"POST\">\n");
	    g.append("<dl>\n");
	    // Dump all entries:
	    sorted = 
		org.w3c.tools.sorter.Sorter.sortComparableEnumeration(penum);

	    for (int i=0; i < sorted.size(); i++) {
		PutedEntry e      = (PutedEntry) sorted.elementAt(i);
		String     fname  = e.getFilename();
		String     author = e.getAuthor();
		long       time   = e.getTime();
		String     url    = e.getURL();

		g.append("<dt><input type=\"checkbox\" name=\""+
			 e.getKey() + "\" value =\"mark\">",
			 (fname != null) ? fname : url,
			 "</dt><dd>");
		if ( fname != null ) {
		    File         file    = new File(fname);
		    File         dir     = new File(file.getParent());
		    // Compute the CVS directory URL for the file:
		    URL          cvsurl  = null;
		    try {
			cvsurl  = new URL(new URL(url), "CVS");
		    } catch (Exception ex) {
			cvsurl = null;
		    }
		    // Display status:
		    int st = -1;
		    try {
			// Local status first:
			CvsDirectory cvs = 
			    CvsDirectory.getManager(dir,
						    putlist.props);
			st  = cvs.status(file.getName());
			if ( cvsurl != null )
			    g.append("Status: <a href=\""+cvsurl+ "\">"
				     , cvs.statusToString(st)
				     , "</a><br>");
			else
			    g.append("Status: "
				     , cvs.statusToString(st)
				     , "<br>");

			if (url != null)
			    g.append("URL: <a href=\"",
				     url,
				     "\">"+url+"</a><br>");
	  
		    } catch (CvsException ex) {
			g.append("Status: <strong>CVS ERROR</strong>: "
				 , ex.getMessage()
				 , "<br>");
		    }
		    // Publish status next (when possible)
		    if ( st != CVS.FILE_Q ) {
			try {
			    File         sf  = putlist.getServerFile(file); 
			    File         sd  = new File(sf.getParent());
			    CvsDirectory sc  = 
				CvsDirectory.getManager(sd, putlist.props);
			    int          sst = sc.status(file.getName());
			    if (sst == CVS.FILE_C) {
				g.append("Publish: (needed) <B><U>",
					 sc.statusToString(sst),
					 "</U></B><br>");
			    } else if ((st == CVS.FILE_M) || 
				       (sst != CVS.FILE_OK)) 
				{
				    g.append("Publish: (needed) <em>"
					     , sc.statusToString(sst)
					     , "</em><br>");
				} else {
				    g.append("Publish: <em>"
					     , sc.statusToString(sst)
					     , "</em><br>");
				}
			} catch (CvsException ex) {
			    g.append("Publish: <strong>CVS ERROR</strong>: "
				     , ex.getMessage()
				     , "<br>");
			}
		    }
		}
		// Display author:
		if ( author != null )
		    g.append("Modified by <em>"+author+"</em> on <strong>"+
			     new Date(time).toString() + "</strong>.<br>\n");
		else
		    g.append("Modified on <strong>"+
			     new Date(time).toString()+
			     "</strong>.<br>\n");
	    }
	    g.append("</dl>\n");
	    // The command button:
	    g.append ("<hr noshade width=\"40%\">\n<center>\n",
		      "<table border=\"0\">",
		      "<tr align=\"left\"><td>\n");
	    g.append ("<b>Perform action on marked entries:</b><p>\n") ;
	    g.append ("<input type=\"radio\" name=\"action\" ",
		      "value=\"publish\">Publish<br>\n");
	    g.append ("<input type=\"radio\" name=\"action\" value=\"remove\">"
		      + "Remove \n");
	    g.append ("</p><center>\n") ;
	    g.append ("<input type=\"submit\" name=\"submit\" "+
		      "value=\"Perform Action\">\n") ;
	    g.append ("</center>");
	    g.append ("</form>\n") ;
	    g.append ("</td></tr></table></center>\n");
	}

	penum = putlist.getDelEntries();
	if (penum.hasMoreElements()) {
	    g.append("<h1>List of deleted files</h1>\n");
	    g.append("<form action=\""+request.getURL()+
		     "\" method=\"POST\">\n");
	    g.append("<dl>\n");
	    //Dump only confirmed entries
	    sorted = 
		org.w3c.tools.sorter.Sorter.sortComparableEnumeration(penum);
	    for (int i=0; i < sorted.size(); i++) {
		DeletedEntry e = (DeletedEntry) sorted.elementAt(i);
		if (e.isConfirmed()) {
		    String     fname  = e.getFilename();
		    String     author = e.getAuthor();
		    long       time   = e.getTime();
		    String     url    = e.getURL();
		    g.append("<dt><input type=\"checkbox\" name=\""+
			     e.getKey() + "\" value =\"mark\">",
			     (fname != null) ? fname : url,
			     "</dt><dd>");
		    if (url != null)
			g.append("URL: <b>"+url+"</b><br>");
		    // Display author:
		    if ( author != null )
			g.append("Deleted by <em>"+
				 author+"</em> on <strong>"+
				 new Date(time).toString() +
				 "</strong>.<br>\n");
		    else
			g.append("Deleted on <strong>"+
				 new Date(time).toString()+
				 "</strong>.<br>\n");
		}
	    }
	    g.append("</dl>\n");
	    // The command button:
	    g.append ("<hr noshade width=\"40%\">\n<center>\n",
		      "<table border=\"0\">",
		      "<tr align=\"left\"><td>\n");
	    g.append ("<b>Perform action on marked entries:</b><p>\n") ;
	    g.append ("<input type=\"radio\" name=\"action\" ",
		      "value=\"delete\">Delete<br>\n");
	    g.append ("<input type=\"radio\" name=\"action\" "+
		      "value=\"removedel\">Remove \n");
	    g.append ("</p><center>\n") ;
	    g.append ("<input type=\"submit\" name=\"submit\" "+
		      "value=\"Perform Action\">\n") ;
	    g.append ("</center>");
	    g.append ("</form>\n") ;
	    g.append ("</td></tr></table></center>\n");
	}

	g.append ("<h1>Putlist Configuration</h1>");
	g.append ("<center>");
	g.append ("<table border=\"0\"><tr align=\"left\"><td>");
	g.append ("<form action=\""+request.getURL()+"\" method=\"POST\">\n");
	g.append ("<input type=\"hidden\" name=\"action\" value=\"config\">");
	g.append ("<input type=\"checkbox\"",
		  (putlist.getAutoPublishFlag() ? " CHECKED " : " "),
		  "name=\"autopublish\" value=\"mark\"> Auto Publish<br>");
	g.append ("<input type=\"checkbox\"",
		  (putlist.getAutoDeleteFlag() ? " CHECKED " : " "),
		  "name=\"autodelete\" value=\"mark\"> Auto Delete<br>");
	g.append ("Max published entries <input type=\"text\" "+
		  "size=\"3\" maxlength=\"2\" name=\"mpe\" value=\""+
		  putlist.getMaxPublishedEntryStored()+
		  "\">");
	g.append ("</p><center>");
	g.append ("<input type=\"submit\" name=\"submit\" "+
		  "value=\"Save Configuration\">") ;
	g.append ("</center></form>\n");
	g.append ("</td></tr></table>\n");
	g.append ("</center>");
	g.append ("<hr noshade width=\"80%\">");
	g.close();
	Reply reply = createDefaultReply(request, HTTP.OK);
	reply.addPragma("no-cache");
	reply.setNoCache();
	reply.setStream(g);
	return reply;
    }

    protected void performAction(Request request, String action, String key) 
	throws HTTPException
    {

	if ( action.equals("publish") ) {
	    PutedEntry pe = (PutedEntry) putlist.getEntry(key);
	    if ( pe == null ) {
		// We're in troubles !
		if ( putlist.debug )
		    System.out.println("PutList: "+key+" not found !");
		return ;
	    }
	    if (putlist.publish(pe) == PutListResource.FILE_CF) {
		//send error.
		Reply error = request.makeReply(HTTP.CONFLICT);
		HtmlGenerator gerr = getHtmlGenerator("Warning");
		gerr.append ("<H1>Warning</H1> The file on publish space has "+
			     "been modified directly and attempting to merge"+
			     " has failed.<p>"+
			     "Ask your system administrator.");
		error.setStream(gerr);
		throw new HTTPException(error);
	    }
	} else if ( action.equals("delete") ) {
	    DeletedEntry de = (DeletedEntry) putlist.getDelEntry(key);
	    if (de == null) {
		// We're in troubles !
		if ( putlist.debug )
		    System.out.println("PutList: "+key+" not found !");
		return ;
	    }
	    putlist.delete(de);
	} else if ( action.equals("remove") ) {
	    putlist.removeEntry(key);
	} else if ( action.equals("removedel") ) {
	    putlist.removeDelEntry(key);
	} else if ( putlist.debug ) {
	    System.out.println("PutList: "+action+" unknown.");
	}
    }

    /**
     * handle the request.
     * @param request the incomming request.
     * @param data the URLDecoder.
     * @exception org.w3c.tools.resources.ProtocolException if a protocol 
     * error occurs
     */

    public Reply handle(Request request, org.w3c.jigsaw.forms.URLDecoder data) 
	throws ProtocolException
    {
	// Get the action to perform:
	String action = data.getValue("action");
	if (action == null) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    error.setContent("You must select the action to be performed.");
	    return error;
	}
	if (action.equals("config")) {
	    if (data.getValue("autopublish") != null)
		putlist.setAutoPublish(true);
	    else
		putlist.setAutoPublish(false);

	    if (data.getValue("autodelete") != null)
		putlist.setAutoDelete(true);
	    else
		putlist.setAutoDelete(false);

	    String max = null;
	    if ((max = data.getValue("mpe")) != null) {
		try {
		    putlist.setMaxPublishedEntryStored(Integer.parseInt(max));
		} catch (NumberFormatException ex) {
		    Reply error = request.makeReply(HTTP.BAD_REQUEST);
		    error.setContent("Invalid number : "+max);
		    return error;
		}
	    }
	} else {
	    Enumeration genum = null;
	    if (action.equals("publish") || action.equals("remove"))
		genum = putlist.getEntriesKeys();
	    else
		genum = putlist.getDelEntriesKeys();
	    // Check all entries and perform action:
	    while ( genum.hasMoreElements() ) {
		String key = (String) genum.nextElement();
		if (data.getValue(key) != null) {
		    // Perform action on that entry:
		    if ( putlist.debug )
			System.out.println("PutList: "+action+" on "+key);
		    performAction(request, action, key);
		} else {
		    if ( putlist.debug )
			System.out.println("PutList: "+key+" not marked !");
		}
	    }
	} 
	try {
	    return get(request);
	} catch (ResourceException ex) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	    error.setContent(ex.getMessage());
	    return error;
	}
    }
} // PutListFrame
