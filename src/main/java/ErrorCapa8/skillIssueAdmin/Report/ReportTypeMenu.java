package ErrorCapa8.skillIssueAdmin.Report;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class ReportTypeMenu implements InventoryHolder {
    private static final int[] SLOTS = {11,12,13,14,15,22};
    private static final Material[] ICONS = {Material.PAPER,Material.POISONOUS_POTATO,Material.IRON_SWORD,Material.EMERALD,Material.LEATHER_CHESTPLATE,Material.WRITABLE_BOOK};
    private final Inventory inventory;
    private final java.util.UUID targetId;
    private final String targetName;
    public ReportTypeMenu(java.util.UUID targetId, String targetName) {
        this.targetId = targetId; this.targetName = targetName;
        inventory = Bukkit.createInventory(this, 36, Component.text("§8Motivo • " + targetName));
        MenuItems.frame(inventory);
        inventory.setItem(4, MenuItems.item(Material.PAPER, "§bReporte contra §f" + targetName, "§7Elige el motivo para enviar el reporte."));
        ReportType[] types = ReportType.values();
        for (int i = 0; i < types.length; i++) inventory.setItem(SLOTS[i], MenuItems.item(ICONS[i], "§e" + types[i].label(), "§7Enviar reporte por este motivo"));
        inventory.setItem(27, MenuItems.item(Material.ARROW, "§e← Volver a jugadores"));
        inventory.setItem(31, MenuItems.item(Material.BARRIER, "§cCancelar"));
    }
    static ReportType typeAt(int slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == slot) return ReportType.values()[i];
        return null;
    }
    public java.util.UUID targetId() { return targetId; }
    public String targetName() { return targetName; }
    @Override public Inventory getInventory() { return inventory; }
}
