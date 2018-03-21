// Jdbc.java
// $Id: Jdbc.java,v 1.1 2010/06/15 12:27:30 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.jdbc;

import java.util.Properties;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Jdbc {

    public final static String MAX_CONNECTIONS_P = 
	"org.w3c.tools.jdbc.maxconn";

    public final static int DEFAULT_MAX_CONN = 10;

    public final static String JDBC_DRIVER_P =
	"org.w3c.tools.jdbc.jdbcdriver";

    public final static String USER_P =
	"org.w3c.tools.jdbc.user";

    public final static String PASSWORD_P =
	"org.w3c.tools.jdbc.password";

    public static int getMaxConn(Properties props) {
	return getInt(props, MAX_CONNECTIONS_P, DEFAULT_MAX_CONN);
    }

    public static Properties setMaxConn(Properties props, int max) {
	if (max < 1) {
	    props.put(MAX_CONNECTIONS_P, String.valueOf(DEFAULT_MAX_CONN));
	} else {
	    props.put(MAX_CONNECTIONS_P, String.valueOf(max));
	}
	return props;
    }

    public static String getDriver(Properties props) {
	return (String) props.get(JDBC_DRIVER_P);
    }

    public static Properties setDriver(Properties props, String driver) {
	if (driver != null)
	    props.put(JDBC_DRIVER_P, driver);
	return props;
    }

    public static String getUser(Properties props) {
	return (String) props.get(USER_P);
    }

    public static Properties setUser(Properties props, String user) {
	if (user != null)
	    props.put(USER_P, user);
	return props;
    }

    public static String getPassword(Properties props) {
	return (String) props.get(PASSWORD_P);
    }

    public static Properties setPassword(Properties props, String password) {
	if (password != null)
	    props.put(PASSWORD_P, password);
	return props;
    }

    //
    // private
    //

    private static void setBoolean(Properties props, 
				   String name,
				   boolean bool) 
    {
	props.put(name, String.valueOf(bool));
    }

    private static boolean getBoolean(Properties props, String name) {
	String p = (String) props.get(name);
	if (p != null) {
	    return p.equalsIgnoreCase("true");
	}
	return false;
    }

    private static int getInt(Properties props, String name, int def) {
	String str = (String) props.get(name);
	if (str != null) {
	    try {
		return Integer.parseInt(str);
	    } catch (NumberFormatException ex) {
		return def;
	    }
	} else {
	    return def;
	}
    }

    private static void setInt(Properties props, String name, int i) {
	String sint = String.valueOf(i);
	props.put(name, sint);
    }

    private static long getLong(Properties props, String name, long def) {
	String str = (String) props.get(name);
	if (str != null) {
	    try {
		return Long.parseLong(str);
	    } catch (NumberFormatException ex) {
		return def;
	    }
	} else {
	    return def;
	}
    }

    private static void setLong(Properties props, String name, long l) {
	String slong = String.valueOf(l);
	props.put(name, slong);
    }

}
