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
// $Id: SSLAdapter.java,v 1.2 2010/06/15 17:53:07 smhuang Exp $

package org.w3c.jigsaw.https;

import java.io.ByteArrayInputStream;

import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.StringTokenizer;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.w3c.jigsaw.daemon.ServerHandlerInitException;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Request;

import org.w3c.jigsaw.https.socket.SSLProperties;
import org.w3c.jigsaw.https.socket.SSLSocketClient;
import org.w3c.jigsaw.https.socket.SSLSocketClientFactory;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.RequestInterface;

import org.w3c.util.ObservableProperties;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.1, 27 December 2000, 6 February 2004
 * 
 * This class supplies a Jigsaw SSL daemon adapter
 * for enabling Jigsaw https support in accordance with the JSSE API
 * wrapping SSL features in order to solve multiple inheritance problems
 */
public class SSLAdapter {
    
    /**
     * The throwable initCause method via introspection 
     * (for JDK backward compatibility)
     */
    private static final Method initCause;
    
    /**
     * The new style certificate support level indicator
     * (for JSSE backward compatibility)
     */
    private static final boolean supportsNewStyleCertificates;
    
    /**
     * The X509 factory for compatible certificate conversions
     */
    private static final CertificateFactory x509Factory;

    static {
        Class c = java.lang.Throwable.class;
        Class cp[] = { java.lang.Throwable.class };
        Method ic = null;
        try {
            ic = c.getMethod("initCause", cp);
        } catch (Exception ex) {
            ic = null;
        }
        finally {
            initCause = ic;
        }
        boolean supported = false;
        CertificateFactory fact = null;
        try {
            supported = (null != 
	      javax.net.ssl.SSLSession.class.getMethod("getPeerCertificates",
						       (Class [])null));
        } catch (Exception ex) {
            supported = false;
            try {
                fact = CertificateFactory.getInstance("X.509");
            } catch (Exception sub) {
                fact = null;
            }
        }
        finally {
            supportsNewStyleCertificates = supported;
            x509Factory  = fact;
        }
    }
	
    /**
     * flag for enabling debug output if applicable
     */
    private static boolean debug = false;
    
    /**
     * The internal no laceholder object, which has a different than the 
     * expected type
     */
    private static final Object NO_ENTRY = "null";
    
    /**
     * The servlet api spec request attribute name of the cipher suite
     */
    private static final String ALGORITHM = 
                                          "javax.servlet.request.cipher_suite";
    
    /**
     * The servlet api spec request attribute name of the key size
     */
    private static final String KEYSIZE = "javax.servlet.request.key_size";
    
    /**
     * The servlet api spec request attribute name of the certificate chain
     */
    private static final String CERTCHAIN = 
                                       "javax.servlet.request.X509Certificate";
    
    /**
     * The servlet api spec attribute value for client authentication
     */
    private static final String CLIENT_CERT_AUTH = "CLIENT_CERT";

    
    /**
     * flag indicating TLS support
     */
    private boolean ssl_enabled = false;
    
    /**
     * reference to the daemon in question
     */
    private httpd daemon = null;
    
    /**
     * uri of the daemon in question
     */
    private URL url = null;
    
    /**
     * Fills in the stack trace of a cause if possible with respect to the
     * api level.
     *
     * @param throwable  the thowable to be extended
     * @param cause  the cause to be filled in
     */  
    public static final void fillInStackTrace(Throwable throwable,
					      Throwable cause) {
        if (null != initCause) {
	    try {
		Object[] param = { cause };
		initCause.invoke(throwable, param);
	    } catch (Exception iex) {
                // ignore
            }
        }
    }
    
    /**
     * Supplies the ssl session attached to the specified request if any.
     *
     * @param request  the request in question
     * @return  the attached ssl session or null if not applicable
     */
    private static final SSLSession getSession(Request request) {
     	Client cl = request.getClient();
	if (cl instanceof SSLSocketClient) {
	    return ((SSLSocketClient)cl).getSession();
        }
        return null;
    }
    
    /**
     * An ugly way to compute the key size (to be improved)
     * @param algorithm  the algorithm name
     * @return the key size as an integer object
     */
    private static final Integer getKeySize(String algorithm) {
        if (null != algorithm) {
	    StringTokenizer parser = new StringTokenizer(algorithm, "_");
	    while (parser.hasMoreTokens()) {
		try {
		    return Integer.valueOf(parser.nextToken());
		}
		catch (NumberFormatException ex) {
		    // ignore and continue lookup
		}
	    }
        }
        return null;
    }
    
    /**
     * Supplies the calculated or cached key size.
     * @param algorithm  the algorithm name
     * @param session  the ssl underlying session
     * @return the key size as an integer object
     */
    private static final Integer getKeySize(String algorithm,
					    SSLSession session) {
       // FIXME: find a better way to compute the key size
       //        at least this ugly computation gets cached
	   //        at the moment
	Object keysize = session.getValue(KEYSIZE + "." + algorithm);
	if (keysize instanceof Integer) {
	    return (Integer)keysize;
	} else {
	    if (null == keysize) {
		Integer keysize2 = getKeySize(algorithm);
		if (null != keysize2) {
		    session.putValue(KEYSIZE+"."+algorithm, keysize2);
                    return keysize2;
		} else {
		    session.putValue(KEYSIZE+"."+algorithm, NO_ENTRY);
		    return null;
		}
	    } else {
		return null; // keysize could not be computed,
		             //i.e. equals NO_ENTRY
	    }
	}
    }
    
    /**
     * Supplies the peer certificates if available.
     *
     * @param session  the underlying ssl session
     * @return  the certificate chain or null
     * @throws SSLPeerUnverifiedException  iff certificates cannot be ontained
     */
    private static final Certificate[] getPeerCertificates(SSLSession session) 
        throws SSLPeerUnverifiedException {
        try {
	    // using introspection due to possible compatibility issues
	    if (supportsNewStyleCertificates) {
		return session.getPeerCertificates();
	    } else {
		// using deprecated method as a fallback with
		// explicit certificate conversion
		if (null != x509Factory) {
		    javax.security.cert.X509Certificate[] oldStyleCerts;
		    oldStyleCerts = session.getPeerCertificateChain();
		    if (null != oldStyleCerts) {
			int count = oldStyleCerts.length;
			X509Certificate[] newStyleCerts;
			newStyleCerts =  new X509Certificate[count];
			for (int i = 0; i < count; i++) {
			    newStyleCerts[i] = 
			     (X509Certificate)x509Factory.generateCertificate(
				 new ByteArrayInputStream(
				     oldStyleCerts[i].getEncoded()));
			}
			return newStyleCerts;
		    } else { 
			throw new SSLPeerUnverifiedException("No peer "+
						     "certificates available");
		    }
		} else {
		    throw new SSLPeerUnverifiedException("No suitable"+
		     		      " certificate compatibility applicable");
		}
	    }
	} catch (SSLPeerUnverifiedException ex) {
	    throw ex;
        } catch (Exception ex) {
	    SSLPeerUnverifiedException sub;
	    sub = new SSLPeerUnverifiedException(ex.toString());
	    fillInStackTrace(sub, ex);
	    throw sub;
        }
    }
    
    /**
     * constructor for a TLS support adapter
     * @param server reference to the daemon in question
     */
    public SSLAdapter(httpd server) {
        if (null != server) {
	    ssl_enabled = false;
	    daemon = server;
	    url = null;
        } else {
	    throw new NullPointerException("No daemon intance supplied for "+
					   " creating SSL adapter");
	}
    }
    
    /**
     * method for initializing the properties of a daemon
     * @exception ServerHandlerInitException thrown if initialization fails
     */
    public void initializeProperties() 
        throws ServerHandlerInitException 
    {
        ObservableProperties props = daemon.getProperties();
        // default to well-known factory if applicable        
        if (props.getBoolean(SSLProperties.SSL_ENABLED_P, true)) {
            String factory_class = props.getString(httpd.CLIENT_FACTORY_P,
						   null);
            if (null != factory_class) {
                try {
		    Class factory = Class.forName(factory_class);
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    ssl_enabled = 
		      (SSLSocketClientFactory.class.isAssignableFrom(factory));
                } catch (Exception ex) {
                    String error = "Initialization failed";
                    daemon.fatal(ex, error);
                    if (debug) {
                        System.out.println(error);
                        ex.printStackTrace();
                    }
                    ServerHandlerInitException sub;
                    sub = new ServerHandlerInitException(ex.getMessage());
                    fillInStackTrace(sub, ex);
                    throw sub;
                }
            } else {
		throw new ServerHandlerInitException("No socket client"
						     +" factory specified");
	    }
        } else {
	    ssl_enabled = false;
	}
        url = null;
    }

    /**
     * method for preparing a reply interface for a request
     * @param req the current request to be handled
     * @exception ProtocolException thrown if the request url is malformed
     */
    public void perform(RequestInterface req) 
	throws ProtocolException  {
	Request request = (Request)req;
	if (ssl_enabled) {
	    // set request protocol to https
	    URL url = request.getURL();
	    try {
		request.setURL(new URL("https", url.getHost(), 
				       url.getPort(), url.getFile()));
		
		// tk, 1 February 2004, added SSL client attributes
		// according to Servlet v2.4 spec
		SSLSession session = getSession(request);
		if (null != session) {
		    String algorithm = session.getCipherSuite();
		    request.setState(ALGORITHM, algorithm);
		    
		    Integer keysize = getKeySize(algorithm, session);
		    if (null != keysize) {
			request.setState(KEYSIZE, keysize);
		    }
		    
		    try {
                        Certificate[] chain = getPeerCertificates(session);
                        if (chain instanceof X509Certificate[]) {
                            X509Certificate[] x509chain;
			    x509chain = (X509Certificate[])chain;
                            request.setState(CERTCHAIN, x509chain);
                            request.setState(AuthFilter.STATE_AUTHTYPE,
					     CLIENT_CERT_AUTH);
                            if (x509chain.length > 0) {
				request.setState(AuthFilter.STATE_AUTHUSER,
				        x509chain[0].getSubjectDN().getName());
                            }
                        }
		    } catch (SSLPeerUnverifiedException ex) {
			if (debug) {
			    ex.printStackTrace();
			}
			// no certificates available, ignore
		    }
		}
	    } catch (MalformedURLException ex) {
		String error = "Bad url during switching to https";
		daemon.fatal(ex, error);
		if (debug) {
		    System.out.println(error);
		    ex.printStackTrace();
		}
		ProtocolException sub = new ProtocolException(ex.getMessage());
		fillInStackTrace(sub, ex);
		throw sub;
	    }
	}
    }
    
    /**
     * method for supplying a daemon uri
     * @return uri of the daemon in question
     */
    public URL getURL() {
        if (url == null) {
	    if (ssl_enabled) {
		try {
		    if (daemon.getPort() != 443) {
			url = new URL("https", daemon.getHost(), 
				      daemon.getPort(), "/");
		    } else {
			url = new URL("https", daemon.getHost(), "/");
		    }
		} catch (MalformedURLException ex) {
		    if (debug) {
			ex.printStackTrace();
		    }
		    throw new RuntimeException("Unable to construct "+
					       "server uri. (" + 
					       ex.getMessage() + ")");
		}
	    } else {
		try {
		    if (daemon.getPort() != 80 ) {
			url = new URL("http", daemon.getHost(), 
				      daemon.getPort(), "/");
		    } else {
			url = new URL("http", daemon.getHost(), "/");
		    }
		} catch (MalformedURLException ex) {
		    throw new RuntimeException("Unable to construct"+
					       " server uri. (" +
					       ex.getMessage() + ")");
		}
	    }
	}		
        return url;
    }
    
    /**
     * method for indicating TLS support
     * @return flag for indicating TLS support enabled
     */
    public boolean sslEnabled() {
	return ssl_enabled;
    }
}
