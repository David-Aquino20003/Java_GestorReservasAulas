package GestorReservarAula.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;

import GestorReservarAula.models.*;
import GestorReservarAula.util.TipoAula;
import GestorReservarAula.util.TipoEvento;
import GestorReservarAula.exceptions.ConflictoDeHorarioException;
import GestorReservarAula.exceptions.ValidacionDeReservaException;
import GestorReservarAula.persistence.GestorArchivos;

//Clase principal de lógica. Usa Streams y ArrayList.
public class GestorReservas {
    private final List<Aula> aulas;
    private final List<Reserva> reservas;
    private int nextReservaId = 1;

    //Constructor que inicia el sistema y carga los datos.
    public GestorReservas() throws ValidacionDeReservaException {
        //Cargar datos
        this.aulas = GestorArchivos.cargarAulas();
        this.reservas = GestorArchivos.cargarReservas(this.aulas);
        
        //Inicializar ID consecutivo
        this.nextReservaId = initializeNextReservaId();
        
        //Guardar datos al inicio (para crear archivos si no existen)
        this.guardarDatos();
    }

    //Inicializa el ID consecutivo. Usa Streams.
    private int initializeNextReservaId() {
        return reservas.stream()
            .map(r -> {
                try {
                    //Intenta parsear el número después de 'R'
                    return Integer.parseInt(r.getIdReserva().substring(1));
                } catch (NumberFormatException e) {
                    //Ignora IDs malformados, usa 0 para no afectar el max
                    return 0; 
                }
            })
            .max(Integer::compare)
            .map(maxId -> maxId + 1)
            .orElse(1);
    }

    //Método para obtener un Aula por su código. Usa Optional.
    private Optional<Aula> getAulaByCodigo(String codigo) {
        return aulas.stream().filter(a -> a.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }
    
    //====================== Gestión de Aulas ======================

    //Método para registrar una nueva aula
    public void registrarAula(String codigo, String nombre, int capacidad, TipoAula tipo) throws ValidacionDeReservaException {
        if (getAulaByCodigo(codigo).isPresent()) {
            throw new ValidacionDeReservaException("Ya existe un aula con el código: " + codigo);
        }
        if (capacidad <= 0) {
             throw new ValidacionDeReservaException("La capacidad debe ser un valor positivo.");
        }
        aulas.add(new Aula(codigo.toUpperCase(), nombre, capacidad, tipo));
        guardarDatos();
    }

    //Método implementado para listar aulas
    public Collection<Aula> listarAulas() {
        return this.aulas;
    }

    //Método implementado para modificar aulas
    public void modificarAula(String codigo, String nuevoNombre, int nuevaCapacidad, TipoAula nuevoTipo) throws ValidacionDeReservaException {
        Aula aula = getAulaByCodigo(codigo)
            .orElseThrow(() -> new ValidacionDeReservaException("Aula no encontrada para modificar: " + codigo));

        if (nuevaCapacidad <= 0) {
            throw new ValidacionDeReservaException("La capacidad debe ser un valor positivo.");
        }
        
        //Aplicar los cambios
        aula.setNombre(nuevoNombre);
        aula.setCapacidad(nuevaCapacidad);
        aula.setTipo(nuevoTipo);
        guardarDatos();
    }

    //========================== Lógica de Validación de Conflicto =======================

    //Valida si la nueva reserva entra en conflicto con las existentes en la misma aula y fecha
    private void validarConflicto(Reserva nuevaReserva, String idExcluir) throws ConflictoDeHorarioException, ValidacionDeReservaException {
        //Validaciones de tiempo y fecha
        if (nuevaReserva.getHoraInicio().isAfter(nuevaReserva.getHoraFin()) || nuevaReserva.getHoraInicio().equals(nuevaReserva.getHoraFin())) {
            throw new ValidacionDeReservaException("La hora de inicio debe ser anterior a la hora de finalizacion.");
        }
        if (nuevaReserva.getFecha().isBefore(LocalDate.now())) {
            throw new ValidacionDeReservaException("La reserva no puede ser en una fecha pasada.");
        }

        //Detección de Conflicto de Horario. Usa Streams
        boolean conflicto = reservas.stream()
            .filter(r -> r.getEstado().equals("activa"))
            .filter(r -> !r.getIdReserva().equals(idExcluir)) //Excluye la propia reserva en caso de modificación
            .filter(r -> r.getAula().getCodigo().equals(nuevaReserva.getAula().getCodigo()))
            .filter(r -> r.getFecha().equals(nuevaReserva.getFecha()))
            .anyMatch(r -> 
                //Condición estándar de solapamiento: (InicioA < FinB) AND (FinA > InicioB)
                r.getHoraInicio().isBefore(nuevaReserva.getHoraFin()) && r.getHoraFin().isAfter(nuevaReserva.getHoraInicio())
            );
        if (conflicto) {
            //Lanza la excepción personalizada ConflictoDeHorarioException
            throw new ConflictoDeHorarioException(String.format("Conflicto de horario: El aula %s ya esta reservada en la fecha %s.",
                nuevaReserva.getAula().getCodigo(), nuevaReserva.getFecha()));
        }
    }

    //====================== Gestión de Reservas ======================

    //Método central para registrar cualquier reserva.
    private void registrarReserva(Reserva reserva) throws ConflictoDeHorarioException, ValidacionDeReservaException {
        reserva.validarReglasEspecificas(); //Valida reglas de subclase
        validarConflicto(reserva, "");//Lanza la excepción si hay conflicto.
        reservas.add(reserva);
        guardarDatos();
    }

    //Métodos de registro
    public void registrarReservaClase(String codigoAula, LocalDate fecha, LocalTime hInicio, LocalTime hFin, String responsable,
                                    String materia, int numEstudiantes) throws ConflictoDeHorarioException, ValidacionDeReservaException {
        Aula aula = getAulaByCodigo(codigoAula).orElseThrow(() -> new ValidacionDeReservaException("Aula no encontrada: " + codigoAula));
        String id = "R" + nextReservaId++;
        registrarReserva(new ReservaClase(id, aula, fecha, hInicio, hFin, responsable, materia, numEstudiantes));
    }
    
    public void registrarReservaEvento(String codigoAula, LocalDate fecha, LocalTime hInicio, LocalTime hFin, String responsable,
                                       TipoEvento tipoEvento, int asistentesEsperados) throws ConflictoDeHorarioException, ValidacionDeReservaException {
        Aula aula = getAulaByCodigo(codigoAula).orElseThrow(() -> new ValidacionDeReservaException("Aula no encontrada: " + codigoAula));
        String id = "R" + nextReservaId++;
        registrarReserva(new ReservaEvento(id, aula, fecha, hInicio, hFin, responsable, tipoEvento, asistentesEsperados));
    }

    public void registrarReservaPractica(String codigoAula, LocalDate fecha, LocalTime hInicio, LocalTime hFin, String responsable,
                                        String descripcionPractica, int numEquipos) throws ConflictoDeHorarioException, ValidacionDeReservaException {
        Aula aula = getAulaByCodigo(codigoAula).orElseThrow(() -> new ValidacionDeReservaException("Aula no encontrada: " + codigoAula));
        String id = "R" + nextReservaId++;
        registrarReserva(new ReservaPractica(id, aula, fecha, hInicio, hFin, responsable, descripcionPractica, numEquipos));
    }
    
    //====================== Busqueda y Modificacion =======================

    //Búsqueda por ID
    public Optional<Reserva> buscarReservaPorId(String id) {
        return reservas.stream()
            .filter(r -> r.getIdReserva().equalsIgnoreCase(id))
            .findFirst();
    }

    //Búsqueda por texto en campo responsable
    public List<Reserva> buscarReservasPorResponsable(String texto) {
        return reservas.stream()
            .filter(r -> r.getResponsable().toLowerCase().contains(texto.toLowerCase()))
            .collect(Collectors.toList());
    }

    //Modificación de reserva
    public void modificarReserva(String id, LocalDate nuevaFecha, LocalTime nuevoHInicio, LocalTime nuevoHFin, String nuevoResponsable)
        throws ConflictoDeHorarioException, ValidacionDeReservaException {
        
        Reserva reserva = buscarReservaPorId(id)
            .orElseThrow(() -> new ValidacionDeReservaException("Reserva no encontrada: " + id));

        if (reserva.getEstado().equals("cancelada")) {
             throw new ValidacionDeReservaException("No se puede modificar una reserva cancelada. Debe reactivarla primero.");
        }

        //Crear una reserva "temporal" con los nuevos datos para validar el conflicto sin modificar el objeto real.
        //Se usa ReservaClase ya que solo necesitamos los datos de fecha, hora y aula para validar el conflicto.
        Reserva tempReserva = new ReservaClase(id, reserva.getAula(), nuevaFecha, nuevoHInicio, nuevoHFin, nuevoResponsable, "", 0);

        //Validar el conflicto, excluyendo el ID actual para que no choque consigo misma.
        validarConflicto(tempReserva, id); 
        
        //Si no hay conflicto, aplicar los cambios al objeto real.
        reserva.setFecha(nuevaFecha);
        reserva.setHoraInicio(nuevoHInicio);
        reserva.setHoraFin(nuevoHFin);
        reserva.setResponsable(nuevoResponsable);
        guardarDatos();
    }
    
    //Cancelación de reserva
    public void cancelarReserva(String id) throws ValidacionDeReservaException {
        Reserva reserva = buscarReservaPorId(id)
            .orElseThrow(() -> new ValidacionDeReservaException("Reserva no encontrada para cancelar: " + id));
        if (reserva.getEstado().equals("cancelada")) {
            throw new ValidacionDeReservaException("La reserva ya está cancelada.");
        }
        reserva.setEstado("cancelada"); //Control de estado
        guardarDatos();
    }

    //================== Reportes =======================

    //Listado configurable de reservas
    public List<Reserva> listarReservas(String campoOrden, boolean ascendente) {
        //Ordena por campo de la reserva
        Comparator<Reserva> comparator;
        switch (campoOrden.toLowerCase()) {
            case "id":
                comparator = Comparator.comparing(Reserva::getIdReserva);
                break;
            case "fecha":
                comparator = Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHoraInicio);
                break;
            case "aula":
                comparator = Comparator.comparing(r -> r.getAula().getCodigo());
                break;
            case "responsable":
                comparator = Comparator.comparing(Reserva::getResponsable);
                break;
                //Si no se encuentra ninguna opción, ordena por ID
            default:
                comparator = Comparator.comparing(Reserva::getIdReserva);
        }
        if (!ascendente) {
            comparator = comparator.reversed();
        }
        return reservas.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    //Genera el reporte del Top 3 aulas con más horas reservadas
    public String generarReporteTopAulas() {
        //Usa Streams para filtrar, agrupar, sumar minutos y ordenar
        Map<String, Long> horasPorAula = reservas.stream()
            .filter(r -> r.getEstado().equals("activa"))
            .collect(groupingBy(
                r -> r.getAula().getCodigo() + " - " + r.getAula().getNombre(),
                summingLong(r -> ChronoUnit.MINUTES.between(r.getHoraInicio(), r.getHoraFin()))
            ));
        //Ordena y limita el Top 3
        String reporte = horasPorAula.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) //Ordena por valor (total minutos)
            .limit(3) //Limita a 3
            .map(entry -> String.format("- %s: %d horas (Total min: %d)", //Formatea el reporte
                entry.getKey(), entry.getValue() / 60, entry.getValue())) //Divide por 60 para convertir a horas
 
            .collect(joining("\n"));
        //Agrupa y junta
        //Exporta el reporte
        String resultado = "=== Top 3 Aulas con mas horas reservadas (Activas) ===\n" + (reporte.isEmpty() ? "No hay reservas activas." : reporte);
        GestorArchivos.exportarReporte("reporte_top_aulas.txt", resultado);
        return resultado;
    }

    //Reporte distribución por tipo de reserva
    public String generarReporteDistribucionPorTipoReserva() {
        Map<String, Long> distribucion = reservas.stream()
            .collect(Collectors.groupingBy(r -> {
                if (r instanceof ReservaClase) return "Clase";
                else if (r instanceof ReservaEvento) return "Evento";
                else if (r instanceof ReservaPractica) return "Practica";
                else return "Otro";
            }, Collectors.counting()));

        String reporte = distribucion.entrySet().stream()
            .map(e -> String.format("- %s: %d", e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));

        //Exporta el reporte
        String resultado = "=== Distribucion de Reservas por Tipo ===\n" + (reporte.isEmpty() ? "No hay reservas registradas." : reporte);
        GestorArchivos.exportarReporte("reporte_distribucion_tipo.txt", resultado);
        return resultado;
    }

    //Implementacion de reporte de ocupación por tipo de aula
    public String generarReporteOcupacionPorTipoAula() {
        //Agrupa por TipoAula y suma los minutos de duración de las reservas activas
        Map<TipoAula, Long> horasPorTipo = reservas.stream()
            .filter(r -> r.getEstado().equals("activa"))
            .collect(Collectors.groupingBy(
                r -> r.getAula().getTipo(),
                Collectors.summingLong(r -> ChronoUnit.MINUTES.between(r.getHoraInicio(), r.getHoraFin()))
            ));
        
        //Ordena y limita el Top 3
        String reporte = horasPorTipo.entrySet().stream() //Ordena por valor (total minutos)
            .map(entry -> String.format("- Tipo %s: %d horas (Total min: %d)", //Formatea el reporte
                entry.getKey(), entry.getValue() / 60, entry.getValue())) //Divide por 60 para convertir a horas
            .collect(Collectors.joining("\n")); //Agrupa y junta
        
        String resultado = "=== Ocupacion de Aulas por Tipo (Activas) ===\n" + (reporte.isEmpty() ? "No hay reservas activas." : reporte);
        GestorArchivos.exportarReporte("reporte_ocupacion_tipo_aula.txt", resultado);
        return resultado;
    }

    //=================== Persistencia ====================

    //Método para guardar todos los datos
    public void guardarDatos() {
        GestorArchivos.guardarAulas(aulas);
        GestorArchivos.guardarReservas(reservas);
    }
}