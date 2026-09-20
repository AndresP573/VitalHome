package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.ui.controladores.AppRepo;
import java.util.List;

public class AlertasViewModel extends AndroidViewModel {
    private final AppRepo repo;
    private final MutableLiveData<List<AlertEvent>> events = new MutableLiveData<>();

    public AlertasViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        events.setValue(repo.getEvents());
    }

    public LiveData<List<AlertEvent>> getEvents() {
        return events;
    }

    public void refresh() {
        List<AlertEvent> lista = events.getValue();
        if (lista != null) {
            lista.clear();
            lista.addAll(repo.getEvents());
            events.setValue(lista);
        }
    }

    public void clearAll() {
        List<AlertEvent> lista = events.getValue();
        if (lista != null) {
            lista.clear();
            repo.saveEvents(lista);
            events.setValue(lista);
        }
    }
}
