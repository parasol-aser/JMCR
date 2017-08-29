// CookieDate.java
// $Id: CookieDate.java,v 1.1 2010/06/15 12:19:51 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.http;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CookieDate extends HttpDate {
    protected static String days[] = { "Pad", "Sun", "Mon", "Tue", "Wed",
 				       "Thu" , "Fri", "Sat" };
    protected static String months[] = { "Jan", "Feb", "Mar", "Apr",
 					 "May", "Jun", "Jul", "Aug",
 					 "Sep", "Oct", "Nov", "Dec" };
    
    protected void updateByteValue() {
	if (cal == null) {
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    cal = Calendar.getInstance(tz);
	}
	// Dump the date, according to Cookie prefered format
	Date now = new Date(date.longValue());
	cal.setTime(now);
	// Dump the date, according to HTTP/1.1 prefered format
	HttpBuffer buf = new HttpBuffer(32);
	buf.append(days[cal.get(Calendar.DAY_OF_WEEK)]);
	buf.append(','); buf.append(' ');
	buf.appendInt(cal.get(Calendar.DAY_OF_MONTH), 2, (byte) '0');
	buf.append('-');
	buf.append(months[cal.get(Calendar.MONTH)]);
	buf.append('-');
	buf.appendInt(cal.get(Calendar.YEAR), 2, (byte) '0');
	buf.append(' ');
	buf.appendInt(cal.get(Calendar.HOUR_OF_DAY), 2, (byte) '0');
	buf.append(':');
	buf.appendInt(cal.get(Calendar.MINUTE), 2, (byte) '0');
	buf.append(':');
	buf.appendInt(cal.get(Calendar.SECOND), 2, (byte) '0');
	buf.append(" GMT");
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    public CookieDate(boolean isValid, long date) {
	super(isValid, date);
    }

}
