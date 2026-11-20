package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.Jugador;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class Baneo extends Sancion {
    private final CausaBaneo causa;
    private final String detalleCausa;
    private final String mensaje;
    private final Instant expiraEn;
    private final boolean permanente;

    public Baneo(UUID id, UUID jugadorId, String jugadorNombre, CausaBaneo causa, String detalleCausa,
                 String mensaje, UUID responsableId, String responsableNombre, Instant creadaEn,
                 Instant expiraEn, boolean permanente) {
        super(id, jugadorId, jugadorNombre, responsableId, responsableNombre, creadaEn);
        if (permanente != (expiraEn == null)) throw new IllegalArgumentException("La duración permanente debe corresponder a una expiración nula.");
        this.causa = causa; this.detalleCausa = detalleCausa == null ? "" : detalleCausa.trim();
        this.mensaje = mensaje == null ? "" : mensaje.trim(); this.expiraEn = expiraEn; this.permanente = permanente;
    }

    /** Constructor para baneos cargados desde sanciones.txt (conserva número, estado y motivo de perdón). */
    Baneo(int numero, boolean activa, String motivoPerdon, UUID id, UUID jugadorId, String jugadorNombre,
          CausaBaneo causa, String detalleCausa, String mensaje, UUID responsableId, String responsableNombre,
          Instant creadaEn, Instant expiraEn, boolean permanente) {
        super(id, numero, jugadorId, jugadorNombre, responsableId, responsableNombre, creadaEn, "", activa, motivoPerdon);
        if (permanente != (expiraEn == null)) throw new IllegalArgumentException("La duración permanente debe corresponder a una expiración nula.");
        this.causa = causa; this.detalleCausa = detalleCausa == null ? "" : detalleCausa.trim();
        this.mensaje = mensaje == null ? "" : mensaje.trim(); this.expiraEn = expiraEn; this.permanente = permanente;
    }

    public CausaBaneo getCausa() { return causa; }
    public String getDetalleCausa() { return detalleCausa; }
    public String getMensaje() { return mensaje; }
    public Instant getExpiraEn() { return expiraEn; }
    public boolean esPermanente() { return permanente; }
    public boolean estaActivo(Instant ahora) { return permanente || expiraEn.isAfter(ahora); }
    public String causaCompleta() { return causa == CausaBaneo.OTRA && !detalleCausa.isBlank() ? detalleCausa : causa.etiqueta(); }
    public String duracionTexto() {
        if (permanente) return "Permanente";
        long dias = Math.max(1, java.time.Duration.between(getCreadaEn(), expiraEn).toDays());
        return dias + (dias == 1 ? " día" : " días");
    }
    public Component mensajeKick() {
        Component text = Component.text("SKILL ISSUE ADMIN", NamedTextColor.GOLD)
                .append(Component.newline()).append(Component.text("Has sido expulsado del servidor.", NamedTextColor.RED))
                .append(Component.newline()).append(Component.text("Causa: ", NamedTextColor.GRAY)).append(Component.text(causaCompleta(), NamedTextColor.WHITE))
                .append(Component.newline()).append(Component.text("Duración: ", NamedTextColor.GRAY)).append(Component.text(duracionTexto(), NamedTextColor.WHITE));
        if (!mensaje.isBlank()) text = text.append(Component.newline()).append(Component.text("Mensaje del administrador: ", NamedTextColor.GRAY)).append(Component.text(mensaje, NamedTextColor.WHITE));
        return text.append(Component.newline()).append(Component.text("Staff responsable: ", NamedTextColor.GRAY)).append(Component.text(getResponsableNombre(), NamedTextColor.WHITE));
    }
    public String mensajeIngreso() {
        StringBuilder text = new StringBuilder("SKILL ISSUE ADMIN\nHas sido expulsado del servidor.\nCausa: ")
                .append(causaCompleta()).append("\nDuración: ").append(duracionTexto());
        if (!mensaje.isBlank()) text.append("\nMensaje del administrador: ").append(mensaje);
        return text.append("\nStaff responsable: ").append(getResponsableNombre()).toString();
    }

    // ------------------------------------------------------------------
    // Implementación de los métodos abstractos de Sancion
    // ------------------------------------------------------------------

    /** Efecto del baneo: expulsa al jugador si está conectado. BanEnforcementListener le impide volver a entrar. */
    @Override public boolean aplicarEfecto(Jugador j) {
        Player player = buscarJugadorConectado(j);
        if (player == null) return false;
        player.kick(mensajeKick());
        return true;
    }

    @Override public String getTipo() { return "BANEO"; }

    @Override public CausaBaneo getTipoInfraccion() { return causa; }

    @Override public NivelSeveridad getNivelEnEscala() {
        return permanente ? NivelSeveridad.BANEO_PERMANENTE : NivelSeveridad.BANEO_TEMPORAL;
    }

    /** En un baneo el motivo es la causa elegida en el menú de /ban. */
    @Override public String getMotivo() { return causaCompleta(); }

    @Override public boolean haExpirado(Instant ahora) { return !estaActivo(ahora); }

    @Override protected String datosExtra() {
        return causa.name() + SEPARADOR + vacioComoGuion(detalleCausa) + SEPARADOR + vacioComoGuion(mensaje) + SEPARADOR
                + permanente + SEPARADOR + (expiraEn == null ? "-" : String.valueOf(expiraEn.toEpochMilli()));
    }
}
