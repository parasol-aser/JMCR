package edu.tamu.aser.rvtest.buffer;

/****************************MAIN**************************/
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.tamu.aser.reexcution.JUnit4MCRRunner;

import static org.junit.Assert.*;

@RunWith(JUnit4MCRRunner.class)
public class BufferTest {
  static int SIZE = 2; /* parameter */
  static int PRODS = 2; /* parameter */
  static int CONS = 2; /* parameter */
  static BufferImpl buf;
  
  @Test
  public void testBoundedBuffer() throws Exception {
     buf = new BufferImpl(SIZE);

     final Producer[] producers = new Producer[PRODS];
     final Consumer[] consumers = new Consumer[CONS];

     for (int i=0; i<PRODS; i++) {
         producers[i] = new Producer(buf);
         producers[i].start();
     }
     for (int i=0; i<CONS; i++) {
         consumers[i] = new Consumer(buf);
         consumers[i].start();
     }

     for (int i=0; i<PRODS; i++) {
         producers[i].join();
     }
     for (int i=0; i<CONS; i++) {
         consumers[i].join();
     }
     
//     Producer p1 = new Producer(buf, MODCOUNT);
//     Producer p2 = new Producer(buf, MODCOUNT);
//     Consumer c1 = new Consumer(buf);
//     Consumer c2 = new Consumer(buf);
//     
//     p1.start();
//     p2.start();
//     c1.start();
//     c2.start();
//     
//     p1.join();
//     p2.join();
//     c1.join();
//     c2.join();
     
  }
}

