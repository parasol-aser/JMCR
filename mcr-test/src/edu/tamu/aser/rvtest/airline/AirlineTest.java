package edu.tamu.aser.rvtest.airline;

import junit.framework.Assert;
//import omcr.airline.airline;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

@RunWith(JUnit4MCRRunner.class)
public class AirlineTest {

    public static void main(String args[]) throws Exception {
        AirlineTest airlineTest = new AirlineTest();
//        airlineTest.test5ThreadsFullInvarient();
//        airlineTest.test5ThreadsNotTooMany();
        airlineTest.test5ThreadsNotTooFew();
//        airlineTest.test3ThreadsFullInvarient();
//        airlineTest.test3ThreadsNotTooMany();
//        airlineTest.test3ThreadsNotTooFew();
//        airlineTest.test2ThreadsFullInvarient();
//        airlineTest.test2ThreadsNotTooMany();
//        airlineTest.test2ThreadsNotTooFew();
    }
    
    // @Test
    public void test2ThreadsFullInvarient() throws Exception {
        makeBookings(2);
        testFullInvarient();
    }

//     @Test
    public void test2ThreadsNotTooMany() throws Exception {
        makeBookings(2);
        testNotTooManyTicketsSold();
    }

    // @Test
    public void test2ThreadsNotTooFew() throws Exception {
        makeBookings(2);
        testNotTooFewTicketsSold();
    }
    
//     @Test
    public void test3ThreadsFullInvarient() throws Exception {
        makeBookings(3);
        testFullInvarient();
    }

    // @Test
    public void test3ThreadsNotTooMany() throws Exception {
        makeBookings(3);
        testNotTooManyTicketsSold();
    }

//    @Test
    public void test3ThreadsNotTooFew() throws Exception {
        makeBookings(3);
        testNotTooFewTicketsSold();
    }

//     @Test
    public void test5ThreadsFullInvarient() throws Exception {
        makeBookings(6);
        testFullInvarient();
    }

    // @Test
    public void test5ThreadsNotTooMany() throws Exception {
        makeBookings(5);
        testNotTooManyTicketsSold();
    }

    //@Test
    public void test5ThreadsNotTooFew() throws Exception {
        makeBookings(6);
        testNotTooFewTicketsSold();
    }

    private Airline airline;

    public void makeBookings(int numTickets) throws Exception {
        airline = new Airline(numTickets);
        airline.makeBookings();
    }

    @SuppressWarnings("deprecation")
	public void testFullInvarient() {
        if (airline.numberOfSeatsSold != airline.maximumCapacity) {
            Assert.fail("Too many or too few tickets were sold! Number of tickets sold: " + airline.numberOfSeatsSold + " out of max: "
                    + airline.maximumCapacity);
        }
    }

    @SuppressWarnings("deprecation")
	public void testNotTooManyTicketsSold() {
        if (airline.numberOfSeatsSold > airline.maximumCapacity) {
            Assert.fail("Too many were sold! Number of tickets sold: " + airline.numberOfSeatsSold + " out of max: " + airline.maximumCapacity);
        }
    }

    @SuppressWarnings("deprecation")
	public void testNotTooFewTicketsSold() {
        if (airline.numberOfSeatsSold < airline.maximumCapacity) {
            Assert.fail("Too few were sold! Number of tickets sold: " + airline.numberOfSeatsSold + " out of max: " + airline.maximumCapacity);
        }
    }
    
    @Test
	public void test() throws InterruptedException {
		try {
			AirlineTest.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}
