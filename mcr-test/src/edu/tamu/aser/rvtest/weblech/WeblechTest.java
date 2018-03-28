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

// $Header: /project/jiss/smhuang/leap/weblech/src/driver/WeblechSpider.java,v 1.1 2010/06/30 15:45:29 smhuang Exp $

package edu.tamu.aser.rvtest.weblech;

import spider.SpiderConfig;
import spider.Spider;

import spider.Constants;
import weblech.util.Logger;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Category;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexecution.JUnit4MCRRunner;


@RunWith(JUnit4MCRRunner.class)
public class WeblechTest implements Constants
{
    /** For class-related messages */
    private static Category _logClass = Category.getInstance(WeblechTest.class); 
    
    public static void main(String[] args)
    {
//        _logClass.debug("main()");
//
//        if(args.length < 1 || args.length > 2)
//        {
//            usage();
//            System.exit(0);
//        }
    	

        String propsFile = "Spider.properties";
//        boolean resume = false;
//        if(args.length == 1)
//        {
//            propsFile = args[0];
//        }
//        else if(!args[0].equals("-resume"))
//        {
//            usage();
//            System.exit(0);
//        }
//        else
//        {
//            resume = true;
//            propsFile = args[1];
//        }

        Properties props = null;
        try
        {
            FileInputStream propsIn = new FileInputStream(propsFile);
            props = new Properties();
            props.load(propsIn);
            propsIn.close();
        }
        catch(FileNotFoundException fnfe)
        {
            _logClass.error("File not found: " + args[0], fnfe);
            System.exit(1);
        }
        catch(IOException ioe)
        {
            _logClass.error("IO Exception caught reading config file: " + ioe.getMessage(), ioe);
            System.exit(1);
        }

        _logClass.debug("Configuring Spider from properties");
        SpiderConfig config = new SpiderConfig(props);
        _logClass.debug(config);
        Spider spider = new Spider(config);

//        if(resume)
//        {
//            _logClass.info("Reading checkpoint...");
//            spider.readCheckpoint();
//        }

        _logClass.info("Starting Spider...");
        spider.start();

        System.out.println("\nHit any key to stop Spider\n");
        try
        {
            while(spider.isRunning())
            {
                if(System.in.available() != 0)
                {
                    System.out.println("\nStopping Spider...\n");
                    spider.stop();
                    break;
                }
                pause(SPIDER_STOP_PAUSE);
            }
        }
        catch(IOException ioe)
        {
            _logClass.error("Unexpected exception caught: " + ioe.getMessage(), ioe);
            System.exit(1);
        }
    }

    private static void pause(long howLong)
    {
        try
        {
            Thread.sleep(howLong);
        }
        catch(InterruptedException ignored)
        {
        }
    }

    private static void usage()
    {
        System.out.println("Usage: weblech.ui.TextSpider [-resume] [config file]");
    }
	
	@Test
	public void test() {
		WeblechTest.main(new String[]{});
	}
} // End class TextSpider
