package com.example.agroapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.agroapp.dao.UsuarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Usuario;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText etUsuario, etPassword;
    private CardView btnLogin, btnRegistrar;
    private DatabaseHelper dbHelper;
    private UsuarioDAO usuarioDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        dbHelper = DatabaseHelper.getInstance(this);
        usuarioDAO = new UsuarioDAO(dbHelper);
        
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        
        // Verificar si ya hay una sesión activa
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            irAMainActivity();
        }
        
        btnLogin.setOnClickListener(v -> iniciarSesion());
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }
    
    private void iniciarSesion() {
        String username = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validar que el usuario existe en la BD
        Usuario usuario = usuarioDAO.validarUsuario(username, password);
        
        if (usuario != null) {
            // Usuario existe y credenciales correctas
            guardarSesion(usuario, password);
            Toast.makeText(this, "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
            irAMainActivity();
        } else {
            // Verificar si el usuario existe pero la contraseña es incorrecta
            Usuario usuarioExistente = usuarioDAO.obtenerPorUsername(username);
            
            if (usuarioExistente != null) {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "El usuario no existe. Use 'Registrar Usuario' para crear una cuenta", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void registrarUsuario() {
        String username = etUsuario.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verificar si ya existe algún usuario en el sistema
        if (usuarioDAO.existeAlgunUsuario()) {
            Toast.makeText(this, "Ya existe un usuario registrado. Solo se permite un usuario en el sistema.", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Verificar si el usuario ya existe (redundante pero por seguridad)
        Usuario usuarioExistente = usuarioDAO.obtenerPorUsername(username);
        
        if (usuarioExistente != null) {
            Toast.makeText(this, "El usuario ya existe. Use 'Iniciar Sesión' si ya tiene cuenta", 
                    Toast.LENGTH_LONG).show();
            return;
        }
        
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setNombre(username);
        
        long id = usuarioDAO.insertar(nuevoUsuario);
        
        if (id > 0) {
            nuevoUsuario.setId((int) id);
            guardarSesion(nuevoUsuario, password);
            Toast.makeText(this, "¡Cuenta creada exitosamente! Bienvenido " + nuevoUsuario.getNombre(), 
                    Toast.LENGTH_LONG).show();
            irAMainActivity();
        } else {
            Toast.makeText(this, "Error al crear la cuenta. Intente nuevamente", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void guardarSesion(Usuario usuario, String password) {
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", usuario.getId());
        editor.putString("userName", usuario.getNombre());
        editor.putString("password", password);
        editor.apply();
    }
    
    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
