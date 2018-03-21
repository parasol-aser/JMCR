// JigsawHttpSession.java
// $Id: JigsawHttpSession.java,v 1.1 2010/06/15 12:24:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import javax.servlet.ServletContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.util.ArrayEnumeration;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JigsawHttpSession implements HttpSession {

    private JigsawHttpSessionContext sc = null;
    
    private JigsawServletContext servletContext = null;

    private String id = null;

    private long creationTime     = -1;
    private long lastAccessedTime = -1;

    private boolean isValid = false;
    private boolean isNew   = false;

    private Cookie cookie = null;

    private Hashtable values = null;

    private int maxidle = -1;

    /**
     * Returns the identifier assigned to this session. An HttpSession's 
     * identifier is a unique string that is created and maintained by
     * HttpSessionContext. 
     * @return the identifier assigned to this session 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public String getId() {
	return id;
    }

    /**
     * Returns the context in which this session is bound. 
     * @return the context in which this session is bound.
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     * @deprecated since jsdk2.1
     */
    public HttpSessionContext getSessionContext() {
	return sc;
    }

    /**
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     * @param name a string specifying the name of the object
     * @return the object with the specified name
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */
    public Object getAttribute(String name) {
	return getValue(name);
    }

    /**
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session. 
     * @return an <code>Enumeration</code> of <code>String</code> objects 
     * specifying the names of all the objects bound to	this session
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */
    public Enumeration getAttributeNames() {
	if (!isValid)
            throw new IllegalStateException("Invalid session");
	return values.keys();
    }

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     *
     * <p>After this method executes, and if the object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls 
     * <code>HttpSessionBindingListener.valueBound</code>.
     *
     * @param name the name to which the object is bound; cannot be null
     * @param value the object to be bound; cannot be null
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */

    public void setAttribute(String name, Object value) {
	putValue(name, value);
    }

    /**
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     *
     * <p>After this method executes, and if the object
     * implements <code>HttpSessionBindingListener</code>,
     * the container calls 
     * <code>HttpSessionBindingListener.valueUnbound</code>.
     * 
     * @param name the name of the object to remove from this session
     * @exception IllegalStateException	if this method is called on an
     *					invalidated session
     */
    public void removeAttribute(String name) {
	removeValue(name);
    }

    /**
     * Returns the time at which this session representation was created, 
     * in milliseconds since midnight, January 1, 1970 UTC. 
     * @return the time when the session was created 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public long getCreationTime() {
	return creationTime;
    }

    /**
     * Returns the last time the client sent a request carrying the identifier
     * assigned to the session. Time is expressed as milliseconds
     * since midnight, January 1, 1970 UTC. Application level operations, 
     * such as getting or setting a value associated with the session,
     * does not affect the access time. 
     * @return the last time the client sent a request carrying the identifier
     * assigned to the session 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public long getLastAccessedTime() {
	return lastAccessedTime;
    }

    protected void setLastAccessedTime() {
	lastAccessedTime = System.currentTimeMillis();
    }

    /**
     * Causes this representation of the session to be invalidated and removed
     * from its context. 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public void invalidate() {
        // adding unbinding events during invalidate() in accordance 
        // with servlet api, tk, 23.10.2001
        // start of modification        
        Enumeration names = values.keys();
        while (names.hasMoreElements()) {
              String name = (String)(names.nextElement());
              removeValue(name);
        }
        // end of modification
        
	isValid = false;
	sc.removeSession(id);
    }

    /**
     * Returns the object bound to the given name in the session's application
     * layer data. Returns null if there is no such binding. 
     * @param name - the name of the binding to find 
     * @return the value bound to that name, or null if the binding does 
     * not exist. 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */    
    public Object getValue(String name) {
	if (!isValid)
            throw new IllegalStateException("Invalid session");
	return values.get(name);
    }

    /**
     * Binds the specified object into the session's application layer data 
     * with the given name. Any existing binding with the same name
     * is replaced. New (or existing) values that implement the 
     * HttpSessionBindingListener interface will call its valueBound() method. 
     * @param name - the name to which the data object will be bound. 
     * This parameter cannot be null. 
     * @param value - the data object to be bound. This parameter cannot 
     * be null. 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public void putValue(String name, Object value)
    {
	if (!isValid)
            throw new IllegalStateException("Invalid session");
	removeValue(name);
        // null check added in accordance with servlet api, tk. 23.10.2001
        if (value != null) {
           values.put(name, value);
 	   if (value instanceof HttpSessionBindingListener)
	       valueBound((HttpSessionBindingListener)value, name);
        }
    }

    /**
     * Removes the object bound to the given name in the session's application
     * layer data. Does nothing if there is no object bound to the
     * given name. The value that implements the HttpSessionBindingListener 
     * interface will call its valueUnbound() method. 
     * @param name - the name of the object to remove 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public void removeValue(String name) {
	if (!isValid)
            throw new IllegalStateException("Invalid session");
	Object value = values.get(name);
	if (value != null) {
	    values.remove(name);
	    if (value instanceof HttpSessionBindingListener)
		valueUnbound((HttpSessionBindingListener)value, name);
	}
    }

    protected void valueBound(HttpSessionBindingListener value, String name) 
    {
	value.valueBound(new HttpSessionBindingEvent(this, name));
    }

    protected void valueUnbound(HttpSessionBindingListener value, String name) 
    {
	value.valueUnbound(new HttpSessionBindingEvent(this, name));
    }

    /**
     * Returns an array of the names of all the application layer data objects
     * bound into the session. For example, if you want to delete
     * all of the data objects bound into the session, use this method to 
     * obtain their names. 
     * @return an array containing the names of all of the application layer
     * data objects bound into the session 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     * @deprecated since jsdk2.2
     */
    public String[] getValueNames() {
	if (!isValid)
            throw new IllegalStateException("Invalid session");
	String names[] = new String[values.size()];
	Enumeration e = values.keys();
	int i = 0;
	while (e.hasMoreElements()) {
	    names[i++] = (String)e.nextElement();
	}
	return names;
    }

    /**
     * A session is considered to be "new" if it has been created by the 
     * server, but the client has not yet acknowledged joining the
     * session. For example, if the server supported only cookie-based 
     * sessions and the client had completely disabled the use of
     * cookies, then calls to HttpServletRequest.getSession() would always 
     * return "new" sessions. 
     * @return true if the session has been created by the server but the 
     * client has not yet acknowledged joining the session; false otherwise 
     * @exception IllegalStateException if an attempt is made to access 
     * session data after the session has been invalidated 
     */
    public boolean isNew() {
	return isNew;
    }

    protected void setNoMoreNew() {
	isNew = false;
    }

    protected boolean isValid() {
        return isValid;
    }

    protected Cookie getCookie() {
        return cookie;
    }

    //jsdk2.1

    public void setMaxInactiveInterval(int interval) {
	maxidle = interval;
    }

    public int getMaxInactiveInterval() {
	return maxidle;
    }

    // jsdk 2.3
    public ServletContext getServletContext() {
	return servletContext;
    }

    public JigsawHttpSession(JigsawHttpSessionContext context, 
			     JigsawServletContext servletContext,
			     Cookie cookie) {
	this.values = new Hashtable();
	this.creationTime = System.currentTimeMillis();
	this.lastAccessedTime = creationTime;
	this.sc = context;
	this.id = context.addSession(this);
	this.cookie = cookie;
	this.servletContext = servletContext;
	cookie.setValue(this.id);
	isValid = true;
	isNew = true;
    }

}
