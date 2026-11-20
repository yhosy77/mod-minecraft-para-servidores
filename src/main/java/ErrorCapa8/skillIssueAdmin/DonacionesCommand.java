package ErrorCapa8.skillIssueAdmin;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** /donaciones [dias] : total donado en el periodo (por defecto los últimos 30 días) (RF-13). */
public final class DonacionesCommand extends ComandoAdministrativo {
    private static final int DIAS_POR_DEFECTO = 30;
    private static final int MAXIMO_A_MOSTRAR = 10;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final GestorTransacciones transacciones;

    public DonacionesCommand(GestorJugadores jugadores, GestorTransacciones transacciones) {
        super("donaciones", "Muestra el total donado en un periodo", jugadores);
        this.transacciones = transacciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        int dias = DIAS_POR_DEFECTO;
        if (args.length == 1) {
            try {
                dias = Integer.parseInt(args[0]);
                if (dias <= 0) throw new NumberFormatException();
            } catch (NumberFormatException exception) {
                error(admin, "Los días deben ser un número entero mayor a cero.");
                return;
            }
        } else if (args.length > 1) { uso(admin, "/donaciones [días]"); return; }

        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = hasta.minusDays(dias);
        List<Donacion> lista = transacciones.donacionesEntre(desde, hasta);
        admin.sendMessage(Component.text("--- Donaciones de los últimos " + dias + " días ---", NamedTextColor.GOLD));
        admin.sendMessage(Component.text("Total donado: $" + transacciones.totalDonado(desde, hasta)
                + " en " + lista.size() + " donación(es)", NamedTextColor.YELLOW));
        for (int i = 0; i < Math.min(lista.size(), MAXIMO_A_MOSTRAR); i++) {
            Donacion d = lista.get(i);
            info(admin, FORMATO.format(d.getFecha()) + " - " + d.getNombreJugador() + ": $" + d.getMonto()
                    + (d.getMensaje().isBlank() ? "" : " \"" + d.getMensaje() + "\""));
        }
    }
}
