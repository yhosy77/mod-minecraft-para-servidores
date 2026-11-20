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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PersistenciaArchivo implements IPersistencia {
    private final File archivo;
    private final File archivoSanciones;
    private final File archivoRegistrosSanciones;
    public PersistenciaArchivo(File carpetaDatos) {
        archivo = new File(carpetaDatos, "rangos.yml");
        archivoSanciones = new File(carpetaDatos, "sanciones.yml");
        archivoRegistrosSanciones = new File(carpetaDatos, "sanciones.txt");
    }

    /** Lee sanciones.txt: devuelve las líneas no vacías (lista vacía si el archivo aún no existe). */
    @Override public List<String> cargarRegistrosSanciones() throws IOException {
        if (!archivoRegistrosSanciones.exists()) return new ArrayList<>();
        List<String> registros = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivoRegistrosSanciones, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.isBlank()) registros.add(linea.trim());
            }
        }
        return registros;
    }

    /** Escribe sanciones.txt completo con una línea por sanción. */
    @Override public void guardarRegistrosSanciones(List<String> registros) throws IOException {
        File parent = archivoRegistrosSanciones.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("No se pudo crear la carpeta de sanciones.");
        try (PrintWriter salida = new PrintWriter(new FileWriter(archivoRegistrosSanciones, StandardCharsets.UTF_8))) {
            for (String registro : registros) salida.println(registro);
            if (salida.checkError()) throw new IOException("Error al escribir " + archivoRegistrosSanciones.getName());
        }
    }

    @Override public PersistenciaDatos cargar() throws IOException {
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
            if (nombre != null && rangoId != null) jugadores.add(new Jugador(UUID.fromString(uuidText), nombre, rangoId));
        }
        return new PersistenciaDatos(rangos, jugadores, yaml.getString("rangoPredeterminado", "player"));
    }

    @Override public void guardar(Collection<Rango> rangos, Collection<Jugador> jugadores, String defaultId) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", 2);
        yaml.set("rangoPredeterminado", defaultId);
        for (Rango rango : rangos) {
            String path = "rangos." + rango.getId() + ".";
            yaml.set(path + "nombre", rango.getNombre());
            yaml.set(path + "tipo", rango.getTipo().name());
            yaml.set(path + "base", rango.esBase());
            yaml.set(path + "permisos", new ArrayList<>(rango.getPermisos()));
            yaml.set(path + "color", rango.getColorNombre());
            yaml.set(path + "formatos", rango.getFormatos().stream().map(Enum::name).sorted().toList());
        }
        for (Jugador jugador : jugadores) {
            String path = "jugadores." + jugador.getUuid() + ".";
            yaml.set(path + "nombre", jugador.getNombre());
            yaml.set(path + "rango", jugador.getRangoId());
        }
        File parent = archivo.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("No se pudo crear la carpeta de datos del plugin.");
        yaml.save(archivo);
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
