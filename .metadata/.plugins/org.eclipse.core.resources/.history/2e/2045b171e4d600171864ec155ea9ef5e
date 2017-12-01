package omcr.manager;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class ManagerTest {
	
	
	
	
	
	

	/**
	 * gets 2 parameters:   1. number of threads
	 *                      2. number of pointers to release
	 */
	public static void main(String arg[])
	{
		
		
//		long start, end;
//		start = System.nanoTime(); //start timestamp
//		
//		int init_req_counter;
//		if ( arg.length == 2)
//		{
//			init_req_counter=request_counter = 100;
//		}
//		else if (arg.length != 3){
//			System.out.println("ERROR - wrong number of arguments");
//			System.out.println("Usage:Manager OutputFile NumOfThreads NumOfPtrs ");
//			return;
//		}
//		else
//		{
//			request_counter = Integer.parseInt(arg[2]);
//			init_req_counter= request_counter;
//		}
		int num_of_threads = 4;
		int init_req_counter = 10;
		int request_counter = 0;
		int released_counter = 0;
		boolean flag = false;
		Manager manager = new Manager(num_of_threads, request_counter, released_counter, flag);
		System.out.println("Number of memory blocks to release: " + init_req_counter);
		System.out.println("Number of memory blocks released: " + released_counter);
		//FileOutputStream outStream;
		//DataOutputStream outputStream;
		try
		{
			//outStream = new FileOutputStream(arg[0],true);
			//outputStream = new DataOutputStream (outStream);
			
			assert(init_req_counter == Manager.released_counter);
			flag = !(init_req_counter == Manager.released_counter);
			if(flag)
			{
				//outputStream.writeBytes( "Program name: Manager , Bug found: " + flag + "\r\n");
				System.out.println("Program name: Manager , Bug found: " + flag + "\r\n");
				throw new Exception();
			}
			else
			{
				//outputStream.writeBytes( "Program name: Manager , None\r\n");
				System.out.println("Program name: Manager , None\r\n");
			}//*/
			
			//outputStream.flush();
			//outputStream.close();
			//outStream.close();
		}
		//catch (IOException E){System.out.println("Unable to write results to file "+E.getMessage());}
		catch(Exception e)
		{
			"Crashed_with".equals(e);
		}
//		end = System.nanoTime(); //** end timestamp
//		double time = (((double)(end - start)/1000000000));
//		System.out.println("\nEXECUTION TIME: "+time+"s");
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			ManagerTest.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}
