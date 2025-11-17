package com.example.agroapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.agroapp.models.EventoSanitario;

import java.util.Calendar;

public class NotificationHelper {

    /**
     * Programa una notificación para un evento sanitario
     * @param context Contexto de la aplicación
     * @param evento EventoSanitario para el cual programar la notificación
     */
    public static void programarNotificacion(Context context, EventoSanitario evento) {
        if (!evento.isRecordatorio()) {
            return; // No programar si el recordatorio está desactivado
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("evento_id", evento.getId());
        intent.putExtra("tipo", evento.getTipo());
        intent.putExtra("descripcion", evento.getDescripcion());
        
        // Usar FLAG_IMMUTABLE para Android 12+
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            evento.getId(), // Usar el ID del evento como request code
            intent,
            flags
        );

        // Programar la alarma para 1 día antes del evento a las 9:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(evento.getFechaEvento());
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Un día antes
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        // Solo programar si la fecha está en el futuro
        if (triggerTime > currentTime) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Para Android 6.0+, usar setExactAndAllowWhileIdle para mayor precisión
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            }
        }
    }

    /**
     * Cancela una notificación programada para un evento
     * @param context Contexto de la aplicación
     * @param eventoId ID del evento sanitario
     */
    public static void cancelarNotificacion(Context context, int eventoId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            eventoId,
            intent,
            flags
        );
        
        // Cancelar la alarma
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * Reprograma una notificación (útil al actualizar un evento)
     * @param context Contexto de la aplicación
     * @param evento EventoSanitario actualizado
     */
    public static void reprogramarNotificacion(Context context, EventoSanitario evento) {
        cancelarNotificacion(context, evento.getId());
        programarNotificacion(context, evento);
    }
}
