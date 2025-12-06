package com.example.agroapp.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agroapp.R;
import com.example.agroapp.adapters.EventoSanitarioAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.EventoSanitarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.EventoSanitario;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventosSanitariosActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private EventoSanitarioAdapter adapter;
    private EventoSanitarioDAO eventoDAO;
    private AnimalDAO animalDAO;
    private List<EventoSanitario> eventosList;
    private int animalId;  // ID interno para consultas FK
    private String animalArete;  // Arete recibido desde el intent
    private ExecutorService executorService;
    private Handler mainHandler;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos_sanitarios);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Eventos Sanitarios");
        }

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        eventoDAO = new EventoSanitarioDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Recibir arete y convertir a ID interno para consultas FK
        animalArete = getIntent().getStringExtra("arete");
        if (animalArete != null && !animalArete.isEmpty()) {
            animalId = animalDAO.obtenerIdPorArete(animalArete);
        } else {
            animalId = -1;
        }

        recyclerView = findViewById(R.id.recyclerEventos);
        Button btnNuevo = findViewById(R.id.btnNuevoEvento);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarEventos();
        
        btnNuevo.setOnClickListener(v -> mostrarDialogoNuevoEvento());
    }
    
    private void cargarEventos() {
        executorService.execute(() -> {
            List<EventoSanitario> eventosTemp = eventoDAO.obtenerEventosPorAnimal(animalId);
            mainHandler.post(() -> {
                eventosList = eventosTemp;
                adapter = new EventoSanitarioAdapter(this, eventosList, animalDAO, evento -> {
                    mostrarOpcionesEvento(evento);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }
    
    private void mostrarDialogoNuevoEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_evento_sanitario, null);
        builder.setView(dialogView);
        
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        android.widget.Button btnFechaEvento = dialogView.findViewById(R.id.btnFechaEvento);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        
        String[] tipos = {"Vacuna", "Desparasitación", "Vitaminas", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(tipoAdapter);
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String[] fechaSeleccionada = {sdf.format(calendar.getTime())};
        btnFechaEvento.setText(fechaSeleccionada[0]);
        
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
        
        builder.setTitle("Nuevo Evento Sanitario")
            .setPositiveButton("Guardar", (dialog, which) -> {
                EventoSanitario evento = new EventoSanitario();
                evento.setAnimalId(animalId);
                evento.setTipo(spinnerTipo.getSelectedItem().toString());
                evento.setFechaProgramada(fechaSeleccionada[0]);
                evento.setDescripcion(etDescripcion.getText().toString());
                evento.setEstado("Pendiente");
                evento.setRecordatorio(1);
                
                executorService.execute(() -> {
                    eventoDAO.insertarEvento(evento);
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show();
                        cargarEventos();
                    });
                });
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void mostrarOpcionesEvento(EventoSanitario evento) {
        String[] opciones = {"Editar", "Marcar como Realizado", "Eliminar"};
        new AlertDialog.Builder(this)
            .setTitle("Opciones")
            .setItems(opciones, (dialog, which) -> {
                if (which == 0) {
                    // Editar evento
                    mostrarDialogoEditarEvento(evento);
                } else if (which == 1) {
                    // Marcar como realizado
                    executorService.execute(() -> {
                        evento.setEstado("Realizado");
                        evento.setFechaRealizada(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                        eventoDAO.actualizarEvento(evento);
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                            cargarEventos();
                        });
                    });
                } else {
                    // Eliminar
                    executorService.execute(() -> {
                        eventoDAO.eliminarEvento(evento.getId());
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                            cargarEventos();
                        });
                    });
                }
            })
            .show();
    }
    
    private void mostrarDialogoEditarEvento(EventoSanitario evento) {
        try {
            if (evento == null) {
                Log.e("EventosSanitariosActivity", "Evento es null en mostrarDialogoEditarEvento");
                Toast.makeText(this, "Error: evento no válido", Toast.LENGTH_SHORT).show();
                return;
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_evento_sanitario, null);
            builder.setView(dialogView);
            
            Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
            android.widget.Button btnFechaEvento = dialogView.findViewById(R.id.btnFechaEvento);
            EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
            
            if (spinnerTipo == null || btnFechaEvento == null || etDescripcion == null) {
                Log.e("EventosSanitariosActivity", "Error: vistas del diálogo son null");
                Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String[] tipos = {"Vacuna", "Desparasitación", "Vitaminas", "Otro"};
            ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tipos);
            spinnerTipo.setAdapter(tipoAdapter);
            
            // Cargar datos existentes con verificación null
            String tipoEvento = evento.getTipo();
            if (tipoEvento != null) {
                for (int i = 0; i < tipos.length; i++) {
                    if (tipos[i].equals(tipoEvento)) {
                        spinnerTipo.setSelection(i);
                        break;
                    }
                }
            }
            
            String descripcion = evento.getDescripcion();
            if (descripcion != null) {
                etDescripcion.setText(descripcion);
            }
            
            String fechaProgramada = evento.getFechaProgramada();
            if (fechaProgramada != null) {
                btnFechaEvento.setText(fechaProgramada);
            }
            
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            final String[] fechaSeleccionada = {fechaProgramada != null ? fechaProgramada : sdf.format(calendar.getTime())};
            
            btnFechaEvento.setOnClickListener(v -> {
                try {
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
                } catch (Exception e) {
                    Log.e("EventosSanitariosActivity", "Error al mostrar DatePicker", e);
                    Toast.makeText(this, "Error al abrir selector de fecha", Toast.LENGTH_SHORT).show();
                }
            });
            
            builder.setTitle("Editar Evento Sanitario")
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    try {
                        evento.setTipo(spinnerTipo.getSelectedItem().toString());
                        evento.setFechaProgramada(fechaSeleccionada[0]);
                        evento.setDescripcion(etDescripcion.getText().toString());
                        
                        executorService.execute(() -> {
                            try {
                                eventoDAO.actualizarEvento(evento);
                                mainHandler.post(() -> {
                                    Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                                    cargarEventos();
                                });
                            } catch (Exception e) {
                                Log.e("EventosSanitariosActivity", "Error al actualizar evento en DB", e);
                                mainHandler.post(() -> 
                                    Toast.makeText(this, "Error al actualizar evento", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    } catch (Exception e) {
                        Log.e("EventosSanitariosActivity", "Error al preparar actualización", e);
                        Toast.makeText(this, "Error al actualizar evento", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
        } catch (Exception e) {
            Log.e("EventosSanitariosActivity", "Error general en mostrarDialogoEditarEvento", e);
            Toast.makeText(this, "Error al editar evento: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
