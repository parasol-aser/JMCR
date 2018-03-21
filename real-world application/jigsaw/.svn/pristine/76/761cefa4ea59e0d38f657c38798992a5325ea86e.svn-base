// TimeFormatter.java
// $Id: TimeFormatter.java,v 1.1 2010/06/15 12:25:40 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util ;

import java.util.Date;

/**
 * This class does date formatting using the same format strings accepted by the
 * strftime(3) UNIX call. This class has static methods only.
 * @author <a href="mail:anto@w3.org">Antonio Ram&iacute;rez</a>
 */

public class TimeFormatter {

    private static String[] fullWeekDays =
    {
	"Sunday",
	"Monday",
	"Tuesday",
	"Wednesday",
	"Thursday",
	"Friday",
	"Saturday"
    } ;

    private static String[] abrWeekDays =
    {
	"Sun",
	"Mon",
	"Tue",
	"Wed",
	"Thu",
	"Fri",
	"Sat"
    } ;

    private static String[] fullMonths =
    {
	"January",
	"February",
	"March",
	"April",
	"May",
	"June",
	"July",
	"August",
	"September",
	"October",
	"November",
	"December"
    } ;

    private static String[] abrMonths =
    {
	"Jan",
	"Feb",
	"Mar",
	"Apr",
	"May",
	"Jun",
	"Jul",
	"Aug",
	"Sep",
	"Oct",
	"Nov",
	"Dec"
    } ;
	
    /**
     * Format the given date as a string, according to the given
     * format string.
     * The format string is of the form used by the strftime(3) UNIX
     * call.
     * @param date The date to format
     * @param format The formatting string
     * @return the String with the formatted date.  */
    public static String format(Date date,String format) {
	StringBuffer buf = new StringBuffer(50) ;
	char ch;
	for(int i=0;i<format.length();i++) {
	    ch = format.charAt(i) ;
	    if(ch == '%') {
		++i ;
		if(i == format.length()) break ;
		ch = format.charAt(i) ;
		if(ch == 'E') {
		    // Alternate Era
		    ++i;
		} else if(ch == 'Q') {
		    // Alternate numeric symbols
		    ++i;
		}
		if(i == format.length()) break ;
		ch = format.charAt(i) ;
		switch(ch) {
		  case 'A':
		      buf.append(fullWeekDays[date.getDay()]) ;
		      break ;
		  case 'a':
		      buf.append(abrWeekDays[date.getDay()]) ;
		      break ;

		  case 'B':
		      buf.append(fullMonths[date.getMonth()]) ;
		      break ;

		  case 'b':
		  case 'h':
		      buf.append(abrMonths[date.getMonth()]) ;
		      break ;

		  case 'C':
		      appendPadded(buf,(date.getYear()+1900) / 100, 2) ;
		      break ;

		  case 'c':
		      buf.append(date.toLocaleString()) ;
		      break ;

		  case 'D':
		      buf.append(TimeFormatter.format(date,"%m/%d/%y"));
		      break;
		      
		  case 'd':
		      appendPadded(buf,date.getDate(),2) ;
		      break;

		  case 'e':
		      appendPadded(buf,date.getMonth()+1,2,' ') ;
		      break;

		  case 'H':
		      appendPadded(buf,date.getHours(),2) ;
		      break ;

		  case 'I':
		  case 'l':
		      int a = date.getHours() % 12 ;
		      if(a==0) a = 12 ;
		      appendPadded(buf,a,2,ch=='I' ? '0' : ' ') ;
		      break ;

		  case 'j':
		      buf.append("[?]") ;
		      // No simple way to get this as of now
		      break ;

		  case 'k':
		      appendPadded(buf,date.getHours(),2,' ') ;
		      break ;

		  case 'M':
		      appendPadded(buf,date.getMinutes(),2) ;
		      break ;

		  case 'm':
		      appendPadded(buf,date.getMonth()+1,2) ;
		      break ;

		  case 'n':
		      buf.append('\n') ;
		      break ;

		  case 'p':
		      buf.append(date.getHours()<12 ? "am" : "pm") ;
		      break;

		  case 'R':
		      buf.append(TimeFormatter.format(date,"%H:%M")) ;
		      break ;

		  case 'r':
		      buf.append(TimeFormatter.format(date,"%l:%M%p")) ;
		      break ;

		  case 'S':
		      appendPadded(buf,date.getSeconds(),2) ;
		      break ;
		      
		  case 'T':
		      buf.append(TimeFormatter.format(date,"%H:%M:%S"));
		      break ;

		  case 't':
		      buf.append('\t') ;
		      break ;

		  case 'U':
		  case 'u':
		  case 'V':
		  case 'W':
		      buf.append("[?]");
		      // Weekdays are a pain, especially
		      // without day of year (0-365) ;
		      break ;

		  case 'w':
		      buf.append(date.getDay()) ;
		      break ;

		  case 'X':
		      buf.append(TimeFormatter.format(date,"%H:%M:%S"));
		      break ;

		  case 'x':
		      buf.append(TimeFormatter.format(date,"%B %e, %Y")) ;
		      break ;

		  case 'y':
		      appendPadded(buf,(date.getYear()+1900) % 100,2) ;
		      break ;

		  case 'Y':
		      appendPadded(buf,(date.getYear()+1900),4) ;
		      break ;

		  case 'Z':
		      String strdate = date.toString() ;
		      buf.append(strdate.substring(20,23)) ;
		      // (!)
		      // There should be a better way
		      // to do this...
		      break ;
		  case '%':
		      buf.append('%') ;
		      break ;
		      
		}
	    } else {
		buf.append(ch) ;
	    }
	}
	return buf.toString() ;
    }

    private static void appendPadded(StringBuffer buf,
				     int n,
				     int digits,
				     char pad) {
	String foo = String.valueOf(n).trim() ;
	for(int i=0;i<digits-foo.length();i++) 
	    buf.append(pad);
	buf.append(foo) ;
    }

    private static final void appendPadded(StringBuffer buf,
					   int n,
					   int digits) {
	appendPadded(buf,n,digits,'0') ;
    }

    /**
     * For testing purposes
     */
    public static void main(String[] args) {
	try {
	    System.out.println(TimeFormatter
			       .format(new Date(),args[0])) ;
	} catch(Exception ex) {
	    ex.printStackTrace() ;
	}
    }
}
