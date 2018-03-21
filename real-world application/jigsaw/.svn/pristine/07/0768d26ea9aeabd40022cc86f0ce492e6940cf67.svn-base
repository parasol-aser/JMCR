// Command.java
// $Id: ControlCommand.java,v 1.1 2010/06/15 12:21:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * This interface is used to supply implementations of SSI
 * commands. They have to be registered in a CommandRegistry, which in
 * turn is used by the SSIFrame. A control command is a command 
 * like loop or if witch can modify the way to execute commands.
 * A control command have to register is position and to know the
 * next position. A position is an integer, witch can be an array
 * index.
 * @see org.w3c.jigsaw.ssi.commands.CommandRegistry
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public interface ControlCommand extends Command {

    /**
     * register the command position in the structure
     * witch store the SSIFrame.
     */
    public void setPosition(SSIFrame ssiframe,
			    Request request,
			    CommandRegistry registry,
			    ArrayDictionary parameters,
			    Dictionary variables,
			    int position);

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
	throws ControlCommandException;

}


