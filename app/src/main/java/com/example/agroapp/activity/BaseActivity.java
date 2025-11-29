package com.example.agroapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    
    private static final String TAG = "BaseActivity";
    private static final String PREFS_NAME = "SessionPrefs";
    private static final String KEY_LAST_ACTIVITY_TIME = "lastActivityTime";
    private static final long SESSION_TIMEOUT = 10000; // 10 segundos en milisegundos
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        verificarSesion();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        guardarTiempoActividad();
    }
    
    private void verificarSesion() {
        // No verificar en LoginActivity

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long ultimaActividad = prefs.getLong(KEY_LAST_ACTIVITY_TIME, 0);
        long tiempoActual = System.currentTimeMillis();
        
        // Si han pasado más de 10 segundos, mostrar diálogo de contraseña
        if (ultimaActividad > 0 && (tiempoActual - ultimaActividad) > SESSION_TIMEOUT) {
            mostrarDialogoContraseña();
        }
    }
    
    private void guardarTiempoActividad() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis()).apply();
    }
    
    private void mostrarDialogoContraseña() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sesión Expirada");
        builder.setMessage("Por seguridad, ingresa tu contraseña nuevamente:");
        builder.setCancelable(false);
        
        EditText etPassword = new EditText(this);
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                               android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassword.setHint("Contraseña");
        etPassword.setPadding(50, 30, 50, 30);
        builder.setView(etPassword);
        
        builder.setPositiveButton("Ingresar", null); // Se configura después para prevenir cierre automático
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Volver al login
            dialog.dismiss();
            volverAlLogin();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Configurar botón Ingresar para no cerrar automáticamente
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            
            Log.d(TAG, "Intentando validar contraseña de sesión");
            
            if (password.isEmpty()) {
                Log.d(TAG, "Contraseña vacía");
                Toast.makeText(this, "Ingrese su contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            
            SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
            String passwordGuardada = prefs.getString("password", "");
            
            Log.d(TAG, "Contraseña guardada existe: " + (!passwordGuardada.isEmpty()));
            Log.d(TAG, "Longitud contraseña ingresada: " + password.length());
            Log.d(TAG, "Longitud contraseña guardada: " + passwordGuardada.length());
            
            if (password.equals(passwordGuardada)) {
                // Contraseña correcta, actualizar tiempo y continuar
                Log.d(TAG, "Contraseña correcta - reanudando sesión");
                guardarTiempoActividad();
                Toast.makeText(this, "Sesión reanudada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Log.d(TAG, "Contraseña incorrecta");
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                etPassword.setText("");
                etPassword.requestFocus();
            }
        });
    }
    
    private void volverAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
