package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Clase abstracta base de todas las sanciones (Baneo, Mute, Advertido).
 *
 * Cada subclase implementa aplicarEfecto(Jugador) a su manera: polimorfismo de inclusión.
 * GestorSanciones trabaja con "Sancion" sin saber de qué tipo concreto es.
 *
 * Línea en sanciones.txt (los campos extra dependen de la subclase):
 *   TIPO;numero;uuid;jugadorId;jugadorNombre;responsableId;responsableNombre;creadaEnMillis;motivo;activa;motivoPerdon;extra...
 */
public abstract class Sancion {
    /** Separador de campos en sanciones.txt (el mismo que usa PersistenciaArchivo). */
    public static final String SEPARADOR = ";";
    /** Formato para mostrar fechas en el chat. */
    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /** Contador estático compartido por todas las sanciones: genera el número corto (#1, #2...). */
    private static int contadorSanciones = 0;

    // --- Atributos originales (Felipe) ---
    private final UUID id;
    private final UUID jugadorId;
    private final String jugadorNombre;
    private final UUID responsableId;
    private final String responsableNombre;
    private final Instant creadaEn;

    // --- Atributos del módulo de sanciones ---
    private final int numero;          // id corto (int) que usan los admins: /perdonar 5
    private final String motivo;
    private boolean activa;            // false = revocada (perdonada)
    private String motivoPerdon;

    /** Constructor original de Felipe: se conserva para no romper Baneo ni la carga desde sanciones.yml. */
    protected Sancion(UUID id, UUID jugadorId, String jugadorNombre, UUID responsableId,
                      String responsableNombre, Instant creadaEn) {
        this(id, getSiguienteId(), jugadorId, jugadorNombre, responsableId, responsableNombre, creadaEn, "", true, "");
    }

    /** Constructor completo: lo usan Mute, Advertido y la carga desde sanciones.txt. */
    protected Sancion(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId,
                      String responsableNombre, Instant creadaEn, String motivo, boolean activa, String motivoPerdon) {
        this.id = id; this.jugadorId = jugadorId; this.jugadorNombre = jugadorNombre;
        this.responsableId = responsableId; this.responsableNombre = responsableNombre; this.creadaEn = creadaEn;
        this.numero = numero;
        this.motivo = motivo == null ? "" : limpiar(motivo);
        this.activa = activa;
        this.motivoPerdon = motivoPerdon == null ? "" : limpiar(motivoPerdon);
        actualizarContador(numero);
    }

    // ------------------------------------------------------------------
    // Contador estático
    // ------------------------------------------------------------------

    /** Devuelve el siguiente número de sanción disponible. */
    public static synchronized int getSiguienteId() {
        contadorSanciones++;
        return contadorSanciones;
    }

    /** Al cargar sanciones guardadas, el contador sube al mayor número leído para no repetirlos. */
    protected static synchronized void actualizarContador(int numeroExistente) {
        if (numeroExistente > contadorSanciones) contadorSanciones = numeroExistente;
    }

    /** Lo usa GestorSanciones al terminar de cargar: deja el contador en el mayor número en uso. */
    static synchronized void fijarContador(int valor) {
        contadorSanciones = valor;
    }

    // ------------------------------------------------------------------
    // Métodos abstractos (cada subclase los implementa)
    // ------------------------------------------------------------------

    /**
     * Aplica el efecto de la sanción sobre el jugador dentro del juego.
     * @return true si se aplicó en el momento; false si el jugador no está conectado
     *         (la sanción igual queda registrada y guardada).
     */
    public abstract boolean aplicarEfecto(Jugador j);

    /** "BANEO", "MUTE" o "ADVERTIDO". */
    public abstract String getTipo();

    /** Campos propios de la subclase para sanciones.txt. */
    protected abstract String datosExtra();

    // ------------------------------------------------------------------
    // Lógica común
    // ------------------------------------------------------------------

    /**
     * Revoca (perdona) la sanción.
     * @throws SancionInvalidaException si ya estaba revocada.
     */
    public void revocar(String motivoPerdon) throws SancionInvalidaException {
        if (!activa) throw new SancionInvalidaException("La sanción #" + numero + " ya estaba revocada.");
        this.activa = false;
        this.motivoPerdon = motivoPerdon == null || motivoPerdon.isBlank() ? "Sin motivo especificado" : limpiar(motivoPerdon);
    }

    /** Revierte una revocación si no pudo persistirse, para no dejar el estado en memoria divergente. */
    final void restaurarActiva() { this.activa = true; this.motivoPerdon = ""; }

    /** Por defecto una sanción no vence; Baneo y Mute lo sobrescriben según su duración. */
    public boolean haExpirado(Instant ahora) { return false; }

    /** Vigente = no revocada y no vencida. */
    public boolean estaVigente(Instant ahora) { return activa && !haExpirado(ahora); }

    /** Texto corto para listar en el chat (/sanciones). */
    public String getResumen() {
        String estado = !activa ? "REVOCADA" : haExpirado(Instant.now()) ? "EXPIRADA" : "VIGENTE";
        return "#" + numero + " [" + getTipo() + "] " + getMotivo() + " - " + getFecha()
                + " (por " + responsableNombre + ") " + estado;
    }

    /** Convierte la sanción en una línea para sanciones.txt. */
    public String toLinea() {
        return getTipo() + SEPARADOR + numero + SEPARADOR + id + SEPARADOR + jugadorId + SEPARADOR
                + limpiar(jugadorNombre) + SEPARADOR + responsableId + SEPARADOR + limpiar(responsableNombre) + SEPARADOR
                + creadaEn.toEpochMilli() + SEPARADOR + vacioComoGuion(motivo) + SEPARADOR + activa + SEPARADOR
                + vacioComoGuion(motivoPerdon) + SEPARADOR + datosExtra();
    }

    /** PersistenciaArchivo escribe toString(), así que devuelve la misma línea. */
    @Override public String toString() { return toLinea(); }

    /**
     * Reconstruye la sanción correcta (Baneo, Mute o Advertido) desde una línea de sanciones.txt.
     * @throws SancionInvalidaException si la línea está dañada.
     */
    public static Sancion desdeLinea(String linea) throws SancionInvalidaException {
        String[] p = linea.split(SEPARADOR, -1);
        if (p.length < 13) throw new SancionInvalidaException("Línea incompleta: " + linea);
        try {
            String tipo = p[0].trim().toUpperCase(java.util.Locale.ROOT);
            int numero = Integer.parseInt(p[1].trim());
            UUID id = UUID.fromString(p[2].trim());
            UUID jugadorId = UUID.fromString(p[3].trim());
            String jugadorNombre = p[4];
            UUID responsableId = UUID.fromString(p[5].trim());
            String responsableNombre = p[6];
            Instant creadaEn = Instant.ofEpochMilli(Long.parseLong(p[7].trim()));
            String motivo = guionComoVacio(p[8]);
            boolean activa = Boolean.parseBoolean(p[9].trim());
            String perdon = guionComoVacio(p[10]);
            switch (tipo) {
                case "BANEO":
                    if (p.length < 16) throw new SancionInvalidaException("Baneo incompleto: " + linea);
                    boolean permanente = Boolean.parseBoolean(p[14].trim());
                    Instant expiraEn = permanente ? null : Instant.ofEpochMilli(Long.parseLong(p[15].trim()));
                    return new Baneo(numero, activa, perdon, id, jugadorId, jugadorNombre,
                            CausaBaneo.valueOf(p[11].trim()), guionComoVacio(p[12]), guionComoVacio(p[13]),
                            responsableId, responsableNombre, creadaEn, expiraEn, permanente);
                case "MUTE":
                    return new Mute(id, numero, jugadorId, jugadorNombre, responsableId, responsableNombre,
                            creadaEn, motivo, activa, perdon, Integer.parseInt(p[11].trim()), p[12].trim());
                case "ADVERTIDO":
                    return new Advertido(id, numero, jugadorId, jugadorNombre, responsableId, responsableNombre,
                            creadaEn, motivo, activa, perdon, Integer.parseInt(p[11].trim()), Boolean.parseBoolean(p[12].trim()));
                default:
                    throw new SancionInvalidaException("Tipo de sanción desconocido: " + tipo);
            }
        } catch (IllegalArgumentException exception) { // incluye NumberFormatException y UUID/enum inválidos
            throw new SancionInvalidaException("Dato inválido en la línea: " + linea);
        }
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /** Busca al Player conectado que corresponde al Jugador (o null si está desconectado). */
    protected Player buscarJugadorConectado(Jugador j) {
        UUID uuid = j != null ? j.getUuid() : jugadorId;
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline() ? player : null;
    }

    /** Quita ';' y saltos de línea para no dañar el formato del archivo. */
    protected static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(SEPARADOR, ",").replace("\n", " ").replace("\r", " ").trim();
    }

    protected static String vacioComoGuion(String texto) { return texto == null || texto.isBlank() ? "-" : limpiar(texto); }
    protected static String guionComoVacio(String texto) { return texto == null || texto.equals("-") ? "" : texto; }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public UUID getId() { return id; }
    public UUID getJugadorId() { return jugadorId; }
    public String getJugadorNombre() { return jugadorNombre; }
    public UUID getResponsableId() { return responsableId; }
    public String getResponsableNombre() { return responsableNombre; }
    public Instant getCreadaEn() { return creadaEn; }

    public int getNumero() { return numero; }
    public String getMotivo() { return motivo; }
    public String getFecha() { return FORMATO_FECHA.format(creadaEn); }
    public String getAdminResponsable() { return responsableNombre; }
    public boolean isActiva() { return activa; }
    public String getMotivoPerdon() { return motivoPerdon; }
}
