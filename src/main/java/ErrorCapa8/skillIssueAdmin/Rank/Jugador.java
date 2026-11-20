package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.UUID;

public final class Jugador {
    private final UUID uuid;
    private String nombre;
    private String rangoId;
    public Jugador(UUID uuid, String nombre, String rangoId) {
        this.uuid = uuid; this.nombre = nombre; this.rangoId = rangoId;
    }
    public UUID getUuid() { return uuid; }
    public String getNombre() { return nombre; }
    public String getRangoId() { return rangoId; }
    public void actualizar(String nombre, String rangoId) { this.nombre = nombre; this.rangoId = rangoId; }
    public void asignarRango(String rangoId) { this.rangoId = rangoId; }
}
