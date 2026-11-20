package ErrorCapa8.skillIssueAdmin.Rank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankRgbColorMenu implements InventoryHolder {
    private static final String[] HEX = {"#FF6B6B", "#FF8C42", "#FFD166", "#A3DE83", "#06D6A0", "#2EC4B6",
            "#4CC9F0", "#4895EF", "#4361EE", "#7B2CBF", "#C77DFF", "#F15BB5"};
    private static final String[] LABELS = {"Coral", "Naranja", "Amarillo suave", "Verde claro", "Menta", "Turquesa",
            "Celeste", "Azul cielo", "Índigo", "Violeta", "Lavanda", "Rosa"};
    private static final Material[] ICONS = {Material.RED_STAINED_GLASS, Material.ORANGE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.GREEN_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
            Material.PINK_STAINED_GLASS};
    private final Inventory inventory;
    private final RankColorMenu parent;

    public RankRgbColorMenu(RankColorMenu parent) {
        this.parent = parent;
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Tonos RGB"));
        RankItems.frame(inventory);
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < HEX.length; i++) {
            TextColor color = colorAtIndex(i);
            inventory.setItem(slots[i], RankItems.item(ICONS[i], Component.text(LABELS[i] + " " + HEX[i], color),
                    "§7Color hexadecimal RGB", "§7Clic para aplicar al editor"));
        }
        inventory.setItem(45, RankItems.item(Material.ARROW, "§e← Volver al editor"));
        inventory.setItem(49, RankItems.item(Material.NAME_TAG, "§dIntroducir HEX", "§7Escribe un color como #33AACC en el chat."));
    }
    static TextColor colorAt(int slot) {
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < HEX.length; i++) if (slots[i] == slot) return colorAtIndex(i);
        return null;
    }
    private static TextColor colorAtIndex(int index) { return TextColor.fromHexString(HEX[index]); }
    public RankColorMenu parent() { return parent; }
    @Override public Inventory getInventory() { return inventory; }
}
