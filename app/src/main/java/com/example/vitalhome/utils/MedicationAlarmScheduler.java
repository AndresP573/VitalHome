package com.example.vitalhome.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import java.util.Calendar;
import java.util.List;

/**
 * Gestor centralizado para programar, cancelar y reprogramar alarmas
 * de medicamentos usando el AlarmManager de Android.
 */
public class MedicationAlarmScheduler {

    private static final String TAG = "MedicationAlarmScheduler";

    /**
     * Programación de la alarma para un medicamento específico.
     */
    public static void programar(Context ctx, Medication m) {
        if (m == null) {
            Log.d(TAG, "programar abortado: Medication es null");
            return;
        }
        Log.d(TAG, "Entrando a programar: " + m.getNombre() + " (" + m.getHora() + ")");

        if (m.isTaken()) {
            Log.d(TAG, "programar abortado: El medicamento ya fue tomado");
            return;
        }

        try {
            String horaStr = m.getHora();
            if (horaStr == null) {
                Log.d(TAG, "programar abortado: la hora es null");
                return;
            }

            String[] parts = horaStr.split(":");
            if (parts.length != 2) {
                Log.d(TAG, "programar abortado: formato de hora inválido");
                return;
            }

            int hora = Integer.parseInt(parts[0].trim());
            int minuto = Integer.parseInt(parts[1].trim());

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hora);
            cal.set(Calendar.MINUTE, minuto);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(ctx, MedicationAlarmReceiver.class);
            intent.putExtra("med_id", m.getId());
            intent.putExtra("med_nombre", m.getNombre());
            intent.putExtra("med_hora", m.getHora());

            int requestCode = m.getId().hashCode();
            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                Intent showIntent = new Intent(ctx, com.example.vitalhome.ui.controladores.MainActivity.class);
                PendingIntent showPi = PendingIntent.getActivity(
                        ctx,
                        requestCode,
                        showIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );

                AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), showPi);
                Log.d(TAG, "Programando AlarmClock exacta e infalible para: " + cal.getTime());

                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()) {
                        am.setAlarmClock(clockInfo, pi);
                    } else {
                        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                    }
                } catch (SecurityException se) {
                    Log.e(TAG, "Excepción de seguridad al programar alarma exacta, usando setAndAllowWhileIdle", se);
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al programar alarma de medicamento", e);
        }
    }

    /**
     * Cancela la alarma activa de un medicamento mediante su ID.
     */
    public static void cancelar(Context ctx, String medId) {
        if (medId == null) return;
        Log.d(TAG, "Cancelando alarma para medId: " + medId);

        try {
            Intent intent = new Intent(ctx, MedicationAlarmReceiver.class);
            int requestCode = medId.hashCode();

            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
            );

            if (pi != null) {
                AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                if (am != null) {
                    am.cancel(pi);
                }
                pi.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al cancelar alarma de medicamento", e);
        }
    }

    /**
     * Reprograma las alarmas de todos los medicamentos no tomados en un hilo secundario de fondo (Multi-threading).
     */
    public static void reprogramarTodas(Context ctx) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                AppRepo repo = new AppRepo(ctx);
                List<Medication> meds = repo.getMeds();
                if (meds != null) {
                    for (Medication m : meds) {
                        if (!m.isTaken()) {
                            programar(ctx, m);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reprogramando alarmas en hilo secundario", e);
            }
        });
    }
}
