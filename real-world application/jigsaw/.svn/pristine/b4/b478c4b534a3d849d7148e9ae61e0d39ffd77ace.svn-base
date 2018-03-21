// JpegFileResource.java
// $Id: JpegFileResource.java,v 1.1 2010/06/15 12:20:39 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import org.w3c.tools.resources.FileResource;

import org.w3c.tools.jpeg.JpegCommentHandler;

public class JpegFileResource extends ImageFileResource {
    /**
     * Save the given stream as the underlying file content.
     * This method preserve the old file version in a <code>~</code> file.
     * @param in The input stream to use as the resource entity.
     * @return A boolean, <strong>true</strong> if the resource was just
     * created, <strong>false</strong> otherwise.
     * @exception IOException If dumping the content failed.
     */

    public synchronized boolean newMetadataContent(InputStream in) 
	throws IOException
    {
	File   file     = getFile() ;
	boolean created = (! file.exists() || (file.length() == 0));
	String name     = file.getName() ;
	File   temp     = new File(file.getParent(), "#"+name+"#") ;
	String iomsg    = null ;
	JpegCommentHandler jpegHandler = new JpegCommentHandler(file);
	// We are not catching IO exceptions here, except to remove temp:
	try {
	    FileOutputStream fout  = new FileOutputStream(temp) ;
	    char             buf[] = new char[4096] ;
	    Writer writer = jpegHandler.getOutputStreamWriter(fout);
	    InputStreamReader reader = new InputStreamReader(in);
	    for (int got = 0 ; (got = reader.read(buf)) > 0 ; )
		writer.write(buf, 0, got) ;
	    writer.close() ;
	} catch (IOException ex) {
	    iomsg = ex.getMessage() ;
	} finally {
	    if ( iomsg != null ) {
		temp.delete() ;
		throw new IOException(iomsg) ;
	    } else {
		if (getBackupFlag()) {
		    File backup = getBackupFile();
		    if (backup.exists())
			backup.delete();
		    file.renameTo(getBackupFile()) ;
		}
		// with some OSes, rename doesn't overwrite so...
		if (file.exists()) 
		    file.delete();
		temp.renameTo(file) ;
		// update our attributes for this new content:
		updateFileAttributes() ;
	    }
	}
	return created;
    }
}
