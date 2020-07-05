import java.util.ArrayList;

public class Cuadruplo {

    public String operador;
    public String argumento1;
    public String argumento2;
    public String respuesta;
    public boolean ponerEtiqueta = false;

    public Cuadruplo(String operador, String argumento1, String argumento2, String respuesta) {
        this.operador = operador;
        this.argumento1 = argumento1;
        this.argumento2 = argumento2;
        this.respuesta = respuesta;
    }

    public Cuadruplo(){
    }

    public String getOperador(){
        return this.operador;
    }

    public void setOperador(String operador){
        this.operador = operador;
    }

    public String getArgumento1(){
        return this.argumento1;
    }

    public void setArgumento1(String argumento1){
        this.argumento1 = argumento1;
    }

    public String getArgumento2(){
        return this.argumento2;
    }

    public void setArgumento2(String argumento2){
        this.argumento2 = argumento2;
    }

    public String getRespuesta(){
        return this.respuesta;
    }

    public void setRespuesta(String respuesta){
        this.respuesta = respuesta;
    }

	public void setPonerEtiqueta(boolean b) {
		ponerEtiqueta = b;
    }

	public boolean getPonerEtiqueta() {
        return ponerEtiqueta; }
}