package com.example.vitalhome.controladores;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    private static final String CANAL_SOS = "SOS_CHANNEL";

    public static void sendSOS(Context ctx, String quien) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CANAL_SOS, "Alertas SOS", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager m = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (m != null) m.createNotificationChannel(c);
        }
        
        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_SOS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 ALERTA SOS")
                .setContentText(quien + " necesita ayuda urgente.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000});

        try {
            NotificationManagerCompat.from(ctx).notify(1, n.build());
        } catch (SecurityException ignored) {}
    }
}