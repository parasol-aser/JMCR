// DirectoryResource.java
// $Id: ExitloopCommand.java,v 1.1 2010/06/15 12:21:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;
import java.util.Hashtable;

import org.w3c.www.http.HTTP;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the SSI <code>exitloop</code> command.  
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class ExitloopCommand implements ControlCommand {
    private final static String  NAME  = "exitloop";
    private final static boolean debug = true;

    private static final String keys[] = {
	"name",
	"command",
	"var",
	"equals"
    };

    protected static Hashtable exitloops = null;

    static {
	exitloops = new Hashtable(23);
    }

    public String getValue(Dictionary variables, String var, Request request) {
	return null;
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return false;
    }

    protected static int getPosition(String name) 
	throws ControlCommandException
    {
	Integer pos = (Integer)exitloops.get(name);
	if (pos == null)
	    throw new ControlCommandException(NAME,"Position unknown.");
	else return pos.intValue();
    }

    public void setPosition(SSIFrame ssiframe,
			    Request request,
			    CommandRegistry registry,
			    ArrayDictionary parameters,
			    Dictionary variables,
			    int position) 
    {
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	if (name != null)
	    exitloops.put(ssiframe.getResource().getURLPath()+
			  ":"+name, new Integer(position));
    }

    public Reply execute(SSIFrame ssiframe
			 , Request request
			 , ArrayDictionary parameters
			 , Dictionary variables) 
    {
	return ssiframe.createCommandReply(request, HTTP.OK);
    }

    protected boolean check(CommandRegistry registry
			    , ArrayDictionary parameters
			    , Dictionary variables
			    , Request request)
    {
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	String command  = (String) values[1];
	String var      = (String) values[2];
	String equals   = (String) values[3];

	if ((command == null) || (var == null) || (equals == null))
	    return true;
	Command cmd = registry.lookupCommand(command);
	String value = cmd.getValue(variables,var,request);
	return value.equals(equals);
    }

    public int jumpTo(SSIFrame ssiframe,
		      Request request,
		      CommandRegistry registry,
		      ArrayDictionary parameters,
		      Dictionary variables)
	throws ControlCommandException    
    {
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	if (name != null) {
	    if (! check(registry,parameters,variables, request))
		return (getPosition(ssiframe.getResource().getURLPath()+":"+
				    name)+1);
	    return 
	       (EndloopCommand.getPosition(ssiframe.getResource().getURLPath()+
					   ":"+name)+1);
	}
	throw new ControlCommandException(NAME,"name not initialized.");    
    }

    public String getName() {
	return NAME;
    }

}
