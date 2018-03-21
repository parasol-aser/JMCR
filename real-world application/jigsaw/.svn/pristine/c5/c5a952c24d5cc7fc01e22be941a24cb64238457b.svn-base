// ExecCommand.java
// $Id: ExecCommand.java,v 1.1 2010/06/15 12:21:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.io.IOException;
import java.io.StringBufferInputStream;

import java.util.Dictionary;
import java.util.Vector;

import org.w3c.www.http.HTTP ;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.DelayedInputStream;
import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the SSI <code>exec</code> command.
 * It inserts the output from a CGI script or a shell command in the
 * document. Note that in the Jigsaw architecture CGI scripts are just
 * another resource class, so that no distinction is made between
 * executing a CGI script or including a file.
 * Relies on variables set by DefaultCommandRegistry.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class ExecCommand extends BasicCommand {
    private final static String NAME = "exec" ;

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {	    
	String cmd = (String) parameters.get("cmd") ;
	if(cmd != null) return executeCmd(ssiframe,
					  request,
					  parameters,
					  variables,
					  cmd) ;

	// "cgi" would be handled here (just like an include...)
	Reply reply = ssiframe.createDefaultReply(request,HTTP.OK) ;
	reply.setContent("[unimplemented: use include]") ;
	handleSimpleIMS(request,reply) ;
	return reply ;
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }

    private Reply executeCmd(SSIFrame ssiframe,
			     Request request,
			     ArrayDictionary parameters,
			     Dictionary variables,
			     String cmd)
    {
	// Deny command if secure flag was set
	if(((Boolean)variables.get("secure")).booleanValue()) {
	    Reply reply = ssiframe
		.createCommandReply(request,HTTP.OK) ;
	    reply.setContent("[exec cmd not allowed: secure SSI]") ;
	    return reply ;
	}
	
	ArrayDictionary ssiVars = (ArrayDictionary)
	    variables.get("ssiVars") ;

	if(ssiVars==null) {
	    Reply reply = ssiframe
		.createCommandReply(request,HTTP.OK) ;
	    reply.setContent("[exec: can't find environment]") ;
	    handleSimpleIMS(request,reply) ;
	    return reply ;
	}

	String[] env  = new String[ssiVars.size()] ;
	for(int i=0,j=0;
	    i<ssiVars.capacity() && ssiVars.keyAt(i)!=null;
	    i++)
	    env[j++] =
		(ssiVars.keyAt(i).toString())
		+ "="
		+ (ssiVars.elementAt(i).toString()) ;

	Reply reply = new Reply(request.getClient()) ;
	reply.setStream(new DelayedProcessStream(cmd,env)) ;
	return reply ;
    }

    private final void addEnv(Vector env,String var,String val)
    {
	if(var!=null && val!=null)
	    env.addElement(var+'='+val) ;
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

class DelayedProcessStream extends DelayedInputStream {
    private String cmd = null ;
    private String[] env = null ;

    DelayedProcessStream(String cmd, String[] env)
    {
	this.cmd = cmd ;
	this.env = env ;
    }

    protected final void init()
    {
	Process proc = null ;
	try {
	    proc = Runtime.getRuntime().exec(cmd,env) ;
	} catch(IOException ex) {
	    proc = null ;
	}

	if(proc == null)
	    in = new StringBufferInputStream
		("[exec: cannot start process: \""+cmd+"\"]") ;
	else 
	    in = proc.getInputStream() ;
    }

}


