package GestorReservarAula.models;

import java.time.LocalDate;
import java.time.LocalTime;
import GestorReservarAula.util.TipoAula;
import GestorReservarAula.exceptions.ValidacionDeReservaException;

/*Clase para reserva de Practica
Aplica reglas: solo se puede en laboratorios*/
public class ReservaPractica extends Reserva {
    private String descripcionPractica;
    private int numEquipos; //Numero de equipos

    public ReservaPractica(String idReserva, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, 
                            String responsable, String descripcionPractica, int numEquipos){
        super(idReserva, aula, fecha, horaInicio, horaFin, responsable);
        this.descripcionPractica = descripcionPractica;
        this.numEquipos = numEquipos;
    }

    //Regla específica para ReservaPractica
    @Override
    public void validarReglasEspecificas() throws ValidacionDeReservaException {
        TipoAula tipo = this.getAula().getTipo();
        //Una práctica solo puede ser en un Laboratorio.
        if (!tipo.equals(TipoAula.LABORATORIO)) {
            throw new ValidacionDeReservaException("Las practicas solo pueden reservarse en aulas de tipo LABORATORIO.");
        }
        //Se podría añadir una validación sobre la capacidad de equipos si fuera relevante.
    }

    //Getters
    public String getDescripcionPractica() {
        return descripcionPractica;
    }
    public int getNumEquipos() {
        return numEquipos;
    }

    //Persistencia
    @Override
    public String toCsvString() {
        return String.format("Practica,%s,%s,%s,%s,%s,%s,%s,%d", getIdReserva(), getAula().getCodigo(), 
            getFecha(), getHoraInicio(), getHoraFin(), getResponsable(), descripcionPractica, numEquipos);
    }
}
