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
import java.util.Map;

/**
 * ViewModel correspondiente al Dashboard del Cuidador.
 * Proporciona información general sobre el adulto mayor asignado mediante escucha optimizada en Cloud Firestore.
 */
public class DashboardCuidadorViewModel extends AndroidViewModel {

    private static final String TAG = "DashboardCuidadorVM";

    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> nombreAdultoMayor = new MutableLiveData<>();
    private final MutableLiveData<Integer> medicinasCount = new MutableLiveData<>();

    private final MutableLiveData<String> vitalsSummary = new MutableLiveData<>("🔋 85% | 📍 En el hogar");
    private ListenerRegistration pacienteListener;

    public DashboardCuidadorViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public DashboardCuidadorViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        this.prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
        vincularYEscucharPacienteEspecifico();
    }

    public void refreshData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                nombreUsuario.postValue(prefs.getString("nombre", "Usuario"));
                nombreAdultoMayor.postValue(prefs.getString("nombreAdultoMayor", "Familiar"));
                repo.getMedsAsync(meds -> medicinasCount.postValue(meds != null ? meds.size() : 0));
            } catch (Exception e) {
                Log.e(TAG, "Error refrescando datos en DashboardCuidadorViewModel", e);
            }
        });
    }

    /**
     * Vinculación optimizada: Filtra específicamente por el documento del cliente/paciente asignado si existe,
     * o limita la consulta a 1 paciente cliente en lugar de escuchar a toda la colección indescriminadamente.
     */
    public void vincularYEscucharPacienteEspecifico() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String emailCliente = prefs.getString("email_cliente", "");

            if (!emailCliente.isEmpty()) {
                String docId = emailCliente.replace(".", "_");
                pacienteListener = db.collection("users").document(docId)
                        .addSnapshotListener((doc, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error escuchando paciente específico en Firestore", e);
                                return;
                            }
                            if (doc != null && doc.exists()) {
                                procesarDocPaciente(doc);
                            }
                        });
            } else {
                pacienteListener = db.collection("users")
                        .whereEqualTo("rol", "cliente")
                        .limit(1)
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error escuchando paciente cliente en Firestore", e);
                                return;
                            }
                            if (snapshots == null || snapshots.isEmpty()) return;
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                if (doc != null && doc.exists()) {
                                    procesarDocPaciente(doc);
                                    break;
                                }
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error vinculando paciente específico", e);
        }
    }

    private void procesarDocPaciente(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
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
            } catch (Exception e) {
                Log.e(TAG, "Error procesando documento de paciente", e);
            }
        });
    }

    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getNombreAdultoMayor() { return nombreAdultoMayor; }
    public LiveData<Integer> getMedicinasCount() { return medicinasCount; }
    public LiveData<String> getVitalsSummary() { return vitalsSummary; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (pacienteListener != null) {
            pacienteListener.remove();
            pacienteListener = null;
        }
    }
}
