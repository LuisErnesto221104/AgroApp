package com.example.agroapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.agroapp.activity.CalendarioActivity;
import com.example.agroapp.R;
import com.example.agroapp.dao.NotificacionDAO;
import com.example.agroapp.database.DatabaseHelper;

/**
 * BroadcastReceiver para manejar las notificaciones de eventos sanitarios.
 * Implementa RF009: Recibe y muestra las 3 notificaciones por evento.
 */
public class NotificationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "agroapp_channel";
    private static final String CHANNEL_NAME = "AgroApp Recordatorios";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String titulo = intent.getStringExtra("titulo");
        String mensaje = intent.getStringExtra("mensaje");
        int eventoId = intent.getIntExtra("eventoId", 0);
        int tipoNotificacion = intent.getIntExtra("tipoNotificacion", -1);
        String fechaEvento = intent.getStringExtra("fechaEvento");
        
        Log.d(TAG, "Recibida notificación: eventoId=" + eventoId + 
            ", tipo=" + tipoNotificacion + ", fecha=" + fechaEvento);
        
        // Verificar si la notificación ya fue enviada (evitar duplicados)
        if (!verificarYRegistrarNotificacion(context, eventoId, tipoNotificacion)) {
            Log.d(TAG, "Notificación ya fue enviada anteriormente, omitiendo");
            return;
        }
        
        crearCanalNotificacion(context);
        
        // Generar un ID único para cada notificación combinando eventoId y tipo
        int notificationId = generarNotificationId(eventoId, tipoNotificacion);
        
        mostrarNotificacion(context, titulo, mensaje, eventoId, notificationId);
    }
    
    /**
     * Verifica si la notificación ya fue enviada y la registra como enviada.
     * @return true si se puede enviar, false si ya fue enviada
     */
    private boolean verificarYRegistrarNotificacion(Context context, int eventoId, int tipoNotificacion) {
        if (tipoNotificacion < 0) {
            // Notificación sin tipo (compatibilidad con versiones anteriores)
            return true;
        }
        
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            NotificacionDAO notificacionDAO = new NotificacionDAO(dbHelper);
            
            // Verificar si ya fue enviada
            if (notificacionDAO.notificacionYaEnviada(eventoId, tipoNotificacion)) {
                return false;
            }
            
            // Marcar como enviada (usa INSERT OR REPLACE para manejar caso de registro inexistente)
            notificacionDAO.marcarNotificacionEnviadaConInsert(eventoId, tipoNotificacion);
            return true;
        } catch (android.database.sqlite.SQLiteException e) {
            Log.e(TAG, "Error de base de datos al verificar/registrar notificación", e);
            // En caso de error de BD, permitir enviar la notificación
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al verificar/registrar notificación", e);
            // En caso de error, permitir enviar la notificación
            return true;
        }
    }
    
    /**
     * Genera un ID único para cada notificación basado en eventoId y tipo.
     * Utiliza el mismo patrón de offset que los request codes en NotificationHelper.
     * 
     * Nota: Para event IDs muy grandes (> Integer.MAX_VALUE - 20000), podría
     * producir overflow, pero esto es prácticamente imposible con SQLite
     * auto-increment en uso normal de la aplicación.
     */
    private int generarNotificationId(int eventoId, int tipoNotificacion) {
        if (tipoNotificacion < 0) {
            return eventoId;
        }
        // Usar mismos offsets que NotificationHelper para evitar colisiones
        // 0=3 días antes, 10000=1 día antes, 20000=mismo día
        switch (tipoNotificacion) {
            case NotificationHelper.NOTIFICATION_TYPE_3_DAYS:
                return eventoId;
            case NotificationHelper.NOTIFICATION_TYPE_1_DAY:
                return eventoId + 10000;
            case NotificationHelper.NOTIFICATION_TYPE_SAME_DAY:
                return eventoId + 20000;
            default:
                return eventoId;
        }
    }
    
    private void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones de eventos sanitarios (RF009)");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void mostrarNotificacion(Context context, String titulo, String mensaje, 
            int eventoId, int notificationId) {
        Intent intent = new Intent(context, CalendarioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("eventoId", eventoId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notificación mostrada con ID: " + notificationId);
        }
    }
}
