package ErrorCapa8.skillIssueAdmin;

import java.time.LocalDateTime;
import java.util.Objects;

public class CompraRango {

    private final String idCompra;
    private final String nombreJugador;
    private final String nombreRango;
    private final double precioPagado;
    private final LocalDateTime fecha;
    private boolean completada;

    public CompraRango(String idCompra, String nombreJugador, String nombreRango, double precioPagado) {
        if (idCompra == null || idCompra.isBlank()) {
            throw new IllegalArgumentException("El ID de la compra no puede estar vacío.");
        }
        if (nombreJugador == null || nombreJugador.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío.");
        }
        if (nombreRango == null || nombreRango.isBlank()) {
            throw new IllegalArgumentException("El nombre del rango no puede estar vacío.");
        }
        if (precioPagado < 0) {
            throw new IllegalArgumentException("El precio pagado no puede ser negativo.");
        }

        this.idCompra = idCompra.trim();
        this.nombreJugador = nombreJugador.trim();
        this.nombreRango = nombreRango.trim();
        this.precioPagado = precioPagado;
        this.fecha = LocalDateTime.now();
        this.completada = false;
    }

    /**
     * Valida y marca la compra como completada.
     * Cuando integres con Samuel Zona (Jugador / Rango), aquí se debe asignar el rango al Jugador.
     */
    public boolean procesarCompra(double montoEntregado) throws SancionInvalidaException {
        if (completada) {
            throw new SancionInvalidaException("La compra " + idCompra + " ya fue completada.");
        }
        if (montoEntregado < precioPagado) {
            throw new SancionInvalidaException(
                    "Monto insuficiente para comprar el rango " + nombreRango +
                            ". Requerido: " + precioPagado + ", recibido: " + montoEntregado
            );
        }

        // TODO: Conectar con Jugador.asignarRango(rango) cuando esté disponible la clase Rango.
        this.completada = true;
        return true;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getNombreRango() {
        return nombreRango;
    }

    public double getPrecioPagado() {
        return precioPagado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isCompletada() {
        return completada;
    }

    @Override
    public String toString() {
        return "CompraRango{" +
                "idCompra='" + idCompra + '\'' +
                ", nombreJugador='" + nombreJugador + '\'' +
                ", nombreRango='" + nombreRango + '\'' +
                ", precioPagado=" + precioPagado +
                ", completada=" + completada +
                ", fecha=" + fecha +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompraRango that)) return false;
        return Objects.equals(idCompra, that.idCompra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCompra);
    }
}
