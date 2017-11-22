#include <threads.h>
#include <stdbool.h>
#include <assert.h>
#include "libinterface.h"

#define LOOP 4
#define NULL 0

//global
int x;
int y;
int z;

int flag;

/* uint32_t */ int var = 0;

void p0() {
	int i;
	for(i=0; i<LOOP; i++){
		store_32(&z, 0);
		store_32(&x, 0);
		store_32(&y, 0);
		store_32(&x, 2);
		store_32(&y, 3);
		store_32(&z, 1);
	}
}

void p1() {
	int i;
	for(i=0; i<LOOP; i++){
		int _z = load_32(&z);

		int b = (_z==1);

		if (b)	
		{
			/* code */
			int _x = load_32(&x);
			int _y = load_32(&y);
			int t = _x+1;
			int b1 = (t==_y);
			if (b1)
			{
				//correct
			}
			else{
				_x = load_32(&x);
				_y = load_32(&y);
				printf("x = %d; y = %d\n", _x, _y);
			}
		}
	}
}

void p0l(void *a) {
	int i;
	for(i=0;i<PROBLEMSIZE;i++) {
		p0();
	}
}


void p1l(void *a) {
	int i;
	for(i=0;i<PROBLEMSIZE;i++) {
		p1();
	}
}


int user_main(int argc, char **argv)
{
	thrd_t a, b;

	store_32(&x, 1);

	store_32(&y, 2);

	store_32(&z, 0);

	// store_32(&flag, 0);

	thrd_create(&a, p0l, NULL);
	thrd_create(&b, p1l, NULL);

	thrd_join(a);
	thrd_join(b);

	return 0;
}
