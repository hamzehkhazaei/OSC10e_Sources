/**
 * Example code for synch tools.
 * Modified by Hkh
 * Not in the OSC book
 */

#include<stdio.h>
#include<string.h>
#include<pthread.h>
#include<stdlib.h>
#include<unistd.h>

pthread_t tid[2];
int counter;

int flag[2]={0,0};
int turn;

void* doSomeThing(void *arg)
{
    int no = (int)(long)arg;
    if (no == 0){
        flag[0] = 1;
        turn = 1; // give the turn to thread 1
        while((flag[1] == 1) && turn == 1);
    }else{
        flag[1] = 1;
        turn = 0; // give the turn to thread 0
        while((flag[0] == 1) && turn == 0);
    }
    
    // enter critical section
    unsigned long i = 0;
    counter += 1;
    printf("\n Job %d started\n", counter);
    for(i=0; i<(0xFFFFFFFF);i++);
    printf("\n Job %d finished\n", counter);
    
    flag[no] = 0;
    // exit critical section
    return NULL;
}

int main(void)
{
    int i = 0;
    int err;

    while(i < 2)
    {
        err = pthread_create(&(tid[i]), NULL, &doSomeThing, (void *)(long)i);
        if (err != 0)
            printf("\ncan't create thread :[%s]", strerror(err));
        i++;
    }

    pthread_join(tid[0], NULL);
    pthread_join(tid[1], NULL);

    return 0;
}