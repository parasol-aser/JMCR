// ProxyDispatcher.java
// $Id: ProxyDispatcher.java,v 1.1 2010/06/15 12:28:31 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// please first read the full copyright statement in file COPYRIGHT.HTML

package org.w3c.www.protocol.http.proxy ;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.net.URL;
import java.net.URLConnection;

import org.w3c.util.ObservableProperties;
import org.w3c.util.PropertyMonitoring;

import org.w3c.www.protocol.http.HttpException;
import org.w3c.www.protocol.http.HttpManager;
import org.w3c.www.protocol.http.PropRequestFilter;
import org.w3c.www.protocol.http.Reply;
import org.w3c.www.protocol.http.Request;

import org.w3c.www.http.HttpRequestMessage;

/**
 * The proxy dispatcher applies some <em>rules</em> to a request.
 * The goal of that filter is to allow special pre-processing of requests
 * based, on their target host, before sending them off the net.
 * <p>The filter is configured through a <em>rule file</em> whose format
 * is described by the following BNF:
 * <code>
 * rule-file=(<em>record</em>)*<br>
 * record=<strong>EOL</strong>|<em>comment</em>|<em>rule</em><br>
 * comment=<strong>#</strong>(<strong>^EOL</strong>)*<strong>EOL</strong><br>
 * rule=<em>rule-lhs</em>(<strong>SPACE</strong>)*<em>rule-rhs</em><br>
 * rule-lhs=(<strong>token</strong>)
 *  |(<strong>token</strong> (<strong>.</strong> <strog>token</strong>)*<br>
 * rule-lhr=<em>forbid</em>|<em>direct</em>|<em>redirect</em>
 *          |<em>proxy</em>|<em>authorization</em>|<em>proxyauth</em><br>
 * forbid=<strong>FORBID</strong>|<strong>forbid</strong><br>
 * direct=<strong>DIRECT</strong>|<strong>direct</strong><br>
 * redirect=(<strong>REDIRECT</strong>|<strong>proxy</strong>) <em>url</em><br>
 * proxy=(<strong>PROXY</strong>|<strong>proxy</strong>) <em>url</em><br>
 * url=<strong>any valid URL</strong></br>
 * authorization=(<strong>AUTHORIZATION</strong>|<strong>authorization</strong>
 *  <em>user</em> <em>password</em><br>
 * proxyauth=(<strong>PROXYAUTH</strong>|<strong>proxyauth</strong>
 *  <em>user</em> <em>password</em> <em>url</em><br>
 * </code>
 * <p>A sample rule file looks like this:
 * <code>
 * # Some comments
 * 
 * edu proxy http://class.w3.org:8001/
 * org proxy http://class.w3.org:8001/
 * fr  direct
 * www.evilsite.com redirect http://www.goodsite.com/warning.html
 * www.w3.org direct
 * 138.96.24 direct
 * www.playboy.com forbid
 * default proxy http://cache.inria.fr:8080/
 * </code>
 * <p>The algorithm used to lookup rules is the following: 
 * <ul>
 * <li>Split all rules <em>left hand side</em> into its components, eg 
 * H1.H2.H3 is splitted into { H1, H2, H3 }, then reverse the components and 
 * map that to the rule. In our example above, { org, w3, www} would be mapped
 * to <em>direct</em>.
 * <li>Split the fully qualified host name into its components, eg, A.B.C is
 * splitted into { A, B, C } and reverse it.
 * <li>Find the longest match in the mapping table of rules, and get
 * apply the given rule.
 * </ul>
 * <p>In our example, a request to <strong>www.isi.edu</strong> would match
 * the <em>edu</em> rule, and a request for <strong>www.w3.org</strong>
 * would match the <em>direct</em> rule, for example.
 * <p>Three rules are defined:
 * <dl>
 * <dt>direct<dd>Run that request directly against the target host.
 * <dt>forbid<dd>Emit a forbid message, indicating that the user is not
 * allowed to contact this host.
 * <dt>proxy<dd>Run that request through the given <em>proxy</em>.
 * <dt>proxyauth<dd>Run that request through a proxy with the right proxy
 * credentials.
 * </dl>
 * <p>For numeric IP addresses, the most significant part is the beginning,
 * so {A, B, C} are deducted directly. In the example { 138, 96, 24 } is mapped
 * to direct.
 * <p>If no rules are applied, then the default rule (root rule) is applied.
 * See the example.
 */

public class ProxyDispatcher
    implements PropRequestFilter, PropertyMonitoring 
{
    /**
     * Name of the property giving the rule file URL.
     */
    public static final 
    String RULE_P = "org.w3c.www.protocol.http.proxy.rules";

    /**
     * Name of the property turning that filter in debug mode.
     */
    public static final
    String DEBUG_P = "org.w3c.www.protocol.http.proxy.debug";

    /**
     * Name of the property turning that filter in debug mode.
     */
    public static final
	String CHECK_RULES_LAST_MODIFIED_P = 
	"org.w3c.www.protocol.http.proxy.rules.check.lastmodified";

    /**
     * The properties we initialized ourself from.
     */
    protected ObservableProperties props = null;  
    /**
     * The current set of rules to apply.
     */
    protected RuleNode rules = null;
    /**
     * Are we in debug mode ?
     */
    protected boolean debug = false;

    protected boolean check_rules = false;

    protected static final String disabled = "disabled";

    protected long lastParsingTime = -1;

    /**
     * Parse the given input stream as a rule file.
     * @param in The input stream to parse.
     * @exception IOException if an IO error occurs.
     * @exception RuleParserException if parsing failed.
     */

    protected void parseRules(InputStream in)
	throws IOException, RuleParserException
    {
	RuleParser parser = new RuleParser(in);
	RuleNode   nroot  = parser.parse();
	rules = nroot;
	lastParsingTime = System.currentTimeMillis();
    }

    /**
     * Parse the default set of rules.
     * <p>IOf the rules cannot be parsed, the filter emits an error
     * message to standard error, and turn itself into transparent mode.
     */

    protected void parseRules() {
	if ( debug )
	    System.out.println("PARSING RULES...");
	String ruleurl = props.getString(RULE_P, null);
	InputStream in = null;
	// Try opening the rule file as a URL:
	try {
	    URL url = new URL(ruleurl);
	    in = url.openStream();
	} catch (Exception ex) {
	// If this fails, it may be just a file name:
	    try {
		in = (new BufferedInputStream
		      (new FileInputStream
		       (new File(ruleurl))));
	    } catch (Exception nex) {
		System.err.println("* ProxyDispatcher: unable to open rule "
				   + "file \"" + ruleurl + "\"");
		rules = null;
		return;
	    }
	} 
	// Parse that input stream as a rule file:
	try {
	    parseRules(in);
	} catch (Exception ex) {
	    System.err.println("Error parsing rules from: "+ruleurl);
	    ex.printStackTrace();
	    rules = null;	    
	} finally {
	    if ( in != null ) {
		try {
		    in.close();
		} catch (IOException ex) {
		}
	    }
	}
	if ( debug )
	    System.out.println("DONE.");
    }

    protected boolean needsParsing() {
	if (rules == null) 
	    return true;
	if (! check_rules)
	    return false;
	long rulesStamp = -1;
	String ruleurl  = props.getString(RULE_P, null);
	try {
	    URL url = new URL(ruleurl);
	    if (url.getProtocol().equalsIgnoreCase("file")) {
		File file = new File(url.getFile());
		rulesStamp = file.lastModified();
	    } else {
		URLConnection con = url.openConnection();
		rulesStamp = con.getLastModified();
	    }
	} catch (Exception ex) {
	    File file = new File(ruleurl);
	    rulesStamp = file.lastModified();
	}
	System.out.println("rulesStamp : "+rulesStamp);
	return (lastParsingTime < rulesStamp);
    }

    /**
     * Filter requests before they are emitted.
     * Look for a matching rule, and if found apply it before continuing
     * the process. If a forbid rule was apply, this method will return
     * with a <em>forbidden</em> message.
     * @param request The request to filter.
     * @return A Reply instance, if processing is not to be continued,
     * <strong>false</strong>otherwise.
     */

    public Reply ingoingFilter(Request request) {
	if (needsParsing())
	    parseRules();
	if ( rules != null ) {
	    URL    url  = request.getURL();
	    String host = url.getHost();
	    Rule   rule = rules.lookupRule(host);
	    if ( rule != null ) {
		if ( debug ) {
		    String args = rule.getRuleArgs();
		    if (args == null) {
			args = "";
		    } else {
			args = " "+args;
		    }
		    System.out.println("["+ getClass().getName() +
				       "]: applying rule <"+rule.getRuleName()+
				       args +"> to " + request.getURL());
		}
		return rule.apply(request);
	    }
	}
	return null;
    }

    /**
     * Filter requests when an error occurs during the process.
     * This filter tries to do a direct connection if it is needed
     * @param reques The request to filter.
     * @param reply It's associated reply.
     * @return Always <strong>null</strong>.
     */

    public boolean exceptionFilter(Request request, HttpException ex) {
	// if it was a proxy connection, try a direct one
	// add test for exception here
	if(request.hasProxy()) {
	    Reply reply       = null;
	    HttpManager hm    = HttpManager.getManager();
	    request.setProxy(null);
	    if ( debug )
		System.out.println("["+getClass().getName()+"]: direct fetch "
				   +"for " + request.getURL());
	    return true;
	}
	return false;
    }

    /**
     * Filter requests after processing.
     * This filter doesn't do any post-processing.
     * @param reques The request to filter.
     * @param reply It's associated reply.
     * @return Always <strong>null</strong>.
     */

    public Reply outgoingFilter(Request request, Reply reply) {
	return null;
    }

   /**
     * PropertyMonitoring implementation - Commit property changes.
     * @param name The name of the property that has changed.
     * @return A boolean <strong>true</strong> if change was commited, 
     * <strong>false</strong> otherwise.
     */

    public boolean propertyChanged(String name) {
	if(name.equals(RULE_P)) {
	    try {
		parseRules();
	    } catch (Exception ex) {
		ex.printStackTrace();
		return false;
	    }
	} else if (name.equals(DEBUG_P)) {
	    debug = props.getBoolean(DEBUG_P, false);
	} else if (name.equals(CHECK_RULES_LAST_MODIFIED_P)) {
	    check_rules = props.getBoolean(CHECK_RULES_LAST_MODIFIED_P, false);
	}
	return true;
    }

    public void initialize(HttpManager manager) {
	// Prepare empty entry list:
	props = manager.getProperties();
	props.registerObserver(this);
	// Initialize from properties:
	parseRules();
	if (debug = props.getBoolean(DEBUG_P, false)) 
	    System.out.println("["+getClass().getName()+": debuging on.");
	check_rules = props.getBoolean(CHECK_RULES_LAST_MODIFIED_P, false);
	// Install ourself
	manager.setFilter(this);
    }

    /**
     * We don't maintain cached infos.
     */

    public void sync() {
    }

    /**
     * Empty constructor, for dynamic instantiation.
     */

    public ProxyDispatcher() {
	super();
    }
}
