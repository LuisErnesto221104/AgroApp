package com.example.agroapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agroapp.dao.UsuarioDAO;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Usuario;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private DatabaseHelper dbHelper;
    private UsuarioDAO usuarioDAO;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        dbHelper = DatabaseHelper.getInstance(this);
        usuarioDAO = new UsuarioDAO(dbHelper);
        
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        
        // Verificar si ya hay una sesión activa
        SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            irAMainActivity();
        }
        
        btnLogin.setOnClickListener(v -> intentarLogin());
    }
    
    private void intentarLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Usuario usuario = usuarioDAO.validarUsuario(username, password);
        
        if (usuario != null) {
            // Guardar sesión
            SharedPreferences prefs = getSharedPreferences("AgroAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putInt("userId", usuario.getId());
            editor.putString("userName", usuario.getNombre());
            editor.apply();
            
            Toast.makeText(this, "Bienvenido " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
            irAMainActivity();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
