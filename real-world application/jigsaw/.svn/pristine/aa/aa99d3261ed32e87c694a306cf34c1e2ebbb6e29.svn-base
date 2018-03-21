// PasswordEditorFrame.java
// $Id: PasswordEditorFrame.java,v 1.2 2010/06/15 17:53:05 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.jigsaw.frames.HTTPFrame;
import org.w3c.jigsaw.frames.PostableFrame;

import org.w3c.jigsaw.auth.AuthRealm;
import org.w3c.jigsaw.auth.AuthUser;
import org.w3c.jigsaw.auth.RealmsCatalog;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.jigsaw.forms.URLDecoder;

public class PasswordEditorFrame extends PostableFrame {

    /**
     * Attribute index - The name of the realm to edit.
     */
    protected static int ATTR_REALM = -1;

    static {
	Class     c = null;
	Attribute a = null;

	try {
	    c = Class.forName("org.w3c.jigsaw.resources.PasswordEditorFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the name of the realm to edit
	a = new StringAttribute("realm"
				, null
				, Attribute.EDITABLE);
	ATTR_REALM = AttributeRegistry.registerAttribute(c, a);
    }

    /**
     * The loaded realm, when loaded.
     */
    ResourceReference rr_realm = null;

    /**
     * Get the name of the realm to edit.
     * @return The name of the realm to edit, as a String.
     */

    public String getRealm() {
	return getString(ATTR_REALM, null);
    }

    protected synchronized boolean changePassword(String username,
						  String oldpassword,
						  String newpassword) 
    {
	// Get a handle on the authentication realm:
	if ( rr_realm == null ) {
	    // Load the realm from the auth realm catalog:
	    RealmsCatalog c = ((httpd)getServer()).getRealmsCatalog();
	    String        r = getRealm();
	    if ( r == null ) {
		getServer().errlog(this, "attribute realm no initialized.");
		return false;
	    }
	    // Really, load the store now:

	    rr_realm = c.loadRealm(r);
	}
	// If we did get the realm:
	if ( rr_realm != null ) {
	    try {
		AuthRealm realm = (AuthRealm) rr_realm.lock();
		// Get the user:
		ResourceReference rr_user = realm.loadUser(username);
		if (rr_user == null)
		    return false;
		try {
		    AuthUser user = (AuthUser)rr_user.lock();
		    // Check the old password first:
		    String passwd = user.getPassword();
		    if ((passwd == null) || ! passwd.equals(oldpassword))
			return false;
		    // Set the new password:
		    user.setPassword(newpassword);
		    return true;
		} catch (InvalidResourceException ex) {
		    return false;
		} finally {
		    rr_user.unlock();
		}
	    } catch (InvalidResourceException ex) {
		return false;
	    } finally {
		rr_realm.unlock();
	    }
	}
	return false;
    }

    protected HtmlGenerator generateForm(String msg) {
	// Create the HTML and set title:
	HtmlGenerator g = new HtmlGenerator("Password editor for "+getRealm());
	// Add style link
	addStyleSheet(g);
	g.append("<h1>Password editor for "
		 , getRealm()
		 , "</h1>");
	// If some message is available, dump it:
	if ( msg != null ) 
	    g.append("<hr>", msg, "</hr>");
	// And then display the form:
	g.append("<form method=\"POST\" action=\"",
		 getURLPath(),
		 "\">");
	g.append("<table width=\"100%\">");
	g.append("<tr><th align=right>username");
	g.append("<th align=left><input type=\"text\" name=\"username\">");
	g.append("<tr><th align=right>old password");
	g.append("<th align=left><input type=\"password\" name=\"opasswd\">");
	g.append("<tr><th align=right>new password");
	g.append("<th align=left><input type=\"password\" name=\"npasswd\">");
	g.append("<tr><th align=right>confirm");
	g.append("<th align=left><input type=\"password\" name=\"cpasswd\">");
	g.append("</table>");
	g.append("<input type=\"submit\" value=\"Change\">");
	g.append("</form>");
	return g;
    }

    protected final HtmlGenerator generateForm() {
	return generateForm(null);
    }

    /**
     * Handle a get request on the password editor.
     * Dump a form suitable for editing a user entry.
     * @param request The request to handle.
     * @exception ProtocolException If processing the request failed.
     * @exception ResourceException If this resource got a fatal error.
     * @return An HTTP Reply instance.
     */

    public Reply get(Request request) 
	throws ProtocolException, ResourceException
    {
	Reply reply = createDefaultReply(request, HTTP.OK);
	reply.setStream(generateForm());
	return reply;
    }

    /**
     * Handle a post request.
     * Do change the password, when possible.
     * @param request The request to handle.
     * @param data The form decoded data.
     * @exception ProtocolException If processing the request failed.
     * @return An HTTP Reply instance.
     */

    public Reply handle(Request request, URLDecoder data) 
	throws ProtocolException 
    {
	String username = data.getValue("username");
	String opasswd  = data.getValue("opasswd");
	String npasswd  = data.getValue("npasswd");
	String cpasswd  = data.getValue("cpasswd");
	HtmlGenerator g = null;
	if ((username == null)
	    || (opasswd == null)
	    || (npasswd == null)
	    || (cpasswd == null)) {
	    // Check that all values are available:
	    if (username == null)
		g = generateForm("Fill in <em>all</em> the fields.");
	    else
		g = generateForm("Hey, "+username+", could you feel in "
				 + "<em>all</em> the fields please.");
	} else 	if ( ! npasswd.equals(cpasswd) ) {
	    // Check that new and confirmed password are the same:
	    g = generateForm("New and confirmed password don't "
			     + " match, try again "
			     + ((username == null) ? "." : (username+".")));
	} else if ( changePassword(username, opasswd, npasswd) ) {
	    // Run the change:
	    g = new HtmlGenerator("Password now changed.");
	    // Add style link
	    addStyleSheet(g);
	    g.append("<h1>Your password has been changed</h1>");
	    g.append("<p>Operation succeeded, have fun !");
	} else {
	    // Changing the password failed, don't provide explanations:
	    g = new HtmlGenerator("Password change failed");
	    // Add style link
	    addStyleSheet(g);
	    g.append("<h1>Changing the password failed</h1>");
	    g.append("You were not allowed to change the password for user \""
		     , username
		     , "\".");
	}
	// We always succeed, that's cool:
	Reply reply = createDefaultReply(request, HTTP.OK);
	reply.setStream(g);
	return reply;
    }

}
