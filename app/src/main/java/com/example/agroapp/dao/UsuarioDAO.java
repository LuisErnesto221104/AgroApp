package com.example.agroapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Usuario;

public class UsuarioDAO {
    private DatabaseHelper dbHelper;
    
    public UsuarioDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public Usuario validarUsuario(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Usuario usuario = null;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            DatabaseHelper.COL_USUARIO_USERNAME + "=? AND " + DatabaseHelper.COL_USUARIO_PASSWORD + "=?",
            new String[]{username, password},
            null, null, null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE))
            );
            cursor.close();
        }
        
        return usuario;
    }
    
    public Usuario obtenerPorUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Usuario usuario = null;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            DatabaseHelper.COL_USUARIO_USERNAME + "=?",
            new String[]{username},
            null, null, null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            usuario = new Usuario(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USUARIO_NOMBRE))
            );
            cursor.close();
        }
        
        return usuario;
    }
    
    public long insertar(Usuario usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_USUARIO_USERNAME, usuario.getUsername());
        values.put(DatabaseHelper.COL_USUARIO_PASSWORD, usuario.getPassword());
        values.put(DatabaseHelper.COL_USUARIO_NOMBRE, usuario.getNombre());
        
        return db.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
    }
    
    public long insertarUsuario(Usuario usuario) {
        return insertar(usuario);
    }
    
    public boolean existeAlgunUsuario() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USUARIOS, null);
        
        boolean existe = false;
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            existe = count > 0;
            cursor.close();
        }
        
        return existe;
    }
}
