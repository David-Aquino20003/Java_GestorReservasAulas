/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package GestorReservarAula;

/**
 *
 * @author dawia
 */

import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

import GestorReservarAula.models.Aula;
import GestorReservarAula.models.Reserva;
import GestorReservarAula.services.GestorReservas;
import GestorReservarAula.util.TipoAula;
import GestorReservarAula.util.TipoEvento;
import GestorReservarAula.exceptions.ConflictoDeHorarioException;
import GestorReservarAula.exceptions.ValidacionDeReservaException;

/*Clase principal que contiene la interfaz de consola para el usuario.
Se encarga de la interaccion (I/O) y maneja las excepciones lanzadas por el servicio.*/
public class GestorReservaAula {
    private static GestorReservas gestor;
    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("=== Gestor de Reservas de Aulas ITCA ===");
        try {
            //Inicializa el gestor, carga datos
            gestor = new GestorReservas(); 
            scanner = new Scanner(System.in);
            int opcion;

            do {
                mostrarMenuPrincipal();
                
                opcion = leerOpcionMenu(null); 

                switch (opcion) {
                    case 1: 
                        menuAulas();
                        break;
                    case 2: 
                        menuReservas();
                        break;
                    case 3:
                        menuReportes();
                        break;
                    case 0:
                        System.out.println("Guardando datos y saliendo...");
                        gestor.guardarDatos();
                        break;
                    default:
                        if (opcion != -1) System.out.println("Opcion no valida. Intente de nuevo");
                }
            } while (opcion != 0);

        } catch (ValidacionDeReservaException e) {
            System.err.println("Error fatal de inicializacion: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en el sistema principal: " + e.getMessage());
        } finally {
             if (scanner != null) scanner.close();
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Gestion de Aulas");
        System.out.println("2. Gestion de Reservas");
        System.out.println("3. Reportes");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opcion: ");
    }

    //====================== Metodos de lectura de consola (In/Out) ======================

    //Lee una opcion de menu (entero no negativo). Maneja NumberFormatException.
    private static int leerOpcionMenu(Integer valorDefecto) {
        String input = scanner.nextLine();
        if (input.isEmpty() && valorDefecto != null) return valorDefecto;

        //Intenta parsear el input como entero
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Entrada no valida. Debe ser un numero.");
            return -1;
        }
    }

    //Lee un entero. Devuelve -1 en caso de error de formato.
    private static int leerEntero(String prompt, int valorDefecto) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        if (input.isEmpty()) return valorDefecto;

        try {
            int num = Integer.parseInt(input);
            if (num <= 0) {
                System.err.println("El valor debe ser positivo. Cancelando operacion.");
                return -1; //Usar -1 para indicar error fatal en el flujo
            }
            return num;
        } catch (NumberFormatException e) {
            System.err.println("Formato numerico no valido. Cancelando operacion.");
            return -1;
        }
    }

    //Devuelve valorDefecto si la entrada esta vacia.
    private static String leerString(String prompt, String valorDefecto) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        return input.isEmpty() ? valorDefecto : input;
    }

    //Lee una fecha. Devuelve valorDefecto si la entrada esta vacia o null si hay error de formato.
    private static LocalDate leerFecha(String prompt, LocalDate valorDefecto) {
        System.out.print(prompt + " (YYYY-MM-DD): ");
        String input = scanner.nextLine();
        if (input.isEmpty()) return valorDefecto;

        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.err.println("Formato de fecha no valido. Use el formato YYYY-MM-DD. Cancelando operacion.");
            return null; //Devuelve null para forzar la salida en caso de error
        }
    }

    //Lee una hora. Devuelve valorDefecto si la entrada esta vacia o null si hay error de formato.
    private static LocalTime leerHora(String prompt, LocalTime valorDefecto) {
        System.out.print(prompt + " (HH:mm): ");
        String input = scanner.nextLine();
        if (input.isEmpty()) return valorDefecto;

        try {
            return LocalTime.parse(input);
        } catch (DateTimeParseException e) {
            System.err.println("Formato de hora no valido. Use el formato HH:mm. Cancelando operacion.");
            return null; //Devuelve null para forzar la salida en caso de error
        }
    }

    //Lee una enumeracion (TipoAula o TipoEvento).
    private static <T extends Enum<T>> T leerTipoEnum(String prompt, Class<T> enumClass, T valorDefecto) { 
        System.out.print(prompt);
        String input = scanner.nextLine().trim(); //Quita espacios iniciales/finales
        if (input.isEmpty()) return valorDefecto;

        //Convertir la entrada del usuario a mayusculas para una comparacion flexible
        String inputUpperCase = input.toUpperCase();

        //Intentar encontrar la constante de la ENUM que coincida
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().toUpperCase().equals(inputUpperCase)) {
                return enumConstant;
            }
        }
        //Si no se encontro ninguna coincidencia despues de revisar todas las constantes
        System.err.println("Tipo no valido. Intente de nuevo.");
        return null;
    }

    //====================== Gestion de Aulas ======================
    
    private static void menuAulas() {
        int opcion;
        do {
            //Opciones del menu de Aulas
            System.out.println("\n=== GESTION DE AULAS ===");
            System.out.println("1. Registrar Aula");
            System.out.println("2. Listar Aulas");
            System.out.println("3. Modificar Aula");
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");
            opcion = leerOpcionMenu(null);

            switch (opcion) {
                case 1:
                    registrarAula();
                    break;
                case 2:
                    listarAulas();
                    break;
                case 3:
                    modificarAula();
                    break;
                case 0:
                    break; 
                case -1: 
                    break; //Error de lectura
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void registrarAula() {
        System.out.println("\n=== REGISTRAR AULA ===");
        System.out.print("Codigo del Aula (ej: A101): ");
        String codigo = scanner.nextLine().toUpperCase();

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();
        
        //Uso de leerEntero: -1 indica error para salir del flujo
        int capacidad = leerEntero("Capacidad: ", -1); 
        if (capacidad == -1) return;

        //Uso de helper generico para Enum: null indica error o entrada vacia
        TipoAula tipo = leerTipoEnum("Tipo de Aula (Teorica, Laboratorio, Auditorio): ", TipoAula.class, null);
        if (tipo == null) return;

        try {
            gestor.registrarAula(codigo, nombre, capacidad, tipo);
            System.out.println("Aula " + codigo + " registrada exitosamente.");
        } catch (ValidacionDeReservaException e) {
            System.err.println("Error al registrar aula: " + e.getMessage());
        }
    }
    
    //Funcion para Modificar Aula. Usa helpers con valores por defecto.
    private static void modificarAula() {
        System.out.println("\n=== MODIFICAR AULA ===");
        System.out.print("Ingrese el Codigo del Aula a modificar: ");
        String codigo = scanner.nextLine().toUpperCase();
        
        //Se debe usar la implementacion completa del gestor: listarAulas
        Optional<Aula> aulaOpt = gestor.listarAulas().stream()
            .filter(a -> a.getCodigo().equals(codigo))
            .findFirst();

        //Validar que la Aula exista
        if (!aulaOpt.isPresent()) {
            System.err.println("Aula no encontrada.");
            return;
        }
        
        //Obtener los datos actuales
        Aula aulaActual = aulaOpt.get();
        System.out.println("Datos actuales: " + aulaActual.toString());

        //Uso de helpers unificados (limpio y conciso)
        String nuevoNombre = leerString("Nuevo Nombre (Dejar vacio para no cambiar: '" + aulaActual.getNombre() + "'): ", aulaActual.getNombre());
        int nuevaCapacidad = leerEntero("Nueva Capacidad (Dejar vacio para no cambiar: '" + aulaActual.getCapacidad() + "'): ", aulaActual.getCapacidad());
        TipoAula nuevoTipo = leerTipoEnum("Nuevo Tipo (Dejar vacio para no cambiar: '" + aulaActual.getTipo() + "'): ", TipoAula.class, aulaActual.getTipo());
        
        if (nuevoTipo == null || nuevaCapacidad == -1) return; //Validacion de error en lectura
        
        try {
            //Llama al metodo implementado en GestorReservas
            gestor.modificarAula(codigo, nuevoNombre, nuevaCapacidad, nuevoTipo);
            System.out.println("Aula " + codigo + " modificada exitosamente.");
        } catch (ValidacionDeReservaException e) {
            System.err.println("Error al modificar aula: " + e.getMessage());
        }
    }

    private static void listarAulas() {
        //Se usa la implementacion del gestor: listarAulas que devuelve Collection<Aula>
        Collection<Aula> aulas = gestor.listarAulas();

        if (aulas.isEmpty()) {
            System.out.println("No hay aulas registradas.");
            return;
        }

        System.out.println("\n=== LISTADO DE AULAS ===");
        //Uso de Streams para impresion concisa
        aulas.forEach(System.out::println);
    }

    //======================== Gestion de Reservas (In/Out) ======================
    
    private static void menuReservas() {
        int opcion;
        do {
            //Opciones del menu de Reservas
            System.out.println("\n=== GESTION DE RESERVAS ===");
            System.out.println("1. Registrar Nueva Reserva");
            System.out.println("2. Listar y Buscar Reservas");
            System.out.println("3. Modificar Reserva");
            System.out.println("4. Cancelar Reserva");
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");
            
            opcion = leerOpcionMenu(null);

            switch (opcion) {
                case 1: 
                    menuRegistroReserva();
                    break;
                case 2:
                    menuBusquedaListadoReservas();
                    break;
                case 3: 
                    modificarReserva();
                    break;
                case 4: 
                    cancelarReserva();
                    break;
                case 0:
                    break;
                case -1: 
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }
    
    private static void menuRegistroReserva() {
        int opcion;
        do {
            //Opciones del menu de Registro
            System.out.println("\n=== TIPO DE RESERVA ===");
            System.out.println("1. Clase");
            System.out.println("2. Evento");
            System.out.println("3. Practica");
            System.out.println("0. Volver a Gestion de Reservas");
            System.out.print("Seleccione el tipo de reserva: ");
            
            opcion = leerOpcionMenu(null);

            switch (opcion) {
                case 1: 
                    registrarReservaClase();
                    break; 
                case 2: 
                    registrarReservaEvento();
                    break;
                case 3:
                    registrarReservaPractica();
                    break;
                case 0:
                    break;
                case -1:
                    break; 
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    //Funcion que lee los datos comunes para cualquier reserva
    private static boolean leerDatosBaseReserva(String tipo, String[] datos) {
        System.out.println("\n=== REGISTRAR RESERVA DE " + tipo.toUpperCase() + " ===");
        System.out.print("Codigo del Aula: ");
        datos[0] = scanner.nextLine().toUpperCase(); //CodigoAula
        
        //Uso de leerFecha/leerHora unificados. Devuelve null en caso de error
        LocalDate fecha = leerFecha("Fecha de la Reserva", null);
        if (fecha == null) return false;
        datos[1] = fecha.toString();

        //Uso de leerHora unificado. Devuelve null en caso de error
        LocalTime hInicio = leerHora("Hora de Inicio", null);
        if (hInicio == null) return false;
        datos[2] = hInicio.toString();

        //Uso de leerHora unificado. Devuelve null en caso de error
        LocalTime hFin = leerHora("Hora de Finalizacion", null);
        if (hFin == null) return false;
        datos[3] = hFin.toString();

        System.out.print("Nombre del Responsable: ");
        datos[4] = scanner.nextLine(); //Responsable
        return true;
    }

    //Funcion para Registrar Reserva de Clase
    private static void registrarReservaClase() {
        String[] datos = new String[5];
        if (!leerDatosBaseReserva("Clase", datos)) return;

        System.out.print("Materia: ");
        String materia = scanner.nextLine();

        int numEstudiantes = leerEntero("Numero de Estudiantes", -1);
        if (numEstudiantes == -1) return;

        //Llama al servicio, que maneja internamente las validaciones y excepciones
        try {
            gestor.registrarReservaClase(
                datos[0], LocalDate.parse(datos[1]), LocalTime.parse(datos[2]), LocalTime.parse(datos[3]), datos[4],
                materia, numEstudiantes
            );
            System.out.println("Reserva de Clase registrada exitosamente.");
        } catch (ConflictoDeHorarioException | ValidacionDeReservaException e) {
            System.err.println("Error al registrar: " + e.getMessage());
        }
    }

    //Funcion para Registrar Reserva de Evento
    private static void registrarReservaEvento() {
        String[] datos = new String[5];
        if (!leerDatosBaseReserva("Evento", datos)) return;
        
        TipoEvento tipoEvento = leerTipoEnum("Tipo de Evento (Conferencia, Taller, Reunion): ", TipoEvento.class, null);
        if (tipoEvento == null) return;
        
        int asistentes = leerEntero("Asistentes Esperados", -1);
        if (asistentes == -1) return;

        //Llama al servicio, que maneja internamente las validaciones y excepciones
        try {
            gestor.registrarReservaEvento(
                datos[0], LocalDate.parse(datos[1]), LocalTime.parse(datos[2]), LocalTime.parse(datos[3]), datos[4],
                tipoEvento, asistentes
            );
            System.out.println("Reserva de Evento registrada exitosamente.");
        } catch (ConflictoDeHorarioException | ValidacionDeReservaException e) {
            System.err.println("Error al registrar: " + e.getMessage());
        }
    }
    
    //Funcion para Registrar Reserva de Practica
    private static void registrarReservaPractica() {
        String[] datos = new String[5];
        if (!leerDatosBaseReserva("Practica", datos)) return;

        System.out.print("Descripcion de la Practica: ");
        String descripcion = scanner.nextLine();
        
        int numEquipos = leerEntero("Numero de Equipos Requeridos", -1);
        if (numEquipos == -1) return;

        try {
            gestor.registrarReservaPractica(
                datos[0], LocalDate.parse(datos[1]), LocalTime.parse(datos[2]), LocalTime.parse(datos[3]), datos[4],
                descripcion, numEquipos
            );
            System.out.println("Reserva de Practica registrada exitosamente.");
        } catch (ConflictoDeHorarioException | ValidacionDeReservaException e) {
            System.err.println("Error al registrar: " + e.getMessage());
        }
    }
    
    //====================== Busqueda, Listado y Modificacion =======================

    private static void menuBusquedaListadoReservas() {
        //Menu de Busqueda y Listado
        int opcion;
        do {
            System.out.println("\n=== BUSQUEDA Y LISTADO DE RESERVAS===");
            System.out.println("1. Listar todas las Reservas");
            System.out.println("2. Buscar por ID");
            System.out.println("3. Buscar por Responsable");
            System.out.println("0. Volver al menu anterior");
            System.out.print("Seleccione una opcion: ");
            
            opcion = leerOpcionMenu(null);

            switch (opcion) {
                case 1:
                    listarReservas();
                    break;
                case 2:
                    buscarReservaPorId();
                    break;
                case 3:
                    buscarReservasPorResponsable();
                    break;
                case 0:
                    break;
                case -1:
                    break; 
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void listarReservas() {
        System.out.println("\n=== LISTADO DE RESERVAS ===");
        String campo = leerString("Ordenar por campo (ID/FECHA/AULA/RESPONSABLE - [id]): ", "id");
        String ordenStr = leerString("Orden (ASC/DESC - [ASC]): ", "ASC");
        boolean ascendente = ordenStr.toUpperCase().startsWith("A");

        //Logica de ordenamiento y filtrado por Streams esta en el GestorReservas
        List<Reserva> lista = gestor.listarReservas(campo, ascendente);

        if (lista.isEmpty()) {
            System.out.println("No hay reservas registradas.");
            return;
        }

        //Muestra el tipo de reserva y su contenido
        lista.forEach(System.out::println);
    }

    //Funcion para buscar Reserva por ID
    private static void buscarReservaPorId() {
        System.out.print("Ingrese el ID de la reserva a buscar: ");
        String id = scanner.nextLine();
        Optional<Reserva> reservaOpt = gestor.buscarReservaPorId(id);

        //Validar que la Reserva exista
        if (reservaOpt.isPresent()) {
            System.out.println("\nReserva encontrada: " + reservaOpt.get());
        } else {
            System.out.println("Reserva con ID '" + id + "' no encontrada.");
        }
    }

    //Funcion para buscar Reservas por Responsable
    private static void buscarReservasPorResponsable() {
        System.out.print("Ingrese texto a buscar en el responsable: ");
        String texto = scanner.nextLine();
        List<Reserva> reservas = gestor.buscarReservasPorResponsable(texto);

        if (reservas.isEmpty()) {
            System.out.println("No se encontraron reservas para el responsable que contenga: " + texto);
            return;
        }
        
        //Muestra el tipo de reserva y su contenido
        System.out.println("\n=== RESERVAS ENCONTRADAS ===");
        reservas.forEach(System.out::println);
    }

    //Funcion simplificada para la Modificacion de Reserva (Requisito 3).
    private static void modificarReserva() {
        System.out.println("\n=== MODIFICAR RESERVA ===");
        System.out.print("Ingrese el ID de la reserva a modificar (ej: R1): ");
        String id = scanner.nextLine();

        try {
            Reserva reservaActual = gestor.buscarReservaPorId(id)
                .orElseThrow(() -> new ValidacionDeReservaException("Reserva no encontrada: " + id));

            System.out.println("Datos actuales: " + reservaActual.toString());

            //Uso de helpers unificados (limpio y conciso)
            LocalDate nuevaFecha = leerFecha("Nueva Fecha", reservaActual.getFecha());
            LocalTime nuevoHInicio = leerHora("Nueva Hora de Inicio", reservaActual.getHoraInicio());
            LocalTime nuevoHFin = leerHora("Nueva Hora de Finalizacion", reservaActual.getHoraFin());
            String nuevoResponsable = leerString("Nuevo Responsable", reservaActual.getResponsable());

            //Validar si algun campo clave es nulo (error de I/O)
            if (nuevaFecha == null || nuevoHInicio == null || nuevoHFin == null) return;
            
            gestor.modificarReserva(id, nuevaFecha, nuevoHInicio, nuevoHFin, nuevoResponsable);
            System.out.println("Reserva " + id + " modificada exitosamente.");

        } catch (ConflictoDeHorarioException | ValidacionDeReservaException e) {
            System.err.println("Error al modificar: " + e.getMessage());
        }
    }

    private static void cancelarReserva() {
        System.out.println("\n=== CANCELAR RESERVA ===");
        System.out.print("Ingrese el ID de la reserva a cancelar (ej: R1): ");
        String id = scanner.nextLine();

        try {
            gestor.cancelarReserva(id);
            System.out.println("Reserva " + id + " cancelada exitosamente.");
        } catch (ValidacionDeReservaException e) {
            System.err.println("Error al cancelar: " + e.getMessage());
        }
    }

    //====================== Menu de Reportes (I/O) ======================

    private static void menuReportes() {
        int opcion;
        do {
            //Opciones del menu de Reportes
            System.out.println("\n=== REPORTES ===");
            System.out.println("1. Top 3 Aulas con mas Horas Reservadas");
            System.out.println("2. Ocupacion por Tipo de Aula");
            System.out.println("3. Distribucion por Tipo de Reserva");
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");
            
            opcion = leerOpcionMenu(null);

            switch (opcion) {
                case 1:
                    //Se usa la implementacion del gestor: generarReporteTopAulas
                    System.out.println(gestor.generarReporteTopAulas());
                    break;
                case 2:
                    //Se usa la implementacion del gestor: generarReporteOcupacionPorTipoAula
                    System.out.println(gestor.generarReporteOcupacionPorTipoAula());
                    break;
                case 3:
                    //Se usa la implementacion del gestor: generarReporteDistribucionPorTipoReserva
                    System.out.println(gestor.generarReporteDistribucionPorTipoReserva());
                    break;
                case 0:
                    break;
                case -1:
                    break; 
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }
}