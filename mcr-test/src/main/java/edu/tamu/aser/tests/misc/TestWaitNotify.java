package edu.tamu.aser.tests.misc;

/****************************MAIN**************************/
import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class TestWaitNotify {

  @Test
  public void testBoundedBuffer() throws Exception {

	 BufferImpl buf = new BufferImpl();
     MyProducer p1 = new MyProducer(buf);
     MyProducer p2 = new MyProducer(buf);
     
     MyConsumer c1 = new MyConsumer(buf);
     MyConsumer c2 = new MyConsumer(buf);
     
     p1.start();
     p2.start();
     c1.start();
     c2.start();
     
     p1.join();
     p2.join();
     c1.join();
     c2.join();
     
//     System.out.println(buf.getCount());

  }
}

/*********************BUFFER*****************************/

class BufferImpl{
    protected static int count= 0;
//    final protected int size;

    public BufferImpl() {
//        this.size = size;
        count = 0;
    }
    
    public int getCount(){
    	return count;
    }

    public synchronized void put() throws InterruptedException {
        while (count == 2){
           wait();
        }
        ++count;
        notify();                                                                                                                                                                  
    }

    public synchronized void get() throws InterruptedException {
    	while (count == 0)
           wait();
    	--count;
    	notify();                                                                                                                                                                
    }
    
    public void setCount(int v) {
		count = v;
	}
}

class MyConsumer extends Thread {

    BufferImpl buf;

    MyConsumer(BufferImpl b) {buf = b;}

    public void run() {
      try {                                                                                                                                      
	    buf.get();                                                                                                                                                                                   
      } catch(InterruptedException e ){
    	  e.printStackTrace();
      }
    }
}

class MyProducer extends Thread {
    BufferImpl buf;

    MyProducer(BufferImpl b) {
      buf = b;
    }

    public void run() {
      try {	                                                                                                                                          
           buf.put();                                                                                                                                                                                           
      } catch (InterruptedException e){}
    }
}

