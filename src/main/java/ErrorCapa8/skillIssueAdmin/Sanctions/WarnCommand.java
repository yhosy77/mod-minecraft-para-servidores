package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** /warn <jugador> <severidad 1-3> <motivo...> : advierte a un jugador conectado. */
public final class WarnCommand extends ComandoAdministrativo {
    private final GestorSanciones sanciones;

    public WarnCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        super("warn", "Envía una advertencia visual y sonora a un jugador", jugadores);
        this.sanciones = sanciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length < 3) { uso(admin, "/warn <jugador> <severidad 1-3> <motivo>"); return; }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { error(admin, "El jugador " + args[0] + " no está conectado."); return; }
        int severidad;
        try {
            severidad = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            error(admin, "La severidad debe ser 1, 2 o 3.");
            return;
        }
        String motivo = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        try {
            // Las advertencias graves (3) requieren que el staff hable con el jugador.
            Advertido advertencia = new Advertido(target.getUniqueId(), target.getName(), admin.getUniqueId(),
                    admin.getName(), motivo, severidad, severidad == 3);
            sanciones.aplicarSancion(gestorJugadores.buscarJugador(target.getUniqueId()), advertencia);
            exito(admin, "Advertencia #" + advertencia.getNumero() + " (" + advertencia.getNombreSeveridad()
                    + ") enviada a " + target.getName() + ".");
        } catch (SancionInvalidaException exception) {
            error(admin, exception.getMessage());
        }
    }
}
