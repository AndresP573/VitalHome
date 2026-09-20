package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.ui.controladores.AppRepo;
import com.example.vitalhome.utils.NotificationHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SosCountdownViewModel extends AndroidViewModel {
    private final AppRepo repo;
    private CountDownTimer timer;
    
    private final MutableLiveData<String> countdownText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timerFinished = new MutableLiveData<>();

    public SosCountdownViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        timerFinished.setValue(false);
    }

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

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void confirmSOS() {
        cancelTimer();
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("SOS", "Emergencia confirmada", "Casa", hora));
        NotificationHelper.sendSOS(getApplication(), "Familiar");
        timerFinished.setValue(true);
    }

    public void estoyBien() {
        cancelTimer();
        String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        repo.addEvent(new AlertEvent("SOS", "Estoy bien: alerta cancelada", "Casa", hora));
        NotificationHelper.sosEstoyBien(getApplication(), "Familiar");
    }

    public void eventoConsumido() {
        timerFinished.setValue(false);
    }

    public LiveData<String> getCountdownText() { return countdownText; }
    public LiveData<Boolean> getTimerFinished() { return timerFinished; }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimer();
    }
}
