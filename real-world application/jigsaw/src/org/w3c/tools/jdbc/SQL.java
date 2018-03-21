// SQL.java
// $Id: SQL.java,v 1.1 2010/06/15 12:27:29 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class SQL {

    public static SimpleDateFormat formatter = null;

    static {
	formatter = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static String encode(String string) {
	int          len    = string.length();
	StringBuffer buffer = new StringBuffer(len);
	char         c;
	buffer.append("'");
	for (int i = 0 ; i < len ; i++) {
	    switch (c = string.charAt(i)) 
		{
		case '\'':
		    buffer.append("\\'");
		    break;
		default:
		    buffer.append(c);
		}
	}
	buffer.append("'");
	return buffer.toString();
    }

    public static Object getMatchingValue(Class c, Object value) {
	if (value == null) {
	    return null;
	}
	if (c.isInstance(value)) {
	    return value;
	}
	String stringvalue = String.valueOf(value);
	if (c == String.class) {
	    return stringvalue;
	} else if ((c == Integer.class) || (c == int.class)) {
	    return new Integer(stringvalue);
	} else if ((c == Long.class) || (c == long.class)) {
	    return new Long(stringvalue);
	} else if ((c == Boolean.class) || (c == boolean.class)) {
	    return new Boolean ((stringvalue.equalsIgnoreCase("true") ||
				 stringvalue.equalsIgnoreCase("t") ||
				 stringvalue.equalsIgnoreCase("y") ||
				 stringvalue.equalsIgnoreCase("yes") ||
				 stringvalue.equalsIgnoreCase("1")));
	} else if ((c == Character.class) || (c == char.class)) {
	    return new Character(stringvalue.charAt(0));
	} else if ((c == Double.class) || (c == double.class)) {
	    return new Double(stringvalue);
	} else if ((c == Float.class) || (c == float.class)) {
	    return new Float(stringvalue);
	} else if ((c == Short.class) || (c == short.class)) {
	    return new Short(stringvalue);
	}
	return null;
    }

    public static String getSQLValue(Object value) {
	Class c = value.getClass();
	if (c == String.class) {
	    return encode((String)value);
	} else if ((c == Date.class) || (c == java.sql.Date.class)) {
	    String date = formatter.format((Date)value);
	    return encode(date);
	} else {
	    return String.valueOf(value);
	}
    }

    /**
     * Split the SQL operator and the value, (default operator is '=')
     * example:<br>
     * "~*.*toto.*" will become { "~*", ".*toto.*" }<br>
     * but "\~*.*toto.*" will become { "=", "~*.*toto.*" }
     * <p>possible operators are:
     * <table border=0>
     * <tr><td> &lt; </td><td>Less than?</td></tr> 
     * <tr><td> &lt;= </td><td>Less than or equals?</td></tr>  
     * <tr><td> &lt;&gt; </td><td>Not equal?</td></tr>  
     * <tr><td> = </td><td>Equals?</td></tr>  
     * <tr><td> &gt; </td><td>Greater than?</td></tr>
     * <tr><td> &gt;= </td><td>Greater than or equals?</td></tr>
     * <tr><td> ~~ </td><td>LIKE</td></tr>
     * <tr><td> !~~ </td><td>NOT LIKE</td></tr>
     * <tr><td> ~ </td><td>Match (regex), case sensitive</td></tr>
     * <tr><td> ~* </td><td>Match (regex), case insensitive</td></tr>
     * <tr><td> !~ </td><td>Does not match (regex), case sensitive </td></tr>
     * <tr><td> !~* </td><td>Does not match (regex), case insensitive</td></tr>
     * </table>
     */
    public static String[] getSQLOperator(Object val) {
	Class cl = val.getClass();
	if (cl != String.class) {
	    String split[] = { " = ", getSQLValue(val) };
	    return split;
	}
	String value = (String) val;
	String result[] = new String[2];
	char c = value.charAt(0);
	switch (c) 
	    {
	    case '~':
		c = value.charAt(1);
		if (c == '*') {                       // ~*
		    result[0] = " ~* ";
		    result[1] = value.substring(2);
		} else if (c == '~') {                // ~~
		    result[0] = " ~~ ";
		    result[1] = value.substring(2);
		} else {                              // ~
		    result[0] = " ~ ";
		    result[1] = value.substring(1);
		}
		break;
	    case '<':
		c = value.charAt(1);
		if (c == '=') {                       // <=
		    result[0] = " <= ";
		    result[1] = value.substring(2);
		} else if (c == '>') {                // <>
		    result[0] = " <> ";
		    result[1] = value.substring(2);
		} else {                              // <
		    result[0] = " < ";
		    result[1] = value.substring(1);
		}
		break;
	    case '>':
		c = value.charAt(1);
		if (c == '=') {                      // >=
		    result[0] = " >= ";
		    result[1] = value.substring(2);
		} else {                             // >
		    result[0] = " > ";
		    result[1] = value.substring(1);
		}
		break;
	    case '!':
		if (c == '~') {
		    c = value.charAt(2);
		    if (c == '~') {                 // !~~
			result[0] = " !~~ ";
			result[1] = value.substring(3);
		    } else if (c == '*') {          // !~* 
			result[0] = " !~* ";
			result[1] = value.substring(3);
		    } else {                        // !~
			result[0] = " !~ ";
			result[1] = value.substring(2);
		    }
		    break;
		} 
	    case '\\':
		value = value.substring(1);
	    case '=':                               // =
	    default:
		result[0] = " = ";
		result[1] = value;
	    }
	result[1] = encode(result[1]);
	return result;
    }

}
