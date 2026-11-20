package ErrorCapa8.skillIssueAdmin.Rank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankMainMenu implements InventoryHolder {
    private final Inventory inventory;
    public RankMainMenu(GestorJugadores gestor) {
        inventory = Bukkit.createInventory(this, 36, Component.text("§8Gestión de rangos"));
        RankItems.frame(inventory);
        Rango defaultRank = gestor.obtenerRangoPredeterminado();
        inventory.setItem(10, RankItems.item(Material.NETHER_STAR, "§eRango predeterminado",
                "§7Actual: §f" + defaultRank.getNombre(), "§7Seleccionar un rango tipo PLAYER"));
        inventory.setItem(12, RankItems.item(Material.PLAYER_HEAD, "§bAsignar rango a jugador", "§7Selecciona jugador, rango y confirma el cambio."));
        inventory.setItem(14, RankItems.item(Material.ANVIL, "§aCrear rango", "§7Crear un rango ADMIN, OP o PLAYER"));
        inventory.setItem(16, RankItems.item(Material.BARRIER, "§cEliminar rango", "§7Eliminar rangos personalizados"));
        inventory.setItem(22, RankItems.item(Material.PAINTING, "§dPersonalizar color", "§7Elige un rango y cambia el color de su prefijo."));
    }
    @Override public Inventory getInventory() { return inventory; }
}
