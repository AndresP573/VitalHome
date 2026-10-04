package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ViewModel encargado del mantenimiento y registro de medicamentos dentro del plan diario.
 * Filtra los medicamentos según el timestamp de limpieza para evitar que reaparezcan al cargar nuevos.
 */
public class MedicamentosViewModel extends AndroidViewModel {

    // --- Atributos de repositorio y LiveData ---
    private final AppRepo repo;
    private final MutableLiveData<List<Medication>> plan = new MutableLiveData<>(new ArrayList<>());
    private ListenerRegistration medsListener;

    public MedicamentosViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        subirMedsLocalesAFirestoreSiExisten();
        cargarPlan();
        iniciarEscuchaMedsFirestore();
    }

    private void subirMedsLocalesAFirestoreSiExisten() {
        try {
            List<Medication> localMeds = repo.getMeds();
            if (localMeds != null && !localMeds.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance("default");
                for (Medication m : localMeds) {
                    if (m != null) {
                        Map<String, Object> medData = new HashMap<>();
                        medData.put("id", m.getId());
                        medData.put("nombre", m.getNombre());
                        medData.put("hora", m.getHora());
                        medData.put("taken", m.isTaken());
                        medData.put("createdAt", System.currentTimeMillis());

                        db.collection("medications")
                            .document(m.getId())
                            .set(medData, SetOptions.merge());
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void cargarPlan() {
        try {
            List<Medication> listaBase = repo.getMeds();
            List<Medication> listaFiltrada = new ArrayList<>();
            if (listaBase != null) {
                for (Medication m : listaBase) {
                    if (m != null) {
                        listaFiltrada.add(m);
                    }
                }
            }

            ordenar(listaFiltrada);
            plan.setValue(listaFiltrada);
        } catch (Exception e) {
            Log.e("MedicamentosVM", "Error cargando plan local", e);
        }
    }

    private void iniciarEscuchaMedsFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance("default");
            medsListener = db.collection("medications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    
                    SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                    long limpiezaTs = p.getLong("limpieza_timestamp", 0L);
                    List<Medication> localMeds = repo.getMeds();

                    Map<String, Boolean> localTakenMap = new HashMap<>();
                    if (localMeds != null) {
                        for (Medication lm : localMeds) {
                            if (lm != null && lm.isTaken()) {
                                localTakenMap.put(lm.getId(), true);
                            }
                        }
                    }

                    List<Medication> listaMeds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String medDocId = doc.getString("id");
                        if (medDocId == null) medDocId = doc.getId();
                        String nombre = doc.getString("nombre");
                        String hora = doc.getString("hora");
                        Boolean takenNube = doc.getBoolean("taken");
                        Long createdAt = doc.getLong("createdAt");

                        // Si fue creado antes de la última limpieza, se omite de la vista activa
                        if (createdAt != null && createdAt <= limpiezaTs) {
                            continue;
                        }

                        boolean isTaken = Objects.equals(takenNube, Boolean.TRUE) || Objects.equals(localTakenMap.get(medDocId), Boolean.TRUE);

                        if (nombre != null && hora != null) {
                            listaMeds.add(new Medication(medDocId, nombre, hora, isTaken));
                        }
                    }
                    ordenar(listaMeds);
                    repo.saveMeds(listaMeds);
                    plan.postValue(listaMeds);
                });
        } catch (Exception ignored) {}
    }

    private void ordenar(List<Medication> lista) {
        if (lista != null) {
            try {
                lista.sort((m1, m2) -> {
                    if (m1 == null || m2 == null) return 0;
                    if (m1.isTaken() != m2.isTaken()) {
                        return m1.isTaken() ? 1 : -1;
                    }
                    String h1 = m1.getHora() != null ? m1.getHora() : "00:00";
                    String h2 = m2.getHora() != null ? m2.getHora() : "00:00";
                    return h1.compareTo(h2);
                });
            } catch (Exception ignored) {}
        }
    }

    public void agregar(String nombre, String hora) {
        try {
            String id = UUID.randomUUID().toString();
            Medication nuevoMed = new Medication(id, nombre, hora, false);

            List<Medication> listaBase = repo.getMeds();
            List<Medication> lista = new ArrayList<>(Objects.requireNonNullElseGet(listaBase, ArrayList::new));
            lista.add(0, nuevoMed);
            repo.saveMeds(lista);

            // Guardar y sincronizar en Firestore con timestamp actual
            try {
                Map<String, Object> medData = new HashMap<>();
                medData.put("id", nuevoMed.getId());
                medData.put("nombre", nuevoMed.getNombre());
                medData.put("hora", nuevoMed.getHora());
                medData.put("taken", false);
                medData.put("createdAt", System.currentTimeMillis());

                FirebaseFirestore.getInstance("default")
                    .collection("medications")
                    .document(nuevoMed.getId())
                    .set(medData, SetOptions.merge());
            } catch (Exception ignored) {}

            try {
                SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                if ("cliente".equals(p.getString("rol", "cuidador"))) {
                    MedicationAlarmScheduler.programar(getApplication(), nuevoMed);
                }
            } catch (Exception ignored) {}

            cargarPlan();
        } catch (Exception e) {
            Log.e("MedicamentosVM", "Error agregando medicamento", e);
        }
    }

    public void tomar(Medication m) {
        if (m == null) return;
        try {
            List<Medication> listaBase = repo.getMeds();
            List<Medication> nuevaLista = new ArrayList<>();
            if (listaBase != null) {
                for (Medication item : listaBase) {
                    if (item != null && !Objects.equals(item.getId(), m.getId())) {
                        nuevaLista.add(item);
                    }
                }
            }
            repo.saveMeds(nuevaLista);
            
            try {
                MedicationAlarmScheduler.cancelar(getApplication(), m.getId());
            } catch (Exception ignored) {}

            try {
                FirebaseFirestore.getInstance("default")
                    .collection("medications")
                    .document(m.getId())
                    .delete();
            } catch (Exception ignored) {}

            cargarPlan();
        } catch (Exception e) {
            Log.e("MedicamentosVM", "Error eliminando medicamento tomado", e);
        }
    }

    /**
     * Limpia la lista de medicamentos localmente y actualiza el timestamp de limpieza
     * para que los remedios anteriores queden archivados y no reaparezcan al cargar nuevos.
     */
    public void limpiarPlanYArchivarEnNube() {
        try {
            repo.saveMeds(new ArrayList<>());
            plan.postValue(new ArrayList<>());

            SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
            p.edit().putLong("limpieza_timestamp", System.currentTimeMillis()).apply();
        } catch (Exception e) {
            Log.e("MedicamentosVM", "Error limpiando plan localmente", e);
        }
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
