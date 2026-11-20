package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankListMenu implements InventoryHolder {
    public enum Mode { DEFAULT, DELETE, COLOR }
    private final Inventory inventory;
    private final Mode mode;
    private final int page;
    private final List<Rango> rangos;
    public RankListMenu(GestorJugadores gestor, Mode mode, int page) {
        this.mode = mode; this.page = Math.max(0, page);
        rangos = mode == Mode.DEFAULT ? gestor.obtenerRangos(TipoRango.PLAYER)
                : mode == Mode.DELETE ? gestor.obtenerRangos().stream().filter(r -> !r.esBase()).toList()
                : gestor.obtenerRangos().stream().toList();
        String title = switch (mode) {
            case DEFAULT -> "§8Elegir rango predeterminado";
            case DELETE -> "§8Elegir rango para eliminar";
            case COLOR -> "§8Elegir rango para colorear";
        };
        inventory = Bukkit.createInventory(this, 54, Component.text(title));
        RankItems.frame(inventory);
        int[] slots = RankItems.contentSlots();
        int from = this.page * slots.length;
        for (int i = 0; i < slots.length && from + i < rangos.size(); i++) {
            Rango rango = rangos.get(from + i);
            Material icon = rango.getTipo() == TipoRango.PLAYER ? Material.LEATHER_CHESTPLATE
                    : rango.getTipo() == TipoRango.OP || rango.getTipo() == TipoRango.MOD ? Material.IRON_CHESTPLATE : Material.NETHERITE_CHESTPLATE;
            String marker = gestor.obtenerRangoPredeterminado().getId().equals(rango.getId()) ? " §a(Predeterminado)" : "";
            inventory.setItem(slots[i], RankItems.item(icon, "§f" + rango.getNombre() + marker,
                    "§7Tipo: §e" + rango.getTipo(), "§7Permisos: §f" + String.join(", ", rango.getPermisos()),
                    mode == Mode.DELETE ? "§7Clic para revisar eliminación"
                            : mode == Mode.COLOR ? "§7Color actual: " + rango.getColorNombre() : "§7Clic para establecer predeterminado"));
        }
        if (rangos.isEmpty()) inventory.setItem(22, RankItems.item(Material.BARRIER,
                mode == Mode.DELETE ? "§cNo hay rangos personalizados" : mode == Mode.DEFAULT ? "§cNo hay rangos PLAYER disponibles" : "§cNo hay rangos"));
        inventory.setItem(49, RankItems.item(Material.ARROW, "§e← Volver"));
        if (this.page > 0) inventory.setItem(45, RankItems.item(Material.ARROW, "§ePágina anterior"));
        if (from + slots.length < rangos.size()) inventory.setItem(53, RankItems.item(Material.ARROW, "§ePágina siguiente"));
        inventory.setItem(4, RankItems.item(Material.BOOK, switch (mode) {
            case DEFAULT -> "§bSolo rangos PLAYER";
            case DELETE -> "§bRangos base protegidos";
            case COLOR -> "§bEl color afecta el prefijo visible";
        }));
    }
    public Rango rangoAt(int rawSlot) {
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < slots.length; i++) if (slots[i] == rawSlot) {
            int index = page * slots.length + i;
            return index < rangos.size() ? rangos.get(index) : null;
        }
        return null;
    }
    public Mode mode() { return mode; }
    public int page() { return page; }
    @Override public Inventory getInventory() { return inventory; }
}
