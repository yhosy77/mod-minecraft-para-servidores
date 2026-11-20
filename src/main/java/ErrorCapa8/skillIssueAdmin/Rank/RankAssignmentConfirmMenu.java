package ErrorCapa8.skillIssueAdmin.Rank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankAssignmentConfirmMenu implements InventoryHolder {
    private final Inventory inventory;
    private final java.util.UUID jugadorId;
    private final String jugadorNombre;
    private final String rangoId;
    private final String rangoNombre;
    public RankAssignmentConfirmMenu(Jugador jugador, Rango rango, Rango actual) {
        jugadorId = jugador.getUuid(); jugadorNombre = jugador.getNombre();
        rangoId = rango.getId(); rangoNombre = rango.getNombre();
        inventory = Bukkit.createInventory(this, 27, Component.text("§8Confirmar asignación"));
        RankItems.frame(inventory);
        inventory.setItem(13, RankItems.item(Material.WRITABLE_BOOK, "§bConfirmar cambio de rango",
                "§7Jugador: §f" + jugadorNombre, "§7Actual: §f" + (actual == null ? "Desconocido" : actual.getNombre()),
                "§7Nuevo: §e" + rangoNombre, "§7Se guardará y actualizará su visualización."));
        inventory.setItem(11, RankItems.item(Material.LIME_CONCRETE, "§aConfirmar"));
        inventory.setItem(15, RankItems.item(Material.RED_CONCRETE, "§cCancelar"));
    }
    public java.util.UUID jugadorId() { return jugadorId; }
    public String jugadorNombre() { return jugadorNombre; }
    public String rangoId() { return rangoId; }
    public String rangoNombre() { return rangoNombre; }
    @Override public Inventory getInventory() { return inventory; }
}
