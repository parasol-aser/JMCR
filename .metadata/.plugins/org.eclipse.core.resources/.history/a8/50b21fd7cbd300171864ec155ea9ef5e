package edu.tamu.aser.rvtest.buffer;
/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

/********************CONSUMER*******************************/

class Consumer extends Thread {

    final BufferImpl buf;

    Consumer(BufferImpl b) {buf = b;}

    public void run() {
      try {
          //        while(true) {                                                                                                                                                                            
//            System.out.println("Consumer trying to get - "+this);                                                                                                                                          
	   buf.get();
            //        }                                                                                                                                                                                      
      } catch(InterruptedException e ){}
    }
}
