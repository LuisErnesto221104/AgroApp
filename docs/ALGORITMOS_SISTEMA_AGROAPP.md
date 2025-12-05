# ALGORITMOS DEL SISTEMA AGROAPP
## Documentación Técnica de Algoritmos Implementados

Fecha: Diciembre 2025
Sistema: AgroApp - Gestión Ganadera
Versión: 1.0

---

# ÍNDICE DE ALGORITMOS

| # | Ilustración | Algoritmo |
|---|-------------|-----------|
| 1 | 14 | Registrar Nuevo Animal |
| 2 | 15 | Actualizar Animal |
| 3 | 16 | Eliminar Animal |
| 4 | 17 | Consultar Animal |
| 5 | 18 | Registrar Venta de Animal |
| 6 | 20 | Programar Evento Sanitario |
| 7 | 22 | Flujo Completo de Notificaciones |
| 8 | 23 | Marcar Evento como Realizado |
| 9 | 28 | Consultar Gastos con Filtro |
| 10 | 29 | Registro de Compra Dividida |
| 11 | 31 | Registrar Alimento |
| 12 | 34 | Visualizar Estadística del Hato |

---

# ALGORITMO 1: REGISTRAR NUEVO ANIMAL
Ilustración 14 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- arete: String - Número de arete SINIGA (10 dígitos)
- raza: String - Raza del animal
- sexo: String - "Macho" o "Hembra"
- fechaNacimiento: String - Formato dd/MM/yyyy
- fechaIngreso: String - Formato dd/MM/yyyy
- precioCompra: Double - Precio de adquisición en pesos
- estado: String - Estado inicial del animal
- observaciones: String - Notas adicionales
- foto: Bitmap - Imagen del animal (opcional)

Salidas:
- resultado: Boolean - TRUE si el registro fue exitoso, FALSE en caso contrario
- animalId: Long - ID del animal insertado en la base de datos

## 2. Paso a Paso del Proceso

1. El usuario accede a RegistroAnimalActivity desde el menú principal
2. El sistema muestra el formulario de registro con campos vacíos
3. El usuario completa los campos obligatorios (arete, precio)
4. El usuario selecciona raza y sexo de los Spinners predefinidos
5. El usuario selecciona fechas mediante DatePickerDialog
6. Opcionalmente, el usuario captura o selecciona una foto
7. El usuario presiona el botón "Guardar"
8. El sistema ejecuta validaciones secuenciales
9. Si todas las validaciones pasan, se crea el objeto Animal
10. Se ejecuta la inserción en SQLite mediante ExecutorService
11. Se notifica el resultado al usuario mediante Toast
12. Si es exitoso, se cierra la Activity y se retorna a la anterior

## 3. Validaciones Necesarias

Validación de arete obligatorio:
- Campo no puede estar vacío
- Debe contener exactamente 10 dígitos numéricos (regex: \d{10})
- Debe ser único en la base de datos (consulta previa)

Validación de precio:
- Campo no puede estar vacío
- Debe ser un valor numérico válido
- Debe ser mayor a cero

Validación de fechas:
- Fecha de nacimiento debe ser anterior a fecha de ingreso
- Ambas fechas deben tener formato válido dd/MM/yyyy

Validación de imagen:
- Si existe, debe poder convertirse a Base64
- Tamaño máximo recomendado: 800px en dimensión mayor
- Compresión JPEG al 70%

## 4. Manejo de Errores

Error de arete duplicado:
- Código: -1 retornado por insertarAnimal()
- Acción: Mostrar Toast "Error: El número de arete ya existe"
- Recuperación: Enfocar campo de arete para corrección

Error de formato de arete:
- Detección: Regex no coincide con \d{10}
- Acción: Mostrar error en EditText
- Recuperación: Limpiar campo y solicitar reingreso

Error de precio inválido:
- Detección: NumberFormatException al parsear
- Acción: Mostrar Toast con mensaje descriptivo
- Recuperación: Enfocar campo de precio

Error de base de datos:
- Detección: SQLException durante inserción
- Acción: Registrar en Log y mostrar mensaje genérico
- Recuperación: Mantener datos en formulario para reintento

Error de procesamiento de imagen:
- Detección: OutOfMemoryError o IOException
- Acción: Registrar en Log, continuar sin imagen
- Recuperación: Guardar animal sin foto

## 5. Consideraciones Técnicas

Base de datos:
- Tabla: animales
- Constraint UNIQUE en columna numero_arete
- Índice en columna estado para consultas frecuentes

Procesamiento asíncrono:
- ExecutorService para operaciones de BD
- Handler para actualizar UI en hilo principal
- Evita bloqueo de interfaz durante inserción

Manejo de imágenes:
- Redimensionamiento a máximo 800px
- Compresión JPEG 70% para reducir tamaño
- Almacenamiento como String Base64 en SQLite
- Considera límite de 1MB por registro

Arquitectura:
- Patrón MVP con AnimalPresenter
- AnimalDAO para acceso a datos
- Callbacks para comunicación asíncrona

---

# ALGORITMO 2: ACTUALIZAR ANIMAL
Ilustración 15 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- animalId: Integer - ID del animal a actualizar
- raza: String - Nueva raza
- sexo: String - Nuevo sexo
- fechaNacimiento: String - Nueva fecha de nacimiento
- fechaIngreso: String - Nueva fecha de ingreso
- precioCompra: Double - Nuevo precio de compra
- estado: String - Nuevo estado
- observaciones: String - Nuevas observaciones
- foto: Bitmap - Nueva foto (opcional)

Salidas:
- resultado: Boolean - TRUE si la actualización fue exitosa
- filasAfectadas: Integer - Número de registros modificados

## 2. Paso a Paso del Proceso

1. El sistema recibe el ID del animal a editar via Intent
2. Se carga el animal desde la base de datos en hilo secundario
3. Se verifica que el animal existe y no está en estado final
4. Se rellenan los campos del formulario con datos actuales
5. El campo de arete se deshabilita (no editable)
6. El usuario modifica los campos deseados
7. El usuario presiona el botón "Actualizar"
8. Se ejecutan las validaciones de datos modificados
9. Se procesa la nueva imagen si fue cambiada
10. Se actualiza el objeto Animal con nuevos valores
11. Se ejecuta UPDATE en SQLite via ExecutorService
12. Se notifica resultado y se cierra la Activity

## 3. Validaciones Necesarias

Validación de existencia:
- El animal debe existir en la base de datos
- Consulta por ID debe retornar un registro válido

Validación de estado:
- Animal no puede estar en estado "Vendido"
- Animal no puede estar en estado "Muerto"
- Estados finales bloquean toda edición

Validación de arete:
- Campo deshabilitado en modo edición
- Se preserva el valor original sin modificación

Validación de precio:
- Debe ser mayor a cero
- Debe ser un valor numérico válido

Validación de coherencia de fechas:
- Fecha de nacimiento anterior a fecha de ingreso

## 4. Manejo de Errores

Error de animal no encontrado:
- Detección: obtenerAnimalPorId retorna null
- Acción: Mostrar Toast "Animal no encontrado"
- Recuperación: Cerrar Activity y retornar

Error de estado final:
- Detección: Estado es "Vendido" o "Muerto"
- Acción: Mostrar mensaje explicativo
- Recuperación: Bloquear formulario, solo permitir visualización

Error de actualización fallida:
- Detección: filasAfectadas es 0
- Acción: Mostrar Toast "Error al actualizar animal"
- Recuperación: Mantener Activity abierta para reintento

Error de concurrencia:
- Detección: Registro modificado por otro proceso
- Acción: Recargar datos frescos de BD
- Recuperación: Mostrar datos actualizados al usuario

## 5. Consideraciones Técnicas

Integridad del arete:
- Número de arete es inmutable después de creación
- EditText deshabilitado programáticamente
- Previene violación de constraint UNIQUE

Manejo de estado:
- Estados finales son terminales
- UI se adapta según estado del animal
- Botones de acción se ocultan/deshabilitan

Optimización de imagen:
- Solo reprocesar si bitmap cambió
- Mantener foto anterior si no hay cambios
- Comparación por referencia de objeto

Transaccionalidad:
- Actualización es operación atómica
- Rollback automático en caso de error
- No hay estados intermedios inconsistentes

---

# ALGORITMO 3: ELIMINAR ANIMAL
Ilustración 16 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- animalId: Integer - ID del animal a eliminar

Salidas:
- resultado: Boolean - TRUE si la eliminación fue exitosa
- filasAfectadas: Integer - Número de registros eliminados

## 2. Paso a Paso del Proceso

1. El usuario selecciona la opción "Eliminar" desde DetalleAnimalActivity
2. El sistema obtiene la información del animal por su ID
3. Se verifica que el animal existe en la base de datos
4. Se muestra diálogo de confirmación con advertencia de datos relacionados
5. El usuario lee la advertencia sobre eliminación en cascada
6. Si el usuario confirma, se ejecuta la eliminación
7. SQLite elimina automáticamente registros relacionados via CASCADE
8. Se notifica el resultado al usuario
9. Se cierra la Activity y se retorna a la lista de animales
10. La lista se actualiza automáticamente sin el animal eliminado

## 3. Validaciones Necesarias

Validación de existencia:
- El animal debe existir en la base de datos
- Consulta previa para obtener nombre del animal

Validación de confirmación:
- Diálogo obligatorio antes de eliminar
- Mensaje claro sobre consecuencias
- Botones explícitos "Eliminar" y "Cancelar"

Validación de integridad referencial:
- Configuración previa de ON DELETE CASCADE
- Tablas afectadas: calendario_sanitario, historial_clinico, gastos, alimentacion

## 4. Manejo de Errores

Error de animal no encontrado:
- Detección: obtenerAnimalPorId retorna null
- Acción: Mostrar Toast "Animal no encontrado"
- Recuperación: Cerrar Activity

Error de eliminación fallida:
- Detección: filasAfectadas es 0
- Acción: Mostrar Toast "Error al eliminar animal"
- Recuperación: Mantener Activity abierta

Error de constraint de base de datos:
- Detección: SQLException por foreign key
- Acción: Registrar error en Log
- Recuperación: Verificar configuración CASCADE

Usuario cancela operación:
- Detección: Click en botón "Cancelar"
- Acción: Cerrar diálogo sin cambios
- Recuperación: Continuar en pantalla de detalle

## 5. Consideraciones Técnicas

Eliminación en cascada:
- PRAGMA foreign_keys = ON habilitado en DatabaseHelper
- Todas las tablas relacionadas tienen ON DELETE CASCADE
- Eliminación atómica de animal y registros dependientes

Tablas afectadas por CASCADE:
- calendario_sanitario: eventos sanitarios del animal
- historial_clinico: registros médicos
- gastos: gastos asociados al animal
- alimentacion: registros de alimentación

Advertencia al usuario:
- Mensaje detallado de datos que se perderán
- Operación irreversible claramente indicada
- Confirmación explícita requerida

Rendimiento:
- Una sola operación DELETE en tabla animales
- SQLite maneja CASCADE internamente
- Índices en foreign keys para optimización

---

# ALGORITMO 4: CONSULTAR ANIMAL
Ilustración 17 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- animalId: Integer - ID del animal a consultar

Salidas:
- animal: Animal - Objeto con datos completos del animal
- inversionTotal: Double - Suma de precio de compra más gastos
- ganancia: Double - Diferencia entre venta e inversión (si aplica)

## 2. Paso a Paso del Proceso

1. El sistema recibe el ID del animal via Intent
2. Se registra tiempo de inicio para métricas de rendimiento
3. Se ejecuta consulta a base de datos en hilo secundario
4. Se obtienen datos básicos del animal
5. Se decodifica y muestra la foto si existe
6. Se configura el badge de estado con color correspondiente
7. Se muestra información en grid (raza, sexo, edad, fechas)
8. Se calcula la edad del animal desde fecha de nacimiento
9. Se consultan gastos totales asociados al animal
10. Se calcula inversión total (precio compra + gastos)
11. Si está vendido, se calcula y muestra ganancia/pérdida
12. Se configura visibilidad de acciones según estado
13. Se verifica tiempo de carga contra RNF001 (menos de 2 segundos)

## 3. Validaciones Necesarias

Validación de existencia:
- Animal debe existir en base de datos
- ID debe ser válido y mayor a cero

Validación de foto:
- Verificar que string Base64 no esté vacío
- Verificar decodificación exitosa a Bitmap

Validación de estado para colores:
- "Sano": Verde (#16a34a)
- "Enfermo": Rojo (#dc2626)
- "Vendido": Morado (#7e22ce)
- "Muerto": Gris (#6b7280)

Validación de rendimiento:
- Tiempo de carga debe ser menor a 2000ms
- Registrar advertencia si se excede

## 4. Manejo de Errores

Error de animal no encontrado:
- Detección: obtenerAnimalPorId retorna null
- Acción: Mostrar Toast y cerrar Activity
- Recuperación: Retornar a lista de animales

Error de decodificación de imagen:
- Detección: Exception al decodificar Base64
- Acción: Mostrar imagen placeholder por defecto
- Recuperación: Continuar mostrando otros datos

Error de cálculo de edad:
- Detección: Fecha de nacimiento vacía o inválida
- Acción: Mostrar "-" en campo de edad
- Recuperación: Continuar con otros campos

Error de rendimiento:
- Detección: Tiempo de carga mayor a 2 segundos
- Acción: Registrar advertencia en Log
- Recuperación: Continuar operación normal

## 5. Consideraciones Técnicas

Cálculo de edad:
- Basado en diferencia entre fecha actual y nacimiento
- Muestra años si es mayor a 12 meses
- Muestra meses si es menor a 12 meses

Cálculo de inversión:
- Fórmula: precioCompra + totalGastos
- totalGastos obtenido de suma de tabla gastos
- Considera todos los gastos del animal

Cálculo de ganancia:
- Fórmula: precioVenta - inversionTotal
- Positivo: Ganancia (color verde)
- Negativo: Pérdida (color rojo)

Estados finales:
- Vendido o Muerto bloquean acciones
- Tarjetas de acción se deshabilitan
- Botones de editar/vender se ocultan

Requisito RNF001:
- Tiempo de respuesta menor a 2 segundos
- Monitoreo activo en cada consulta
- Log de advertencia si se excede

---

# ALGORITMO 5: REGISTRAR VENTA DE ANIMAL
Ilustración 18 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- animalId: Integer - ID del animal a vender
- precioVenta: Double - Precio de venta acordado
- fechaVenta: String - Fecha de la transacción (formato dd/MM/yyyy)

Salidas:
- resultado: Boolean - TRUE si el registro fue exitoso
- ganancia: Double - Ganancia o pérdida calculada
- resumenVenta: String - Resumen formateado de la operación

## 2. Paso a Paso del Proceso

1. El usuario presiona "Registrar Venta" en DetalleAnimalActivity
2. Se muestra diálogo para ingresar precio de venta
3. El usuario ingresa el precio de venta
4. Se selecciona la fecha de venta (por defecto fecha actual)
5. Se valida que el precio sea mayor a cero
6. Se obtiene el animal de la base de datos
7. Se verifica que el estado no sea "Vendido" ni "Muerto"
8. Se actualizan los campos: estado, fechaSalida, precioVenta
9. Se ejecuta UPDATE en base de datos via ExecutorService
10. Se calcula la inversión total (compra + gastos)
11. Se calcula la ganancia o pérdida
12. Se muestra diálogo con resumen financiero
13. Se cierra Activity al confirmar resumen

## 3. Validaciones Necesarias

Validación de precio de venta:
- Campo obligatorio, no puede estar vacío
- Debe ser un valor numérico válido
- Debe ser mayor a cero

Validación de estado actual:
- No debe ser "Vendido" (ya fue vendido)
- No debe ser "Muerto" (no se puede vender)

Validación de existencia:
- Animal debe existir en base de datos
- ID debe ser válido

## 4. Manejo de Errores

Error de precio vacío:
- Detección: Campo de precio vacío
- Acción: Mostrar Toast "Ingrese el precio de venta"
- Recuperación: Mantener diálogo abierto

Error de precio inválido:
- Detección: Valor menor o igual a cero
- Acción: Mostrar Toast descriptivo
- Recuperación: Limpiar campo y solicitar reingreso

Error de animal ya vendido:
- Detección: Estado actual es "Vendido"
- Acción: Mostrar Toast "Este animal ya fue vendido"
- Recuperación: Cerrar diálogo

Error de animal muerto:
- Detección: Estado actual es "Muerto"
- Acción: Mostrar Toast "No se puede vender un animal muerto"
- Recuperación: Cerrar diálogo

Error de actualización:
- Detección: filasAfectadas es 0
- Acción: Mostrar error genérico
- Recuperación: Mantener datos para reintento

## 5. Consideraciones Técnicas

Cálculo financiero:
- Inversión = precioCompra + totalGastos
- Ganancia = precioVenta - Inversión
- Formato de moneda: Locale es-MX

Actualización de estado:
- Estado cambia a "Vendido"
- fechaSalida se establece con fecha de venta
- precioVenta se almacena para cálculos futuros

Resumen de venta:
- Muestra desglose completo
- Precio de compra original
- Total de gastos acumulados
- Inversión total calculada
- Precio de venta ingresado
- Ganancia o pérdida resultante

Transición de estado:
- Estado "Vendido" es terminal
- Animal ya no aparece en animales activos
- Bloquea futuras modificaciones

---

# ALGORITMO 6: PROGRAMAR EVENTO SANITARIO
Ilustración 20 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- raza: String - Raza del ganado para el evento
- tipo: String - Tipo de evento (Vacuna, Desparasitación, Vitaminas, Otro)
- fechaProgramada: String - Fecha del evento (formato dd/MM/yyyy)
- horaRecordatorio: String - Hora para notificaciones (formato HH:mm)
- descripcion: String - Descripción detallada del evento
- costo: Double - Costo estimado (opcional)

Salidas:
- eventoId: Long - ID del evento creado en base de datos
- notificacionesProgramadas: Integer - Número de notificaciones configuradas (3)

## 2. Paso a Paso del Proceso

1. El usuario accede a CalendarioActivity desde el menú principal
2. El usuario presiona el botón de agregar nuevo evento
3. Se muestra formulario con campos del evento sanitario
4. El usuario selecciona la raza de los animales afectados
5. El usuario selecciona el tipo de evento del Spinner
6. El usuario selecciona fecha mediante DatePickerDialog
7. El usuario selecciona hora de recordatorio mediante TimePickerDialog
8. El usuario ingresa descripción y costo opcional
9. El usuario presiona "Guardar"
10. Se validan los campos ingresados
11. Se crea el objeto EventoSanitario
12. Se inserta en base de datos
13. Se programan 3 notificaciones via AlarmManager
14. Se muestra confirmación al usuario

## 3. Validaciones Necesarias

Validación de raza:
- Debe existir al menos un animal de la raza seleccionada
- Consulta previa a tabla animales

Validación de tipo:
- Debe ser uno de los tipos predefinidos
- Spinner con opciones fijas

Validación de fecha:
- Formato válido dd/MM/yyyy
- Advertencia si fecha es pasada (permite registro retroactivo)

Validación de costo:
- No puede ser negativo
- Valor por defecto: 0

Validación de descripción:
- Campo obligatorio
- Longitud mínima recomendada

## 4. Manejo de Errores

Error de raza sin animales:
- Detección: Consulta retorna lista vacía
- Acción: Mostrar Toast "No hay animales de la raza X"
- Recuperación: Permitir selección de otra raza

Error de costo negativo:
- Detección: Valor menor a cero
- Acción: Mostrar mensaje de error
- Recuperación: Solicitar reingreso

Error de inserción en BD:
- Detección: insertarEvento retorna -1 o 0
- Acción: Mostrar Toast "Error al crear evento"
- Recuperación: Mantener formulario con datos

Error de programación de alarma:
- Detección: Exception en AlarmManager
- Acción: Registrar en Log
- Recuperación: Evento creado pero sin notificaciones

Error de fecha pasada:
- Detección: fechaProgramada menor a fecha actual
- Acción: Mostrar advertencia informativa
- Recuperación: Permitir continuar (registro retroactivo)

## 5. Consideraciones Técnicas

Sistema de notificaciones:
- 3 notificaciones por evento: -3 días, -1 día, día del evento
- Hora fija: 9:00 AM
- AlarmManager con setExactAndAllowWhileIdle para Android 6.0+

Cálculo de requestCode:
- Fórmula: eventoId * 100 + (offset + 10)
- Garantiza unicidad para cada notificación de cada evento
- Permite cancelación individual

Almacenamiento:
- Tabla: calendario_sanitario
- Campos: id, raza, tipo, fecha_programada, descripcion, costo, estado, recordatorio

Estado inicial:
- Estado: "Pendiente"
- Recordatorio: 1 (activo)

---

# ALGORITMO 7: FLUJO COMPLETO DE NOTIFICACIONES
Ilustración 22 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- evento: EventoSanitario - Evento para el cual programar notificaciones

Salidas:
- exito: Boolean - TRUE si todas las notificaciones se programaron
- alarmasCreadas: Integer - Número de alarmas configuradas (máximo 3)

## 2. Paso a Paso del Proceso

1. Se recibe el evento sanitario a procesar
2. Se verifica que el recordatorio esté activo
3. Se parsea la fecha programada del evento
4. Se calcula la fecha para notificación de 3 días antes
5. Se programa alarma si la fecha está en el futuro
6. Se calcula la fecha para notificación de 1 día antes
7. Se programa alarma si la fecha está en el futuro
8. Se calcula la fecha para notificación del mismo día
9. Se programa alarma si la fecha está en el futuro
10. Se crea Intent con datos del evento para cada alarma
11. Se calcula requestCode único para cada notificación
12. Se configura PendingIntent con flags apropiados
13. Se programa AlarmManager según versión de Android
14. Se registra resultado en Log

## 3. Validaciones Necesarias

Validación de recordatorio activo:
- Campo recordatorio debe ser 1
- Si es 0, no se programan notificaciones

Validación de fecha:
- Debe poder parsearse con formato dd/MM/yyyy
- Fecha debe ser válida

Validación de tiempo futuro:
- Solo se programan notificaciones para fechas futuras
- Fechas pasadas se ignoran silenciosamente

Validación de versión Android:
- Android 12+: Agregar FLAG_IMMUTABLE
- Android 6.0+: Usar setExactAndAllowWhileIdle

## 4. Manejo de Errores

Error de parseo de fecha:
- Detección: ParseException
- Acción: Registrar error en Log
- Recuperación: Retornar FALSE, no programar notificaciones

Error de fecha pasada:
- Detección: tiempoTrigger menor a tiempoActual
- Acción: No programar esa notificación específica
- Recuperación: Continuar con siguientes notificaciones

Error de AlarmManager:
- Detección: SecurityException por permisos
- Acción: Registrar en Log
- Recuperación: Notificar al usuario sobre permisos

Error de PendingIntent:
- Detección: Exception al crear intent
- Acción: Registrar en Log
- Recuperación: Continuar con otras notificaciones

## 5. Consideraciones Técnicas

Offsets de notificación:
- OFFSETS_DIAS = [-3, -1, 0]
- -3: Recordatorio anticipado
- -1: Recordatorio urgente
- 0: Día del evento

Prefijos de mensajes:
- 3 días antes: "Recordatorio: "
- 1 día antes: "Recordatorio urgente: "
- Mismo día: "HOY: "

Cálculo de requestCode:
- Fórmula: eventoId * 100 + (offset + 10)
- Ejemplo evento 5: 507 (3 días), 509 (1 día), 510 (mismo día)

Compatibilidad Android:
- API 23+: setExactAndAllowWhileIdle (modo Doze)
- API 31+: FLAG_IMMUTABLE requerido
- Versiones anteriores: setExact

BroadcastReceiver:
- NotificationReceiver recibe las alarmas
- Extrae datos del Intent
- Crea y muestra la notificación

Cancelación de notificaciones:
- Usa mismo requestCode para cancelar
- Cancela PendingIntent y alarma
- Se ejecuta al marcar evento como realizado

---

# ALGORITMO 8: MARCAR EVENTO COMO REALIZADO
Ilustración 23 - Diagrama de Actividad

## 1. Entradas y Salidas

Entradas:
- evento: EventoSanitario - Evento a marcar como realizado

Salidas:
- exito: Boolean - TRUE si se actualizó correctamente
- filasAfectadas: Integer - Número de registros modificados

## 2. Paso a Paso del Proceso

1. El usuario visualiza la lista de eventos en CalendarioActivity
2. El usuario selecciona un evento pendiente
3. El sistema verifica el estado actual del evento
4. Se valida que el estado no sea "Realizado" ni "Cancelado"
5. Se actualiza el estado a "Realizado"
6. Se establece la fecha de realización (fecha actual)
7. Se ejecuta UPDATE en base de datos
8. Se cancelan las notificaciones pendientes del evento
9. Se notifica el resultado al usuario
10. Se recarga la lista de eventos

## 3. Validaciones Necesarias

Validación de estado actual:
- No debe ser "Realizado" (ya completado)
- No debe ser "Cancelado" (evento anulado)
- Solo eventos "Pendiente" pueden marcarse

Validación de existencia:
- Evento debe existir en base de datos
- ID debe ser válido

## 4. Manejo de Errores

Error de evento ya realizado:
- Detección: Estado es "Realizado"
- Acción: Mostrar advertencia
- Recuperación: No realizar cambios

Error de evento cancelado:
- Detección: Estado es "Cancelado"
- Acción: Mostrar error explicativo
- Recuperación: No permitir operación

Error de actualización:
- Detección: filasAfectadas es 0
- Acción: Mostrar Toast "Error al actualizar evento"
- Recuperación: Mantener estado original

Error de cancelación de alarmas:
- Detección: Exception en AlarmManager
- Acción: Registrar en Log
- Recuperación: Evento actualizado pero alarmas pueden persistir

## 5. Consideraciones Técnicas

Transición de estados:
- Pendiente -> Realizado (operación normal)
- Pendiente -> Cancelado (alternativa)
- Realizado y Cancelado son estados finales

Actualización de campos:
- estado: "Realizado"
- fechaRealizada: fecha actual formateada

Cancelación de notificaciones:
- Se cancelan las 3 alarmas programadas
- Usa requestCodes calculados: eventoId * 100 + offset
- Previene notificaciones innecesarias

Recarga de lista:
- Adapter notifica cambios
- Lista se actualiza visualmente
- Evento aparece con estado actualizado

---

# ALGORITMO 9: CONSULTAR GASTOS CON FILTRO
Ilustración 28 - Diagrama de Actividades

## 1. Entradas y Salidas

Entradas:
- animalIdFiltro: Integer - ID del animal para filtrar (-1 para todos)

Salidas:
- listaGastos: List - Lista de gastos filtrados
- totalGastos: Double - Suma total de los gastos mostrados

## 2. Paso a Paso del Proceso

1. El usuario accede a GastosActivity desde el menú o detalle de animal
2. Si viene desde detalle de animal, se recibe el ID via Intent
3. Se determina el modo de consulta (por animal o todos)
4. Se ejecuta consulta a base de datos en hilo secundario
5. Se obtienen los gastos según el filtro aplicado
6. Se actualiza la lista en el hilo principal
7. El adapter notifica los cambios al RecyclerView
8. Se muestra la lista actualizada al usuario
9. El usuario puede seleccionar un gasto para ver opciones
10. Se ofrece opción de eliminar el gasto seleccionado

## 3. Validaciones Necesarias

Validación de filtro:
- Si animalIdFiltro es -1, consultar todos los gastos
- Si animalIdFiltro es mayor a 0, filtrar por ese animal

Validación de existencia:
- Verificar que existan gastos para mostrar
- Mostrar mensaje si lista está vacía

Validación de eliminación:
- Confirmar antes de eliminar
- Mostrar diálogo de confirmación

## 4. Manejo de Errores

Error de base de datos:
- Detección: SQLException durante consulta
- Acción: Registrar en Log, mostrar lista vacía
- Recuperación: Mostrar mensaje de error

Error de adapter:
- Detección: NullPointerException en adapter
- Acción: Crear nuevo adapter
- Recuperación: Recargar lista completa

Error de eliminación:
- Detección: filasAfectadas es 0
- Acción: Mostrar Toast de error
- Recuperación: Mantener lista sin cambios

Lista vacía:
- Detección: Consulta retorna lista vacía
- Acción: Mostrar mensaje informativo
- Recuperación: Ofrecer opción de agregar gasto

## 5. Consideraciones Técnicas

Consulta por animal:
- Usa obtenerGastosPorAnimal(animalId)
- Retorna gastos ordenados por fecha descendente

Consulta general:
- Usa obtenerTodosLosGastos()
- Incluye gastos de todos los animales

Actualización de UI:
- ExecutorService para consulta
- Handler para actualizar en hilo principal
- notifyDataSetChanged() para refrescar lista

Eliminación:
- Diálogo de confirmación obligatorio
- Operación asíncrona
- Recarga automática de lista

---

# ALGORITMO 10: REGISTRO DE COMPRA DIVIDIDA
Ilustración 29 - Diagrama de Actividades

## 1. Entradas y Salidas

Entradas:
- nombreCompra: String - Descripción de la compra
- precioTotal: Double - Precio total de la compra
- tipoCompra: String - Tipo/categoría del gasto
- animalesSeleccionados: List - IDs de animales seleccionados

Salidas:
- exito: Boolean - TRUE si el registro fue exitoso
- gastosCreados: Integer - Número de gastos insertados
- precioPorAnimal: Double - Precio calculado por animal

## 2. Paso a Paso del Proceso

1. El usuario accede a RegistroComprasActivity
2. El sistema carga la lista de todos los animales activos
3. Se muestran los animales con checkboxes para selección
4. El usuario puede filtrar animales por arete o raza
5. El usuario ingresa el nombre de la compra
6. El usuario ingresa el precio total
7. El usuario selecciona los animales participantes
8. El sistema calcula el precio por animal en tiempo real
9. El usuario presiona "Registrar Compra"
10. Se validan todos los campos
11. Se ejecuta inserción masiva en hilo secundario
12. Se crea un gasto por cada animal seleccionado
13. Se notifica el resultado al usuario
14. Se cierra la Activity

## 3. Validaciones Necesarias

Validación de nombre:
- Campo obligatorio
- No puede estar vacío

Validación de precio:
- Campo obligatorio
- Debe ser numérico válido
- Debe ser mayor a cero

Validación de selección:
- Al menos un animal debe estar seleccionado
- Contador de seleccionados visible

Validación de cálculo:
- División precisa del precio
- Mostrar precio por animal actualizado

## 4. Manejo de Errores

Error de nombre vacío:
- Detección: Campo de nombre vacío
- Acción: Mostrar Toast "Complete el nombre de la compra"
- Recuperación: Enfocar campo

Error de precio inválido:
- Detección: Precio vacío o menor/igual a cero
- Acción: Mostrar mensaje descriptivo
- Recuperación: Enfocar campo de precio

Error de selección vacía:
- Detección: Ningún animal seleccionado
- Acción: Mostrar Toast "Seleccione al menos un animal"
- Recuperación: Mantener formulario

Error de inserción:
- Detección: SQLException durante INSERT
- Acción: Registrar error, mostrar mensaje
- Recuperación: Rollback implícito por transacción fallida

## 5. Consideraciones Técnicas

Cálculo de división:
- precioPorAnimal = precioTotal / cantidadAnimales
- Precisión de 2 decimales
- Actualización en tiempo real al cambiar selección

Filtrado de animales:
- Por número de arete (contiene)
- Por raza (contiene)
- Case-insensitive

Inserción masiva:
- Loop sobre animales seleccionados
- Un INSERT por animal
- Observaciones incluyen cantidad de animales

Estructura del gasto:
- animalId: ID del animal
- tipo: categoría seleccionada
- concepto: nombre de la compra
- monto: precio dividido
- fecha: fecha actual
- observaciones: "Compra distribuida entre N animales"

---

# ALGORITMO 11: REGISTRAR ALIMENTO
Ilustración 31 - Diagrama de Actividades

## 1. Entradas y Salidas

Entradas:
- modoSeleccion: String - "INDIVIDUAL" o "POR_RAZA"
- animalId: Integer - ID del animal (modo individual)
- razasSeleccionadas: List - Razas seleccionadas (modo por raza)
- tipoAlimento: String - Tipo de alimento
- cantidad: Double - Cantidad suministrada
- unidad: String - Unidad de medida
- fecha: String - Fecha del suministro
- costo: Double - Costo del alimento (opcional)
- observaciones: String - Notas adicionales

Salidas:
- exito: Boolean - TRUE si el registro fue exitoso
- registrosCreados: Integer - Número de registros insertados

## 2. Paso a Paso del Proceso

1. El usuario accede a AlimentacionActivity
2. El usuario selecciona el modo de registro
3. Modo Individual: selecciona un animal del Spinner
4. Modo Por Raza: marca las razas en checkboxes
5. El usuario selecciona tipo de alimento
6. El usuario ingresa cantidad y selecciona unidad
7. El usuario selecciona fecha de suministro
8. Opcionalmente ingresa costo y observaciones
9. El usuario presiona "Guardar"
10. Se validan los campos según el modo
11. Se determina la lista de animales afectados
12. Se ejecuta inserción en hilo secundario
13. Se crea un registro por cada animal
14. Se notifica el resultado
15. Se recarga la lista de alimentación

## 3. Validaciones Necesarias

Validación de cantidad:
- Campo obligatorio
- Debe ser mayor a cero
- Debe ser numérico válido

Validación de costo:
- Opcional pero si se ingresa
- No puede ser negativo

Validación modo Individual:
- Debe seleccionar un animal del Spinner
- animalId debe ser mayor a 0

Validación modo Por Raza:
- Al menos una raza debe estar seleccionada
- Deben existir animales de las razas seleccionadas

Validación de unidad:
- Debe ser una de las opciones predefinidas
- kg, g, toneladas, pacas, litros

## 4. Manejo de Errores

Error de cantidad inválida:
- Detección: Cantidad vacía o menor/igual a cero
- Acción: Mostrar mensaje de error
- Recuperación: Enfocar campo de cantidad

Error de costo negativo:
- Detección: Valor menor a cero
- Acción: Mostrar mensaje
- Recuperación: Corregir valor

Error sin animal seleccionado:
- Detección: animalId es 0 o menor
- Acción: Mostrar Toast "Seleccione un animal"
- Recuperación: Enfocar Spinner

Error sin razas seleccionadas:
- Detección: Lista de razas vacía
- Acción: Mostrar Toast "Seleccione al menos una raza"
- Recuperación: Mantener formulario

Error de inserción:
- Detección: SQLException
- Acción: Registrar en Log
- Recuperación: Mostrar error genérico

## 5. Consideraciones Técnicas

Modos de selección:
- INDIVIDUAL: un registro por operación
- POR_RAZA: un registro por animal de las razas seleccionadas

Tipos de alimento predefinidos:
- Pasto, Forraje, Concentrado, Suplemento, Otro

Unidades de medida:
- kg (kilogramos)
- g (gramos)
- toneladas
- pacas
- litros

Consulta de registros:
- Por animal específico
- General (todos los animales)
- Ordenados por fecha descendente

Inserción masiva:
- Loop sobre lista de animales
- Un INSERT por animal
- Mismos datos base para todos

---

# ALGORITMO 12: VISUALIZAR ESTADÍSTICA DEL HATO
Ilustración 34 - Diagrama de Actividades

## 1. Entradas y Salidas

Entradas:
- Ninguna entrada directa del usuario

Salidas:
- totalAnimales: Integer - Cantidad total de animales registrados
- animalesActivos: Integer - Animales sin fecha de salida y no muertos
- animalesSanos: Integer - Animales con estado "Sano"
- animalesEnfermos: Integer - Animales con estado "Enfermo"
- animalesVendidos: Integer - Animales con estado "Vendido"
- animalesMuertos: Integer - Animales con estado "Muerto"
- totalGastos: Double - Suma de todos los gastos
- promedioGastos: Double - Promedio de gastos por animal
- vacunasPendientes: Integer - Eventos sanitarios próximos a vencer

## 2. Paso a Paso del Proceso

1. El usuario accede a ReportesActivity desde el menú principal
2. Se inicia la carga de datos en hilo secundario
3. Se consultan todos los animales de la base de datos
4. Se cuentan animales por cada estado
5. Se calculan animales activos (sin fecha salida, no muertos)
6. Se consulta la suma total de gastos
7. Se calcula el promedio de gastos por animal
8. Se consultan eventos sanitarios pendientes
9. Se filtran eventos de los próximos 7 días
10. Se actualiza la interfaz en hilo principal
11. Se muestran estadísticas en tarjetas
12. Se muestra alerta de vacunas si hay pendientes
13. Se configura badge en botón de calendario

## 3. Validaciones Necesarias

Validación de división:
- Evitar división por cero en promedio
- Si no hay animales, promedio es 0

Validación de fechas de eventos:
- Parsear correctamente formato dd/MM/yyyy
- Comparar con fecha actual

Validación de rango de vacunas:
- Próximos 7 días desde fecha actual
- Incluir día actual

Validación de estados:
- Mapear estados exactamente como están en BD
- Case-sensitive

## 4. Manejo de Errores

Error de base de datos:
- Detección: SQLException en consultas
- Acción: Mostrar valores en cero
- Recuperación: Log de error, continuar con otros datos

Error de parseo de fechas:
- Detección: ParseException
- Acción: Ignorar evento, continuar conteo
- Recuperación: Registrar advertencia

Error de división por cero:
- Detección: totalAnimales es 0
- Acción: Establecer promedio en 0
- Recuperación: Continuar normal

Error de actualización UI:
- Detección: Exception en runOnUiThread
- Acción: Registrar en Log
- Recuperación: Mantener valores por defecto

## 5. Consideraciones Técnicas

Conteos de animales:
- Total: obtenerTodosLosAnimales().size()
- Por estado: obtenerAnimalesPorEstado(estado).size()

Cálculo de activos:
- Animales sin fecha de salida
- Animales con estado diferente a "Muerto"

Cálculo de gastos:
- Total: suma de todos los montos en tabla gastos
- Promedio: totalGastos / totalAnimales

Alerta de vacunas:
- Filtra eventos con estado "Pendiente"
- Compara fechaProgramada con rango actual + 7 días
- Muestra badge con cantidad

Formato de moneda:
- NumberFormat con Locale "es-MX"
- Símbolo de pesos mexicanos

Generación de PDF:
- PdfDocument API de Android
- Tamaño A4 (595 x 842 puntos)
- Guarda en directorio de descargas
- Nombre: AgroApp_Reporte_[fecha].pdf

---

# RESUMEN TÉCNICO DE ALGORITMOS

## Complejidad Algorítmica

| # | Algoritmo | Tiempo | Espacio | Operaciones BD |
|---|-----------|--------|---------|----------------|
| 1 | Registrar Animal | O(1) | O(1) | 2 |
| 2 | Actualizar Animal | O(1) | O(1) | 2 |
| 3 | Eliminar Animal | O(n) | O(1) | 1 + CASCADE |
| 4 | Consultar Animal | O(1) | O(1) | 2 |
| 5 | Registrar Venta | O(1) | O(1) | 2 |
| 6 | Programar Evento | O(1) | O(1) | 1 + 3 alarmas |
| 7 | Notificaciones | O(1) | O(1) | 0 |
| 8 | Marcar Realizado | O(1) | O(1) | 1 |
| 9 | Consultar Gastos | O(n) | O(n) | 1 |
| 10 | Compra Dividida | O(n) | O(n) | n |
| 11 | Registrar Alimento | O(n) | O(n) | n |
| 12 | Estadística Hato | O(n) | O(n) | 5-6 |

Donde n = número de animales afectados

## Patrones de Diseño Utilizados

| Patrón | Algoritmos | Propósito |
|--------|------------|-----------|
| Singleton | Todos | DatabaseHelper única instancia |
| MVP | 1, 2, 4 | Separación de responsabilidades |
| DAO | Todos | Acceso a datos encapsulado |
| Observer | 9, 11, 12 | Notificar cambios al adapter |
| Template Method | 4, 12 | Estructura común de carga |
| Strategy | 10, 11 | Selección individual vs por raza |

---

Documento generado para el Sistema AgroApp - Diciembre 2025
