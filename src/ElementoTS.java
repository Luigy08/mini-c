
import java.util.ArrayList;

public class ElementoTS{

    public String ID;
    public String tipo;
    public String ambito;
    public int Bloque; 
    public ArrayList<ElementoTS> parametros = new ArrayList<ElementoTS>();
    public ArrayList<ElementoTS> recordVars = new ArrayList<ElementoTS>();

    public ElementoTS() {
    }

    public void addParametro(ElementoTS par) {
        parametros.add(par);
    }
    

    public String getID() {
        return this.ID;
    }

    public void setID(String id) {
        this.ID = id;
    }

    public ArrayList<ElementoTS> getParametros() {
        return parametros;
    }

    public void setParametros(ArrayList<ElementoTS> parametros) {
        this.parametros = parametros;
    }

    public ArrayList<ElementoTS> getRecordVars() {
        return recordVars;
    }

    public void setRecordVars(ArrayList<ElementoTS> RecordVars) {
        this.recordVars = RecordVars;
    }

    public void addRecordVar(ElementoTS elemento) {
        this.recordVars.add(elemento);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public int getBloque() {
        return Bloque;
    }

    public void setBloque(int Bloque) {
        this.Bloque = Bloque;
    }

}