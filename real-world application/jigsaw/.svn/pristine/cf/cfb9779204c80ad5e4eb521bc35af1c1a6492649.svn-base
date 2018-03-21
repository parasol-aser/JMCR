// ProcessFilter.java
// $Id: ProcessFilter.java,v 1.2 2010/06/15 17:52:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.StringArrayAttribute;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpReplyMessage;

import org.w3c.jigsaw.http.HTTPException;
import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

class ProcessFeeder extends Thread {
    Process      proc    = null ;
    OutputStream out     = null ;
    InputStream  in      = null ;
    int          count = -1 ;

    public void run () {
	try {
	    byte buffer[] = new byte[4096] ;
	    int  got      = -1 ;
	    int emitted   = 0;

	    // Send the data to the target process:
	    if ( count >= 0 ) {
		while ( (count > 0) && ((got = in.read(buffer)) > 0) ) {
		    out.write (buffer, 0, got) ;
		    count   -= got ;
		    emitted += got;
		}
	    } else {
		while ( (got = in.read(buffer)) > 0 ) {
		    out.write (buffer, 0, got) ;
		    emitted += got;
		}
	    }
	    // Clean up the process:
	    out.flush() ;
	    out.close() ;
	    proc.waitFor() ;
	} catch (Exception e) {
	    System.out.println ("ProcessFeeder: caught exception !") ;
	    e.printStackTrace() ;
	}
    }
	

    ProcessFeeder (Process proc, InputStream in) {
	this (proc, in, -1) ;
    }
	
    ProcessFeeder (Process proc, InputStream in, int count) {
	this.proc   = proc ;
	this.out    = proc.getOutputStream() ;
	this.in     = in ;
	this.count  = count ;
    }
}

/**
 * This filter process a normal entity body through any process. 
 * <p>Although, you would probably include the filtered resource inside a 
 * NegotiatedResource to make sure the target browser accepts this
 * content-encoding.
 */

public class ProcessFilter extends ResourceFilter {
    /**
     * Attribute index - The command we should pass the stream through.
     */
    protected static int ATTR_COMMAND = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.ProcessFilter") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The command attribute
	a = new StringArrayAttribute("command"
				     , null
				     , Attribute.EDITABLE|Attribute.MANDATORY);
	ATTR_COMMAND = AttributeRegistry.registerAttribute(cls, a) ;
    }

    /**
     * A pointer to our runtime object.
     */
    protected Runtime runtime  = null ;

    /**
     * Get the command we should process the reply stream through.
     */

    public String[] getCommand() {
	return (String[]) getValue(ATTR_COMMAND, null) ;
    }

    /**
     * Process the request output through the provided process filter.
     * @exception org.w3c.tools.resources.ProtocolException 
     * If processing should be interrupted, because an abnormal situation 
     * occured. 
     */

    public ReplyInterface outgoingFilter (RequestInterface req, 
					  ReplyInterface rep)
	throws ProtocolException
    {
	Request request = (Request) req;
	Reply   reply   = (Reply) rep;
	Process       process   = null ;
	ProcessFeeder feeder    = null ;
	String        command[] = getCommand() ;

	// Some sanity checks:
	if (reply.getStatus() != HTTP.OK)
	    return null;
	InputStream in = reply.openStream() ;
	if ( in == null )
	    return null;
	if ( command == null ) {
	    Reply error = request.makeReply(HTTP.INTERNAL_SERVER_ERROR) ;
	    error.setContent("The process filter of this resource is not "
			     + " configured properly (it has no command).");
	    throw new HTTPException(error);
	}
	// Okay, run the reply stream through the process:
	try {
	    process = runtime.exec (command) ;
	} catch (IOException e) {
	    Reply error = request.makeReply(HTTP.NOT_FOUND) ;
	    error.setContent("The filter's process command "+
			     command[0]+
			     " wasn't found: "+e.getMessage()) ;
	    throw new HTTPException (error);
	}
	if ( reply.hasContentLength() ) {
	    feeder = new ProcessFeeder (process
					, in
					, reply.getContentLength()) ;
	} else {
	    feeder = new ProcessFeeder (process, in) ;
	}
	feeder.start() ;
	reply.setContentLength(-1) ;
	reply.setStream(process.getInputStream());
	return reply ;
    }

    /**
     * Initialize a process filter.
     * Just get a pointer to the runtime object.
     * @param values The default attribute values.
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	this.runtime = Runtime.getRuntime();
    }

}


