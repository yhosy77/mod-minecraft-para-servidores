package ErrorCapa8.skillIssueAdmin.Report;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ReportAdminMenu implements InventoryHolder {
    static final int[] SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private final Inventory inventory;
    private final int page;
    private final List<PlayerReport> reports;
    public ReportAdminMenu(ReportCreationManager manager, int page) {
        this.page = Math.max(0, page);
        reports = manager.reports();
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Gestión de reportes • " + (this.page + 1)));
        MenuItems.frame(inventory);
        inventory.setItem(4, MenuItems.item(Material.WRITABLE_BOOK, "§bGestión de reportes",
                "§7Clic izquierdo: subir prioridad", "§7Mayús + clic izquierdo: bajar prioridad",
                "§7Clic derecho: Abierto → En revisión → Resuelto", "§7Mayús + clic derecho: descartar (desde En revisión)",
                "§7Un reporte cerrado no se puede reabrir.", "§7Filtros: /gestreports estado | fechas"));
        int from = this.page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && from + i < reports.size(); i++) {
            PlayerReport report = reports.get(from + i);
            long minutes = Math.max(0, Duration.between(report.createdAt(), Instant.now()).toMinutes());
            String color = switch (report.priority()) { case LOW -> "§a"; case MEDIUM -> "§e"; case URGENT -> "§c"; };
            String colorEstado = switch (report.estado()) {
                case ABIERTO -> "§e"; case EN_REVISION -> "§6"; case RESUELTO -> "§a"; case DESCARTADO -> "§7";
            };
            Material icono = switch (report.estado()) {
                case RESUELTO -> Material.LIME_DYE;
                case DESCARTADO -> Material.GRAY_DYE;
                default -> report.priority().icon();
            };
            String atendido = report.resolvedBy().isBlank() ? "§8Sin atender" : "§7Atendido por: §f" + report.resolvedBy();
            inventory.setItem(SLOTS[i], MenuItems.item(icono,
                    "§f#" + report.numero() + " " + report.targetName() + " §8• §e" + report.type().label(),
                    "§7Motivo: §f" + report.motivo(), "§7Reportado por: §f" + report.reporterName(),
                    "§7Prioridad: " + color + report.priority().label(),
                    "§7Estado: " + colorEstado + report.estado().etiqueta(), atendido,
                    "§7Hace: §f" + minutes + " min", "§8Clic izq: prioridad",
                    report.resolved() ? "§8Cerrado: no se puede reabrir" : "§8Clic der: avanzar estado"));
        }
        if (reports.isEmpty()) inventory.setItem(22, MenuItems.item(Material.LIME_DYE, "§aNo hay reportes guardados"));
        if (this.page > 0) inventory.setItem(45, MenuItems.item(Material.ARROW, "§e← Página anterior"));
        inventory.setItem(49, MenuItems.item(Material.BARRIER, "§cCerrar"));
        if (from + SLOTS.length < reports.size()) inventory.setItem(53, MenuItems.item(Material.ARROW, "§ePágina siguiente →"));
    }
    public PlayerReport reportAt(int slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == slot) {
            int index = page * SLOTS.length + i;
            return index < reports.size() ? reports.get(index) : null;
        }
        return null;
    }
    public int page() { return page; }
    @Override public Inventory getInventory() { return inventory; }
}
