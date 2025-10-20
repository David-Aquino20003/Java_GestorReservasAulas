package GestorReservarAula.persistence;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

import GestorReservarAula.models.*;
import GestorReservarAula.util.TipoAula;
import GestorReservarAula.util.TipoEvento;


/*Clase para manejar persistencia de datos en archivos TXT o CSV
Separa la logica de entrada/salida de datos de la logica de negocio*/
public class GestorArchivos {
    private static final String Aulas_File = "aulas.txt";
    private static final String Reservas_File = "reservas.txt";

    //Escribe la lista completa de Aulas en el archivo.
    public static void guardarAulas(List<Aula> aulas) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(Aulas_File))) {
            for (Aula aula : aulas) {
                writer.println(aula.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar aulas: " + e.getMessage());
        }
    }

    //Carga la lista de Aulas desde el archivo
    public static List<Aula> cargarAulas() {
        List<Aula> aulas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(Aulas_File))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                //Minimo 4 partes: codigo, nombre, capacidad, tipo
                if (partes.length >= 4) {
                    String codigo = partes[0];
                    String nombre = partes[1];

                    int capacidad = Integer.parseInt(partes[2]);
                    
                    TipoAula tipo = TipoAula.valueOf(partes[3].toUpperCase());

                    aulas.add(new Aula(codigo, nombre, capacidad, tipo));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de aulas no encontrado. Se creara uno nuevo al guardar.");
        } catch (IOException | IllegalArgumentException e) {
            //Manejo de multiples excepciones en una sola clausula catch
            System.err.println("Error al cargar aulas. Corrupcion de datos: " + e.getMessage());
        }
        return aulas;
    }

    //Escribe la lista completa de Reservas en el archivo (sobrescribe).
    public static void guardarReservas(List<Reserva> reservas) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(Reservas_File))) {
            for (Reserva reserva : reservas) {
                writer.println(reserva.toCsvString());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar reservas: " + e.getMessage());
        }
    }

    //Carga la lista de Reservas desde el archivo. Requiere la lista de aulas para asignar la referencia.
    public static List<Reserva> cargarReservas(List<Aula> aulas) {
        List<Reserva> reservas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(Reservas_File))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",");
                /*El minimo de campos para una reserva especifica (Clase/Evento/Practica) es 9:
                Tipo(0), Id(1), CodigoAula(2), Fecha(3), HIni(4), HFin(5), Responsable(6), DatoEsp1(7), DatoEsp2(8)*/
                if (partes.length < 9) continue; 

                String tipoReserva = partes[0];
                String idReserva = partes[1];
                String codigoAula = partes[2];
                
                //Conversion de tipos.
                LocalDate fecha = LocalDate.parse(partes[3]);
                LocalTime horaInicio = LocalTime.parse(partes[4]);
                LocalTime horaFin = LocalTime.parse(partes[5]);
                String responsable = partes[6];

                //Buscar el Aula correspondiente
                Aula aula = aulas.stream()
                        .filter(a -> a.getCodigo().equals(codigoAula))
                        .findFirst()
                        .orElse(null);

                if (aula == null) {
                    System.err.printf("Advertencia: Aula con codigo %s no encontrada para la reserva %s. Se omite.%n", codigoAula, idReserva);
                    continue;
                }

                Reserva reserva = null;

                //Crear la instancia de la subclase de Reserva
                switch (tipoReserva.toUpperCase()) {
                    case "CLASE":
                        //Se asume que partes[7] es materia y partes[8] es numEstudiantes
                        String materia = partes[7]; 
                        int numEstudiantes = Integer.parseInt(partes[8]);
                        reserva = new ReservaClase(idReserva, aula, fecha, horaInicio, horaFin, responsable, materia, numEstudiantes);
                        break;
                    case "EVENTO":
                        //Se asume que partes[7] es TipoEvento y partes[8] es asistentes
                        TipoEvento tipoEvento = TipoEvento.valueOf(partes[7].toUpperCase());
                        int asistentes = Integer.parseInt(partes[8]);
                        reserva = new ReservaEvento(idReserva, aula, fecha, horaInicio, horaFin, responsable, tipoEvento, asistentes);
                        break;
                    case "PRACTICA":
                        //Se asume que partes[7] es descripcion y partes[8] es numEquipos
                        String descripcion = partes[7];
                        int numEquipos = Integer.parseInt(partes[8]);
                        reserva = new ReservaPractica(idReserva, aula, fecha, horaInicio, horaFin, responsable, descripcion, numEquipos);
                        break;
                }

                if (reserva != null) {
                    /*El estado (ej. "activa") de la Reserva SOLO se persiste (guarda/carga) si es variable y no se establece 
                    por defecto en el constructor. Si se establece por defecto, se omite su lectura/escritura en el archivo.*/
                    reservas.add(reserva);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de reservas no encontrado. Se creara uno nuevo al guardar.");
        } catch (IOException | RuntimeException e) { 
            //Captura IOException (lectura/escritura) y RuntimeException (Parse/Number/IllegalArg)
            System.err.println("Error al cargar reservas. Corrupcion de datos: " + e.getMessage());
        }
        return reservas;
    }

    //Exporta el contenido de un reporte a un archivo de texto.
    public static void exportarReporte(String nombreArchivo, String contenido) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            writer.print(contenido);
            System.out.printf("Reporte exportado exitosamente a: %s%n", nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al exportar reporte: " + e.getMessage());
        }
    }
}