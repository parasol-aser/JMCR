package omcr.account;
import static org.junit.Assert.fail;

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

/*
 * The Bank class is a demo for a multi-threaded system which manages accounts
 * while keeping track of their internal balance. The chance for error is very low
 * although possible for the lack of synchronization.
 */
@RunWith(JUnit4MCRRunner.class)
public class Bank{

	// Total balance as recorded in bank.
	static int Bank_Total;

	// all accounts
	static Account[] accounts; 

	// random numbers generator
	static Random Bank_random;

	// The number of accounts is randomly chosen from [10,110]
	static int NUM_ACCOUNTS = 4;//Math.abs((Bank_random.nextInt()%10 + 1)*10);

	/*
	 * Method main creates all the accounts from which the Bank accepts requests
	 * for actions. The total sum of the accounts is recorded on each
	 * action execution.
	 */
	public static void main(String args[]){

		Bank_Total = 0;
		Bank_random = new Random();
		accounts = new Account[NUM_ACCOUNTS];

		try{

			// create all accounts
			for(int i = 0; i< NUM_ACCOUNTS; i++){
				accounts[i] = new Account(i);
			}

			System.out.println("Bank system started");

			// start all accounts
			for(int i = 0; i< NUM_ACCOUNTS; i++){
				accounts[i].start();
			}

			// wait for all threads (accounts) to die.
			for(int i = 0; i< NUM_ACCOUNTS; i++){
				accounts[i].join();
			}


			System.out.println("");
			System.out.println("End of the week.");

			int Total_Balance = 0;
			// sum up all balances.
			for(int i = 0; i< NUM_ACCOUNTS; i++){
				Total_Balance += accounts[i].Balance;
			}	

			// Give report.
			System.out.println("Bank records = "+Bank_Total+", accounts balance = "+Total_Balance+".");
			assert(Bank_Total == Total_Balance);
			if(Bank_Total == Total_Balance)
				System.out.println("Records match.[None]");
			else
			{
				fail();
				System.err.println("Records don't match !!![Bug]");
				throw new Exception();
			}
		}
		catch(Exception e)
		{
			"Crashed_with".equals(e);
		}
	}

	/*
	 * The Service method performs the actual action on the account, 
	 * and it also updates the Bank's records. (Bank_Total)
	 */
	public static void Service(int id,int sum){
		accounts[id].Balance += sum;
		int tmp = Bank_Total;
		//try{Thread.sleep(10);}catch(Exception e){} //Nuno: added this
		Bank_Total = sum + tmp;
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			Bank.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}

