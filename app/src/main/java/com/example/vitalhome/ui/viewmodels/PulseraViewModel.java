package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.AppExecutors;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import org.json.JSONObject;
import java.util.Map;

/**
 * ViewModel encargado de administrar la lectura de los datos de la pulsera inteligente.
 * Escucha y consulta en tiempo real los signos vitales medidos por el adulto mayor desde Cloud Firestore.
 */
public class PulseraViewModel extends AndroidViewModel {

    private static final String TAG = "PulseraViewModel";

    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> pulso = new MutableLiveData<>("-- LPM");
    private final MutableLiveData<String> oxigeno = new MutableLiveData<>("--%");
    private final MutableLiveData<String> presion = new MutableLiveData<>("--/--");
    private ListenerRegistration vitalsListener;

    public PulseraViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public PulseraViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        this.prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        loadVitalsLocal();
        cargarSignosVitalesDesdeFirestore();
        escucharSignosVitalesEnVivo();
    }

    private void loadVitalsLocal() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String data = prefs.getString("ultimos_vitals", "{}");
                JSONObject obj = new JSONObject(data);
                if (obj.has("pulso")) pulso.postValue(obj.getInt("pulso") + " LPM");
                if (obj.has("oxig")) oxigeno.postValue(obj.getInt("oxig") + "%");
                if (obj.has("pres")) presion.postValue(obj.getString("pres"));
            } catch (Exception e) {
                Log.e(TAG, "Error leyendo ultimos_vitals locales en PulseraViewModel", e);
            }
        });
    }

    public void cargarSignosVitalesDesdeFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("vitals").document("latest").get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            actualizarVitalsDesdeDoc(doc);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error cargando vitals desde Firestore", e));
        } catch (Exception e) {
            Log.e(TAG, "Error al consultar colección vitals en Firestore", e);
        }
    }

    private void escucharSignosVitalesEnVivo() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            vitalsListener = db.collection("vitals").document("latest")
                    .addSnapshotListener((doc, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error en listener de vitals en Firestore", e);
                            return;
                        }
                        if (doc == null || !doc.exists()) return;
                        actualizarVitalsDesdeDoc(doc);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar listener de vitals en vivo", e);
        }
    }

    private void actualizarVitalsDesdeDoc(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                Object pObj = doc.contains("pulso") ? doc.get("pulso") : null;
                Object oObj = doc.contains("oxigeno") ? doc.get("oxigeno") : null;
                Object presObj = doc.contains("presion") ? doc.get("presion") : null;

                if (doc.contains("ultimosSignosVitales")) {
                    Object obj = doc.get("ultimosSignosVitales");
                    if (obj instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) obj;
                        if (pObj == null) pObj = map.get("pulso");
                        if (oObj == null) oObj = map.get("oxigeno");
                        if (presObj == null) presObj = map.get("presion");
                    }
                }

                if (pObj != null) {
                    String pStr = pObj.toString();
                    pulso.postValue(pStr.contains("LPM") ? pStr : pStr + " LPM");
                }
                if (oObj != null) {
                    String oStr = oObj.toString();
                    oxigeno.postValue(oStr.contains("%") ? oStr : oStr + "%");
                }
                if (presObj != null) {
                    presion.postValue(String.valueOf(presObj));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando vitals desde DocumentSnapshot", e);
            }
        });
    }

    public LiveData<String> getPulso() { return pulso; }
    public LiveData<String> getOxigeno() { return oxigeno; }
    public LiveData<String> getPresion() { return presion; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (vitalsListener != null) {
            vitalsListener.remove();
            vitalsListener = null;
        }
    }
}
