package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** /perdonar <numero> [motivo...] : levanta (revoca) una sanción; sirve para quitar baneos, mutes y advertencias. */
public final class PerdonarCommand implements BasicCommand {
    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;
    public PerdonarCommand(GestorJugadores jugadores, GestorSanciones sanciones) { this.jugadores = jugadores; this.sanciones = sanciones; }

    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player admin)) {
            source.getSender().sendMessage(Component.text("Este comando solo puede usarlo un jugador."));
            return;
        }
        if (!jugadores.esAdministrador(admin.getUniqueId())) {
            admin.sendMessage(Component.text("No tienes permiso; se requiere el rango ADMIN.", NamedTextColor.RED));
            return;
        }
        if (args.length < 1) {
            admin.sendMessage(Component.text("Uso: /perdonar <número de sanción> [motivo]", NamedTextColor.YELLOW));
            return;
        }
        int numero;
        try {
            numero = Integer.parseInt(args[0].replace("#", ""));
        } catch (NumberFormatException exception) {
            admin.sendMessage(Component.text("El número de sanción debe ser un entero (ej: /perdonar 5).", NamedTextColor.RED));
            return;
        }
        String motivo = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
        if (sanciones.levantarSancion(numero, motivo))
            admin.sendMessage(Component.text("Sanción #" + numero + " revocada.", NamedTextColor.GREEN));
        else
            admin.sendMessage(Component.text("No existe una sanción activa con el número #" + numero + ".", NamedTextColor.RED));
    }
}
