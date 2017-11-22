#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define COUNT 1
#define NULL 0

#define N1 10
#define N2 10
#define NUM 2

//global
int x;
int enter[NUM];
int number[NUM];

int max(){
	int max = 0;
	int i;
	for (i = 0; i < NUM; ++i)
	{
		/* code */
		int m = number[i];
		int b = (m>max);
		if (b)
		{
			max = m;
		}
	}
}

void lock(int i){
	int n1 = 0;
	int n2 = 0;

	enter[i] = 1;
	number[i] = 1+max();
	enter[i] = 0;

	int j;
	for (j = 0; j < NUM; ++j)
	{
		while(enter[j] == 1){
			if(n1++ > N1){
				break;
			}
		}

		while(number[j] !=0 && number[j] < number[i]){
			int num_j = number[j];
			if (n2++ > N2){
				break;
			}
		}
	}
}

void unlock(int i){
	number[i] = 0;
}

void p0(void *p) {
	int i = 0;
	lock(i);

	x == 1;

	if (x != 1){
		printf("atomicity violation\n");
	}

	unlock(i);

}

void p1(void *p) {

	int i = 1;
	lock(i);

	x = 2;

	if (x != 2){
		printf("atomicity violation\n");
	}

	unlock(i);

}


int main(int argc, char **argv)
{
	pthread_t a, b;
	x =0;

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
