package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * /sanciones <jugador> : historial cronológico de sanciones (RF-04), estado actual
 * y reincidencia en los últimos 60 días (RF-08). Funciona aunque el jugador esté desconectado.
 */
public final class SancionesCommand extends ComandoAdministrativo {
    private static final int MAXIMO_A_MOSTRAR = 10;
    private final GestorSanciones sanciones;

    public SancionesCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        super("sanciones", "Muestra el historial de sanciones y la reincidencia de un jugador", jugadores);
        this.sanciones = sanciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length != 1) { uso(admin, "/sanciones <jugador>"); return; }
        Jugador jugador = gestorJugadores.obtenerJugadores().stream()
                .filter(j -> j.getNombre().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if (jugador == null) { error(admin, "No hay ningún jugador registrado con el nombre " + args[0] + "."); return; }

        List<Sancion> lista = sanciones.obtenerSancionesPorJugador(jugador.getUuid());
        admin.sendMessage(Component.text("--- Sanciones de " + jugador.getNombre() + " ---", NamedTextColor.GOLD));
        admin.sendMessage(Component.text("Estado: " + sanciones.estadoJugador(jugador.getUuid()), NamedTextColor.YELLOW));
        if (lista.isEmpty()) info(admin, "No tiene sanciones.");
        for (int i = 0; i < Math.min(lista.size(), MAXIMO_A_MOSTRAR); i++)
            admin.sendMessage(Component.text(lista.get(i).getResumen(), NamedTextColor.WHITE));
        if (lista.size() > MAXIMO_A_MOSTRAR) info(admin, "... y " + (lista.size() - MAXIMO_A_MOSTRAR) + " más.");
        admin.sendMessage(Component.text("Reincidencia: " + sanciones.getNivelReincidencia(jugador), NamedTextColor.YELLOW));
    }
}
