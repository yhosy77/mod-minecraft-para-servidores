package ErrorCapa8.skillIssueAdmin.Rank;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** Applies rank prefixes without replacing any player's scoreboard. */
public final class RankDisplayManager implements Listener {
    private static final String TEAM_PREFIX = "sia_";
    private final JavaPlugin plugin;
    private final GestorJugadores gestor;
    private final Map<UUID, Map<Scoreboard, Binding>> bindings = new HashMap<>();
    private final Set<Scoreboard> touchedScoreboards = new HashSet<>();

    public RankDisplayManager(JavaPlugin plugin, GestorJugadores gestor) {
        this.plugin = plugin;
        this.gestor = gestor;
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) refreshPlayer(player);
    }

    public void refreshPlayer(Player player) {
        if (player == null || !player.isOnline()) return;
        UUID uuid = player.getUniqueId();
        Jugador jugador = gestor.buscarJugador(uuid);
        Rango rango = jugador == null ? null : gestor.buscarRango(jugador.getRangoId());
        if (rango == null) return;

        String entry = player.getName();
        Set<Scoreboard> activeBoards = new HashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();
            if (board != null) activeBoards.add(board);
        }
        if (activeBoards.isEmpty()) activeBoards.add(Bukkit.getScoreboardManager().getMainScoreboard());
        Map<Scoreboard, Binding> playerBindings = bindings.computeIfAbsent(uuid, ignored -> new HashMap<>());
        Map<Scoreboard, Binding> staleBindings = new HashMap<>(playerBindings);
        for (Scoreboard board : activeBoards) {
            staleBindings.remove(board);
            applyToBoard(player, rango, board, playerBindings);
        }
        for (Binding stale : staleBindings.values()) {
            removeOwnedEntry(stale);
            playerBindings.remove(stale.scoreboard());
        }
    }

    private void applyToBoard(Player player, Rango rango, Scoreboard scoreboard, Map<Scoreboard, Binding> playerBindings) {
        String entry = player.getName();
        String desiredTeamName = teamName(rango.getId());
        Binding previous = playerBindings.get(scoreboard);
        if (previous != null && !previous.entry().equals(entry)) removeOwnedEntry(previous);
        Team current = scoreboard.getEntryTeam(entry);
        if (current != null && !current.getName().startsWith(TEAM_PREFIX)) {
            plugin.getLogger().log(Level.FINE, "No se cambió el equipo de {0}: ya pertenece al equipo externo {1}.", new Object[]{entry, current.getName()});
            if (previous != null) removeOwnedEntry(previous);
            playerBindings.remove(scoreboard);
            return;
        }
        if (current != null && !current.getName().equals(desiredTeamName)) current.removeEntry(entry);

        Team team = scoreboard.getTeam(desiredTeamName);
        if (team == null) {
            try { team = scoreboard.registerNewTeam(desiredTeamName); }
            catch (IllegalArgumentException collision) {
                plugin.getLogger().log(Level.WARNING, "No se pudo crear el equipo de visualización para el rango " + rango.getNombre(), collision);
                return;
            }
        }
        Component prefix = Component.text("[" + rango.getNombre().toUpperCase(java.util.Locale.ROOT) + "] ", rango.getColor());
        for (TextDecoration decoration : TextDecoration.values()) prefix = prefix.decoration(decoration, rango.tieneFormato(decoration));
        team.prefix(prefix);
        team.color(NamedTextColor.WHITE);
        team.addEntry(entry);
        playerBindings.put(scoreboard, new Binding(scoreboard, entry, team.getName()));
        touchedScoreboards.add(scoreboard);
    }

    public void removePlayer(Player player) {
        Map<Scoreboard, Binding> playerBindings = bindings.remove(player.getUniqueId());
        if (playerBindings != null) playerBindings.values().forEach(this::removeOwnedEntry);
    }

    public void shutdown() {
        for (Map<Scoreboard, Binding> playerBindings : bindings.values()) playerBindings.values().forEach(this::removeOwnedEntry);
        bindings.clear();
        for (Scoreboard scoreboard : touchedScoreboards) {
            for (Team team : scoreboard.getTeams()) {
                if (team.getName().startsWith(TEAM_PREFIX) && team.getEntries().isEmpty()) team.unregister();
            }
        }
        touchedScoreboards.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, this::refreshAll);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { removePlayer(event.getPlayer()); }

    private void removeOwnedEntry(Binding binding) {
        Team team = binding.scoreboard().getTeam(binding.teamName());
        if (team != null && team.getName().startsWith(TEAM_PREFIX)) team.removeEntry(binding.entry());
    }

    private String teamName(String rankId) {
        String hash = UUID.nameUUIDFromBytes(rankId.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        return TEAM_PREFIX + hash.substring(0, 12);
    }

    private record Binding(Scoreboard scoreboard, String entry, String teamName) { }
}
