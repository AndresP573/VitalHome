package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.repository.AppRepo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

/**
 * ViewModel correspondiente al Dashboard del Cuidador.
 * Proporciona información general sobre el adulto mayor asignado mediante escucha directa en Cloud Firestore sin índices requeridos.
 */
public class DashboardCuidadorViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> nombreAdultoMayor = new MutableLiveData<>();
    private final MutableLiveData<Integer> medicinasCount = new MutableLiveData<>();

    private final MutableLiveData<String> vitalsSummary = new MutableLiveData<>("🔋 85% | 📍 En el hogar");

    // --- Bloque 2: Constructor e inicialización ---
    public DashboardCuidadorViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
        vincularYEscucharPacienteEspecifico();
    }

    // --- Bloque 3: Carga y actualización de datos ---
    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombre", "Usuario"));
        nombreAdultoMayor.setValue(prefs.getString("nombreAdultoMayor", "Familiar"));
        medicinasCount.setValue(repo.getMeds().size());
    }

    // --- Vinculación y Escucha en Vivo de Signos Vitales y Perfil del Abuelo ---
    public void vincularYEscucharPacienteEspecifico() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            db.collection("users")
                .whereEqualTo("rol", "cliente")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (doc != null && doc.exists()) {
                            procesarDocPaciente(doc);
                            break;
                        }
                    }
                });
        } catch (Exception ignored) {}
    }

    private void procesarDocPaciente(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return;

        String abueloNombre = doc.getString("nombre");
        if (abueloNombre != null && !abueloNombre.isEmpty()) {
            nombreAdultoMayor.postValue(abueloNombre);
            prefs.edit().putString("nombreAdultoMayor", abueloNombre).apply();
        }

        Object p = null, o = null, pres = null;

        if (doc.contains("ultimosSignosVitales")) {
            Object obj = doc.get("ultimosSignosVitales");
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                p = map.get("pulso");
                o = map.get("oxigeno");
                pres = map.get("presion");
            }
        }

        if (p == null) p = doc.get("pulso");
        if (o == null) o = doc.get("oxigeno");
        if (pres == null) pres = doc.get("presion");

        if (p != null || o != null || pres != null) {
            String pStr = (p != null) ? (p.toString().contains("LPM") ? p.toString() : p + " LPM") : "-- LPM";
            String oStr = (o != null) ? (o.toString().contains("%") ? o.toString() : o + "%") : "--%";
            String presStr = (pres != null) ? String.valueOf(pres) : "--/--";

            String res = "Pulso: " + pStr + " | O2: " + oStr + " | Presión: " + presStr;
            vitalsSummary.postValue(res);
        }
    }

    // --- Bloque 4: Métodos Getters para LiveData ---
    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getNombreAdultoMayor() { return nombreAdultoMayor; }
    public LiveData<Integer> getMedicinasCount() { return medicinasCount; }
    public LiveData<String> getVitalsSummary() { return vitalsSummary; }
}
