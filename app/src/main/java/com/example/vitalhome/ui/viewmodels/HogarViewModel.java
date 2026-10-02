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
import java.util.Objects;

/**
 * ViewModel para gestionar el estado simulado de los sensores del hogar (puerta principal y humo/fuego).
 */
public class HogarViewModel extends AndroidViewModel {

    // --- Atributos de estado y repositorio ---
    private final AppRepo repo;
    private final MutableLiveData<Boolean> puertaAbierta = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fuegoDetectado = new MutableLiveData<>(false);

    /**
     * Constructor del ViewModel del Hogar.
     *
     * @param application Contexto de la aplicación.
     */
    public HogarViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
    }

    // --- Métodos de alternancia de estado de sensores ---
    /**
     * Alterna el estado del sensor de la puerta principal (Abierta / Cerrada) y emite notificaciones.
     */
    public void togglePuerta() {
        boolean nuevoEstado = !Objects.equals(Boolean.TRUE, puertaAbierta.getValue());
        puertaAbierta.setValue(nuevoEstado);

        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if (nuevoEstado) {
            NotificationHelper.puertaAbierta(getApplication());
            repo.addEvent(new AlertEvent("HOGAR", "Puerta principal abierta", "Casa", hora));
        } else {
            NotificationHelper.puertaCerrada(getApplication());
            repo.addEvent(new AlertEvent("HOGAR", "Puerta principal cerrada", "Casa", hora));
        }
    }

    /**
     * Alterna la simulación de detección de fuego o humo en el domicilio.
     */
    public void toggleFuego() {
        boolean nuevoEstado = !Objects.equals(Boolean.TRUE, fuegoDetectado.getValue());
        fuegoDetectado.setValue(nuevoEstado);

        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        if (nuevoEstado) {
            NotificationHelper.fuego(getApplication());
            repo.addEvent(new AlertEvent("HOGAR", "🔥 ¡Alerta de fuego!", "Casa", hora));
        } else {
            NotificationHelper.fuegoApagado(getApplication());
            repo.addEvent(new AlertEvent("HOGAR", "Alarma de fuego apagada", "Casa", hora));
        }
    }

    // --- Getters de LiveData ---
    public LiveData<Boolean> getPuertaAbierta() { return puertaAbierta; }
    public LiveData<Boolean> getFuegoDetectado() { return fuegoDetectado; }
}
