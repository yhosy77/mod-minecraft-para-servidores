package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.JugadorNoEncontradoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GestorJugadores {
    public static final String ARCHIVO_JUGADORES = "jugadores.txt";
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[\\p{L}\\p{N}_ -]{1,24}");
    private final JavaPlugin plugin;
    private final IPersistencia persistencia;
    private final Map<String, Rango> rangos = new LinkedHashMap<>();
    private final Map<UUID, Jugador> jugadoresPorUuid = new LinkedHashMap<>();
    private final ArrayList<Jugador> jugadores = new ArrayList<>();
    private String rangoPredeterminadoId = "player";

    public GestorJugadores(IPersistencia persistencia) {
        this(null, persistencia);
    }

    public GestorJugadores(JavaPlugin plugin, IPersistencia persistencia) {
        if (persistencia == null) throw new IllegalArgumentException("La persistencia no puede ser nula.");
        this.plugin = plugin;
        this.persistencia = persistencia;
        cargar();
    }

    private void cargar() {
        boolean changed = false;
        try {
            PersistenciaDatos datos = persistencia.cargar();
            for (Rango rango : datos.rangos()) rangos.put(rango.getId(), rango);
            for (Jugador jugador : datos.jugadores()) jugadoresPorUuid.put(jugador.getUuid(), jugador);
            rangoPredeterminadoId = datos.rangoPredeterminadoId();
        } catch (IOException | RuntimeException exception) {
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "No se pudieron cargar los rangos; se utilizarán los rangos base.", exception);
            }
            changed = true;
        }
        if (jugadoresPorUuid.isEmpty() && persistencia.verificarArchivo(ARCHIVO_JUGADORES)) {
            for (String linea : persistencia.cargarDatos(ARCHIVO_JUGADORES)) {
                Jugador cargado = Jugador.desdeLinea(linea);
                if (cargado != null) {
                    jugadoresPorUuid.put(cargado.getUuid(), cargado);
                }
            }
        }
        changed |= migrarBaseModeradorAOp();
        changed |= asegurarRangosBase();
        Rango selectedDefault = rangos.get(rangoPredeterminadoId);
        if (selectedDefault == null || !selectedDefault.esValidoComoPredeterminado()) {
            rangoPredeterminadoId = "player";
            changed = true;
        }
        for (Jugador jugador : jugadoresPorUuid.values()) {
            if (!rangos.containsKey(jugador.getRangoId())) {
                jugador.asignarRango(rangoPredeterminadoId);
                changed = true;
            }
        }
        sincronizarListaJugadores();
        if (changed) guardar();
    }

    private void sincronizarListaJugadores() {
        jugadores.clear();
        jugadores.addAll(jugadoresPorUuid.values());
    }

    private boolean asegurarRangosBase() {
        boolean changed = false;
        changed |= asegurarBase("admin", "ADMIN", TipoRango.ADMIN);
        changed |= asegurarBase("op", "OP", TipoRango.OP);
        changed |= asegurarBase("player", "PLAYER", TipoRango.PLAYER);
        return changed;
    }

    private boolean asegurarBase(String id, String nombre, TipoRango tipo) {
        Rango existente = rangos.get(id);
        if (existente == null) {
            rangos.put(id, new Rango(id, nombre, tipo, true));
            return true;
        }
        boolean changed = !existente.esBase() || existente.getTipo() != tipo;
        if (existente.getTipo() != tipo) rangos.put(id, new Rango(id, existente.getNombre(), tipo, true, existente.getColor()));
        else if (!existente.esBase()) existente.setBase(true);
        return changed;
    }

    private boolean migrarBaseModeradorAOp() {
        Rango oldMod = rangos.get("mod");
        if (oldMod == null) return false;
        if (!oldMod.esBase()) return false;
        rangos.remove("mod");
        Rango currentOp = rangos.get("op");
        if (currentOp == null) {
            rangos.put("op", new Rango("op", oldMod.getNombre(), TipoRango.OP, true, oldMod.getColor()));
        }
        for (Jugador jugador : jugadoresPorUuid.values()) if (jugador.getRangoId().equals("mod")) jugador.asignarRango("op");
        if ("mod".equals(rangoPredeterminadoId)) rangoPredeterminadoId = "player";
        return true;
    }

    // ------------------------------------------------------------------
    // Métodos de integración de Juan Bello (gestión y archivo plano)
    // ------------------------------------------------------------------

    public synchronized void registrarJugador(Jugador j) {
        if (j == null) throw new IllegalArgumentException("El jugador no puede ser nulo.");
        if (jugadoresPorUuid.containsKey(j.getUuid())) {
            throw new IllegalArgumentException("Ya existe un jugador registrado con el UUID " + j.getUuid() + ".");
        }
        for (Jugador existente : jugadores) {
            if (existente.getNombre().equalsIgnoreCase(j.getNombre())) {
                throw new IllegalArgumentException("Ya existe un jugador registrado con el nombre " + j.getNombre() + ".");
            }
        }
        if (!rangos.containsKey(j.getRangoId())) {
            j.asignarRango(rangoPredeterminadoId);
        }
        jugadoresPorUuid.put(j.getUuid(), j);
        sincronizarListaJugadores();
        guardar();
    }

    public synchronized Jugador buscarJugador(String id) throws JugadorNoEncontradoException {
        if (id == null || id.isBlank()) {
            throw new JugadorNoEncontradoException("El identificador del jugador no puede estar vacío.");
        }
        String criterio = id.trim();
        for (Jugador j : jugadores) {
            if (j.getUuid().toString().equalsIgnoreCase(criterio) || j.getNombre().equalsIgnoreCase(criterio)) {
                return j;
            }
        }
        throw new JugadorNoEncontradoException("No se encontró ningún jugador con identificador: " + criterio);
    }

    public synchronized Jugador buscarJugador(String nombre, boolean exacto) throws JugadorNoEncontradoException {
        if (nombre == null || nombre.isBlank()) {
            throw new JugadorNoEncontradoException("El nombre del jugador no puede estar vacío.");
        }
        String criterio = nombre.trim();
        String criterioLower = criterio.toLowerCase(Locale.ROOT);
        for (Jugador j : jugadores) {
            boolean coincide = exacto
                    ? j.getNombre().equalsIgnoreCase(criterio)
                    : j.getNombre().toLowerCase(Locale.ROOT).contains(criterioLower);
            if (coincide) {
                return j;
            }
        }
        throw new JugadorNoEncontradoException("No se encontró ningún jugador con el nombre: " + criterio);
    }

    public synchronized boolean eliminarJugador(String id) {
        if (id == null || id.isBlank()) return false;
        String criterio = id.trim();
        Jugador objetivo = null;
        for (Jugador j : jugadores) {
            if (j.getUuid().toString().equalsIgnoreCase(criterio) || j.getNombre().equalsIgnoreCase(criterio)) {
                objetivo = j;
                break;
            }
        }
        if (objetivo == null) return false;
        jugadoresPorUuid.remove(objetivo.getUuid());
        sincronizarListaJugadores();
        guardar();
        return true;
    }

    public synchronized void actualizarHoras(String id, int horas) throws JugadorNoEncontradoException {
        if (horas < 0) {
            throw new IllegalArgumentException("Las horas a sumar no pueden ser negativas.");
        }
        Jugador jugador = buscarJugador(id);
        jugador.registrarHoras(horas);
        guardar();
    }

    public static boolean validarConexionGlobal(String ip) {
        if (ip == null || ip.isBlank()) return false;
        String limpia = ip.trim();
        if (limpia.equalsIgnoreCase("localhost") || limpia.equals("::1")) return true;
        String[] octetos = limpia.split("\\.", -1);
        if (octetos.length != 4) return false;
        for (String octeto : octetos) {
            if (!octeto.matches("\\d{1,3}")) return false;
            int valor = Integer.parseInt(octeto);
            if (valor < 0 || valor > 255) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Métodos de gestión de rangos y PaperMC
    // ------------------------------------------------------------------

    public synchronized Collection<Rango> obtenerRangos() {
        return rangos.values().stream().sorted(Comparator.comparing(r -> r.getNombre().toLowerCase(Locale.ROOT))).toList();
    }

    public synchronized List<Rango> obtenerRangos(TipoRango tipo) {
        return rangos.values().stream().filter(r -> r.getTipo() == tipo)
                .sorted(Comparator.comparing(r -> r.getNombre().toLowerCase(Locale.ROOT))).toList();
    }

    public synchronized Rango buscarRango(String id) { return rangos.get(id); }

    public synchronized Rango buscarRangoPorNombre(String nombre) {
        return rangos.values().stream().filter(r -> r.getNombre().equalsIgnoreCase(nombre.trim())).findFirst().orElse(null);
    }

    public synchronized Rango obtenerRangoPredeterminado() { return rangos.get(rangoPredeterminadoId); }

    public synchronized Jugador buscarJugador(UUID uuid) { return jugadoresPorUuid.get(uuid); }

    public synchronized Rango obtenerRangoJugador(UUID uuid) {
        Jugador jugador = jugadoresPorUuid.get(uuid);
        return jugador == null ? null : rangos.get(jugador.getRangoId());
    }

    public synchronized boolean esAdministrador(UUID uuid) {
        Rango rango = obtenerRangoJugador(uuid);
        return rango != null && rango.getTipo().puedeGestionarRangos();
    }

    public synchronized List<Jugador> jugadoresConRango(String rangoId) {
        return jugadores.stream().filter(j -> j.getRangoId().equals(rangoId)).toList();
    }

    public synchronized void registrarOActualizarJugador(Player player) {
        Jugador jugador = jugadoresPorUuid.get(player.getUniqueId());
        if (jugador == null) {
            String rankId = player.isOp() ? "admin" : rangoPredeterminadoId;
            jugadoresPorUuid.put(player.getUniqueId(), new Jugador(player.getUniqueId(), player.getName(), rankId));
            sincronizarListaJugadores();
            guardar();
        } else {
            if (!jugador.getNombre().equals(player.getName())) {
                jugador.actualizar(player.getName(), jugador.getRangoId());
                sincronizarListaJugadores();
                guardar();
            }
        }
    }

    public synchronized Rango crearRango(String nombre, TipoRango tipo) {
        return crearRango(nombre, tipo, Rango.colorPredeterminado(tipo), java.util.Set.of());
    }

    public synchronized Rango crearRango(String nombre, TipoRango tipo, net.kyori.adventure.text.format.TextColor color,
                                          java.util.Set<net.kyori.adventure.text.format.TextDecoration> formatos) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (tipo == null) throw new IllegalArgumentException("Selecciona un tipo de rango válido.");
        String clean = nombre.trim();
        if (!NOMBRE_VALIDO.matcher(clean).matches()) throw new IllegalArgumentException("Usa de 1 a 24 letras, números, espacios, guiones o guiones bajos.");
        if (buscarRangoPorNombre(clean) != null) throw new IllegalArgumentException("Ya existe un rango con ese nombre.");
        String id = Rango.normalizarId(clean);
        if (rangos.containsKey(id)) id += "-" + UUID.randomUUID().toString().substring(0, 8);
        Rango rango = new Rango(id, clean, tipo, false, color, formatos);
        rangos.put(id, rango);
        guardar();
        return rango;
    }

    public synchronized void establecerRangoPredeterminado(String rangoId) {
        Rango rango = rangos.get(rangoId);
        if (rango == null) throw new IllegalArgumentException("Ese rango ya no existe.");
        if (!rango.esValidoComoPredeterminado()) throw new IllegalArgumentException("El predeterminado debe ser PLAYER y no tener permisos de staff.");
        rangoPredeterminadoId = rangoId;
        guardar();
    }

    public synchronized List<Jugador> obtenerJugadores() {
        return jugadores.stream().sorted(Comparator.comparing(j -> j.getNombre().toLowerCase(Locale.ROOT))).toList();
    }

    public synchronized void actualizarProgreso(UUID uuid, int nivel, long monedas, long horasJugadas) {
        Jugador jugador = jugadoresPorUuid.get(uuid);
        if (jugador == null) throw new IllegalArgumentException("El jugador no está registrado.");
        jugador.actualizarProgreso(nivel, monedas, horasJugadas);
        guardar();
    }

    public synchronized void asignarRango(UUID jugadorId, String rangoId) {
        Jugador jugador = jugadoresPorUuid.get(jugadorId);
        Rango rango = rangos.get(rangoId);
        if (jugador == null) throw new IllegalArgumentException("El jugador no está registrado.");
        if (rango == null) throw new IllegalArgumentException("El rango seleccionado ya no existe.");
        jugador.asignarRango(rangoId);
        guardar();
    }

    public synchronized void cambiarEstilo(String rangoId, net.kyori.adventure.text.format.TextColor color,
                                           java.util.Set<net.kyori.adventure.text.format.TextDecoration> formatos) {
        Rango rango = rangos.get(rangoId);
        if (rango == null) throw new IllegalArgumentException("El rango seleccionado ya no existe.");
        rango.setColor(color);
        rango.setFormatos(formatos);
        guardar();
    }

    public synchronized void eliminarRango(String rangoId) {
        Rango rango = rangos.get(rangoId);
        if (rango == null) throw new IllegalArgumentException("Ese rango ya no existe.");
        if (rango.esBase()) throw new IllegalArgumentException("Los rangos base no se pueden eliminar.");
        if (rango.getTipo() == TipoRango.ADMIN && obtenerRangos(TipoRango.ADMIN).size() <= 1)
            throw new IllegalArgumentException("No puedes eliminar el último rango ADMIN.");
        if (rangoId.equals(rangoPredeterminadoId))
            throw new IllegalArgumentException("Primero establece otro rango PLAYER como predeterminado.");
        String fallbackId = switch (rango.getTipo()) {
            case ADMIN -> "admin";
            case OP, MOD -> "op";
            case PLAYER -> rangoPredeterminadoId;
        };
        for (Jugador jugador : jugadores) if (jugador.getRangoId().equals(rangoId)) jugador.asignarRango(fallbackId);
        rangos.remove(rangoId);
        guardar();
    }

    private void guardar() {
        try {
            persistencia.guardar(rangos.values(), jugadores, rangoPredeterminadoId);
        } catch (IOException | RuntimeException exception) {
            if (plugin != null) {
                plugin.getLogger().log(Level.SEVERE, "No se pudieron guardar los cambios de rangos.", exception);
            }
        }
        persistencia.guardarDatos(ARCHIVO_JUGADORES, new ArrayList<>(jugadores));
    }
}
