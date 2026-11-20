package ErrorCapa8.skillIssueAdmin;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

/**
 * Comando que solo puede usar un jugador con rango ADMIN.
 * Hereda el flujo de ComandoBase y solo cambia la regla de permisos.
 */
public abstract class ComandoAdministrativo extends ComandoBase {
    protected final GestorJugadores gestorJugadores;

    protected ComandoAdministrativo(String nombreComando, String descripcion, GestorJugadores gestorJugadores) {
        super(nombreComando, descripcion);
        this.gestorJugadores = gestorJugadores;
    }

    @Override
    protected boolean tienePermiso(CommandSourceStack source) {
        return source.getExecutor() instanceof Player player && gestorJugadores.esAdministrador(player.getUniqueId());
    }

    @Override
    protected String mensajeSinPermiso() { return "No tienes permiso; se requiere el rango ADMIN."; }
}
