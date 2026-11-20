package ErrorCapa8.skillIssueAdmin.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class ReportMenu implements InventoryHolder {
    static final int PAGE_SIZE = 28;
    private static final int[] SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private final Inventory inventory;
    private final int page;
    private final UUID viewer;

    /** @param viewer quien abre el menú; no aparece en la lista porque no puede reportarse a sí mismo (RF-01). */
    public ReportMenu(int page, UUID viewer) {
        this.page = Math.max(0, page);
        this.viewer = viewer;
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Reportar jugador • " + (this.page + 1)));
        MenuItems.frame(inventory);
        List<Player> players = onlinePlayers(viewer);
        int from = this.page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && from + i < players.size(); i++) {
            Player target = players.get(from + i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("§e" + target.getName()));
            meta.lore(List.of(Component.text("§7Clic para elegir motivo")));
            head.setItemMeta(meta);
            inventory.setItem(SLOTS[i], head);
        }
        if (players.isEmpty()) inventory.setItem(22, MenuItems.item(Material.BARRIER, "§cNo hay otros jugadores conectados"));
        inventory.setItem(4, MenuItems.item(Material.PAPER, "§bSelecciona a quién reportar", "§7No puedes reportarte a ti mismo."));
        if (this.page > 0) inventory.setItem(45, MenuItems.item(Material.ARROW, "§e← Página anterior"));
        inventory.setItem(49, MenuItems.item(Material.BARRIER, "§cCancelar", "§7Cerrar el menú"));
        if (from + PAGE_SIZE < players.size()) inventory.setItem(53, MenuItems.item(Material.ARROW, "§ePágina siguiente →"));
    }

    /** Jugadores conectados que se pueden reportar (todos menos quien reporta). */
    static List<Player> onlinePlayers(UUID viewer) {
        return new ArrayList<>(Bukkit.getOnlinePlayers()).stream().map(p -> (Player) p)
                .filter(p -> !p.getUniqueId().equals(viewer))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
    }
    static int playerAt(int slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == slot) return i;
        return -1;
    }
    public int page() { return page; }
    public UUID viewer() { return viewer; }
    @Override public Inventory getInventory() { return inventory; }
}
