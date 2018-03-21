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

// $Header: /project/jiss/smhuang/leap/weblech/src/spider/URLObject.java,v 1.1 2010/06/30 15:45:26 smhuang Exp $

package spider;

import org.apache.log4j.Category;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

import weblech.util.Log4j;

public class URLObject
{
    private final static Category _logClass = Category.getInstance(URLObject.class);

    static
    {
        Log4j.init();
    }

    private final URL sourceURL;
    private final String contentType;
    private final byte[] content;

    private final SpiderConfig config;

    public URLObject(URL sourceURL, String contentType, byte[] content, SpiderConfig config)
    {
        this.sourceURL = sourceURL;
        this.contentType = contentType;
        this.content = content;
        this.config = config;
    }

    public URLObject(URL sourceURL, SpiderConfig config)
    {
        this.sourceURL = sourceURL;
        this.config = config;

        String s = sourceURL.toExternalForm().toLowerCase();
        if(s.indexOf(".jpg") != -1)
        {
            contentType = "image/jpeg";
        }
        else if(s.indexOf(".gif") != -1)
        {
            contentType = "image/gif";
        }
        else
        {
            contentType = "text/html";
        }

        if(existsOnDisk())
        {

            File f = new File(convertToFileName());
            if(f.isDirectory())
            {
                f = new File(f, "index.html");
            }
            content = new byte[(int) f.length()];
            try
            {
                FileInputStream in = new FileInputStream(f);
                in.read(content);
                in.close();
            }
            catch(IOException ioe)
            {
                _logClass.warn("IO Exception reading disk version of URL " + sourceURL, ioe);
            }
        }
        else
        {
            content = new byte[0];
        }
    }

    public String getContentType()
    {
        return contentType;
    }

    public boolean isHTML()
    {
        return contentType.toLowerCase().startsWith("text/html");
    }

    public boolean isXML()
    {
        return contentType.toLowerCase().startsWith("text/xml");
    }

    public boolean isImage()
    {
        return contentType.startsWith("image/");
    }

    public String getStringContent()
    {
        return new String(content);
    }

    private String convertToFileName()
    {
        String url = sourceURL.toExternalForm();
        int httpIdx = url.indexOf("http://");
        if(httpIdx == 0)
        {
            url = url.substring(7);
        }
        // Check for at least one slash -- otherwise host name (e.g. sourceforge.net)
        if(url.indexOf("/") < 0)
        {
            url = url + "/";
        }
        // If trailing slash, add index.html as default
        if(url.endsWith("/"))
        {
            url = url + "index.html";
        }
        url = textReplace("?", URLEncoder.encode("?"), url);
        url = textReplace("&", URLEncoder.encode("&"), url);
        return config.getSaveRootDirectory().getPath() + "/" + url;
    }

    public boolean existsOnDisk()
    {
        File f = new File(convertToFileName());
        return (f.exists() && !f.isDirectory());
    }

    public void writeToFile()
    {
        writeToFile(convertToFileName());
    }

    public void writeToFile(String fileName)
    {
        _logClass.debug("writeToFile(" + fileName + ")");
        try
        {
            File f = new File(fileName);
            f.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(content);
            out.flush();
            out.close();
        }
        catch(IOException ioe)
        {
            _logClass.warn("IO Exception writing to " + fileName, ioe);
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("URLObject: ");
        sb.append(contentType);
        if(false)//isHTML() || isXML())
        {
            sb.append("\n");
            sb.append(getStringContent());
        }
        return sb.toString();
    }

    private String textReplace(String find, String replace, String input)
    {
        int startPos = 0;
        while(true)
        {
            int textPos = input.indexOf(find, startPos);
            if(textPos < 0)
            {
                break;
            }
            input = input.substring(0, textPos) + replace + input.substring(textPos + find.length());
            startPos = textPos + replace.length();
        }
        return input;
    }
}
