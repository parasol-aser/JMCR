// DirectoryResource.java
// $Id: CounterCommand.java,v 1.1 2010/06/15 12:21:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands;

import java.util.Dictionary;

import org.w3c.www.http.HTTP;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.ssi.SSIFrame;

/**
 * Implementation of the SSI <code>counter</code> command.  
 * Used to do things like cpt = cpt + 1.
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */
public class CounterCommand implements Command {
    private final static String  NAME  = "cpt";
    private final static boolean debug = true;

    private static final String keys[] = {
	"name",
	"init",
	"incr",
	"value"
    };

    protected final int defaultinit = 0;

    public String getName() {
	return NAME;
    }

    public String getValue(Dictionary variables, String var, Request request) {
	return String.valueOf(getCounterValue(variables, var));
    }

    protected void initCounterValue(Dictionary d, String name, String value) {
	d.put(getClass().getName()+"."+name, new Integer(value));
    }

    protected void changeCounterValue(Dictionary d, String name, String incr) {
	int change       = (Integer.valueOf(incr)).intValue();
	int value = getCounterValue(d,name) + change;
	d.put(getClass().getName()+"."+name, new Integer(value));
    }

    protected int getCounterValue(Dictionary d, String name) {
	Integer value = (Integer)d.get(getClass().getName()+"."+name);
	if (value != null) 
	    return value.intValue();
	else 
	    return defaultinit;
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }

    public Reply execute(SSIFrame ssiframe
			 , Request request
			 , ArrayDictionary parameters
			 , Dictionary variables) {
	Object values[] = parameters.getMany(keys);
	String name     = (String) values[0];
	String init     = (String) values[1];
	String incr     = (String) values[2];
	String value     = (String) values[3];
	String text     = null;
	if (name != null) {
	    if (init != null)
		initCounterValue(variables,name,init);
	    if (incr != null) {
		changeCounterValue(variables,name,incr);
	    }
	    if (value != null) {
		text = String.valueOf(getCounterValue(variables,name));
	    }
	}
	Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
	if (text != null)
	    reply.setContent(text);
	return reply;
    }

}
