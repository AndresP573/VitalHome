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
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.AppExecutors;
import com.example.vitalhome.utils.DateUtils;
import com.example.vitalhome.utils.MedicationAlarmScheduler;
import com.example.vitalhome.utils.NotificationHelper;
import com.example.vitalhome.utils.VitalSignsThresholds;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ViewModel para el Dashboard del usuario (Cliente / Adulto Mayor).
 * Administra los signos vitales, medicamentos del día, contactos de emergencia y mediciones de salud.
 */
public class DashboardClienteViewModel extends AndroidViewModel {

    private static final String TAG = "DashboardClienteVM";

    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> pulso = new MutableLiveData<>("-- LPM");
    private final MutableLiveData<String> oxigeno = new MutableLiveData<>("--%");
    private final MutableLiveData<List<Medication>> medicamentos = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> contactName1 = new MutableLiveData<>();
    private final MutableLiveData<String> contactPhone1 = new MutableLiveData<>();
    private final MutableLiveData<String> contactName2 = new MutableLiveData<>();
    private final MutableLiveData<String> contactPhone2 = new MutableLiveData<>();

    private final MutableLiveData<Integer> tabIndex = new MutableLiveData<>(1);

    /**
     * Clase estática para encapsular los datos de actualización de signos vitales.
     * Encapsulada con propiedades privadas e inmutables, constructor explícito y getters públicos.
     */
    public static class VitalsUpdate {
        private final int pulso;
        private final int oxigeno;
        private final String presion;

        public VitalsUpdate(int pulso, int oxigeno, String presion) {
            this.pulso = pulso;
            this.oxigeno = oxigeno;
            this.presion = presion;
        }

        public int getPulso() {
            return pulso;
        }

        public int getOxigeno() {
            return oxigeno;
        }

        public String getPresion() {
            return presion;
        }
    }

    private final MutableLiveData<VitalsUpdate> lastVitalsUpdate = new MutableLiveData<>();

    public DashboardClienteViewModel(@NonNull Application application) {
        this(application, new AppRepo(application));
    }

    public DashboardClienteViewModel(@NonNull Application application, @NonNull AppRepo repo) {
        super(application);
        this.repo = repo;
        this.prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
        iniciarEscuchaMedsFirestore();
    }

    public void iniciarEscuchaMedsFirestore() {
        try {
            FirebaseFirestore.getInstance()
                    .collection("medications")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error escuchando medicamentos en Firestore", e);
                            return;
                        }
                        if (snapshots == null) return;

                        AppExecutors.getInstance().diskIO().execute(() -> {
                            try {
                                List<Medication> listaMeds = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    String id = doc.getString("id");
                                    if (id == null) id = doc.getId();
                                    String nombre = doc.getString("nombre");
                                    String hora = doc.getString("hora");
                                    Boolean taken = doc.getBoolean("taken");

                                    if (nombre != null && hora != null && !Boolean.TRUE.equals(taken)) {
                                        Medication m = new Medication(id, nombre, hora, false);
                                        listaMeds.add(m);
                                        try {
                                            MedicationAlarmScheduler.programar(getApplication(), m);
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Error al programar alarma para medicamento descargado", ex);
                                        }
                                    }
                                }
                                repo.saveMeds(listaMeds);
                                medicamentos.postValue(listaMeds);
                            } catch (Exception ex) {
                                Log.e(TAG, "Error procesando snapshot de medicamentos en DashboardCliente", ex);
                            }
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error iniciando escucha de Firestore en DashboardClienteViewModel", e);
        }
    }

    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombreAdultoMayor", "Usuario"));
        refrescarSignos();
        refrescarMeds();
        cargarContactos();
    }

    private void refrescarSignos() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String json = prefs.getString("ultimos_vitals", "{}");
                JSONObject data = new JSONObject(json);
                if (data.has("pulso")) pulso.postValue(data.getInt("pulso") + " LPM");
                if (data.has("oxig")) oxigeno.postValue(data.getInt("oxig") + "%");
            } catch (Exception e) {
                Log.e(TAG, "Error al parsear ultimos_vitals desde SharedPreferences", e);
            }
        });
    }

    private void refrescarMeds() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<Medication> items = repo.getMeds();
                List<Medication> sinTomar = new ArrayList<>();
                if (items != null) {
                    for (Medication m : items) {
                        if (m != null && !m.isTaken()) {
                            sinTomar.add(m);
                        }
                    }
                }
                medicamentos.postValue(sinTomar);
            } catch (Exception e) {
                Log.e(TAG, "Error refrescando medicamentos en DashboardClienteViewModel", e);
            }
        });
    }

    private void cargarContactos() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                contactName1.postValue(prefs.getString("client_contact_name_1", ""));
                contactPhone1.postValue(prefs.getString("client_contact_phone_1", ""));
                contactName2.postValue(prefs.getString("client_contact_name_2", ""));
                contactPhone2.postValue(prefs.getString("client_contact_phone_2", ""));
            } catch (Exception e) {
                Log.e(TAG, "Error cargando contactos de emergencia", e);
            }
        });
    }

    public void guardarContactos(String n1, String p1, String n2, String p2) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                prefs.edit()
                        .putString("client_contact_name_1", n1)
                        .putString("client_contact_phone_1", p1)
                        .putString("client_contact_name_2", n2)
                        .putString("client_contact_phone_2", p2)
                        .apply();
                cargarContactos();
            } catch (Exception e) {
                Log.e(TAG, "Error guardando contactos de emergencia", e);
            }
        });
    }

    public void tomar(Medication m) {
        if (m == null) return;

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                String horaStr = DateUtils.getCurrentTimeFormatted();
                repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", horaStr));

                List<Medication> items = repo.getMeds();
                List<Medication> nuevaLista = new ArrayList<>();
                if (items != null) {
                    for (Medication item : items) {
                        if (item != null && !Objects.equals(item.getId(), m.getId()) && !item.isTaken()) {
                            nuevaLista.add(item);
                        }
                    }
                }
                repo.saveMeds(nuevaLista);
                medicamentos.postValue(nuevaLista);
                MedicationAlarmScheduler.cancelar(getApplication(), m.getId());

                try {
                    FirebaseFirestore.getInstance()
                            .collection("medications")
                            .document(m.getId())
                            .set(Collections.singletonMap("taken", true), SetOptions.merge());
                } catch (Exception ex) {
                    Log.e(TAG, "Error actualizando estado del medicamento en Firestore", ex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar toma de medicamento", e);
            }
        });
    }

    public void limpiarRemedios() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                List<Medication> items = repo.getMeds();
                if (items != null) {
                    for (Medication m : items) {
                        if (m != null) {
                            MedicationAlarmScheduler.cancelar(getApplication(), m.getId());
                        }
                    }
                }
                repo.saveMeds(new ArrayList<>());
                prefs.edit().putBoolean("plan_limpiado_localmente", true).apply();
                medicamentos.postValue(new ArrayList<>());
            } catch (Exception e) {
                Log.e(TAG, "Error limpiando remedios en DashboardClienteViewModel", e);
            }
        });
    }

    public void medirAhora() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                int p;
                if (Math.random() < 0.25) {
                    p = (int) (Math.random() * 30 + 101);
                } else {
                    p = (int) (Math.random() * 31 + 60);
                }

                int o;
                if (Math.random() < 0.15) {
                    o = (int) (Math.random() * 4 + 88);
                } else {
                    o = (int) (Math.random() * 5 + 95);
                }

                int sis, dia;
                if (Math.random() < 0.25) {
                    sis = (int) (Math.random() * 26 + 140);
                    dia = (int) (Math.random() * 16 + 90);
                } else {
                    sis = (int) (Math.random() * 20 + 110);
                    dia = (int) (Math.random() * 11 + 70);
                }
                String presString = sis + "/" + dia;

                pulso.postValue(p + " LPM");
                oxigeno.postValue(o + "%");
                lastVitalsUpdate.postValue(new VitalsUpdate(p, o, presString));

                prefs.edit().putString("ultimos_vitals", "{\"pulso\":" + p + ",\"oxig\":" + o + ",\"pres\":\"" + presString + "\"}").apply();

                String horaStr = DateUtils.getCurrentTimeFormatted();
                List<String> anomalias = new ArrayList<>();

                if (VitalSignsThresholds.isPulsoAnomalo(p)) {
                    String msg = p > VitalSignsThresholds.PULSO_MAX_NORMAL ? ("Pulso elevado: " + p + " LPM") : ("Pulso bajo: " + p + " LPM");
                    anomalias.add(msg);
                }

                if (VitalSignsThresholds.isPresionAnomala(sis, dia)) {
                    anomalias.add("Presión elevada: " + presString + " mmHg");
                }

                if (VitalSignsThresholds.isOxigenoAnomalo(o)) {
                    anomalias.add("Oxígeno bajo: " + o + "%");
                }

                if (!anomalias.isEmpty()) {
                    String resumenAnomalias = String.join(" | ", anomalias);
                    NotificationHelper.notificarAlertaCuidador(getApplication(), "SALUD", "⚠️ Alerta de Salud", resumenAnomalias);
                    repo.addEvent(new AlertEvent("SALUD", resumenAnomalias, "Casa", horaStr));
                } else {
                    NotificationHelper.pulso(getApplication(), p);
                    repo.addEvent(new AlertEvent("SALUD", "Control: " + p + " LPM | O2: " + o + "% | Presión: " + presString, "Casa", horaStr));
                }

                try {
                    String abueloEmail = prefs.getString("email", "marta@vitalhome.com").replace(".", "_");
                    String abueloNombre = prefs.getString("nombreAdultoMayor", "Adulto Mayor");

                    Map<String, Object> vitalsData = new HashMap<>();
                    vitalsData.put("pulso", p + " LPM");
                    vitalsData.put("oxigeno", o + "%");
                    vitalsData.put("presion", presString);
                    vitalsData.put("pacienteNombre", abueloNombre);
                    vitalsData.put("updatedAt", new Date());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(abueloEmail)
                            .set(Collections.singletonMap("ultimosSignosVitales", vitalsData), SetOptions.merge());

                    db.collection("vitals")
                            .document("latest")
                            .set(vitalsData);
                } catch (Exception ex) {
                    Log.e(TAG, "Error sincronizando signos vitales con Firestore", ex);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al ejecutar medirAhora", e);
            }
        });
    }

    public void vitalsMostrado() {
        lastVitalsUpdate.setValue(null);
    }

    public void logout() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                prefs.edit().putBoolean("isLoggedIn", false).apply();
            } catch (Exception e) {
                Log.e(TAG, "Error en logout al modificar SharedPreferences", e);
            }
        });
    }

    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getPulso() { return pulso; }
    public LiveData<String> getOxigeno() { return oxigeno; }
    public LiveData<List<Medication>> getMedicamentos() { return medicamentos; }
    public LiveData<String> getContactName1() { return contactName1; }
    public LiveData<String> getContactPhone1() { return contactPhone1; }
    public LiveData<String> getContactName2() { return contactName2; }
    public LiveData<String> getContactPhone2() { return contactPhone2; }
    public LiveData<VitalsUpdate> getLastVitalsUpdate() { return lastVitalsUpdate; }

    public LiveData<Integer> getTabIndex() { return tabIndex; }
    public void setTab(int index) { tabIndex.setValue(index); }
}
