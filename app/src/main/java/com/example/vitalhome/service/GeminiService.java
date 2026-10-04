package com.example.vitalhome.service;

import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
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
        sendMessageWithRetry(userMessage, 0, callback);
    }

    private void sendMessageWithRetry(String userMessage, int reintentos, GeminiCallback callback) {
        String systemInstructionText = "Eres VitalBot, un asistente virtual amable, paciente y claro integrado en la app VitalHome. " +
                "Tu objetivo es ayudar a adultos mayores respondiendo dudas sobre sus recordatorios, medicamentos o estado de la casa domótica. " +
                "Usa frases cortas, sencillas y muy cálidas.";

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
                    mainHandler.postDelayed(() -> sendMessageWithRetry(userMessage, reintentos + 1, callback), 1500);
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
                    // Si los servidores de Google están sobrecargados (503/429), reintentamos automáticamente en 1.5s
                    mainHandler.postDelayed(() -> sendMessageWithRetry(userMessage, reintentos + 1, callback), 1500);
                } else {
                    mainHandler.post(() -> callback.onError("Error HTTP " + response.code() + ":\n" + responseString));
                }
            }
        });
    }
}
