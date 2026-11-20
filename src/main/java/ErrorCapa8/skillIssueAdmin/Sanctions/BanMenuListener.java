package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public final class BanMenuListener implements Listener {
    private enum ChatStep { CUSTOM_CAUSE, EXTRA_MESSAGE }
    private record PendingChat(BanDraft draft, ChatStep step) { }

    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;
    private final JavaPlugin plugin;
    private final Map<UUID, PendingChat> pendingChat = new ConcurrentHashMap<>();

    public BanMenuListener(JavaPlugin plugin, GestorJugadores jugadores, GestorSanciones sanciones) {
        this.plugin = plugin;
        this.jugadores = jugadores;
        this.sanciones = sanciones;
    }

    private boolean isBanMenu(InventoryHolder holder) {
        return holder instanceof BanMenuViews.PlayerSelect || holder instanceof BanMenuViews.ReasonSelect
                || holder instanceof BanMenuViews.DurationSelect || holder instanceof BanMenuViews.MessageSelect
                || holder instanceof BanMenuViews.Confirm;
    }

    @EventHandler public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!isBanMenu(holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player admin = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (!jugadores.esAdministrador(admin.getUniqueId())) {
            admin.closeInventory();
            admin.sendMessage(Component.text("§cSe requiere el rango ADMIN."));
            return;
        }
        if (holder instanceof BanMenuViews.PlayerSelect) handlePlayerMenu(admin, (BanMenuViews.PlayerSelect) holder, event.getRawSlot());
        else if (holder instanceof BanMenuViews.ReasonSelect) handleReasonMenu(admin, (BanMenuViews.ReasonSelect) holder, event.getRawSlot());
        else if (holder instanceof BanMenuViews.DurationSelect) handleDurationMenu(admin, (BanMenuViews.DurationSelect) holder, event.getRawSlot());
        else if (holder instanceof BanMenuViews.MessageSelect) handleMessageMenu(admin, (BanMenuViews.MessageSelect) holder, event.getRawSlot());
        else if (holder instanceof BanMenuViews.Confirm) handleConfirmMenu(admin, (BanMenuViews.Confirm) holder, event.getRawSlot());
    }

    private void handlePlayerMenu(Player admin, BanMenuViews.PlayerSelect menu, int slot) {
        if (slot == 49) { admin.closeInventory(); return; }
        if (slot == 45 && menu.page > 0) { admin.openInventory(BanMenuViews.playerSelect(menu.page - 1, admin).getInventory()); return; }
        if (slot == 53) { admin.openInventory(BanMenuViews.playerSelect(menu.page + 1, admin).getInventory()); return; }
        int index = BanMenuViews.playerIndexAt(slot);
        if (index < 0) return;
        int targetIndex = menu.page * BanMenuViews.PAGE_SIZE + index;
        if (targetIndex >= menu.targets.size()) return;
        Player selected = menu.targets.get(targetIndex);
        Player target = Bukkit.getPlayer(selected.getUniqueId());
        if (target == null || !target.isOnline()) {
            admin.sendMessage(Component.text("§cEse jugador se desconectó; actualiza el menú para continuar."));
            admin.openInventory(BanMenuViews.playerSelect(menu.page, admin).getInventory());
            return;
        }
        admin.openInventory(BanMenuViews.reasonSelect(new BanDraft(target.getUniqueId(), target.getName())).getInventory());
    }

    private void handleReasonMenu(Player admin, BanMenuViews.ReasonSelect menu, int slot) {
        if (slot == 40) { admin.closeInventory(); return; }
        if (slot == 36) { admin.openInventory(BanMenuViews.playerSelect(0, admin).getInventory()); return; }
        int[] slots = {19,20,21,22,23,24};
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != slot) continue;
            CausaBaneo cause = CausaBaneo.values()[i];
            if (cause == CausaBaneo.OTRA) askInChat(admin, menu.draft, ChatStep.CUSTOM_CAUSE);
            else {
                menu.draft.setCausa(cause, "");
                admin.openInventory(BanMenuViews.durationSelect(menu.draft).getInventory());
            }
            return;
        }
    }

    private void handleDurationMenu(Player admin, BanMenuViews.DurationSelect menu, int slot) {
        if (slot == 40) { admin.closeInventory(); return; }
        if (slot == 36) { admin.openInventory(BanMenuViews.reasonSelect(menu.draft).getInventory()); return; }
        int[] slots = {19,20,21,22,23,24};
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != slot) continue;
            menu.draft.setDuracion(DuracionBaneo.values()[i]);
            admin.openInventory(BanMenuViews.messageSelect(menu.draft).getInventory());
            return;
        }
    }

    private void handleMessageMenu(Player admin, BanMenuViews.MessageSelect menu, int slot) {
        if (slot == 40) { admin.closeInventory(); return; }
        if (slot == 36) { admin.openInventory(BanMenuViews.durationSelect(menu.draft).getInventory()); return; }
        if (slot == 20) askInChat(admin, menu.draft, ChatStep.EXTRA_MESSAGE);
        else if (slot == 24) {
            menu.draft.setMensaje("");
            admin.openInventory(BanMenuViews.confirm(menu.draft).getInventory());
        }
    }

    private void handleConfirmMenu(Player admin, BanMenuViews.Confirm menu, int slot) {
        if (slot == 29) { admin.closeInventory(); return; }
        if (slot != 33) return;
        Player target = Bukkit.getPlayer(menu.draft.getJugadorId());
        if (target == null || !target.isOnline()) {
            admin.closeInventory();
            admin.sendMessage(Component.text("§cEl jugador se desconectó; el baneo no se aplicó."));
            return;
        }
        try {
            Baneo ban = sanciones.aplicarBaneo(menu.draft, admin);
            admin.closeInventory();
            admin.sendMessage(Component.text("§aBaneo aplicado y guardado para §f" + ban.getJugadorNombre() + "§a (" + ban.duracionTexto() + ")."));
            if (ban.esPermanente() && menu.draft.getDuracion() != DuracionBaneo.PERMANENTE)
                admin.sendMessage(Component.text("§eEl baneo subió a permanente por reincidencia o periodo de prueba (RF-07/RF-08)."));
        } catch (IOException exception) {
            admin.closeInventory();
            admin.sendMessage(Component.text("§cNo se pudo guardar el baneo; no se aplicó."));
            admin.getServer().getLogger().severe("No se pudo persistir el baneo: " + exception.getMessage());
        } catch (IllegalArgumentException | SecurityException exception) {
            admin.closeInventory();
            admin.sendMessage(Component.text("§c" + exception.getMessage()));
        }
    }

    private void askInChat(Player admin, BanDraft draft, ChatStep step) {
        pendingChat.put(admin.getUniqueId(), new PendingChat(draft, step));
        admin.closeInventory();
        String prompt = step == ChatStep.CUSTOM_CAUSE
                ? "§eEscribe el motivo personalizado en el chat (o 'cancelar' para abortar)."
                : "§eEscribe el mensaje para el jugador (o 'cancelar' para abortar).";
        admin.sendMessage(Component.text(prompt));
    }

    @EventHandler public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PendingChat pending = pendingChat.get(player.getUniqueId());
        if (pending == null) return;
        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override public void run() { processChatResponse(player, pending, text); }
        });
    }

    private void processChatResponse(Player player, PendingChat expected, String text) {
        PendingChat current = pendingChat.remove(player.getUniqueId());
        if (current != expected) return;
        if (!jugadores.esAdministrador(player.getUniqueId())) {
            player.sendMessage(Component.text("§cYa no tienes el rango ADMIN; se canceló el proceso."));
            return;
        }
        if (text.equalsIgnoreCase("cancelar")) { player.sendMessage(Component.text("§eProceso de baneo cancelado.")); return; }
        if (text.isBlank() || text.length() > 200) {
            player.sendMessage(Component.text("§cEl texto debe tener entre 1 y 200 caracteres. El proceso se canceló."));
            return;
        }
        if (expected.step() == ChatStep.CUSTOM_CAUSE) {
            expected.draft().setCausa(CausaBaneo.OTRA, text);
            player.openInventory(BanMenuViews.durationSelect(expected.draft()).getInventory());
        } else {
            expected.draft().setMensaje(text);
            player.openInventory(BanMenuViews.confirm(expected.draft()).getInventory());
        }
    }

    @EventHandler public void onDrag(InventoryDragEvent event) {
        if (isBanMenu(event.getView().getTopInventory().getHolder(false))) event.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { pendingChat.remove(event.getPlayer().getUniqueId()); }
}
