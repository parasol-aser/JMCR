// ConfigCommand.java
// $Id: ConfigCommand.java,v 1.1 2010/06/15 12:21:55 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Dictionary;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the <code>config</code> SSI command.
 * Used to set the <code>sizefmt</code> and <code>datefmt</code> variables,
 * which control the output of file sizes and dates.
 * @author Antonio Ramirez <anto@mit.edu>
 */
public class ConfigCommand implements Command {
    private final static String NAME = "config" ;

    public Reply execute(SSIFrame ssiframe,
			 Request request,
			 ArrayDictionary parameters,
			 Dictionary variables)
    {
	String parName = null, parValue = null ;

	for(int i=0;i<parameters.capacity();i++) {
	    parName = (String) parameters.keyAt(i) ;
	    if(parName==null) continue ;

	    parValue = (String) parameters.elementAt(i) ;

	    // Check to see if parameters and/or values are permissible
	    if(parName.equals("sizefmt")) {
		if(!parValue.equalsIgnoreCase("bytes")
		   && !parValue.equalsIgnoreCase("abbrev"))
		    continue ;
		else variables.put(parName,parValue.toLowerCase()) ;
	    } else if(parName.equals("datefmt")) {
		variables.put(parName,parValue) ;
	    }
	}
	
	return null ;
    }

    public String getName()
    {
	return NAME;
    }

    public String getValue(Dictionary variables, String variable,
			   Request request) {
	return "null";
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }
}
