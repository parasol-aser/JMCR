package driver;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4ReExRunner;

@RunWith(JUnit4ReExRunner.class)
public class JigsawDriver 
{
	static org.w3c.jigsaw.daemon.ServerHandlerManager server;

	public static void main(final String[] args)
	{
		try{
		Thread t1 = new Thread()
		{
			public void run()
			{
				server = org.w3c.jigsaw.daemon.ServerHandlerManager.test(new String[0]);
			}
		};
		
		Thread t2 = new Thread()
		{
			public void run()
			{
				JigsawHarnessPretex.main(args);
			}
		};
		
		t1.start();
		t2.start();
		//Thread.sleep(60000);
		
		t1.join();
		t2.join();
		//System.exit(0);
		server.shutdown();
//		Set<Thread> threadSet =Thread.getAllStackTraces().keySet();
//		for(Thread t: threadSet)
//		{
//			if(t.isDaemon())
//			{
//				if(t.getName().contains("http-server"))
//				{
//					t.interrupt();
//				}
//			}
//		}

		System.out.println("Done");

	}catch(Exception e)
	{
		
	}
	}
	
	@Test
	public void test() throws InterruptedException {
		JigsawDriver.main(new String[]{});
	}
}
