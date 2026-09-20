package com.example.vitalhome.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.ui.controladores.AppRepo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicamentosViewModel extends AndroidViewModel {

    private final AppRepo repo;
    private final MutableLiveData<List<Medication>> plan = new MutableLiveData<>();

    public MedicamentosViewModel(@NonNull Application app) {
        super(app);
        repo = new AppRepo(app);
        List<Medication> lista = repo.getMeds();
        ordenar(lista);
        plan.setValue(lista);
    }

    public LiveData<List<Medication>> getPlan() {
        return plan;
    }

    public void agregar(String nombre, String hora) {
        List<Medication> lista = plan.getValue();
        lista.add(0, new Medication("" + System.currentTimeMillis(), nombre, hora));
        ordenar(lista);
        repo.saveMeds(lista);
        plan.setValue(lista);
    }

    public void tomar(Medication m) {
        List<Medication> lista = plan.getValue();
        m.setTaken(true);
        repo.saveMeds(lista);
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", getTime()));
        ordenar(lista);
        plan.setValue(lista);
    }

    private void ordenar(List<Medication> lista) {
        lista.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}