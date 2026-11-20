package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/**
 * Mute: silencia al jugador en el chat durante cierta cantidad de minutos.
 *
 * aplicarEfecto() le avisa al jugador. El bloqueo real de los mensajes lo hace
 * MuteEnforcementListener preguntando a GestorSanciones.estaMuteado(uuid).
 */
public final class Mute extends Sancion {
    public static final String CANAL_GLOBAL = "global";

    private final int duracionMinutos;
    private final String canalSilenciado;

    /** Constructor para un mute NUEVO (lo usa MuteCommand). */
    public Mute(UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre,
                String motivo, int duracionMinutos, String canalSilenciado) throws SancionInvalidaException {
        super(UUID.randomUUID(), getSiguienteId(), jugadorId, jugadorNombre, responsableId, responsableNombre,
                Instant.now(), motivo, true, "");
        if (motivo == null || motivo.isBlank()) throw new SancionInvalidaException("El motivo del mute no puede estar vacío.");
        if (duracionMinutos <= 0) throw new SancionInvalidaException("Un mute debe durar al menos 1 minuto.");
        this.duracionMinutos = duracionMinutos;
        this.canalSilenciado = canalSilenciado == null || canalSilenciado.isBlank() ? CANAL_GLOBAL : limpiar(canalSilenciado);
    }

    /** Constructor para un mute cargado desde sanciones.txt. */
    Mute(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre,
         Instant creadaEn, String motivo, boolean activa, String motivoPerdon, int duracionMinutos, String canalSilenciado) {
        super(id, numero, jugadorId, jugadorNombre, responsableId, responsableNombre, creadaEn, motivo, activa, motivoPerdon);
        this.duracionMinutos = duracionMinutos;
        this.canalSilenciado = canalSilenciado;
    }

    /** Efecto del mute: avisa al jugador que quedó silenciado. */
    @Override public boolean aplicarEfecto(Jugador j) {
        Player player = buscarJugadorConectado(j);
        if (player == null) return false;
        player.sendMessage(Component.text("Has sido silenciado por " + duracionMinutos + " minuto(s).", NamedTextColor.RED));
        player.sendMessage(Component.text("Motivo: ", NamedTextColor.GRAY).append(Component.text(getMotivo(), NamedTextColor.WHITE)));
        return true;
    }

    @Override public boolean haExpirado(Instant ahora) { return !getExpiraEn().isAfter(ahora); }

    @Override public String getTipo() { return "MUTE"; }

    @Override protected String datosExtra() { return duracionMinutos + SEPARADOR + canalSilenciado; }

    /** Mensaje que ve el jugador cuando intenta escribir estando silenciado. */
    public Component mensajeSilenciado() {
        return Component.text("Estás silenciado hasta " + FORMATO_FECHA.format(getExpiraEn()) + ". ", NamedTextColor.RED)
                .append(Component.text("Motivo: " + getMotivo(), NamedTextColor.GRAY));
    }

    public Instant getExpiraEn() { return getCreadaEn().plus(duracionMinutos, ChronoUnit.MINUTES); }
    public int getDuracionMinutos() { return duracionMinutos; }
    public String getCanalSilenciado() { return canalSilenciado; }
}
