package omcr.manager;

class TmemoryHandler extends Thread
{

    public void run()
    {
        while ( Manager.request_counter > 0)
        {
            System.out.println ("Memory Blocks to be released yet: "+Manager.request_counter);
            while ( !Manager.flag )
            {
               // yield();
                try {sleep (10);} catch (Exception e) {}

            }
            Manager.request_counter--;
            Manager.flag=false;
        }
        System.out.println ("\nTmemoryHandler thread finish");
    }
}