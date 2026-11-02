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
import com.example.vitalhome.utils.AppExecutors;
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
 * Implementa máquina de estados (EstadoMedicamentos) y ejecuta operaciones E/S en diskIO.
 */
public class MedicamentosViewModel extends AndroidViewModel {

    private static final String TAG = "MedicamentosViewModel";

    public enum EstadoMedicamentos {
        CARGANDO,
        EXITO,
        VACIO,
        ERROR
    }

    private final AppRepo repo;
    private final MutableLiveData<List<Medication>> plan = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<EstadoMedicamentos> estado = new MutableLiveData<>(EstadoMedicamentos.CARGANDO);
    private ListenerRegistration medsListener;

    public MedicamentosViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public MedicamentosViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        subirMedsLocalesAFirestoreSiExisten();
        cargarPlan();
        iniciarEscuchaMedsFirestore();
    }

    public LiveData<EstadoMedicamentos> getEstado() {
        return estado;
    }

    public LiveData<List<Medication>> getPlan() {
        return plan;
    }

    private void subirMedsLocalesAFirestoreSiExisten() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<Medication> localMeds = repo.getMeds();
                if (localMeds != null && !localMeds.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
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
            } catch (Exception e) {
                Log.e(TAG, "Error subiendo medicamentos locales a Firestore", e);
            }
        });
    }

    private void cargarPlan() {
        estado.setValue(EstadoMedicamentos.CARGANDO);
        AppExecutors.getInstance().diskIO().execute(() -> {
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
                plan.postValue(listaFiltrada);
                if (listaFiltrada.isEmpty()) {
                    estado.postValue(EstadoMedicamentos.VACIO);
                } else {
                    estado.postValue(EstadoMedicamentos.EXITO);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cargando plan local de medicamentos", e);
                estado.postValue(EstadoMedicamentos.ERROR);
            }
        });
    }

    private void iniciarEscuchaMedsFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            medsListener = db.collection("medications")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error en listener de Firestore medicamentos", e);
                            estado.postValue(EstadoMedicamentos.ERROR);
                            return;
                        }
                        if (snapshots == null) return;

                        AppExecutors.getInstance().diskIO().execute(() -> {
                            try {
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
                                if (listaMeds.isEmpty()) {
                                    estado.postValue(EstadoMedicamentos.VACIO);
                                } else {
                                    estado.postValue(EstadoMedicamentos.EXITO);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error procesando snapshot de Firestore", ex);
                                estado.postValue(EstadoMedicamentos.ERROR);
                            }
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar escucha de Firestore en medicamentos", e);
            estado.postValue(EstadoMedicamentos.ERROR);
        }
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
            } catch (Exception e) {
                Log.e(TAG, "Error ordenando medicamentos", e);
            }
        }
    }

    public void agregar(String nombre, String hora) {
        estado.setValue(EstadoMedicamentos.CARGANDO);
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String id = UUID.randomUUID().toString();
                Medication nuevoMed = new Medication(id, nombre, hora, false);

                List<Medication> listaBase = repo.getMeds();
                List<Medication> lista = new ArrayList<>(Objects.requireNonNullElseGet(listaBase, ArrayList::new));
                lista.add(0, nuevoMed);
                repo.saveMeds(lista);

                try {
                    Map<String, Object> medData = new HashMap<>();
                    medData.put("id", nuevoMed.getId());
                    medData.put("nombre", nuevoMed.getNombre());
                    medData.put("hora", nuevoMed.getHora());
                    medData.put("taken", false);
                    medData.put("createdAt", System.currentTimeMillis());

                    FirebaseFirestore.getInstance()
                            .collection("medications")
                            .document(nuevoMed.getId())
                            .set(medData, SetOptions.merge());
                } catch (Exception ex) {
                    Log.e(TAG, "Error guardando medicamento en Firestore", ex);
                }

                try {
                    SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                    if ("cliente".equals(p.getString("rol", "cuidador"))) {
                        MedicationAlarmScheduler.programar(getApplication(), nuevoMed);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error al programar alarma de medicamento", ex);
                }

                cargarPlan();
            } catch (Exception e) {
                Log.e(TAG, "Error agregando medicamento", e);
                estado.postValue(EstadoMedicamentos.ERROR);
            }
        });
    }

    public void tomar(Medication m) {
        if (m == null) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<Medication> listaBase = repo.getMeds();
                List<Medication> nuevaLista = new ArrayList<>();
                if (listaBase != null) {
                    for (Medication item : listaBase) {
                        if (item != null) {
                            if (Objects.equals(item.getId(), m.getId())) {
                                nuevaLista.add(new Medication(item.getId(), item.getNombre(), item.getHora(), true));
                            } else {
                                nuevaLista.add(item);
                            }
                        }
                    }
                }
                repo.saveMeds(nuevaLista);

                try {
                    MedicationAlarmScheduler.cancelar(getApplication(), m.getId());
                } catch (Exception ex) {
                    Log.e(TAG, "Error cancelando alarma al tomar medicamento", ex);
                }

                try {
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("taken", true);

                    FirebaseFirestore.getInstance()
                            .collection("medications")
                            .document(m.getId())
                            .set(updateData, SetOptions.merge());
                } catch (Exception ex) {
                    Log.e(TAG, "Error actualizando estado taken en Firestore", ex);
                }

                cargarPlan();
            } catch (Exception e) {
                Log.e(TAG, "Error marcando medicamento como tomado", e);
            }
        });
    }

    public void limpiarPlanYArchivarEnNube() {
        estado.setValue(EstadoMedicamentos.CARGANDO);
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                repo.saveMeds(new ArrayList<>());
                plan.postValue(new ArrayList<>());

                try {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("medications").get().addOnSuccessListener(snapshots -> {
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                db.collection("medications").document(doc.getId()).delete();
                            }
                        }
                    });
                } catch (Exception ex) {
                    Log.e(TAG, "Error limpiando documentos de medications en Firestore", ex);
                }

                SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                p.edit().putLong("limpieza_timestamp", System.currentTimeMillis()).apply();
                estado.postValue(EstadoMedicamentos.VACIO);
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando plan de medicamentos", e);
                estado.postValue(EstadoMedicamentos.ERROR);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (medsListener != null) {
            medsListener.remove();
            medsListener = null;
        }
    }
}
