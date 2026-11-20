package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * /mute <jugador> <minutos> <tipo> <motivo...> : silencia a un jugador conectado (RF-03).
 * tipo = insultos | trampas | spam | toxicidad | exploits | otra.
 * Si el jugador reincide (RF-07 / RF-08), la sanción sube a baneo temporal o permanente.
 */
public final class MuteCommand extends ComandoAdministrativo {
    /** Duración del baneo temporal cuando un mute sube de nivel por reincidencia. */
    public static final int DIAS_BANEO_POR_REINCIDENCIA = 3;
    private final GestorSanciones sanciones;

    public MuteCommand(GestorJugadores jugadores, GestorSanciones sanciones) {
        super("mute", "Silencia a un jugador en el chat por unos minutos", jugadores);
        this.sanciones = sanciones;
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        Player admin = jugador(source);
        if (args.length < 4) {
            uso(admin, "/mute <jugador> <minutos> <insultos|trampas|spam|toxicidad|exploits|otra> <motivo>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { error(admin, "El jugador " + args[0] + " no está conectado."); return; }
        int minutos;
        try {
            minutos = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            error(admin, "Los minutos deben ser un número entero mayor a cero.");
            return;
        }
        CausaBaneo tipo = tipoInfraccion(args[2]);
        if (tipo == null) { error(admin, "Tipo de infracción inválido. Usa: insultos, trampas, spam, toxicidad, exploits u otra."); return; }
        String motivo = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        Jugador jugador = gestorJugadores.buscarJugador(target.getUniqueId());

        try {
            GestorSanciones.SugerenciaSeveridad sugerencia = sanciones.calcularNivelSugerido(jugador, tipo, NivelSeveridad.MUTE);
            if (!sugerencia.subio()) {
                Mute mute = new Mute(target.getUniqueId(), target.getName(), admin.getUniqueId(), admin.getName(),
                        tipo, motivo, minutos, Mute.CANAL_GLOBAL);
                sanciones.aplicarSancion(jugador, mute);
                exito(admin, "Mute #" + mute.getNumero() + " aplicado a " + target.getName() + " por " + minutos + " minuto(s).");
                return;
            }
            // RF-07 / RF-08: la sanción sube un nivel en la escala.
            boolean permanente = sugerencia.nivel() == NivelSeveridad.BANEO_PERMANENTE;
            Instant ahora = Instant.now();
            Baneo baneo = new Baneo(UUID.randomUUID(), target.getUniqueId(), target.getName(), tipo, motivo,
                    "Sanción aumentada por " + String.join(" y ", sugerencia.razones()),
                    admin.getUniqueId(), admin.getName(), ahora,
                    permanente ? null : ahora.plus(DIAS_BANEO_POR_REINCIDENCIA, ChronoUnit.DAYS), permanente);
            sanciones.aplicarSancion(jugador, baneo);
            exito(admin, "Por " + String.join(" y ", sugerencia.razones()) + ", el mute subió a "
                    + sugerencia.nivel().etiqueta().toLowerCase(Locale.ROOT)
                    + (permanente ? "" : " de " + DIAS_BANEO_POR_REINCIDENCIA + " días") + " (sanción #" + baneo.getNumero() + ").");
        } catch (SancionInvalidaException exception) {
            error(admin, exception.getMessage());
        }
    }

    static CausaBaneo tipoInfraccion(String texto) {
        try {
            return CausaBaneo.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length == 3) return Arrays.stream(CausaBaneo.values()).map(c -> c.name().toLowerCase(Locale.ROOT)).toList();
        return List.of();
    }
}
