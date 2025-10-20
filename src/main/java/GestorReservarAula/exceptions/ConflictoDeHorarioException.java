package GestorReservarAula.exceptions;

//Excepcion personalizada que maneja conflictos de horarios
public class ConflictoDeHorarioException extends Exception{
    
    public ConflictoDeHorarioException(String mensaje){
        super(mensaje);
    }
}
