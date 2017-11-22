#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

/* atomic */ int flag0, flag1;
/* atomic */ int turn;

#define true 1
#define false 0
#define NULL 0

#define N1 4
#define N2 4

int var;

void p0(void *p) {
	
	flag0 = 1;
	int n1 = 0;
	int n2 = 0;

	while (flag1 ==1)
	{
		if(n2++ >N2){
			break;
		}
		if (turn != 0)
		{
			flag0 = 0;
			while(turn != 0) {
				if(n1++ > N1){
					break;
				}
			}
			flag0 = 1;
		}
	}

	// std::atomic_thread_fence(std::memory_order_acquire);

	// critical section
	var = 1;

	if (var != 1){
		printf("atomicity violation\n");
	}

	turn = 1;
	flag0 = 0;
}

void p1(void *p) {

	flag1 = 1;
	int n1 = 0;
	int n2 = 0;

	while (flag0 ==1)
	{
		if(n2++ >N2){
			break;
		}
		if (turn != 1)
		{
			flag1 = 0;
			while(turn != 1) {
				if(n1++ > N1){
					break;
				}
			}
			flag1 = 1;
		}
	}

	// critical section
	var = 2;

	if (var != 2){
		printf("atomicity violation\n");
	}


	turn = 0;
	flag1 = 0;
}

int user_main(int argc, char **argv)
{
	pthread_t a, b;

	flag0 = 0;

	flag1 = 0;

	turn = 0;
	var  =0;

	for (int i = 0; i < NUM; ++i)
	{
		enter[i] = 0;
		number[i] = 0;

	}

	pthread_create(&a, NULL, p0, NULL);
	pthread_create(&b, NULL, p1, NULL);

	pthread_join(a);
	pthread_join(b);

	return 0;
}
