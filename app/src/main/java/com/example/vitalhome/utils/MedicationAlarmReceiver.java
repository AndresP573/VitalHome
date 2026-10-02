package com.example.vitalhome.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Receptor de difusiones (BroadcastReceiver) para el manejo de alarmas de medicamentos.
 * Se activa cuando expira la alarma programada, emitiendo notificaciones y registrando el evento.
 */
public class MedicationAlarmReceiver extends BroadcastReceiver {

    // --- Bloque 1: Recepción del intent de alarma o reinicio ---
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        // Reprogramación automática tras reinicio del dispositivo o actualización
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || 
            "android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            Log.d("AlarmDebug", "Reinicio detectado. Reprogramando alarmas...");
            MedicationAlarmScheduler.reprogramarTodas(context);
            return;
        }

        String medId = intent.getStringExtra("med_id");
        Log.d("AlarmDebug", "onReceive llegó con med_id: " + medId);
        if (medId == null) return;

        // --- Bloque 2: Consulta del medicamento en repositorio ---
        AppRepo repo = new AppRepo(context);
        List<Medication> meds = repo.getMeds();
        if (meds == null) {
            Log.d("AlarmDebug", "Medicamento no encontrado: lista es null");
            return;
        }

        Medication targetMed = null;
        for (Medication m : meds) {
            if (java.util.Objects.equals(medId, m.getId())) {
                targetMed = m;
                break;
            }
        }

        if (targetMed == null) {
            Log.d("AlarmDebug", "Medicamento no encontrado en la lista");
            return;
        }

        Log.d("AlarmDebug", "Medicamento encontrado: " + targetMed.getNombre() + ", tomado: " + targetMed.isTaken());

        // --- Bloque 3: Verificación de estado de toma ---
        if (targetMed.isTaken()) {
            return;
        }

        // --- Bloque 4: Generación de notificación y registro de evento ---
        int notifId = medId.hashCode();
        NotificationHelper.medicamento(context, notifId, targetMed.getNombre(), targetMed.getHora());

        String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("MEDICINA", "Hora de tomar " + targetMed.getNombre(), "Casa", horaActual));

        // --- Bloque 5: Reprogramación para el día siguiente ---
        MedicationAlarmScheduler.programar(context, targetMed);
    }
}
