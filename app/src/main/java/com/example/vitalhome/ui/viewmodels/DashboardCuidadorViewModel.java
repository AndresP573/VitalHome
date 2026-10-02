package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.repository.AppRepo;

/**
 * ViewModel correspondiente al Dashboard del Cuidador.
 * Proporciona información general sobre el adulto mayor asignado y el estado de sus medicamentos.
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
        escucharSignosVitalesEnVivo();
    }

    // --- Bloque 3: Carga y actualización de datos ---
    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombre", "Usuario"));
        nombreAdultoMayor.setValue(prefs.getString("nombreAdultoMayor", "Familiar"));
        medicinasCount.setValue(repo.getMeds().size());
    }

    // --- Escuchar Signos Vitales del Abuelo en vivo desde Cloud Firestore ---
    public void escucharSignosVitalesEnVivo() {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance("default")
                .collection("users")
                .whereEqualTo("rol", "cliente")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (doc.contains("ultimosSignosVitales")) {
                            java.util.Map<String, Object> vitals = (java.util.Map<String, Object>) doc.get("ultimosSignosVitales");
                            if (vitals != null) {
                                Object p = vitals.get("pulso");
                                Object o = vitals.get("oxigeno");
                                Object pres = vitals.get("presion");
                                String res = "Pulso: " + p + " LPM | O2: " + o + "% | Presión: " + pres;
                                vitalsSummary.postValue(res);
                            }
                        }
                    }
                });
        } catch (Exception ignored) {}
    }

    // --- Bloque 4: Métodos Getters para LiveData ---
    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getNombreAdultoMayor() { return nombreAdultoMayor; }
    public LiveData<Integer> getMedicinasCount() { return medicinasCount; }
    public LiveData<String> getVitalsSummary() { return vitalsSummary; }
}
