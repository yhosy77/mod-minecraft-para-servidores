package ErrorCapa8.skillIssueAdmin.Report;

import ErrorCapa8.skillIssueAdmin.Rank.IPersistencia;
import ErrorCapa8.skillIssueAdmin.SkillIssueAdmin;
import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Gestor de reportes: crea, atiende, filtra y guarda los reportes.
 *
 * Persistencia (IPersistencia): reportes.txt, una línea por reporte. Se carga en el
 * constructor y se guarda después de cada cambio. Los reportes del reports.yml anterior
 * se pasan una sola vez a reportes.txt.
 */
public final class ReportCreationManager {
    public static final String ARCHIVO = "reportes.txt";
    private static final long COOLDOWN_MILLIS = 5 * 60 * 1000L;

    private final SkillIssueAdmin plugin;
    private final IPersistencia persistencia;
    private final File archivoAnterior;
    private final List<PlayerReport> reports = new ArrayList<>();
    private final Map<UUID, Long> lastReportAt = new ConcurrentHashMap<>();

    public ReportCreationManager(SkillIssueAdmin plugin, IPersistencia persistencia) {
        this.plugin = plugin;
        this.persistencia = persistencia;
        this.archivoAnterior = new File(plugin.getDataFolder(), "reports.yml");
        cargar();
    }

    // ------------------------------------------------------------------
    // Crear y atender reportes (RF-01, RF-06)
    // ------------------------------------------------------------------

    public long remainingCooldownMillis(UUID reporter) {
        Long last = lastReportAt.get(reporter);
        return last == null ? 0 : Math.max(0, COOLDOWN_MILLIS - (System.currentTimeMillis() - last));
    }

    /**
     * Crea un reporte (RF-01): no permite auto-reportes ni motivos vacíos, y respeta el tiempo de espera.
     * @throws IllegalArgumentException si los datos no son válidos.
     * @throws IllegalStateException si el jugador todavía está en tiempo de espera.
     */
    public synchronized PlayerReport crearReporte(Player reporter, Player target, ReportType type, String motivo) {
        if (reporter == null || target == null) throw new IllegalArgumentException("El jugador reportado no existe o no está conectado.");
        if (reporter.getUniqueId().equals(target.getUniqueId())) throw new IllegalArgumentException("No puedes reportarte a ti mismo.");
        if (type == null) throw new IllegalArgumentException("Debes elegir el tipo de reporte.");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo del reporte no puede estar vacío.");
        long remaining = remainingCooldownMillis(reporter.getUniqueId());
        if (remaining > 0) {
            long seconds = (remaining + 999) / 1000;
            throw new IllegalStateException("Espera " + (seconds / 60) + " min " + (seconds % 60) + " s para enviar otro reporte.");
        }
        PlayerReport report = new PlayerReport(reporter.getUniqueId(), reporter.getName(), target.getUniqueId(), target.getName(), type, motivo);
        reports.add(report);
        lastReportAt.put(reporter.getUniqueId(), System.currentTimeMillis());
        guardar();
        return report;
    }

    /**
     * Cambia el estado de un reporte y registra al admin que lo atendió (RF-06).
     * @throws IllegalStateException si el cambio no está permitido (por ejemplo, reabrir uno cerrado).
     */
    public synchronized void resolverReporte(PlayerReport report, EstadoReporte estado, String staff) {
        report.cambiarEstado(estado, staff);
        guardar();
    }

    public synchronized void setPriority(PlayerReport report, ReportPriority priority) {
        report.setPriority(priority);
        guardar();
    }

    public synchronized PlayerReport buscarReporte(int numero) {
        for (PlayerReport report : reports) if (report.numero() == numero) return report;
        return null;
    }

    /** Todos los reportes: primero los abiertos, luego por prioridad y del más nuevo al más viejo. */
    public synchronized List<PlayerReport> reports() {
        return reports.stream().sorted(Comparator.comparing(PlayerReport::resolved)
                .thenComparing((PlayerReport r) -> r.priority().ordinal(), Comparator.reverseOrder())
                .thenComparing(PlayerReport::createdAt, Comparator.reverseOrder())).toList();
    }

    // ------------------------------------------------------------------
    // Filtros (RF-09) - sobrecarga de métodos
    // ------------------------------------------------------------------

    /**
     * Filtra solo por estado ("abierto", "en_revision", "resuelto" o "descartado").
     * @throws IllegalArgumentException si el estado no existe.
     */
    public synchronized List<PlayerReport> filtrarReportes(String estado) {
        EstadoReporte buscado = EstadoReporte.desdeTexto(estado);
        if (buscado == null)
            throw new IllegalArgumentException("Estado inválido. Usa: abierto, en_revision, resuelto o descartado.");
        return filtrarReportes(new CriterioFiltro(buscado, null, null));
    }

    /** Filtra con un criterio completo (estado y/o rango de fechas). */
    public synchronized List<PlayerReport> filtrarReportes(CriterioFiltro criterio) {
        return reports().stream().filter(criterio::coincide).toList();
    }

    /**
     * Criterio de búsqueda de reportes (clase anidada estática).
     * Cualquier campo en null no se tiene en cuenta. El rango de fechas incluye el día inicial y el final.
     */
    public static final class CriterioFiltro {
        private final EstadoReporte estadoBuscado;
        private final LocalDate fechaDesde;
        private final LocalDate fechaHasta;

        public CriterioFiltro(EstadoReporte estadoBuscado, LocalDate fechaDesde, LocalDate fechaHasta) {
            if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde))
                throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial.");
            this.estadoBuscado = estadoBuscado;
            this.fechaDesde = fechaDesde;
            this.fechaHasta = fechaHasta;
        }

        public boolean coincide(PlayerReport report) {
            if (estadoBuscado != null && report.estado() != estadoBuscado) return false;
            LocalDate fecha = LocalDate.ofInstant(report.createdAt(), ZoneId.systemDefault());
            if (fechaDesde != null && fecha.isBefore(fechaDesde)) return false;
            return fechaHasta == null || !fecha.isAfter(fechaHasta);
        }

        public EstadoReporte getEstadoBuscado() { return estadoBuscado; }
        public LocalDate getFechaDesde() { return fechaDesde; }
        public LocalDate getFechaHasta() { return fechaHasta; }
    }

    // ------------------------------------------------------------------
    // Persistencia
    // ------------------------------------------------------------------

    private void guardar() {
        List<String> lineas = new ArrayList<>();
        for (PlayerReport report : reports) lineas.add(report.toLinea());
        if (!persistencia.guardarDatos(ARCHIVO, lineas))
            plugin.getLogger().severe("No se pudieron guardar los reportes en " + ARCHIVO + ".");
    }

    private void cargar() {
        int numeroLinea = 0;
        for (String linea : persistencia.cargarDatos(ARCHIVO)) {
            numeroLinea++;
            try {
                reports.add(PlayerReport.desdeLinea(linea));
            } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
                plugin.getLogger().warning("Se omitió la línea " + numeroLinea + " de " + ARCHIVO + " porque está dañada.");
            }
        }
        if (reports.isEmpty() && archivoAnterior.exists() && migrarDesdeYaml() > 0) guardar();
        plugin.getLogger().info("Se cargaron " + reports.size() + " reportes guardados.");
    }

    /** Pasa los reportes del reports.yml de la versión anterior al formato nuevo. */
    private int migrarDesdeYaml() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivoAnterior);
        ConfigurationSection section = yaml.getConfigurationSection("reports");
        if (section == null) return 0;
        int migrados = 0;
        for (String idText : section.getKeys(false)) {
            String path = "reports." + idText + ".";
            try {
                ReportType type = ReportType.valueOf(yaml.getString(path + "type", "OTHER"));
                long resolvedAt = yaml.getLong(path + "resolvedAt", 0L);
                boolean resuelto = yaml.getBoolean(path + "resolved", false);
                reports.add(new PlayerReport(PlayerReport.getSiguienteId(), UUID.fromString(idText),
                        UUID.fromString(yaml.getString(path + "reporterUuid", "")), yaml.getString(path + "reporterName", "Desconocido"),
                        UUID.fromString(yaml.getString(path + "targetUuid", "")), yaml.getString(path + "targetName", "Desconocido"),
                        type, type.label(), Instant.ofEpochMilli(yaml.getLong(path + "createdAt", System.currentTimeMillis())),
                        ReportPriority.valueOf(yaml.getString(path + "priority", "MEDIUM")),
                        resuelto ? EstadoReporte.RESUELTO : EstadoReporte.ABIERTO, yaml.getString(path + "resolvedBy", ""),
                        resolvedAt == 0 ? null : Instant.ofEpochMilli(resolvedAt)));
                migrados++;
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Se omitió el reporte antiguo " + idText + " porque sus datos no son válidos.");
            }
        }
        if (migrados > 0) plugin.getLogger().info("Se pasaron " + migrados + " reportes de reports.yml a " + ARCHIVO + ".");
        return migrados;
    }
}
