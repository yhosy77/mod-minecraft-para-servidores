package ErrorCapa8.skillIssueAdmin.Sanctions;

import ErrorCapa8.skillIssueAdmin.Rank.GestorJugadores;
import ErrorCapa8.skillIssueAdmin.Rank.IPersistencia;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GestorSanciones {
    private final JavaPlugin plugin;
    private final IPersistencia persistencia;
    private final GestorJugadores gestorJugadores;
    private final List<Baneo> baneos = new ArrayList<>();

    public GestorSanciones(JavaPlugin plugin, IPersistencia persistencia, GestorJugadores gestorJugadores) {
        this.plugin = plugin; this.persistencia = persistencia; this.gestorJugadores = gestorJugadores;
        try { baneos.addAll(persistencia.cargarBaneos()); }
        catch (IOException exception) { plugin.getLogger().log(Level.SEVERE, "No se pudieron cargar los baneos persistidos.", exception); }
        plugin.getLogger().info("Se cargaron " + baneos.size() + " registros de baneo.");
    }

    public synchronized List<Baneo> obtenerBaneos() {
        return baneos.stream().sorted(Comparator.comparing(Sancion::getCreadaEn).reversed()).toList();
    }

    public synchronized Optional<Baneo> obtenerBaneoActivo(UUID jugadorId, Instant ahora) {
        return baneos.stream().filter(b -> b.getJugadorId().equals(jugadorId) && b.estaActivo(ahora))
                .max(Comparator.comparing(Sancion::getCreadaEn));
    }

    public synchronized Baneo aplicarBaneo(BanDraft draft, Player administrador) throws IOException {
        if (administrador == null || !gestorJugadores.esAdministrador(administrador.getUniqueId()))
            throw new SecurityException("Solo un rango ADMIN puede aplicar baneos.");
        if (draft == null || !draft.estaCompleto()) throw new IllegalArgumentException("Faltan datos requeridos para el baneo.");
        if (draft.getJugadorId().equals(administrador.getUniqueId())) throw new IllegalArgumentException("No puedes banearte a ti mismo.");
        Player target = Bukkit.getPlayer(draft.getJugadorId());
        if (target == null || !target.isOnline()) throw new IllegalArgumentException("El jugador seleccionado ya se desconectó.");
        if (obtenerBaneoActivo(draft.getJugadorId(), Instant.now()).isPresent())
            throw new IllegalArgumentException("El jugador ya tiene un baneo activo.");

        Instant createdAt = Instant.now();
        Instant expiresAt = draft.getDuracion().calcularExpiracion(createdAt);
        Baneo baneo = new Baneo(UUID.randomUUID(), target.getUniqueId(), target.getName(), draft.getCausa(),
                draft.getDetalleCausa(), draft.getMensaje(), administrador.getUniqueId(), administrador.getName(),
                createdAt, expiresAt, draft.getDuracion() == DuracionBaneo.PERMANENTE);
        baneos.add(baneo);
        try { persistencia.guardarBaneos(baneos); }
        catch (IOException exception) {
            baneos.remove(baneo);
            throw exception;
        }
        target.kick(baneo.mensajeKick());
        return baneo;
    }
}
