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

// $Header: /project/jiss/smhuang/leap/weblech/src/spider/DownloadQueue.java,v 1.1 2010/06/30 15:45:27 smhuang Exp $

package spider;

import java.util.*;
import java.net.URL;
import java.io.Serializable;

public class DownloadQueue implements Serializable
{
    private SpiderConfig config;

    private List interestingURLsToDownload;
    private List averageURLsToDownload;
    private List boringURLsToDownload;
    private Set urlsInQueue;

    public DownloadQueue(SpiderConfig config)
    {
        this.config = config;
        interestingURLsToDownload = new ArrayList();
        averageURLsToDownload = new ArrayList();
        boringURLsToDownload = new ArrayList();
        urlsInQueue = new HashSet();
    }

    public void queueURL(URLToDownload url)
    {
        URL u = url.getURL();
        if(urlsInQueue.contains(u))
        {
            return;
        }

        if(config.isInteresting(u))
        {
            if(config.isDepthFirstSearch())
            {
                interestingURLsToDownload.add(0, url);
            }
            else
            {
                interestingURLsToDownload.add(url);
            }
        }
        else if(config.isBoring(u))
        {
            if(config.isDepthFirstSearch())
            {
                boringURLsToDownload.add(0, url);
            }
            else
            {
                boringURLsToDownload.add(url);
            }
        }
        else
        {
            if(config.isDepthFirstSearch())
            {
                averageURLsToDownload.add(0, url);
            }
            else
            {
                averageURLsToDownload.add(url);
            }
        }

        urlsInQueue.add(u);
    }

    public void queueURLs(Collection urls)
    {
        for(Iterator i = urls.iterator(); i.hasNext(); )
        {
            URLToDownload u2d = (URLToDownload) i.next();
            queueURL(u2d);
        }
    }

    public URLToDownload getNextInQueue()
    {
        if(interestingURLsToDownload.size() > 0)
        {
            return returnURLFrom(interestingURLsToDownload);
        }
        else if(averageURLsToDownload.size() > 0)
        {
            return returnURLFrom(averageURLsToDownload);
        }
        else if(boringURLsToDownload.size() > 0)
        {
            return returnURLFrom(boringURLsToDownload);
        }
        else
        {
            return null;
        }
    }

    private URLToDownload returnURLFrom(List urlList)
    {
        URLToDownload u2d = (URLToDownload) urlList.get(0);
        urlList.remove(0);
        urlsInQueue.remove(u2d.getURL());
        return u2d;
    }

    public int size()
    {
        return interestingURLsToDownload.size() + averageURLsToDownload.size() + boringURLsToDownload.size();
    }

    public String toString()
    {
        return size() + " URLs";
    }

} // End class DownloadQueue
