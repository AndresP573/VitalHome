package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
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
 * Sincroniza en tiempo real los eventos de alerta desde Cloud Firestore.
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
        events.setValue(repo.getEvents());
        iniciarEscuchaFirestore();
    }

    private void iniciarEscuchaFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            firestoreListener = db.collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
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
                    if (!listaAlertas.isEmpty()) {
                        events.postValue(listaAlertas);
                    }
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

    public void clearAll() {
        repo.saveEvents(new ArrayList<>());
        events.setValue(new ArrayList<>());
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
