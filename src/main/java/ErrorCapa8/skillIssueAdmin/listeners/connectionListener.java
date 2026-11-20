package ErrorCapa8.skillIssueAdmin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Da la bienvenida al jugador cuando entra al servidor. */
public class connectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("¡Bienvenido a Skill Issue Admin, " + player.getName() + "!", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Usa /report para reportar a alguien y /profile para ver tu perfil.", NamedTextColor.GRAY));
    }
}
