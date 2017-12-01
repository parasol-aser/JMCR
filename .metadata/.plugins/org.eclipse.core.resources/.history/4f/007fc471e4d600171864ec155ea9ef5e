//
// -Class NewThread-
// One separate iteration of sorting process of bubble sort
//

package omcr.loader;

class NewThread extends Thread{
	static int array[];
	int fin;            //array will sort until fin
	static int priority;
	static boolean endd;//boolean variable which shows
	//that sorting process was finish

	public NewThread(int finish){ //C'tor
		fin=finish;
		setPriority(priority);
		if(fin>0){
			endd=false;
		}else{
			endd=true;
		}

		/*try {
       this.sleep(0,1);
      }
   catch (InterruptedException e){}*/
	}

	private static synchronized void swpArray(int i){
		int temp;
		temp=array[i+1];
		//System.out.println("before-> array["+i+"]: "+array[i]+"\tarray["+(i+1)+"]: "+array[i+1]);
		array[i+1]=array[i];
		array[i]=temp;
		//System.out.println(" after-> array["+i+"]: "+array[i]+"\tarray["+(i+1)+"]: "+array[i+1]);
	}


	public void run(){
		int i;

		for(i=0;i<fin;i++){
			if(array[i]>array[i+1]){
				swpArray(i);
			}
			if(i==0){  //start of the next iteration
				NewThread ntt=new NewThread(fin-1);
				ntt.start();
			}
		}
	}
}
