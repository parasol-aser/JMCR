// CvsEntryResource.java
// $Id: CvsEntryResource.java,v 1.2 2010/06/15 17:53:13 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigedit.cvs2 ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

import java.util.Hashtable;

import org.w3c.cvs2.CvsDirectory;
import org.w3c.cvs2.CvsException;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpInteger;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;

import org.w3c.jigsaw.forms.URLDecoder;
import org.w3c.jigsaw.forms.URLDecoderException;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.ContainerResource;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.Utils;

import org.w3c.jigsaw.html.HtmlGenerator ;

/**
 * This class exports the state of an entry. For the time being it doesn't
 * do much, but it should act as a directory (container) for all revisions
 * of a document. 
 * <p>The lookup method of this entity should be able to turn a revision number
 * into an editable (or viewable) document.
 */

public class CvsEntryResource extends ContainerResource {

    protected String                 name        = null;
    protected ResourceReference      rr_cvsframe = null;

    static byte startAnchor[] = { (byte)'<',(byte)'a',(byte)' ',(byte)'h',
				  (byte)'r',(byte)'e',(byte)'f',(byte)'=',
				  (byte)'"' };
    static byte midAnchor[] = { (byte)'"',(byte)'>' };

    static byte endAnchor[] = { (byte)'<',(byte)'/',(byte)'a',(byte)'>' };

    static byte edittext [] = { (byte)'e',(byte)'d',(byte)'i',(byte)'t',
				(byte)' ',(byte)'l',(byte)'o',(byte)'g' };

    static byte pattern[] = { (byte)'r',(byte)'e',(byte)'v',(byte)'i',
			      (byte)'s',(byte)'i',(byte)'o',(byte)'n' };

    static byte increments[] = new byte[128] ;

    static {
	for(int i=0;i<128;i++) {
	    increments[i] = 8 ;
	}

	increments[(int)'r'] = 7;
	increments[(int)'e'] = 6;
	increments[(int)'v'] = 5;
	increments[(int)'i'] = 2;
	increments[(int)'s'] = 3;
	increments[(int)'o'] = 1;
    }

    class RevisionNumberException extends Exception {

	RevisionNumberException (String message) {
	    super(message);
	}

    }

    class RevisionResource extends FramedResource {
	
	ResourceReference rr = null;
	
	public ResourceReference getResourceReference() {
	    if (rr == null)
		rr = new DummyResourceReference(this);
	    return rr;
	}

	RevisionResource(String revision) {
	    registerFrame( new RevisionFrame(revision), new Hashtable(3));
	}
    }

    class RevisionFrame extends HTTPFrame {
	
	String revision = null;

	protected synchronized void updateHeaders() {
	    try {
		CvsFrame cvsframe = (CvsFrame) rr_cvsframe.lock();
		Resource res      = cvsframe.getResource();
		ResourceReference rr_dir = res.getParent();
		try {
		    Resource parent = rr_dir.lock();
		    if (parent instanceof ContainerResource) {
			ContainerResource dir = (ContainerResource) parent;
			ResourceReference rr_res = dir.lookup(name);
			if (rr_res == null) {
			    this.setValue(ATTR_CONTENT_TYPE,
				     org.w3c.www.mime.Utils.getMimeType(name));
			    return;
			}
			try {
			    res = rr_res.lock();
			    if (res instanceof FileResource) {
				HTTPFrame httpFrame = (HTTPFrame)
				    res.getFrame(
					 Class.forName(
					   "org.w3c.jigsaw.frames.HTTPFrame"));
			    //Added by Jeff Huang
			    //TODO: FIXIT
				this.setValue(ATTR_CONTENT_LANGUAGE,
					      httpFrame.getContentLanguage());
				this.setValue(ATTR_CONTENT_ENCODING,
					      httpFrame.getContentEncoding());
				this.setValue(ATTR_CONTENT_TYPE,
					      httpFrame.getContentType());
			    }
			} catch (InvalidResourceException ex) {
			    //nothing to do ;(
			} catch (ClassNotFoundException ex2) {
			    //pfff
			} finally {
			    rr_res.unlock();
			}
		    }
		} catch (InvalidResourceException ex) {
		    //nothing to do ;(
		} finally {
		    rr_dir.unlock();
		}
	    } catch (InvalidResourceException ex) {
		//nothing to do ;(
	    } finally {
		rr_cvsframe.unlock();
	    }
	}

	public Reply createDefaultReply(Request request, int status) {
	    Reply reply = super.createDefaultReply(request, status);
	    HttpInteger contentlength = 
		HttpFactory.makeInteger(getInt(ATTR_CONTENT_LENGTH, -1));
	    reply.setHeaderValue(Reply.H_CONTENT_LENGTH, contentlength);
	    return reply;
	}

	public Reply get (Request request)
	    throws ProtocolException
	{
	    try {
		try {
		    checkRevisionNumber(revision);
		} catch (RevisionNumberException ex) {
		    Reply error = 
			request.makeReply(HTTP.BAD_REQUEST);
		    error.setContent("Bad revision number : <b>"+
				     ex.getMessage()+"</b>");
		    return error;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
		    getCvsManager().revert(name, out, revision, null);
		    byte content[] = out.toByteArray();
		    ByteArrayInputStream in = 
			new ByteArrayInputStream(content);
		    this.setValue(ATTR_CONTENT_LENGTH, 
				  new Integer(content.length));
		    Reply reply = createDefaultReply(request, HTTP.OK);
		    reply.setStream(in);
		    // fancy thing, we should get the content location
		    // of the "real" resource...
		    String req_s = request.getURL().toString();
		    int first_sl = req_s.lastIndexOf((int)'/');
		    int second_sl = req_s.lastIndexOf((int)'/', first_sl-1 );
		    int third_sl = req_s.lastIndexOf((int)'/', second_sl-1 );
		    String sub_u = req_s.substring(0, third_sl);
		    reply.setContentLocation(sub_u+'/'+name);
		    return reply;
		} catch (InvalidResourceException ex) {
		    Reply error = 
			request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		    error.setContent("CvsFrame invalid");
		    return error;
		} finally {
		    rr_cvsframe.unlock();
		}
	    } catch (CvsException ex) {
		Reply error = 
		    request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		error.setContent("Cvs operation failed : <b>"+
				 ex.getMessage()+"</b>");
		return error;
	    }
	}

	RevisionFrame (String revision) {
	    this.revision = revision;
	    updateHeaders();
	}
	
    }

    class CvsEntryFrame extends PostableFrame {

	protected Reply dolog (Request request)
	    throws ProtocolException
	{
	    String log = null;
	    try {
		log = getCvsManager().log(name);
	    } catch (CvsException cvs_ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		HtmlGenerator g = getHtmlGenerator("CVS log command failed") ;
		g.append ("<p>The CVS <strong>log</strong> command failed "
			  + " on " + name
			  + " with the following error message: "
			  + "<em>" + cvs_ex.getMessage() + "</em>"
			  + "<hr> from class: " + this.getClass().getName()) ;
		error.setStream (g) ;
		throw new HTTPException (error) ;
	    } catch (InvalidResourceException ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		HtmlGenerator g = getHtmlGenerator("CVS log command failed") ;
		g.append ("<p>The CVS <strong>log</strong> command failed "
			  + " on " + name
			  + " with the following error message: "
			  + "<em>" + ex.getMessage() + "</em>"
			  + "<hr> from class: " + this.getClass().getName()) ;
		error.setStream (g) ;
		throw new HTTPException (error) ;
	    } finally {
		rr_cvsframe.unlock();
	    }
	    Reply reply = request.makeReply(HTTP.OK) ;
	    reply.setContentType (MimeType.TEXT_HTML) ;
	    reply.setStream (parseLog(log)) ;
	    return reply ;
	}

	protected Reply dodiff (Request request)
	    throws HTTPException
	{
	    String diff = null ;
	    try {
		diff = getCvsManager().diff(name);
	    } catch (CvsException cvs_ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		HtmlGenerator g = getHtmlGenerator("CVS diff command failed") ;
		g.append ("<p>The CVS <strong>diff</strong> command failed "
			  + " on " + name
			  + " with the following error message: "
			  + "<em>" + cvs_ex.getMessage() + "</em>"
			  + "<hr> from class: " + this.getClass().getName()) ;
		error.setStream (g) ;
		throw new HTTPException (error) ;
	    } catch (InvalidResourceException ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		HtmlGenerator g = getHtmlGenerator("CVS diff command failed") ;
		g.append ("<p>The CVS <strong>diff</strong> command failed "
			  + " on " + name
			  + " with the following error message: "
			  + "<em>" + ex.getMessage() + "</em>"
			  + "<hr> from class: " + this.getClass().getName()) ;
		error.setStream (g) ;
		throw new HTTPException (error) ;
	    } finally {
		rr_cvsframe.unlock();
	    }
	    // if there are no differences, generate a dummy report
	    Reply         reply = request.makeReply(HTTP.OK) ;
	    if((diff == null) || (diff.length() == 0)) {
		HtmlGenerator g = getHtmlGenerator("CVS diff command results");
		g.append("<P>No differences between " + name
			 + " and the repository</P>");
		reply.setStream(g);
	    } else {
		HtmlGenerator g = getHtmlGenerator("Diff result");
		g.append ("<center>");
		g.append (" [ <a href=\"../\">Up to directory</A> ] &middot;");
		g.append (" [ <a href=\""+getCvsURL(),
			  "\">Back to CVS</A> ] \n");
		g.append ("</center>");
		g.append ("<hr noshade width=\"80%\"><p>");
		g.append ("<span class=\"title\"> CVS diff of ",
			  name,"</span>\n");
		g.append ("<pre>",parseDiff(diff),"</pre>\n");
		g.append ("<p><hr noshade width=\"80%\">");
		g.append ("<center>");
		g.append (" [ <a href=\"../\">Up to directory</A> ] &middot;");
		g.append (" [ <a href=\""+getCvsURL(),
			  "\">Back to CVS</A> ] \n");
		g.append ("</center>");
		reply.setStream(g);
	    }
	    return reply ;
	}

	protected Reply doEditRev (Request request, String revision)
	    throws HTTPException
	{
	    HtmlGenerator g = getHtmlGenerator("Edit log");
	    g.append ("<center>");
	    g.append (" [ <a href=\"../\">Up to directory</A> ] &middot; ");
	    g.append (" [ <a href=\""+getCvsURL()+
		      "\">Back to CVS</A> ] &middot; ");
	    g.append (" [ <a href=\"./",name+"?log\">Back to log</A> ] ");
	    g.append ("<hr noshade width=\"80%\"><p>");
	    g.append ("<span class=\"title\"> Edit log comment of ",
		      name,
		      " (revision "+revision+")</span>\n");
	    g.append ("<form  method=\"post\">\n");
	    g.append ("<input type=\"hidden\" name=\"revision\" value=\"",
		      revision,"\">\n");
	    g.append ("<table border=\"0\">\n");
	    g.append ("<tr><td align=\"left\">");
	    g.append ("<textarea name=\"comment\" rows=\"3\" cols=\"50\">\n") ;
	    g.append ("</textarea></td></tr>\n") ;
	    g.append ("</td></tr><tr><td align=\"center\">");
	    g.append ("<input type=\"submit\" name=\"submit\" " +
		      "value=\" Save Comment \">" );
	    g.append ("</td></tr>");
	    g.append ("</table></center></form>\n");
	    g.append ("<hr noshade width=\"80%\">");
	    Reply reply = request.makeReply(HTTP.OK) ;
	    reply.setStream(g);
	    return reply;
	}

	protected String getRevisionToEdit(Request request) 
	    throws HTTPException
	{
	    String      query = request.getQueryString() ;
	    if (query == null) 
		return null;
	    InputStream in    = new StringBufferInputStream(query) ;
	    URLDecoder  d     = new URLDecoder (in, getOverrideFlag()) ;
	    try {
		d.parse () ;
	    } catch (URLDecoderException e) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: "+
				 "unable to decode form data.");
		throw new HTTPException (error) ;
	    } catch (IOException e) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Invalid request: unable to read form data.");
		throw new HTTPException (error) ;
	    }
	    return d.getValue("editlog") ;
	}

	/**
	 * Getting an entry entity start with dumping the log for this entity
	 * which (should) act as a directory for all versions of it.
	 */

	public Reply get (Request request)
	    throws ProtocolException
	{
	    if ( ! request.hasState("query") ) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		error.setContent("Invalid query field.");
		throw new HTTPException (error) ;
	    }
	    String cmd = request.getQueryString() ;
	    if ( cmd.equalsIgnoreCase("log") ) {
		return dolog(request) ;
	    } else if ( cmd.equalsIgnoreCase ("diff")) {
		return dodiff(request) ;
	    } else {
		String rev = getRevisionToEdit(request);
		if (rev != null)
		    return doEditRev(request, rev);
		else {
		    Reply error = 
			request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		    error.setContent ("Unknown command: "+cmd) ;
		    throw new HTTPException(error) ;
		}
	    }
	}

	public Reply handle (Request request, URLDecoder data)
	    throws ProtocolException
	{
	    String revision = data.getValue("revision");
	    String comment  = data.getValue("comment");
	    
	    if (revision == null) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		error.setContent("No revision selected !");
		throw new HTTPException (error) ;
	    } else if (comment == null) {
		Reply error = request.makeReply(HTTP.BAD_REQUEST) ;
		error.setContent("Empty comment not allowed.");
		throw new HTTPException (error) ;
	    }
	    try {
		String command[] = new String[2];
		command[0] = "-m"+revision+":\""+comment+"\"";
		command[1] = name;
		getCvsManager().admin(command);
	    } catch (CvsException ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
		HtmlGenerator g = 
		    getHtmlGenerator("CVS admin command failed") ;
		g.append ("<p>The CVS <strong>admin</strong> command failed "
			  + " on " + name
			  + " with the following error message: "
			  + "<em>" + ex.getMessage() + "</em>");
		error.setStream (g) ;
		throw new HTTPException (error) ;
	    } catch (InvalidResourceException inv_ex) {
		Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		error.setContent("CvsFrame invalid");
		return error;
	    }
	    //well done
	    return dolog(request);
	}
    }

    //
    //CvsEntryResource
    //

    private DummyResourceReference self        = null;
    private Hashtable              revisions   = null;
    private CvsDirectory           cvs         = null; 

    protected int getMinor(String revision) {
	int index = revision.indexOf(".");
	if (index == -1)
	    return -1;
	try {
	    return Integer.parseInt(revision.substring(index+1));
	} catch (NumberFormatException ex) {
	    return -1;
	}
    }

    protected CvsDirectory getCvsManager() 
	throws InvalidResourceException
    {
	if (cvs == null) {
	    try {
		CvsFrame cvsframe = (CvsFrame) rr_cvsframe.lock();
		cvs = cvsframe.getCvsManager();
	    } finally {
		rr_cvsframe.unlock();
	    }
	}
	return cvs;
    }

    protected String getCvsURL() {
	String CVSURL = null;
	try {
	    CvsFrame cvsframe = (CvsFrame) rr_cvsframe.lock();
	    CVSURL = cvsframe.getResource().getURLPath();
	} catch (InvalidResourceException ex) {
	    return null;
	} finally {
	    rr_cvsframe.unlock();
	}
	return CVSURL;
    }

    protected void checkRevisionNumber(String rev) 
	throws RevisionNumberException, CvsException
    {
	try {
	    String revision = getCvsManager().revision(name);
	    if (revision == null) 
		return; //can't be tested
	    int minor  = getMinor(revision);
	    int minor2 = getMinor(rev);
	    if ((minor2 > minor) || (minor2 <= 0))
		throw new RevisionNumberException(rev);
	} catch (InvalidResourceException inv_ex) {
	    throw new RevisionNumberException("Unable to check revision "+
					      "number, CvsFrame invalid.");
	} finally {
	    rr_cvsframe.unlock();
	}
    }

    /**
     * Analogous to standard C's <code>strncmp</code>, for byte arrays.
     * (Should be in some utility package, I'll put it here for now)
     * @param ba1 the first byte array
     * @param off1 where to start in the first array
     * @param ba2 the second byte array
     * @param off2 where to start in the second array
     * @param n the length to compare up to
     * @return <strong>true</strong> if both specified parts of the 
     * arrays are equal, <strong>false</strong> if they aren't .
     */
    static final boolean byteArrayNEquals(byte[] ba1, int off1,
					  byte[] ba2, int off2,
					  int n)
    {
	// So that only one addition is needed inside loop
	int corr = off2 - off1;
	int max = n+off1;
	for(int i=off1;i<max;i++) 
	    if(ba1[i] != ba2[i+corr])
		return false;
	return true;
    }

    static final boolean isDigitOrPoint(byte ch) {
	return ch=='.' || ch=='0' || ch=='1' || ch=='2' || ch=='3' || 
	    ch=='4' || ch=='5' || ch=='6' || ch=='7' || ch=='8' || 
	    ch=='9';
    }

    /**
     * Does the same as Character.isSpace, without need to cast the
     * byte into a char.
     * @param ch the character
     * @return whether or not ch is ASCII white space
     * @see java.lang.Character#isSpace
     */
    static final boolean isSpace(byte ch)
    {
	return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r';
    }

    protected void writeLinks(OutputStream out, 
			     byte b[], 
			     int offset, 
			     int length) 
	throws IOException
    {
	out.write(startAnchor);
	out.write(("./"+name+"/").getBytes());
	out.write(b,offset, length);
	out.write(midAnchor);
	out.write(b,offset, length);
	out.write(endAnchor);
	out.write((byte)' ');
	out.write((byte)'[');
	out.write(startAnchor);
	out.write(("./"+name+"?editlog=").getBytes());
	out.write(b,offset, length);
	out.write(midAnchor);
	out.write(edittext);
	out.write(endAnchor);
	out.write((byte)']');
    }

    /**
     * replace < by &lt;.
     */
    protected String parseDiff(String diff) {
	StringBuffer newdiff = new StringBuffer();
	int idx = diff.indexOf('<');
	if (idx == -1)
	    return diff;
	while ((idx = diff.indexOf('<')) != -1) {
	    newdiff.append(diff.substring(0, idx));
	    newdiff.append("&lt;");
	    diff = diff.substring(idx+1);
	}
	newdiff.append(diff);
	return newdiff.toString();
    }

    protected HtmlGenerator parseLog (String log) {
	HtmlGenerator g = getHtmlGenerator("CVS log of "+name);
	String CVSURL = getCvsURL();
	String head = 
	    "[ <a href=\"./../\">Up to directory</a> ] &middot; "+
	    "[ <a href=\""+CVSURL+"\">Back to CVS</a> ]";

	g.append("<center>",head,"<hr noshade width=\"80%\"></center><p>");
	g.append ("<span class=\"title\"> CVS log of ",name,"</span>\n");
	g.append ("<div class=\"box\"><pre>\n");
	//parse the log string
	ByteArrayOutputStream out = 
	    new ByteArrayOutputStream();
	byte unparsed[] = log.getBytes();
	int byteIdx = 0;
	int startIdx = 0;
	byte ch;
	byteIdx += 7;
	do {
	    while(byteIdx < unparsed.length) {
		if( (ch = unparsed[byteIdx]) == (byte) 'n' ) {
		    if(byteArrayNEquals(unparsed, byteIdx-7,
					pattern, 0,
					7)) {
			break;
		    }
		}
		byteIdx += increments[ch>=0 ? ch : 0];
	    }
	    if(++byteIdx >= unparsed.length)
		break;
	    //we just found 'revision'
	    while ((byteIdx <= unparsed.length) &&
		   isSpace(unparsed[byteIdx])) {
		byteIdx++;
	    }
	    out.write(unparsed, startIdx, byteIdx-startIdx);
	    startIdx = byteIdx;
	    //get the revision number
	    while ((byteIdx <= unparsed.length) && 
		   isDigitOrPoint(unparsed[byteIdx])) {
		byteIdx++;
	    }
	    //revision number startIdx, byteIdx-1;
	    if (byteIdx-1 > startIdx) {
		String rev = new String(unparsed, 
					startIdx, 
					byteIdx-startIdx);
		try { 
		    checkRevisionNumber(rev);
		    writeLinks(out, unparsed, startIdx, byteIdx-startIdx);
		} catch (Exception ex) {
		    out.write(unparsed, startIdx, byteIdx-startIdx);
		}
	    }
	    startIdx = byteIdx;
	} while (byteIdx < unparsed.length) ;
	// Add the last chunk of unparsed text 
	int length = unparsed.length-startIdx-1;
	if (length > 0)
	    out.write(unparsed, startIdx, length);
	String parsedlog = out.toString();
	g.append(parsedlog);
	//end of parsing
	g.append ("\n</pre></div>\n");
	g.append("<hr noshade width=\"80%\"><center>",head,"</center><p>");
	return g;
    }

    protected synchronized 
	ResourceReference getRevisionResource(String revision) 
    {
	RevisionResource res = (RevisionResource) revisions.get(revision);
	if (res == null) {
	    res = new RevisionResource(revision);
	    revisions.put(revision, res);
	}
	return res.getResourceReference();
    }

    public ResourceReference lookup (String revision) {
	try {
	    checkRevisionNumber(revision);
	    return getRevisionResource(revision);
	} catch (CvsException ex) {
	} catch (RevisionNumberException ex2) {
	}
	return null;
    }

    public synchronized ResourceReference getResourceReference() {
	if (self == null)
	    self = new DummyResourceReference(this);
	return self;
    }

    protected HtmlGenerator getHtmlGenerator(String title) {
	try {
	    CvsFrame cvsframe = (CvsFrame)rr_cvsframe.lock();
	    return CvsFrame.getHtmlGenerator(cvsframe, title);
	} catch (InvalidResourceException ex) {
	    return CvsFrame.getHtmlGenerator(title);
	} finally {
	    rr_cvsframe.unlock();
	}
    }

    CvsEntryResource (ResourceReference rr_cvsframe, String name) {
	revisions = new Hashtable(3);
	this.name = name;
	this.rr_cvsframe = rr_cvsframe;
	Hashtable defs = new Hashtable(3);
	registerFrame( new CvsEntryFrame(), defs );
    }

}
