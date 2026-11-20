package ErrorCapa8.skillIssueAdmin.Rank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ErrorCapa8.skillIssueAdmin.Sanctions.Baneo;

public interface IPersistencia {
    PersistenciaDatos cargar() throws IOException;
    void guardar(Collection<Rango> rangos, Collection<Jugador> jugadores, String rangoPredeterminadoId) throws IOException;
    List<Baneo> cargarBaneos() throws IOException;
    void guardarBaneos(Collection<Baneo> baneos) throws IOException;
    List<String> cargarRegistrosSanciones() throws IOException;
    void guardarRegistrosSanciones(Collection<String> registros) throws IOException;

    boolean guardarDatos(String nombreArchivo, List<?> datos);
    ArrayList<String> cargarDatos(String nombreArchivo);
    boolean verificarArchivo(String nombreArchivo);
}
