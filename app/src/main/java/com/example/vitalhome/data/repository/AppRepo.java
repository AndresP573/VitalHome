package com.example.vitalhome.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.utils.AppExecutors;
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
 * Utiliza SharedPreferences, Gson y Cloud Firestore para el almacenamiento y sincronización en tiempo real.
 * Todas las operaciones de lectura/escritura en disco y deserialización con Gson se delegan al hilo secundario diskIO.
 */
public class AppRepo {

    private static final String TAG = "AppRepo";
    private final SharedPreferences p;
    private final Gson gson = new Gson();

    public interface Callback<T> {
        void onResult(T result);
    }

    /**
     * Constructor del repositorio.
     *
     * @param ctx Contexto de la aplicación utilizado para obtener SharedPreferences.
     */
    public AppRepo(Context ctx) {
        p = ctx != null ? ctx.getApplicationContext().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE) : null;
    }

    // --- Persistencia de lista de medicamentos ---
    public List<Medication> getMeds() {
        if (p == null) return new ArrayList<>();
        try {
            String json = p.getString("lista_medicamentos", "[]");
            List<Medication> res = gson.fromJson(json, new TypeToken<List<Medication>>(){}.getType());
            return res != null ? res : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo lista_medicamentos desde SharedPreferences", e);
            return new ArrayList<>();
        }
    }

    public void getMedsAsync(Callback<List<Medication>> callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Medication> res = getMeds();
            if (callback != null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(res));
            }
        });
    }

    public void saveMeds(List<Medication> list) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                if (p != null) {
                    p.edit().putString("lista_medicamentos", gson.toJson(list != null ? list : new ArrayList<>())).apply();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error guardando lista_medicamentos en SharedPreferences", e);
            }
        });
    }

    // --- Persistencia de historial de eventos ---
    public List<AlertEvent> getEvents() {
        if (p == null) return new ArrayList<>();
        try {
            String json = p.getString("historial_alertas", "[]");
            List<AlertEvent> res = gson.fromJson(json, new TypeToken<List<AlertEvent>>(){}.getType());
            return res != null ? res : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo historial_alertas desde SharedPreferences", e);
            return new ArrayList<>();
        }
    }

    public void getEventsAsync(Callback<List<AlertEvent>> callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<AlertEvent> res = getEvents();
            if (callback != null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(res));
            }
        });
    }

    public void saveEvents(List<AlertEvent> list) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                if (p != null) {
                    p.edit().putString("historial_alertas", gson.toJson(list != null ? list : new ArrayList<>())).apply();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error guardando historial_alertas en SharedPreferences", e);
            }
        });
    }

    /**
     * Registra un nuevo evento al inicio del historial local y lo sincroniza
     * automáticamente en vivo a Cloud Firestore en el hilo diskIO.
     *
     * @param ev Objeto AlertEvent a agregar.
     */
    public void addEvent(AlertEvent ev) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<AlertEvent> list = getEvents();
                list.add(0, ev);
                if (p != null) {
                    p.edit().putString("historial_alertas", gson.toJson(list)).apply();
                    p.edit().putBoolean("alertas_limpiadas_localmente", false).apply();
                }

                if (p != null && ev != null) {
                    String detalle = ev.getData() != null ? ev.getData() : "";
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

                    FirebaseFirestore.getInstance()
                        .collection("alerts")
                        .document(customDocId)
                        .set(alertData);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error agregando evento a Firestore/local", e);
            }
        });
    }

    // --- Persistencia de Historial de Chat ---
    public String getChatHistory() {
        if (p == null) return "¡Hola! Soy VitalBot 🤖\n¿En qué te puedo ayudar hoy sobre tus medicamentos, recordatorios o el estado de tu hogar?";
        try {
            return p.getString("chat_history_transcript", "¡Hola! Soy VitalBot 🤖\n¿En qué te puedo ayudar hoy sobre tus medicamentos, recordatorios o el estado de tu hogar?");
        } catch (Exception e) {
            Log.e(TAG, "Error leyendo chat history de SharedPreferences", e);
            return "¡Hola! Soy VitalBot 🤖\n¿En qué te puedo ayudar hoy sobre tus medicamentos, recordatorios o el estado de tu hogar?";
        }
    }

    public void saveChatHistory(String transcript) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                if (p != null) {
                    p.edit().putString("chat_history_transcript", transcript).apply();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error guardando chat history en SharedPreferences", e);
            }
        });
    }
}
