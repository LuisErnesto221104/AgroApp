# AgroApp - Aplicación de Gestión Ganadera

## Descripción
AgroApp es una aplicación móvil Android diseñada para pequeños productores ganaderos que permite gestionar su ganado de manera integral y completamente offline. La aplicación utiliza SQLite para almacenamiento local y no requiere conexión a internet.

## Características Principales

### 1. Gestión de Animales
- Registro completo de animales con datos como arete, nombre, raza, sexo, fechas, pesos y foto
- Búsqueda y filtrado por estado (Sano, Enfermo, Vendido, Muerto)
- Edición y eliminación de animales
- Vista detallada con toda la información del animal

### 2. Calendario Sanitario
- Programación de eventos sanitarios (Vacunas, Desparasitación, Vitaminas, etc.)
- Recordatorios automáticos un día antes del evento
- Seguimiento del estado de eventos (Pendiente/Realizado)
- Notificaciones push para eventos próximos

### 3. Historial Clínico
- Registro de enfermedades, síntomas y tratamientos por animal
- Seguimiento del estado del tratamiento
- Observaciones adicionales para cada caso

### 4. Control de Gastos
- Registro de gastos e inversiones
- Categorización por tipo (Alimento, Medicamento, Vacuna, Equipo, etc.)
- Asociación opcional con animales específicos
- Visualización de totales y promedios

### 5. Registro de Alimentación
- Control de tipo de alimento suministrado
- Registro de cantidades y unidades
- Historial por animal

### 6. Reportes y Estadísticas
- Estadísticas generales del hato
- Totales de gastos y promedios
- Generación de reportes en PDF
- Almacenamiento automático en carpeta Descargas

### 7. Recomendaciones Nutricionales
- Guías de alimentación para bovinos de carne y leche
- Recomendaciones por etapa productiva
- Consejos de manejo sanitario y ambiental

### 8. Gestión de Usuarios
- Sistema de login con usuario y contraseña
- Usuario administrador predeterminado (admin/admin123)
- Persistencia de sesión

## Requisitos Técnicos

- **Android:** 8.0 (API 27) o superior
- **Espacio:** Mínimo 50 MB
- **Permisos:**
  - Almacenamiento (lectura/escritura de fotos y PDFs)
  - Notificaciones (recordatorios de eventos sanitarios)
  - Alarmas exactas (programación de recordatorios)

## Tecnologías Utilizadas

- **Lenguaje:** Java
- **Base de Datos:** SQLite
- **Arquitectura:** MVP (Model-View-Presenter) con capa DAO
- **UI Components:** Material Design, CardView, RecyclerView
- **Notificaciones:** AlarmManager + BroadcastReceiver
- **PDFs:** PdfDocument API de Android

## Estructura de la Base de Datos

La aplicación utiliza 6 tablas principales:

1. **usuarios** - Gestión de acceso
2. **animales** - Información del ganado
3. **calendario_sanitario** - Eventos sanitarios programados
4. **historial_clinico** - Registros médicos
5. **gastos** - Control financiero
6. **alimentacion** - Registro de alimentación

Todas las tablas tienen relaciones con claves foráneas y eliminación en cascada.

## Instalación

1. Abre el proyecto en Android Studio
2. Sincroniza el proyecto con Gradle (Sync Now)
3. Conecta un dispositivo Android o inicia un emulador
4. Ejecuta la aplicación (Run > Run 'app')

## Configuración Inicial

### Primera Ejecución
1. La aplicación creará automáticamente la base de datos SQLite
2. Se insertará un usuario administrador por defecto:
   - **Usuario:** admin
   - **Contraseña:** admin123

### Recomendaciones
- Cambia la contraseña del administrador después del primer acceso
- Realiza respaldos periódicos de la base de datos
- Otorga todos los permisos solicitados para funcionalidad completa

## Uso de la Aplicación

### Login
1. Inicia la aplicación
2. Ingresa credenciales (admin/admin123 por defecto)
3. Presiona "Iniciar Sesión"

### Registrar un Animal
1. Desde el menú principal, selecciona "Registrar Animal"
2. Completa todos los campos requeridos
3. Selecciona una foto (opcional)
4. Presiona "Guardar"

### Programar Evento Sanitario
1. Accede al "Calendario"
2. Presiona el botón flotante (+)
3. Selecciona el animal y tipo de evento
4. Establece la fecha
5. Activa el recordatorio si lo deseas
6. Guarda el evento

### Generar Reporte PDF
1. Accede a "Reportes"
2. Revisa las estadísticas mostradas
3. Presiona "Generar Reporte PDF"
4. El PDF se guardará en la carpeta Descargas

## Navegación

La aplicación cuenta con un menú principal tipo grid con 8 opciones:

- 🐄 **Animales** - Lista y gestión de ganado
- ➕ **Registrar Animal** - Agregar nuevo animal
- 📅 **Calendario** - Eventos sanitarios
- 💰 **Gastos** - Control financiero
- 🌾 **Alimentación** - Registros de alimentación
- 📊 **Reportes** - Estadísticas y PDF
- 📋 **Recomendaciones** - Guías nutricionales
- 🚪 **Salir** - Cerrar sesión

## Solución de Problemas

### La aplicación no abre
- Verifica que tu dispositivo tenga Android 8.0 o superior
- Asegúrate de haber otorgado todos los permisos necesarios

### No se guardan las fotos
- Verifica los permisos de almacenamiento
- En Android 13+, otorga permiso de "Fotos y videos"

### No llegan las notificaciones
- Verifica que las notificaciones estén habilitadas para AgroApp
- En Android 12+, otorga permiso de alarmas exactas
- Revisa que el recordatorio esté activado al crear el evento

### No se genera el PDF
- Verifica permisos de escritura en almacenamiento
- Asegúrate de tener espacio suficiente en el dispositivo
- Revisa la carpeta Descargas para encontrar el archivo

## Limitaciones Conocidas

- La aplicación funciona completamente offline (no hay sincronización en la nube)
- Los respaldos deben hacerse manualmente
- El tamaño de las fotos no se optimiza automáticamente
- Un solo usuario activo por instalación

## Próximas Mejoras Sugeridas

- [ ] Soporte multiusuario con perfiles
- [ ] Gráficas visuales de estadísticas
- [ ] Exportación/importación de base de datos
- [ ] Compresión automática de fotos
- [ ] Widget para eventos próximos
- [ ] Modo oscuro
- [ ] Soporte para tablets

## Créditos

**Desarrollado para:** Pequeños productores ganaderos  
**Versión:** 1.0  
**Fecha:** 2024  
**Licencia:** Uso privado

## Soporte

Para problemas o sugerencias relacionadas con la aplicación, documenta:
1. Versión de Android
2. Descripción detallada del problema
3. Pasos para reproducir el error
4. Capturas de pantalla si es posible

---

**¡Gracias por usar AgroApp!**  
Gestiona tu ganado de manera eficiente y profesional.
