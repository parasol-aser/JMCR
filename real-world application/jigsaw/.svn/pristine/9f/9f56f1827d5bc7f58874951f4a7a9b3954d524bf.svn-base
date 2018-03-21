// HourLimiterFilter.java
// $Id: HourLimiterFilter.java,v 1.2 2010/06/15 17:52:53 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.util.Calendar;
import java.util.Date;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.BooleanAttribute;
import org.w3c.tools.resources.DateAttribute;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HTTP;
import org.w3c.jigsaw.html.HtmlGenerator;

public class HourLimiterFilter extends ResourceFilter {
    /**
     * Repeat every day?
     */
    public static int ATTR_DAY_REPEAT = -1;
    /**
     * Repeat every week?
     */
    public static int ATTR_WEEK_REPEAT = -1;   
    /**
     * Repeat every month?
     */
    public static int ATTR_MONTH_REPEAT = -1;  
    /**
     * Repeat every year?
     */
    public static int ATTR_YEAR_REPEAT = -1;
    /**
     * start date
     */
    public static int ATTR_DATE_START = -1;
    /**
     * end date
     */
    public static int ATTR_DATE_END = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	
	try {
	    cls = Class.forName("org.w3c.jigsaw.filters.HourLimiterFilter") ;
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// Declare the day_repeat attribute
	a = new BooleanAttribute("day_repeat"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_DAY_REPEAT = AttributeRegistry.registerAttribute(cls, a) ;
	// Declare the week_repeat attribute
	a = new BooleanAttribute("week_repeat"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_WEEK_REPEAT = AttributeRegistry.registerAttribute(cls, a) ;
	// Declare the day_repeat attribute
	a = new BooleanAttribute("month_repeat"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_MONTH_REPEAT = AttributeRegistry.registerAttribute(cls, a) ;
	// Declare the week_repeat attribute
	a = new BooleanAttribute("year_repeat"
				 , Boolean.FALSE
				 , Attribute.EDITABLE) ;
	ATTR_YEAR_REPEAT = AttributeRegistry.registerAttribute(cls, a) ;
	// Declare the start date
	a = new DateAttribute("start"
			      , null
			      , Attribute.EDITABLE) ;
	ATTR_DATE_START = AttributeRegistry.registerAttribute(cls, a) ;
	// Declare the end date
	a = new DateAttribute("end"
			      , null
			      , Attribute.EDITABLE) ;
	ATTR_DATE_END = AttributeRegistry.registerAttribute(cls, a) ;
    }

    private int a_year, a_month, a_week, a_day, a_time;
    private int b_year, b_month, b_week, b_day, b_time;

    protected boolean getDayRepeat() {
	return getBoolean(ATTR_DAY_REPEAT, false);
    }

    protected boolean getWeekRepeat() {
	return getBoolean(ATTR_WEEK_REPEAT, false);
    }

    protected boolean getMonthRepeat() {
	return getBoolean(ATTR_MONTH_REPEAT, false);
    }

    protected boolean getYearRepeat() {
	return getBoolean(ATTR_YEAR_REPEAT, false);
    }

    /**
     * We override setValues to compute locally everything we need
     * @param idx The index of the attribute to modify.
     * @param value The new attribute value.
     */
    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (idx == ATTR_DATE_START) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date(((Long)value).longValue()));
	    a_year  = cal.get(Calendar.YEAR);
	    a_month = cal.get(Calendar.MONTH);
	    a_week  = cal.get(Calendar.DAY_OF_WEEK);
	    a_day   = cal.get(Calendar.DAY_OF_MONTH);
	    a_time  = cal.get(Calendar.HOUR_OF_DAY) * 3600 +
		cal.get(Calendar.MINUTE) * 60 +
		cal.get(Calendar.SECOND);
	} else if (idx == ATTR_DATE_END) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date(((Long)value).longValue()));
	    b_year  = cal.get(Calendar.YEAR);
	    b_month = cal.get(Calendar.MONTH);
	    b_week  = cal.get(Calendar.DAY_OF_WEEK);
	    b_day   = cal.get(Calendar.DAY_OF_MONTH);
	    b_time  = cal.get(Calendar.HOUR_OF_DAY) * 3600 + 
		cal.get(Calendar.MINUTE) * 60 +
		cal.get(Calendar.SECOND);
	}
    }

    /**
     * Initialize the filter.
     */

    public void initialize(Object values[]) {
	Calendar cal = Calendar.getInstance();
	long d = System.currentTimeMillis();

	super.initialize(values);
	cal.setTime(new Date(getLong(ATTR_DATE_START, d)));
	a_year  = cal.get(Calendar.YEAR);
	a_month = cal.get(Calendar.MONTH);
	a_week  = cal.get(Calendar.DAY_OF_WEEK);
	a_day   = cal.get(Calendar.DAY_OF_MONTH);
	a_time  = cal.get(Calendar.HOUR_OF_DAY) * 3600 +
	    cal.get(Calendar.MINUTE) * 60 +
	    cal.get(Calendar.SECOND);

	cal.setTime(new Date(getLong(ATTR_DATE_END, d)));
	b_year  = cal.get(Calendar.YEAR);
	b_month = cal.get(Calendar.MONTH);
	b_week  = cal.get(Calendar.DAY_OF_WEEK);
	b_day   = cal.get(Calendar.DAY_OF_MONTH);
	b_time  = cal.get(Calendar.HOUR_OF_DAY) * 3600 + 
	    cal.get(Calendar.MINUTE) * 60 +
	    cal.get(Calendar.SECOND);
    }

    /**
     * We check that the date is in the right values
     * otherwise, send a NOT_AVAILABLE and fills the right Retry-After header
     * @return a Reply if blocked, null otherwise
     */

    public synchronized ReplyInterface ingoingFilter(RequestInterface req) {
	Request request = (Request) req;
	Reply   reply   = null;
	int n_year, n_month, n_week, n_day, n_time;
	int a, b, n;
	Calendar cal = Calendar.getInstance();

	n_year  = cal.get(Calendar.YEAR);
	n_month = cal.get(Calendar.MONTH);
	n_week  = cal.get(Calendar.DAY_OF_WEEK);
	n_day   = cal.get(Calendar.DAY_OF_MONTH);
	n_time  = cal.get(Calendar.HOUR_OF_DAY) * 3600 + 
	    cal.get(Calendar.MINUTE) * 60 +
	    cal.get(Calendar.SECOND);
	
	if (getDayRepeat()) { // check it if it is repeated every day
	    if ((n_time < a_time) || (n_time > b_time)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		if (n_time < a_time)
		    reply.setRetryAfter(a_time - n_time);
		else
		    reply.setRetryAfter(84600 + n_time - a_time);
	    }
	} else if (getWeekRepeat()) { // check it if it is repeated every week
	    a = a_time + a_week * 86400;
	    b = b_time + b_week * 86400;
	    n = n_time + n_week * 86400;
	    if ((n < a) || (n > b)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		if (n < a)
		    reply.setRetryAfter(a - n);
		else
		    reply.setRetryAfter(84600*7 + n - a);
	    }
	} else if (getMonthRepeat()) { // check it if it's repeated every month
	    a = a_time + a_day * 86400;
	    b = b_time + b_day * 86400;
	    n = n_time + n_day * 86400;
	    if ((n < a) || (n > b)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		if (n < a)
		    reply.setRetryAfter(a - n);
		else {
		    cal.setTime(new Date(getLong(ATTR_DATE_START, -1)));
		    cal.set(Calendar.YEAR, n_year);
		    cal.set(Calendar.MONTH, n_month);
		    cal.roll(Calendar.MONTH, true);
		    reply.setRetryAfter(cal.getTime().getTime());
		}
	    }
	} else if (getYearRepeat()) { // check it if it's repeated every year
	    Calendar c_a = Calendar.getInstance();
	    Calendar c_b = Calendar.getInstance();
	    c_a.setTime(new Date(getLong(ATTR_DATE_START, -1)));
	    c_b.setTime(new Date(getLong(ATTR_DATE_END, -1)));
	    c_a.set(Calendar.YEAR, n_year);
	    c_b.set(Calendar.YEAR, n_year);
	    if (cal.before(c_a)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		reply.setRetryAfter(c_a.getTime().getTime());
	    } else if (cal.after(c_b)) {
		c_a.roll(Calendar.YEAR, true);
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		reply.setRetryAfter(c_a.getTime().getTime());
	    }
	} else { /* no repeat */
	    Calendar c_a = Calendar.getInstance();
	    Calendar c_b = Calendar.getInstance();
	    c_a.setTime(new Date(getLong(ATTR_DATE_START, -1)));
	    c_b.setTime(new Date(getLong(ATTR_DATE_END, -1)));
	    if (cal.before(c_a)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
		reply.setRetryAfter(c_a.getTime().getTime());
	    } else if (cal.after(c_b)) {
		reply = request.makeReply(HTTP.SERVICE_UNAVAILABLE);
	    }
	}
	if (reply != null) {
	    HtmlGenerator g = new HtmlGenerator("Service Unavailable");
	    g.append("You may retry after the delay or the date given");
	    reply.setStream(g);
	}
	return reply;
    }
}
