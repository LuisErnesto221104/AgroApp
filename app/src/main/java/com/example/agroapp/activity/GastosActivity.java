package com.example.agroapp.activity;

import static com.example.agroapp.R.id.*;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.agroapp.adapters.GastoAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.GastoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import com.example.agroapp.models.Gasto;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GastosActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private GastoAdapter adapter;
    private GastoDAO gastoDAO;
    private AnimalDAO animalDAO;
    private List<Gasto> gastosList;
    private int animalIdFiltro = -1;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gastos e Inversiones");
        }
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        gastoDAO = new GastoDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        // Inicializar ExecutorService y Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        animalIdFiltro = getIntent().getIntExtra("animalId", -1);
        
        recyclerView = findViewById(R.id.recyclerGastos);
        Button btnNuevo = findViewById(R.id.btnNuevoGasto);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Inicializar lista y adapter UNA SOLA VEZ
        gastosList = new ArrayList<>();
        adapter = new GastoAdapter(this, gastosList, animalDAO, gasto -> {
            mostrarOpcionesGasto(gasto);
        });
        recyclerView.setAdapter(adapter);
        
        cargarGastos();
        
        btnNuevo.setOnClickListener(v -> {
            // Abrir RegistroComprasActivity
            android.content.Intent intent = new android.content.Intent(this, RegistroComprasActivity.class);
            startActivity(intent);
        });
    }
    
    private void cargarGastos() {
        // Ejecutar consulta de BD en hilo secundario
        executorService.execute(() -> {
            List<Gasto> nuevosGastos;
            if (animalIdFiltro != -1) {
                nuevosGastos = gastoDAO.obtenerGastosPorAnimal(animalIdFiltro);
            } else {
                nuevosGastos = gastoDAO.obtenerTodosLosGastos();
            }
            
            // Actualizar UI en el hilo principal
            mainHandler.post(() -> {
                gastosList.clear();
                gastosList.addAll(nuevosGastos);
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    private void mostrarDialogoNuevoGasto() {
        // Cargar animales en hilo secundario antes de mostrar el diálogo
        executorService.execute(() -> {
            List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
            
            // Extraer razas únicas
            java.util.Set<String> razasSet = new java.util.HashSet<>();
            for (Animal animal : animales) {
                razasSet.add(animal.getRaza());
            }
            String[] razas = razasSet.toArray(new String[0]);
            
            // Mostrar diálogo en el hilo principal
            mainHandler.post(() -> {
                if (razas.length == 0) {
                    Toast.makeText(this, "Debe registrar un animal primero", Toast.LENGTH_LONG).show();
                    return;
                }
                
                mostrarDialogoNuevoGastoConRazas(razas);
            });
        });
    }
    
    @SuppressLint("MissingInflatedId")
    private void mostrarDialogoNuevoGastoConRazas(String[] razas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_gasto, null);
        builder.setView(dialogView);
        
        Spinner spinnerRaza = dialogView.findViewById(R.id.spinnerRaza);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);
        EditText etConcepto = dialogView.findViewById(R.id.etConcepto);
        EditText etMonto = dialogView.findViewById(R.id.etMonto);
        android.widget.Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        EditText etObservaciones;
        etObservaciones = dialogView.findViewById(R.id.etObservaciones);

        // Configurar spinner de razas
        ArrayAdapter<String> razaAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(razaAdapter);
        
        // Configurar spinner de tipos
        String[] tipos = {"Alimento", "Medicamento", "Vacuna", "Consulta Veterinaria", "Otro"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, tipos);
        spinnerTipo.setAdapter(tipoAdapter);
        
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
        
        builder.setTitle("Nuevo Gasto")
            .setPositiveButton("Guardar", (dialog, which) -> {
                try {
                    String conceptoStr = etConcepto.getText().toString().trim();
                    String montoStr = etMonto.getText().toString().trim();
                    
                    if (conceptoStr.isEmpty() || montoStr.isEmpty()) {
                        Toast.makeText(this, "Complete todos los campos requeridos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Gasto gasto = new Gasto();
                    gasto.setRaza(spinnerRaza.getSelectedItem().toString());
                    gasto.setTipo(spinnerTipo.getSelectedItem().toString());
                    gasto.setConcepto(conceptoStr);
                    gasto.setMonto(Double.parseDouble(montoStr));
                    gasto.setFecha(fechaSeleccionada[0]);
                    gasto.setObservaciones(etObservaciones.getText().toString());
                    
                    // Insertar en hilo secundario
                    executorService.execute(() -> {
                        gastoDAO.insertarGasto(gasto);
                        mainHandler.post(() -> {
                            Toast.makeText(this, "Gasto registrado para raza " + gasto.getRaza(), Toast.LENGTH_SHORT).show();
                            cargarGastos();
                        });
                    });
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void mostrarOpcionesGasto(Gasto gasto) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar gasto")
            .setMessage("¿Desea eliminar este gasto?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                // Eliminar en hilo secundario
                executorService.execute(() -> {
                    gastoDAO.eliminarGasto(gasto.getId());
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Gasto eliminado", Toast.LENGTH_SHORT).show();
                        cargarGastos();
                    });
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar gastos al volver de otra actividad
        cargarGastos();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar ExecutorService al destruir la actividad
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
