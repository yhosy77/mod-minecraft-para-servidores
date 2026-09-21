# Plantilla Propuesta:
Descripción: **Esta es la plantilla que usaremos para los requerimientos**

### Ejemplo
| ID | Descripción | Actor | Prioridad | Criterio de Aceptación Verificable |
| :---: | :--- | :---: | :---: | :--- |
| **RF-01** | El sistema debe permitir a un jugador enviar un reporte detallado contra otro jugador por conducta indebida. | Jugador | Alta | Permite ingresar el NickName del acusado y el motivo. El acusado debe existir en el sistema. No permite reportes vacíos ni auto-reportes.|
| **RF-02** | El sistema debe permitir al administrador registrar nuevos jugadores con sus atributos base (NickName, Rango, Skin, Nivel, Monedas y Fecha de Ingreso).| Admin | Alta |El NickName tiene de 3 a 16 caracteres (letras, números y guion bajo) y no pueden existir dos jugadores con el mismo NickName. El nivel y las monedas no pueden ser negativos.|
| **RF-03** | El sistema debe permitir al administrador aplicar una sanción (Baneo o Mute) a un jugador especificando tipo de infracción, motivo, duración y staff responsable.| Admin | Alta |El tipo de infracción (insultos, trampas o spam) y el motivo son obligatorios. La duración debe ser mayor a cero, salvo en un baneo permanente. El jugador aparece como "Sancionado" mientras la sanción esté vigente y deja de aparecer así cuando se cumple la duración.|
| **RF-04** | El sistema debe permitir al administrador consultar el historial completo de sanciones de un jugador específico. | Admin | Media | Muestra la lista cronológica de todas las sanciones (activas e históricas) asociadas al NickName consultado. |
| **RF-05** | El sistema debe permitir al administrador asignar o cambiar el rango de permisos de un jugador. | Admin | Media | Solo se pueden asignar rangos que existan en la jerarquía del sistema. Tras el cambio, la ficha del jugador muestra el nuevo rango y sus permisos.|
| **RF-06** | El sistema debe permitir al administrador atender los reportes recibidos, cambiando su estado hasta resolverlos o descartarlos.| Admin | Alta |Los estados siguen el orden Abierto → En revisión → Resuelto o Descartado. Un reporte cerrado no se puede reabrir.
| **RF-07** | El sistema debe permitir al administrador revocar una sanción activa (Unban / Unmute) con una justificación. Después de revocarla, el jugador queda en estado "Advertido" durante 30 días.| Admin | Media |	La sanción se marca como "Revocada" y se guarda el motivo del perdón. Si durante esos 30 días el jugador recibe una sanción por el mismo tipo de infracción, la sanción sube un nivel en la escala de severidad. |
| **RF-08** |El sistema debe calcular automáticamente el nivel de reincidencia de un jugador basado en su historial de sanciones.| Sistema | Media |Si el jugador tiene 3 o más sanciones no revocadas en los últimos 60 días, la severidad sugerida sube un nivel en la escala Mute → Baneo temporal → Baneo permanente.|
| **RF-09** | El sistema debe permitir al administrador filtrar la lista de reportes por estado o por rango de fechas. | Admin | Baja | 	Muestra únicamente los reportes que coinciden con los filtros seleccionados. El rango de fechas incluye el día inicial y el final. |
| **RF-10** | El sistema debe permitir al administrador generar un resumen estadístico de la actividad y sanciones aplicadas por el staff. | Admin | Baja | Muestra el conteo de sanciones y reportes atendidos por cada administrador en el periodo elegido.|
| **RF-11** | El sistema debe permitir al jugador subir a un rango gratuito si cumple el nivel o las monedas requeridas.| Jugador | Media | Para subir a Miembro se requiere nivel 10 o 500 monedas; si se usan monedas, se descuentan del saldo. No se puede subir a un rango exclusivo por esta vía. Si no cumple los requisitos, se muestra un mensaje con lo que le falta.|
| **RF-12** |El sistema debe permitir al administrador registrar la compra de un rango exclusivo pagado fuera del sistema. | Admin | Baja | Se guarda la fecha, el valor y el rango comprado. Los rangos exclusivos solo dan beneficios cosméticos y nunca permisos de staff.|
| **RF-13** |El sistema debe permitir registrar donaciones de un jugador sin que reciba recompensas a cambio. | Jugador| Baja | El monto debe ser mayor a cero. La donación no cambia el rango ni las monedas del jugador. El administrador puede consultar el total donado por periodo. |


| **RNF-01** | La información debe guardarse en archivos planos entre ejecuciones. | Alta |Al reiniciar, los jugadores, reportes y sanciones siguen disponibles. Si un archivo no existe, se crea sin error.|
| **RNF-02** |Los formularios deben validar los datos y el sistema no debe cerrarse ante datos inválidos o archivos dañados. | Alta | Ante un dato inválido se muestra un mensaje claro de qué corregir. Una línea dañada en un archivo se omite y se informa al usuario.|




# Comments, upgrades.
En el RF_07 Podríamos poner una condición para que el jugador se mantenga a raya no cree?, tipo..., bueno, para que no vuelva a cometer la misma infracción o algo así. Yo creo que podría ser temporal, pero la verdad no sé si lo ponemos ahí mismo o si lo ponemos como un requerimiento nuevo.
