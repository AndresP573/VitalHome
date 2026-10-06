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
     * automáticamente en vivo a Cloud Firestore SOLO para acciones de usuario y emergencias reales
     * (omitiendo los recordatorios automáticos de alarma local "Hora de tomar...").
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

        // --- Sincronización Global en Tiempo Real: SOLO para acciones reales de usuario / emergencias ---
        try {
            if (p != null && ev != null) {
                String detalle = ev.getData() != null ? ev.getData() : "";
                // Los recordatorios automáticos "Hora de tomar..." son exclusivamente locales del abuelo.
                // El cuidador SOLO recibe la confirmación cuando el abuelo presiona "YA LO TOMÉ" ("Tomó ...").
                if (detalle.startsWith("Hora de tomar")) {
                    return;
                }

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
                alertData.put("descripcion", detalle);
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

    // --- Persistencia de Historial de Chat ---
    public String getChatHistory() {
        if (p == null) return "¡Hola! Soy VitalBot 🤖\n¿En qué te puedo ayudar hoy sobre tus medicamentos, recordatorios o el estado de tu hogar?";
        return p.getString("chat_history_transcript", "¡Hola! Soy VitalBot 🤖\n¿En qué te puedo ayudar hoy sobre tus medicamentos, recordatorios o el estado de tu hogar?");
    }

    public void saveChatHistory(String transcript) {
        if (p != null) {
            p.edit().putString("chat_history_transcript", transcript).apply();
        }
    }
}
