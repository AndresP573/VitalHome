package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.json.JSONObject;

/**
 * ViewModel encargado de administrar la lectura de los datos de la pulsera inteligente.
 */
public class PulseraViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final SharedPreferences prefs;
    
    private final MutableLiveData<String> pulso = new MutableLiveData<>();
    private final MutableLiveData<String> oxigeno = new MutableLiveData<>();
    private final MutableLiveData<String> presion = new MutableLiveData<>();

    // --- Bloque 2: Constructor e inicialización ---
    public PulseraViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        loadVitals();
    }

    // --- Bloque 3: Carga de signos vitales almacenados ---
    private void loadVitals() {
        String data = prefs.getString("ultimos_vitals", "{}");
        try {
            JSONObject obj = new JSONObject(data);
            pulso.setValue(obj.has("pulso") ? obj.getInt("pulso") + " LPM" : "-- LPM");
            oxigeno.setValue(obj.has("oxig") ? obj.getInt("oxig") + "%" : "--%");
            presion.setValue(obj.has("pres") ? obj.getString("pres") : "--/--");
        } catch (Exception e) {
            pulso.setValue("-- LPM");
            oxigeno.setValue("--%");
            presion.setValue("--/--");
        }
    }

    // --- Bloque 4: Métodos Getters ---
    public LiveData<String> getPulso() { return pulso; }
    public LiveData<String> getOxigeno() { return oxigeno; }
    public LiveData<String> getPresion() { return presion; }
}
