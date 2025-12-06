# Diagramas de Flujo - Algoritmos del Sistema AgroApp
## Notación Clásica de Diagramas de Flujo

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
| 2 | Actualizar Animal | Modificación de datos |
| 3 | Eliminar Animal | Eliminación con confirmación |
| 4 | Consultar Animal | Carga de datos |
| 5 | Registrar Venta | Proceso de venta |
| 6 | Programar Evento Sanitario | Calendario sanitario |
| 7 | Validar Arete SINIGA | Validación de identificador |
| 8 | Capturar Foto | Cámara o galería |
| 9 | Guardar Animal en BD | Persistencia |
| 10 | Operaciones CRUD DAO | Acceso a datos |

---

## 1. Diagrama de Flujo: Registrar Animal

```plantuml
@startuml DF_RegistrarAnimal
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE
skinparam defaultFontSize 10

' Definir estilos para notación clásica
skinparam activity {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    DiamondBackgroundColor #FFF9C4
    DiamondBorderColor #F57F17
}

title **DIAGRAMA DE FLUJO: REGISTRAR ANIMAL**\nguardarAnimal()

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA DE DATOS ===
inicio --> ===B1===
===B1=== --> [/Leer: arete, raza,\nsexo, fechas, precio/] as entrada

' === PROCESO 1: Obtener datos ===
entrada --> [Obtener datos\ndel formulario] as proc1

' === DECISIÓN 1: Arete vacío ===
proc1 --> <¿arete vacío?> as dec1

dec1 --> [Sí] [Mostrar error:\n"Arete obligatorio"] as err1
err1 --> ===C1===
===C1=== --> "**FIN**" as fin1

dec1 --> [No] ===B2===

' === DECISIÓN 2: Formato SINIGA ===
===B2=== --> <¿formato\n≠ 10 dígitos?> as dec2

dec2 --> [Sí] [Mostrar error:\n"Formato inválido"] as err2
err2 --> ===C2===
===C2=== --> fin1

dec2 --> [No] ===B3===

' === SUBPROGRAMA: Validar único ===
===B3=== --> [[validarAreteUnico()]] as sub1

' === DECISIÓN 3: Arete existe ===
sub1 --> <¿arete\nexiste en BD?> as dec3

dec3 --> [Sí] [Mostrar error:\n"Arete duplicado"] as err3
err3 --> ===C3===
===C3=== --> fin1

dec3 --> [No] ===B4===

' === DECISIÓN 4: Precio válido ===
===B4=== --> <¿precio ≤ 0?> as dec4

dec4 --> [Sí] [Mostrar error:\n"Precio inválido"] as err4
err4 --> ===C4===
===C4=== --> fin1

dec4 --> [No] ===B5===

' === SUBPROGRAMA: Validar fechas ===
===B5=== --> [[validarFechas()]] as sub2

' === DECISIÓN 5: Fechas coherentes ===
sub2 --> <¿fechaNac >\nfechaIng?> as dec5

dec5 --> [Sí] [Mostrar error:\n"Fechas incoherentes"] as err5
err5 --> ===C5===
===C5=== --> fin1

dec5 --> [No] ===B6===

' === PROCESO: Crear Animal ===
===B6=== --> [Crear objeto\nAnimal] as proc2

proc2 --> [Asignar atributos\nal objeto] as proc3

' === SUBPROGRAMA: Guardar ===
proc3 --> [[guardarEnBD()]] as sub3

' === DECISIÓN 6: Éxito ===
sub3 --> <¿guardado\nexitoso?> as dec6

dec6 --> [No] [Mostrar error:\n"Error al guardar"] as err6
err6 --> ===C6===
===C6=== --> fin1

dec6 --> [Sí] ===B7===

' === SALIDA ===
===B7=== --> [/Mostrar: "Animal\nregistrado"/] as salida

salida --> [Cerrar\nActivity] as proc4

proc4 --> "**FIN**" as fin2

@enduml
```

---

## 2. Diagrama de Flujo: Validar Arete SINIGA

```plantuml
@startuml DF_ValidarArete
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #E3F2FD
    BorderColor #1565C0
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: VALIDAR ARETE SINIGA**\nvalidarArete(String arete)

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir: arete/] as entrada

' === DECISIÓN 1: Null ===
entrada --> <¿arete == null?> as dec1

dec1 --> [Sí] ===E1===
===E1=== --> [/Retornar: false/] as ret1
ret1 --> "**FIN**" as fin

dec1 --> [No] ===B1===

' === PROCESO: Trim ===
===B1=== --> [arete = arete.trim()] as proc1

' === DECISIÓN 2: Vacío ===
proc1 --> <¿arete.isEmpty()?> as dec2

dec2 --> [Sí] [Mostrar error:\n"Arete obligatorio"] as err1
err1 --> ===E2===
===E2=== --> ret1

dec2 --> [No] ===B2===

' === DECISIÓN 3: Longitud ===
===B2=== --> <¿length ≠ 10?> as dec3

dec3 --> [Sí] [Mostrar error:\n"Debe tener 10 caracteres"] as err2
err2 --> ===E3===
===E3=== --> ret1

dec3 --> [No] ===B3===

' === DECISIÓN 4: Solo dígitos ===
===B3=== --> <¿!matches("\\d{10}")?> as dec4

dec4 --> [Sí] [Mostrar error:\n"Solo dígitos numéricos"] as err3
err3 --> ===E4===
===E4=== --> ret1

dec4 --> [No] ===B4===

' === SALIDA EXITOSA ===
===B4=== --> [/Retornar: true/] as ret2

ret2 --> "**FIN**" as fin2

@enduml
```

---

## 3. Diagrama de Flujo: Eliminar Animal

```plantuml
@startuml DF_EliminarAnimal
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #FFEBEE
    BorderColor #C62828
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: ELIMINAR ANIMAL**\nconfirmarEliminacion()

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir: animalId/] as entrada

' === PROCESO: Mostrar diálogo ===
entrada --> [Crear AlertDialog\n"¿Eliminar animal?"] as proc1

' === SALIDA: Mostrar opciones ===
proc1 --> [/Mostrar:\ndiálogo confirmación/] as salida1

' === DECISIÓN: Confirma ===
salida1 --> <¿Usuario\nconfirma?> as dec1

dec1 --> [No/Cancelar] [Cerrar\ndiálogo] as proc2
proc2 --> "**FIN**" as fin1

dec1 --> [Sí/Eliminar] ===B1===

' === SUBPROGRAMA: Eliminar ===
===B1=== --> [[animalDAO.eliminar()]] as sub1

' === PROCESO: Ejecutar en BD ===
sub1 --> [DELETE FROM animales\nWHERE id = animalId] as proc3

' === DECISIÓN: Éxito ===
proc3 --> <¿filas\nafectadas > 0?> as dec2

dec2 --> [No] [Mostrar error:\n"No se pudo eliminar"] as err1
err1 --> "**FIN**" as fin2

dec2 --> [Sí] ===B2===

' === CASCADE ===
===B2=== --> [CASCADE: Eliminar\nregistros relacionados] as proc4

note right of proc4
  - calendario_sanitario
  - historial_clinico
  - gastos
  - alimentacion
end note

' === SALIDA EXITOSA ===
proc4 --> [/Mostrar:\n"Animal eliminado"/] as salida2

salida2 --> [Actualizar\nlista] as proc5

proc5 --> [finish()] as proc6

proc6 --> "**FIN**" as fin3

@enduml
```

---

## 4. Diagrama de Flujo: Capturar Foto

```plantuml
@startuml DF_CapturarFoto
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #FFF3E0
    BorderColor #E65100
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: CAPTURAR FOTO**\nFlujo de imagen (cámara/galería)

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === DECISIÓN: Fuente ===
inicio --> <¿Fuente de\nimagen?> as dec1

' === RAMA GALERÍA ===
dec1 --> [Galería] ===G1===
===G1=== --> [Intent ACTION_PICK\nMediaStore.Images] as procG1
procG1 --> [startActivityForResult\n(intent, PICK_IMAGE)] as procG2
procG2 --> ===WAIT===

' === RAMA CÁMARA ===
dec1 --> [Cámara] ===C1===

' === SUBPROGRAMA: Verificar permisos ===
===C1=== --> [[verificarPermisos()]] as subC1

' === DECISIÓN: Permiso ===
subC1 --> <¿Permiso\ncámara?> as decC1

decC1 --> [No] [Solicitar\npermiso] as procC1
procC1 --> <¿Concedido?> as decC2
decC2 --> [No] [/Mostrar:\n"Permiso denegado"/] as errC1
errC1 --> "**FIN**" as finC

decC2 --> [Sí] ===C2===
decC1 --> [Sí] ===C2===

' === SUBPROGRAMA: Crear archivo ===
===C2=== --> [[crearArchivoImagen()]] as subC2

subC2 --> [Generar nombre:\nANIMAL_timestamp.jpg] as procC2

procC2 --> [FileProvider.\ngetUriForFile()] as procC3

procC3 --> [Intent ACTION_IMAGE_CAPTURE\nEXTRA_OUTPUT = uri] as procC4

procC4 --> [startActivityForResult\n(intent, TAKE_PHOTO)] as procC5

procC5 --> ===WAIT===

' === CALLBACK ===
===WAIT=== --> [onActivityResult\n(requestCode, resultCode, data)] as callback

' === DECISIÓN: Resultado OK ===
callback --> <¿resultCode\n== RESULT_OK?> as decR1

decR1 --> [No] "**FIN**" as finR

decR1 --> [Sí] ===R1===

' === DECISIÓN: Tipo ===
===R1=== --> <¿requestCode?> as decR2

decR2 --> [PICK_IMAGE] [bitmap = getBitmap\n(data.getData())] as procR1

decR2 --> [TAKE_PHOTO] [bitmap = decodeFile\n(currentPhotoPath)] as procR2

procR1 --> ===R2===
procR2 --> ===R2===

' === SUBPROGRAMA: Procesar ===
===R2=== --> [[procesarImagen()]] as subR1

subR1 --> [Redimensionar\nmax 800px] as procR3

procR3 --> [Comprimir\nJPEG 70%] as procR4

procR4 --> [Convertir\na Base64] as procR5

procR5 --> [fotoBase64 =\nresultado] as procR6

' === SALIDA ===
procR6 --> [/Mostrar:\npreview en ImageView/] as salidaR

salidaR --> [/Mostrar:\n"Foto guardada"/] as salidaR2

salidaR2 --> "**FIN**" as finOK

@enduml
```

---

## 5. Diagrama de Flujo: Guardar Animal en BD

```plantuml
@startuml DF_GuardarAnimalBD
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #E8F5E9
    BorderColor #2E7D32
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: GUARDAR ANIMAL EN BD**\nAnimalDAO.insertarAnimal() / actualizarAnimal()

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir: Animal animal,\nboolean modoEdicion/] as entrada

' === DECISIÓN: Modo ===
entrada --> <¿modoEdicion?> as dec1

' =========== RAMA ACTUALIZAR ===========
dec1 --> [Sí] ===U1===

===U1=== --> [db = getWritableDatabase()] as procU1

procU1 --> [ContentValues values\n= new ContentValues()] as procU2

procU2 --> [Poblar values con\natributos del animal] as procU3

procU3 --> [rows = db.update(\n"animales", values,\n"id=?", {animal.id})] as procU4

procU4 --> <¿rows > 0?> as decU1

decU1 --> [No] ===EU===
===EU=== --> [/Retornar: -1\n(error)/] as retU1
retU1 --> "**FIN**" as finU

decU1 --> [Sí] ===SU===
===SU=== --> [/Retornar: rows\n(éxito)/] as retU2
retU2 --> "**FIN**" as finU2

' =========== RAMA INSERTAR ===========
dec1 --> [No] ===I1===

' === SUBPROGRAMA: Verificar duplicado ===
===I1=== --> [[existeArete()]] as subI1

subI1 --> <¿arete existe?> as decI1

decI1 --> [Sí] ===EI===
===EI=== --> [/Retornar: -1\n(duplicado)/] as retI1
retI1 --> "**FIN**" as finI1

decI1 --> [No] ===I2===

===I2=== --> [db = getWritableDatabase()] as procI1

procI1 --> [ContentValues values\n= new ContentValues()] as procI2

procI2 --> [values.put("numero_arete",\nanimal.getNumeroArete())] as procI3

procI3 --> [values.put("raza",\nanimal.getRaza())] as procI4

procI4 --> [values.put("sexo",\nanimal.getSexo())] as procI5

procI5 --> [... poblar resto\nde campos ...] as procI6

procI6 --> [id = db.insert(\n"animales", null, values)] as procI7

procI7 --> <¿id > 0?> as decI2

decI2 --> [No] ===EI2===
===EI2=== --> [/Retornar: -1\n(error)/] as retI2
retI2 --> "**FIN**" as finI2

decI2 --> [Sí] ===SI===
===SI=== --> [/Retornar: id\n(nuevo ID)/] as retI3
retI3 --> "**FIN**" as finI3

@enduml
```

---

## 6. Diagrama de Flujo: Operaciones CRUD DAO

```plantuml
@startuml DF_CRUD_DAO
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #E3F2FD
    BorderColor #1565C0
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: OPERACIONES CRUD**\nAnimalDAO

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir:\noperación CRUD/] as entrada

' === DECISIÓN: Tipo operación ===
entrada --> <¿Tipo de\noperación?> as dec1

' =========== CREATE ===========
dec1 --> [CREATE] ===C1===
===C1=== --> [/Entrada:\nAnimal animal/] as entC
entC --> [[existeArete()]] as subC1
subC1 --> <¿duplicado?> as decC1
decC1 --> [Sí] [/Retornar: -1/] as retC1
retC1 --> "**FIN**" as finC1
decC1 --> [No] [db.insert()] as procC
procC --> [/Retornar: newId/] as retC2
retC2 --> "**FIN**" as finC2

' =========== READ ===========
dec1 --> [READ] ===R1===
===R1=== --> <¿Por ID o\nTodos?> as decR1

decR1 --> [Por ID] [/Entrada: int id/] as entR1
entR1 --> [db.query(\nWHERE id = ?)] as procR1
procR1 --> <¿cursor\nmoveToFirst()?> as decR2
decR2 --> [No] [/Retornar: null/] as retR1
retR1 --> "**FIN**" as finR1
decR2 --> [Sí] [[cursorToAnimal()]] as subR1
subR1 --> [/Retornar: Animal/] as retR2
retR2 --> "**FIN**" as finR2

decR1 --> [Todos] [db.query(\nORDER BY nombre)] as procR2
procR2 --> [List<Animal> lista\n= new ArrayList()] as procR3
procR3 --> <¿cursor.moveToNext()?> as decR3
decR3 --> [Sí] [[cursorToAnimal()]] as subR2
subR2 --> [lista.add(animal)] as procR4
procR4 --> decR3
decR3 --> [No] [cursor.close()] as procR5
procR5 --> [/Retornar: lista/] as retR3
retR3 --> "**FIN**" as finR3

' =========== UPDATE ===========
dec1 --> [UPDATE] ===U1===
===U1=== --> [/Entrada:\nAnimal animal/] as entU
entU --> [ContentValues\ncon nuevos valores] as procU1
procU1 --> [rows = db.update(\nWHERE id = animal.id)] as procU2
procU2 --> [/Retornar: rows/] as retU
retU --> "**FIN**" as finU

' =========== DELETE ===========
dec1 --> [DELETE] ===D1===
===D1=== --> [/Entrada: int id/] as entD
entD --> [rows = db.delete(\nWHERE id = ?)] as procD
procD --> [/Retornar: rows/] as retD

note right of procD
  ON DELETE CASCADE
  elimina automáticamente:
  - calendario_sanitario
  - historial_clinico  
  - gastos
  - alimentacion
end note

retD --> "**FIN**" as finD

@enduml
```

---

## 7. Diagrama de Flujo: Registrar Venta

```plantuml
@startuml DF_RegistrarVenta
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #FFF8E1
    BorderColor #F57F17
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: REGISTRAR VENTA**\nmostrarDialogoRegistrarVenta()

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir: animalId/] as entrada

' === PROCESO: Crear diálogo ===
entrada --> [AlertDialog.Builder\n"Registrar Venta"] as proc1

proc1 --> [Agregar EditText\npara precio] as proc2

proc2 --> [Agregar EditText\npara fecha] as proc3

proc3 --> [/Mostrar diálogo/] as salida1

' === DECISIÓN: Acción usuario ===
salida1 --> <¿Acción\nusuario?> as dec1

dec1 --> [Cancelar] "**FIN**" as fin1

dec1 --> [Confirmar] ===V1===

' === ENTRADA: Datos venta ===
===V1=== --> [/Leer: precioVenta,\nfechaVenta/] as entrada2

' === DECISIÓN: Precio válido ===
entrada2 --> <¿precioVenta\n<= 0?> as dec2

dec2 --> [Sí] [/Mostrar:\n"Precio inválido"/] as err1
err1 --> salida1

dec2 --> [No] ===V2===

' === PROCESO: Actualizar animal ===
===V2=== --> [animal.setEstado("VENDIDO")] as proc4

proc4 --> [animal.setPrecioVenta(precio)] as proc5

proc5 --> [animal.setFechaSalida(fecha)] as proc6

' === SUBPROGRAMA: Actualizar BD ===
proc6 --> [[animalDAO.actualizar()]] as sub1

' === DECISIÓN: Éxito ===
sub1 --> <¿actualización\nexitosa?> as dec3

dec3 --> [No] [/Mostrar:\n"Error al registrar"/] as err2
err2 --> "**FIN**" as fin2

dec3 --> [Sí] ===V3===

' === PROCESO: Calcular ganancia ===
===V3=== --> [ganancia = precioVenta\n- precioCompra - gastos] as proc7

' === SALIDA ===
proc7 --> [/Mostrar:\n"Venta registrada\nGanancia: $X"/] as salida2

salida2 --> [Actualizar UI\n(estado = VENDIDO)] as proc8

proc8 --> [Deshabilitar\nbotones edición] as proc9

proc9 --> "**FIN**" as fin3

@enduml
```

---

## 8. Diagrama de Flujo: Programar Evento Sanitario

```plantuml
@startuml DF_ProgramarEvento
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #E8F5E9
    BorderColor #388E3C
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: PROGRAMAR EVENTO SANITARIO**\ncrearEventoPorRaza()

' === INICIO ===
(*) --> "**INICIO**" as inicio

inicio --> [/Entrada: tipoEvento,\nraza, fecha, hora/] as entrada

' === PROCESO: Obtener animales ===
entrada --> [[animalDAO.obtenerPorRaza()]] as sub1

sub1 --> [List<Animal> animales] as proc1

' === DECISIÓN: Lista vacía ===
proc1 --> <¿animales\n.isEmpty()?> as dec1

dec1 --> [Sí] [/Mostrar:\n"No hay animales\nde esta raza"/] as err1
err1 --> "**FIN**" as fin1

dec1 --> [No] ===E1===

' === PROCESO: Iterar animales ===
===E1=== --> [int i = 0] as proc2

proc2 --> <¿i < animales\n.size()?> as dec2

dec2 --> [No] ===FIN===

dec2 --> [Sí] [animal = animales.get(i)] as proc3

' === PROCESO: Crear evento ===
proc3 --> [EventoSanitario evento\n= new EventoSanitario()] as proc4

proc4 --> [evento.setAnimalId(\nanimal.getId())] as proc5

proc5 --> [evento.setTipoEvento(\ntipoEvento)] as proc6

proc6 --> [evento.setFechaProgramada(\nfecha)] as proc7

proc7 --> [evento.setHoraRecordatorio(\nhora)] as proc8

proc8 --> [evento.setEstado(\n"PENDIENTE")] as proc9

' === SUBPROGRAMA: Guardar ===
proc9 --> [[eventoDAO.insertar()]] as sub2

sub2 --> [eventoId = resultado] as proc10

' === DECISIÓN: Programar notif ===
proc10 --> <¿hora != null?> as dec3

dec3 --> [Sí] [[NotificationHelper\n.programar()]] as sub3
sub3 --> ===NEXT===

dec3 --> [No] ===NEXT===

===NEXT=== --> [i++] as proc11

proc11 --> dec2

' === SALIDA FINAL ===
===FIN=== --> [/Mostrar:\n"X eventos creados"/] as salida

salida --> [Actualizar\ncalendario] as proc12

proc12 --> "**FIN**" as fin2

@enduml
```

---

## 9. Diagrama de Flujo: Mostrar DatePicker

```plantuml
@startuml DF_DatePicker
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #FCE4EC
    BorderColor #C2185B
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: SELECTOR DE FECHA**\nmostrarDatePicker(boolean esFechaNacimiento)

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === ENTRADA ===
inicio --> [/Recibir:\nesFechaNacimiento/] as entrada

' === PROCESO: Crear formato ===
entrada --> [SimpleDateFormat sdf\n= "dd/MM/yyyy"] as proc1

' === PROCESO: Obtener fecha actual ===
proc1 --> [year = calendar.get(YEAR)\nmonth = calendar.get(MONTH)\nday = calendar.get(DAY)] as proc2

' === PROCESO: Crear diálogo ===
proc2 --> [DatePickerDialog dialog\n= new DatePickerDialog(\ncontext, listener,\nyear, month, day)] as proc3

' === SALIDA: Mostrar ===
proc3 --> [/dialog.show()/] as salida1

' === CALLBACK ===
salida1 --> [Usuario selecciona\nfecha en calendario] as user1

user1 --> [onDateSet(\nyear, month, day)] as callback

' === PROCESO: Actualizar calendar ===
callback --> [calendar.set(\nyear, month, day)] as proc4

' === PROCESO: Formatear ===
proc4 --> [String fecha =\nsdf.format(calendar.getTime())] as proc5

' === DECISIÓN: Tipo fecha ===
proc5 --> <¿esFecha\nNacimiento?> as dec1

dec1 --> [Sí] ===N1===
===N1=== --> [fechaNacimiento[0]\n= fecha] as procN1
procN1 --> [btnFechaNacimiento\n.setText(fecha)] as procN2
procN2 --> ===FIN===

dec1 --> [No] ===I1===
===I1=== --> [fechaIngreso[0]\n= fecha] as procI1
procI1 --> [btnFechaAdquisicion\n.setText(fecha)] as procI2
procI2 --> ===FIN===

===FIN=== --> "**FIN**" as fin

@enduml
```

---

## 10. Diagrama de Flujo: Ciclo de Vida Activity

```plantuml
@startuml DF_CicloVida
!theme plain
left to right direction
skinparam backgroundColor #FEFEFE

skinparam activity {
    BackgroundColor #E3F2FD
    BorderColor #1976D2
    DiamondBackgroundColor #FFF9C4
}

title **DIAGRAMA DE FLUJO: CICLO DE VIDA ACTIVITY**\nBaseActivity + RegistroAnimalActivity

' === INICIO ===
(*) --> "**INICIO**" as inicio

' === onCreate ===
inicio --> [onCreate(Bundle)] as proc1

proc1 --> [super.onCreate()] as proc2

proc2 --> [setContentView()\nlayout XML] as proc3

proc3 --> [inicializarVistas()] as proc4

proc4 --> [configurarSpinners()] as proc5

proc5 --> [configurarListeners()] as proc6

proc6 --> ===A===

' === onStart ===
===A=== --> [onStart()] as proc7

proc7 --> ===B===

' === onResume ===
===B=== --> [onResume()] as proc8

proc8 --> [[BaseActivity\n.verificarSesion()]] as sub1

' === DECISIÓN: Sesión expirada ===
sub1 --> <¿tiempo >\n10 segundos?> as dec1

dec1 --> [Sí] [/Mostrar diálogo\ncontraseña/] as proc9
proc9 --> <¿Contraseña\ncorrecta?> as dec2
dec2 --> [No] [Volver a\nLoginActivity] as proc10
proc10 --> "**FIN**" as fin1
dec2 --> [Sí] ===C===

dec1 --> [No] ===C===

' === RUNNING ===
===C=== --> [ACTIVITY\nRUNNING] as running

running --> <¿Evento?> as dec3

dec3 --> [Usuario\ninteractúa] running

dec3 --> [Otra Activity\nal frente] ===D===

' === onPause ===
===D=== --> [onPause()] as proc11

proc11 --> [[BaseActivity\n.guardarTiempoActividad()]] as sub2

sub2 --> [SharedPreferences\n.putLong(timestamp)] as proc12

proc12 --> ===E===

' === onStop ===
===E=== --> [onStop()] as proc13

proc13 --> <¿Vuelve?> as dec4

dec4 --> [Sí] [onRestart()] as proc14
proc14 --> ===B===

dec4 --> [No] ===F===

' === onDestroy ===
===F=== --> [onDestroy()] as proc15

proc15 --> [presenter.destruir()\nExecutorService.shutdown()] as proc16

proc16 --> [Liberar\nrecursos] as proc17

proc17 --> "**FIN**" as fin2

@enduml
```

---

## Leyenda de Símbolos PlantUML

| PlantUML | Símbolo Clásico | Uso |
|----------|-----------------|-----|
| `(*)` | Óvalo | Inicio del diagrama |
| `"**FIN**"` | Óvalo | Fin del diagrama |
| `[Proceso]` | Rectángulo | Acción o proceso |
| `<¿Condición?>` | Rombo | Decisión |
| `[/Entrada/]` | Paralelogramo | Entrada de datos |
| `[/Salida/]` | Paralelogramo | Salida de datos |
| `[[Subprograma()]]` | Rectángulo doble | Llamada a función |
| `===CONECTOR===` | Círculo | Conector entre partes |
| `note` | Anotación | Documentación |

---

> **Nota**: Todos los diagramas usan `left to right direction` para orientación horizontal y conectores (`===X===`) para unir flujos y evitar diagramas demasiado verticales.
