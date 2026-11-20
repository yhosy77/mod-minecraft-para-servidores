package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class BanCommand implements BasicCommand {
    private final GestorJugadores jugadores;
    public BanCommand(GestorJugadores jugadores) { this.jugadores = jugadores; }

    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Este comando solo puede usarlo un jugador."));
            return;
        }
        if (!jugadores.esAdministrador(player.getUniqueId())) {
            player.sendMessage(Component.text("No tienes permiso; se requiere el rango ADMIN."));
            return;
        }
        if (args.length != 0) {
            player.sendMessage(Component.text("Uso: /ban"));
            return;
        }
        player.openInventory(BanMenuViews.playerSelect(0, player).getInventory());
    }
}
