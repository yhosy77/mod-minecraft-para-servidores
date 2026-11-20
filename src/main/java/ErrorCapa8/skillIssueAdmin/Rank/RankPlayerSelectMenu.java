package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class RankPlayerSelectMenu implements InventoryHolder {
    private final Inventory inventory;
    private final List<Jugador> jugadores;
    private final int page;
    public RankPlayerSelectMenu(GestorJugadores gestor, int page) {
        this.page = Math.max(0, page);
        jugadores = gestor.obtenerJugadores();
        inventory = Bukkit.createInventory(this, 54, Component.text("§8Seleccionar jugador • " + (this.page + 1)));
        RankItems.frame(inventory);
        int[] slots = RankItems.contentSlots();
        int from = this.page * slots.length;
        for (int i = 0; i < slots.length && from + i < jugadores.size(); i++) {
            Jugador jugador = jugadores.get(from + i);
            Rango rango = gestor.buscarRango(jugador.getRangoId());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(jugador.getUuid()));
            meta.displayName(Component.text("§f" + jugador.getNombre()));
            meta.lore(List.of(Component.text("§7Rango actual: §e" + (rango == null ? "Desconocido" : rango.getNombre())),
                    Component.text("§8UUID: " + jugador.getUuid()), Component.text("§7Clic para seleccionar")));
            head.setItemMeta(meta);
            inventory.setItem(slots[i], head);
        }
        if (jugadores.isEmpty()) inventory.setItem(22, RankItems.item(Material.BARRIER, "§cNo hay jugadores registrados"));
        inventory.setItem(4, RankItems.item(Material.PLAYER_HEAD, "§bElige un jugador"));
        inventory.setItem(49, RankItems.item(Material.ARROW, "§e← Volver"));
        if (this.page > 0) inventory.setItem(45, RankItems.item(Material.ARROW, "§ePágina anterior"));
        if (from + slots.length < jugadores.size()) inventory.setItem(53, RankItems.item(Material.ARROW, "§ePágina siguiente"));
    }
    public Jugador jugadorAt(int slot) {
        int[] slots = RankItems.contentSlots();
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) {
            int index = page * slots.length + i;
            return index < jugadores.size() ? jugadores.get(index) : null;
        }
        return null;
    }
    public int page() { return page; }
    @Override public Inventory getInventory() { return inventory; }
}
