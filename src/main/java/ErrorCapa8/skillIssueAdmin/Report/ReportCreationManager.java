package ErrorCapa8.skillIssueAdmin.Report;

import ErrorCapa8.skillIssueAdmin.SkillIssueAdmin;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class ReportCreationManager {
    private static final long COOLDOWN_MILLIS = 5 * 60 * 1000L;
    private final SkillIssueAdmin plugin;
    private final File file;
    private final List<PlayerReport> reports = new ArrayList<>();
    private final Map<UUID, Long> lastReportAt = new ConcurrentHashMap<>();

    public ReportCreationManager(SkillIssueAdmin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "reports.yml");
        load();
    }
    public long remainingCooldownMillis(UUID reporter) {
        Long last = lastReportAt.get(reporter);
        return last == null ? 0 : Math.max(0, COOLDOWN_MILLIS - (System.currentTimeMillis() - last));
    }
    public synchronized boolean submit(Player reporter, Player target, ReportType type) {
        if (remainingCooldownMillis(reporter.getUniqueId()) > 0) return false;
        reports.add(new PlayerReport(reporter.getUniqueId(), reporter.getName(), target.getUniqueId(), target.getName(), type));
        lastReportAt.put(reporter.getUniqueId(), System.currentTimeMillis());
        save();
        return true;
    }
    public synchronized List<PlayerReport> reports() {
        return reports.stream().sorted(Comparator.comparing(PlayerReport::resolved)
                .thenComparing((PlayerReport r) -> r.priority().ordinal(), Comparator.reverseOrder())
                .thenComparing(PlayerReport::createdAt, Comparator.reverseOrder())).toList();
    }
    public synchronized void setPriority(PlayerReport report, ReportPriority priority) { report.setPriority(priority); save(); }
    public synchronized void setResolved(PlayerReport report, boolean value, String staff) { report.setResolved(value, staff); save(); }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("reports");
        if (section == null) return;
        for (String idText : section.getKeys(false)) {
            String path = "reports." + idText + ".";
            try {
                UUID id = UUID.fromString(idText);
                UUID reporterId = UUID.fromString(yaml.getString(path + "reporterUuid", ""));
                UUID targetId = UUID.fromString(yaml.getString(path + "targetUuid", ""));
                ReportType type = ReportType.valueOf(yaml.getString(path + "type", "OTHER"));
                ReportPriority priority = ReportPriority.valueOf(yaml.getString(path + "priority", "MEDIUM"));
                long resolvedAt = yaml.getLong(path + "resolvedAt", 0L);
                reports.add(new PlayerReport(id, reporterId, yaml.getString(path + "reporterName", "Desconocido"),
                        targetId, yaml.getString(path + "targetName", "Desconocido"), type,
                        Instant.ofEpochMilli(yaml.getLong(path + "createdAt", System.currentTimeMillis())), priority,
                        yaml.getBoolean(path + "resolved", false), yaml.getString(path + "resolvedBy", ""),
                        resolvedAt == 0 ? null : Instant.ofEpochMilli(resolvedAt)));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Se omitió el reporte guardado " + idText + " porque sus datos no son válidos.");
            }
        }
        plugin.getLogger().info("Se cargaron " + reports.size() + " reportes guardados.");
    }
    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (PlayerReport report : reports) {
            String path = "reports." + report.id() + ".";
            yaml.set(path + "reporterUuid", report.reporterId().toString());
            yaml.set(path + "reporterName", report.reporterName());
            yaml.set(path + "targetUuid", report.targetId().toString());
            yaml.set(path + "targetName", report.targetName());
            yaml.set(path + "type", report.type().name());
            yaml.set(path + "createdAt", report.createdAt().toEpochMilli());
            yaml.set(path + "priority", report.priority().name());
            yaml.set(path + "resolved", report.resolved());
            yaml.set(path + "resolvedBy", report.resolvedBy());
            yaml.set(path + "resolvedAt", report.resolvedAt() == null ? 0L : report.resolvedAt().toEpochMilli());
        }
        try {
            File folder = file.getParentFile();
            if (!folder.exists() && !folder.mkdirs()) throw new IOException("No se pudo crear " + folder);
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "No se pudieron guardar los reportes en " + file, exception);
        }
    }
}
