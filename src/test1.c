	int x, res;
int sumaCuadrados(int n);
void main() {
  printf("Ingrese un numero: ");
	scanf(x);
	res = sumaCuadrados(x);
  printf(res);
  return 0;
}
int sumaCuadrados(int n) {
	if(n==0) {
		return 1;
	}
	else {
		return n*n + sumaCuadrados(n - 1);
	}
}