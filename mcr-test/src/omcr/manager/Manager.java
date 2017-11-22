package omcr.manager;
import static org.junit.Assert.fail;

import java.io.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;


@RunWith(JUnit4MCRRunner.class)
public class Manager
{
	

	static String notes_lock=new String();
	static int num_of_notes_set = 0;
	
	public static int request_counter;
	static int released_counter;
	static boolean flag;
	
	static String req_counter_lock=new String();
	static String rel_counter_lock=new String();


	public Manager(int num_of_threads, int req_counter, int rel_counter, boolean flag)
	{
		
		this.released_counter = rel_counter;
		this.request_counter = req_counter;
		this.flag = flag;
		
		Trelease releasers[] = new Trelease[ num_of_threads ];
		TmemoryHandler t = new TmemoryHandler();
		t.start();
		for ( int i = 0 ; i < num_of_threads ; ++i)
		{
			releasers[i] = new Trelease(i);
			releasers[i].start();
		}
		for ( int i = 0 ; i < num_of_threads ; ++i)
		{
			try
			{
				releasers[i].join();
			}catch( InterruptedException e){ }


		}
		try
		{
			t.join();
		}catch( InterruptedException e){ }

	}

	public static void setNote(int index,boolean op)
	{
		synchronized(Manager.notes_lock)
		{
			if ( op )
			{
				num_of_notes_set++;

			}
			else
			{
				num_of_notes_set--;
			}
		}

	}

	public static boolean isOtherNoteSet()
	{
		synchronized(Manager.notes_lock)
		{
			if (num_of_notes_set == 1)
			{
				return true;
			}
			else return false;
		}

	}
	
	
}