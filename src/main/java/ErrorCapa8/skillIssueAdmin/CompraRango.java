package ErrorCapa8.skillIssueAdmin;

import java.time.LocalDateTime;
import java.util.Objects;

public class CompraRango {

    private final String idCompra;
    private final String nombreJugador;
    private final String nombreRango;
    private final double precioPagado;
    private LocalDateTime fecha;
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

    /** Constructor para una compra cargada desde compras.txt (conserva su fecha y estado). */
    public CompraRango(String idCompra, String nombreJugador, String nombreRango, double precioPagado,
                       LocalDateTime fecha, boolean completada) {
        this(idCompra, nombreJugador, nombreRango, precioPagado);
        if (fecha != null) this.fecha = fecha;
        this.completada = completada;
    }

    /**
     * Valida y marca la compra como completada. La asignación del rango al jugador
     * la hace GestorJugadores desde /comprarango (RF-12).
     * @throws IllegalStateException si la compra ya estaba completada.
     * @throws IllegalArgumentException si el monto no alcanza.
     */
    public boolean procesarCompra(double montoEntregado) {
        if (completada) {
            throw new IllegalStateException("La compra " + idCompra + " ya fue completada.");
        }
        if (montoEntregado < precioPagado) {
            throw new IllegalArgumentException(
                    "Monto insuficiente para comprar el rango " + nombreRango +
                            ". Requerido: " + precioPagado + ", recibido: " + montoEntregado
            );
        }
        this.completada = true;
        return true;
    }

    /** Línea para compras.txt: id;jugador;rango;valor;fecha;completada */
    public String toLinea() {
        return idCompra + ";" + nombreJugador.replace(";", ",") + ";" + nombreRango.replace(";", ",") + ";"
                + precioPagado + ";" + fecha + ";" + completada;
    }

    /** @throws IllegalArgumentException si la línea está dañada. */
    public static CompraRango desdeLinea(String linea) {
        String[] p = linea.split(";", -1);
        if (p.length < 6) throw new IllegalArgumentException("línea incompleta");
        return new CompraRango(p[0], p[1], p[2], Double.parseDouble(p[3].trim()),
                LocalDateTime.parse(p[4].trim()), Boolean.parseBoolean(p[5].trim()));
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
