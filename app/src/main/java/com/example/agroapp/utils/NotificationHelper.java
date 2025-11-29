package com.example.agroapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.agroapp.models.EventoSanitario;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Helper para programar notificaciones de eventos sanitarios.
 * Implementa RF009: Notifica al usuario 3 días antes, 1 día antes y el mismo día de cada evento.
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    
    // Constantes para tipos de notificación según RF009
    public static final int NOTIFICATION_TYPE_3_DAYS = 0;
    public static final int NOTIFICATION_TYPE_1_DAY = 1;
    public static final int NOTIFICATION_TYPE_SAME_DAY = 2;
    
    // Request code offsets para distinguir las 3 notificaciones de cada evento
    private static final int REQUEST_CODE_OFFSET_3_DAYS = 0;
    private static final int REQUEST_CODE_OFFSET_1_DAY = 10000;
    private static final int REQUEST_CODE_OFFSET_SAME_DAY = 20000;

    /**
     * Programa las 3 notificaciones para un evento sanitario según RF009:
     * - 3 días antes del evento
     * - 1 día antes del evento
     * - El mismo día del evento
     * 
     * @param context Contexto de la aplicación
     * @param evento EventoSanitario para el cual programar las notificaciones
     */
    public static void programarNotificacion(Context context, EventoSanitario evento) {
        if (evento.getRecordatorio() != 1) {
            return; // No programar si el recordatorio está desactivado
        }

        // Parsear la fecha del evento
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar fechaEvento = Calendar.getInstance();
        try {
            fechaEvento.setTime(sdf.parse(evento.getFechaProgramada()));
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear fecha del evento", e);
            return;
        }
        
        // Obtener hora del recordatorio o usar 9:00 por defecto
        int hora = 9;
        int minuto = 0;
        if (evento.getHoraRecordatorio() != null && !evento.getHoraRecordatorio().isEmpty()) {
            try {
                String[] partes = evento.getHoraRecordatorio().split(":");
                hora = Integer.parseInt(partes[0]);
                minuto = Integer.parseInt(partes[1]);
            } catch (Exception e) {
                Log.w(TAG, "Error al parsear hora, usando 9:00 por defecto", e);
            }
        }

        long currentTime = System.currentTimeMillis();

        // Programar notificación 3 días antes
        programarNotificacionIndividual(context, evento, fechaEvento, -3, hora, minuto, 
            NOTIFICATION_TYPE_3_DAYS, currentTime, "en 3 días");

        // Programar notificación 1 día antes
        programarNotificacionIndividual(context, evento, fechaEvento, -1, hora, minuto,
            NOTIFICATION_TYPE_1_DAY, currentTime, "mañana");

        // Programar notificación el mismo día
        programarNotificacionIndividual(context, evento, fechaEvento, 0, hora, minuto,
            NOTIFICATION_TYPE_SAME_DAY, currentTime, "hoy");
    }

    /**
     * Programa una notificación individual para un evento
     */
    private static void programarNotificacionIndividual(Context context, EventoSanitario evento,
            Calendar fechaEvento, int diasAntes, int hora, int minuto, int tipoNotificacion,
            long currentTime, String mensajeTiempo) {
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager no disponible");
            return;
        }

        // Calcular la fecha/hora de la notificación
        Calendar calendar = (Calendar) fechaEvento.clone();
        calendar.add(Calendar.DAY_OF_MONTH, diasAntes);
        calendar.set(Calendar.HOUR_OF_DAY, hora);
        calendar.set(Calendar.MINUTE, minuto);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        // Solo programar si la fecha está en el futuro
        if (triggerTime <= currentTime) {
            Log.d(TAG, "Notificación " + tipoNotificacion + " para evento " + evento.getId() + 
                " no programada (fecha pasada)");
            return;
        }

        // Crear intent con información del evento y tipo de notificación
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("titulo", "Recordatorio: " + evento.getTipo());
        intent.putExtra("mensaje", evento.getDescripcion() + " - " + mensajeTiempo);
        intent.putExtra("eventoId", evento.getId());
        intent.putExtra("tipoNotificacion", tipoNotificacion);
        intent.putExtra("fechaEvento", evento.getFechaProgramada());

        // Calcular request code único para cada notificación
        int requestCode = calcularRequestCode(evento.getId(), tipoNotificacion);

        // Usar FLAG_IMMUTABLE para Android 12+
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags
        );

        // Programar la alarma usando setAndAllowWhileIdle para mayor confiabilidad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        Log.d(TAG, "Notificación programada: evento=" + evento.getId() + 
            ", tipo=" + tipoNotificacion + ", tiempo=" + calendar.getTime());
    }

    /**
     * Calcula un request code único para cada notificación de un evento.
     * Cada evento tiene 3 notificaciones con códigos únicos.
     */
    private static int calcularRequestCode(int eventoId, int tipoNotificacion) {
        switch (tipoNotificacion) {
            case NOTIFICATION_TYPE_3_DAYS:
                return eventoId + REQUEST_CODE_OFFSET_3_DAYS;
            case NOTIFICATION_TYPE_1_DAY:
                return eventoId + REQUEST_CODE_OFFSET_1_DAY;
            case NOTIFICATION_TYPE_SAME_DAY:
                return eventoId + REQUEST_CODE_OFFSET_SAME_DAY;
            default:
                return eventoId;
        }
    }

    /**
     * Cancela todas las notificaciones programadas para un evento (las 3 notificaciones).
     * @param context Contexto de la aplicación
     * @param eventoId ID del evento sanitario
     */
    public static void cancelarNotificacion(Context context, int eventoId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        // Cancelar las 3 notificaciones programadas para este evento
        for (int tipoNotificacion = 0; tipoNotificacion <= 2; tipoNotificacion++) {
            int requestCode = calcularRequestCode(eventoId, tipoNotificacion);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
            );
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        Log.d(TAG, "Todas las notificaciones canceladas para evento: " + eventoId);
    }

    /**
     * Reprograma todas las notificaciones de un evento (útil al actualizar un evento).
     * @param context Contexto de la aplicación
     * @param evento EventoSanitario actualizado
     */
    public static void reprogramarNotificacion(Context context, EventoSanitario evento) {
        cancelarNotificacion(context, evento.getId());
        programarNotificacion(context, evento);
    }

    /**
     * Obtiene el mensaje descriptivo para un tipo de notificación.
     * @param tipoNotificacion Tipo de notificación (0, 1, o 2)
     * @return Descripción del tiempo restante
     */
    public static String getMensajeTiempo(int tipoNotificacion) {
        switch (tipoNotificacion) {
            case NOTIFICATION_TYPE_3_DAYS:
                return "en 3 días";
            case NOTIFICATION_TYPE_1_DAY:
                return "mañana";
            case NOTIFICATION_TYPE_SAME_DAY:
                return "hoy";
            default:
                return "";
        }
    }
}
