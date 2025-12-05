package com.example.agroapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.Animal;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {
    private DatabaseHelper dbHelper;
    
    public AnimalDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public boolean existeArete(String numeroArete) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            new String[]{DatabaseHelper.COL_ANIMAL_ID},
            DatabaseHelper.COL_ANIMAL_ARETE + "=?",
            new String[]{numeroArete},
            null, null, null
        );
        
        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return existe;
    }
    
    public long insertarAnimal(Animal animal) {
        // Verificar si ya existe un animal con el mismo arete
        if (existeArete(animal.getNumeroArete())) {
            return -1;
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_ANIMAL_ARETE, animal.getNumeroArete());
        values.put(DatabaseHelper.COL_ANIMAL_NOMBRE, animal.getNombre());
        values.put(DatabaseHelper.COL_ANIMAL_RAZA, animal.getRaza());
        values.put(DatabaseHelper.COL_ANIMAL_SEXO, animal.getSexo());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_NACIMIENTO, animal.getFechaNacimiento());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_INGRESO, animal.getFechaIngreso());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_SALIDA, animal.getFechaSalida());
        values.put(DatabaseHelper.COL_ANIMAL_PRECIO_COMPRA, animal.getPrecioCompra());
        values.put(DatabaseHelper.COL_ANIMAL_PRECIO_VENTA, animal.getPrecioVenta());
        values.put(DatabaseHelper.COL_ANIMAL_FOTO, animal.getFoto());
        values.put(DatabaseHelper.COL_ANIMAL_ESTADO, animal.getEstado());
        values.put(DatabaseHelper.COL_ANIMAL_OBSERVACIONES, animal.getObservaciones());
        
        return db.insert(DatabaseHelper.TABLE_ANIMALES, null, values);
    }
    
    public int actualizarAnimal(Animal animal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_ANIMAL_ARETE, animal.getNumeroArete());
        values.put(DatabaseHelper.COL_ANIMAL_NOMBRE, animal.getNombre());
        values.put(DatabaseHelper.COL_ANIMAL_RAZA, animal.getRaza());
        values.put(DatabaseHelper.COL_ANIMAL_SEXO, animal.getSexo());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_NACIMIENTO, animal.getFechaNacimiento());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_INGRESO, animal.getFechaIngreso());
        values.put(DatabaseHelper.COL_ANIMAL_FECHA_SALIDA, animal.getFechaSalida());
        values.put(DatabaseHelper.COL_ANIMAL_PRECIO_COMPRA, animal.getPrecioCompra());
        values.put(DatabaseHelper.COL_ANIMAL_PRECIO_VENTA, animal.getPrecioVenta());
        values.put(DatabaseHelper.COL_ANIMAL_FOTO, animal.getFoto());
        values.put(DatabaseHelper.COL_ANIMAL_ESTADO, animal.getEstado());
        values.put(DatabaseHelper.COL_ANIMAL_OBSERVACIONES, animal.getObservaciones());
        
        return db.update(DatabaseHelper.TABLE_ANIMALES, values,
            DatabaseHelper.COL_ANIMAL_ID + "=?",
            new String[]{String.valueOf(animal.getId())});
    }
    
    public int eliminarAnimal(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ANIMALES,
            DatabaseHelper.COL_ANIMAL_ID + "=?",
            new String[]{String.valueOf(id)});
    }
    
    public Animal obtenerAnimalPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Animal animal = null;
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            null,
            DatabaseHelper.COL_ANIMAL_ID + "=?",
            new String[]{String.valueOf(id)},
            null, null, null
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            animal = cursorToAnimal(cursor);
            cursor.close();
        }
        
        return animal;
    }
    
    public List<Animal> obtenerTodosLosAnimales() {
        List<Animal> animales = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            null, null, null, null, null,
            DatabaseHelper.COL_ANIMAL_NOMBRE + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                animales.add(cursorToAnimal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return animales;
    }
    
    // Alias para obtenerTodosLosAnimales
    public List<Animal> obtenerTodos() {
        return obtenerTodosLosAnimales();
    }
    
    public List<Animal> obtenerAnimalesPorEstado(String estado) {
        List<Animal> animales = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            null,
            DatabaseHelper.COL_ANIMAL_ESTADO + "=?",
            new String[]{estado},
            null, null,
            DatabaseHelper.COL_ANIMAL_NOMBRE + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                animales.add(cursorToAnimal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return animales;
    }
    
    private Animal cursorToAnimal(Cursor cursor) {
        return new Animal(
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_ARETE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_NOMBRE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_RAZA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_SEXO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_FECHA_NACIMIENTO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_FECHA_INGRESO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_FECHA_SALIDA)),
            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_PRECIO_COMPRA)),
            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_PRECIO_VENTA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_FOTO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_ESTADO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_OBSERVACIONES))
        );
    }
}
