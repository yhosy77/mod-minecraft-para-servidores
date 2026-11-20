package ErrorCapa8.skillIssueAdmin.Rank;

import ErrorCapa8.skillIssueAdmin.ComandoAdministrativo;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/** /rank : abre la gestión gráfica de rangos (solo ADMIN). */
public final class RankCommand extends ComandoAdministrativo {
    public RankCommand(GestorJugadores gestor) {
        super("rank", "Abre la gestión administrativa de rangos", gestor);
    }

    @Override protected void ejecutarComando(CommandSourceStack source, String[] args) {
        jugador(source).openInventory(new RankMainMenu(gestorJugadores).getInventory());
    }
}
