package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.Set;

public enum TipoRango {
    ADMIN(Set.of("skillissueadmin.rank.admin", "skillissueadmin.moderation", "skillissueadmin.reports")),
    OP(Set.of("skillissueadmin.rank.op", "skillissueadmin.moderation")),
    MOD(Set.of("skillissueadmin.rank.mod", "skillissueadmin.moderation")),
    PLAYER(Set.of("skillissueadmin.rank.player"));

    private final Set<String> permisos;
    TipoRango(Set<String> permisos) { this.permisos = permisos; }
    public Set<String> permisos() { return permisos; }
    public boolean puedeGestionarRangos() { return this == ADMIN; }
    public boolean tienePermisosElevados() { return this != PLAYER; }
}
