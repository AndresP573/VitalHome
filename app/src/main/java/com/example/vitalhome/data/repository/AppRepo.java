package com.example.vitalhome.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de la aplicación para gestionar la persistencia local de datos en SharedPreferences.
 * Maneja la lectura y escritura de listas de medicamentos e historial de alertas usando Gson.
 */
public class AppRepo {

    // --- Bloque 1: Atributos y constructor ---
    private SharedPreferences p;
    private Gson gson = new Gson();

    public AppRepo(Context ctx) {
        p = ctx.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
    }

    // --- Bloque 2: Persistencia de medicamentos ---
    public List<Medication> getMeds() {
        String json = p.getString("lista_medicamentos", "[]");
        List<Medication> res = gson.fromJson(json, new TypeToken<List<Medication>>(){}.getType());
        return res != null ? res : new ArrayList<>();
    }

    public void saveMeds(List<Medication> list) {
        p.edit().putString("lista_medicamentos", gson.toJson(list)).apply();
    }

    // --- Bloque 3: Persistencia de eventos de alerta ---
    public List<AlertEvent> getEvents() {
        String json = p.getString("historial_alertas", "[]");
        List<AlertEvent> res = gson.fromJson(json, new TypeToken<List<AlertEvent>>(){}.getType());
        return res != null ? res : new ArrayList<>();
    }

    public void saveEvents(List<AlertEvent> list) {
        p.edit().putString("historial_alertas", gson.toJson(list)).apply();
    }

    public void addEvent(AlertEvent ev) {
        List<AlertEvent> list = getEvents();
        list.add(0, ev);
        saveEvents(list);
    }
}
