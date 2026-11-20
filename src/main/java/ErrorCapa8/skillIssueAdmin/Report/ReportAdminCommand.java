package ErrorCapa8.skillIssueAdmin.Report;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class ReportAdminCommand implements BasicCommand {
    private final ReportCreationManager manager;
    public ReportAdminCommand(ReportCreationManager manager) { this.manager = manager; }
    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("§cEste comando solo se puede usar dentro del juego.")); return;
        }
        if (!player.hasPermission("skillissueadmin.reports")) {
            player.sendMessage(Component.text("§cNo tienes permiso para gestionar reportes.")); return;
        }
        player.openInventory(new ReportAdminMenu(manager, 0).getInventory());
    }
}
