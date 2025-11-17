# Notas Técnicas para Desarrolladores - AgroApp

## Arquitectura del Proyecto

### Patrón de Diseño
La aplicación implementa una arquitectura **MVP simplificada con capa DAO**:

```
View (Activities) → Presenter Logic (dentro de Activities) → DAO → Database
                ↓
            Models (POJOs)
```

### Estructura de Paquetes

```
com.example.agroapp/
├── database/
│   └── DatabaseHelper.java          # Singleton para SQLite
├── models/
│   ├── Usuario.java
│   ├── Animal.java
│   ├── EventoSanitario.java
│   ├── HistorialClinico.java
│   ├── Gasto.java
│   └── Alimentacion.java
├── dao/
│   ├── UsuarioDAO.java
│   ├── AnimalDAO.java
│   ├── EventoSanitarioDAO.java
│   ├── HistorialClinicoDAO.java
│   ├── GastoDAO.java
│   └── AlimentacionDAO.java
├── adapters/
│   ├── AnimalAdapter.java
│   ├── EventoSanitarioAdapter.java
│   ├── HistorialClinicoAdapter.java
│   ├── GastoAdapter.java
│   └── AlimentacionAdapter.java
├── utils/
│   ├── NotificationReceiver.java    # BroadcastReceiver
│   └── NotificationHelper.java      # Gestión de alarmas
└── [Activities - 11 archivos]
```

## Base de Datos

### Esquema SQLite

**Versión:** 1  
**Nombre:** agroapp.db  

#### Tablas y Relaciones

```sql
usuarios (id, username, password, nombre)
    ↓
animales (id, usuario_id, arete, nombre, raza, sexo, ...)
    ↓ (cascada)
    ├── calendario_sanitario (id, animal_id, tipo, fechas, ...)
    ├── historial_clinico (id, animal_id, fecha, enfermedad, ...)
    ├── gastos (id, animal_id, tipo, concepto, monto, ...)
    └── alimentacion (id, animal_id, tipo_alimento, cantidad, ...)
```

### Foreign Keys
- **HABILITADAS** con `PRAGMA foreign_keys=ON` en `onConfigure()`
- **Eliminación en cascada:** Al eliminar un animal, se eliminan automáticamente todos sus registros relacionados

### Usuario Predeterminado
```java
username: "admin"
password: "admin123"
nombre: "Administrador"
```

## Activities y Navegación

### LoginActivity (LAUNCHER)
- **Layout:** `activity_login.xml`
- **Funcionalidad:** Autenticación de usuario
- **SharedPreferences:** Guarda `userId` y `userName` para persistencia de sesión
- **Navegación:** → MainActivity

### MainActivity
- **Layout:** `activity_main.xml` (GridLayout con 8 CardViews)
- **Funcionalidad:** Menú principal
- **Navegación:** Lanza las 7 activities secundarias o cierra sesión

### GestionAnimalesActivity
- **Layout:** `activity_gestion_animales.xml`
- **Componentes:** RecyclerView + SearchView + Spinner de filtro + FAB
- **Funcionalidad:** Lista, busca y filtra animales
- **Navegación:** → RegistroAnimalActivity (nuevo), DetalleAnimalActivity (detalle)

### RegistroAnimalActivity
- **Layout:** `activity_registro_animal.xml`
- **Modo:** Dual (nuevo/editar)
- **Componentes:** 3 Spinners (raza, sexo, estado), 2 DatePickers, ImagePicker
- **Validación:** Campos obligatorios, formato de números
- **Intent Extra:** `ANIMAL_ID` (opcional, para edición)

### DetalleAnimalActivity
- **Layout:** `activity_detalle_animal.xml`
- **Funcionalidad:** Vista completa del animal + acciones
- **Navegación:** → 4 activities relacionadas (Historial, Eventos, Gastos, Alimentación)
- **Acciones:** Editar, Eliminar (con confirmación)

### CalendarioActivity
- **Layout:** `activity_calendario.xml`
- **Dialog:** `dialog_evento_sanitario.xml`
- **Componentes:** RecyclerView + Spinner filtro + FAB
- **Notificaciones:** Integra `NotificationHelper` para programar alarmas
- **Funcionalidad:** CRUD de eventos sanitarios

### EventosSanitariosActivity
- **Layout:** `activity_eventos_sanitarios.xml`
- **Funcionalidad:** Vista de eventos por animal específico
- **Intent Extra:** `ANIMAL_ID` (requerido)

### HistorialClinicoActivity
- **Layout:** `activity_historial_clinico.xml`
- **Dialog:** `dialog_historial_clinico.xml`
- **Intent Extra:** `ANIMAL_ID` (requerido)

### GastosActivity
- **Layout:** `activity_gastos.xml`
- **Dialog:** `dialog_gasto.xml`
- **Intent Extra:** `ANIMAL_ID` (opcional, para filtrar)
- **Spinner Tipos:** Alimento, Medicamento, Vacuna, Equipo, Mano de Obra, Infraestructura, Otro

### AlimentacionActivity
- **Layout:** `activity_alimentacion.xml`
- **Dialog:** `dialog_alimentacion.xml`
- **Intent Extra:** `ANIMAL_ID` (requerido)
- **Spinners:** Tipo (Pasto, Forraje, Concentrado, etc.), Unidad (kg, g, toneladas, etc.)

### ReportesActivity
- **Layout:** `activity_reportes.xml`
- **Funcionalidad:** Estadísticas + generación de PDF
- **PDF:** Usa `android.graphics.pdf.PdfDocument`
- **Destino:** `Environment.DIRECTORY_DOWNLOADS`

### RecomendacionesActivity
- **Layout:** `activity_recomendaciones.xml`
- **Funcionalidad:** Contenido estático de recomendaciones nutricionales
- **Método:** `cargarRecomendaciones()` construye un String formateado

## Sistema de Notificaciones

### Componentes

#### NotificationHelper.java
```java
programarNotificacion(Context, EventoSanitario)  // Crea alarma
cancelarNotificacion(Context, eventoId)          // Cancela alarma
reprogramarNotificacion(Context, EventoSanitario) // Actualiza alarma
```

#### NotificationReceiver.java
```java
onReceive() → crearCanalNotificacion() → mostrarNotificacion()
```

### Configuración de Alarma
- **Trigger:** 1 día antes del evento a las 9:00 AM
- **Tipo:** `RTC_WAKEUP` con `setExactAndAllowWhileIdle()`
- **PendingIntent:** `FLAG_IMMUTABLE` para Android 12+
- **Request Code:** ID del evento (permite múltiples alarmas únicas)

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

## Adaptadores RecyclerView

### Patrón Común
```java
public interface OnItemClickListener {
    void onItemClick(Model item);
}

private List<Model> lista;
private OnItemClickListener listener;

// ViewHolder interno estático
// onCreateViewHolder() infla item layout
// onBindViewHolder() vincula datos
// getItemCount() retorna lista.size()
```

### Layouts de Items
- `item_animal.xml` - Arete, nombre, raza, estado (badge coloreado)
- `item_evento_sanitario.xml` - Animal, tipo, fecha, descripción, estado
- `item_historial_clinico.xml` - Fecha, enfermedad, tratamiento, estado
- `item_gasto.xml` - Animal, tipo, concepto, monto (formato moneda), fecha
- `item_alimentacion.xml` - Animal, tipo alimento, cantidad+unidad, fecha

## Gestión de Imágenes

### Selección de Foto
```java
Intent intent = new Intent(Intent.ACTION_PICK);
intent.setType("image/*");
startActivityForResult(intent, 100);
```

### Almacenamiento
- Las fotos NO se copian, solo se guarda el **URI** como String en SQLite
- Campo: `Animal.fotoUri` (TEXT)
- Carga: `ImageView.setImageURI(Uri.parse(uriString))`

### Permisos
- Android < 10: `READ_EXTERNAL_STORAGE`
- Android >= 13: `READ_MEDIA_IMAGES`

## Generación de PDFs

### Implementación en ReportesActivity

```java
PdfDocument pdfDocument = new PdfDocument();
PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
PdfDocument.Page page = pdfDocument.startPage(pageInfo);

Canvas canvas = page.getCanvas();
Paint paint = new Paint();
// Dibujar texto, líneas, etc.

pdfDocument.finishPage(page);

File file = new File(downloadsDir, fileName);
FileOutputStream fos = new FileOutputStream(file);
pdfDocument.writeTo(fos);
pdfDocument.close();
```

### Permisos
- Android < 10: `WRITE_EXTERNAL_STORAGE`
- Android >= 10: No requiere permiso para `DIRECTORY_DOWNLOADS`

## Recursos (res/)

### Strings (values/strings.xml)
18 strings en español para UI común

### Colors (values/colors.xml)
```xml
primary: #4CAF50 (verde agrícola)
accent: #FF9800 (naranja)
estado_sano: #4CAF50
estado_enfermo: #F44336
estado_vendido: #2196F3
estado_muerto: #9E9E9E
```

### Layouts
- **11 Activity Layouts**
- **5 Item Layouts** (RecyclerView)
- **4 Dialog Layouts** (AlertDialog personalizados)

## Dependencias Gradle

```kotlin
// Core
implementation(libs.appcompat)
implementation(libs.material)

// UI Components
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
```

## Configuración Build

```kotlin
compileSdk = 36
minSdk = 27  // Android 8.0 Oreo
targetSdk = 36

compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
```

## Mejores Prácticas Implementadas

### 1. Singleton para Database
```java
private static DatabaseHelper instance;
public static synchronized DatabaseHelper getInstance(Context context) {
    if (instance == null) {
        instance = new DatabaseHelper(context.getApplicationContext());
    }
    return instance;
}
```

### 2. Cierre de Cursores
Todos los DAOs implementan `finally { if (cursor != null) cursor.close(); }`

### 3. Manejo de Excepciones
Try-catch en operaciones de:
- Base de datos
- Archivos (PDF, imágenes)
- Parsing de fechas
- Notificaciones

### 4. Validaciones
- Campos obligatorios antes de guardar
- Formato de números decimales
- Fechas válidas
- Estados consistentes

### 5. UI Thread Safety
- Operaciones de DB en método `onCreate()` o eventos de UI (no requiere AsyncTask para operaciones rápidas)
- Para operaciones pesadas futuras, considerar `ExecutorService`

## Debugging

### Logs Útiles
```java
Log.d("AgroApp", "DatabaseHelper: DB creada exitosamente");
Log.e("AgroApp", "Error al insertar animal: " + e.getMessage());
```

### Inspección de DB
```bash
adb shell
cd /data/data/com.example.agroapp/databases/
sqlite3 agroapp.db
.tables
.schema animales
SELECT * FROM animales;
```

### Permisos en Runtime
Para Android 6.0+, implementar verificación de permisos:
```java
if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
}
```

## Migración de Base de Datos

### onUpgrade()
Actualmente implementa DROP + CREATE. Para producción:

```java
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 2) {
        db.execSQL("ALTER TABLE animales ADD COLUMN nueva_columna TEXT");
    }
    if (oldVersion < 3) {
        // Migración a versión 3
    }
}
```

## Testing

### Test Unitarios Sugeridos
- [ ] DAOs: CRUD completo para cada entidad
- [ ] DatabaseHelper: Creación de tablas y foreign keys
- [ ] NotificationHelper: Programación de alarmas
- [ ] Validaciones: Campos requeridos, formatos

### Test de UI Sugeridos
- [ ] Login con credenciales válidas/inválidas
- [ ] Navegación entre activities
- [ ] CRUD de animales
- [ ] Filtros y búsquedas
- [ ] Generación de PDF

## Problemas Conocidos y Soluciones

### 1. Deprecated Locale Constructor
**Ubicación:** `GastoAdapter.java`
```java
// Actual (funciona pero deprecated)
Locale locale = new Locale("es", "MX");

// Solución futura
Locale locale = new Locale.Builder().setLanguage("es").setRegion("MX").build();
```

### 2. CalendarioActivity - IDs de Dialog
**Problema:** Dialog usa IDs diferentes a los referenciados
**Solución:** Actualizar los IDs en `dialog_evento_sanitario.xml` para que coincidan

### 3. Gestión de Memoria con Fotos
**Problema:** Cargar imágenes grandes puede causar OutOfMemoryError
**Solución:** Implementar compresión/redimensionado antes de mostrar:
```java
BitmapFactory.Options options = new BitmapFactory.Options();
options.inSampleSize = 4; // Reduce tamaño
Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
```

## Próximos Pasos de Desarrollo

### Prioridad Alta
1. Probar en dispositivo físico con Android 8.0+
2. Verificar permisos en Android 13+
3. Validar notificaciones en diferentes versiones
4. Optimizar carga de imágenes

### Prioridad Media
1. Implementar respaldo/restauración de DB
2. Agregar gráficas con MPAndroidChart
3. Mejorar UI con animaciones
4. Implementar modo oscuro

### Prioridad Baja
1. Soporte multilenguaje (inglés)
2. Widget de escritorio
3. Integración con calendario del sistema
4. Exportar datos a CSV/Excel

## Contacto y Contribuciones

Para modificaciones o mejoras al proyecto:
1. Documenta cambios en CHANGELOG.md
2. Actualiza versión en build.gradle.kts
3. Prueba en múltiples versiones de Android
4. Actualiza este documento técnico

---

**Última actualización:** 2024  
**Versión del documento:** 1.0
