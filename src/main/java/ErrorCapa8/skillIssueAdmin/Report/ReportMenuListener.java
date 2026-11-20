package ErrorCapa8.skillIssueAdmin.Report;

import ErrorCapa8.skillIssueAdmin.SkillIssueAdmin;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public final class ReportMenuListener implements Listener {
    private final SkillIssueAdmin plugin;
    private final ReportCreationManager manager;
    public ReportMenuListener(SkillIssueAdmin plugin, ReportCreationManager manager) { this.plugin = plugin; this.manager = manager; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof ReportMenu || holder instanceof ReportTypeMenu || holder instanceof ReportAdminMenu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getRawSlot();
        if (holder instanceof ReportMenu menu) {
            if (slot == 49) { player.closeInventory(); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new ReportMenu(menu.page() - 1, player.getUniqueId()).getInventory()); return; }
            if (slot == 53) { player.openInventory(new ReportMenu(menu.page() + 1, player.getUniqueId()).getInventory()); return; }
            int itemIndex = ReportMenu.playerAt(slot);
            if (itemIndex < 0) return;
            List<Player> players = ReportMenu.onlinePlayers(player.getUniqueId());
            int targetIndex = menu.page() * ReportMenu.PAGE_SIZE + itemIndex;
            if (targetIndex >= players.size()) return;
            Player target = players.get(targetIndex);
            player.openInventory(new ReportTypeMenu(target.getUniqueId(), target.getName()).getInventory());
            return;
        }
        if (holder instanceof ReportTypeMenu menu) {
            if (slot == 31) { player.closeInventory(); return; }
            if (slot == 27) { player.openInventory(new ReportMenu(0, player.getUniqueId()).getInventory()); return; }
            ReportType type = ReportTypeMenu.typeAt(slot);
            if (type == null) return;
            player.closeInventory();
            Player target = Bukkit.getPlayer(menu.targetId());
            if (target == null) { player.sendMessage(Component.text("§cEse usuario ya no está conectado.")); return; }
            try {
                PlayerReport report = manager.crearReporte(player, target, type, type.label());
                player.sendMessage(Component.text("§aReporte #" + report.numero() + " guardado contra §f" + target.getName() + " §7por §e" + type.label() + "§a."));
                avisarStaff(target.getName());
            } catch (IllegalArgumentException | IllegalStateException exception) {
                player.sendMessage(Component.text("§c" + exception.getMessage()));
            }
            return;
        }
        if (holder instanceof ReportAdminMenu menu) {
            if (slot == 49) { player.closeInventory(); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new ReportAdminMenu(manager, menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new ReportAdminMenu(manager, menu.page() + 1).getInventory()); return; }
            PlayerReport report = menu.reportAt(slot);
            if (report == null) return;
            if (event.isRightClick()) {
                // Clic derecho: avanza el estado. Mayús + clic derecho: descarta (RF-06).
                EstadoReporte nuevo = event.isShiftClick() ? EstadoReporte.DESCARTADO : report.estado().siguiente();
                try {
                    if (nuevo == null) throw new IllegalStateException("El reporte #" + report.numero() + " ya está cerrado y no se puede reabrir.");
                    manager.resolverReporte(report, nuevo, player.getName());
                    player.sendMessage(Component.text("§aReporte #" + report.numero() + " ahora está: " + nuevo.etiqueta() + "."));
                } catch (IllegalStateException exception) {
                    player.sendMessage(Component.text("§c" + exception.getMessage()));
                }
            } else {
                ReportPriority next = report.priority().next(event.isShiftClick() ? -1 : 1);
                manager.setPriority(report, next);
                player.sendMessage(Component.text("§aPrioridad actualizada a " + next.label() + "."));
            }
            player.openInventory(new ReportAdminMenu(manager, menu.page()).getInventory());
        }
    }

    /** Avisa a los admins conectados que llegó un reporte nuevo. */
    static void avisarStaff(String reportado) {
        for (Player staff : Bukkit.getOnlinePlayers()) if (staff.hasPermission("skillissueadmin.reports"))
            staff.sendMessage(Component.text("§6Nuevo reporte: §f" + reportado + " §7(usa /gestreports)"));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (holder instanceof ReportMenu || holder instanceof ReportTypeMenu || holder instanceof ReportAdminMenu) event.setCancelled(true);
    }
}
