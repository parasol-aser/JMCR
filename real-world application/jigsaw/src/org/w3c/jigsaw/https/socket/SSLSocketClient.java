/**
 * Copyright (c) 2000/2001 Thomas Kopp
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
// $Id: SSLSocketClient.java,v 1.1 2010/06/15 12:28:29 smhuang Exp $

package org.w3c.jigsaw.https.socket;

import java.net.Socket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.socket.SocketClient;
import org.w3c.jigsaw.http.socket.SocketClientFactory;
import org.w3c.jigsaw.http.socket.SocketClientState;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.1, 6 February 2004
 * 
 * This class extends a Jigsaw SocketClient designed for the 
 * http protocol
 * in order to supply a SocketClient for the https protocol including 
 * client authentication in accordance with the JSSE API.
 */
public class SSLSocketClient extends SocketClient {

    /**
     * Creates an empty client.
     *
     * @param server  the target http(s( daemon
     * @param pool  the client factory in use
     * @param state  the socket cliente state in use
     */

    protected SSLSocketClient(httpd server, SocketClientFactory pool,
			      SocketClientState state) {
	super(server, pool, state);
    }
    
    /**
     * Supplies the ssl session associated with the underlying socket.
     *
     * @return  the associated ssl session or null
     */
    public SSLSession getSession() {
        // the socket instance is inherited from the super-class
        if (socket instanceof SSLSocket) {
	    return ((SSLSocket)socket).getSession();
        }
        return null;
    }
}
