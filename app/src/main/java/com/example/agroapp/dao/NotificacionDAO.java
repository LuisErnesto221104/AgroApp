package com.example.agroapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.agroapp.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar el tracking de notificaciones enviadas (RF009).
 * Permite auditoría de las 3 notificaciones programadas por evento.
 */
public class NotificacionDAO {
    
    private DatabaseHelper dbHelper;
    
    // Estados de notificación
    public static final String ESTADO_PROGRAMADA = "programada";
    public static final String ESTADO_ENVIADA = "enviada";
    public static final String ESTADO_CANCELADA = "cancelada";
    
    public NotificacionDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    /**
     * Registra una notificación programada para un evento.
     * @param eventoId ID del evento
     * @param tipoNotificacion Tipo (0=3 días, 1=1 día, 2=mismo día)
     * @param fechaProgramada Timestamp en milisegundos de cuando se enviará
     * @return ID de la notificación registrada o -1 si hay error
     */
    public long registrarNotificacionProgramada(int eventoId, int tipoNotificacion, long fechaProgramada) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_NOTIFICACION_EVENTO_ID, eventoId);
        values.put(DatabaseHelper.COL_NOTIFICACION_TIPO, tipoNotificacion);
        values.put(DatabaseHelper.COL_NOTIFICACION_FECHA_PROGRAMADA, fechaProgramada);
        values.put(DatabaseHelper.COL_NOTIFICACION_ESTADO, ESTADO_PROGRAMADA);
        
        // Usar INSERT OR REPLACE para evitar duplicados
        return db.insertWithOnConflict(DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS, 
            null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    /**
     * Marca una notificación como enviada.
     * @param eventoId ID del evento
     * @param tipoNotificacion Tipo de notificación
     * @return Número de filas actualizadas
     */
    public int marcarNotificacionEnviada(int eventoId, int tipoNotificacion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_NOTIFICACION_FECHA_ENVIADA, System.currentTimeMillis());
        values.put(DatabaseHelper.COL_NOTIFICACION_ESTADO, ESTADO_ENVIADA);
        
        return db.update(DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS, values,
            DatabaseHelper.COL_NOTIFICACION_EVENTO_ID + "=? AND " + 
            DatabaseHelper.COL_NOTIFICACION_TIPO + "=?",
            new String[]{String.valueOf(eventoId), String.valueOf(tipoNotificacion)});
    }
    
    /**
     * Marca una notificación como enviada, creando el registro si no existe.
     * Este método es más robusto ya que maneja el caso donde la notificación
     * se dispara sin haber sido registrada previamente.
     * @param eventoId ID del evento
     * @param tipoNotificacion Tipo de notificación
     * @return ID del registro o número de filas actualizadas
     */
    public long marcarNotificacionEnviadaConInsert(int eventoId, int tipoNotificacion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_NOTIFICACION_EVENTO_ID, eventoId);
        values.put(DatabaseHelper.COL_NOTIFICACION_TIPO, tipoNotificacion);
        values.put(DatabaseHelper.COL_NOTIFICACION_FECHA_ENVIADA, System.currentTimeMillis());
        values.put(DatabaseHelper.COL_NOTIFICACION_ESTADO, ESTADO_ENVIADA);
        
        // Usar INSERT OR REPLACE para crear o actualizar el registro
        return db.insertWithOnConflict(DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS, 
            null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
    
    /**
     * Cancela todas las notificaciones programadas para un evento.
     * @param eventoId ID del evento
     * @return Número de notificaciones canceladas
     */
    public int cancelarNotificacionesEvento(int eventoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COL_NOTIFICACION_ESTADO, ESTADO_CANCELADA);
        
        return db.update(DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS, values,
            DatabaseHelper.COL_NOTIFICACION_EVENTO_ID + "=? AND " + 
            DatabaseHelper.COL_NOTIFICACION_ESTADO + "=?",
            new String[]{String.valueOf(eventoId), ESTADO_PROGRAMADA});
    }
    
    /**
     * Elimina todos los registros de notificaciones para un evento.
     * @param eventoId ID del evento
     * @return Número de registros eliminados
     */
    public int eliminarNotificacionesEvento(int eventoId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS,
            DatabaseHelper.COL_NOTIFICACION_EVENTO_ID + "=?",
            new String[]{String.valueOf(eventoId)});
    }
    
    /**
     * Verifica si una notificación específica ya fue enviada.
     * @param eventoId ID del evento
     * @param tipoNotificacion Tipo de notificación
     * @return true si ya fue enviada, false en caso contrario
     */
    public boolean notificacionYaEnviada(int eventoId, int tipoNotificacion) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS,
            new String[]{DatabaseHelper.COL_NOTIFICACION_ESTADO},
            DatabaseHelper.COL_NOTIFICACION_EVENTO_ID + "=? AND " + 
            DatabaseHelper.COL_NOTIFICACION_TIPO + "=?",
            new String[]{String.valueOf(eventoId), String.valueOf(tipoNotificacion)},
            null, null, null
        );
        
        boolean enviada = false;
        if (cursor != null && cursor.moveToFirst()) {
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_ESTADO));
            enviada = ESTADO_ENVIADA.equals(estado);
            cursor.close();
        }
        
        return enviada;
    }
    
    /**
     * Obtiene todas las notificaciones programadas pendientes de enviar.
     * @return Lista de arrays [eventoId, tipoNotificacion, fechaProgramada]
     */
    public List<long[]> obtenerNotificacionesPendientes() {
        List<long[]> notificaciones = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS,
            new String[]{
                DatabaseHelper.COL_NOTIFICACION_EVENTO_ID,
                DatabaseHelper.COL_NOTIFICACION_TIPO,
                DatabaseHelper.COL_NOTIFICACION_FECHA_PROGRAMADA
            },
            DatabaseHelper.COL_NOTIFICACION_ESTADO + "=?",
            new String[]{ESTADO_PROGRAMADA},
            null, null,
            DatabaseHelper.COL_NOTIFICACION_FECHA_PROGRAMADA + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long[] notif = new long[3];
                notif[0] = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_EVENTO_ID));
                notif[1] = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_TIPO));
                notif[2] = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_FECHA_PROGRAMADA));
                notificaciones.add(notif);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return notificaciones;
    }
    
    /**
     * Obtiene el estado de las notificaciones de un evento para auditoría.
     * @param eventoId ID del evento
     * @return Lista de arrays [tipoNotificacion, estado, fechaProgramada, fechaEnviada]
     */
    public List<Object[]> obtenerEstadoNotificacionesEvento(int eventoId) {
        List<Object[]> estados = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
            DatabaseHelper.TABLE_NOTIFICACIONES_ENVIADAS,
            null,
            DatabaseHelper.COL_NOTIFICACION_EVENTO_ID + "=?",
            new String[]{String.valueOf(eventoId)},
            null, null,
            DatabaseHelper.COL_NOTIFICACION_TIPO + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Object[] estado = new Object[4];
                estado[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_TIPO));
                estado[1] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_ESTADO));
                estado[2] = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_FECHA_PROGRAMADA));
                estado[3] = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICACION_FECHA_ENVIADA));
                estados.add(estado);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return estados;
    }
}
