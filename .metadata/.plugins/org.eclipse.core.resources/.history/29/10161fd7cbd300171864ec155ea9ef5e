package edu.tamu.aser.rvtest.buffer;
/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

/*******************PRODUCER************************/

class Producer extends Thread {
    final BufferImpl buf;
//    final int modCount;

    Producer(BufferImpl b) {
      buf = b;
//      modCount = mc;
    }

    public void run() {
      try {
        int tmp = 0;
          //while(true) {                                                                                                                                                                                    
//            System.out.println("Producer trying to put - "+this);                                                                                                                                          
            buf.put(new Integer(tmp));
            //tmp= (tmp+1) % modCount;                                                                                                                                                                       
	    // }                                                                                                                                                                                             
      } catch (InterruptedException e){}
    }
}