package com.example.vitalhome.controladores;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.vitalhome.modelo.AlertEvent;
import com.example.vitalhome.modelo.Medication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class AppRepo {
    private SharedPreferences p;
    private Gson gson = new Gson();

    public AppRepo(Context ctx) {
        p = ctx.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
    }

    public List<Medication> getMeds() {
        String json = p.getString("lista_medicamentos", "[]");
        List<Medication> res = gson.fromJson(json, new TypeToken<List<Medication>>(){}.getType());
        return res != null ? res : new ArrayList<>();
    }

    public void saveMeds(List<Medication> list) {
        p.edit().putString("lista_medicamentos", gson.toJson(list)).apply();
    }

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