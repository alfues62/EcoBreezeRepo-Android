package com.m4gti.ecobreeze.logic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.m4gti.ecobreeze.R;

/**
 * @class NotificationHelper
 * @brief Clase que maneja la creación y envío de notificaciones para alertas de sensores.
 *
 * Esta clase proporciona métodos para crear un canal de notificación y enviar alertas de sensores a los usuarios.
 * Las notificaciones son útiles para informar sobre fallos o errores detectados por los sensores.
 *
 * Métodos principales:
 *   1. `createNotificationChannel()`: Crea un canal de notificación para las alertas de sensores.
 *   2. `sendSensorAlertNotification()`: Envía una notificación de alerta de sensor con el mensaje proporcionado.
 *
 * @note Requiere el contexto de la aplicación y, en el caso de `createNotificationChannel()`, utiliza las capacidades de la API de Android Oreo (nivel 26) o superior.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "sensor_alerts";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alertas de Sensor";
            String description = "Notificaciones de fallos o errores en los sensores";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendSensorAlertNotification(Context context, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icono)
                .setContentTitle("Alerta de Sensor")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
