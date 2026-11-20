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
            if (slot == 45 && menu.page() > 0) { player.openInventory(new ReportMenu(menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new ReportMenu(menu.page() + 1).getInventory()); return; }
            int itemIndex = ReportMenu.playerAt(slot);
            if (itemIndex < 0) return;
            List<Player> players = ReportMenu.onlinePlayers();
            int targetIndex = menu.page() * ReportMenu.PAGE_SIZE + itemIndex;
            if (targetIndex >= players.size()) return;
            Player target = players.get(targetIndex);
            player.openInventory(new ReportTypeMenu(target.getUniqueId(), target.getName()).getInventory());
            return;
        }
        if (holder instanceof ReportTypeMenu menu) {
            if (slot == 31) { player.closeInventory(); return; }
            if (slot == 27) { player.openInventory(new ReportMenu(0).getInventory()); return; }
            ReportType type = ReportTypeMenu.typeAt(slot);
            if (type == null) return;
            Player target = Bukkit.getPlayer(menu.targetId());
            if (target == null) { player.closeInventory(); player.sendMessage(Component.text("§cEse usuario ya no está conectado.")); return; }
            if (!manager.submit(player, target, type)) {
                player.closeInventory();
                long seconds = (manager.remainingCooldownMillis(player.getUniqueId()) + 999) / 1000;
                player.sendMessage(Component.text("§cEspera " + (seconds / 60) + " min " + (seconds % 60) + " s para enviar otro reporte."));
                return;
            }
            player.closeInventory();
            player.sendMessage(Component.text("§aReporte guardado contra §f" + target.getName() + " §7por §e" + type.label() + "§a."));
            for (Player staff : Bukkit.getOnlinePlayers()) if (staff.hasPermission("skillissueadmin.reports"))
                staff.sendMessage(Component.text("§6Nuevo reporte: §f" + target.getName() + " §7(usa /gestreports)"));
            return;
        }
        if (holder instanceof ReportAdminMenu menu) {
            if (slot == 49) { player.closeInventory(); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new ReportAdminMenu(manager, menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new ReportAdminMenu(manager, menu.page() + 1).getInventory()); return; }
            PlayerReport report = menu.reportAt(slot);
            if (report == null) return;
            if (event.isRightClick()) {
                boolean resolved = !report.resolved();
                manager.setResolved(report, resolved, player.getName());
                player.sendMessage(Component.text(resolved ? "§aReporte marcado como resuelto." : "§eReporte reabierto."));
            } else {
                ReportPriority next = report.priority().next(event.isShiftClick() ? -1 : 1);
                manager.setPriority(report, next);
                player.sendMessage(Component.text("§aPrioridad actualizada a " + next.label() + "."));
            }
            player.openInventory(new ReportAdminMenu(manager, menu.page()).getInventory());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (holder instanceof ReportMenu || holder instanceof ReportTypeMenu || holder instanceof ReportAdminMenu) event.setCancelled(true);
    }
}
