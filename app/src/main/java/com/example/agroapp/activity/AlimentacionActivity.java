package com.example.agroapp.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agroapp.R;
import com.example.agroapp.adapters.AlimentacionAdapter;
import com.example.agroapp.dao.AlimentacionDAO;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Alimentacion;
import com.example.agroapp.models.Animal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlimentacionActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private AlimentacionDAO alimentacionDAO;
    private AnimalDAO animalDAO;
    private int animalIdFiltro = -1;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Control de Alimentación");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        alimentacionDAO = new AlimentacionDAO(dbHelper);
        animalDAO = new AnimalDAO(dbHelper);
        
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        animalIdFiltro = getIntent().getIntExtra("animalId", -1);
        
        recyclerView = findViewById(R.id.recyclerAlimentacion);
        Button btnNuevo = findViewById(R.id.btnNuevoRegistro);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        cargarRegistros();
        
        btnNuevo.setOnClickListener(v -> mostrarDialogoNuevoRegistro());
    }
    
    private void cargarRegistros() {
        executorService.execute(() -> {
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
            
            mainHandler.post(() -> {
                AlimentacionAdapter adapter = new AlimentacionAdapter(this, alimentacionList, animalDAO, this::mostrarOpcionesRegistro);
                recyclerView.setAdapter(adapter);
            });
        });
    }
    
    private void mostrarDialogoNuevoRegistro() {
        executorService.execute(() -> {
            // Cargar animales en hilo secundario
            List<Animal> animales = animalDAO.obtenerTodosLosAnimales();
            
            mainHandler.post(() -> {
                mostrarDialogoConAnimales(animales);
            });
        });
    }
    
    private void mostrarDialogoConAnimales(List<Animal> animales) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_alimentacion, null);
        builder.setView(dialogView);
        
        Spinner spinnerAnimal = dialogView.findViewById(R.id.spinnerAnimal);
        CheckBox cbSeleccionarPorRaza = dialogView.findViewById(R.id.cbSeleccionarPorRaza);
        LinearLayout layoutRazas = dialogView.findViewById(R.id.layoutRazas);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipoAlimento);
        Spinner spinnerUnidad = dialogView.findViewById(R.id.spinnerUnidad);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidad);
        EditText etCosto = dialogView.findViewById(R.id.etCosto);
        android.widget.Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        EditText etObservaciones = dialogView.findViewById(R.id.etObservaciones);
        
        // Configurar spinner de animales
        String[] nombresAnimales = new String[animales.size()];
        for (int i = 0; i < animales.size(); i++) {
            nombresAnimales[i] = animales.get(i).getNumeroArete() + " - " + animales.get(i).getRaza();
        }
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, nombresAnimales);
        spinnerAnimal.setAdapter(animalAdapter);
        
        // Obtener razas únicas
        Set<String> razasSet = new HashSet<>();
        for (Animal animal : animales) {
            razasSet.add(animal.getRaza());
        }
        List<String> razasList = new ArrayList<>(razasSet);
        List<CheckBox> checkBoxesRazas = new ArrayList<>();
        
        for (String raza : razasList) {
            CheckBox cb = new CheckBox(this);
            cb.setText(raza);
            checkBoxesRazas.add(cb);
            layoutRazas.addView(cb);
        }
        
        // Toggle entre selección individual y por raza
        cbSeleccionarPorRaza.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spinnerAnimal.setVisibility(View.GONE);
                layoutRazas.setVisibility(View.VISIBLE);
            } else {
                spinnerAnimal.setVisibility(View.VISIBLE);
                layoutRazas.setVisibility(View.GONE);
            }
        });
        
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
                try {
                    double cantidad = Double.parseDouble(etCantidad.getText().toString());
                    double costo;
                    String costoStr = etCosto.getText().toString().trim();
                    if (!costoStr.isEmpty()) {
                        costo = Double.parseDouble(costoStr);
                    } else {
                        costo = 0;
                    }

                    if (cbSeleccionarPorRaza.isChecked()) {
                        // Guardar para todos los animales de las razas seleccionadas
                        List<String> razasSeleccionadas = new ArrayList<>();
                        for (CheckBox cb : checkBoxesRazas) {
                            if (cb.isChecked()) {
                                razasSeleccionadas.add(cb.getText().toString());
                            }
                        }
                        
                        if (razasSeleccionadas.isEmpty()) {
                            Toast.makeText(this, "Seleccione al menos una raza", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        List<Animal> animalesFiltrados = animales.stream()
                            .filter(a -> razasSeleccionadas.contains(a.getRaza()))
                            .collect(Collectors.toList());
                        
                        int totalAnimales = animalesFiltrados.size();
                        
                        executorService.execute(() -> {
                            for (Animal animal : animalesFiltrados) {
                                guardarAlimentacion(animal.getId(), 
                                    spinnerTipo.getSelectedItem().toString(),
                                    cantidad,
                                    spinnerUnidad.getSelectedItem().toString(),
                                    fechaSeleccionada[0],
                                    costo,
                                    etObservaciones.getText().toString());
                            }
                            
                            mainHandler.post(() -> {
                                Toast.makeText(this, "Registros guardados para " + totalAnimales + " animales", 
                                    Toast.LENGTH_LONG).show();
                                cargarRegistros();
                            });
                        });
                    } else {
                        // Guardar para un solo animal
                        if (!animales.isEmpty()) {
                            final int animalIdFinal = animales.get(spinnerAnimal.getSelectedItemPosition()).getId();
                            final String tipoFinal = spinnerTipo.getSelectedItem().toString();
                            final String unidadFinal = spinnerUnidad.getSelectedItem().toString();
                            final String observacionesFinal = etObservaciones.getText().toString();
                            
                            executorService.execute(() -> {
                                guardarAlimentacion(animalIdFinal,
                                    tipoFinal,
                                    cantidad,
                                    unidadFinal,
                                    fechaSeleccionada[0],
                                    costo,
                                    observacionesFinal);
                                
                                mainHandler.post(() -> {
                                    Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
                                    cargarRegistros();
                                });
                            });
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .create()
            .show();
    }
    
    private void guardarAlimentacion(int animalId, String tipo, double cantidad, 
                                    String unidad, String fecha, double costo, String observaciones) {
        Alimentacion alimentacion = new Alimentacion();
        alimentacion.setAnimalId(animalId);
        alimentacion.setTipoAlimento(tipo);
        alimentacion.setCantidad(cantidad);
        alimentacion.setUnidad(unidad);
        alimentacion.setFecha(fecha);
        alimentacion.setCosto(costo);
        alimentacion.setObservaciones(observaciones);
        alimentacionDAO.insertarAlimentacion(alimentacion);
    }
    
    private void mostrarOpcionesRegistro(Alimentacion alimentacion) {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar registro")
            .setMessage("¿Desea eliminar este registro?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                executorService.execute(() -> {
                    alimentacionDAO.eliminarAlimentacion(alimentacion.getId());
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                        cargarRegistros();
                    });
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
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
