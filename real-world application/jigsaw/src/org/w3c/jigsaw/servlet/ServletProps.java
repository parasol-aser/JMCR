// ServletProps.java
// $Id: ServletProps.java,v 1.2 2010/06/15 17:52:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.servlet;

import org.w3c.jigsaw.http.httpd;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.FileAttribute;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.jigsaw.config.PropertySet;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServletProps extends PropertySet {
    
    /**
     * Name of the property indicating the servlet log file.
     * This property indicates the name of the servlet log file to use.
     * <p>This property defaults to the <code>servlets</code> file in 
     * the server log directory.
     */
    protected static String SERVLET_LOG_FILE_P   = 
	"org.w3c.jigsaw.servlet.servlet-log-file";

    /**
     * Name of the property indicating the max number of session loaded
     * in memory.
     */
    protected static String SERVLET_TIMEOUT  = 
	"org.w3c.jigsaw.servlet.timeout";

    /**
     * Name of the property indicating the max number 
     * for single thread model servlet instance pool size limitation, tk, 20.10.2001
     */
    protected static String SERVLET_INSTANCEMAX  = 
	"org.w3c.jigsaw.servlet.instancemax";    
    
    /**
     * Name of the property indicating the max number of session loaded
     * in memory.
     */
    protected static String SERVLET_MAX_SESSION  = 
	"org.w3c.jigsaw.servlet.max-sessions";

    /**
     * Name of the property indicating the max idle time of session.
     */
    protected static String SERVLET_SESSION_IDLE = 
	"org.w3c.jigsaw.servlet.sessions-max-idle-time";

    /**
     * Name of the property indicating the max idle time of session.
     */
    protected static String SERVLET_SESSION_SWEEP = 
	"org.w3c.jigsaw.servlet.sessions-sweep-delay";

    /**
     * Name of the property indicating the session cookie name.
     */
    protected static String SERVLET_COOKIE_NAME = 
	"org.w3c.jigsaw.servlet.session.cookie.name";

    /**
     * The default Session Cookie name.
     */
    protected static String DEFAULT_COOKIE_NAME = "JIGSAW-SESSION-ID";

    /**
     * Name of the property indicating the session cookie domain.
     */
    protected static String SERVLET_COOKIE_DOMAIN = 
	"org.w3c.jigsaw.servlet.session.cookie.domain";

    /**
     * Name of the property indicating the session cookie maxage.
     */
    protected static String SERVLET_COOKIE_MAXAGE = 
	"org.w3c.jigsaw.servlet.session.cookie.maxage";

    /**
     * Name of the property indicating the session cookie path.
     */
    protected static String SERVLET_COOKIE_PATH = 
	"org.w3c.jigsaw.servlet.session.cookie.path";

    /**
     * Name of the property indicating the session cookie comment.
     */
    protected static String SERVLET_COOKIE_COMMENT = 
	"org.w3c.jigsaw.servlet.session.cookie.comment";

    /**
     * Name of the property indicating the session cookie secure flag.
     */
    protected static String SERVLET_COOKIE_SECURE = 
	"org.w3c.jigsaw.servlet.session.cookie.secure";

    /**
     * Name of the servlet PropertySet.
     */
    protected static String SERVLET_PROPS_NAME = "Servlets";

    /**
     * Attribute index - The index for our servlet log file attribute.
     */
    protected static int ATTR_SERVLET_LOG_FILE = -1 ;

    /**
     * Attribute index - The index for our servlet session max idle time.
     */
    protected static int ATTR_MAX_IDLE_TIME = -1;

    /**
     * Attribute index - The index for our servlet timeout
     */
    protected static int ATTR_SERVLET_TIMEOUT = -1;

    /**
     * Attribute index - The index for for single thread model servlet instance pool size limitation, tk, 20.10.2001
     */
    protected static int ATTR_SERVLET_INSTANCEMAX = -1;
    
    /**
     * Attribute index - The index for our servlet session max number in 
     * memory.
     */
    protected static int ATTR_SESSIONS_MAX = -1;

    /**
     * Attribute index - The index for our servlet session sweep delay.
     */
    protected static int ATTR_SESSIONS_SWEEP = -1;

    /**
     * Attribute index - The index for our session cookie name.
     */
    protected static int ATTR_SESSIONS_COOKIE_NAME = -1;

    /**
     * Attribute index - The index for our session cookie path.
     */
    protected static int ATTR_SESSIONS_COOKIE_PATH = -1;

    /**
     * Attribute index - The index for our session cookie domain.
     */
    protected static int ATTR_SESSIONS_COOKIE_DOMAIN = -1;

    /**
     * Attribute index - The index for our session cookie comment.
     */
    protected static int ATTR_SESSIONS_COOKIE_COMMENT = -1;

    /**
     * Attribute index - The index for our session cookie maxage.
     */
    protected static int ATTR_SESSIONS_COOKIE_MAXAGE = -1;

    /**
     * Attribute index - The index for our session cookie secure.
     */
    protected static int ATTR_SESSIONS_COOKIE_SECURE = -1;

   

    static {
	Class cls = null;
	Attribute a = null;

	try {
	    cls = Class.forName("org.w3c.jigsaw.servlet.ServletProps");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// The servlet log file:
	a = new FileAttribute(SERVLET_LOG_FILE_P,
			      null,
			      Attribute.EDITABLE);
	ATTR_SERVLET_LOG_FILE = AttributeRegistry.registerAttribute(cls, a);
	// The servlet session max idle time:
	a = new LongAttribute(SERVLET_SESSION_IDLE,
			      new Long(1800000),
			      Attribute.EDITABLE);
	ATTR_MAX_IDLE_TIME = AttributeRegistry.registerAttribute(cls, a);
	// The servlet timeout:
	a = new LongAttribute(SERVLET_TIMEOUT,
			      new Long(1800000),
			      Attribute.EDITABLE);
	ATTR_SERVLET_TIMEOUT = AttributeRegistry.registerAttribute(cls, a);
	// The servlet instancemax, added for single thread model servlet instance pool size limitation, tk, 20.10.2001
	a = new IntegerAttribute(SERVLET_INSTANCEMAX,
			      new Integer(0),
			      Attribute.EDITABLE);
	ATTR_SERVLET_INSTANCEMAX = AttributeRegistry.registerAttribute(cls, a);
	// The servlet sessions max number in memory:
	a = new IntegerAttribute(SERVLET_MAX_SESSION,
				 new Integer(1024),
				 Attribute.EDITABLE);
	ATTR_SESSIONS_MAX = AttributeRegistry.registerAttribute(cls, a);
	// The servlet sessions sweep delay:
	a = new IntegerAttribute(SERVLET_SESSION_SWEEP,
				 new Integer(30000),
				 Attribute.EDITABLE);
	ATTR_SESSIONS_SWEEP = AttributeRegistry.registerAttribute(cls, a);
	// The session cookie name
	a = new StringAttribute(SERVLET_COOKIE_NAME,
				DEFAULT_COOKIE_NAME,
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_NAME = 
	    AttributeRegistry.registerAttribute(cls, a);
	// The session cookie path
	a = new StringAttribute(SERVLET_COOKIE_PATH,
				"/",
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_PATH = 
	    AttributeRegistry.registerAttribute(cls, a);
	// The session cookie domain
	a = new StringAttribute(SERVLET_COOKIE_DOMAIN,
				null,
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_DOMAIN = 
	    AttributeRegistry.registerAttribute(cls, a);
	// The session cookie comment
	a = new StringAttribute(SERVLET_COOKIE_COMMENT,
				"Jigsaw Server Session Tracking Cookie",
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_COMMENT = 
	    AttributeRegistry.registerAttribute(cls, a);
	// The session cookie maxage
	a = new IntegerAttribute(SERVLET_COOKIE_MAXAGE,
				new Integer(-1),
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_MAXAGE = 
	    AttributeRegistry.registerAttribute(cls, a);
	// The session cookie secure flag
	a = new BooleanAttribute(SERVLET_COOKIE_SECURE,
				Boolean.FALSE,
				Attribute.EDITABLE);
	ATTR_SESSIONS_COOKIE_NAME = 
	    AttributeRegistry.registerAttribute(cls, a);
    }

    private static String title = "Servlet properties";

    private JigsawHttpSessionContext sessionContext = null;

    /**
     * Returns the max idle time for a session.
     * @return The max idle time in milli seconds.
     */
    public long getSessionsMaxIdleTime() {
	return ((Long) 
		getValue(ATTR_MAX_IDLE_TIME, new Long(1800000))).longValue();
    }

    /**
     * Returns the max number of session in memory.
     * @return The max number of session.
     */
    public int getMaxSessionsNumber() {
	return ((Integer)
		getValue(ATTR_SESSIONS_MAX, new Integer(1024))).intValue();
    }

    /**
     * Returns the delay between two sessions idle time check.
     * @return the delay between two sessions idle time check in milli seconds.
     */
    public int getSessionsSweepDelay() {
	return ((Integer)
		getValue(ATTR_SESSIONS_SWEEP, new Integer(30000))).intValue();
    }

    /**
     * Returns the session context.
     * @return A JigsawHttpSessionContext instance.
     */
    public JigsawHttpSessionContext getSessionContext() {
	if (sessionContext == null)
	    sessionContext = new JigsawHttpSessionContext(server,this);
	return sessionContext;
    }

    /**
     * Get this property set title.
     * @return A String encoded title.
     */

    public String getTitle() {
	return title;
    }

    ServletProps(httpd server) {
	super(SERVLET_PROPS_NAME, server);
    }

}
