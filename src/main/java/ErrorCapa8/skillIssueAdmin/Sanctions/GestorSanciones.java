package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.IPersistencia;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Administra todas las sanciones del servidor (Baneo, Mute, Advertido).
 *
 * - historial: TODAS las sanciones (vigentes, vencidas y revocadas). Es lo que se guarda.
 * - sancionesActivas: solo las vigentes.
 *
 * Persistencia (IPersistencia):
 * - Todas las sanciones se guardan en sanciones.txt, una línea por sanción.
 *   Se carga en el constructor y se guarda después de cada cambio.
 * - Los baneos que existían en el sanciones.yml anterior se pasan una sola vez a sanciones.txt.
 */
public final class GestorSanciones {
    public static final String ARCHIVO = "sanciones.txt";

    private final JavaPlugin plugin;
    private final IPersistencia persistencia;
    private final GestorJugadores gestorJugadores;
    private final ArrayList<Sancion> historial = new ArrayList<>();
    private final ArrayList<Sancion> sancionesActivas = new ArrayList<>();

    public GestorSanciones(JavaPlugin plugin, IPersistencia persistencia, GestorJugadores gestorJugadores) {
        this.plugin = plugin; this.persistencia = persistencia; this.gestorJugadores = gestorJugadores;
        cargar();
    }

    // ------------------------------------------------------------------
    // Métodos originales de Felipe (usados por el menú de /ban y BanEnforcementListener)
    // ------------------------------------------------------------------

    public synchronized List<Baneo> obtenerBaneos() {
        return historial.stream().filter(s -> s instanceof Baneo).map(s -> (Baneo) s)
                .sorted(Comparator.comparing(Sancion::getCreadaEn).reversed()).toList();
    }

    public synchronized Optional<Baneo> obtenerBaneoActivo(UUID jugadorId, Instant ahora) {
        return historial.stream().filter(s -> s instanceof Baneo).map(s -> (Baneo) s)
                .filter(b -> b.getJugadorId().equals(jugadorId) && b.estaVigente(ahora))
                .max(Comparator.comparing(Sancion::getCreadaEn));
    }

    public synchronized Baneo aplicarBaneo(BanDraft draft, Player administrador) throws IOException {
        if (administrador == null || !gestorJugadores.esAdministrador(administrador.getUniqueId()))
            throw new SecurityException("Solo un rango ADMIN puede aplicar baneos.");
        if (draft == null || !draft.estaCompleto()) throw new IllegalArgumentException("Faltan datos requeridos para el baneo.");
        if (draft.getJugadorId().equals(administrador.getUniqueId())) throw new IllegalArgumentException("No puedes banearte a ti mismo.");
        Player target = Bukkit.getPlayer(draft.getJugadorId());
        if (target == null || !target.isOnline()) throw new IllegalArgumentException("El jugador seleccionado ya se desconectó.");
        if (obtenerBaneoActivo(draft.getJugadorId(), Instant.now()).isPresent())
            throw new IllegalArgumentException("El jugador ya tiene un baneo activo.");

        Instant createdAt = Instant.now();
        Instant expiresAt = draft.getDuracion().calcularExpiracion(createdAt);
        Baneo baneo = new Baneo(UUID.randomUUID(), target.getUniqueId(), target.getName(), draft.getCausa(),
                draft.getDetalleCausa(), draft.getMensaje(), administrador.getUniqueId(), administrador.getName(),
                createdAt, expiresAt, draft.getDuracion() == DuracionBaneo.PERMANENTE);
        registrarYGuardar(baneo);

        // Polimorfismo: el efecto lo decide la propia sanción (Baneo -> kick).
        if (!baneo.aplicarEfecto(gestorJugadores.buscarJugador(target.getUniqueId()))) target.kick(baneo.mensajeKick());
        return baneo;
    }

    // ------------------------------------------------------------------
    // Métodos del módulo de sanciones
    // ------------------------------------------------------------------

    /**
     * Registra cualquier sanción (Baneo, Mute o Advertido), la guarda y aplica su efecto.
     * @throws SancionInvalidaException si los datos no son válidos, el jugador ya tiene una
     *         sanción del mismo tipo vigente (baneo o mute) o no se pudo guardar.
     */
    public synchronized void aplicarSancion(Jugador j, Sancion s) throws SancionInvalidaException {
        if (j == null) throw new SancionInvalidaException("El jugador no está registrado en el servidor.");
        if (s == null) throw new SancionInvalidaException("La sanción no puede ser nula.");
        if (!j.getUuid().equals(s.getJugadorId()))
            throw new SancionInvalidaException("La sanción #" + s.getNumero() + " no corresponde a " + j.getNombre() + ".");
        if (s.getResponsableId().equals(j.getUuid())) throw new SancionInvalidaException("No puedes sancionarte a ti mismo.");
        if (!s.isActiva()) throw new SancionInvalidaException("No se puede aplicar una sanción revocada.");
        if (buscarPorNumero(s.getNumero()) != null) throw new SancionInvalidaException("La sanción #" + s.getNumero() + " ya fue aplicada.");
        if (s instanceof Baneo && obtenerBaneoActivo(j.getUuid(), Instant.now()).isPresent())
            throw new SancionInvalidaException(j.getNombre() + " ya tiene un baneo activo.");
        if (s instanceof Mute && estaMuteado(j.getUuid()))
            throw new SancionInvalidaException(j.getNombre() + " ya está silenciado.");

        try {
            registrarYGuardar(s);
        } catch (IOException exception) {
            throw new SancionInvalidaException("No se pudo guardar la sanción: " + exception.getMessage());
        }
        s.aplicarEfecto(j); // polimorfismo: cada tipo hace algo distinto
    }

    /** Levanta (revoca) una sanción por su número, sin motivo. */
    public synchronized boolean levantarSancion(int idSancion) {
        return levantarSancion(idSancion, "Sin motivo especificado");
    }

    /**
     * Levanta (revoca) una sanción por su número.
     * @return true si se levantó; false si no existe, ya estaba revocada o no se pudo guardar.
     */
    public synchronized boolean levantarSancion(int idSancion, String motivoPerdon) {
        Sancion s = buscarPorNumero(idSancion);
        if (s == null || !s.isActiva()) return false;
        try {
            s.revocar(motivoPerdon);
        } catch (SancionInvalidaException exception) {
            return false;
        }
        sancionesActivas.remove(s);
        if (!guardar()) {
            s.restaurarActiva();
            sancionesActivas.add(s);
            plugin.getLogger().warning("No se pudo persistir la revocación de la sanción #" + idSancion + ".");
            return false;
        }
        return true;
    }

    /** Busca una sanción por su número corto (#5). */
    public synchronized Sancion buscarPorNumero(int numero) {
        for (Sancion s : historial) if (s.getNumero() == numero) return s;
        return null;
    }

    /** Todas las sanciones de un jugador (vigentes, vencidas y revocadas), la más reciente primero. */
    public synchronized ArrayList<Sancion> obtenerSancionesPorJugador(String id) {
        ArrayList<Sancion> resultado = new ArrayList<>();
        for (Sancion s : historial) if (s.getJugadorId().toString().equalsIgnoreCase(id)) resultado.add(s);
        resultado.sort(Comparator.comparing(Sancion::getCreadaEn).reversed());
        return resultado;
    }

    public synchronized ArrayList<Sancion> obtenerSancionesPorJugador(UUID id) {
        return obtenerSancionesPorJugador(id.toString());
    }

    /** Cuántas sanciones tiene el jugador sin haber sido perdonadas (las vencidas cuentan, las revocadas no). */
    public synchronized int calcularReincidencia(Jugador j) {
        if (j == null) return 0;
        int cantidad = 0;
        for (Sancion s : historial) if (s.getJugadorId().equals(j.getUuid()) && s.isActiva()) cantidad++;
        return cantidad;
    }

    /** Traduce la reincidencia a un nivel legible. */
    public String getNivelReincidencia(Jugador j) {
        int cantidad = calcularReincidencia(j);
        if (cantidad == 0) return "Sin antecedentes";
        if (cantidad <= 2) return "Bajo";
        if (cantidad <= 4) return "Medio";
        return "Alto";
    }

    /** ¿El jugador está silenciado ahora mismo? (lo usa MuteEnforcementListener). */
    public synchronized boolean estaMuteado(UUID jugadorId) {
        return obtenerMuteActivo(jugadorId) != null;
    }

    public synchronized Mute obtenerMuteActivo(UUID jugadorId) {
        limpiarExpiradas();
        for (Sancion s : sancionesActivas)
            if (s instanceof Mute mute && s.getJugadorId().equals(jugadorId)) return mute;
        return null;
    }

    /** Saca de sancionesActivas las que ya vencieron (siguen en el historial). */
    public synchronized void limpiarExpiradas() {
        Instant ahora = Instant.now();
        sancionesActivas.removeIf(s -> !s.estaVigente(ahora));
    }

    public synchronized ArrayList<Sancion> getSancionesActivas() {
        limpiarExpiradas();
        return new ArrayList<>(sancionesActivas);
    }

    public synchronized ArrayList<Sancion> getHistorial() {
        return new ArrayList<>(historial);
    }

    // ------------------------------------------------------------------
    // Persistencia
    // ------------------------------------------------------------------

    /** Guarda todo el historial en sanciones.txt (guardarDatos de IPersistencia). También se llama en onDisable(). */
    public synchronized boolean guardar() {
        ArrayList<String> lineas = new ArrayList<>();
        for (Sancion s : historial) lineas.add(s.toLinea());
        boolean guardado = persistencia.guardarDatos(ARCHIVO, lineas);
        if (!guardado) plugin.getLogger().severe("No se pudo guardar " + ARCHIVO + ".");
        return guardado;
    }

    /** Agrega la sanción y la guarda; si no se puede guardar, la deshace. */
    private void registrarYGuardar(Sancion s) throws IOException {
        historial.add(s);
        sancionesActivas.add(s);
        if (!guardar()) {
            historial.remove(s);
            sancionesActivas.remove(s);
            throw new IOException("No se pudo escribir " + ARCHIVO + ".");
        }
    }

    /**
     * Lee sanciones.txt (cargarDatos de IPersistencia) y pasa a ese archivo lo que haya quedado
     * guardado en sanciones.yml (historial o baneos de versiones anteriores), sin repetir sanciones.
     */
    private void cargar() {
        Set<UUID> idsCargados = new HashSet<>();
        for (String linea : persistencia.cargarDatos(ARCHIVO)) agregarDesdeLinea(linea, idsCargados);

        int migrados = 0;
        try {
            for (String linea : persistencia.cargarRegistrosSanciones())
                if (agregarDesdeLinea(linea, idsCargados)) migrados++;
            for (Baneo legado : persistencia.cargarBaneos())
                if (idsCargados.add(legado.getId())) { historial.add(legado); migrados++; }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "No se pudo leer sanciones.yml para pasar las sanciones antiguas.", exception);
        }
        if (migrados > 0 && guardar())
            plugin.getLogger().info("Se pasaron " + migrados + " sanciones de sanciones.yml a " + ARCHIVO + ".");

        // Los baneos repetidos de sanciones.yml que se descartaron gastaron números: se liberan.
        int mayorNumero = 0;
        for (Sancion s : historial) mayorNumero = Math.max(mayorNumero, s.getNumero());
        Sancion.fijarContador(mayorNumero);

        Instant ahora = Instant.now();
        for (Sancion s : historial) if (s.estaVigente(ahora)) sancionesActivas.add(s);
        plugin.getLogger().info("Se cargaron " + historial.size() + " sanciones (" + sancionesActivas.size() + " activas).");
    }

    /** Reconstruye una sanción desde su línea; devuelve false si está dañada o ya estaba cargada. */
    private boolean agregarDesdeLinea(String linea, Set<UUID> idsCargados) {
        try {
            Sancion s = Sancion.desdeLinea(linea.trim());
            if (!idsCargados.add(s.getId())) return false;
            historial.add(s);
            return true;
        } catch (SancionInvalidaException exception) {
            plugin.getLogger().warning("Registro ignorado en " + ARCHIVO + ": " + exception.getMessage());
            return false;
        }
    }
}
