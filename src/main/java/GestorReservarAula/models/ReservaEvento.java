package GestorReservarAula.models;

import GestorReservarAula.exceptions.ValidacionDeReservaException;
import java.time.LocalDate;
import java.time.LocalTime;
import GestorReservarAula.util.TipoAula;
import GestorReservarAula.util.TipoEvento;

/*Clase para reserva de Evento
Aplica reglas: depende del tipoEvento y la capacidad*/
public class ReservaEvento extends Reserva{
    private TipoEvento tipoEvento;
    private int asistentesEsperados;

    public ReservaEvento(String idReserva, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable, TipoEvento tipoEvento, int asistentesEsperados){
        super(idReserva, aula, fecha, horaInicio, horaFin, responsable);
        this.tipoEvento = tipoEvento;
        this.asistentesEsperados = asistentesEsperados;
    }

    //Regla especifica para ReservaEvento
    @Override
    public void validarReglasEspecificas() throws ValidacionDeReservaException {
        TipoAula tipoAula = this.getAula().getTipo();
        //Conferencias y talleres requieren un auditorio o laboratorio
        if ((tipoEvento.equals(TipoEvento.CONFERENCIA) || tipoEvento.equals(TipoEvento.TALLER)) 
        && !(tipoAula.equals(TipoAula.AUDITORIO) || tipoAula.equals(TipoAula.LABORATORIO))){
            throw new ValidacionDeReservaException("Conferencias o Talleres solo pueden reservarse en aulas de tipo Auditorio o Laboratorio.");
        }
        //La capacidad del aula debe ser suficiente
        if (this.asistentesEsperados > this.getAula().getCapacidad()){
            throw new ValidacionDeReservaException(String.format("Capacidad insuficiente. Aula '%s' tiene %d, requeridos %d.",
            this.getAula().getCodigo(), this.getAula().getCapacidad(), this.asistentesEsperados));
        }
    }

    //Getters
    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }
    public int getAsistentesEsperados() {
        return asistentesEsperados;
    }

    //Persistencia
    @Override
    public String toCsvString() {
        return String.format("Evento,%s,%s,%s,%s,%s,%s,%s,%d", getIdReserva(), getAula().getCodigo(), 
            getFecha(), getHoraInicio(), getHoraFin(), getResponsable(), tipoEvento, asistentesEsperados);
    }
}
