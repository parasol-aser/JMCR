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
// $Id: SSLSocketClientFactory.java,v 1.2 2010/06/15 17:53:09 smhuang Exp $

package org.w3c.jigsaw.https.socket;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Constructor;
/* import java.lang.reflect.InvocationHandler; */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/* import java.lang.reflect.Proxy; */

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;

import java.security.cert.CertificateException;

import java.security.spec.InvalidParameterSpecException;

import javax.net.ServerSocketFactory;

import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.socket.SocketClient;
import org.w3c.jigsaw.http.socket.SocketClientFactory;
import org.w3c.jigsaw.http.socket.SocketClientState;

import org.w3c.jigsaw.https.SSLAdapter;

import org.w3c.util.ObservableProperties;

/**
 * @author Thomas Kopp, Dialogika GmbH
 * @version 1.1, 27 December 2000, 6 February 2004
 *
 * This class extends a Jigsaw SocketClientFactory designed for the
 * http protocol
 * in order to supply a SocketClientFactory for the https protocol
 * in accordance with the JSSE API.
 *
 * Three legal tricks are applied for working around if required:
 * Proxy classes are used for addressing multiple inheritance and 
 * non-official api.
 * Non-static access via introspection provides for addressing static api.
 * The java.lang.Object type is used for mapping non-official types.
 */
public class SSLSocketClientFactory extends SocketClientFactory {
    
    /**
     * The used api-part of javax.net.ssl.KeyManagerFactory.
     * A static interface KeyManagerFactory can be used under a
     * real proxy approach.
     */
    private static final class KeyManagerFactory extends Delegator {
        
        /**
         * Creates the specified pseudo-proxy front end.
         *
         * @param target  the target object in use
         */
        private KeyManagerFactory(Object target) {
            super(target);
        }
        
        /**
         * Supplies the default factory algorithm.
         *
         * @return the default algorithm
         */
        public /* static */ String getDefaultAlgorithm() { 
            try {
		return (String)invoke("getDefaultAlgorithm", null, null); 
            }
            catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }       
        
        /**
         * Generates a key manager factory.
         *
         * @param algorithm  the name of the factory algorithm
         * @return  a key manager factory instance
         * @throws NoSuchAlgorithmException if the factory algorithm is
	 * unavailable
         */
        public /* static */ Object getInstance(String algorithm) 
            throws NoSuchAlgorithmException {
            try {
                  return invoke("getInstance", 
                                new Class[] {String.class}, 
                                new Object[] {algorithm});
            }
            catch (NoSuchAlgorithmException ex) {
                  throw ex;
            }
            catch (Exception ex) {
                  RuntimeException rex = new RuntimeException(ex.toString());
                  SSLAdapter.fillInStackTrace(rex, ex);
                  throw rex;
            }
        }

        /**
         * Initializes this factory.
         *
         * @param ks  the underlying keystore
         * @param password  the key access password in use
         * @throws KeyStoreException  if initialization fails
         * @throws NoSuchAlgorithmException  if the specified algorithm
	 *         is unavailable
         * @throws UnrecoverableKeyException  if the key in question
	 *         cannot be recovered
         */
        public void init(KeyStore ks, char[] password) 
            throws KeyStoreException, NoSuchAlgorithmException, 
	           UnrecoverableKeyException
	{
            try {
		invoke("init", 
		       new Class[] {KeyStore.class, char[].class}, 
		       new Object[] {ks, password});
            } catch (KeyStoreException ex) {
		throw ex;
            } catch (NoSuchAlgorithmException ex) {
		throw ex;
            } catch (UnrecoverableKeyException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }

        /**
         * Alternatively initializes this factory.
         *
         * @param parameters the manager factory parameters
	 *                   (unavailable prior to JDK 1.4)
         * @throws InvalidAlgorithmParameterException  if initialization fails
         */
        public void init(Object parameters) 
            throws InvalidAlgorithmParameterException 
	{
            try {
		invoke("init", 
		       new Class[] {Object.class}, 
		       new Object[] {parameters});
            } catch (InvalidAlgorithmParameterException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
        
        /**
         * Supplies the available key managers of this factory.
         *
         * @return  the available key managers
         */
        public Object getKeyManagers() {
            try {
		return invoke("getKeyManagers", null, null); 
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
    }
    
    
    /**
     * The used api-part of javax.net.ssl.TrustManagerFactory.
     * A static interface TrustManagerFactory can be used under a
     * real proxy approach.
     */
    private static final class TrustManagerFactory extends Delegator {
	
        /**
         * Creates the specified pseudo-proxy front end.
         *
         * @param target  the target object in use
         */
        private TrustManagerFactory(Object target) {
            super(target);
        }
        
        /**
         * Supplies the default factory algorithm.
         *
         * @return the default algorithm
         */
        public /* static */ String getDefaultAlgorithm() { 
            try {
		return (String)invoke("getDefaultAlgorithm", null, null); 
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }       
        
        /**
         * Generates a trust manager factory.
         *
         * @param algorithm  the name of the factory algorithm
         * @return  a trust manager factory instance
         * @throws NoSuchAlgorithmException  if the factory algorithm
	 *         is unavailable
         */
        public /* static */ Object getInstance(String algorithm) 
            throws NoSuchAlgorithmException 
	{
            try {
		return invoke("getInstance", 
			      new Class[] {String.class}, 
			      new Object[] {algorithm});
            } catch (NoSuchAlgorithmException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
	
        /**
         * Initializes this factory.
         *
         * @param ks  the underlying keystore
         * @throws KeyStoreException  if initialization fails
         */
        public void init(KeyStore ks) 
            throws KeyStoreException
	{
            try {
		invoke("init", 
		       new Class[] {KeyStore.class}, 
		       new Object[] {ks});
            } catch (KeyStoreException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }

        /**
         * Alternatively initializes this factory.
         *
         * @param parameters  the manager factory parameters
	 *        (unavailable prior to JDK 1.4)
         * @throws InvalidAlgorithmParameterException  if initialization fails
         */
        public void init(Object parameters) 
            throws InvalidAlgorithmParameterException
	{
            try {
		invoke("init", 
		       new Class[] {Object.class}, 
		       new Object[] {parameters});
            } catch (InvalidAlgorithmParameterException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
        
        /**
         * Supplies the available trust managers of this factory.
         *
         * @return  the available truat managers
         */
        public Object getTrustManagers() {
            try {
		return invoke("getTrustManagers", null, null); 
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
    }

    
    /**
     * The used api-part of javxx.net.ssl.SSLContext.
     * A static interface SSLContext can be used under a real proxy approach.
     */
    private static final class SSLContext extends Delegator {
        
        /**
         * Creates the specified pseudo-proxy front end.
         *
         * @param target  the target object in use
         */
        private SSLContext(Object target) {
            super(target);
        }
        
        /**
         * Generates an ssl context, which implements the specified
	 * secure socket protocol.
         *
         * @param protcol  the name of protocol implementation
         * @return  an ssl context instance
         * @throws NoSuchAlgorithmException if the specified implementation
	 *                                  is not available
         */
        public /* static */ Object getInstance(String protocol) 
            throws NoSuchAlgorithmException
	{
            try {
		return invoke("getInstance", 
			      new Class[] {String.class}, 
			      new Object[] {protocol});
            } catch (NoSuchAlgorithmException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }

        /**
         * Initializes this context.
         *
         * @param km  the key manager array used
         * @param tm  the trust manager array used
         * @param random  the secure random used for initializing seed
         * @throws KeyManagementException  if initialization fails
         */
        public void init(Object km, Object tm, SecureRandom random) 
            throws KeyManagementException
	{
            try {
		invoke("init", 
		       new Class[] {Object.class, Object.class,
					SecureRandom.class}, 
		       new Object[] {km, tm, random});
            } catch (KeyManagementException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
        
        /**
         * Supplies an ssl server socket factory.
         *
         * @return  a server socket factory instance.
         */
        public SSLServerSocketFactory getServerSocketFactory() {
            try {
		return (SSLServerSocketFactory)invoke("getServerSocketFactory",
						      null, null); 
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
    }
    
    
    /**
     * The generic manager factory paremeters bridge.
     * A static interface ManagerFactoryParametersFactory can be used under
     * a real proxy approach.
     */
    private static final class ManagerFactoryParametersFactory 
	extends Delegator
    {
        
        /**
         * Creates the specified pseudo-proxy front end.
         *
         * @param target  the target object in use
         */
        private ManagerFactoryParametersFactory(Object target) {
            super(target);
        }
        
        /**
         * Generates a manager factory parameters instance.
         *
         * @param path  the generic path argument for a parameters instance
         * @param password  the password for a parameters instance
         * @return  a manager factory parameters instance
         * @throws InvalidAlgorithmParameterException  if the specified
	 *                                         arguments are not suitable
         * @throws NoSuchMethodException  if the specified method
	 *                                         is not available
         */
        public /* static */ Object getInstance(String path, String password) 
            throws InvalidAlgorithmParameterException, NoSuchMethodException
	{
            try {
		return invoke("getInstance", 
			      new Class[] {String.class, String.class}, 
			      new Object[] {path, password});
            } catch (InvalidAlgorithmParameterException ex) {
		throw ex;
            } catch (NoSuchMethodException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
        
        /**
         * Generates a manager factory parameters instance.
         *
         * @param path  the generic path argument for a parameters instance
         * @return  a manager factory parameters instance
         * @throws InvalidAlgorithmParameterException  if the specified
	 *         arguments are not suitable
         * @throws NoSuchMethodException  if the specified method is not
	 *         available
         */
        public /* static */ Object getInstance(String path) 
            throws InvalidAlgorithmParameterException, NoSuchMethodException
	{
            try {
		return invoke("getInstance", 
			      new Class[] {String.class}, 
			      new Object[] {path});
            } catch (InvalidAlgorithmParameterException ex) {
		throw ex;
            } catch (NoSuchMethodException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
        
        /**
         * Generates a manager factory parameters instance.
         *
         * @return a manager factory parameters instance
         * @throws InvalidAlgorithmParameterException  if the specified
	 *         arguments are not suitable
         * @throws NoSuchMethodException  if the specified method is not
	 *         available
         */
        public /* static */ Object getInstance() 
            throws InvalidAlgorithmParameterException, NoSuchMethodException
	{
            try {
		return invoke("getInstance", null, null);
            } catch (InvalidAlgorithmParameterException ex) {
		throw ex;
            } catch (NoSuchMethodException ex) {
		throw ex;
            } catch (Exception ex) {
		RuntimeException rex = new RuntimeException(ex.toString());
		SSLAdapter.fillInStackTrace(rex, ex);
		throw rex;
            }
        }
    }
    
    
    /**
     * The standard delegation pattern used as a quasi-proxy implementation.
     * Unfortunately, a real proxy requires at least JDK 1.3.
     */
    private static class Delegator /* implements InvocationHandler */ {
        
        /**
         * The facade type in use
         */
        private final Class facade;
     
        /**
         * The delegation target type
         */
        private final Class type;
        
        /**
         * The delegation target object
         */
        private final Object peer;
        
        /** 
         * Constructs a delegator instance.
         *
         * @param target  the target object in use
         * @param source  the source interface in use
         */
        public Delegator(Object target, Class source) {
            if (target instanceof Class) {
		// static delegation
		type = (Class)target;
		peer = null;
            } else {
		// object delegation
		type = target.getClass();
		peer = target;
            }
            facade = source;
        }
        
        /** 
         * Constructs a delegator instance.
         *
         * @param target  the target object in use
         */
        public Delegator(Object target) {
            this(target, null);
        }
        
        /**
         * Delegates the specified method.
         *
         * @param name  the accessed method name
         * @param deftypes  the method type
         * @param args  the method arguments
         * @return  the method invocation result
         * @throws Throwable  if the invocation fails
         */
        /*
	  public Object invoke(String method, Class[] types, Object[] args)
                   throws Throwable {
                String name = method.getName();
                Class[] deftypes = method.getParameterTypes();
            ...
        */
        public Object invoke(String name, Class[] deftypes, Object[] args)
	    throws Exception
	{
            int count = ((args != null) ? args.length : 0);
            Class[] types = new Class[count];
            Object[] param = new Object[count];
            for (int i = 0; i < count; i++) {
                Object arg = args[i];
                // a delegator proxy being aware of its own, hence
                // supplying its target object and interface
                // type if applicable
                Class art = null;
                if (null != arg) {
		    art = arg.getClass();
		    /*
                   if (Proxy.isProxyClass(art)) {
                     InvocationHandler handler=Proxy.getInvocationHandler(arg);
                      if (Delegator.class == handler.getClass()) {
                         // supply the underlying peer instance
                         arg = ((Delegator)handler).peer;      
                         // supply the wrapped interface type
                         Class[] cls = art.getInterfaces();
                         if ((null != cls)&&(cls.length == 1)) {
                            art = cls[0];
                         }
                      }
                   }
		    */
		    if (arg instanceof Delegator) {
			Delegator ref = (Delegator)arg;
			arg = ref.peer;
			if (null != ref.facade) {
			    art = ref.facade;
			}
		    }
                }
		
                // runtime types overwrite interface types
                types[i] = ((null != art) ? art :
                            ((null != deftypes)&&(i < deftypes.length) ? 
                             deftypes[i] : Object.class));
                param[i] = arg;
            }
            Method m = type.getMethod(name, types);
            return m.invoke(peer, param);
        }
	
        /**
         * Supplies a proxy for this delegator.
         *
         * @param source  the interface in use
         * @return  an delegation proxy instance
         */
        /*
           public Object getProxy(Class source) {
              return Proxy.newProxyInstance(source.getClassLoader(), 
                                             new Class[] { source }, this);
            }
         */
    }
    
    /**
     * The client authentication support level indicator 
     * (for JSSE backward compatibility),
     * which also indicates the api level in question.
     */
    private static final boolean supportsOptionalClientAuth;
    
    /**
     * The implementation nmespace depending on the api level in question.
     */
    private static final String implementationNamespace;
    
    static {
        boolean supported = false;
        try {
            Class c = javax.net.ssl.SSLServerSocket.class;
            Class cp[] = { java.lang.Boolean.TYPE };
            Method ic = null;
            supported = (null != c.getMethod("setWantClientAuth", cp));
        } catch (Exception ex) {
            supported = false;
        } finally {
            supportsOptionalClientAuth = supported;
            implementationNamespace = (supported ? 
                                       "javax.net.ssl." : 
                                       "com.sun.net.ssl.");
        }
    }
    

    /**
     * The property key for the system protocol package lookup
     */
    public static final String PROTOCOL_HANDLER_S="java.protocol.handler.pkgs";
    
    /**
     * static flag for enabling debug output if applicable
     */
    private static boolean debug = false;
    
    /**
     * The context used for creating a server socket factory
     */
    private SSLContext context = null;
    
    /**
     * The daemon of this factory
     */
    private httpd daemon = null;
    
    /**
     * The daemon bind address for this factory
     */
    private InetAddress bindAddr = null;

    /**
     * The daemon bind address for this factory
     */
    private int maxClients = 0;

    /**
     * factory method for creating a secure server socket
     * @return a new server socket instance
     * @throws java.io.IOException due to socket creation problems
     */
    public ServerSocket createServerSocket()
	throws IOException
    {
	int port = daemon.getPort();
	int clients = Math.max(128, maxClients);
	ServerSocket serversocket = null;
	if (bindAddr == null) {
	    serversocket = getFactory().createServerSocket(port, clients);
	} else {
	    serversocket = getFactory().createServerSocket(port, clients,
							   bindAddr);
	}
	// tk, 1 February 2004,
	// added optional client authentication,
	// which is forced, if a truststore is configured and
	// the org.w3c.jigsaw.ssl.authenticate is not set to false
	if (serversocket instanceof SSLServerSocket) {
	    ObservableProperties props = daemon.getProperties();
        
        // decide client authentication based on trust configuration
	    boolean mandatory;
	    mandatory = props.getBoolean(SSLProperties.MUST_AUTHENTICATE_P,
					 false);
	    boolean generic;
	    generic = props.getBoolean(SSLProperties.TRUSTSTORE_GENERIC_P,
				       false);
	    String trust;
	    trust = props.getString(SSLProperties.TRUSTSTORE_PATH_P,
				    null);
	    
	    boolean authenticate = mandatory||generic||
		                   ((null != trust)&&(trust.length() > 0));
	    
	    if (authenticate) {
		SSLServerSocket sslsocket = (SSLServerSocket)serversocket;
		if (mandatory) {
		    sslsocket.setNeedClientAuth(true);
		} else {
		    if (supportsOptionalClientAuth) {
			sslsocket.setWantClientAuth(true);
		    } else {
			throw new SSLProtocolException("Optional client "+
				      "authentication not supported by the"+
				      " current api level. Consider upgrading"+
				      " your api or using obligatory client"+
				      " authentication or using server "+
				      "authentication only");
		    }
		}
	    }
	}
	return serversocket;
    }

    /**
     * Adds a security provider.
     *
     * @param provider  the provider class name in question
     * @throws java.lang.ClassNotFoundException  if the provider is unavailable
     * @throws java.lang.IllegalAccessException  if the provider has no
     *         accessible default constructor
     * @throws java.lang.InstantiationException  if the provider cannot be
     *         instantiated
     */
    private static final void addProvider(String provider) 
        throws ClassNotFoundException, IllegalAccessException,
	       InstantiationException
    {
        if (null != provider) {
	    if (null == Security.getProvider(provider)) {
		Class support = Class.forName(provider);
		Provider supplier = (Provider)support.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
		Security.addProvider(supplier);
		if (debug) {
		    System.out.println("Added new security provider: " + 
				       supplier.getInfo() + ".");
		}
	    }
        }
    }

    /**
     * Sets the protocol handler.
     *
     * @param handler  the handler class name in question
     */
    private static final void setHandler(String handler) {
        if (null != handler) {
	    System.setProperty(PROTOCOL_HANDLER_S, handler);
	    if (debug) {
		System.out.println("Set new protocol handler: "+handler+".");
	    }
        }
    }
    
    /**
     * Loads a key store for read access.
     *
     * @param props  the underlying property set 
     * @param typekey  the store type property key
     * @param pathkey  the keystore path property key
     * @param passkey  the password property key
     * @return  the loaded keystore
     * @throws KeyStoreException  if initialization fails
     * @throws java.io.IOException  if keystore cannot be loaded
     * @throws NoSuchAlgorithmException  if the integrity check is inavailable
     * @throws CertificateException  if the a certificate is inaccessible
     */
    private static final KeyStore getStore(ObservableProperties props, 
                                           String typekey, String pathkey,
					   String passkey) 
        throws KeyStoreException, IOException, NoSuchAlgorithmException,
	       CertificateException
    {
        String storepath = props.getString(pathkey, null);
        if (null != storepath) {
            if ("".equals(storepath.trim())) storepath = null;
        }
	String storepass = props.getString(passkey, null);
        if ((null != storepath)||(null != storepass)) {
	    String storetype = props.getString(typekey,
					       KeyStore.getDefaultType());
	    KeyStore store = KeyStore.getInstance(storetype);
	    
	    store.load((null != storepath) ? 
		       new BufferedInputStream(new FileInputStream(storepath)):
		       null,
		  (null != storepass) ? storepass.toCharArray() : new char[0]);
	    return store;
        } else {
	    return null;
	}
    }
    
    /**
     * Instantiates a manager factory parameters object.
     *
     * @param props  the underlying property set 
     * @param typekey  the store type property key
     * @param pathkey  the keystore path property key
     * @param passkey  the password property key
     * @return the specified manager factory parameters instance
     * @throws InvalidParameterSpecException  if api support is not sufficient
     * @throws InvalidAlgorithmParameterException  if generic initialization
     *         fails
     * @throws ClassNotFoundException  if the provider is unavailable
     */
    private static final Object getParams(ObservableProperties props, 
                                          String typekey, String pathkey,
					  String passkey) 
        throws InvalidParameterSpecException,
	       InvalidAlgorithmParameterException,
	       ClassNotFoundException 
    {
        if (supportsOptionalClientAuth) {
	    String paratype = props.getString(typekey, null);
	    if ((null != paratype)&&(paratype.length() > 0)) {
		Class parameterFactory = Class.forName(paratype);
	    //Added by Jeff Huang
	    //TODO: FIXIT
		String path = props.getString(pathkey, null);
		String pass = props.getString(passkey, null);

		ManagerFactoryParametersFactory mfpboot;
		mfpboot =new ManagerFactoryParametersFactory(parameterFactory);
		Object mfpload = null;
		try {
                    mfpload = mfpboot.getInstance(path, pass);
		} catch (NoSuchMethodException ex) {
                    try {
			mfpload = mfpboot.getInstance(path);
                    } catch (NoSuchMethodException sub) {
			try {
			    mfpload = mfpboot.getInstance();
			} catch (NoSuchMethodException next) {
			    throw new InvalidAlgorithmParameterException(
				"Factory specified by type property has no "+
				"suitable instantiation method");
			}
                    }
		}
              
		Class managerFactoryParameters = 
		    Class.forName(implementationNamespace +
				                   "ManagerFactoryParameters");
	    //Added by Jeff Huang
	    //TODO: FIXIT
		if (managerFactoryParameters.isInstance(mfpload)) {
		    return new Delegator(mfpload, managerFactoryParameters);
		} else {
		    throw new InvalidAlgorithmParameterException(
			"Factory specified by type property does not "+
			"supply manager factory parameters");
		}
	    } else {
		throw new InvalidAlgorithmParameterException("No manager "+
		     "factory parameter class specified as the type property");
	    }
        } else {
	    throw new InvalidParameterSpecException("Generic manager "+
		"factory parameters not supported by the current api level. " +
		"Consider upgrading your api or using a classic keystore");
	}
    }

    /**
     * Creates an ssl context.
     *
     * @param props  the underlying property set 
     * @return  the ssl context used for the socket factory
     * @throws ClassNotFoundException  if the provider is unavailable
     * @throws KeyStoreException  if keystore initialization fails
     * @throws IOException  if keystore cannot be loaded
     * @throws NoSuchAlgorithmException  if the integrity check is unavailable
     * @throws InvalidParameterSpecException  if api support is not sufficient
     * @throws InvalidAlgorithmParameterException  if generic initialization
     *         fails
     * @throws CertificateException  if the a certificate is inaccessible
     * @throws UnrecoverableKeyException  if the key in question cannot
     *         be recovered
     * @throws InstantiationException  if the specified factory is abstract
     * @throws IllegalAccessException  if factory constructor is unreachable
     * @throws InvocationTargetException  if factory initialization fails
     * @throws KeyManagementException  if initialization fails
     */
    private static final SSLContext createContext(ObservableProperties props)
        throws ClassNotFoundException, KeyStoreException, IOException, 
               NoSuchAlgorithmException, InvalidParameterSpecException, 
               InvalidAlgorithmParameterException, CertificateException, 
               UnrecoverableKeyException, InstantiationException,
               IllegalAccessException, InvocationTargetException,
               KeyManagementException 
    {
	
	    //Added by Jeff Huang
	    //TODO: FIXIT
    	
	// switch according to api-level
	Class keyManagerFactory   = Class.forName(implementationNamespace + 
						  "KeyManagerFactory");
	Class trustManagerFactory = Class.forName(implementationNamespace + 
						  "TrustManagerFactory");
	Class sslContext          = Class.forName(implementationNamespace +
						  "SSLContext");
	
              // the ugly but legal key manager factory bootstrap
	KeyManagerFactory kmfboot = new KeyManagerFactory(keyManagerFactory);
	String kmftype = props.getString(SSLProperties.KEYMANAGER_TYPE_P,null);
	Object kmfload = kmfboot.getInstance((null != kmftype) ? 
					     kmftype : 
					     kmfboot.getDefaultAlgorithm());
	KeyManagerFactory kmf = new KeyManagerFactory(kmfload);
	
	boolean kgen = props.getBoolean(SSLProperties.KEYSTORE_GENERIC_P,
				       SSLProperties.DEFAULT_KEYSTORE_GENERIC);
	if (kgen) {
	    // generic key material instantiation (not prior to before JDK 1.4)
	    Object kmfp = getParams(props, 
				    SSLProperties.TRUSTSTORE_TYPE_P,
				    SSLProperties.TRUSTSTORE_PATH_P,
				    SSLProperties.TRUSTSTORE_PASSWORD_P);
	    
	    kmf.init(kmfp);
	} else {
	    KeyStore ks = getStore(props, 
				   SSLProperties.KEYSTORE_TYPE_P,
				   SSLProperties.KEYSTORE_PATH_P,
				   SSLProperties.KEYSTORE_PASSWORD_P);
	    
	    // reusing the store password for key access
	    String keypass = props.getString(SSLProperties.KEYSTORE_PASSWORD_P,
					     null);
	    
	    kmf.init(ks, (null != keypass ? keypass.toCharArray() : 
			                    new char[0]));
	}
	
	// the ugly but legal trust manager factory bootstrap
	TrustManagerFactory tmfboot;
	tmfboot = new TrustManagerFactory(trustManagerFactory);
	String tmftype = props.getString(SSLProperties.TRUSTMANAGER_TYPE_P,
					 null);
	Object tmfload = tmfboot.getInstance((null != tmftype) ? tmftype :
					        tmfboot.getDefaultAlgorithm());
	TrustManagerFactory tmf = new TrustManagerFactory(tmfload);
	
	boolean tgen = props.getBoolean(SSLProperties.TRUSTSTORE_GENERIC_P,
				     SSLProperties.DEFAULT_TRUSTSTORE_GENERIC);
	if (tgen) {
	    // generic trust material instantiation (not < JDK 1.4)
	    Object tmfp = getParams(props, 
				    SSLProperties.TRUSTSTORE_TYPE_P,
				    SSLProperties.TRUSTSTORE_PATH_P,
				    SSLProperties.TRUSTSTORE_PASSWORD_P);
	    tmf.init(tmfp);
	} else {
	    KeyStore ts = getStore(props, 
				   SSLProperties.TRUSTSTORE_TYPE_P,
				   SSLProperties.TRUSTSTORE_PATH_P,
				   SSLProperties.TRUSTSTORE_PASSWORD_P);
	    tmf.init(ts);
	}
	
	// accessing the protocol type
	String protocol = props.getString(SSLProperties.PROTOCOL_NAME_P, 
					  SSLProperties.DEFAULT_PROTOCOL_NAME);
	
	// the ugly but legal ssl context bootstrap
	SSLContext ctxboot = new SSLContext(sslContext);
	Object ctxload = ctxboot.getInstance(protocol);
	SSLContext context = new SSLContext(ctxload);
	context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), 
		     new SecureRandom());
	return context;
    }
    
    /**
     * method for intializing this factory
     * @param server the daemon of this factory
     */
    public void initialize(httpd server) {
        super.initialize(server);
        daemon = server;
        daemon.registerPropertySet(new SSLProperties(daemon));
        ObservableProperties props = daemon.getProperties();
	
        try {
            
	    // Providers or protocols are switched by default
	    // in a compatible way as postulated by the JDK 1.4 policies
	    String provider;
	    provider = props.getString(SSLProperties.SECURITY_PROVIDER_P,
				       (supportsOptionalClientAuth ? null :
				     SSLProperties.DEFAULT_SECURITY_PROVIDER));
	    addProvider(provider);
	    
	    String handler = props.getString(SSLProperties.PROTOCOL_HANDLER_P,
					  (supportsOptionalClientAuth ? null :
				      SSLProperties.DEFAULT_PROTOCOL_HANDLER));
	    setHandler(handler);
	    
	    context = createContext(props);
	    
	    String bindAddrName = props.getString(BINDADDR_P, null);
	    if (bindAddrName != null) {
		try {
		    bindAddr = InetAddress.getByName(bindAddrName);
		} catch (Exception ex) {
		    bindAddr = null;
		}
	    } else {
		bindAddr = null;
	    }
	    
	    maxClients = props.getInteger(MAXCLIENTS_P, MAXCLIENTS);
	    
        } catch (Exception ex) {
	    String mes;
	    mes = "Unable to initialize secure socket provider";
	    daemon.fatal(ex, mes);
	    if (debug) {
		System.err.println(mes);
		ex.printStackTrace();
	    }
	    RuntimeException rex;
	    rex = new RuntimeException(mes);
	    SSLAdapter.fillInStackTrace(rex, ex);
	    throw rex;
        }
    }

    /**
     * method for handling a dynamic property modification
     * @param name the name of the property modified
     * @return true if and only if the modification has been handled 
     * successfully
     */
    public boolean propertyChanged(String name) {
        if (super.propertyChanged(name)) {
	    ObservableProperties props = daemon.getProperties();
	    
	    try {
		if (name.equals(MAXCLIENTS_P)) {
                    int newmax = props.getInteger(MAXCLIENTS_P, -1);
                    if (newmax > maxClients) {
			for (int i = maxClients-newmax; --i >= 0; ) {
			    addClient(true);
			}
                    } else if (newmax > 0) {
			maxClients = newmax;
                    }
                    return true;
		} else if (name.equals(BINDADDR_P)) {
		    bindAddr = InetAddress.getByName(
			props.getString(BINDADDR_P, null));
		    return true;
		} else if ((name.equals(SSLProperties.KEYSTORE_GENERIC_P)) ||
			   (name.equals(SSLProperties.KEYSTORE_PATH_P)) ||
			   (name.equals(SSLProperties.KEYSTORE_TYPE_P)) ||
			   (name.equals(SSLProperties.KEYSTORE_PASSWORD_P)) ||
			   (name.equals(SSLProperties.TRUSTSTORE_GENERIC_P)) ||
			   (name.equals(SSLProperties.TRUSTSTORE_PATH_P)) ||
			   (name.equals(SSLProperties.TRUSTSTORE_TYPE_P)) ||
			   (name.equals(SSLProperties.TRUSTSTORE_PASSWORD_P))||
			   (name.equals(SSLProperties.PROTOCOL_NAME_P))) {
		    
		    context = createContext(props);
		    return true;
		} else {
		    return true;
		}
	    } catch (Exception ex) {
		String mes;
		mes = "Unable to re-initialize secure socket provider";
		daemon.fatal(ex, mes);
		if (debug) {
                    System.err.println(mes);
                    ex.printStackTrace();
		}
		// RuntimeException rex;
		// rex = new RuntimeException(sub);
		// SSLAdapter.fillInStackTrace(rex, cause);
		// throw rex;
		return false;
	    }
        } else {
	    return false;
	}
    }
    
    /**
     * server sockt factory creation
     * @return the secure server socket factory
     * @throws java.io.IOException due to factory creation problems
     */
    private ServerSocketFactory getFactory()
	throws SSLKeyException
    {
        ServerSocketFactory factory;
        factory = ((null != context) ? 
                   context.getServerSocketFactory() : 
                   // make best effort to obtain a factory
                   SSLServerSocketFactory.getDefault()); 
	
        String[] supported =
	          ((SSLServerSocketFactory)factory).getSupportedCipherSuites();
        if (debug) {
	    System.out.println("Supported suites:");
	    for (int i = 0; i < supported.length; i++) {
		System.out.println("          " + supported[i]);
	    }
	    String[] enabled =
		((SSLServerSocketFactory)factory).getDefaultCipherSuites();
	    System.out.println("Enabled suites:");
	    for (int i = 0; i < enabled.length; i++) {
		System.out.println("         " + enabled[i]);
	    }
        }
	
        if (supported.length < 1) {
            SSLKeyException ex = new SSLKeyException(
		"No cipher suites supported by this "
		+ "SSL socket factory. "
		+ "Please check your factory, key store, "
		+ "store password and cerificates");
	    daemon.fatal(ex, ex.getMessage());
	    if (debug) {
		ex.printStackTrace();
	    }
	    throw ex;
        }
        return factory;
    }

    /**
     * Factory for creating a new client for this pool.
     * @param server  the target http daemon
     * @param state  the client state holder
     * @return a new socket client
     */
    protected SocketClient createClient(httpd server,
					SocketClientState state) {
        return new SSLSocketClient(server, this, state);
    }
}
