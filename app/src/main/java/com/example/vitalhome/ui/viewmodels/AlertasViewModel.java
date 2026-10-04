package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.repository.AppRepo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel encargado de la gestión de la lista de eventos de alerta en la aplicación.
 * Limpia la vista de alertas de forma exclusivamente local sin borrar los registros de Cloud Firestore.
 */
public class AlertasViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y dependencias ---
    private final AppRepo repo;
    private final MutableLiveData<List<AlertEvent>> events = new MutableLiveData<>();
    private ListenerRegistration firestoreListener;

    // --- Bloque 2: Constructor e inicialización ---
    public AlertasViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        cargarEventosLocales();
        iniciarEscuchaFirestore();
    }

    private void cargarEventosLocales() {
        try {
            SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
            boolean limpiadoLocal = p.getBoolean("alertas_limpiadas_localmente", false);

            List<AlertEvent> listaBase = repo.getEvents();
            if (limpiadoLocal && (listaBase == null || listaBase.isEmpty())) {
                events.setValue(new ArrayList<>());
                return;
            }

            events.setValue(listaBase != null ? listaBase : new ArrayList<>());
        } catch (Exception e) {
            Log.e("AlertasVM", "Error cargando eventos locales", e);
        }
    }

    private void iniciarEscuchaFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            firestoreListener = db.collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                    boolean limpiadoLocal = p.getBoolean("alertas_limpiadas_localmente", false);
                    List<AlertEvent> localEvents = repo.getEvents();

                    if (limpiadoLocal && (localEvents == null || localEvents.isEmpty())) {
                        events.postValue(new ArrayList<>());
                        return;
                    }

                    List<AlertEvent> listaAlertas = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String tipo = doc.getString("tipo");
                        String desc = doc.getString("descripcion");
                        String abuelo = doc.getString("pacienteNombre");
                        Date ts = doc.getDate("timestamp");
                        String hora = ts != null ? new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts) : "00:00";

                        String detalleFinal = (abuelo != null ? abuelo + ": " : "") + (desc != null ? desc : "");
                        listaAlertas.add(new AlertEvent(tipo != null ? tipo : "ALERTA", detalleFinal, "Casa", hora));
                    }
                    repo.saveEvents(listaAlertas);
                    events.postValue(listaAlertas);
                });
        } catch (Exception ignored) {}
    }

    // --- Bloque 3: Acceso a LiveData ---
    public LiveData<List<AlertEvent>> getEvents() {
        return events;
    }

    // --- Bloque 4: Operaciones de actualización y limpieza local de eventos ---
    public void refresh() {
        SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        boolean limpiadoLocal = p.getBoolean("alertas_limpiadas_localmente", false);
        if (limpiadoLocal && (repo.getEvents() == null || repo.getEvents().isEmpty())) {
            events.setValue(new ArrayList<>());
            return;
        }

        if (events.getValue() == null || events.getValue().isEmpty()) {
            events.setValue(repo.getEvents());
        }
    }

    /**
     * Limpia completamente el historial de eventos localmente en el dispositivo
     * reservando el 100% de los documentos en la colección 'alerts' de Cloud Firestore para análisis futuro.
     */
    public void clearAll() {
        repo.saveEvents(new ArrayList<>());
        events.postValue(new ArrayList<>());

        try {
            SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
            p.edit().putBoolean("alertas_limpiadas_localmente", true).apply();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }
}
