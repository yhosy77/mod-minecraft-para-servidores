package ErrorCapa8.skillIssueAdmin.Sanctions;

import java.time.Instant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/** Enforces persisted sanctions before the player enters the server. */
public final class BanEnforcementListener implements Listener {
    private final GestorSanciones sanciones;
    public BanEnforcementListener(GestorSanciones sanciones) { this.sanciones = sanciones; }

    @EventHandler public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        sanciones.obtenerBaneoActivo(event.getUniqueId(), Instant.now()).ifPresent(baneo ->
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, baneo.mensajeIngreso()));
    }
}
