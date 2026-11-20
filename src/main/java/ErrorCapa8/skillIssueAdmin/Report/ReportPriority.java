package ErrorCapa8.skillIssueAdmin.Report;

import org.bukkit.Material;

public enum ReportPriority {
    LOW("Bajo", Material.LIME_DYE), MEDIUM("Medio", Material.YELLOW_DYE), URGENT("Urgente", Material.RED_DYE);
    private final String label;
    private final Material icon;
    ReportPriority(String label, Material icon) { this.label = label; this.icon = icon; }
    public String label() { return label; }
    public Material icon() { return icon; }
    public ReportPriority next(int step) { return values()[Math.floorMod(ordinal() + step, values().length)]; }
}
