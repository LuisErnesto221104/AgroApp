# Diagrama de Arquitectura Lógica UML - AgroApp

## Ilustración XX - Diagrama de Arquitectura Lógica

```plantuml
@startuml DiagramaArquitecturaLogica_AgroApp
!pragma teoz true
skinparam monochrome true
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 11
skinparam packageStyle rectangle
skinparam linetype ortho

title **Diagrama de Arquitectura Lógica - AgroApp**\n(Patrón MVC - Model-View-Controller)

' ═══════════════════════════════════════════════════════════════
' CAPA DE PRESENTACIÓN (VIEW / BOUNDARY)
' ═══════════════════════════════════════════════════════════════

package "<<Capa de Presentación>>\ncom.example.agroapp.activity" as VIEW #FAFAFA {
    
    class "LoginActivity" as LA <<Activity>> {
        - etUsuario: EditText
        - etPassword: EditText
        + iniciarSesion()
        + registrarUsuario()
        - guardarSesion()
    }
    
    class "MainActivity" as MA <<Activity>> {
        - btnAnimales: CardView
        - btnEventos: CardView
        - btnGastos: CardView
        + navegarAModulo()
        + cerrarSesion()
    }
    
    class "RegistroAnimalActivity" as RAA <<Activity>> {
        - etArete: EditText
        - spRaza: Spinner
        - imgFoto: ImageView
        + guardarAnimal()
        + cargarDatosAnimal()
        + tomarFoto()
    }
    
    class "GestionAnimalesActivity" as GAA <<Activity>> {
        - recyclerView: RecyclerView
        - fabAgregar: FloatingActionButton
        + cargarAnimales()
        + filtrarPorEstado()
    }
    
    class "DetalleAnimalActivity" as DAA <<Activity>> {
        - tvNombre: TextView
        - imgAnimal: ImageView
        + mostrarDetalle()
        + editarAnimal()
        + eliminarAnimal()
    }
    
    class "EventosSanitariosActivity" as ESA <<Activity>> {
        - recyclerView: RecyclerView
        + registrarEvento()
        + listarEventos()
    }
    
    class "GastosActivity" as GaA <<Activity>> {
        - recyclerView: RecyclerView
        - tvTotal: TextView
        + registrarGasto()
        + calcularTotal()
    }
    
    class "AlimentacionActivity" as AlA <<Activity>> {
        - recyclerView: RecyclerView
        + registrarAlimentacion()
        + listarRegistros()
    }
    
    class "HistorialClinicoActivity" as HCA <<Activity>> {
        - recyclerView: RecyclerView
        + verHistorial()
        + agregarRegistro()
    }
    
    class "ReportesActivity" as ReA <<Activity>> {
        - chartView: View
        + generarReporte()
        + exportarPDF()
    }
    
    class "CalendarioActivity" as CaA <<Activity>> {
        - calendarView: CalendarView
        + mostrarEventos()
        + programarRecordatorio()
    }
    
    class "RecomendacionesActivity" as RecA <<Activity>> {
        - listRecomendaciones: ListView
        + cargarRecomendaciones()
    }
    
    class "RegistroComprasActivity" as RCA <<Activity>> {
        - etProveedor: EditText
        + registrarCompra()
    }
    
    abstract class "BaseActivity" as BA <<Abstract>> {
        # toolbar: Toolbar
        + configurarToolbar()
        + mostrarMensaje()
    }
}

' ═══════════════════════════════════════════════════════════════
' CAPA DE ADAPTADORES (VISTA - RecyclerView)
' ═══════════════════════════════════════════════════════════════

package "<<Adaptadores>>\ncom.example.agroapp.adapters" as ADAPTERS #F5F5F5 {
    
    class "AnimalAdapter" as AnA <<Adapter>> {
        - listaAnimales: List<Animal>
        + onBindViewHolder()
        + onCreateViewHolder()
        + setOnItemClickListener()
    }
    
    class "EventoSanitarioAdapter" as EsAd <<Adapter>> {
        - listaEventos: List<EventoSanitario>
        + onBindViewHolder()
    }
    
    class "GastoAdapter" as GaAd <<Adapter>> {
        - listaGastos: List<Gasto>
        + onBindViewHolder()
    }
    
    class "AlimentacionAdapter" as AlAd <<Adapter>> {
        - listaAlimentacion: List<Alimentacion>
        + onBindViewHolder()
    }
    
    class "HistorialClinicoAdapter" as HcAd <<Adapter>> {
        - listaHistorial: List<HistorialClinico>
        + onBindViewHolder()
    }
}

' ═══════════════════════════════════════════════════════════════
' CAPA DE CONTROL (CONTROLLER)
' ═══════════════════════════════════════════════════════════════

package "<<Capa de Control>>\ncom.example.agroapp.presenter" as CONTROLLER #F0F0F0 {
    
    class "AnimalPresenter" as AP <<Controller>> {
        - animalDAO: AnimalDAO
        - view: AnimalView
        - executor: ExecutorService
        __
        + validarArete(String): boolean
        + validarPrecio(double, String): boolean
        + validarFechasCoherentes(String, String): boolean
        + procesarImagen(Bitmap): String
        + guardarAnimal(Animal, boolean)
        + cargarAnimalPorArete(String, Callback)
    }
    
    interface "AnimalView" as AV <<Interface>> {
        + mostrarExito(String)
        + mostrarError(String)
        + cerrarActividad()
        + ejecutarEnUIThread(Runnable)
    }
}

' ═══════════════════════════════════════════════════════════════
' CAPA DE ACCESO A DATOS (DAO / CONTROL)
' ═══════════════════════════════════════════════════════════════

package "<<Capa de Acceso a Datos>>\ncom.example.agroapp.dao" as DAO #EBEBEB {
    
    class "UsuarioDAO" as UD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + validarUsuario(String, String): Usuario
        + obtenerPorUsername(String): Usuario
        + existeAlgunUsuario(): boolean
        + insertar(Usuario): long
    }
    
    class "AnimalDAO" as AD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + insertarAnimal(Animal): long
        + actualizarAnimal(Animal): int
        + eliminarAnimalPorArete(String): int
        + obtenerTodosLosAnimales(): List<Animal>
        + obtenerAnimalPorArete(String): Animal
        + obtenerIdPorArete(String): int
        + obtenerAnimalesPorEstado(String): List<Animal>
        - existeArete(String): boolean
        - cursorToAnimal(Cursor): Animal
    }
    
    class "EventoSanitarioDAO" as ESD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + insertar(EventoSanitario): long
        + obtenerPorAnimal(int): List<EventoSanitario>
        + obtenerTodos(): List<EventoSanitario>
        + eliminar(int): int
    }
    
    class "GastoDAO" as GD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + insertar(Gasto): long
        + obtenerPorAnimal(int): List<Gasto>
        + obtenerTodos(): List<Gasto>
        + calcularTotal(): double
        + eliminar(int): int
    }
    
    class "AlimentacionDAO" as AliD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + insertar(Alimentacion): long
        + obtenerPorAnimal(int): List<Alimentacion>
        + obtenerTodos(): List<Alimentacion>
        + eliminar(int): int
    }
    
    class "HistorialClinicoDAO" as HCD <<DAO>> {
        - dbHelper: DatabaseHelper
        __
        + insertar(HistorialClinico): long
        + obtenerPorAnimal(int): List<HistorialClinico>
        + eliminar(int): int
    }
}

' ═══════════════════════════════════════════════════════════════
' CAPA DE MODELO (ENTITY)
' ═══════════════════════════════════════════════════════════════

package "<<Capa de Modelo>>\ncom.example.agroapp.models" as MODEL #E6E6E6 {
    
    class "Usuario" as U <<Entity>> {
        - id: int
        - username: String
        - password: String
        - nombre: String
        __
        + getters()
        + setters()
    }
    
    class "Animal" as An <<Entity>> {
        - id: int
        - numeroArete: String
        - nombre: String
        - raza: String
        - sexo: String
        - fechaNacimiento: String
        - fechaIngreso: String
        - pesoNacer: double
        - pesoActual: double
        - precioCompra: double
        - estado: String
        - foto: String
        - observaciones: String
        __
        + getters()
        + setters()
    }
    
    class "EventoSanitario" as ES <<Entity>> {
        - id: int
        - animalId: int
        - tipoEvento: String
        - descripcion: String
        - fecha: String
        - proximaFecha: String
        - veterinario: String
        - costo: double
        __
        + getters()
        + setters()
    }
    
    class "Gasto" as G <<Entity>> {
        - id: int
        - animalId: int
        - tipo: String
        - descripcion: String
        - monto: double
        - fecha: String
        __
        + getters()
        + setters()
    }
    
    class "Alimentacion" as Ali <<Entity>> {
        - id: int
        - animalId: int
        - tipoAlimento: String
        - cantidad: double
        - unidad: String
        - fecha: String
        - costo: double
        __
        + getters()
        + setters()
    }
    
    class "HistorialClinico" as HC <<Entity>> {
        - id: int
        - animalId: int
        - diagnostico: String
        - tratamiento: String
        - fecha: String
        - veterinario: String
        __
        + getters()
        + setters()
    }
}

' ═══════════════════════════════════════════════════════════════
' CAPA DE INFRAESTRUCTURA
' ═══════════════════════════════════════════════════════════════

package "<<Capa de Infraestructura>>\ncom.example.agroapp.database" as INFRA #E0E0E0 {
    
    class "DatabaseHelper" as DH <<SQLiteOpenHelper>> {
        - DATABASE_NAME: String = "ganado.db"
        - DATABASE_VERSION: int
        __
        + onCreate(SQLiteDatabase)
        + onUpgrade(SQLiteDatabase, int, int)
        + getWritableDatabase(): SQLiteDatabase
        + getReadableDatabase(): SQLiteDatabase
    }
}

package "<<Utilidades>>\ncom.example.agroapp.utils" as UTILS #DADADA {
    
    class "NotificationHelper" as NH <<Utility>> {
        - context: Context
        - CHANNEL_ID: String
        __
        + crearCanalNotificacion()
        + mostrarNotificacion(String, String)
        + programarRecordatorio(Date, String)
    }
    
    class "NotificationReceiver" as NR <<BroadcastReceiver>> {
        + onReceive(Context, Intent)
    }
}

' ═══════════════════════════════════════════════════════════════
' ALMACENAMIENTO EXTERNO
' ═══════════════════════════════════════════════════════════════

package "<<Almacenamiento>>" as STORAGE #D5D5D5 {
    
    database "SQLite\nganado.db" as DB {
        + usuarios
        + animales
        + eventos_sanitarios
        + gastos
        + alimentacion
        + historial_clinico
    }
    
    storage "SharedPreferences\nAgroAppPrefs" as SP {
        + isLoggedIn: boolean
        + userId: int
        + userName: String
        + password: String
    }
}

' ═══════════════════════════════════════════════════════════════
' RELACIONES ENTRE CAPAS
' ═══════════════════════════════════════════════════════════════

' Herencia de BaseActivity
BA <|-- LA
BA <|-- MA
BA <|-- RAA
BA <|-- GAA
BA <|-- DAA
BA <|-- ESA
BA <|-- GaA
BA <|-- AlA
BA <|-- HCA
BA <|-- ReA
BA <|-- CaA
BA <|-- RecA
BA <|-- RCA

' Activities usan Adapters
GAA --> AnA : usa
ESA --> EsAd : usa
GaA --> GaAd : usa
AlA --> AlAd : usa
HCA --> HcAd : usa

' Activities implementan interfaces
RAA ..|> AV : implements

' Activities usan Presenter
RAA --> AP : usa

' Presenter usa DAO
AP --> AD : delega

' Activities usan DAOs directamente
LA --> UD : usa
GAA --> AD : usa
DAA --> AD : usa
ESA --> ESD : usa
GaA --> GD : usa
AlA --> AliD : usa
HCA --> HCD : usa

' DAOs usan DatabaseHelper
UD --> DH : usa
AD --> DH : usa
ESD --> DH : usa
GD --> DH : usa
AliD --> DH : usa
HCD --> DH : usa

' DAOs manipulan Models
UD --> U : CRUD
AD --> An : CRUD
ESD --> ES : CRUD
GD --> G : CRUD
AliD --> Ali : CRUD
HCD --> HC : CRUD

' Adapters usan Models
AnA --> An : muestra
EsAd --> ES : muestra
GaAd --> G : muestra
AlAd --> Ali : muestra
HcAd --> HC : muestra

' DatabaseHelper accede a SQLite
DH --> DB : gestiona

' Activities acceden a SharedPreferences
LA --> SP : lee/escribe
MA --> SP : lee

' Relaciones FK entre Models
An "1" --> "*" ES : tiene
An "1" --> "*" G : tiene
An "1" --> "*" Ali : tiene
An "1" --> "*" HC : tiene

' Utils
CaA --> NH : usa
NH --> NR : dispara

@enduml
```

---

## Descripción de la Arquitectura Lógica

### Patrón Arquitectónico: MVC (Model-View-Controller)

AgroApp implementa el patrón **MVC (Model-View-Controller)** adaptado para Android, combinado con el patrón **DAO (Data Access Object)** para la capa de persistencia.

| Componente | Rol en AgroApp | Responsabilidad |
|------------|----------------|-----------------|
| **Model** | `models/` + `dao/` | Entidades (POJOs) y acceso a datos |
| **View** | `activity/` + `adapters/` + layouts XML | Interfaz de usuario |
| **Controller** | `presenter/` + Activities | Lógica de control y coordinación |

### Capas del Sistema

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| **Presentación (View)** | `activity`, `adapters` | Interfaces de usuario, captura de eventos, navegación |
| **Control (Controller)** | `presenter` | Lógica de negocio, validaciones, operaciones asíncronas |
| **Acceso a Datos (DAO)** | `dao` | Operaciones CRUD, consultas SQL, mapeo objeto-relacional |
| **Modelo (Entity)** | `models` | POJOs con atributos y accessors, sin lógica de negocio |
| **Infraestructura** | `database`, `utils` | Gestión de BD, notificaciones, utilidades |

### Componentes por Capa

#### Capa de Presentación (14 Activities)

| Componente | Descripción |
|------------|-------------|
| `LoginActivity` | Autenticación y registro de usuarios |
| `MainActivity` | Menú principal con navegación a módulos |
| `RegistroAnimalActivity` | Formulario de registro/edición de animales |
| `GestionAnimalesActivity` | Lista y filtrado de animales |
| `DetalleAnimalActivity` | Vista detallada de un animal |
| `EventosSanitariosActivity` | Gestión de eventos sanitarios |
| `GastosActivity` | Registro y seguimiento de gastos |
| `AlimentacionActivity` | Control de alimentación |
| `HistorialClinicoActivity` | Historial médico por animal |
| `ReportesActivity` | Generación de reportes y estadísticas |
| `CalendarioActivity` | Calendario con recordatorios |
| `RecomendacionesActivity` | Sugerencias de manejo |
| `RegistroComprasActivity` | Registro de compras |
| `BaseActivity` | Clase base abstracta con funcionalidad común |

#### Capa de Adaptadores (5 Adapters)

| Componente | Modelo Asociado |
|------------|-----------------|
| `AnimalAdapter` | `Animal` |
| `EventoSanitarioAdapter` | `EventoSanitario` |
| `GastoAdapter` | `Gasto` |
| `AlimentacionAdapter` | `Alimentacion` |
| `HistorialClinicoAdapter` | `HistorialClinico` |

#### Capa de Control (1 Controller)

| Componente | Responsabilidades |
|------------|-------------------|
| `AnimalPresenter` | Actúa como **Controller**: validaciones (arete, fechas, precios), procesamiento de imagen, operaciones asíncronas con ExecutorService, coordina Model y View |

#### Capa de Acceso a Datos (6 DAOs)

| Componente | Tabla BD |
|------------|----------|
| `UsuarioDAO` | `usuarios` |
| `AnimalDAO` | `animales` |
| `EventoSanitarioDAO` | `eventos_sanitarios` |
| `GastoDAO` | `gastos` |
| `AlimentacionDAO` | `alimentacion` |
| `HistorialClinicoDAO` | `historial_clinico` |

#### Capa de Modelo (6 Entities)

| Entidad | Atributos Principales |
|---------|----------------------|
| `Usuario` | id, username, password, nombre |
| `Animal` | id, numeroArete, raza, sexo, fechas, pesos, estado, foto |
| `EventoSanitario` | id, animalId, tipoEvento, fecha, costo |
| `Gasto` | id, animalId, tipo, monto, fecha |
| `Alimentacion` | id, animalId, tipoAlimento, cantidad, costo |
| `HistorialClinico` | id, animalId, diagnostico, tratamiento |

---

## Flujo de Comunicación entre Capas

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE PRESENTACIÓN                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Login     │  │    Main     │  │  Registro   │  │   Gestión   │  ...   │
│  │  Activity   │  │  Activity   │  │   Animal    │  │   Animales  │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼────────────────┼────────────────┼────────────────┼────────────────┘
          │                │                │                │
          │                │                │ implements     │
          │                │                ▼                │
          │                │         ┌─────────────┐        │
          │                │         │ AnimalView  │        │
          │                │         │ (Interface) │        │
          │                │         └──────┬──────┘        │
          │                │                │                │
          │                │                ▼                │
          │                │    ┌───────────────────┐       │
          │                │    │  AnimalPresenter  │       │
          │                │    │   (Controller)    │       │
          │                │    │  - Validaciones   │       │
          │                │    │  - Async ops      │       │
          │                │    └─────────┬─────────┘       │
          │                │              │                  │
          ▼                ▼              ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE ACCESO A DATOS (DAO)                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ UsuarioDAO  │  │  AnimalDAO  │  │  GastoDAO   │  │ EventoDAO   │  ...   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼────────────────┼────────────────┼────────────────┼────────────────┘
          │                │                │                │
          ▼                ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE INFRAESTRUCTURA                              │
│                      ┌─────────────────────┐                                 │
│                      │   DatabaseHelper    │                                 │
│                      │  (SQLiteOpenHelper) │                                 │
│                      └──────────┬──────────┘                                 │
└─────────────────────────────────┼───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ALMACENAMIENTO                                       │
│         ┌─────────────────┐              ┌─────────────────┐                │
│         │     SQLite      │              │ SharedPreferences│                │
│         │   ganado.db     │              │   AgroAppPrefs   │                │
│         └─────────────────┘              └─────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Relaciones del Modelo de Datos

```
┌─────────────┐
│   Usuario   │
│  (1 único)  │
└─────────────┘

┌─────────────┐       ┌──────────────────┐
│   Animal    │──1:N──│ EventoSanitario  │
│  (arete PK) │       └──────────────────┘
│             │       ┌──────────────────┐
│             │──1:N──│      Gasto       │
│             │       └──────────────────┘
│             │       ┌──────────────────┐
│             │──1:N──│   Alimentacion   │
│             │       └──────────────────┘
│             │       ┌──────────────────┐
│             │──1:N──│ HistorialClinico │
└─────────────┘       └──────────────────┘
```

---

## Principios de Diseño Aplicados

| Principio | Aplicación en AgroApp |
|-----------|----------------------|
| **Separación de Responsabilidades** | Cada capa tiene responsabilidades específicas y bien definidas |
| **Inversión de Dependencias** | Controller depende de interfaz `AnimalView`, no de implementación concreta |
| **Patrón Repository/DAO** | DAOs encapsulan toda la lógica de acceso a datos |
| **Herencia** | `BaseActivity` provee funcionalidad común a todas las Activities |
| **Composición** | Activities componen Adapters para mostrar listas |
| **Inyección de Dependencias** | DAOs reciben `DatabaseHelper` por constructor |

---

## Notas Técnicas

### Operaciones Asíncronas
El `AnimalPresenter` (actuando como Controller) utiliza `ExecutorService` para ejecutar operaciones de BD en hilo secundario, evitando bloquear el hilo UI.

### Comunicación View-Controller
La interfaz `AnimalView` define el contrato de comunicación:
- `mostrarExito()` / `mostrarError()` - Feedback al usuario
- `cerrarActividad()` - Navegación
- `ejecutarEnUIThread()` - Actualización de UI desde hilo secundario

### Identificador Único
El sistema usa el **número de arete SINIGA** (10 dígitos) como identificador visible para el usuario, mientras que el **ID interno** se usa para relaciones FK en la base de datos.
