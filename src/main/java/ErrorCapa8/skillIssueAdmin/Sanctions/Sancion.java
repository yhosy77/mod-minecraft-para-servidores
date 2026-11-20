package ErrorCapa8.skillIssueAdmin.Sanctions;

import java.time.Instant;
import java.util.UUID;

public abstract class Sancion {
    private final UUID id;
    private final UUID jugadorId;
    private final String jugadorNombre;
    private final UUID responsableId;
    private final String responsableNombre;
    private final Instant creadaEn;

    protected Sancion(UUID id, UUID jugadorId, String jugadorNombre, UUID responsableId,
                      String responsableNombre, Instant creadaEn) {
        this.id = id; this.jugadorId = jugadorId; this.jugadorNombre = jugadorNombre;
        this.responsableId = responsableId; this.responsableNombre = responsableNombre; this.creadaEn = creadaEn;
    }
    public UUID getId() { return id; }
    public UUID getJugadorId() { return jugadorId; }
    public String getJugadorNombre() { return jugadorNombre; }
    public UUID getResponsableId() { return responsableId; }
    public String getResponsableNombre() { return responsableNombre; }
    public Instant getCreadaEn() { return creadaEn; }
}
