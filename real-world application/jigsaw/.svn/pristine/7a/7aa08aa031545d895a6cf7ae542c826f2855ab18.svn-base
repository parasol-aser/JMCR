// DirectoryResource.java
// $Id: IfCommand.java,v 1.1 2010/06/15 12:21:53 smhuang Exp $
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
 * Implementation of the SSI <code>if</code> command.  
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class IfCommand implements ControlCommand {
    private final static String  NAME  = "if";
    private final static boolean debug = true;

    private static final String keys[] = { 
	"name",
	"command",
	"var",
	"equals"
    };

    protected static Hashtable ifstore = null;

    static {
	ifstore = new Hashtable(23);
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return false;
    }

    /**
     * Returns the (String) value of the given variable.
     * @return a String instance.
     */
    public String getValue(Dictionary variables, String var, Request request) {
	return null;
    }

    protected static int getPosition(String name) 
	throws ControlCommandException    
    {
	Integer pos = (Integer)ifstore.get(name);
	if (pos == null)
	    throw new ControlCommandException(NAME,"Position unknown.");
	else return pos.intValue();
    }

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
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	if (name != null)
	    ifstore.put(ssiframe.getResource().getURLPath()+
			":"+name, new Integer(position));
    }
    /**
     * Executes this command. Might modify variables.
     * Must <em>not</em> modify the parameters.
     * <P> It may handle conditional requests, <em>except</em> that if
     * it replies with a status of HTTP.NOT_MODIFIED, it <em>must</em>
     * still reply with a content (the same content that it would have
     * returned for an inconditional request).  This is because
     * further SSI commands down the line may decide thay they have
     * been modified, and then a content must be emitted by SSIFrame.
     *
     * @param request the original HTTP request
     * @param parameters The parameters for this command
     * @param variables The global variables for the parse 
     * @return a Reply with the output from the command */
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
	    return false;
	Command cmd = registry.lookupCommand(command);
	String value = cmd.getValue(variables,var, request);
	return value.equals(equals);
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
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	if (name != null) {
	    if (check(registry,parameters,variables,request))
		return getPosition(ssiframe.getResource().getURLPath()+":"+
				   name)+1;
	    try {
		return 
		  (ElseCommand.getPosition(ssiframe.getResource().getURLPath()+
					   ":"+name)+1);
	    } catch (ControlCommandException ex) {
		return 
		 (EndifCommand.getPosition(ssiframe.getResource().getURLPath()+
					   ":"+name)+1);
	    }
	}
	throw new ControlCommandException(NAME,"name not initialized.");    
    }
    /** 
     * Returns the name of this command. <em>(Case sensitivity is up to
     * the <code>lookupCommand</code> method in the command registry.)</em>
     *
     * @return the name of the command
     * @see org.w3c.jigsaw.ssi.commands.CommandRegistry#lookupCommand
     */
    public String getName() {
	return NAME;
    }

}
