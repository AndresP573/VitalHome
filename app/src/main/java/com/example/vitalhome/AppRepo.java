package com.example.vitalhome;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class AppRepo {
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    public AppRepo(Context ctx) {
        prefs = ctx.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
    }

    public List<Medication> cargarPlanMedico() {
        String json = prefs.getString("lista_medicamentos", "[]");
        List<Medication> res = gson.fromJson(json, new TypeToken<List<Medication>>(){}.getType());
        return res != null ? res : new ArrayList<>();
    }

    public void guardarPlanMedico(List<Medication> lista) {
        prefs.edit().putString("lista_medicamentos", gson.toJson(lista)).apply();
    }

    public List<AlertEvent> cargarHistorial() {
        String json = prefs.getString("historial_alertas", "[]");
        List<AlertEvent> res = gson.fromJson(json, new TypeToken<List<AlertEvent>>(){}.getType());
        return res != null ? res : new ArrayList<>();
    }

    public void guardarHistorial(List<AlertEvent> lista) {
        prefs.edit().putString("historial_alertas", gson.toJson(lista)).apply();
    }

    public void registrarNuevoEvento(AlertEvent ev) {
        List<AlertEvent> temp = cargarHistorial();
        temp.add(0, ev);
        guardarHistorial(temp);
    }
}