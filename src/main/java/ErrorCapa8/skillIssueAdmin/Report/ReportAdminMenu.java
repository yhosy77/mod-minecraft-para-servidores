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
        inventory.setItem(4, MenuItems.item(Material.WRITABLE_BOOK, "§bGestión de reportes", "§7Clic izquierdo: subir prioridad", "§7Mayús + clic izquierdo: bajar prioridad", "§7Clic derecho: completar/reabrir caso"));
        int from = this.page * SLOTS.length;
        for (int i = 0; i < SLOTS.length && from + i < reports.size(); i++) {
            PlayerReport report = reports.get(from + i);
            long minutes = Math.max(0, Duration.between(report.createdAt(), Instant.now()).toMinutes());
            String color = switch (report.priority()) { case LOW -> "§a"; case MEDIUM -> "§e"; case URGENT -> "§c"; };
            inventory.setItem(SLOTS[i], MenuItems.item(report.resolved() ? Material.LIME_DYE : report.priority().icon(),
                    "§f" + report.targetName() + " §8• §e" + report.type().label(),
                    "§7Reportado por: §f" + report.reporterName(), "§7Prioridad: " + color + report.priority().label(),
                    "§7Estado: " + (report.resolved() ? "§aResuelto" : "§ePendiente"), "§7Hace: §f" + minutes + " min",
                    "§8ID: " + report.id(), "§8Clic izq: subir prioridad", "§8Mayús+clic izq: bajar prioridad",
                    "§8Clic der: " + (report.resolved() ? "reabrir" : "completar"), "§7UUID objetivo: §f" + report.targetId()));
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
