package ErrorCapa8.skillIssueAdmin.Sanctions;

import java.util.UUID;

/** Temporary GUI state; it is never persisted until an administrator confirms. */
public final class BanDraft {
    private final UUID jugadorId;
    private final String jugadorNombre;
    private CausaBaneo causa;
    private String detalleCausa = "";
    private DuracionBaneo duracion;
    private String mensaje = "";

    public BanDraft(UUID jugadorId, String jugadorNombre) { this.jugadorId = jugadorId; this.jugadorNombre = jugadorNombre; }
    public UUID getJugadorId() { return jugadorId; }
    public String getJugadorNombre() { return jugadorNombre; }
    public CausaBaneo getCausa() { return causa; }
    public String getDetalleCausa() { return detalleCausa; }
    public DuracionBaneo getDuracion() { return duracion; }
    public String getMensaje() { return mensaje; }
    public void setCausa(CausaBaneo causa, String detalle) { this.causa = causa; this.detalleCausa = detalle == null ? "" : detalle.trim(); }
    public void setDuracion(DuracionBaneo duracion) { this.duracion = duracion; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje == null ? "" : mensaje.trim(); }
    public boolean estaCompleto() {
        return jugadorId != null && jugadorNombre != null && causa != null
                && (causa != CausaBaneo.OTRA || !detalleCausa.isBlank()) && duracion != null;
    }
}
