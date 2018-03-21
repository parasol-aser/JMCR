// DebugFilter.java
// $Id: DebugFilter.java,v 1.1 2010/06/15 12:25:15 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A simple debug filter, that will dump traffic
 * This filter will dump the outgoing request, and the incomming reply to
 * the java process standard output.
 * <p>Usefull for debugging !
 */

public class DebugFilter implements PropRequestFilter {

    /**
     * This filter doesn't handle exceptions.
     * @param request The request that triggered the exception.
     * @param ex The triggered exception.
     * @return Always <strong>false</strong>.
     */

    public boolean exceptionFilter(Request request, HttpException ex) {
	return false;
    }

    /**
     * PropRequestFilter implementation - Initialize the filter.
     * Time to register ourself to the HttpManager.
     * @param manager The HTTP manager that is initializing ourself.
     */

    public void initialize(HttpManager manager) {
	// We install ourself as a global filter, we are cool !
	manager.setFilter(this);
    }

    /**
     * The ingoing filter just dumps the request.
     * @param request The request to be filtered.
     * @exception HttpException is never thrown.
     */

    public Reply ingoingFilter(Request request) 
	throws HttpException
    {
	// On the way in, emit the request to stdout:
        System.out.println("\nREQUEST : \n");
	request.dump(System.out);
	return null;
    }

    /**
     * The outgoing filter just dumps the reply.
     * @param request The request that is filtered.
     * @param reply The corresponding reply.
     * @exception HttpException is never thrown.
     */

    public Reply outgoingFilter(Request Request, Reply reply) 
	throws HttpException
    {
	// On the way out, emit the reply to stdou:
        System.out.println("\nREPLY : \n");
	reply.dump(System.out);
	return null;
    }

    /**
     * We do not maintain any in-memory cached state.
     */

    public void sync() {
	return;
    }

}
