package com.example.agroapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agroapp.adapters.AlimentacionAdapter;
import com.example.agroapp.dao.AlimentacionDAO;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Alimentacion;
import com.example.agroapp.models.Animal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AlimentacionActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private AlimentacionDAO alimentacionDAO;
    private AnimalDAO animalDAO;
    private int animalIdFiltro = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Control de Alimentación");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        alimentacionDAO = new AlimentacionDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        animalIdFiltro = getIntent().getIntExtra("animalId", -1);
        
        recyclerView = findViewById(R.id.recyclerAlimentacion);
        FloatingActionButton fabNuevo = findViewById(R.id.fabNuevoRegistro);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarRegistros();
        
        fabNuevo.setOnClickListener(v -> mostrarDialogoNuevoRegistro());
    }
    
    private void cargarRegistros() {
        List<Alimentacion> alimentacionList;
        if (animalIdFiltro != -1) {
            alimentacionList = alimentacionDAO.obtenerAlimentacionPorAnimal(animalIdFiltro);
        } else {
            alimentacionList = new java.util.ArrayList<>();
            List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
            for (Animal animal : animales) {
                alimentacionList.addAll(alimentacionDAO.obtenerAlimentacionPorAnimal(animal.getId()));
            }
        }
        AlimentacionAdapter adapter = new AlimentacionAdapter(this, alimentacionList, animalDAO, this::mostrarOpcionesRegistro);
        recyclerView.setAdapter(adapter);
    }
    
    private void mostrarDialogoNuevoRegistro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_alimentacion, null);
        builder.setView(dialogView);
        
        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipoAlimento);
        Spinner spinnerUnidad = dialogView.findViewById(R.id.spinnerUnidad);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidad);
        android.widget.Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        EditText etObservaciones = dialogView.findViewById(R.id.etObservaciones);
        
        // Configurar spinner de animales
        List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
        String[] nombresAnimales = new String[animales.size()];
        for (int i = 0; i < animales.size(); i++) {
            nombresAnimales[i] = animales.get(i).getNumeroArete() + " - " + 
                                 (animales.get(i).getNombre() != null ? animales.get(i).getNombre() : "Sin nombre");
        }
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, nombresAnimales);
        spinnerAnimal.setAdapter(animalAdapter);
        
        // Configurar spinner de tipos
        String[] tipos = {"Pasto", "Forraje", "Concentrado", "Grano", "Suplemento", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(tipoAdapter);
        
        // Configurar spinner de unidades
        String[] unidades = {"kg", "g", "toneladas", "pacas", "litros"};
        ArrayAdapter<String> unidadAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, unidades);
        spinnerUnidad.setAdapter(unidadAdapter);
        
        // DatePicker para fecha
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
        
        builder.setTitle("Nuevo Registro de Alimentación")
            .setPositiveButton("Guardar", (dialog, which) -> {
                if (!animales.isEmpty()) {
                    try {
                        Alimentacion alimentacion = new Alimentacion();
                    alimentacion.setAnimalId(animales.get(spinnerAnimal.getSelectedItemPosition()).getId());
                    alimentacion.setTipoAlimento(spinnerTipo.getSelectedItem().toString());
                    alimentacion.setCantidad(Double.parseDouble(etCantidad.getText().toString()));
                    alimentacion.setUnidad(spinnerUnidad.getSelectedItem().toString());
                    alimentacion.setFecha(fechaSeleccionada[0]);
                        alimentacion.setObservaciones(etObservaciones.getText().toString());
                        
                        alimentacionDAO.insertarAlimentacion(alimentacion);
                        Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
                        cargarRegistros();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void mostrarOpcionesRegistro(Alimentacion alimentacion) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Desea eliminar este registro?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                alimentacionDAO.eliminarAlimentacion(alimentacion.getId());
                Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                cargarRegistros();
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
