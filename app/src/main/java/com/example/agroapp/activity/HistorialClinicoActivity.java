package com.example.agroapp.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
import com.example.agroapp.adapters.HistorialClinicoAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.HistorialClinicoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.HistorialClinico;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistorialClinicoActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private HistorialClinicoAdapter adapter;
    private HistorialClinicoDAO historialDAO;
    private AnimalDAO animalDAO;
    private List<HistorialClinico> historialList;
    private int animalId;  // ID interno para consultas FK
    private String animalArete;  // Arete recibido desde el intent
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_clinico);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historial Clínico");
        }
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        historialDAO = new HistorialClinicoDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        // Recibir arete y convertir a ID interno para consultas FK
        animalArete = getIntent().getStringExtra("arete");
        if (animalArete != null && !animalArete.isEmpty()) {
            animalId = animalDAO.obtenerIdPorArete(animalArete);
        } else {
            animalId = -1;
        }
        
        recyclerView = findViewById(R.id.recyclerHistorial);
        Button btnNuevo = findViewById(R.id.btnNuevoRegistro);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarHistorial();
        
        btnNuevo.setOnClickListener(v -> mostrarDialogoNuevoRegistro());
    }
    
    private void cargarHistorial() {
        historialList = historialDAO.obtenerHistorialPorAnimal(animalId);
        adapter = new HistorialClinicoAdapter(this, historialList, historial -> {
            mostrarOpcionesHistorial(historial);
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void mostrarDialogoNuevoRegistro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_historial_clinico, null);
        builder.setView(dialogView);
        
        android.widget.Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        EditText etEnfermedad = dialogView.findViewById(R.id.etEnfermedad);
        EditText etSintomas = dialogView.findViewById(R.id.etSintomas);
        EditText etTratamiento = dialogView.findViewById(R.id.etTratamiento);
        Spinner spinnerEstado = dialogView.findViewById(R.id.spinnerEstado);
        EditText etObservaciones = dialogView.findViewById(R.id.etObservaciones);
        
        String[] estados = {"En Tratamiento", "Recuperado", "Crónico"};
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, estados);
        spinnerEstado.setAdapter(estadoAdapter);
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String[] fechaSeleccionada = {sdf.format(calendar.getTime())};
        btnFecha.setText(fechaSeleccionada[0]);
        
        btnFecha.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    fechaSeleccionada[0] = sdf.format(calendar.getTime());
                    btnFecha.setText(fechaSeleccionada[0]);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        builder.setTitle("Nuevo Registro Clínico")
            .setPositiveButton("Guardar", (dialog, which) -> {
                HistorialClinico historial = new HistorialClinico();
                historial.setAnimalId(animalId);
                historial.setFecha(fechaSeleccionada[0]);
                historial.setEnfermedad(etEnfermedad.getText().toString());
                historial.setSintomas(etSintomas.getText().toString());
                historial.setTratamiento(etTratamiento.getText().toString());
                historial.setEstado(spinnerEstado.getSelectedItem().toString());
                historial.setObservaciones(etObservaciones.getText().toString());
                
                historialDAO.insertarHistorial(historial);
                Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
                cargarHistorial();
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void mostrarOpcionesHistorial(HistorialClinico historial) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Desea eliminar este registro clínico?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                historialDAO.eliminarHistorial(historial.getId());
                Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                cargarHistorial();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
