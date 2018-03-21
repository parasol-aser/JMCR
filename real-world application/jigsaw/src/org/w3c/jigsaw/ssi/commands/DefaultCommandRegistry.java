// DefaultCommandRegistry.java
// $Id: DefaultCommandRegistry.java,v 1.1 2010/06/15 12:21:54 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.commands ;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import org.w3c.www.http.HeaderValue;
import org.w3c.util.ArrayDictionary;
import org.w3c.jigsaw.http.Client;
import org.w3c.jigsaw.http.Request;
import org.w3c.www.mime.MimeType;
import org.w3c.jigsaw.ssi.SSIFrame;

import org.w3c.jigsaw.forms.URLDecoder ;
import org.w3c.jigsaw.forms.URLDecoderException ;

/**
 *      <P>This class provides the most general and commonly used SSI commands.
 *	Compatibility with the NCSA-style directive set has been maintained
 *	as much as it made sense to, and new functionality adequate to Jigsaw
 *	has been added.</P>
 *
 *      <P>In the description that follows, please refer to
 *	<A HREF="http://hoohoo.ncsa.uiuc.edu/docs/tutorials/includes.html">
 *	  the NCSA server-side includes tutorial
 *	</a> for comparison.</P>
 *
 *      <P>The full set of commands of the DefaultCommandRegistry is:
 *      <DL>
 *	<DT> <A name="cmd-config"><code>config</code></A>
 *	<DD> The <code>errmsg</code> tag is not implemented.
 *
 *	<DT> <A name="cmd-include"><code>include</code></A>
 *	<DD>
 *	  The <code>file</code> and <code>virtual</code> tags are handled in
 *	  the same way. Both originate an internal request to the URL given
 *	  as the value of the tag. There is no provision for including a file
 *	  that is not indexed by Jigsaw. This command can be used to include
 *	  the content of <em>any</em> resource. This includes the SSIFrame.
 *	  <P> In addition, the following tags are admissible:
 *	  <DL>
 *	    <DT> <code>ifheader</code>
 *	    <DD>
 *	      Its value is interpreted as a header name. It causes the
 *	      resource to be included only if that header was defined in the
 *	      original (client) request.
 *	    <DT> <code>else</code>
 *	    <DD>
 *	      Used in conjunction with <code>ifheader</code>, it specifies a
 *	      URL to be included in case the header is <em>not</em> defined.
 *	  </DL>
 *
 *	<DT> <A name="cmd-echo"><code>echo</code></A>
 *	<DD>
 *	  In addition to the <code>var</code> tag, which has the NCSA
 *	  behavior, the following tags are admissible:
 *	  <DL>
 *	    <DT> <code>reqstate</code>
 *	    <DD>
 *	      Its value is interpreted as a Jigsaw request state, and
 *	      is expanded as the value of the state. For instance, the
 *	      command <BR> <code>&lt;!--#echo
 *	      reqstate="org.w3c.jigsaw.filters.CounterFilter.count"--&gt;</code><BR>
 *	      will print the current hit-count, assuming a
 *	      CounterFilter exists for the resource.
 *	    <DT> <code>reqheader</code>
 *	    <DD>
 *	      Its value is interpreted as a header in the request, and is
 *	      expanded as the value of the header.
 *	    <DT> <code>here</code>
 *	      If this tag is present, command is expanded as interpreted relative
 *	      to the innermost internal request. By default, it is interpreted
 *	      relative to the original (client) request.
 *	  </DL>
 *	  
 *	<DT> <A name="cmd-fsize"><code>fsize</code></A>
 *	<DD>
 *	  Behaves like its NCSA counterpart, except that it also
 *	  recognizes the tag <code>here</code>. If present, this tag
 *	  indicates to include the file size of the innermost included
 *	  file. Normally, it includes the file size of the topmost
 *	  SSI-parsed file requested by the client.  It honors the
 *	  <code>sizefmt</code> variable, as set by
 *	  <code>config</code>.
 *
 *	<DT> <A name="cmd-flastmod"><code>flastmod</code></A>
 *	<DD>
 *	  In addition to NCSA behavior, it honors the
 *	  <code>here</code> tag, which indicates to include the time
 *	  stamp of the innermost included file.
 *
 *	<DT> <A name="cmd-exec"><code>exec</code></A>
 *	<DD>
 *	  It accepts <em>only</em> the <code>cmd</code> tag.  Given
 *	  that the <code>include</code> command can include
 *	  CgiResources, the <code>cgi</code> tag is superfluous.  
 *	  <P> If the SSIFrame <code>secure</code> attribute is set,
 *	  this command will be inoperative.
 *	  
 *	<DT> <A name="cmd-params"><code>params</code></A>
 *	<DD>
 *	  This command expands to an HTML unordered list of the
 *	  parameters that it was called with. Provided mainly for instructional
 *	  purposes.
 *
 *	<DT> <A name="cmd-count"><code>count</code></A>
 *	<DD>
 *	  Expands to the access count reported by the CounterFilter.
 *	  (This may or may not mean the access count of the document,
 *	  depending on the way the CounterFilter is set up)
 *
 *      </DL>
 *
 * @author Antonio Ramirez <anto@mit.edu>
 *
 */	  
	    

public class DefaultCommandRegistry extends BasicCommandRegistry {
    private static final boolean[] needsEscape = new boolean[256] ;

    private static MimeType type = MimeType.APPLICATION_X_WWW_FORM_URLENCODED ;

    private static Command[] cmds =
    {
	new ConfigCommand(),
	new IncludeCommand(),
	new EchoCommand(),
	new FSizeCommand(),
	new FLastModCommand(),
	new ExecCommand(),
	new SampleCommand(),
	new CounterCommand(),
	new CountCommand(),
	new CookieCommand(),
	new org.w3c.jigsaw.ssi.jdbc.jdbcCommand(),
	new LoopCommand(),
	new EndloopCommand(),
	new ExitloopCommand(),
	new IfCommand(),
	new ElseCommand(),
	new EndifCommand(),
	new org.w3c.jigsaw.ssi.servlets.ServletCommand()
    };

   
    static {
	for(int i=0;i<256;i++)
	    needsEscape[i] = false ;

	needsEscape[(int)'&'] =
	    needsEscape[(int)';'] =
	    needsEscape[(int)'\''] = 
	    needsEscape[(int)'`'] =
	    needsEscape[(int)'"'] =
	    needsEscape[(int)'|'] =
	    needsEscape[(int)'*'] =
	    needsEscape[(int)'?'] =
	    needsEscape[(int)'~'] =
	    needsEscape[(int)'<'] =
	    needsEscape[(int)'>'] =
	    needsEscape[(int)'^'] =
	    needsEscape[(int)'('] =
	    needsEscape[(int)')'] =
	    needsEscape[(int)'['] =
	    needsEscape[(int)']'] =
	    needsEscape[(int)'{'] =
	    needsEscape[(int)'}'] =
	    needsEscape[(int)'$'] =
	    needsEscape[(int)'\\'] = true ;
    }

    private String unescape(String str)
    {
	if(str == null) return null ;
	StringBuffer buf = new StringBuffer(str.length() + 10) ;
	for(int i=0;i<str.length();i++) {
	    if(needsEscape[(int)((byte)str.charAt(i))])
		buf.append('\\') ;
	    buf.append(str.charAt(i)) ;
	}
	return buf.toString() ;
    }

    public DefaultCommandRegistry()
    {
	for(int i=0;i<cmds.length;i++) {
	    registerCommand(cmds[i]) ;
	}
    }

    public Dictionary initVariables(SSIFrame ssiframe,
				    Request request,
				    Dictionary variables)
    {
	variables = super.initVariables(ssiframe,request,variables) ;
	if(variables == null) variables = new Hashtable(5) ;

	// Format variables:
	safePut(variables,"sizefmt","abbrev") ;  // Abbreviated file sizes
	safePut(variables,"datefmt","%c") ; // Locale's format
	ArrayDictionary ssiVars = new ArrayDictionary(22) ;

	// CGI/SSI variables:
	// This weird nesting of dictionaries is so that the set of
	// variables that is exposed via the echo comand is clearly
	// delimited.
	safePut(ssiVars,"DOCUMENT_NAME",
		ssiframe.getFileResource().getFilename()) ;
	
	safePut(ssiVars,"DOCUMENT_URI",
		request.getURL().toString()) ;
	
	safePut(ssiVars,"QUERY_STRING_UNESCAPED",
		unescape(request.getQueryString())) ;
	
	safePut(ssiVars,"SERVER_SOFTWARE",
		ssiframe.getFileResource().getServer().getSoftware()) ;
	
	safePut(ssiVars,"SERVER_NAME",
		"jigsaw") ;
	
	safePut(ssiVars,"GATEWAY_INTERFACE",
		"org.w3c.jigsaw.ssi.SSIFrame") ; // (?)
	
	safePut(ssiVars,"SERVER_PROTOCOL",
		request.getVersion()) ;
	
	safePut(ssiVars,"SERVER_PORT",
		String.valueOf(ssiframe.getFileResource()
			       .getServer()
			       .getLocalPort())) ;
	
	safePut(ssiVars,"REQUEST_METHOD",
		request.getMethod()) ;
	
	safePut(ssiVars,"PATH_INFO",
		"") ;
	
	safePut(ssiVars,"PATH_TRANSLATED",
		"") ;
	
	safePut(ssiVars,"SCRIPT_NAME",
		"org.w3c.jigsaw.ssi.SSIFrame") ; // (?)
	
	String queryString = request.getQueryString() ;
	safePut(ssiVars,"QUERY_STRING",
		queryString) ;
	
	java.net.InetAddress addr =
	    request.getClient().getInetAddress() ;
	    
	safePut(ssiVars,"REMOTE_HOST",
		addr.getHostName()) ;

	// This should be simpler (why not addr.getIPAddress() ???)
	String s = addr.toString() ;
	byte[] ip = addr.getAddress() ;
	int idx = s.indexOf('/') ;
	safePut(ssiVars,"REMOTE_ADDR",
		(idx == -1) ? s : s.substring(idx+1)) ;
	
	safePut(ssiVars,"REMOTE_USER",
	  request.getState(org.w3c.jigsaw.auth.AuthFilter.STATE_AUTHUSER)) ;
	
	safePut(ssiVars,"AUTH_TYPE",
	  request.getState(org.w3c.jigsaw.auth.AuthFilter.STATE_AUTHTYPE)) ;

	safePut(ssiVars,"REMOTE_IDENT",
		"") ;
	
	safePut(ssiVars,"CONTENT_TYPE",
		request.getContentType()) ;
	
	int cl = request.getContentLength() ;
	if(cl != -1)
	    ssiVars.put("CONTENT_LENGTH",
			String.valueOf(cl)) ; 
	
	HeaderValue hval = request.getHeaderValue(Request.H_ACCEPT) ;
	if(hval != null)
	    ssiVars.put("HTTP_ACCEPT",hval.toExternalForm()) ;
	
	safePut(ssiVars,"HTTP_USER_AGENT",
		request.getUserAgent()) ;

	safePut(ssiVars,"X_LAST_MODIFIED",
	       new Date(ssiframe.getFileResource().getFile().lastModified())) ;

	safePut(ssiVars,"X_FILE_SIZE",
		new Long(ssiframe.getFileResource().getFile().length())) ;

	variables.put("ssiVars",ssiVars) ;
	
	if(variables.get("topSsiVars")==null) 
	    variables.put("topSsiVars",ssiVars) ;
	
	// Check to see if there's a query (either through POST
	// or through getQueryString).
	// If so, throw an URLDecoder to the mix.
	// FIXME!!! URLDecoder strangeness

	try {
	    InputStream qDataStream = null ;
	    if(request.getMethod().equals("POST") &&
	       type.match(request.getContentType()) >= 0) {
		// Notify the client that we are willing to continue:
		Client client = request.getClient();
		if ( client != null ) 
		    client.sendContinue();
		qDataStream = request.getInputStream() ;
	    } else if(queryString != null) {
		qDataStream = new StringBufferInputStream(queryString) ;
	    }
	    if(qDataStream != null) {
		URLDecoder formData = new URLDecoder(qDataStream) ;
		formData.parse() ;
		variables.put("formData",formData) ;
	    }
	} catch(IOException ex) {
	    // nil
	} catch(URLDecoderException ex) {
	    // nil
	}

	return variables ;
    }

    private static final void safePut(Dictionary dict,
				      Object key,
				      Object value)
    {
	if(key==null || value==null) return ;
	dict.put(key,value) ;
    }
}


