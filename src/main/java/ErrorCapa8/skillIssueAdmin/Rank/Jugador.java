package ErrorCapa8.skillIssueAdmin.Rank;

import java.time.Instant;
import java.util.UUID;

public final class Jugador {
    /** Requisitos para subir gratis al rango Miembro (RF-11): nivel 10 o 500 monedas. */
    public static final int NIVEL_MIN_MIEMBRO = 10;
    public static final int MONEDAS_MIN_MIEMBRO = 500;

    private final UUID uuid;
    private String nombre;
    private String rangoId;
    private int nivel;
    private long monedas;
    private long horasJugadas;
    private Instant fechaIngreso;

    public Jugador(UUID uuid, String nombre, String rangoId) {
        this(uuid, nombre, rangoId, 1, 0, 0, Instant.now());
    }

    public Jugador(UUID uuid, String nombre, String rangoId, int nivel, long monedas, long horasJugadas, Instant fechaIngreso) {
        if (uuid == null || nombre == null || nombre.isBlank() || rangoId == null || rangoId.isBlank()) {
            throw new IllegalArgumentException("Los datos de identidad del jugador son obligatorios.");
        }
        if (nivel < 0 || monedas < 0 || horasJugadas < 0) {
            throw new IllegalArgumentException("Nivel, monedas y horas no pueden ser negativos.");
        }
        this.uuid = uuid;
        this.nombre = nombre.trim();
        this.rangoId = rangoId.trim();
        this.nivel = nivel;
        this.monedas = monedas;
        this.horasJugadas = horasJugadas;
        this.fechaIngreso = fechaIngreso == null ? Instant.now() : fechaIngreso;
    }

    public UUID getUuid() { return uuid; }
    public String getNombre() { return nombre; }
    public String getRangoId() { return rangoId; }
    public synchronized int getNivel() { return nivel; }
    public synchronized long getMonedas() { return monedas; }
    public synchronized long getHorasJugadas() { return horasJugadas; }
    public Instant getFechaIngreso() { return fechaIngreso; }

    public synchronized void registrarHoras(int horas) {
        if (horas < 0) {
            throw new IllegalArgumentException("Las horas registradas no pueden ser negativas.");
        }
        this.horasJugadas += horas;
    }

    public synchronized void actualizarProgreso(int nivel, long monedas, long horasJugadas) {
        if (nivel < 0 || monedas < 0 || horasJugadas < 0) {
            throw new IllegalArgumentException("Nivel, monedas y horas no pueden ser negativos.");
        }
        this.nivel = nivel;
        this.monedas = monedas;
        this.horasJugadas = horasJugadas;
    }

    public void actualizar(String nombre, String rangoId) {
        this.nombre = nombre;
        this.rangoId = rangoId;
    }

    public void asignarRango(String rangoId) {
        this.rangoId = rangoId;
    }

    /**
     * Sube al rango gratuito (RF-11). Con nivel 10 o más es gratis; si no, se pagan 500 monedas.
     * No sirve para rangos exclusivos de pago ni para rangos de staff.
     * @return true si subió; false si no cumple los requisitos o el rango no es válido.
     */
    public synchronized boolean subirRangoGratuito(Rango rangoMiembro) {
        if (rangoMiembro == null || rangoMiembro.esPago() || rangoMiembro.getTipo() != TipoRango.PLAYER) return false;
        if (nivel >= NIVEL_MIN_MIEMBRO) {
            rangoId = rangoMiembro.getId();
            return true;
        }
        if (monedas >= MONEDAS_MIN_MIEMBRO) {
            monedas -= MONEDAS_MIN_MIEMBRO;
            rangoId = rangoMiembro.getId();
            return true;
        }
        return false;
    }

    /** Mensaje con lo que le falta para subir a Miembro. */
    public synchronized String requisitosFaltantes() {
        if (nivel >= NIVEL_MIN_MIEMBRO || monedas >= MONEDAS_MIN_MIEMBRO) return "Ya cumples los requisitos para ser Miembro.";
        return "Te faltan " + (NIVEL_MIN_MIEMBRO - nivel) + " nivel(es) para llegar a nivel " + NIVEL_MIN_MIEMBRO
                + ", o " + (MONEDAS_MIN_MIEMBRO - monedas) + " moneda(s) para tener " + MONEDAS_MIN_MIEMBRO + ".";
    }

    @Override
    public synchronized String toString() {
        String nombreSeguro = nombre.replace(";", ",");
        return uuid + ";" + nombreSeguro + ";" + rangoId + ";" + nivel + ";" + monedas + ";" + horasJugadas + ";" + fechaIngreso.toEpochMilli();
    }

    public static Jugador desdeLinea(String linea) {
        if (linea == null || linea.isBlank()) return null;
        String[] partes = linea.split(";", -1);
        if (partes.length < 7) return null;
        try {
            return new Jugador(
                    UUID.fromString(partes[0].trim()),
                    partes[1].trim(),
                    partes[2].trim(),
                    Integer.parseInt(partes[3].trim()),
                    Long.parseLong(partes[4].trim()),
                    Long.parseLong(partes[5].trim()),
                    Instant.ofEpochMilli(Long.parseLong(partes[6].trim()))
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
