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

    // Modelo ligero: flash
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=AQ.Ab8RN6L4Tf2UL4rxqcJvZwP6KzNgB0xmBlVWeEE7Nw2viatr_w";
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
        // Instrucciones/ prompt para darle al asistente, al adulto y sistemas de domotica
        String systemInstruction = "Eres VitalBot, un asistente virtual amable, paciente y claro integrado en la app VitalHome. " +
                "Tu objetivo es ayudar a adultos mayores respondiendo dudas sobre sus recordatorios, medicamentos o estado de la casa domótica. " +
                "Usa frases cortas, sencillas y muy cálidas.";

        JsonObject jsonBody = new JsonObject();
        JsonArray contentsArray = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray partsArray = new JsonArray();

        // Le pasamos el contexto combinado con la pregunta del usuario
        JsonObject partObj = new JsonObject();
        partObj.addProperty("text", systemInstruction + "\n\nPregunta del usuario: " + userMessage);
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
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        // Esta es la respuesta de la API de Gemini
                        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
                        String textOutput = root.getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        mainHandler.post(() -> callback.onSuccess(textOutput));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("Error al parsear la respuesta"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Error en la petición: " + response.code()));
                }
            }
        });
    }
}
