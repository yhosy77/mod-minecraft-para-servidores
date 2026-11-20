package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** /warn <jugador> <severidad 1-3> <motivo...> : advierte a un jugador conectado. */
public final class WarnCommand implements BasicCommand {
    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;
    public WarnCommand(GestorJugadores jugadores, GestorSanciones sanciones) { this.jugadores = jugadores; this.sanciones = sanciones; }

    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player admin)) {
            source.getSender().sendMessage(Component.text("Este comando solo puede usarlo un jugador."));
            return;
        }
        if (!jugadores.esAdministrador(admin.getUniqueId())) {
            admin.sendMessage(Component.text("No tienes permiso; se requiere el rango ADMIN.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            admin.sendMessage(Component.text("Uso: /warn <jugador> <severidad 1-3> <motivo>", NamedTextColor.YELLOW));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            admin.sendMessage(Component.text("El jugador " + args[0] + " no está conectado.", NamedTextColor.RED));
            return;
        }
        int severidad;
        try {
            severidad = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            admin.sendMessage(Component.text("La severidad debe ser 1, 2 o 3.", NamedTextColor.RED));
            return;
        }
        String motivo = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        try {
            // Las advertencias graves (3) requieren que el staff hable con el jugador.
            Advertido advertencia = new Advertido(target.getUniqueId(), target.getName(), admin.getUniqueId(),
                    admin.getName(), motivo, severidad, severidad == 3);
            sanciones.aplicarSancion(jugadores.buscarJugador(target.getUniqueId()), advertencia);
            admin.sendMessage(Component.text("Advertencia #" + advertencia.getNumero() + " (" + advertencia.getNombreSeveridad()
                    + ") enviada a " + target.getName() + ".", NamedTextColor.GREEN));
        } catch (SancionInvalidaException exception) {
            admin.sendMessage(Component.text(exception.getMessage(), NamedTextColor.RED));
        }
    }
}
