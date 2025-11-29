package com.example.agroapp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agroapp.R;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.GastoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import com.example.agroapp.models.Gasto;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroComprasActivity extends BaseActivity {
    
    private EditText etNombreCompra, etPrecioTotal, etBuscarAnimal;
    private RadioGroup rgTipoCompra;
    private LinearLayout layoutAnimales;
    private TextView tvTotalPorAnimal;
    private Button btnGuardar;
    
    private AnimalDAO animalDAO;
    private GastoDAO gastoDAO;
    private List<Animal> animalesList;
    private List<Animal> todosLosAnimales;
    private List<CheckBox> checkBoxes;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_compras);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registro de Compras");
        }
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        gastoDAO = new GastoDAO(dbHelper);
        
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        inicializarVistas();
        cargarAnimales();
        configurarListeners();
    }
    
    private void inicializarVistas() {
        etNombreCompra = findViewById(R.id.etNombreCompra);
        etPrecioTotal = findViewById(R.id.etPrecioTotal);
        etBuscarAnimal = findViewById(R.id.etBuscarAnimal);
        rgTipoCompra = findViewById(R.id.rgTipoCompra);
        layoutAnimales = findViewById(R.id.layoutAnimales);
        tvTotalPorAnimal = findViewById(R.id.tvTotalPorAnimal);
        btnGuardar = findViewById(R.id.btnGuardar);
        
        checkBoxes = new ArrayList<>();
    }
    
    private void cargarAnimales() {
        executorService.execute(() -> {
            List<Animal> todosAnimales = animalDAO.obtenerTodos();
            todosLosAnimales = new ArrayList<>();
            animalesList = new ArrayList<>();
            
            // Filtrar animales vendidos y muertos
            for (Animal animal : todosAnimales) {
                if (animal.getEstado() != null && 
                    !animal.getEstado().equalsIgnoreCase("vendido") && 
                    !animal.getEstado().equalsIgnoreCase("muerto")) {
                    todosLosAnimales.add(animal);
                    animalesList.add(animal);
                }
            }
            
            mainHandler.post(() -> {
                mostrarAnimales(animalesList);
            });
        });
    }
    
    private void mostrarAnimales(List<Animal> animales) {
        layoutAnimales.removeAllViews();
        checkBoxes.clear();
        
        if (animales.isEmpty()) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay animales que coincidan con la búsqueda");
            tvVacio.setPadding(16, 16, 16, 16);
            layoutAnimales.addView(tvVacio);
            return;
        }
        
        for (Animal animal : animales) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(animal.getNumeroArete() + " - " + animal.getRaza());
            checkBox.setTag(animal.getId());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> calcularPorAnimal());
            
            checkBoxes.add(checkBox);
            layoutAnimales.addView(checkBox);
        }
    }
    
    private void filtrarAnimales(String query) {
        if (todosLosAnimales == null) {
            return;
        }
        
        if (query == null || query.trim().isEmpty()) {
            mostrarAnimales(todosLosAnimales);
            return;
        }
        
        String queryLower = query.toLowerCase().trim();
        List<Animal> filtrados = new ArrayList<>();
        
        for (Animal animal : todosLosAnimales) {
            String arete = animal.getNumeroArete().toLowerCase();
            String raza = animal.getRaza().toLowerCase();
            
            if (arete.contains(queryLower) || raza.contains(queryLower)) {
                filtrados.add(animal);
            }
        }
        
        mostrarAnimales(filtrados);
    }
    
    private void configurarListeners() {
        // Listener para búsqueda de animales
        etBuscarAnimal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarAnimales(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etPrecioTotal.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                calcularPorAnimal();
            }
        });
        
        btnGuardar.setOnClickListener(v -> guardarCompra());
    }
    
    private void calcularPorAnimal() {
        String precioStr = etPrecioTotal.getText().toString().trim();
        if (precioStr.isEmpty()) {
            tvTotalPorAnimal.setText("Por animal: $0.00");
            return;
        }
        
        double precioTotal = Double.parseDouble(precioStr);
        int animalesSeleccionados = 0;
        
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) {
                animalesSeleccionados++;
            }
        }
        
        if (animalesSeleccionados > 0) {
            double precioPorAnimal = precioTotal / animalesSeleccionados;
            tvTotalPorAnimal.setText(String.format("Por animal: $%.2f", precioPorAnimal));
        } else {
            tvTotalPorAnimal.setText("Por animal: $0.00");
        }
    }
    
    private void guardarCompra() {
        String nombreCompra = etNombreCompra.getText().toString().trim();
        String precioStr = etPrecioTotal.getText().toString().trim();
        
        if (nombreCompra.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int animalesSeleccionados = 0;
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) animalesSeleccionados++;
        }
        
        if (animalesSeleccionados == 0) {
            Toast.makeText(this, "Seleccione al menos un animal", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double precioTotal = Double.parseDouble(precioStr);
        double precioPorAnimal = precioTotal / animalesSeleccionados;
        final int totalAnimales = animalesSeleccionados;
        
        int tipoSeleccionado = rgTipoCompra.getCheckedRadioButtonId();
        RadioButton rbSeleccionado = findViewById(tipoSeleccionado);
        String tipoCompra = rbSeleccionado.getText().toString();
        
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        
        executorService.execute(() -> {
            for (CheckBox cb : checkBoxes) {
                if (cb.isChecked()) {
                    int animalId = (int) cb.getTag();
                    
                    Gasto gasto = new Gasto();
                    gasto.setAnimalId(animalId);
                    gasto.setTipo(tipoCompra);
                    gasto.setConcepto(nombreCompra);
                    gasto.setMonto(precioPorAnimal);
                    gasto.setFecha(fechaActual);
                    gasto.setObservaciones("Compra distribuida entre " + totalAnimales + " animales");
                    
                    gastoDAO.insertarGasto(gasto);
                }
            }
            
            mainHandler.post(() -> {
                Toast.makeText(this, "Compra registrada exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
