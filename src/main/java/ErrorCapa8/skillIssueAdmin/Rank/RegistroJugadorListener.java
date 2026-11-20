package ErrorCapa8.skillIssueAdmin.Rank;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RegistroJugadorListener implements Listener {
    private final GestorJugadores gestor;
    private final RankDisplayManager displayManager;
    private final RankPermissionManager permissionManager;
    public RegistroJugadorListener(GestorJugadores gestor, RankDisplayManager displayManager, RankPermissionManager permissionManager) {
        this.gestor = gestor; this.displayManager = displayManager; this.permissionManager = permissionManager;
    }
    @EventHandler public void onJoin(PlayerJoinEvent event) {
        gestor.registrarOActualizarJugador(event.getPlayer());
        permissionManager.apply(event.getPlayer());
        displayManager.refreshPlayer(event.getPlayer());
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { permissionManager.clear(event.getPlayer()); }
}
