// httpd.java
// $Id: httpd.java,v 1.2 2010/06/15 17:52:58 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.w3c.tools.resources.AbstractContainer;
import org.w3c.tools.resources.DummyResourceReference;
import org.w3c.tools.resources.FilterInterface;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ResourceSpace;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.store.ResourceStoreManager;
import org.w3c.tools.resources.indexer.IndexerModule;
import org.w3c.tools.resources.indexer.IndexersCatalog;
import org.w3c.tools.resources.indexer.ResourceIndexer;
import org.w3c.tools.timers.EventManager;
import org.w3c.jigsaw.auth.RealmsCatalog;
import org.w3c.jigsaw.resources.CheckpointResource;
import org.w3c.jigsaw.daemon.ServerHandler;
import org.w3c.jigsaw.daemon.ServerHandlerInitException;
import org.w3c.jigsaw.daemon.ServerHandlerManager;
import org.w3c.www.http.HTTP;
import org.w3c.www.http.HeaderValue;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;
import org.w3c.www.http.HttpTokenList;
import org.w3c.jigsaw.config.PropertySet;
import org.w3c.util.IO;
import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;
import org.w3c.util.Status;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.upgrade.Upgrader;
import org.w3c.www.mime.MimeParserFactory;
import org.w3c.www.mime.MimeType;

/**
 * <p>The server main class. This class can be used either through its
 * main method, to run a full httpd server, or simply by importing it
 * into your app. This latter possibility allows you to export some of
 * your application state through http.
 *
 * <p>The server itself uses this to report about memory consumption,
 * running threads, etc.
 */

public class httpd implements 
      ServerInterface, Runnable, PropertyMonitoring, Cloneable, Status
{
    /**
     * The current displayed version of Jigsaw.
     */
    public static final String version = "2.2.6";
    /**
     * The current internal version counter of Jigsaw.
     * This counter is bumped anytime the configuration needs upgrade.
     */
    public static final int verscount = 4;

    /**
     * debug flag
     */
    public static final boolean debug = true;

    public static final
    String VERSCOUNT_P = "org.w3c.jigsaw.version.counter";
    /**
     * Name of the server software property.
     * The server software is the string that gets emited by Jigsaw 
     * on each reply, to tell the client what server emited the reply.
     * <p>This property defaults to <strong>Jigsaw/1.0a</strong>.
     */
    public static final 
    String SERVER_SOFTWARE_P = "org.w3c.jigsaw.server";
    /**
     * If the Host property is not set (see below), you can select if you
     * want to use FQDN (broken on some jdk implementation) or just the IP
     * address as the default host name, it usually defaults to "false"
     * means, use FQDN.
     */
    public static final String DEFHOSTIP_P = "org.w3c.jigsaw.defhostip" ;
    /**
     * Name of the server host property.
     * The host property should be set to the name of the host running
     * this server.
     * <p>This property defaults to the local host name, although if you want
     * directory listing to work propertly, you might need to provide the 
     * full host name (including its domain).
     */
    public static final String HOST_P            = "org.w3c.jigsaw.host" ;
    /**
     * Name of the property giving the server root directory.
     * <p>The server root directory is used to deduce a bunch of defaults
     * properties, when they don't have any specific values.
     * <p>This property has no defaults.
     */
    public static final String ROOT_P = "org.w3c.jigsaw.root" ;
    /**
     * Name of the property giving the server's config directory.
     */
    public static final String CONFIG_P = "org.w3c.jigsaw.config";
    /**
     * Name of the property giving the server space directory.
     * The server space directory should contain an index file, built
     * with the indexer.
     * <p>This property defaults to <org.w3c.jigsaw.root>/WWW.
     */
    public static final String SPACE_P            = "org.w3c.jigsaw.space" ;
    /**
     * Name of the server port property.
     * At initializatiojn time, the server will bind its accepting socket
     * to the host its runs on, and to the provided port.
     * <p>This property defaults to <code>8888</code>.
     */
    public static final String PORT_P            = "org.w3c.jigsaw.port" ;
    /**
     * Name of the server's trace property.
     * When set to true, the server will emit some traces indicating 
     * its current state by using the logger <em>trace</em> methods.
     * This property should be set to <string>true</strong> if you want
     * clients to emit traces.
     * <p>This property defaults to <strong>false</strong>.
     */
    public static final String TRACE_P           = "org.w3c.jigsaw.trace" ;
    /**
     * Name of the server's keep alive flag.
     * This property is used to determine wether this server should keep
     * its connection alive. Keeping connection alive requires this flag
     * to set to <strong>true</strong>, and clients to be compliant to the
     * keep alive feature as described in HTTP/1.1 specification.
     * <p>This property defaults to <strong>true</strong>.
     */
    public static final String KEEP_ALIVE_P      = "org.w3c.jigsaw.keepAlive" ;
    /**
     * Name of the server's connection time out property.
     * This property gives, in milliseconds, the timeout to use for
     * connections that remains idel, waiting for an incoming request.
     * <p>This property defaults to <code>10000</code> milliseconds.
     */
    public static final String KEEP_TIMEOUT_P=
                                           "org.w3c.jigsaw.keep_alive.timeout";
    /**
     * Name of the server's request time out property.
     * The request time out property value indicates, in milliseconds, the
     * allowed duration of a request. Any request whose duration exceeds
     * this time out value will be aborted.
     * <p>This property defaults to <code>60000</code>.
     */
    public static final String REQUEST_TIMEOUT_P=
                                              "org.w3c.jigsaw.request.timeout";
    /**
     * Name of the client thread priority property.
     * Every client threads will run at the given priority, which should be
     * in the range of valid threads priority.
     * <p>This property defaults to <code>Thread.NORM_PRIORITY</code>.
     */
    public static final String CLIENT_PRIORITY_P=
                                              "org.w3c.jigsaw.client.priority";
    /**
     * Nam eof the property giving the client output buffer size.
     * Each clients, when not using a shuffler, has to allocate its own
     * output buffer, Output buffer size may increase/decrease significantly
     * the Jigsaw performances, so change it with care.
     * <p>This property defaults to <code>8192</code>.
     */
    public static final String CLIENT_BUFSIZE_P=
                                               "org.w3c.jigsaw.client.bufsize";
    /**
   * Name of the property indicating wether client should be debuged.
   * When debuged, clients emit some traces, through the server logger
   * about their current state.
   * <p>This property defaults to <strong>false</strong>.
   */
    public static final String CLIENT_DEBUG_P="org.w3c.jigsaw.client.debug" ;
    /**
     * Name of  property that indicates if some security manager is required.
     * You usually don't want to run a security manager for the server, 
     * except in the unlikely (right now) case that you want the server to
     * be able to host agents.
     * <p>This property defaults to <string>false</strong>.
     */
    public static final String USE_SM_P = "org.w3c.http.useSecurityManager" ;
    /**
     * Name of property indicating the logger class to use.
     * The Jigsaw server allows you to implement your own logger. The only
     * logger provided with the core server is the 
     * <code>org.w3c.jigsaw.core.CommonLogger</code>, which implements the
     * common log format.
     * <p>Property defaults to <code>org.w3c.jigsaw.core.CommonLogger</code>
     */
    public static final String LOGGER_P = "org.w3c.jigsaw.logger" ;
    /**
     * Name of property indicating the "lenient" mode of HTTP parsing.
     * <p>Property defaults to <code>true</code>
     */
    public static final String LENIENT_P = "org.w3c.jigsaw.http.lenient" ;
    /**
     * Name of the property indicating the client factory class.
     */
    public static final String CLIENT_FACTORY_P = 
	"org.w3c.jigsaw.http.ClientFactory";
    /**
     * Name of the property giving the shuffler path.
     * This property should be set if you are to use the shuffler. The 
     * data shuffler is an external process to whiuch Jigsaw delegates 
     * the task of writing back document content to clients. Use this
     * when you think your server isn't fast enough.
     * <p>This should be an absloute path.
     * <p>This property has no defaults.
     */
    public static final String SHUFFLER_PATH_P="org.w3c.jigsaw.shuffler.path";

    /**
     * Name of the property giving the name of the root resource.
     * Upon startup, or restart, the server will look in its root store
     * a resource whose name is given by this resource, and install it as
     * its root resource.
     * <p>This property defaults to <code>root</code>.
     */
    public static final String ROOT_NAME_P = "org.w3c.jigsaw.root.name" ;
    public static final String ROOT_CLASS_P = "org.w3c.jigsaw.root.class" ;

    /**
     * Max number of store loaded in memory.
     */ 
    public static final String MAX_LOADED_STORE_P="org.w3c.jigsaw.loadedstore";
    public static final int MAX_LOADED_STORE = 128;
    int max_loaded_store = -1;

    /**
     * Max number of store loaded in memory.
     */ 
    public static final String STORE_SIZE_LIMIT_P="org.w3c.jigsaw.storesize";
    public static final int STORE_SIZE_LIMIT = -1;
    int store_size_limit = -1;
    
    /**
     * Name of the property giving the path of the property file.
     * this should be used internally (for restart) only.
     * <p>This property defaults to <code>config/httpd.props</code>.
     */
    public static final String PROPS_P = "org.w3c.jigsaw.propfile" ;
    /**
     * Name of the property indicating if the file-system is case sensitive.
     * This property determines wether Jigsaw will list all files to check 
     * for case sensitivity, before creating a resource for that file.
     * <p>For obvious security reasons, this property defaults to 
     * <strong>true</strong>.
     */
    public static final 
    String FS_SENSITIVITY = "org.w3c.jigsaw.checkSensitivity";
    /**
     * Name of the property indicating the URL of Jigsaw's help.
     * This URL should point to the URL path of Jigsaw's documentation
     * as served by that server.
     */
    public static String DOCURL_P = "org.w3c.jigsaw.docurl";

    /**
     * Name of the property indicating the startup classes to load
     */
    public static String STARTUP_P = "org.w3c.jigsaw.startup";

    /**
     * Name of the property indicating the trash directory.
     */
    public static String TRASHDIR_P = "org.w3c.jigsaw.trashdir";

    /**
     * Name of the property indicating the URL of Jigsaw's chekpointer.
     */
    public static 
    String CHECKURL_P = "org.w3c.jigsaw.checkpointer";

    /**
     * Name of the property indicating the public methods allowed on that 
     * server.
     * This property should provide a <code>|</code> separated list of
     * methods available on that server.
     * <p>This property defaults to: <strong>GET | HEAD | PUT | POST 
     * | OPTIONS | DELETE | LINK | UNLINK | TRACE</code>.
     */
    public static 
    String PUBLIC_P = "org.w3c.jigsaw.publicMethods";
    /**
     * Name of the property that indicates the root resource for edit.
     * The edit root resource is the one that will show up by default
     * when accessing the admin server from JigAdmin.
     */
    public static 
    String EDIT_ROOT_P = "org.w3c.jigsaw.edit.root";

    /**
     * Name of the serializer class used to store resources.
     */
    public static String SERIALIZER_CLASS_P = "org.w3c.jigsaw.serializer";

    /**
     * UNIX - Name of the property that indicates the server user.
     * When set, the server will try to turn itself to the given user name
     * after initialization. If this fail, the server will abort.
     * <p>This property has no default value.
     */
    public static
    String SERVER_USER_P = "org.w3c.jigsaw.unix.user";
    /**
     * UNIX - Name of the property that indicates the server group.
     * When set, the server will try to turn itself to the given group name
     * after initialization. If this fail, the server will abort.
     * <p>This property has no default value.
     */
    public static
    String SERVER_GROUP_P = "org.w3c.jigsaw.unix.group";

    /**
     * Should we show the URL that triggered an error in the error message
     * or not?
     * Displaying it can lead to so-called "cross-scripting" hacks
     */
    public static
    String DISPLAY_URL_ON_ERROR_P = "org.w3c.jigsaw.error.url";
   
    /**
     * The list of currently running servers.
     */
    private static Hashtable servers = new Hashtable() ;

    /* FIXME */ public Thread thread    = null ;

    private String         software  = "Jigsaw/2.2.6";
    private ServerSocket   socket    = null ;
    private Logger         logger    = null ;
    private Shuffler       shuffler  = null ;
    public  EventManager   timer     = null ;
    ClientFactory          factory   = null ;

    // FIXME This is a temporary hack to take care of clones
    protected int[] instances = {1};    // object containing the nb of clones

    /**
     * The (optional) server handler manager that created us.
     */
    private ServerHandlerManager shm = null;
    /**
     * The server identifier can be any String.
     * This identifier is used by the configuration applets, to show all the 
     * running servers in the process, and to edit their properties, etc.
     */
    private String identifier = null ;

    /**
     * This server statistics object.
     */
    private httpdStatistics statistics = null ;
    /**
     * This server set of properties.
     */
    protected ObservableProperties props    = null ;
    /** 
     * Should the server run the server in trace mode ?
     */
    private boolean tracep = false ;
    /**
     * Should the server try to keep connections alive ?
     */
    private boolean keep = true ; 
    /**
     * What logger class should the server use to log accesses.
     */
    private String logger_class = null ;
    /**
     * Should we display URL on error?
     */
    private boolean uri_error = false;
    /**
     * Are we lenient in HTTP mode?
     */
    private boolean lenient = true;
    /**
     * What client factory class should the server use.
     */
    private String 
    factory_class = "org.w3c.jigsaw.http.socket.SocketClientFactory";
    /**
     * The coordinate of the shuffler, or <strong>null</strong> is none is to 
     * be used. 
     */
    private String shuffler_path = null ;
    /**
     * The server's root directory.
     */
    private File root_dir = null ;
    /**
     * The directory containing the server exported documents.
     */
    private File space_dir = null ;
    /**
     * FIXME check
     * The server host name.
     */
    protected String host = null ;
    /**
     * FIXME check
     * The server port.
     */
    protected int port = 8001 ;
    /**
     * This server client debug flag.
     */
    private boolean client_debug = false ;
    /**
     * This server's request time slice, in milliseconds.
     */
    private int request_time_out = 1200000 ;
    /**
     * This server's connection allowed idle time in milliseconds.
     */
    private int connection_time_out = 1200000 ;
    /**
     * This server's client thread priority.
     */
    private int client_priority = Thread.NORM_PRIORITY ;
    /**
     * This server's clients buffer size.
     */
    private int client_bufsize = 4096 ;
    /**
     * Is the file system case-sensitive ?
     */
    private boolean sensitivity = true;
    /**
     * This server root entity.
     */
    public FramedResource root = null ;
    /**
     * the old root ResourceReference
     */
    private ResourceReference root_reference = null;
    /**
     * FIXME check for clones
     * This server URL.
     */
    protected URL url = null ;
    /**
     * Finishing (killing) the server.
     */
    private boolean finishing = false ;
    /**
     * Finishing, but restart straight up.
     */
    private boolean restarting = false ;
    /**
     * The indexer attached to this server.
     */
    private ResourceIndexer indexer = null ;
    /**
     * The realm catalog
     */
    private RealmsCatalog realms = null ;
    /**
     * The resource store manager for this server.
     */
    private ResourceStoreManager manager = null ;
    /**
     * The root resource's identifier.
     */
    private String root_name = null ;
    private String root_class = null;
    /**
     * The full URL of Jigsaw's documentation as served by that server.
     */
    private String docurl = null;

    /**
     * The trash directory
     */
    private String trashdir = null;

    /**
     * The full URL of Jigsaw's chekpointer.
     */
    private String checkurl = null;

    /**
     * The list of public methods for that server.
     */
    private  String publicMethods[] = { "GET"
					, "HEAD"
					, "PUT"
					, "POST"
					, "LINK"
					, "UNLINK"
					, "DELETE"
					, "OPTIONS" 
					, "TRACE"
    } ;
    /**
     * The <code>Public</code> header value, computed out of the
     * <code>publicMethods</code> variable.
     */
    private HttpTokenList publicHeader = null;
    /**
     * The edit root for that server.
     */
    private ResourceReference editroot = null;
    /**
     * the sets of properties of this server
     */
    private Vector         propSet     = new Vector(8);
    /**
     * the catalog of indexers of this server 
     */
    private IndexersCatalog indexers = null;
    /**
     * the resource context of this server 
     */
    private ResourceContext context = null;
    /**
     * the config resource of this server
     */
    private AbstractContainer configResource = null;
    /**
     * and its dummy resource reference
     */
    private ResourceReference rr_configResource = null;

    // is this server a clone?
    private boolean isAClone = false;
    // our master server ID, if we are a clone
    private String masterID = null;

    /**
     * The property monitoring implementation.
     * @param name The name of the property that has changed.
     * @return A boolean, <strong>true</strong> if the changed was taken into
     *    account, <strong>false</strong> otherwise.
     */
    public boolean propertyChanged (String name) {
	// Is this a property we are interested in ?
	if ( name.equals(SERVER_SOFTWARE_P) ) {
	    software = props.getString(name, software);
	    return true;
	} else if ( name.equals(TRACE_P) ) {
	    tracep = props.getBoolean(name, tracep) ;
	    errlog (name + " changed to " + tracep) ;
	    return true ;
	} else if ( name.equals(LENIENT_P) ) {
	    lenient = props.getBoolean(name, lenient) ;
	    errlog (name + " changed to " + lenient) ;
	    return true ;
	} else if ( name.equals(DISPLAY_URL_ON_ERROR_P) ) {
	    uri_error = props.getBoolean(name, uri_error);
	    errlog (name + " changed to " + uri_error);
	    return true;
	} else if ( name.equals(KEEP_ALIVE_P) ) {
	    keep = props.getBoolean (name, keep) ;
	    errlog (name + " changed to " + keep) ;
	    return true ;
	} else if ( name.equals(LOGGER_P) ) {
	    String tmp_logger_class;
	    Logger tmp_logger;
	    tmp_logger_class = props.getString(name, logger_class);
	    // for now the removal of the logger should be done by hand
	    if (!tmp_logger_class.equals(logger_class)) {
		try {
		    tmp_logger = (Logger) 
			Class.forName(tmp_logger_class).newInstance() ;
		    //Added by Jeff Huang
		    //TODO: FIXIT
		} catch (Exception ex) {
		    errlog (name + " change failed (bad logger class)") ;
		    return false;
		}
		synchronized (this) {
		    if (logger != null) {
			logger.shutdown();
		    }
		    tmp_logger.initialize(this);
		    logger = tmp_logger;
		    logger_class = tmp_logger_class;
		}
	    }
	    return true;
	} else if ( name.equals(ROOT_NAME_P) ) {
	    String newname = props.getString(name, null);
	    if ( changeRoot(newname) != null ) {
		errlog("new root resource ["+newname+"]");
		return true;
	    } else {
		errlog("failed to change root to ["+newname+"].");
		return false;
	    }
	} else if ( name.equals(SPACE_P) ) {
	    errlog (name + " change failed (server running)") ;
	    return false ;
	} else if ( name.equals(HOST_P) ) {
	    errlog (name + " change failed (server running)") ;
	    return false ;
	} else if ( name.equals(PORT_P) ) {
	    // we will restart the server
	    errlog (name + " switching port : " + props.getInteger(name, 80)) ;
	    int newport = props.getInteger(name, 80);
	    if (port != newport) {
		int oldport = port;
		port = newport;
		checkpoint();
		ServerSocket newsocket = null;
		try {
		    newsocket = factory.createServerSocket();
		    socket.close();
		    socket = newsocket;
		} catch (Exception ex) {
		    try { newsocket.close(); } catch (Exception e) {};
		    port = oldport;
		    // an error occured, return false
		    return false;
		}
	    }
	    return true ;
	} else if ( name.equals(CLIENT_DEBUG_P) ) {
	    client_debug = props.getBoolean(name, client_debug) ;
	    errlog (name + " changed to " + client_debug) ;
	    return true ;
	} else if ( name.equals(REQUEST_TIMEOUT_P) ){
	    request_time_out = props.getInteger(name, request_time_out);
	    errlog (name + " changed to " + request_time_out) ;
	    return true ;
	} else if ( name.equals(KEEP_TIMEOUT_P) ) {
	    connection_time_out = props.getInteger(name
						   , connection_time_out);
	    errlog (name + " changed to " + connection_time_out) ;
	    return true ;
	} else if ( name.equals(CLIENT_PRIORITY_P) ){
	    client_priority = props.getInteger (name, client_priority) ;
	    errlog (name + " changed to " + client_priority) ;
	    return true ;
	} else if ( name.equals(CLIENT_BUFSIZE_P) ){
	    client_bufsize = props.getInteger (name, client_bufsize) ;
	    errlog (name + " changed to " + client_bufsize) ;
	    return true ;
	} else if ( name.equals(DOCURL_P) ) {
	    String propval = props.getString(name, docurl);
	    try {
		URL u  = new URL(getURL(), propval);
		docurl = u.toExternalForm();
	    } catch (Exception ex) {
		return false;
	    }
	    return true;
	} else if (name.equals(TRASHDIR_P)) {
	    trashdir = props.getString(name, trashdir);
	    File dir = new File(trashdir);
	    if (! dir.exists())
		dir.mkdirs();
	    errlog(name + " changed to "+ trashdir);
	    return true;
	} else if ( name.equals(CHECKURL_P) ) {
	    checkurl = props.getString(name, checkurl);
	    errlog(name + " changed to "+ checkurl);
	    return true;
	} else if ( name.equals(PUBLIC_P) ) {
	    publicMethods = props.getStringArray(name, publicMethods);
	    publicHeader  = null;
	    return true;
	} else if ( name.equals(SERVER_USER_P) ) {
	    String user = props.getString(SERVER_USER_P, null);
	    errlog("new user: "+user);
	    return false;
	} else if (name.equals(SERVER_GROUP_P) ) {
	    String group = props.getString(SERVER_GROUP_P, null);
	    errlog("new group: "+group);
	    return false;
	} else {
	    // We  don't care about this one
	    return true ;
	}
    }

    /**
     * Initialize this server indexer.
     */
    private void initializeIndexer() {
	ResourceContext c  = getDefaultContext();
	IndexersCatalog ic = getIndexersCatalog();
	IndexerModule   m  = new IndexerModule(ic);
	// Register the default indexer:
	m.registerIndexer(c, "default");
	// Register the indexer module:
	c.registerModule(IndexerModule.NAME, m);
    }

    /**
     * Initialize the resource store manager for this server.
     */
    private void initializeResourceSpace(String server_name,
					 String root_class,
					 String root_name,
					 String serializer,
					 int max_loaded_store) 
    {
	Hashtable defs = new Hashtable(11) ;
	defs.put("url", "/");
	defs.put("directory", space_dir) ;
	defs.put("context", getDefaultContext()) ;
	
	this.manager = new ResourceStoreManager(server_name,
						this.getStoreDirectory(),
						root_class,
						root_name,
						serializer,
						max_loaded_store,
						store_size_limit,
						defs);
    }

    /**
     * Lookup the root store for some resource.
     * @param name The name of the resource to lookup in the root store.
     * @return The loaded resource, or <strong>null</strong>.
     */
    public ResourceReference loadResource(String name) {
	Hashtable defs = new Hashtable(11) ;
	defs.put("url", "/"+name);
	defs.put("directory", space_dir) ;
	ResourceContext context = new ResourceContext(getDefaultContext());
	defs.put("context", context) ;
	ResourceReference rr = manager.loadRootResource(name, defs);
	if (rr != null)
	    context.setResourceReference(rr);
	return rr;
    }

    /**
     * start the automatic checkpoint
     */
    public void startCheckpoint() {
	if (checkurl == null) {
	    errlog("checkpointer URL unknown.");
	    checkpoint();
	    return;
	}
	try {
	    LookupState   ls  = new LookupState(checkurl);
	    LookupResult  lr  = new LookupResult(root.getResourceReference());
	    if (root.lookup(ls,lr)) {
		ResourceReference  target = lr.getTarget();
		if (target != null) {
		    try {
			Resource res = target.lock();
			if (res instanceof CheckpointResource) {
			    ((CheckpointResource) res).activate();
			    errlog("Chekpointer started at: "+new Date()+".");
			} else {
			    errlog("The chekpointer url ("+checkurl+
				   ") doesn't point to a CheckpointResource");
			    checkpoint();
			}
		    } catch (InvalidResourceException ex) {
			errlog("Invalid Checkpointer : "+ex.getMessage());
			checkpoint();
		    } finally {
			target.unlock();
		    }
		} else {
		    errlog("can't find Checkpointer");
		    checkpoint();
		}
	    } else {
		errlog("Checkpointer: lookup fail");
		checkpoint();
	    }
	} catch (ProtocolException ex) {
	    errlog("Checkpointer : "+ex.getMessage());
	    checkpoint();
	}
    }

    /**
     * Checkpoint all cached data, by saving them to disk.
     */
    public void checkpoint() {
	manager.checkpoint();
    }

    /**
     * Dynamically change the root resource for the server.
     * This is kind a dangerous operation !
     * @param name The name of the new root resource, to be found in the
     * root resource store.
     * @return The new installed root resource, or <strong>null</strong>
     * if we couldn't load the given resource.
     */
    public synchronized ResourceReference loadRoot(String name) {
	ResourceReference newroot = null;
	String editRootName = props.getString(EDIT_ROOT_P, null);

	// Restore the appropriate root resource:
	Hashtable defs = new Hashtable(11) ;
	defs.put("url", "/");
	defs.put("directory", space_dir) ;
	ResourceContext context = null;
	if ((editRootName != null) && (! name.equals(editRootName))) {
	    if (editroot == null) {
		Hashtable edefs = new Hashtable(11) ;
		edefs.put("url", "/");
		edefs.put("directory", space_dir) ;
		ResourceContext econtext = 
		    new ResourceContext(getDefaultContext());
		edefs.put("context", econtext) ;
		editroot = manager.loadRootResource(editRootName, edefs);
		if (editroot != null)
		    econtext.setResourceReference(editroot);
	    }
	    context = new ResourceContext(editroot);
	} else {
	    context = new ResourceContext(getDefaultContext());
	}
	defs.put("context", context) ;
	ResourceReference rr = manager.loadRootResource(name, defs);
	if (rr != null)
	    context.setResourceReference(rr);
	return rr;
    }

    private synchronized FramedResource changeRoot(String name) {
	ResourceReference newroot = loadRoot(name);
	FramedResource oldroot = this.root;
	String oldroot_name = this.root_name;
	if ( newroot != null ) {
	    try {
		this.root      = (FramedResource)newroot.lock();
		this.root_name = name;
		if (root_reference != null)
		    root_reference.unlock();
		root_reference = newroot;
		return root;
	    } catch (InvalidResourceException ex) {
		this.root = oldroot;
		this.root_name = oldroot_name;
		return null;
	    }
	}
	return null;
    }

    /**
     * Initialize this server's root resource.
     * @exception ServerHandlerInitException if unable to be initialized.
     */

    private void initializeRootResource() 
	throws ServerHandlerInitException
    {
	// Check for un-found root resource:
	if ( changeRoot(root_name) == null ) {
	    String err = ("Unable to restore root resource ["+root_name+"]"
			  +" from store (not found).");
	    throw new ServerHandlerInitException(err);
	}
    }

    /**
     * Initialize the realms catalog for this server.
     */

    private void initializeRealmsCatalog() {
	this.realms = 
	    new RealmsCatalog(new ResourceContext(getDefaultContext()));
    }

    /**
     * Initialize the server logger and the statistics object.
     * @exception ServerHandlerInitException if unable to be initialized.
     */

    private void initializeLogger() 
	throws ServerHandlerInitException
    {
	if ( logger_class != null ) {
	    try {
		logger = (Logger) Class.forName(logger_class).newInstance() ;
		
		//Added by Jeff Huang
		logger = new org.w3c.jigsaw.http.CommonLogger();
		logger.initialize (this) ;
	    } catch (Exception ex) {
		String err = ("Unable to create logger of class ["+
			      logger_class +"]"+
			      "\r\ndetails: \r\n"+
			      ex.getMessage());
		throw new ServerHandlerInitException(err);
	    }
	} else {
	    warning (getBanner() + ": no logger specified, not logging.");
	}
	// Initialize the statistics object:
	statistics = new httpdStatistics(this) ;
    }

    /**
     * Initialize the server socket, create a suitable client factory, start.
     * This method creates the physicall listening socket, and instantiate
     * an appropriate client factory for that socket. It then run the accept
     * thread, ready to accept new incomming connections.
     * @exception ServerHandlerInitException if unable to be initialized.
     */

    private void initializeServerSocket() 
	throws ServerHandlerInitException
    {
	// Create a suitable client factory:
	try {
	    Class c = Class.forName(factory_class);
	    factory = (ClientFactory) c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
	    factory.initialize(this);
	} catch (Exception ex) {
	    String err = ("Unable to create a client factory of class "+
			  "\"" + factory_class + "\""+
			  " details: \r\n" + ex.getMessage());
	    throw new ServerHandlerInitException(err);
	}
	// If needed, create a server socket instance for that context:
	try {
	    socket = factory.createServerSocket();
	} catch (IOException ex) {
	    String err = ("Unable to create server socket on port "+port
			  + ": " + ex.getMessage() + ".");
	    throw new ServerHandlerInitException(err);
	}
	this.thread   = new Thread (this) ;
	this.thread.setName(identifier) ;
	this.thread.setPriority (Thread.MAX_PRIORITY) ;
    }

    protected MimeParserFactory getMimeClientFactory(Client client) {
	return new MimeClientFactory(client);
    }

    /**
     * Initialize our event manager.
     */

    private void initializeEventManager() {
	this.timer   = new EventManager () ;
	this.timer.setDaemon(true);
	this.timer.start() ;
    }

    /**
     * startup classes
     */
    protected void loadStartupClasses() {
	String classes[] = props.getStringArray(STARTUP_P, null);
	if (classes != null) {
	    for (int i = 0 ; i < classes.length ; i++) {
		try {
		    Class c = Class.forName(classes[i]);
		    httpdPreloadInterface hpi = 
			(httpdPreloadInterface)c.newInstance();
		    //Added by Jeff Huang
		    //TODO: FIXIT
		    hpi.preload(this);
		} catch (ClassNotFoundException cnfex) {
		    errlog("Startup class not found : "+cnfex.getMessage());
		} catch (InstantiationException iex) {
		    errlog("Unable to instanciate : "+iex.getMessage());
		} catch (ClassCastException ccex) {
		    errlog("Startup classes must be instance of "+
			   "httpdPreloadInterface: "+ccex.getMessage());
		} catch (IllegalAccessException iaex) {
		    errlog("IllegalAccess "+iaex.getMessage());
		}
	    }
	}
    }

    /**
     * FIXME protected for now to handle clones
     * Initialize some of the servers instance values from properties.
     * @exception ServerHandlerInitException if unable to be initialized.
     */

    protected void initializeProperties() 
	throws ServerHandlerInitException
    {
	// Compute some default values (host and port)
	String defhost  = null ;
	String rootstr  = null ;
	String spacestr = null ;
	
	boolean ip_host = props.getBoolean(DEFHOSTIP_P, false);

	try {
	    if (ip_host)
		defhost = InetAddress.getLocalHost().getHostAddress() ;
	    else 
		defhost = InetAddress.getLocalHost().getHostName() ;
	} catch (UnknownHostException e) {
	    defhost = null;
	}
	// Second stage: get property values:
	software         = props.getString(SERVER_SOFTWARE_P, software);
	tracep           = props.getBoolean(TRACE_P,tracep) ;
	uri_error        = props.getBoolean(DISPLAY_URL_ON_ERROR_P, false);
	lenient          = props.getBoolean(LENIENT_P, true);
	keep             = props.getBoolean(KEEP_ALIVE_P,keep) ;
	logger_class     = props.getString(LOGGER_P, null) ;
	factory_class    = props.getString(CLIENT_FACTORY_P,factory_class);
	shuffler_path    = props.getString(SHUFFLER_PATH_P, null) ;
	rootstr          = props.getString(ROOT_P, null) ;
	spacestr         = props.getString(SPACE_P, null);
	host             = props.getString(HOST_P, defhost) ;
	port             = props.getInteger(PORT_P, port) ;
	root_name        = props.getString(ROOT_NAME_P, "root") ;
	root_class       = props.getString(ROOT_CLASS_P, null);
	max_loaded_store = props.getInteger(MAX_LOADED_STORE_P,
					    MAX_LOADED_STORE);
	store_size_limit = props.getInteger(STORE_SIZE_LIMIT_P,
					    STORE_SIZE_LIMIT);
	sensitivity      = props.getBoolean(FS_SENSITIVITY, true);
	publicMethods    = props.getStringArray(PUBLIC_P, publicMethods);
	// Get client properties:
	client_debug        = props.getBoolean (CLIENT_DEBUG_P, client_debug) ;
	request_time_out    = props.getInteger (REQUEST_TIMEOUT_P
						, request_time_out);
	connection_time_out = props.getInteger (KEEP_TIMEOUT_P
						, connection_time_out);
	client_priority     = props.getInteger(CLIENT_PRIORITY_P
					       , client_priority);
	client_bufsize      = props.getInteger(CLIENT_BUFSIZE_P
					       , client_bufsize);
	// Check that a host name has been given:
	if ( host == null )
	    throw new ServerHandlerInitException(this.getClass().getName()
						 +"[initializeProperties]: "
						 +"[host] undefined.");
	// Default the root directory to the current directory:
	if ( rootstr == null ) {
	    // Try the current directory as root:
	    rootstr = System.getProperties().getProperty("user.dir", null) ;
	    if ( rootstr == null )
		throw new ServerHandlerInitException(this.getClass().getName()
						     +"[initializeProperties]:"
						     +"[root] undefined.");
	}
	root_dir = new File(rootstr) ;
	// Default the space directory to root/WWW
	if ( spacestr == null ) 
	    space_dir = new File(root_dir, "WWW") ;
	else
	    space_dir = new File(spacestr) ;
	// Help URL:
	String propval = props.getString(DOCURL_P, null);
	if ( propval != null ) {
	    try {
		URL u  = new URL(getURL(), propval);
		docurl = u.toExternalForm();
	    } catch (Exception ex) {
	    }
	}
	// Trash Dir
	trashdir = props.getString(TRASHDIR_P, trashdir);
	// checpointer url
	checkurl = props.getString(CHECKURL_P, checkurl);
    }

    /**
     * Register a property set to the server.
     * @param propSet The property set to register.
     */    
    public synchronized void registerPropertySet(PropertySet set) {
	// Add this set to our known property set:
	propSet.addElement(set);
    }

    /** 
     * Enumerate all the registered property sets
     * @return an enumeration of </code>PropertySet</code>
     */
    public Enumeration enumeratePropertySet() {
	return propSet.elements(); 
    }

    /** 
     * Get a property set matching a specific name 
     * @return a Resource, the property set found
     */   
    public Resource getPropertySet(String name) {
	for (int i = 0 ; i < propSet.size() ; i++) { 
	    PropertySet set = (PropertySet) propSet.elementAt(i);
	    if ( set.getIdentifier().equals(name) )
		return set;
	}
	return null; 
    } 

    protected void initializePropertySets() {
	registerPropertySet(new GeneralProp("general", this)); 
	registerPropertySet(new ConnectionProp("connection", this)); 
	registerPropertySet(new LoggingProp("logging", this)); 
    } 

    /**
     * Get this server statistics.
     */

    public httpdStatistics getStatistics() {
	return statistics ;
    }

    /**
     * Get this server properties.
     */

    public ObservableProperties getProperties() {
	return props ;
    }

    /**
     * Is the underlying file-system case sensitive ?
     * @return A boolean, <strong>true</strong> if file system is case 
     * sensitive, <strong>false</strong> otherwise.
     */
    public boolean checkFileSystemSensitivity() {
	return sensitivity;
    }

    /**
     * Get the full URL of Jigsaw's documentation.
     * @return A String encoded URL.
     */
    public String getDocumentationURL() {
	return docurl;
    }

    /**
     * Get the tracsh directory
     */
    public String getTrashDirectory() {
	return trashdir;
    }

    /**
     * Get the client's debug flags from the properties.
     */
    public final boolean getClientDebug() {
	return client_debug ;
    }

    /**
     * Does this server wants clients to try keeping connections alive ?
     */
    public final boolean getClientKeepConnection() {
	return keep ;
    }

    /**
     * Get the request allowed time slice from the properties.
     */
    public final int getRequestTimeOut() {
	return request_time_out ;
    }

    /**
     * Get the connection allowed idle time from the properties.
     */
    public final int getConnectionTimeOut() {
	return connection_time_out ;
    }

    /**
     * Get the client's threads priority from the properties.
     */
    public final int getClientThreadPriority() {
	return client_priority ;
    }

    /**
     * Get the client's buffer size.
     */
    public final int getClientBufferSize() {
	return client_bufsize ;
    }

    /**
     * Get this server host name.
     */
    public String getHost () {
	return host ;
    }

    /**
     * Get this server port number.
     */
    public int getPort () {
	return port ;
    }

    /**
     * Get the server current root resource.
     */
    public FramedResource getRoot() {
	return root ;
    }

    /**
     * get the resource reference of the root resource of the server
     */
    public ResourceReference getRootReference() {
	return root_reference;
    }

    /**
     * Get the logger for that server.
     * @return A Logger compatible instance, or <strong>null</strong> if 
     * no logger specified.
     */
    public Logger getLogger() {
	return logger;
    }

    /**
     * Get the server's edit root resource.
     * The edit root is the one that shows up by default when using JigAdmin
     * It is named "root" in the interface.
     * @return An HTTPResource.
     */

    public synchronized ResourceReference getEditRoot() {
	if ( editroot == null ) {
	    // Check for the appropriate property:
	    String name = props.getString(EDIT_ROOT_P, null);
	    if ( name != null ) {
		editroot = loadRoot(name);
	    }
	    if ( editroot == null ) {
		editroot = getRootReference();
	    }
	}
	return editroot;
    }

    /**
     * Get the server URL.
     */
    public URL getURL() {
	if ( url == null ) {
	    try {
		if ( port != 80 ) 
		    url = new URL("http", host, port, "/");
		else
		    url = new URL("http", host, "/");
	    } catch (MalformedURLException ex) {
		throw new RuntimeException("unable to build server's URL");
	    }
	}		
	return url ;
    }

    /**
     * Get the server software string.
     */
    public String getSoftware () {
	return software;
    }

    /**
     * Get the server local port
     */
    public int getLocalPort() {
	return socket.getLocalPort() ;
    }

    /**
     * Get this server identifier.
     */
    public String getIdentifier() {
	return identifier ;
    }

    /**
     * Get the server inet address
     * @return The INET address this server is listening to.
     */
    public InetAddress getInetAddress() {
	return socket.getInetAddress() ;
    }

    /**
     * Get this server root directory.
     */
    public File getRootDirectory() {
	return root_dir ;
    }

    /**
     * Get this server space diretory
     */
    public File getSpaceDir() {
	return space_dir;
    }

    /**
     * Get this server config directory.
     */
    public File getConfigDirectory() {
	File file = props.getFile(CONFIG_P, null);
	return (file == null) ? new File(getRootDirectory(), "config") : file;
    }

    /**
     * Get this server authentication directory.
     */
    public File getAuthDirectory() {
	return new File(getConfigDirectory(), "auth");
    }

    /**
     * Get this server store directory.
     */
    public File getStoreDirectory() {
	return new File(getConfigDirectory(), "stores");
    }

    /**
     * Get this server index directory
     */
    public File getIndexerDirectory() {
	return new File(getConfigDirectory(), "indexers");
    }

    /**
     * Get temp directory
     */
    public File getTempDirectory() {
	return new File(getRootDirectory(), "tmp");
    }

    /**
     * Clean the temp dir.
     */
    protected void cleanTempDirectory() {
	org.w3c.util.IO.clean(getTempDirectory());
    }

    /**
     * get the indexer catalog of this server 
     */
    public IndexersCatalog getIndexersCatalog() {
	if ( indexers == null )
	    indexers = new IndexersCatalog(
		new ResourceContext(getDefaultContext()));
	return indexers;
    }

    /**
     * Get this server realm catalog.
     */
    public RealmsCatalog getRealmsCatalog() {
	return realms ;
    }

    /**
     * Get this server resourcestore manager.
     */
    public ResourceStoreManager getResourceStoreManager() {
	return manager ;
    }

    /**
     * Get this server resource space
     */
    public ResourceSpace getResourceSpace() {
	return manager ;
    }

    /**
     * Get the default resource context for that server.
     */
    public ResourceContext getDefaultContext() {
	return context;
    }

    /**
     * Get the lenient value, tru if we are lenient in HTTP parsing
     */
    public boolean isLenient() {
	return lenient;
    }

    /**
     * Cleanup the resources associated with this server context.
     * This method should only be called by the server thread itself, when
     * it is requested to perform the cleanup.
     * @param restart If <strong>true</strong> the server is restarted 
     *     (reinitialized) straight away.
     */

    protected synchronized void cleanup(boolean restart) {
	// Close the accepting socket:
	try {
	    socket.close() ;
	    socket = null ;
	} catch (IOException ex) {
	    errlog ("[cleanup]: IOException while closing server socket.");
	}
	// FIXME temporary hack for clones
 	synchronized( instances ) {
	    // remove one instance
	    instances[0]--;         // @wplatzer
	    if ( factory != null )
		factory.shutdown(true) ;
	    factory = null ;
	    if ( manager != null && instances[0] == 0) // FIXME (shm)
		manager.shutdown() ;
	    manager = null ;
	    if ( shuffler != null )
		shuffler.shutdown() ;
	    shuffler = null ;
	    // Unregister to property monitoring
	    props.unregisterObserver (this) ;
	    errlog ("shutdown completed at: "+new Date()+".") ;
	    // Finally close the log
	    if ( logger != null && instances[0] == 0) // FIXME shm
		logger.shutdown() ;
	    logger = null ;
	    // Release any other pointers:
	    timer.stopEventManager() ;
	    System.out.println (getIdentifier()+": " + getURL() + " done.") ;
	    System.out.flush() ;
	// Keep the data neede to reinit (in case needed)
	    File init_propfile = props.getFile(PROPS_P, null);
	    ObservableProperties init_props = props ;
	    String init_identifier = identifier ;
	    // Release pointed data:
	    identifier = null ;
	    manager    = null ;
	    factory    = null ;
	    shuffler   = null ;
	    // FIXME clones props      = null ;
	    indexer    = null ;
	    root       = null ;
	    realms     = null ;
	    logger     = null ;
	    socket     = null ;
	    timer      = null ;
	    thread     = null ;
	    url        = null ;
	    restarting = false ;
	    finishing  = false ;
	    if ( restart ) {
		try {
		    instances[0]++; //FIXME clones
		    initialize(shm, init_identifier, init_props) ;
		    start();
		} catch (Exception ex) {
		    // We really can't do more than this here:
		    System.out.println("*** server restart failed.") ;
		    ex.printStackTrace() ;
		}
	    }
	}
    }

    /**
     * Shutdown the server properly.
     * This methods shutdown the server, and clean-up all its associated 
     * resources. If the current thread is not the server thread, it unblocks
     * the server thread from its accept() call, and forces it to perform
     * the rest of the shutdown operation itself.
     * @see httpd#cleanup
     */

    public synchronized void shutdown () {
	checkpoint();
	errlog ("shutdown inited...(save done)") ;
	finishing = true ;
	try {
	    Socket unlock = new Socket(host, port) ;
	    unlock.close() ;
	} catch (IOException ex) {
	    errlog("[shutdown]: IOException while unblocking server thread.");
	}
	shm.removeServerHandler(this);
	cleanTempDirectory();
    }

    /**
     * Restart the server properly.
     * This methods restarts the server. It cleans-up all its associated 
     * resources, and reinitialize it from scratch. If the current thread is
     * not the server thread, it unblocks
     * the server thread from its accept() call, and forces it to perform
     * the rest of the restart operation itself.
     * @param reload_properties Should we reload the properties from the
     *    property file, or should we just reinitialize from the current set
     *    of properties.
     * @see httpd#cleanup
     */

    public synchronized void restart () {
	errlog ("[restart]: inited !") ;
	finishing    = true ;
	restarting   = true ;
	try {
	    Socket unlock = new Socket(host, port) ;
	    unlock.close() ;
	} catch (IOException ex) {
	    errlog ("[restart]: IOException while unblocking server thread.");
	}
    }

    /**
     * Turn debugging on/off for this instance of httpd server.
     * @param A boolean, true turns debugging on, flase turns it off.
     */

    public void debug (boolean onoff) {
	tracep = onoff ;
    }

    /**
     * Emit a server trace. Traces are used solely for debugging purposes. You
     * should either use <b>log</b> or <b>error</b> to report informations.
     * @param client The client object which wants to report the trace.
     * @param msg The trace message.
     * @see httpd#log
     */

    public void trace (Client client, String msg) {
	if ( tracep && (logger != null) )
	    logger.trace (client, msg) ;
    }

    /**
     * Emit a server trace, on behalf of the server itself.
     * @param msg The trace the server wants to emit.
     */
    public void trace (String msg) {
	if ( tracep && (logger != null))
	    logger.trace (msg) ;
    }

    /**
     * Emit a log entry.
     * @param client The client whose request is to be logged.
     * @param request The request that has been handled.
     * @param reply The emitted reply.
     * @param nbytes The number of bytes emitted back to the client.
     * @param duration The time it took to process the request.
     */
    public void log (Client client
		     , Request request, Reply reply
		     , int nbytes
		     , long duration) {
	if ( logger != null )
	    logger.log (request, reply, nbytes, duration) ;
	statistics.updateStatistics(client, request, reply, nbytes, duration) ;
    }

    /**
     * Emit a log message.
     * @param msg The message to log.
     */
    public void log(String msg) {
	logger.log(msg);
    }

    /**
     * Emit a server error on behalf of some client object.
     * @param client The client.
     * @param msg The error message.
     */
    public void errlog (Client client, String msg) {
	if ( logger != null )
	    logger.errlog(client, msg) ;
    }

    /**
     * Emit an error on behalf of the server.
     * @param msg The error message.
     */

    public void errlog (String msg) {
	if ( logger != null )
	    logger.errlog ("["+identifier+"] "+msg) ;
    }

    /**
     * The prefered form for reporting errors.
     * @param from The object that emited the error.
     * @param msg The error message.
     */
    public void errlog(Object from, String msg) {
	if ( logger != null )
	    logger.errlog("["+from.getClass().getName()+"]: "+msg);
    }

    /**
     * Another nice way of reporting errors from an HTTPResource.
     * @param from The resource that trigered the error.
     * @param msg The error message.
     */
    public void errlog(Resource from, String msg) {
	if ( logger != null )
	    logger.errlog(from.getClass().getName()+"@"+from.unsafeGetURLPath()
			  + ": " + msg);
    }

    /**
     * Emit a fatal error.
     * @param e Any exception that caused the error.
     * @param msg Any additional message.
     */
    public void fatal (Exception e, String msg) {
	System.out.println ("*** Fatal Error, aborting") ;
	System.out.println (this.getClass().getName() + ": " + msg) ;
	e.printStackTrace() ;
	throw new RuntimeException (msg) ;
    }

    /**
     * Emit a fatal error.
     * @param msg Any error message
     */
    public void fatal(String msg) {
	System.out.println("*** Fatal error, aborting") ;
	System.out.println(this.getClass().getName() + ": " + msg) ;
	throw new RuntimeException(msg) ;
    }
	
    /**
     * Emit a warning.
     * Warnings are emited, typically if the configuration is inconsistent,
     * and the server can continue its work.
     * @param msg The warning message.
     */
    public void warning (String msg) {
	System.out.println ("*** Warning : " + msg) ;
    }

    /**
     * Emit a warning.
     * @param e Any exception.
     * @param msg Any message.
     */
    public void warning (Exception e, String msg) {
	System.out.println ("*** Warning: " + msg) ;
	e.printStackTrace() ;
    }

    /**
     * Get a shuffler for this server's client.
     * Whenever possible, we use a shuffler program to speed up communication
     * with the client. This methods return whatever the server deems 
     * appropriate for this client shuffler.
     * @return A Shuffler instance, or <strong>null</strong>.
     * @see org.w3c.jigsaw.http.Shuffler
     */
    public synchronized Shuffler getShuffler (Client client) {
	return shuffler ;
    }

    protected String getBanner() {
	return "Jigsaw["+version+"]";
    }

    public void run () {
	// Emit some traces before starting up:
	System.out.println(getBanner()+": serving at "+getURL());
	System.out.flush() ;
	errlog("started at: "+new Date()+".");
	// Enter the evil loop:
	while ( ( ! finishing) && ( socket != null ) ) {
	    Socket ns = null ;
	    try {
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		ns = socket.accept() ;
		ns.setTcpNoDelay(true);
	    } catch (IOException e) {
		if (debug)
		    e.printStackTrace() ;
		errlog ("failed to accept incoming connection on "+socket) ;
		// just in case, as it may have been created.
		try { ns.close(); } catch (Exception ex) {};
		ns = null;
	    }
	    if ( (socket != null) && (ns != null) && (factory != null) ) 
		factory.handleConnection (ns) ;
	}
	// Our socket has been closed, perform associated cleanup.
	cleanup(restarting) ;
    }

    /**
     * Perform the given request on behalf of this server.
     * @param request The request to perform.
     * @return A non-null Reply instance.
     * @exception ProtocolException If some error occurs during processing the
     * request.
     * @exception ResourceException If a resource got a fatal error.
     */

    public ReplyInterface perform(RequestInterface req)
	throws ProtocolException, ResourceException
    {
	Request request = (Request) req;
	// This may be a server-wide request, this is an ugly hack in HTTP spec
	if ( request.getURL() == Request.THE_SERVER ) {
	    if ( request.getMethod().equals("OPTIONS") ) {
		HttpTokenList pub = null;
		synchronized(this) {
		    if ( publicHeader == null )
			pub = HttpFactory.makeStringList(publicMethods);
		    publicHeader = pub;
		}
		Reply reply = request.makeReply(HTTP.OK);
		if ( pub != null ) {
		    reply.setHeaderValue(Reply.H_PUBLIC, pub);
		}
		reply.setContentLength(0);
		return reply;
	    }
	}
	if (request.getMethod().equals("TRACE")) {
	    // check if the resource can be proxied
	    boolean doit = true;
	    LookupState   ls = new LookupState(request);
	    LookupResult  lr = new LookupResult(root.getResourceReference());
	    try {
		if ( root.lookup(ls, lr) ) {
		    ResourceReference  target = lr.getTarget();
		    if (target != null) {
			try {
			    // this is plain ugly and won't work for proxy
			    // not based on this resource
			    // do we need another way to to this?
			    FramedResource fr = (FramedResource) target.lock();
			    Class cff = Class.forName(
				          "org.w3c.jigsaw.proxy.ForwardFrame");
			    doit = (fr.getFrameReference(cff) == null);
			} catch (Exception ex) {
			    // fail miserably to the fallback
			} finally {
			    target.unlock();
			}
		    }
		}
	    } catch (Exception ex) {};
	    if (doit) {
		Reply reply = request.makeReply(HTTP.OK);
		reply.setNoCache(); // don't cache this
		reply.setMaxAge(-1); 
		// Dump the request as the body
		// Removed unused headers:
		// FIXME should be something else for chuncked stream
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		try {
		    reply.setContentType(new MimeType("message/http"));
		    request.dump(ba);
		    reply.setContentLength(ba.size());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		reply.setStream(new ByteArrayInputStream(ba.toByteArray()));
		return reply;
	    }
	}
	// Create a lookup state, and a lookup result:

	ProtocolException error = null;
	LookupState   ls = null;
	LookupResult  lr = null;
	// Run the lookup algorithm of root resource:
	// catch exception to get error (FIXME)
	try {
	    lr = new LookupResult(root.getResourceReference());
	    ls = new LookupState(request);

	    if ( root.lookup(ls, lr) ) {
		if (lr.hasReply())
		    return lr.getReply();
	    }
	} catch (ProtocolException ex) {
	    error = ex;
	} catch (Exception ex) {
	    /*
	     * We have a problem here, the error can be a configuration
	     * or resource/extension problem, and it should be a 
	     * 5xx error, or it is a client side error and it should be
	     * a 4xx error, ex, try with "Authorization:" and it fails.
	     * For now we will reply with a 400, but with a FIXME
	     */
	    Reply err = request.makeReply(HTTP.BAD_REQUEST);
	    err.setContent("<html><head><title>Bad Request</title></head>\n"
			   + "<body><p>The server was not able to "
			   + "understand this request</p></body></html>");
	    error = new ProtocolException(err);
	}
	// Let the target resource perform the method
	ResourceReference  target = lr.getTarget();
	Reply              reply  = null;

	ResourceFilter filters[]  = lr.getFilters();
	int            infilter   = 0;

	if (error == null) {
	    //call the ingoing filters:
	    try {
		// temporary target resource !!! WARNING
		request.setTargetResource(target);
		if ( filters != null ) {
		    for ( ; infilter < filters.length ; infilter++ ) {
			if ( filters[infilter] == null )
			    continue;
			reply = (Reply)filters[infilter].
			                               ingoingFilter(request,
								     filters,
								     infilter);
			if ( reply != null ) {
			    return reply;
			}
		    }
		}
	    } catch (ProtocolException ex) {
		error = ex;
	    }
	    //perform the request:
	    if ((error == null) && (target != null)) { 
		request.setFilters(filters, infilter);
		request.setTargetResource(target);
		try {
		    FramedResource res = (FramedResource)target.lock();
		    reply = (Reply) res.perform(request);
		    if (reply == null) {
			reply = request.makeReply(HTTP.NOT_FOUND);
			if (uri_error) {
			    reply.setContent("<html><head><title>Not Found" +
					     "</title></head>\n"+
					     "<body><h1>Invalid" +
					     " URL</h1><p>The URL <b>"+
					     request.getURL()+
					     "</b> that you requested is not" +
					     " available "+
					     " for this protocol.</body>\n"
					     +"</html>");
			} else {
			    reply.setContent("<html><head><title>Not Found" +
					     "</title></head>\n"+
					     "<body><h1>Invalid" +
					     " URL</h1><p>The URL" +
					     "</b> that you requested is not" +
					     " available "+
					     " for this protocol.</body>\n"
					     +"</html>");
			}
			reply.setContentType(org.w3c.www.mime.MimeType.
					     TEXT_HTML);
		    }
		} catch (InvalidResourceException ex) {
		    //FIXME
		    reply = request.makeReply(HTTP.NOT_FOUND);
		    if (uri_error) {
			reply.setContent("<html><head><title>Not"+
					 " Found</title>"+
					 "</head><body><b>The URL <b>"+
					 request.getURL()+
					 "</b> that you requested is not " +
					 "available, "+
					 " probably deleted.</body></html>");
		    } else {
			reply.setContent("<html><head><title>Not"+
					 " Found</title>"+
					 "</head><body><b>The URL"+
					 " that you requested is not " +
					 "available, "+
					 " probably deleted.</body></html>");
		    }
		    reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
		} finally {
		    target.unlock();
		}
	    } else {
		reply = request.makeReply(HTTP.NOT_FOUND);
		if (uri_error) {
		    reply.setContent("<html><head>\n"+
				     "<title>Not Found</title></head>"+
				     "<body><h1>Invalid URL</h1><p>The URL"+
				     " <b>"+ request.getURL()+
				     "</b> that you requested is not"+
				     " available "+
				     " on that server.</body></html>");
		} else {
		    reply.setContent("<html><head>\n"+
				     "<title>Not Found</title></head>"+
				     "<body><h1>Invalid URL</h1><p>The URL"+
				     " that you requested is not"+
				     " available "+
				     " on that server.</body></html>");
		}
		reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	    }
	}
	// call the outgoing filters:
	if ((reply == null) || (reply.getStatus() != HTTP.DONE)) {
	    if ( error == null ) {
		for (int i = infilter ; --i >= 0 ; ) {
		    if ( filters[i] == null )
			continue;
		    Reply fr = (Reply)filters[i].outgoingFilter(request,
								reply,
								filters,
								i);
		    if ( fr != null )
			return fr;
		}
	    } else {
		// Make sure we always invoke appropriate filters:
		if (filters != null) {
		    for (int i = filters.length ; --i >= 0 ; ) {
			if ( filters[i] == null ) {
			    continue;
			}
			Reply fr = (Reply)filters[i].exceptionFilter(request,
								 error,
								 filters,
								 i);
			if ( fr != null ){
			    return fr;
			}
		    }
		}
		reply = (Reply)error.getReply() ;
		if (reply == null) {
		    reply = request.makeReply(HTTP.INTERNAL_SERVER_ERROR);
		    if (uri_error) {
			reply.setContent("<html><head>\n"+
					 "<title>Server Error</title>"+
					 "</head><body><h1>Invalid URL</h1>"+
					 "<p>The URL <b>"+
					 request.getURL()+
					 "</b>: isn't available "+
					 " on that server.</body></html>");
		    } else {
			reply.setContent("<html><head>\n"+
					 "<title>Server Error</title>"+
					 "</head><body><h1>Invalid URL</h1>"+
					 "<p>The URL"+
					 " isn't available "+
					 " on that server.</body></html>");
		    }
		    reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
		}
	    }
	}
	return reply;
    }

    protected boolean checkUpgrade(String id, ObservableProperties props) {
	// Check for an upgrade:
	int configvers = props.getInteger(httpd.VERSCOUNT_P, 1);
	if (configvers < httpd.verscount) {
	    System.err.println("*** Jigsaw needs upgrade from internal "+
			       "version "+configvers+" to "+ httpd.verscount);
	    if (httpd.verscount == 4) {
	        org.w3c.tools.resources.serialization.Serializer serializer =
	         new org.w3c.tools.resources.serialization.xml.XMLSerializer();
		props.put(httpd.SERIALIZER_CLASS_P, 
		    "org.w3c.tools.resources.serialization.xml.XMLSerializer");
		File propsfile    = props.getFile(PROPS_P, null);
		Upgrader upgrader = new Upgrader(id,
						 getConfigDirectory(), 
						 propsfile,
						 serializer);
		props.put(httpd.VERSCOUNT_P, String.valueOf(httpd.verscount));
		props.put(httpd.SERVER_SOFTWARE_P, "Jigsaw/"+version);
		try {
		    upgrader.upgrade(httpd.verscount);
		    return true;
		} catch (Exception ex) {
		    System.err.println(ex.getMessage());
		    return false;
		}
	    }
	    return false;
	}
	return true;
    }

    /**
     * Initialize a new HTTP server.
     * The server wil first be initialized from the available properties,
     * it will than startup, and finally run in its own thread.
     * @param identifier The string identifying this server's occurence.
     * @param props A set of properties to initialize from.
     * @exception ServerHandlerInitException if unable to be initialized.
     */

    public void initialize (ServerHandlerManager shm,
			    String identifier,
			    ObservableProperties props) 
	throws ServerHandlerInitException
    {
	// Check for an optional upgrade of config files:
	// checkUpgrade(shm, identifier, props);
	this.shm = shm;
	// Initialize from properties:
	this.identifier = identifier ;
	this.props = props;
	// with an explicit cast for buggy compilers
	this.props.registerObserver((PropertyMonitoring)this) ;
	initializeProperties() ;
	if (! checkUpgrade(identifier, props))
	    throw new ServerHandlerInitException("Upgrade failed.");
	// Create the socket, and run the server:
	initializeServerSocket();
    }

    /**
     * start the server
     * it will than startup, and finally run in its own thread.
     */

    public void start () 
	throws ServerHandlerInitException
    {
	if (!isAClone) {
	    // Create the default resource context (shared by all resources):
	    this.context = new ResourceContext(this);
	    //FIXME
	    // Create the resource store manager
	    initializeResourceSpace(identifier,
				    root_class,
				    props.getString(EDIT_ROOT_P, root_name),
				    props.getString(SERIALIZER_CLASS_P, null),
				    max_loaded_store);
	    this.context.setSpace(getResourceSpace());
	    // Create the resource indexer object
	    initializeIndexer();
	    // Resurect this server root entity:
	    initializeRootResource();
	    // Resurect the realms catalog
	    initializeRealmsCatalog() ;
	    // Initialize property sets
	    initializePropertySets();
	    // Create this server event manager 
	    initializeEventManager();
	    // Initialize the logger object:
	    initializeLogger();
	    // Initialize the shuffler object:
	    if ( shuffler_path != null ) {
		try {
		    this.shuffler = new Shuffler (shuffler_path) ;
		} catch (Error e) {
		    warning ("unable to launch shuffler to "+
			     shuffler_path+
			     ": " + e.getMessage()) ;
		    this.shuffler = null ;
		} catch (RuntimeException e) {
		    warning (e, "unable to launch shuffler to "+
			     shuffler_path+
			     ": " + e.getMessage()) ;
		    this.shuffler = null ;
		}
	    }
	    if ( this.shuffler != null )
		trace ("using shuffler at: " + shuffler_path) ;
	    // startup classes
	    loadStartupClasses();
	    // Yeah, now start:
	    this.thread.start();
	} else {
	    httpd mainServ = (httpd) shm.lookupServerHandler(masterID);
	    this.context = mainServ.getDefaultContext();
	    this.realms = mainServ.realms;
	    this.manager = mainServ.manager;
	    // We basically re-use:
	    // - the master indexer
	    // - the master realms catalog
	    // FIXED no need to use the same logger! - the master logger:
	    // Initialize the logger object:
	    initializeLogger();
	    // Resurect this server root entity:
	    initializeRootResource();
	    // We use our own event manager
	    initializeEventManager();
	    // Yeah, now start:
	    this.thread.start();
	}
    }

    /**
     * clone this server
     * @exception ServerHandlerInitException if unable to be initialized.
     */
    public ServerHandler clone(ServerHandlerManager shm
			       , String id
			       , ObservableProperties props) 
	throws ServerHandlerInitException
    {
	// Clone this master server:
	httpd server      = null;
	try {
	    server = (httpd) clone();
	} catch (CloneNotSupportedException ex) {
	    throw new ServerHandlerInitException(this.getClass().getName()
						 + ": clone not supported !");
	}
	server.shm = shm;
	// Nullify some of the cached instance variables:
	server.url = null;
	// Initialize 
	server.masterID = server.identifier;
	server.identifier = id;
	server.props      = props;
	server.props.registerObserver((PropertyMonitoring) server);
	server.initializeProperties();
	server.initializeServerSocket();
	server.isAClone = true;
	return server;
    }

    /**
     * get this server config resource
     */
    public ResourceReference getConfigResource() {
	if ( rr_configResource == null ) {
	    configResource = new ConfigResource(this);
	    rr_configResource = new DummyResourceReference(configResource);
	}
	return rr_configResource;
    }

    /**
     * Give the status of this class as a partial HTML text which will be added
     * into a block level element
     * @return a String, the generated HTML
     */
    public String getHTMLStatus() {
	StringBuffer sb = new StringBuffer();
	if (factory instanceof Status) {
	    sb.append(((Status)factory).getHTMLStatus());
	}
	sb.append(manager.getHTMLStatus());
	return sb.toString();
    }

    /**
     * Create a new server instance in this process.
     * @param identifier The server's identifier.
     * @param props The server properties.
     */
    public httpd() {
	super();
    }

    /**
     * this server's usage
     */
    public static void usage () {
	PrintStream o = System.out ;
	
	o.println("usage: httpd [OPTIONS]") ;
	o.println("-id <id>          : server identifier.");
	o.println("-port <number>    : listen on the given port number.");
	o.println("-host <host>      : full name of host running the server.");
	o.println("-root <directory> : root directory of server.") ;
	o.println("-space <directory>: space directory exported by server") ;
	o.println("-p     <propfile> : property file to read.");
	o.println("-trace            : turns debugging on.") ;
	o.println("-config           : config directory to use.") ;
	o.println("-maxstores <int>  : Max number of stores in memory.") ;
	System.exit (1) ;
    }

    /**
     * debugging main
     */
    public static void main (String args[]) {
	Integer cmdport    = null ;
	String  cmdhost    = null ;
	String  cmdroot    = null ;
	String  cmdspace   = null ;
	String  cmdprop    = null ;
	String  cmdid      = "http-server" ;
	String  cmdconfig  = "config";
	Boolean cmdtrace   = null ;
	boolean noupgrade  = false;
	String  maxstores  = null;

	// Parse command line options:
	for (int i = 0 ; i < args.length ; i++) {
	    if ( args[i].equals ("-port") ) {
		try {
		    cmdport = new Integer(args[++i]) ;
		} catch (NumberFormatException ex) {
		    System.out.println ("invalid port number ["+args[i]+"]");
		    System.exit (1) ;
		}
	    } else if ( args[i].equals("-id") && (i+1 < args.length)) {
		cmdid = args[++i];
	    } else if ( args[i].equals ("-maxstores") && (i+1 < args.length)) {
		maxstores = args[++i];
	    } else if ( args[i].equals ("-host") && (i+1 < args.length)) {
		cmdhost = args[++i] ;
	    } else if ( args[i].equals ("-root") && (i+1 < args.length)) {
		cmdroot = args[++i] ;
	    } else if ( args[i].equals ("-space") && (i+1 < args.length)) {
		cmdspace = args[++i] ;
	    } else if ( args[i].equals ("-p") && (i+1 < args.length)) {
		cmdprop = args[++i] ;
	    } else if ( args[i].equals ("-trace") ) {
		cmdtrace = Boolean.TRUE;
	    } else if ( args[i].equals ("?") || args[i].equals ("-help") ) {
		usage() ;
	    } else if (args[i].equals("-config") && (i+1 < args.length)) {
		cmdconfig = args[++i];
	    } else if ( args[i].equals("-noupgrade") ) {
		noupgrade = true;
	    } else {
		continue;
		// System.out.println ("unknown option: ["+args[i]+"]") ;
		// System.exit (1) ;
	    }
	}
	// Get the properties for this server:
	ObservableProperties props = null;
	props = new ObservableProperties(System.getProperties()) ;
	// Get the root and configuration directories:
	File root   = ((cmdroot == null)
		       ? new File(props.getProperty("user.dir", null))
		       : new File(cmdroot));
	File config = new File(root, cmdconfig);
	// Locate the property file:
	if (cmdprop == null) {
	    // Try to guess it, cause it is really required:
	    File guess = new File (config, cmdid+".props");
	    if ( ! guess.exists() )
		// A hack for smooth upgrade from 1.0alpha3 to greater:
		guess = new File(config, "httpd.props");
	    cmdprop = guess.getAbsolutePath() ;
	}
	if ( cmdprop != null ) {
	    System.out.println ("loading properties from: " + cmdprop) ;
	    try {
		File propfile = new File(cmdprop) ;
		props.load(new FileInputStream(propfile)) ;
		props.put (PROPS_P, propfile.getAbsolutePath()) ;
	    } catch (FileNotFoundException ex) {
		System.out.println ("Unable to load properties: "+cmdprop);
		System.out.println ("\t"+ex.getMessage()) ;
		System.exit (1) ;
	    } catch (IOException ex) {
		System.out.println ("Unable to load properties: "+cmdprop);
		System.out.println ("\t"+ex.getMessage()) ;
		System.exit (1) ;
	    }
	    System.setProperties (props) ;
	}
	// Check for an upgrade:
	int configvers = props.getInteger(httpd.VERSCOUNT_P, 1);
	if (configvers < httpd.verscount) {
	    System.err.println("+ Jigsaw needs upgrade from internal version "+
			       configvers+
			       " to " + httpd.verscount);
	    if ( noupgrade ) {
		System.err.println("+ Jigsaw cannot run in that version.");
		System.exit(1);
	    }
	    // upgrade(configvers, httpd.verscount, args);
	    return;
	}
	// Override properties with our command line options:
	if ( cmdport != null ) 
	    props.put (PORT_P, cmdport.toString()) ;
	if ( cmdhost != null ) 
	    props.put (HOST_P, cmdhost) ;
	if ( cmdroot != null )
	    props.put (ROOT_P, root.getAbsolutePath()) ;
	if ( cmdconfig != null )
	    props.put(CONFIG_P, config.getAbsolutePath());
	if ( cmdspace != null )
	    props.put (SPACE_P, cmdspace) ;
	if ( cmdtrace != null ) {
	    props.put (TRACE_P, "true") ;
	    props.put (CLIENT_DEBUG_P, "true") ;
	}
	if (maxstores != null) 
	    props.put (MAX_LOADED_STORE_P, maxstores);

	// Install security manager if needed:
	if (Boolean.getBoolean(USE_SM_P)) {
	    SecurityManager sm = new httpdSecurityManager() ;
	    System.setSecurityManager (sm) ;
	}
	// Run the server:
	try {
	    httpd server = new httpd ();
	    server.initialize(null, cmdid, props) ;
	} catch (Exception e) {
	    System.out.println ("*** [httpd]: fatal error, exiting !") ;
	    e.printStackTrace () ;
	}
    }
}
