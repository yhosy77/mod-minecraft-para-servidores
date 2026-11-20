package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** /sanciones <jugador> : muestra el historial de sanciones y la reincidencia (funciona aunque esté desconectado). */
public final class SancionesCommand implements BasicCommand {
    private static final int MAXIMO_A_MOSTRAR = 10;
    private final GestorJugadores jugadores;
    private final GestorSanciones sanciones;
    public SancionesCommand(GestorJugadores jugadores, GestorSanciones sanciones) { this.jugadores = jugadores; this.sanciones = sanciones; }

    @Override public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player admin)) {
            source.getSender().sendMessage(Component.text("Este comando solo puede usarlo un jugador."));
            return;
        }
        if (!jugadores.esAdministrador(admin.getUniqueId())) {
            admin.sendMessage(Component.text("No tienes permiso; se requiere el rango ADMIN.", NamedTextColor.RED));
            return;
        }
        if (args.length != 1) {
            admin.sendMessage(Component.text("Uso: /sanciones <jugador>", NamedTextColor.YELLOW));
            return;
        }
        Jugador jugador = jugadores.obtenerJugadores().stream()
                .filter(j -> j.getNombre().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if (jugador == null) {
            admin.sendMessage(Component.text("No hay ningún jugador registrado con el nombre " + args[0] + ".", NamedTextColor.RED));
            return;
        }

        List<Sancion> lista = sanciones.obtenerSancionesPorJugador(jugador.getUuid());
        admin.sendMessage(Component.text("--- Sanciones de " + jugador.getNombre() + " ---", NamedTextColor.GOLD));
        if (lista.isEmpty()) admin.sendMessage(Component.text("No tiene sanciones.", NamedTextColor.GRAY));
        for (int i = 0; i < Math.min(lista.size(), MAXIMO_A_MOSTRAR); i++)
            admin.sendMessage(Component.text(lista.get(i).getResumen(), NamedTextColor.WHITE));
        if (lista.size() > MAXIMO_A_MOSTRAR)
            admin.sendMessage(Component.text("... y " + (lista.size() - MAXIMO_A_MOSTRAR) + " más.", NamedTextColor.GRAY));
        admin.sendMessage(Component.text("Reincidencia: " + sanciones.calcularReincidencia(jugador)
                + " (" + sanciones.getNivelReincidencia(jugador) + ")", NamedTextColor.YELLOW));
    }
}
