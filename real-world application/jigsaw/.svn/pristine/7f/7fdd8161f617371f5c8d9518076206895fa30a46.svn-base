// HttpDate.java
// $Id: HttpDate.java,v 1.1 2010/06/15 12:19:51 smhuang Exp $$
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class HttpDate extends BasicValue {
    
    protected static byte bdays[][] = {{(byte)'P',(byte)'a',(byte)'d'},
				       {(byte)'S',(byte)'u',(byte)'n'}, 
				       {(byte)'M',(byte)'o',(byte)'n'},
				       {(byte)'T',(byte)'u',(byte)'e'},
				       {(byte)'W',(byte)'e',(byte)'d'},
				       {(byte)'T',(byte)'h',(byte)'u'},
				       {(byte)'F',(byte)'r',(byte)'i'}, 
				       {(byte)'S',(byte)'a',(byte)'t'}};
    
    protected static byte bmonthes[][] = {{(byte)'J',(byte)'a',(byte)'n'}, 
					  {(byte)'F',(byte)'e',(byte)'b'}, 
					  {(byte)'M',(byte)'a',(byte)'r'},
					  {(byte)'A',(byte)'p',(byte)'r'},
					  {(byte)'M',(byte)'a',(byte)'y'},
					  {(byte)'J',(byte)'u',(byte)'n'},
					  {(byte)'J',(byte)'u',(byte)'l'},
					  {(byte)'A',(byte)'u',(byte)'g'},
					  {(byte)'S',(byte)'e',(byte)'p'},
					  {(byte)'O',(byte)'c',(byte)'t'},
					  {(byte)'N',(byte)'o',(byte)'v'}, 
					  {(byte)'D',(byte)'e',(byte)'c'}};
    
    protected Long date = null;

    protected Calendar cal = null;
    
    protected void parse() {
	ParseState ps = new ParseState();
	ps.ioff   = roff;
	ps.bufend = rlen;
	date = new Long(HttpParser.parseDateOrDeltaSeconds(raw, ps));
    }

    protected void updateByteValue() {
	if (cal == null) {
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    cal = Calendar.getInstance(tz);
	}
	Date now = new Date(date.longValue());
	cal.setTime(now);
	// Dump the date, according to HTTP/1.1 prefered format
	byte buf[] = new byte[29];
	int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
	int j = 0;
	for (int i = 0; i< 3; i++) {
	    buf[j++] = bdays[dayofweek][i];
	}
	buf[j++] = (byte) ',';
	buf[j++] = (byte) ' ';
	int day = cal.get(Calendar.DAY_OF_MONTH);
	if (day < 10) {
	    buf[j++] = 48;
	    buf[j++] = (byte)(48 + day);
	} else {
	    buf[j++] = (byte) (48 + (day / 10));
	    buf[j++] = (byte) (48 + (day % 10));
	}
	buf[j++] = (byte) ' ';
	int month = cal.get(Calendar.MONTH);
	for (int i = 0; i< 3; i++) {
	    buf[j++] = bmonthes[month][i];
	}
	buf[j++] = (byte) ' ';
	int year = cal.get(Calendar.YEAR);
	// not y10k compliant
	buf[j+3] = (byte) (48 + (year % 10));
	year = year / 10;
	buf[j+2] = (byte) (48 + (year % 10));
	year = year / 10;
	buf[j+1] = (byte) (48 + (year % 10));
	year = year / 10;
	buf[j] = (byte) (48 + year);
	j += 4;
	buf[j++] = (byte) ' ';
	int hour = cal.get(Calendar.HOUR_OF_DAY);
	if (hour < 10) {
	    buf[j++] = (byte) 48;
	    buf[j++] = (byte) (48 + hour);
	} else {
	    buf[j++] = (byte) (48 + (hour / 10));
	    buf[j++] = (byte) (48 + (hour % 10));
	}
	buf[j++] = (byte) ':';
	int minute = cal.get(Calendar.MINUTE);
	if (minute < 10) {
	    buf[j++] = (byte) 48;
	    buf[j++] = (byte) (48 + minute);
	} else {
	    buf[j++] = (byte) (48 + (minute / 10));
	    buf[j++] = (byte) (48 + (minute % 10));
	}
	buf[j++] = (byte) ':';
	int second = cal.get(Calendar.SECOND);
	if (second < 10) {
	    buf[j++] = (byte) 48;
	    buf[j++] = (byte) (48 + second);
	} else {
	    buf[j++] = (byte) (48 + (second / 10));
	    buf[j++] = (byte) (48 + (second % 10));
	} 
	buf[j++] = (byte) ' '; buf[j++] = (byte) 'G'; 
	buf[j++] = (byte) 'M'; buf[j++] = (byte) 'T';
	raw = buf;
	roff = 0;
	rlen = raw.length;
    }

    /**
     * Get the date value.
     * @return A Long giving the date as a number of mmilliseconds since epoch.
     */

    public Object getValue() {
	validate();
	return date;
    }

    /**
     * Set this date object value.
     * @param date The new date value, as the number of milliseconds since
     * epoch.
     */

    public void setValue(long date) {
	if ( date == this.date.longValue() )
	    return ;
	invalidateByteValue();
	this.date = new Long(date);
	this.isValid = true ;
    }

    HttpDate(boolean isValid, long date) {
	this.isValid = isValid;
	this.date    = new Long(date);
    }

    HttpDate() {
	this.isValid = false;
    }

}
