package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.AppExecutors;
import com.example.vitalhome.utils.DateUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel encargado de la gestión de la lista de eventos de alerta en la aplicación.
 * Implementa máquina de estados (EstadoAlertas) y ejecuta lecturas/escrituras en hilos de fondo diskIO.
 */
public class AlertasViewModel extends AndroidViewModel {

    private static final String TAG = "AlertasViewModel";

    public enum EstadoAlertas {
        CARGANDO,
        EXITO,
        VACIO,
        ERROR
    }

    private final AppRepo repo;
    private final MutableLiveData<List<AlertEvent>> events = new MutableLiveData<>();
    private final MutableLiveData<EstadoAlertas> estado = new MutableLiveData<>(EstadoAlertas.CARGANDO);
    private ListenerRegistration firestoreListener;

    public AlertasViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public AlertasViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        cargarEventosLocales();
        iniciarEscuchaFirestore();
    }

    public LiveData<EstadoAlertas> getEstado() {
        return estado;
    }

    public LiveData<List<AlertEvent>> getEvents() {
        return events;
    }

    private void cargarEventosLocales() {
        estado.setValue(EstadoAlertas.CARGANDO);
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<AlertEvent> listaBase = repo.getEvents();
                List<AlertEvent> res = listaBase != null ? listaBase : new ArrayList<>();
                events.postValue(res);
                if (res.isEmpty()) {
                    estado.postValue(EstadoAlertas.VACIO);
                } else {
                    estado.postValue(EstadoAlertas.EXITO);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cargando eventos locales", e);
                estado.postValue(EstadoAlertas.ERROR);
            }
        });
    }

    private void iniciarEscuchaFirestore() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            firestoreListener = db.collection("alerts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error escuchando alertas de Firestore", e);
                            estado.postValue(EstadoAlertas.ERROR);
                            return;
                        }
                        if (snapshots == null) return;

                        AppExecutors.getInstance().diskIO().execute(() -> {
                            try {
                                SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                                long limpiezaTs = p.getLong("alertas_limpieza_timestamp", 0L);

                                List<AlertEvent> listaAlertas = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    Date ts = doc.getDate("timestamp");
                                    if (ts != null && ts.getTime() <= limpiezaTs) {
                                        continue;
                                    }

                                    String tipo = doc.getString("tipo");
                                    String desc = doc.getString("descripcion");
                                    String abuelo = doc.getString("pacienteNombre");
                                    String hora = ts != null ? DateUtils.formatTime(ts) : "00:00";

                                    String detalleFinal = (abuelo != null ? abuelo + ": " : "") + (desc != null ? desc : "");
                                    listaAlertas.add(new AlertEvent(tipo != null ? tipo : "ALERTA", detalleFinal, "Casa", hora));
                                }
                                repo.saveEvents(listaAlertas);
                                events.postValue(listaAlertas);
                                if (listaAlertas.isEmpty()) {
                                    estado.postValue(EstadoAlertas.VACIO);
                                } else {
                                    estado.postValue(EstadoAlertas.EXITO);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error procesando alertas de Firestore", ex);
                                estado.postValue(EstadoAlertas.ERROR);
                            }
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar Firestore snapshot listener en AlertasViewModel", e);
            estado.postValue(EstadoAlertas.ERROR);
        }
    }

    public void refresh() {
        cargarEventosLocales();
    }

    public void clearAll() {
        estado.setValue(EstadoAlertas.CARGANDO);
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                repo.saveEvents(new ArrayList<>());
                events.postValue(new ArrayList<>());
                SharedPreferences p = getApplication().getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
                p.edit().putLong("alertas_limpieza_timestamp", System.currentTimeMillis()).apply();
                estado.postValue(EstadoAlertas.VACIO);
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando eventos", e);
                estado.postValue(EstadoAlertas.ERROR);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (firestoreListener != null) {
            firestoreListener.remove();
            firestoreListener = null;
        }
    }
}
