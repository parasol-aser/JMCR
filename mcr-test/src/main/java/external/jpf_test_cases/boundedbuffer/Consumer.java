package external.jpf_test_cases.boundedbuffer;
/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

/********************CONSUMER*******************************/

class Consumer extends Thread {

    Buffer buf;

    Consumer(Buffer b) {buf = b;}

    public void run() {
      try {
        for(int i=0;i<Buffer.N;i++){
            // int tmp = ((Integer)buf.get()).intValue();
            // System.out.println(this+" consumed "+tmp);
            buf.get();
        }
      } catch(InterruptedException e ){}
    }
}



