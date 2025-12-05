package com.example.agroapp.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agroapp.R;
import com.example.agroapp.adapters.EventoSanitarioAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.EventoSanitarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import com.example.agroapp.models.EventoSanitario;
import com.example.agroapp.utils.NotificationHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class CalendarioActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private CalendarView calendarView;
    private EventoSanitarioDAO eventoDAO;
    private AnimalDAO animalDAO;
    private List<EventoSanitario> todosLosEventos;
    
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Calendario Sanitario");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        eventoDAO = new EventoSanitarioDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        // Registrar launcher para permisos
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Se necesita el permiso de notificaciones para recibir recordatorios", 
                        Toast.LENGTH_LONG).show();
                }
            });
        
        // Solicitar permisos necesarios
        solicitarPermisos();
        
        recyclerView = findViewById(R.id.recyclerEventos);
        calendarView = findViewById(R.id.calendarView);
        Button btnNuevo = findViewById(R.id.btnNuevoEvento);
        Spinner spinnerFiltro = findViewById(R.id.spinnerFiltro);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String[] filtros = {"Todos", "Pendiente", "Realizado", "Cancelado"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, filtros);
        spinnerFiltro.setAdapter(spinnerAdapter);
        
        cargarEventos(spinnerFiltro.getSelectedItem().toString());
        
        // Listener para el CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaSeleccionada = sdf.format(selectedDate.getTime());
            
            // Filtrar eventos por fecha seleccionada
            filtrarEventosPorFecha(fechaSeleccionada, spinnerFiltro.getSelectedItem().toString());
        });
        
        btnNuevo.setOnClickListener(v -> mostrarDialogoNuevoEvento());
        
        spinnerFiltro.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                cargarEventos(parent.getItemAtPosition(position).toString());
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    
    private void solicitarPermisos() {
        // Solicitar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        // Verificar y solicitar permiso para alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("Permiso necesario")
                    .setMessage("Para programar recordatorios exactos, necesitas habilitar el permiso de alarmas exactas en la configuración")
                    .setPositiveButton("Configuración", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            }
        }
    }
    
    private void cargarEventos(String filtro) {
        todosLosEventos = eventoDAO.obtenerTodosLosEventos();
        List<EventoSanitario> eventosList = todosLosEventos;
        if (!"Todos".equals(filtro)) {
            eventosList = todosLosEventos.stream()
                .filter(e -> e.getEstado().equalsIgnoreCase(filtro))
                .collect(Collectors.toList());
        }
        EventoSanitarioAdapter adapter = new EventoSanitarioAdapter(this, eventosList, animalDAO, this::mostrarDialogoEditarEvento);
        recyclerView.setAdapter(adapter);
    }
    
    private void filtrarEventosPorFecha(String fecha, String filtroEstado) {
        if (todosLosEventos == null) {
            todosLosEventos = eventoDAO.obtenerTodosLosEventos();
        }
        
        List<EventoSanitario> eventosFiltrados = todosLosEventos.stream()
            .filter(e -> e.getFechaProgramada().equals(fecha))
            .filter(e -> "Todos".equals(filtroEstado) || e.getEstado().equalsIgnoreCase(filtroEstado))
            .collect(Collectors.toList());
        
        EventoSanitarioAdapter adapter = new EventoSanitarioAdapter(this, eventosFiltrados, animalDAO, this::mostrarDialogoEditarEvento);
        recyclerView.setAdapter(adapter);
        
        Toast.makeText(this, eventosFiltrados.size() + " eventos para " + fecha, Toast.LENGTH_SHORT).show();
    }
    
    private void mostrarDialogoNuevoEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_evento_sanitario, null);
        builder.setView(dialogView);
        
        Spinner spinnerRaza = dialogView.findViewById(R.id.spinnerRaza);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        Button btnFechaEvento = dialogView.findViewById(R.id.btnFechaEvento);
        Button btnHoraEvento = dialogView.findViewById(R.id.btnHoraEvento);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        EditText etCosto = dialogView.findViewById(R.id.etCosto);
        
        // Configurar spinner de razas únicas
        List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
        java.util.Set<String> razasSet = new java.util.HashSet<>();
        for (Animal animal : animales) {
            razasSet.add(animal.getRaza());
        }
        String[] razas = razasSet.toArray(new String[0]);
        ArrayAdapter<String> razaAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(razaAdapter);
        
        // Configurar spinner de tipos
        String[] tipos = {"Vacuna", "Desparasitación", "Vitaminas", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(tipoAdapter);
        
        // DatePicker para fecha
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String[] fechaSeleccionada = {sdf.format(calendar.getTime())};
        btnFechaEvento.setText(fechaSeleccionada[0]);
        
        // TimePicker para hora
        final String[] horaSeleccionada = {"09:00"};
        btnHoraEvento.setText(horaSeleccionada[0]);
        
        btnFechaEvento.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    fechaSeleccionada[0] = sdf.format(calendar.getTime());
                    btnFechaEvento.setText(fechaSeleccionada[0]);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        btnHoraEvento.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    horaSeleccionada[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    btnHoraEvento.setText(horaSeleccionada[0]);
                },
                9, 0, true
            );
            timePickerDialog.show();
        });
        
        builder.setTitle("Nuevo Evento Sanitario")
            .setPositiveButton("Guardar", (dialog, which) -> {
                if (razas.length > 0) {
                    // Validación de costo mejorada
                    double costo = 0;
                    try {
                        String costoStr = etCosto.getText().toString().trim();
                        if (!costoStr.isEmpty()) {
                            costo = Double.parseDouble(costoStr);
                            if (costo < 0) {
                                Toast.makeText(this, "El costo no puede ser negativo", Toast.LENGTH_SHORT).show();
                                return; // No cerrar el diálogo
                            }
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Ingrese un costo válido (números decimales permitidos)", Toast.LENGTH_SHORT).show();
                        return; // No cerrar el diálogo
                    }
                    
                    // Crear UN evento por raza
                    String razaSeleccionada = spinnerRaza.getSelectedItem().toString();
                    crearEventoPorRaza(razaSeleccionada, spinnerTipo.getSelectedItem().toString(),
                        fechaSeleccionada[0], horaSeleccionada[0],
                        etDescripcion.getText().toString(), costo, sdf);
                    
                    Toast.makeText(this, "Evento creado para raza " + razaSeleccionada, 
                        Toast.LENGTH_SHORT).show();
                    recreate();
                }
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void crearEventoPorRaza(String raza, String tipo, String fecha, String hora,
                                   String descripcion, double costo, SimpleDateFormat sdf) {
        EventoSanitario evento = new EventoSanitario();
        evento.setRaza(raza);
        evento.setTipo(tipo);
        evento.setFechaProgramada(fecha);
        evento.setHoraRecordatorio(hora);
        evento.setDescripcion(descripcion);
        evento.setCosto(costo);
        evento.setEstado("Pendiente");
        evento.setRecordatorio(1);
        
        long eventoId = eventoDAO.insertarEvento(evento);
        evento.setId((int) eventoId);
        
        // Programar notificación
        try {
            evento.setFechaEvento(sdf.parse(evento.getFechaProgramada()));
            NotificationHelper.programarNotificacion(this, evento);
        } catch (ParseException e) {
            Log.e("CalendarioActivity", "Error al parsear fecha", e);
        }
    }
    
    private void crearEvento(int animalId, String tipo, String fecha, String hora,
                           String descripcion, double costo, SimpleDateFormat sdf) {
        EventoSanitario evento = new EventoSanitario();
        evento.setAnimalId(animalId);
        evento.setTipo(tipo);
        evento.setFechaProgramada(fecha);
        evento.setHoraRecordatorio(hora);
        evento.setDescripcion(descripcion);
        evento.setCosto(costo);
        evento.setEstado("Pendiente");
        evento.setRecordatorio(1);
        
        long eventoId = eventoDAO.insertarEvento(evento);
        evento.setId((int) eventoId);
        
        // Programar notificación
        try {
            evento.setFechaEvento(sdf.parse(evento.getFechaProgramada()));
            NotificationHelper.programarNotificacion(this, evento);
        } catch (ParseException e) {
            Log.e("CalendarioActivity", "Error parsing date", e);
        }
    }
    
    private void mostrarDialogoEditarEvento(EventoSanitario evento) {
        String[] opciones = {"Marcar como Realizado", "Editar", "Eliminar"};
        new AlertDialog.Builder(this)
            .setTitle("Opciones")
            .setItems(opciones, (dialog, which) -> {
                switch (which) {
                    case 0:
                        evento.setEstado("Realizado");
                        evento.setFechaRealizada(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                        eventoDAO.actualizarEvento(evento);
                        NotificationHelper.cancelarNotificacion(this, evento.getId());
                        Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                        recreate();
                        break;
                    case 1:
                        mostrarDialogoEditar(evento);
                        break;
                    case 2:
                        new AlertDialog.Builder(this)
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Está seguro de eliminar este evento?")
                            .setPositiveButton("Eliminar", (d, w) -> {
                                eventoDAO.eliminarEvento(evento.getId());
                                NotificationHelper.cancelarNotificacion(this, evento.getId());
                                Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                                recreate();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                        break;
                }
            })
            .show();
    }
    
    private void mostrarDialogoEditar(EventoSanitario evento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_evento_sanitario, null);
        builder.setView(dialogView);
        
        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        Button btnFechaEvento = dialogView.findViewById(R.id.btnFechaEvento);
        Button btnHoraEvento = dialogView.findViewById(R.id.btnHoraEvento);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        EditText etCosto = dialogView.findViewById(R.id.etCosto);
        
        // Cargar datos actuales
        List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
        String[] nombresAnimales = new String[animales.size()];
        int animalIndex = 0;
        for (int i = 0; i < animales.size(); i++) {
            nombresAnimales[i] = animales.get(i).getNumeroArete() + " - " + animales.get(i).getRaza();
            if (animales.get(i).getId() == evento.getAnimalId()) {
                animalIndex = i;
            }
        }
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, nombresAnimales);
        spinnerAnimal.setAdapter(animalAdapter);
        spinnerAnimal.setSelection(animalIndex);
        
        String[] tipos = {"Vacuna", "Desparasitación", "Vitaminas", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(tipoAdapter);
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i].equals(evento.getTipo())) {
                spinnerTipo.setSelection(i);
                break;
            }
        }
        
        btnFechaEvento.setText(evento.getFechaProgramada());
        btnHoraEvento.setText(evento.getHoraRecordatorio() != null ? evento.getHoraRecordatorio() : "09:00");
        etDescripcion.setText(evento.getDescripcion());
        etCosto.setText(evento.getCosto() > 0 ? String.valueOf(evento.getCosto()) : "");
        
        final String[] fechaSeleccionada = {evento.getFechaProgramada()};
        final String[] horaSeleccionada = {evento.getHoraRecordatorio() != null ? evento.getHoraRecordatorio() : "09:00"};
        
        btnFechaEvento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    calendar.set(year, month, dayOfMonth);
                    fechaSeleccionada[0] = sdf.format(calendar.getTime());
                    btnFechaEvento.setText(fechaSeleccionada[0]);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        btnHoraEvento.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    horaSeleccionada[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    btnHoraEvento.setText(horaSeleccionada[0]);
                },
                9, 0, true
            );
            timePickerDialog.show();
        });
        
        builder.setTitle("Editar Evento")
            .setPositiveButton("Guardar", (dialog, which) -> {
                evento.setAnimalId(animales.get(spinnerAnimal.getSelectedItemPosition()).getId());
                evento.setTipo(spinnerTipo.getSelectedItem().toString());
                evento.setFechaProgramada(fechaSeleccionada[0]);
                evento.setHoraRecordatorio(horaSeleccionada[0]);
                evento.setDescripcion(etDescripcion.getText().toString());
                
                // Validación de costo mejorada
                double costo = 0;
                try {
                    String costoStr = etCosto.getText().toString().trim();
                    if (!costoStr.isEmpty()) {
                        costo = Double.parseDouble(costoStr);
                        if (costo < 0) {
                            Toast.makeText(this, "El costo no puede ser negativo", Toast.LENGTH_SHORT).show();
                            return; // No cerrar el diálogo
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Ingrese un costo válido (números decimales permitidos)", Toast.LENGTH_SHORT).show();
                    return; // No cerrar el diálogo
                }
                evento.setCosto(costo);
                
                eventoDAO.actualizarEvento(evento);
                
                // Reprogramar notificación
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    evento.setFechaEvento(sdf.parse(evento.getFechaProgramada()));
                    NotificationHelper.programarNotificacion(this, evento);
                } catch (ParseException e) {
                    Log.e("CalendarioActivity", "Error parsing date", e);
                }
                
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                recreate();
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
