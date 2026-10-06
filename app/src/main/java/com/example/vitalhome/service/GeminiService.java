package com.example.vitalhome.service;

import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;

public class GeminiService {

    // API Key de Google AI Studio obtenida de tu proyecto VitalHome
    private static final String API_KEY = "AQ.Ab8RN6KOirM5-5Oc4K4qd7zUq1HdLLEXpzishbJkmn8v-5KJ4w";

    // Modelo oficial indicado por Google: gemini-3.8-flash
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeminiCallback {
        void onSuccess(String responseText);
        void onError(String error);
    }

    public void sendMessage(String userMessage, GeminiCallback callback) {
        String systemInstructionText = "Eres VitalBot, un asistente virtual amable, paciente y claro integrado en la app VitalHome. " +
                "Tu objetivo es ayudar a adultos mayores respondiendo dudas sobre sus recordatorios, medicamentos o estado de la casa domótica. " +
                "Usa frases cortas, sencillas y muy cálidas.";
        sendMessageInternal(systemInstructionText, userMessage, 0, callback);
    }

    public void sendMessageWithContext(Context context, String userMessage, GeminiCallback callback) {
        AppRepo repo = new AppRepo(context);
        SharedPreferences prefs = context.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);

        // 1. Signos Vitales
        String vitalsJson = prefs.getString("ultimos_vitals", "{}");
        String pacienteNombre = prefs.getString("nombreAdultoMayor", "Adulto Mayor");

        // 2. Medicamentos
        List<Medication> meds = repo.getMeds();
        StringBuilder medsBuilder = new StringBuilder();
        if (meds != null && !meds.isEmpty()) {
            for (Medication m : meds) {
                if (m != null) {
                    medsBuilder.append("- ").append(m.getNombre()).append(" a las ").append(m.getHora())
                               .append(m.isTaken() ? " (Tomado ✅)" : " (Pendiente ⏳)").append("\n");
                }
            }
        } else {
            medsBuilder.append("No hay medicamentos registrados para hoy.\n");
        }

        // 3. Alertas y Eventos Recientes (Hogar, SOS, Salud)
        List<AlertEvent> events = repo.getEvents();
        StringBuilder eventsBuilder = new StringBuilder();
        int count = 0;
        if (events != null) {
            for (AlertEvent ev : events) {
                if (ev != null && count < 5) {
                    eventsBuilder.append("- [").append(ev.getTipo()).append("] ").append(ev.getData())
                                 .append(" a las ").append(ev.getHora()).append("\n");
                    count++;
                }
            }
        }
        if (count == 0) {
            eventsBuilder.append("No hay alertas ni eventos recientes.\n");
        }

        String systemInstructionText = "Eres VitalBot, un asistente virtual amable, paciente y claro integrado en la app VitalHome. " +
                "Tu objetivo es ayudar al usuario (" + pacienteNombre + ") respondiendo dudas sobre sus recordatorios, medicamentos, estado de la casa domótica y signos vitales. " +
                "Usa frases cortas, sencillas y muy cálidas.\n\n" +
                "=== DATOS EN TIEMPO REAL DE FIREBASE / LOCAL (CONTEXTO ACTUAL) ===\n" +
                "1. Últimos Signos Vitales del paciente: " + vitalsJson + "\n" +
                "2. Plan de Medicamentos de hoy:\n" + medsBuilder.toString() +
                "3. Alertas y Eventos Recientes (Hogar, SOS, Salud):\n" + eventsBuilder.toString() +
                "===================================================================\n" +
                "Usa estos datos reales para responder de forma precisa, útil y personalizada a la pregunta del usuario.";

        sendMessageInternal(systemInstructionText, userMessage, 0, callback);
    }

    private void sendMessageInternal(String systemInstructionText, String userMessage, int reintentos, GeminiCallback callback) {
        JsonObject jsonBody = new JsonObject();

        // Configuración de Instrucción del Sistema
        JsonObject systemInstructionObj = new JsonObject();
        JsonArray systemPartsArray = new JsonArray();
        JsonObject systemPartObj = new JsonObject();
        systemPartObj.addProperty("text", systemInstructionText);
        systemPartsArray.add(systemPartObj);
        systemInstructionObj.add("parts", systemPartsArray);
        jsonBody.add("system_instruction", systemInstructionObj);

        // Contenido del usuario
        JsonArray contentsArray = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray partsArray = new JsonArray();

        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", userMessage);
        partsArray.add(partObj);

        contentObj.add("parts", partsArray);
        contentsArray.add(contentObj);
        jsonBody.add("contents", contentsArray);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (reintentos < 3) {
                    mainHandler.postDelayed(() -> sendMessageInternal(systemInstructionText, userMessage, reintentos + 1, callback), 1500);
                } else {
                    mainHandler.post(() -> callback.onError("Fallo de red: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful() && !responseString.isEmpty()) {
                    try {
                        JsonObject root = JsonParser.parseString(responseString).getAsJsonObject();
                        String textOutput = root.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        mainHandler.post(() -> callback.onSuccess(textOutput));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Error al parsear respuesta de Gemini: " + e.getMessage() + "\nRaw: " + responseString));
                    }
                } else if ((response.code() == 503 || response.code() == 429) && reintentos < 3) {
                    mainHandler.postDelayed(() -> sendMessageInternal(systemInstructionText, userMessage, reintentos + 1, callback), 1500);
                } else if (response.code() == 429) {
                    mainHandler.post(() -> callback.onError("🤖 ¡Hola! El asistente ha alcanzado su límite temporal de consultas gratuitas (Error 429). Por favor, intenta de nuevo más tarde."));
                } else {
                    mainHandler.post(() -> callback.onError("🤖 El servicio de IA está ocupado en este momento. Por favor, intenta nuevamente en unos minutos."));
                }
            }
        });
    }
}
