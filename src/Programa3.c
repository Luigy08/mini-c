int miFuncion(int a, int b);

int main(){
    int i,x,b;
    x = 0;
    b=miFuncion(x,3);
    printf(b);
    return 0;
}

int miFuncion(int x, int d){
   x=d*2;
   return x;
}