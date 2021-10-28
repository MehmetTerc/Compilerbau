#include <stdio.h>
#define ZAHL 5
int fak(int n){
    int n1=n;
    if (n1<2)
        return 1;
    else
        return n1*fak(n1-1);
}
int main(){
    printf("fak(ZAHL)=%d\n", fak(ZAHL));
}   
