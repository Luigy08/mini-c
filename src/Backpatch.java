import java.util.ArrayList;

class Backpatch { 

    public Backpatch() {
    }

    public static ArrayList<Integer> crearLista (int indice) {
        ArrayList<Integer> nuevaLista = new ArrayList<Integer>(); 
        nuevaLista.add(indice); 
        return nuevaLista; 
    }

    public static void completa(ArrayList<Integer>listaV, int marcador){
        for(Integer indice : listaV){ 
            TablaCuadruplo.insertarLinea(indice, marcador);
        }
    }

    public static ArrayList<Integer> fusion(ArrayList<Integer> listaV, ArrayList<Integer> listaF){
        ArrayList<Integer> nuevaLista = new ArrayList<Integer>(); 
        for(int i = 0; i < listaV.size(); i++){
            nuevaLista.add(listaV.get(i)); 
        }

        for(int i = 0; i < listaF.size(); i++){
            nuevaLista.add(listaF.get(i)); 
        }

        return nuevaLista; 
    }

}