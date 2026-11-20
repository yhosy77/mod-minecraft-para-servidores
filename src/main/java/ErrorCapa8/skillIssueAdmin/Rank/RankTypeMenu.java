package ErrorCapa8.skillIssueAdmin.Rank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankTypeMenu implements InventoryHolder {
    private static final int[] SLOTS = {10,12,14,16};
    private static final TipoRango[] TYPES = {TipoRango.ADMIN, TipoRango.MOD, TipoRango.PLAYER, TipoRango.EXCLUSIVO};
    private final Inventory inventory;
    public RankTypeMenu() {
        inventory = Bukkit.createInventory(this, 27, Component.text("§8Tipo del nuevo rango"));
        RankItems.frame(inventory);
        inventory.setItem(10, RankItems.item(Material.NETHERITE_CHESTPLATE, "§cADMIN", "§7Acceso administrativo completo"));
        inventory.setItem(12, RankItems.item(Material.IRON_CHESTPLATE, "§eMOD", "§7Permisos de moderación del plugin"));
        inventory.setItem(14, RankItems.item(Material.LEATHER_CHESTPLATE, "§aPLAYER", "§7Permisos normales de jugador"));
        inventory.setItem(16, RankItems.item(Material.AMETHYST_SHARD, "§dEXCLUSIVO", "§7Rango de pago: solo cosmético", "§7Nunca da permisos de staff"));
        inventory.setItem(22, RankItems.item(Material.ARROW, "§e← Volver"));
    }
    static TipoRango tipoAt(int slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == slot) return TYPES[i];
        return null;
    }
    @Override public Inventory getInventory() { return inventory; }
}
