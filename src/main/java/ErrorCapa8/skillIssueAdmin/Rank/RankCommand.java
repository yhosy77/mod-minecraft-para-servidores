package ErrorCapa8.skillIssueAdmin.Rank;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class RankCommand implements BasicCommand {
    private final GestorJugadores gestor;
    public RankCommand(GestorJugadores gestor) { this.gestor = gestor; }
    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("§c/rank solo puede usarse dentro del juego."));
            return;
        }
        if (!gestor.esAdministrador(player.getUniqueId())) {
            player.sendMessage(Component.text("§cNo tienes permisos. Solo un rango ADMIN puede usar /rank."));
            return;
        }
        player.openInventory(new RankMainMenu(gestor).getInventory());
    }
}
