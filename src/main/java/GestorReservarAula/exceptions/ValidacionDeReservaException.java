package GestorReservarAula.exceptions;

//Excepcion personalizada que maneja validaciones funcionales y asegurar mensajes descriptivos del usuario
public class ValidacionDeReservaException extends Exception{
    
    public ValidacionDeReservaException(String mensaje){
        super(mensaje);
    }
}
