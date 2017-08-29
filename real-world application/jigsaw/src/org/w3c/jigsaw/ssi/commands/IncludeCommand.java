// IncludeCommand.java
// $Id: IncludeCommand.java,v 1.1 2010/06/15 12:21:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the SSI <code>include</code> command.  (CGI
 * scripts <em>can</em> be included, simply by providing a so-called
 * virtual path to a CgiResource).
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class IncludeCommand extends BasicCommand {
    private final static String NAME = "include" ;
    public String getName() { return NAME; }

    private static final String[] keys =
    {
	"virtual",
	"file",
	"ifheader",
	"else"
    } ;

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables) 
    {
	// Get the relevant parameters
	Object[] values = parameters.getMany(keys) ;
	String targetName = (String) values[0] ;
	if(targetName == null)
	    targetName = (String) values[1] ;
	String ifheader = (String) values[2] ;
	String alt = (String) values[3] ;

	if(targetName == null) return null ; // Nothing to include 

	if(ifheader != null &&
	   !request.getOriginal().hasHeader(ifheader)) {
	    if(alt == null) return null ;
	    else targetName = alt ;
	}

	// If we are at an illegal depth, don't include.
	Integer depth = (Integer) variables.get("depth") ;
	int maxDepth = ((Integer)variables.get("maxDepth")).intValue() ;

	if(maxDepth != 0 && depth.intValue()>maxDepth) {
	    Reply reply = ssiframe
		.createCommandReply(request,HTTP.OK) ;
	    reply.setContent("[recursion depth limit exceeded]") ;
	    
	    handleSimpleIMS(request,reply) ;
	    return reply ;
	}
	
	Request subReq = null ;
	Reply subRep = null ;
	
	try {
	    // Prepare an internal request
	    subReq =
		prepareRequest(request,
			       new URL(ssiframe.getURL(request)
				       , targetName)
			       .toString(),
			       variables,
			       depth) ;
	    
	    // Obtain a reply for it
	    subRep = 
	      (Reply) ssiframe.getFileResource().getServer().perform(subReq) ;
	    
	    // If it has status NOT_MODIFIED, it means the included
	    // ssiframe was also SSI, and we don't calculate anything
	    // here.
	    // Otherwise, see if we can reply NOT_MODIFIED.
	    if(subRep.getStatus() != HTTP.NOT_MODIFIED) {
		long ims = request.getIfModifiedSince() ;
		
		if(ims==-1) {
		    Long IMS = (Long)
			request.getState(STATE_IF_MODIFIED_SINCE) ;
		    if(IMS != null) ims = IMS.longValue() ;
		}
		
		long lmd = subRep.getLastModified() ;
		lmd -= lmd % 1000 ; // this is annoying
		
		if(ims != -1 && lmd != -1 && ims>=lmd) {
		    subRep.setStatus(HTTP.NOT_MODIFIED) ;
		    //close the stream
		    subRep.openStream().close();
		}
	    }
	    
	    return subRep ;
	    
	} catch(MalformedURLException ex) {
	    Reply reply = ssiframe
		.createCommandReply(request,HTTP.OK) ;
	    reply.setContent("[malformed URL]") ;
	    handleSimpleIMS(request,reply) ;
	    return reply ;
	} catch(Exception ex) {
	    Reply reply = ssiframe
		.createCommandReply(request,HTTP.OK) ;
	    reply.setContent("[error including: "+targetName+"]") ;
	    handleSimpleIMS(request,reply) ;
	    return reply ;
	}
    }

    private Request prepareRequest(Request request,
				   String url,
				   Dictionary variables,
				   Integer depth)
    {
	Request subReq = (Request) request.getClone() ;

	subReq.setURLPath(url) ;
	long ims = request.getIfModifiedSince() ;
	if(ims != -1) {
	    subReq.setHeaderValue(Request.H_IF_MODIFIED_SINCE,null) ;
	    subReq.setState(STATE_IF_MODIFIED_SINCE,
			    new Long(ims)) ;
	}

	subReq.setState(SSIFrame.STATE_VARIABLES,
			variables) ;

	subReq.setState(SSIFrame.STATE_DEPTH,
			new Integer(depth.intValue()+1)) ;
	
	
	return subReq ;
    }

  public String getValue(Dictionary variables, 
			 String variable,
			 Request request) {
    return "null";
  }

}
