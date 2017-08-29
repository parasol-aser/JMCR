// PushReply.java
// $Id: PushReply.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2001.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.cache.push;

import org.w3c.www.mime.Utils;
import org.w3c.www.mime.MimeType;

import org.w3c.www.protocol.http.Reply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * PushReply
 * "Forged" Reply for resources inserted into the cache from a push source
 * Created by PushCacheHandler on receipt of an "ADD" request
 *
 * @author Paul Henshaw, The Fantastic Corporation, Paul.Henshaw@fantastic.com
 * @version $Revision: 1.1 $
 * $Id: PushReply.java,v 1.1 2010/06/15 12:25:44 smhuang Exp $
 */ 
public class PushReply extends Reply {
    /**
     * Default mime type is "text/html"
     */
    public static final String DEFAULT_MIME_TYPE="text/html";

    private String _path=null;
    private String _url=null;
    private FileInputStream _fis;
    private File _file;

    /**
     * The URL for which the Reply has been forged
     */
    public String getUrl() {
	return(_url);
    }

    /**
     * The file to be stored 
     */
    public File getFile() {
        return(_file);
    }

    /**
     * Access to file contents
     */
    public FileInputStream getStream() {
	return(_fis);
    }
    
    /**
     * Construct a PushReply.  
     * Use PushCacheManager.storeReply to store reply in the cache
     * Throws a FileNotFoundException if the path specified is not found
     *
     * @param path  absolute pathname of the file to appear as response
     * @param url   the URL to masquerade as
     */
    public PushReply(String path, String url) throws FileNotFoundException {
	super((short)1,(short)1,200);  // HTTP 1.1, 200 OK

	_url=new String(url);
	_path=new String(path);
	_file=new File(path);

	_fis=new FileInputStream(_file);
	setStream(_fis);

	String mimeType=Utils.guessContentTypeFromName(_url);

	try {
	    if(mimeType==null || mimeType.equals("content/unknown")) {
		setContentType(new MimeType(DEFAULT_MIME_TYPE));
	    }
	    else {
		setContentType(new MimeType(mimeType));
	    }
	}
	catch(org.w3c.www.mime.MimeTypeFormatException e) {
	    e.printStackTrace();
	    // SHOULD NEVER HAPPEN
	}

	setContentLength((int)_file.length());
	setValue(PushCacheManager.HEADER_FIELD,PushCacheManager.HEADER_VALUE);
    }
}
