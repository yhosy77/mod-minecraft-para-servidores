package ErrorCapa8.skillIssueAdmin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import org.bukkit.entity.Player;

/** /donar <monto> [mensaje] : el jugador registra una donación, sin recibir nada a cambio (RF-13). */
public final class DonarCommand extends ComandoBase {
    private final GestorTransacciones transacciones;

    public DonarCommand(GestorTransacciones transacciones) {
        super("donar", "Registra una donación al servidor");
        this.transacciones = transacciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player player = jugador(source);
        if (args.length < 1) { uso(player, "/donar <monto> [mensaje]"); return; }
        double monto;
        try {
            monto = Double.parseDouble(args[0].replace(",", "."));
        } catch (NumberFormatException exception) {
            error(player, "El monto debe ser un número mayor a cero (ej: /donar 5000).");
            return;
        }
        String mensaje = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
        try {
            Donacion donacion = transacciones.registrarDonacion(player.getName(), monto, mensaje);
            exito(player, "¡Gracias por tu donación de $" + donacion.getMonto() + "! (" + donacion.getIdDonacion() + ")");
            info(player, "Las donaciones no dan recompensas: tu rango y tus monedas no cambian.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            error(player, exception.getMessage());
        }
    }
}
