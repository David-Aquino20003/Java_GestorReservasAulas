package GestorReservarAula.models;

import GestorReservarAula.exceptions.ValidacionDeReservaException;
import java.time.LocalDate;
import java.time.LocalTime;
import GestorReservarAula.util.TipoAula;

/*Clase para reserva de clase
Aplica reglas: solo puede en aulas teoricas o laboratorio*/
public class ReservaClase extends Reserva {
    private String materia;
    private int numEstudiantes;

    //Constructor
    public ReservaClase(String idReserva, Aula aula, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String responsable,
                        String materia, int numEstudiantes) {
        super(idReserva, aula, fecha, horaInicio, horaFin, responsable);
        this.materia = materia;
        this.numEstudiantes = numEstudiantes;
    }

    //Regla de validación específica: Solo TEORICA o LABORATORIO, y capacidad.
    @Override
    public void validarReglasEspecificas() throws ValidacionDeReservaException {
        TipoAula tipo = this.getAula().getTipo();
        if (tipo.equals(TipoAula.AUDITORIO)) {
            throw new ValidacionDeReservaException("Las clases solo pueden reservarse en aulas TEORICAS o LABORATORIO.");
        }
        if (this.numEstudiantes > this.getAula().getCapacidad()) {
             throw new ValidacionDeReservaException(String.format("Capacidad insuficiente. Aula '%s' tiene %d, requeridos %d.",
                this.getAula().getCodigo(), this.getAula().getCapacidad(), this.numEstudiantes));
        }
    }

    //Metodo para la persistencia, será sobrescrito por las subclases para incluir sus datos especificos
    @Override
    public String toCsvString() {
        return String.format("CLASE,%s,%s,%s,%s,%s,%s,%s,%s,%d",
                getIdReserva(), getAula().getCodigo(), getFecha(), getHoraInicio(), getHoraFin(), getResponsable(), getEstado(),
                materia, numEstudiantes);
    }
}