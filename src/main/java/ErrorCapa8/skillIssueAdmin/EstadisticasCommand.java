package ErrorCapa8.skillIssueAdmin;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Report.PlayerReport;
import ErrorCapa8.skillIssueAdmin.Report.ReportCreationManager;
import ErrorCapa8.skillIssueAdmin.Sanctions.GestorSanciones;
import ErrorCapa8.skillIssueAdmin.Sanctions.Sancion;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * /estadisticas [dias] : resumen de la actividad del staff en el periodo (RF-10).
 * Cuenta, por cada administrador, las sanciones que aplicó y los reportes que atendió.
 */
public final class EstadisticasCommand extends ComandoAdministrativo {
    private static final int DIAS_POR_DEFECTO = 30;
    private final GestorSanciones sanciones;
    private final ReportCreationManager reportes;

    public EstadisticasCommand(GestorJugadores jugadores, GestorSanciones sanciones, ReportCreationManager reportes) {
        super("estadisticas", "Resumen de sanciones y reportes atendidos por cada administrador", jugadores);
        this.sanciones = sanciones;
        this.reportes = reportes;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        int dias = DIAS_POR_DEFECTO;
        if (args.length == 1) {
            try {
                dias = Integer.parseInt(args[0]);
                if (dias <= 0) throw new NumberFormatException();
            } catch (NumberFormatException exception) {
                error(admin, "Los días deben ser un número entero mayor a cero.");
                return;
            }
        } else if (args.length > 1) { uso(admin, "/estadisticas [días]"); return; }

        Instant desde = Instant.now().minus(dias, ChronoUnit.DAYS);
        Map<String, int[]> conteo = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);   // [sanciones, reportes]
        for (Sancion s : sanciones.getHistorial())
            if (!s.getCreadaEn().isBefore(desde)) conteo.computeIfAbsent(s.getAdminResponsable(), k -> new int[2])[0]++;
        for (PlayerReport r : reportes.reports())
            if (!r.resolvedBy().isBlank() && r.resolvedAt() != null && !r.resolvedAt().isBefore(desde))
                conteo.computeIfAbsent(r.resolvedBy(), k -> new int[2])[1]++;

        admin.sendMessage(Component.text("--- Actividad del staff en los últimos " + dias + " días ---", NamedTextColor.GOLD));
        if (conteo.isEmpty()) { info(admin, "No hubo sanciones ni reportes atendidos en ese periodo."); return; }
        int totalSanciones = 0, totalReportes = 0;
        for (Map.Entry<String, int[]> fila : conteo.entrySet()) {
            admin.sendMessage(Component.text(fila.getKey() + ": " + fila.getValue()[0] + " sanción(es), "
                    + fila.getValue()[1] + " reporte(s) atendido(s)", NamedTextColor.WHITE));
            totalSanciones += fila.getValue()[0];
            totalReportes += fila.getValue()[1];
        }
        admin.sendMessage(Component.text("Total: " + totalSanciones + " sanciones y " + totalReportes + " reportes atendidos.", NamedTextColor.YELLOW));
    }
}
