package com.example.agroapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.agroapp.R;
import com.example.agroapp.dao.AnimalDAO;
import com.example.agroapp.dao.GastoDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalleAnimalActivity extends BaseActivity {
    
    private ImageView ivFotoAnimal;
    private TextView tvNombre, tvArete, badgeEstado;
    private TextView tvRaza, tvSexo, tvEdad, tvPesoActual, tvFechaNacimiento, tvFechaIngreso;
    private TextView tvInversionTotal, tvPrecioCompra, tvTotalGastos;
    private LinearLayout layoutGanancia;
    private TextView tvGanancia;
    private CardView cardEventosSanitarios, cardHistorialClinico, cardGastos, cardAlimentacion;
    private CardView btnEditar, btnEliminar, btnRegistrarVenta;
    
    private AnimalDAO animalDAO;
    private GastoDAO gastoDAO;
    private String animalArete;  // Usamos arete como identificador visible
    private Animal animal;
    private NumberFormat currencyFormatter;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_animal);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Animal");
        }
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        animalDAO = new AnimalDAO(dbHelper);
        gastoDAO = new GastoDAO(dbHelper);
        
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale.Builder().setLanguage("es").setRegion("MX").build());
        
        animalArete = getIntent().getStringExtra("arete");
        
        // Performance logging (RNF001)
        long startTime = System.currentTimeMillis();
        
        inicializarVistas();
        cargarDatos();
        configurarListeners();
        
        // Measure loading time
        long loadTime = System.currentTimeMillis() - startTime;
        if (loadTime > 2000) {
            Log.w("DetalleAnimal", "Tiempo de carga alto: " + loadTime + "ms (RNF001 requiere < 2s)");
        }
    }
    
    private void inicializarVistas() {
        // Header
        ivFotoAnimal = findViewById(R.id.ivFotoAnimal);
        tvNombre = findViewById(R.id.tvNombre);
        tvArete = findViewById(R.id.tvArete);
        badgeEstado = findViewById(R.id.badgeEstado);
        
        // Info grid
        tvRaza = findViewById(R.id.tvRaza);
        tvSexo = findViewById(R.id.tvSexo);
        tvEdad = findViewById(R.id.tvEdad);
        tvPesoActual = findViewById(R.id.tvPesoActual);
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento);
        tvFechaIngreso = findViewById(R.id.tvFechaIngreso);
        
        // Investment card
        tvInversionTotal = findViewById(R.id.tvInversionTotal);
        tvPrecioCompra = findViewById(R.id.tvPrecioCompra);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        layoutGanancia = findViewById(R.id.layoutGanancia);
        tvGanancia = findViewById(R.id.tvGanancia);
        
        // Action cards
        cardEventosSanitarios = findViewById(R.id.cardEventosSanitarios);
        cardHistorialClinico = findViewById(R.id.cardHistorialClinico);
        cardGastos = findViewById(R.id.cardGastos);
        cardAlimentacion = findViewById(R.id.cardAlimentacion);
        
        // Buttons
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnRegistrarVenta = findViewById(R.id.btnRegistrarVenta);
    }
    
    private void cargarDatos() {
        animal = animalDAO.obtenerAnimalPorArete(animalArete);
        if (animal != null) {
            // Header
            tvNombre.setText(animal.getNombre() != null ? animal.getNombre() : "Sin nombre");
            tvArete.setText("Arete: " + animal.getNumeroArete());
            
            // Cargar foto si existe
            if (animal.getFoto() != null && !animal.getFoto().isEmpty()) {
                try {
                    byte[] decodedString = android.util.Base64.decode(animal.getFoto(), android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(
                        decodedString, 0, decodedString.length);
                    ivFotoAnimal.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Mantener imagen por defecto si hay error
                }
            }
            
            // Badge estado
            String estado = animal.getEstado() != null ? animal.getEstado() : "Activo";
            badgeEstado.setText(estado);
            configurarBadgeEstado(estado);
            
            // Info grid
            tvRaza.setText(animal.getRaza() != null ? animal.getRaza() : "-");
            tvSexo.setText(animal.getSexo() != null ? animal.getSexo() : "-");
            tvEdad.setText(calcularEdad(animal.getFechaNacimiento()));
            tvPesoActual.setText("0 kg"); // Por ahora sin peso
            tvFechaNacimiento.setText(animal.getFechaNacimiento() != null ? animal.getFechaNacimiento() : "-");
            tvFechaIngreso.setText(animal.getFechaIngreso() != null ? animal.getFechaIngreso() : "-");
            
            // Investment calculation (RD004)
            // Formula: Inversión Total = Precio de Compra + Total de Gastos
            // donde Total de Gastos incluye: alimentación, veterinario, medicinas, mantenimiento, etc.
            double precioCompra = animal.getPrecioCompra();
            double totalGastos = gastoDAO.obtenerTotalGastosPorAnimal(animalId);
            double inversionTotal = precioCompra + totalGastos;
            
            tvInversionTotal.setText(currencyFormatter.format(inversionTotal));
            tvPrecioCompra.setText("Compra: " + currencyFormatter.format(precioCompra));
            tvTotalGastos.setText("Gastos: " + currencyFormatter.format(totalGastos));
            
            // Ganancia (solo si está vendido)
            if (animal.getFechaSalida() != null && animal.getPrecioVenta() > 0) {
                double ganancia = animal.getPrecioVenta() - inversionTotal;
                layoutGanancia.setVisibility(View.VISIBLE);
                tvGanancia.setText((ganancia >= 0 ? "Ganancia: " : "Pérdida: ") + 
                        currencyFormatter.format(Math.abs(ganancia)));
                tvGanancia.setTextColor(ganancia >= 0 ? Color.parseColor("#16a34a") : Color.parseColor("#dc2626"));
                btnRegistrarVenta.setVisibility(View.GONE);
            } else {
                layoutGanancia.setVisibility(View.GONE);
                btnRegistrarVenta.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void configurarBadgeEstado(String estado) {
        int color;
        switch (estado.toLowerCase()) {
            case "sano":
                color = Color.parseColor("#16a34a"); // Verde
                break;
            case "enfermo":
                color = Color.parseColor("#dc2626"); // Rojo
                break;
            case "vendido":
                color = Color.parseColor("#7e22ce"); // Morado
                break;
            case "muerto":
                color = Color.parseColor("#6b7280"); // Gris
                break;
            default:
                color = Color.parseColor("#16a34a"); // Verde por defecto
        }
        badgeEstado.setBackgroundColor(color);
    }
    
    private String calcularEdad(String fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isEmpty()) {
            return "-";
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaNac = sdf.parse(fechaNacimiento);
            Calendar nacimiento = Calendar.getInstance();
            nacimiento.setTime(fechaNac);
            
            Calendar hoy = Calendar.getInstance();
            int años = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
            int meses = hoy.get(Calendar.MONTH) - nacimiento.get(Calendar.MONTH);
            
            if (meses < 0) {
                años--;
                meses += 12;
            }
            
            if (años > 0) {
                return años + (años == 1 ? " año" : " años");
            } else {
                return meses + (meses == 1 ? " mes" : " meses");
            }
        } catch (ParseException e) {
            return "-";
        }
    }
    
    private void configurarListeners() {
        // Verificar si el animal está vendido o muerto
        boolean bloqueado = (animal.getEstado() != null && 
            (animal.getEstado().equalsIgnoreCase("vendido") || animal.getEstado().equalsIgnoreCase("muerto")));
        
        if (bloqueado) {
            // Deshabilitar y oscurecer las tarjetas bloqueadas
            cardEventosSanitarios.setEnabled(false);
            cardEventosSanitarios.setAlpha(0.5f);
            cardHistorialClinico.setEnabled(false);
            cardHistorialClinico.setAlpha(0.5f);
            cardGastos.setEnabled(false);
            cardGastos.setAlpha(0.5f);
            cardAlimentacion.setEnabled(false);
            cardAlimentacion.setAlpha(0.5f);
            
            // Mostrar mensaje al intentar acceder
            View.OnClickListener mensajeBloqueado = v -> 
                Toast.makeText(this, "No se pueden realizar acciones en un animal " + animal.getEstado().toLowerCase(), 
                    Toast.LENGTH_SHORT).show();
            
            cardEventosSanitarios.setOnClickListener(mensajeBloqueado);
            cardHistorialClinico.setOnClickListener(mensajeBloqueado);
            cardGastos.setOnClickListener(mensajeBloqueado);
            cardAlimentacion.setOnClickListener(mensajeBloqueado);
            
            // Ocultar botón de editar y registrar venta si está muerto o vendido
            btnEditar.setVisibility(View.GONE);
            btnRegistrarVenta.setVisibility(View.GONE);
        } else {
            // Configuración normal de listeners
            cardEventosSanitarios.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventosSanitariosActivity.class);
                intent.putExtra("arete", animalArete);
                startActivity(intent);
            });
            
            cardHistorialClinico.setOnClickListener(v -> {
                Intent intent = new Intent(this, HistorialClinicoActivity.class);
                intent.putExtra("arete", animalArete);
                startActivity(intent);
            });
            
            cardGastos.setOnClickListener(v -> {
                Intent intent = new Intent(this, GastosActivity.class);
                intent.putExtra("arete", animalArete);
                startActivity(intent);
            });
            
            cardAlimentacion.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlimentacionActivity.class);
                intent.putExtra("arete", animalArete);
                startActivity(intent);
            });
        }
        
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroAnimalActivity.class);
            intent.putExtra("modo", "editar");
            intent.putExtra("arete", animalArete);
            startActivity(intent);
        });
        
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        
        btnRegistrarVenta.setOnClickListener(v -> {
            mostrarDialogoRegistrarVenta();
        });
    }
    
    private void confirmarEliminacion() {
        // Obtener el nombre del animal para el mensaje personalizado
        Animal animal = animalDAO.obtenerAnimalPorArete(animalArete);
        String nombreAnimal = animal != null ? animal.getNombre() : "este animal";
        
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Eliminación")
            .setMessage("¿Está seguro de eliminar a " + nombreAnimal + "?\n\n" +
                        "⚠️ ADVERTENCIA: Esta acción eliminará permanentemente:\n" +
                        "• Todos los eventos sanitarios\n" +
                        "• Todo el historial clínico\n" +
                        "• Todos los registros de alimentación\n" +
                        "• Todos los gastos asociados\n\n" +
                        "Esta acción NO se puede deshacer.")
            .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                animalDAO.eliminarAnimalPorArete(animalArete);
                Toast.makeText(this, "Animal eliminado exitosamente", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void mostrarDialogoRegistrarVenta() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registrar_venta, null);
        
        EditText etPrecioVenta = dialogView.findViewById(R.id.etPrecioVenta);
        android.widget.Button btnFechaVenta = dialogView.findViewById(R.id.btnFechaVenta);
        
        final String[] fechaVenta = {new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date())};
        btnFechaVenta.setText(fechaVenta[0]);
        
        btnFechaVenta.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                fechaVenta[0] = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                btnFechaVenta.setText(fechaVenta[0]);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        
        builder.setView(dialogView)
            .setTitle("Registrar Venta")
            .setPositiveButton("Guardar", (dialog, which) -> {
                String precioStr = etPrecioVenta.getText().toString().trim();
                if (precioStr.isEmpty()) {
                    Toast.makeText(this, "Ingrese el precio de venta", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double precioVenta = Double.parseDouble(precioStr);
                
                executorService.execute(() -> {
                    Animal animal = animalDAO.obtenerAnimalPorArete(animalArete);
                    if (animal != null) {
                        animal.setEstado("Vendido");
                        animal.setFechaSalida(fechaVenta[0]);
                        animal.setPrecioVenta(precioVenta);
                        animalDAO.actualizarAnimal(animal);
                        
                        double ganancia = precioVenta - animal.getPrecioCompra();
                        
                        mainHandler.post(() -> {
                            String mensaje = String.format(Locale.getDefault(),
                                "Animal vendido\nPrecio compra: $%.2f\nPrecio venta: $%.2f\n%s: $%.2f",
                                animal.getPrecioCompra(), precioVenta,
                                ganancia >= 0 ? "Ganancia" : "Pérdida",
                                Math.abs(ganancia));
                            
                            new android.app.AlertDialog.Builder(this)
                                .setTitle("Venta Registrada")
                                .setMessage(mensaje)
                                .setPositiveButton("OK", (d, w) -> finish())
                                .show();
                        });
                    }
                });
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
