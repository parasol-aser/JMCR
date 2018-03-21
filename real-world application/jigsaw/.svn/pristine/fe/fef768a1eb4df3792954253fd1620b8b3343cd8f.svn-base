// DateAttribute.java
// $Id: DateAttribute.java,v 1.1 2010/06/15 12:20:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class DateAttribute extends LongAttribute {

    String cachedPickledValue = null;
    Long   cachedPickledLong = null;

    public DateAttribute(String name, Object def, int flags) {
	super(name, (Long) def, flags) ;
	this.type = "java.util.Date".intern();
    }

    public DateAttribute() {
	super();
    }

    /**
     * Get a DateFormat compliant with RFC 822 updated by RFC 1123.
     * @return a SimpleDateFormat instance.
     */
    private SimpleDateFormat getDateFormatter() {
	SimpleDateFormat formatter = 
	    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	return formatter;
    }

    /**
     * Pickle an integer to the given output stream.
     * @param obj The object to pickle.
     * @exception IOException If some IO error occured.
     */

    public String pickle(Object obj) {
	if (obj == cachedPickledLong) {
	    if (cachedPickledValue == null) {
		SimpleDateFormat formatter = getDateFormatter();
		String s = formatter.format(new Date(((Long)obj).longValue()));
		cachedPickledValue = s;
		return s;
	    }
	    return cachedPickledValue;
	}
	SimpleDateFormat formatter = getDateFormatter();
	return formatter.format(new Date(((Long)obj).longValue()));
    }

    /**
     * Unpickle an integer from the given input stream.
     * @param value the string representation of this integer
     * @return An instance of Integer.
     * @exception IOException If some IO error occured.
     */

    public Object unpickle (String value) {
	try {
	    SimpleDateFormat formatter = getDateFormatter();
	    Long l = new Long((formatter.parse(value)).getTime());
	    cachedPickledLong = l;
	    return l;
	} catch (ParseException ex) {
	    return new Long(-1);
	}
    }

}
