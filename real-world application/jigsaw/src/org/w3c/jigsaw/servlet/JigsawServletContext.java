// JigsawServletContext.java
// $Id: JigsawServletContext.java,v 1.1 2010/06/15 12:24:10 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.w3c.util.EmptyEnumeration;
import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;

import org.w3c.tools.resources.event.StructureChangedAdapter;
import org.w3c.tools.resources.event.StructureChangedEvent;

import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.DirectoryResource;
import org.w3c.tools.resources.FileResource;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.proxy.ForwardFrame;
import org.w3c.jigsaw.resources.VirtualHostResource;
import org.w3c.jigsaw.http.httpd;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class JigsawServletContext extends StructureChangedAdapter
                                  implements ServletContext,  
					     PropertyMonitoring
{

    public static final String TEMPDIR_P = "javax.servlet.context.tempdir";

    class Logger {
	File             logfile  = null;
	RandomAccessFile log      = null ;
	byte             msgbuf[] = null ;
	boolean          closed   = true;      

	private final String monthnames[] = {
	    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
	    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
	};

	String getDate() {
	    Date now = new Date();
	    return (now.getDate()
		    + "/" + monthnames[now.getMonth()]
		    + "/" + (now.getYear() + 1900)
		    + ((now.getHours() < 10)
		       ? (":0" + now.getHours())
		       : (":" + now.getHours()))
		    + ((now.getMinutes() < 10)
		       ? (":0" + now.getMinutes())
		       : (":" + now.getMinutes()))
		    + ((now.getSeconds() < 10)
		       ? (":0" + now.getSeconds())
		       : (":" + now.getSeconds()))
		    + ((now.getTimezoneOffset() < 0)
		       ? " " + (now.getTimezoneOffset() / 60)
		       : " +" + (now.getTimezoneOffset() / 60)));
	}

	void log(String msg) {
	    msg = "["+getDate()+"] "+msg+"\n";
	    try {
		if ( log == null || closed)
		    openLogFile();
		if ( log != null ) {
		    int len = msg.length() ;
		    if ( len > msgbuf.length ) 
			msgbuf = new byte[len] ;
		    msg.getBytes (0, len, msgbuf, 0) ;
		    log.write (msgbuf, 0, len) ;
		}
	    } catch (IOException ex) {
		System.out.println("Can't write ("+
				   msg+") to logfile ["+
				   logfile+"] : "+ex.getMessage());
	    }
	}

	void log(Exception ex, String msg) {
	    log(msg+" : "+ex.getClass().getName()+" ("+ex.getMessage()+")");
	}

	void log(Throwable throwable, String msg) {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(out);
	    throwable.printStackTrace(writer);
	    writer.close();
	    String stacktrace = out.toString();
	    log(msg+" "+stacktrace);
	}

	void openLogFile() 
	    throws IOException
	{
	    RandomAccessFile old = log ;
	    log = new RandomAccessFile (logfile, "rw") ;
	    log.seek (log.length()) ;
	    closed = false;
	    if ( old != null )
		old.close () ;
	}

	void close() {
	    try {
		if (log != null)
		    log.close();
		closed = true;
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

	Logger(File logfile) {
	    this.logfile = logfile;
	    this.msgbuf  = new byte[128] ;
	    log("Servlet Logger started");
	}
    }

    private ResourceReference reference = null;

    private Logger logger = null;

    private ObservableProperties props = null;

    private File directory = null;

    private Hashtable attributes = null;

    protected static String logdir     = "logs" ;

    protected static String deflogfile = "servlets";

    public boolean propertyChanged (String name) {
	if (name.equals(ServletProps.SERVLET_LOG_FILE_P)) {
	    if (logger != null) {
		logger.close();
		File newlogfile = new File((String) 
				   props.get(ServletProps.SERVLET_LOG_FILE_P));
		if (newlogfile.getPath().length() < 1) 
		    newlogfile = getServletLogFile();
		logger = new Logger(newlogfile);
	    }
	}
	return true;
    }

    public void resourceUnloaded(StructureChangedEvent evt){
	if (logger != null) {
	    logger.close();
	}
    }

    protected long getServletTimeout() {
	return props.getLong(ServletProps.SERVLET_TIMEOUT, -1);
    }

    protected int getServletInstanceMax() { // added for single thread model servlet instance pool size limitation, tk, 20.10.2001
	return props.getInteger(ServletProps.SERVLET_INSTANCEMAX, 0);
    }
    
    /**
     * A useful utility routine that tries to guess the content-type
     * of an object based upon its extension.
     */
    protected static String guessContentTypeFromName(String fname) {
	return org.w3c.www.mime.Utils.guessContentTypeFromName(fname);
    }

    /**
     * ServletContext implementation - Get the MIME type for given file.
     */
    public String getMimeType(String filename) {
	return guessContentTypeFromName(filename);
    }

    public ServerInterface getServer() {
	try {
	    Resource res = reference.lock();
	    return ((ServletDirectoryFrame)res).getServer();
	} catch(InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    reference.unlock();
	}
    }

    public File getServletLogFile() {
	ServerInterface server = getServer();
	File logfile = null;
	String file = (String)
	    server.getProperties().getString(ServletProps.SERVLET_LOG_FILE_P,
					     null);
	if (file != null)
	    logfile = new File(file);
	if ((logfile != null) && (logfile.getPath().length() < 1))
	    logfile = null;
	if (logfile == null) {
	    File root_dir = server.getRootDirectory();
	    if (root_dir == null) {
		throw new RuntimeException("unable to build a default "+
					   "value for the servlet log file.");
	    }
	    logfile = new File(new File(root_dir, logdir), deflogfile);
	    server.getProperties().putValue(ServletProps.SERVLET_LOG_FILE_P,
					    logfile.getAbsolutePath());
	}
	return logfile;
    }

    /**
     * ServletContext implementation - Lookup a given servlet.
     * @deprecated since jsdk2.1
     */

    public Servlet getServlet(String name) {
	try {
	    Resource res = reference.lock();
	    return ((ServletDirectoryFrame)res).getServlet(name);
	} catch(InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    reference.unlock();
	}
    }

    /**
     * ServletContext implementation - Enumerate all servlets within context.
     * @deprecated since jsdk2.1
     */

    public Enumeration getServlets() {
	try {
	    Resource res = reference.lock();
	    return ((ServletDirectoryFrame)res).getServlets();
	} catch(InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    reference.unlock();
	}
    }

    /**
     * ServletContext implementation - Enumerate all servlets names 
     * within context.
     * @deprecated since jsdk2.1
     */
    public Enumeration getServletNames() {
	try {
	    Resource res = reference.lock();
	    return ((ServletDirectoryFrame)res).getServletNames();
	} catch(InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    reference.unlock();
	}
    }

    /**
     * ServletContext implementation - Log a message.
     */
    public void log(String msg) {
	logger.log(msg);
    }

    /**
     * @deprecated since jsdk2.1
     */
    public void log(Exception ex, String msg) {
	logger.log(ex,msg);
    }

    public void log(String message, Throwable throwable) {
	logger.log(throwable, message);
    }

    /**
     * ServletContext implementation - Translate a piece of path.
     * @param path the virtual path to translate
     * @param rr_root the Root ResourceReference
     * @param rr the target ResourceReference
     * @return the real path
     */
    protected static String getRealPath(String path, 
					ResourceReference rr_root,
					ResourceReference rr) 
    {
	ResourceReference local_root = getLocalRoot(rr_root, rr);
	try {
	    FramedResource root = (FramedResource)local_root.lock();
	    LookupState    ls   = new LookupState(path);
	    LookupResult   lr   = new LookupResult(local_root);
	    if (root.lookup(ls,lr)) {
		ResourceReference  target = lr.getTarget();
		if (target != null) {
		    try {
			FramedResource res = (FramedResource)target.lock();
			if (res instanceof FileResource) {
			    File file = ((FileResource)res).getFile();
			    return file.getAbsolutePath();
			} else if (res instanceof DirectoryResource) {
			    DirectoryResource dir = (DirectoryResource) res;
			    return dir.getDirectory().getAbsolutePath();
			    //return getFilePath(dir);
			}
		    } finally {
			target.unlock();
		    }
		}
	    }
	    return null;
	} catch (InvalidResourceException ex) {
	    return null;
	} catch (org.w3c.tools.resources.ProtocolException pex) {
	    return null;
	} finally {
	    local_root.unlock();
	}
    }
    
    /**
     * from Servlet 2.3, very file-oriented
     * FIXME always returning null as of now
     */
    public Set getResourcePaths(String path) {
	return null;
    }
    /**
     * from Servlet 2.3, very file-oriented should return a web-app container
     * name
     * FIXME always returning null as of now
     */
    public String getServletContextName() {
	return null;
    }

    /**
     * ServletContext implementation - Translate a piece of path.
     * @param path the virtual path to translate
     * @return the real path
     */
    public String getRealPath(String path) {
	ResourceReference rr_root = ((httpd) getServer()).getRootReference();
	return getRealPath(path, rr_root, reference);
    }

    protected static String getFilePath(DirectoryResource dir) {
	HTTPFrame frame = 
	    (HTTPFrame)dir.getFrame("org.w3c.jigsaw.frames.HTTPFrame");
	String indexes[] = frame.getIndexes();
	if (indexes != null) {
	    for (int i = 0 ; i < indexes.length ; i++) {
		String index = indexes[i];
		if ( index != null && index.length() > 0) {
		    ResourceReference rr = dir.lookup(index);
		    if (rr != null) {
			try {
			    FramedResource ri = (FramedResource) rr.lock();
			    if (ri instanceof FileResource) {
				FileResource fr = (FileResource) ri;
				File file = fr.getFile();
				return file.getAbsolutePath();
			    } else {
				// we don't know 
				return null;
			    }
			} catch (InvalidResourceException ex) {
			} finally {
			    rr.unlock();
			}
		    }
		}
	    }
	    return dir.getDirectory().getAbsolutePath();
	} else {
	    return dir.getDirectory().getAbsolutePath();
	}
    }

    protected static ResourceReference getLocalRoot(ResourceReference rr_root,
						    ResourceReference ref) 
    {
	try {
	    FramedResource root = (FramedResource)rr_root.lock();
	    if (root instanceof VirtualHostResource) {
		//backward to the virtual host resource
		ResourceReference rr  = null;
		ResourceReference rrp = null;
		FramedResource    res = null;
		try {
		    res = (FramedResource)ref.lock();
		    if (res instanceof ResourceFrame) {
			ResourceFrame fr = (ResourceFrame)res;
			rr = fr.getResource().getResourceReference();
		    } else {
			rr = ref;
		    }
		} catch (InvalidResourceException ex) {
		    return rr_root;
		} finally {
		    ref.unlock();
		}

		while (true) {
		    try {
			res = (FramedResource)rr.lock();
			rrp = res.getParent();
			if ((rrp == rr_root) || (rrp == null)) {
			    return getLocalRoot(rr, ref);
			}
		    } catch (InvalidResourceException ex) {
			return rr_root;
		    } finally {
			rr.unlock();
		    }
		    rr = rrp;
		}
	    } else {
		try {
		    FramedResource res = (FramedResource)rr_root.lock();
		    ForwardFrame   ffr = (ForwardFrame)
			res.getFrame("org.w3c.jigsaw.proxy.ForwardFrame");
		    if (ffr == null) {
			return rr_root;
		    } else {
			ResourceReference rr = ffr.getLocalRootResource();
			return getLocalRoot(rr, ref);
		    }
		} catch (InvalidResourceException ex) {
		    return rr_root;
		}
	    }
	} catch (InvalidResourceException ex) {
	    return rr_root;
	} finally {
	    rr_root.unlock();
	}
    }

    /**
     * ServletContext implementation - Get server informations.
     */

    public String getServerInfo() {
	try {
	    Resource res = reference.lock();
	    return ((ServletDirectoryFrame)res).getServerInfo();
	} catch(InvalidResourceException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    reference.unlock();
	}	
    }

    /**
     * ServletContext implementation - Get an attribute value.
     * We map this into the ServletWrapper attributes, without
     * support for name clashes though.
     * @param name The attribute name.
     */

    public Object getAttribute(String name) {
	Object attribute = attributes.get(name);
	if (attribute != null) {
	    return attribute;
	} else {
	    try {
		Resource res = reference.lock();
		return ((ServletDirectoryFrame)res).getAttribute(name);
	    } catch(InvalidResourceException ex) {
		ex.printStackTrace();
		return null;
	    } finally {
		reference.unlock();
	    }
	}
    }

    public void setAttribute(String name, Object object) {
	attributes.put(name, object);
    }

    public void removeAttribute(String name) {
	attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
	return attributes.keys();
    }

    /**
     * Returns a <code>String</code> containing the value of the named
     * context-wide initialization parameter, or <code>null</code> if the 
     * parameter does not exist.
     *
     * <p>This method can make available configuration information useful
     * to an entire "web application".  For example, it can provide a 
     * webmaster's email address or the name of a system that holds 
     * critical data.
     *
     * @param name a <code>String</code> containing the name of the
     * parameter whose value is requested
     * @return 	a <code>String</code> containing at least the 
     * servlet container name and version number
     * @see ServletConfig#getInitParameter
     */
    public String getInitParameter(String name) {
	// @@ not implemented @@
	return null;
    }

   


    /**
     * Returns the names of the context's initialization parameters as an
     * <code>Enumeration</code> of <code>String</code> objects, or an
     * empty <code>Enumeration</code> if the context has no initialization
     * parameters.
     *
     * @return an <code>Enumeration</code> of <code>String</code> 
     * objects containing the names of the context's initialization parameters
     * @see ServletConfig#getInitParameter
     */
    public Enumeration getInitParameterNames() {
	// @@ not implemented @@
	return new EmptyEnumeration();
    }

    private AutoReloadServletLoader loader = null;

    /** 
     * Get or create a suitable LocalServletLoader instance to load 
     * that servlet.
     * @return A LocalServletLoader instance.
     */
    protected synchronized AutoReloadServletLoader getLocalServletLoader() {
	if ( loader == null ) {
	    loader = new AutoReloadServletLoader(this);
	}
	return loader;
    }

    protected synchronized 
	AutoReloadServletLoader createNewLocalServletLoader (boolean keepold) 
    {
	if ((loader != null) && keepold)
	    loader = new AutoReloadServletLoader(loader);
	else
	    loader = new AutoReloadServletLoader(this);
	return loader;
    }

    public File getServletDirectory() {
	return directory;
    }

    //jsdk2.1

    /**
     * Returns a RequestDispatcher object for the specified URL path if 
     * the context knows of an active source (such as a servlet, JSP page,
     * CGI script, etc) of content for the particular path. This format of
     * the URL path must be of the form /dir/dir/file.ext. The servlet 
     * engine is responsible for implementing whatever functionality is 
     * required to wrap the target source with an implementation of
     * the RequestDispatcher interface. 
     * @param urlpath Path to use to look up the target server resource
     */
    public RequestDispatcher getRequestDispatcher(String urlpath) {
	return JigsawRequestDispatcher.getRequestDispatcher(urlpath, 
							   (httpd)getServer(),
							    reference);
    }

    /**
     * Returns a {@link RequestDispatcher} object that acts
     * as a wrapper for the named servlet.
     *
     * <p>Servlets (and JSP pages also) may be given names via server 
     * administration or via a web application deployment descriptor.
     * A servlet instance can determine its name using 
     * {@link ServletConfig#getServletName}.
     *
     * <p>This method returns <code>null</code> if the 
     * <code>ServletContext</code>
     * cannot return a <code>RequestDispatcher</code> for any reason.
     *
     * @param name a <code>String</code> specifying the name
     * of a servlet to wrap
     * @return a <code>RequestDispatcher</code> object
     * that acts as a wrapper for the named servlet
     * @see RequestDispatcher
     * @see ServletContext#getContext
     * @see ServletConfig#getServletName
     */
    public RequestDispatcher getNamedDispatcher(String name) {
	if (name == null) {
	    throw new IllegalArgumentException("null");
	}
	return JigsawRequestDispatcher.getRequestDispatcher(name, 
							    reference,
							   (httpd)getServer());
    }

    public int getMajorVersion() {
	return 2;
    }

    public int getMinorVersion() {
	return 2;
    }

    public ServletContext getContext(String uripath) {
	if (uripath == null)
	    return null;
	//first, find the ServletDirectoryFrame.
	// Prepare for lookup:
	ResourceReference rr_root = null;
	rr_root = ((httpd) getServer()).getRootReference();

	FramedResource root = null;
	root = ((httpd) getServer()).getRoot();

	// Do the lookup:
	ResourceReference r_target = null;
	try {
	    LookupState  ls = new LookupState(uripath);
	    LookupResult lr = new LookupResult(rr_root);
	    root.lookup(ls, lr);
	    r_target = lr.getTarget();
	} catch (Exception ex) {
	    r_target = null;
	}
	//then return its context
	if (r_target != null) {
	    try {
		Resource target = r_target.lock();
		if (target instanceof FramedResource) {
		    ServletDirectoryFrame frame = (ServletDirectoryFrame)
			((FramedResource) target).
		      getFrame("org.w3c.jigsaw.servlet.ServletDirectoryFrame");
		    if (frame != null)
			return frame.getServletContext();
		}
	    } catch (InvalidResourceException ex) {
		// continue
	    } finally {
		r_target.unlock();
	    }
	}
	return null;
    }

    public URL getResource(String path) 
	throws MalformedURLException
    {
	// FIXME? is it allowed?
	File file = new File(path);
	if (file.exists()) {
	    return new URL("file", "", file.getAbsolutePath());
	}
	String realpath = getRealPath(path);
	if (realpath != null) {
	    file = new File(realpath);
	    if (file.exists()) {
		return new URL("file", "", file.getAbsolutePath());
	    } else {
		return null;
	    }
	} else {
	    // it could be a virtual resource
	    // check that it exists (on server)
	    // FIXME (Virtual host)
	    return null;
	}
    }

    public InputStream getResourceAsStream(String path) {
	try {
	    URL resource = getResource(path);
	    if (resource == null)
		return null;
	    try {
		URLConnection c = resource.openConnection();
		return c.getInputStream();
	    } catch (IOException ex) {
		return null;
	    }
	} catch (MalformedURLException ex) {
	    return null;
	}
    }

    /**
     * Create a new ServletContext.
     * @param ref a ResourceReference pointing on a ServletDirectoryFrame.
     */
    protected JigsawServletContext(ResourceReference ref, 
				   ObservableProperties props) 
    {
	this.reference  = ref;
	this.props      = props;
	this.attributes = new Hashtable(3);
	this.logger     = new Logger(getServletLogFile());
	this.loader     = new AutoReloadServletLoader(this);

	props.registerObserver(this);

	try {
	    Resource res = reference.lock();
	    if (! (res instanceof ServletDirectoryFrame)) {
		throw new IllegalArgumentException("This reference is not "+
				      "pointing on a ServletDirectoryFrame.");
	    } else {
		ServletDirectoryFrame sframe = (ServletDirectoryFrame)res;
		FramedResource resource = (FramedResource)sframe.getResource();
		resource.addStructureChangedListener(this);
		if (resource.definesAttribute("directory"))
		    this.directory = 
			(File) resource.getValue("directory", null);
	    }
	} catch(InvalidResourceException ex) {
	    throw new IllegalArgumentException("This reference is pointing on"+
				         " an Invalid ServletDirectoryFrame.");
	} finally {
	    reference.unlock();
	}
    }

}
