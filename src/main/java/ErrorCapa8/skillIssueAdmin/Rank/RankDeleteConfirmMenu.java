package ErrorCapa8.skillIssueAdmin.Rank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankDeleteConfirmMenu implements InventoryHolder {
    private final Inventory inventory;
    private final String rangoId;
    private final String rangoNombre;
    private final long habilitadoEn;
    public RankDeleteConfirmMenu(String rangoId, String rangoNombre, long habilitadoEn, int remainingSeconds) {
        this.rangoId = rangoId; this.rangoNombre = rangoNombre; this.habilitadoEn = habilitadoEn;
        inventory = Bukkit.createInventory(this, 27, Component.text("§8Confirmar eliminación"));
        RankItems.frame(inventory);
        inventory.setItem(13, RankItems.item(Material.TNT, "§cEliminar §f" + rangoNombre,
                "§7Esta acción reasignará a los jugadores afectados.", "§7Confirma cuando termine la cuenta regresiva."));
        update(remainingSeconds);
    }
    public void update(int remainingSeconds) {
        if (remainingSeconds > 0) {
            inventory.setItem(11, RankItems.item(Material.CLOCK, "§eEspera §f" + remainingSeconds + " §esegundos", "§7La confirmación sigue bloqueada."));
            inventory.setItem(15, RankItems.item(Material.BARRIER, "§cCancelar"));
        } else {
            inventory.setItem(11, RankItems.item(Material.LIME_CONCRETE, "§aConfirmar eliminación", "§7Clic para eliminar el rango."));
            inventory.setItem(15, RankItems.item(Material.BARRIER, "§cCancelar", "§7No se aplicarán cambios."));
        }
    }
    public String rangoId() { return rangoId; }
    public String rangoNombre() { return rangoNombre; }
    public long habilitadoEn() { return habilitadoEn; }
    @Override public Inventory getInventory() { return inventory; }
}
