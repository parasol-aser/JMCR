// CountCommand.java
// $Id: CountCommand.java,v 1.1 2010/06/15 12:21:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.www.http.HTTP ;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * This command inserts the number of recorded accesses to this resource,
 * as reported by org.w3c.jigsaw.filter.CounterFilter.
 * @see org.w3c.jigsaw.filters.CounterFilter
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class CountCommand extends BasicCommand {
    private static final String NAME = "hitcount" ;

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	Reply reply =
	    ssiframe.createCommandReply(request,HTTP.OK) ;
	
	Integer count = (Integer)
	  request.getState(org.w3c.jigsaw.filters.CounterFilter.STATE_COUNT) ;
	if(count == null)
	    reply.setContent("[unknown]") ;
	else reply.setContent(count.toString()) ;

	// FIXME, NOTE:
	// the handling of weak validation should be configurable
	handleSimpleIMS(request,reply) ;
	return reply ;
    }			 

    public String getName()
    {
	return NAME ;
    }

  public String getValue(Dictionary variables, String variable, 
			 Request request) {
    return "null";
  }

}
