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
// $Id: webdavsd.java,v 1.1 2010/06/15 12:29:19 smhuang Exp $

package org.w3c.jigsaw.webdavs;

import java.net.URL;

import org.w3c.jigsaw.daemon.ServerHandlerInitException;
import org.w3c.jigsaw.https.SSLAdapter;
import org.w3c.jigsaw.webdav.webdavd;

import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ProtocolException;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.0.2, 30 January 2001
 * 
 * This class extends a Jigsaw webdav daemon
 * in order to supply a Jigsaw webdav daemon with https support
 * in accordance with the JSSE API.
 */
public class webdavsd extends webdavd {

    /**
     * reference to the TLS support adapter of this daemon
     */
    private SSLAdapter adapter = null;
    
    /**
     * constructor of this daemon
     */
    public webdavsd() {
        super();
        adapter = new SSLAdapter(this);
    }
    
    /**
     * clone method of this daemon
     */
    protected Object clone()
        throws CloneNotSupportedException 
    {
        webdavsd daemon = (webdavsd)(super.clone());
        daemon.adapter = new SSLAdapter(daemon);
        return daemon;
    }
    
    /**
     * method for initializing the properties of this daemon
     * @exception ServerHandlerInitException if initialization fails
     */
    protected void initializeProperties() 
        throws ServerHandlerInitException 
    {
	super.initializeProperties();
        adapter.initializeProperties();
    }

    /**
     * method for supplying a reply interface of a request
     * @param req current request to be handled
     * @return reply for a current request
     */
    public ReplyInterface perform(RequestInterface req) 
	throws ProtocolException, ResourceException  
    {
        adapter.perform(req);
    	return super.perform(req);
    }
    
    /**
     * method for supplying the uri of this daemon
     * @return uri of this daemon
     */
    public URL getURL() {
        return adapter.getURL();
    }
}
