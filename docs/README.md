# AgroApp - Sistema de Gestión Ganadera

## 📋 Descripción
AgroApp es una aplicación móvil Android diseñada para pequeños productores ganaderos de Michoacán, México. Permite gestionar ganado de manera integral y completamente offline utilizando SQLite para almacenamiento local.

---

## 🚀 Características Principales

### 1. Gestión de Animales
- Registro completo con validación de arete de 10 dígitos numéricos
- Captura de foto por cámara o galería
- Búsqueda y filtrado por estado (Sano, Enfermo, Vendido, Muerto)
- Vista detallada con información completa del animal
- Edición y eliminación con validaciones

### 2. Calendario Sanitario
- Programación de eventos sanitarios (Vacunas, Desparasitación, Vitaminas)
- Notificaciones automáticas un día antes del evento
- Seguimiento del estado (Pendiente/Realizado)
- Badge visual de eventos próximos

### 3. Historial Clínico
- Registro de enfermedades, síntomas y tratamientos
- Seguimiento del estado del tratamiento
- Observaciones detalladas por caso

### 4. Control de Gastos
- Registro de inversiones por categoría
- Asociación opcional con animales específicos
- Cálculo automático de inversión total por animal
- Totales y promedios

### 5. Registro de Alimentación
- Control de tipo y cantidad de alimento
- Historial por animal
- Múltiples unidades de medida

### 6. Reportes y Estadísticas
- Estadísticas en tiempo real del hato
- Generación de reportes en PDF
- Almacenamiento automático en carpeta Descargas

### 7. Sistema de Autenticación
- Doble botón: Login y Registro separados
- Validación en base de datos
- Sesión persistente con timeout de seguridad (10 segundos de inactividad)
- Contraseña almacenada para revalidación

---

## 🧠 Algoritmos y Lógica Principal

### 1. **Sistema de Autenticación (LoginActivity)**

#### Algoritmo de Inicio de Sesión
```
FUNCIÓN iniciarSesion():
    1. Capturar usuario y contraseña
    2. Validar campos no vacíos
    3. Consultar BD: validarUsuario(username, password)
    4. SI usuario existe Y contraseña correcta:
        a. Guardar sesión (userId, userName, password)
        b. Redirigir a MainActivity
    5. SINO SI usuario existe PERO contraseña incorrecta:
        a. Mostrar "Contraseña incorrecta"
    6. SINO:
        a. Mostrar "Usuario no existe. Use Registrar Usuario"
```

#### Algoritmo de Registro
```
FUNCIÓN registrarUsuario():
    1. Capturar usuario y contraseña
    2. Validar campos no vacíos
    3. Verificar si usuario ya existe en BD
    4. SI usuario existe:
        a. Mostrar "Usuario ya existe. Use Iniciar Sesión"
    5. SINO:
        a. Crear objeto Usuario
        b. Insertar en BD
        c. Guardar sesión
        d. Redirigir a MainActivity
```

**Código Clave:**
```java
// Validación de usuario existente
Usuario usuario = usuarioDAO.validarUsuario(username, password);

// Verificación de existencia
Usuario usuarioExistente = usuarioDAO.obtenerPorUsername(username);

// Guardado de sesión con contraseña
editor.putString("password", usuario.getPassword());
```

---

### 2. **Validación de Arete (RegistroAnimalActivity)**

#### Algoritmo de Validación
```
FUNCIÓN guardarAnimal():
    1. Capturar número de arete (trim())
    2. VALIDACIÓN #1: Campo no vacío
    3. VALIDACIÓN #2: Longitud exacta = 10 caracteres
    4. VALIDACIÓN #3: Solo dígitos numéricos (regex: \d{10})
    5. SI todas las validaciones pasan:
        a. Crear objeto Animal
        b. Asignar datos (incluida foto en Base64)
        c. Insertar o actualizar en BD
    6. SINO:
        a. Mostrar mensaje de error específico
```

**Código Clave:**
```java
// Validación de longitud exacta
if (arete.length() != 10) {
    Toast.makeText(this, "Debe tener exactamente 10 caracteres", LENGTH_SHORT).show();
    return;
}

// Validación de solo números
if (!arete.matches("\\d{10}")) {
    Toast.makeText(this, "Debe contener solo números (10 dígitos)", LENGTH_SHORT).show();
    return;
}
```

**Layout XML:**
```xml
<EditText
    android:inputType="number"
    android:maxLength="10"
    android:hint="Arete (10 dígitos)"/>
```

---

### 3. **Sistema de Captura de Fotos**

#### Algoritmo de Selección de Foto
```
FUNCIÓN seleccionarFoto():
    1. Crear Intent de tipo ACTION_PICK
    2. Especificar EXTERNAL_CONTENT_URI
    3. Iniciar activity con código PICK_IMAGE
    
FUNCIÓN onActivityResult():
    SI requestCode == PICK_IMAGE Y resultCode == OK:
        1. Obtener URI de la imagen
        2. Convertir URI a Bitmap
        3. Redimensionar a máximo 800px
        4. Comprimir a JPEG 80%
        5. Convertir a Base64
        6. Almacenar en variable fotoBase64
        7. Mostrar en ImageView
```

#### Algoritmo de Captura con Cámara
```
FUNCIÓN verificarPermisosCamara():
    SI permiso CAMERA no otorgado:
        Solicitar permiso
    SINO:
        Llamar tomarFoto()

FUNCIÓN tomarFoto():
    1. Crear archivo temporal con timestamp
    2. Obtener URI usando FileProvider
    3. Crear Intent ACTION_IMAGE_CAPTURE
    4. Adjuntar URI como EXTRA_OUTPUT
    5. Iniciar activity con código TAKE_PHOTO

FUNCIÓN crearArchivoImagen():
    1. Generar nombre: "ANIMAL_yyyyMMdd_HHmmss.jpg"
    2. Crear en directorio PICTURES de la app
    3. Guardar path absoluto
    4. Retornar File
```

**Código Clave:**
```java
// Redimensionamiento inteligente
float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
int newWidth = Math.round(width * ratio);
int newHeight = Math.round(height * ratio);
Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

// Conversión a Base64
ByteArrayOutputStream baos = new ByteArrayOutputStream();
resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
byte[] imageBytes = baos.toByteArray();
fotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

// Decodificación para mostrar
byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
ivFotoAnimal.setImageBitmap(decodedByte);
```

**FileProvider Configuration (file_paths.xml):**
```xml
<paths>
    <external-path name="external_files" path="."/>
    <external-cache-path name="external_cache" path="." />
    <cache-path name="cache" path="." />
</paths>
```

---

### 4. **Carga Asíncrona de Datos (GestionAnimalesActivity)**

#### Algoritmo de Threading
```
FUNCIÓN cargarAnimales():
    Executor.execute(() -> {
        // HILO SECUNDARIO (Background)
        1. Consultar BD: obtenerTodosLosAnimales()
        2. Almacenar resultado en lista temporal
        
        Handler.post(() -> {
            // HILO PRINCIPAL (UI Thread)
            3. Limpiar lista del adaptador
            4. Agregar nuevos datos
            5. Notificar cambios: adapter.notifyDataSetChanged()
        })
    })
```

**Código Clave:**
```java
private final ExecutorService executor = Executors.newSingleThreadExecutor();
private final Handler handler = new Handler(Looper.getMainLooper());

private void cargarAnimales() {
    executor.execute(() -> {
        // Operación pesada en segundo plano
        List<Animal> animalesDesdeDB = animalDAO.obtenerTodosLosAnimales();
        
        // Actualización de UI en hilo principal
        handler.post(() -> {
            animalesList.clear();
            animalesList.addAll(animalesDesdeDB);
            adapter.notifyDataSetChanged();
        });
    });
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (executor != null && !executor.isShutdown()) {
        executor.shutdown();
    }
}
```

**Patrón Implementado:** Producer-Consumer con ExecutorService

---

### 5. **Sistema de Filtrado (GestionAnimalesActivity)**

#### Algoritmo de Filtro Combinado
```
FUNCIÓN aplicarFiltros(textoBusqueda):
    1. Limpiar lista actual
    2. Para cada animal en lista completa:
        a. VERIFICAR estado: estadoFiltro == "Todos" O estado coincide
        b. VERIFICAR texto: vacío O arete contiene O raza contiene
        c. SI ambas condiciones TRUE:
            - Agregar a lista filtrada
    3. Notificar cambios al adaptador
```

**Código Clave:**
```java
private void aplicarFiltros(String texto) {
    animalesList.clear();
    String textoBusqueda = texto.toLowerCase().trim();
    
    for (Animal animal : animalesListFull) {
        // Filtro por estado
        boolean cumpleEstado = estadoFiltro.equals("Todos") || 
            (animal.getEstado() != null && animal.getEstado().equals(estadoFiltro));
        
        // Filtro por texto
        boolean cumpleTexto = textoBusqueda.isEmpty() ||
            (animal.getNumeroArete() != null && 
             animal.getNumeroArete().toLowerCase().contains(textoBusqueda)) ||
            (animal.getRaza() != null && 
             animal.getRaza().toLowerCase().contains(textoBusqueda));
        
        if (cumpleEstado && cumpleTexto) {
            animalesList.add(animal);
        }
    }
    adapter.notifyDataSetChanged();
}
```

---

### 6. **Cálculo de Inversión Total (MainActivity)**

#### Algoritmo de Estadísticas en Tiempo Real
```
FUNCIÓN cargarEstadisticas():
    Executor.execute(() -> {
        1. Obtener todos los animales de BD
        2. Inicializar contadores: activos=0, sanos=0, vendidos=0, muertos=0
        3. Para cada animal:
            SI tiene fechaSalida:
                vendidos++
            SINO SI estado == "Muerto":
                muertos++
            SINO:
                activos++
                SI estado == "Sano":
                    sanos++
        
        4. Obtener eventos sanitarios pendientes
        5. Filtrar eventos en próximos 7 días
        6. Contar vacunas pendientes
        
        Handler.post(() -> {
            7. Actualizar TextViews con estadísticas
            8. Mostrar/ocultar alerta de vacunas
        })
    })
```

**Código Clave:**
```java
// Cálculo de inversión total por animal
double precioCompra = animal.getPrecioCompra();
double totalGastos = gastoDAO.obtenerTotalGastosPorAnimal(animalId);
double inversionTotal = precioCompra + totalGastos;

// Cálculo de ganancia (si está vendido)
if (animal.getFechaSalida() != null && animal.getPrecioVenta() > 0) {
    double ganancia = animal.getPrecioVenta() - inversionTotal;
    tvGanancia.setText((ganancia >= 0 ? "Ganancia: " : "Pérdida: ") + 
            currencyFormatter.format(Math.abs(ganancia)));
}
```

---

### 7. **Sistema de Timeout de Sesión (BaseActivity)**

#### Algoritmo de Seguridad
```
FUNCIÓN onResume():
    1. Obtener tiempoUltimaActividad de SharedPreferences
    2. Obtener tiempoActual del sistema
    3. Calcular diferencia = tiempoActual - tiempoUltimaActividad
    4. SI diferencia > TIMEOUT (10 segundos):
        a. Mostrar diálogo de reautenticación
        b. Solicitar contraseña
        c. Verificar contra contraseña guardada en SharedPreferences
        d. SI correcta: actualizar tiempo y continuar
        e. SI incorrecta: redirigir a LoginActivity

FUNCIÓN onPause():
    1. Guardar System.currentTimeMillis() en SharedPreferences
```

**Código Clave:**
```java
private static final long SESSION_TIMEOUT = 10000; // 10 segundos

private void verificarSesion() {
    if (this instanceof LoginActivity) return;
    
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    long ultimaActividad = prefs.getLong(KEY_LAST_ACTIVITY_TIME, 0);
    long tiempoActual = System.currentTimeMillis();
    
    if (ultimaActividad > 0 && (tiempoActual - ultimaActividad) > SESSION_TIMEOUT) {
        mostrarDialogoContraseña();
    }
}

private void verificarContraseña(String password) {
    SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
    String passwordGuardada = prefs.getString("password", "");
    
    if (password.equals(passwordGuardada)) {
        guardarTiempoActividad();
        Toast.makeText(this, "Sesión reanudada", LENGTH_SHORT).show();
    } else {
        volverAlLogin();
    }
}
```

---

## 🗄️ Estructura de Base de Datos

### Esquema SQLite

#### Tabla: animales
```sql
CREATE TABLE animales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_arete TEXT NOT NULL UNIQUE,
    nombre TEXT,
    raza TEXT,
    sexo TEXT,
    fecha_nacimiento TEXT,
    fecha_ingreso TEXT,
    precio_compra REAL,
    precio_venta REAL,
    fecha_salida TEXT,
    estado TEXT,
    observaciones TEXT,
    foto TEXT  -- Base64 encoded
)
```

#### Tabla: usuarios
```sql
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    nombre TEXT
)
```

#### Tabla: calendario_sanitario
```sql
CREATE TABLE calendario_sanitario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    animal_id INTEGER,
    tipo_evento TEXT,
    descripcion TEXT,
    fecha_programada TEXT,
    fecha_realizada TEXT,
    estado TEXT,
    recordatorio INTEGER,
    FOREIGN KEY(animal_id) REFERENCES animales(id) ON DELETE CASCADE
)
```

#### Tabla: gastos
```sql
CREATE TABLE gastos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    animal_id INTEGER,
    tipo TEXT,
    descripcion TEXT,
    monto REAL,
    fecha TEXT,
    FOREIGN KEY(animal_id) REFERENCES animales(id) ON DELETE CASCADE
)
```

---

## 🎨 Paleta de Colores

La aplicación utiliza una paleta natural inspirada en el campo:

| Color | Hex | RGB | Uso |
|-------|-----|-----|-----|
| Beige Claro | `#faf4de` | 250, 244, 222 | Background |
| Tan | `#c78f52` | 199, 143, 82 | Accent/Botones |
| Sage | `#98a287` | 152, 162, 135 | Texto secundario |
| Verde Bosque | `#41692a` | 65, 105, 42 | Primary/Estados sanos |
| Azul Teal | `#0188a8` | 1, 136, 168 | Vendidos/Info |
| Marrón Oscuro | `#6d3e14` | 109, 62, 20 | Texto principal/Dark |

---

## 🛠️ Tecnologías y Patrones

### Arquitectura
- **Patrón:** DAO (Data Access Object)
- **Threading:** ExecutorService + Handler
- **Persistencia:** SQLite + SharedPreferences

### Componentes Clave
```
├── Activities (Vista)
│   ├── LoginActivity (Autenticación)
│   ├── MainActivity (Dashboard)
│   ├── GestionAnimalesActivity (Lista)
│   ├── RegistroAnimalActivity (CRUD)
│   └── DetalleAnimalActivity (Vista detallada)
│
├── DAO (Acceso a datos)
│   ├── AnimalDAO
│   ├── UsuarioDAO
│   ├── EventoSanitarioDAO
│   └── GastoDAO
│
├── Models (Entidades)
│   ├── Animal
│   ├── Usuario
│   ├── EventoSanitario
│   └── Gasto
│
├── Database
│   └── DatabaseHelper (SQLiteOpenHelper)
│
└── Utils
    ├── NotificationReceiver (Alarmas)
    └── FileProvider (Fotos)
```

---

## 📱 Requisitos Técnicos

- **Android:** 8.0 (API 27) - Android 14 (API 34)
- **Espacio:** 50 MB mínimo
- **RAM:** 2 GB recomendado
- **Permisos:**
  - `CAMERA` - Captura de fotos
  - `READ_MEDIA_IMAGES` - Acceso a galería (Android 13+)
  - `READ/WRITE_EXTERNAL_STORAGE` - PDFs y fotos (Android 12-)
  - `POST_NOTIFICATIONS` - Notificaciones de eventos
  - `SCHEDULE_EXACT_ALARM` - Recordatorios precisos

---

## 🔧 Instalación y Configuración

### 1. Clonar Repositorio
```bash
git clone https://github.com/LuisErnesto221104/AgroApp.git
cd AgroApp
```

### 2. Abrir en Android Studio
```
File → Open → Seleccionar carpeta AgroApp
```

### 3. Sincronizar Gradle
```
Sync Project with Gradle Files
```

### 4. Configurar Emulador o Dispositivo
```
- Emulador: AVD con Android 8.0+
- Físico: Habilitar Depuración USB
```

### 5. Ejecutar
```
Run → Run 'app' (Shift + F10)
```

---

## 📊 Complejidad Algorítmica

| Operación | Complejidad | Descripción |
|-----------|-------------|-------------|
| Login | O(1) | Query indexed por username |
| Cargar animales | O(n) | Lectura completa de tabla |
| Filtrar animales | O(n·m) | n animales, m longitud texto |
| Guardar animal | O(1) | Insert/Update directo |
| Calcular estadísticas | O(n) | Un recorrido sobre animales |
| Convertir foto Base64 | O(k) | k = tamaño imagen |

---

## 🚀 Optimizaciones Implementadas

1. **Threading:** Operaciones de BD en segundo plano
2. **Lazy Loading:** Adaptadores con ViewHolder pattern
3. **Caching:** Lista completa separada para filtros
4. **Compresión:** Imágenes redimensionadas a 800px + JPEG 80%
5. **Indexación:** Primary keys y foreign keys en BD
6. **Singleton:** DatabaseHelper única instancia

---

## 🐛 Solución de Problemas

### Error: ANR (App Not Responding)
**Causa:** Operación de BD en hilo principal  
**Solución:** Usar ExecutorService como en GestionAnimalesActivity

### Error: FileProvider URI
**Causa:** Falta configuración en AndroidManifest  
**Solución:** Agregar `<provider>` y `file_paths.xml`

### Error: Cámara no disponible
**Causa:** Falta `<queries>` en Manifest (Android 11+)  
**Solución:** Agregar intent-filter para IMAGE_CAPTURE

---

## 📄 Licencia
Uso privado - Desarrollado para pequeños ganaderos de Michoacán

## 👨‍💻 Autor
**Luis Ernesto**  
GitHub: [@LuisErnesto221104](https://github.com/LuisErnesto221104)

---

**¡Gestiona tu ganado de manera profesional con AgroApp!** 🐄
