package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class RankItems {
    private RankItems() {}
    static ItemStack item(Material material, String name, String... lore) {
        return item(material, Component.text(name), lore);
    }
    static ItemStack item(Material material, Component name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(Arrays.stream(lore).map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }
    static void frame(Inventory inventory) {
        ItemStack pane = item(Material.GRAY_STAINED_GLASS_PANE, "§r");
        int rows = inventory.getSize() / 9;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int row = slot / 9, column = slot % 9;
            if (row == 0 || row == rows - 1 || column == 0 || column == 8) inventory.setItem(slot, pane);
        }
    }
    static int[] contentSlots() { return new int[]{10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43}; }
}
