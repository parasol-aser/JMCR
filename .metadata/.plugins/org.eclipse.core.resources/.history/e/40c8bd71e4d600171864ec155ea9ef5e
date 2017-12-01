//
//  -Class Loader-
//  Create array,start sort process,verify final array
//


package omcr.loader;

import java.lang.Integer;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;
import edu.tamu.aser.rvtest.tso.lamport;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.RandomAccessFile;

//import bouvlesort.NewThread;
@RunWith(JUnit4MCRRunner.class)
public class Loader {

	static int array[];   //array of integers
	static int n_bugs;
	
	public static void main(String[] args){
//		if(args.length<2) {
//			System.out.println("wrong input");
//			System.exit(1);
//		}
//		String outputFile="tmp.txt";//output file name
//		String conc=args[1]; //concurrency
		int len=10;    //length of array
		int prior=3;   //priority of sorting process
		

		/*if(conc.equals("low"))
			len=200;
		if(conc.equals("medium"))
			len=1000;
		if(conc.equals("high"))
			len=20000;*/



		array=new int[len];
		Thread curTh=Thread.currentThread();
		curTh.setPriority(1);
		NewThread.priority=prior;
		int i;

		for(i=0;i<len;i++){
			array[i]=len-i;
		}


		NewThread.array=array;
		NewThread ntr=new NewThread(len-1);
		ntr.start();

		try {
			while(!NewThread.endd) {
				curTh.sleep(2000);
			}
		}
		catch (InterruptedException e){}

		n_bugs = 0;
		for(i=0;i<len-1;i++){
			if(array[i]>array[i+1]) {
				n_bugs++;
			}
		}
		String outString="";
		try{
			assert(n_bugs==0);
			if(n_bugs==0) {
				outString+="[None]";
			}
			else{
				outString+="finished with "+n_bugs+" bugs <Initialization-Sleep Pattern>";
				//RandomAccessFile outFile=new RandomAccessFile(outputFile,"rw");//create new file
				//outFile.writeBytes("SortProgram "+outString);
				System.out.println(outString);
				throw new Exception();
			}

			//RandomAccessFile outFile=new RandomAccessFile(outputFile,"rw");//create new file
			//outFile.writeBytes("SortProgram "+outString);
			System.out.println(outString);
		}
		catch (IOException e){
			System.out.println(""+e);
		}
		catch(Exception e)
		{
			"Crashed_with".equals(e);
		}

	}
	
	@Test
	public void test() throws InterruptedException {
		try {
		
		Loader.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}