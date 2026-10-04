package com.example.vitalhome.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Receptor de difusiones (BroadcastReceiver) para el manejo infalible de alarmas de medicamentos.
 * Adquiere un WakeLock temporal para despertar la CPU y emitir la alarma sonora aunque el teléfono esté dormido/bloqueado.
 */
public class MedicationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // Despertar la CPU temporalmente para reproducir la alarma de reloj
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = (pm != null) ? pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VitalHome:MedAlarmWakeLock") : null;
        if (wakeLock != null) {
            try {
                wakeLock.acquire(10000); // 10 segundos de WakeLock
            } catch (Exception ignored) {}
        }

        // Reprogramación automática tras reinicio del dispositivo o actualización
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || 
            "android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            Log.d("AlarmDebug", "Reinicio detectado. Reprogramando alarmas...");
            MedicationAlarmScheduler.reprogramarTodas(context);
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
            return;
        }

        String medId = intent.getStringExtra("med_id");
        String medNombre = intent.getStringExtra("med_nombre");
        String medHora = intent.getStringExtra("med_hora");

        Log.d("AlarmDebug", "onReceive llegó con med_id: " + medId + ", nombre: " + medNombre);

        // Si la información viene directamente en los extras del intent
        if (medNombre != null && !medNombre.isEmpty()) {
            int notifId = (medId != null) ? Math.abs(medId.hashCode()) : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            NotificationHelper.medicamento(context, notifId, medNombre, medHora != null ? medHora : "");

            String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            AppRepo repo = new AppRepo(context);
            repo.addEvent(new AlertEvent("MEDICINA", "Hora de tomar " + medNombre, "Casa", horaActual));
            
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
            return;
        }

        // Fallback: consulta en repositorio
        if (medId != null) {
            AppRepo repo = new AppRepo(context);
            List<Medication> meds = repo.getMeds();
            if (meds != null) {
                for (Medication m : meds) {
                    if (java.util.Objects.equals(medId, m.getId())) {
                        if (!m.isTaken()) {
                            int notifId = Math.abs(m.getId().hashCode());
                            NotificationHelper.medicamento(context, notifId, m.getNombre(), m.getHora());

                            String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                            repo.addEvent(new AlertEvent("MEDICINA", "Hora de tomar " + m.getNombre(), "Casa", horaActual));
                        }
                        break;
                    }
                }
            }
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
