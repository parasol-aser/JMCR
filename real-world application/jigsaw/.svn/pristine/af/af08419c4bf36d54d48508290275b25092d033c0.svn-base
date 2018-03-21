// jdbcCommand.java
// $Id: jdbcCommand.java,v 1.2 2010/06/15 17:52:50 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.jdbc;

import java.util.Dictionary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.w3c.www.http.HTTP;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.ssi.SSIFrame;

import org.w3c.jigsaw.ssi.commands.Command;

/**
 * Implementation of the SSI <code>jdbc</code> command.  
 */
public class jdbcCommand implements Command {
    private final static String  NAME  = "jdbc";
    private final static boolean debug = true;

    private static final String keys[] = {
	"select",
	"url",
	"driver",
	"user",
	"password",
	"name",
	"column",
	"next"
    };

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return true;
    }

    protected Connection getConnection(String driver, 
				       String url, 
				       String user,
				       String password) 
    {
	try {
	    Class.forName(driver);
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    return DriverManager.getConnection(url, user, password);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    protected ResultSet performSelect(Connection conn, String cmd) {
	try {
	    Statement smt = conn.createStatement();
	    ResultSet set = smt.executeQuery(cmd);
	    return set;
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    protected void addSet(Dictionary d, 
			  String name, 
			  Request request,
			  ResultSet set) {
	d.put(request.toString()+"."+name, set);
    }

    protected ResultSet getSet(Dictionary d, String name, Request request) {
	return (ResultSet) d.get(request.toString()+"."+name);
    }

    public String getName() {
	return NAME;
    }

    public String getValue(Dictionary variables, String var, Request request) {
	ResultSet set = getSet(variables, var, request);
	if (! hasMoreValue(variables, var, request))
	    return "empty";
	else return "not-empty";
    }

    protected void sethasMoreValueFlag(Dictionary d,
				       String name,
				       Request request,
				       boolean flag)
    {
	d.put(request.toString()+"."+name+".flag", new Boolean(flag));
    }

    protected boolean hasMoreValue(Dictionary d,
				   String name,
				   Request request)
    {
	Boolean flag = (Boolean) d.get(request.toString()+"."+name+".flag");
	return (flag == null) ? true : flag.booleanValue();
    }

    public synchronized Reply execute(SSIFrame ssiframe,
				      Request request,
				      ArrayDictionary parameters,
				      Dictionary variables) 
    {
	Object values[] = parameters.getMany(keys);
	String select   = (String) values[0];
	String url      = (String) values[1];
	String driver   = (String) values[2];
	String user     = (String) values[3];
	String password = (String) values[4];
	String name     = (String) values[5];
	String column   = (String) values[6];
	String next     = (String) values[7];
	String text     = null;
	if ( select != null ) {
	    // user and password are optionnals, they can be null.
	    Connection conn = getConnection(driver, url, user, password);
	    if ( conn != null ) {
		addSet(variables, name, request, performSelect(conn, select));
		sethasMoreValueFlag(variables, name, request, true);
	    }
	} else if (column != null) {
	    ResultSet set = getSet(variables, name, request);
	    try {
		if ( set != null ) 
		    text = set.getObject(Integer.parseInt(column)).toString();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	} else if ( next != null ) {
	    ResultSet set = getSet(variables, name, request);
	    if ( set != null ) {
		try {
		    sethasMoreValueFlag(variables, name, request, set.next());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}

	// We are NOT doing notMod hack here (tricky and useless ?)
	Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
	if ( text != null )
	    reply.setContent(text);
	return reply;
	
    }
}
