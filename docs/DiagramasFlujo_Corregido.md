# Diagramas de Flujo - Algoritmos del Sistema AgroApp
## Notación Clásica de Diagramas de Flujo (Sintaxis Corregida)

### Simbología Utilizada

| Símbolo | Nombre | Descripción |
|---------|--------|-------------|
| ⬭ (Óvalo) | Inicio/Fin | Indica el comienzo o término del diagrama |
| ▭ (Rectángulo) | Proceso/Acción | Representa una operación o instrucción |
| ◇ (Rombo) | Decisión | Punto de bifurcación condicional |
| ▱ (Paralelogramo) | Entrada/Salida | Entrada o salida de datos |
| ⬡ (Rectángulo doble) | Subprograma | Llamada a otro procedimiento |
| ○ (Círculo) | Conector | Une partes del diagrama |

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
skinparam backgroundColor #FEFEFE
skinparam defaultFontSize 11
skinparam ActivityBackgroundColor #E8F5E9
skinparam ActivityBorderColor #2E7D32
skinparam DiamondBackgroundColor #FFF9C4
skinparam DiamondBorderColor #F57F17

title DIAGRAMA DE FLUJO: REGISTRAR ANIMAL\nguardarAnimal()

start
:Leer: arete, raza,
sexo, fechas, precio/
:Obtener datos
del formulario;

if (¿arete vacío?) then (Sí)
  :Mostrar error:
  "Arete obligatorio";
  stop
else (No)
endif

if (¿formato ≠ 10 dígitos?) then (Sí)
  :Mostrar error:
  "Formato inválido";
  stop
else (No)
endif

:validarAreteUnico()|

if (¿arete existe en BD?) then (Sí)
  :Mostrar error:
  "Arete duplicado";
  stop
else (No)
endif

if (¿precio ≤ 0?) then (Sí)
  :Mostrar error:
  "Precio inválido";
  stop
else (No)
endif

:validarFechas()|

if (¿fechaNac > fechaIng?) then (Sí)
  :Mostrar error:
  "Fechas incoherentes";
  stop
else (No)
endif

:Crear objeto Animal;
:Asignar atributos al objeto;
:guardarEnBD()|

if (¿guardado exitoso?) then (Sí)
  :Mostrar: "Animal registrado"/
  :Cerrar Activity;
  stop
else (No)
  :Mostrar error:
  "Error al guardar";
  stop
endif

@enduml
```

---

## 2. Diagrama de Flujo: Validar Arete SINIGA

```plantuml
@startuml DF_ValidarArete
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #E3F2FD
skinparam ActivityBorderColor #1565C0
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: VALIDAR ARETE SINIGA\nvalidarArete(String arete)

start
:Recibir: arete/

if (¿arete == null?) then (Sí)
  :Retornar: false/
  stop
else (No)
endif

:arete = arete.trim();

if (¿arete.isEmpty()?) then (Sí)
  :Mostrar error:
  "Arete obligatorio";
  :Retornar: false/
  stop
else (No)
endif

if (¿length ≠ 10?) then (Sí)
  :Mostrar error:
  "Debe tener 10 caracteres";
  :Retornar: false/
  stop
else (No)
endif

if (¿!matches("\\d{10}")?) then (Sí)
  :Mostrar error:
  "Solo dígitos numéricos";
  :Retornar: false/
  stop
else (No)
endif

:Retornar: true/
stop

@enduml
```

---

## 3. Diagrama de Flujo: Eliminar Animal

```plantuml
@startuml DF_EliminarAnimal
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #FFEBEE
skinparam ActivityBorderColor #C62828
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: ELIMINAR ANIMAL\nconfirmarEliminacion()

start
:Recibir: animalId/
:Crear AlertDialog
"¿Eliminar animal?";
:Mostrar diálogo confirmación/

if (¿Usuario confirma?) then (No/Cancelar)
  :Cerrar diálogo;
  stop
else (Sí/Eliminar)
endif

:animalDAO.eliminar()|
:DELETE FROM animales
WHERE id = animalId;

if (¿filas afectadas > 0?) then (No)
  :Mostrar error:
  "No se pudo eliminar";
  stop
else (Sí)
endif

:CASCADE: Eliminar
registros relacionados;

note right
  - calendario_sanitario
  - historial_clinico
  - gastos
  - alimentacion
end note

:Mostrar: "Animal eliminado"/
:Actualizar lista;
:finish();
stop

@enduml
```

---

## 4. Diagrama de Flujo: Capturar Foto

```plantuml
@startuml DF_CapturarFoto
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #FFF3E0
skinparam ActivityBorderColor #E65100
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: CAPTURAR FOTO\nFlujo de imagen (cámara/galería)

start

if (¿Fuente de imagen?) then (Galería)
  :Intent ACTION_PICK
  MediaStore.Images;
  :startActivityForResult
  (intent, PICK_IMAGE);
else (Cámara)
  :verificarPermisos()|
  
  if (¿Permiso cámara?) then (No)
    :Solicitar permiso;
    if (¿Concedido?) then (No)
      :Mostrar: "Permiso denegado"/
      stop
    else (Sí)
    endif
  else (Sí)
  endif
  
  :crearArchivoImagen()|
  :Generar nombre:
  ANIMAL_timestamp.jpg;
  :FileProvider.getUriForFile();
  :Intent ACTION_IMAGE_CAPTURE
  EXTRA_OUTPUT = uri;
  :startActivityForResult
  (intent, TAKE_PHOTO);
endif

:onActivityResult
(requestCode, resultCode, data);

if (¿resultCode == RESULT_OK?) then (No)
  stop
else (Sí)
endif

if (¿requestCode?) then (PICK_IMAGE)
  :bitmap = getBitmap
  (data.getData());
else (TAKE_PHOTO)
  :bitmap = decodeFile
  (currentPhotoPath);
endif

:procesarImagen()|
:Redimensionar max 800px;
:Comprimir JPEG 70%;
:Convertir a Base64;
:fotoBase64 = resultado;
:Mostrar preview en ImageView/
:Mostrar: "Foto guardada"/
stop

@enduml
```

---

## 5. Diagrama de Flujo: Guardar Animal en BD

```plantuml
@startuml DF_GuardarAnimalBD
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #E8F5E9
skinparam ActivityBorderColor #2E7D32
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: GUARDAR ANIMAL EN BD\nAnimalDAO.insertarAnimal() / actualizarAnimal()

start
:Recibir: Animal animal,
boolean modoEdicion/

if (¿modoEdicion?) then (Sí)
  :db = getWritableDatabase();
  :ContentValues values
  = new ContentValues();
  :Poblar values con
  atributos del animal;
  :rows = db.update(
  "animales", values,
  "id=?", {animal.id});
  
  if (¿rows > 0?) then (No)
    :Retornar: -1 (error)/
    stop
  else (Sí)
    :Retornar: rows (éxito)/
    stop
  endif
  
else (No)
  :existeArete()|
  
  if (¿arete existe?) then (Sí)
    :Retornar: -1 (duplicado)/
    stop
  else (No)
  endif
  
  :db = getWritableDatabase();
  :ContentValues values
  = new ContentValues();
  :values.put("numero_arete",
  animal.getNumeroArete());
  :values.put("raza",
  animal.getRaza());
  :values.put("sexo",
  animal.getSexo());
  :... poblar resto de campos ...;
  :id = db.insert(
  "animales", null, values);
  
  if (¿id > 0?) then (No)
    :Retornar: -1 (error)/
    stop
  else (Sí)
    :Retornar: id (nuevo ID)/
    stop
  endif
endif

@enduml
```

---

## 6. Diagrama de Flujo: Operaciones CRUD DAO

```plantuml
@startuml DF_CRUD_DAO
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #E3F2FD
skinparam ActivityBorderColor #1565C0
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: OPERACIONES CRUD\nAnimalDAO

start
:Recibir: operación CRUD/

switch (¿Tipo de operación?)
case (CREATE)
  :Entrada: Animal animal/
  :existeArete()|
  if (¿duplicado?) then (Sí)
    :Retornar: -1/
    stop
  else (No)
    :db.insert();
    :Retornar: newId/
    stop
  endif

case (READ por ID)
  :Entrada: int id/
  :db.query(WHERE id = ?);
  if (¿cursor.moveToFirst()?) then (No)
    :Retornar: null/
    stop
  else (Sí)
    :cursorToAnimal()|
    :Retornar: Animal/
    stop
  endif

case (READ todos)
  :db.query(ORDER BY nombre);
  :List<Animal> lista = new ArrayList();
  while (¿cursor.moveToNext()?) is (Sí)
    :cursorToAnimal()|
    :lista.add(animal);
  endwhile (No)
  :cursor.close();
  :Retornar: lista/
  stop

case (UPDATE)
  :Entrada: Animal animal/
  :ContentValues con nuevos valores;
  :rows = db.update(WHERE id = animal.id);
  :Retornar: rows/
  stop

case (DELETE)
  :Entrada: int id/
  :rows = db.delete(WHERE id = ?);
  note right
    ON DELETE CASCADE
    elimina automáticamente:
    - calendario_sanitario
    - historial_clinico
    - gastos
    - alimentacion
  end note
  :Retornar: rows/
  stop

endswitch

@enduml
```

---

## 7. Diagrama de Flujo: Registrar Venta

```plantuml
@startuml DF_RegistrarVenta
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #FFF8E1
skinparam ActivityBorderColor #F57F17
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: REGISTRAR VENTA\nmostrarDialogoRegistrarVenta()

start
:Recibir: animalId/
:AlertDialog.Builder
"Registrar Venta";
:Agregar EditText para precio;
:Agregar EditText para fecha;
:Mostrar diálogo/

if (¿Acción usuario?) then (Cancelar)
  stop
else (Confirmar)
endif

:Leer: precioVenta, fechaVenta/

if (¿precioVenta <= 0?) then (Sí)
  :Mostrar: "Precio inválido"/
  stop
else (No)
endif

:animal.setEstado("VENDIDO");
:animal.setPrecioVenta(precio);
:animal.setFechaSalida(fecha);
:animalDAO.actualizar()|

if (¿actualización exitosa?) then (No)
  :Mostrar: "Error al registrar"/
  stop
else (Sí)
endif

:ganancia = precioVenta
- precioCompra - gastos;
:Mostrar: "Venta registrada
Ganancia: $X"/
:Actualizar UI (estado = VENDIDO);
:Deshabilitar botones edición;
stop

@enduml
```

---

## 8. Diagrama de Flujo: Programar Evento Sanitario

```plantuml
@startuml DF_ProgramarEvento
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #E8F5E9
skinparam ActivityBorderColor #388E3C
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: PROGRAMAR EVENTO SANITARIO\ncrearEventoPorRaza()

start
:Entrada: tipoEvento,
raza, fecha, hora/
:animalDAO.obtenerPorRaza()|
:List<Animal> animales;

if (¿animales.isEmpty()?) then (Sí)
  :Mostrar: "No hay animales
  de esta raza"/
  stop
else (No)
endif

:int i = 0;

while (¿i < animales.size()?) is (Sí)
  :animal = animales.get(i);
  :EventoSanitario evento
  = new EventoSanitario();
  :evento.setAnimalId(animal.getId());
  :evento.setTipoEvento(tipoEvento);
  :evento.setFechaProgramada(fecha);
  :evento.setHoraRecordatorio(hora);
  :evento.setEstado("PENDIENTE");
  :eventoDAO.insertar()|
  :eventoId = resultado;
  
  if (¿hora != null?) then (Sí)
    :NotificationHelper.programar()|
  else (No)
  endif
  
  :i++;
endwhile (No)

:Mostrar: "X eventos creados"/
:Actualizar calendario;
stop

@enduml
```

---

## 9. Diagrama de Flujo: Mostrar DatePicker

```plantuml
@startuml DF_DatePicker
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #FCE4EC
skinparam ActivityBorderColor #C2185B
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: SELECTOR DE FECHA\nmostrarDatePicker(boolean esFechaNacimiento)

start
:Recibir: esFechaNacimiento/
:SimpleDateFormat sdf = "dd/MM/yyyy";
:year = calendar.get(YEAR)
month = calendar.get(MONTH)
day = calendar.get(DAY);
:DatePickerDialog dialog
= new DatePickerDialog(
context, listener,
year, month, day);
:dialog.show()/

:Usuario selecciona fecha en calendario;
:onDateSet(year, month, day);
:calendar.set(year, month, day);
:String fecha =
sdf.format(calendar.getTime());

if (¿esFechaNacimiento?) then (Sí)
  :fechaNacimiento[0] = fecha;
  :btnFechaNacimiento.setText(fecha);
else (No)
  :fechaIngreso[0] = fecha;
  :btnFechaAdquisicion.setText(fecha);
endif

stop

@enduml
```

---

## 10. Diagrama de Flujo: Ciclo de Vida Activity

```plantuml
@startuml DF_CicloVida
!theme plain
skinparam backgroundColor #FEFEFE
skinparam ActivityBackgroundColor #E3F2FD
skinparam ActivityBorderColor #1976D2
skinparam DiamondBackgroundColor #FFF9C4

title DIAGRAMA DE FLUJO: CICLO DE VIDA ACTIVITY\nBaseActivity + RegistroAnimalActivity

start

partition onCreate {
  :onCreate(Bundle);
  :super.onCreate();
  :setContentView() layout XML;
  :inicializarVistas();
  :configurarSpinners();
  :configurarListeners();
}

partition onStart {
  :onStart();
}

partition onResume {
  :onResume();
  :BaseActivity.verificarSesion()|
  
  if (¿tiempo > 10 segundos?) then (Sí)
    :Mostrar diálogo contraseña/
    if (¿Contraseña correcta?) then (No)
      :Volver a LoginActivity;
      stop
    else (Sí)
    endif
  else (No)
  endif
}

:ACTIVITY RUNNING;

repeat
  :Usuario interactúa;
repeat while (¿Continúa en primer plano?) is (Sí)
->No;

partition onPause {
  :onPause();
  :BaseActivity.guardarTiempoActividad()|
  :SharedPreferences.putLong(timestamp);
}

partition onStop {
  :onStop();
}

if (¿Vuelve?) then (Sí)
  :onRestart();
  backward :onStart();
else (No)
endif

partition onDestroy {
  :onDestroy();
  :presenter.destruir()
  ExecutorService.shutdown();
  :Liberar recursos;
}

stop

@enduml
```

---

## Leyenda de Símbolos PlantUML (Sintaxis Correcta)

| PlantUML | Símbolo Clásico | Descripción |
|----------|-----------------|-------------|
| `start` | Óvalo verde | Inicio del diagrama |
| `stop` | Óvalo rojo | Fin del diagrama |
| `:Texto;` | Rectángulo | Acción o proceso |
| `:Texto\|` | Rectángulo con línea | Subprograma/Llamada |
| `:Texto/` | Paralelogramo | Entrada/Salida de datos |
| `if...then...else...endif` | Rombo | Decisión binaria |
| `switch...case...endswitch` | Rombos múltiples | Decisión múltiple |
| `while...endwhile` | Bucle | Iteración |
| `partition` | Grupo | Agrupa actividades |
| `note` | Nota | Documentación |

---

## Notas sobre la Sintaxis

### Símbolos de terminación en actividades:

```
:Proceso normal;          <- Termina con punto y coma
:Subprograma()|           <- Termina con pipe (subrutina)  
:Entrada o Salida/        <- Termina con slash (I/O)
```

### Ejemplo de cada tipo:

```plantuml
@startuml ejemplo
start
:Entrada de datos/
:Procesar información;
:llamarFuncion()|
if (¿condición?) then (Sí)
  :Acción A;
else (No)
  :Acción B;
endif
:Mostrar resultado/
stop
@enduml
```

---

> **Nota**: Esta versión usa la sintaxis correcta de diagramas de actividad de PlantUML. Cada actividad usa los terminadores apropiados para indicar su tipo.
