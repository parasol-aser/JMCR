// CommandRegistry.java
// $Id: CommandRegistry.java,v 1.1 2010/06/15 12:21:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * This class represents a group of SSI commands.  This is an abstract
 * class.  A concrete subclass of this class is supplied to the
 * SSIFrame for finding the commands present in the document.
 *
 * @see org.w3c.jigsaw.ssi.commands.BasicCommandRegistry
 * @see org.w3c.jigsaw.ssi.commands.DefaultCommandRegistry
 * @author Antonio Ramirez <anto@mit.edu>
 */

public abstract class CommandRegistry implements java.io.Serializable {
    /** 
     * Look up a command from its name.
     * (Should <em>never</em> return null, and have a pseudo-command
     * to handle non-existent commands).
     *
     * @param name the name
     * @return the command
     */
    public abstract Command lookupCommand(String name) ;

    /**
     * Initialize execution variables.  Called before any of the SSI
     * commands in the documents are executed.  This method augments
     * or modifies the dictionary given as argument.  If the variable
     * dictionary is null, it may create a new one and return it.
     * SSIFrame will always call this method with variables set to
     * null. Its existence is mainly to facilitate the subclassing of
     * an existing registry.
     *
     * @param request the HTTP request
     * @param variables other variables previously defined
     * @return the modified/augmented set of variables */
    public abstract Dictionary initVariables(SSIFrame ssiframe,
					     Request request,
					     Dictionary variables) ;
}


