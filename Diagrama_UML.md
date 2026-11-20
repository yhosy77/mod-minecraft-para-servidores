# Diagrama de Clases UML — Skill Issue Admin

> **Proyecto:** Skill Issue Admin (Plugin PaperMC / Java 21 · Grupo **POO - Error Capa 8**)  
> **Correspondencia:** 1:1 con el código fuente unificado de la entrega final (`59` tipos principales en `5` paquetes, `10` tipos anidados y contratos externos de Paper/Bukkit/JDK).

## Resumen Arquitectónico y Principios de POO Aplicados

| Paquete | Clases / Tipos Principales | Tipos Anidados | Responsabilidad Principal |
| :--- | :---: | :---: | :--- |
| `ErrorCapa8.skillIssueAdmin` | 5 | 0 | Ciclo de vida del plugin (`SkillIssueAdmin`), economía (`CompraRango`, `Donacion`) y excepciones globales (`JugadorNoEncontradoException`, `SancionInvalidaException`). |
| `ErrorCapa8.skillIssueAdmin.Rank` | 23 | 3 | Dominio (`Jugador`, `Rango`, `TipoRango`), persistencia (`IPersistencia`, `PersistenciaArchivo`, `PersistenciaDatos`), gestión (`GestorJugadores`, `RankDisplayManager`, `RankPermissionManager`), comandos (`/rank`, `/profile`) y GUI de rangos. |
| `ErrorCapa8.skillIssueAdmin.Sanctions` | 18 | 7 | Jerarquía polimórfica (`Sancion`, `Baneo`, `Mute`, `Advertido`), enums (`CausaBaneo`, `DuracionBaneo`), `GestorSanciones`, `SancionInvalidaException`, listeners de bloqueo, comandos (`/ban`, `/mute`, `/warn`, `/sanciones`, `/perdonar`) y asistente GUI (`BanMenuViews`, `BanMenuListener`, `BanDraft`). |
| `ErrorCapa8.skillIssueAdmin.Report` | 11 | 0 | Modelo de reportes (`PlayerReport`, `ReportType`, `ReportPriority`), `ReportCreationManager` (cooldown y YAML), comandos (`/report`, `/gestreports`) y vistas GUI (`ReportMenu`, `ReportTypeMenu`, `ReportAdminMenu`). |
| `ErrorCapa8.skillIssueAdmin.listeners` | 2 | 0 | Listeners generales (`ModeracionListener`, `connectionListener`). |

**Conceptos clave reflejados en el diagrama:**
- **Abstracción y Polimorfismo de Inclusión:** `Sancion` define los métodos abstractos `aplicarEfecto(Jugador)`, `getTipo()` y `datosExtra()` implementados por `Baneo`, `Mute` y `Advertido`; `BasicCommand`, `Listener` e `InventoryHolder` unifican comandos, eventos e inventarios.
- **Interfaces y Desacoplamiento:** `IPersistencia` desacopla `GestorJugadores` y `GestorSanciones` de la implementación concreta `PersistenciaArchivo`.
- **Encapsulamiento y Sobrecarga:** Atributos privados con validación en constructores/setters, sobrecarga en `Jugador`, `Rango`, `Sancion`, `GestorJugadores.crearRango(...)`, `GestorSanciones.levantarSancion(...)` / `obtenerSancionesPorJugador(...)`, `RankItems.item(...)` y `PlayerReport`.
- **Miembros Estáticos, Records, Enums y Clases Anidadas:** `Sancion.contadorSanciones` / `getSiguienteId()` / `desdeLinea()`, `PersistenciaDatos`, `Binding`, `PendingCreation`, `PendingChat`, `ChatStep`, `RankListMenu.Mode` y las vistas estáticas de `BanMenuViews`.
- **Excepciones Personalizadas:** `SancionInvalidaException` (en raíz y en `Sanctions`) y `JugadorNoEncontradoException` heredando de `Exception`.

---

## Diagrama de Clases Completo (Mermaid)

```mermaid
classDiagram
    direction TB

    %% =========================================================================
    %% CONTRATOS EXTERNOS (PaperMC / Bukkit / JDK)
    %% =========================================================================
    namespace Contratos_API_Paper_Bukkit {
        class JavaPlugin {
            <<external>>
            +getServer() Server
            +getDataFolder() File
            +getLogger() Logger
            +registerCommand(String name, BasicCommand cmd) void
        }
        class BasicCommand {
            <<interface>>
            +execute(CommandSourceStack source, String[] args) void
            +suggest(CommandSourceStack source, String[] args) Collection~String~
        }
        class Listener {
            <<interface>>
        }
        class InventoryHolder {
            <<interface>>
            +getInventory() Inventory
        }
        class Exception {
            <<external>>
        }
        class WebSocketListener["java.net.http.WebSocket.Listener"] {
            <<interface>>
        }
    }

    %% =========================================================================
    %% PAQUETE RAIZ: ErrorCapa8.skillIssueAdmin
    %% =========================================================================
    namespace ErrorCapa8_skillIssueAdmin {
        class SkillIssueAdmin {
            -ReportCreationManager reportManager
            -GestorJugadores gestorJugadores
            -RankDisplayManager rankDisplayManager
            -RankPermissionManager rankPermissionManager
            -GestorSanciones gestorSanciones
            +onEnable() void
            +onDisable() void
        }
        class CompraRango {
            -String idCompra
            -String nombreJugador
            -String nombreRango
            -double precioPagado
            -LocalDateTime fecha
            -boolean completada
            +CompraRango(String idCompra, String nombreJugador, String nombreRango, double precioPagado)
            +procesarCompra(double montoEntregado) boolean
            +getIdCompra() String
            +getNombreJugador() String
            +getNombreRango() String
            +getPrecioPagado() double
            +getFecha() LocalDateTime
            +isCompletada() boolean
            +toString() String
            +equals(Object o) boolean
            +hashCode() int
        }
        class Donacion {
            -String idDonacion
            -String nombreJugador
            -double monto
            -String mensaje
            -LocalDateTime fecha
            -boolean confirmada
            +Donacion(String idDonacion, String nombreJugador, double monto, String mensaje)
            +confirmarDonacion() void
            +getIdDonacion() String
            +getNombreJugador() String
            +getMonto() double
            +getMensaje() String
            +getFecha() LocalDateTime
            +isConfirmada() boolean
            +toString() String
            +equals(Object o) boolean
            +hashCode() int
        }
        class JugadorNoEncontradoException {
            <<exception>>
            +JugadorNoEncontradoException(String mensaje)
            +JugadorNoEncontradoException(String mensaje, Throwable causa)
        }
        class SancionInvalidaException_Root["SancionInvalidaException (raíz)"] {
            <<exception>>
            +SancionInvalidaException(String mensaje)
            +SancionInvalidaException(String mensaje, Throwable causa)
        }
    }

    %% =========================================================================
    %% PAQUETE: ErrorCapa8.skillIssueAdmin.Rank
    %% =========================================================================
    namespace ErrorCapa8_skillIssueAdmin_Rank {
        class IPersistencia {
            <<interface>>
            +cargar() PersistenciaDatos
            +guardar(Collection~Rango~ rangos, Collection~Jugador~ jugadores, String rangoPredeterminadoId) void
            +cargarBaneos() List~Baneo~
            +guardarBaneos(Collection~Baneo~ baneos) void
            +cargarRegistrosSanciones() List~String~
            +guardarRegistrosSanciones(Collection~String~ registros) void
        }
        class PersistenciaArchivo {
            -File archivo
            -File archivoSanciones
            +PersistenciaArchivo(File carpetaDatos)
            +cargar() PersistenciaDatos
            +guardar(Collection~Rango~ rangos, Collection~Jugador~ jugadores, String defaultId) void
            +cargarBaneos() List~Baneo~
            +guardarBaneos(Collection~Baneo~ baneos) void
            +cargarRegistrosSanciones() List~String~
            +guardarRegistrosSanciones(Collection~String~ registros) void
            -valueOrEmpty(Map data, String key) String
        }
        class PersistenciaDatos {
            <<record>>
            +List~Rango~ rangos
            +List~Jugador~ jugadores
            +String rangoPredeterminadoId
        }
        class Jugador {
            -UUID uuid
            -String nombre
            -String rangoId
            -int nivel
            -long monedas
            -long horasJugadas
            -Instant fechaIngreso
            +Jugador(UUID uuid, String nombre, String rangoId)
            +Jugador(UUID uuid, String nombre, String rangoId, int nivel, long monedas, long horasJugadas, Instant fechaIngreso)
            +getUuid() UUID
            +getNombre() String
            +getRangoId() String
            +getNivel() int
            +getMonedas() long
            +getHorasJugadas() long
            +getFechaIngreso() Instant
            +actualizarProgreso(int nivel, long monedas, long horasJugadas) void
            +actualizar(String nombre, String rangoId) void
            +asignarRango(String rangoId) void
        }
        class Rango {
            -String id
            -String nombre
            -TipoRango tipo
            -boolean base
            -TextColor color
            -EnumSet~TextDecoration~ formatos
            +Rango(String nombre, TipoRango tipo, boolean base)
            +Rango(String id, String nombre, TipoRango tipo, boolean base)
            +Rango(String id, String nombre, TipoRango tipo, boolean base, TextColor color)
            +Rango(String id, String nombre, TipoRango tipo, boolean base, TextColor color, Set~TextDecoration~ formatos)
            +normalizarId(String nombre)$ String
            +parseColor(String value)$ TextColor
            +colorPredeterminado(TipoRango tipo)$ NamedTextColor
            +getId() String
            +getNombre() String
            +getTipo() TipoRango
            +esBase() boolean
            +setBase(boolean base) void
            +getPermisos() Set~String~
            +tienePermiso(String permiso) boolean
            +esValidoComoPredeterminado() boolean
            +getColor() TextColor
            +getColorNombre() String
            +getFormatos() Set~TextDecoration~
            +tieneFormato(TextDecoration formato) boolean
            +setColor(TextColor color) void
            +setFormatos(Set~TextDecoration~ formatos) void
        }
        class TipoRango {
            <<enumeration>>
            ADMIN
            OP
            MOD
            PLAYER
            -Set~String~ permisos
            +permisos() Set~String~
            +puedeGestionarRangos() boolean
            +tienePermisosElevados() boolean
        }
        class GestorJugadores {
            -Pattern NOMBRE_VALIDO$
            -JavaPlugin plugin
            -IPersistencia persistencia
            -Map~String, Rango~ rangos
            -Map~UUID, Jugador~ jugadores
            -String rangoPredeterminadoId
            +GestorJugadores(JavaPlugin plugin, IPersistencia persistencia)
            -cargar() void
            -asegurarRangosBase() boolean
            -asegurarBase(String id, String nombre, TipoRango tipo) boolean
            -migrarBaseModeradorAOp() boolean
            -guardar() void
            +obtenerRangos() Collection~Rango~
            +obtenerRangos(TipoRango tipo) List~Rango~
            +buscarRango(String id) Rango
            +buscarRangoPorNombre(String nombre) Rango
            +obtenerRangoPredeterminado() Rango
            +buscarJugador(UUID uuid) Jugador
            +obtenerRangoJugador(UUID uuid) Rango
            +esAdministrador(UUID uuid) boolean
            +jugadoresConRango(String rangoId) List~Jugador~
            +registrarOActualizarJugador(Player player) void
            +crearRango(String nombre, TipoRango tipo) Rango
            +crearRango(String nombre, TipoRango tipo, TextColor color, Set~TextDecoration~ formatos) Rango
            +establecerRangoPredeterminado(String rangoId) void
            +obtenerJugadores() List~Jugador~
            +actualizarProgreso(UUID uuid, int nivel, long monedas, long horasJugadas) void
            +asignarRango(UUID jugadorId, String rangoId) void
            +cambiarEstilo(String rangoId, TextColor color, Set~TextDecoration~ formatos) void
            +eliminarRango(String rangoId) void
        }
        class RankDisplayManager {
            -String TEAM_PREFIX$
            -JavaPlugin plugin
            -GestorJugadores gestor
            -Map~UUID, Map~Scoreboard, Binding~~ bindings
            -Set~Scoreboard~ touchedScoreboards
            +RankDisplayManager(JavaPlugin plugin, GestorJugadores gestor)
            +refreshAll() void
            +refreshPlayer(Player player) void
            -applyToBoard(Player player, Rango rango, Scoreboard scoreboard, Map~Scoreboard, Binding~ playerBindings) void
            +removePlayer(Player player) void
            +shutdown() void
            +onJoin(PlayerJoinEvent event) void
            +onQuit(PlayerQuitEvent event) void
            -removeOwnedEntry(Binding binding) void
            -teamName(String rankId) String
        }
        class RankDisplayManager_Binding["RankDisplayManager.Binding"] {
            <<record>>
            +Scoreboard scoreboard
            +String entry
            +String teamName
        }
        class RankPermissionManager {
            -JavaPlugin plugin
            -GestorJugadores gestor
            -Map~UUID, PermissionAttachment~ attachments
            -Set~String~ managedPermissions
            +RankPermissionManager(JavaPlugin plugin, GestorJugadores gestor)
            +apply(Player player) void
            +refreshAll() void
            +clear(Player player) void
            +clearAll() void
        }
        class RegistroJugadorListener {
            -GestorJugadores gestor
            -RankDisplayManager displayManager
            -RankPermissionManager permissionManager
            +RegistroJugadorListener(GestorJugadores gestor, RankDisplayManager displayManager, RankPermissionManager permissionManager)
            +onJoin(PlayerJoinEvent event) void
            +onQuit(PlayerQuitEvent event) void
        }
        class RankCommand {
            -GestorJugadores gestor
            +RankCommand(GestorJugadores gestor)
            +execute(CommandSourceStack source, String[] args) void
        }
        class ProfileCommand {
            -DateTimeFormatter DATE_FORMAT$
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            +ProfileCommand(GestorJugadores jugadores, GestorSanciones sanciones)
            +execute(CommandSourceStack source, String[] args) void
        }
        class RankMenuListener {
            -long DELETE_DELAY_MILLIS$
            -SkillIssueAdmin plugin
            -GestorJugadores gestor
            -RankDisplayManager displayManager
            -RankPermissionManager permissionManager
            -Map~UUID, PendingCreation~ pendingCreation
            -Map~UUID, RankColorMenu~ pendingHexInput
            -Map~UUID, BukkitTask~ deletionCountdowns
            +RankMenuListener(SkillIssueAdmin plugin, GestorJugadores gestor, RankDisplayManager displayManager, RankPermissionManager permissionManager)
            +onInventoryClick(InventoryClickEvent event) void
            +onInventoryDrag(InventoryDragEvent event) void
            +onInventoryClose(InventoryCloseEvent event) void
            +onQuit(PlayerQuitEvent event) void
            +onChat(AsyncChatEvent event) void
            -abrirConfirmacion(Player player, Rango rango) void
            -cancelarCuenta(UUID playerId) void
        }
        class RankMenuListener_PendingCreation["RankMenuListener.PendingCreation"] {
            <<record>>
            +TipoRango tipo
            +TextColor color
            +Set~TextDecoration~ formatos
        }
        class RankItems {
            -RankItems()
            ~item(Material material, String name, String[] lore)$ ItemStack
            ~item(Material material, Component name, String[] lore)$ ItemStack
            ~frame(Inventory inventory)$ void
            ~contentSlots()$ int[]
        }
        class RankMainMenu {
            -Inventory inventory
            +RankMainMenu(GestorJugadores gestor)
            +getInventory() Inventory
        }
        class RankTypeMenu {
            -int[] SLOTS$
            -TipoRango[] TYPES$
            -Inventory inventory
            +RankTypeMenu()
            ~tipoAt(int slot)$ TipoRango
            +getInventory() Inventory
        }
        class RankColorMenu {
            -NamedTextColor[] COLORS$
            -Material[] COLOR_ICONS$
            -TextDecoration[] FORMATS$
            -int[] FORMAT_SLOTS$
            -Material[] FORMAT_ICONS$
            -String[] FORMAT_LABELS$
            -Inventory inventory
            -boolean creation
            -String rangoId
            -String previewName
            -TipoRango tipo
            -TextColor color
            -EnumSet~TextDecoration~ formatos
            +RankColorMenu(TipoRango tipo)
            +RankColorMenu(Rango rango)
            -RankColorMenu(boolean creation, String rangoId, String previewName, TipoRango tipo, TextColor color, Set~TextDecoration~ formatos)
            +setColor(TextColor color) void
            +toggle(TextDecoration decoration) void
            +color() TextColor
            +formatos() Set~TextDecoration~
            +creation() boolean
            +rangoId() String
            +previewName() String
            +tipo() TipoRango
            -refresh() void
            -previewComponent() Component
            -colorPersistido() String
            ~colorAt(int slot)$ TextColor
            ~formatAt(int slot)$ TextDecoration
            +getInventory() Inventory
        }
        class RankRgbColorMenu {
            -String[] HEX$
            -String[] LABELS$
            -Material[] ICONS$
            -Inventory inventory
            -RankColorMenu parent
            +RankRgbColorMenu(RankColorMenu parent)
            ~colorAt(int slot)$ TextColor
            -colorAtIndex(int index)$ TextColor
            +parent() RankColorMenu
            +getInventory() Inventory
        }
        class RankListMenu {
            -Inventory inventory
            -Mode mode
            -int page
            -List~Rango~ rangos
            +RankListMenu(GestorJugadores gestor, Mode mode, int page)
            +rangoAt(int rawSlot) Rango
            +mode() Mode
            +page() int
            +getInventory() Inventory
        }
        class RankListMenu_Mode["RankListMenu.Mode"] {
            <<enumeration>>
            DEFAULT
            DELETE
            COLOR
        }
        class RankPlayerSelectMenu {
            -Inventory inventory
            -List~Jugador~ jugadores
            -int page
            +RankPlayerSelectMenu(GestorJugadores gestor, int page)
            +jugadorAt(int slot) Jugador
            +page() int
            +getInventory() Inventory
        }
        class RankAssignmentMenu {
            -Inventory inventory
            -Jugador jugador
            -List~Rango~ rangos
            -int page
            +RankAssignmentMenu(GestorJugadores gestor, Jugador jugador, int page)
            +rangoAt(int slot) Rango
            +jugador() Jugador
            +page() int
            +getInventory() Inventory
        }
        class RankAssignmentConfirmMenu {
            -Inventory inventory
            -UUID jugadorId
            -String jugadorNombre
            -String rangoId
            -String rangoNombre
            +RankAssignmentConfirmMenu(Jugador jugador, Rango rango, Rango actual)
            +jugadorId() UUID
            +jugadorNombre() String
            +rangoId() String
            +rangoNombre() String
            +getInventory() Inventory
        }
        class RankDeleteConfirmMenu {
            -Inventory inventory
            -String rangoId
            -String rangoNombre
            -long habilitadoEn
            +RankDeleteConfirmMenu(String rangoId, String rangoNombre, long habilitadoEn, int remainingSeconds)
            +update(int remainingSeconds) void
            +rangoId() String
            +rangoNombre() String
            +habilitadoEn() long
            +getInventory() Inventory
        }
    }

    %% =========================================================================
    %% PAQUETE: ErrorCapa8.skillIssueAdmin.Sanctions
    %% =========================================================================
    namespace ErrorCapa8_skillIssueAdmin_Sanctions {
        class Sancion {
            <<abstract>>
            +String SEPARADOR$
            +DateTimeFormatter FORMATO_FECHA$
            -int contadorSanciones$
            -UUID id
            -UUID jugadorId
            -String jugadorNombre
            -UUID responsableId
            -String responsableNombre
            -Instant creadaEn
            -int numero
            -String motivo
            -boolean activa
            -String motivoPerdon
            #Sancion(UUID id, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, Instant creadaEn)
            #Sancion(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, Instant creadaEn, String motivo, boolean activa, String motivoPerdon)
            +getSiguienteId()$ int
            #actualizarContador(int numeroExistente)$ void
            ~fijarContador(int valor)$ void
            +desdeLinea(String linea)$ Sancion
            +aplicarEfecto(Jugador j)* boolean
            +getTipo()* String
            #datosExtra()* String
            +revocar(String motivoPerdon) void
            ~restaurarActiva() void
            +haExpirado(Instant ahora) boolean
            +estaVigente(Instant ahora) boolean
            +getResumen() String
            +toLinea() String
            +toString() String
            #buscarJugadorConectado(Jugador j) Player
            #limpiar(String texto)$ String
            #vacioComoGuion(String texto)$ String
            #guionComoVacio(String texto)$ String
            +getId() UUID
            +getJugadorId() UUID
            +getJugadorNombre() String
            +getResponsableId() UUID
            +getResponsableNombre() String
            +getCreadaEn() Instant
            +getNumero() int
            +getMotivo() String
            +getFecha() String
            +getAdminResponsable() String
            +isActiva() boolean
            +getMotivoPerdon() String
        }
        class Baneo {
            -CausaBaneo causa
            -String detalleCausa
            -String mensaje
            -Instant expiraEn
            -boolean permanente
            +Baneo(UUID id, UUID jugadorId, String jugadorNombre, CausaBaneo causa, String detalleCausa, String mensaje, UUID responsableId, String responsableNombre, Instant creadaEn, Instant expiraEn, boolean permanente)
            ~Baneo(int numero, boolean activa, String motivoPerdon, UUID id, UUID jugadorId, String jugadorNombre, CausaBaneo causa, String detalleCausa, String mensaje, UUID responsableId, String responsableNombre, Instant creadaEn, Instant expiraEn, boolean permanente)
            +getCausa() CausaBaneo
            +getDetalleCausa() String
            +getMensaje() String
            +getExpiraEn() Instant
            +esPermanente() boolean
            +estaActivo(Instant ahora) boolean
            +causaCompleta() String
            +duracionTexto() String
            +mensajeKick() Component
            +mensajeIngreso() String
            +aplicarEfecto(Jugador j) boolean
            +getTipo() String
            +getMotivo() String
            +haExpirado(Instant ahora) boolean
            #datosExtra() String
        }
        class Mute {
            +String CANAL_GLOBAL$
            -int duracionMinutos
            -String canalSilenciado
            +Mute(UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, String motivo, int duracionMinutos, String canalSilenciado)
            ~Mute(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, Instant creadaEn, String motivo, boolean activa, String motivoPerdon, int duracionMinutos, String canalSilenciado)
            +aplicarEfecto(Jugador j) boolean
            +haExpirado(Instant ahora) boolean
            +getTipo() String
            #datosExtra() String
            +mensajeSilenciado() Component
            +getExpiraEn() Instant
            +getDuracionMinutos() int
            +getCanalSilenciado() String
        }
        class Advertido {
            -int nivelSeveridad
            -boolean requiereConfirmacion
            +Advertido(UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, String motivo, int nivelSeveridad, boolean requiereConfirmacion)
            ~Advertido(UUID id, int numero, UUID jugadorId, String jugadorNombre, UUID responsableId, String responsableNombre, Instant creadaEn, String motivo, boolean activa, String motivoPerdon, int nivelSeveridad, boolean requiereConfirmacion)
            +aplicarEfecto(Jugador j) boolean
            +getTipo() String
            #datosExtra() String
            +getNombreSeveridad() String
            +getNivelSeveridad() int
            +isRequiereConfirmacion() boolean
        }
        class CausaBaneo {
            <<enumeration>>
            INSULTOS
            TRAMPAS
            SPAM
            TOXICIDAD
            EXPLOITS
            OTRA
            -String etiqueta
            -String descripcion
            -Material icono
            +etiqueta() String
            +descripcion() String
            +icono() Material
        }
        class DuracionBaneo {
            <<enumeration>>
            UN_DIA
            TRES_DIAS
            SIETE_DIAS
            CATORCE_DIAS
            TREINTA_DIAS
            PERMANENTE
            -String etiqueta
            -int dias
            -Material icono
            +etiqueta() String
            +icono() Material
            +calcularExpiracion(Instant creadoEn) Instant
        }
        class SancionInvalidaException_Sanctions["SancionInvalidaException (Sanctions)"] {
            <<exception>>
            +SancionInvalidaException(String mensaje)
        }
        class GestorSanciones {
            +String ARCHIVO$
            -JavaPlugin plugin
            -IPersistencia persistencia
            -GestorJugadores gestorJugadores
            -ArrayList~Sancion~ historial
            -ArrayList~Sancion~ sancionesActivas
            +GestorSanciones(JavaPlugin plugin, IPersistencia persistencia, GestorJugadores gestorJugadores)
            +obtenerBaneos() List~Baneo~
            +obtenerBaneoActivo(UUID jugadorId, Instant ahora) Optional~Baneo~
            +aplicarBaneo(BanDraft draft, Player administrador) Baneo
            +aplicarSancion(Jugador j, Sancion s) void
            +levantarSancion(int idSancion) boolean
            +levantarSancion(int idSancion, String motivoPerdon) boolean
            +buscarPorNumero(int numero) Sancion
            +obtenerSancionesPorJugador(String id) ArrayList~Sancion~
            +obtenerSancionesPorJugador(UUID id) ArrayList~Sancion~
            +calcularReincidencia(Jugador j) int
            +getNivelReincidencia(Jugador j) String
            +estaMuteado(UUID jugadorId) boolean
            +obtenerMuteActivo(UUID jugadorId) Mute
            +limpiarExpiradas() void
            +getSancionesActivas() ArrayList~Sancion~
            +getHistorial() ArrayList~Sancion~
            +guardar() boolean
            -registrarYGuardar(Sancion s) void
            -cargar() void
        }
        class BanCommand {
            -GestorJugadores jugadores
            +BanCommand(GestorJugadores jugadores)
            +execute(CommandSourceStack source, String[] args) void
        }
        class MuteCommand {
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            +MuteCommand(GestorJugadores jugadores, GestorSanciones sanciones)
            +execute(CommandSourceStack source, String[] args) void
        }
        class WarnCommand {
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            +WarnCommand(GestorJugadores jugadores, GestorSanciones sanciones)
            +execute(CommandSourceStack source, String[] args) void
        }
        class SancionesCommand {
            -int MAXIMO_A_MOSTRAR$
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            +SancionesCommand(GestorJugadores jugadores, GestorSanciones sanciones)
            +execute(CommandSourceStack source, String[] args) void
        }
        class PerdonarCommand {
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            +PerdonarCommand(GestorJugadores jugadores, GestorSanciones sanciones)
            +execute(CommandSourceStack source, String[] args) void
        }
        class BanEnforcementListener {
            -GestorSanciones sanciones
            +BanEnforcementListener(GestorSanciones sanciones)
            +onPreLogin(AsyncPlayerPreLoginEvent event) void
        }
        class MuteEnforcementListener {
            -GestorSanciones sanciones
            +MuteEnforcementListener(GestorSanciones sanciones)
            +onChat(AsyncChatEvent event) void
        }
        class BanDraft {
            -UUID jugadorId
            -String jugadorNombre
            -CausaBaneo causa
            -String detalleCausa
            -DuracionBaneo duracion
            -String mensaje
            +BanDraft(UUID jugadorId, String jugadorNombre)
            +getJugadorId() UUID
            +getJugadorNombre() String
            +getCausa() CausaBaneo
            +getDetalleCausa() String
            +getDuracion() DuracionBaneo
            +getMensaje() String
            +setCausa(CausaBaneo causa, String detalle) void
            +setDuracion(DuracionBaneo duracion) void
            +setMensaje(String mensaje) void
            +estaCompleto() boolean
        }
        class BanMenuListener {
            -GestorJugadores jugadores
            -GestorSanciones sanciones
            -JavaPlugin plugin
            -Map~UUID, PendingChat~ pendingChat
            +BanMenuListener(JavaPlugin plugin, GestorJugadores jugadores, GestorSanciones sanciones)
            -isBanMenu(InventoryHolder holder) boolean
            +onClick(InventoryClickEvent event) void
            +onChat(AsyncChatEvent event) void
            +onDrag(InventoryDragEvent event) void
            +onQuit(PlayerQuitEvent event) void
            -handlePlayerMenu(Player admin, PlayerSelect menu, int slot) void
            -handleReasonMenu(Player admin, ReasonSelect menu, int slot) void
            -handleDurationMenu(Player admin, DurationSelect menu, int slot) void
            -handleMessageMenu(Player admin, MessageSelect menu, int slot) void
            -handleConfirmMenu(Player admin, Confirm menu, int slot) void
            -askInChat(Player admin, BanDraft draft, ChatStep step) void
            -processChatResponse(Player player, PendingChat expected, String text) void
        }
        class BanMenuListener_ChatStep["BanMenuListener.ChatStep"] {
            <<enumeration>>
            CUSTOM_CAUSE
            EXTRA_MESSAGE
        }
        class BanMenuListener_PendingChat["BanMenuListener.PendingChat"] {
            <<record>>
            +BanDraft draft
            +ChatStep step
        }
        class BanMenuViews {
            ~int PAGE_SIZE$
            -int[] PLAYER_SLOTS$
            -BanMenuViews()
            +playerSelect(int page, Player viewer)$ PlayerSelect
            +reasonSelect(BanDraft draft)$ ReasonSelect
            +durationSelect(BanDraft draft)$ DurationSelect
            +messageSelect(BanDraft draft)$ MessageSelect
            +confirm(BanDraft draft)$ Confirm
            ~item(Material material, String title, String[] lore)$ ItemStack
            ~frame(Inventory inv)$ void
            ~navigation(Inventory inv, boolean back)$ void
            ~eligiblePlayers()$ List~Player~
            ~playerIndexAt(int slot)$ int
        }
        class BanMenuViews_PlayerSelect["BanMenuViews.PlayerSelect"] {
            ~Inventory inventory
            ~int page
            ~List~Player~ targets
            ~PlayerSelect(int page, Player viewer)
            +getInventory() Inventory
        }
        class BanMenuViews_ReasonSelect["BanMenuViews.ReasonSelect"] {
            ~Inventory inventory
            ~BanDraft draft
            ~ReasonSelect(BanDraft draft)
            +getInventory() Inventory
        }
        class BanMenuViews_DurationSelect["BanMenuViews.DurationSelect"] {
            ~Inventory inventory
            ~BanDraft draft
            ~DurationSelect(BanDraft draft)
            +getInventory() Inventory
        }
        class BanMenuViews_MessageSelect["BanMenuViews.MessageSelect"] {
            ~Inventory inventory
            ~BanDraft draft
            ~MessageSelect(BanDraft draft)
            +getInventory() Inventory
        }
        class BanMenuViews_Confirm["BanMenuViews.Confirm"] {
            ~Inventory inventory
            ~BanDraft draft
            ~Confirm(BanDraft draft)
            +getInventory() Inventory
        }
    }

    %% =========================================================================
    %% PAQUETE: ErrorCapa8.skillIssueAdmin.Report
    %% =========================================================================
    namespace ErrorCapa8_skillIssueAdmin_Report {
        class PlayerReport {
            -UUID id
            -UUID reporterId
            -String reporterName
            -UUID targetId
            -String targetName
            -ReportType type
            -Instant createdAt
            -ReportPriority priority
            -boolean resolved
            -String resolvedBy
            -Instant resolvedAt
            +PlayerReport(UUID reporterId, String reporterName, UUID targetId, String targetName, ReportType type)
            +PlayerReport(UUID id, UUID reporterId, String reporterName, UUID targetId, String targetName, ReportType type, Instant createdAt, ReportPriority priority, boolean resolved, String resolvedBy, Instant resolvedAt)
            +id() UUID
            +reporterId() UUID
            +reporterName() String
            +targetId() UUID
            +targetName() String
            +type() ReportType
            +createdAt() Instant
            +priority() ReportPriority
            +resolved() boolean
            +resolvedBy() String
            +resolvedAt() Instant
            +setPriority(ReportPriority priority) void
            +setResolved(boolean value, String staff) void
        }
        class ReportType {
            <<enumeration>>
            SPAM
            TOXICITY
            HACKS
            SCAM
            HARASSMENT
            OTHER
            -String label
            +label() String
        }
        class ReportPriority {
            <<enumeration>>
            LOW
            MEDIUM
            URGENT
            -String label
            -Material icon
            +label() String
            +icon() Material
            +next(int step) ReportPriority
        }
        class ReportCreationManager {
            -long COOLDOWN_MILLIS$
            -SkillIssueAdmin plugin
            -File file
            -List~PlayerReport~ reports
            -Map~UUID, Long~ lastReportAt
            +ReportCreationManager(SkillIssueAdmin plugin)
            +remainingCooldownMillis(UUID reporter) long
            +submit(Player reporter, Player target, ReportType type) boolean
            +reports() List~PlayerReport~
            +setPriority(PlayerReport report, ReportPriority priority) void
            +setResolved(PlayerReport report, boolean value, String staff) void
            -load() void
            -save() void
        }
        class ReportPlayer {
            -ReportCreationManager manager
            +ReportPlayer(ReportCreationManager manager)
            +execute(CommandSourceStack source, String[] args) void
            +suggest(CommandSourceStack source, String[] args) Collection~String~
        }
        class ReportAdminCommand {
            -ReportCreationManager manager
            +ReportAdminCommand(ReportCreationManager manager)
            +execute(CommandSourceStack source, String[] args) void
        }
        class ReportMenuListener {
            -SkillIssueAdmin plugin
            -ReportCreationManager manager
            +ReportMenuListener(SkillIssueAdmin plugin, ReportCreationManager manager)
            +onClick(InventoryClickEvent event) void
            +onDrag(InventoryDragEvent event) void
        }
        class ReportMenu {
            ~int PAGE_SIZE$
            -int[] SLOTS$
            -Inventory inventory
            -int page
            +ReportMenu(int page)
            ~onlinePlayers()$ List~Player~
            ~playerAt(int slot)$ int
            +page() int
            +getInventory() Inventory
        }
        class ReportTypeMenu {
            -int[] SLOTS$
            -Material[] ICONS$
            -Inventory inventory
            -UUID targetId
            -String targetName
            +ReportTypeMenu(UUID targetId, String targetName)
            ~typeAt(int slot)$ ReportType
            +targetId() UUID
            +targetName() String
            +getInventory() Inventory
        }
        class ReportAdminMenu {
            ~int[] SLOTS$
            -Inventory inventory
            -int page
            -List~PlayerReport~ reports
            +ReportAdminMenu(ReportCreationManager manager, int page)
            +reportAt(int slot) PlayerReport
            +page() int
            +getInventory() Inventory
        }
        class MenuItems {
            -MenuItems()
            ~item(Material material, String name, String[] lore)$ ItemStack
            ~frame(Inventory inventory)$ void
        }
    }

    %% =========================================================================
    %% PAQUETE: ErrorCapa8.skillIssueAdmin.listeners
    %% =========================================================================
    namespace ErrorCapa8_skillIssueAdmin_listeners {
        class ModeracionListener {
            +ModeracionListener()
            +onAsyncPlayerChat(AsyncPlayerChatEvent e) void
            +onPlayerLogin(PlayerLoginEvent e) void
        }
        class connectionListener {
            +onJoin(PlayerJoinEvent event) void
        }
    }

    %% =========================================================================
    %% RELACIONES DE HERENCIA (extends)
    %% =========================================================================
    JavaPlugin <|-- SkillIssueAdmin : extends
    Exception <|-- JugadorNoEncontradoException : extends
    Exception <|-- SancionInvalidaException_Root : extends
    Exception <|-- SancionInvalidaException_Sanctions : extends
    Sancion <|-- Baneo : extends
    Sancion <|-- Mute : extends
    Sancion <|-- Advertido : extends

    %% =========================================================================
    %% IMPLEMENTACION DE INTERFACES (implements)
    %% =========================================================================
    IPersistencia <|.. PersistenciaArchivo : implements

    BasicCommand <|.. RankCommand : implements
    BasicCommand <|.. ProfileCommand : implements
    BasicCommand <|.. BanCommand : implements
    BasicCommand <|.. MuteCommand : implements
    BasicCommand <|.. WarnCommand : implements
    BasicCommand <|.. SancionesCommand : implements
    BasicCommand <|.. PerdonarCommand : implements
    BasicCommand <|.. ReportPlayer : implements
    BasicCommand <|.. ReportAdminCommand : implements

    Listener <|.. RankDisplayManager : implements
    Listener <|.. RegistroJugadorListener : implements
    Listener <|.. RankMenuListener : implements
    Listener <|.. BanEnforcementListener : implements
    Listener <|.. MuteEnforcementListener : implements
    Listener <|.. BanMenuListener : implements
    Listener <|.. ReportMenuListener : implements
    Listener <|.. ModeracionListener : implements
    WebSocketListener <|.. connectionListener : implements

    InventoryHolder <|.. RankMainMenu : implements
    InventoryHolder <|.. RankTypeMenu : implements
    InventoryHolder <|.. RankColorMenu : implements
    InventoryHolder <|.. RankRgbColorMenu : implements
    InventoryHolder <|.. RankListMenu : implements
    InventoryHolder <|.. RankPlayerSelectMenu : implements
    InventoryHolder <|.. RankAssignmentMenu : implements
    InventoryHolder <|.. RankAssignmentConfirmMenu : implements
    InventoryHolder <|.. RankDeleteConfirmMenu : implements
    InventoryHolder <|.. BanMenuViews_PlayerSelect : implements
    InventoryHolder <|.. BanMenuViews_ReasonSelect : implements
    InventoryHolder <|.. BanMenuViews_DurationSelect : implements
    InventoryHolder <|.. BanMenuViews_MessageSelect : implements
    InventoryHolder <|.. BanMenuViews_Confirm : implements
    InventoryHolder <|.. ReportMenu : implements
    InventoryHolder <|.. ReportTypeMenu : implements
    InventoryHolder <|.. ReportAdminMenu : implements

    %% =========================================================================
    %% COMPOSICION, AGREGACION Y ANIDAMIENTO
    %% =========================================================================
    SkillIssueAdmin "1" *-- "1" ReportCreationManager : crea y administra
    SkillIssueAdmin "1" *-- "1" GestorJugadores : crea y administra
    SkillIssueAdmin "1" *-- "1" GestorSanciones : crea y administra
    SkillIssueAdmin "1" *-- "1" RankDisplayManager : crea y administra
    SkillIssueAdmin "1" *-- "1" RankPermissionManager : crea y administra

    GestorJugadores "1" o-- "*" Rango : administra
    GestorJugadores "1" o-- "*" Jugador : administra
    GestorSanciones "1" o-- "*" Sancion : historial / sancionesActivas
    ReportCreationManager "1" o-- "*" PlayerReport : administra

    RankDisplayManager *-- RankDisplayManager_Binding : nested record
    RankMenuListener *-- RankMenuListener_PendingCreation : nested record
    RankListMenu *-- RankListMenu_Mode : nested enum
    BanMenuListener *-- BanMenuListener_ChatStep : nested enum
    BanMenuListener *-- BanMenuListener_PendingChat : nested record
    BanMenuViews *-- BanMenuViews_PlayerSelect : nested
    BanMenuViews *-- BanMenuViews_ReasonSelect : nested
    BanMenuViews *-- BanMenuViews_DurationSelect : nested
    BanMenuViews *-- BanMenuViews_MessageSelect : nested
    BanMenuViews *-- BanMenuViews_Confirm : nested

    %% =========================================================================
    %% ASOCIACIONES Y DEPENDENCIAS POR PAQUETE
    %% =========================================================================
    %% --- Núcleo & Economía ---
    SkillIssueAdmin ..> PersistenciaArchivo : instancia IPersistencia
    SkillIssueAdmin ..> RankCommand : registra /rank
    SkillIssueAdmin ..> ProfileCommand : registra /profile
    SkillIssueAdmin ..> BanCommand : registra /ban
    SkillIssueAdmin ..> MuteCommand : registra /mute
    SkillIssueAdmin ..> WarnCommand : registra /warn
    SkillIssueAdmin ..> SancionesCommand : registra /sanciones
    SkillIssueAdmin ..> PerdonarCommand : registra /perdonar
    SkillIssueAdmin ..> ReportPlayer : registra /report
    SkillIssueAdmin ..> ReportAdminCommand : registra /gestreports
    SkillIssueAdmin ..> RegistroJugadorListener : registra eventos
    SkillIssueAdmin ..> RankMenuListener : registra eventos
    SkillIssueAdmin ..> BanMenuListener : registra eventos
    SkillIssueAdmin ..> BanEnforcementListener : registra eventos
    SkillIssueAdmin ..> MuteEnforcementListener : registra eventos
    SkillIssueAdmin ..> ReportMenuListener : registra eventos
    CompraRango ..> SancionInvalidaException_Root : lanza

    %% --- Paquete Rank ---
    GestorJugadores "1" --> "1" IPersistencia : usa
    PersistenciaArchivo ..> PersistenciaDatos : retorna
    PersistenciaArchivo ..> Rango : serializa
    PersistenciaArchivo ..> Jugador : serializa
    PersistenciaArchivo ..> Baneo : serializa legado
    PersistenciaDatos "1" o-- "*" Rango : contiene
    PersistenciaDatos "1" o-- "*" Jugador : contiene
    Rango "*" --> "1" TipoRango : tipo
    Jugador "*" --> "1" Rango : rangoId
    RankDisplayManager --> GestorJugadores : consulta
    RankDisplayManager ..> Rango : formatea prefijo
    RankPermissionManager --> GestorJugadores : consulta
    RankPermissionManager ..> TipoRango : permisos
    RegistroJugadorListener --> GestorJugadores : registra jugador
    RegistroJugadorListener --> RankDisplayManager : actualiza prefijo
    RegistroJugadorListener --> RankPermissionManager : aplica permisos
    RankCommand --> GestorJugadores : usa
    RankCommand ..> RankMainMenu : abre
    ProfileCommand --> GestorJugadores : consulta perfil
    ProfileCommand --> GestorSanciones : consulta historial
    RankMenuListener --> SkillIssueAdmin : programa tareas
    RankMenuListener --> GestorJugadores : modifica rangos
    RankMenuListener --> RankDisplayManager : refresca
    RankMenuListener --> RankPermissionManager : reaplica
    RankMenuListener ..> RankMainMenu : navega
    RankMenuListener ..> RankTypeMenu : navega
    RankMenuListener ..> RankListMenu : navega
    RankMenuListener ..> RankPlayerSelectMenu : navega
    RankMenuListener ..> RankAssignmentMenu : navega
    RankMenuListener ..> RankAssignmentConfirmMenu : navega
    RankMenuListener ..> RankColorMenu : navega
    RankMenuListener ..> RankRgbColorMenu : navega
    RankMenuListener ..> RankDeleteConfirmMenu : navega
    RankRgbColorMenu --> RankColorMenu : parent
    RankListMenu --> RankListMenu_Mode : mode
    RankAssignmentMenu --> Jugador : jugador
    RankAssignmentMenu --> Rango : rangos
    RankMainMenu ..> RankItems : usa
    RankTypeMenu ..> RankItems : usa
    RankColorMenu ..> RankItems : usa
    RankRgbColorMenu ..> RankItems : usa
    RankListMenu ..> RankItems : usa
    RankPlayerSelectMenu ..> RankItems : usa
    RankAssignmentMenu ..> RankItems : usa
    RankAssignmentConfirmMenu ..> RankItems : usa
    RankDeleteConfirmMenu ..> RankItems : usa

    %% --- Paquete Sanctions ---
    GestorSanciones "1" --> "1" IPersistencia : persiste
    GestorSanciones --> GestorJugadores : valida staff/jugador
    Baneo "*" --> "1" CausaBaneo : causa
    BanDraft --> CausaBaneo : causa
    BanDraft --> DuracionBaneo : duracion
    BanMenuListener --> GestorJugadores : valida admin
    BanMenuListener --> GestorSanciones : aplicaBaneo
    BanMenuListener --> BanDraft : gestiona
    BanMenuListener ..> BanMenuViews : navega vistas
    BanMenuViews ..> BanDraft : construye con
    BanMenuViews ..> CausaBaneo : lista causas
    BanMenuViews ..> DuracionBaneo : lista duraciones
    BanMenuViews_ReasonSelect --> BanDraft : draft
    BanMenuViews_DurationSelect --> BanDraft : draft
    BanMenuViews_MessageSelect --> BanDraft : draft
    BanMenuViews_Confirm --> BanDraft : draft
    BanCommand --> GestorJugadores : valida admin
    BanCommand ..> BanMenuViews : abre
    MuteCommand --> GestorJugadores : valida
    MuteCommand --> GestorSanciones : aplicaSancion
    MuteCommand ..> Mute : crea
    WarnCommand --> GestorJugadores : valida
    WarnCommand --> GestorSanciones : aplicaSancion
    WarnCommand ..> Advertido : crea
    SancionesCommand --> GestorJugadores : busca jugador
    SancionesCommand --> GestorSanciones : consulta historial
    PerdonarCommand --> GestorJugadores : valida admin
    PerdonarCommand --> GestorSanciones : levantarSancion
    BanEnforcementListener --> GestorSanciones : verifica baneo
    MuteEnforcementListener --> GestorSanciones : verifica mute
    Sancion ..> Jugador : aplicaEfecto
    Sancion ..> SancionInvalidaException_Sanctions : lanza
    GestorSanciones ..> SancionInvalidaException_Sanctions : lanza
    Mute ..> SancionInvalidaException_Sanctions : lanza
    Advertido ..> SancionInvalidaException_Sanctions : lanza

    %% --- Paquete Report ---
    PlayerReport "*" --> "1" ReportType : type
    PlayerReport "*" --> "1" ReportPriority : priority
    ReportCreationManager --> SkillIssueAdmin : usa dataFolder/logger
    ReportPlayer --> ReportCreationManager : verifica cooldown
    ReportPlayer ..> ReportMenu : abre
    ReportPlayer ..> ReportTypeMenu : abre
    ReportAdminCommand --> ReportCreationManager : consulta
    ReportAdminCommand ..> ReportAdminMenu : abre
    ReportMenuListener --> SkillIssueAdmin : usa
    ReportMenuListener --> ReportCreationManager : envia/actualiza
    ReportMenuListener ..> ReportMenu : maneja
    ReportMenuListener ..> ReportTypeMenu : maneja
    ReportMenuListener ..> ReportAdminMenu : maneja
    ReportAdminMenu --> PlayerReport : lista
    ReportMenu ..> MenuItems : usa
    ReportTypeMenu ..> MenuItems : usa
    ReportAdminMenu ..> MenuItems : usa
```
