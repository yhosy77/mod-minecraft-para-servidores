package ErrorCapa8.skillIssueAdmin.Report;

import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class MenuItems {
    private MenuItems() {}
    static ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(Arrays.stream(lore).map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }
    static void frame(Inventory inventory) {
        ItemStack pane = item(Material.GRAY_STAINED_GLASS_PANE, "§r");
        int rows = inventory.getSize() / 9;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9, col = slot % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) inventory.setItem(slot, pane);
        }
    }
}
