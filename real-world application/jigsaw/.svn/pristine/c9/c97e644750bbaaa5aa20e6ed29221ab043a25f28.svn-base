// FSizeCommand.java
// $Id: FSizeCommand.java,v 1.1 2010/06/15 12:21:51 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.www.http.HTTP ;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the SSI <code>fsize</code> command.
 * It inserts the size of the unparsed file in the document,
 * according to the current value of the variable <code>sizefmt</code>.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class FSizeCommand extends BasicCommand {
    private final static String NAME = "fsize" ;
    private static final long MBsize = 1024*1024 ;
    private static final long KBsize = 1024 ;

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	Reply reply = ssiframe
	    .createCommandReply(request,HTTP.OK) ;
	
	Dictionary ssiVars = (Dictionary)
	    variables.get( parameters.get("here") == null
			   ? "topSsiVars"
			   : "ssiVars" ) ;

	String sizefmt = (String)
	    variables.get("sizefmt") ;

	Long Fsize = (Long)
	    ssiVars.get("X_FILE_SIZE") ;

	long fsize = Fsize.longValue() ;
	
	if(sizefmt==null || sizefmt.equalsIgnoreCase("bytes")) 
	    reply.setContent(withCommas(fsize)) ;
	else if(sizefmt.equalsIgnoreCase("abbrev")) {
	    String unit = null ;
	    long cut = 1;
	    if(fsize>=MBsize) {
		unit = " MB" ;
		cut = MBsize ;
	    } else if(fsize>=KBsize) {
		unit = " KB" ;
		cut = KBsize ; 
	    } else {
		reply.setContent(withCommas(fsize)+" bytes") ;
	    }
	    if(cut != 1) {
		double n = (double) fsize / cut ;
		long ip = (long) n ;
		int fp = (int)(100*(n - ip)) ;
		
		reply.setContent(withCommas(ip)+"."+fp+unit) ;
	    }
	} 

	handleSimpleIMS(request,reply) ;
	return reply ;

    }

    private String withCommas(long n)
    {
	String nstr = String.valueOf(n) ;
	StringBuffer buf = new StringBuffer(20) ;
	int length = nstr.length() ;
	for(int i = 0;i<length;i++) {
	    buf.append(nstr.charAt(i)) ;
	    if((length-i)%3==1 && i+1 != length)
		buf.append(',') ;
	}
	return buf.toString() ;

    }

    public String getName()
    {
	return NAME;
    }

    public String getValue(Dictionary variables, 
			   String variable, 
			   Request request) 
    {
	return "null";
    }

}
