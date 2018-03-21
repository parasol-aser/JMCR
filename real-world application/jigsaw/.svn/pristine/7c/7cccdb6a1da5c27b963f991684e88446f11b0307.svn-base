// SampleCommand.java
// $Id: SampleCommand.java,v 1.1 2010/06/15 12:21:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.www.http.HTTP ;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

public class SampleCommand extends BasicCommand {
    protected static final String NAME = "params";

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	Reply reply = ssiframe
	    .createCommandReply(request,HTTP.OK) ;
	StringBuffer sb = new StringBuffer();
	sb.append("<ul>");
	for(int i=0;
	    i<parameters.capacity() && parameters.keyAt(i) != null;
	    i++)
	    sb.append("<li>"+parameters.keyAt(i)+" = \""+
		      parameters.elementAt(i)+"\"</li>");
	sb.append("</ul>");
	reply.setContent(sb.toString());

	handleSimpleIMS(request,reply) ;
	return reply ;
    }

    public String getName()
    {
	return NAME;
    }

    public String getValue(Dictionary variables, 
			   String variable, 
			   Request request) {
	return "null";
    }

}
