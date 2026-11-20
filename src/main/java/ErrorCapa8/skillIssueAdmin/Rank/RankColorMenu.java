package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.EnumSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Shared style editor used by both new-rank creation and editing existing ranks. */
public final class RankColorMenu implements InventoryHolder {
    private static final NamedTextColor[] COLORS = {NamedTextColor.BLACK, NamedTextColor.DARK_BLUE, NamedTextColor.DARK_GREEN,
            NamedTextColor.DARK_AQUA, NamedTextColor.DARK_RED, NamedTextColor.DARK_PURPLE, NamedTextColor.GOLD,
            NamedTextColor.GRAY, NamedTextColor.DARK_GRAY, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW, NamedTextColor.WHITE};
    private static final Material[] COLOR_ICONS = {Material.BLACK_DYE, Material.BLUE_DYE, Material.GREEN_DYE, Material.CYAN_DYE,
            Material.RED_DYE, Material.PURPLE_DYE, Material.ORANGE_DYE, Material.LIGHT_GRAY_DYE, Material.GRAY_DYE,
            Material.LAPIS_LAZULI, Material.LIME_DYE, Material.LIGHT_BLUE_DYE, Material.REDSTONE, Material.MAGENTA_DYE,
            Material.YELLOW_DYE, Material.BONE_MEAL};
    private static final TextDecoration[] FORMATS = {TextDecoration.BOLD, TextDecoration.ITALIC, TextDecoration.UNDERLINED,
            TextDecoration.STRIKETHROUGH, TextDecoration.OBFUSCATED};
    private static final int[] FORMAT_SLOTS = {28,29,30,31,32};
    private static final Material[] FORMAT_ICONS = {Material.IRON_INGOT, Material.FEATHER, Material.NAME_TAG, Material.SHEARS, Material.MAGMA_CREAM};
    private static final String[] FORMAT_LABELS = {"Negrita", "Cursiva", "Subrayado", "Tachado", "Ofuscado"};

    private final Inventory inventory;
    private final boolean creation;
    private final String rangoId;
    private final String previewName;
    private final TipoRango tipo;
    private TextColor color;
    private final EnumSet<TextDecoration> formatos;

    public RankColorMenu(TipoRango tipo) {
        this(true, null, tipo.name(), tipo, Rango.colorPredeterminado(tipo), Set.of());
    }
    public RankColorMenu(Rango rango) {
        this(false, rango.getId(), rango.getNombre(), rango.getTipo(), rango.getColor(), rango.getFormatos());
    }
    private RankColorMenu(boolean creation, String rangoId, String previewName, TipoRango tipo, TextColor color, Set<TextDecoration> formatos) {
        this.creation = creation; this.rangoId = rangoId; this.previewName = previewName; this.tipo = tipo;
        this.color = color; this.formatos = formatos == null || formatos.isEmpty()
                ? EnumSet.noneOf(TextDecoration.class) : EnumSet.copyOf(formatos);
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Estilo • " + (creation ? tipo : previewName)));
        refresh();
    }

    public void setColor(TextColor color) { this.color = color; refresh(); }
    public void toggle(TextDecoration decoration) {
        if (!formatos.add(decoration)) formatos.remove(decoration);
        refresh();
    }
    public TextColor color() { return color; }
    public Set<TextDecoration> formatos() { return Set.copyOf(formatos); }
    public boolean creation() { return creation; }
    public String rangoId() { return rangoId; }
    public String previewName() { return previewName; }
    public TipoRango tipo() { return tipo; }

    private void refresh() {
        inventory.clear();
        RankItems.frame(inventory);
        inventory.setItem(4, RankItems.item(Material.NAME_TAG, previewComponent(), "§7Vista previa: el nombre conserva color blanco."));
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < COLORS.length; i++) {
            String label = NamedTextColor.NAMES.keyOr(COLORS[i], "gray");
            inventory.setItem(slots[i], RankItems.item(COLOR_ICONS[i], Component.text(label, COLORS[i]),
                    "§7Color estándar", color.equals(COLORS[i]) ? "§aSeleccionado" : "§7Clic para seleccionar"));
        }
        for (int i = 0; i < FORMATS.length; i++) {
            boolean enabled = formatos.contains(FORMATS[i]);
            String state = enabled ? "§aActivado ✓" : "§7Desactivado";
            inventory.setItem(FORMAT_SLOTS[i], RankItems.item(enabled ? Material.LIME_DYE : FORMAT_ICONS[i],
                    Component.text(FORMAT_LABELS[i], enabled ? NamedTextColor.GREEN : NamedTextColor.GRAY),
                    "§7Formato: " + FORMATS[i].name(), state, "§7Clic para alternar"));
        }
        inventory.setItem(34, RankItems.item(Material.FIREWORK_STAR, "§dTonos RGB", "§7Elegir un tono RGB o introducir hexadecimal."));
        inventory.setItem(45, RankItems.item(Material.BARRIER, "§cCancelar", "§7Descartar todos los cambios."));
        inventory.setItem(49, RankItems.item(Material.LIME_CONCRETE, creation ? "§aConfirmar estilo" : "§aGuardar estilo"));
        inventory.setItem(53, RankItems.item(Material.PAPER, "§bColor actual: §f" + colorPersistido(), "§7Formatos activos: " + formatos.size()));
    }

    private Component previewComponent() {
        Component prefix = Component.text("[" + previewName.toUpperCase(java.util.Locale.ROOT) + "] ", color);
        for (TextDecoration decoration : TextDecoration.values()) prefix = prefix.decoration(decoration, formatos.contains(decoration));
        return Component.empty().append(prefix).append(Component.text("ImFelipeVM", NamedTextColor.WHITE));
    }
    private String colorPersistido() {
        if (color instanceof NamedTextColor named) return NamedTextColor.NAMES.keyOr(named, "gray");
        return String.format(java.util.Locale.ROOT, "#%06X", color.value());
    }
    static TextColor colorAt(int slot) {
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < COLORS.length; i++) if (slots[i] == slot) return COLORS[i];
        return null;
    }
    static TextDecoration formatAt(int slot) {
        for (int i = 0; i < FORMAT_SLOTS.length; i++) if (FORMAT_SLOTS[i] == slot) return FORMATS[i];
        return null;
    }
    @Override public Inventory getInventory() { return inventory; }
}
