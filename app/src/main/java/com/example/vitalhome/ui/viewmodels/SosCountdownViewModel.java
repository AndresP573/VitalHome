package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.os.CountDownTimer;
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
 * ViewModel para gestionar la cuenta regresiva antes de disparar una alerta SOS automática.
 * Permite cancelar la cuenta regresiva, confirmar la emergencia o notificar un estado seguro.
 */
public class SosCountdownViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final AppRepo repo;
    private CountDownTimer timer;
    
    private final MutableLiveData<String> countdownText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timerFinished = new MutableLiveData<>();
    private final MutableLiveData<String> notifyingContactsText = new MutableLiveData<>();

    // --- Bloque 2: Constructor ---
    public SosCountdownViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        timerFinished.setValue(false);
        cargarContactosNotificacion(application);
    }

    private void cargarContactosNotificacion(Application application) {
        android.content.SharedPreferences prefs = application.getSharedPreferences("VitalHomePrefs", android.content.Context.MODE_PRIVATE);
        String n1 = prefs.getString("client_contact_name_1", "").trim();
        String n2 = prefs.getString("client_contact_name_2", "").trim();

        String aviso;
        if (!n1.isEmpty() && !n2.isEmpty()) {
            aviso = "Avisando a: " + n1 + " y " + n2;
        } else if (!n1.isEmpty()) {
            aviso = "Avisando a: " + n1;
        } else if (!n2.isEmpty()) {
            aviso = "Avisando a: " + n2;
        } else {
            aviso = "Avisando a tus contactos de emergencia";
        }
        notifyingContactsText.setValue(aviso);
    }

    // --- Bloque 3: Gestión del Temporizador SOS ---
    /**
     * Inicia la cuenta regresiva de 10 segundos para la alerta SOS.
     */
    public void startTimer() {
        if (timer != null) return;
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownText.setValue("Notificando en " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                confirmSOS();
            }
        }.start();
    }

    /**
     * Cancela el temporizador activo si existe.
     */
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // --- Bloque 4: Acciones SOS y Notificaciones ---
    /**
     * Confirma la emergencia SOS, emite notificaciones y guarda el evento.
     */
    public void confirmSOS() {
        cancelTimer();
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("SOS", "Emergencia confirmada", "Casa", hora));

        // Sincronización Global en Tiempo Real a través de la Colección 'alerts' en Firestore
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("VitalHomePrefs", android.content.Context.MODE_PRIVATE);
        String abueloEmail = prefs.getString("email", "marta@vitalhome.com");
        String abueloNombre = prefs.getString("nombreAdultoMayor", prefs.getString("nombre", "Marta Rodríguez"));

        java.util.Map<String, Object> sosData = new java.util.HashMap<>();
        sosData.put("pacienteEmail", abueloEmail);
        sosData.put("pacienteNombre", abueloNombre);
        sosData.put("tipo", "SOS");
        sosData.put("titulo", "¡SOS ACTIVADO!");
        sosData.put("descripcion", "¡Emergencia SOS confirmada por " + abueloNombre + "!");
        sosData.put("timestamp", new Date());
        sosData.put("resuelto", false);

        com.google.firebase.firestore.FirebaseFirestore.getInstance("default")
            .collection("alerts")
            .add(sosData);

        timerFinished.setValue(true);
    }

    /**
     * Cancela la alerta informando que el usuario se encuentra a salvo.
     */
    public void estoyBien() {
        cancelTimer();
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("SOS", "Estoy bien: alerta cancelada", "Casa", hora));
        NotificationHelper.sosEstoyBien(getApplication(), "Familiar");
    }

    public void eventoConsumido() {
        timerFinished.setValue(false);
    }

    // --- Bloque 5: Getters y Limpieza ---
    public LiveData<String> getCountdownText() { return countdownText; }
    public LiveData<Boolean> getTimerFinished() { return timerFinished; }
    public LiveData<String> getNotifyingContactsText() { return notifyingContactsText; }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimer();
    }
}
