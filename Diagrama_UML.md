```mermaid
classDiagram
    %% INTERFAZ DE PERSISTENCIA
    class IPersistencia {
        <<interface>>
        +guardarDatos(String archivo, ArrayList datos) boolean
        +cargarDatos(String archivo) ArrayList
    }

    %% CLASE ABSTRACTA Y HERENCIA DE SANCIONES
    class Sancion {
        <<abstract>>
        #int id
        #String motivo
        #String fecha
        #boolean activa
        -int contadorSanciones$+getSiguienteId()$ int
        +aplicarEfecto()* boolean
        +revocar() void
        +gettersAndSetters()
    }

    class Baneo {
        -boolean permanente
        -int duracionDias
        +aplicarEfecto() boolean
    }

    class Mute {
        -int duracionMinutos
        -String canalSilenciado
        +aplicarEfecto() boolean
    }

    class Advertido {
        -int nivelSeveridad
        -boolean requiereConfirmacion
        +aplicarEfecto() boolean
    }

    %% ENTIDADES DEL DOMINIO
    class Jugador {
        -String id
        -String nombre
        -String skin
        -int nivel
        -int monedas
        -int horasJugadas
        -String fechaIngreso
        -Rango rangoActual
        -ArrayList~Sancion~ historialSanciones
        +NIVEL_MIN_MIEMBRO$int = 10 +MONEDAS_MIN_MIEMBRO$ int = 500
        +registrarHoras(int horas) void
        +asignarRango(Rango nuevoRango) void
        +subirRangoGratuito(Rango rangoMiembro) boolean
        +agregarSancion(Sancion s) void
        +tienePermiso(String permiso) boolean
        +gettersAndSetters()
    }

    class Rango {
        -String nombre
        -ArrayList~String~ permisos
        -boolean espago
        +agregarPermiso(String permiso) void
        +tienePermiso(String permiso) boolean
        +esRangoPago() boolean
        +gettersAndSetters()
    }

    class Reporte {
        -int id
        -String motivo
        -String estado
        -String fecha
        -String reportante
        -String reportado
        -int contadorReportes$+getSiguienteId()$ int
        +cambiarEstado(String nuevoEstado) void
        +getEstado() String
        +getFecha() String
        +gettersAndSetters()
    }

    class Donacion {
        -int id
        -double monto
        -String fecha
        -Jugador jugador
        +validarMonto() boolean
        +getMonto() double
        +gettersAndSetters()
    }

    class CompraRango {
        -String fechaCompra
        -Donacion donacionAsociada
        -Rango rangoAdquirido
        +procesarCompra(Jugador j) boolean
        +gettersAndSetters()
    }

    %% GESTORES Y CLASE ANIDADA
    class GestorReportes {
        -ArrayList~Reporte~ reportes
        -IPersistencia persistencia
        +crearReporte(Jugador j1, Jugador j2, String motivo) Reporte
        +resolverReporte(int id, String estado) void
        +filtrarReportes(String estado) List~Reporte~
        +filtrarReportes(CriterioFiltro criterio) List~Reporte~
    }

    class CriterioFiltro {
        <<nested>>
        -String estadoBuscado
        -String fechaDesde
        -String fechaHasta
        +coincide(Reporte r) boolean
    }

    class GestorJugadores {
        -ArrayList~Jugador~ jugadores
        -IPersistencia persistencia
        +registrarJugador(Jugador j) void
        +buscarJugador(String id) Jugador
        +buscarJugador(String nombre, boolean exacto) Jugador
        +eliminarJugador(String id) boolean
        +actualizarHoras(String id, int horas) void
    }

    class GestorSanciones {
        -ArrayList~Sancion~ sancionesActivas
        -IPersistencia persistencia
        +aplicarSancion(Jugador j, Sancion s) void
        +levantarSancion(int idSancion) boolean
        +obtenerSancionesPorJugador(String id) ArrayList~Sancion~
    }


    %% COMANDOS
    class BasicCommand{
        -String nombreComando
    }



    %% PERSISTENCIA EN ARCHIVOS
    class PersistenciaArchivo {
        -String rutaBase
        +guardarDatos(String archivo, ArrayList datos) boolean
        +cargarDatos(String archivo) ArrayList
        +verificarArchivo(String nombreArchivo) boolean
    }

    %% EXCEPCIONES PERSONALIZADAS
    class JugadorNoEncontradoException {
        +JugadorNoEncontradoException(String msg)
    }

    class SancionInvalidaException {
        +SancionInvalidaException(String msg)
    }

    %% JERARQUÍA DE SANCIONES
    Sancion <|-- Baneo : extends
    Sancion <|-- Mute : extends
    Sancion <|-- Advertido : extends

    %% IMPLEMENTACION DE INTERFAZ
    IPersistencia <|.. PersistenciaArchivo : implements

    %% RELACIONES DEL DOMINIO
    Jugador "1" --> "1" Rango : tiene
    Jugador "1" *-- "0..*" Sancion : historial
    GestorReportes "1" o-- "0..*" Reporte : administra
    GestorReportes *-- CriterioFiltro : contiene (nested)
    GestorJugadores "1" o-- "0..*" Jugador : administra
    GestorSanciones --> Jugador : procesa
    GestorSanciones --> Sancion : administra
    Jugador "1" --> "0..*" Donacion : realiza
    Jugador "1" --> "0..*" CompraRango : realiza
    CompraRango "0..*" --> "1" Rango : adquiere

    %% EXCEPCIONES
    GestorJugadores ..> JugadorNoEncontradoException : lanza
    Sancion ..> SancionInvalidaException : lanza
