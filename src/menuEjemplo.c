int saldo, deposito, retiro, opcion;
void main(){
  opcion=1;
  printf("Introduzca saldo inicial: ");
  scanf(saldo);
  while(opcion != 5){
    printf("===Bienvenido al banco===");
    printf("\nElija una opcion");
    printf("\n1. Depositar");
    printf("\n2. Retirar");
    printf("\n3. Ver saldo");
    printf("\n4. Abonar intereses");
    printf("\n5. Salir");
    printf("Su opcion: ");
    scanf(opcion);
    if (opcion == 1){
        printf("Monto a depositar: ");
        scanf(deposito);
        saldo = saldo + deposito;
    }
    if (opcion == 3){
        printf("\nSu saldo es: %d ", saldo);
    }
    if (opcion == 5){
        printf("\nGracias por usar este programa");
    }
  }
  printf("termino");
  return 0;
}