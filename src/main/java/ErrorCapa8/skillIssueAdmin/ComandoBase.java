package ErrorCapa8.skillIssueAdmin;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Clase base abstracta de todos los comandos del plugin (polimorfismo de inclusión).
 *
 * Implementa la interfaz BasicCommand de Paper y define el flujo común en execute():
 * 1. Verifica si el comando exige que lo use un jugador (no la consola).
 * 2. Verifica los permisos con tienePermiso().
 * 3. Delega el trabajo real en ejecutarComando(), que cada subclase implementa a su manera.
 *
 * Así SkillIssueAdmin registra todos los comandos como ComandoBase sin saber qué hace cada uno.
 */
public abstract class ComandoBase implements BasicCommand {
    protected final String nombreComando;
    protected final String descripcion;

    protected ComandoBase(String nombreComando, String descripcion) {
        this.nombreComando = nombreComando;
        this.descripcion = descripcion;
    }

    @Override
    public final void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (requiereJugador() && !(source.getExecutor() instanceof Player)) {
            sender.sendMessage(Component.text("Este comando solo puede usarlo un jugador.", NamedTextColor.RED));
            return;
        }
        if (!tienePermiso(source)) {
            sender.sendMessage(Component.text(mensajeSinPermiso(), NamedTextColor.RED));
            return;
        }
        ejecutarComando(source, args);
    }

    /** Lógica propia de cada comando. */
    protected abstract void ejecutarComando(CommandSourceStack source, String[] args);

    /** Por defecto los comandos se usan dentro del juego; una subclase puede permitir la consola. */
    protected boolean requiereJugador() { return true; }

    /** Por defecto cualquiera puede usarlo; las subclases restringen. */
    protected boolean tienePermiso(CommandSourceStack source) { return true; }

    protected String mensajeSinPermiso() { return "No tienes permiso para usar /" + nombreComando + "."; }

    /** El jugador que ejecuta el comando (solo válido si requiereJugador() es true). */
    protected static Player jugador(CommandSourceStack source) {
        return (Player) source.getExecutor();
    }

    protected static void error(CommandSender destino, String mensaje) {
        destino.sendMessage(Component.text(mensaje, NamedTextColor.RED));
    }

    protected static void exito(CommandSender destino, String mensaje) {
        destino.sendMessage(Component.text(mensaje, NamedTextColor.GREEN));
    }

    protected static void info(CommandSender destino, String mensaje) {
        destino.sendMessage(Component.text(mensaje, NamedTextColor.GRAY));
    }

    protected static void uso(CommandSender destino, String uso) {
        destino.sendMessage(Component.text("Uso: " + uso, NamedTextColor.YELLOW));
    }

    public String getNombreComando() { return nombreComando; }
    public String getDescripcion() { return descripcion; }
}
