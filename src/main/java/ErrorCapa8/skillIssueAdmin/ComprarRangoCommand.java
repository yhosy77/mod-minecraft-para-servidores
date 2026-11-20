package ErrorCapa8.skillIssueAdmin;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import ErrorCapa8.skillIssueAdmin.Rank.Rango;
import ErrorCapa8.skillIssueAdmin.Rank.RankDisplayManager;
import ErrorCapa8.skillIssueAdmin.Rank.RankPermissionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * /comprarango <jugador> <rango> <valor> : el admin registra la compra de un rango exclusivo
 * pagado fuera del sistema (RF-12). Se guarda fecha, valor y rango, y se le asigna al jugador.
 */
public final class ComprarRangoCommand extends ComandoAdministrativo {
    private final GestorTransacciones transacciones;
    private final RankDisplayManager displayManager;
    private final RankPermissionManager permissionManager;

    public ComprarRangoCommand(GestorJugadores jugadores, GestorTransacciones transacciones,
                               RankDisplayManager displayManager, RankPermissionManager permissionManager) {
        super("comprarango", "Registra la compra de un rango exclusivo", jugadores);
        this.transacciones = transacciones;
        this.displayManager = displayManager;
        this.permissionManager = permissionManager;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length != 3) { uso(admin, "/comprarango <jugador> <rango> <valor>"); return; }
        try {
            Jugador jugador = gestorJugadores.buscarJugador(args[0], true);
            Rango rango = gestorJugadores.buscarRango(Rango.normalizarId(args[1]));
            if (rango == null) rango = gestorJugadores.buscarRangoPorNombre(args[1]);
            double valor = Double.parseDouble(args[2].replace(",", "."));
            CompraRango compra = transacciones.registrarCompra(jugador.getNombre(), rango, valor);
            gestorJugadores.asignarRango(jugador.getUuid(), rango.getId());
            Player online = Bukkit.getPlayer(jugador.getUuid());
            if (online != null) {
                permissionManager.apply(online);
                displayManager.refreshPlayer(online);
                exito(online, "¡Ahora tienes el rango exclusivo " + rango.getNombre() + "!");
            }
            exito(admin, "Compra " + compra.getIdCompra() + " registrada: " + jugador.getNombre() + " compró "
                    + rango.getNombre() + " por $" + valor + ".");
        } catch (JugadorNoEncontradoException exception) {
            error(admin, exception.getMessage());
        } catch (NumberFormatException exception) {
            error(admin, "El valor debe ser un número (ej: 15000).");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            error(admin, exception.getMessage());
        }
    }
}
