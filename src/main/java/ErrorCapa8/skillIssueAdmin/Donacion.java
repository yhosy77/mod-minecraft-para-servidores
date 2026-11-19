package ErrorCapa8.skillIssueAdmin;

import java.time.LocalDateTime;
import java.util.Objects;

public class Donacion {

    private final String idDonacion;
    private final String nombreJugador;
    private final double monto;
    private final String mensaje;
    private final LocalDateTime fecha;
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

    public void confirmarDonacion() {
        this.confirmada = true;
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
