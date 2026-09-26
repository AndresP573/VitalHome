package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.NotificationHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel encargado del monitoreo y control de sensores del hogar (puerta y detección de fuego).
 */
public class HogarViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final AppRepo repo;
    private final MutableLiveData<Boolean> puertaAbierta = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fuegoDetectado = new MutableLiveData<>(false);

    // --- Bloque 2: Constructor ---
    public HogarViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
    }

    // --- Bloque 3: Accessores LiveData ---
    public LiveData<Boolean> getPuertaAbierta() { return puertaAbierta; }
    public LiveData<Boolean> getFuegoDetectado() { return fuegoDetectado; }

    // --- Bloque 4: Control de estados de los sensores del hogar ---
    /**
     * Alterna el estado del sensor de puerta y genera una alerta/notificación si se abre.
     */
    public void togglePuerta() {
        boolean nuevoEstado = !Boolean.TRUE.equals(puertaAbierta.getValue());
        puertaAbierta.setValue(nuevoEstado);
        
        if (nuevoEstado) {
            String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            repo.addEvent(new AlertEvent("HOGAR", "Puerta principal abierta", "Casa", hora));
            NotificationHelper.puertaAbierta(getApplication());
        }
    }

    /**
     * Alterna el estado del sensor de fuego y desencadena una alerta SOS si se activa.
     */
    public void toggleFuego() {
        boolean nuevoEstado = !Boolean.TRUE.equals(fuegoDetectado.getValue());
        fuegoDetectado.setValue(nuevoEstado);
        
        if (nuevoEstado) {
            String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            repo.addEvent(new AlertEvent("SOS", "¡Fuego detectado!", "Casa", hora));
            NotificationHelper.fuego(getApplication());
        }
    }
}
