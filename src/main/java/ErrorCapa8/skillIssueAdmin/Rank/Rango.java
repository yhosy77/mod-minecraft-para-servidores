package ErrorCapa8.skillIssueAdmin.Rank;

import java.text.Normalizer;
import java.util.Locale;
import java.util.EnumSet;
import java.util.Set;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Rango {
    private final String id;
    private final String nombre;
    private final TipoRango tipo;
    private boolean base;
    private TextColor color;
    private final EnumSet<TextDecoration> formatos;

    public Rango(String nombre, TipoRango tipo, boolean base) {
        this(normalizarId(nombre), nombre.trim(), tipo, base, colorPredeterminado(tipo));
    }
    public Rango(String id, String nombre, TipoRango tipo, boolean base) {
        this(id, nombre, tipo, base, colorPredeterminado(tipo));
    }
    public Rango(String id, String nombre, TipoRango tipo, boolean base, TextColor color) {
        this(id, nombre, tipo, base, color, Set.of());
    }
    public Rango(String id, String nombre, TipoRango tipo, boolean base, TextColor color, Set<TextDecoration> formatos) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre del rango no puede estar vacío.");
        this.id = id;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.base = base;
        this.color = color == null ? colorPredeterminado(tipo) : color;
        this.formatos = formatos == null || formatos.isEmpty() ? EnumSet.noneOf(TextDecoration.class) : EnumSet.copyOf(formatos);
    }
    public static String normalizarId(String nombre) {
        String normalized = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return normalized.isBlank() ? "rango" : normalized;
    }
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoRango getTipo() { return tipo; }
    public boolean esBase() { return base; }
    public void setBase(boolean base) { this.base = base; }
    public Set<String> getPermisos() { return tipo.permisos(); }
    public boolean tienePermiso(String permiso) { return getPermisos().contains(permiso); }
    public boolean esValidoComoPredeterminado() { return tipo == TipoRango.PLAYER && !tipo.tienePermisosElevados(); }
    public TextColor getColor() { return color; }
    public String getColorNombre() {
        if (color instanceof NamedTextColor named) return NamedTextColor.NAMES.keyOr(named, "gray");
        return String.format(Locale.ROOT, "#%06X", color.value());
    }
    public Set<TextDecoration> getFormatos() { return Set.copyOf(formatos); }
    public boolean tieneFormato(TextDecoration formato) { return formatos.contains(formato); }
    public void setColor(TextColor color) { this.color = color == null ? colorPredeterminado(tipo) : color; }
    public void setFormatos(Set<TextDecoration> formatos) {
        this.formatos.clear();
        if (formatos != null) this.formatos.addAll(formatos);
    }
    public static TextColor parseColor(String value) {
        if (value == null || value.isBlank()) return NamedTextColor.GRAY;
        NamedTextColor named = NamedTextColor.NAMES.value(value.toLowerCase(Locale.ROOT));
        if (named != null) return named;
        try {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            if (hex.matches("(?i)[0-9a-f]{6}")) return TextColor.color(Integer.parseInt(hex, 16));
        } catch (NumberFormatException ignored) { }
        return NamedTextColor.GRAY;
    }
    public static NamedTextColor colorPredeterminado(TipoRango tipo) {
        return switch (tipo) {
            case ADMIN -> NamedTextColor.RED;
            case OP -> NamedTextColor.GOLD;
            case MOD -> NamedTextColor.AQUA;
            case PLAYER -> NamedTextColor.GRAY;
        };
    }
}
