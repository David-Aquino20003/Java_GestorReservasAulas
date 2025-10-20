package GestorReservarAula.models;

import GestorReservarAula.util.TipoAula;

//clase para modelar un aula
public class Aula {
    private String codigo;
    private String nombre;
    private int capacidad;
    private TipoAula tipo; //Uso de TipoAula

    //Constructor
    public Aula(String codigo, String nombre, int capacidad, TipoAula tipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    //Contructor con el proposito de dar soporte a la persistencia de datos
    public Aula(String codigo){
        this.codigo = codigo;
    }

    //Getters
    public String getCodigo() {
        return codigo;
    }
    public String getNombre() {
        return nombre;
    }
    public int getCapacidad() {
        return capacidad;
    }
    public TipoAula getTipo() {
        return tipo;
    }

    //Setters
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    public void setTipo(TipoAula tipo) {
        this.tipo = tipo;
    }
    
    //Metodo para simplificar la representacion en listados
    @Override
    public String toString(){
        return String.format("Aula [Codigo: %s, Nombre: %s, Capacidad: %d, Tipo: %s]", codigo, nombre, capacidad, tipo);
    }
    //Metodo para la persistencia en formato CSV/TXT
    public String toCsvString(){
        return String.format("%s,%s,%d,%s", codigo, nombre, capacidad, tipo);
    }  
}