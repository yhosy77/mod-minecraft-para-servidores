package ErrorCapa8.skillIssueAdmin;

import ErrorCapa8.skillIssueAdmin.Rank.IPersistencia;
import ErrorCapa8.skillIssueAdmin.Rank.Rango;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Gestiona las donaciones (RF-13) y las compras de rangos exclusivos (RF-12).
 *
 * Persistencia (IPersistencia): donaciones.txt y compras.txt. Se cargan en el constructor
 * y se guardan después de cada registro.
 */
public final class GestorTransacciones {
    public static final String ARCHIVO_DONACIONES = "donaciones.txt";
    public static final String ARCHIVO_COMPRAS = "compras.txt";

    private final JavaPlugin plugin;
    private final IPersistencia persistencia;
    private final ArrayList<Donacion> donaciones = new ArrayList<>();
    private final ArrayList<CompraRango> compras = new ArrayList<>();

    public GestorTransacciones(JavaPlugin plugin, IPersistencia persistencia) {
        this.plugin = plugin;
        this.persistencia = persistencia;
        cargar();
    }

    // ------------------------------------------------------------------
    // Donaciones (RF-13)
    // ------------------------------------------------------------------

    /**
     * Registra una donación. El monto debe ser mayor a cero y no da recompensas:
     * no cambia el rango ni las monedas del jugador.
     * @throws IllegalArgumentException si el monto no es válido.
     * @throws IllegalStateException si no se pudo guardar.
     */
    public synchronized Donacion registrarDonacion(String nombreJugador, double monto, String mensaje) {
        Donacion donacion = new Donacion(nuevoId("D"), nombreJugador, monto, mensaje);
        donacion.confirmarDonacion();
        donaciones.add(donacion);
        if (!guardarDonaciones()) {
            donaciones.remove(donacion);
            throw new IllegalStateException("No se pudo guardar la donación.");
        }
        return donacion;
    }

    /** Donaciones hechas entre dos fechas (incluidas), de la más reciente a la más antigua. */
    public synchronized List<Donacion> donacionesEntre(LocalDateTime desde, LocalDateTime hasta) {
        return donaciones.stream().filter(d -> !d.getFecha().isBefore(desde) && !d.getFecha().isAfter(hasta))
                .sorted(Comparator.comparing(Donacion::getFecha).reversed()).toList();
    }

    /** Total donado en un periodo (RF-13). */
    public synchronized double totalDonado(LocalDateTime desde, LocalDateTime hasta) {
        return donacionesEntre(desde, hasta).stream().mapToDouble(Donacion::getMonto).sum();
    }

    // ------------------------------------------------------------------
    // Compras de rangos exclusivos (RF-12)
    // ------------------------------------------------------------------

    /**
     * Registra la compra de un rango exclusivo pagado fuera del sistema: guarda fecha, valor y rango.
     * @throws IllegalArgumentException si el rango no es exclusivo o el valor no es válido.
     * @throws IllegalStateException si no se pudo guardar.
     */
    public synchronized CompraRango registrarCompra(String nombreJugador, Rango rango, double valor) {
        if (rango == null) throw new IllegalArgumentException("Ese rango no existe.");
        if (!rango.esPago())
            throw new IllegalArgumentException("Solo se pueden registrar compras de rangos EXCLUSIVOS (créalo en /rank).");
        CompraRango compra = new CompraRango(nuevoId("C"), nombreJugador, rango.getNombre(), valor);
        compra.procesarCompra(valor);
        compras.add(compra);
        if (!guardarCompras()) {
            compras.remove(compra);
            throw new IllegalStateException("No se pudo guardar la compra.");
        }
        return compra;
    }

    public synchronized List<CompraRango> getCompras() { return new ArrayList<>(compras); }
    public synchronized List<Donacion> getDonaciones() { return new ArrayList<>(donaciones); }

    // ------------------------------------------------------------------
    // Persistencia
    // ------------------------------------------------------------------

    private static String nuevoId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean guardarDonaciones() {
        List<String> lineas = new ArrayList<>();
        for (Donacion d : donaciones) lineas.add(d.toLinea());
        return persistencia.guardarDatos(ARCHIVO_DONACIONES, lineas);
    }

    private boolean guardarCompras() {
        List<String> lineas = new ArrayList<>();
        for (CompraRango c : compras) lineas.add(c.toLinea());
        return persistencia.guardarDatos(ARCHIVO_COMPRAS, lineas);
    }

    private void cargar() {
        for (String linea : persistencia.cargarDatos(ARCHIVO_DONACIONES)) {
            try { donaciones.add(Donacion.desdeLinea(linea)); }
            catch (RuntimeException e) { plugin.getLogger().warning("Se omitió una línea dañada de " + ARCHIVO_DONACIONES + ": " + linea); }
        }
        for (String linea : persistencia.cargarDatos(ARCHIVO_COMPRAS)) {
            try { compras.add(CompraRango.desdeLinea(linea)); }
            catch (RuntimeException e) { plugin.getLogger().warning("Se omitió una línea dañada de " + ARCHIVO_COMPRAS + ": " + linea); }
        }
        plugin.getLogger().info("Se cargaron " + donaciones.size() + " donaciones y " + compras.size() + " compras de rangos.");
    }
}
