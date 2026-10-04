package com.example.vitalhome.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio centralizado para la persistencia de datos de la aplicación.
 * Utiliza SharedPreferences, Gson y Cloud Firestore para el almacenamiento y sincronización en tiempo real de todas las alertas.
 */
public class AppRepo {

    // --- Atributos de almacenamiento e inmutabilidad ---
    private final SharedPreferences p;
    private final Gson gson = new Gson();

    /**
     * Constructor del repositorio.
     *
     * @param ctx Contexto de la aplicación utilizado para obtener SharedPreferences.
     */
    public AppRepo(Context ctx) {
        p = ctx != null ? ctx.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE) : null;
    }

    // --- Persistencia de lista de medicamentos ---
    public List<Medication> getMeds() {
        if (p == null) return new ArrayList<>();
        String json = p.getString("lista_medicamentos", "[]");
        List<Medication> res = gson.fromJson(json, new TypeToken<List<Medication>>(){}.getType());
        if (res == null) {
            return new ArrayList<>();
        }
        return res;
    }

    public void saveMeds(List<Medication> list) {
        if (p != null) {
            p.edit().putString("lista_medicamentos", gson.toJson(list)).apply();
        }
    }

    // --- Persistencia de historial de eventos ---
    public List<AlertEvent> getEvents() {
        if (p == null) return new ArrayList<>();
        String json = p.getString("historial_alertas", "[]");
        List<AlertEvent> res = gson.fromJson(json, new TypeToken<List<AlertEvent>>(){}.getType());
        if (res == null) {
            return new ArrayList<>();
        }
        return res;
    }

    public void saveEvents(List<AlertEvent> list) {
        if (p != null) {
            p.edit().putString("historial_alertas", gson.toJson(list)).apply();
        }
    }

    /**
     * Registra un nuevo evento al inicio del historial local y lo sincroniza
     * automáticamente en vivo a Cloud Firestore para TODAS las alertas de la aplicación.
     *
     * @param ev Objeto AlertEvent a agregar.
     */
    public void addEvent(AlertEvent ev) {
        List<AlertEvent> list = getEvents();
        list.add(0, ev);
        saveEvents(list);

        if (p != null) {
            p.edit().putBoolean("alertas_limpiadas_localmente", false).apply();
        }

        // --- Sincronización Global en Tiempo Real de TODAS las Alertas en Firestore ---
        try {
            if (p != null) {
                String abueloEmail = p.getString("email", "marta@vitalhome.com");
                String abueloNombre = p.getString("nombreAdultoMayor", p.getString("nombre", "Familiar"));

                String tituloVal;
                if ("MEDICINA".equalsIgnoreCase(ev.getTipo())) {
                    tituloVal = "✅ Medicamento Tomado";
                } else if ("HOGAR".equalsIgnoreCase(ev.getTipo())) {
                    tituloVal = "🏠 ¡Alerta en el Hogar!";
                } else {
                    tituloVal = "¡ALERTA " + (ev.getTipo() != null ? ev.getTipo().toUpperCase() : "") + "!";
                }

                Map<String, Object> alertData = new HashMap<>();
                alertData.put("pacienteEmail", abueloEmail);
                alertData.put("pacienteNombre", abueloNombre);
                alertData.put("tipo", ev.getTipo() != null ? ev.getTipo() : "ALERTA");
                alertData.put("titulo", tituloVal);
                alertData.put("descripcion", ev.getData() != null ? ev.getData() : "Evento detectado");
                alertData.put("timestamp", new Date());
                alertData.put("resuelto", false);

                String tipoCleano = ev.getTipo() != null ? ev.getTipo().toLowerCase().trim() : "evento";
                String customDocId = "alerta_" + tipoCleano + "_" + System.currentTimeMillis();

                FirebaseFirestore.getInstance("default")
                    .collection("alerts")
                    .document(customDocId)
                    .set(alertData);
            }
        } catch (Exception ignored) {}
    }
}
