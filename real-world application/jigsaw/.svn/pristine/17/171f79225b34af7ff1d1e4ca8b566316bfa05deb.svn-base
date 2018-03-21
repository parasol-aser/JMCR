// CommonLogger.java
// $Id: CommonLogger.java,v 1.1 2010/06/15 12:21:59 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.net.URL;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.w3c.jigsaw.auth.AuthFilter;

import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;

/**
 * The CommonLogger class implements the abstract Logger class.
 * The resulting log will conform to the 
 * <a href="http://www.w3.org/Daemon/User/Config/Logging.html#common-logfile-format">common log format</a>).
 * @see org.w3c.jigsaw.http.Logger
 */

public class CommonLogger extends Logger implements PropertyMonitoring {
    protected static final String monthnames[] = {
	"Jan", "Feb", "Mar", "Apr", "May", "Jun",
	"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    protected static String          noUrl = "*NOURL*";
	
    /**
     * Name of the property indicating the log file.
     * This property indicates the name of the log file to use.
     * <p>This property defaults to the <code>log</code> file in the server
     * log directory.
     */
    public static final 
    String LOGNAME_P = "org.w3c.jigsaw.logger.logname" ;
    /**
     * Name of the property indicating the error log file.
     * This property indicates the name of the error log file to use.
     * <p>This property defaults to the <code>errlog</code> file in the
     * server log directory.
     */
    public static final 
    String ERRLOGNAME_P = "org.w3c.jigsaw.logger.errlogname" ;
    /**
     * Name of the property indicating the server trace file.
     * This property indicates the name of the trace file to use.
     * <p>This property defaults to the <code>trace</code> file in the 
     * server log directory.
     */
    public static final 
    String LOGDIRNAME_P = "org.w3c.jigsaw.logger.logdirname";
    /**
     * Name of the property indicating the server log directory.
     * <p>This property defaults to the <code>logs</code> directory in the 
     * server main directory.
     */
    public static final 
    String TRACELOGNAME_P = "org.w3c.jigsaw.logger.tracelogname";
    /**
     * Name of the property indicating the buffer size for the logger.
     * This buffer size applies only the the log file, not to the error
     * log file, or the trace log file. It can be set to zero if you want
     * no buffering.
     * <p>This property default to <strong>4096</strong>.
     */
    public static final 
    String BUFSIZE_P = "org.w3c.jigsaw.logger.bufferSize";
    /**
     * Name of the property indicating the buffer size for the logger.
     * This buffer size applies only the the log file, not to the error
     * log file, or the trace log file. It can be set to zero if you want
     * no buffering.
     * <p>This property default to <strong>4096</strong>.
     */
    public static final 
    String ROTATE_LEVEL_P = "org.w3c.jigsaw.logger.rotateLevel";

    private   byte                 msgbuf[]    = null ;
    protected RandomAccessFile     log         = null ;
    protected RandomAccessFile     errlog      = null ;
    protected RandomAccessFile     trace       = null ;
    protected httpd                server      = null ;
    protected ObservableProperties props       = null ;
    protected int                  bufsize     = 8192 ;
    protected int                  bufptr      = 0    ;
    protected int                  rotateLevel = 0    ;
    protected byte                 buffer[]    = null ;
    protected int                  year        = -1   ;
    protected int                  month       = -1   ;
    protected int                  day         = -1   ;
    protected int                  hour        = -1   ;
    private   Calendar             cal         = null ;
    private   long                 datestamp   = -1   ;
    private   char                 datecache[] = { 'D','D','/','M','M','M',
						   '/','Y','Y','Y','Y',':',
						   'H','H',':','M','M',':',
						   'S','S',' ','+','0','0',
						   '0','0'};

    /**
     * Property monitoring for the logger.
     * The logger allows you to dynamically (typically through the property
     * setter) change the names of the file to which it logs error, access
     * and traces.
     * @param name The name of the property that has changed.
     * @return A boolean, <strong>true</strong> if the change was made, 
     *    <strong>false</strong> otherwise.
     */

    public boolean propertyChanged (String name) {
	if ( name.equals(LOGNAME_P) ) {
	    try {
		openLogFile () ;
	    } catch (Exception e) {
		e.printStackTrace() ;
		return false ;
	    }
	    return true ;
	} else if ( name.equals(ERRLOGNAME_P) ) {
	    try {
		openErrorLogFile() ;
	    } catch (Exception e) {
		e.printStackTrace() ;
		return false ;
	    } 
	    return true ;
	} else if ( name.equals(TRACELOGNAME_P) ) {
	    try {
		openTraceFile() ;
	    } catch (Exception e) {
		e.printStackTrace() ;
		return false ;
	    }
	    return true ;
	} else if ( name.equals(LOGDIRNAME_P) ) {
	    try {
		openLogFile () ;
		openErrorLogFile() ;
		openTraceFile() ;
	    } catch (Exception e) {
		e.printStackTrace() ;
		return false ;
	    }
	    return true ;
	} else if ( name.equals(BUFSIZE_P) ) {
	    synchronized (this) {
		bufsize = props.getInteger(name, bufsize);
		// Reset buffer before resizing:
		if ( bufptr > 0 ) {
		    try {
			log.write(buffer, 0, bufptr);
			bufptr = 0;
		    } catch (IOException ex) {
		    }
		}
		// Set new buffer:
		buffer = (bufsize > 0) ? new byte[bufsize] : null;
		return true;
	    }
	} else if (name.equals(ROTATE_LEVEL_P) ) {
	    int newLevel = props.getInteger(name, rotateLevel);
	    if (newLevel != rotateLevel) {
		synchronized (this) {
		    sync();
		    rotateLevel = newLevel;
		    openLogFile();
		}
	    }
	    return true;
	} else {
	    return true ;
	}
    }

    /**
     * Output the given message to the given RandomAccessFile.
     * This method makes its best effort to avoid one byte writes (which you
     * get when writing the string as a whole). It first copies the string 
     * bytes into a private byte array, and than, write them all at once.
     * @param f The RandomAccessFile to write to, which should be one of
     *    log, errlog or trace.
     * @param msg The message to be written.
     * @exception IOException If writing to the output failed.
     */

    protected synchronized void output (RandomAccessFile f, String msg)
	throws IOException
    {
	int len = msg.length() ;
	if ( len > msgbuf.length ) 
	    msgbuf = new byte[len] ;
	msg.getBytes (0, len, msgbuf, 0) ;
	f.write (msgbuf, 0, len) ;
    }

    protected synchronized void appendLogBuffer(String msg)
	throws IOException
    {
	int msglen = msg.length();
	if ( bufptr + msglen > buffer.length ) {
	    // Flush the buffer:
	    log.write(buffer, 0, bufptr);
	    bufptr = 0;
	    // Check for messages greater then buffer:
	    if ( msglen > buffer.length ) {
		byte huge[] = new byte[msglen];
		msg.getBytes(0, msglen, huge, 0);
		log.write(huge, 0, msglen);
		return;
	    }
	} 
	// Append that message to buffer:
	msg.getBytes(0, msglen, buffer, bufptr);
	bufptr += msglen;
    }

    protected void logmsg (String msg) {
	if ( log != null ) {
	    try {
		if ( buffer == null ) {
		    output (log, msg) ;
		} else {
		    appendLogBuffer(msg);
		}
	    } catch (IOException e) {
		throw new HTTPRuntimeException (this,"logmsg",e.getMessage()) ;
	    }
	}
    }

    protected void errlogmsg (String msg) {
	if ( errlog != null ) {
	    try {
		output (errlog, msg) ;
	    } catch (IOException e) {
		throw new HTTPRuntimeException (this
						, "errlogmsg"
						, e.getMessage()) ;
	    }
	}
    }

    protected void tracemsg (String msg) {
	if ( trace != null ) {
	    try {
		output (trace, msg) ;
	    } catch (IOException e) {
		throw new HTTPRuntimeException (this
						, "tracemsg"
						, e.getMessage()) ;
	    }
	}
    }

    protected synchronized void checkLogFile(Date now) {
	if (cal == null) {
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    cal = Calendar.getInstance(tz);
	}
	cal.setTime(now);
	int nowYear = cal.get(Calendar.YEAR);
	if (rotateLevel == 1) {
	    // rotate every year
	    if (nowYear != year) {
		if (log != null) {
		    sync();
		}
		this.year = nowYear;
		openLogFile(year);
	    }
	} else {
	    int nowMonth = cal.get(Calendar.MONTH);
	    if (rotateLevel == 2) {
		// rotate every month
		if ((nowYear != year) || (nowMonth != month)) {
		    if (log != null) {
			sync();
		    }
		    this.year = nowYear;
		    this.month = nowMonth;
		    openLogFile(year, month);
		}
	    } else {
		int nowDay = cal.get(Calendar.DAY_OF_MONTH);
		if (rotateLevel == 3) {
		    // rotate every day
		    if ((nowYear != year) || (nowMonth != month) 
			|| (nowDay != day)) {
			if (log != null) {
			    sync();
			}
			this.year  = nowYear;
			this.month = nowMonth;
			this.day   = nowDay;
			openLogFile(year, month, day);
		    } 
		}
	    }
	}
    }

    protected void openLogFile(int year, int month, int day) {
	this.year = year;
	this.month = month;
	this.day = day;

	String ext = null;
	if (month < 9) {
	    if (day < 10) {
		ext = "_"+year+"_0"+(month+1)+"_0"+day;
	    } else {
		ext = "_"+year+"_0"+(month+1)+"_"+day;
	    }
	} else {
	    if (day < 10) {
		ext = "_"+year+"_"+(month+1)+"_0"+day;
	    } else {
		ext = "_"+year+"_"+(month+1)+"_"+day;
	    }
	}
	String logname = getFilename(LOGNAME_P, "log") + ext;
	try {
	    RandomAccessFile old = log ;
	    log = new RandomAccessFile (logname, "rw") ;
	    log.seek (log.length()) ;
	    if ( old != null )
		old.close () ;
	} catch (IOException e) {
	    throw new HTTPRuntimeException (this.getClass().getName()
					    , "openLogFile"
					    , "unable to open "+logname);
	}
    }

    protected void openLogFile(int year, int month) {
	this.year = year;
	this.month = month;

	String ext = null;
	if (month < 9)
	    ext = "_"+year+"_0"+(month+1);
	else
	    ext = "_"+year+"_"+(month+1);

	String logname = getFilename(LOGNAME_P, "log") + ext;
	try {
	    RandomAccessFile old = log ;
	    log = new RandomAccessFile (logname, "rw") ;
	    log.seek (log.length()) ;
	    if ( old != null )
		old.close () ;
	} catch (IOException e) {
	    throw new HTTPRuntimeException (this.getClass().getName()
					    , "openLogFile"
					    , "unable to open "+logname);
	}
    }

    protected void openLogFile(int year) {
	this.year = year;
	
	String logname = getFilename(LOGNAME_P, "log") + "_" + year;
	try {
	    RandomAccessFile old = log ;
	    log = new RandomAccessFile (logname, "rw") ;
	    log.seek (log.length()) ;
	    if ( old != null )
		old.close () ;
	} catch (IOException e) {
	    throw new HTTPRuntimeException (this.getClass().getName()
					    , "openLogFile"
					    , "unable to open "+logname);
	}
    }

    /**
     * It actually does multiple things, check when to rotate log files
     * and also dumps the formatted date string to a stringbuffer
     * it is dirty but hopefully faster than the previous version of the logger
     */
    protected synchronized void dateCache(long date, StringBuffer sb) {
	if (cal == null) {
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    cal = Calendar.getInstance(tz);
	}
	long ldate;
	// should we use the request date or just log the 
	// end of the request?
	if (date < 0) {
	    ldate = System.currentTimeMillis();
	} else {
	    ldate = date;
	}
	Date now = new Date(ldate);
	cal.setTime(now);
	if ((ldate > datestamp + 3600000) || (datestamp == -1)) {
	    datestamp = ldate % 3600000;
	    if (hour == -1) {
		hour = cal.get(Calendar.HOUR_OF_DAY);
	    } else {
		int nhour = cal.get(Calendar.HOUR_OF_DAY);
		if (nhour != hour) {
		    hour = nhour;
		    TimeZone tz = TimeZone.getTimeZone("UTC");
		    cal = Calendar.getInstance(tz);
		    cal.setTime(now);
		}
	    }
	    if (rotateLevel > 0) {
		checkLogFile(now);
	    }
	    int day = cal.get(Calendar.DAY_OF_MONTH);
	    
	    if (day < 10) {
		datecache[0] = '0';
		datecache[1] = (char)('0' + day);
	    } else {
		datecache[0] = (char)('0' + day / 10);
		datecache[1] = (char)('0' + day % 10);
	    }
	    monthnames[cal.get(Calendar.MONTH)].getChars(0,3,datecache,3);
	    int year = cal.get(Calendar.YEAR);
	    datecache[10] = (char)('0' + year % 10);
	    year = year / 10;
	    datecache[9] = (char)('0' + year % 10);
	    year = year / 10;
	    datecache[8] = (char)('0' + year % 10);
	    year = year / 10;
	    datecache[7] = (char)('0' + year);
	    if (hour < 10) {
		datecache[12] = '0';
		datecache[13] = (char)('0' + hour);
	    } else {
		datecache[12] = (char)('0' + hour / 10);
		datecache[13] = (char)('0' + hour % 10);
	    }
	}
	int minutes = cal.get(Calendar.MINUTE);
	if (minutes < 10) {
	    datecache[15] = '0';
	    datecache[16] = (char)('0' + minutes);
	} else {
	    datecache[15] = (char)('0' + minutes / 10);
	    datecache[16] = (char)('0' + minutes % 10);	    
	}
	int seconds = cal.get(Calendar.SECOND);
	if (seconds < 10) {
	    datecache[18] = '0';
	    datecache[19] = (char)('0' + seconds);
	} else {
	    datecache[18] = (char)('0' + seconds / 10);
	    datecache[19] = (char)('0' + seconds % 10);	    
	}
	sb.append(datecache);
    }
	    

	
    /**
     * Log the given HTTP transaction.
     * This is shamelessly slow.
     */
    public void log (Request request, Reply reply, int nbytes, long duration) {
	Client client = request.getClient() ;
	long   date   = reply.getDate();

	String user = (String) request.getState(AuthFilter.STATE_AUTHUSER);
	URL urlst = (URL) request.getState(Request.ORIG_URL_STATE);
	String requrl;
	if (urlst == null) {
	    URL u = request.getURL();
	    if (u == null) {
		requrl = noUrl;
	    } else {
		requrl = u.toExternalForm();
	    }
	} else {
	    requrl = urlst.toExternalForm();
	}
	StringBuffer sb = new StringBuffer(512);
	String logs;
	int status = reply.getStatus();
	if ((status > 999) || (status < 0)) {
	    status = 999; // means unknown
	}
	synchronized(sb) {
	    byte ib[] = client.getInetAddress().getAddress();
	    if (ib.length == 4) {
		boolean doit;
		for (int i=0; i< 4; i++) {
		    doit = false;
		    int b = ib[i];
		    if (b < 0) {
			b += 256;
		    }
		    if (b > 99) {
			sb.append((char)('0' + (b / 100)));
			b = b % 100;
			doit = true;
		    }
		    if (doit || (b > 9)) {
			sb.append((char)('0' + (b / 10)));
			b = b % 10;
		    }
		    sb.append((char)('0'+b));
		    if (i < 3) {
			sb.append('.');
		    }
		}
	    } else { // ipv6, let's be safe :)
		sb.append(client.getInetAddress().getHostAddress());
	    }
	    sb.append(" - ");
	    if (user == null) {
		sb.append("- [");
	    } else {
		sb.append(user);
		sb.append(" [");
	    }
	    dateCache(date, sb);
	    sb.append("] \"");
	    sb.append(request.getMethod());
	    sb.append(' ');
	    sb.append(requrl);
	    sb.append(' ');
	    sb.append(request.getVersion());
	    sb.append("\" ");
	    sb.append((char)('0'+ status / 100));
	    status = status % 100;
	    sb.append((char)('0'+ status / 10));
	    status = status % 10;
	    sb.append((char)('0'+ status));
	    sb.append(' ');
	    if (nbytes < 0) {
		sb.append('-');
	    } else {
		sb.append(nbytes);
	    }
	    sb.append('\n');
	    logs = sb.toString();
	}
	logmsg(logs);
    }

    public void log(String msg) {
	logmsg(msg);
    }

    public void errlog (Client client, String msg) {
	errlogmsg (client + ": " + msg + "\n") ;
    }

    public void errlog (String msg) {
	errlogmsg (msg + "\n") ;
    }

    public void trace (Client client, String msg) {
	tracemsg (client + ": " + msg + "\n") ;
    }

    public void trace (String msg) {
	tracemsg (msg + "\n") ;
    }

    /**
     * Get the name for the file indicated by the provided property.
     * This method first looks for a property value. If none is found, it
     * than constructs a default filename from the server root, by 
     * using the provided default name.
     * <p>This method shall either succeed in getting a filename, or throw
     * a runtime exception.
     * @param propname The name of the property.
     * @param def The default file name to use.
     * @exception HTTPRuntimeException If no file name could be deduced from
     *     the provided set of properties.
     */

    protected String getFilename (String propname, String def) {
	String filename = props.getString (propname, null) ;
	File flogdir = null;
	if ( filename == null ) {
	    String logdirname = props.getString(LOGDIRNAME_P, null);
	    if ( logdirname == null) {
		File root_dir = server.getRootDirectory();
		if ( root_dir == null ) {
		    String msg = "unable to build a default value for the \""
			+ propname + "\" value." ;
		    throw new HTTPRuntimeException (this.getClass().getName()
						    , "getFilename"
						    , msg) ;
		}
		flogdir = new File(root_dir, "logs") ;
	    } else {
		try {
		    flogdir = new File(logdirname);
		} catch (RuntimeException ex) {
		    String msg = "unable to access log directory "+logdirname;
		    throw new HTTPRuntimeException (this.getClass().getName()
						    , "getFilename"
						    , msg) ;
		}
	    }
	    return (new File(flogdir, def)).getAbsolutePath() ;
	} else {
	    String logdirname = props.getString(LOGDIRNAME_P, null);
	    if ( logdirname == null)
		return filename ;
	    try {
		flogdir = new File(logdirname);
	    } catch (RuntimeException ex) {
		String msg = "unable to access log directory "+logdirname;
		throw new HTTPRuntimeException (this.getClass().getName()
						, "getFilename"
						, msg) ;
	    }
	    return (new File(flogdir, filename)).getAbsolutePath() ;
	}
    }

    /**
     * Open this logger log file.
     */

    protected void openLogFile () {
	if (rotateLevel > 0) {
	    Date now = new Date();
	    this.year = -1;
	    checkLogFile(now);
	} else {
	    String logname = getFilename(LOGNAME_P, "log") ;
	    try {
		RandomAccessFile old = log ;
		log = new RandomAccessFile (logname, "rw") ;
		log.seek (log.length()) ;
		if ( old != null )
		    old.close () ;
	    } catch (IOException e) {
		throw new HTTPRuntimeException (this.getClass().getName()
						, "openLogFile"
						, "unable to open "+logname);
	    }
	}
    }

    /**
     * Open this logger error log file.
     */

    protected void openErrorLogFile () {
	String errlogname = getFilename (ERRLOGNAME_P, "errlog") ;
	try {
	    RandomAccessFile old = errlog ;
	    errlog = new RandomAccessFile (errlogname, "rw") ;
	    errlog.seek (errlog.length()) ;
	    if ( old != null )
		old.close() ;
	} catch (IOException e) {
	    throw new HTTPRuntimeException (this.getClass().getName()
					    , "openErrorLogFile"
					    , "unable to open "+errlogname);
	}
    }

    /**
     * Open this logger trace file.
     */

    protected void openTraceFile () {
	String tracename = getFilename (TRACELOGNAME_P, "traces");
	try {
	    RandomAccessFile old = trace ;
	    trace = new RandomAccessFile (tracename, "rw") ;
	    trace.seek (trace.length()) ;
	    if ( old != null )
		old.close() ;
	} catch (IOException e) {
	    throw new HTTPRuntimeException (this.getClass().getName()
					    , "openTraceFile"
					    , "unable to open "+tracename);
	}
    }

    /**
     * Save all pending data to stable storage.
     */

    public synchronized void sync() {
	try {
	    if ((buffer != null) && (bufptr > 0)) {
		log.write(buffer, 0, bufptr);
		bufptr = 0;
	    }
	} catch (IOException ex) {
	    server.errlog(getClass().getName()
			  + ": IO exception in method sync \""
                          + ex.getMessage() + "\".");
	}
    }

    /**
     * Shutdown this logger.
     */

    public synchronized void shutdown () {
	server.getProperties().unregisterObserver (this) ;
	try {
	    // Flush any pending output:
	    if ((buffer != null) && (bufptr > 0)) {
		log.write(buffer, 0, bufptr);
		bufptr = 0;
	    }
	    log.close() ; 
	    log = null ;
	    errlog.close() ;
	    errlog = null ;
	    trace.close() ;
	    trace = null ;
	} catch (IOException ex) {
	    server.errlog(getClass().getName()
			  + ": IO exception in method shutdown \""
                          + ex.getMessage() + "\".");
	}
    }
		
    /**
     * Initialize this logger for the given server.
     * This method gets the server properties describe above to
     * initialize its various log files.
     * @param server The server to which thiss logger should initialize.
     */

    public void initialize (httpd server) {
	this.server = server ;
	this.props  = server.getProperties() ;
	// Register for property changes:
	props.registerObserver (this) ;
        // init the rotation level
        rotateLevel = props.getInteger(ROTATE_LEVEL_P, 0);
	// Open the various logs:
	openLogFile () ;
	openErrorLogFile() ;
	openTraceFile() ;
	// Setup the log buffer is possible:
	if ((bufsize = props.getInteger(BUFSIZE_P, bufsize)) > 0 ) 
	    buffer = new byte[bufsize];
	return ;
    }
	
    /**
     * Construct a new Logger instance.
     */

    CommonLogger () {
	this.msgbuf = new byte[128] ;
    }
}


