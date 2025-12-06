# Diagramas de Flujo - Algoritmos del Sistema AgroApp
## Notación UML en PlantUML

---

## Índice de Diagramas

| No. | Algoritmo | Actividad Principal |
|-----|-----------|---------------------|
| 1 | Registrar Animal | RegistroAnimalActivity.guardarAnimal() |
| 2 | Actualizar Animal | RegistroAnimalActivity.guardarAnimal() (modo editar) |
| 3 | Eliminar Animal | DetalleAnimalActivity.confirmarEliminacion() |
| 4 | Consultar Animal | DetalleAnimalActivity.cargarDatos() |
| 5 | Registrar Venta | DetalleAnimalActivity.mostrarDialogoRegistrarVenta() |
| 6 | Programar Evento Sanitario | CalendarioActivity.crearEventoPorRaza() |
| 7 | Flujo de Notificaciones | NotificationHelper.programarNotificacion() |
| 8 | Marcar Evento como Realizado | EventosSanitariosActivity.mostrarOpcionesEvento() |
| 9 | Consultar Gastos con Filtro | GastosActivity.cargarGastos() |
| 10 | Registro de Compra Dividida | RegistroComprasActivity.guardarCompra() |
| 11 | Registrar Alimento | AlimentacionActivity.guardarAlimentacion() |
| 12 | Visualizar Estadística del Hato | MainActivity.cargarEstadisticas() |

---

## 1. Algoritmo: Registrar Animal

```plantuml
@startuml DiagramaFlujo_RegistrarAnimal
!theme plain
skinparam activityBackgroundColor #E8F5E9
skinparam activityBorderColor #2E7D32
skinparam activityDiamondBackgroundColor #FFF9C4
skinparam activityDiamondBorderColor #F57F17

title Diagrama de Flujo: Registrar Animal\n(RegistroAnimalActivity.guardarAnimal())

|Usuario|
start
:Completa formulario de registro;
:Presiona botón "Guardar";

|RegistroAnimalActivity|
:guardarAnimal();
:Obtener datos del formulario;
note right
  arete = etArete.getText()
  raza = spinnerRaza.getSelectedItem()
  sexo = spinnerSexo.getSelectedItem()
  fechaNac = fechaNacimiento[0]
  fechaIng = fechaIngreso[0]
  precioCompra = etPrecioCompra.getText()
end note

' Validación 1: Arete vacío
if (¿arete.isEmpty()?) then (Sí)
  :mostrarError("El número de arete es obligatorio");
  :etArete.setError("Campo obligatorio");
  :etArete.requestFocus();
  stop
endif

' Validación 2: Formato SINIGA
if (¿arete.matches("\\d{10}")?) then (No)
  :mostrarError("Formato SINIGA: 10 dígitos");
  :etArete.setError("Formato inválido");
  stop
endif

' Validación 3: Arete único
|AnimalPresenter|
:validarArete(arete);
:animalDAO.obtenerAnimalPorArete(arete);

if (¿existe animal con mismo arete?) then (Sí)
  :mostrarError("Ya existe un animal\ncon este número de arete");
  |RegistroAnimalActivity|
  stop
endif

|RegistroAnimalActivity|
' Validación 4: Precio vacío
if (¿precioStr.isEmpty()?) then (Sí)
  :mostrarError("El precio de compra es obligatorio");
  stop
endif

' Validación 5: Precio válido
if (¿precioCompra <= 0?) then (Sí)
  :mostrarError("El precio debe ser mayor a cero");
  stop
endif

' Validación 6: Fechas coherentes
|AnimalPresenter|
:validarFechasCoherentes(fechaNac, fechaIng);
if (¿fechaNacimiento > fechaIngreso?) then (Sí)
  :mostrarError("Fecha de nacimiento\nno puede ser posterior\na fecha de ingreso");
  |RegistroAnimalActivity|
  stop
endif

|RegistroAnimalActivity|
:Crear objeto Animal;
note right
  animal.setNumeroArete(arete)
  animal.setRaza(raza)
  animal.setSexo(sexo)
  animal.setFechaNacimiento(fechaNac)
  animal.setFechaIngreso(fechaIng)
  animal.setPrecioCompra(precioCompra)
  animal.setEstado(estado)
  animal.setFoto(fotoBase64)
end note

|AnimalPresenter|
:guardarAnimal(animal, false);

|ExecutorService|
:ejecutar en hilo secundario;
:animalDAO.insertarAnimal(animal);

|AnimalDAO|
:db.insert("animales", valores);
if (¿inserción exitosa?) then (No)
  |RegistroAnimalActivity|
  :mostrarError("Error al guardar");
  stop
endif

|Handler - MainThread|
:ejecutarEnUIThread();
|RegistroAnimalActivity|
:mostrarExito("Animal registrado exitosamente");
:cerrarActividad();
:finish();

|Usuario|
:Regresa a pantalla anterior;
stop

@enduml
```

---

## 2. Algoritmo: Actualizar Animal

```plantuml
@startuml DiagramaFlujo_ActualizarAnimal
!theme plain
skinparam activityBackgroundColor #E3F2FD
skinparam activityBorderColor #1565C0
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Actualizar Animal\n(RegistroAnimalActivity - modo "editar")

|Usuario|
start
:Abre DetalleAnimalActivity;
:Presiona botón "Editar";

|DetalleAnimalActivity|
:Intent intent = new Intent(RegistroAnimalActivity);
:intent.putExtra("modo", "editar");
:intent.putExtra("animalId", animalId);
:startActivity(intent);

|RegistroAnimalActivity|
:onCreate();
:modo = getIntent().getStringExtra("modo");
:animalId = getIntent().getIntExtra("animalId", -1);

if (¿modo.equals("editar") && animalId != -1?) then (Sí)
  :setTitle("Editar Animal");
  :etArete.setEnabled(false);
  note right: Bloquear edición del arete
  :etArete.setAlpha(0.5f);
  :cargarDatosAnimal();
endif

|AnimalPresenter|
:cargarAnimal(animalId, callback);

|ExecutorService|
:animalDAO.obtenerAnimalPorId(animalId);

|Handler - MainThread|
:callback.onAnimalCargado(animal);

|RegistroAnimalActivity|
:Poblar formulario con datos;
note right
  etArete.setText(animal.getNumeroArete())
  btnFechaNacimiento.setText(animal.getFechaNacimiento())
  btnFechaAdquisicion.setText(animal.getFechaIngreso())
  etPrecioCompra.setText(animal.getPrecioCompra())
  setSpinnerValue(spinnerRaza, animal.getRaza())
  setSpinnerValue(spinnerSexo, animal.getSexo())
  setSpinnerValue(spinnerEstado, animal.getEstado())
  Cargar foto si existe
end note

if (¿animal.estado == "Vendido" || "Muerto"?) then (Sí)
  :spinnerEstado.setEnabled(false);
  :spinnerEstado.setAlpha(0.5f);
  note right: Bloquear cambio de estado
endif

|Usuario|
:Modifica campos permitidos;
:Presiona "Guardar";

|RegistroAnimalActivity|
:guardarAnimal();
:Ejecutar validaciones;
note right
  - Validar precio > 0
  - Validar fechas coherentes
  - NO valida arete único (está bloqueado)
end note

if (¿Validaciones exitosas?) then (No)
  :mostrarError(mensaje);
  stop
endif

:animal.setId(animalId);
note right: Importante para UPDATE

|AnimalPresenter|
:guardarAnimal(animal, true);
note right: isEditing = true

|ExecutorService|
:animalDAO.actualizarAnimal(animal);

|AnimalDAO|
:db.update("animales", valores, "id = ?", id);

|Handler - MainThread|
:ejecutarEnUIThread();
|RegistroAnimalActivity|
:mostrarExito("Animal actualizado exitosamente");
:finish();

|Usuario|
:Regresa a DetalleAnimalActivity;
:cargarDatos() - actualiza vista;
stop

@enduml
```

---

## 3. Algoritmo: Eliminar Animal

```plantuml
@startuml DiagramaFlujo_EliminarAnimal
!theme plain
skinparam activityBackgroundColor #FFEBEE
skinparam activityBorderColor #C62828
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Eliminar Animal\n(DetalleAnimalActivity.confirmarEliminacion())

|Usuario|
start
:Visualiza DetalleAnimalActivity;
:Presiona botón "Eliminar";

|DetalleAnimalActivity|
:confirmarEliminacion();
:animal = animalDAO.obtenerAnimalPorId(animalId);
:nombreAnimal = animal.getNombre();

:Crear AlertDialog.Builder;
:setTitle("⚠️ Confirmar Eliminación");
note right
  Mensaje de advertencia:
  "¿Está seguro de eliminar a {nombre}?
  
  ⚠️ ADVERTENCIA: Esta acción eliminará:
  • Todos los eventos sanitarios
  • Todo el historial clínico
  • Todos los registros de alimentación
  • Todos los gastos asociados
  
  Esta acción NO se puede deshacer."
end note

:show();

|Usuario|
if (¿Usuario confirma "Sí, eliminar"?) then (Cancelar)
  :dialog.dismiss();
  stop
endif

|DetalleAnimalActivity|
:Ejecutar eliminación;
:animalDAO.eliminarAnimal(animalId);

|AnimalDAO|
:db.delete("animales", "id = ?", animalId);
note right
  ON DELETE CASCADE elimina automáticamente:
  - eventos_sanitarios
  - gastos
  - alimentacion
  - historial_clinico
end note

|DetalleAnimalActivity|
:Toast.makeText("Animal eliminado exitosamente");
:finish();

|Usuario|
:Regresa a GestionAnimalesActivity;
:Lista de animales se actualiza;
stop

@enduml
```

---

## 4. Algoritmo: Consultar Animal

```plantuml
@startuml DiagramaFlujo_ConsultarAnimal
!theme plain
skinparam activityBackgroundColor #E8EAF6
skinparam activityBorderColor #3F51B5
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Consultar Animal\n(DetalleAnimalActivity.cargarDatos())

|Usuario|
start
:Selecciona animal en GestionAnimalesActivity;
:Tap en item de RecyclerView;

|GestionAnimalesActivity|
:Intent intent = new Intent(DetalleAnimalActivity);
:intent.putExtra("animalId", animal.getId());
:startActivity(intent);

|DetalleAnimalActivity|
:onCreate();
:animalId = getIntent().getIntExtra("animalId", -1);
:dbHelper = DatabaseHelper.getInstance(this);
:animalDAO = new AnimalDAO(dbHelper);
:gastoDAO = new GastoDAO(dbHelper);

:long startTime = System.currentTimeMillis();
note right: Medición RNF001 (< 2 segundos)

:inicializarVistas();
:cargarDatos();

:animal = animalDAO.obtenerAnimalPorId(animalId);

if (¿animal != null?) then (No)
  :mostrarError("Animal no encontrado");
  stop
endif

' Cargar datos del header
:tvNombre.setText(animal.getNombre());
:tvArete.setText("Arete: " + animal.getNumeroArete());

' Cargar foto
if (¿animal.getFoto() != null && !isEmpty?) then (Sí)
  :byte[] decoded = Base64.decode(animal.getFoto());
  :Bitmap bitmap = BitmapFactory.decodeByteArray(decoded);
  :ivFotoAnimal.setImageBitmap(bitmap);
endif

' Configurar badge estado
:badgeEstado.setText(estado);
:configurarBadgeEstado(estado);
note right
  switch(estado):
    "sano" → Verde #16a34a
    "enfermo" → Rojo #dc2626
    "vendido" → Morado #7e22ce
    "muerto" → Gris #6b7280
end note

' Cargar información grid
:tvRaza.setText(animal.getRaza());
:tvSexo.setText(animal.getSexo());
:tvEdad.setText(calcularEdad(animal.getFechaNacimiento()));
:tvFechaNacimiento.setText(animal.getFechaNacimiento());
:tvFechaIngreso.setText(animal.getFechaIngreso());

' Calcular inversión total (RD004)
:double precioCompra = animal.getPrecioCompra();
:double totalGastos = gastoDAO.obtenerTotalGastosPorAnimal(animalId);
:double inversionTotal = precioCompra + totalGastos;

:tvInversionTotal.setText(currencyFormatter.format(inversionTotal));
:tvPrecioCompra.setText("Compra: " + formatear(precioCompra));
:tvTotalGastos.setText("Gastos: " + formatear(totalGastos));

' Mostrar ganancia si vendido
if (¿animal.getFechaSalida() != null && precioVenta > 0?) then (Sí)
  :double ganancia = precioVenta - inversionTotal;
  :layoutGanancia.setVisibility(VISIBLE);
  :tvGanancia.setText(formatear(ganancia));
  if (¿ganancia >= 0?) then (Sí)
    :tvGanancia.setTextColor(Verde);
  else (No)
    :tvGanancia.setTextColor(Rojo);
  endif
  :btnRegistrarVenta.setVisibility(GONE);
else (No)
  :layoutGanancia.setVisibility(GONE);
  :btnRegistrarVenta.setVisibility(VISIBLE);
endif

:configurarListeners();

:long loadTime = System.currentTimeMillis() - startTime;
if (¿loadTime > 2000?) then (Sí)
  :Log.w("Tiempo de carga alto: " + loadTime + "ms");
  note right: Violación RNF001
endif

|Usuario|
:Visualiza información completa del animal;
stop

@enduml
```

---

## 5. Algoritmo: Registrar Venta

```plantuml
@startuml DiagramaFlujo_RegistrarVenta
!theme plain
skinparam activityBackgroundColor #F3E5F5
skinparam activityBorderColor #7B1FA2
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Registrar Venta\n(DetalleAnimalActivity.mostrarDialogoRegistrarVenta())

|Usuario|
start
:Visualiza DetalleAnimalActivity;
:Presiona botón "Registrar Venta";

|DetalleAnimalActivity|
:mostrarDialogoRegistrarVenta();
:AlertDialog.Builder builder = new AlertDialog.Builder();
:View dialogView = inflate(R.layout.dialog_registrar_venta);

:EditText etPrecioVenta = findViewById(etPrecioVenta);
:Button btnFechaVenta = findViewById(btnFechaVenta);

:String[] fechaVenta = {formato.format(new Date())};
:btnFechaVenta.setText(fechaVenta[0]);
note right: Fecha actual por defecto

' Configurar DatePicker
:btnFechaVenta.setOnClickListener -> DatePickerDialog;

:builder.setView(dialogView);
:builder.setTitle("Registrar Venta");
:dialog.show();

|Usuario|
:Ingresa precio de venta;
:Selecciona fecha de venta (opcional);
:Presiona "Guardar";

|DetalleAnimalActivity|
:String precioStr = etPrecioVenta.getText();

if (¿precioStr.isEmpty()?) then (Sí)
  :Toast("Ingrese el precio de venta");
  stop
endif

:double precioVenta = Double.parseDouble(precioStr);

|ExecutorService|
:executorService.execute(() -> {});

:Animal animal = animalDAO.obtenerAnimalPorId(animalId);

if (¿animal != null?) then (Sí)
  :animal.setEstado("Vendido");
  :animal.setFechaSalida(fechaVenta[0]);
  :animal.setPrecioVenta(precioVenta);
  :animalDAO.actualizarAnimal(animal);
  
  :double ganancia = precioVenta - animal.getPrecioCompra();
  
  |Handler - MainThread|
  :mainHandler.post(() -> {});
  
  |DetalleAnimalActivity|
  :Construir mensaje de resumen;
  note right
    "Animal vendido
     Precio compra: ${precioCompra}
     Precio venta: ${precioVenta}
     Ganancia/Pérdida: ${|ganancia|}"
  end note
  
  :Mostrar AlertDialog con resumen;
  :dialog.setPositiveButton("OK", () -> finish());
  :dialog.show();
  
  |Usuario|
  :Presiona "OK";
  
  |DetalleAnimalActivity|
  :finish();
  
  |Usuario|
  :Regresa a GestionAnimalesActivity;
  :Animal aparece como "Vendido";
  
else (No)
  |DetalleAnimalActivity|
  :Toast("Error: Animal no encontrado");
endif

stop

@enduml
```

---

## 6. Algoritmo: Programar Evento Sanitario

```plantuml
@startuml DiagramaFlujo_ProgramarEventoSanitario
!theme plain
skinparam activityBackgroundColor #E0F7FA
skinparam activityBorderColor #00838F
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Programar Evento Sanitario\n(CalendarioActivity.crearEventoPorRaza())

|Usuario|
start
:Accede a CalendarioActivity;
:Presiona "Nuevo Evento";

|CalendarioActivity|
:mostrarDialogoNuevoEvento();
:View dialogView = inflate(dialog_evento_sanitario);

:Spinner spinnerRaza = findViewById(spinnerRaza);
:Spinner spinnerTipo = findViewById(spinnerTipo);
:Button btnFechaEvento = findViewById(btnFechaEvento);
:Button btnHoraEvento = findViewById(btnHoraEvento);
:EditText etDescripcion = findViewById(etDescripcion);
:EditText etCosto = findViewById(etCosto);

' Cargar razas únicas
:List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
:Set<String> razasSet = new HashSet<>();
:foreach animal -> razasSet.add(animal.getRaza());
:spinnerRaza.setAdapter(razasSet);

' Configurar tipos
:String[] tipos = {"Vacuna", "Desparasitación", "Vitaminas", "Otro"};
:spinnerTipo.setAdapter(tipos);

' Configurar fecha y hora por defecto
:fechaSeleccionada = formato.format(Calendar.getInstance());
:horaSeleccionada = "09:00";

:dialog.show();

|Usuario|
:Selecciona raza;
:Selecciona tipo de evento;
:Selecciona fecha programada;
:Selecciona hora del recordatorio;
:Ingresa descripción;
:Ingresa costo (opcional);
:Presiona "Guardar";

|CalendarioActivity|
' Validar costo
:String costoStr = etCosto.getText();
:double costo = 0;

if (¿costoStr no está vacío?) then (Sí)
  :costo = Double.parseDouble(costoStr);
  if (¿costo < 0?) then (Sí)
    :Toast("El costo no puede ser negativo");
    stop
  endif
endif

if (¿razas.length > 0?) then (Sí)
  :String razaSeleccionada = spinnerRaza.getSelectedItem();
  :crearEventoPorRaza(razaSeleccionada, tipo, fecha, hora, descripcion, costo, sdf);
endif

|crearEventoPorRaza()|
:EventoSanitario evento = new EventoSanitario();
:evento.setRaza(raza);
:evento.setTipo(tipo);
:evento.setFechaProgramada(fecha);
:evento.setHoraRecordatorio(hora);
:evento.setDescripcion(descripcion);
:evento.setCosto(costo);
:evento.setEstado("Pendiente");
:evento.setRecordatorio(1);
note right: Recordatorio activado

:long eventoId = eventoDAO.insertarEvento(evento);
:evento.setId((int) eventoId);

|EventoSanitarioDAO|
:db.insert("eventos_sanitarios", valores);
:return lastInsertRowId;

|CalendarioActivity|
' Programar notificación
:evento.setFechaEvento(sdf.parse(fechaProgramada));
:NotificationHelper.programarNotificacion(this, evento);

|NotificationHelper|
:Ver Diagrama 7 - Flujo de Notificaciones;

|CalendarioActivity|
:Toast("Evento creado para raza " + razaSeleccionada);
:recreate();
note right: Recargar vista con nuevo evento

|Usuario|
:Evento aparece en calendario;
:Recibirá notificaciones según RF009;
stop

@enduml
```

---

## 7. Algoritmo: Flujo de Notificaciones

```plantuml
@startuml DiagramaFlujo_Notificaciones
!theme plain
skinparam activityBackgroundColor #FFFDE7
skinparam activityBorderColor #F9A825
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Flujo de Notificaciones (RF009)\n(NotificationHelper.programarNotificacion())

|CalendarioActivity|
start
:Crear evento sanitario;
:NotificationHelper.programarNotificacion(context, evento);

|NotificationHelper|
:programarNotificacion(context, evento);

if (¿evento.getRecordatorio() != 1?) then (Sí)
  :return;
  note right: Recordatorio desactivado
  stop
endif

:SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
:Calendar calendar = Calendar.getInstance();
:calendar.setTime(sdf.parse(evento.getFechaProgramada()));

fork
  :Notificación 3 días antes;
  :programarNotificacionIndividual(context, evento, calendar, -3, "🔔 Recordatorio: ");
fork again
  :Notificación 1 día antes;
  :programarNotificacionIndividual(context, evento, calendar, -1, "⚠️ Recordatorio urgente: ");
fork again
  :Notificación mismo día;
  :programarNotificacionIndividual(context, evento, calendar, 0, "🚨 ¡HOY! ");
end fork

|programarNotificacionIndividual()|
:AlarmManager alarmManager = getSystemService(ALARM_SERVICE);

:Intent intent = new Intent(context, NotificationReceiver.class);
:intent.putExtra("titulo", prefijo + evento.getTipo());
:intent.putExtra("mensaje", evento.getDescripcion());
:intent.putExtra("eventoId", evento.getId());

' Calcular request code único
:int requestCode = evento.getId() * 100 + (diasOffset + 10);
note right
  Ejemplos:
  EventoId=5, -3 días → 5*100 + 7 = 507
  EventoId=5, -1 día  → 5*100 + 9 = 509
  EventoId=5, mismo día → 5*100 + 10 = 510
end note

:PendingIntent pendingIntent = PendingIntent.getBroadcast();

:Calendar calNotif = (Calendar) fechaEvento.clone();
:calNotif.add(Calendar.DAY_OF_MONTH, diasOffset);
:calNotif.set(Calendar.HOUR_OF_DAY, 9);
:calNotif.set(Calendar.MINUTE, 0);
note right: Todas las notificaciones a las 9:00 AM

:long triggerTime = calNotif.getTimeInMillis();
:long currentTime = System.currentTimeMillis();

if (¿triggerTime > currentTime?) then (Sí)
  if (¿Build.VERSION >= Android M?) then (Sí)
    :alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerTime, pendingIntent);
    note right: Mayor precisión en Doze mode
  else (No)
    :alarmManager.setExact(RTC_WAKEUP, triggerTime, pendingIntent);
  endif
else (No)
  :No programar (fecha pasada);
endif

|Sistema Android - AlarmManager|
:Almacena alarma programada;

== Cuando llega la hora programada ==

|NotificationReceiver|
:onReceive(context, intent);
:String titulo = intent.getStringExtra("titulo");
:String mensaje = intent.getStringExtra("mensaje");

:NotificationManager nm = getSystemService();
:NotificationCompat.Builder builder = new Builder();
:builder.setSmallIcon(R.drawable.ic_notification);
:builder.setContentTitle(titulo);
:builder.setContentText(mensaje);
:builder.setPriority(PRIORITY_HIGH);

:nm.notify(eventoId, builder.build());

|Usuario|
:Recibe notificación en dispositivo;
:Tap en notificación → Abre CalendarioActivity;
stop

@enduml
```

---

## 8. Algoritmo: Marcar Evento como Realizado

```plantuml
@startuml DiagramaFlujo_MarcarEventoRealizado
!theme plain
skinparam activityBackgroundColor #E8F5E9
skinparam activityBorderColor #388E3C
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Marcar Evento como Realizado\n(EventosSanitariosActivity.mostrarOpcionesEvento())

|Usuario|
start
:Accede a EventosSanitariosActivity;
:Tap en evento del RecyclerView;

|EventosSanitariosActivity|
:adapter.onItemClick(evento);
:mostrarOpcionesEvento(evento);

:String[] opciones = {"Editar", "Marcar como Realizado", "Eliminar"};
:AlertDialog.Builder builder = new AlertDialog.Builder();
:builder.setTitle("Opciones");
:builder.setItems(opciones, listener);
:dialog.show();

|Usuario|
:Selecciona "Marcar como Realizado";

|EventosSanitariosActivity|
:case 1: // Marcar como Realizado;

|ExecutorService|
:executorService.execute(() -> {});

:evento.setEstado("Realizado");
:String fechaHoy = sdf.format(Calendar.getInstance().getTime());
:evento.setFechaRealizada(fechaHoy);
note right
  Guarda la fecha en que se
  realizó efectivamente el evento
end note

:eventoDAO.actualizarEvento(evento);

|EventoSanitarioDAO|
:ContentValues valores = new ContentValues();
:valores.put("estado", "Realizado");
:valores.put("fecha_realizada", fechaRealizada);
:db.update("eventos_sanitarios", valores, "id = ?", evento.getId());

|EventosSanitariosActivity|
' Cancelar notificaciones pendientes (solo en CalendarioActivity)
note right
  En EventosSanitariosActivity no se
  cancelan las notificaciones programadas
  (se hace en CalendarioActivity)
end note

|Handler - MainThread|
:mainHandler.post(() -> {});

|EventosSanitariosActivity|
:Toast("Evento actualizado");
:cargarEventos();

|cargarEventos()|
:eventosList = eventoDAO.obtenerEventosPorAnimal(animalId);
:adapter = new EventoSanitarioAdapter(eventosList);
:recyclerView.setAdapter(adapter);

|Usuario|
:Evento aparece con estado "Realizado";
:Color del badge cambia a verde;
stop

@enduml
```

---

## 9. Algoritmo: Consultar Gastos con Filtro

```plantuml
@startuml DiagramaFlujo_ConsultarGastos
!theme plain
skinparam activityBackgroundColor #FFF3E0
skinparam activityBorderColor #E65100
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Consultar Gastos con Filtro\n(GastosActivity.cargarGastos())

|Usuario|
start

fork
  :Desde MainActivity;
  :cardGastos.onClick();
  :Intent intent = new Intent(GastosActivity);
  note right: Sin filtro de animal
fork again
  :Desde DetalleAnimalActivity;
  :cardGastos.onClick();
  :intent.putExtra("animalId", animalId);
  note right: Con filtro de animal específico
end fork

:startActivity(intent);

|GastosActivity|
:onCreate();
:gastoDAO = new GastoDAO(dbHelper);
:animalDAO = new AnimalDAO(dbHelper);

:animalIdFiltro = getIntent().getIntExtra("animalId", -1);
note right
  animalIdFiltro = -1 → Sin filtro (todos)
  animalIdFiltro > 0 → Filtrar por animal
end note

:recyclerView.setLayoutManager(new LinearLayoutManager());
:gastosList = new ArrayList<>();
:adapter = new GastoAdapter(gastosList, animalDAO, onItemClick);
:recyclerView.setAdapter(adapter);

:cargarGastos();

|cargarGastos()|
|ExecutorService|
:executorService.execute(() -> {});

:List<Gasto> nuevosGastos;

if (¿animalIdFiltro != -1?) then (Filtrado)
  :nuevosGastos = gastoDAO.obtenerGastosPorAnimal(animalIdFiltro);
  
  |GastoDAO|
  :String sql = "SELECT * FROM gastos WHERE animal_id = ?";
  :cursor = db.rawQuery(sql, animalId);
  :Construir lista de Gasto desde cursor;
  
else (Sin filtro)
  :nuevosGastos = gastoDAO.obtenerTodosLosGastos();
  
  |GastoDAO|
  :String sql = "SELECT * FROM gastos ORDER BY fecha DESC";
  :cursor = db.rawQuery(sql);
  :Construir lista de Gasto desde cursor;
endif

|Handler - MainThread|
:mainHandler.post(() -> {});

|GastosActivity|
:gastosList.clear();
:gastosList.addAll(nuevosGastos);
:adapter.notifyDataSetChanged();

|Usuario|
:Visualiza lista de gastos;

if (¿Usuario hace tap en gasto?) then (Sí)
  :mostrarOpcionesGasto(gasto);
  :Opciones: Editar / Eliminar;
endif

stop

@enduml
```

---

## 10. Algoritmo: Registro de Compra Dividida

```plantuml
@startuml DiagramaFlujo_CompraDividida
!theme plain
skinparam activityBackgroundColor #FCE4EC
skinparam activityBorderColor #C2185B
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Registro de Compra Dividida\n(RegistroComprasActivity.guardarCompra())

|Usuario|
start
:Accede a RegistroComprasActivity;
:Completa formulario de compra;
note right
  - Nombre de la compra
  - Precio total
  - Tipo de compra (Alimento, Medicamento, etc.)
  - Buscar y seleccionar animales
end note

|RegistroComprasActivity|
:onCreate();
:inicializarVistas();
:cargarAnimales();

|cargarAnimales()|
|ExecutorService|
:List<Animal> todosAnimales = animalDAO.obtenerTodos();
:Filtrar animales (excluir vendidos y muertos);

|Handler - MainThread|
:mostrarAnimales(animalesList);

|mostrarAnimales()|
:layoutAnimales.removeAllViews();
:checkBoxes.clear();

:foreach Animal animal in animales:;
:  CheckBox cb = new CheckBox();
:  cb.setText(arete + " - " + raza);
:  cb.setTag(animal.getId());
:  cb.setOnCheckedChangeListener(calcularPorAnimal);
:  layoutAnimales.addView(cb);
:  checkBoxes.add(cb);

|Usuario|
:Selecciona múltiples animales con CheckBox;
:El precio por animal se recalcula automáticamente;

|calcularPorAnimal()|
:double precioTotal = etPrecioTotal.getText();
:int animalesSeleccionados = 0;

:foreach CheckBox cb in checkBoxes:;
:  if (cb.isChecked()) animalesSeleccionados++;

if (¿animalesSeleccionados > 0?) then (Sí)
  :double precioPorAnimal = precioTotal / animalesSeleccionados;
  :tvTotalPorAnimal.setText("Por animal: $" + precioPorAnimal);
else (No)
  :tvTotalPorAnimal.setText("Por animal: $0.00");
endif

|Usuario|
:Presiona "Guardar";

|guardarCompra()|
:String nombreCompra = etNombreCompra.getText();
:String precioStr = etPrecioTotal.getText();

' Validación
if (¿nombreCompra.isEmpty() || precioStr.isEmpty()?) then (Sí)
  :Toast("Complete todos los campos");
  stop
endif

:int animalesSeleccionados = contar checkboxes marcados;

if (¿animalesSeleccionados == 0?) then (Sí)
  :Toast("Seleccione al menos un animal");
  stop
endif

:double precioTotal = Double.parseDouble(precioStr);
:double precioPorAnimal = precioTotal / animalesSeleccionados;
note right
  Fórmula de división:
  precioPorAnimal = precioTotal / cantidadAnimales
end note

:RadioButton rbSeleccionado = findViewById(rgTipoCompra.getCheckedId());
:String tipoCompra = rbSeleccionado.getText();
:String fechaActual = sdf.format(new Date());

|ExecutorService|
:executorService.execute(() -> {});

:foreach CheckBox cb in checkBoxes:;
:  if (cb.isChecked()) {;

:    int animalId = (int) cb.getTag();
    
:    Gasto gasto = new Gasto();
:    gasto.setAnimalId(animalId);
:    gasto.setTipo(tipoCompra);
:    gasto.setConcepto(nombreCompra);
:    gasto.setMonto(precioPorAnimal);
note right
  Cada animal recibe
  el monto dividido
end note
:    gasto.setFecha(fechaActual);
:    gasto.setObservaciones("Compra distribuida entre " + totalAnimales + " animales");
    
:    gastoDAO.insertarGasto(gasto);

|GastoDAO|
:db.insert("gastos", valores);

:  } // fin foreach;

|Handler - MainThread|
:mainHandler.post(() -> {});

|RegistroComprasActivity|
:Toast("Compra registrada exitosamente");
:finish();

|Usuario|
:Regresa a pantalla anterior;
:Cada animal tiene su gasto proporcional registrado;
stop

@enduml
```

---

## 11. Algoritmo: Registrar Alimento

```plantuml
@startuml DiagramaFlujo_RegistrarAlimento
!theme plain
skinparam activityBackgroundColor #F1F8E9
skinparam activityBorderColor #689F38
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Registrar Alimento\n(AlimentacionActivity.guardarAlimentacion())

|Usuario|
start
:Accede a AlimentacionActivity;
:Presiona "Nuevo Registro";

|AlimentacionActivity|
:mostrarDialogoNuevoRegistro();

|ExecutorService|
:List<Animal> animales = animalDAO.obtenerTodosLosAnimales();

|Handler - MainThread|
:mostrarDialogoConAnimales(animales);

|mostrarDialogoConAnimales()|
:View dialogView = inflate(dialog_alimentacion);

:Spinner spinnerAnimal;
:CheckBox cbSeleccionarPorRaza;
:LinearLayout layoutRazas;
:Spinner spinnerTipo;
:Spinner spinnerUnidad;
:EditText etCantidad, etCosto;
:Button btnFecha;
:EditText etObservaciones;

' Configurar spinner de animales
:String[] nombresAnimales = foreach animal -> arete + " - " + raza;
:spinnerAnimal.setAdapter(nombresAnimales);

' Obtener razas únicas para checkboxes
:Set<String> razasSet = new HashSet<>();
:foreach animal -> razasSet.add(animal.getRaza());

:foreach String raza in razasList:;
:  CheckBox cb = new CheckBox(raza);
:  layoutRazas.addView(cb);

' Toggle entre individual y por raza
:cbSeleccionarPorRaza.setOnCheckedChangeListener((btn, isChecked) -> {;
if (¿isChecked?) then (Por Raza)
  :spinnerAnimal.setVisibility(GONE);
  :layoutRazas.setVisibility(VISIBLE);
else (Individual)
  :spinnerAnimal.setVisibility(VISIBLE);
  :layoutRazas.setVisibility(GONE);
endif
:});

' Configurar spinners
:String[] tipos = {"Pasto", "Forraje", "Concentrado", "Grano", "Suplemento", "Otro"};
:String[] unidades = {"kg", "g", "toneladas", "pacas", "litros"};

:dialog.show();

|Usuario|
:Decide modo de registro;

if (¿Selecciona "Por Raza"?) then (Sí - Múltiples)
  :Marca checkboxes de razas;
else (No - Individual)
  :Selecciona un animal del spinner;
endif

:Completa tipo, cantidad, unidad, costo, fecha;
:Presiona "Guardar";

|AlimentacionActivity|
:double cantidad = Double.parseDouble(etCantidad);
:double costo = etCosto.isEmpty() ? 0 : Double.parseDouble(etCosto);

if (¿cbSeleccionarPorRaza.isChecked()?) then (Sí - Por Raza)
  :List<String> razasSeleccionadas = new ArrayList();
  :foreach CheckBox cb -> if checked: razasSeleccionadas.add(cb.getText());
  
  if (¿razasSeleccionadas.isEmpty()?) then (Sí)
    :Toast("Seleccione al menos una raza");
    stop
  endif
  
  :List<Animal> animalesFiltrados = animales.stream();
  :  .filter(a -> razasSeleccionadas.contains(a.getRaza()));
  :  .collect(toList());
  
  :int totalAnimales = animalesFiltrados.size();
  
  |ExecutorService|
  :foreach Animal animal in animalesFiltrados:;
  :  guardarAlimentacion(animal.getId(), tipo, cantidad, unidad, fecha, costo, observaciones);
  
  |AlimentacionDAO|
  :db.insert("alimentacion", valores);
  note right
    Se crea un registro por
    cada animal de las razas
    seleccionadas
  end note
  
  |Handler - MainThread|
  :Toast("Registros guardados para " + totalAnimales + " animales");
  
else (No - Individual)
  :int animalId = animales.get(spinnerAnimal.getSelectedItemPosition()).getId();
  
  |ExecutorService|
  :guardarAlimentacion(animalId, tipo, cantidad, unidad, fecha, costo, observaciones);
  
  |AlimentacionDAO|
  :db.insert("alimentacion", valores);
  
  |Handler - MainThread|
  :Toast("Registro guardado");
endif

|AlimentacionActivity|
:cargarRegistros();
:recyclerView.setAdapter(new AlimentacionAdapter(alimentacionList));

|Usuario|
:Visualiza nuevo(s) registro(s) en lista;
stop

@enduml
```

---

## 12. Algoritmo: Visualizar Estadística del Hato

```plantuml
@startuml DiagramaFlujo_EstadisticaHato
!theme plain
skinparam activityBackgroundColor #E1F5FE
skinparam activityBorderColor #0277BD
skinparam activityDiamondBackgroundColor #FFF9C4

title Diagrama de Flujo: Visualizar Estadística del Hato\n(MainActivity.cargarEstadisticas())

|Usuario|
start
:Abre la aplicación;
:Login exitoso;

|MainActivity|
:onCreate();
:executorService = Executors.newSingleThreadExecutor();
:mainHandler = new Handler(Looper.getMainLooper());
:animalDAO = new AnimalDAO(dbHelper);
:eventoDAO = new EventoSanitarioDAO(dbHelper);

:inicializarVistas();
note right
  tvAnimalesActivos
  tvAnimalesSanos
  tvAnimalesVendidos
  tvAnimalesMuertos
  tvVacunasPendientes
  cardVacunaAlert
  badgeCalendario
end note

:cargarEstadisticas();

|cargarEstadisticas()|
|ExecutorService|
:executorService.execute(() -> {});

:List<Animal> todosLosAnimales = animalDAO.obtenerTodos();

' Inicializar contadores
:int activos = 0;
:int sanos = 0;
:int vendidos = 0;
:int muertos = 0;

:foreach Animal animal in todosLosAnimales:;
:  String estado = animal.getEstado();

if (¿animal.getFechaSalida() != null && !isEmpty?) then (Vendido)
  :vendidos++;
else if (¿"Muerto".equalsIgnoreCase(estado)?) then (Muerto)
  :muertos++;
else (Activo)
  :activos++;
  if (¿"Sano".equalsIgnoreCase(estado)?) then (Sí)
    :sanos++;
  endif
endif

' Verificar vacunas pendientes (próximos 7 días)
:List<EventoSanitario> eventosPendientes = eventoDAO.obtenerEventosPendientes();
:int vacunasPendientes = 0;

:Calendar hoy = Calendar.getInstance();
:Calendar limiteDias = Calendar.getInstance();
:limiteDias.add(Calendar.DAY_OF_MONTH, 7);

:foreach EventoSanitario evento in eventosPendientes:;
:  Date fechaProgramada = sdf.parse(evento.getFechaProgramada());
:  Calendar calEvento = Calendar.getInstance();
:  calEvento.setTime(fechaProgramada);

if (¿calEvento >= hoy && calEvento <= limiteDias?) then (Dentro de 7 días)
  :vacunasPendientes++;
endif

' Preparar datos finales
:final int finalActivos = activos;
:final int finalSanos = sanos;
:final int finalVendidos = vendidos;
:final int finalMuertos = muertos;
:final int finalVacunas = vacunasPendientes;

|Handler - MainThread|
:mainHandler.post(() -> {});

|MainActivity|
' Actualizar TextViews
:tvAnimalesActivos.setText(String.valueOf(finalActivos));
:tvAnimalesSanos.setText(String.valueOf(finalSanos));
:tvAnimalesVendidos.setText(String.valueOf(finalVendidos));
:tvAnimalesMuertos.setText(String.valueOf(finalMuertos));

' Mostrar/ocultar alerta de vacunas
if (¿finalVacunas > 0?) then (Sí)
  :cardVacunaAlert.setVisibility(VISIBLE);
  :badgeCalendario.setVisibility(VISIBLE);
  :badgeCalendario.setText(String.valueOf(finalVacunas));
  
  if (¿finalVacunas == 1?) then (Singular)
    :tvVacunasPendientes.setText("Tienes 1 vacuna próxima a vencer");
  else (Plural)
    :tvVacunasPendientes.setText("Tienes " + finalVacunas + " vacunas próximas a vencer");
  endif
else (No)
  :cardVacunaAlert.setVisibility(GONE);
  :badgeCalendario.setVisibility(GONE);
endif

|Usuario|
:Visualiza dashboard con estadísticas;
note right
  Panel principal muestra:
  ┌─────────────────────────┐
  │ 🐄 Activos: XX         │
  │ ✅ Sanos: XX            │
  │ 💰 Vendidos: XX         │
  │ ☠️ Muertos: XX          │
  ├─────────────────────────┤
  │ ⚠️ X vacunas próximas   │
  └─────────────────────────┘
end note

stop

@enduml
```

---

## Resumen de Flujos y Componentes

| Diagrama | Activity/Helper | DAO | Hilos |
|----------|-----------------|-----|-------|
| 1. Registrar Animal | RegistroAnimalActivity, AnimalPresenter | AnimalDAO | ExecutorService + Handler |
| 2. Actualizar Animal | RegistroAnimalActivity (modo editar) | AnimalDAO | ExecutorService + Handler |
| 3. Eliminar Animal | DetalleAnimalActivity | AnimalDAO | Síncrono (UI Thread) |
| 4. Consultar Animal | DetalleAnimalActivity | AnimalDAO, GastoDAO | Síncrono |
| 5. Registrar Venta | DetalleAnimalActivity | AnimalDAO | ExecutorService + Handler |
| 6. Programar Evento | CalendarioActivity | EventoSanitarioDAO | Síncrono |
| 7. Notificaciones | NotificationHelper, NotificationReceiver | - | AlarmManager |
| 8. Marcar Realizado | EventosSanitariosActivity | EventoSanitarioDAO | ExecutorService + Handler |
| 9. Consultar Gastos | GastosActivity | GastoDAO | ExecutorService + Handler |
| 10. Compra Dividida | RegistroComprasActivity | GastoDAO | ExecutorService + Handler |
| 11. Registrar Alimento | AlimentacionActivity | AlimentacionDAO | ExecutorService + Handler |
| 12. Estadística Hato | MainActivity | AnimalDAO, EventoSanitarioDAO | ExecutorService + Handler |

---

**Documento generado automáticamente basado en el código fuente del sistema AgroApp**
