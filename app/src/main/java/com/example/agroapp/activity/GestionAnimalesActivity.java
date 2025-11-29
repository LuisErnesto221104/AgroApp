package com.example.agroapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agroapp.R;
import com.example.agroapp.adapters.AnimalAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GestionAnimalesActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private AnimalDAO animalDAO;
    private List<Animal> animalesList;
    private List<Animal> animalesListFull;
    private String estadoFiltro = "Todos";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_animales);
        
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mis Animales");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        
        recyclerView = findViewById(R.id.recyclerAnimales);
        Spinner spinnerFiltro = findViewById(R.id.spinnerFiltro);
        SearchView searchView = findViewById(R.id.searchView);
        Button btnNuevo = findViewById(R.id.btnNuevoAnimal);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Configurar spinner de filtros
        String[] filtros = {"Todos", "Sano", "Enfermo", "Vendido", "Muerto"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, filtros);
        spinnerFiltro.setAdapter(spinnerAdapter);
        
        cargarAnimales();
        
        // Listener para filtro
        spinnerFiltro.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filtrarPorEstado(filtros[position]);
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Listener para búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarPorTexto(newText);
                return true;
            }
        });
        
        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroAnimalActivity.class);
            intent.putExtra("modo", "nuevo");
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        cargarAnimales();
    }
    
    private void cargarAnimales() {
        animalesList = animalDAO.obtenerTodosLosAnimales();
        animalesListFull = new ArrayList<>(animalesList);
        adapter = new AnimalAdapter(this, animalesList, animal -> {
            Intent intent = new Intent(this, DetalleAnimalActivity.class);
            intent.putExtra("animalId", animal.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void filtrarPorEstado(String estado) {
        estadoFiltro = estado;
        SearchView searchView = findViewById(R.id.searchView);
        String textoActual = searchView.getQuery().toString();
        aplicarFiltros(textoActual);
    }
    
    private void filtrarPorTexto(String texto) {
        aplicarFiltros(texto);
    }
    
    private void aplicarFiltros(String texto) {
        animalesList.clear();
        String textoBusqueda = texto.toLowerCase().trim();
        
        for (Animal animal : animalesListFull) {
            // Filtro por estado
            boolean cumpleEstado = estadoFiltro.equals("Todos") || 
                (animal.getEstado() != null && animal.getEstado().equals(estadoFiltro));
            
            // Filtro por texto
            boolean cumpleTexto = textoBusqueda.isEmpty() ||
                (animal.getNumeroArete() != null && animal.getNumeroArete().toLowerCase().contains(textoBusqueda)) ||
                (animal.getRaza() != null && animal.getRaza().toLowerCase().contains(textoBusqueda));
            
            if (cumpleEstado && cumpleTexto) {
                animalesList.add(animal);
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
