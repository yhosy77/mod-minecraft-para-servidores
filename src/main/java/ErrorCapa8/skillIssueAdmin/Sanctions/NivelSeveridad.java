package ErrorCapa8.skillIssueAdmin.Sanctions;

/** Escala de severidad de las sanciones (RF-07 y RF-08): Mute → Baneo temporal → Baneo permanente. */
public enum NivelSeveridad {
    MUTE("Mute"),
    BANEO_TEMPORAL("Baneo temporal"),
    BANEO_PERMANENTE("Baneo permanente");

    private final String etiqueta;

    NivelSeveridad(String etiqueta) { this.etiqueta = etiqueta; }

    public String etiqueta() { return etiqueta; }

    /** Un nivel más arriba en la escala (el permanente ya es el máximo). */
    public NivelSeveridad subir() {
        return this == BANEO_PERMANENTE ? BANEO_PERMANENTE : values()[ordinal() + 1];
    }
}
