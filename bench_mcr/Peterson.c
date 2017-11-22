#include <threads.h>
#include <stdbool.h>
#include <assert.h>
#include "libinterface.h"

#define COUNT 10
#define NULL 0
//global
int flag1;
int flag2;
int turn;

int x;


void p0(void *p) {
    int count = 0;

    flag1 = 1;
    turn = 1;

    int _flag2 = load_32(&flag2);
    int _turn = load_32(&turn);

    while (flag2 ==1 && turn == 1){
        if (count++>COUNT)      
        {
            /* code */
            break;
        }
    }
    //critical 
    x == 1;

    if (x != 1){
        printf("atomicity violation\n");
    }

    flag1 = 0;

}

void p1(void *p) {

    int count = 0;

    flag2 = 1;
    turn =  0;

    while(flag1 == 1 && turn ==1){
        if (count++ > COUNT)
        {
            /* code */
            break;
        }
    }

    x == 2;

    if (x != 2){
        printf("atomicity violation\n");
    }

    flag2 = 0;

}

int user_main(int argc, char **argv)
{
    pthread_t a, b;
    
    flag1 = 0;
    flag2 = 0;
    turn = 0;
    x = 0;

    pthread_create(&a, NULL, p0, NULL);
    pthread_create(&b, NULL, p1, NULL);

    pthread_join(a);
    pthread_join(b);

    return 0;
}