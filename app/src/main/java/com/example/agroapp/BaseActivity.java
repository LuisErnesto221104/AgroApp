package com.example.agroapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    
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
        builder.setView(etPassword);
        
        builder.setPositiveButton("Ingresar", (dialog, which) -> {
            String password = etPassword.getText().toString();
            verificarContraseña(password);
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // Volver al login
            volverAlLogin();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void verificarContraseña(String password) {
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        String passwordGuardada = prefs.getString("password", "");
        
        if (password.equals(passwordGuardada)) {
            // Contraseña correcta, actualizar tiempo y continuar
            guardarTiempoActividad();
            Toast.makeText(this, "Sesión reanudada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            volverAlLogin();
        }
    }
    
    private void volverAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
