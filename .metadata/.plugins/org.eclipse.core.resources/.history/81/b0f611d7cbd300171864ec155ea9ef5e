package omcr.airline;

import java.io.FileOutputStream;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

/**
 * Created by IntelliJ IDEA.
 * User: amit rotstein I.D: 037698867
 * Date: Oct 17, 2003
 * Time: 1:02:13 PM
 * To change this template use Options | File Templates.
 */

public  class airline implements Runnable{

	static int  Num_Of_Seats_Sold;
	int         Maximum_Capacity, Num_of_tickets_issued;
	boolean     StopSales;
	Thread      threadArr[] ;
	FileOutputStream output;

	private String fileName;

	public airline (String fileName, String Concurency){
		Num_Of_Seats_Sold = 0;
		StopSales = false;
		this.fileName = fileName;
		try {
			output = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		
		
		if(Concurency.equals("little")) Num_of_tickets_issued = 4;
		if(Concurency.equals("average")) Num_of_tickets_issued = 100;
		if(Concurency.equals( "lot")) Num_of_tickets_issued = 5000;
		Num_of_tickets_issued = 4;
		Maximum_Capacity = Num_of_tickets_issued - (Num_of_tickets_issued)/2 ; // issuing 10% more tickets for sale
		threadArr = new Thread[Num_of_tickets_issued];

		System.out.println( "The airline issued "+ Num_of_tickets_issued +" tickets for "+Maximum_Capacity+" seats to be sold.");
		/**
		 * starting the selling of the tickets:
		 * "StopSales" indicates to the airline that the max capacity was sold & that they should stop issuing tickets
		 */
		int i=0;
		for( i=0; i < Num_of_tickets_issued; i++) {

			threadArr[i] = new Thread (this) ;
			
			/**
			 * first the airline is checking to see if it's agents had sold all the seats:
			 */
			if( StopSales ){
				Num_Of_Seats_Sold--;
				System.out.println("decrease seats sold to "+Num_Of_Seats_Sold+" and break");
				break;
			}
			/**
			 * THE BUG : StopSales is updated by the selling posts ( public void run() ), and by the time it is updated
			 *           more tickets then are allowed to be are sold by other threads that are still running
			 */
			threadArr[i].start();  // "make the sale !!!"
//			System.out.println("start "+(i+1));
		}
	
		/*try{
			for(i=0; i < Num_of_tickets_issued; i++) {
				threadArr[i].join();
				System.out.println("join "+(i+1));
			}
		}
		catch(InterruptedException e)
		{
			System.err.println(e.getMessage());
		}*/
 
		try {
			output = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		String str1="< "+fileName+" , Concurency="+Concurency+" , "+"No Error"+" >";
		String str2="< "+fileName+" , Concurency="+Concurency+" , "+"Interleaving Bug"+" >";

		try{
//			System.out.println("SOLD "+ Num_Of_Seats_Sold + " Seats !!!");
			assert(Num_Of_Seats_Sold <= Maximum_Capacity);
			if (Num_Of_Seats_Sold > Maximum_Capacity)
			{
				fail();
				output.write(str2.getBytes());
				System.err.println(str2);
				throw new Exception();
			}
			else
			{
				output.write(str1.getBytes());
				System.out.println(str1);
			}
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		catch(Exception e)
		{
			"Crashed_with".equals(e);
		}

	}
	/**
	 * the selling post:
	 * making the sale & checking if limit was reached ( and updating "StopSales" ),
	 */
	public void run() {

		Num_Of_Seats_Sold++;                          // making the sale
		//bug here!
		//try {Thread.currentThread().sleep(10);} catch (InterruptedException e) {}
		if (Num_Of_Seats_Sold > Maximum_Capacity){     // checking
			System.out.println("maximum capacity exceeded, "+Num_Of_Seats_Sold +" sold");
			StopSales = true;                   // updating
		}
		
	}

	
}



