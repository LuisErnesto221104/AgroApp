package com.example.agroapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agroapp.adapters.EventoSanitarioAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.EventoSanitarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.EventoSanitario;
import com.example.agroapp.utils.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class CalendarioActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private EventoSanitarioDAO eventoDAO;
    private AnimalDAO animalDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Calendario Sanitario");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        eventoDAO = new EventoSanitarioDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        recyclerView = findViewById(R.id.recyclerEventos);
        FloatingActionButton fabNuevo = findViewById(R.id.fabNuevoEvento);
        Spinner spinnerFiltro = findViewById(R.id.spinnerFiltro);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String[] filtros = {"Todos", "Pendiente", "Realizado", "Cancelado"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, filtros);
        spinnerFiltro.setAdapter(spinnerAdapter);
        
        cargarEventos(spinnerFiltro.getSelectedItem().toString());
        
        fabNuevo.setOnClickListener(v -> mostrarDialogoNuevoEvento());
        
        spinnerFiltro.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                cargarEventos(parent.getItemAtPosition(position).toString());
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    
    private void cargarEventos(String filtro) {
        List<EventoSanitario> eventosList = eventoDAO.obtenerTodosLosEventos();
        if (!"Todos".equals(filtro)) {
            eventosList = eventosList.stream()
                .filter(e -> e.getEstado().equalsIgnoreCase(filtro))
                .collect(Collectors.toList());
        }
        EventoSanitarioAdapter adapter = new EventoSanitarioAdapter(this, eventosList, animalDAO, this::mostrarDialogoEditarEvento);
        recyclerView.setAdapter(adapter);
    }
    
    private void mostrarDialogoNuevoEvento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_evento_sanitario, null);
        builder.setView(dialogView);
        
        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        android.widget.Button btnFechaEvento = dialogView.findViewById(R.id.btnFechaEvento);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        
        // Configurar spinner de animales
        List<com.example.agroapp.models.Animal> animales = animalDAO.obtenerTodosLosAnimales();
        String[] nombresAnimales = new String[animales.size()];
        for (int i = 0; i < animales.size(); i++) {
            nombresAnimales[i] = animales.get(i).getNumeroArete() + " - " + 
                                 (animales.get(i).getNombre() != null ? animales.get(i).getNombre() : "Sin nombre");
        }
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, nombresAnimales);
        spinnerAnimal.setAdapter(animalAdapter);
        
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
                if (!animales.isEmpty()) {
                    EventoSanitario evento = new EventoSanitario();
                    evento.setAnimalId(animales.get(spinnerAnimal.getSelectedItemPosition()).getId());
                    evento.setTipo(spinnerTipo.getSelectedItem().toString());
                    evento.setFechaProgramada(fechaSeleccionada[0]);
                    evento.setDescripcion(etDescripcion.getText().toString());
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
                    
                    Toast.makeText(this, "Evento guardado", Toast.LENGTH_SHORT).show();
                    recreate(); // Recargar la actividad para reflejar los cambios
                }
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
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
                        // Cancelar notificación al marcar como realizado
                        NotificationHelper.cancelarNotificacion(this, evento.getId());
                        Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                        recreate();
                        break;
                    case 1:
                        // Implementar edición
                        Toast.makeText(this, "Función de edición", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        eventoDAO.eliminarEvento(evento.getId());
                        // Cancelar notificación al eliminar
                        NotificationHelper.cancelarNotificacion(this, evento.getId());
                        Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                        recreate();
                        break;
                }
            })
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
