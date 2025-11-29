package com.example.agroapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.agroapp.R;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.EventoSanitarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import com.example.agroapp.models.EventoSanitario;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {
    
    private TextView tvWelcome, tvAnimalesActivos, tvAnimalesSanos, tvAnimalesVendidos, tvAnimalesMuertos;
    private TextView tvVacunasPendientes;
    private CardView cardAnimales, cardRegistroAnimal, cardCalendario, cardGastos, 
                     cardReportes, cardRecomendaciones, cardAlimentacion, cardVacunaAlert,
                     cardRegistroCompras;
    private TextView badgeCalendario;
    private ImageView btnLogout;
    
    private DatabaseHelper dbHelper;
    private AnimalDAO animalDAO;
    private EventoSanitarioDAO eventoDAO;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Inicializar base de datos
        dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        eventoDAO = new EventoSanitarioDAO(dbHelper);
        
        // Inicializar vistas
        tvWelcome = findViewById(R.id.tvWelcome);
        tvAnimalesActivos = findViewById(R.id.tvAnimalesActivos);
        tvAnimalesSanos = findViewById(R.id.tvAnimalesSanos);
        tvAnimalesVendidos = findViewById(R.id.tvAnimalesVendidos);
        tvAnimalesMuertos = findViewById(R.id.tvAnimalesMuertos);
        tvVacunasPendientes = findViewById(R.id.tvVacunasPendientes);
        cardVacunaAlert = findViewById(R.id.cardVacunaAlert);
        badgeCalendario = findViewById(R.id.badgeCalendario);
        btnLogout = findViewById(R.id.btnLogout);
        
        cardAnimales = findViewById(R.id.cardAnimales);
        cardRegistroAnimal = findViewById(R.id.cardRegistroAnimal);
        cardCalendario = findViewById(R.id.cardCalendario);
        cardGastos = findViewById(R.id.cardGastos);
        cardReportes = findViewById(R.id.cardReportes);
        cardRecomendaciones = findViewById(R.id.cardRecomendaciones);
        cardAlimentacion = findViewById(R.id.cardAlimentacion);
        cardRegistroCompras = findViewById(R.id.cardRegistroCompras);
        
        // Obtener nombre de usuario
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "Usuario");
        tvWelcome.setText(userName);
        
        // Cargar estadísticas
        cargarEstadisticas();
        
        // Configurar listeners
        btnLogout.setOnClickListener(v -> cerrarSesion());
        
        cardAnimales.setOnClickListener(v -> {
            startActivity(new Intent(this, GestionAnimalesActivity.class));
        });
        
        cardRegistroAnimal.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroAnimalActivity.class);
            intent.putExtra("modo", "nuevo");
            startActivity(intent);
        });
        
        cardCalendario.setOnClickListener(v -> {
            startActivity(new Intent(this, CalendarioActivity.class));
        });
        
        cardGastos.setOnClickListener(v -> {
            startActivity(new Intent(this, GastosActivity.class));
        });
        
        cardAlimentacion.setOnClickListener(v -> {
            startActivity(new Intent(this, AlimentacionActivity.class));
        });
        
        cardReportes.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportesActivity.class));
        });
        
        cardRecomendaciones.setOnClickListener(v -> {
            startActivity(new Intent(this, RecomendacionesActivity.class));
        });
        
        cardRegistroCompras.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroComprasActivity.class));
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar estadísticas cuando volvemos a la pantalla
        cargarEstadisticas();
    }
    
    private void cargarEstadisticas() {
        executorService.execute(() -> {
            try {
                // Obtener todos los animales
                List<Animal> todosLosAnimales = animalDAO.obtenerTodos();
                
                // Calcular estadísticas
                int activos = 0;
                int sanos = 0;
                int vendidos = 0;
                int muertos = 0;
                
                for (Animal animal : todosLosAnimales) {
                    String estado = animal.getEstado();
                    
                    if (animal.getFechaSalida() != null && !animal.getFechaSalida().isEmpty()) {
                        vendidos++;
                    } else if ("Muerto".equalsIgnoreCase(estado)) {
                        muertos++;
                    } else {
                        activos++;
                        if ("Sano".equalsIgnoreCase(estado)) {
                            sanos++;
                        }
                    }
                }
                
                // Verificar vacunas pendientes
                List<EventoSanitario> eventosPendientes = eventoDAO.obtenerEventosPendientes();
                int vacunasPendientes = 0;
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar hoy = Calendar.getInstance();
                Calendar limiteDias = Calendar.getInstance();
                limiteDias.add(Calendar.DAY_OF_MONTH, 7); // Próximos 7 días
                
                for (EventoSanitario evento : eventosPendientes) {
                    try {
                        Date fechaProgramada = sdf.parse(evento.getFechaProgramada());
                        if (fechaProgramada != null) {
                            Calendar calEvento = Calendar.getInstance();
                            calEvento.setTime(fechaProgramada);
                            
                            if (!calEvento.before(hoy) && !calEvento.after(limiteDias)) {
                                vacunasPendientes++;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                
                // Actualizar UI en el hilo principal
                final int finalActivos = activos;
                final int finalSanos = sanos;
                final int finalVendidos = vendidos;
                final int finalMuertos = muertos;
                final int finalVacunas = vacunasPendientes;
                
                mainHandler.post(() -> {
                    tvAnimalesActivos.setText(String.valueOf(finalActivos));
                    tvAnimalesSanos.setText(String.valueOf(finalSanos));
                    tvAnimalesVendidos.setText(String.valueOf(finalVendidos));
                    tvAnimalesMuertos.setText(String.valueOf(finalMuertos));
                    
                    // Mostrar/ocultar alerta de vacunas
                    if (finalVacunas > 0) {
                        cardVacunaAlert.setVisibility(View.VISIBLE);
                        badgeCalendario.setVisibility(View.VISIBLE);
                        badgeCalendario.setText(String.valueOf(finalVacunas));
                        tvVacunasPendientes.setText("Tienes " + finalVacunas + 
                            (finalVacunas == 1 ? " vacuna próxima" : " vacunas próximas") + " a vencer");
                    } else {
                        cardVacunaAlert.setVisibility(View.GONE);
                        badgeCalendario.setVisibility(View.GONE);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    // Valores por defecto en caso de error
                    tvAnimalesActivos.setText("0");
                    tvAnimalesSanos.setText("0");
                    tvAnimalesVendidos.setText("0");
                    tvAnimalesMuertos.setText("0");
                });
            }
        });
    }
    
    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
