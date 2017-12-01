package omcr.garage;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.exploration.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class GarageTest {
	public static void main(String[] args)
	{
		GarageManager bos = new GarageManager();

		bos.GetParametersFromUser(args);
		bos.TakeWorkersFromAgency();
		bos.GiveTasksToWorkers();
	}
	
	@Test
	public void test() throws InterruptedException {
		try {
			GarageTest.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}
