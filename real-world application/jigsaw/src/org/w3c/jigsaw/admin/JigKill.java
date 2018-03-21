// JigKill.java
// $Id: JigKill.java,v 1.1 2010/06/15 12:24:26 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigsaw.admin;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Vector;

import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpFactory;

import org.w3c.tools.codec.Base64Encoder;

/**
 * Simple class used to save and/or kill Jigsaw.
 *
 * <DL>
 * <DT><B>Options:</B>
 * <DD><CODE>-u username</CODE>  User name (defaults to "admin")
 * <DD><CODE>-p password</CODE>  Password (required)
 * <DD><CODE>--username username</CODE>  Same as <CODE>-u</CODE>
 * <DD><CODE>--password password</CODE>  Same as <CODE>-p</CODE>
 * <DD><CODE>--save</CODE>  Save configuration of all servers</DD>
 * <DD><CODE>--stop</CODE>  Stop all servers</DD>
 * <DD><CODE>--ping</CODE>  check if servers are reachable</DL>
 * (The last argument is assumed to be the URL to the Admin server, 
 * see Usage examples below...)
 * <P>
 * <DL>
 * <DT><B>Example usage:</B>
 * <DD>(be sure that jigsaw's <CODE>*.jar</CODE> file is in CLASSPATH, 
 * e.g. <CODE>export CLASSPATH=~/Jigsaw/classes/jigsaw.zip:
 * ${CLASSPATH}</CODE>)</DD>
 * <DL><DT>- Save and exit the server 
 * http://gyros.informatik.med.uni-giessen.de:
 *   <DD><CODE>java org.w3c.jigsaw.admin.JigKill -u admin -p for#8pj 
 * http://gyros.informatik.med.uni-giessen.de:8009/</CODE></DD>
 *
 *   <DD>OR (using explicit options)</DD>
 *
 *   <DD><CODE>java org.w3c.jigsaw.admin.JigKill -u admin -p for#8pj 
 * --save --stop http://gyros.informatik.med.uni-giessen.de:8009/</CODE></DL> 
 *
 * <DL><DT>- Stop a server (as fast as possible, e.g. for system shutdown):
 *   <DD><CODE>java org.w3c.jigsaw.admin.JigKill -u admin -p for#8pj 
 * --stop http://gyros.informatik.med.uni-giessen.de:8009/</CODE></DL>
 *
 * <DL><DT>- Save the servers current configuration:
 *   <DD><CODE>java org.w3c.jigsaw.admin.JigKill -u admin -p for#8pj 
 * --save http://gyros.informatik.med.uni-giessen.de:8009/</CODE></DL>
 *
 * <DL><DT>- Check if a server is still alive:
 *   <DD><CODE>java org.w3c.jigsaw.admin.JigKill -u admin -p for#8pj 
 * --ping http://gyros.informatik.med.uni-giessen.de:8009/</CODE></DL></DL>
 * <P>
 * <DL>
 * <DT><B>Changes from V1.1 to 1.2:</B>
 * <DD>- Added <CODE>--stop</CODE> option: Simply stop jigsaw quick&savely.
 * </DD>
 * <DD>- Added <CODE>--save</CODE> option: Snapshot the current config 
 * (something for a daily crontab job).</DD>
 * <DD>- Added <CODE>--ping</CODE> option to see if the server is still 
 * running (usefull for scripts).</DD>
 * <DD>- Added <CODE>--username</CODE> and <CODE>--password</CODE> options 
 * as aliases for <CODE>-u</CODE> and <CODE>-p</CODE>.</DD></DL>
 * 
 * <DD>- JigKill now returns 0 for success and non-0 for failure 
 * (of the requested operation) EVERYTIMES 
 *   (except the JVM cores) to make script-processing as much as easy.</DD>
 * <DD>- <CODE>-u</CODE> option now defaults to "admin", as jigsaw's 
 * default config does.</DD>
 * <DD>- JigKill now saves all servers first before stopping them all 
 * (first backup, then destroy...).</DD></DL>
 * <P>
 * <DL>
 * <DT><B>ToDo:</B>
 * <DD>- Adding a <CODE>--restart</CODE> option.</DD></DL>
 *
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 * @author  Roland Mainz (Roland.Mainz@informatik.med.uni-giessen.de)
 */
public class JigKill {
    // the following variables are equivalents to ANSI-C's <stdlib.h> defines
    /**
     * Exit value of the JVM on success of the requested operation.
     */
    final protected static int EXIT_SUCCESS = 0;  
    // JigKill has done it's operation successfull
    /**
     * Exit value of the JVM on failure of the requested operation.
     */    
    final protected static int EXIT_FAILURE = 1;  
    // JigKill failed to do it's work

   
    /* object instance variables */  
    protected RemoteResource adminServer = null;
    protected RemoteResource ctrls[]     = null;

    /**
     * Print usage description and exit with exit status 
     * {@link #EXIT_FAILURE EXIT_FAILURE}
     * @see #main(String[])
     */
    protected static void usage() {
        System.err.println( "Usage:\n" +
                            "\tjava org.w3c.jigsaw.admin.JigKill "+
			    "-u <username> -p <password> [--save] "+
			    "[--stop] [--ping] <admin server url>\n" );
        System.exit( EXIT_FAILURE );
    }

    /**
     * Get all the server's ControlResource.
     * @param admin the admin server (a RemoteResource)
     * @return an array of RemoteResource
     * @exception RemoteAccessException if any remote error occurs.     
     */
    protected RemoteResource[] getControls( RemoteResource admin ) 
	throws RemoteAccessException
    {
        String names[]   = admin.enumerateResourceIdentifiers();
        Vector vcontrols = new Vector( 2 );

        for( int i = 0 ; i < names.length ; i++ ) 
	    {
		if( (! names[ i ].equals( "control" )) && 
		    (! names[ i ].equals( "realms"  )) ) 
		    {
			RemoteResource srr = admin.loadResource( names[ i ] );

			// load the control node
			RemoteResource control = srr.loadResource( "control" );
			vcontrols.addElement( control );
		    }
	    }

        RemoteResource controls[] = new RemoteResource[ vcontrols.size() ];
        vcontrols.copyInto( controls );

        return controls;
    }

    /**
     * Save the configuration of the servers and the admin server 
     * and then kill all servers (including the admin server).
     * @exception RemoteAccessException if any remote error occurs.
     * @see #save()
     * @see #stop()
     */
    public void kill() 
	throws RemoteAccessException
    {
        // first save all servers...
        save();

        // ...then stop/kill them all
        stop();
    }

    /**
     * Stop the servers and the admin server.
     * @exception RemoteAccessException if any remote error occurs.
     * @see #doLoadResource(String)
     * @see #kill()
     * @since JigKill 1.2
     */
    public void stop() 
	throws RemoteAccessException
    {
        // stop all servers
        doLoadResource( "stop" );
    }

   
    /**
     * Save the configuration of the servers and the admin server.
     * @exception RemoteAccessException if any remote error occurs.
     * @see #doLoadResource(String)
     * @see #kill()
     * @since JigKill 1.2
     */
    public void save() 
	throws RemoteAccessException
    {
        // save all servers
        doLoadResource( "save" );
    }        

   
    /**
     * Send a "command" to all servers and the admin server.
     * @param cmd name of the resource to load.
     * @exception RemoteAccessException if any remote error occurs.
     * @see #save()
     * @see #stop()
     * @since JigKill 1.2
     */    
    protected void doLoadResource( String cmd ) 
	throws RemoteAccessException
    {                
        // servers
        for( int i = 0 ; i < ctrls.length ; i++ ) {
	    ctrls[ i ].loadResource( cmd );
	}

        // admin servers
        RemoteResource ctrl = adminServer.loadResource( "control" );
        ctrl.loadResource( cmd );

        //done
    }

    /**
     * Contructor.
     * @param adminURL the admin server URL
     * @param username the username
     * @param password the password
     */
    public JigKill( URL adminURL, String username, String password ) 
	throws RemoteAccessException
    {
        AdminContext   ctxt       = new AdminContext( adminURL );
        HttpCredential credential = HttpFactory.makeCredential( "Basic" );
        Base64Encoder  encoder    = new Base64Encoder( username + ":" + 
						       password );

        credential.setAuthParameter( "cookie", encoder.processString() );
        ctxt.setCredential( credential );
        ctxt.initialize();        

        adminServer = ctxt.getAdminResource();
        ctrls       = getControls( adminServer );
    }

    /**
     * Main program entry.
     * This method exits the JVM with either 
     * {@link #EXIT_SUCCESS EXIT_SUCCESS} or 
     * {@link #EXIT_FAILURE EXIT_FAILURE} for the requested operation. 
     * @param args program arguments
     * @see #usage()
     */     
    public static void main( String args[] ) {
        // options
        String  username   = "admin";
        String  password   = null;
        String  url        = null;
        boolean doSave     = false;
        boolean doStop     = false; 
        boolean doPing     = false;

        boolean doKill     = true;  
	// not an option, but a flag: 
	// If none of the --save, --stop, --ping switches are given,
	// kill the server (e.g. set --save, --stop options)

        try {
	    for( int i = 0 ; i < args.length ; i++ ) 
		{
		    if (args[i].equals("-u") || args[i].equals("--username"))  
			username = args[ ++i ];             
		    else if (args[i].equals("-p") || 
			     args[i].equals("--password"))
			password = args[ ++i ];             
		    else if (args[i].equals("--save")) { 
			doSave = true;  
			doKill = false; 
		    } else if (args[i].equals("--stop")) { 
			doStop = true;
			doKill = false; 
		    } else if (args[i].equals("--ping")) { 
			doPing = true;
			doKill = false; 
		    } else {
			url = args[ i ];
		    }
		}        
        } catch( ArrayIndexOutOfBoundsException exc ) {
	    // ignore this...
        }

        // check for required arguments... 
        if( (username == null) || (password == null) || (url == null) )
	    usage();

        // the default action is (e.g. none of the --save, 
	// --stop, --ping option set is to KILL)
        if( doKill ) {
	    doSave = doStop = true;
	}

        // process URL
        URL adminURL = null;
        try {
	    adminURL = new URL( url );
	} catch( MalformedURLException ex )  {
	    System.err.println( "Invalid URL : " + url );
	    System.exit( EXIT_FAILURE );
	}

        // and now: EXECUTE !
        try {
	    JigKill jigk = new JigKill( adminURL, username, password );

	    if( doPing ) {
		System.out.println( "Servers are alive." );
	    } 
	    
	    if( doSave ) {
		jigk.save();
		System.out.println( "Servers saved." );
	    }

	    if( doStop ) {
		jigk.stop();
		System.out.println( "Servers killed." );
	    }  

	    // success :-)
	    System.exit( EXIT_SUCCESS );
	} catch( RemoteAccessException ex ) {
	    // catch RemoteAccessException (problems lile wrong login, 
	    //server not found etc.)
	    String msg = ex.getMessage();
	    
	    if( msg.equals( "Unauthorized" ) )
		System.err.println( "Invalid username/password." );
	    else
		System.err.println( "Error : " + msg );

	    System.exit( EXIT_FAILURE );  
	} catch( Exception exc ) {
	    // catch ANY other exception which may occur... 
	    System.err.println( "Unexcepted fatal error:" );
	    exc.printStackTrace();
	}

        // for all cases (except the only one above) return 
	// failure for usage by the underlying scripts
        System.exit( EXIT_FAILURE );
    }
}


