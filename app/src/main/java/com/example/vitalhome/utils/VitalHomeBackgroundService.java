package com.example.vitalhome.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.vitalhome.R;
import com.example.vitalhome.ui.controladores.MainActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio en primer plano (Foreground Service) para mantener el monitoreo continuo
 * de alertas críticas de emergencia (SOS, Hogar, Fuego, Medicina) en tiempo real, incluso con la app en segundo plano o el celular bloqueado.
 */
public class VitalHomeBackgroundService extends Service {

    private static final String CHANNEL_SERVICE_ID = "VITALHOME_SERVICE_CHANNEL";
    private static final int SERVICE_NOTIFICATION_ID = 999;

    private ListenerRegistration alertListener;
    private final Set<String> alertIdsNotificadas = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalServicio();
        Notification notification = crearNotificacionServicio();
        startForeground(SERVICE_NOTIFICATION_ID, notification);

        iniciarEscuchaFirestoreSegundoPlano();
    }

    private void crearCanalServicio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_SERVICE_ID,
                    "Monitoreo Continuo VitalHome",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Mantiene activa la vigilancia de emergencias SOS y eventos del hogar en segundo plano");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification crearNotificacionServicio() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_SERVICE_ID)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle("VitalHome Protección Activa")
                .setContentText("Monitoreando emergencias SOS y hogar en tiempo real")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private void iniciarEscuchaFirestoreSegundoPlano() {
        SharedPreferences prefs = getSharedPreferences("VitalHomePrefs", MODE_PRIVATE);
        String rol = prefs.getString("rol", "cuidador");

        if (!"cuidador".equals(rol)) return;

        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously();
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            alertListener = db.collection("alerts")
                    .whereEqualTo("resuelto", false)
                    .addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
                        private boolean initialLoadDone = false;

                        @Override
                        public void onEvent(@Nullable com.google.firebase.firestore.QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                            if (e != null || snapshots == null) return;

                            if (!initialLoadDone) {
                                initialLoadDone = true;
                                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                                    alertIdsNotificadas.add(doc.getId());
                                }
                                return;
                            }

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    String docId = dc.getDocument().getId();
                                    boolean isLocal = dc.getDocument().getMetadata().hasPendingWrites();

                                    String tipo = dc.getDocument().getString("tipo");
                                    String tituloDoc = dc.getDocument().getString("titulo");
                                    String desc = dc.getDocument().getString("descripcion");
                                    String abuelo = dc.getDocument().getString("pacienteNombre");
                                    String abueloNombre = abuelo != null ? abuelo : "Tu familiar";
                                    Date ts = dc.getDocument().getDate("timestamp");

                                    boolean esEmergenciaCritica = "SOS".equalsIgnoreCase(tipo) || "CAIDA".equalsIgnoreCase(tipo) || "FUEGO".equalsIgnoreCase(tipo) || "HOGAR".equalsIgnoreCase(tipo) || "MEDICINA".equalsIgnoreCase(tipo) || "SALUD".equalsIgnoreCase(tipo);
                                    boolean esReciente = ts != null && (Math.abs(System.currentTimeMillis() - ts.getTime()) < 600000); // Creado en los últimos 10 minutos

                                    if (!isLocal && !alertIdsNotificadas.contains(docId) && esEmergenciaCritica && esReciente) {
                                        alertIdsNotificadas.add(docId);

                                        String tituloFinal;
                                        String mensajeText;

                                        if ("MEDICINA".equalsIgnoreCase(tipo)) {
                                            tituloFinal = "✅ Medicamento Tomado";
                                            mensajeText = desc != null ? desc : "Medicamento tomado";
                                        } else if ("SOS".equalsIgnoreCase(tipo)) {
                                            tituloFinal = "🚨 ALERTA SOS";
                                            mensajeText = abueloNombre + ": " + (desc != null ? desc : "Emergencia SOS");
                                        } else if ("CAIDA".equalsIgnoreCase(tipo)) {
                                            tituloFinal = "⚠️ ¡ALERTA DE CAÍDA!";
                                            mensajeText = abueloNombre + ": " + (desc != null ? desc : "Caída detectada");
                                        } else if ("HOGAR".equalsIgnoreCase(tipo)) {
                                            tituloFinal = "🏠 ¡ALERTA EN EL HOGAR!";
                                            mensajeText = abueloNombre + ": " + (desc != null ? desc : "Evento del hogar");
                                        } else {
                                            tituloFinal = tituloDoc != null ? tituloDoc : "⚠️ Alerta";
                                            mensajeText = abueloNombre + ": " + (desc != null ? desc : "Evento detectado");
                                        }

                                        NotificationHelper.notificarAlertaCuidadorConId(VitalHomeBackgroundService.this, docId, tipo, tituloFinal, mensajeText);
                                    }
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("BackgroundService", "Error en escucha Firestore", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertListener != null) {
            alertListener.remove();
            alertListener = null;
        }
    }
}
