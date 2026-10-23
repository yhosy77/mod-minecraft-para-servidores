# Registro de decisiones para el proyecto: "Skill Issue Admin"

# Registro de decisiones V1
Se penso primeramente hacer un mod para el uso de redstone o un metodo educativo para evitar la adiccion de los jovenes a las redes sociales.
Pero luego al ver la posible dificultad se hizo una lluvia de ideas junto con claude.IA
De ahi se decidió mejor hacer un Mod para los servidores de minecraft
Esto debido a que notamos una gran carencia en la administracion de los servidores al administrar los usuarios, prioridades, compras, baneos, etc.


# DEC 001 - Cambio de Idea de Mod a Plugin.

**Fecha:** 22-09-2026
**Estado:** Aprobado.

### Contexto:
El proyecto necesitaba reorganizar y redirigir su enfoque. Durante la primera reunión del grupo para debatir los puntos conceptuales del proyecto, se analizó que la decisión de crear un mod, siendo esta orientada a ser implementada en servidores públicos, por lo cual se tomó la decisión de cambiar la orientación del proyecto. 

### Decisión:

Cambiar la idea de desarrollar un Mod a desarrollar un Plugin que se pueda implementar desde Minecraft Vanilla, usando el ecosistema Paper/Spigot.

### Razones:

- Los Plugins están mas relacionados con la idea propuesta.
- Simplificación en la conexión con internet del Mod.

### Consecuencias: 
El proyecto contará con un entorno de desarrollo creado en Gradle/Kotlin y desarrollada en java.

# DEC 002 - Separar archivos
**Fecha:** 22-09-2026
**Estado:** Aprobado.

### Contexto:
En el proyecto trabajar todo sobre un mismo archivo puede ser muy tedioso y difícil de manejar. Haciendo que los programadores se confundan más fácilmente y/o cometan más errores.

### Decisión y Consecuencias:
Decidimos que en lo que más se pueda, utilizaremos archivos separados para poder llevar el control y el orden mucho más fácil para el proyecto y así también podremos sacar y/o tener una mejor producción y comunicación con cada uno de los integrantes.

# DEC 003 - Usar PaperMC como API.
**Fecha:** 12-10-2026
**Estado:** Aprobado.

### Contexto:
Paper ofrece una API más sencilla para el desarrollo del plugin y está mejor optimizada, sumado a que ya tiene en su código algunas interfaces que nos permiten usar de una manera más sencilla la implementación de comandos y por medio de Gradle nos permite abrir el servidor y probarlo.

### Decisión y Consecuencias:
Descargamos la plantilla del PaperMC y la subimos al repositorio. Además fuimos desarrollando unos plugins de prueba con funciones básicas para familiarizarnos con el entorno de desarrollo mientras fijabamos el rumbo del proyecto para crear el Diagrama UML.
