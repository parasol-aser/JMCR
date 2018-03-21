// JdbcBeanInterface.java
// $Id: JdbcBeanInterface.java,v 1.1 2010/06/15 12:27:30 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc; 

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface JdbcBeanInterface {

    /**
     * Set the JDBC driver
     * @param jdbcDriver the jdbc driver
     */
    public void setJdbcDriver(String jdbcDriver);

    /**
     * Get the JDBC driver
     * @return the jdbc driver
     */
    public String getJdbcDriver();

    /**
     * Set the Jdbc username property
     * @param jdbcUser the username
     */
    public void setJdbcUser(String JdbcUser);

    /**
     * get the Jdbc username property
     * @return the Jdbc username property
     */
    public String getJdbcUser();

    /**
     * Set the password property
     * @param jdbcPassword the password
     */
    public void setJdbcPassword(String jdbcPassword);

    /**
     * Get the password property
     * @return the Jdbc password 
     */
    public String getJdbcPassword();

    /**
     * Set the Jdbc URI
     * @param jdbcURI the URI (ie: <b>jdbc:protocol://host/db</b>)
     */
    public void setJdbcURI(String jdbcURI);

    /**
     * Get the Jdbc URI
     * @return the URI (ie: <b>jdbc:protocol://host/db</b>)
     */
    public String getJdbcURI();

    /**
     * Set the max number os simultaneous Jdbc connections
     * @param maxConn the max number of connections
     */
    public void setMaxConn(int maxConn);

    /**
     * Get the max number os simultaneous Jdbc connections
     * @return the max number of connections
     */
    public int getMaxConn();

    /**
     * Set the name of the SQL table
     * @param jdbcTable the SQL table name
     */
    public void setJdbcTable(String jdbcTable);

    /**
     * Return the name of the SQL table
     * @return the SQL table name
     */
    public String getJdbcTable();

    /**
     * Set the read-only flag
     * @param readonly
     */
    public void setReadOnly(boolean readonly);

    /**
     * Is this table read-only? (default is false)
     * @return a boolean
     */
    public boolean getReadOnly();

    public JdbcBeanInterface getDefault();

    /**
     * Get our SQL serializer
     * @return a JdbcBeanSerializer instance
     */
    public JdbcBeanSerializer getSerializer();

    /**
     * Add a PropertyChangeListener to the listener list. The listener 
     * is registered for all properties.
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener to the listener list. 
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
