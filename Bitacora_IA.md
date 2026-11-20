Bitacora de IA del proyecto "Skill Issue Admin"

La idea de hacer un mod de minecraft fue propuesta por Felipe, pero tras su previa experiencia programando mods, y las complicaciones que le conllevó animar, un asistente de IA (claude) mandó algunas propuestas para darle un nuevo enfoque al proyecto sin la necesidad de agregar mobs (animales y mounstros), y fue discutida con el equipo hasta definir Esta idea del proyecto.

# Primer integrante
### Yo, David Santiago Díaz Lozano (Responsable de producto), hago reporte sobre mi uso de IA.
`Inteligencia Artificial Usada`: Gemini.

`Prompt`: *Una pregunta Gemini, si existen varios "Git hub", en qué se diferencian y por qué nunca había escuchado de Git lab y los otros, pero si he escuchado solo de Github? acaso los otros son tan malos que nunca se usan?*

**Respuesta:** La razón por la cual casi solo se escucha hablar de GitHub es porque se convirtió en el estándar de la comunidad global de desarrollo, los proyectos de código abierto (open source) y la formación académica.¿En qué se diferencian si todos usan Git?Todos estos servicios tienen el mismo motor técnico por debajo: Git. Los comandos de Git que ejecutas en la consola de tu computadora funcionan exactamente igual en todos ellos.   La diferencia no está en cómo guardas tu código, sino en la interfaz web, las herramientas adicionales que ofrecen y el público al que están dirigidos:   GitHub: Es el estándar para proyectos públicos, educación y la comunidad de desarrolladores. Funciona como una "red social" para programadores donde la gente comparte proyectos y arma su portafolio.GitLab: Es una de las alternativas más usadas en empresas medianas y grandes. Destaca por tener herramientas avanzadas de CI/CD (Integración y Despliegue Continuo) nativas y por permitir instalar la plataforma en servidores propios (self-hosted), algo crucial para la privacidad corporativa. En lugar de llamar a los cambios Pull Requests como en GitHub, los llama Merge Requests.   Bitbucket: Pertenece a la empresa Atlassian. Es extremadamente popular en empresas porque se integra de forma directa con herramientas de gestión de proyectos como Jira y Confluence.Azure DevOps: Es la plataforma de Microsoft integrada con su ecosistema de la nube para entornos corporativos.Codeberg: Una alternativa sin fines de lucro, alojada en Europa, pensada para quienes buscan una opción verdaderamente libre y centrada en la privacidad.   ¿Por qué casi solo se conoce GitHub si no estás en la industria?Efecto de red: Al concentrar la gran mayoría de bibliotecas y proyectos de código abierto del mundo, la comunidad de programadores converge naturalmente en GitHub.Uso académico y tutoriales: Como es la plataforma pública más grande, casi todos los cursos universitarios, bootcamps y guías de programación la eligen como plataforma predeterminada para enseñar.Portafolio profesional: En los procesos de contratación de programadores junior, es común mostrar la cuenta de GitHub como prueba de código enviado o proyectos realizados.

`Siguiente prompt`: *ok ok, yo estoy haciendo un proyecto junto con mis amigos para Programación Orientada a Objetos, y pues quería preguntarte si algunas de estas plataformas nos podrían dar una ventaja en nuestro proyecto que estamos haciendo en Github, sin perder el foco  obviamente. Es decir, que si me recomiendas hacer o utilizar una de estas herramientas para alguna cosa de nuestro proyecto, sin perder de vista que tenemos que finalizar nuestro proyecto.*

**Respuesta:** Para un proyecto universitario de Programación Orientada a Objetos (POO) donde la prioridad es la entrega y la calidad del código, la recomendación definitiva es no cambiar de plataforma y quedarse en GitHub.Migrar el repositorio a GitLab o Bitbucket en este momento no les dará una ventaja técnica directa sobre su proyecto, pero sí les sumaría una curva de aprendizaje y tiempo de configuración que podría restarles atención a lo realmente importante: la arquitectura de clases, el diseño y la entrega a tiempo.Sin embargo, sí pueden sacarle una ventaja gigantesca a GitHub utilizando herramientas que ya vienen integradas en la plataforma y que la mayoría de los estudiantes no aprovechan. Esto mejorará la coordinación de tu equipo y le dará una presentación impecable al proyecto frente a tu profesor:1. Organización del trabajo con GitHub Projects (Tablero Kanban)En proyectos de POO es muy común repartir el desarrollo por módulos, diagramas de clases o componentes.Ventaja: En lugar de coordinar por chat qué hace cada uno, usen la pestaña Projects dentro de su repositorio en GitHub.Cómo usarlo: Creen un tablero tipo Kanban sencillo con tres columnas: Por hacer, En progreso y Hecho. Asignen cada clase o requerimiento (ej. "Crear clase Usuario", "Implementar interfaz de la base de datos") a un integrante del grupo.2. Flujo de trabajo con Ramas (Branches) y Pull Requests (PR)Una de las mayores pérdidas de tiempo en proyectos grupales son los conflictos al fusionar el código (merge conflicts) o cuando alguien sobrescribe por error el trabajo de otro.Ventaja: Nadie debe subir código directamente a la rama principal (main).Cómo usarlo: Cada integrante crea una rama secundaria para su tarea (ej. feature/clase-estudiante), trabaja ahí, y al terminar abre un Pull Request para que otro compañero revise el código antes de unirlo a main. (Recordatorio: en GitLab esto se conoce como Merge Request, pero la lógica en GitHub es exactamente la misma).   3. Registro de errores con GitHub IssuesVentaja: Les permite rastrear errores (bugs) o pendientes sin olvidar nada.Cómo usarlo: Si durante la prueba del programa encuentran un fallo (ej. "El método calcularPromedio() arroja una excepción NullPointer"), abran un Issue, etiquétenlo como bug y asígnesenlo a quien escribió esa parte del código.4. Integración Continua con GitHub Actions (Opcional)Si en su curso de POO están realizando pruebas unitarias (como JUnit en Java o pytest en Python):Ventaja: Pueden configurar una GitHub Action muy básica (toma menos de 5 minutos usando las plantillas predeterminadas de GitHub).Cómo usarlo: Cada vez que alguien suba código o cree un Pull Request, GitHub ejecutará automáticamente las pruebas unitarias y les avisará si el nuevo código rompió alguna funcionalidad existente.


### Reporte de IA: Felipe Camacho

`Inteligencia Artificial Usada`: ChatGPT.

## 1. ¿El proyecto debería desarrollarse como un mod de Minecraft o como un plugin de servidor?

**Respuesta:**  
Las funcionalidades principales de *Skill Issue Admin* —reportes, sanciones, rangos, historial y persistencia— corresponden principalmente a lógica del servidor, por lo que un **plugin de Paper/Spigot** resulta más adecuado. Un mod sería necesario principalmente si se requirieran modificaciones del cliente o nuevas mecánicas, bloques, ítems o interfaces.

## 2. ¿Ha cambiado significativamente la forma de desarrollar plugins en versiones recientes de Minecraft?

**Respuesta:**  
Sí. Un cambio importante ocurrió en **Paper 1.20.5**, especialmente en el uso de mappings y acceso a internals/NMS. Sin embargo, la estructura tradicional basada en `JavaPlugin` y `plugin.yml` continúa siendo válida. Paper recomienda utilizar su API en lugar de depender directamente de NMS por problemas de compatibilidad entre versiones. 

## 3. ¿Es obligatorio utilizar Java 25 para el proyecto?

**Respuesta:**  
Solo si se utiliza **Minecraft 26.1 o superior**. Estas versiones requieren Java 25. Para mantener Java 21, que es la versión utilizada en clase, se puede trabajar con **Minecraft 1.21.11 + Paper**, que continúa utilizando Java 21. 

## 4. ¿Es obligatorio utilizar Kotlin para desarrollar el plugin?

**Respuesta:**  
No. Paper permite desarrollar plugins en Java, Kotlin, Groovy y Scala. El uso de `build.gradle.kts` corresponde al sistema de configuración de Gradle y **no significa que el código del plugin deba estar escrito en Kotlin**. El proyecto puede desarrollarse completamente en Java.

## 5. ¿Podemos hacer el proyecto como un mod utilizando únicamente Java?

**Respuesta:**  
Sí. Fabric permite desarrollar mods utilizando Java y, técnicamente, *Skill Issue Admin* podría implementarse como un mod server-side. Sin embargo, dado que los requerimientos principales corresponden a administración y lógica del servidor, un plugin de Paper sigue siendo una alternativa más directa para este proyecto.

Bitacora de IA de la entrega dos del proyecto "Skill Issue Admin":

## 

`Inteligencia Artificial Usada`: ChatGPT.

### 6. Propuesta para la gestión de reportes mediante interfaces gráficas

### Reporte de IA: Felipe Camacho

`Inteligencia Artificial Usada`: ChatGPT.

`Prompt`: *Dame una idea para este requerimiento que planteamos para que el server siga siendo "jugable" para los mods y que no les salgan chorrocientos reportes en pantalla a la vez: El sistema debe permitir al administrador atender los reportes recibidos, cambiando su estado hasta resolverlos o descartarlos. Tengo la idea de que se pueda abrir como un inventario y que se ordenen los jugadores por cantidad de reportes para que puedan ser atendidos por "prioridad"... qué tan viable es esa idea? Y que propondrías tu?*

**Respuesta:** La idea de utilizar un inventario gráfico para gestionar los reportes es viable y encaja con el objetivo del plugin: administrar la moderación sin interrumpir la experiencia de juego. Se propuso implementar un centro de reportes accesible mediante el comando `/reportes`, desde el cual los moderadores puedan consultar y gestionar los casos pendientes.

La interfaz principal podría representar a cada jugador reportado mediante su cabeza de Minecraft. La descripción del objeto mostraría la cantidad de reportes recibidos, los motivos y la hora del reporte más reciente. Los casos se organizarían por prioridad y se incluirían botones para navegar entre páginas.

Se propuso clasificar los reportes en tres niveles de prioridad:

- **Alta:** reportes urgentes o múltiples reportes recientes.
- **Media:** jugadores con varios reportes pendientes.
- **Baja:** reportes aislados sin indicios de urgencia.

Sin embargo, la cantidad de reportes no debe considerarse una prueba de culpabilidad. Por ejemplo, varios reportes coordinados contra un jugador inocente no deberían desplazar automáticamente un caso potencialmente más grave.

Al seleccionar un jugador, se abriría una segunda interfaz que permitiría consultar sus reportes individuales, incluyendo el motivo, el reportante, la hora y las evidencias disponibles. Desde esta interfaz, el moderador podría cambiar el estado del reporte a `EN_REVISION`, resolverlo o descartarlo si no procede.

Para evitar interrupciones durante el juego, también se propusieron las siguientes medidas:

1. **Notificaciones agrupadas:** evitar que aparezca un mensaje por cada reporte recibido; en su lugar, acumularlos en la interfaz.
2. **Notificaciones discretas:** mostrar mensajes breves, como «Hay 3 reportes pendientes», con la posibilidad de desactivarlos.
3. **Actualización de datos:** consultar los reportes más recientes al abrir la interfaz, evitando que el moderador tenga que reiniciar el menú para ver los cambios.

Para la primera versión se recomendó priorizar la creación del inventario de reportes pendientes, el ordenamiento por antigüedad y cantidad de reportes recientes, la interfaz de detalle, las acciones para revisar, resolver y descartar casos, y la persistencia de los reportes y sus estados después de reiniciar el servidor.

**Conclusión:** La propuesta es técnicamente viable mediante Paper/Bukkit y permite relacionar los requerimientos del plugin con los conceptos de la segunda entrega académica: entidades de negocio, colecciones, operaciones CRUD y persistencia. Se recomienda conservar la idea del inventario gráfico, complementándola con un sistema de prioridades y una interfaz para gestionar los reportes individuales. Los criterios definitivos deberán validarse con los requerimientos del proyecto antes de su implementación.

### Claude
Contexto del problema:

Al hacer merge de las ramas de todos los integrantes hacia main, surgieron conflictos de integración y errores de compilación. Teníamos varias clases con nombres de métodos distintos que intentaban conectarse entre sí e imports rotos. Además, estábamos bloqueados con la implementación del último requerimiento del proyecto, ya que no estábamos seguros de cómo abordarlo y a ninguno en el equipo se le ocurría una forma clara de estructurarlo sin romper lo que ya funcionaba.

Prompt / Consulta realizada a la IA:

"Estamos juntando las ramas de nuestro proyecto en equipo y el código no compila. Tenemos conflictos entre las clases de diferentes módulos porque cada uno nombró los métodos y parámetros de forma distinta, y además nos están saltando errores de referencia circular en los imports. Por otro lado, nos falta implementar el último requerimiento del proyecto pero estamos bloqueados y no sabemos cómo enfocarlo ni dónde conectarlo. ¿Cómo podemos conectar estas clases para que compilen bien sin rehacer todo y qué estructura nos recomiendas para implementar ese último requerimiento?"

Resumen de la respuesta obtenida:

La IA nos sugirió no modificar la lógica interna de cada integrante, sino crear una clase intermedia (tipo Controller o fachada) para adaptar los datos entre módulos y limpiar los imports cruzados. Para el último requerimiento, nos propuso una idea base sobre cómo dividirlo en pasos lógicos y en qué punto del flujo principal integrarlo para aprovechar las clases que ya teníamos creadas.

Aplicación en el proyecto (Lo que hicimos nosotros):

Lo que tomamos de la IA: Aplicamos la idea de la clase controladora para centralizar las llamadas entre módulos, seguimos su guía para resolver los errores de compilación y usamos su enfoque inicial como punto de partida para destrabarnos con el último requerimiento.

Lo que ajustamos manualmente: La IA dio una solución muy general tanto para la conexión como para el último requerimiento, sin tener en cuenta las validaciones específicas ni los casos borde de nuestro proyecto. Nosotros nos encargamos de programar la lógica real del último requerimiento, adaptar manualmente los tipos de retorno, resolver los conflictos línea por línea en Git y hacer las pruebas finales para que cumpliera con todos los criterios de la entrega.
### Reporte de IA: Felipe Camacho

`Inteligencia Artificial Usada`: ChatGPT - Codex.

## 1. ¿El proyecto debería desarrollarse como un mod de Minecraft o como un plugin de servidor?

# Bitácora IA — Nuevas implementaciones administrativas

**Proyecto:** Skill Issue Admin  
**Fecha:** 20 de noviembre de 2026  
**Integrante:** Juan Felipe Camacho Landazábal  
**Inteligencia Artificial usada:** ChatGPT y Codex

---

# 1. Implementación del sistema de Unban

## Objetivo
Desarrollar un sistema administrativo que permita revocar baneos mediante interfaces virtuales dentro de Minecraft, conservando el historial de las sanciones anteriores.

## Prompt

Se solicitó a ChatGPT elaborar un prompt para Codex que permitiera implementar un sistema de Unban integrado con la arquitectura existente del plugin.

Los requerimientos principales fueron:

- Crear el comando administrativo `/unban`.
- Mostrar mediante una GUI los jugadores que tengan baneos activos.
- Permitir seleccionar un jugador y consultar la información de su baneo.
- Incorporar una interfaz de confirmación y cancelación.
- Revocar el baneo utilizando `GestorSanciones`.
- Registrar quién realizó la revocación y cuándo, si el modelo existente lo permite.
- Actualizar la persistencia mediante `IPersistencia` y `PersistenciaArchivo`.
- Conservar el historial de la sanción después de revocarla.
- Validar permisos y evitar operaciones sobre baneos inexistentes, expirados o previamente revocados.

También se indicó que Codex debía inspeccionar primero las clases existentes y respetar la arquitectura del proyecto.

## Respuesta de la IA

ChatGPT generó un prompt estructurado para Codex, especificando el flujo de interacción mediante interfaces virtuales, las validaciones necesarias y la integración con el gestor de sanciones.

Se estableció que la revocación de un baneo no debía eliminar su registro histórico, puesto que esta información sería necesaria para futuras consultas de antecedentes y reincidencias.

## Decisiones técnicas

- La revocación se realizará mediante el gestor de sanciones.
- Se conservará el registro histórico del baneo.
- La interfaz gráfica será el medio de interacción administrativa.
- La persistencia deberá reflejar los cambios realizados.

## Verificación

Pendiente de comprobar:
- Revocación de baneos activos.
- Persistencia después de reiniciar el servidor.
- Conservación del historial.
- Restricción de acceso para usuarios sin permisos administrativos.

---

# 2. Implementación del sistema de estadísticas

## Objetivo
Desarrollar una interfaz administrativa que permita consultar estadísticas generales del servidor a partir de la información almacenada por los gestores existentes.

## Prompt

Se solicitó a ChatGPT elaborar un prompt para Codex que permitiera implementar un comando `/stats` mediante interfaces virtuales.

Los requerimientos principales fueron:

- Crear una interfaz gráfica de estadísticas generales.
- Mostrar el total de jugadores registrados.
- Mostrar el total de reportes y su distribución entre pendientes y resueltos.
- Mostrar el total de sanciones.
- Consultar la cantidad de baneos activos, mutes activos y advertencias registradas.
- Organizar la información mediante ítems representativos, nombres descriptivos y lore.
- Incorporar navegación entre páginas cuando sea necesario.

Se estableció que los datos debían obtenerse directamente de `GestorJugadores`, `GestorReportes` y `GestorSanciones`, según las operaciones disponibles en las clases existentes.

También se indicó que no debían crearse contadores independientes que pudieran desactualizarse respecto a los registros reales.

## Respuesta de la IA

ChatGPT generó un prompt para implementar el sistema de estadísticas utilizando los gestores existentes como fuentes de información.

Se recomendó calcular las estadísticas a partir de los registros disponibles, evitando duplicar información y manteniendo la consistencia con el sistema de persistencia.

## Decisiones técnicas

- Los gestores existentes serán la fuente de los datos.
- Las estadísticas se calcularán a partir de la información registrada.
- La presentación se realizará mediante interfaces virtuales.
- No se introducirán sistemas de almacenamiento paralelos.

## Verificación

Pendiente de comprobar:
- Correspondencia entre estadísticas y registros reales.
- Actualización de los valores después de nuevas operaciones.
- Correcta navegación de las interfaces.
- Acceso exclusivo para administradores.

---

# 3. Implementación del sistema de historial y reincidencias

## Objetivo
Desarrollar un sistema que permita consultar los antecedentes administrativos de los jugadores, relacionando los reportes recibidos con las sanciones aplicadas.

## Prompt

Se solicitó a ChatGPT elaborar un prompt para Codex que permitiera implementar un sistema de consulta de historial mediante interfaces virtuales.

Los requerimientos principales fueron:

- Incorporar un comando `/history` o una opción equivalente dentro de la interfaz administrativa.
- Mostrar una GUI con los jugadores registrados.
- Permitir seleccionar un jugador para consultar sus antecedentes.
- Mostrar los reportes recibidos, incluyendo motivo, descripción, fecha y estado.
- Mostrar las sanciones recibidas, incluyendo tipo, motivo, duración, responsable y estado actual.
- Incorporar estadísticas individuales relacionadas con reportes y sanciones.
- Identificar reincidencias mediante la frecuencia de incidentes y la repetición de motivos o categorías.
- Permitir navegar entre páginas cuando existan numerosos registros.

Se especificó que los reportes no debían interpretarse automáticamente como culpabilidad. El sistema debía distinguir entre denuncias recibidas, reportes resueltos y sanciones efectivamente aplicadas.

También se indicó que no debía implementarse un sistema automático de puntuación ni modificar sanciones como consecuencia de las estadísticas.

## Respuesta de la IA

ChatGPT generó un prompt para desarrollar el historial administrativo utilizando `GestorReportes` y `GestorSanciones` como fuentes de información.

Se estableció que la reincidencia debía identificarse mediante la consulta de antecedentes existentes, sin confundir la cantidad de reportes con la cantidad de sanciones confirmadas.

La interfaz debía permitir consultar la información de manera organizada y facilitar la evaluación de antecedentes por parte del personal administrativo.

## Decisiones técnicas

- Los reportes y las sanciones se consultarán desde sus respectivos gestores.
- Se conservará la distinción entre reportes y sanciones.
- La reincidencia se determinará a partir de los antecedentes registrados.
- El sistema tendrá una finalidad informativa y no aplicará sanciones automáticamente.
- Se utilizarán interfaces virtuales para seleccionar jugadores y consultar sus registros.

## Verificación

Pendiente de comprobar:
- Consulta de reportes y sanciones de un jugador.
- Correcta diferenciación entre reportes y sanciones.
- Identificación de antecedentes repetidos.
- Visualización de información histórica.
- Navegación y permisos administrativos.

---

# 4. Consideraciones generales de implementación

Para las tres funcionalidades se establecieron las siguientes condiciones:

1. Trabajar sobre una copia independiente del proyecto original, conservando una versión estable para presentación.
2. Inspeccionar las clases y métodos existentes antes de implementar cambios.
3. Mantener la arquitectura orientada a objetos del proyecto.
4. Reutilizar los gestores y mecanismos de persistencia existentes.
5. Utilizar interfaces virtuales siguiendo el patrón de las interfaces administrativas ya desarrolladas.
6. Evitar modificaciones innecesarias en funcionalidades ajenas al alcance.
7. Mantener las versiones actuales de Java, Paper y Gradle.
8. Compilar el proyecto al finalizar y documentar los resultados de las pruebas.

**Resultado esperado:** incorporar tres funcionalidades administrativas nuevas —Unban, Estadísticas e Historial y Reincidencias— sin comprometer la versión estable del plugin ni alterar la arquitectura existente.

**Respuesta:**  
Las funcionalidades principales de *Skill Issue Admin* —reportes, sanciones, rangos, historial y persistencia— corresponden principalmente a lógica del servidor, por lo que un **plugin de Paper/Spigot** resulta más adecuado. Un mod sería necesario principalmente si se requirieran modificaciones del cliente o nuevas mecánicas, bloques, ítems o interfaces.
