package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankAssignmentMenu implements InventoryHolder {
    private final Inventory inventory;
    private final Jugador jugador;
    private final List<Rango> rangos;
    private final int page;
    public RankAssignmentMenu(GestorJugadores gestor, Jugador jugador, int page) {
        this.jugador = jugador;
        this.page = Math.max(0, page);
        rangos = List.copyOf(gestor.obtenerRangos());
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Asignar rango • " + jugador.getNombre()));
        RankItems.frame(inventory);
        int[] slots = RankItems.contentSlots();
        int from = this.page * slots.length;
        for (int i = 0; i < slots.length && from + i < rangos.size(); i++) {
            Rango rango = rangos.get(from + i);
            Material icon = rango.getTipo() == TipoRango.PLAYER ? Material.LEATHER_CHESTPLATE
                    : rango.getTipo() == TipoRango.ADMIN ? Material.NETHERITE_CHESTPLATE : Material.IRON_CHESTPLATE;
            inventory.setItem(slots[i], RankItems.item(icon, "§f" + rango.getNombre(),
                    "§7Tipo: §e" + rango.getTipo(), "§7Color: " + rango.getColorNombre(),
                    "§7Permisos: §f" + String.join(", ", rango.getPermisos()),
                    rango.getId().equals(jugador.getRangoId()) ? "§aRango actual" : "§7Clic para continuar"));
        }
        inventory.setItem(4, RankItems.item(Material.PLAYER_HEAD, "§bJugador: §f" + jugador.getNombre(), "§7Selecciona el rango nuevo."));
        inventory.setItem(49, RankItems.item(Material.ARROW, "§e← Volver"));
        if (this.page > 0) inventory.setItem(45, RankItems.item(Material.ARROW, "§ePágina anterior"));
        if (from + slots.length < rangos.size()) inventory.setItem(53, RankItems.item(Material.ARROW, "§ePágina siguiente"));
    }
    public Rango rangoAt(int slot) {
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) {
            int index = page * slots.length + i;
            return index < rangos.size() ? rangos.get(index) : null;
        }
        return null;
    }
    public Jugador jugador() { return jugador; }
    public int page() { return page; }
    @Override public Inventory getInventory() { return inventory; }
}
