package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.MedicationAlarmScheduler;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ViewModel encargado del mantenimiento y registro de medicamentos dentro del plan diario.
 * Sincroniza en tiempo real los medicamentos desde Cloud Firestore.
 */
public class MedicamentosViewModel extends AndroidViewModel {

    // --- Atributos de repositorio y LiveData ---
    private final AppRepo repo;
    private final MutableLiveData<List<Medication>> plan = new MutableLiveData<>();
    private ListenerRegistration medsListener;

    public MedicamentosViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        cargarPlan();
        iniciarEscuchaMedsFirestore();
    }

    private void cargarPlan() {
        List<Medication> lista = repo.getMeds();
        ordenar(lista);
        plan.setValue(lista);
    }

    private void iniciarEscuchaMedsFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            medsListener = db.collection("medications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    List<Medication> listaMeds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String id = doc.getString("id");
                        String nombre = doc.getString("nombre");
                        String hora = doc.getString("hora");
                        Boolean taken = doc.getBoolean("taken");

                        if (id != null && nombre != null && hora != null) {
                            listaMeds.add(new Medication(id, nombre, hora, Boolean.TRUE.equals(taken)));
                        }
                    }
                    if (!listaMeds.isEmpty()) {
                        ordenar(listaMeds);
                        repo.saveMeds(listaMeds);
                        plan.postValue(listaMeds);
                    }
                });
        } catch (Exception ignored) {}
    }

    private void ordenar(List<Medication> lista) {
        if (lista != null) {
            lista.sort((m1, m2) -> {
                if (m1.isTaken() != m2.isTaken()) {
                    return m1.isTaken() ? 1 : -1;
                }
                return m1.getHora().compareTo(m2.getHora());
            });
        }
    }

    public void agregar(String nombre, String hora) {
        String id = UUID.randomUUID().toString();
        Medication nuevoMed = new Medication(id, nombre, hora, false);

        List<Medication> lista = repo.getMeds();
        if (lista == null) {
            lista = new ArrayList<>();
        }
        lista.add(0, nuevoMed);
        repo.saveMeds(lista);

        // Guardar y sincronizar en Firestore
        try {
            Map<String, Object> medData = new HashMap<>();
            medData.put("id", nuevoMed.getId());
            medData.put("nombre", nuevoMed.getNombre());
            medData.put("hora", nuevoMed.getHora());
            medData.put("taken", false);

            FirebaseFirestore.getInstance("default")
                .collection("medications")
                .document(nuevoMed.getId())
                .set(medData);
        } catch (Exception ignored) {}

        MedicationAlarmScheduler.programar(getApplication(), nuevoMed);
        cargarPlan();
    }

    public void tomar(Medication m) {
        List<Medication> lista = repo.getMeds();
        if (lista != null) {
            for (Medication item : lista) {
                if (item.getId().equals(m.getId())) {
                    item.setTaken(true);
                    break;
                }
            }
            repo.saveMeds(lista);
            MedicationAlarmScheduler.cancelar(getApplication(), m.getId());

            // Actualizar estado en Firestore
            try {
                FirebaseFirestore.getInstance("default")
                    .collection("medications")
                    .document(m.getId())
                    .update("taken", true);
            } catch (Exception ignored) {}
        }
        cargarPlan();
    }

    public LiveData<List<Medication>> getPlan() { return plan; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (medsListener != null) {
            medsListener.remove();
            medsListener = null;
        }
    }
}
