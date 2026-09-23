package com.example.vitalhome.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.controladores.MainActivity;

public class NotificationHelper {
    private static final String CANAL_EMERGENCIAS = "EMERGENCIAS_CHANNEL";
    private static final String CANAL_SALUD = "SALUD_CHANNEL";
    private static final String CANAL_HOGAR = "HOGAR_CHANNEL";
    private static final String CANAL_MEDICAMENTOS = "MEDICAMENTOS_CHANNEL";

    private static void crearCanalSiFalta(Context ctx, String id, String nombre) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(id, nombre, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager m = ctx.getSystemService(NotificationManager.class);
            if (m != null) m.createNotificationChannel(c);
        }
    }

    private static PendingIntent getPendingIntent(Context ctx, int requestCode) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(ctx, requestCode, i, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void sendSOS(Context ctx, String quien) {
        crearCanalSiFalta(ctx, CANAL_EMERGENCIAS, "Emergencias");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_EMERGENCIAS)
                .setSmallIcon(R.drawable.ic_notif_warning)
                .setContentTitle("🚨 ALERTA SOS")
                .setContentText(quien + " necesita ayuda urgente.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 101))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(1, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void sosEstoyBien(Context ctx, String quien) {
        crearCanalSiFalta(ctx, CANAL_EMERGENCIAS, "Emergencias");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_EMERGENCIAS)
                .setSmallIcon(R.drawable.ic_notif_check)
                .setContentTitle("✅ Estoy bien")
                .setContentText(quien + " canceló la alerta: se encuentra bien.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 102))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(2, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void pulso(Context ctx, int lpm) {
        crearCanalSiFalta(ctx, CANAL_SALUD, "Salud");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_SALUD)
                .setSmallIcon(R.drawable.ic_notif_heart)
                .setContentTitle("❤️ Pulso registrado")
                .setContentText("Tu pulso es de " + lpm + " LPM.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 103))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(3, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void pulsoElevado(Context ctx, int lpm) {
        crearCanalSiFalta(ctx, CANAL_SALUD, "Salud");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_SALUD)
                .setSmallIcon(R.drawable.ic_notif_warning)
                .setContentTitle("⚠️ Pulso elevado")
                .setContentText(lpm + " LPM, por encima de lo normal.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 104))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(3, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void presionElevada(Context ctx, int sis, int dia) {
        crearCanalSiFalta(ctx, CANAL_SALUD, "Salud");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_SALUD)
                .setSmallIcon(R.drawable.ic_notif_warning)
                .setContentTitle("⚠️ Presión elevada")
                .setContentText(sis + "/" + dia + " mmHg. Consulta a tu médico.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 105))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(4, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void oxigenoBajo(Context ctx, int pct) {
        crearCanalSiFalta(ctx, CANAL_SALUD, "Salud");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_SALUD)
                .setSmallIcon(R.drawable.ic_notif_warning)
                .setContentTitle("⚠️ Oxígeno bajo")
                .setContentText("Saturación de " + pct + "%, por debajo de lo normal.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 106))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(5, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void fuego(Context ctx) {
        crearCanalSiFalta(ctx, CANAL_EMERGENCIAS, "Emergencias");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_EMERGENCIAS)
                .setSmallIcon(R.drawable.ic_notif_fire)
                .setContentTitle("🔥 ¡Fuego detectado!")
                .setContentText("El detector de humo se activó en el hogar.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 107))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(6, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void puertaAbierta(Context ctx) {
        crearCanalSiFalta(ctx, CANAL_HOGAR, "Hogar");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_HOGAR)
                .setSmallIcon(R.drawable.ic_notif_door)
                .setContentTitle("🚪 Puerta abierta")
                .setContentText("La puerta principal se abrió.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 108))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(7, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void medicamento(Context ctx, int notifId, String nombre, String hora) {
        Log.d("AlarmDebug", "notificando medicamento: " + nombre);
        crearCanalSiFalta(ctx, CANAL_MEDICAMENTOS, "Medicamentos");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_MEDICAMENTOS)
                .setSmallIcon(R.drawable.ic_notif_pill)
                .setContentTitle("💊 Hora de tu medicamento")
                .setContentText("Es hora de tomar " + nombre + " (" + hora + ").")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, notifId))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(notifId, n.build());
        } catch (SecurityException ignored) {}
    }
}
