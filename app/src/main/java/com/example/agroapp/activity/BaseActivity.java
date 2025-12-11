package com.example.agroapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agroapp.dao.UsuarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Usuario;

public class BaseActivity extends AppCompatActivity {
    
    private static final String TAG = "BaseActivity";
    private static final String PREFS_NAME = "SessionPrefs";
    private static final String KEY_LAST_ACTIVITY_TIME = "lastActivityTime";
    private static final long SESSION_TIMEOUT = 10000; // 10 segundos en milisegundos
    
    private UsuarioDAO usuarioDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        usuarioDAO = new UsuarioDAO(dbHelper);
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
            
            // Obtener el username del usuario actual de la sesión
            SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);
            
            if (userId == -1) {
                Log.d(TAG, "No hay usuario en sesión");
                volverAlLogin();
                dialog.dismiss();
                return;
            }
            
            // Validar contraseña contra la base de datos
            Usuario usuario = usuarioDAO.obtenerPorId(userId);
            
            if (usuario == null) {
                Log.d(TAG, "Usuario no encontrado en BD");
                Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                volverAlLogin();
                dialog.dismiss();
                return;
            }
            
            Log.d(TAG, "Verificando contraseña para usuario: " + usuario.getUsername());
            
            // Comparar con la contraseña real de la base de datos
            if (password.equals(usuario.getPassword())) {
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
        // Limpiar la sesión al volver al login
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        SharedPreferences sessionPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sessionPrefs.edit().clear().apply();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
