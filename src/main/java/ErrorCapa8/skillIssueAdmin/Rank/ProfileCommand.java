package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.ComandoBase;
import ErrorCapa8.skillIssueAdmin.Sanctions.GestorSanciones;
import ErrorCapa8.skillIssueAdmin.Sanctions.Sancion;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /profile [jugador] : muestra el perfil del jugador con su rango, progreso y estado de sanciones. */
public final class ProfileCommand extends ComandoBase {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.ROOT).withZone(ZoneId.systemDefault());
    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;

    public ProfileCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        super("profile", "Muestra el perfil registrado de un jugador");
        this.jugadores = jugadores; this.sanciones = sanciones;
    }

    /** También se puede usar desde la consola indicando el jugador. */
    @Override protected boolean requiereJugador() { return false; }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        Jugador target;
        if (args.length == 0) {
            if (!(source.getExecutor() instanceof Player player)) { uso(sender, "/profile <jugador>"); return; }
            target = jugadores.buscarJugador(player.getUniqueId());
        } else if (args.length == 1) {
            target = jugadores.obtenerJugadores().stream()
                    .filter(j -> j.getNombre().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        } else {
            uso(sender, "/profile [jugador]");
            return;
        }
        if (target == null) { error(sender, "No se encontró un perfil registrado para ese jugador."); return; }

        Rango rank = jugadores.buscarRango(target.getRangoId());
        String rankName = rank == null ? "Sin rango" : rank.getNombre();
        sender.sendMessage(Component.text("===== Perfil de " + target.getNombre() + " =====", NamedTextColor.GOLD));
        info(sender, "Rango: " + rankName + (rank != null && rank.esPago() ? " (exclusivo)" : ""));
        info(sender, "Nivel: " + target.getNivel());
        info(sender, "Monedas: " + target.getMonedas());
        info(sender, "Horas jugadas: " + target.getHorasJugadas());
        info(sender, "Miembro desde: " + DATE_FORMAT.format(target.getFechaIngreso()));
        if (target.getRangoId().equals(jugadores.obtenerRangoPredeterminado().getId()))
            info(sender, "Subir a Miembro (/subirrango): " + target.requisitosFaltantes());

        java.util.List<Sancion> history = sanciones.obtenerSancionesPorJugador(target.getUuid());
        long active = sanciones.getSancionesActivas().stream()
                .filter(s -> s.getJugadorId().equals(target.getUuid())).count();
        String estado = sanciones.estadoJugador(target.getUuid());
        sender.sendMessage(Component.text("Estado: " + estado, estado.startsWith("Sancionado") ? NamedTextColor.RED
                : estado.startsWith("Advertido") ? NamedTextColor.YELLOW : NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Sanciones activas: " + active + " (historial: " + history.size() + ")",
                active == 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
    }
}
