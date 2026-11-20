# Registro de decisiones del proyecto "Skill Issue Admin"

Cada decisión de diseño se registra con el formato visto en clase: **Decisión · Alternativas · Por qué · Consecuencia**.

| Versión | Entrega | Decisiones |
| :---: | :---: | :--- |
| v1 | Entrega 1 | Idea inicial, DEC 001, DEC 002 |
| v2 | Entrega 2 | DEC 003 |
| v3 | Entrega 3 | DEC 004 a DEC 018 |

---

# Registro de decisiones v1

Primero pensamos en hacer un mod para el uso de redstone, o una herramienta educativa para evitar la adicción de los jóvenes a las redes sociales. Al ver la dificultad, hicimos una lluvia de ideas con Claude (IA) y decidimos hacer un proyecto para los **servidores de Minecraft**. Notamos una gran carencia en la administración de los servidores: el manejo de usuarios, prioridades, compras y baneos.

## DEC 001 - Cambio de Mod a Plugin
**Fecha:** 22-09-2026 · **Estado:** Aprobado

**Contexto:** En la primera reunión analizamos que un mod, pensado para servidores públicos, obligaría a que cada jugador lo instalara en su cliente.

**Decisión:** Desarrollar un **plugin** de servidor que funcione con Minecraft vanilla, usando el ecosistema Paper/Spigot.

**Razones:**
- Las funciones del proyecto (reportes, sanciones, rangos, historial) son lógica del servidor; no necesitan modificar el cliente.
- Es más simple de distribuir: el jugador entra sin instalar nada.

**Consecuencias:** El proyecto usa un entorno de desarrollo en Gradle (Kotlin DSL) y está escrito en Java.

## DEC 002 - Separar el código en archivos
**Fecha:** 22-09-2026 · **Estado:** Aprobado

**Contexto:** Trabajar todo en un mismo archivo es tedioso y hace que los programadores se confundan y cometan más errores.

**Decisión y consecuencias:** En lo posible, una clase por archivo y una responsabilidad por clase. Así es más fácil llevar el orden, repartir el trabajo y revisar los cambios de cada integrante.

---

# Registro de decisiones v2

## DEC 003 - Usar PaperMC como API
**Fecha:** 12-10-2026 · **Estado:** Aprobado

**Contexto:** Paper ofrece una API más sencilla y mejor optimizada que Bukkit o Spigot. Ya trae interfaces para registrar comandos y escuchar eventos, y con Gradle permite levantar un servidor de prueba.

**Decisión y consecuencias:** Usamos la plantilla de PaperMC y la subimos al repositorio. Hicimos plugins de prueba con funciones básicas para familiarizarnos con el entorno mientras definíamos el diagrama UML.

---

# Registro de decisiones v3

## DEC 004 - `Sancion` es una clase abstracta con tres subclases
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `Sancion` es abstracta, con el método abstracto `aplicarEfecto(Jugador)`. `Baneo`, `Mute` y `Advertido` la extienden. |
| **Alternativas** | Una interfaz `Sancion` · una sola clase concreta con un atributo `tipo` y un `switch`. |
| **Por qué** | Las tres comparten estado (número, jugador, admin, motivo, fecha, si está activa, motivo del perdón) y lógica común (`revocar()`, `estaVigente()`, guardar y leer de texto). Difieren en el efecto: el baneo expulsa, el mute avisa del silencio y la advertencia muestra un título con sonido. Una interfaz obligaría a repetir el estado en cada clase; un atributo `tipo` llenaría el código de `if`/`switch`. |
| **Consecuencia** | `GestorSanciones` trabaja con referencias `Sancion` y llama a `aplicarEfecto()` sin preguntar el tipo (polimorfismo de inclusión). Agregar una sanción nueva, como `Kick`, no modifica el gestor. |

## DEC 005 - Persistencia detrás de la interfaz `IPersistencia`
**Fecha:** 20-11-2026 · **Estado:** Aprobado (corrección del profesor)

| | |
| :--- | :--- |
| **Decisión** | La interfaz `IPersistencia` define el contrato (`guardarDatos`, `cargarDatos`, `verificarArchivo`) y `PersistenciaArchivo` lo implementa con archivos planos. Cada gestor recibe una `IPersistencia` en el constructor, carga sus datos al crearse y guarda después de cada cambio. |
| **Alternativas** | Que cada gestor abra y escriba sus propios archivos · usar una base de datos (SQLite). |
| **Por qué** | Separa la lógica de negocio del formato de los archivos. Es lo que pidió el profesor en la corrección: una clase de persistencia dedicada e intercambiable. La persistencia es una capacidad, no un linaje, así que encaja como interfaz (clase de Interfaces). |
| **Consecuencia** | Para pasar a una base de datos basta crear otra clase que implemente `IPersistencia`; los gestores no cambian. Los cuatro gestores (`GestorJugadores`, `GestorSanciones`, `ReportCreationManager`, `GestorTransacciones`) usan la misma instancia. |

## DEC 006 - Archivos `.txt` con una línea por objeto y separador `;`
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | Guardar todo en `rangos.txt`, `jugadores.txt`, `sanciones.txt`, `reportes.txt`, `donaciones.txt` y `compras.txt`, con una línea por objeto. Cada entidad sabe convertirse a texto (`toLinea()`) y reconstruirse (`desdeLinea()`). |
| **Alternativas** | Seguir con los YAML de Bukkit (`rangos.yml`, `sanciones.yml`, `reports.yml`) · serialización binaria de Java. |
| **Por qué** | El curso pide **archivos planos**. El texto se puede leer y revisar a mano. Si una línea se daña, solo se pierde esa línea: se omite, se avisa en la consola y el resto carga (RNF-02). |
| **Consecuencia** | Hay que limpiar el `;` de los textos que escriben los usuarios (se reemplaza por `,`). Los datos de las versiones anteriores en YAML se pasan automáticamente a `.txt` la primera vez, sin duplicar nada. |

## DEC 007 - `ComandoBase`: clase abstracta para todos los comandos
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `ComandoBase` (abstracta) implementa la interfaz `BasicCommand` de Paper. Su método `execute()` es `final`: valida si lo usa un jugador y los permisos, y luego llama al método abstracto `ejecutarComando()`. `ComandoAdministrativo` la extiende y solo cambia la regla de permisos (rango ADMIN). |
| **Alternativas** | Que cada comando implemente `BasicCommand` y repita las validaciones · la clase `BasicCommand` propia con `ejecutar(...)` del diagrama v1, que tenía parámetros distintos en cada comando. |
| **Por qué** | En la v1 cada comando tenía un `ejecutar` con parámetros diferentes, y al revisarlo nos dimos cuenta de que eso no era polimorfismo, porque no se podían tratar todos igual. Con una sola firma, `SkillIssueAdmin` registra todos los comandos de la misma manera. Además se elimina el código repetido de "¿es jugador?" y "¿es ADMIN?" que estaba en cada comando. |
| **Consecuencia** | Un comando nuevo solo implementa `ejecutarComando()` y, si hace falta, sobrescribe `tienePermiso()` o `requiereJugador()` (por ejemplo, `/profile` se puede usar desde la consola). |

## DEC 008 - Menús de inventario como interfaz gráfica
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | La interfaz gráfica son **menús de inventario** (clases que implementan `InventoryHolder`, como `ReportAdminMenu`, `BanMenuViews` y `RankMainMenu`), manejados por listeners de eventos. |
| **Alternativas** | Solo comandos de texto · ventanas Swing/JavaFX · un mod de cliente con pantallas propias. |
| **Por qué** | Minecraft vanilla no permite abrir ventanas propias en el cliente, y un mod contradice la DEC 001. Los inventarios son la GUI nativa que todo jugador ya sabe usar. Implementar `InventoryHolder` permite saber qué menú se clicó con `instanceof`, en un solo punto por listener. |
| **Consecuencia** | Los eventos de clic (`InventoryClickEvent`) cubren el **mouse**: clic izquierdo, derecho y con Mayús. Las respuestas escritas en el chat (`AsyncChatEvent`) y el autocompletado con Tab cubren el **teclado**. Los listeners solo traducen el clic y **delegan** la operación al gestor; no tienen reglas de negocio ni escriben archivos. |

## DEC 009 - Estados del reporte como `enum` con sus transiciones
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `EstadoReporte` es un `enum` (ABIERTO, EN_REVISION, RESUELTO, DESCARTADO) con el método `puedePasarA()`. `PlayerReport.cambiarEstado()` lanza `IllegalStateException` si el cambio no se permite. |
| **Alternativas** | Un `boolean resuelto` (como estaba antes) · un `String estado`. |
| **Por qué** | El RF-06 exige cuatro estados en orden y que un reporte cerrado no se reabra. Con un `boolean` no caben cuatro estados; con un `String` se puede escribir cualquier valor. El `enum` solo permite valores válidos y concentra la regla en un solo lugar. |
| **Consecuencia** | El menú de `/gestreports` y el comando de filtros usan la misma regla. Antes, un clic derecho podía reabrir un reporte; ahora muestra "no se puede reabrir". |

## DEC 010 - `CriterioFiltro` como clase anidada estática
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `CriterioFiltro` (estado, fecha desde, fecha hasta y el método `coincide()`) es una clase **estática anidada** dentro de `ReportCreationManager`. `filtrarReportes` tiene dos versiones (sobrecarga): con un `String` de estado o con un `CriterioFiltro`. |
| **Alternativas** | Un método `filtrarReportes(estado, desde, hasta)` con muchos parámetros sueltos · `CriterioFiltro` como clase aparte. |
| **Por qué** | Agrupa los filtros en un objeto fácil de leer y extender. Solo la usa el gestor de reportes, así que anidarla deja claro a quién pertenece y no agrega otro archivo que nadie más usa (clase de Clases anidadas). |
| **Consecuencia** | Se crea con `new ReportCreationManager.CriterioFiltro(...)`. Agregar un filtro nuevo (por ejemplo, por tipo) solo cambia esta clase. |

## DEC 011 - Número corto con contador estático, más un UUID interno
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | Las sanciones y los reportes tienen un **número corto** (#1, #2…) que sale de un contador `static` (`getSiguienteId()`) y, además, un `UUID` interno. |
| **Alternativas** | Solo `UUID` · solo un número entero. |
| **Por qué** | Los admins necesitan escribir algo corto, como `/perdonar 5`. El `UUID` evita choques al pasar datos antiguos y entre archivos. El contador es de la clase, así que todas las instancias lo comparten y no se repite. |
| **Consecuencia** | Al cargar los archivos, el contador se ajusta al número más alto guardado; al reiniciar el servidor no se repiten números. |

## DEC 012 - Reincidencia y periodo de prueba en `GestorSanciones` con el `enum` `NivelSeveridad`
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | La escala Mute → Baneo temporal → Baneo permanente es el `enum` `NivelSeveridad` (con `subir()`). Las reglas del RF-07 (30 días de prueba tras un perdón) y del RF-08 (3 sanciones en 60 días) viven en `GestorSanciones.calcularNivelSugerido()`, que devuelve un `record SugerenciaSeveridad` con el nivel y las razones. |
| **Alternativas** | Poner los `if` de reincidencia dentro de cada comando · guardar la severidad como texto. |
| **Por qué** | Es una regla de negocio, así que va en el gestor y no en la interfaz (la rúbrica penaliza la lógica dentro de los eventos). Así `/mute` y el menú de `/ban` aplican exactamente la misma regla. |
| **Consecuencia** | Un mute de un reincidente sube solo a baneo temporal (3 días) o permanente, y el admin ve el motivo. Las constantes `PERIODO_PRUEBA_DIAS`, `VENTANA_REINCIDENCIA_DIAS` y `UMBRAL_REINCIDENCIA` se cambian en un solo lugar. |

## DEC 013 - Excepciones personalizadas para errores de negocio
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `SancionInvalidaException` y `JugadorNoEncontradoException` extienden `Exception` (checked). Para validaciones simples usamos `IllegalArgumentException` (dato inválido) e `IllegalStateException` (operación no permitida en ese momento). |
| **Alternativas** | Que todo sea `RuntimeException` · devolver `boolean` o códigos de error. |
| **Por qué** | Al ser checked, el compilador obliga a manejar los errores de negocio más importantes, y cada comando los captura para mostrar un mensaje claro en rojo. Devolver `false` no dice qué salió mal. (Clase de Excepciones: checked frente a unchecked, `throw` frente a `throws`.) |
| **Consecuencia** | `CompraRango.procesarCompra()` usaba `SancionInvalidaException` para un pago insuficiente; lo cambiamos a `IllegalArgumentException`, porque un pago no es una sanción. |

## DEC 014 - Un solo `ModeracionListener` para aplicar las sanciones en tiempo real
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `ModeracionListener` bloquea el chat de los muteados (`AsyncChatEvent`) y la entrada de los baneados (`AsyncPlayerPreLoginEvent`), preguntándole a `GestorSanciones`. |
| **Alternativas** | Un listener por tipo de sanción (antes existían `MuteEnforcementListener` y `BanEnforcementListener`) · revisar las sanciones dentro de cada comando. |
| **Por qué** | Centraliza en una clase todo lo que hace cumplir las sanciones. Usamos los eventos actuales de Paper en lugar de `AsyncPlayerChatEvent` y `PlayerLoginEvent`, que están obsoletos. |
| **Consecuencia** | Se eliminaron los dos listeners duplicados. Un tipo de sanción nuevo que afecte al jugador en tiempo real se agrega en un solo lugar. |

## DEC 015 - Rango EXCLUSIVO como valor del `enum` `TipoRango`
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | Los rangos de pago son de tipo `TipoRango.EXCLUSIVO`, con permisos solo cosméticos. `Rango.esPago()` se calcula a partir del tipo. |
| **Alternativas** | Un atributo `boolean esPago` aparte en `Rango` · una subclase `RangoPago`. |
| **Por qué** | Los permisos ya dependían del tipo de rango. Con un `boolean` aparte se podría marcar un rango ADMIN como "de pago", y el RF-12 exige que un rango exclusivo **nunca** tenga permisos de staff. Una subclase sería herencia sin especialización real. |
| **Consecuencia** | `tienePermisosElevados()` solo es verdadero para ADMIN, OP y MOD. `/comprarango` solo acepta rangos EXCLUSIVOS, y `/subirrango` no permite subir gratis a uno. |

## DEC 016 - Paquetes por módulo con capas separadas dentro
**Fecha:** 20-11-2026 · **Estado:** Aprobado, con una mejora pendiente

| | |
| :--- | :--- |
| **Decisión** | Los paquetes están organizados por módulo (`Rank`, `Report`, `Sanctions`, `listeners`). Dentro de cada uno, las clases están separadas por responsabilidad: menús y listeners (interfaz), gestores y entidades (negocio) y persistencia. |
| **Alternativas** | Paquetes por capa (`gui`, `negocio`, `persistencia`). |
| **Por qué** | Cada integrante desarrolló un módulo; así cada uno trabajó en su carpeta y hubo menos conflictos al integrar en Git. |
| **Consecuencia** | La interfaz y la persistencia quedaron dentro del paquete `Rank`, porque ahí empezó la persistencia de rangos. **Mejora pendiente:** moverlas a un paquete `persistencia` propio. |

## DEC 017 - `enum` para los catálogos fijos
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | `CausaBaneo`, `DuracionBaneo`, `ReportType`, `ReportPriority`, `TipoRango`, `EstadoReporte` y `NivelSeveridad` son `enum`. |
| **Alternativas** | Constantes `String` o `int`. |
| **Por qué** | Solo permiten valores válidos, pueden tener atributos y métodos propios (etiqueta, ícono, `calcularExpiracion()`, `subir()`, `puedePasarA()`) y el compilador avisa si un `switch` no cubre un valor. |
| **Consecuencia** | Al agregar `TipoRango.EXCLUSIVO`, el compilador señaló todos los `switch` que había que actualizar. |

## DEC 018 - `record` para datos auxiliares
**Fecha:** 20-11-2026 · **Estado:** Aprobado

| | |
| :--- | :--- |
| **Decisión** | Los datos que solo se pasan de un lado a otro sin comportamiento son `record`: `PersistenciaDatos`, `GestorSanciones.SugerenciaSeveridad`, `BanMenuListener.PendingChat`, `RankMenuListener.PendingCreation` y `RankDisplayManager.Binding`. |
| **Alternativas** | Clases normales con atributos, constructor y getters. |
| **Por qué** | Son inmutables y cortos, y el compilador genera el constructor, los getters, `equals` y `toString`. Los que solo usa una clase van anidados dentro de ella. |
| **Consecuencia** | Menos código repetido. Las entidades con reglas (por ejemplo `Jugador` y `Sancion`) siguen siendo clases normales, porque su estado cambia con validaciones. |
