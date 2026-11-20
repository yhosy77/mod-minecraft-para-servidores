package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.ComandoBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

/**
 * /subirrango : el jugador sube gratis al rango Miembro si tiene nivel 10 o 500 monedas (RF-11).
 * Si usa monedas, se le descuentan. Si no cumple, se le dice lo que le falta.
 */
public final class SubirRangoCommand extends ComandoBase {
    private final GestorJugadores gestor;
    private final RankDisplayManager displayManager;
    private final RankPermissionManager permissionManager;

    public SubirRangoCommand(GestorJugadores gestor, RankDisplayManager displayManager, RankPermissionManager permissionManager) {
        super("subirrango", "Sube gratis al rango Miembro");
        this.gestor = gestor;
        this.displayManager = displayManager;
        this.permissionManager = permissionManager;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player player = jugador(source);
        try {
            gestor.subirRangoGratuito(player.getUniqueId());
            permissionManager.apply(player);
            displayManager.refreshPlayer(player);
            exito(player, "¡Ahora eres Miembro! Te quedan " + gestor.buscarJugador(player.getUniqueId()).getMonedas() + " monedas.");
        } catch (IllegalStateException exception) {
            error(player, exception.getMessage());
        }
    }
}
