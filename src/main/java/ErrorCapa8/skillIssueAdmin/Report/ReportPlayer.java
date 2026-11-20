package ErrorCapa8.skillIssueAdmin.Report;

import ErrorCapa8.skillIssueAdmin.ComandoBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * /report                     -> menú para elegir jugador y motivo
 * /report <jugador>           -> menú de motivos para ese jugador
 * /report <jugador> <motivo>  -> crea el reporte directamente con el motivo escrito (RF-01)
 */
public final class ReportPlayer extends ComandoBase {
    private final ReportCreationManager manager;

    public ReportPlayer(ReportCreationManager manager) {
        super("report", "Reporta a un jugador por conducta indebida");
        this.manager = manager;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player player = jugador(source);
        if (args.length == 0) {
            if (enEspera(player)) return;
            player.openInventory(new ReportMenu(0, player.getUniqueId()).getInventory());
            return;
        }
        Player target = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if (target == null) { error(player, "No existe ese usuario o no está conectado."); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { error(player, "No puedes reportarte a ti mismo."); return; }
        if (args.length == 1) {
            if (enEspera(player)) return;
            player.openInventory(new ReportTypeMenu(target.getUniqueId(), target.getName()).getInventory());
            return;
        }
        String motivo = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        try {
            PlayerReport report = manager.crearReporte(player, target, ReportType.OTHER, motivo);
            exito(player, "Reporte #" + report.numero() + " guardado contra " + target.getName() + ". El staff lo revisará.");
            ReportMenuListener.avisarStaff(target.getName());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            error(player, exception.getMessage());
        }
    }

    private boolean enEspera(Player player) {
        long remaining = manager.remainingCooldownMillis(player.getUniqueId());
        if (remaining <= 0) return false;
        long seconds = (remaining + 999) / 1000;
        error(player, "Espera " + (seconds / 60) + " min " + (seconds % 60) + " s para enviar otro reporte.");
        return true;
    }

    @Override public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
