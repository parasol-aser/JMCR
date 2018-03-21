// EchoCommand.java
// $Id: EchoCommand.java,v 1.1 2010/06/15 12:21:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Date;
import java.util.Dictionary;

import java.net.URL ;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.util.ArrayDictionary;
import org.w3c.util.TimeFormatter;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the <code>echo</code> SSI command.
 * As extensions, it has the parameters "reqstate" (for echoing
 * Jigsaw request states) and "reqheader" (for echoing request
 * header).
 * <p>Also, it can take the flag "here", whose presence means that the
 * variable is to be interpreted at the deepest request level (in the
 * case of chained internal requests), instead of doing so at the top
 * (external request) level.  It inserts the value of a variable in
 * the document. 
 * @author Antonio Ramirez <anto@mit.edu>
 */

public class EchoCommand implements Command {
    private final static String NAME = "echo" ;

    private static Dictionary modFlags = null ;

    private static final String[] keys =
    {
	"var",
	"reqstate",
	"reqheader",
	"here"
    } ;

    static {
	String[] names = {
	    "DOCUMENT_NAME"	,
	    "DOCUMENT_URI"	,
	    "QUERY_STRING_UNESCAPED",
	    "SERVER_SOFTWARE"	,
	    "SERVER_NAME"	,
	    "GATEWAY_INTERFACE"	,
	    "SERVER_PROTOCOL"	,
	    "SERVER_PORT"	,
	    "REQUEST_METHOD"	,
	    "PATH_INFO"	        ,
	    "PATH_TRANSLATED"	,
	    "SCRIPT_NAME"	,
	    "QUERY_STRING"	,
	    "REMOTE_HOST"	,
	    "REMOTE_ADDR"	,
	    "REMOTE_USER"	,
	    "AUTH_TYPE"		,
	    "REMOTE_IDENT"	,
	    "CONTENT_TYPE"	,
	    "CONTENT_LENGTH"	,
	    "HTTP_ACCEPT"	,
	    "HTTP_USER_AGENT"   ,
	    "DATE_LOCAL"        ,
	    "DATE_GMT"          ,
	    "LAST_MODIFIED"
	} ;

	Boolean[] values = {
	    Boolean.FALSE,	//    DOCUMENT_NAME	
	    Boolean.FALSE,	//    DOCUMENT_URI	
	    Boolean.TRUE,	//    QUERY_STRING_UNESCAPED
	    Boolean.FALSE,	//    SERVER_SOFTWARE	
	    Boolean.FALSE,	//    SERVER_NAME	
	    Boolean.FALSE,	//    GATEWAY_INTERFACE	
	    Boolean.FALSE,	//    SERVER_PROTOCOL	
	    Boolean.FALSE,	//    SERVER_PORT	
	    Boolean.FALSE,	//    REQUEST_METHOD	
	    Boolean.FALSE,	//    PATH_INFO	        
	    Boolean.FALSE,	//    PATH_TRANSLATED	
	    Boolean.FALSE,	//    SCRIPT_NAME"	
	    Boolean.TRUE,	//    QUERY_STRING	
	    Boolean.FALSE,	//    REMOTE_HOST	
	    Boolean.FALSE,	//    REMOTE_ADDR	
	    Boolean.FALSE,	//    REMOTE_USER
	    Boolean.FALSE,      //    AUTH_TYPE
	    Boolean.FALSE,	//    REMOTE_IDENT	
	    Boolean.FALSE,	//    CONTENT_TYPE	
	    Boolean.FALSE,	//    CONTENT_LENGTH	
	    Boolean.FALSE,	//    HTTP_ACCEPT	
	    Boolean.FALSE,	//    HTTP_USER_AGENT   
	    Boolean.TRUE,	//    DATE_LOCAL        
	    Boolean.TRUE,	//    DATE_GMT          
	    Boolean.FALSE	//    LAST_MODIFIED
	} ;

	modFlags = new ArrayDictionary(names,values) ;
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	// Get the relevant parameters
	Object[] values = parameters.getMany(keys) ;
	String var = (String) values[0] ;
	String reqstate = (String) values[1] ;
	String reqheader = (String) values[2] ;
	boolean here = values[3] != null ;

	boolean notMod = false ;

	Reply reply = ssiframe.createCommandReply(request,HTTP.OK) ;
	String content = null ;
	
	if(var != null) {
	    if(var.equals("DOCUMENT_URI")) {
		URL theUrl = here
		    ? request.getURL()
		    : request.getOriginal().getURL() ;
		if(theUrl != null)
		    content = theUrl.toString() ;
	    } else {
		Dictionary ssiVars = (Dictionary)
		    variables.get(here ? "ssiVars" : "topSsiVars") ;
		
		if(ssiVars == null) return null ;
		
		if(var.equals("DATE_LOCAL")) {
		    content = 
			TimeFormatter
			.format(new Date(),
				(String) variables.get("datefmt")) ;
		} else if(var.equals("DATE_GMT")) {
		    
		    // FIXME: won't do formatting
		    content = new Date().toGMTString() ;
		    
		} else if(var.equals("LAST_MODIFIED")) {
		    
		    content = TimeFormatter
			.format((Date) ssiVars.get("X_LAST_MODIFIED"),
				(String) variables.get("datefmt")) ;
		    
		} else {
		    
		    content = (String) ssiVars.get(var) ;
		    if (content == null)
			content = "";
		}
	    }
	    long ims = request.getIfModifiedSince() ;
	    if(ims != -1) {
		if( var == null ) notMod = true ;
		else {
		    Boolean result = (Boolean) modFlags.get(var) ;
		    notMod = 
			(result == null) ? true : ! result.booleanValue() ;
		}
	    }
	} else if(reqstate != null) {
	    Object state = here
		? request.getState(reqstate)
		: request.getOriginal().getState(reqstate) ;
		
	    if(state != null)
		content = state.toString() ;
	} else if(reqheader != null) {
	    reqheader = reqheader.toLowerCase() ;
	    HeaderValue hvalue =  here
		? request.getHeaderValue(reqheader)
		: request.getOriginal().getHeaderValue(reqheader) ;

	    if(hvalue != null)
		content = hvalue.toExternalForm() ;
	} else return null ;
	
	if(content != null) {
	    reply.setContent(content) ;
	} else return null ;

	if(notMod) {
	    reply.setStatus(HTTP.NOT_MODIFIED) ;
	    reply.setLastModified(request.getIfModifiedSince()) ;
	} 

	return reply ;
    }

    public String getName()
    {
	return NAME;
    }

  public String getValue(Dictionary variables, String variable, 
			 Request request) {
    return "null";
  }

}


