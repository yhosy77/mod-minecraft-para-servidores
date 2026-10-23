# Primera Entrega del Proyecto

# LINK DEL REPOSITORIO
*https://github.com/yhosy77/mod-minecraft-para-servidores*
---

# Skill Issue Admin - Plugin de Gestión para Servidores de Minecraft
**Grupo:** POO-Error Capa 8

# Nuevo nombre definitivo:
Skill Issue Admin
- La frase "Skill Issue", lo propusimos por su definición como una muletilla humorística de la comunidad para referirse a la "falta de habilidad", en este caso, por la falta de habilidad de un admin al tratar de controlar el caos de los baneos, rangos y reportes.

---

## Conformación del Equipo y Roles inciales:
| Nombre Completo | Rol Inicial | Responsabilidades Principales |
| :--- | :--- | :--- |
| **David Santiago Díaz** | Responsable de Producto (PO) | Cuidar el problema y validar que los RF respondan a las necesidades del proyecto. |
| **Samuel Gacharna** | Responsable de Producto (PO) | Apoyo en la definición de requerimientos y criterios de aceptación. |
| **Samuel Zona** | Responsable de Diseño (AR) | Liderar el diagrama de clases UML y el registro de decisiones de POO. |
| **Juan Bello** | Responsable de Diseño (AR) | Garantizar que la arquitectura y clases respeten el diseño estructurado. |
| **Miguel** | Responsable de Calidad (QA) | Definir criterios de aceptación, casos límite, pruebas y manejo de errores. |
| **Juan Felipe Camacho Landazábal** | Responsable de Integración (GI) | Administrar el repositorio de GitHub, ramas, PRs y frecuencia de commits. |

## PASO 1: Descripción del Problema y Contexto de Automatización

### 1. Contexto (Dominio)
El proyecto se ubica en el dominio de la administración de **servidores multijugador del Minecraft (Java obviamente)**. En estos entornos virtuales coinciden simultáneamente decenas o cientos de jugadores en tiempo real. Para garantizar una convivencia armónica y un juego justo, las comunidades requieren moderar comportamientos tóxicos, gestionar rangos/permisos y controlar sanciones de forma ordenada.

### 2. Actores del Sistema
* **Jugador (Player / User):** Usuario convencional del servidor. Puede jugar, interactuar y enviar reportes sobre infracciones cometidas por otros usuarios.
* **Administrador / Moderador (Admin / Staff):** Encargado de mantener el orden, revisar la cola de reportes, aplicar/revocar sanciones (bans, mutes, advertencias) y asignar rangos.
* **Sistema (Skill Issue Admin):** El plugin en Java que procesa los comandos, valida las reglas de negocio, administra la persistencia de datos y ejecuta las restricciones automáticas.

### 3. Proceso Actual (Cómo se hace hoy)
Actualmente, en muchos servidores medianos y pequeños, la moderación se realiza mediante un parche de métodos manuales e ineficientes:
1. Un jugador nota una infracción y debe avisar por chat general o abrir un ticket manual en un canal de Discord externo.
2. Los administradores anotan las sanciones en hojas de texto plano local, blocs de notas o mensajes dispersos.
3. Las sanciones como baneos o mutes temporales se gestionan manualmente o con comandos nativos aislados (`/ban`, `/mute`), sin guardar un historial unificado vinculante.

### 4. El Dolor
* **Las faltas se olvidan:** Si un jugador rompe las reglas varias veces pero lo atienden administradores distintos, nadie sabe que es reincidente y termina recibiendo castigos leves una y otra vez.
* **Mucho trabajo manual y desorden:** Anotar los baneos en notas sueltas, calcular a mano cuántos días dura un castigo y acordarse de quitarlo a tiempo quita mucho tiempo y da espacio a errores o injusticias.
* **Los reportes se pierden:** Los usuarios reportan un problema pero no saben si se atendió, y los dueños del servidor no tienen forma de revisar si los administradores están haciendo bien su trabajo.

### 5. Impacto de la Solución
* **Técnico:** Reemplaza las notas sueltas por un sistema ordenado que guarda toda la información automáticamente y la mantiene a salvo sin perder datos si la aplicación se cierra.
* **Económico:** Lleva un control claro de las compras de rangos y donaciones de los jugadores, evitando cobros duplicados o que se entreguen beneficios a la persona equivocada.
* **Social:** Hace que el juego sea más justo para todos, ya que las sanciones se aplican con reglas claras y automáticas para evitar favoritismos, dando además segundas oportunidades con periodos de prueba a quienes apelan.

---

## PASO 2: Requerimientos del Sistema

## Aplicación de los Conceptos Vistos en Clase

### 1. Clases
Dividimos el proyecto en tres tipos de clases. Las **entidades** (`Jugador`, `Rango`, `Sancion`, `Reporte`, `Donacion` y `CompraRango`) guardan la información del servidor. Los **gestores** (`GestorJugadores`, `GestorSanciones` y `GestorReportes`) se encargan de la lógica, como buscar un jugador, aplicar una sanción o resolver un reporte. Por último, los **comandos** (`ReportCommand`, `BanCommand`, `ProfileCommand` y `RankCommand`) representan lo que el jugador o el staff escribe en el chat.

Decidimos separarlo así para que cada clase tenga una sola responsabilidad. Si mezclábamos todo, por ejemplo poniendo la lógica de sanciones dentro de `Jugador`, esa clase iba a crecer demasiado y cualquier cambio podía dañar otras partes. Con esta separación, si cambiamos cómo funciona un comando, las entidades no se ven afectadas.

### 2. Atributos y Métodos Estáticos
Los usamos en dos partes del proyecto:

- **Generación de IDs:** `Sancion` y `Reporte` tienen un contador estático (`contadorSanciones` y `contadorReportes`) y un método estático `getSiguienteId()`. Como el contador pertenece a la clase y no a cada objeto, todas las sanciones comparten el mismo, entonces cada vez que se crea una nueva recibe un ID diferente y nunca se repite.
- **Requisitos de rango:** en `Jugador` están las constantes `NIVEL_MIN_MIEMBRO = 10` y `MONEDAS_MIN_MIEMBRO = 500`, que son lo que necesita un jugador para subir a Miembro sin pagar. Estos valores son iguales para todos, así que no tiene sentido que cada jugador tenga su propia copia. Además, si el día de mañana se quiere cambiar el requisito, solo hay que modificarlo en un lugar.

### 3. Clases Anidadas
`CriterioFiltro` es una clase estática anidada dentro de `GestorReportes`. La usamos para filtrar reportes por estado y por un rango de fechas. Sin ella, el método `filtrarReportes` tendría que recibir varios parámetros sueltos, lo que lo haría más difícil de leer y de usar.

La dejamos dentro de `GestorReportes` porque es la única clase que la necesita. Crearla como una clase aparte solo habría agregado algo más al proyecto que ninguna otra parte usa.

### 4. Interfaces
Creamos la interfaz `IPersistencia`, que tiene los métodos `guardarDatos()` y `cargarDatos()`. La implementa `PersistenciaArchivo`, y los tres gestores tienen un atributo de tipo `IPersistencia`. De esta forma, los gestores no saben cómo se guardan los datos, solo saben que pueden llamar a esos dos métodos.

La razón principal fue pensar en el futuro del proyecto. Por ahora todo se guarda en archivos, pero si después queremos usar una base de datos, basta con crear una nueva clase que implemente `IPersistencia`, y los gestores siguen funcionando sin cambiar nada.

### 5. Polimorfismo
Es uno de los conceptos que más aparece en el diseño:

- **Sanciones:** `aplicarEfecto()` es un método abstracto en `Sancion`, y cada subclase lo implementa de forma distinta. Un `Baneo` saca al jugador del servidor, un `Mute` le bloquea el chat y un `Advertido` solo le muestra un aviso. `GestorSanciones` llama a `aplicarEfecto()` sin necesidad de saber qué tipo de sanción está aplicando.
- **Comandos:** todos los comandos heredan de `BasicCommand` y sobrescriben el método `ejecutar(Jugador emisor, String[] args)`. En la primera versión del diagrama cada comando tenía un `ejecutar` con parámetros diferentes, y al revisarlo nos dimos cuenta de que eso no era polimorfismo, porque no se podían tratar todos los comandos de la misma manera. Por eso dejamos la misma firma para todos.
- **Sobrecarga:** `buscarJugador` se puede llamar solo con el ID, o con el nombre y un booleano que indica si la búsqueda debe ser exacta. Pasa lo mismo con `filtrarReportes`, que puede recibir solo un estado o un `CriterioFiltro` completo. Así el mismo método se adapta a lo que se necesite buscar.
- **Por interfaz:** como los gestores trabajan con el tipo `IPersistencia` y no directamente con `PersistenciaArchivo`, se puede cambiar la implementación sin modificar el resto del código.

La mayor ventaja del polimorfismo en nuestro proyecto es que facilita agregar cosas nuevas. Por ejemplo, si queremos una sanción `Kick`, solo hay que crear la subclase con su propio `aplicarEfecto()`, y el resto del sistema la puede usar sin hacer cambios.

## Tabla de Trazabilidad Inicial

| Requerimiento | Clases Involucradas (Previstas) | Temas del Curso Aplicados | Estado Inicial |
| :---: | :--- | :--- | :---: |
| **RF-01 / RF-06** | `Reporte`, `GestorReportes` | Clases y Objetos, Encapsulamiento | Pendiente |
| **RF-02 / RF-05** | `Jugador`, `Rango` | Encapsulamiento, Atributos Privados | Pendiente |
| **RF-03 / RF-07** | `Sancion` (Abstracta), `Baneo`, `Mute` | Herencia, Clases Abstractas | Pendiente |
| **RF-04 / RF-08** | `HistorialSanciones`, `GestorJugadores` | Comunicación entre objetos | Pendiente |

##  Conformación del Equipo y Roles (Entrega 2)
| Nombre Completo | Rol Entrega 2 | Responsabilidades Principales |
| :--- | :---: | :--- |
| **David Santiago Díaz** | **Responsable de Diseño (AR)** | Liderar la arquitectura de clases, jerarquías de herencia, interfaces y el Registro de Decisiones v2. |
| **Juan Felipe Camacho Landazábal** | **Responsable de Diseño (AR)** | Diseñar el diagrama de clases UML actualizado y asegurar la correcta estructuración del código en Java. |
| **Samuel Zona** | **Responsable de Producto (PO)** | Validar que la implementación de la Entrega 2 cumpla los 10 Requerimientos Funcionales planteados. |
| **Juan Bello** | **Responsable de Producto (PO)** | Mantener la trazabilidad entre el problema, los requerimientos y los casos de uso del mod. |
| **Samuel Gacharna** | **Responsable de Calidad (QA)** | Diseñar las excepciones personalizadas, probar casos límite y verificar la persistencia sin errores. |
| **Miguel Peña** | **Responsable de Integración (GI)** | Controlar la estructura del repositorio en GitHub, verificar commits de cada integrante y gestionar ramas. |
