package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.SancionInvalidaException;
import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Advertido: advertencia sonora y visual en pantalla.
 * No expulsa ni silencia; queda en el historial y cuenta para la reincidencia.
 * Las advertencias no vencen: solo dejan de contar si se revocan.
 */
public final class Advertido extends Sancion {
    private final int nivelSeveridad;          // 1 = leve, 2 = media, 3 = grave
    private final boolean requiereConfirmacion;

    /** Constructor para una advertencia NUEVA (la usa WarnCommand). */
    public Advertido(UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre,
                     String motivo, int nivelSeveridad, boolean requiereConfirmacion) throws SancionInvalidaException {
        super(UUID.randomUUID(), getSiguienteId(), jugadorId, jugadorNombre, responsableId, responsableNombre,
                Instant.now(), motivo, true, "");
        if (motivo == null || motivo.isBlank()) throw new SancionInvalidaException("El motivo de la advertencia no puede estar vacío.");
        if (nivelSeveridad < 1 || nivelSeveridad > 3) throw new SancionInvalidaException("La severidad debe estar entre 1 y 3.");
        this.nivelSeveridad = nivelSeveridad;
        this.requiereConfirmacion = requiereConfirmacion;
    }

    /** Constructor para una advertencia cargada desde sanciones.txt. */
    Advertido(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre,
              Instant creadaEn, String motivo, boolean activa, String motivoPerdon, int nivelSeveridad, boolean requiereConfirmacion) {
        super(id, numero, jugadorId, jugadorNombre, responsableId, responsableNombre, creadaEn, motivo, activa, motivoPerdon);
        this.nivelSeveridad = nivelSeveridad;
        this.requiereConfirmacion = requiereConfirmacion;
    }

    /** Efecto de la advertencia: título en pantalla + sonido + mensaje en el chat. */
    @Override public boolean aplicarEfecto(Jugador j) {
        Player player = buscarJugadorConectado(j);
        if (player == null) return false;

        player.showTitle(Title.title(
                Component.text("¡ADVERTENCIA!", NamedTextColor.RED).decorate(TextDecoration.BOLD),
                Component.text(getMotivo(), NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofSeconds(1))));

        // Más grave el sonido cuanto mayor es la severidad.
        float tono = nivelSeveridad == 3 ? 0.5f : nivelSeveridad == 2 ? 1.0f : 1.5f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, tono);

        player.sendMessage(Component.text("Has recibido una advertencia (" + getNombreSeveridad() + ").", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Motivo: ", NamedTextColor.GRAY).append(Component.text(getMotivo(), NamedTextColor.WHITE)));
        if (requiereConfirmacion)
            player.sendMessage(Component.text("Un miembro del staff hablará contigo sobre esta advertencia.", NamedTextColor.YELLOW));
        return true;
    }

    @Override public String getTipo() { return "ADVERTIDO"; }

    @Override protected String datosExtra() { return nivelSeveridad + SEPARADOR + requiereConfirmacion; }

    public String getNombreSeveridad() {
        return switch (nivelSeveridad) {
            case 1 -> "leve";
            case 2 -> "media";
            default -> "grave";
        };
    }

    public int getNivelSeveridad() { return nivelSeveridad; }
    public boolean isRequiereConfirmacion() { return requiereConfirmacion; }
}
