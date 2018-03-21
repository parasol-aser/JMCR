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

// $Header: /project/jiss/smhuang/leap/weblech/src/spider/URLToDownload.java,v 1.1 2010/06/30 15:45:25 smhuang Exp $

package spider;

import java.net.URL;

public class URLToDownload implements java.io.Serializable
{
    private final URL url;
    private final URL referer;
    private final int depth;

    public URLToDownload(URL url, int depth)
    {
        this(url, null, depth);
    }

    public URLToDownload(URL url, URL referer, int depth)
    {
        this.url = url;
        this.referer = referer;
        this.depth = depth;
    }

    public URL getURL()
    {
        return url;
    }

    public URL getReferer()
    {
        return referer;
    }

    public int getDepth()
    {
        return depth;
    }

    public String toString()
    {
        return url + ", referer " + referer + ", depth " + depth;
    }
}
