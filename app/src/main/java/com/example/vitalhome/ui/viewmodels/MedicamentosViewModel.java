package com.example.vitalhome.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.MedicationAlarmScheduler;
import com.example.vitalhome.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel responsable de la gestión del plan de medicamentos.
 * Permite agregar nuevos medicamentos, marcar la toma y programar/cancelar alarmas.
 */
public class MedicamentosViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final AppRepo repo;
    private final MutableLiveData<List<Medication>> plan = new MutableLiveData<>();

    // --- Bloque 2: Constructor e inicialización ---
    public MedicamentosViewModel(@NonNull Application app) {
        super(app);
        repo = new AppRepo(app);
        List<Medication> lista = repo.getMeds();
        ordenar(lista);
        plan.setValue(lista);
    }

    // --- Bloque 3: Acceso a LiveData ---
    public LiveData<List<Medication>> getPlan() {
        return plan;
    }

    // --- Bloque 4: Operaciones de medicamentos ---
    /**
     * Agrega un nuevo medicamento al plan, persiste los datos y programa la alarma correspondiente.
     */
    public void agregar(String nombre, String hora) {
        List<Medication> lista = plan.getValue();
        Medication nuevoMed = new Medication("" + System.currentTimeMillis(), nombre, hora);
        lista.add(0, nuevoMed);
        ordenar(lista);
        repo.saveMeds(lista);
        plan.setValue(lista);
        MedicationAlarmScheduler.programar(getApplication(), nuevoMed);
    }

    /**
     * Registra la toma de un medicamento, cancela su alarma y genera eventos y notificaciones.
     */
    public void tomar(Medication m) {
        List<Medication> lista = plan.getValue();
        m.setTaken(true);
        repo.saveMeds(lista);
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", getTime()));
        ordenar(lista);
        plan.setValue(lista);
        MedicationAlarmScheduler.cancelar(getApplication(), m.getId());
        NotificationHelper.medicamentoTomado(getApplication(), m.getNombre(), getTime());
    }

    // --- Bloque 5: Métodos auxiliares ---
    private void ordenar(List<Medication> lista) {
        lista.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
    }

    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }
}
