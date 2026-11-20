package ErrorCapa8.skillIssueAdmin.Report;

import java.util.Locale;

/**
 * Estados de un reporte (RF-06): Abierto → En revisión → Resuelto o Descartado.
 * Un reporte cerrado (Resuelto o Descartado) no se puede reabrir.
 */
public enum EstadoReporte {
    ABIERTO("Abierto"),
    EN_REVISION("En revisión"),
    RESUELTO("Resuelto"),
    DESCARTADO("Descartado");

    private final String etiqueta;

    EstadoReporte(String etiqueta) { this.etiqueta = etiqueta; }

    public String etiqueta() { return etiqueta; }

    public boolean estaCerrado() { return this == RESUELTO || this == DESCARTADO; }

    /** Transiciones permitidas por el RF-06. */
    public boolean puedePasarA(EstadoReporte nuevo) {
        return switch (this) {
            case ABIERTO -> nuevo == EN_REVISION;
            case EN_REVISION -> nuevo == RESUELTO || nuevo == DESCARTADO;
            case RESUELTO, DESCARTADO -> false;
        };
    }

    /** Siguiente paso normal del flujo (null si ya está cerrado). */
    public EstadoReporte siguiente() {
        return switch (this) {
            case ABIERTO -> EN_REVISION;
            case EN_REVISION -> RESUELTO;
            case RESUELTO, DESCARTADO -> null;
        };
    }

    /** Convierte lo que escribe el admin ("abierto", "en_revision", "revision"...) en un estado, o null. */
    public static EstadoReporte desdeTexto(String texto) {
        if (texto == null) return null;
        String normalizado = texto.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_').replace("Ó", "O");
        if (normalizado.equals("REVISION")) return EN_REVISION;
        for (EstadoReporte estado : values()) if (estado.name().equals(normalizado)) return estado;
        return null;
    }
}
