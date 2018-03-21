// RegexRedirectFilter.java
// $Id: RegexRedirectFilter.java,v 1.2 2010/06/15 17:52:55 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.filters;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.ReplyInterface;
import org.w3c.tools.resources.RequestInterface;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceFilter;
import org.w3c.tools.resources.ResourceFrame;
import org.w3c.tools.resources.StringArrayAttribute;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.w3c.jigsaw.html.HtmlGenerator;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpReplyMessage;
import org.w3c.www.http.HttpRequestMessage;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

public class RegexRedirectFilter extends ResourceFilter {
    /*
     * Attribute index - the URL rewriting patterns
     */
    protected static int ATTR_PATTERNS = -1;

    static {
	Class     c = null;
	Attribute a = null;
	try {
	    c = Class.forName("org.w3c.jigsaw.filters.RegexRedirectFilter");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	// Register the PATTERNS attribute:
	a = new StringArrayAttribute("patterns"
				     , null
				     , Attribute.EDITABLE);
	ATTR_PATTERNS = AttributeRegistry.registerAttribute(c, a);
    }

    Pattern patterns[]      = null;
    String  substitutions[] = null;
    Pattern dec_pat         = null;

    /**
     * Catch the regexp to reset the precomputed patterns
     * @param idx The attribute being set.
     * @param val The new attribute value.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if ( idx == ATTR_PATTERNS ) {
	    synchronized (this) {
		patterns = null;;
	    }
	}
    }

    private Pattern[] getPatterns() {
	if (patterns != null)
	    return patterns;
	String raw_pat[] = (String[]) getValue(ATTR_PATTERNS, null);
	if (raw_pat == null)
	    return null;
	patterns = new Pattern[raw_pat.length];
	substitutions = new String[raw_pat.length];
	int realsize = 0;

	PatternCompiler compiler = new Perl5Compiler();
	PatternMatcher matcher = new Perl5Matcher();
	
	for (int i=0; i<raw_pat.length; i++) {
	    try {
		if (matcher.matches(raw_pat[i], dec_pat)) {
		    patterns[realsize] = 
			compiler.compile(matcher.getMatch().group(1));
		    substitutions[realsize] = matcher.getMatch().group(2);
		    realsize++;
		}
	    } catch (MalformedPatternException ex) {
		// bad configuration...
	    }
	}
	if (realsize != raw_pat.length) {
	    // trim that the hard way :)
	    Pattern t_patterns[] = new Pattern[realsize];
	    String t_substitutions[] = new String[realsize];
	    System.arraycopy(patterns, 0, t_patterns, 0, realsize);
	    System.arraycopy(substitutions, 0, t_substitutions, 0, realsize);
	    patterns = t_patterns;
	    substitutions = t_substitutions;
	}
	return patterns;
    }

    private String[] getSubstitutions() {
	if (substitutions != null)
	    return substitutions;
	getPatterns();
	return substitutions;
    }

    /**
     * The right syntax for rules is regexp$   substitution
     * like Apache redirecter rules
     */

    public void initialize(Object values[]) {
	super.initialize(values);
	PatternCompiler compiler = new Perl5Compiler();
	try {
	    dec_pat = compiler.compile("(.*)\\$\\s+(.*)");
	} catch (MalformedPatternException ex) {
	    ex.printStackTrace();
		// should never happen
	}
    }

    public ReplyInterface ingoingFilter(RequestInterface request) {
	Request req = (Request) request;
	String  requrl = req.getURL().toExternalForm();
	PatternMatcher matcher = new Perl5Matcher();
	Pattern pat[] = getPatterns();
	if (pat == null || pat.length == 0)
	    return null;
	String sub[] = getSubstitutions();
	String result = null;
	for (int i=0; i< pat.length; i++) {
	    if (matcher.matches(requrl, pat[i])) {
		Substitution s = new Perl5Substitution(sub[i]);
		result = Util.substitute(matcher, pat[i], s, requrl,
					 Util.SUBSTITUTE_ALL);
		break;
	    }
	}
	if (result != null) {
	    URL loc = null;
	    try {
		loc = new URL(req.getURL(), result);
	    } catch (MalformedURLException ex) {
		return null;
	    }
	    Reply reply = req.makeReply(HTTP.FOUND);
	    reply.setLocation(loc);
	    HtmlGenerator g = new HtmlGenerator("Moved");
	    g.append("<P>This resource has moved, click  if your browser"
		     + " doesn't support automatic redirection<BR>"+
		     "<A HREF=\""+loc.toExternalForm()+"\">"+
		     loc.toExternalForm()+"</A>");
	    reply.setStream(g);
	    return reply ;
	}
	return null;
    }
}
	


