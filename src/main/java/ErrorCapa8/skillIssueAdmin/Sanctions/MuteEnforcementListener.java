package ErrorCapa8.skillIssueAdmin.Sanctions;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Bloquea el chat de los jugadores silenciados (igual que BanEnforcementListener bloquea la entrada).
 * Si Miguel arma su ModeracionListener, puede mover este método allá.
 */
public final class MuteEnforcementListener implements Listener {
    private final GestorSanciones sanciones;
    public MuteEnforcementListener(GestorSanciones sanciones) { this.sanciones = sanciones; }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Mute mute = sanciones.obtenerMuteActivo(event.getPlayer().getUniqueId());
        if (mute == null) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(mute.mensajeSilenciado());
    }
}
