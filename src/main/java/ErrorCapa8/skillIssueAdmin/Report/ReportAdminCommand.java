package ErrorCapa8.skillIssueAdmin.Report;

import ErrorCapa8.skillIssueAdmin.ComandoBase;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * /gestreports                                   -> bandeja gráfica de reportes
 * /gestreports estado <estado>                   -> lista filtrada por estado (RF-09)
 * /gestreports fechas <aaaa-mm-dd> <aaaa-mm-dd> [estado] -> lista filtrada por rango de fechas (RF-09)
 */
public final class ReportAdminCommand extends ComandoBase {
    private static final int MAXIMO_A_MOSTRAR = 15;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final ReportCreationManager manager;

    public ReportAdminCommand(ReportCreationManager manager) {
        super("gestreports", "Abre la bandeja administrativa de reportes");
        this.manager = manager;
    }

    @Override protected boolean tienePermiso(CommandSourceStack source) {
        return source.getExecutor() instanceof Player player && player.hasPermission("skillissueadmin.reports");
    }

    @Override protected String mensajeSinPermiso() { return "No tienes permiso para gestionar reportes."; }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player player = jugador(source);
        if (args.length == 0) {
            player.openInventory(new ReportAdminMenu(manager, 0).getInventory());
            return;
        }
        try {
            if (args[0].equalsIgnoreCase("estado") && args.length == 2) {
                mostrar(player, "estado " + args[1], manager.filtrarReportes(args[1]));
            } else if (args[0].equalsIgnoreCase("fechas") && (args.length == 3 || args.length == 4)) {
                EstadoReporte estado = null;
                if (args.length == 4) {
                    estado = EstadoReporte.desdeTexto(args[3]);
                    if (estado == null) { error(player, "Estado inválido. Usa: abierto, en_revision, resuelto o descartado."); return; }
                }
                ReportCreationManager.CriterioFiltro criterio =
                        new ReportCreationManager.CriterioFiltro(estado, LocalDate.parse(args[1]), LocalDate.parse(args[2]));
                mostrar(player, "del " + args[1] + " al " + args[2] + (estado == null ? "" : " (" + estado.etiqueta() + ")"),
                        manager.filtrarReportes(criterio));
            } else {
                uso(player, "/gestreports  |  /gestreports estado <abierto|en_revision|resuelto|descartado>");
                uso(player, "/gestreports fechas <aaaa-mm-dd> <aaaa-mm-dd> [estado]");
            }
        } catch (DateTimeParseException exception) {
            error(player, "Fecha inválida. Usa el formato aaaa-mm-dd, por ejemplo 2026-11-20.");
        } catch (IllegalArgumentException exception) {
            error(player, exception.getMessage());
        }
    }

    private void mostrar(Player player, String titulo, List<PlayerReport> lista) {
        player.sendMessage(Component.text("--- Reportes " + titulo + ": " + lista.size() + " ---", NamedTextColor.GOLD));
        if (lista.isEmpty()) info(player, "No hay reportes con ese filtro.");
        for (int i = 0; i < Math.min(lista.size(), MAXIMO_A_MOSTRAR); i++) {
            PlayerReport r = lista.get(i);
            player.sendMessage(Component.text("#" + r.numero() + " [" + r.estado().etiqueta() + "] " + r.targetName()
                    + " - " + r.motivo() + " (por " + r.reporterName() + ", " + FORMATO.format(r.createdAt()) + ")", NamedTextColor.WHITE));
        }
        if (lista.size() > MAXIMO_A_MOSTRAR) info(player, "... y " + (lista.size() - MAXIMO_A_MOSTRAR) + " más.");
    }
}
