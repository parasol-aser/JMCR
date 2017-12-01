package omcr.account;
import java.util.*;

/*
 * The Account class represents the actual accounts in the Bank.
 * It's run() method simulates actions on the account.
 */
class Account extends Thread{

	// The balance of the account.
	public int Balance = 0;
	
	// The account id (allocated by the bank)
	public int Account_Id = 0;
	
	// Maximum sum for a single action
	static int MAX_SUM = 300;
	
	// Internal randomizer for the account (simulates account owner)
	Random random = new Random(Bank.Bank_random.nextInt());
	
	/*
	 * The constructor initiates the account's id.
	 */
	Account(int id){
		Account_Id = id;
	}

	/*
	 * The Action method simulates an action on the account, 
	 * where the sum of the action is randomly chosen, and 
	 * then the bank is requested for a service.
	 */
	public void Action(){

		// get a random sum in the range [0,MAX_SUM]
		int sum = 1;//random.nextInt()%MAX_SUM;

		// perform action in Bank
		Bank.Service(Account_Id,sum);
	}

	
	/*
	 * The run method performs the actual actions on the account, and
	 * it is supposed to simulate the account owner's actions during the week.
	 */
	public void run(){
		// maximum 500 actions
		int loop = 2;//Math.abs(random.nextInt()%500);
		// perform action
		for(int i = 0; i< loop; i++){
			this.Action();
			
			/*try{ //sleep a little
				Thread.sleep(10);
			}catch(Exception exception){
			}*/
		}
//		System.out.println("start "+(this.Account_Id+1));
		//System.out.print(".");
	}
}