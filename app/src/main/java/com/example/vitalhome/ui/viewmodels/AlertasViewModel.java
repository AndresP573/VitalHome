package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.repository.AppRepo;
import java.util.List;

/**
 * ViewModel encargado de la gestión de la lista de eventos de alerta en la aplicación.
 * Proporciona métodos para consultar, refrescar y eliminar alertas registradas.
 */
public class AlertasViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y dependencias ---
    private final AppRepo repo;
    private final MutableLiveData<List<AlertEvent>> events = new MutableLiveData<>();

    // --- Bloque 2: Constructor e inicialización ---
    public AlertasViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        events.setValue(repo.getEvents());
    }

    // --- Bloque 3: Acceso a LiveData ---
    public LiveData<List<AlertEvent>> getEvents() {
        return events;
    }

    // --- Bloque 4: Operaciones de actualización y limpieza de eventos ---
    /**
     * Refresca la lista de eventos consultando la fuente de datos actual.
     */
    public void refresh() {
        events.setValue(repo.getEvents());
    }

    /**
     * Elimina todas las alertas registradas y persiste el estado.
     */
    public void clearAll() {
        repo.saveEvents(new java.util.ArrayList<>());
        events.setValue(new java.util.ArrayList<>());
    }
}
