// ServletCommand.java
// $Id: ServletCommand.java,v 1.1 2010/06/15 12:29:53 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.ssi.servlets;

import java.util.Dictionary;
import java.util.Hashtable;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.LookupResult;
import org.w3c.tools.resources.LookupState;
import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceException;
import org.w3c.tools.resources.ResourceReference;

import org.w3c.jigsaw.servlet.JigsawHttpServletRequest;
import org.w3c.jigsaw.servlet.JigsawHttpServletResponse;
import org.w3c.jigsaw.servlet.ServletWrapper;

import org.w3c.util.ArrayDictionary;

import org.w3c.jigsaw.ssi.commands.Command;

import org.w3c.jigsaw.ssi.SSIFrame;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ResourceException;

/**
 * Implementation of the SSI <code>servlet</code> command.
 * Servlet can be executed 
 * simply by providing a url path to a servlet class.
 * @author Benoit Mahe <bmahe@sophia.inria.fr> 
 */
public class ServletCommand implements Command {
    private final static String  NAME  = "servlet";
    private final static boolean debug = true;

    private static final String keys[] = {
	"code",
	"param",
	"value",
	"name"
    };

    protected static Hashtable wrappers = null; // <classname , wrapper>

    static {
	wrappers = new Hashtable(10);
    }

    protected void addParam(Dictionary d, String name, 
			    String param, String value) 
    {
	Hashtable params = (Hashtable)d.get(getClass().getName()+"."+name);
	if (params == null) {
	    params = new Hashtable(5);
	    params.put(param,value);
	} else {
	    Object ovalue = params.get(param);
	    if (ovalue == null) {
		params.put(param,value);
	    } else  if (ovalue instanceof String[]) {
		String oldValues [] = (String[])ovalue;
		String newValues [] = new String[oldValues.length+1];
		System.arraycopy(oldValues,0,newValues,0,oldValues.length);
		newValues[oldValues.length] = value;
		params.put(param,newValues); 
	    } else {
		String newValues [] = new String[2];
		newValues[0] = (String)ovalue;
		newValues[1] = value;
		params.put(param,newValues);
	    }
	}
	d.put(getClass().getName()+"."+name, params);
    }

    protected Hashtable getParams(Dictionary d, String name) {
	return (Hashtable) d.get(getClass().getName()+"."+name);
    }

    public String getName() {
	return NAME;
    }

    public String getValue(Dictionary variables, String var, Request request) {
	return null;
    }

    protected boolean isRemote(String code) {
	try {
	    URL url = new URL(code);
	} catch (MalformedURLException ex) {
	    return false;
	}
	return true;
    }

    /**
     * return true if reply can be cached.
     * @return a boolean.
     */
    public boolean acceptCaching() {
	return false;
    }

    public Reply execute(SSIFrame ssiframe
			 , Request request
			 , ArrayDictionary parameters
			 , Dictionary variables) {
	Object values[] = parameters.getMany(keys);
	String code     = (String) values[0];
	String param    = (String) values[1];
	String value    = (String) values[2];
	String name     = (String) values[3];

	if (name != null) {
	    if ((param != null) && (value != null)) {
		//store a new param for servlet "name"
		addParam(variables,name,param,value);
	    }
	    if (code != null) { // remote or not ??
		ResourceReference r_wrapper = 
		    (ResourceReference)wrappers.get(code);
		if (r_wrapper == null) { //lookup for wrapper
		    httpd server = 
			(httpd) ssiframe.getFileResource().getServer();
		    ResourceReference rr_root = server.getRootReference();
		    try {
			FramedResource root = (FramedResource) rr_root.lock();
			LookupState ls = new LookupState(code);
			LookupResult lr = new LookupResult(rr_root);
			ResourceReference wrap = null;
			if (root.lookup(ls,lr))
			    wrap = lr.getTarget();
			if (wrap != null) {
			    try {
				if (wrap.lock() instanceof ServletWrapper) {
				    wrappers.put(code,wrap);
				    r_wrapper = wrap;
				}
			    } catch (InvalidResourceException ex) { 
				ex.printStackTrace();
				r_wrapper = null;
			    } finally {
				wrap.unlock();
			    }
			}
		    } catch (ProtocolException ex) {
			ex.printStackTrace();
			r_wrapper = null;
		    } catch (InvalidResourceException ex) {
			ex.printStackTrace();
			r_wrapper = null;
		    } finally {
			rr_root.unlock();
		    }
		}
		if (r_wrapper != null) {
		    //initialize the wrapper (params)
		    Hashtable params = getParams(variables, name);
		    request.setState(JigsawHttpServletRequest.STATE_PARAMETERS,
				     params);
		    //perform the request
		    try {
			FramedResource wrapper = 
			    (FramedResource)r_wrapper.lock();
			Request req = (Request)request.getClone();
			req.setState(JigsawHttpServletResponse.INCLUDED, 
				     Boolean.TRUE);
			return (Reply) wrapper.perform(req);
		    } catch (ProtocolException ex) {
			ex.printStackTrace();
			// return default reply
		    } catch (ResourceException ex2) {
			ex2.printStackTrace();
			// return default reply
		    } catch (InvalidResourceException ex3) {
			ex3.printStackTrace();
			// return default reply
		    } finally {
			r_wrapper.unlock();
		    }
		}
	    }
	}
	// We are NOT doing notMod hack here (tricky and useless ?)
	//Reply reply = ssiframe.createCommandReply(request, HTTP.OK);
	Reply reply = request.makeReply(HTTP.OK);
	reply.setContent("");
	return reply;
    }
}
