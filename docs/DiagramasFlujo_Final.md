# Diagramas de Flujo - Algoritmos del Sistema AgroApp
## Notación Clásica de Diagramas de Flujo

### Simbología Utilizada

| Símbolo | PlantUML | Uso |
|---------|----------|-----|
| ⬭ Óvalo | `(*)` | Inicio/Fin |
| ▭ Rectángulo | `[Proceso]` | Acciones/Procesos |
| ◇ Rombo | `<condición>` | Decisiones |
| ▱ Paralelogramo | `>/Entrada/Salida/` | E/S de datos |
| ⬡ Rectángulo doble | `[[subprograma()]]` | Llamadas a funciones |
| ○ Círculo | `(A)` | Conectores |

---

## Índice de Diagramas

| No. | Algoritmo | Descripción |
|-----|-----------|-------------|
| 1 | Registrar Animal | Flujo completo de registro |
| 2 | Validar Arete SINIGA | Validación de identificador |
| 3 | Eliminar Animal | Eliminación con confirmación |
| 4 | Capturar Foto | Cámara o galería |
| 5 | Guardar Animal en BD | Persistencia |
| 6 | Operaciones CRUD DAO | Acceso a datos |
| 7 | Registrar Venta | Proceso de venta |
| 8 | Programar Evento Sanitario | Calendario sanitario |
| 9 | Mostrar DatePicker | Selector de fecha |
| 10 | Ciclo de Vida Activity | Lifecycle Android |

---

## 1. Diagrama de Flujo: Registrar Animal

```plantuml
@startuml DF_RegistrarAnimal
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE
skinparam defaultFontSize 10

title DIAGRAMA DE FLUJO: REGISTRAR ANIMAL - guardarAnimal()

(*) --> "INICIO"
"INICIO" --> >/Leer: arete, raza,\nsexo, fechas, precio/
>/Leer: arete, raza,\nsexo, fechas, precio/ --> [Obtener datos\ndel formulario]

[Obtener datos\ndel formulario] --> <¿arete vacío?>

<¿arete vacío?> --> [Sí] [Mostrar error:\n"Arete obligatorio"]
[Mostrar error:\n"Arete obligatorio"] --> (A)
(A) --> "FIN"

<¿arete vacío?> --> [No] <¿formato ≠ 10 dígitos?>

<¿formato ≠ 10 dígitos?> --> [Sí] [Mostrar error:\n"Formato inválido"]
[Mostrar error:\n"Formato inválido"] --> (A)

<¿formato ≠ 10 dígitos?> --> [No] [[validarAreteUnico()]]

[[validarAreteUnico()]] --> <¿arete existe en BD?>

<¿arete existe en BD?> --> [Sí] [Mostrar error:\n"Arete duplicado"]
[Mostrar error:\n"Arete duplicado"] --> (A)

<¿arete existe en BD?> --> [No] <¿precio ≤ 0?>

<¿precio ≤ 0?> --> [Sí] [Mostrar error:\n"Precio inválido"]
[Mostrar error:\n"Precio inválido"] --> (A)

<¿precio ≤ 0?> --> [No] [[validarFechas()]]

[[validarFechas()]] --> <¿fechaNac > fechaIng?>

<¿fechaNac > fechaIng?> --> [Sí] [Mostrar error:\n"Fechas incoherentes"]
[Mostrar error:\n"Fechas incoherentes"] --> (A)

<¿fechaNac > fechaIng?> --> [No] (B)

(B) --> [Crear objeto Animal]
[Crear objeto Animal] --> [Asignar atributos\nal objeto]
[Asignar atributos\nal objeto] --> [[guardarEnBD()]]

[[guardarEnBD()]] --> <¿guardado exitoso?>

<¿guardado exitoso?> --> [No] [Mostrar error:\n"Error al guardar"]
[Mostrar error:\n"Error al guardar"] --> (A)

<¿guardado exitoso?> --> [Sí] >/Mostrar: "Animal\nregistrado"/
>/Mostrar: "Animal\nregistrado"/ --> [Cerrar Activity]
[Cerrar Activity] --> "FIN"

@enduml
```

---

## 2. Diagrama de Flujo: Validar Arete SINIGA

```plantuml
@startuml DF_ValidarArete
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: VALIDAR ARETE SINIGA - validarArete(String arete)

(*) --> "INICIO"
"INICIO" --> >/Recibir: arete/

>/Recibir: arete/ --> <¿arete == null?>

<¿arete == null?> --> [Sí] >/Retornar: false/
>/Retornar: false/ --> "FIN"

<¿arete == null?> --> [No] [arete = arete.trim()]

[arete = arete.trim()] --> <¿arete.isEmpty()?>

<¿arete.isEmpty()?> --> [Sí] [Mostrar error:\n"Arete obligatorio"]
[Mostrar error:\n"Arete obligatorio"] --> (E)
(E) --> >/Retornar: false/

<¿arete.isEmpty()?> --> [No] <¿length ≠ 10?>

<¿length ≠ 10?> --> [Sí] [Mostrar error:\n"Debe tener 10 caracteres"]
[Mostrar error:\n"Debe tener 10 caracteres"] --> (E)

<¿length ≠ 10?> --> [No] <¿!matches("\\d{10}")?>

<¿!matches("\\d{10}")?> --> [Sí] [Mostrar error:\n"Solo dígitos numéricos"]
[Mostrar error:\n"Solo dígitos numéricos"] --> (E)

<¿!matches("\\d{10}")?> --> [No] >/Retornar: true/
>/Retornar: true/ --> "FIN"

@enduml
```

---

## 3. Diagrama de Flujo: Eliminar Animal

```plantuml
@startuml DF_EliminarAnimal
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: ELIMINAR ANIMAL - confirmarEliminacion()

(*) --> "INICIO"
"INICIO" --> >/Recibir: animalId/

>/Recibir: animalId/ --> [Crear AlertDialog\n"¿Eliminar animal?"]
[Crear AlertDialog\n"¿Eliminar animal?"] --> >/Mostrar diálogo/

>/Mostrar diálogo/ --> <¿Usuario confirma?>

<¿Usuario confirma?> --> [No/Cancelar] [Cerrar diálogo]
[Cerrar diálogo] --> "FIN"

<¿Usuario confirma?> --> [Sí/Eliminar] [[animalDAO.eliminar()]]

[[animalDAO.eliminar()]] --> [DELETE FROM animales\nWHERE id = animalId]

[DELETE FROM animales\nWHERE id = animalId] --> <¿filas afectadas > 0?>

<¿filas afectadas > 0?> --> [No] [Mostrar error:\n"No se pudo eliminar"]
[Mostrar error:\n"No se pudo eliminar"] --> "FIN"

<¿filas afectadas > 0?> --> [Sí] [CASCADE: Eliminar\nregistros relacionados]

note right of [CASCADE: Eliminar\nregistros relacionados]
  - calendario_sanitario
  - historial_clinico
  - gastos
  - alimentacion
end note

[CASCADE: Eliminar\nregistros relacionados] --> >/Mostrar: "Animal eliminado"/
>/Mostrar: "Animal eliminado"/ --> [Actualizar lista]
[Actualizar lista] --> [finish()]
[finish()] --> "FIN"

@enduml
```

---

## 4. Diagrama de Flujo: Capturar Foto

```plantuml
@startuml DF_CapturarFoto
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: CAPTURAR FOTO

(*) --> "INICIO"

"INICIO" --> <¿Fuente de imagen?>

' === RAMA GALERÍA ===
<¿Fuente de imagen?> --> [Galería] [Intent ACTION_PICK\nMediaStore.Images]
[Intent ACTION_PICK\nMediaStore.Images] --> [startActivityForResult\n(PICK_IMAGE)]
[startActivityForResult\n(PICK_IMAGE)] --> (W)

' === RAMA CÁMARA ===
<¿Fuente de imagen?> --> [Cámara] [[verificarPermisos()]]

[[verificarPermisos()]] --> <¿Permiso cámara?>

<¿Permiso cámara?> --> [No] [Solicitar permiso]
[Solicitar permiso] --> <¿Concedido?>
<¿Concedido?> --> [No] >/Mostrar: "Permiso denegado"/
>/Mostrar: "Permiso denegado"/ --> "FIN"

<¿Concedido?> --> [Sí] (C)
<¿Permiso cámara?> --> [Sí] (C)

(C) --> [[crearArchivoImagen()]]
[[crearArchivoImagen()]] --> [Generar nombre:\nANIMAL_timestamp.jpg]
[Generar nombre:\nANIMAL_timestamp.jpg] --> [FileProvider.getUriForFile()]
[FileProvider.getUriForFile()] --> [Intent ACTION_IMAGE_CAPTURE]
[Intent ACTION_IMAGE_CAPTURE] --> [startActivityForResult\n(TAKE_PHOTO)]
[startActivityForResult\n(TAKE_PHOTO)] --> (W)

' === CALLBACK ===
(W) --> [onActivityResult()]

[onActivityResult()] --> <¿resultCode == RESULT_OK?>

<¿resultCode == RESULT_OK?> --> [No] "FIN"

<¿resultCode == RESULT_OK?> --> [Sí] <¿requestCode?>

<¿requestCode?> --> [PICK_IMAGE] [bitmap = getBitmap\n(data.getData())]
<¿requestCode?> --> [TAKE_PHOTO] [bitmap = decodeFile\n(currentPhotoPath)]

[bitmap = getBitmap\n(data.getData())] --> (R)
[bitmap = decodeFile\n(currentPhotoPath)] --> (R)

(R) --> [[procesarImagen()]]
[[procesarImagen()]] --> [Redimensionar max 800px]
[Redimensionar max 800px] --> [Comprimir JPEG 70%]
[Comprimir JPEG 70%] --> [Convertir a Base64]
[Convertir a Base64] --> [fotoBase64 = resultado]
[fotoBase64 = resultado] --> >/Mostrar preview/
>/Mostrar preview/ --> "FIN"

@enduml
```

---

## 5. Diagrama de Flujo: Guardar Animal en BD

```plantuml
@startuml DF_GuardarAnimalBD
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: GUARDAR ANIMAL EN BD

(*) --> "INICIO"
"INICIO" --> >/Recibir: Animal,\nmodoEdicion/

>/Recibir: Animal,\nmodoEdicion/ --> <¿modoEdicion?>

' =========== RAMA ACTUALIZAR ===========
<¿modoEdicion?> --> [Sí] [db = getWritableDatabase()]
[db = getWritableDatabase()] --> [ContentValues values\n= new ContentValues()]
[ContentValues values\n= new ContentValues()] --> [Poblar values con\natributos del animal]
[Poblar values con\natributos del animal] --> [rows = db.update()]

[rows = db.update()] --> <¿rows > 0?>
<¿rows > 0?> --> [No] >/Retornar: -1 (error)/
<¿rows > 0?> --> [Sí] >/Retornar: rows (éxito)/
>/Retornar: -1 (error)/ --> "FIN"
>/Retornar: rows (éxito)/ --> "FIN"

' =========== RAMA INSERTAR ===========
<¿modoEdicion?> --> [No] [[existeArete()]]

[[existeArete()]] --> <¿arete existe?>
<¿arete existe?> --> [Sí] >/Retornar: -1 (duplicado)/
>/Retornar: -1 (duplicado)/ --> "FIN"

<¿arete existe?> --> [No] (I)
(I) --> [db = getWritableDatabase() 2]
[db = getWritableDatabase() 2] --> [Crear ContentValues]
[Crear ContentValues] --> [Poblar todos los campos]
[Poblar todos los campos] --> [id = db.insert()]

[id = db.insert()] --> <¿id > 0?>
<¿id > 0?> --> [No] >/Retornar: -1 (error) 2/
<¿id > 0?> --> [Sí] >/Retornar: id (nuevo)/
>/Retornar: -1 (error) 2/ --> "FIN"
>/Retornar: id (nuevo)/ --> "FIN"

@enduml
```

---

## 6. Diagrama de Flujo: Registrar Venta

```plantuml
@startuml DF_RegistrarVenta
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: REGISTRAR VENTA

(*) --> "INICIO"
"INICIO" --> >/Recibir: animalId/

>/Recibir: animalId/ --> [AlertDialog.Builder\n"Registrar Venta"]
[AlertDialog.Builder\n"Registrar Venta"] --> [Agregar EditText\npara precio y fecha]
[Agregar EditText\npara precio y fecha] --> >/Mostrar diálogo/

>/Mostrar diálogo/ --> <¿Acción usuario?>

<¿Acción usuario?> --> [Cancelar] "FIN"

<¿Acción usuario?> --> [Confirmar] >/Leer: precioVenta,\nfechaVenta/

>/Leer: precioVenta,\nfechaVenta/ --> <¿precioVenta <= 0?>

<¿precioVenta <= 0?> --> [Sí] >/Mostrar: "Precio inválido"/
>/Mostrar: "Precio inválido"/ --> >/Mostrar diálogo/

<¿precioVenta <= 0?> --> [No] [animal.setEstado("VENDIDO")]
[animal.setEstado("VENDIDO")] --> [animal.setPrecioVenta(precio)]
[animal.setPrecioVenta(precio)] --> [animal.setFechaSalida(fecha)]
[animal.setFechaSalida(fecha)] --> [[animalDAO.actualizar()]]

[[animalDAO.actualizar()]] --> <¿actualización exitosa?>

<¿actualización exitosa?> --> [No] >/Mostrar: "Error"/
>/Mostrar: "Error"/ --> "FIN"

<¿actualización exitosa?> --> [Sí] [ganancia = precioVenta\n- precioCompra - gastos]
[ganancia = precioVenta\n- precioCompra - gastos] --> >/Mostrar: "Venta registrada\nGanancia: $X"/
>/Mostrar: "Venta registrada\nGanancia: $X"/ --> [Actualizar UI]
[Actualizar UI] --> [Deshabilitar edición]
[Deshabilitar edición] --> "FIN"

@enduml
```

---

## 7. Diagrama de Flujo: Programar Evento Sanitario

```plantuml
@startuml DF_ProgramarEvento
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: PROGRAMAR EVENTO SANITARIO

(*) --> "INICIO"
"INICIO" --> >/Entrada: tipoEvento,\nraza, fecha, hora/

>/Entrada: tipoEvento,\nraza, fecha, hora/ --> [[animalDAO.obtenerPorRaza()]]
[[animalDAO.obtenerPorRaza()]] --> [List<Animal> animales]

[List<Animal> animales] --> <¿animales.isEmpty()?>

<¿animales.isEmpty()?> --> [Sí] >/Mostrar: "No hay animales\nde esta raza"/
>/Mostrar: "No hay animales\nde esta raza"/ --> "FIN"

<¿animales.isEmpty()?> --> [No] [int i = 0]

[int i = 0] --> <¿i < animales.size()?>

<¿i < animales.size()?> --> [No] (F)

<¿i < animales.size()?> --> [Sí] [animal = animales.get(i)]
[animal = animales.get(i)] --> [Crear EventoSanitario]
[Crear EventoSanitario] --> [Asignar animalId,\ntipoEvento, fecha]
[Asignar animalId,\ntipoEvento, fecha] --> [evento.setEstado("PENDIENTE")]
[evento.setEstado("PENDIENTE")] --> [[eventoDAO.insertar()]]

[[eventoDAO.insertar()]] --> <¿hora != null?>

<¿hora != null?> --> [Sí] [[NotificationHelper.programar()]]
[[NotificationHelper.programar()]] --> [i++]

<¿hora != null?> --> [No] [i++]

[i++] --> <¿i < animales.size()?>

(F) --> >/Mostrar: "X eventos creados"/
>/Mostrar: "X eventos creados"/ --> [Actualizar calendario]
[Actualizar calendario] --> "FIN"

@enduml
```

---

## 8. Diagrama de Flujo: Mostrar DatePicker

```plantuml
@startuml DF_DatePicker
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: SELECTOR DE FECHA

(*) --> "INICIO"
"INICIO" --> >/Recibir: esFechaNacimiento/

>/Recibir: esFechaNacimiento/ --> [SimpleDateFormat sdf\n= "dd/MM/yyyy"]
[SimpleDateFormat sdf\n= "dd/MM/yyyy"] --> [Obtener year, month, day\nde Calendar]
[Obtener year, month, day\nde Calendar] --> [Crear DatePickerDialog]
[Crear DatePickerDialog] --> >/dialog.show()/

>/dialog.show()/ --> [Usuario selecciona fecha]
[Usuario selecciona fecha] --> [onDateSet(year, month, day)]
[onDateSet(year, month, day)] --> [calendar.set(year, month, day)]
[calendar.set(year, month, day)] --> [String fecha =\nsdf.format(calendar.getTime())]

[String fecha =\nsdf.format(calendar.getTime())] --> <¿esFechaNacimiento?>

<¿esFechaNacimiento?> --> [Sí] [fechaNacimiento = fecha]
[fechaNacimiento = fecha] --> [btnFechaNacimiento\n.setText(fecha)]
[btnFechaNacimiento\n.setText(fecha)] --> "FIN"

<¿esFechaNacimiento?> --> [No] [fechaIngreso = fecha]
[fechaIngreso = fecha] --> [btnFechaAdquisicion\n.setText(fecha)]
[btnFechaAdquisicion\n.setText(fecha)] --> "FIN"

@enduml
```

---

## 9. Diagrama de Flujo: Ciclo de Vida Activity

```plantuml
@startuml DF_CicloVida
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: CICLO DE VIDA ACTIVITY

(*) --> "INICIO"

"INICIO" --> [onCreate(Bundle)]
[onCreate(Bundle)] --> [super.onCreate()]
[super.onCreate()] --> [setContentView()]
[setContentView()] --> [inicializarVistas()]
[inicializarVistas()] --> [configurarSpinners()]
[configurarSpinners()] --> [configurarListeners()]

[configurarListeners()] --> [onStart()]
[onStart()] --> [onResume()]

[onResume()] --> [[BaseActivity.verificarSesion()]]

[[BaseActivity.verificarSesion()]] --> <¿tiempo > 10s?>

<¿tiempo > 10s?> --> [Sí] >/Mostrar diálogo contraseña/
>/Mostrar diálogo contraseña/ --> <¿Contraseña correcta?>
<¿Contraseña correcta?> --> [No] [Ir a LoginActivity]
[Ir a LoginActivity] --> "FIN"
<¿Contraseña correcta?> --> [Sí] (R)

<¿tiempo > 10s?> --> [No] (R)

(R) --> [ACTIVITY RUNNING]

[ACTIVITY RUNNING] --> <¿Evento?>
<¿Evento?> --> [Usuario interactúa] [ACTIVITY RUNNING]
<¿Evento?> --> [Otra Activity al frente] [onPause()]

[onPause()] --> [[guardarTiempoActividad()]]
[[guardarTiempoActividad()]] --> [SharedPreferences\n.putLong(timestamp)]
[SharedPreferences\n.putLong(timestamp)] --> [onStop()]

[onStop()] --> <¿Vuelve?>
<¿Vuelve?> --> [Sí] [onRestart()]
[onRestart()] --> [onStart()]

<¿Vuelve?> --> [No] [onDestroy()]
[onDestroy()] --> [presenter.destruir()]
[presenter.destruir()] --> [Liberar recursos]
[Liberar recursos] --> "FIN"

@enduml
```

---

## 10. Diagrama de Flujo: Operaciones CRUD DAO

```plantuml
@startuml DF_CRUD_DAO
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

title DIAGRAMA DE FLUJO: OPERACIONES CRUD - AnimalDAO

(*) --> "INICIO"
"INICIO" --> >/Recibir: operación CRUD/

>/Recibir: operación CRUD/ --> <¿Tipo operación?>

' =========== CREATE ===========
<¿Tipo operación?> --> [CREATE] >/Entrada: Animal/
>/Entrada: Animal/ --> [[existeArete()]]
[[existeArete()]] --> <¿duplicado?>
<¿duplicado?> --> [Sí] >/Retornar: -1/
<¿duplicado?> --> [No] [db.insert()]
[db.insert()] --> >/Retornar: newId/
>/Retornar: -1/ --> "FIN"
>/Retornar: newId/ --> "FIN"

' =========== READ ===========
<¿Tipo operación?> --> [READ] <¿Por ID o Todos?>
<¿Por ID o Todos?> --> [Por ID] >/Entrada: int id/
>/Entrada: int id/ --> [db.query(WHERE id=?)]
[db.query(WHERE id=?)] --> <¿cursor.moveToFirst()?>
<¿cursor.moveToFirst()?> --> [No] >/Retornar: null/
<¿cursor.moveToFirst()?> --> [Sí] [[cursorToAnimal()]]
[[cursorToAnimal()]] --> >/Retornar: Animal/
>/Retornar: null/ --> "FIN"
>/Retornar: Animal/ --> "FIN"

<¿Por ID o Todos?> --> [Todos] [db.query(ORDER BY)]
[db.query(ORDER BY)] --> [List<Animal> lista]
[List<Animal> lista] --> <¿cursor.moveToNext()?>
<¿cursor.moveToNext()?> --> [Sí] [[cursorToAnimal() 2]]
[[cursorToAnimal() 2]] --> [lista.add(animal)]
[lista.add(animal)] --> <¿cursor.moveToNext()?>
<¿cursor.moveToNext()?> --> [No] [cursor.close()]
[cursor.close()] --> >/Retornar: lista/
>/Retornar: lista/ --> "FIN"

' =========== UPDATE ===========
<¿Tipo operación?> --> [UPDATE] >/Entrada: Animal 2/
>/Entrada: Animal 2/ --> [ContentValues con valores]
[ContentValues con valores] --> [db.update()]
[db.update()] --> >/Retornar: rows/
>/Retornar: rows/ --> "FIN"

' =========== DELETE ===========
<¿Tipo operación?> --> [DELETE] >/Entrada: int id 2/
>/Entrada: int id 2/ --> [db.delete()]
[db.delete()] --> >/Retornar: rows 2/

note right of [db.delete()]
  ON DELETE CASCADE:
  - calendario_sanitario
  - historial_clinico
  - gastos
  - alimentacion
end note

>/Retornar: rows 2/ --> "FIN"

@enduml
```

---

## Leyenda de Símbolos PlantUML

| Símbolo Clásico | PlantUML | Descripción |
|-----------------|----------|-------------|
| ⬭ Óvalo | `(*)` → `"INICIO"/"FIN"` | Inicio y Fin del diagrama |
| ▭ Rectángulo | `[Proceso]` | Acción o proceso |
| ◇ Rombo | `<¿Condición?>` | Punto de decisión |
| ▱ Paralelogramo | `>/Entrada/Salida/` | Entrada o salida de datos |
| ⬡ Rectángulo doble | `[[subprograma()]]` | Llamada a función/subrutina |
| ○ Círculo | `(A)`, `(B)`, `(C)` | Conector entre partes |

### Sintaxis de conexiones:

```
[Proceso A] --> [Proceso B]           Conexión simple
<Condición> --> [Sí] [Proceso]        Conexión con etiqueta
```

---

> **Nota**: Todos los diagramas usan `left to right direction` para orientación horizontal. Los conectores `(A)`, `(B)`, etc. unen partes del diagrama para evitar líneas largas.
