package ErrorCapa8.skillIssueAdmin.Rank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import ErrorCapa8.skillIssueAdmin.Sanctions.Baneo;
import ErrorCapa8.skillIssueAdmin.Sanctions.CausaBaneo;
import java.time.Instant;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PersistenciaArchivo implements IPersistencia {
    private final File carpetaDatos;
    private final File archivo;
    private final File archivoSanciones;

    public PersistenciaArchivo(File carpetaDatos) {
        this.carpetaDatos = carpetaDatos == null ? new File("datos") : carpetaDatos;
        this.archivo = new File(this.carpetaDatos, "rangos.yml");
        this.archivoSanciones = new File(this.carpetaDatos, "sanciones.yml");
    }

    public PersistenciaArchivo(String rutaBase) {
        this(new File(rutaBase == null || rutaBase.isBlank() ? "datos" : rutaBase.trim()));
    }

    private File resolverArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío.");
        }
        String soloNombre = new File(nombreArchivo.trim()).getName();
        if (soloNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo no es válido.");
        }
        return new File(carpetaDatos, soloNombre);
    }

    @Override
    public boolean verificarArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return false;
        File objetivo = resolverArchivo(nombreArchivo);
        return objetivo.exists() && objetivo.isFile();
    }

    @Override
    public boolean guardarDatos(String nombreArchivo, List<?> datos) {
        if (nombreArchivo == null || nombreArchivo.isBlank() || datos == null) return false;
        try {
            if (!carpetaDatos.exists() && !carpetaDatos.mkdirs()) {
                return false;
            }
            File objetivo = resolverArchivo(nombreArchivo);
            try (PrintWriter writer = new PrintWriter(new FileWriter(objetivo, StandardCharsets.UTF_8))) {
                for (Object elemento : datos) {
                    if (elemento != null) {
                        writer.println(elemento.toString());
                    }
                }
            }
            return true;
        } catch (IOException exception) {
            System.err.println("Error al guardar datos en " + nombreArchivo + ": " + exception.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<String> cargarDatos(String nombreArchivo) {
        ArrayList<String> lineas = new ArrayList<>();
        if (!verificarArchivo(nombreArchivo)) {
            return lineas;
        }
        File objetivo = resolverArchivo(nombreArchivo);
        try (BufferedReader reader = new BufferedReader(new FileReader(objetivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    lineas.add(linea.trim());
                }
            }
        } catch (IOException exception) {
            System.err.println("Error al cargar datos desde " + nombreArchivo + ": " + exception.getMessage());
        }
        return lineas;
    }

    // ------------------------------------------------------------------
    // Rangos y jugadores en archivos planos: rangos.txt y jugadores.txt
    // ------------------------------------------------------------------

    public static final String ARCHIVO_RANGOS = "rangos.txt";
    public static final String ARCHIVO_JUGADORES = "jugadores.txt";

    /**
     * Carga rangos y jugadores desde rangos.txt y jugadores.txt.
     * Si rangos.txt todavía no existe, lee el rangos.yml de la versión anterior (se pasa a .txt al guardar).
     * Las líneas dañadas se omiten y se informa en la consola.
     *
     * Formato de rangos.txt:
     *   PREDETERMINADO;idRango
     *   RANGO;id;nombre;tipo;base;color;formato1,formato2
     */
    @Override public PersistenciaDatos cargar() throws IOException {
        if (!verificarArchivo(ARCHIVO_RANGOS)) return cargarDesdeYaml();
        List<Rango> rangos = new ArrayList<>();
        String predeterminado = "player";
        for (String linea : cargarDatos(ARCHIVO_RANGOS)) {
            String[] p = linea.split(";", -1);
            try {
                if (p[0].equals("PREDETERMINADO") && p.length >= 2) {
                    predeterminado = p[1].trim();
                } else if (p[0].equals("RANGO") && p.length >= 7) {
                    java.util.Set<TextDecoration> formatos = new java.util.HashSet<>();
                    if (!p[6].isBlank() && !p[6].equals("-"))
                        for (String formato : p[6].split(",")) formatos.add(TextDecoration.valueOf(formato.trim()));
                    rangos.add(new Rango(p[1].trim(), p[2], TipoRango.valueOf(p[3].trim()),
                            Boolean.parseBoolean(p[4].trim()), Rango.parseColor(p[5].trim()), formatos));
                } else {
                    throw new IllegalArgumentException("formato desconocido");
                }
            } catch (IllegalArgumentException exception) {
                System.err.println("Se omitió una línea dañada de " + ARCHIVO_RANGOS + ": " + linea);
            }
        }
        List<Jugador> jugadores = new ArrayList<>();
        for (String linea : cargarDatos(ARCHIVO_JUGADORES)) {
            Jugador jugador = Jugador.desdeLinea(linea);
            if (jugador != null) jugadores.add(jugador);
            else System.err.println("Se omitió una línea dañada de " + ARCHIVO_JUGADORES + ": " + linea);
        }
        return new PersistenciaDatos(rangos, jugadores, predeterminado);
    }

    /** Guarda rangos.txt y jugadores.txt. */
    @Override public void guardar(Collection<Rango> rangos, Collection<Jugador> jugadores, String defaultId) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("PREDETERMINADO;" + defaultId);
        for (Rango rango : rangos) {
            String formatos = rango.getFormatos().stream().map(Enum::name).sorted().reduce((a, b) -> a + "," + b).orElse("-");
            lineas.add("RANGO;" + rango.getId() + ";" + rango.getNombre().replace(";", ",") + ";" + rango.getTipo().name()
                    + ";" + rango.esBase() + ";" + rango.getColorNombre() + ";" + formatos);
        }
        if (!guardarDatos(ARCHIVO_RANGOS, lineas)) throw new IOException("No se pudo escribir " + ARCHIVO_RANGOS + ".");
        if (!guardarDatos(ARCHIVO_JUGADORES, new ArrayList<>(jugadores))) throw new IOException("No se pudo escribir " + ARCHIVO_JUGADORES + ".");
    }

    /** Lee el rangos.yml de la versión anterior (solo se usa si todavía no existe rangos.txt). */
    private PersistenciaDatos cargarDesdeYaml() throws IOException {
        if (!archivo.exists()) return new PersistenciaDatos(List.of(), List.of(), "player");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivo);
        int formatVersion = yaml.getInt("version", 0);
        List<Rango> rangos = new ArrayList<>();
        ConfigurationSection rankSection = yaml.getConfigurationSection("rangos");
        if (rankSection != null) for (String id : rankSection.getKeys(false)) {
            String path = "rangos." + id + ".";
            String nombre = yaml.getString(path + "nombre");
            String tipoText = yaml.getString(path + "tipo", id.equals("op") || (id.equals("mod") && formatVersion < 2) ? "OP" : "PLAYER");
            if (formatVersion < 2 && tipoText.equalsIgnoreCase("MOD")) tipoText = "OP";
            TipoRango tipo = TipoRango.valueOf(tipoText.toUpperCase(java.util.Locale.ROOT));
            boolean base = yaml.getBoolean(path + "base", false);
            String colorName = yaml.getString(path + "color", NamedTextColor.NAMES.keyOr(Rango.colorPredeterminado(tipo), "gray"));
            List<TextDecoration> formatos = new ArrayList<>();
            for (String formato : yaml.getStringList(path + "formatos")) {
                try { formatos.add(TextDecoration.valueOf(formato.toUpperCase(java.util.Locale.ROOT))); }
                catch (IllegalArgumentException ignored) { }
            }
            if (nombre != null) rangos.add(new Rango(id, nombre, tipo, base, Rango.parseColor(colorName), new java.util.HashSet<>(formatos)));
        }
        List<Jugador> jugadores = new ArrayList<>();
        ConfigurationSection playerSection = yaml.getConfigurationSection("jugadores");
        if (playerSection != null) for (String uuidText : playerSection.getKeys(false)) {
            String path = "jugadores." + uuidText + ".";
            String nombre = yaml.getString(path + "nombre");
            String rangoId = yaml.getString(path + "rango");
            if (nombre != null && rangoId != null) {
                long fechaIngreso = yaml.getLong(path + "fechaIngreso", System.currentTimeMillis());
                jugadores.add(new Jugador(UUID.fromString(uuidText), nombre, rangoId,
                        yaml.getInt(path + "nivel", 1), yaml.getLong(path + "monedas", 0),
                        yaml.getLong(path + "horasJugadas", 0), Instant.ofEpochMilli(fechaIngreso)));
            }
        }
        return new PersistenciaDatos(rangos, jugadores, yaml.getString("rangoPredeterminado", "player"));
    }

    @Override public List<Baneo> cargarBaneos() throws IOException {
        if (!archivoSanciones.exists()) return List.of();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivoSanciones);
        List<Baneo> baneos = new ArrayList<>();
        for (Map<?, ?> data : yaml.getMapList("baneos")) {
            try {
                UUID id = UUID.fromString(String.valueOf(data.get("id")));
                UUID jugadorId = UUID.fromString(String.valueOf(data.get("jugadorId")));
                UUID responsableId = UUID.fromString(String.valueOf(data.get("responsableId")));
                CausaBaneo causa = CausaBaneo.valueOf(String.valueOf(data.get("causa")));
                boolean permanente = Boolean.parseBoolean(String.valueOf(data.get("permanente")));
                long creadaMillis = Long.parseLong(String.valueOf(data.get("creadaEn")));
                Object expirationValue = data.get("expiraEn");
                Long expirationMillis = expirationValue == null ? null : Long.parseLong(String.valueOf(expirationValue));
                if (!permanente && (expirationMillis == null || expirationMillis <= 0)) continue;
                baneos.add(new Baneo(id, jugadorId, String.valueOf(data.get("jugadorNombre")), causa,
                        valueOrEmpty(data, "detalleCausa"), valueOrEmpty(data, "mensaje"),
                        responsableId, String.valueOf(data.get("responsableNombre")), java.time.Instant.ofEpochMilli(creadaMillis),
                        permanente ? null : java.time.Instant.ofEpochMilli(expirationMillis), permanente));
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // Una entrada corrupta no debe impedir que se recuperen los demás baneos.
            }
        }
        return List.copyOf(baneos);
    }

    private String valueOrEmpty(Map<?, ?> data, String key) {
        Object value = data.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    @Override public List<String> cargarRegistrosSanciones() throws IOException {
        if (!archivoSanciones.exists()) return List.of();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivoSanciones);
        return List.copyOf(yaml.getStringList("sanciones.historial"));
    }

    @Override public void guardarRegistrosSanciones(Collection<String> registros) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", 2);
        yaml.set("sanciones.version", 2);
        yaml.set("sanciones.historial", new ArrayList<>(registros));
        File parent = archivoSanciones.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("No se pudo crear la carpeta de sanciones.");
        yaml.save(archivoSanciones);
    }

    @Override public void guardarBaneos(Collection<Baneo> baneos) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", 1);
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Baneo baneo : baneos) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", baneo.getId().toString());
            data.put("jugadorId", baneo.getJugadorId().toString());
            data.put("jugadorNombre", baneo.getJugadorNombre());
            data.put("causa", baneo.getCausa().name());
            data.put("detalleCausa", baneo.getDetalleCausa());
            data.put("mensaje", baneo.getMensaje());
            data.put("responsableId", baneo.getResponsableId().toString());
            data.put("responsableNombre", baneo.getResponsableNombre());
            data.put("creadaEn", baneo.getCreadaEn().toEpochMilli());
            data.put("permanente", baneo.esPermanente());
            data.put("expiraEn", baneo.getExpiraEn() == null ? null : baneo.getExpiraEn().toEpochMilli());
            entries.add(data);
        }
        yaml.set("baneos", entries);
        File parent = archivoSanciones.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("No se pudo crear la carpeta de sanciones.");
        yaml.save(archivoSanciones);
    }
}
