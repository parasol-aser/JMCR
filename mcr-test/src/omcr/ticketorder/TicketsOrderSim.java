/***************************/
/** Guy David            ***/
/** I.D : 034529602      ***/
/***************************/
package omcr.ticketorder;

import static org.junit.Assert.fail;

import java.io.*;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;
import omcr.account.Bank;

@RunWith(JUnit4MCRRunner.class)
public class TicketsOrderSim {



	static Seat[] seats;
	static TravelAgent[] agents;
	static int seats_num;
	static int agents_num;
	static final int flight_num=74721;
	static final int airline_company_code=8888;
	static boolean bug_occured = false;
	static int bug_count=0;

	//static int bugSumVal = 0;	//** CHANGE


	static void check_ticket_details(int index){
		boolean bug_flag = false;
		if (seats[index].ticket.flight!=flight_num) {
			System.out.println("Flight number is not initialized on seat  " + index);
			bug_occured = true;
			bug_flag = true;
		}
		if (seats[index].ticket.airline!=airline_company_code) {
			System.out.println("Airline company code  is not initialized on seat  " + index);
			bug_occured = true;
			bug_flag = true;
		}
		/*if (seats[index].ticket.status.isEmpty()) {
			System.out.println("Status is not initialized on seat  " + index);
			bug_occured = true;
			bug_flag = true;
		}
		else if (seats[index].ticket.status.compareTo("Sold")!=0) {
			System.out.println("Status is not initialized on seat  " + index);
			bug_occured = true;
			bug_flag = true;
		}*/
		if (bug_flag == true) {
			bug_count++;
		}

	}


	static class Ticket {
		String status;
		int flight;
		int airline;
		int agent_name;
		boolean empty;
		
		Ticket(){
			empty = true;
			status = "";
			flight = -1;
			airline = -1;
			agent_name = -1;
		}
		
		//Nuno: simulates obj = new Ticket(int name)
		void setNewTicket(int name){
			empty = false;
			flight = flight_num;
			airline = airline_company_code;
			status = new String("Sold");
			agent_name =name;
		}
	}

	static class Seat {
		Ticket ticket;
		
		Seat(){
			ticket = new Ticket();
		}
	}


	static class TravelAgent extends Thread {

		int name;
		//public int localSum = 0; //** CHANGE

		TravelAgent( int agent_name){
			name = agent_name;
		}

		public void run(){
			System.out.println("run thread "+Thread.currentThread().getName());
			for( int i = 0; i < seats_num; i++) {
				if (seats[i].ticket.empty) {
					synchronized(seats[i]){
						if (seats[i].ticket.empty) {
							seats[i].ticket.setNewTicket(this.name);

						}
					}
				}
				else {
					synchronized(seats[i].ticket){
						check_ticket_details(i);
					}
				}	
			}

			/*try {
				for(int i = 0; i < 100; i++)
				{
					//** CHANGE{
					//System.out.println(this.getName());
					TicketsOrderSim.bugSumVal++;
					yield();
					localSum++;
					//** CHANGE}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}


	static void get_input(String [] args){
		if (args[1].compareTo("user")==0) {
			if(args.length == 2 )  {
				System.out.println("When You enter user, You must enter number of agents and tickets ");
				System.exit(0);
			}
			agents_num = Integer.parseInt(args[2]);
			seats_num = Integer.parseInt(args[3]);
		}
		else if (args[1].compareTo("little")==0) {
			agents_num = 3;
			seats_num = 5;
		}
		else if (args[1].compareTo("average")==0) {
			agents_num = 5;
			seats_num = 10;
		}
		else if (args[1].compareTo("lot")==0) {
			agents_num = 10;
			seats_num = 20;
		}
		else 
			System.out.println("You have entered a wrong concurrency parameter.\nThe parameters are little, average, lot.");


	}



	public static void main(String[] args) throws IOException{
//		long start, end;
//		start = System.nanoTime(); //start timestamp
//		if(( args.length != 2 ) && ( args.length != 4 )) {
//
//			System.out.println("You have not entered enough arguments.");
//			System.exit(0);
//		}
//		get_input(args);
		agents_num = 2;
		seats_num  = 4;

		agents = new TravelAgent[agents_num];
		seats = new Seat[seats_num];

		// fill seats array
		for( int i = 0; i < seats_num; i++){
				seats[i] = new Seat();
		}

		// fill thread (agents) array
		for( int i = 0; i < agents_num; i++)
			agents[i] = new TravelAgent(i);

		for( int i = 0; i < agents_num; i++)
			agents[i].start();

		// wait for threads (agents) to finish
		for( int i = 0; i < agents_num; i++){
			try{
				agents[i].join();
			}
			catch(InterruptedException e){
			}
		}

		//** CHANGE{
		/*int threadSum = 0;
		for( int i = 0; i < agents_num; ++i){
			threadSum += agents[i].localSum;				
		}

		if(threadSum!=bugSumVal){
			try {
				System.out.println("Bug : "+threadSum+" != "+bugSumVal);
				throw new Exception();
			} catch (Exception e) {
				"Crashed_with".equals(e);
				//e.printStackTrace();
			}
		}
		else{
			System.out.println("<TicketsOrderSim, All Tickets were sold properly, None ("+threadSum+" == "+bugSumVal+")>\n");
		}*/
		//** CHANGE}

		assert(bug_occured == false);
		if (bug_occured == false) {
			System.out.println("<TicketsOrderSim, All Tickets were sold properly, None>\n");
			// out.write("<TicketsOrderSim, All Tickets were sold properly, No Bug Happened>\n");
		}
		else {
			fail();
			System.err.println("Bug Happened  "+ bug_count+"  Times");
			//out.write("<TicketsOrderSim, "+bug_count+" Tickets were used without being initialized, Double Checked Locking - Partial initialization>\n");
		}
		
//		end = System.nanoTime(); //** end timestamp
//		double time = (((double)(end - start)/1000000000));
//		System.out.println("\nEXECUTION TIME: "+time+"s");
		//out.close();



	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			TicketsOrderSim.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}


