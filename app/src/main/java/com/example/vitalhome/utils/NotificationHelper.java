package com.example.vitalhome.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.controladores.MainActivity;
import java.util.Objects;

/**
 * Clase utilitaria encargada de la creación de canales de notificación y la emisión
 * de alertas del sistema utilizando los sonidos configurados por el usuario (Alarma o Notificación).
 * Implementa deduplicación de ID para evitar notificaciones duplicadas en la bandeja del sistema.
 */
public class NotificationHelper {

    // --- Constantes de canales de notificación ---
    private static final String CANAL_EMERGENCIAS = "EMERGENCIAS_CHANNEL";
    private static final String CANAL_MEDICAMENTOS = "MEDICAMENTOS_CHANNEL";

    // --- Métodos auxiliares para canales con sonido de Alarma o Notificación ---
    private static void crearCanalAlarma(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CANAL_MEDICAMENTOS, "Medicamentos", NotificationManager.IMPORTANCE_HIGH);
            c.enableVibration(true);
            c.setVibrationPattern(new long[]{0, 500, 250, 500});
            AudioAttributes audioAttrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            c.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttrs);
            NotificationManager m = ctx.getSystemService(NotificationManager.class);
            if (m != null) m.createNotificationChannel(c);
        }
    }

    private static void crearCanalNotificacion(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CANAL_EMERGENCIAS, "Emergencias y Alertas", NotificationManager.IMPORTANCE_HIGH);
            c.enableVibration(true);
            c.setVibrationPattern(new long[]{0, 300, 150, 300});
            AudioAttributes audioAttrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            c.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttrs);
            NotificationManager m = ctx.getSystemService(NotificationManager.class);
            if (m != null) m.createNotificationChannel(c);
        }
    }

    private static PendingIntent getPendingIntent(Context ctx, int requestCode) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(ctx, requestCode, i, PendingIntent.FLAG_IMMUTABLE);
    }

    // --- Notificaciones de Alertas y Emergencias para el Cuidador con Deduplicación ---
    public static void notificarAlertaCuidadorConId(Context ctx, String docId, String tipo, String tituloPersonalizado, String mensaje) {
        crearCanalNotificacion(ctx);
        String titulo = Objects.requireNonNullElse(tituloPersonalizado, "⚠️ Alerta VitalHome");
        
        int iconResource = R.drawable.ic_notif_warning;
        if ("MEDICINA".equalsIgnoreCase(tipo)) {
            iconResource = R.drawable.ic_notif_pill;
        } else if ("HOGAR".equalsIgnoreCase(tipo)) {
            iconResource = R.drawable.ic_notif_door;
        } else if ("SALUD".equalsIgnoreCase(tipo)) {
            iconResource = R.drawable.ic_notif_heart;
        }

        NotificationCompat.Builder n = new NotificationCompat.Builder(ctx, CANAL_EMERGENCIAS)
                .setSmallIcon(iconResource)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(ctx, 101))
                .setAutoCancel(true);
        try {
            int notifId = (docId != null && !docId.isEmpty()) ? Math.abs(docId.hashCode()) : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            NotificationManagerCompat.from(ctx).notify(notifId, n.build());
        } catch (SecurityException ignored) {}
    }

    public static void notificarAlertaCuidador(Context ctx, String tipo, String tituloPersonalizado, String mensaje) {
        notificarAlertaCuidadorConId(ctx, null, tipo, tituloPersonalizado, mensaje);
    }

    public static void sendSOS(Context ctx, String quien) {
        notificarAlertaCuidadorConId(ctx, "sos_alert", "SOS", "🚨 ALERTA SOS", quien + " necesita ayuda urgente.");
    }

    public static void sosEstoyBien(Context ctx, String quien) {
        notificarAlertaCuidadorConId(ctx, "sos_estoy_bien", "SOS", "✅ Estoy bien", quien + " canceló la alerta: se encuentra bien.");
    }

    // --- Notificaciones de Salud y Signos Vitales ---
    public static void pulso(Context ctx, int lpm) {
        notificarAlertaCuidador(ctx, "SALUD", "❤️ Pulso registrado", "Tu pulso es de " + lpm + " LPM.");
    }

    public static void pulsoElevado(Context ctx, int lpm) {
        notificarAlertaCuidador(ctx, "SALUD", "⚠️ Pulso elevado", lpm + " LPM, por encima de lo normal.");
    }

    public static void presionElevada(Context ctx, int sis, int dia) {
        notificarAlertaCuidador(ctx, "SALUD", "⚠️ Presión elevada", sis + "/" + dia + " mmHg. Consulta a tu médico.");
    }

    public static void oxigenoBajo(Context ctx, int pct) {
        notificarAlertaCuidador(ctx, "SALUD", "⚠️ Oxígeno bajo", "Saturación de " + pct + "%, por debajo de lo normal.");
    }

    // --- Notificaciones de Eventos del Hogar ---
    public static void fuego(Context ctx) {
        notificarAlertaCuidador(ctx, "HOGAR", "🔥 ¡Fuego detectado!", "El detector de humo se activó en el hogar.");
    }

    public static void fuegoApagado(Context ctx) {
        notificarAlertaCuidador(ctx, "HOGAR", "✅ Alarma de fuego apagada", "El detector de humo se desactivó. Estado normal.");
    }

    public static void puertaAbierta(Context ctx) {
        notificarAlertaCuidador(ctx, "HOGAR", "🚪 Puerta abierta", "La puerta principal se abrió.");
    }

    public static void puertaCerrada(Context ctx) {
        notificarAlertaCuidador(ctx, "HOGAR", "🚪 Puerta cerrada", "La puerta principal fue cerrada.");
    }

    // --- Notificaciones de Medicamentos ---
    public static void medicamento(Context ctx, int notifId, String nombre, String hora) {
        crearCanalAlarma(ctx);
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
        notificarAlertaCuidador(ctx, "MEDICINA", "✅ Medicamento tomado", "Se registró que tomó " + nombreMed + " a las " + hora + ".");
    }
}
