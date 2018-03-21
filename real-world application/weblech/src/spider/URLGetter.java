/*
 * This is the MIT license, see also http://www.opensource.org/licenses/mit-license.html
 *
 * Copyright (c) 2001 Brian Pitcher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// $Header: /project/jiss/smhuang/leap/weblech/src/spider/URLGetter.java,v 1.1 2010/06/30 15:45:25 smhuang Exp $

package spider;

import org.apache.log4j.Category;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Authenticator;
import java.io.*;

import weblech.util.Log4j;

public class URLGetter
{
    private final static Category _logClass = Category.getInstance(URLGetter.class);

    static
    {
        Log4j.init();
    }

    private int failureCount = 0;

    private final SpiderConfig config;

    public URLGetter(SpiderConfig config)
    {
        _logClass.debug("URLGetter()");
        this.config = config;

        Authenticator.setDefault(new DumbAuthenticator(config.getBasicAuthUser(), config.getBasicAuthPassword()));
    }

    public URLObject getURL(URLToDownload url)
    {
        _logClass.debug("getURL(" + url + ")");

        if(failureCount > 10)
        {
            _logClass.warn("Lots of failures recently, waiting 5 seconds before attempting download");
            try { Thread.sleep(5 * 1000); } catch(InterruptedException e) { };
            failureCount = 0;
        }

        URL requestedURL = url.getURL();
        URL referer = url.getReferer();

        try
        {
            _logClass.debug("Creating HTTP connection to " + requestedURL);
            HttpURLConnection conn = (HttpURLConnection) requestedURL.openConnection();
            if(referer != null)
            {
                _logClass.debug("Setting Referer header to " + referer);
                conn.setRequestProperty("Referer", referer.toExternalForm());
            }

            if(config.getUserAgent() != null)
            {
                _logClass.debug("Setting User-Agent to " + config.getUserAgent());
                conn.setRequestProperty("User-Agent", config.getUserAgent());
            }

            conn.setUseCaches(false);

            _logClass.debug("Opening URL");
            long startTime = System.currentTimeMillis();
            conn.connect();

            String resp = conn.getResponseMessage();
            _logClass.debug("Remote server response: " + resp);

            String respStr = conn.getHeaderField(0);
            _logClass.info("Server response: " + respStr);

            for(int i = 1; ; i++)
            {
                String key = conn.getHeaderFieldKey(i);
                if(key == null)
                {
                    break;
                }
                String value = conn.getHeaderField(key);
                _logClass.debug("Received header " + key + ": " + value);
            }

            _logClass.debug("Getting buffered input stream from remote connection");
            BufferedInputStream remoteBIS = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
            byte[] buf = new byte[1024];
            int bytesRead = 0;
            while(bytesRead >= 0)
            {
                baos.write(buf, 0, bytesRead);
                bytesRead = remoteBIS.read(buf);
            }

            byte[] content = baos.toByteArray();
            long timeTaken = System.currentTimeMillis() - startTime;
            if(timeTaken < 100) timeTaken = 500;

            int bytesPerSec = (int) ((double) content.length / ((double)timeTaken / 1000.0));
            _logClass.info("Downloaded " + content.length + " bytes, " + bytesPerSec + " bytes/sec");
            if(content.length < conn.getContentLength())
            {
                _logClass.warn("Didn't download full content for URL: " + url);
                failureCount++;
                return null;
            }
            return new URLObject(requestedURL, conn.getContentType(), content, config);
        }
	catch(FileNotFoundException fnfe) {
	    _logClass.warn("File not found: " + fnfe.getMessage());
	    return null;
	}
        catch(IOException ioe)
        {
            _logClass.warn("Caught IO Exception: " + ioe.getMessage(), ioe);
            failureCount++;
            return null;
        }
    }
}
