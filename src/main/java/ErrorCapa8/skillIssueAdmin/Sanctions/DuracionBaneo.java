package ErrorCapa8.skillIssueAdmin.Sanctions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.bukkit.Material;

public enum DuracionBaneo {
    UN_DIA("1 día", 1, Material.CLOCK),
    TRES_DIAS("3 días", 3, Material.CLOCK),
    SIETE_DIAS("7 días", 7, Material.CLOCK),
    CATORCE_DIAS("14 días", 14, Material.CLOCK),
    TREINTA_DIAS("30 días", 30, Material.CLOCK),
    PERMANENTE("Permanente", 0, Material.OBSIDIAN);

    private final String etiqueta;
    private final int dias;
    private final Material icono;
    DuracionBaneo(String etiqueta, int dias, Material icono) { this.etiqueta = etiqueta; this.dias = dias; this.icono = icono; }
    public String etiqueta() { return etiqueta; }
    public Material icono() { return icono; }
    public Instant calcularExpiracion(Instant creadoEn) { return this == PERMANENTE ? null : creadoEn.plus(dias, ChronoUnit.DAYS); }
}
