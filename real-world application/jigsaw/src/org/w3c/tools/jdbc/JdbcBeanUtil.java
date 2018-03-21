// JdbcBeanUtil.java
// $Id: JdbcBeanUtil.java,v 1.1 2010/06/15 12:27:28 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc;

import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import java.util.Properties;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JdbcBeanUtil {

    /**
     * Copy the following list of properties in the given bean and set the
     * jdbcTable properties
     * <ul>
     * <li>jdbcURI
     * <li>jdbcDriver
     * <li>jdbcUser
     * <li>jdbcPassword
     * </ul>
     * @param father the source bean
     * @param child the bean to initialize
     * @param jdbcTable the SQL table name
     */
    public static void initializeBean(JdbcBeanInterface father,
				      JdbcBeanInterface child,
				      String jdbcTable) 
    {
	child.setJdbcURI(father.getJdbcURI());
	child.setJdbcDriver(father.getJdbcDriver());
	child.setJdbcUser(father.getJdbcUser());
	child.setJdbcPassword(father.getJdbcPassword());
	child.setJdbcTable(jdbcTable);
	child.setMaxConn(father.getMaxConn());
    }

    public static boolean isJdbcBean(Class c) {
	do {
	    Class interfaces[] = c.getInterfaces();
	    for (int i = 0 ; i < interfaces.length ; i++) {
		if (interfaces[i] == JdbcBeanInterface.class) {
		    return true;
		}
	    }
	} while ((c = c.getSuperclass()) != null);
	return false;
    }

    public static boolean isIn(String string, String array[]) {
	if (array == null) {
	    return false;
	}
	for (int i = 0 ; i < array.length ; i++) {
	    if (array[i].equals(string)) {
		return true;
	    }
	}
	return false;
    }

    public static void generateJdbcBeans(String uri,
					 String driver,
					 String user,
					 String password,
					 String outputDir) 
	throws SQLException
    {
	// FIXME
	JdbcServer server = 
	    JdbcServer.getServer(uri, user, password, driver);
	DatabaseMetaData meta = server.getMetaData();
	ResultSet set = null;
	String types[] = { "TABLE" };
	set = meta.getTables("", "", "%", types); // all tables
	if ((set != null) && (set.first())) {
	    do {
		String table = set.getString("TABLE_NAME");
		System.out.println("table: \""+table+"\"");
		ResultSet subset = meta.getColumns("","", table, "%");
		if ((subset != null) && (subset.first())) {
		    do {
			System.out.println("\tColumn: "+
					   subset.getString("COLUMN_NAME"));
			System.out.println("\t>>>>>>> Data Type: "+
					   subset.getString("DATA_TYPE"));
			System.out.println("\t>>>>>>> Type Name: "+
					   subset.getString("TYPE_NAME"));
		    } while (subset.next());
		}
	    } while (set.next());
	}
    }

    public static void main(String args[]) {
	try {
	    JdbcBeanUtil.
		generateJdbcBeans("jdbc:postgresql://coot.inria.fr/dvddb",
				  "org.postgresql.Driver",
				  "joeuser",
				  "caca",
				  "toto");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
