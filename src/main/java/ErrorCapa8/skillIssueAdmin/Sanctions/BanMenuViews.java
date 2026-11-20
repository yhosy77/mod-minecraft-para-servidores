package ErrorCapa8.skillIssueAdmin.Sanctions;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/** Inventory holders and builders for the guided ban workflow. */
public final class BanMenuViews {
    static final int PAGE_SIZE = 28;
    private static final int[] PLAYER_SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private BanMenuViews() { }

    public static PlayerSelect playerSelect(int page, Player viewer) { return new PlayerSelect(page, viewer); }
    public static ReasonSelect reasonSelect(BanDraft draft) { return new ReasonSelect(draft); }
    public static DurationSelect durationSelect(BanDraft draft) { return new DurationSelect(draft); }
    public static MessageSelect messageSelect(BanDraft draft) { return new MessageSelect(draft); }
    public static Confirm confirm(BanDraft draft) { return new Confirm(draft); }

    static ItemStack item(Material material, String title, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(title));
        if (lore.length > 0) meta.lore(java.util.Arrays.stream(lore).map(Component::text).toList());
        item.setItemMeta(meta);
        return item;
    }
    static void frame(Inventory inv) {
        ItemStack glass = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, glass);
    }
    static void navigation(Inventory inv, boolean back) {
        inv.setItem(49, item(Material.BARRIER, "§cCancelar", "§7Cierra el proceso sin aplicar el baneo."));
        if (back) inv.setItem(45, item(Material.ARROW, "§eVolver"));
    }
    static List<Player> eligiblePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(player -> (Player) player)
                .sorted((a,b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
    }
    static int playerIndexAt(int slot) { for (int i=0;i<PLAYER_SLOTS.length;i++) if (PLAYER_SLOTS[i] == slot) return i; return -1; }

    public static final class PlayerSelect implements InventoryHolder {
        final Inventory inventory; final int page; final List<Player> targets;
        PlayerSelect(int page, Player viewer) {
            this.page = Math.max(0, page);
            inventory = Bukkit.createInventory(this, 54, Component.text("§8Banear jugador • " + (this.page+1)));
            frame(inventory);
            inventory.setItem(4, item(Material.SHIELD, "§6Selecciona un jugador", "§7Elige un jugador conectado para continuar."));
            targets = eligiblePlayers().stream().filter(target -> !target.getUniqueId().equals(viewer.getUniqueId())).toList();
            int from = this.page * PAGE_SIZE;
            for (int i=0;i<PAGE_SIZE && from+i<targets.size();i++) {
                Player target = targets.get(from+i);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta(); meta.setOwningPlayer(target);
                meta.displayName(Component.text("§e" + target.getName()));
                meta.lore(List.of(Component.text("§7Clic para seleccionar"), Component.text("§8No puedes seleccionarte a ti mismo.")));
                head.setItemMeta(meta); inventory.setItem(PLAYER_SLOTS[i], head);
            }
            if (targets.isEmpty()) inventory.setItem(22, item(Material.BARRIER,"§cNo hay jugadores conectados"));
            navigation(inventory,false);
            if (this.page>0) inventory.setItem(45,item(Material.ARROW,"§e← Página anterior"));
            if (from+PAGE_SIZE<targets.size()) inventory.setItem(53,item(Material.ARROW,"§ePágina siguiente →"));
        }
        @Override public Inventory getInventory(){ return inventory; }
    }
    public static final class ReasonSelect implements InventoryHolder {
        final Inventory inventory; final BanDraft draft;
        ReasonSelect(BanDraft draft) {
            this.draft=draft; inventory=Bukkit.createInventory(this,45,Component.text("§8Banear • Motivo")); frame(inventory);
            inventory.setItem(4,item(Material.PAPER,"§6Motivo del baneo","§7Jugador: §f"+draft.getJugadorNombre()));
            int[] slots={19,20,21,22,23,24}; int i=0;
            for(CausaBaneo cause:CausaBaneo.values()) inventory.setItem(slots[i++],item(cause.icono(),"§e"+cause.etiqueta(),"§7"+cause.descripcion(),"§aClic para elegir"));
            inventory.setItem(40,item(Material.BARRIER,"§cCancelar")); inventory.setItem(36,item(Material.ARROW,"§eVolver"));
        }
        @Override public Inventory getInventory(){return inventory;}
    }
    public static final class DurationSelect implements InventoryHolder {
        final Inventory inventory; final BanDraft draft;
        DurationSelect(BanDraft draft) {
            this.draft=draft; inventory=Bukkit.createInventory(this,45,Component.text("§8Banear • Duración")); frame(inventory);
            inventory.setItem(4,item(Material.CLOCK,"§6Duración del baneo","§7Motivo: §f"+draft.getCausa().etiqueta()));
            int[] slots={19,20,21,22,23,24}; int i=0;
            for(DuracionBaneo duration:DuracionBaneo.values()) inventory.setItem(slots[i++],item(duration.icono(),"§e"+duration.etiqueta(),"§7Clic para elegir"));
            inventory.setItem(40,item(Material.BARRIER,"§cCancelar")); inventory.setItem(36,item(Material.ARROW,"§eVolver"));
        }
        @Override public Inventory getInventory(){return inventory;}
    }
    public static final class MessageSelect implements InventoryHolder {
        final Inventory inventory; final BanDraft draft;
        MessageSelect(BanDraft draft) {
            this.draft=draft; inventory=Bukkit.createInventory(this,45,Component.text("§8Banear • Mensaje")); frame(inventory);
            inventory.setItem(4,item(Material.WRITABLE_BOOK,"§6Mensaje adicional (opcional)","§7Añade una nota breve o continúa sin ella."));
            inventory.setItem(20,item(Material.ANVIL,"§eEscribir mensaje","§7Se solicitará por el chat."));
            inventory.setItem(24,item(Material.LIME_CONCRETE,"§aContinuar sin mensaje"));
            inventory.setItem(40,item(Material.BARRIER,"§cCancelar")); inventory.setItem(36,item(Material.ARROW,"§eVolver"));
        }
        @Override public Inventory getInventory(){return inventory;}
    }
    public static final class Confirm implements InventoryHolder {
        final Inventory inventory; final BanDraft draft;
        Confirm(BanDraft draft) {
            this.draft=draft; inventory=Bukkit.createInventory(this,45,Component.text("§8Confirmar baneo")); frame(inventory);
            String cause=draft.getCausa()==CausaBaneo.OTRA?draft.getDetalleCausa():draft.getCausa().etiqueta();
            inventory.setItem(13,item(Material.PLAYER_HEAD,"§eJugador: §f"+draft.getJugadorNombre()));
            inventory.setItem(20,item(Material.PAPER,"§6Motivo: §f"+cause));
            inventory.setItem(22,item(Material.CLOCK,"§6Duración: §f"+draft.getDuracion().etiqueta()));
            inventory.setItem(24,item(Material.WRITABLE_BOOK,"§6Mensaje: §f"+(draft.getMensaje().isBlank()?"Sin mensaje":draft.getMensaje())));
            inventory.setItem(29,item(Material.BARRIER,"§cCancelar"));
            inventory.setItem(33,item(Material.LIME_CONCRETE,"§aConfirmar y aplicar","§7Se guardará y expulsará al jugador."));
        }
        @Override public Inventory getInventory(){return inventory;}
    }
}
