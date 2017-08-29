// ClientException.java
// $Id: ClientException.java,v 1.1 2010/06/15 12:21:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http;

import org.w3c.tools.resources.ResourceException;

/**
 * ClientException is used to terminate a channel with a specific client.
 * Each client is represented within the server by some Client instance
 * which is used to keep track of it.
 * When such a client context errs severly (ie IO errors, bad HTTP spoken, etc)
 * the connections has to be cleaned up and closed, that's the purpose of this
 * exception.
 * @see Client
 */

public class ClientException extends ResourceException {
    public Client    client = null ;
    public Exception ex     = null;

    public ClientException (Client client, String msg) {
	super (msg) ;
	this.client = client ;
    }

    public ClientException (Client client, Exception ex) {
	super (ex.getMessage()) ;
	this.client = client ;
	this.ex     = ex;
    }

    public ClientException (Client client, Exception ex, String msg) {
	super (msg) ;
	this.client = client ;
	this.ex     = ex;
    }
}


