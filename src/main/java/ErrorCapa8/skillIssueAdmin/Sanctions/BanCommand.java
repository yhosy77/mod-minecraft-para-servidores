package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

/** /ban : abre el asistente gráfico para aplicar un baneo (solo ADMIN). */
public final class BanCommand extends ComandoAdministrativo {
    public BanCommand(GestorJugadores jugadores) {
        super("ban", "Abre el asistente gráfico para aplicar un baneo", jugadores);
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player player = jugador(source);
        if (args.length != 0) {
            uso(player, "/ban");
            return;
        }
        player.openInventory(BanMenuViews.playerSelect(0, player).getInventory());
    }
}
