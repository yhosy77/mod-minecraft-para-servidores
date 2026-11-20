package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.JugadorNoEncontradoException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

/**
 * /jugador registrar <nick> [nivel] [monedas] : registra un jugador nuevo (RF-02).
 * /jugador monedas <nick> <cantidad>           : asigna las monedas de un jugador.
 */
public final class JugadorCommand extends ComandoAdministrativo {

    public JugadorCommand(GestorJugadores gestor) {
        super("jugador", "Registra jugadores y ajusta sus monedas", gestor);
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length >= 2 && args[0].equalsIgnoreCase("registrar") && args.length <= 4) {
            try {
                int nivel = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
                long monedas = args.length >= 4 ? Long.parseLong(args[3]) : 0;
                Jugador nuevo = gestorJugadores.registrarJugadorManual(args[1], nivel, monedas);
                exito(admin, "Jugador " + nuevo.getNombre() + " registrado (nivel " + nuevo.getNivel()
                        + ", " + nuevo.getMonedas() + " monedas, rango " + nuevo.getRangoId() + ").");
            } catch (NumberFormatException exception) {
                error(admin, "El nivel y las monedas deben ser números enteros.");
            } catch (IllegalArgumentException exception) {
                error(admin, exception.getMessage());
            }
            return;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("monedas")) {
            try {
                Jugador jugador = gestorJugadores.buscarJugador(args[1], true);
                long monedas = Long.parseLong(args[2]);
                gestorJugadores.establecerMonedas(jugador.getUuid(), monedas);
                exito(admin, jugador.getNombre() + " ahora tiene " + monedas + " monedas.");
            } catch (JugadorNoEncontradoException exception) {
                error(admin, exception.getMessage());
            } catch (NumberFormatException exception) {
                error(admin, "La cantidad de monedas debe ser un número entero.");
            } catch (IllegalArgumentException exception) {
                error(admin, exception.getMessage());
            }
            return;
        }
        uso(admin, "/jugador registrar <nick> [nivel] [monedas]");
        uso(admin, "/jugador monedas <nick> <cantidad>");
    }
}
