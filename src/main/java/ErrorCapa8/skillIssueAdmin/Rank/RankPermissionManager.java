package ErrorCapa8.skillIssueAdmin.Rank;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

/** Applies plugin-owned role permissions without changing Bukkit operator status. */
public final class RankPermissionManager {
    private final JavaPlugin plugin;
    private final GestorJugadores gestor;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    private final Set<String> managedPermissions = new HashSet<>();

    public RankPermissionManager(JavaPlugin plugin, GestorJugadores gestor) {
        this.plugin = plugin; this.gestor = gestor;
        for (TipoRango type : TipoRango.values()) managedPermissions.addAll(type.permisos());
    }

    public void apply(Player player) {
        if (player == null || !player.isOnline()) return;
        Rango rango = gestor.obtenerRangoJugador(player.getUniqueId());
        if (rango == null) return;
        PermissionAttachment attachment = attachments.computeIfAbsent(player.getUniqueId(), id -> player.addAttachment(plugin));
        for (String permission : managedPermissions) attachment.setPermission(permission, rango.tienePermiso(permission));
        player.recalculatePermissions();
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) apply(player);
    }

    public void clear(Player player) {
        if (player == null) return;
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
            player.recalculatePermissions();
        }
    }

    public void clearAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) clear(player);
    }
}
