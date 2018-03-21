// DateThread.java
// $Id: DateThread.java,v 1.1 2010/06/15 12:29:49 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.timers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateThread extends Thread {
    protected static String days[] = { "Sun", "Mon", "Tue", "Wed",
				       "Thu" , "Fri", "Sat" };
    protected static String months[] = { "Jan", "Feb", "Mar", "Apr",
					 "May", "Jun", "Jul", "Aug",
					 "Sep", "Oct", "Nov", "Dec" };
    String date = "";
    String logDate = "";
    private TimeZone tz;

    /**
     * Give the Date in HTTP/1.1 preferred format
     * @return A String giving the date in the right format
     */

    public synchronized String getDate() {
	return date;
    }

    public synchronized String getLogDate() {
	return logDate;
    }

    /**
     * The main loop which calculate the date every second
     */

    public void run () { 
	int day;
	int hr,mn,sec;
	StringBuffer sbdate, ldate;
	Calendar cal;
	
	while(true) {
	    sbdate = new StringBuffer(30);
	    ldate = new StringBuffer(30);

	    cal = new GregorianCalendar(tz, Locale.getDefault());
	    day = cal.get(Calendar.DAY_OF_MONTH);
	    hr = cal.get(Calendar.HOUR_OF_DAY);
	    mn = cal.get(Calendar.MINUTE);
	    sec = cal.get(Calendar.SECOND);
	    
	    // construct date according to the http/1.1 format
	    sbdate.append(days[cal.get(Calendar.DAY_OF_WEEK)-1]);
	    sbdate.append(", ");
	    if (day < 10)
		sbdate.append('0');
	    sbdate.append(day);
	    sbdate.append(' ');
	    sbdate.append(months[cal.get(Calendar.MONTH)]);
	    sbdate.append(' ');
	    sbdate.append(cal.get(Calendar.YEAR));
	    sbdate.append(' ');
	    if (hr < 10) 
		sbdate.append('0');
 	    sbdate.append(hr);
	    sbdate.append(':');
 	    if (mn < 10) 
		sbdate.append('0');
	    sbdate.append(mn);
	    sbdate.append(':');
	    if (sec < 10) 
		sbdate.append('0');
	    sbdate.append(sec);
	    sbdate.append(" GMT");

	    // construct date according to the log date format
	    if (day < 10)
		ldate.append('0');
	    ldate.append(day);
	    ldate.append('/');
	    ldate.append(months[cal.get(Calendar.MONTH)]);
	    ldate.append('/');
	    ldate.append(cal.get(Calendar.YEAR));
	    ldate.append(':');
	    if (hr < 10)
		ldate.append('0');
	    ldate.append(hr);
	    ldate.append(':');
	    if (mn < 10)
		ldate.append('0');
	    ldate.append(mn);
	    ldate.append(':');
	    if (sec < 10) 
		ldate.append('0');
	    ldate.append(sec);
	    ldate.append(" +0");

	    synchronized(this) {
		date = sbdate.toString();
		logDate = ldate.toString();
	    }
	    try {
		sleep(1000); // sleep 1 sec before new update
	    } catch (InterruptedException ex) {}
	}
    }

    public DateThread() {
	super("DateThread");
	tz = TimeZone.getTimeZone("GMT"); // should be UTC
	setDaemon(true);
	setPriority(Thread.MAX_PRIORITY);
    }

    public static void main(String args[]) {
	DateThread dt = new DateThread();
	dt.start();

	while(true) {
	    System.out.println("Date ["+dt.getDate()+"]");
	    try {
		sleep(1000);
	    } catch (InterruptedException ex) {}
	}
    }
}
