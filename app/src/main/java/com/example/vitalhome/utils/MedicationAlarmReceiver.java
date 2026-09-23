package com.example.vitalhome.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.ui.controladores.AppRepo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String medId = intent.getStringExtra("med_id");
        Log.d("AlarmDebug", "onReceive llegó con med_id: " + medId);
        if (medId == null) return;

        AppRepo repo = new AppRepo(context);
        List<Medication> meds = repo.getMeds();
        if (meds == null) {
            Log.d("AlarmDebug", "Medicamento no encontrado: lista es null");
            return;
        }

        Medication targetMed = null;
        for (Medication m : meds) {
            if (medId.equals(m.getId())) {
                targetMed = m;
                break;
            }
        }

        if (targetMed == null) {
            Log.d("AlarmDebug", "Medicamento no encontrado en la lista");
            return;
        }

        Log.d("AlarmDebug", "Medicamento encontrado: " + targetMed.getNombre() + ", tomado: " + targetMed.isTaken());

        if (targetMed.isTaken()) {
            return;
        }

        int notifId = medId.hashCode();
        NotificationHelper.medicamento(context, notifId, targetMed.getNombre(), targetMed.getHora());

        String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("MEDICINA", "Hora de tomar " + targetMed.getNombre(), "Casa", horaActual));

        // Reprogramar para el día siguiente
        MedicationAlarmScheduler.programar(context, targetMed);
    }
}
