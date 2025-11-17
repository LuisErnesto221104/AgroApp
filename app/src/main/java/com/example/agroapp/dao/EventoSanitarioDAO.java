package com.example.agroapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agroapp.database.DatabaseHelper;
import com.example.agroapp.models.EventoSanitario;
import java.util.ArrayList;
import java.util.List;

public class EventoSanitarioDAO {
    private DatabaseHelper dbHelper;
    
    public EventoSanitarioDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    public long insertarEvento(EventoSanitario evento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_CALENDARIO_ANIMAL_ID, evento.getAnimalId());
        values.put(DatabaseHelper.COL_CALENDARIO_TIPO, evento.getTipo());
        values.put(DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA, evento.getFechaProgramada());
        values.put(DatabaseHelper.COL_CALENDARIO_FECHA_REALIZADA, evento.getFechaRealizada());
        values.put(DatabaseHelper.COL_CALENDARIO_DESCRIPCION, evento.getDescripcion());
        values.put(DatabaseHelper.COL_CALENDARIO_RECORDATORIO, evento.getRecordatorio());
        values.put(DatabaseHelper.COL_CALENDARIO_ESTADO, evento.getEstado());
        
        return db.insert(DatabaseHelper.TABLE_CALENDARIO_SANITARIO, null, values);
    }
    
    public int actualizarEvento(EventoSanitario evento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_CALENDARIO_ANIMAL_ID, evento.getAnimalId());
        values.put(DatabaseHelper.COL_CALENDARIO_TIPO, evento.getTipo());
        values.put(DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA, evento.getFechaProgramada());
        values.put(DatabaseHelper.COL_CALENDARIO_FECHA_REALIZADA, evento.getFechaRealizada());
        values.put(DatabaseHelper.COL_CALENDARIO_DESCRIPCION, evento.getDescripcion());
        values.put(DatabaseHelper.COL_CALENDARIO_RECORDATORIO, evento.getRecordatorio());
        values.put(DatabaseHelper.COL_CALENDARIO_ESTADO, evento.getEstado());
        
        return db.update(DatabaseHelper.TABLE_CALENDARIO_SANITARIO, values,
            DatabaseHelper.COL_CALENDARIO_ID + "=?",
            new String[]{String.valueOf(evento.getId())});
    }
    
    public int eliminarEvento(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CALENDARIO_SANITARIO,
            DatabaseHelper.COL_CALENDARIO_ID + "=?",
            new String[]{String.valueOf(id)});
    }
    
    public List<EventoSanitario> obtenerTodosLosEventos() {
        List<EventoSanitario> eventos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_CALENDARIO_SANITARIO,
            null, null, null, null, null,
            DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventos.add(cursorToEvento(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return eventos;
    }
    
    public List<EventoSanitario> obtenerEventosPorAnimal(int animalId) {
        List<EventoSanitario> eventos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_CALENDARIO_SANITARIO,
            null,
            DatabaseHelper.COL_CALENDARIO_ANIMAL_ID + "=?",
            new String[]{String.valueOf(animalId)},
            null, null,
            DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventos.add(cursorToEvento(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return eventos;
    }
    
    public List<EventoSanitario> obtenerEventosPendientes() {
        List<EventoSanitario> eventos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_CALENDARIO_SANITARIO,
            null,
            DatabaseHelper.COL_CALENDARIO_ESTADO + "=?",
            new String[]{"Pendiente"},
            null, null,
            DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventos.add(cursorToEvento(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return eventos;
    }
    
    private EventoSanitario cursorToEvento(Cursor cursor) {
        return new EventoSanitario(
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_ID)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_ANIMAL_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_TIPO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_FECHA_PROGRAMADA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_FECHA_REALIZADA)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_DESCRIPCION)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_RECORDATORIO)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CALENDARIO_ESTADO))
        );
    }
}
