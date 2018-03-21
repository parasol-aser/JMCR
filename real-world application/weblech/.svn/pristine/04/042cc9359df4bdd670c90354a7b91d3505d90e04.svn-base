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

// $Header: /project/jiss/smhuang/leap/weblech/src/weblech/util/Log4j.java,v 1.1 2010/06/30 15:45:28 smhuang Exp $

package weblech.util;

import org.apache.log4j.*;

import java.io.IOException;

public class Log4j
{
    private static Category _logClass = Category.getInstance(Log4j.class);

    static
    {
        Layout l = new PatternLayout("%d [%t] %-5p %F:%L - %m\n");
        ConsoleAppender capp = new ConsoleAppender(l);
        capp.setThreshold(Priority.INFO);
        BasicConfigurator.configure(capp);
        try
        {
            FileAppender fapp = new FileAppender(l, "weblech.log", false);
            BasicConfigurator.configure(fapp);
            System.err.println("Log4j configured to use weblech.log -- view full logging here");
        }
        catch(IOException ioe)
        {
            _logClass.warn("IO Exception when configuring log4j: " + ioe.getMessage(), ioe);
        }
        _logClass.debug("Log4j configured");
    }

    public static void init()
    {

    }
}
