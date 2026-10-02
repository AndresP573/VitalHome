package com.example.vitalhome.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.controladores.MainActivity;

/**
 * Clase utilitaria encargada de la creación de canales de notificación y la emisión
 * de alertas del sistema (SOS, salud, eventos del hogar y recordatorios de medicación).
 */
public class NotificationHelper {

    // --- Bloque 1: Constantes de canales de notificación ---
    private static final String CANAL_EMERGENCIAS = "EMERGENCIAS_CHANNEL";
    private static final String CANAL_SALUD = "SALUD_CHANNEL";
    private static final String CANAL_HOGAR = "HOGAR_CHANNEL";
    private static final String CANAL_MEDICAMENTOS = "MEDICAMENTOS_CHANNEL";

    // --- Bloque 2: Métodos auxiliares para canales e intents ---
    private static void crearCanalSiFalta(Context ctx, String id, String nombre) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(id, nombre, NotificationManager.IMPORTANCE_HIGH);
            c.enableVibration(true);
            c.setVibrationPattern(new long[]{0, 500, 250, 500});
            android.media.AudioAttributes audioAttrs = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build();
            c.setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM), audioAttrs);
            NotificationManager m = ctx.getSystemService(NotificationManager.class);
            if (m != null) m.createNotificationChannel(c);
        }
    }

    private static PendingIntent getPendingIntent(Context ctx, int requestCode) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(ctx, requestCode, i, PendingIntent.FLAG_IMMUTABLE);
    }

    // --- Bloque 3: Notificaciones de Emergencia / SOS ---
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

    // --- Bloque 4: Notificaciones de Salud y Signos Vitales ---
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

    // --- Bloque 5: Notificaciones de Eventos del Hogar ---
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

    public static void fuegoApagado(Context ctx) {
        crearCanalSiFalta(ctx, CANAL_HOGAR, "Hogar");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_HOGAR)
                .setSmallIcon(R.drawable.ic_notif_check)
                .setContentTitle("✅ Alarma de fuego apagada")
                .setContentText("El detector de humo se desactivó. Estado normal.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 110))
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

    public static void puertaCerrada(Context ctx) {
        crearCanalSiFalta(ctx, CANAL_HOGAR, "Hogar");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_HOGAR)
                .setSmallIcon(R.drawable.ic_notif_check)
                .setContentTitle("🚪 Puerta cerrada")
                .setContentText("La puerta principal fue cerrada.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 109))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(7, n.build());
        } catch (SecurityException ignored) {}
    }

    // --- Bloque 6: Notificaciones de Medicamentos ---
    public static void medicamento(Context ctx, int notifId, String nombre, String hora) {
        crearCanalSiFalta(ctx, CANAL_MEDICAMENTOS, "Medicamentos");
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_MEDICAMENTOS)
                .setSmallIcon(R.drawable.ic_notif_pill)
                .setContentTitle("💊 Hora de tu medicamento")
                .setContentText("Es hora de tomar " + nombre + " (" + hora + ").")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(getPendingIntent(ctx, notifId))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(notifId, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void medicamentoTomado(Context ctx, String nombreMed, String hora) {
        crearCanalSiFalta(ctx, CANAL_MEDICAMENTOS, "Medicamentos");
        int id = 200 + Math.abs(nombreMed.hashCode()) % 1000;
        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_MEDICAMENTOS)
                .setSmallIcon(R.drawable.ic_notif_check)
                .setContentTitle("✅ Medicamento tomado")
                .setContentText("Se registró que tomó " + nombreMed + " a las " + hora + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, id))
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(id, n.build());
        } catch (SecurityException ignored) {}
    }
}
