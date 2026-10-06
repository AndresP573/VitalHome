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
 * Filtra las alertas usando un timestamp de limpieza para evitar que las alertas antiguas reaparezcan.
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
            List<AlertEvent> listaBase = repo.getEvents();
            events.setValue(listaBase != null ? listaBase : new ArrayList<>());
        } catch (Exception e) {
            Log.e("AlertasVM", "Error cargando eventos locales", e);
        }
    }

    private void iniciarEscuchaFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            firestoreListener = db.collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                    long limpiezaTs = p.getLong("alertas_limpieza_timestamp", 0L);

                    List<AlertEvent> listaAlertas = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Date ts = doc.getDate("timestamp");
                        // Si el evento ocurrió antes de la última limpieza, se omite de la vista activa
                        if (ts != null && ts.getTime() <= limpiezaTs) {
                            continue;
                        }

                        String tipo = doc.getString("tipo");
                        String desc = doc.getString("descripcion");
                        String abuelo = doc.getString("pacienteNombre");
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

    // --- Bloque 4: Operaciones de actualización y limpieza de eventos ---
    public void refresh() {
        if (events.getValue() == null || events.getValue().isEmpty()) {
            events.setValue(repo.getEvents());
        }
    }

    /**
     * Limpia completamente el historial de eventos localmente y actualiza el timestamp de limpieza
     * para que las alertas anteriores queden archivadas y no reaparezcan al entrar nuevas.
     */
    public void clearAll() {
        repo.saveEvents(new ArrayList<>());
        events.postValue(new ArrayList<>());

        try {
            SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
            p.edit().putLong("alertas_limpieza_timestamp", System.currentTimeMillis()).apply();
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
