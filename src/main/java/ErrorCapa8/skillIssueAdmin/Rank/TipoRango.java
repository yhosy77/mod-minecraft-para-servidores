package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.Set;

public enum TipoRango {
    ADMIN(Set.of("skillissueadmin.rank.admin", "skillissueadmin.moderation", "skillissueadmin.reports")),
    OP(Set.of("skillissueadmin.rank.op", "skillissueadmin.moderation")),
    MOD(Set.of("skillissueadmin.rank.mod", "skillissueadmin.moderation")),
    PLAYER(Set.of("skillissueadmin.rank.player")),
    /** Rango exclusivo pagado fuera del sistema (RF-12): solo cosmético, nunca permisos de staff. */
    EXCLUSIVO(Set.of("skillissueadmin.rank.player", "skillissueadmin.rank.exclusivo"));

    private final Set<String> permisos;
    TipoRango(Set<String> permisos) { this.permisos = permisos; }
    public Set<String> permisos() { return permisos; }
    public boolean puedeGestionarRangos() { return this == ADMIN; }
    /** Solo los rangos de staff tienen permisos elevados; PLAYER y EXCLUSIVO no. */
    public boolean tienePermisosElevados() { return this == ADMIN || this == OP || this == MOD; }
}
