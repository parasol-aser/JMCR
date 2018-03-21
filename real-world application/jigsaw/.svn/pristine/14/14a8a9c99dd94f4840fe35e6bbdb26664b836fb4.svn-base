// CvsFrame.java
// $Id: CvsFrame.java,v 1.1 2010/06/15 12:26:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs2 ;


import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.net.URL;

import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;

import org.w3c.cvs2.CVS;
import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCacheControl;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpTokenList;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.MultipleLockException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;

import org.w3c.jigsaw.html.HtmlGenerator;
import org.w3c.jigsaw.html.HtmlLink;

import org.w3c.tools.sorter.Sorter;

import org.w3c.util.ObservableProperties;

public class CvsFrame extends PostableFrame {

    protected static HttpCacheControl CACHE_CONTROL_NOCACHE = null;
    protected static HttpTokenList    PRAGMA_NOCACHE       = null;

    static {
	// Pre-compute the no cache directives:
	CACHE_CONTROL_NOCACHE = HttpFactory.makeCacheControl();
	CACHE_CONTROL_NOCACHE.setNoCache();
	// Pre-compute the no cache directives:
	String nocache[] = { "no-cache" };
	PRAGMA_NOCACHE = HttpFactory.makeStringList(nocache);
    }

    /**
     * Have we already computed our CVS environment ?
     */
    private static boolean inited   = false ;

    private CvsDirectory        cvs     = null ;
    private CvsHandlerInterface handler = null ;
    private Hashtable           entries = null; //<name, CvsEntryResource>

    private static char alphabet[] = { '0','1','2','3','4','5','6','7','8','9',
				       'A','B','C','D','E','F','G','H','I',
				       'J','K','L','M','N','O','P','Q','R',
				       'S','T','U','V','W','X','Y','Z',
				       'a','b','c','d','e','f','g','h','i',
				       'j','k','l','m','n','o','p','q','r',
				       's','t','u','v','w','x','y','z' };

    protected static void addStyle(HtmlGenerator g) {
	g.addStyle("CAPTION {color: red; font-weight: bold; "+
		   "padding: 3pt; }\n");
	g.addStyle(".warning { color: red; font-weight: bold; }\n");
	g.addStyle("P.error { color: black; font-weight: bold; "+
		   "font-size: large; }\n");
	g.addStyle("A {color: darkblue; font-weight: bold; "+
		   " text-decoration: none; }\n");
	g.addStyle("A.alphalink {color: darkblue; text-decoration: none; "+
		   "font: bold 10pt Verdana, sans-serif; }\n");
	g.addStyle("A.script {color: darkblue; text-decoration: none; "+
		   "font: bold 8pt Verdana, sans-serif; }\n");
	g.addStyle("H1.center {color:white;font-weight:bold;"+
		   "text-align:center; }\n"); 
	g.addStyle("DIV.box { margin: 2.0%; margin-top: 1.0%; padding: 1.0%;"+
		   " border: thin solid black; width: 100.0%; }\n");
	g.addStyle("DIV.boxcenter { margin: 2.0%; margin-top: 1.0%; "+
		   " padding: 1.0%; border: thin solid black; width: 100.0%; "+
		   " text-align: center; } \n");
	g.addStyle("DIV.error{ margin-left: 10.0%; margin-top: 1.0%; "+
		   "padding: 1.0%; width: 80.0%; }\n");
	g.addStyle (".title { color: red; font-weight: bold; padding: 3pt; "+
		    " font-size: large; text-align: center; }\n");
	g.addStyle(".statusok { color: gray; }\n");
	g.addStyle(".status { color: red; font-weight: bold}\n");
	g.addStyle (".titleblack { color: black; font-weight: bold;"+
		    " padding: 3pt; font-size: large; "+
		    "text-align: center; }\n");
    }

    /**
     * Get an HtmlGenerator with some style defined:<br>
     * <strong>tags with style</strong>
     * <ul>
     * <li> BODY 
     * <li> CAPTION
     * <li> P.error
     * <li> A
     * <li> A.alphalink
     * <li> H1.center
     * </ul>
     * <strong>Some DIV</strong>
     * <ul>
     * <li> DIV.box
     * <li> DIV.boxcenter
     * <li> DIV.error
     * </ul>
     * <strong>Some SPAN</strong>
     * <ul>
     * <li> .warning
     * <li> .title
     * <li> .status
     * <li> .statusok
     * <li> .titleblack
     * </ul>
     * @param title The document title.
     * @return a HtmlGenerator instance.
     */ 
    public static HtmlGenerator getHtmlGenerator(String title) {
	HtmlGenerator g = new HtmlGenerator(title);
	g.addStyle("BODY {color: black; background: white; "+
		   "font-family: serif; margin-top: 35px }\n");
	addStyle(g);
	return g;
    }

    /**
     * Like getHtmlGenerator(String), and add the frame style sheet.
     * @param title The document title.
     * @return a HtmlGenerator instance.
     */
    public static HtmlGenerator getHtmlGenerator(CvsFrame cvsframe, 
						 String title) 
    {
	HtmlGenerator g = new HtmlGenerator(title);
	cvsframe.addStyleSheet(g);
	addStyle(g);
	return g;
    }

    /**
     * Emit an HTML error message.
     * @param request The request that trigered the error.
     * @param msg The error message.
     * @param ex The CvsException that happened while processing the request.
     * @return An HTTP reply.
     */
    protected static Reply error(Request request, 
				 String msg, 
				 CvsException ex) 
    {
	return error(request, msg, ex.getMessage());
    }

    /**
     * Emit an HTML error message.
     * @param request The request that trigered the error.
     * @param msg The error message.
     * @return An HTTP reply.
     */
    protected static Reply error(Request request, 
				 String msg, 
				 String details) 
    {
	Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	HtmlGenerator g = getHtmlGenerator(msg);
	g.append ("<center>");
	g.append ("[ <A HREF=\"./CVS\">Back</A> ]<hr noshade width=\"80%\">");
	g.append ("</center>");
	g.append ("<div class=\"error\"> <center><p class=\"error\">", msg,
		  "</center><p>\n");
	g.append ("Details : <p><em>",details,"</em><p></div>\n");
	g.append ("<hr noshade width=\"80%\">");
	error.setStream(g);
	return error;
    }

    /**
     * Emit an HTML error message.
     * @param cvsframe The CvsFrame sending this error.
     * @param request The request that trigered the error.
     * @param msg The error message.
     * @param ex The CvsException that happened while processing the request.
     * @return An HTTP reply.
     */
    protected static Reply error(CvsFrame cvsframe,
				 Request request, 
				 String msg, 
				 CvsException ex)
    {
	return error(cvsframe, request, msg, ex.getMessage());
    }

    /**
     * Emit an HTML error message.
     * @param cvsframe The CvsFrame sending this error
     * @param request The request that trigered the error.
     * @param msg The error message.
     * @return An HTTP reply.
     */
    protected static Reply error(CvsFrame cvsframe,
				 Request request, 
				 String msg, 
				 String details) 
    {
	Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
	HtmlGenerator g = getHtmlGenerator(cvsframe, msg);
	g.append ("<center>");
	g.append ("[ <A HREF=\"./CVS\">Back</A> ]<hr noshade width=\"80%\">");
	g.append ("</center>");
	g.append ("<div class=\"error\"> <center><p class=\"error\">", msg,
		  "</center><p>\n");
	g.append ("Details : <p><em>",details,"</em><p></div>\n");
	g.append ("<hr noshade width=\"80%\">");
	error.setStream(g);
	return error;
    }

    /**
     * Get a suitable FramedResource to display the given cvs'ed file.
     * @param name The name of the file.
     * @return A CvsEntryResource, or <strong>null</strong> if none was
     * found.
     */
    protected FramedResource getResourceFor(String name) {
	CvsEntryResource entry = null;
	if (entries == null) {
	    entries = new Hashtable(3);
	    entry = new CvsEntryResource(getFrameReference(), name);
	    entries.put(name, entry);
	} else {
	    entry = (CvsEntryResource) entries.get(name);
	    if (entry == null) {
		entry = new CvsEntryResource(getFrameReference(), name);
		entries.put(name, entry);
	    }
	}
	return entry;
    }

    private CvsDirectory getManager(File directory) 
	throws CvsException
    {
	return CvsModule.getCvsManager(directory, 
				       getContext(),
				       getServer().getProperties());
    }

    /**
     * Get the CVS manager associated with this resource, or create it.
     */
    protected synchronized CvsDirectory getCvsManager() {
	if (cvs == null) {
	    ResourceReference rrp = resource.getParent();
	    if (rrp != null) {
		try {
		    Resource parent = rrp.lock();
		    if (! (parent instanceof DirectoryResource)) {
			getServer().errlog(resource, 
					"not a child of a DirectoryResource");
			throw new RuntimeException(
					"The server is misconfigured.");
		    }
		    // CVS will only work within a (filesystem) 
		    // directory resource
		    File d = ((DirectoryResource) parent).getDirectory();
		    try {
			cvs = getManager(d);
		    } catch (CvsException ex) {
			String msg = ("unable to create a cvs manager for \""+
				      d.getAbsolutePath()+
				      "\".");
			getServer().errlog(this, msg);
			throw new RuntimeException("CVS failed.");
		    }
		    handler = new CvsDirectoryHandler(cvs);
		} catch (InvalidResourceException ex) {
		    getServer().errlog(resource, "Invalid parent");
		    throw new RuntimeException("The server is misconfigured.");
		} finally {
		    rrp.unlock();
		}
	    }
	}
	return cvs;
    }

    protected boolean isIndexed(String name) {
	ResourceReference rrp = resource.getParent();
	if (rrp != null) {
	    try {
		Resource parent = rrp.lock();
		if (! (parent instanceof DirectoryResource)) {
		    getServer().errlog(resource, 
				       "not a child of a DirectoryResource");
		    throw new RuntimeException("The server is misconfigured.");
		}
		return (((DirectoryResource)parent).lookup(name) != null);
	    } catch (InvalidResourceException ex) {
		getServer().errlog(resource, "Invalid parent");
		throw new RuntimeException("The server is misconfigured.");
	    } finally {
		rrp.unlock();
	    }
	} else {
	    getServer().errlog(resource, "No parent!");
	    throw new RuntimeException("The server is misconfigured.");
	}
    }

    /**
     * Perform the given action on the underlying directory as a whole.
     * @param action The action to perform.
     * @param request The request that triggered the action.
     * @param data The decoded form data.
     * @return A suitable HTTP reply.
     * @exception ProtocolException if a protocol error occurs
     */
    protected Reply performDirectoryAction(String action,
					   Request request,
					   org.w3c.jigsaw.forms.URLDecoder 
					   data)
	throws ProtocolException
    { 
	if ( action.equals("refresh") ) {
	    // Following command will cause a refresh if needed:
	    try {
		getCvsManager().listFiles();
	    } catch (CvsException ex) {
		return error(this, request, 
			     "Error while refreshing directory", ex);
	    }
	} else if ( action.equals("commit") ) {
	    // Commit the whole directory:
	    try {
		String comment = data.getValue("comment");
		String u = (String)request.getState(AuthFilter.STATE_AUTHUSER);
		String env[] = {"USER="+u , "LOGNAME="+u };
		comment = ((comment == null)
			   ? "Changed through Jigsaw."
			   : comment);
		comment = ((u != null) ? ("("+u+") "+comment) : comment);
		getCvsManager().commit(comment,env);
	    } catch (CvsException ex) {
		return error(this, request, 
			     "Error while commiting directory", ex);
	    }
	} else if ( action.equals("update") ) {
	    // Update the whole directory:
	    try {
		getCvsManager().update();
	    } catch (CvsException ex) {
		return error(this, request, 
			     "Error while updating directory", ex);
	    }
	} else {
	    // Unknown directory command:
	    return error(this, request, 
			 "Command not allowed", 
			 "This command is not allowed on directories.");
	}
	return get(request);
    }

    /**
     * Register the resource and add CvsProperties in httpd.
     * @param resource The resource to register.
     */
    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
	if ( ! inited ) {
	    synchronized (this.getClass()) {
		httpd s = (httpd) getServer();
		if ( s != null ) {
		    // Register the CVS property sheet if not done yet:
		    ObservableProperties props = s.getProperties() ;
		    s.registerPropertySet(new CvsProp("cvs", s));
		    inited = true ;
		}
	    }
	}
    }
    /**
     * Lookup method for the CVS manager.
     * Lookup for a cvs entry object having the given name, if found, wrap it
     * into a CvsEntryResource object and return it.
     * @param ls The current lookup state.
     * @param lr The (under construction) lookup result.
     * @exception ProtocolException if a protocol error occurs
     */
    protected boolean lookupOther(LookupState ls, LookupResult lr) 
	throws ProtocolException
    {
	if (super.lookupOther(ls,lr)) 
	    return true;
	String name = ls.getNextComponent();
	try {
	    int status = getCvsManager().getDirectoryStatus(name);
	    if ( status == CVS.DIR_Q ) {
		lr.setTarget(null);
		return true;
	    } else if ( status == CVS.DIR_NCO ) {
		// Checkout directory, and relocate:
		getCvsManager().updateDirectory(name);
		Request request = (Request)ls.getRequest();
		if ( request != null ) {
		    Reply relocate = request.makeReply(HTTP.FOUND);
		    try {
			URL myloc    = getURL(request);
			URL location = new URL(myloc, name+"/CVS");
			relocate.setLocation(location);
			lr.setReply(relocate);
		    } catch (Exception ex) {
		    }
		}
		lr.setTarget(null);
		return true;
	    } else if ( getCvsManager().status(name) != CVS.FILE_Q ) {
		FramedResource target = getResourceFor(name);
		lr.setTarget( target.getResourceReference() );
		target.lookup(ls, lr);
		return true;
	    } else {
		lr.setTarget(null);
		return true;
	    }
	} catch (CvsException ex) {
	    throw new HTTPException("status failed in CVS directory.");
	}
    }

    /**
     * Dump one CVS entry into HTML. 
     * The generated HTML is expected to insert itself in a table.
     * @param g The HTML generator to use.
     * @param name The entry to be dumped.
     * @exception CvsException If the CVS access failed to this entry.
     */
    private void dumpFileEntry (HtmlGenerator g, String name)
	throws CvsException
    {
	String  eurl     = resource.getURLPath() + "/" + name;
	int     status   = getCvsManager().status(name);
	String  revision = getCvsManager().revision(name);
	boolean indexed  = isIndexed(name);

	// Entry toggle:
	g.append("<tr><td><input type=\"checkbox\" name=\""+ name+
		 "\" value=\"mark\">" );
	// Dump the entry name (link only if checked out):
	if (( status == CVS.FILE_NCO ) || (! indexed ))
	    g.append("<td align=left><b>",name,"</b>");
	else
	    g.append("<td align=left><a href=\""+name+"\">"+ name + "</a>\n");
	// Dump the revision number
	if (revision == null)
	    g.append("<td>\n");
	else {
	    int index = revision.indexOf(".");
	    if (index != -1) {
		g.append("<td align=left><select name=\""+name+".rev\">\n");
		String minor = revision.substring(index+1);
		String major = revision.substring(0, index);
		int    max   = 0;
		try {
		    max = Integer.parseInt(minor);
		} catch (NumberFormatException ex) {
		    g.append("<option value=\""+revision+"\">"+revision+
			     "</option>\n");
		    max = 0;
		}
		for (int i = max; i > 0; i--) {
		    g.append("<option value=\""+major+"."+i+"\">"+
			     major+"."+i+"</option>\n");
		}
		g.append("</select>\n");
	    } else {
		g.append("<td>\n");
	    }
	}
	// Dump the entry status:
	g.append ("<td>\n") ;
	if (status != CVS.FILE_OK) {
	    g.append("<span class=\"status\">",
		     getCvsManager().statusToString(status),
		     "</span>");
	} else {
	    g.append("<span class=\"statusok\">",
		     getCvsManager().statusToString(status),
		     "</span>");
	}
	if (( status != CVS.FILE_NCO) && (! indexed))
	    g.append(" (not indexed)");

	// Emit a diff/log hyper-link only if this makes sense (entry is known)
	if ( status != CVS.FILE_Q )
	    g.append ("<td> [ <a href=\""+eurl+"?log\">log</a> ] \n") ;
	if ( status == CVS.FILE_M )
	    g.append ("<td> [ <a href=\""+eurl+"?diff\">diff</a> ] \n") ;
    }

    /**
     * Dump one CVS Directory entry into HTML. 
     * The produced HTML is expected to insert itself into a table.
     * @param g The HTML generator to use.
     * @param name The name of the directory to dumped.
     * @exception CvsException If the CVS access failed to this entry.
     */
    private void dumpDirectoryEntry(HtmlGenerator g, String name)
	throws CvsException
    {
	int    status = getCvsManager().getDirectoryStatus(name);
	String eurl   = name + "/CVS";

	// Dump the toggle:
	g.append("<tr><td><input type=\"checkbox\" name=\"" + name
		 + "\" value=\"mark\">" );
	// Dump the entry name (and link if available):
	if (status == CVS.DIR_NCO)
	    g.append("<td align=left><b>",name,"</b>");
	else 
	    g.append("<td align=left><a href=\""+name+"\">" + name + "</a>\n");
	// Dump more links:
	if (status != CVS.DIR_Q) {
	    if ( status != CVS.DIR_NCO )
		g.append("<td align=left><a href=\""
			 , eurl
			 , "\">CVS</a>\n");
	    else
		g.append("<td align=left>CVS [ <a href=\"" + 
			 resource.getURLPath() + 
			 "/" + name+ "\"> checkout</a> ] \n");
	} else {
	    g.append("<td aligne=left>\n");
	}
	// dump one line for the status:
	g.append ("<td>\n") ;
	if (status == CVS.DIR_CO) {
	    g.append("<span class=\"statusok\">",
		     getCvsManager().statusToString(status),
		     "</span>");
	} else {
	    g.append("<span class=\"status\">",
		     getCvsManager().statusToString(status),
		     "</span>");
	}
    }

    protected void refresh(Request request) 
	throws ProtocolException
    {
	try {
	    getCvsManager().refresh();
	} catch (CvsException ex) {
	    Reply error = 
		error(this, request, "Error while refreshing directory", ex);
	    throw new HTTPException (error) ;
	}
    }

    /**
     * Dump the content of the directory as a CVS form.
     * The resulting form allows for trigerring actions on the various files.
     * @exception ProtocolException if a protocol error occurs
     */
    public Reply get (Request request) 
	throws ProtocolException
    {

	String action    = getAction(request);
	boolean showdirs = false;

	if ( action.equals("refreshfiles") ) {
	    refresh(request);
	} else if ( action.equals("refreshdirs") ) {
	    refresh(request);
	    showdirs = true;
	} else if (action.equals("showdirs")) {
	    showdirs = true;
	}

	boolean no_entries = true;
	HtmlGenerator g = getHtmlGenerator(this, "CVS for "+ 
					   getCvsManager().getDirectory());
	addStyleSheet(g);
	g.addLink(new HtmlLink(null, "made", "jigsaw@w3.org"));

	String tablecolor = "white";
	String parentpath = null;
	ResourceReference rr_parent = resource.getParent();
	try {
	    Resource parent = rr_parent.lock();
	    parentpath = parent.getURLPath();
	} catch (InvalidResourceException ex) {
	    getServer().errlog(resource, "Invalid parent");
	    throw new RuntimeException("The server is misconfigured.");
	} finally {
	    rr_parent.unlock();
	}

	//entries
	boolean nofile   = true;
	boolean nodir    = true;
	Vector  names    = null;
	Vector  dirnames = null;

	//javascript
	g.addScript("JavaScript", "var submitOK = true;\n\n");

	g.addScript("function SelectAll() {\n");
        g.addScript("    len = document.liste.elements.length;\n");
        g.addScript("    var i=0; var checked; var item;\n");
        g.addScript("    if (document.liste.elements[i].checked == true)\n");
        g.addScript("        checked = false;\n");
        g.addScript("    else\n");
        g.addScript("        checked = true;\n");
        g.addScript("    for( i=0; i<len; i++) {\n");
	g.addScript("        item = document.liste.elements[i].name;\n");
	g.addScript("        if (item !='action' && item != 'scope')\n");
        g.addScript("            document.liste.elements[i].checked=checked;");
	g.addScript("\n    }\n");
	g.addScript("}\n\n");

	g.addScript("function confirmDirAction() {\n");
	g.addScript("    len = document.liste.elements.length;\n");
	g.addScript("    var i=0; var item; var oneselected=false;\n");
	g.addScript("    for( i=0; i<len; i++) {\n");
	g.addScript("         item = document.liste.elements[i].name;\n");
	g.addScript("         if (item !='action' && item != 'scope'){\n");
	g.addScript("             if (document.liste.elements[i].checked\n");
	g.addScript("                 == true){\n");
	g.addScript("                 oneselected = true;\n");
	g.addScript("             }\n");
	g.addScript("         }\n");
	g.addScript("    }\n");
	g.addScript("    if (oneselected == false){\n");
	g.addScript("        submitOK = false;\n");
	g.addScript("        alert('No directory selected.');\n");
	g.addScript("    } else {\n");
	g.addScript("        submitOK = true;\n");
	g.addScript("    }\n");
	g.addScript("}\n\n");

	g.addScript("function confirmFileAction() {\n");
	g.addScript("    len = document.liste.elements.length;\n");
	g.addScript("    var i=0; var item; var oneselected=false;\n");
	g.addScript("    if (document.liste.scope[0].checked == true){\n");
	g.addScript("        //Marked files\n");
        g.addScript("        for( i=0; i<len; i++) {\n");
	g.addScript("             item = document.liste.elements[i].name;\n");
	g.addScript("             if (item !='action' && item != 'scope'){\n");
	g.addScript("               if (document.liste.elements[i].checked\n");
	g.addScript("                   == true){\n");
	g.addScript("                   oneselected = true;\n");
	g.addScript("               }\n");
	g.addScript("             }\n");
	g.addScript("        }\n");
	g.addScript("        if (oneselected == false){\n");
	g.addScript("            submitOK = false;\n");
	g.addScript("            alert('No file selected.');\n");
	g.addScript("        }\n");
	g.addScript("    } else if (document.liste.scope[1].checked\n");
	g.addScript("               == true){\n");
	g.addScript("        //Directory\n");
	g.addScript("        if (document.liste.action[1].checked == true\n");
	g.addScript("            ||\n");
	g.addScript("           document.liste.action[2].checked == true){\n");
	g.addScript("           oneselected = true;\n");
	g.addScript("        } else {\n");
	g.addScript("           alert('Command not allowed on directory');\n");
	g.addScript("           oneselected = false;\n");
	g.addScript("        }\n");
	g.addScript("    } else if (document.liste.scope[2].checked\n");
	g.addScript("               == true){\n");
	g.addScript("        //Matching files\n");
	g.addScript("        if (document.liste.regval.value == ''){\n");
	g.addScript("           alert('No regular expression specified.');\n");
	g.addScript("           oneselected = false;\n");
	g.addScript("        } else if (document.liste.action[3].checked "+
		    "== true || document.liste.action[4].checked == true){\n");
	g.addScript("            alert('Command not allowed with regular "+
		    "expressions');\n");
	g.addScript("            oneselected = false;\n");
	g.addScript("        } else {\n");
	g.addScript("            oneselected = true;\n");
	g.addScript("        }\n");
	g.addScript("    } else {\n");
	g.addScript("        oneselected = true;\n");
	g.addScript("    }\n");
	g.addScript("    if (oneselected == false) {\n");
	g.addScript("        submitOK = false;\n");
	g.addScript("    } else if (document.liste.action[4].checked != \n");
	g.addScript("               true){\n");
	g.addScript("        submitOK = true;\n");
        g.addScript("    } else if (confirm('Do you really want to remove "+
		    "these files ?')) {\n");
	g.addScript("        submitOK = true;\n");
        g.addScript("    } else {\n");
	g.addScript("        submitOK = false;\n");
	g.addScript("    }\n");
	g.addScript("}\n\n");

	g.addScript("function accFileSub() {\n");
        g.addScript("    if (submitOK == false) {\n");
        g.addScript("        submitOK = true;\n");
        g.addScript("        return(false);\n");
        g.addScript("    } else {\n");
        g.addScript("        return(true);\n");
	g.addScript("    }\n");
	g.addScript("}\n");

	g.addScript("function accDirSub() {\n");
        g.addScript("    if (submitOK == false) {\n");
        g.addScript("        submitOK = true;\n");
        g.addScript("        return(false);\n");
        g.addScript("    } else {\n");
        g.addScript("        return(true);\n");
	g.addScript("    }\n");
	g.addScript("}\n");

	// Dump all file entries:
	Enumeration gen_enum  = null;
	try {
	    gen_enum = getCvsManager().listFiles() ;
	    names = Sorter.sortStringEnumeration(gen_enum);
	    nofile = (names.size() == 0);
	} catch (CvsException ex) {
	    throw new HTTPException(error(this, request,
					  "unable to list files",ex));
	}

	// get a vector of directory entries
	try {
	    gen_enum = getCvsManager().listDirectories();
	    dirnames = Sorter.sortStringEnumeration(gen_enum);
	    nodir  = (dirnames.size() == 0);
	} catch (CvsException ex) {
	    throw new HTTPException(error(this, request
					  , "unable to list directories"
					  , ex));
	}

	String head = " [ <a href=\"./\">Up to directory</A> ]";
	if ( ! nofile ) {
	    head += " &middot; [ <A HREF=\""+
		resource.getURLPath()+"\">Files</A> ]";
	    if ( ! nodir )
		head += " &middot; [ <A HREF=\""+
		    resource.getURLPath()+
		    "?action=showdirs\">Directories</A> ]";
	    else
		head += " &middot; [ Directories ]";
	} else if ( ! nodir ) {
	    head += " &middot; [ Files ]";
	    head += " &middot; [ <A HREF=\""+
		resource.getURLPath()+
		"?action=showdirs\">Directories</A> ]";
	} else {
	    head += " &middot; [ Files ]";
	    head += " &middot; [ Directories ]";
	}
	head += 
	    " &middot; [ <A HREF=\""+resource.getURLPath()+
	    "?action="+(showdirs? "refreshdirs" : "refreshfiles")+
	    "\">Refresh</A> ]";

	g.append ("<A NAME=\"top\"></A>");
	g.append("<center>",head,"<hr noshade width=\"80%\"></center>");
	g.append("<center><div class=\"boxcenter\">");
	g.append(" <a class=\"alphalink\" href=\"#"+
		 alphabet[0]+"\">"+alphabet[0]+"</a>");
	for (int i = 1; i < alphabet.length; i++) 
	    g.append(" &middot; <a class=\"alphalink\" href=\"#"+alphabet[i]+
		     "\">"+alphabet[i]+"</a>");
	g.append("</div></center>");

	if (!nofile && !showdirs ) {
	    no_entries = false;
	    // now generate the form
	    g.append ("<center>");
	    g.append ("<form OnSubmit=\"return(accFileSub());\" "+
		      "name=\"liste\" action=\"", resource.getURLPath(), 
		      "\" method=\"post\">\n");
	    g.append ("<table width=\"90%\" border=\"0\" cellspacing=\"0\"",
		      "cellpadding=\"0\" nosave>\n") ;
	    g.append ("<caption>FILES in ", parentpath, "</caption>\n");
	    // Dump entries, sorted:
	    int alphaidx = 0;
	    for (int i = 0 ; i < names.size() ; i++) {
		String name  = (String)   names.elementAt(i);
		char   ch    = name.charAt(0);
		if (Character.isLetterOrDigit(ch)) {
		    if ((alphaidx == 0) || (ch != alphabet[alphaidx-1])) {
			char alpha = alphabet[alphaidx];
			while (ch != alpha) {
			    g.append("<a name=\""+alpha+"\"></a>\n");
			    alpha = alphabet[++alphaidx];
			}
			if (ch == alpha) {
			    g.append("<a name=\""+alpha+"\"></a>\n");
			    alphaidx++;
			}
		    }
		}
		try {
		    dumpFileEntry (g, name) ;
		} catch (CvsException ex) {
		    g.append ("<td>" + name + 
			      "<strong>CVS Failed</strong>\n") ;
		}
	    }
	    g.append ("<tr><td colspan=\"2\">");
	    g.append ("</center><a class=\"script\" "+
		      "href=\"javascript:SelectAll()\" "+
		      "onMouseOver=\"window.status="+
		      "'Select / Unselect all files';"+
		      "return true\">All/None</a><center>");
	    g.append ("</td></tr>");
	    g.append ("</table><p>\n") ;

	    // Dump the files command area:
	    g.append ("<hr noshade width=\"40%\">");
	    g.append ("<table cellpadding=\"10\" align=\"center\" "+
		      "width=\"90%\" border=\"0\" cellspacing=\"0\" "+
		      "cellpadding=\"0\">");
	    // add proposed actions
	    g.append ("<tr valign=\"top\"><td>");
	    g.append("<strong>Action:</strong><br>\n");
	    g.append("<input type=\"radio\" name=\"action\" value=\"addcom\">"
		     + " Add into the repository <br>\n");
	    g.append("<input type=\"radio\" name=\"action\" value=\"commit\">"
		     + " Incorporate changes into the repository <br>\n");
	    g.append("<input type=\"radio\" name=\"action\" value=\"update\" "
		     + "checked = \"yes\"> Update <br>\n");
	    g.append("<input type=\"radio\" name=\"action\" value=\"revert\">"
		     + " Revert to selected revision <br>\n");
	    g.append("<input type=\"radio\" name=\"action\" value=\"remove\">"+
		     " Remove from repository <br>\n");
	    g.append("</td>\n<td>\n");
	    // add proposed scopes:
	    g.append("<strong>Perform action on:</strong><br>\n\n");
	    g.append("<input type=\"radio\" name=\"scope\" value=\"mark\"" 
		     + "checked=\"yes\"> Marked files<br>\n");
	    g.append("<input type=\"radio\" name=\"scope\" "
		     + "value=\"directory\"> Directory<br>\n");
	    g.append("<input type=\"radio\" name=\"scope\" value=\"regexp\">"+
		     " Matching files <input type=\"text\" name=\"regval\">"+
		     "<br>\n");
	    g.append("</td></tr>");
	    // comments
	    g.append("<tr><td colspan=2 align=\"center\">\n");
	    g.append ("<p align=\"center\"><strong>Comments for " +
		      "add/remove/commit files<br>\n");
	    g.append ("<textarea wrap=\"soft\" name=\"comment\" rows=\"3\" "+
		      "cols=\"50\">\n") ;
	    g.append ("</textarea></td></tr>\n") ;
	    // and close the table and the first form
	    g.append ("<tr valign=\"top\"><td align=\"center\" " +
		      "colspan=\"2\">\n");
	    g.append ("<input type=\"submit\" name=\"submit\" " +
		      "value=\" Perform Action \" "+
		      "onClick=\"confirmFileAction();\">" );
	    g.append ("</table>");
	    g.append ("</form>\n\n");
	    g.append("</table>");
	    g.append("</center>\n");
	}

	if (!nodir && showdirs) {
	    no_entries = false;
	    // the next one is for stephan
	    g.append("<A NAME=\"dirs\"></A>");
	    // now generate the form
	    g.append ("<center>\n");
	    g.append("<form OnSubmit=\"return(accDirSub());\" name=\"liste\"",
		     " method=\"post\">\n");
	    //g.append("<table width=\"80%\">\n") ;
	    g.append ("<table width=\"90%\" border=\"0\" cellspacing=\"0\"",
		      "cellpadding=\"0\">\n") ;
	    g.append("<caption>SUBDIRECTORIES in ", parentpath, 
		     "</caption>\n");
	    // Dump entries, sorted:
	    int alphaidx = 0;
	    for (int i = 0 ; i < dirnames.size() ; i++) {
		String name = (String) dirnames.elementAt(i);
		char   ch   = name.charAt(0);
		if (Character.isLetterOrDigit(ch)) {
		    if ((alphaidx == 0) || (ch != alphabet[alphaidx-1])) {
			char alpha = alphabet[alphaidx];
			while (ch != alpha) {
			    g.append("<a name=\""+alpha+"\"></a>\n");
			    alpha = alphabet[++alphaidx];
			}
			if (ch == alpha) {
			    g.append("<a name=\""+alpha+"\"></a>\n");
			    alphaidx++;
			}
		    }
		}
		try {
		    dumpDirectoryEntry (g, name) ;
		} catch (CvsException e) {
		    g.append ("<td>" + name + 
			      "<strong>CVS Failed</strong>\n") ;
		}
	    }
	    g.append ("<tr><td colspan=\"2\">");
	    g.append ("</center><a class=\"script\" "+
		      "href=\"javascript:SelectAll()\" "+
		      "onMouseOver=\"window.status="+
		      "'Select / Unselect all files';"+
		      "return true\">All/None</a><center>");
	    g.append ("</td></tr>");
	    g.append ("</table><p>\n") ;
	    // Dump the dirs command area:
	    g.append ("<table align=\"center\" width=\"90%\" width=\"90%\" "+
		      "border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
	    g.append ("<input type=\"hidden\" name=\"action\" value=\"add\">");
	    //hidden scope
	    g.append ("<input type=\"hidden\" name=\"scope\""+
		      " value=\"subdir\">\n");
	    g.append ("<tr><td align=\"center\">",
		      "<input type=\"submit\" name=\"submit\" ",
		      "value=\"Add marked directories\" "+
		      "onClick=\"confirmDirAction();\">\n"+
		      "</td></tr>");
	    // and close the table and the first form
	    g.append ("</table></form>\n\n");
	    g.append("</center>\n");
	}
	if (no_entries) {
	    g.append("<center>No entries.</center><p>\n");
	    //new
	    g.append ("<form name=\"liste\""+
		      "action=\"", resource.getURLPath(), 
		      "\" method=\"post\">\n");
	    g.append ("<table cellpadding=\"10\" align=\"center\" "+
		      "width=\"90%\" border=\"0\" cellspacing=\"0\" "+
		      "cellpadding=\"0\">");
	    // add proposed actions
	    g.append ("<tr valign=\"top\"><td>");
	    g.append("<input type=\"hidden\" name=\"action\" ",
		     "value=\"update\">");
	    // add proposed scopes:
	    g.append("<strong>Perform update on:</strong><br>\n\n");
	    g.append("<input type=\"radio\" name=\"scope\" "
		     + "value=\"directory\"> Directory<br>\n");
	    g.append("<input type=\"radio\" name=\"scope\" value=\"regexp\">"+
		     " File <input type=\"text\" name=\"regval\">"+
		     "<br>\n");
	    g.append("</td></tr>");
	    // and close the table and the first form
	    g.append ("<tr valign=\"top\"><td align=\"center\" " +
		      "colspan=\"2\">\n");
	    g.append ("<input type=\"submit\" name=\"submit\" " +
		      "value=\" Update \">");
	    g.append ("</td></tr></table>");
	    g.append ("</form>\n\n");
	    //end new
	} else {
	    g.append("<a name=\"bottom\"></a>");
	    g.append("<center><hr noshade width=\"80%\">",head,"</center>");
	}

	g.close() ;
	// Send back the reply:
	Reply reply = request.makeReply(HTTP.OK) ;
	reply.setHeaderValue(reply.H_CACHE_CONTROL, CACHE_CONTROL_NOCACHE);
	reply.setHeaderValue(reply.H_PRAGMA, PRAGMA_NOCACHE);
	reply.setStream(g);
	return reply ;
    }

    protected String getAction(Request request) 
	throws HTTPException
    {
	String      query = request.getQueryString() ;
	if (query == null) 
	    return "unknown";
	InputStream in    = new StringBufferInputStream(query) ;
	URLDecoder  d     = new URLDecoder (in, getOverrideFlag()) ;
	try {
	    d.parse () ;
	} catch (URLDecoderException e) {
	    Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
	    error.setContent("Invalid request:unable to decode form data.");
	    throw new HTTPException (error) ;
	} catch (IOException e) {
	    Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
	    error.setContent("Invalid request: unable to read form data.");
	    throw new HTTPException (error) ;
	}

	String action = d.getValue("action") ;
	return (action == null ? "unknown" : action);
    }

    /**
     * This is were we handle the big post request.
     * @exception ProtocolException if a protocol error occurs
     */
    public Reply handle (Request request, org.w3c.jigsaw.forms.URLDecoder data)
	throws ProtocolException
    {
	String action  = data.getValue("action") ;
	String scope   = data.getValue("scope");

	// no action, is a bug in the generated form (see get)
	if ( action == null ) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("No action selected !");
	    throw new HTTPException (error) ;
	}

	// Check action's scope:
	Enumeration gen_enum = null;
	Vector      targets  = new Vector() ;

	if ( scope.equals("directory") ) {
	    // Apply action to the whole directory:
	    return performDirectoryAction(action, request, data);
	} else if ( scope.equals("subdir") ) {
	    // Apply action to the subdirectories
	    // Get the list of targets to act on:
	    try {
		gen_enum = getCvsManager().listDirectories() ;
	    } catch (CvsException ex) {
		error(this, request, "unable to list directories", ex);
	    }
	} else { 
	    // Apply action to the files
	    // Get the list of targets to act on:
	    try {
		gen_enum = getCvsManager().listFiles() ;
	    } catch (CvsException ex) {
		error(this, request, "unable to list files", ex);
	    }
	}

	if ( scope.equals("regexp") ) {
	    // direct perform
	    String regval = (String) data.getValue("regval") ;
	    String comment = (String) data.getValue("comment") ;
	    if (action.equals("remove") || action.equals("revert")) {
		throw new HTTPException(error(this, request,
					      "Can't perform "+action+
					      " with regular expression.",
					      "<center><b>"+regval+
					      "</b></center>"));
	    }
	    // direct perform
	    if ( comment != null )
		handler.perform (request, action, regval, comment);
	    else
		handler.perform (request, action, regval);
	} else {
	    while ( gen_enum.hasMoreElements() ) {
		String name = (String) gen_enum.nextElement();
		if ( data.getValue (name) != null )
		    targets.addElement(name) ;
	    }

	    // Perform that action:
	    int size = targets.size();
	    if( size > 0 ) {
		String names[] = new String[size];
		String revs[]  = new String[size];
		targets.copyInto(names);
		for (int i = 0; i < size; i++ )
		    revs[i] = data.getValue(names[i]+".rev");
		// Perform the comand :
		String comment = data.getValue("comment") ;

		if (action.equals("remove")) {
		    //get the parent resource of our own resource
		    ResourceReference rr = getResource().getParent();
		    try {
			Resource res = rr.lock();
			DirectoryResource dirres = null;
			if (! (res instanceof DirectoryResource)) {
			    getServer().errlog(res, 
			       "CvsFrame: not a child of a DirectoryResource");
			    return error(this, request, 
				 "The server is misconfigured.",
				 "The CVS Directory is not a children of a"+
				 "Directory Resource.");
			}
			dirres = (DirectoryResource) res;
			for (int i=0; i < size; i++ ) {
			    String name = names[i];
			    ResourceReference childref = dirres.lookup(name);
			    if (childref != null) {
				try {
				    Resource children = childref.lock();
				    if (children instanceof FileResource) {
					FileResource fres = 
					    (FileResource) children;
					// delete the file
					fres.getFile().delete();
					// delete the resource
					fres.delete();
				    }
				} catch(MultipleLockException mex) {
				    mex.printStackTrace();
				    return error(this, request,
						 "MultipleLockException: "+
						 mex.getMessage(),
						 "Resource "+name+" in use"+
						 ", can't be deleted now.");
				} catch(InvalidResourceException iex) {
				    iex.printStackTrace();
				    return error(this, request,
						 "InvalidResourceException. ",
						 iex.getMessage());
				} catch (Exception ex) {
				    ex.printStackTrace();
				    return error(this, request,
						 "Exception occurs.",
						 ex.getMessage());
				} finally {
				    childref.unlock();
				}
			    } else {
				//no resource, remove the file only
				File file = new File( dirres.getDirectory(),
						      name );
				file.delete();
			    }
			}
		    } catch(InvalidResourceException ex) {
			ex.printStackTrace();
		    } finally {
			rr.unlock();
		    }
		}

		if ( comment != null )
		    handler.perform (request, action, names, revs, comment);
		else
		    handler.perform (request, action, names, revs);
	    }
	}
	return get(request) ;
    }

}


