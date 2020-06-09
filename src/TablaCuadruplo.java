import java.util.ArrayList;

public class TablaCuadruplo {
    public static ArrayList<Cuadruplo> tablaCuadruplo = new ArrayList<Cuadruplo>(); 
    public static ArrayList<String> Intermedio = new ArrayList<String>();

    public static void insertarCuadruplo (Cuadruplo cuad){
        tablaCuadruplo.add(cuad); 
    }

    public static void insertarArgumentos(int indice, String arg1, String arg2){
        tablaCuadruplo.get(indice).argumento1 = arg1; 
        tablaCuadruplo.get(indice).argumento2 = arg2; 
    }

    public static void insertarLinea(int indice, int etiqueta){
        tablaCuadruplo.get(indice).setRespuesta("_etiq" + etiqueta);
    }

    public static void gen(Object operador, Object arg1, Object arg2, Object res){
        try{
            //Casteando objetos a string
            String op, argumento1, argumento2, respuesta; 

            if(operador == null){
                op = " "; 
            }else{
                op = operador.toString(); 
            }

            if(arg1 == null){
                argumento1 = " "; 
            }else{
                argumento1 = arg1.toString(); 
            }

            if(arg2 == null){
                argumento2 = " "; 
            }else{
                argumento2 = arg2.toString(); 
            }

            if(res == null){
                respuesta = " ";
            }else{
                respuesta = res.toString(); 
            }

            Cuadruplo cuad = new Cuadruplo(op, argumento1, argumento2, respuesta);
            insertarCuadruplo(cuad); 
        }catch(Exception e){
            System.out.println("Error al generar cuadruplo"); 
        }
    }

    public static void llenarDeCodigoIntermedio(){ //llena el arraylist de intermedio y se muestra el resultado
        System.out.println("Codigo Intermedio: "); 
        System.out.println("=============================="); 

        String auxiliar = " "; 
        
        for(Cuadruplo cuad : tablaCuadruplo){
            if(cuad.operador.contains("*")||cuad.operador.contains("+")||cuad.operador.contains("/")||cuad.operador.contains("-")){
                auxiliar = cuad.respuesta + " = " + cuad.argumento1 + " " + cuad.operador + " " + cuad.argumento2; 
                Intermedio.add(auxiliar); 
            }else{
                if(cuad.operador.contains(":=")){
                    auxiliar = cuad.respuesta + " " + cuad.operador + " " + cuad.argumento1; 
                    Intermedio.add(auxiliar); 
                }else{
                    if(cuad.operador.contains("IF")){
                        String operando = cuad.operador.substring(2); //saca el operador
                        auxiliar = "IF " + cuad.argumento1 + " " + operando + " " + cuad.argumento2 + "GOTO " + cuad.respuesta; 
                        Intermedio.add(auxiliar); 
                    }else{
                        if(cuad.operador.contains("GOTO")){ //siguiente salto
                            if(cuad.respuesta != " "){
                                auxiliar = "GOTO " + cuad.respuesta; 
                                Intermedio.add(auxiliar); 
                            }else{
                                auxiliar = "GOTO " + "EXIT";
                                Intermedio.add(auxiliar);  
                            }
                        }else{
                            if(cuad.operador.contains("ETIQ")){
                                if(tablaCuadruplo.indexOf(cuad) != tablaCuadruplo.size() - 1){ //para que no se salga del indice
                                    int siguiente = tablaCuadruplo.indexOf(cuad) + 1; 
                                    if(tablaCuadruplo.get(siguiente).operador.contains("ETIQ")){
                                        System.out.println("\n"); 
                                        auxiliar = cuad.respuesta + ":";
                                        Intermedio.add(auxiliar); 
                                        auxiliar = "GOTO " + tablaCuadruplo.get(siguiente).respuesta; 
                                        Intermedio.add(auxiliar); 
                                        auxiliar = cuad.respuesta + ": GOTO " + tablaCuadruplo.get(siguiente).respuesta; 
                                    }else{
                                        System.out.println("\n"); 
                                        auxiliar = cuad.respuesta + ":"; 
                                        Intermedio.add(auxiliar); 
                                    }
                                }else{
                                auxiliar = cuad.respuesta + ": " + "EXIT"; 
                                Intermedio.add(auxiliar);
                                }
                            }else{
                                auxiliar = cuad.operador + " " + cuad.argumento1 + " " + cuad.argumento2 + " " + cuad.respuesta; 
                                Intermedio.add(auxiliar); 
                            }
                        }
                        System.out.println(auxiliar);
                    }
                } 
            }
        }//cierre de for
        System.out.println("==============================");
    }//cierre de metodo

    public static void imprimirTablaCuadruplo(){
        System.out.println("Tabla de Cuadruplos: ");
        System.out.println("=============================="); 

        for(Cuadruplo cuad : tablaCuadruplo){
            System.out.println(String.format(
                "      " + "| Indice: %d | Operador: %s | Argumento1: %s | Argumento2: %s | Resultado: %s |",
                tablaCuadruplo.indexOf(cuad) + 1, cuad.operador, cuad.argumento1, cuad.argumento2,
                cuad.respuesta));
        }
        System.out.println("=============================="); 
    }

    static public void imprimirArrayListIntermedio() {
        System.out.println("\nArray List Codigo Intermedio:");
        System.out.println("============================================================:");
        for (String indice : Intermedio){
            System.out.println(Intermedio.indexOf(indice)+ " | " + indice.toString());
        }
            
        System.out.println("============================================================:");
    }



}