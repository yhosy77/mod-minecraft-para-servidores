package ErrorCapa8.skillIssueAdmin;

import org.bukkit.plugin.java.JavaPlugin;
import ErrorCapa8.skillIssueAdmin.Report.ReportAdminCommand;
import ErrorCapa8.skillIssueAdmin.Report.ReportCreationManager;
import ErrorCapa8.skillIssueAdmin.Report.ReportMenuListener;
import ErrorCapa8.skillIssueAdmin.Report.ReportPlayer;
import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.IPersistencia;
import ErrorCapa8.skillIssueAdmin.Rank.JugadorCommand;
import ErrorCapa8.skillIssueAdmin.Rank.SubirRangoCommand;
import ErrorCapa8.skillIssueAdmin.Rank.PersistenciaArchivo;
import ErrorCapa8.skillIssueAdmin.Rank.RankCommand;
import ErrorCapa8.skillIssueAdmin.Rank.ProfileCommand;
import ErrorCapa8.skillIssueAdmin.Rank.RankDisplayManager;
import ErrorCapa8.skillIssueAdmin.Rank.RankPermissionManager;
import ErrorCapa8.skillIssueAdmin.Rank.RankMenuListener;
import ErrorCapa8.skillIssueAdmin.Rank.RegistroJugadorListener;
import ErrorCapa8.skillIssueAdmin.Sanctions.BanCommand;
import ErrorCapa8.skillIssueAdmin.Sanctions.BanMenuListener;
import ErrorCapa8.skillIssueAdmin.Sanctions.GestorSanciones;
import ErrorCapa8.skillIssueAdmin.Sanctions.MuteCommand;
import ErrorCapa8.skillIssueAdmin.Sanctions.PerdonarCommand;
import ErrorCapa8.skillIssueAdmin.Sanctions.SancionesCommand;
import ErrorCapa8.skillIssueAdmin.Sanctions.WarnCommand;
import ErrorCapa8.skillIssueAdmin.listeners.ModeracionListener;
import ErrorCapa8.skillIssueAdmin.listeners.connectionListener;

public final class SkillIssueAdmin extends JavaPlugin {
    private ReportCreationManager reportManager;
    private GestorJugadores gestorJugadores;
    private RankDisplayManager rankDisplayManager;
    private RankPermissionManager rankPermissionManager;
    private GestorSanciones gestorSanciones;
    private GestorTransacciones gestorTransacciones;

    @Override
    public void onEnable() {
        IPersistencia persistenciaRangos = new PersistenciaArchivo(getDataFolder());
        reportManager = new ReportCreationManager(this, persistenciaRangos);
        getServer().getPluginManager().registerEvents(new ReportMenuListener(this, reportManager), this);
        registerCommand("report", new ReportPlayer(reportManager));
        registerCommand("gestreports", new ReportAdminCommand(reportManager));
        gestorJugadores = new GestorJugadores(this, persistenciaRangos);
        gestorSanciones = new GestorSanciones(this, persistenciaRangos, gestorJugadores);
        getServer().getPluginManager().registerEvents(new BanMenuListener(this, gestorJugadores, gestorSanciones), this);
        registerCommand("ban", new BanCommand(gestorJugadores));
        // Sanciones: mute, advertencias, historial y perdón
        getServer().getPluginManager().registerEvents(new ModeracionListener(gestorSanciones), this);
        registerCommand("mute", new MuteCommand(gestorJugadores, gestorSanciones));
        registerCommand("warn", new WarnCommand(gestorJugadores, gestorSanciones));
        registerCommand("sanciones", new SancionesCommand(gestorJugadores, gestorSanciones));
        registerCommand("perdonar", new PerdonarCommand(gestorJugadores, gestorSanciones));
        rankDisplayManager = new RankDisplayManager(this, gestorJugadores);
        rankPermissionManager = new RankPermissionManager(this, gestorJugadores);
        getServer().getPluginManager().registerEvents(rankDisplayManager, this);
        getServer().getPluginManager().registerEvents(new RankMenuListener(this, gestorJugadores, rankDisplayManager, rankPermissionManager), this);
        getServer().getPluginManager().registerEvents(new RegistroJugadorListener(gestorJugadores, rankDisplayManager, rankPermissionManager), this);
        registerCommand("rank", new RankCommand(gestorJugadores));
        registerCommand("profile", new ProfileCommand(gestorJugadores, gestorSanciones));
        registerCommand("jugador", new JugadorCommand(gestorJugadores));
        registerCommand("subirrango", new SubirRangoCommand(gestorJugadores, rankDisplayManager, rankPermissionManager));
        // Donaciones y compras de rangos exclusivos
        gestorTransacciones = new GestorTransacciones(this, persistenciaRangos);
        registerCommand("donar", new DonarCommand(gestorTransacciones));
        registerCommand("donaciones", new DonacionesCommand(gestorJugadores, gestorTransacciones));
        registerCommand("comprarango", new ComprarRangoCommand(gestorJugadores, gestorTransacciones, rankDisplayManager, rankPermissionManager));
        registerCommand("estadisticas", new EstadisticasCommand(gestorJugadores, gestorSanciones, reportManager));
        getServer().getOnlinePlayers().forEach(player -> {
            gestorJugadores.registrarOActualizarJugador(player);
            rankPermissionManager.apply(player);
            rankDisplayManager.refreshPlayer(player);
        });
        getServer().getPluginManager().registerEvents(new connectionListener(), this);
        getLogger().info("Skill Issue Admin habilitado.");
    }

    @Override
    public void onDisable() {
        if (rankDisplayManager != null) rankDisplayManager.shutdown();
        if (rankPermissionManager != null) rankPermissionManager.clearAll();
        if (gestorSanciones != null) gestorSanciones.guardar();
        getLogger().info("Skill Issue Admin deshabilitado.");
    }
}
