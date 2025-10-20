package GestorReservarAula.models;

import GestorReservarAula.interfaces.Validable;
import java.time.LocalDate;
import java.time.LocalTime;

/*Clase abstracta para todas las reservas
Implementa la interfaz validable y contiene atributos comunes*/
public abstract class Reserva implements Validable{
    private String idReserva;
    private Aula aula;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String responsable;
    private String estado; //Control de estado: activa, cancelada

    //Constructor
    public Reserva(String idReserva, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable) {
        this.idReserva = idReserva;
        this.aula = aula;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.responsable = responsable;
        this.estado = "activa"; //Por defecto es activa
    }

    //Getters
    public String getIdReserva() {
        return idReserva;
    }
    public Aula getAula() {
        return aula;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    public LocalTime getHoraFin() {
        return horaFin;
    }
    public String getResponsable() {
        return responsable;
    }
    public String getEstado() {
        return estado;
    }
    
    //Setters
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    //Metodo para la persistencia, sera sobrescrito por las subclases para incluir sus datos especificos
    public abstract String toCsvString();

    //Metodo general de la reserva para mostrar sus datos
    @Override
    public String toString() {
        return String.format("Reserva [ID: %s, Aula: %s, Fecha: %s, Horario: %s-%s, Resp: %s, Estado: %s]",
        idReserva, aula, fecha, horaInicio, horaFin, responsable, estado);
    }
}
