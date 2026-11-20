package ErrorCapa8.skillIssueAdmin.Report;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ReportPlayer implements BasicCommand {
    private final ReportCreationManager manager;
    public ReportPlayer(ReportCreationManager manager) { this.manager = manager; }
    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("§cEste comando solo puede usarlo un jugador."));
            return;
        }
        if (args.length > 1) { player.sendMessage(Component.text("§cUso: /report [jugador]")); return; }
        Player target = null;
        if (args.length == 1) {
            target = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
            if (target == null) { player.sendMessage(Component.text("§cNo existe ese usuario o no está conectado.")); return; }
        }
        long remaining = manager.remainingCooldownMillis(player.getUniqueId());
        if (remaining > 0) {
            long seconds = (remaining + 999) / 1000;
            player.sendMessage(Component.text("§cEspera " + (seconds / 60) + " min " + (seconds % 60) + " s para enviar otro reporte."));
            return;
        }
        player.openInventory(target == null ? new ReportMenu(0).getInventory()
                : new ReportTypeMenu(target.getUniqueId(), target.getName()).getInventory());
    }
    @Override public java.util.Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length != 1) return java.util.List.of();
        String prefix = args[0].toLowerCase(java.util.Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(prefix)).toList();
    }
}
