package ErrorCapa8.skillIssueAdmin.listeners;

import ErrorCapa8.skillIssueAdmin.Sanctions.GestorSanciones;
import ErrorCapa8.skillIssueAdmin.Sanctions.Mute;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.time.Instant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Hace cumplir las sanciones en tiempo real:
 * - Si el jugador está muteado, cancela sus mensajes de chat y le avisa.
 * - Si tiene un baneo vigente, no lo deja entrar al servidor.
 */
public class ModeracionListener implements Listener {
    private final GestorSanciones gestorSanciones;

    public ModeracionListener(GestorSanciones gestorSanciones) {
        this.gestorSanciones = gestorSanciones;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncChatEvent e) {
        Mute mute = gestorSanciones.obtenerMuteActivo(e.getPlayer().getUniqueId());
        if (mute == null) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(mute.mensajeSilenciado());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent e) {
        gestorSanciones.obtenerBaneoActivo(e.getUniqueId(), Instant.now()).ifPresent(baneo ->
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, baneo.mensajeIngreso()));
    }
}
