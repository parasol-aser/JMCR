// JdbcBean.java
// $Id: JdbcBeanAdapter.java,v 1.1 2010/06/15 12:27:29 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc; 

import java.io.Serializable;
import java.io.IOException;
import java.util.Properties;

import java.beans.Beans;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JdbcBeanAdapter implements JdbcBeanInterface, Serializable {

    protected int        maxConn      = -1;

    protected String     jdbcDriver   = null;
    protected String     jdbcUser     = null;
    protected String     jdbcPassword = null;
    protected String     jdbcURI      = null;
    protected String     jdbcTable    = null;

    protected boolean readonly = false;

    protected PropertyChangeSupport pcs = null;

    protected JdbcBeanSerializer serializer = null;

    protected JdbcBeanInterface defaultbean = null;

    /**
     * Add a PropertyChangeListener to the listener list. The listener 
     * is registered for all properties.
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener to the listener list. 
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }

    /**
     * Set the JDBC driver
     * @param jdbcDriver the jdbc driver
     */
    public void setJdbcDriver(String jdbcDriver) {
	this.jdbcDriver = jdbcDriver;
    }

    /**
     * Get the JDBC driver
     * @return the jdbc driver
     */
    public String getJdbcDriver() {
	return jdbcDriver;
    }

    /**
     * Set the Jdbc username property
     * @param jdbcUser the username
     */
    public void setJdbcUser(String jdbcUser) {
	this.jdbcUser = jdbcUser;
    }

    /**
     * get the Jdbc username property
     * @return the Jdbc username property
     */
    public String getJdbcUser() {
	return jdbcUser;
    }

    /**
     * Set the password property
     * @param jdbcPassword the password
     */
    public void setJdbcPassword(String jdbcPassword) {
	this.jdbcPassword = jdbcPassword;
    }

    /**
     * Get the password property
     * @return the Jdbc password 
     */
    public String getJdbcPassword() {
	return jdbcPassword;
    }

    /**
     * Set the Jdbc URI
     * @param jdbcURI the URI (ie: <b>jdbc:protocol://host/db</b>)
     */
    public void setJdbcURI(String jdbcURI) {
	this.jdbcURI = jdbcURI;
    }

    /**
     * Get the Jdbc URI
     * @return the URI (ie: <b>jdbc:protocol://host/db</b>)
     */
    public String getJdbcURI() {
	return jdbcURI;
    }

    /**
     * Set the max number os simultaneous Jdbc connections
     * @param maxConn the max number of connections
     */
    public void setMaxConn(int maxConn) {
	this.maxConn = maxConn;
    }

    /**
     * Get the max number os simultaneous Jdbc connections
     * @return the max number of connections
     */
    public int getMaxConn() {
	return maxConn;
    }

    /**
     * Set the name of the SQL table
     * @param jdbcTable the SQL table name
     */
    public void setJdbcTable(String jdbcTable) {
	this.jdbcTable = jdbcTable;
    }

    /**
     * Return the name of the SQL table
     * @return the SQL table name
     */
    public String getJdbcTable() {
	return jdbcTable;
    }

    /**
     * Set the read-only flag
     * @param readonly
     */
    public void setReadOnly(boolean readonly) {
	this.readonly = readonly;
    }

    /**
     * Is this table read-only? (default is false)
     * @return a boolean
     */
    public boolean getReadOnly() {
	return readonly;
    }

    /**
     * Get our SQL serializer
     * @return a JdbcBeanSerializer instance
     */
    public JdbcBeanSerializer getSerializer() {
	if (serializer == null) {
	    serializer = new JdbcBeanSerializer(this);
	}
	return serializer;
    }

    public JdbcBeanInterface getDefault() {
	if (defaultbean == null) {
	    Class c = getClass();
	    try {
		defaultbean = (JdbcBeanInterface) 
		    Beans.instantiate(c.getClassLoader(), c.getName());
	    } catch (IOException ex) {
	    } catch (ClassNotFoundException ex) {
	    }
	}
	return defaultbean;
    }

    /**
     * Constructor
     */
    public JdbcBeanAdapter() {
	pcs        = new JdbcPropertyChangeSupport(this);
	serializer = new JdbcBeanSerializer(this);
    }

}
