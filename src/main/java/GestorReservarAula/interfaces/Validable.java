package GestorReservarAula.interfaces;

import GestorReservarAula.exceptions.ValidacionDeReservaException;

public interface Validable {
    
    //Metodo que valida las reglas especificas de la reserva
    void validarReglasEspecificas() throws ValidacionDeReservaException;
}
