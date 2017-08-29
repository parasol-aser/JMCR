package driver;

import spider.SpiderConfig;
import spider.Spider;

import spider.Constants;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Category;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4ReExRunner;

@RunWith(JUnit4ReExRunner.class)
public class MyWeblechTest implements Constants
{
    /** For class-related messages */
    private static Category _logClass = Category.getInstance(MyWeblechTest.class); 
    
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
		MyWeblechTest.main(new String[]{});
	}
} // End class TextSpider
