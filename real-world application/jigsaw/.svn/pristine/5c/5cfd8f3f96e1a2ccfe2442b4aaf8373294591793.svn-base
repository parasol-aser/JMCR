// RuleParserException.java
// $Id: Rule.java,v 1.1 2010/06/15 12:28:32 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.proxy ;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.DataOutputStream;
import java.io.IOException;

import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpCredential;
import org.w3c.www.http.HttpEntityMessage;
import org.w3c.www.http.HttpFactory;
import org.w3c.www.http.HttpMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.tools.codec.Base64Encoder;

import org.w3c.tools.sorter.Comparable;

/**
 * The ForbidRule implements the <code>forbid</code> directive.
 * Forbid prevents all accesses, by the client (be it a proxy or hotjava)
 * to a given set of hosts.
 */

class ForbidRule extends Rule {

    /**
     * Forbid access to the given request.
     * @param request The request to apply the rule too.
     */

    public Reply apply(Request request) {
	Reply reply = request.makeReply(HTTP.OK);
	reply.setContent("<h1>Access forbidden</h1>"
			 + "<p>Access to "+request.getURL()
			 + " is forbidden by proxy dispatcher rules.");
	reply.setContentType(org.w3c.www.mime.MimeType.TEXT_HTML);
	return reply;
    }

    /**
     * Initialize a forbid rule.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize 
     * from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserExctpion If the rule couldn't be initialized
     * from given tokens.
     */

    public void initialize(String tokens[], int offset, int length) 
	throws RuleParserException
    {
	super.initialize(tokens, offset, length);
    }

   
    public ForbidRule() {
	name = "forbid";
    }

}

class ProxyRule extends Rule {
    URL proxy = null;

    /**
     * Convert a proxy rule to a String.
     * @return A String instance.
     */

    public String toString() {
	return host+" "+name+" "+proxy;
    }

    /**
     * Set the appropriate proxy for the given requested URL.
     * @param request The request to apply the rule to.
     * @return Always <strong>null</strong>, will only hack the given 
     * request
     * if needed.
     */

    public Reply apply(Request request) {
	if ( proxy != null )
	    request.setProxy(proxy);
	return null;
    }

    /**
     * Initialize that proxy rule.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize 
     * from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserExctpion If the rule couldn't be initialized
     * from given tokens.
     */

    public void initialize(String tokens[], int offset, int length) 
	throws RuleParserException
    {
	// We have to get the proxy here
	if ( offset+1 != length )
	    throw new RuleParserException("No target proxy.");
	try {
	    args  = tokens[offset];
	    proxy = new URL(args);
	} catch (MalformedURLException ex) {
	    throw new RuleParserException("Invalid target proxy \""
					  + tokens[offset]
					  + "\".");
	}
	host = tokens[0];

    }

    public ProxyRule() {
	name = "proxy";
    }

}

class RedirectRule extends Rule {
    URL redirect = null;

    /**
     * Convert a redirect rule to a String.
     * @return A String instance.
     */

    public String toString() {
	return host+" "+name+" "+redirect;
    }

    /**
     * Set the appropriate redirect URL for the given requested URL.
     * @param request The request to apply the rule to.
     * @return Always <strong>null</strong>, will only hack the given 
     * request
     * if needed.
     */

    public Reply apply(Request request) {
	if ( redirect != null )
	    request.setURL(redirect);
	return null;
    }

    /**
     * Initialize that redirect rule.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize
     * from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserExctpion If the rule couldn't be initialized
     * from given tokens.
     */

    public void initialize(String tokens[], int offset, int length) 
	throws RuleParserException
    {
	// We have to get the redirect URL here
	if ( offset+1 != length )
	    throw new RuleParserException("No target redirect URL.");
	try {
	    args  = tokens[offset];
	    redirect = new URL(args);
	} catch (MalformedURLException ex) {
	    throw new RuleParserException("Invalid target redirect URL \""
					  + tokens[offset]
					  + "\".");
	}
	host = tokens[0];
    }

    public RedirectRule() {
	name = "redirect";
    }

}

/**
 * The DirectRule implements the <code>DIRECT</code> directive.
 * Applying that rule is basically a <em>noop</em>.
 */

class DirectRule extends Rule {

    public DirectRule() {
	name = "direct";
    }

}

/**
 * The authorization rule adds Basic credentials to all requests.
 */

class AuthorizationRule extends Rule {
    /**
     * The credentials to add to the request.
     */
    HttpCredential credential = null;

    String user     = null;
    String password = null;

    public String toString() {
	return host+" "+name+" "+user+" "+password;
    }

    /**
     * Appky this rule to the given request.
     * @param request The request to apply the rule to.
     * @return Always <strong>null</strong>.
     */

    public Reply apply(Request request) {
	if ( ! request.hasHeader(HttpRequestMessage.H_AUTHORIZATION) )
	    request.setHeaderValue(HttpRequestMessage.H_AUTHORIZATION
				   , credential);
	return null;
    }

    /**
     * Initialize this Authorization rule.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize
     * from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserExctpion If the rule couldn't be initialized
     * from given tokens.
     */

    public void initialize(String tokens[], int offset, int length) 
	throws RuleParserException 
    {
	if ( offset + 2 != length ) 
	    throw new RuleParserException("Invalid authorization rule: "
					  + " should be authorization "
					  + " <user> <password>.");
	credential = HttpFactory.makeCredential("Basic");
	user = tokens[offset];
	password = tokens[offset+1];
	args = user+" "+password;
	Base64Encoder base64 = new Base64Encoder(user
						 + ":"
						 + password);
	credential.setAuthParameter("cookie", base64.processString());
	host = tokens[0];
    }

    public AuthorizationRule() {
	name = "authorization";
    }

}

class ProxyAuthRule extends Rule {
    URL proxy = null;
    /**
     * The credentials to add to the request.
     */
    HttpCredential credential = null;

    String user     = null;
    String password = null;

    /**
     * Convert a proxy rule to a String.
     * @return A String instance.
     */

    public String toString() {
	return host+" "+name+" "+user+" "+password+" "+proxy;
    }

    /**
     * Set the appropriate proxy for the given requested URL.
     * @param request The request to apply the rule to.
     * @return Always <strong>null</strong>, will only hack the given 
     * request
     * if needed.
     */

    public Reply apply(Request request) {
	if ( proxy != null )
	    request.setProxy(proxy);
	if ( ! request.hasHeader(HttpRequestMessage.H_PROXY_AUTHORIZATION) )
	    request.setProxyAuthorization(credential);
	return null;
    }

    /**
     * Initialize that proxy rule.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize 
     * from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserExctpion If the rule couldn't be initialized
     * from given tokens.
     */

    public void initialize(String tokens[], int offset, int length) 
	throws RuleParserException
    {
	// We have to get the proxy here
	if ( offset+3 != length )
	    throw new RuleParserException("Invalid proxyauth rule: "
					  + " should be authorization "
					  + " <user> <password> <proxy>.");
	try {
	    user = tokens[offset];
	    password = tokens[offset+1];
	    credential = HttpFactory.makeCredential("Basic");
	    Base64Encoder base64 = new Base64Encoder(user
						     + ":"
						     + password);
	    credential.setAuthParameter("cookie", base64.processString());
	    proxy = new URL(tokens[offset+2]);
	    args = user+" "+password+" "+proxy;
	} catch (MalformedURLException ex) {
	    throw new RuleParserException("Invalid target proxy \""
					  + tokens[offset]
					  + "\".");
	}
	host = tokens[0];
    }

    public ProxyAuthRule() {
	name = "proxyauth";
    }

}

public class Rule implements Comparable {

    protected static String names[] = {"direct", "forbid", "proxy", 
				       "redirect", "authorization",
				       "proxyauth"};

    String host = null;
    String args = null;
    String name = null;

    public String toString() {
	return host+" "+name;
    }

    public String getStringValue() {
	return toString();
    }

    public boolean greaterThan(Comparable comp) {
	return (getStringValue().compareTo(comp.getStringValue()) > 0);
    }

    public void writeRule(DataOutputStream out)
	throws IOException
    {
	out.writeBytes(toString()+"\n");
    }

    public String getHost() {
	return host;
    }

    public String getRuleName() {
	return name;
    }

    public String getRuleArgs() {
	return args;
    }

    /**
     * Initialize the rule with given set of tokens.
     * @param tokens The token array.
     * @param offset Offset within above array, of tokens to initialize from.
     * @param length Total number of tokens in above array.
     * @exception RuleParserException If the rule couldn't be initialized
     * from given tokens.
     */

    protected void initialize(String tokens[], int offset, int length) 
	throws RuleParserException
    {
	if ( offset != length )
	    throw new RuleParserException("Unexpected token: "+tokens[offset]);
	host = tokens[0];
    }

    /**
     * Create a rule, given an array of String.
     * @param tokens Parsed tokens, as a String array.
     * @param offset Offset of the rule tokens within above array.
     * @param length Total number of available tokens.
     * @exception RuleParserException If no rule could be created out of given
     * tokens.
     */

    public static Rule createRule(String tokens[], int offset, int length)
	throws RuleParserException
    {
	Rule rule = null;
	// Make sure there is something to build:
	if ((tokens == null) || (length-offset == 0))
	    return null;
	// Check the rule name:
	String name = tokens[offset];
	if ( name.equalsIgnoreCase("direct") ) {
	    rule = new DirectRule();
	} else if ( name.equalsIgnoreCase("proxy") ) {
	    rule = new ProxyRule();
	} else if ( name.equalsIgnoreCase("forbid") ) {
	    rule = new ForbidRule();
	} else if ( name.equalsIgnoreCase("redirect") ) {
	    rule = new RedirectRule();
	} else if ( name.equalsIgnoreCase("authorization") ) {
	    rule = new AuthorizationRule();
	} else if ( name.equalsIgnoreCase("proxyauth") ) {
	    rule = new ProxyAuthRule();
	} else {
	    throw new RuleParserException("Unknown rule name \""+name+"\"");
	}
	rule.initialize(tokens, offset+1, length);
	return rule;
    }

    public static String[] getRulesName() {
	return names;
    }

    /**
     * Apply given rule to the given request.
     * @param request The request to apply the rule to.
     */

    public Reply apply(Request request) {
	return null;
    }

    /**
     * Empty constructor for dynamic instantiation.
     */

    public Rule() {
    }

}
