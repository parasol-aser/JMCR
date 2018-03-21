// Unix.java
// //$Id: Unix.java,v 1.1 2010/06/15 12:25:38 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

// modified by gisburn (Roland.Mainz@informatik.med.uni-giessen.de)
// to work under java 2 (implementing JNI 1.1)

package org.w3c.util;

/**
 * Native methods to do some UNIX specific system calls.
 * This class can be used on UNIX variants to access some specific system
 * calls.
 */

public class Unix  {
    private static final String  NATIVE_LIBRARY = "Unix";
    /**
     * Are the calls we support really availables ?
     */
    private static boolean haslibrary = false;
    private static Unix    that       = null;

    private native int     libunix_getUID(String user);
    private native int     libunix_getGID(String group);
    private native boolean libunix_setUID(int    uid);
    private native boolean libunix_setGID(int    gid);
    private native boolean libunix_chRoot(String root);

    /**
     * Get the UNIX system call manger.
     * @return An instance of this class, suitable to call UNIX system
     * calls.
     */

    public static synchronized Unix getUnix() {
        if( that == null )  {
	    // Load the library:
	    try  {
		System.loadLibrary( NATIVE_LIBRARY );
		haslibrary = true;
	    } catch( UnsatisfiedLinkError ex ) {
		haslibrary = false;
	    } catch( RuntimeException ex ) {
		haslibrary = false;
	    }
	    // Create the only instance:
	    that = new Unix();
        }
        return( that );
    }

    /**
     * Can I perform UNIX system calls through that instance ?
     * @return A boolean, <strong>true</strong> if these system calls are
     * allowed, <strong>false</strong> otherwise.
     */

    public boolean isResolved() {
        return( haslibrary );
    }

    /**
     * Get the user identifier for that user.
     * @return The user's identifier, or <strong>-1</strong> if user was not
     * found.
     */

    public int getUID( String uname ) {
        // FIXME: Security check needed here
        if( uname == null )
	    return( -1 );
	return( libunix_getUID( uname ) );
    }

    /**
     * Get the group identifier for that group.
     * @return The group identifier, or <strong>-1</strong> if not found.
     */

    public int getGID( String gname ) {
        // FIXME: Security check needed
        if( gname == null )
	    return( -1 );
	return( libunix_getGID( gname ) );
    }

    /**
     * Set the user id for the running process.
     * @param uid The new user identifier for the process.
     * @exception UnixException If failed.
     */

    public void setUID( int uid ) 
	throws UnixException
    {
        // FIXME: Security check needed
        if( !libunix_setUID( uid ) )
	    throw new UnixException( "setuid failed" );
    }

    /**
     * Set the group id for the running process.
     * @param gid The new user identifier for the process.
     * @exception UnixException If failed.
     */

    public void setGID( int gid ) 
	throws UnixException
    {
        // FIXME: Security check needed
        if( !libunix_setGID( gid ) )
	    throw new UnixException( "setgid failed" );
    }

    /**
     * Change the process root, using <code>chroot</code> system call.
     * @param root The new root for the process.
     * @exception UnixException If failed.
     */

    public void chroot( String root ) 
	throws UnixException
    {
        if( root == null )
	    throw new NullPointerException( "chroot: root == null" );
	
	if( !libunix_chRoot( root ) )
	    throw new UnixException( "chroot failed" );
    }
}
