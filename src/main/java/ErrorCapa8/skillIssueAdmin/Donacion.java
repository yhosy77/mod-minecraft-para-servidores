package ErrorCapa8.skillIssueAdmin;

import java.time.LocalDateTime;
import java.util.Objects;

public class Donacion {

    private final String idDonacion;
    private final String nombreJugador;
    private final double monto;
    private final String mensaje;
    private LocalDateTime fecha;
    private boolean confirmada;

    public Donacion(String idDonacion, String nombreJugador, double monto, String mensaje) {
        if (idDonacion == null || idDonacion.isBlank()) {
            throw new IllegalArgumentException("El ID de la donación no puede estar vacío.");
        }
        if (nombreJugador == null || nombreJugador.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío.");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de la donación debe ser mayor que 0.");
        }

        this.idDonacion = idDonacion.trim();
        this.nombreJugador = nombreJugador.trim();
        this.monto = monto;
        this.mensaje = (mensaje == null) ? "" : mensaje.trim();
        this.fecha = LocalDateTime.now();
        this.confirmada = false;
    }

    /** Constructor para una donación cargada desde donaciones.txt (conserva su fecha y estado). */
    public Donacion(String idDonacion, String nombreJugador, double monto, String mensaje, LocalDateTime fecha, boolean confirmada) {
        this(idDonacion, nombreJugador, monto, mensaje);
        if (fecha != null) this.fecha = fecha;
        this.confirmada = confirmada;
    }

    public void confirmarDonacion() {
        this.confirmada = true;
    }

    /** Línea para donaciones.txt: id;jugador;monto;mensaje;fecha;confirmada */
    public String toLinea() {
        return idDonacion + ";" + nombreJugador.replace(";", ",") + ";" + monto + ";"
                + (mensaje.isBlank() ? "-" : mensaje.replace(";", ",").replace("\n", " ")) + ";" + getFecha() + ";" + confirmada;
    }

    /** @throws IllegalArgumentException si la línea está dañada. */
    public static Donacion desdeLinea(String linea) {
        String[] p = linea.split(";", -1);
        if (p.length < 6) throw new IllegalArgumentException("línea incompleta");
        return new Donacion(p[0], p[1], Double.parseDouble(p[2].trim()), p[3].equals("-") ? "" : p[3],
                LocalDateTime.parse(p[4].trim()), Boolean.parseBoolean(p[5].trim()));
    }

    public String getIdDonacion() {
        return idDonacion;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public double getMonto() {
        return monto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public boolean isConfirmada() {
        return confirmada;
    }

    @Override
    public String toString() {
        return "Donacion{" +
                "idDonacion='" + idDonacion + '\'' +
                ", nombreJugador='" + nombreJugador + '\'' +
                ", monto=" + monto +
                ", confirmada=" + confirmada +
                ", fecha=" + fecha +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Donacion donacion)) return false;
        return Objects.equals(idDonacion, donacion.idDonacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDonacion);
    }
}
