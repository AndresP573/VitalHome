package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.example.vitalhome.data.modelo.Medication;
import com.example.vitalhome.data.repository.AppRepo;
import com.example.vitalhome.utils.MedicationAlarmScheduler;
import com.example.vitalhome.utils.NotificationHelper;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * ViewModel para el Dashboard del usuario (Cliente / Adulto Mayor).
 * Administra los signos vitales con tasa de refresco instantánea, medicamentos del día,
 * contactos de emergencia y mediciones de salud agregadas en un único evento clínico por medición.
 */
public class DashboardClienteViewModel extends AndroidViewModel {

    // --- Atributos y LiveData ---
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
     */
    public static class VitalsUpdate {
        public final int p, o;
        public final String pres;
        public VitalsUpdate(int p, int o, String pres) { this.p = p; this.o = o; this.pres = pres; }
    }
    private final MutableLiveData<VitalsUpdate> lastVitalsUpdate = new MutableLiveData<>();

    /**
     * Constructor e Inicialización del ViewModel.
     *
     * @param application Contexto de la aplicación.
     */
    public DashboardClienteViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
        iniciarEscuchaMedsFirestore();
    }

    public void iniciarEscuchaMedsFirestore() {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("medications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    List<Medication> listaMeds = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
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
                            } catch (Exception ignored) {}
                        }
                    }
                    repo.saveMeds(listaMeds);
                    medicamentos.postValue(listaMeds);
                });
        } catch (Exception ignored) {}
    }

    // --- Métodos para refrescar datos desde preferencias y repositorio ---
    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombreAdultoMayor", "Usuario"));
        refrescarSignos();
        refrescarMeds();
        cargarContactos();
    }

    private void refrescarSignos() {
        String json = prefs.getString("ultimos_vitals", "{}");
        try {
            JSONObject data = new JSONObject(json);
            if (data.has("pulso")) pulso.setValue(data.getInt("pulso") + " LPM");
            if (data.has("oxig")) oxigeno.setValue(data.getInt("oxig") + "%");
        } catch (Exception ignored) {}
    }

    private void refrescarMeds() {
        List<Medication> items = repo.getMeds();
        List<Medication> sinTomar = new ArrayList<>();
        if (items != null) {
            for (Medication m : items) {
                if (m != null && !m.isTaken()) {
                    sinTomar.add(m);
                }
            }
        }
        medicamentos.setValue(sinTomar);
    }

    private void cargarContactos() {
        contactName1.setValue(prefs.getString("client_contact_name_1", ""));
        contactPhone1.setValue(prefs.getString("client_contact_phone_1", ""));
        contactName2.setValue(prefs.getString("client_contact_name_2", ""));
        contactPhone2.setValue(prefs.getString("client_contact_phone_2", ""));
    }

    // --- Gestión de Contactos de Emergencia ---
    public void guardarContactos(String n1, String p1, String n2, String p2) {
        prefs.edit()
                .putString("client_contact_name_1", n1)
                .putString("client_contact_phone_1", p1)
                .putString("client_contact_name_2", n2)
                .putString("client_contact_phone_2", p2)
                .apply();
        cargarContactos();
    }

    // --- Gestión de Medicamentos ---
    public void tomar(Medication m) {
        if (m == null) return;

        // 1. Registrar el evento de toma en el Registro de Eventos
        String horaStr = getTime();
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", horaStr));

        // 2. Ocultar el medicamento de la lista diaria local del abuelo
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
        medicamentos.setValue(nuevaLista);
        MedicationAlarmScheduler.cancelar(getApplication(), m.getId());

        // 3. Actualizar taken: true en Cloud Firestore (SIN borrar el documento) para que la pantalla del Cuidador pase a VERDE
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("medications")
                .document(m.getId())
                .set(java.util.Collections.singletonMap("taken", true), com.google.firebase.firestore.SetOptions.merge());
        } catch (Exception ignored) {}
    }

    public void limpiarRemedios() {
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
        medicamentos.setValue(new ArrayList<>());
    }

    // --- Medición y Simulación de Signos Vitales Agregados en 1 Sola Alerta ---
    public void medirAhora() {
        com.example.vitalhome.utils.AppExecutors.getInstance().diskIO().execute(() -> {
            int p;
            if (Math.random() < 0.25) {
                p = (int) (Math.random() * 30 + 101); // 101-130 (Elevado)
            } else {
                p = (int) (Math.random() * 31 + 60);  // 60-90 (Normal)
            }

            int o;
            if (Math.random() < 0.15) {
                o = (int) (Math.random() * 4 + 88);   // 88-91 (Bajo)
            } else {
                o = (int) (Math.random() * 5 + 95);   // 95-99 (Normal)
            }

            int sis, dia;
            if (Math.random() < 0.25) {
                sis = (int) (Math.random() * 26 + 140); // 140-165 (Elevada)
                dia = (int) (Math.random() * 16 + 90);  // 90-105 (Elevada)
            } else {
                sis = (int) (Math.random() * 20 + 110); // 110-129 (Normal)
                dia = (int) (Math.random() * 11 + 70);  // 70-80 (Normal)
            }
            String presString = sis + "/" + dia;

            // Actualización INSTANTÁNEA de LiveData en pantalla (0ms de latencia)
            pulso.postValue(p + " LPM");
            oxigeno.postValue(o + "%");
            lastVitalsUpdate.postValue(new VitalsUpdate(p, o, presString));

            prefs.edit().putString("ultimos_vitals", "{\"pulso\":" + p + ",\"oxig\":" + o + ",\"pres\":\"" + presString + "\"}").apply();

            String horaStr = getTime();
            List<String> anomalias = new ArrayList<>();

            if (com.example.vitalhome.utils.VitalSignsThresholds.isPulsoAnomalo(p)) {
                String msg = p > com.example.vitalhome.utils.VitalSignsThresholds.PULSO_MAX_NORMAL ? ("Pulso elevado: " + p + " LPM") : ("Pulso bajo: " + p + " LPM");
                anomalias.add(msg);
            }

            if (com.example.vitalhome.utils.VitalSignsThresholds.isPresionAnomala(sis, dia)) {
                anomalias.add("Presión elevada: " + presString + " mmHg");
            }

            if (com.example.vitalhome.utils.VitalSignsThresholds.isOxigenoAnomalo(o)) {
                anomalias.add("Oxígeno bajo: " + o + "%");
            }

            if (!anomalias.isEmpty()) {
                // ANÓMALO: Generar UNA sola alerta agregada detallando todos los valores elevados/bajos
                String resumenAnomalias = String.join(" | ", anomalias);
                NotificationHelper.notificarAlertaCuidador(getApplication(), "SALUD", "⚠️ Alerta de Salud", resumenAnomalias);
                repo.addEvent(new AlertEvent("SALUD", resumenAnomalias, "Casa", horaStr));
            } else {
                // NORMAL: Registrar un único evento de control rutinario completo
                NotificationHelper.pulso(getApplication(), p);
                repo.addEvent(new AlertEvent("SALUD", "Control: " + p + " LPM | O2: " + o + "% | Presión: " + presString, "Casa", horaStr));
            }

            // Sincronización en tiempo real con Cloud Firestore
            try {
                String abueloEmail = prefs.getString("email", "marta@vitalhome.com").replace(".", "_");
                String abueloNombre = prefs.getString("nombreAdultoMayor", "Adulto Mayor");

                java.util.Map<String, Object> vitalsData = new java.util.HashMap<>();
                vitalsData.put("pulso", p + " LPM");
                vitalsData.put("oxigeno", o + "%");
                vitalsData.put("presion", presString);
                vitalsData.put("pacienteNombre", abueloNombre);
                vitalsData.put("updatedAt", new Date());

                com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                db.collection("users")
                    .document(abueloEmail)
                    .set(java.util.Collections.singletonMap("ultimosSignosVitales", vitalsData), com.google.firebase.firestore.SetOptions.merge());

                db.collection("vitals")
                    .document("latest")
                    .set(vitalsData);
            } catch (Exception ignored) {}
        });
    }

    public void vitalsMostrado() {
        lastVitalsUpdate.setValue(null);
    }

    public void logout() {
        prefs.edit().putBoolean("isLoggedIn", false).apply();
    }

    // --- Métodos Auxiliares y Getters ---
    private String getTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
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
