package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import org.bukkit.entity.Player;

/**
 * /perdonar <numero> <justificación...> : revoca una sanción (RF-07).
 * El jugador queda en estado "Advertido" durante 30 días.
 */
public final class PerdonarCommand extends ComandoAdministrativo {
    private final GestorSanciones sanciones;

    public PerdonarCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        super("perdonar", "Revoca una sanción por su número", jugadores);
        this.sanciones = sanciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length < 2) { uso(admin, "/perdonar <número de sanción> <justificación>"); return; }
        int numero;
        try {
            numero = Integer.parseInt(args[0].replace("#", ""));
        } catch (NumberFormatException exception) {
            error(admin, "El número de sanción debe ser un entero (ej: /perdonar 5 se disculpó).");
            return;
        }
        String justificacion = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (sanciones.levantarSancion(numero, justificacion))
            exito(admin, "Sanción #" + numero + " revocada. El jugador queda Advertido durante "
                    + GestorSanciones.PERIODO_PRUEBA_DIAS + " días.");
        else
            error(admin, "No existe una sanción activa con el número #" + numero + ".");
    }
}
