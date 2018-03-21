// httpdSecurityManager.java
// $Id: httpdSecurityManager.java,v 1.1 2010/06/15 12:21:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

/**
 * The <b>jhttpd</b> security manager. 
 * You really need this if you plan to accept agent execution on your server.
 * Although, in next versions, the security manager may be used to limit
 * your server users in what entities they can export.
 * <p>Add the <b>-s</b> command line argument to <b>jhttpd</b> invocation to 
 * set the security manager to an instance of this class.
 */

public class httpdSecurityManager extends SecurityManager {
    /**
     * Name of the property indicating if agents are allowed to accept().
     * When <strong>true</strong>, this property indicates that agents are
     * allowed to use the <em>accept</em> method of ServerSockets.
     * <p>This property defaults to <strong>false</strong>.
     */
    public static final String SM_AGENT_ACCEPT_P = "org.w3c.jigsaw.security.agent.accept";
    /**
     * Name of the property indicating if agents are allowed to write().
     * When <strong>true</strong>, this property indicates that agents
     * are allowed to use the <em>write</em> method of output streams.
     * <p>This property defaults to <strong>false</strong>.
     */
    public static final String SM_AGENT_WRITE_P  = "org.w3c.jigsaw.security.write";
    /**
     * Name of the property indicating if security maneger is debuged.
     * When <strong>true</strong> this property makes the security manager
     * emits debugging traces.
     * <p>This property defaults to <strong>false</strong>.
     */
    public static final String SM_DEBUG_P        = "org.w3c.jigsaw.debug" ;

    private static boolean debug        = false ;
    private static boolean agent_accept = false ;
    private static boolean agent_write  = false ;

    static {
	// Get properties:
	agent_accept = Boolean.getBoolean (SM_AGENT_ACCEPT_P) ;
	agent_write  = Boolean.getBoolean (SM_AGENT_WRITE_P) ;
	debug        = Boolean.getBoolean (SM_DEBUG_P) ;
    }

    protected final boolean inAgent () {
//	ClassLoader loader = currentClassLoader() ;
// Agent are not available yet with new Jigsaw design
//	if ( loader == null ) {
//	    return false ;
//	} else if ( loader instanceof org.w3c.jigsaw.agent.AgentClassLoader ) {
//	    return true ;
//	} else {
//	    throw new SecurityException ("Unknown class loader: " + loader) ;
//	}
	return false ;
    }

    protected void trace (String msg) {
	if ( inAgent() )
	    System.out.println ("[agent-security] " + msg) ;
	else
	    System.out.println ("[httpd-security] " + msg) ;
    }

    public void checkAccept (String host, int port) {
	if ( debug )
	    trace ("checkAccept: " + host + "@" + port) ;
	if ( inAgent() && ( ! agent_accept ) )
	    throw new SecurityException() ;
	return ;
    }

    public void checkAccess (Thread thr) {
	if ( debug )
	    trace ("checkAccess: " + thr.getName()) ;
	if ( inAgent() )
	    throw new SecurityException ("Access denied to agents.") ;
	return ;
    }

    public void checkCreateClassLoader () {
	if ( debug )
	    trace ("checkCreateClassLoader.") ;
	if ( inAgent() )
	    throw new SecurityException ("createClassLoader denied to agents.");
	return ;
    }

    public void checkListen (int port) {
	if ( debug )
	    trace ("checkListen: " + port) ;
	if ( inAgent() )
	    throw new SecurityException ("Listen denied to agents.");
	return ;
    }

    public void checkPropertiesAccess () {
	if ( debug )
	    trace ("checkPropertiesAccess.") ;
	if ( inAgent() ) 
	    throw new SecurityException ("Properties denied to agents") ;
	return ;
    }

    public void checkRead (String file) {
	if ( debug )
	    trace ("checkRead: " + file) ;
	if ( inAgent() )
	    throw new SecurityException ("Read(file) denied to agents.");

	return ;
    }

    public void checkRead (int fd) {
	if ( debug ) 
	    trace ("checkRead: " + fd) ;
	if ( inAgent() )
	    throw new SecurityException ("Read(fd) denied to agents.");
	return ;
    }

    public void checkWrite (int fd) {
	if ( debug )
	    trace ("checkWrite: " + fd) ;
	if ( inAgent() )
	    throw new SecurityException ("Write(fd) denied to agents.");
	return ;
    }

    public void checkWrite (String file) {
	if ( debug )
	    trace ("checkWrite: " + file) ;
	if ( inAgent() && ( ! agent_write) )
	    throw new SecurityException ("write(file) denied to agents.") ;
	return ;
    }

}
