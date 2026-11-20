package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.SkillIssueAdmin;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

public final class RankMenuListener implements Listener {
    private static final long DELETE_DELAY_MILLIS = 10_000L;
    private final SkillIssueAdmin plugin;
    private final GestorJugadores gestor;
    private final RankDisplayManager displayManager;
    private final RankPermissionManager permissionManager;
    private final Map<UUID, PendingCreation> pendingCreation = new ConcurrentHashMap<>();
    private final Map<UUID, RankColorMenu> pendingHexInput = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> deletionCountdowns = new ConcurrentHashMap<>();

    public RankMenuListener(SkillIssueAdmin plugin, GestorJugadores gestor, RankDisplayManager displayManager, RankPermissionManager permissionManager) {
        this.plugin = plugin; this.gestor = gestor; this.displayManager = displayManager; this.permissionManager = permissionManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof RankMainMenu || holder instanceof RankTypeMenu || holder instanceof RankListMenu || holder instanceof RankDeleteConfirmMenu
                || holder instanceof RankPlayerSelectMenu || holder instanceof RankAssignmentMenu || holder instanceof RankAssignmentConfirmMenu
                || holder instanceof RankColorMenu || holder instanceof RankRgbColorMenu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (!gestor.esAdministrador(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(Component.text("§cSolo un rango ADMIN puede gestionar rangos."));
            return;
        }
        int slot = event.getRawSlot();
        if (holder instanceof RankMainMenu) {
            if (slot == 10) player.openInventory(new RankListMenu(gestor, RankListMenu.Mode.DEFAULT, 0).getInventory());
            else if (slot == 12) player.openInventory(new RankPlayerSelectMenu(gestor, 0).getInventory());
            else if (slot == 14) player.openInventory(new RankTypeMenu().getInventory());
            else if (slot == 16) player.openInventory(new RankListMenu(gestor, RankListMenu.Mode.DELETE, 0).getInventory());
            else if (slot == 22) player.openInventory(new RankListMenu(gestor, RankListMenu.Mode.COLOR, 0).getInventory());
            return;
        }
        if (holder instanceof RankTypeMenu) {
            if (slot == 22) { player.openInventory(new RankMainMenu(gestor).getInventory()); return; }
            TipoRango tipo = RankTypeMenu.tipoAt(slot);
            if (tipo == null) return;
            player.openInventory(new RankColorMenu(tipo).getInventory());
            return;
        }
        if (holder instanceof RankListMenu menu) {
            if (slot == 49) { player.openInventory(new RankMainMenu(gestor).getInventory()); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new RankListMenu(gestor, menu.mode(), menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new RankListMenu(gestor, menu.mode(), menu.page() + 1).getInventory()); return; }
            Rango rango = menu.rangoAt(slot);
            if (rango == null) return;
            if (menu.mode() == RankListMenu.Mode.DEFAULT) {
                try {
                    gestor.establecerRangoPredeterminado(rango.getId());
                    player.sendMessage(Component.text("§aRango predeterminado actualizado: §f" + rango.getNombre()));
                    player.openInventory(new RankMainMenu(gestor).getInventory());
                } catch (IllegalArgumentException exception) { player.sendMessage(Component.text("§c" + exception.getMessage())); }
            } else if (menu.mode() == RankListMenu.Mode.DELETE) {
                if (gestor.obtenerRangoPredeterminado().getId().equals(rango.getId())) {
                    player.sendMessage(Component.text("§cEse rango es el predeterminado. Establece primero otro rango PLAYER como predeterminado."));
                    return;
                }
                abrirConfirmacion(player, rango);
            } else {
                player.openInventory(new RankColorMenu(rango).getInventory());
            }
            return;
        }
        if (holder instanceof RankPlayerSelectMenu menu) {
            if (slot == 49) { player.openInventory(new RankMainMenu(gestor).getInventory()); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new RankPlayerSelectMenu(gestor, menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new RankPlayerSelectMenu(gestor, menu.page() + 1).getInventory()); return; }
            Jugador jugador = menu.jugadorAt(slot);
            if (jugador != null) player.openInventory(new RankAssignmentMenu(gestor, jugador, 0).getInventory());
            return;
        }
        if (holder instanceof RankAssignmentMenu menu) {
            if (slot == 49) { player.openInventory(new RankPlayerSelectMenu(gestor, 0).getInventory()); return; }
            if (slot == 45 && menu.page() > 0) { player.openInventory(new RankAssignmentMenu(gestor, menu.jugador(), menu.page() - 1).getInventory()); return; }
            if (slot == 53) { player.openInventory(new RankAssignmentMenu(gestor, menu.jugador(), menu.page() + 1).getInventory()); return; }
            Rango selected = menu.rangoAt(slot);
            Jugador currentPlayer = gestor.buscarJugador(menu.jugador().getUuid());
            if (selected != null && currentPlayer != null) {
                player.openInventory(new RankAssignmentConfirmMenu(currentPlayer, selected, gestor.buscarRango(currentPlayer.getRangoId())).getInventory());
            }
            return;
        }
        if (holder instanceof RankAssignmentConfirmMenu confirm) {
            if (slot == 15) { player.openInventory(new RankPlayerSelectMenu(gestor, 0).getInventory()); return; }
            if (slot != 11) return;
            try {
                gestor.asignarRango(confirm.jugadorId(), confirm.rangoId());
                Player target = Bukkit.getPlayer(confirm.jugadorId());
                if (target != null) {
                    permissionManager.apply(target);
                    displayManager.refreshPlayer(target);
                }
                player.sendMessage(Component.text("§aRango §e" + confirm.rangoNombre() + " §aasignado a §f" + confirm.jugadorNombre() + "§a."));
                player.openInventory(new RankMainMenu(gestor).getInventory());
            } catch (IllegalArgumentException exception) {
                player.sendMessage(Component.text("§c" + exception.getMessage()));
                player.openInventory(new RankPlayerSelectMenu(gestor, 0).getInventory());
            }
            return;
        }
        if (holder instanceof RankColorMenu menu) {
            if (slot == 45) {
                player.openInventory(menu.creation() ? new RankMainMenu(gestor).getInventory()
                        : new RankListMenu(gestor, RankListMenu.Mode.COLOR, 0).getInventory());
                return;
            }
            if (slot == 34) { player.openInventory(new RankRgbColorMenu(menu).getInventory()); return; }
            if (slot == 49) {
                if (menu.creation()) {
                    pendingCreation.put(player.getUniqueId(), new PendingCreation(menu.tipo(), menu.color(), menu.formatos()));
                    player.closeInventory();
                    player.sendMessage(Component.text("§eEscribe el nombre del rango tipo " + menu.tipo() + " en el chat. Escribe «cancelar» para abortar."));
                } else {
                    try {
                        gestor.cambiarEstilo(menu.rangoId(), menu.color(), menu.formatos());
                        displayManager.refreshAll();
                        player.sendMessage(Component.text("§aEstilo de §f" + menu.previewName() + " §aguardado."));
                        player.openInventory(new RankMainMenu(gestor).getInventory());
                    } catch (IllegalArgumentException exception) { player.sendMessage(Component.text("§c" + exception.getMessage())); }
                }
                return;
            }
            TextColor color = RankColorMenu.colorAt(slot);
            if (color != null) { menu.setColor(color); return; }
            TextDecoration format = RankColorMenu.formatAt(slot);
            if (format != null) menu.toggle(format);
            return;
        }
        if (holder instanceof RankRgbColorMenu menu) {
            if (slot == 45) { player.openInventory(menu.parent().getInventory()); return; }
            if (slot == 49) {
                pendingHexInput.put(player.getUniqueId(), menu.parent());
                player.closeInventory();
                player.sendMessage(Component.text("§eEscribe un color hexadecimal RGB (por ejemplo #33AACC). Escribe «cancelar» para volver."));
                return;
            }
            TextColor color = RankRgbColorMenu.colorAt(slot);
            if (color != null) {
                menu.parent().setColor(color);
                player.openInventory(menu.parent().getInventory());
            }
            return;
        }
        if (holder instanceof RankDeleteConfirmMenu confirm) {
            if (slot == 15) { player.closeInventory(); player.openInventory(new RankListMenu(gestor, RankListMenu.Mode.DELETE, 0).getInventory()); return; }
            if (slot != 11) return;
            long remaining = confirm.habilitadoEn() - System.currentTimeMillis();
            if (remaining > 0) {
                confirm.update((int) Math.ceil(remaining / 1000.0));
                player.sendMessage(Component.text("§eEspera " + (int) Math.ceil(remaining / 1000.0) + " segundos antes de confirmar."));
                return;
            }
            int reassigned = gestor.jugadoresConRango(confirm.rangoId()).size();
            try {
                gestor.eliminarRango(confirm.rangoId());
                cancelarCuenta(player.getUniqueId());
                permissionManager.refreshAll();
                displayManager.refreshAll();
                player.sendMessage(Component.text("§aRango eliminado. Jugadores reasignados: " + reassigned + "."));
                player.openInventory(new RankMainMenu(gestor).getInventory());
            } catch (IllegalArgumentException exception) {
                player.sendMessage(Component.text("§c" + exception.getMessage()));
                player.openInventory(new RankMainMenu(gestor).getInventory());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (holder instanceof RankMainMenu || holder instanceof RankTypeMenu || holder instanceof RankListMenu || holder instanceof RankDeleteConfirmMenu
                || holder instanceof RankPlayerSelectMenu || holder instanceof RankAssignmentMenu || holder instanceof RankAssignmentConfirmMenu
                || holder instanceof RankColorMenu || holder instanceof RankRgbColorMenu) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof RankDeleteConfirmMenu) cancelarCuenta(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        pendingCreation.remove(id);
        pendingHexInput.remove(id);
        cancelarCuenta(id);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String nombre = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        RankColorMenu styleInput = pendingHexInput.get(id);
        if (styleInput != null) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(id);
                if (player == null || !pendingHexInput.remove(id, styleInput)) return;
                if (nombre.equalsIgnoreCase("cancelar")) { player.openInventory(styleInput.getInventory()); return; }
                if (!nombre.matches("(?i)#?[0-9a-f]{6}")) {
                    player.sendMessage(Component.text("§cFormato inválido. Usa seis dígitos hexadecimales, por ejemplo #33AACC."));
                    player.openInventory(styleInput.getInventory());
                    return;
                }
                styleInput.setColor(Rango.parseColor(nombre));
                player.openInventory(styleInput.getInventory());
            });
            return;
        }
        PendingCreation pending = pendingCreation.get(id);
        if (pending == null) return;
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(id);
            if (player == null || pendingCreation.get(id) != pending) return;
            if (!gestor.esAdministrador(id)) { player.sendMessage(Component.text("§cYa no tienes permiso para crear rangos.")); return; }
            if (nombre.equalsIgnoreCase("cancelar")) {
                pendingCreation.remove(id, pending);
                player.sendMessage(Component.text("§7Creación cancelada."));
                return;
            }
            try {
                Rango rango = gestor.crearRango(nombre, pending.tipo(), pending.color(), pending.formatos());
                pendingCreation.remove(id, pending);
                player.sendMessage(Component.text("§aRango §f" + rango.getNombre() + " §acreado con tipo §e" + pending.tipo() + "§a."));
            } catch (IllegalArgumentException exception) {
                player.sendMessage(Component.text("§c" + exception.getMessage() + " Escribe otro nombre o «cancelar»."));
            }
        });
    }

    private void abrirConfirmacion(Player player, Rango rango) {
        long habilitadoEn = System.currentTimeMillis() + DELETE_DELAY_MILLIS;
        RankDeleteConfirmMenu menu = new RankDeleteConfirmMenu(rango.getId(), rango.getNombre(), habilitadoEn, 10);
        player.openInventory(menu.getInventory());
        cancelarCuenta(player.getUniqueId());
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder(false) != menu) {
                cancelarCuenta(player.getUniqueId());
                return;
            }
            long remaining = habilitadoEn - System.currentTimeMillis();
            menu.update((int) Math.max(0, Math.ceil(remaining / 1000.0)));
            if (remaining <= 0) cancelarCuenta(player.getUniqueId());
        }, 0L, 20L);
        deletionCountdowns.put(player.getUniqueId(), task);
    }

    private void cancelarCuenta(UUID playerId) {
        BukkitTask task = deletionCountdowns.remove(playerId);
        if (task != null) task.cancel();
    }

    private record PendingCreation(TipoRango tipo, TextColor color, Set<TextDecoration> formatos) { }
}
