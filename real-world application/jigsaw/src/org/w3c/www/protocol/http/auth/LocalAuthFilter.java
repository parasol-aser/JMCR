// LocalAuthFilter.java
// $Id: LocalAuthFilter.java,v 1.1 2010/06/15 12:27:25 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.auth;

import java.net.URL;

import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;
import org.w3c.www.protocol.http.RequestFilter;

public class LocalAuthFilter implements RequestFilter {
    HttpCredential credentials = null;
    boolean        isProxy     = false;

    /**
     * Install a local authentication filter for the given target.
     * @param manager The HttpManager.
     * @param for The domain to which given credentials apply.
     * @param credentials The credentials to use for this domain.
     */

    public static void installLocalAuth(HttpManager manager
					, URL domain
					, HttpCredential credentials) {
	// Create the cheap but efficient filter:
	LocalAuthFilter filter = new LocalAuthFilter(credentials);
	// Install it appropriately:
	URL includes[] = new URL[1];
	includes[0] = domain;
	manager.setFilter(includes, null, filter);
    }

    /**
     * Install a proxy authentication filter globally.
     * @param manager The HttpManager.
     * @param credentials The credentials to use for this domain.
     */

    public static void installProxyAuth(HttpManager manager
					, HttpCredential credentials) {
	// Create the cheap but efficient filter:
	LocalAuthFilter filter = new LocalAuthFilter(credentials);
	// Install it appropriately:
	filter.isProxy = true;
	manager.setFilter(filter);
    }

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
     * The ingoing filter just decorates the request with the credentials.
     * @param request The request to be processed.
     * @return Always <strong>null</strong>.
     */

    public Reply ingoingFilter(Request request) {
	// Just add our credentials to the request:
	if ( isProxy )
	    request.setProxyAuthorization(credentials);
	else
	    request.setAuthorization(credentials);
	return null;
    }

    /**
     * The outgoing filter is empty here.
     */

    public Reply outgoingFilter(Request request, Reply reply) {
	return null;
    }

    /**
     * We don't maintain cached informations.
     */

    public void sync() {
    }

    LocalAuthFilter(HttpCredential credentials) {
	this.credentials = credentials;
    }

}


