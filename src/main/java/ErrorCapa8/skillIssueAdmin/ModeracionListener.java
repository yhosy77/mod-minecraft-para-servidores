package ErrorCapa8.skillIssueAdmin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class ModeracionListener implements Listener {

    // TODO: Cuando Samuel Gacharna tenga lista GestorSanciones, inyectarla aquí:
    // private final GestorSanciones gestorSanciones;
    // public ModeracionListener(GestorSanciones gestorSanciones) {
    //     this.gestorSanciones = gestorSanciones;
    // }

    public ModeracionListener() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        // TODO: Reemplazar esta verificación temporal por la consulta real a GestorSanciones / Mute activo
        boolean estaMuteado = false;

        if (estaMuteado) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "No puedes enviar mensajes porque estás silenciado (Mute).");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        // TODO: Reemplazar esta verificación temporal por la consulta real a GestorSanciones / Baneo activo
        boolean estaBaneado = false;
        String motivoBaneo = "Has sido baneado del servidor.";

        if (estaBaneado) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + motivoBaneo);
        }
    }
}
