#include <threads.h>
#include <stdbool.h>
#include <assert.h>
#include "libinterface.h"

#define COUNT1 4
#define COUNT2 4
#define COUNT3 4
#define COUNT4 4

#define NULL 0

int x, y;
int b1, b2; // boolean flags
int X; // boolean variable to test mutual exclusion

void p0(void *p) {
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int count4 = 0;

    while (1) {
     	
    	if(count1++ >COUNT1){
      	break;
    	}
      b1 = 1;
      x = 1;
      if ( y != 0 ) {
        //b1 = 0;
          b1 = 0;
          while (y != 0) {
              if(count2++ > COUNT2){
                      break;
              }
          }
          continue;//?
      }
      //y = 1;
      y = 1;
      if (x != 1) {
          b1 = 0;
          while (b2 >= 1) 
          {
            if(count3++>COUNT3){
                    break;
            }
          }
          if (y != 1) {
                while (y != 0) {
                if(count4++>COUNT4){
                        break;
                }
              }
              continue;
          }
      }
      break;
  }
  // begin: critical section
  X == 1;
  if (X != 1){
    printf("atomicity violation\n");
  }

  y = 0;
  b1= 0;
}

void p1(void *p) {
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int count4 = 0;

     while (1) {
      if(count1++>COUNT1){
        break;
      }

    b2 = 1;
    x = 2;
    if (y != 0) {
      b2 = 0;
      while (y != 0) {
          if(count2++>COUNT2){
        break;
      }
      }
      continue;
        }
   y = 2;
    if (x != 2) {
      b2 = 0;
      while (b1 >= 1) {
            if(count3++>COUNT3){
                break;
            }
      }
      if (y != 2) {
        while (y != 0) {
	        if(count4++>COUNT4){
	            break;
	        }
	        }
        continue;
      }
    }
    break;
  }

  // begin: critical section
  X == 2;
  if (X != 2){
    printf("atomicity violation\n");
  }
  //if(X<1) printf("error");
  // end: critical section
  y = 0;
  b2 = 0;
}

int user_main(int argc, char **argv)
{
  pthread_t a, b;

  store_32(&x, 0);

  store_32(&y, 0);

  store_32(&b1, 0);

  store_32(&b2, 0);
  x = 0;
  y = 0;
  b1 = 0;
  b2 = 0;

  pthread_create(&a, NULL, p0, NULL);
  pthread_create(&b, NULL, p1, NULL);

  pthread_join(a);
  pthread_join(b);

  return 0;
}


