# Guía de Compilación y Ejecución - AgroApp

## Requisitos Previos

### Software Necesario
1. **Android Studio** (versión recomendada: Hedgehog 2023.1.1 o superior)
   - Descargar desde: https://developer.android.com/studio
   
2. **JDK 11** o superior
   - Incluido con Android Studio o descargar desde: https://adoptium.net/

3. **Android SDK**
   - API Level 27 (Android 8.0) como mínimo
   - API Level 36 para compilación (se descarga automáticamente)

### Dispositivo de Prueba
- **Emulador Android:** Pixel 5 o similar con Android 8.0+
- **Dispositivo físico:** Android 8.0 (Oreo) o superior con modo desarrollador habilitado

## Pasos de Instalación

### 1. Abrir el Proyecto en Android Studio

```bash
# Opción A: Desde la línea de comandos
cd C:\Users\ernes\AndroidStudioProjects\AgroApp
studio .

# Opción B: Desde Android Studio
File > Open > Seleccionar carpeta AgroApp
```

### 2. Sincronizar el Proyecto con Gradle

Una vez abierto el proyecto, Android Studio mostrará una notificación:

```
"Gradle files have changed since last project sync. A project sync may be necessary..."
```

**Acción:** Hacer clic en **"Sync Now"**

Alternativamente:
- Menú: `File > Sync Project with Gradle Files`
- Atajo: `Ctrl+Shift+O` (Windows/Linux) o `Cmd+Shift+O` (Mac)

**Tiempo estimado:** 2-5 minutos (primera vez)

### 3. Descargar Dependencias

Gradle descargará automáticamente las siguientes dependencias:

- androidx.appcompat:appcompat
- com.google.android.material:material
- androidx.cardview:cardview:1.0.0
- androidx.recyclerview:recyclerview:1.3.2
- androidx.constraintlayout:constraintlayout:2.1.4
- androidx.coordinatorlayout:coordinatorlayout:1.2.0

**Estado:** Observar la barra de progreso en la parte inferior de Android Studio.

### 4. Resolver Posibles Errores

#### Error: "SDK Platform 36 not found"
**Solución:**
1. Ir a `Tools > SDK Manager`
2. En la pestaña `SDK Platforms`, marcar `Android API 36`
3. Hacer clic en `Apply` y esperar la descarga
4. Volver a sincronizar con Gradle

#### Error: "Build Tools version not found"
**Solución:**
1. Ir a `Tools > SDK Manager`
2. En la pestaña `SDK Tools`, verificar que `Android SDK Build-Tools` esté instalado
3. Si no está, instalarlo y sincronizar nuevamente

#### Error: "JDK not found"
**Solución:**
1. Ir a `File > Project Structure > SDK Location`
2. Seleccionar un JDK 11 o superior
3. Aplicar y sincronizar

### 5. Verificar la Configuración

**Verificar que no hay errores:**
- La pestaña `Build` en la parte inferior NO debe mostrar errores rojos
- Los archivos Java NO deben tener subrayados rojos (excepto advertencias menores)

**Nota:** Es normal ver advertencias sobre constructores deprecados (Locale). Esto no impide la compilación.

## Compilación

### Opción 1: Desde Android Studio (GUI)

1. **Build APK:**
   - Menú: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
   - Esperar mensaje: `APK(s) generated successfully`
   - Ubicación: `app/build/outputs/apk/debug/app-debug.apk`

2. **Build y Run:**
   - Botón verde `Run ▶` en la barra de herramientas
   - O presionar `Shift+F10`

### Opción 2: Desde la Línea de Comandos

**En Windows (PowerShell):**
```powershell
# Navegar al directorio del proyecto
cd C:\Users\ernes\AndroidStudioProjects\AgroApp

# Limpiar build anterior
.\gradlew clean

# Compilar APK de debug
.\gradlew assembleDebug

# APK generado en:
# app\build\outputs\apk\debug\app-debug.apk
```

**En Linux/Mac (Terminal):**
```bash
cd /path/to/AgroApp
./gradlew clean
./gradlew assembleDebug
```

### Build Release (APK Firmado)

Para generar un APK de producción firmado:

1. **Crear Keystore:**
```bash
keytool -genkey -v -keystore agroapp-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias agroapp
```

2. **Configurar build.gradle.kts:**
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("agroapp-release-key.jks")
            storePassword = "your_password"
            keyAlias = "agroapp"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

3. **Compilar Release:**
```bash
./gradlew assembleRelease
```

## Ejecución

### Configurar Dispositivo de Prueba

#### Emulador (Recomendado para desarrollo)

1. **Crear AVD (Android Virtual Device):**
   - `Tools > Device Manager`
   - Clic en `Create Device`
   - Seleccionar: `Phone > Pixel 5`
   - System Image: `API 27` (Android 8.0) o superior
   - Configuración recomendada:
     - RAM: 2048 MB mínimo
     - VM Heap: 512 MB
     - Internal Storage: 2048 MB
   - Finish

2. **Iniciar Emulador:**
   - Desde Device Manager, hacer clic en el ícono ▶ del emulador creado

#### Dispositivo Físico

1. **Habilitar Modo Desarrollador:**
   - `Ajustes > Acerca del teléfono`
   - Tocar 7 veces en `Número de compilación`

2. **Habilitar Depuración USB:**
   - `Ajustes > Opciones de desarrollador`
   - Activar `Depuración USB`

3. **Conectar al PC:**
   - Conectar con cable USB
   - Aceptar la autorización en el dispositivo
   - Verificar en `adb devices`:
     ```bash
     adb devices
     # Debe aparecer: XXXXXXXX device
     ```

### Ejecutar la Aplicación

1. **Desde Android Studio:**
   - Seleccionar el dispositivo en el desplegable superior
   - Hacer clic en `Run ▶` (o `Shift+F10`)

2. **Desde la línea de comandos:**
   ```bash
   # Instalar APK en dispositivo conectado
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # Iniciar aplicación
   adb shell am start -n com.example.agroapp/.LoginActivity
   ```

### Primera Ejecución

Al iniciar la aplicación por primera vez:

1. **Pantalla de Login aparece automáticamente**
2. **Credenciales predeterminadas:**
   - Usuario: `admin`
   - Contraseña: `admin123`
3. **La base de datos se crea automáticamente** con:
   - Todas las tablas
   - Usuario administrador
   - Relaciones y restricciones

## Verificación de Funcionalidad

### Checklist de Pruebas Básicas

- [ ] Login exitoso con admin/admin123
- [ ] Menú principal muestra 8 opciones
- [ ] Registrar un nuevo animal
- [ ] Ver lista de animales
- [ ] Ver detalle de un animal
- [ ] Crear evento en calendario
- [ ] Agregar registro de historial clínico
- [ ] Registrar un gasto
- [ ] Registrar alimentación
- [ ] Ver reportes y estadísticas
- [ ] Generar PDF (verificar carpeta Descargas)
- [ ] Ver recomendaciones
- [ ] Cerrar sesión

### Otorgar Permisos

Al usar funciones específicas, la app solicitará permisos:

1. **Al seleccionar foto de animal:**
   - Android < 13: "Permitir acceso a fotos y multimedia"
   - Android >= 13: "Permitir acceso a fotos"
   - **Acción:** Aceptar

2. **Al generar PDF:**
   - Android < 10: "Permitir acceso a almacenamiento"
   - Android >= 10: Sin solicitud (acceso directo a Downloads)

3. **Al crear evento con recordatorio:**
   - "Permitir notificaciones de AgroApp"
   - Android >= 12: "Permitir alarmas y recordatorios"
   - **Acción:** Aceptar ambos

## Debugging

### Ver Logs en Tiempo Real

```bash
# Ver todos los logs de AgroApp
adb logcat | findstr "AgroApp"

# Limpiar logs y ver solo nuevos
adb logcat -c
adb logcat *:E  # Solo errores
```

### Inspeccionar Base de Datos

```bash
# Conectar a shell del dispositivo
adb shell

# Navegar a DB (requiere root en dispositivo físico)
cd /data/data/com.example.agroapp/databases/
sqlite3 agroapp.db

# Comandos útiles en SQLite
.tables                           # Listar tablas
.schema animales                  # Ver estructura
SELECT * FROM animales;           # Consultar datos
.exit                             # Salir
```

### Desinstalar Aplicación

```bash
# Desinstalar completamente (borra base de datos)
adb uninstall com.example.agroapp
```

## Troubleshooting

### Problema: "App keeps stopping"

**Causas comunes:**
1. Permisos no otorgados
2. Actividad no registrada en AndroidManifest.xml
3. Error en layouts XML

**Solución:**
```bash
# Ver el error específico
adb logcat | findstr "AndroidRuntime"
```

### Problema: Gradle Sync falla

**Solución:**
1. `File > Invalidate Caches / Restart`
2. Eliminar carpetas `.gradle` y `.idea`
3. Volver a abrir el proyecto
4. Sync nuevamente

### Problema: "No JVM could be found"

**Solución:**
1. Verificar variable de entorno `JAVA_HOME`
2. En Android Studio: `File > Project Structure > SDK Location`
3. Seleccionar JDK válido

### Problema: Emulador muy lento

**Solución:**
1. Habilitar aceleración de hardware:
   - Intel: HAXM
   - AMD: WHPX (Windows Hypervisor Platform)
2. Reducir resolución del emulador
3. Cerrar aplicaciones pesadas en el host

## Optimización del Build

### Reducir Tiempo de Compilación

**En gradle.properties:**
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError
org.gradle.parallel=true
org.gradle.caching=true
android.enableJetifier=true
android.useAndroidX=true
```

### Habilitar Instant Run

`File > Settings > Build, Execution, Deployment > Instant Run`  
Marcar: `Enable Instant Run to hot swap code changes`

## Recursos Adicionales

### Documentación Android
- Guía oficial: https://developer.android.com/guide
- API Reference: https://developer.android.com/reference

### Tutoriales
- SQLite: https://developer.android.com/training/data-storage/sqlite
- RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
- Notifications: https://developer.android.com/develop/ui/views/notifications

### Herramientas Útiles
- **Android Studio Profiler:** Para analizar rendimiento
- **Layout Inspector:** Para inspeccionar jerarquía de vistas
- **Database Inspector:** Para ver SQLite en tiempo real (Android Studio 4.1+)

## Contacto

Para problemas durante la compilación o ejecución:
1. Verificar versión de Android Studio
2. Revisar logs completos
3. Consultar archivo TECHNICAL_NOTES.md para detalles internos

---

**Última actualización:** 2024  
**Versión:** 1.0
