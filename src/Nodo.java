import java.util.ArrayList;

public class Nodo{

    public String EtiquetaV;
    public String EtiquetaF; 
    public String Etiqueta; 
    public ArrayList<Nodo> hijos = new ArrayList<>();
    public String valor;
    public String Mensaje; //mensaje de error
    public String ErrorAcumulado;
    public int Id;
    public boolean error;
    public String lugar; //add lugar a los nodos 
    public int cuad;

    int lineaCuadruplo; //para el marcador

    //listas que se usan en el cuadruplo
    public ArrayList<Integer> listaVerdadera = new ArrayList<>();
    public ArrayList<Integer> listaFalsa = new ArrayList<>();
    public ArrayList<Integer> listaSiguiente = new ArrayList<>();

    public Nodo() {
        this.error = false;
    }

    public void addHijos(Nodo hijo) {
        hijos.add(hijo);
    }
    
    public void setError(boolean err){
        this.error = err;
    }

    public boolean getError(){
        return this.error;
    }

    public String getEtiqueta() {
        return Etiqueta;
    }

    public void setEtiqueta(String Etiqueta) {
        this.Etiqueta = Etiqueta;
    }
    
    public String getEtiquetaV() {
        return EtiquetaV;
    }

    public void setEtiquetaV(String EtiquetaV) {
        this.EtiquetaV = EtiquetaV;
    }

    public String getEtiquetaF() {
        return EtiquetaF;
    }

    public void setEtiquetaF(String EtiquetaF) {
        this.EtiquetaF = EtiquetaF;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public ArrayList<Nodo> getHijos() {
        return hijos;
    }

    public void setHijos(ArrayList<Nodo> hijos) {
        this.hijos = hijos;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getMensaje() {
        return Mensaje;
    }

    public void setMensaje(String msg) {
        this.Mensaje = msg;
    }

    public String getErrorAcumulado() {
        return ErrorAcumulado;
    }

    public void setErrorAcumulado(String ErrorAcumulado) {
        this.ErrorAcumulado = ErrorAcumulado;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    // lista_verdadera
	public void setListaVerdadera(ArrayList<Integer> lv) {
		listaVerdadera = lv;
	}
	public void addListaVerdadera(int n) {
		listaVerdadera.add(n);
	}
	public ArrayList<Integer> getListaVerdadera() { return listaVerdadera; }
	public int getListaV(int i) { return listaVerdadera.get(i); }

	// lista_falsa
	public void setListaFalsa(ArrayList<Integer> lf) {
		listaFalsa = lf;
	}
	public void addListaFalsa(int n) {
		listaFalsa.add(n);
	}
	public ArrayList<Integer> getListaFalsa() { return listaFalsa; }
	public int getListaF(int i) { return listaFalsa.get(i); }

	// cuad
	public void setCuad(int c) {
		cuad = c;
	}
	public int getCuad() { return cuad; }

	// lista_siguiente
	public void setListaSig(ArrayList<Integer> ls) {
		listaSiguiente = ls;
	}
	public void addListaSig(int n) {
		listaSiguiente.add(n);
	}
	public ArrayList<Integer> getListaSig() { return listaSiguiente; }
	public int getListaS(int i) { return listaSiguiente.get(i); }

    
}

