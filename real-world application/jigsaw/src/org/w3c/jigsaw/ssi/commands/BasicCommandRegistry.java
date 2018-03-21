// BasicCommandRegistry.java
// $Id: BasicCommandRegistry.java,v 1.1 2010/06/15 12:21:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;
import java.util.Hashtable;

import java.io.PrintStream;

import org.w3c.www.http.HTTP ;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * An implementation of CommandRegistry that uses a hash table
 * to store the commands.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class BasicCommandRegistry extends CommandRegistry
{

    Hashtable /*<String,Command>*/ commands = null ;

    public BasicCommandRegistry()
    {
	commands = new Hashtable(23) ;
    }

    public void registerCommand(Command cmd)
    {
	commands.put( cmd.getName().toLowerCase(),
		      SSIFrame.debug
		      ? getDebugWrapperCommand (cmd)
		      : cmd ) ;
    }

    protected Command getDebugWrapperCommand(Command cmd) {
	if (cmd instanceof ControlCommand)
	    return new DebugWrapperControlCommand(cmd);
	else
	    return new DebugWrapperCommand(cmd);
    }

    public Command lookupCommand(String name)
    {
	Command cmd = (Command)
	    commands.get(name.toLowerCase()) ;
	if(cmd != null) return cmd ;
	else return new DefaultCommand(name) ;
    }

    public Dictionary initVariables(SSIFrame ssiframe,
				    Request request,
				    Dictionary variables)
    {
	return variables ;
    }
}

class DefaultCommand implements Command {
    private String badCommand ;

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }

    DefaultCommand(String badCommand)
    {
	this.badCommand = badCommand ;
    }

    public String getName()
    {
	return null ;
    }

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	Reply reply =
	    ssiframe.createCommandReply(request,HTTP.OK) ;

	reply.setContent("[SSIFrame: unknown command \""+badCommand+"\"]");
	
	return reply ;
    }

    public boolean modifiedSince(long date,
				 SSIFrame ssiframe,
				 Request request,
				 ArrayDictionary parameters,
				 Dictionary variables)
    {
	return false;
    }

  public String getValue(Dictionary variables, String variable, 
			 Request request) {
    return "null";
  }

}

class DebugWrapperCommand implements Command {

    Command cmd ;

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	System.out.println("@@@@ command accept caching : "+
			   cmd.acceptCaching());
	return cmd.acceptCaching();
    }

    public DebugWrapperCommand(Command cmd)
    {
	this.cmd = cmd ;
	System.out.println("@@@@ Added command: "+cmd.getName()) ;
    }

    public final Reply execute(SSIFrame ssiframe,
			       Request request,
			       ArrayDictionary parameters,
			       Dictionary variables)
    {
	System.out.println("@@@@ Executing command: "+cmd.getName()+
			   " "+parameters) ;
	return cmd.execute(ssiframe,request,parameters,variables) ;
    }
			 
    public final String getName()
    {
	return cmd.getName() ;
    }

    public String getValue(Dictionary variables, 
			   String variable, 
			   Request request) 
    {
	String value = cmd.getValue(variables, variable, request) ;
	System.out.println("@@@@ Get Value "+cmd.getName()+" : "+value);
	return value;
    }

}

class DebugWrapperControlCommand extends DebugWrapperCommand 
                                 implements ControlCommand 
{
    /**
     * register the command position in the structure
     * witch store the SSIFrame.
     */
    public void setPosition(SSIFrame ssiframe,
			    Request request,
			    CommandRegistry registry,
			    ArrayDictionary parameters,
			    Dictionary variables,
			    int position)
    {
	System.out.println("@@@@ "+cmd.getName()+
			   " SetPosition ["+position+"]");
	((ControlCommand)cmd).setPosition(ssiframe, request, registry, 
					  parameters, variables, position);
    }

    /**
     * Give the next position in the structure witch
     * store the SSIFrame.
     * @return An integer
     * @exception ControlCommandException if action failed.
     */
    public int jumpTo(SSIFrame ssiframe,
		      Request request,
		      CommandRegistry registry,
		      ArrayDictionary parameters,
		      Dictionary variables)
	throws ControlCommandException
    {
	int pos = ((ControlCommand)cmd).jumpTo(ssiframe, request, 
					       registry, parameters, 
					       variables);
	System.out.println("@@@@ "+cmd.getName()+" Jump to "+pos);
	return pos;
    }

    public DebugWrapperControlCommand(Command cmd) {
	super(cmd);
    }
}


