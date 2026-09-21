# Plantilla Propuesta:
Descripción: **Esta es la plantilla que usaremos para los requerimientos**

### Ejemplo
| ID | Descripción | Actor | Prioridad | Criterio de Aceptación Verificable |
| :---: | :--- | :---: | :---: | :--- |
| **RF-01** | El sistema debe permitir a un jugador enviar un reporte detallado contra otro jugador por conducta indebida. | Jugador | Alta | Permite ingresar un NickName acusado y motivo. No permite reportes vacíos ni auto-reportes. |
| **RF-02** | El sistema debe permitir al administrador registrar nuevos jugadores con sus atributos base (NickName, Rango, Skin, Fecha de Ingreso). | Admin | Alta | El NickName debe ser alfanumérico (3 a 16 caracteres) y no pueden existir dos jugadores con el mismo NickName. |
| **RF-03** | El sistema debe permitir al administrador aplicar una sanción (Baneo o Mute) a un jugador especificando motivo, duración y staff responsable. | Admin | Alta | Se crea un objeto de sanción asociado al jugador, cambiando su estado a "Sancionado" y guardando la fecha exacta. |
| **RF-04** | El sistema debe permitir al administrador consultar el historial completo de sanciones de un jugador específico. | Admin | Media | Muestra la lista cronológica de todas las sanciones (activas e históricas) asociadas al NickName consultado. |
| **RF-05** | El sistema debe permitir al administrador asignar o cambiar el rango de permisos de un jugador. | Admin | Media | Valida que el rango exista en la jerarquía del sistema y actualice inmediatamente los permisos del jugador. |
| **RF-06** |
| **RF-07** | El sistema debe permitir al administrador revocar o perdonar una sanción activa (Unban / Unmute) con una justificación. | Admin | Media | La sanción se marca como "Revocada", se registra el motivo de perdón y el jugador recupera sus accesos normales. |
| **RF-08** | El sistema debe calcular automáticamente el nivel de reincidencia de un jugador basado en su historial de sanciones. | Sistema | Media | Si el jugador supera 3 sanciones previas, el sistema eleva automáticamente la severidad sugerida para la siguiente sanción. |
| **RF-09** | El sistema debe permitir al administrador filtrar la lista de reportes por estado o por rango de fechas. | Admin | Baja | Retorna únicamente la colección de reportes que coincidan exactamente con los filtros seleccionados. |
| **RF-10** | El sistema debe permitir al administrador generar un resumen estadístico de la actividad y sanciones aplicadas por el staff. | Admin | Baja | Retorna el conteo total de sanciones y reportes atendidos agrupados por cada administrador en un periodo. |


# Comments, upgrades.
En el RF_07 Podríamos poner una condición para que el jugador se mantenga a raya no cree?, tipo..., bueno, para que no vuelva a cometer la misma infracción o algo así. Yo creo que podría ser temporal, pero la verdad no sé si lo ponemos ahí mismo o si lo ponemos como un requerimiento nuevo.
