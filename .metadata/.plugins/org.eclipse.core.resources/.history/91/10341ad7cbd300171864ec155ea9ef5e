package edu.tamu.aser.rvtest.omcr;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class Critical {
	public int turn;
	
	public static void main(String[] args){
		Thread t1, t2;

		Critical c = new Critical();
		Section s1 = new Section(c, 0);
		Section s2 = new Section(c, 1);

		t1 = new Thread(s1);
		t1.start();

		t2 = new Thread(s2);
		t2.start();

		try {
			t1.join();
		}
		catch ( InterruptedException e ) {}

		try {
			t2.join();
		}
		catch ( InterruptedException e ) {}
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			Critical.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}

class Section implements Runnable {

	Critical t;
	int threadNumber;
	public boolean bug;

	public Section(Critical t, int threadNumber)
	{
		this.t = t;
		this.threadNumber = threadNumber;
	}

	public void run() {
		bug = false;
		if(threadNumber == 0)
		{
			t.turn = 0;
			System.out.println("["+Thread.currentThread().getName()+"] In critical section, thread number = " + threadNumber);
			while(t.turn != 0)
			{
				if(t.turn == 0)
					bug = true;
			}
			System.out.println("["+Thread.currentThread().getName()+"] Out critical section, thread number = " + threadNumber);
			t.turn = 1;
			
			 assert(!bug);
		}
		else
		{
			if(threadNumber == 1)
			{
				t.turn = 1;
				System.out.println("["+Thread.currentThread().getName()+"] In critical section, thread number = " + threadNumber);
				while(t.turn != 1);
				System.out.println("["+Thread.currentThread().getName()+"] Out critical section, thread number = " + threadNumber);
				t.turn = 0;          
			}
			else
			{
				System.err.println("This algorithm only supports two threads");
			}
		}
	}
}






























