// Command.java
// $Id: Command.java,v 1.1 2010/06/15 12:21:52 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * This interface is used to supply implementations of SSI
 * commands. They have to be registered in a CommandRegistry, which in
 * turn is used by the SSIFrame.
 *
 * @see org.w3c.jigsaw.ssi.commands.CommandRegistry
 * @author Antonio Ramirez <anto@mit.edu>
 */

public interface Command {
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
    public Reply execute(SSIFrame resource,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables);

    /** 
     * Returns the name of this command. <em>(Case sensitivity is up to
     * the <code>lookupCommand</code> method in the command registry.)</em>
     *
     * @return the name of the command
     * @see org.w3c.jigsaw.ssi.commands.CommandRegistry#lookupCommand
     */

    public String getName() ;

    /**
     * Returns the (String) value of the given variable.
     * @return a String instance.
     */
    public String getValue(Dictionary variables, 
			   String variable, 
			   Request request);

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching();

}


