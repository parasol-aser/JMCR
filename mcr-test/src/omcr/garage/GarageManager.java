package omcr.garage;

import java.io.FileReader;
import java.io.BufferedReader;

public class GarageManager
{
	public final static String AGENCY_FILE = "newWorkers.txt";
	public final static int NUMBER_OF_WORKERS = 5;
	public final static int MAX_NAME_LENGTH = 10;

	private String[] workersNames = new String[NUMBER_OF_WORKERS];
	private boolean foreignWorkersHired = false;
	//private java.io.BufferedWriter outputFile;
	private String strBugProbability = "littile";
	public int iBugProbability = 200;
	public static int printedCard = 0;
	
	private  boolean bugOccured = false;

	protected GarageStatus status;




	public void TakeWorkersFromAgency()
	{
		FileReader file;
		/*try
        { // Try to open a file with workers names.
            file = new FileReader(AGENCY_FILE);
        }
        catch(java.io.FileNotFoundException error)
        { // No such file exist so hire foreign workers.
            System.out.println("Workers names File Not Found");
            HireForeignWorkers();
            return;
        }
        BufferedReader fileInput = new BufferedReader(file);

         // Try to get the workers names but if something goes wrong
         // than hire the foreign workers..
        if(!GetWorkersNames(fileInput))*/
		HireForeignWorkers();

		/*try
        { // Try to close the file.
            fileInput.close();
        }
        catch(java.io.IOException error)
        { // Close operation failed - hire the foreign workers.
            System.out.println("Error closing workers names File");
             HireForeignWorkers();
            return;
        }*/
	}




	protected boolean GetWorkersNames(BufferedReader fileInput)
	{
		String text = "";
		int numWorkers = 0;
		while(text != null && numWorkers < NUMBER_OF_WORKERS)
		{ // Take more workers until we have enough or there aren't enough workers.
			try
			{ // Try to get the next worker name and accept him if he is appropriate..
				text = fileInput.readLine();

				if(text != null && text.length() != 0 && text.length() <= MAX_NAME_LENGTH)
					workersNames[numWorkers++] = text;
			}
			catch (java.io.IOException error)
			{
				System.out.println("Error reading workers names File");
				return false;
			}
		}
		if(numWorkers != NUMBER_OF_WORKERS)
		{// If we didn't get enough appropriate workers.
			System.out.println("Not Enough appropriate workers found in agency");
			return false;
		}

		System.out.println("Taking workers From Agency :");
		PrintWorkersNames();
		return true;
	}




	protected void HireForeignWorkers()
	{
		if(!foreignWorkersHired)
		{
			//System.out.println("Hiring foreign workers :");

			// Take the default workers.
			for(int i = 0; i < NUMBER_OF_WORKERS; i++)
				workersNames[i] = "Chong"  + (i + 1);

			foreignWorkersHired = true;
			//PrintWorkersNames();
		}
	}




	protected void PrintWorkersNames()
	{
		for(int i = 0; i < NUMBER_OF_WORKERS; i++)
			System.out.println(workersNames[i]);

		System.out.println();
	}





	public void GiveTasksToWorkers()
	{
		// Compute task number for each worker.
		int[] taskNumber = new int[NUMBER_OF_WORKERS];
		for(int i = 0; i < NUMBER_OF_WORKERS; i++)
			taskNumber[i] = i;//SetTaskToWorker(workersNames[i]); 

		status  =  new GarageStatus(NUMBER_OF_WORKERS);
		GarageWorker[] threads = new GarageWorker[NUMBER_OF_WORKERS + 1];
		for(int i = 0; i < NUMBER_OF_WORKERS + 1; i++)
		{ // Start threads for workers and one for boss.
			if(i == NUMBER_OF_WORKERS)
				threads[i] = new GarageWorker("Bos",0,true,i);
			else
				threads[i] = new GarageWorker(workersNames[i], taskNumber[i], false,i);

			threads[i].start();
		}

		for (int i = 0; i < NUMBER_OF_WORKERS + 1; i++)
		{ // Wait for all threads to end.
			try
			{
				threads[i].join();
			} catch (InterruptedException error)
			{
				error.getMessage();
			}
		}
		assert(GarageManager.printedCard == NUMBER_OF_WORKERS);
	}






	protected int SetTaskToWorker(String workerName)
	{
		// Task number is depended on the bos decision and the worker name.
		int bosFactor =  (int) (Math.random() * iBugProbability);
		int workerNameLen =  workerName.length();

		if(workerNameLen < 2)
			bosFactor =  0;
		else if(workerNameLen <= bosFactor)
			bosFactor = (int)((workerNameLen / 3) * 2);

		// We cut bosFactor chars from worker name and get it's hash code.
		return workerName.substring(bosFactor).hashCode();
	}





	public void GetParametersFromUser(String[] args)
	{
		if(args == null)
			return;

		/*if(args.length > 0)
            outputFile = OpenOutputFile(args[0]);
        else
            outputFile = OpenOutputFile("Output.txt");*/

		if((args.length >1))
			strBugProbability = args[1];

		AdjustBugProbability();
	}





	public java.io.BufferedWriter OpenOutputFile(String fileName)
	{
		java.io.BufferedWriter fileOutput;
		try
		{ // Try open output file.
			fileOutput = new java.io.BufferedWriter(new java.io.FileWriter(fileName));
			return fileOutput;
		}
		catch(java.io.IOException error)
		{ // open action failed.
			System.out.println("Exception - Output File Can't be opened !!!");
			System.exit(1);
		}
		// Never reach here !.
		return null;
	}





	void  AdjustBugProbability()
	{
		try
		{
			if(strBugProbability.compareTo("little") == 0)
			{
				iBugProbability = 250;
				//System.out.println("Bug Probability Is little");
				//outputFile.write("Bug Probability Is little");
			}
			else if(strBugProbability.compareTo("average") == 0)
			{
				iBugProbability = 160;
				//System.out.println("Bug Probability Is average");
				//outputFile.write("Bug Probability Is average");
			}
			else if(strBugProbability.compareTo("lot") == 0)
			{
				iBugProbability = 80;
				//System.out.println("Bug Probability Is lot");
				//outputFile.write("Bug Probability Is lot");
			}
			else
			{
				System.out.println("Bug Probability inserted by user is not : little, average, lot :");
				System.out.println("Default bug Probability Is little");

				//outputFile.write("Bug Probability inserted by user is not : little, average, lot :");
				//outputFile.newLine();
				//outputFile.write("Default bug Probability Is little");
			}
			/*outputFile.newLine();
           outputFile.newLine();
           outputFile.flush();*/
		}
		catch(Exception error)
		{
			System.out.println(error.getMessage());
			System.exit(1);
		}

		System.out.println();
	}





	class GarageStatus
	{ // Class that manage the garage workers status.
		// This class will be synchronized in the threads.
		private int workersInTask;
		private  boolean  managerArrived = false;
		public int[] workersTaskLength = new int[NUMBER_OF_WORKERS];

		public  GarageStatus(int numOfWorkers)
		{
			workersInTask = numOfWorkers;
		}

		public void  ManagerArrived()
		{
			managerArrived = true;
		}

		public boolean IsManagerArrived()
		{
			return managerArrived;
		}

		public void WorkerFinishedTask()
		{
			workersInTask--;
		}

		public boolean AllWorkersFinished()
		{
			return (workersInTask == 0);
		}
	}





	class GarageWorker extends Thread
	{
		private int taskNumber, workerNum;
		private boolean isManager;
		private String workerName;
		int taskTime = 0;
		boolean working = false;



		public GarageWorker(String name, int task, boolean manager, int serial)
		{
			taskNumber  = task;
			isManager     = manager;
			workerName   = name;
			workerNum     = serial;
		}






		public void run()
		{
			if(isManager)
			{ // Only boss thread enters here.
				synchronized(status)
				{ // Anounce that bos arrived.
					System.out.println("Manager arrived !");
					status.ManagerArrived();
				}
				boolean tasksNotFinished = true, printedOutput = false;
			
				while(tasksNotFinished)
				{// Manager wait for workers to complete their tasks.
					// This is the blocking critical section.
					printedOutput = PrintOutput(printedOutput);
					
					//Nuno: added sleep to avoid checking the condition too often
					try{
						System.out.println("task not finished");
					Thread.currentThread().sleep(100);
					}
					catch(Exception e){;}
					//
					
					synchronized(status)
					{ // Check if workers finished.
						if(status.AllWorkersFinished())
							tasksNotFinished = false;
						else
							yield();
					}
				}

				int doneWaiting = 0; //Nuno: added this to avoid having the bug as a deadlock
				// After all workers printed cards - print output.
				while(!AllPrintedCard() && doneWaiting < 3){
					//Nuno: added sleep to avoid checking the condition too often
					try{
						System.out.println("!allprintedcard");
					Thread.currentThread().sleep(100);
					}
					catch(Exception e){;}
					//
					doneWaiting++;
				}
				PrintOutput(printedOutput);
			}
			else
			{ 
				//Nuno: added sleep to avoid checking the condition waitformanager too often
				try{
					Thread.currentThread().sleep(100);
				}
				catch(Exception e){;}
				//
				// Workers threads enter here.
				WaitForManager();
				GoToWork();
				PrintCard();
			}
		}





		public synchronized boolean AllPrintedCard()
		{
			return (GarageManager.printedCard == NUMBER_OF_WORKERS);
		}





		public  void PrintCard()
		{
			synchronized(status)
			{
				status.workersTaskLength[workerNum] = taskTime / 200;
			}

			// This is done to recognize that a bug occured.
			// The variable working was added for that purpose.
			if(!working)
				bugOccured = true;

			GarageManager.printedCard++;
		}





		public synchronized boolean PrintOutput(boolean printedOutput)
		{
			try
			{
				System.out.println("Printed cards: "+GarageManager.printedCard+" ; Num Workers: "+NUMBER_OF_WORKERS);
				if(!printedOutput && GarageManager.printedCard == NUMBER_OF_WORKERS)
				{
					System.out.print("< GarageManager, (");
					// outputFile.write("< GarageManager, (");
					for(int i = 0; i < NUMBER_OF_WORKERS; i++)
					{
						if(i != NUMBER_OF_WORKERS - 1)
						{
							System.out.print(status.workersTaskLength[i] + ", ");
							//outputFile.write(status.workersTaskLength[i] + ", ");
						}
						else
						{
							System.out.print(status.workersTaskLength[i] + "), ");
							//outputFile.write(status.workersTaskLength[i] + "), ");
						}
					}


					if(bugOccured)
					{
						System.out.print("Blocking-Critical-Section Bug>");
						//outputFile.write("Blocking-Critical-Section Bug>");
					}
					else
					{
						System.out.print("No-Bug>");
						//outputFile.write("No-Bug>");
					}

					System.out.flush();
					System.out.println();
					//outputFile.newLine();

					//outputFile.flush();
					//outputFile.close();
					return true;
				}
			}
			catch(Exception error)
			{
				System.out.print(error.getMessage());
				System.exit(1);
			}
			return printedOutput;
		}







		public void WaitForManager()
		{
			boolean canCont = false;
			while(!canCont)
			{
				synchronized(status)
				{
					if(!status.IsManagerArrived())
					{
						System.out.println("Wait for manager");
						yield();
					}
					else
						if(status.IsManagerArrived())
							canCont = true;
				}
			}
		}





		public void GoToWork()
		{
			switch(taskNumber % 8)
			{
			case 0:
				WorkingOn("Cleaning", 1000);
				break;
			case 1:
			{
				int bosKindness = taskNumber*10;//(int)(Math.random() * 100);
				if(bosKindness > 90)
					WorkingOn("Take a day off.................................", 0);
				else if(bosKindness > 60)
					WorkingOn("Washing cars", 800);
				else
					WorkingOn("Changing tires", 1400);
			}
			break;
			case 2:
				WorkingOn("Fixing engines", 2400);
				break;
			case 3:
				WorkingOn("Answering phones", 1800);
				break;
			case 4:
				WorkingOn("Fixing gears", 2000);
				break;
			case 5:
				WorkingOn("Painting Cars", 1600);
				break;
			case 6:
				WorkingOn("Working on breaks", 2200);
				break;
			case 7:
				WorkingOn("Fixing engines", 2400);
				break;
			default:
				System.out.println("Error - default definition reached, exit ...");
			}
		}





		public void WorkingOn(String task, int time)
		{
			System.out.println(workerName + " is  " + task);
			for(int i = -1;i < time; i++){;}


			taskTime = time;
			working = true;

			synchronized(status)
			{
				status.WorkerFinishedTask();
			}
		}
	}





	
}
