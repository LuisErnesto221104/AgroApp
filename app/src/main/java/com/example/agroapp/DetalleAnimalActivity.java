package com.example.agroapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.GastoDAO;
import com.example.agroapp.dao.HistorialClinicoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.text.NumberFormat;
import java.util.Locale;

public class DetalleAnimalActivity extends AppCompatActivity {
    
    private TextView tvArete, tvNombre, tvRaza, tvSexo, tvEdad, tvEstado, 
                     tvFechaIngreso, tvPrecioCompra, tvTotalGastos, tvObservaciones;
    private Button btnEditar, btnEliminar, btnHistorialClinico, btnEventosSanitarios, 
                   btnGastos, btnAlimentacion;
    private AnimalDAO animalDAO;
    private GastoDAO gastoDAO;
    private HistorialClinicoDAO historialDAO;
    private int animalId;
    private Animal animal;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_animal);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detalle del Animal");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        gastoDAO = new GastoDAO(dbHelper);
        historialDAO = new HistorialClinicoDAO(dbHelper);
        
        animalId = getIntent().getIntExtra("animalId", -1);
        
        inicializarVistas();
        cargarDatos();
        configurarListeners();
    }
    
    private void inicializarVistas() {
        tvArete = findViewById(R.id.tvArete);
        tvNombre = findViewById(R.id.tvNombre);
        tvRaza = findViewById(R.id.tvRaza);
        tvSexo = findViewById(R.id.tvSexo);
        tvEdad = findViewById(R.id.tvEdad);
        tvEstado = findViewById(R.id.tvEstado);
        tvFechaIngreso = findViewById(R.id.tvFechaIngreso);
        tvPrecioCompra = findViewById(R.id.tvPrecioCompra);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvObservaciones = findViewById(R.id.tvObservaciones);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnHistorialClinico = findViewById(R.id.btnHistorialClinico);
        btnEventosSanitarios = findViewById(R.id.btnEventosSanitarios);
        btnGastos = findViewById(R.id.btnGastos);
        btnAlimentacion = findViewById(R.id.btnAlimentacion);
    }
    
    private void cargarDatos() {
        animal = animalDAO.obtenerAnimalPorId(animalId);
        if (animal != null) {
            tvArete.setText("Arete: " + animal.getNumeroArete());
            tvNombre.setText(animal.getNombre() != null ? animal.getNombre() : "Sin nombre");
            tvRaza.setText("Raza: " + (animal.getRaza() != null ? animal.getRaza() : "-"));
            tvSexo.setText("Sexo: " + (animal.getSexo() != null ? animal.getSexo() : "-"));
            tvEstado.setText("Estado: " + (animal.getEstado() != null ? animal.getEstado() : "-"));
            tvFechaIngreso.setText("Ingreso: " + (animal.getFechaIngreso() != null ? animal.getFechaIngreso() : "-"));
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("es").setRegion("MX").build());
            tvPrecioCompra.setText("Precio Compra: " + formatter.format(animal.getPrecioCompra()));
            
            double totalGastos = gastoDAO.obtenerTotalGastosPorAnimal(animalId);
            tvTotalGastos.setText("Total Gastos: " + formatter.format(totalGastos));
            
            tvObservaciones.setText(animal.getObservaciones() != null ? animal.getObservaciones() : "Sin observaciones");
        }
    }
    
    private void configurarListeners() {
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroAnimalActivity.class);
            intent.putExtra("modo", "editar");
            intent.putExtra("animalId", animalId);
            startActivity(intent);
        });
        
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        
        btnHistorialClinico.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistorialClinicoActivity.class);
            intent.putExtra("animalId", animalId);
            startActivity(intent);
        });
        
        btnEventosSanitarios.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventosSanitariosActivity.class);
            intent.putExtra("animalId", animalId);
            startActivity(intent);
        });
        
        btnGastos.setOnClickListener(v -> {
            Intent intent = new Intent(this, GastosActivity.class);
            intent.putExtra("animalId", animalId);
            startActivity(intent);
        });
        
        btnAlimentacion.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlimentacionActivity.class);
            intent.putExtra("animalId", animalId);
            startActivity(intent);
        });
    }
    
    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Está seguro de que desea eliminar este animal? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                animalDAO.eliminarAnimal(animalId);
                Toast.makeText(this, "Animal eliminado", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
