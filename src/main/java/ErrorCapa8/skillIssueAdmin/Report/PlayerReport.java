package ErrorCapa8.skillIssueAdmin.Report;

import java.time.Instant;
import java.util.UUID;

public final class PlayerReport {
    private final UUID id;
    private final UUID reporterId;
    private final String reporterName;
    private final UUID targetId;
    private final String targetName;
    private final ReportType type;
    private final Instant createdAt;
    private ReportPriority priority;
    private boolean resolved;
    private String resolvedBy;
    private Instant resolvedAt;

    public PlayerReport(UUID reporterId, String reporterName, UUID targetId, String targetName, ReportType type) {
        this(UUID.randomUUID(), reporterId, reporterName, targetId, targetName, type, Instant.now(), ReportPriority.MEDIUM, false, "", null);
    }
    public PlayerReport(UUID id, UUID reporterId, String reporterName, UUID targetId, String targetName,
                        ReportType type, Instant createdAt, ReportPriority priority, boolean resolved,
                        String resolvedBy, Instant resolvedAt) {
        this.id = id; this.reporterId = reporterId; this.reporterName = reporterName;
        this.targetId = targetId; this.targetName = targetName; this.type = type;
        this.createdAt = createdAt; this.priority = priority; this.resolved = resolved;
        this.resolvedBy = resolvedBy; this.resolvedAt = resolvedAt;
    }
    public UUID id() { return id; }
    public UUID reporterId() { return reporterId; }
    public String reporterName() { return reporterName; }
    public UUID targetId() { return targetId; }
    public String targetName() { return targetName; }
    public ReportType type() { return type; }
    public Instant createdAt() { return createdAt; }
    public ReportPriority priority() { return priority; }
    public boolean resolved() { return resolved; }
    public String resolvedBy() { return resolvedBy; }
    public Instant resolvedAt() { return resolvedAt; }
    public void setPriority(ReportPriority priority) { this.priority = priority; }
    public void setResolved(boolean value, String staff) {
        resolved = value; resolvedBy = value ? staff : ""; resolvedAt = value ? Instant.now() : null;
    }
}
