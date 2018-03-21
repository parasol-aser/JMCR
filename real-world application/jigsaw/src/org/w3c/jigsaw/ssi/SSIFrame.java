// Ssiframe.java
// $Id: SSIFrame.java,v 1.2 2010/06/15 17:53:09 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi ;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import java.util.Dictionary;
import java.util.Vector;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpDate;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpInteger;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.ClassAttribute;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;

import org.w3c.tools.resources.event.AttributeChangedEvent;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.ssi.commands.CommandRegistry;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * This resource implements server-side parsing of HTML documents.
 * Any comment of the form <code>&lt;!--#commandName param1=val1
 * ... paramn=valn --&gt;</code> will be interpreted as an include
 * directive.
 * <p> Commands are looked up in an instance of the class
 * supplied in the registryClass attribute, which must be a subclass
 * of <code>org.w3c.jigsaw.ssi.CommandRegistry</code>.
 *
 * @author Antonio Ramirez <anto@mit.edu>
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 * @see org.w3c.jigsaw.ssi.commands.CommandRegistry
 * @see org.w3c.jigsaw.ssi.commands.Command
 */

public class SSIFrame extends HTTPFrame {

    public static final boolean debug = false ;

    /** Attributes index - The segments */
    private static int ATTR_SEGMENTS = -1 ;

    /** Attributes index - The total unparsed size */
    private static int ATTR_UNPARSED_SIZE = -1 ;

    /** Attribute index - The class to use for making the CommandRegistry */
    private static int ATTR_REGISTRY_CLASS = -1 ;

    /** Attributes index - The maximum recursive parsing depth */
    private static int ATTR_MAX_DEPTH = -1 ;

    /** Attribute index - Whether or not to deny insecure commands */
    private static int ATTR_SECURE = -1 ;

    /**
     * The command registry used by the resource.
     */
    private CommandRegistry commReg = null ;

    /**
     * The most specific class of the current command registry.
     */
    private Class regClass = null ;

    /**
     * Here we keep track of created registries to avoid
     * making more than one per registry class.
     */
    private static Dictionary regs = new ArrayDictionary(5) ;

    /** Will hold the increments for finding "<!--#" */
    private static byte startIncrements[] = new byte[128] ;

    /** Same thing for "-->" */
    private static byte endIncrements[] = new byte[128] ;

    /** The start pattern */
    private static byte startPat[] =
    {
	(byte)'<',(byte)'!',(byte)'-',(byte)'-',(byte)'#'
    };

    /** The end pattern */
    private static byte endPat[] =
    {
	(byte)'-',(byte)'-',(byte)'>'
    };

    // For value-less parameters 
    private static final String emptyString = "" ;

    /**
     * Our "very global" variables
     */
    protected Dictionary vars = null;

    /**
     * Message state - the current recursion depth
     */
    public static final String STATE_DEPTH =
	"org.w3c.jigsaw.ssi.SSIResource.depth" ;

    /**
     * Message state - the current variables
     */
    public static final String STATE_VARIABLES =
	"org.w3c.jigsaw.ssi.SSIResource.variables" ;

    private boolean cacheReplies = true;

    protected void doNotCacheReply() {
	cacheReplies = false;
    }

    protected boolean cacheReplies() {
	return cacheReplies;
    }

    /**
     * Listen its resource.
     */
    public void attributeChanged(AttributeChangedEvent evt) {
	super.attributeChanged(evt);
	String name = evt.getAttribute().getName();
	if ((name.equals("file-stamp")) || (name.equals("file-length"))) {
	    setValue(ATTR_SEGMENTS, (Segment[]) null);
	}
    }

    private final int getUnparsedSize()
    {
	return getInt(ATTR_UNPARSED_SIZE,-1) ;
    }

    private final void setUnparsedSize(int unparsedSize)
    {
	setValue(ATTR_UNPARSED_SIZE,new Integer(unparsedSize)) ;
    }

    /**
     * Makes sure that checkContent() is called on _any_ HTTP method,
     * so that the internal representation of commands is always consistent.
     * @param request The HTTPRequest
     * @param filters The filters to apply
     * @return a ReplyInterface instance
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If this resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface request)
	throws ProtocolException, ResourceException
    {
	if (! checkRequest(request)) {
	    return performFrames(request);
	}
	if (fresource != null)
	    fresource.checkContent();
	return super.perform(request) ;
    }

    /**
     * Perform a get (associated with a FileResource)
     * @param request the HTTP request
     * @return a Reply instance.
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If this resource got a fatal error.
     */
    protected Reply getFileResource(Request request)
	throws ProtocolException, ResourceException
    {
	Reply reply = handle(request) ;
	return reply != null
	    ? reply
	    : super.getFileResource(request) ;
    }

    /**
     * Perform a post.
     * @param request the HTTP request
     * @return a Reply instance.
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If this resource got a fatal error.
     */
    public Reply post(Request request)
	throws ProtocolException, ResourceException
    {
	Reply reply = handle(request) ;
	return reply != null
	    ? reply
	    : super.post(request) ;
    }

    /**
     * Handles all relevant HTTP methods.
     * Merges the partial replies from each of the segments into
     * one global reply.
     * <strong>Remark</strong>: no direct relation to PostableResource.handle()
     * @param request The HTTP request
     * @return a Reply instance.
     * @exception ProtocolException If processing the request failed.
     */

    public Reply handle(Request request)
	throws ProtocolException
    {
	if (fresource == null)
	    return null;

	if(SSIFrame.debug)
	    System.out.println("@@@@ handle: "+
			       (request.isInternal() 
				? "internal" : "external") ) ;
	fresource.checkContent() ;
	
	Integer depth =
	    (Integer) request.getState(STATE_DEPTH) ;
	if(depth == null) depth = new Integer(0) ;

	int unparsedSize = 0 ;

	Segment[] segments = getSegments() ;
	if(segments == null) {
	    parseFirstTime() ;
	    if( (segments = getSegments()) == null )
		return null ; // Last resort: fall back to superclass
	}
	Reply reply = null ;
	try {
	    // Obtain a command registry
	    updateRegistry() ;

	    vars = (Dictionary)
		request.getState(STATE_VARIABLES) ;

	    // Initialize the registry-dependent variables:
	    vars = commReg.initVariables(this,request,vars) ;

	    // Add our "very global" variables
	    vars.put("secure",getValue(ATTR_SECURE,Boolean.TRUE)) ;
	    vars.put("maxDepth",getValue(ATTR_MAX_DEPTH,new Integer(10))) ;
	    vars.put("depth",depth) ;
	    vars.put("registry",commReg) ;

	    // Prepare the initial reply
	    // (which represents the unparsed parts of the document)
	    // and a prototype reply for segments that return null.
	    reply = createDefaultReply(request,HTTP.OK) ; 
	    Reply defSegReply = createDefaultReply(request,HTTP.OK) ;

	    int unpSize = getUnparsedSize() ;
	    if(unpSize == -1) 
		reply.setHeaderValue(Reply.H_CONTENT_LENGTH,null) ;
	    else 
		reply.setContentLength(unpSize) ;
	    defSegReply.setHeaderValue(Reply.H_CONTENT_LENGTH,null) ;

	    long ims = request.getIfModifiedSince() ;
	    long cmt = fresource.getFileStamp() ;	
	    // used to be getLastModified()
	    // should be something better
	    // than either
	    if(SSIFrame.debug)
		System.out.println("@@@@ IMS: "+cmt+" vs "+ims) ;
	    if(ims != -1 && cmt != -1 && cmt <= ims) {
		reply.setStatus(HTTP.NOT_MODIFIED) ;
		defSegReply.setStatus(HTTP.NOT_MODIFIED) ;
	    } else if(ims != -1) {
		if(SSIFrame.debug)
		    System.out.println("@@@@ Removed NOT MODIFIED") ;
	    }

	    
	    if(cmt != -1)
		defSegReply.setLastModified(cmt) ;
	    
	    // For each segment:
	    // 	. obtain a reply,
	    // 	. merge its headers with the global reply's headers,
	    Reply[] partReps = new Reply[segments.length] ;
	    for(int i=0;i<segments.length;i++) {
		if(!segments[i].isUnparsed()) {
		    if(SSIFrame.debug)
			System.out.println("@@@@ Analyzing segment " +
					   segments[i]) ;

		    partReps[i] = segments[i].init(this, request, 
						   vars, commReg, i);

		    if(SSIFrame.debug) {
			if (partReps[i] == null)
			    System.out.println("@@@@ (null segment)") ;
			System.out.println("@@@@ cacheReplies : "+
					   cacheReplies());
		    }
		    
		    merge(reply,
			  partReps[i] != null ? partReps[i] : defSegReply) ;
		}
	    }

	    // Set a stream, unless we're not supposed to.
	    // Also handle the case of no command segments.
	    switch(reply.getStatus()) {
	    default:
		reply.setStream
		    (new SSIStream(cacheReplies(),
				   segments,
				   partReps,
				   new RandomAccessFile(fresource.getFile(),
							"r"))) ;
	    case HTTP.NO_CONTENT:
	    case HTTP.NOT_MODIFIED:
	    }

	    if(SSIFrame.debug)
		System.out.println("@@@@ Last-modified: " +
				   reply.getLastModified()) ;

	    reply.setDate(System.currentTimeMillis()) ;
	    return reply ;

	} catch(SSIException ex) {
	    reply = createDefaultReply(request,HTTP.INTERNAL_SERVER_ERROR) ;
	    reply.setContent("SSIFrame is misconfigured: "+
			     ex.getMessage());
	    throw new HTTPException(reply) ;
	} catch(Exception ex) {
	    ex.printStackTrace() ;
	    if(SSIFrame.debug) {
		if(SSIFrame.debug)
		    System.out.println("@@@@ Fallback to FileResource") ;
	    }
	    return null ;  // Last resort: fall back to superclass
	}
    }

    // The headers to merge and their corresponding callbacks
    // (more to come)
    private static final int mergeHeaders[] =
    {
	Reply.H_AGE,
	Reply.H_CONTENT_LENGTH,
	Reply.H_EXPIRES,
	Reply.H_LAST_MODIFIED,
    } ;

    private static final Merger mergers[] =
    {
	new IntMaximizer(),	// 	Reply.H_AGE
	new IntAdder(),		// 	Reply.H_CONTENT_LENGTH
	new DateMinimizer(),	// 	Reply.H_EXPIRES
	new DateMaximizer()	// 	Reply.H_LAST_MODIFIED
    } ;

    /**
     * Merges the headers (and status code) of a segment's reply with
     * those of the global reply.
     *
     * @param glob the global reply
     * @param part the segment's partial reply
     */
    private void merge(Reply glob, Reply part)
    {
	// Deal with status code first
	int pstat = part.getStatus() ;
	int gstat = glob.getStatus() ;
	
	if(pstat == HTTP.NOT_MODIFIED) {
	    switch(gstat) {
	    default:
		glob.setStatus(HTTP.OK) ;
	    case HTTP.NOT_MODIFIED:
	    }
	} else if(gstat == HTTP.NOT_MODIFIED) {
	    if(SSIFrame.debug)
		System.out.println("**** removed NOT MODIFIED") ;
	    glob.setStatus(HTTP.OK) ;
	}
	
	// Now handle headers
	// "pointers to methods" would make this simpler
	for(int i=0;i<mergeHeaders.length;i++) 
	    glob.setHeaderValue(mergeHeaders[i],
				mergers[i]
				.merge(glob.getHeaderValue(mergeHeaders[i]),
				       part.getHeaderValue(mergeHeaders[i]))) ;
	
	// Now handle annoying quasi-headers:
	int pint,gint ;

	// Cache-Control: max-age=n
	// (don't merge if set as attribute)
	if( getMaxAge() != -1 &&
	    (pint = part.getMaxAge()) != -1 ) {
	    if( (gint = glob.getMaxAge()) != -1)
		pint = Math.min(gint,pint) ;
	    glob.setMaxAge(pint) ;
	}
    }

    /**
     * Retrieves the segments from the attribute
     * @return An array of segments
     */
    private final Segment[] getSegments()
    {
	return (Segment[]) getValue(ATTR_SEGMENTS,null) ;
    }
	
    /**
     * Updates the working command registry if either the registryClass
     * attribute has changed or it has never been created before.
     * <p>To avoid unnecessarily creating command registry
     * instances, this method will keep track of which kinds of command
     * registries have been created, and avoid making duplicates.
     * @exception SSIException If the operation can't be performed.
     */
    private void updateRegistry() 
	throws SSIException 
    {
	try {
	    Class attrRegClass = (Class) getValue(ATTR_REGISTRY_CLASS, null) ;
	    if(attrRegClass == null)
		attrRegClass = Class.forName
		    ("org.w3c.jigsaw.ssi.commands.DefaultCommandRegistry") ;
	    
	    if(regClass == null ||
	       !attrRegClass.equals(regClass)) {
		regClass = attrRegClass ;
		commReg = fetchRegistry(regClass) ;
	    }
	} catch(ClassNotFoundException ex) {
	    throw new SSIException("Cannot make registry: "+ex.getMessage()) ;
	}
    }

    /**
     * Returns an instance of the given command registry class,
     * either a new instance, or an old one if it exists in the dictionary.
     * @exception SSIException If the operation can't be performed.
     */
    private CommandRegistry fetchRegistry(Class regClass)
	throws SSIException
    {
	try {
	    CommandRegistry reg = (CommandRegistry) regs.get(regClass) ;
	    if(reg!=null) 
		return reg;
	    else {
		reg = (CommandRegistry) regClass.newInstance();
		regs.put(regClass,reg) ;
		return reg;
	    }
	} catch(Exception ex) {
	    throw new SSIException("Cannot fetch command registry: "+
				   ex.getMessage()) ;
	}
    }

    /** Reads the unparsed file into memory, if not already done */
    private byte[] readUnparsed()
	throws IOException
    {
	File file = fresource.getFile();
	ByteArrayOutputStream out =
	    new ByteArrayOutputStream((int)file.length()) ;
	
	FileInputStream in = new FileInputStream(file);

	byte[] buf = new byte[4096] ;
	int len = 0;
	
	while( (len = in.read(buf)) != -1) 
	    out.write(buf,0,len);
	
	in.close();
	out.close();
	
	byte[] unparsed = out.toByteArray() ;
	return unparsed ;
    }
	

    /**
     * Does a first-time parse and sets the segment list attribute
     * accordingly.
     */
    private void parseFirstTime()
    {
	if (debug)
	    System.out.println("@@@ parseFirstTime");
	cacheReplies = true;
	byte[] unparsed = null ;
	try {
	    unparsed = readUnparsed() ;
	} catch(IOException ex) {
	    setValue(ATTR_SEGMENTS,null) ;
	    return ;
	}

	// The parsing code was adapted from phttpd 
	
	int byteIdx = 0, startInc, endInc, startParam, endParam, paramIdx,i ;
	byte ch, quote ;
	int max ,length = 0;
	boolean valueFound ;
	
	int unparsedSize = 0 ;
	
	// For maintaining the segment list 
	Vector buildSegments = new Vector(20) ;

	StringBuffer cmdBuf = null ;
	String cmdName = null ;
	Vector /*<String>*/ parNames = null ;
	Vector /*<String>*/ parValues = null ;
	String name = null , value = null ;
	
	// To store where the last segment ended
	int lastSegEnd = 0;

	do {
	    byteIdx += 4;
	    while(byteIdx < unparsed.length) {
		if( (ch = unparsed[byteIdx]) == (byte) '#' )
		    if(byteArrayNEquals(unparsed, byteIdx-4,
					startPat, 0,
					4)) {
			break;
		    }

		// This is an ugly work-around to the
		// absence of unsigned bytes in Java.
		byteIdx += startIncrements[ch>=0 ? ch : 0];
	    }

	    if(++byteIdx >= unparsed.length)
		break; // Nothing found
	    
	    // Record the start of the command name and parameter list
	    startInc = (startParam = paramIdx = byteIdx) - 5 ;
	    
	    // Add the previous segment of unparsed text
	    // (Unless empty)
	    if(startInc > lastSegEnd) {
		buildSegments.addElement(new Segment(lastSegEnd,
						     startInc));
		unparsedSize += startInc - lastSegEnd ;
		lastSegEnd = startInc ;
	    }

	    // Now find the end of the comment
	    byteIdx += 2;
	    while(byteIdx < unparsed.length) {
		if( (ch = unparsed[byteIdx]) == (byte) '>')
		    if(unparsed[byteIdx-2] == (byte) '-' &&
		       unparsed[byteIdx-1] == (byte) '-')
			break;

		// This is an ugly work-around to the absence of
		// unsigned bytes in Java:
		byteIdx += endIncrements[ch>=0 ? ch : 0] ;
	    }
	    if(++byteIdx >= unparsed.length)
		break; // No end found

	    // The end of the parameter list is 3 bytes earlier
	    endParam = byteIdx - 3 ;
	    
	    // Record the nominal end of the command segment
	    endInc = byteIdx ;

	    // Skip white space before command 
	    while(paramIdx < endParam && isSpace(unparsed[paramIdx]) )
		paramIdx++;
	    if( paramIdx >= endParam )
		continue; // No command name
	    

	    max = endParam - paramIdx ;

	    cmdName = parseCmdName(unparsed,paramIdx,max) ;

	    // If not found, take this one as unparsed and
	    // search for the next include.
	    if(cmdName == null) {
		buildSegments.addElement(new Segment(startInc,
						     endInc));
		unparsedSize += endInc - startInc ;
		lastSegEnd = endInc ;
		continue;
	    }

	    parNames = new Vector(5) ;
	    parValues = new Vector(5) ;

	    parseCmdParams(unparsed,
			   paramIdx+cmdName.length(),
			   endParam,
			   parNames,parValues) ;

	    buildSegments.addElement( new Segment(this,
						  cmdName,
						  new
						  ArrayDictionary(parNames,
								  parValues),
						  lastSegEnd,
						  endInc)) ;
	    lastSegEnd = endInc ;
	    
	} while(byteIdx < unparsed.length) ;
	
	
	// Add the last chunk of unparsed text as a segment
	buildSegments.addElement(new Segment(lastSegEnd,
					     unparsed.length));
	unparsedSize += unparsed.length - lastSegEnd ;

	setUnparsedSize(unparsedSize) ;
	
	Segment[] segs = new Segment[buildSegments.size()] ;
	buildSegments.copyInto(segs) ;
	setValue(ATTR_SEGMENTS,segs) ;
    }

    private final String parseCmdName(byte[] unparsed,int start,int max)
    {
	StringBuffer cmdBuf = new StringBuffer(80) ;
	char ch ;
	for(int i=0;i<max;i++) {
	    ch = (char) unparsed[start+i];
	    if(Character.isWhitespace(ch)) break ;
	    cmdBuf.append(ch) ;
	}
	return cmdBuf.length() == 0? null : cmdBuf.toString() ;
    }

    private final void parseCmdParams(byte[] unparsed,
				      int start,int end,
				      Vector names,Vector values)
    {
	String name = null ;
	String value = null ;
	int startParam = -1 ;

	int paramIdx = start ;
	while(paramIdx < end) {
	    while(isSpace(unparsed[paramIdx++]))
		;
	    if(paramIdx >= end)
		break;
	    
	    byte ch = unparsed[--paramIdx];
	    startParam = paramIdx;
	    while(paramIdx < end
		  && !isSpace(ch)
		  && ch != (byte) '=')
		ch = unparsed[++paramIdx];
	    
	    int length = paramIdx - startParam ;
	    if(length<=0) break;
	    
	    name = new String(unparsed,0,startParam,length) ;
	    value =  emptyString ;
	    
	    boolean valueFound = false ;
	    while(isSpace(ch)
		  || ch == (byte) '=') {
		if(ch == (byte) '=')
		    valueFound = true ;
		ch = unparsed[++paramIdx] ;
	    }
	    
	    if(paramIdx >= end)
		valueFound = false ;

	    byte quote ;
	    if(valueFound)
		if(ch == '"' || ch == '\'' ) {
		    quote = ch ;
		    ch = unparsed[++paramIdx];
		    
		    startParam = paramIdx ;
		    while(paramIdx < end && ch != quote )
			ch = unparsed[++paramIdx];
		    length = paramIdx - startParam ;
		    value = new String(unparsed,
				       0,
				       startParam,
				       length);
		    paramIdx++ ;  
		} else {
		    startParam = paramIdx ;
		    while(paramIdx < end
			  && ! isSpace(ch))
			ch = unparsed[++paramIdx];
		    length = paramIdx - startParam ;
		    value = new String (unparsed,
					0,
					startParam,
					length);
		}
	    names.addElement(name) ;
	    values.addElement(value) ;
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
     * @return <strong>true</strong> if both specified parts of the arrays are
     *           equal, <strong>false</strong> if they aren't .
     */
    public static final boolean byteArrayNEquals(byte[] ba1, int off1,
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

    /**
     * Does the same as Character.isSpace, without need to cast the
     * byte into a char.
     * @param ch the character
     * @return whether or not ch is ASCII white space
     * @see java.lang.Character#isSpace
     */
    private final boolean isSpace(byte ch)
    {
	return ch==' ' || ch=='\t' || ch=='\n' || ch=='\r' ;
    }

    public long getLastModified()
    //FIXME
    {
	long a = super.getLastModified() ;
	return a - a % 1000 ;
    }
    public final Reply createDefaultReply(Request request, int status)
    {
	Reply reply = super.createDefaultReply(request,status) ;
	reply.setHeaderValue(Reply.H_LAST_MODIFIED,null) ;
	reply.setKeepConnection(false);
	return reply ;
    }

    public final Reply createCommandReply(Request request, int status)
    {
	return createDefaultReply(request,status) ;
    }

    static {
	// Initialize search increments first
	for(int i=0;i<128;i++) {
	    startIncrements[i] = 5 ;
	    endIncrements[i] = 3 ;
	}
	startIncrements[(int)('<')] = 4;
	startIncrements[(int)('!')] = 3;
	startIncrements[(int)('-')] = 1;
	endIncrements[(int)('-')] = 1;

	// Initialize attributes
	Attribute a = null;
	Class cls = null;
	Class regClass = null ;

	try {
	    cls = Class.forName("org.w3c.jigsaw.ssi.SSIFrame") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    regClass = 
	  Class.forName("org.w3c.jigsaw.ssi.commands.DefaultCommandRegistry") ;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

	// The maxDepth attribute
	a = new IntegerAttribute("maxDepth"
				 , new Integer(10)
				 , Attribute.EDITABLE) ;
	ATTR_MAX_DEPTH = AttributeRegistry.registerAttribute(cls,a) ;

	// The secure attribute
	a = new BooleanAttribute("secure"
				 , Boolean.TRUE
				 , Attribute.EDITABLE) ;
	ATTR_SECURE = AttributeRegistry.registerAttribute(cls,a) ;

	// The segments attribute
	a = new SegmentArrayAttribute("segments"
				      , null
				      , Attribute.COMPUTED) ;
	ATTR_SEGMENTS = AttributeRegistry.registerAttribute(cls,a) ;

	// The unparsedSize attribute
	a = new IntegerAttribute("unparsedSize"
				 , null
				 , Attribute.COMPUTED) ;
	ATTR_UNPARSED_SIZE = AttributeRegistry.registerAttribute(cls,a) ;

	// The registryClass attribute
	a = new ClassAttribute("registryClass"
			       , regClass
			       , Attribute.EDITABLE ) ;
	ATTR_REGISTRY_CLASS = AttributeRegistry.registerAttribute(cls,a) ;

    }

}

/**
 * Merger classes are used to provide callbacks and make
 * header merging more uniform. (Though it may be overkill...)
 */
abstract class Merger {
    abstract HeaderValue merge(HeaderValue g,HeaderValue p) ;
}

class IntMaximizer extends Merger {
    HeaderValue merge(HeaderValue g,HeaderValue p)
    {
	if(p != null) {
	    if(g != null) {
		((HttpInteger) g)
		    .setValue(Math.max( ((Integer) g.getValue()).intValue() ,
				      ((Integer) p.getValue()).intValue()  )) ;
	    } else return p ;
	}
	return g ;
    }
}	    

class IntMinimizer extends Merger {
    HeaderValue merge(HeaderValue g,HeaderValue p)
    {
	if(p != null) {
	    if(g != null) {
		((HttpInteger) g)
		    .setValue(Math.min(((Integer) g.getValue()).intValue() ,
					((Integer) p.getValue()).intValue())) ;
	    } else return p ;
	}
	return g ;
    }
}

class IntAdder extends Merger {
    HeaderValue merge(HeaderValue g,HeaderValue p) {
	if(SSIFrame.debug)
	    System.out.println("&&&& Adder: g="+g+", p="+p) ;
	if(g != null) {
	    if(p != null) {
		int b = ((Integer) g.getValue()).intValue() +
			       ((Integer) p.getValue()).intValue() ;

		((HttpInteger) g)
		    .setValue( b  ) ;
	    } else return null ;
	}
	return g ;
    }
}

class DateMinimizer extends Merger {
    HeaderValue merge(HeaderValue g,HeaderValue p)
    {
	if(p != null) {
	    if(g != null) {
		((HttpDate) g)
		    .setValue(Math.min(((Long) g.getValue()).longValue() ,
				       ((Long) p.getValue()).longValue())) ;
	    } else return p ;
	}
	return g ;
    }
}
class DateMaximizer extends Merger {
    HeaderValue merge(HeaderValue g,HeaderValue p)
    {
	if(p != null) {
	    if(g != null) {
	        ((HttpDate) g)
		    .setValue(Math.max(  ((Long) g.getValue()).longValue() ,
					 ((Long) p.getValue()).longValue())) ;
	    } else return p ;
	}
	return g ;
    }
}

	
