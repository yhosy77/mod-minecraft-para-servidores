package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.Sanctions.GestorSanciones;
import ErrorCapa8.skillIssueAdmin.Sanctions.Sancion;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** Muestra los datos persistidos del perfil, consultando el gestor de rangos existente. */
public final class ProfileCommand implements BasicCommand {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.ROOT).withZone(ZoneId.systemDefault());
    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;

    public ProfileCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        this.jugadores = jugadores; this.sanciones = sanciones;
    }

    @Override public void execute(CommandSourceStack source, String[] args) {
        Jugador target;
        if (args.length == 0) {
            if (!(source.getExecutor() instanceof Player player)) {
                source.getSender().sendMessage(Component.text("Uso: /profile <jugador>", NamedTextColor.RED));
                return;
            }
            target = jugadores.buscarJugador(player.getUniqueId());
        } else if (args.length == 1) {
            target = jugadores.obtenerJugadores().stream()
                    .filter(j -> j.getNombre().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        } else {
            source.getSender().sendMessage(Component.text("Uso: /profile [jugador]", NamedTextColor.RED));
            return;
        }
        if (target == null) {
            source.getSender().sendMessage(Component.text("No se encontró un perfil registrado para ese jugador.", NamedTextColor.RED));
            return;
        }
        Rango rank = jugadores.buscarRango(target.getRangoId());
        String rankName = rank == null ? "Sin rango" : rank.getNombre();
        source.getSender().sendMessage(Component.text("===== Perfil de " + target.getNombre() + " =====", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text("Rango: " + rankName, NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Nivel: " + target.getNivel(), NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Monedas: " + target.getMonedas(), NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Horas jugadas: " + target.getHorasJugadas(), NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Miembro desde: " + DATE_FORMAT.format(target.getFechaIngreso()), NamedTextColor.GRAY));
        java.util.List<Sancion> history = sanciones.obtenerSancionesPorJugador(target.getUuid());
        long active = sanciones.getSancionesActivas().stream()
                .filter(s -> s.getJugadorId().equals(target.getUuid())).count();
        source.getSender().sendMessage(Component.text("Sanciones activas: " + active + " (historial: " + history.size() + ")",
                active == 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
    }
}
