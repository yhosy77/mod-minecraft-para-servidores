# Skill Issue Admin — Plugin de moderación para servidores de Minecraft

**Grupo:** POO - Error Capa 8 · Programación Orientada a Objetos · Universidad de La Sabana
**Repositorio:** https://github.com/yhosy77/mod-minecraft-para-servidores

**Nombre del proyecto.** "Skill Issue" es una muletilla humorística de la comunidad de Minecraft para referirse a la "falta de habilidad". En este caso, la falta de habilidad de un admin para controlar el caos de los baneos, los rangos y los reportes.

## Contenido

1. [Descripción del problema](#1-descripción-del-problema)
2. [Requerimientos](#2-requerimientos)
3. [Cómo ejecutar el plugin](#3-cómo-ejecutar-el-plugin)
4. [Comandos del plugin](#4-comandos-del-plugin)
5. [Arquitectura y organización del código](#5-arquitectura-y-organización-del-código)
6. [Persistencia en archivos planos](#6-persistencia-en-archivos-planos)
7. [Temas del curso aplicados en el código](#7-temas-del-curso-aplicados-en-el-código)
8. [Matriz de trazabilidad](#8-matriz-de-trazabilidad)
9. [API usada: PaperMC](#9-api-usada-papermc)
10. [Equipo y roles](#10-equipo-y-roles)
11. [Documentos del proyecto](#11-documentos-del-proyecto)

---

## 1. Descripción del problema

### Contexto
El proyecto se ubica en la administración de **servidores multijugador de Minecraft Java**. En ellos coinciden decenas o cientos de jugadores en tiempo real. Para que la convivencia sea sana y el juego sea justo, las comunidades necesitan moderar comportamientos tóxicos, gestionar rangos y permisos y llevar un control ordenado de las sanciones.

### Actores
* **Jugador:** juega, interactúa, reporta a otros jugadores, consulta su perfil, sube de rango y puede donar.
* **Administrador / Staff:** atiende los reportes, aplica y revoca sanciones (baneos, mutes, advertencias), asigna rangos y registra compras.
* **Sistema (Skill Issue Admin):** el plugin en Java que procesa los comandos y los menús, valida las reglas de negocio, guarda los datos y aplica las restricciones automáticas (bloquear el chat o la entrada).

### Proceso actual
En muchos servidores pequeños y medianos la moderación se hace a mano:
1. El jugador que ve una infracción avisa por el chat general o abre un ticket en un canal de Discord externo.
2. Los administradores anotan las sanciones en blocs de notas, hojas de texto o mensajes sueltos.
3. Los baneos y mutes temporales se manejan a mano o con comandos aislados (`/ban`, `/mute`), sin un historial unificado.

### El dolor
* **Las faltas se olvidan.** Si a un jugador lo atienden admins distintos, nadie sabe que es reincidente y recibe castigos leves una y otra vez.
* **Mucho trabajo manual.** Anotar los baneos, calcular cuánto dura cada castigo y acordarse de quitarlo a tiempo quita tiempo y deja espacio para errores o injusticias.
* **Los reportes se pierden.** Los jugadores reportan sin saber si alguien los atendió, y los dueños del servidor no pueden revisar el trabajo de los admins.

### Impacto de la solución
* **Técnico:** cambia las notas sueltas por un sistema que guarda todo automáticamente en archivos y no pierde datos al reiniciar el servidor.
* **Económico:** lleva el registro de las compras de rangos exclusivos y de las donaciones, con fecha y valor.
* **Social:** las sanciones siguen reglas claras y automáticas: la reincidencia sube la severidad y quien es perdonado queda en un periodo de prueba de 30 días.

---

## 2. Requerimientos

La matriz completa con actor, prioridad y criterio de aceptación está en [Plantilla de requerimientos.md](Plantilla%20de%20requerimientos.md). Resumen:

| ID | Requerimiento | Estado |
| :---: | :--- | :---: |
| RF-01 | El jugador reporta a otro jugador con un motivo (no permite reportes vacíos ni auto-reportes). | Implementado |
| RF-02 | El administrador registra jugadores con NickName, nivel, monedas y fecha de ingreso (NickName de 3 a 16 caracteres y único). | Implementado (sin el campo *skin*) |
| RF-03 | El administrador aplica un baneo o un mute con tipo de infracción, motivo, duración y staff responsable. | Implementado |
| RF-04 | El administrador consulta el historial completo de sanciones de un jugador. | Implementado |
| RF-05 | El administrador asigna o cambia el rango de un jugador. | Implementado |
| RF-06 | El administrador atiende los reportes: Abierto → En revisión → Resuelto o Descartado, sin reabrir los cerrados. | Implementado |
| RF-07 | Revocar una sanción con justificación deja al jugador "Advertido" 30 días; si reincide en lo mismo, la sanción sube un nivel. | Implementado |
| RF-08 | Con 3 o más sanciones no revocadas en 60 días, la severidad sugerida sube un nivel (Mute → Baneo temporal → Baneo permanente). | Implementado |
| RF-09 | El administrador filtra los reportes por estado o por rango de fechas. | Implementado |
| RF-10 | El administrador ve cuántas sanciones aplicó y cuántos reportes atendió cada admin en un periodo. | Implementado |
| RF-11 | El jugador sube gratis al rango Miembro con nivel 10 o 500 monedas (las monedas se descuentan). | Implementado |
| RF-12 | El administrador registra la compra de un rango exclusivo (fecha, valor y rango); el rango nunca da permisos de staff. | Implementado |
| RF-13 | El jugador registra donaciones sin recibir recompensas; el admin consulta el total por periodo. | Implementado |
| RNF-01 | La información se guarda en archivos planos entre ejecuciones y, si un archivo no existe, se crea sin error. | Implementado |
| RNF-02 | Ante datos inválidos se muestra un mensaje claro; una línea dañada en un archivo se omite y se informa. | Implementado |

---

## 3. Cómo ejecutar el plugin

**Requisitos:** Java 25 y Paper 26.3. El proyecto usa Gradle con Kotlin DSL; el plugin `run-paper` descarga el servidor automáticamente.

1. Abrir la carpeta del repositorio en **IntelliJ IDEA** (con el plugin *Minecraft Development* o como proyecto Gradle).
2. Ejecutar la tarea de Gradle **`runServer`** (*Gradle → Tasks → run paper → runServer*). Compila el plugin, levanta un servidor Paper 26.3 en la carpeta `run/` y lo carga.
3. Entrar al servidor desde Minecraft Java en `localhost`.
4. Darse permisos: en la consola del servidor, `op <tu_nick>`. Un jugador con OP recibe el rango **ADMIN** la primera vez que entra.

Para generar el `.jar` se usa la tarea **`build`**; el archivo queda en `build/libs/`.
El repositorio no incluye el script `gradlew`, así que desde consola hace falta tener Gradle instalado.

**Archivos que crea el plugin** (en `run/plugins/skill-issue-admin/`): `rangos.txt`, `jugadores.txt`, `sanciones.txt`, `reportes.txt`, `donaciones.txt` y `compras.txt`.

---

## 4. Comandos del plugin

| Comando | Quién | Qué hace | RF |
| :--- | :---: | :--- | :---: |
| `/report [jugador] [motivo]` | Jugador | Sin argumentos abre el menú de jugadores; con jugador abre el menú de motivos; con motivo crea el reporte directo. | RF-01 |
| `/gestreports` | Admin | Abre la bandeja gráfica de reportes (prioridad y estados con clics). | RF-06 |
| `/gestreports estado <estado>` | Admin | Lista los reportes de un estado: `abierto`, `en_revision`, `resuelto` o `descartado`. | RF-09 |
| `/gestreports fechas <desde> <hasta> [estado]` | Admin | Lista los reportes entre dos fechas (`aaaa-mm-dd`), incluidas ambas. | RF-09 |
| `/ban` | Admin | Asistente gráfico de baneo: jugador → causa → duración → mensaje → confirmar. | RF-03 |
| `/mute <jugador> <minutos> <tipo> <motivo>` | Admin | Silencia al jugador. Tipo: `insultos`, `trampas`, `spam`, `toxicidad`, `exploits` u `otra`. Si reincide, sube a baneo. | RF-03, RF-07, RF-08 |
| `/warn <jugador> <severidad 1-3> <motivo>` | Admin | Advertencia con título en pantalla y sonido. | RF-03 |
| `/sanciones <jugador>` | Admin | Historial de sanciones, estado actual y reincidencia en 60 días. | RF-04, RF-08 |
| `/perdonar <número> <justificación>` | Admin | Revoca una sanción; el jugador queda Advertido 30 días. | RF-07 |
| `/rank` | Admin | Menú gráfico para crear, eliminar, colorear y asignar rangos. | RF-05 |
| `/jugador registrar <nick> [nivel] [monedas]` | Admin | Registra un jugador nuevo con sus datos base. | RF-02 |
| `/jugador monedas <nick> <cantidad>` | Admin | Ajusta las monedas de un jugador. | RF-02 |
| `/profile [jugador]` | Todos | Perfil: rango, nivel, monedas, horas, fecha de ingreso, estado y sanciones. | RF-02, RF-03 |
| `/subirrango` | Jugador | Sube a Miembro si cumple nivel 10 o 500 monedas; si no, dice lo que le falta. | RF-11 |
| `/comprarango <jugador> <rango> <valor>` | Admin | Registra la compra de un rango EXCLUSIVO y se lo asigna. | RF-12 |
| `/donar <monto> [mensaje]` | Jugador | Registra una donación (no da recompensas). | RF-13 |
| `/donaciones [días]` | Admin | Total donado en el periodo (30 días por defecto). | RF-13 |
| `/estadisticas [días]` | Admin | Sanciones aplicadas y reportes atendidos por cada admin. | RF-10 |

Además, sin comando: el **chat de los muteados se bloquea**, los **baneados no pueden entrar** y el **nivel y las horas jugadas** se actualizan solos al entrar y salir.

---

## 5. Arquitectura y organización del código

Todo el código está en `src/main/java/ErrorCapa8/skillIssueAdmin/`. Está organizado **por módulo**, y dentro de cada módulo cada clase cumple una sola responsabilidad: interfaz gráfica, lógica de negocio o persistencia (ver la DEC 016 en el registro de decisiones).

| Paquete | Contenido |
| :--- | :--- |
| raíz | `SkillIssueAdmin` (clase principal), `ComandoBase` y `ComandoAdministrativo` (base de los comandos), excepciones personalizadas, `Donacion`, `CompraRango`, `GestorTransacciones` y sus comandos, `EstadisticasCommand`. |
| `Rank` | Jugadores y rangos: `Jugador`, `Rango`, `TipoRango`, `GestorJugadores`, la **persistencia** (`IPersistencia`, `PersistenciaArchivo`, `PersistenciaDatos`), los menús de `/rank` y los comandos `/profile`, `/jugador` y `/subirrango`. |
| `Report` | Reportes: `PlayerReport`, `EstadoReporte`, `ReportType`, `ReportPriority`, el gestor `ReportCreationManager` (con la clase anidada `CriterioFiltro`), los menús y los comandos `/report` y `/gestreports`. |
| `Sanctions` | Sanciones: `Sancion` (abstracta), `Baneo`, `Mute`, `Advertido`, `NivelSeveridad`, `GestorSanciones`, el asistente gráfico de `/ban` y los comandos de sanciones. |
| `listeners` | `ModeracionListener` (aplica las sanciones en tiempo real) y `connectionListener` (bienvenida). |

### Las tres capas

| Capa | Clases | Regla |
| :--- | :--- | :--- |
| **Interfaz gráfica y eventos** | Menús (`ReportMenu`, `ReportAdminMenu`, `BanMenuViews`, `RankMainMenu`…), listeners (`ReportMenuListener`, `BanMenuListener`, `RankMenuListener`, `ModeracionListener`) y comandos (`*Command`). | Muestran datos y reciben clics o texto, y le **delegan** la operación al gestor. No guardan archivos. |
| **Lógica de negocio** | Entidades (`Jugador`, `Rango`, `Sancion` y sus hijas, `PlayerReport`, `Donacion`, `CompraRango`) y gestores (`GestorJugadores`, `GestorSanciones`, `ReportCreationManager`, `GestorTransacciones`). | Tienen las reglas: validaciones, estados, reincidencia, requisitos de Miembro. Guardan las colecciones en `ArrayList`/`Map`. |
| **Persistencia** | `IPersistencia` (contrato) y `PersistenciaArchivo` (archivos planos). | Leen y escriben archivos. Los gestores solo conocen la interfaz, así que se puede cambiar la implementación sin tocar el negocio. |

### Flujo de una operación (ejemplo: `/mute`)
1. Paper llama a `ComandoBase.execute()`, que verifica que lo use un jugador y que sea ADMIN (`ComandoAdministrativo.tienePermiso()`).
2. `MuteCommand.ejecutarComando()` lee los argumentos y le pide a `GestorSanciones.calcularNivelSugerido()` la severidad según la reincidencia.
3. Se crea un `Mute` (o un `Baneo`, si subió de nivel) y se llama a `GestorSanciones.aplicarSancion()`.
4. El gestor valida, lo agrega al historial, guarda `sanciones.txt` con `IPersistencia.guardarDatos()` y llama a `sancion.aplicarEfecto(jugador)`. Cada tipo de sanción hace algo distinto (polimorfismo).
5. Desde ese momento, `ModeracionListener` cancela los mensajes del jugador mientras el mute esté vigente.

---

## 6. Persistencia en archivos planos

### Clases de persistencia
| Clase | Rol |
| :--- | :--- |
| `Rank/IPersistencia` | **Interfaz** (contrato). Métodos genéricos `guardarDatos(archivo, lista)`, `cargarDatos(archivo)` y `verificarArchivo(archivo)`, y métodos específicos para rangos/jugadores (`cargar`, `guardar`) y para los datos antiguos (`cargarBaneos`, `cargarRegistrosSanciones`). |
| `Rank/PersistenciaArchivo` | **Implementación** con archivos planos: `PrintWriter`/`FileWriter` para escribir y `BufferedReader`/`FileReader` para leer, en UTF-8. Crea la carpeta si no existe y captura las `IOException`. |
| `Rank/PersistenciaDatos` | `record` que devuelve juntos los rangos, los jugadores y el rango predeterminado al cargar. |

Cada **gestor** recibe una `IPersistencia` en el constructor, **carga sus datos al crearse** y **guarda después de cada cambio**. Cada **entidad** sabe convertirse en una línea de texto y reconstruirse desde ella (`toLinea()`/`toString()` y `desdeLinea()`).

### Archivos
Todos usan `;` como separador y una línea por objeto. Si un texto escrito por un usuario trae `;`, se reemplaza por `,` para no dañar la línea.

| Archivo | Quién lo maneja | Formato de cada línea |
| :--- | :--- | :--- |
| `rangos.txt` | `GestorJugadores` → `PersistenciaArchivo.guardar()` | `PREDETERMINADO;id` y `RANGO;id;nombre;tipo;base;color;formatos` |
| `jugadores.txt` | `GestorJugadores` (`Jugador.toString()` / `Jugador.desdeLinea()`) | `uuid;nombre;rango;nivel;monedas;horas;fechaIngreso` |
| `sanciones.txt` | `GestorSanciones` (`Sancion.toLinea()` / `Sancion.desdeLinea()`) | `TIPO;número;uuid;jugadorId;jugador;adminId;admin;fecha;motivo;activa;motivoPerdon;datos propios del tipo;revocadaEn` |
| `reportes.txt` | `ReportCreationManager` (`PlayerReport.toLinea()` / `desdeLinea()`) | `número;id;reportanteId;reportante;reportadoId;reportado;tipo;motivo;fecha;prioridad;estado;atendidoPor;atendidoEn` |
| `donaciones.txt` | `GestorTransacciones` (`Donacion.toLinea()` / `desdeLinea()`) | `id;jugador;monto;mensaje;fecha;confirmada` |
| `compras.txt` | `GestorTransacciones` (`CompraRango.toLinea()` / `desdeLinea()`) | `id;jugador;rango;valor;fecha;completada` |

`Sancion.desdeLinea()` lee el `TIPO` de la línea y crea el objeto correcto (`Baneo`, `Mute` o `Advertido`). Por eso un solo archivo guarda las tres clases.

### Operaciones CRUD
| Entidad | Crear | Consultar | Actualizar | Eliminar |
| :--- | :--- | :--- | :--- | :--- |
| Jugador | `registrarJugador`, `registrarJugadorManual`, registro automático al entrar | `buscarJugador(id)`, `buscarJugador(nombre, exacto)`, `/profile` | `asignarRango`, `establecerMonedas`, `actualizarEstadisticasJuego`, `subirRangoGratuito` | `eliminarJugador` |
| Rango | `crearRango` (menú `/rank`) | `buscarRango`, `obtenerRangos` | `cambiarEstilo`, `establecerRangoPredeterminado` | `eliminarRango` |
| Sanción | `aplicarSancion`, `aplicarBaneo` | `obtenerSancionesPorJugador`, `buscarPorNumero` | `levantarSancion` (revocar) | No se borran: quedan en el historial para la reincidencia (RF-04, RF-08) |
| Reporte | `crearReporte` | `reports()`, `filtrarReportes`, `buscarReporte` | `resolverReporte`, `setPriority` | No se borran: quedan para las estadísticas (RF-10) |
| Donación / Compra | `registrarDonacion`, `registrarCompra` | `donacionesEntre`, `totalDonado`, `getCompras` | `procesarCompra` (marca completada) | No se borran: son registros de dinero |

### Datos antiguos y archivos dañados (RNF-01, RNF-02)
* **Si un archivo no existe**, `cargarDatos()` devuelve una lista vacía y el archivo se crea en el primer guardado.
* **Si una línea está dañada**, se omite y se avisa en la consola del servidor; el resto se carga normal. El servidor no se cae.
* **Migración:** las versiones anteriores guardaban en YAML (`rangos.yml`, `sanciones.yml`, `reports.yml`). Al arrancar, si el `.txt` todavía no existe, se leen esos archivos una sola vez y se pasan a `.txt` sin duplicar nada.

---

## 7. Temas del curso aplicados en el código

### 7.1 Clases y objetos
Las **entidades del dominio** (`Jugador`, `Rango`, `Sancion`, `Baneo`, `Mute`, `Advertido`, `PlayerReport`, `Donacion`, `CompraRango`) tienen estado propio (atributos) y comportamiento propio. Por ejemplo, `Jugador.subirRangoGratuito()`, `Sancion.revocar()` y `PlayerReport.cambiarEstado()`.

### 7.2 Comunicación entre objetos
Los objetos colaboran con métodos que reciben parámetros y devuelven resultados. Por ejemplo:
* `MuteCommand` pide a `GestorJugadores.buscarJugador(uuid)` el `Jugador`.
* Luego `GestorSanciones.calcularNivelSugerido(jugador, tipo, nivel)` devuelve una `SugerenciaSeveridad`.
* Con eso llama a `aplicarSancion(jugador, sancion)`, que a su vez invoca `sancion.aplicarEfecto(jugador)`.

### 7.3 Encapsulamiento
Todos los atributos son `private` (o `protected` cuando las subclases los necesitan). El estado solo cambia con métodos que validan reglas:
* `Sancion.revocar()` no deja revocar dos veces.
* `PlayerReport.cambiarEstado()` no deja saltarse estados ni reabrir.
* `Jugador.actualizarProgreso()` no acepta valores negativos.
* `Mute`, `Advertido`, `Donacion` y `CompraRango` validan sus datos en el constructor.

No hay setters sin validación sobre el estado importante.

### 7.4 Colecciones
`ArrayList` en los gestores: `historial` y `sancionesActivas` en `GestorSanciones`, `reports` en `ReportCreationManager`, `donaciones` y `compras` en `GestorTransacciones`. `LinkedHashMap` para buscar jugadores por UUID y rangos por id en `GestorJugadores`. Los getters devuelven **copias** de las listas, para que nadie las modifique desde afuera.

### 7.5 Herencia
* `Sancion` → `Baneo`, `Mute`, `Advertido`. Es una relación real de "es una": comparten id, jugador, admin, motivo, fecha y estado, y cada una agrega lo suyo (duración en días, minutos y canal, severidad).
* `ComandoBase` → `ComandoAdministrativo` → comandos de staff (`MuteCommand`, `BanCommand`, `RankCommand`…). `ComandoBase` → comandos de jugador (`ReportPlayer`, `ProfileCommand`, `SubirRangoCommand`, `DonarCommand`).
* Excepciones: `SancionInvalidaException` y `JugadorNoEncontradoException` extienden `Exception`.

### 7.6 Polimorfismo
* **Inclusión / sobrescritura:**
  * `GestorSanciones` trabaja con referencias `Sancion` y llama a `aplicarEfecto()`, `getTipo()`, `haExpirado()` y `getNivelEnEscala()` sin preguntar qué tipo es. `Baneo` expulsa, `Mute` avisa del silencio y `Advertido` muestra un título con sonido.
  * Los comandos se registran todos como `BasicCommand` y Paper llama a `execute()`. Ese método está en `ComandoBase` y cada subclase sobrescribe `ejecutarComando()` y, si hace falta, `tienePermiso()` o `requiereJugador()`.
* **Sobrecarga:** `buscarJugador(String id)` / `buscarJugador(String nombre, boolean exacto)`, `filtrarReportes(String estado)` / `filtrarReportes(CriterioFiltro)`, `levantarSancion(int)` / `levantarSancion(int, String)`, `obtenerSancionesPorJugador(String)` / `(UUID)`, `crearRango(...)` con dos firmas.
* **Por interfaz:** los gestores usan `IPersistencia` y no `PersistenciaArchivo`; los menús implementan `InventoryHolder`; los listeners implementan `Listener`.

**Ventaja:** para agregar una sanción nueva (por ejemplo `Kick`) solo hay que crear la subclase con su `aplicarEfecto()`. El gestor, el archivo y `/sanciones` funcionan sin cambios.

### 7.7 Clases abstractas e interfaces
* **Abstractas:**
  * `Sancion` tiene los métodos abstractos `aplicarEfecto(Jugador)`, `getTipo()` y `datosExtra()`.
  * `ComandoBase` tiene el método abstracto `ejecutarComando()` y define el flujo común en `execute()`, que es `final`.
  * `ComandoAdministrativo` es abstracta y solo cambia la regla de permisos.
* **Interfaces propias:** `IPersistencia`.
* **Interfaces de Paper/Bukkit que implementamos:** `BasicCommand` (comandos), `Listener` (eventos) e `InventoryHolder` (menús).
* **Por qué una y no la otra:** las sanciones comparten estado, así que son una clase abstracta. La persistencia es una capacidad que no depende de un linaje ("lo que sabe hacer"), así que es una interfaz.

### 7.8 Atributos y métodos estáticos
* **Contadores de id:** `Sancion.contadorSanciones` con `getSiguienteId()`, y `PlayerReport.contadorReportes` con `getSiguienteId()`. Al ser de la clase, todas las sanciones comparten el contador y los números (#1, #2…) no se repiten. Al cargar los archivos, el contador sube hasta el número más alto guardado.
* **Constantes:** `Jugador.NIVEL_MIN_MIEMBRO = 10` y `MONEDAS_MIN_MIEMBRO = 500`, `GestorSanciones.PERIODO_PRUEBA_DIAS = 30` y `VENTANA_REINCIDENCIA_DIAS = 60`. Son iguales para todos y se cambian en un solo lugar.
* **Métodos de fábrica:** `Sancion.desdeLinea()`, `PlayerReport.desdeLinea()`, `Jugador.desdeLinea()`, `EstadoReporte.desdeTexto()` y `GestorJugadores.validarConexionGlobal()`.

### 7.9 Clases anidadas
Usamos los cuatro tipos vistos en clase:
* **Estática anidada:**
  * `ReportCreationManager.CriterioFiltro`: agrupa estado y rango de fechas para `filtrarReportes`; solo la usa el gestor de reportes.
  * `BanMenuViews.PlayerSelect`, `ReasonSelect`, `DurationSelect`, `MessageSelect` y `Confirm`: una por pantalla del asistente de `/ban`.
* **Record anidado (estático):** `GestorSanciones.SugerenciaSeveridad`, `RankDisplayManager.Binding`, `RankMenuListener.PendingCreation` y `BanMenuListener.PendingChat`.
* **Enum anidado:** `RankListMenu.Mode` y `BanMenuListener.ChatStep`.
* **Clase anónima:** en `BanMenuListener.onChat()` se crea un `new Runnable() { ... }` para devolver el procesamiento al hilo principal del servidor.

### 7.10 Excepciones
* **Personalizadas (checked):**
  * `SancionInvalidaException`: sanción mal formada, repetida o ya revocada.
  * `JugadorNoEncontradoException`: búsqueda de un jugador que no existe.

  Al ser checked, el compilador obliga a manejarlas, y los comandos las capturan para mostrarle el mensaje al admin.
* **Unchecked para validaciones:** `IllegalArgumentException` (dato inválido) e `IllegalStateException` (operación no permitida en el estado actual, como reabrir un reporte).
* **`try/catch` en la persistencia:** se capturan `IOException`, `NumberFormatException` y otras al leer líneas dañadas. La línea se omite, se informa en la consola y el servidor sigue funcionando (RNF-02).
* **Pendiente de mejora:** quedan tres `catch` sin mensaje. Dos están en la lectura del YAML antiguo (`PersistenciaArchivo`) y uno en `Rango.parseColor()`; en los tres casos el valor inválido se reemplaza por uno por defecto (por ejemplo, color gris). Conviene agregarles un aviso en la consola.

### 7.11 Interfaz gráfica, eventos, mouse y teclado
Minecraft no permite abrir ventanas propias (Swing/JavaFX) en el cliente. La interfaz gráfica nativa de un plugin son los **menús de inventario** (ver la DEC 008).

| Concepto | Dónde |
| :--- | :--- |
| **Componentes de entrada** | Texto que el admin escribe en el chat cuando el menú lo pide: causa personalizada y mensaje del baneo (`BanMenuListener`), nombre y color RGB de un rango (`RankMenuListener`). También los argumentos de los comandos. |
| **Componentes de salida** | Mensajes de chat, títulos en pantalla y sonido de `Advertido`, descripciones (lore) de los ítems, prefijo de rango sobre el nombre (`RankDisplayManager`). |
| **Componentes mixtos** | Ítems que muestran información y además responden a clics: cada reporte en `ReportAdminMenu`, cada rango en `RankListMenu`. |
| **Componentes de acción** | Botones: confirmar o cancelar (barrera), página anterior y siguiente (flechas), opciones de causa y duración en `/ban`. |
| **Programación acción-evento** | `@EventHandler` en `ReportMenuListener`, `BanMenuListener`, `RankMenuListener` (`InventoryClickEvent`, `InventoryDragEvent`, `AsyncChatEvent`), `ModeracionListener` (`AsyncChatEvent`, `AsyncPlayerPreLoginEvent`) y `RegistroJugadorListener` (`PlayerJoinEvent`, `PlayerQuitEvent`). Los manejadores **delegan** al gestor (por ejemplo, `manager.resolverReporte(...)`). |
| **Mouse** | En `/gestreports`: clic izquierdo sube la prioridad, Mayús + clic izquierdo la baja, clic derecho avanza el estado y Mayús + clic derecho lo descarta. En el resto de menús, clic para elegir. |
| **Teclado** | Respuestas escritas en el chat dentro de los asistentes (y la palabra `cancelar` para abortar), y el autocompletado con **Tab** de `/report` y `/mute` (`suggest()`). |
| **Validación sincrónica** | Mientras se escribe: el autocompletado con Tab solo sugiere jugadores conectados y tipos de infracción válidos. Los menús solo muestran opciones válidas (por ejemplo, no te aparece tu propio nombre en `/report`). |
| **Validación asincrónica** | Al enviar: `ComandoBase` revisa jugador y permisos, y los gestores revisan las reglas (motivo vacío, duración ≤ 0, auto-reporte, NickName inválido, estado no permitido) y responden con un mensaje claro en rojo. El texto del chat llega en un hilo aparte (`AsyncChatEvent`) y se procesa luego en el hilo principal. |

### 7.12 Diseño UML
El diagrama de clases está en [Diagrama_UML.md](Diagrama_UML.md) (Mermaid) y coincide 1:1 con el código de la Entrega 3:
* Tiene un **diagrama general** con las clases principales.
* Tiene **8 diagramas detallados** por módulo, con todos los atributos, métodos, parámetros, visibilidades, estáticos y abstractos.
* Muestra todas las relaciones: herencia, implementación, asociación, agregación, clases anidadas y dependencias, incluidas las excepciones que lanza cada clase.

---

## 8. Matriz de trazabilidad

| Requerimiento | Clases involucradas | Temas del curso | Estado |
| :--- | :--- | :--- | :---: |
| RF-01 Reportar jugador | `ReportPlayer`, `ReportMenu`, `ReportTypeMenu`, `ReportMenuListener`, `ReportCreationManager`, `PlayerReport` | Eventos, validación, comunicación entre objetos | Implementado |
| RF-02 Registrar jugadores | `JugadorCommand`, `GestorJugadores`, `Jugador`, `RegistroJugadorListener` | Encapsulamiento, colecciones, excepciones | Implementado |
| RF-03 Aplicar baneo o mute | `BanCommand`, `BanMenuViews`, `BanMenuListener`, `MuteCommand`, `WarnCommand`, `GestorSanciones`, `Baneo`, `Mute`, `Advertido` | Herencia, clase abstracta, polimorfismo, clases anidadas | Implementado |
| RF-04 Historial de sanciones | `SancionesCommand`, `GestorSanciones` | Colecciones, sobrecarga | Implementado |
| RF-05 Asignar rango | `RankCommand`, menús `Rank*`, `GestorJugadores`, `Rango` | Eventos de mouse, encapsulamiento | Implementado |
| RF-06 Atender reportes | `ReportAdminMenu`, `ReportMenuListener`, `EstadoReporte`, `PlayerReport` | Enum con reglas, eventos de mouse, excepciones | Implementado |
| RF-07 Revocar y periodo de prueba | `PerdonarCommand`, `GestorSanciones`, `Sancion.revocar()` | Encapsulamiento, constantes estáticas | Implementado |
| RF-08 Reincidencia | `GestorSanciones`, `NivelSeveridad`, `MuteCommand` | Polimorfismo, records anidados | Implementado |
| RF-09 Filtrar reportes | `ReportAdminCommand`, `ReportCreationManager.CriterioFiltro` | Clase anidada estática, sobrecarga | Implementado |
| RF-10 Estadísticas del staff | `EstadisticasCommand`, `GestorSanciones`, `ReportCreationManager` | Colecciones, comunicación entre objetos | Implementado |
| RF-11 Subir a Miembro | `SubirRangoCommand`, `GestorJugadores`, `Jugador`, `Rango` | Constantes estáticas, encapsulamiento | Implementado |
| RF-12 Comprar rango exclusivo | `ComprarRangoCommand`, `GestorTransacciones`, `CompraRango`, `TipoRango` | Enum, persistencia, excepciones | Implementado |
| RF-13 Donaciones | `DonarCommand`, `DonacionesCommand`, `GestorTransacciones`, `Donacion` | Colecciones, persistencia | Implementado |
| RNF-01 Persistencia | `IPersistencia`, `PersistenciaArchivo` y los cuatro gestores | Interfaces, archivos planos, CRUD | Implementado |
| RNF-02 Datos inválidos | `ComandoBase`, gestores, `PersistenciaArchivo`, excepciones personalizadas | Excepciones, validación sincrónica y asincrónica | Implementado |

---

## 9. API usada: PaperMC

Elegimos **PaperMC** (construida sobre Spigot/Bukkit) entre Bukkit, Spigot, Sponge y Paper (ver la DEC 003):
* **Contratos por interfaces:** Paper define interfaces como `BasicCommand` (comandos), `Listener` (eventos) e `InventoryHolder` (menús). Nuestras clases las implementan sin acoplarse a la lógica interna del juego.
* **Polimorfismo en los comandos:** todos los comandos se registran igual con `registerCommand(nombre, comando)`, y nuestra clase abstracta `ComandoBase` centraliza las validaciones comunes.
* **API moderna:** usamos los eventos actuales (`AsyncChatEvent`, `AsyncPlayerPreLoginEvent`) y los textos de Adventure (`Component`) en lugar de las versiones obsoletas.
* **Herramientas:** Gradle con el plugin `run-paper` compila el plugin y levanta un servidor de prueba con una sola tarea.

---

## 10. Equipo y roles

Los roles rotan en cada entrega.

### Entrega 1
| Nombre | Rol | Responsabilidades |
| :--- | :---: | :--- |
| David Santiago Díaz | Producto (PO) | Cuidar el problema y validar que los RF respondan a las necesidades del proyecto. |
| Samuel Gacharna | Producto (PO) | Apoyo en la definición de requerimientos y criterios de aceptación. |
| Samuel Zona | Diseño (AR) | Liderar el diagrama de clases UML y el registro de decisiones de POO. |
| Juan Bello | Diseño (AR) | Garantizar que la arquitectura y las clases respeten el diseño. |
| Miguel Peña | Calidad (QA) | Definir criterios de aceptación, casos límite, pruebas y manejo de errores. |
| Juan Felipe Camacho Landazábal | Integración (GI) | Administrar el repositorio, las ramas, los PRs y la frecuencia de commits. |

### Entrega 2
| Nombre | Rol | Responsabilidades |
| :--- | :---: | :--- |
| David Santiago Díaz | Diseño (AR) | Liderar la arquitectura de clases, jerarquías, interfaces y el Registro de decisiones v2. |
| Juan Felipe Camacho Landazábal | Diseño (AR) | Diseñar el diagrama UML actualizado y la estructura del código en Java. |
| Samuel Zona | Producto (PO) | Validar que la implementación cumpla los requerimientos funcionales. |
| Juan Bello | Producto (PO) | Mantener la trazabilidad entre el problema, los requerimientos y los casos de uso. |
| Samuel Gacharna | Calidad (QA) | Diseñar las excepciones personalizadas, probar casos límite y verificar la persistencia. |
| Miguel Peña | Integración (GI) | Controlar el repositorio, verificar los commits de cada integrante y gestionar las ramas. |

### Entrega 3
| Nombre | Rol | Responsabilidades |
| :--- | :---: | :--- |
| David Santiago Díaz | Producto (PO) | Validar que el plugin cumpla los requerimientos (RF-01 a RF-13) y preparar la narrativa de la sustentación. |
| Juan Felipe Camacho Landazábal | Producto (PO) | Verificar el flujo de comandos y menús desde el punto de vista del jugador y del admin. |
| Samuel Zona | Calidad (QA) | Probar casos límite, verificar las excepciones personalizadas y la persistencia, y auditar la `Bitacora_IA.md`. |
| Samuel Gacharna | Integración (GI) | Administrar el repositorio, integrar los módulos de todos, revisar y fusionar los PRs y verificar que el proyecto compile limpio. |
| Juan Bello | Diseño (AR) | Consolidar el Diagrama de Clases UML y garantizar que coincida 1:1 con el código Java. |
| Miguel Peña | Diseño (AR) | Documentar el Registro de decisiones v3 (polimorfismo en `Sancion` y `ComandoBase`, desacoplamiento con `IPersistencia`, `CriterioFiltro`). |

### Módulos de código por integrante
| Integrante | Módulo |
| :--- | :--- |
| David Santiago Díaz | Sistema de reportes: `ReportCreationManager`, `CriterioFiltro`, `/report`, `/gestreports` |
| Juan Felipe Camacho Landazábal | Base de comandos y administración: `ComandoBase`, `/ban`, `/rank`, menús gráficos |
| Samuel Zona | Jugadores y rangos: `Jugador`, `Rango`, `/profile`, `/subirrango` |
| Samuel Gacharna | Sanciones: `Sancion`, `Baneo`, `Mute`, `Advertido`, `GestorSanciones` |
| Juan Bello | Persistencia: `IPersistencia`, `PersistenciaArchivo`, `GestorJugadores` |
| Miguel Peña | Excepciones, `ModeracionListener`, `Donacion`, `CompraRango` |

---

## 11. Documentos del proyecto

* [Registro_de_decisiones.md](Registro_de_decisiones.md): decisiones de POO, alternativas, por qué y consecuencias.
* [Diagrama_UML.md](Diagrama_UML.md): diagrama de clases.
* [Plantilla de requerimientos.md](Plantilla%20de%20requerimientos.md): matriz de requerimientos con criterios de aceptación.
* [Bitacora_IA.md](Bitacora_IA.md): qué le pedimos a la IA, qué respondió, qué aceptamos y qué corregimos.
