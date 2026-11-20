package ErrorCapa8.skillIssueAdmin.Report;

import java.time.Instant;
import java.util.UUID;

/**
 * Reporte de un jugador contra otro.
 *
 * Línea en reportes.txt:
 *   numero;id;reportanteId;reportanteNombre;reportadoId;reportadoNombre;tipo;motivo;creadoEn;prioridad;estado;atendidoPor;atendidoEn
 */
public final class PlayerReport {
    public static final String SEPARADOR = ";";

    /** Contador estático compartido por todos los reportes: da el número corto (#1, #2...). */
    private static int contadorReportes = 0;

    private final int numero;
    private final UUID id;
    private final UUID reporterId;
    private final String reporterName;
    private final UUID targetId;
    private final String targetName;
    private final ReportType type;
    private final String motivo;
    private final Instant createdAt;
    private ReportPriority priority;
    private EstadoReporte estado;
    private String resolvedBy;     // nick del admin que lo atendió por última vez
    private Instant resolvedAt;    // cuándo lo atendió

    /** Reporte nuevo: estado Abierto y prioridad media. */
    public PlayerReport(UUID reporterId, String reporterName, UUID targetId, String targetName, ReportType type, String motivo) {
        this(getSiguienteId(), UUID.randomUUID(), reporterId, reporterName, targetId, targetName, type, motivo,
                Instant.now(), ReportPriority.MEDIUM, EstadoReporte.ABIERTO, "", null);
    }

    public PlayerReport(int numero, UUID id, UUID reporterId, String reporterName, UUID targetId, String targetName,
                        ReportType type, String motivo, Instant createdAt, ReportPriority priority,
                        EstadoReporte estado, String resolvedBy, Instant resolvedAt) {
        this.numero = numero; this.id = id; this.reporterId = reporterId; this.reporterName = reporterName;
        this.targetId = targetId; this.targetName = targetName; this.type = type;
        this.motivo = motivo == null || motivo.isBlank() ? type.label() : motivo.trim();
        this.createdAt = createdAt; this.priority = priority; this.estado = estado;
        this.resolvedBy = resolvedBy == null ? "" : resolvedBy; this.resolvedAt = resolvedAt;
        actualizarContador(numero);
    }

    // ------------------------------------------------------------------
    // Contador estático
    // ------------------------------------------------------------------

    public static synchronized int getSiguienteId() {
        contadorReportes++;
        return contadorReportes;
    }

    static synchronized void actualizarContador(int numeroExistente) {
        if (numeroExistente > contadorReportes) contadorReportes = numeroExistente;
    }

    // ------------------------------------------------------------------
    // Estado (RF-06)
    // ------------------------------------------------------------------

    /**
     * Cambia el estado siguiendo Abierto → En revisión → Resuelto/Descartado y registra al admin.
     * @throws IllegalStateException si el cambio no está permitido (por ejemplo, reabrir un reporte cerrado).
     */
    public void cambiarEstado(EstadoReporte nuevo, String staff) {
        if (estado.estaCerrado())
            throw new IllegalStateException("El reporte #" + numero + " ya está " + estado.etiqueta().toLowerCase() + " y no se puede reabrir.");
        if (!estado.puedePasarA(nuevo))
            throw new IllegalStateException("El reporte #" + numero + " está " + estado.etiqueta().toLowerCase()
                    + "; primero debe pasar a " + estado.siguiente().etiqueta() + ".");
        estado = nuevo;
        resolvedBy = staff == null ? "" : staff;
        resolvedAt = Instant.now();
    }

    // ------------------------------------------------------------------
    // Archivo plano
    // ------------------------------------------------------------------

    public String toLinea() {
        return numero + SEPARADOR + id + SEPARADOR + reporterId + SEPARADOR + limpiar(reporterName) + SEPARADOR
                + targetId + SEPARADOR + limpiar(targetName) + SEPARADOR + type.name() + SEPARADOR + limpiar(motivo) + SEPARADOR
                + createdAt.toEpochMilli() + SEPARADOR + priority.name() + SEPARADOR + estado.name() + SEPARADOR
                + (resolvedBy.isBlank() ? "-" : limpiar(resolvedBy)) + SEPARADOR
                + (resolvedAt == null ? "-" : String.valueOf(resolvedAt.toEpochMilli()));
    }

    @Override public String toString() { return toLinea(); }

    /** @throws IllegalArgumentException si la línea está dañada. */
    public static PlayerReport desdeLinea(String linea) {
        String[] p = linea.split(SEPARADOR, -1);
        if (p.length < 13) throw new IllegalArgumentException("línea incompleta");
        return new PlayerReport(Integer.parseInt(p[0].trim()), UUID.fromString(p[1].trim()),
                UUID.fromString(p[2].trim()), p[3], UUID.fromString(p[4].trim()), p[5],
                ReportType.valueOf(p[6].trim()), p[7], Instant.ofEpochMilli(Long.parseLong(p[8].trim())),
                ReportPriority.valueOf(p[9].trim()), EstadoReporte.valueOf(p[10].trim()),
                p[11].equals("-") ? "" : p[11],
                p[12].trim().equals("-") ? null : Instant.ofEpochMilli(Long.parseLong(p[12].trim())));
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(SEPARADOR, ",").replace("\n", " ").replace("\r", " ").trim();
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public int numero() { return numero; }
    public UUID id() { return id; }
    public UUID reporterId() { return reporterId; }
    public String reporterName() { return reporterName; }
    public UUID targetId() { return targetId; }
    public String targetName() { return targetName; }
    public ReportType type() { return type; }
    public String motivo() { return motivo; }
    public Instant createdAt() { return createdAt; }
    public ReportPriority priority() { return priority; }
    public EstadoReporte estado() { return estado; }
    /** true si el reporte ya está cerrado (Resuelto o Descartado). */
    public boolean resolved() { return estado.estaCerrado(); }
    public String resolvedBy() { return resolvedBy; }
    public Instant resolvedAt() { return resolvedAt; }
    public void setPriority(ReportPriority priority) { this.priority = priority; }
}
