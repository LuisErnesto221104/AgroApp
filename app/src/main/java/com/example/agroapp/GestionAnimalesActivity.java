package com.example.agroapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.agroapp.adapters.AnimalAdapter;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GestionAnimalesActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private AnimalAdapter adapter;
    private AnimalDAO animalDAO;
    private List<Animal> animalesList;
    private List<Animal> animalesListFull;
    
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
        FloatingActionButton fabNuevo = findViewById(R.id.fabNuevoAnimal);
        
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
        
        fabNuevo.setOnClickListener(v -> {
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
        if (estado.equals("Todos")) {
            animalesList.clear();
            animalesList.addAll(animalesListFull);
        } else {
            animalesList.clear();
            for (Animal animal : animalesListFull) {
                if (animal.getEstado() != null && animal.getEstado().equals(estado)) {
                    animalesList.add(animal);
                }
            }
        }
        adapter.notifyItemRangeChanged(0, animalesList.size());
    }
    
    private void filtrarPorTexto(String texto) {
        animalesList.clear();
        if (texto.isEmpty()) {
            animalesList.addAll(animalesListFull);
        } else {
            String textoBusqueda = texto.toLowerCase();
            for (Animal animal : animalesListFull) {
                if ((animal.getNumeroArete() != null && animal.getNumeroArete().toLowerCase().contains(textoBusqueda)) ||
                    (animal.getNombre() != null && animal.getNombre().toLowerCase().contains(textoBusqueda)) ||
                    (animal.getRaza() != null && animal.getRaza().toLowerCase().contains(textoBusqueda))) {
                    animalesList.add(animal);
                }
            }
        }
        adapter.notifyItemRangeChanged(0, animalesList.size());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
