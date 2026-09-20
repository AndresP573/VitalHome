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
import com.example.vitalhome.ui.controladores.AppRepo;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardClienteViewModel extends AndroidViewModel {
    private final AppRepo repo;
    private final SharedPreferences prefs;
    
    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> pulso = new MutableLiveData<>();
    private final MutableLiveData<String> oxigeno = new MutableLiveData<>();
    private final MutableLiveData<List<Medication>> medicamentos = new MutableLiveData<>();
    
    private final MutableLiveData<String> contactName1 = new MutableLiveData<>();
    private final MutableLiveData<String> contactPhone1 = new MutableLiveData<>();
    private final MutableLiveData<String> contactName2 = new MutableLiveData<>();
    private final MutableLiveData<String> contactPhone2 = new MutableLiveData<>();
    
    private final MutableLiveData<Integer> tabIndex = new MutableLiveData<>(1);

    public static class VitalsUpdate {
        public final int p, o;
        public final String pres;
        public VitalsUpdate(int p, int o, String pres) { this.p = p; this.o = o; this.pres = pres; }
    }
    private final MutableLiveData<VitalsUpdate> lastVitalsUpdate = new MutableLiveData<>();

    public DashboardClienteViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
    }

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
        items.sort((m1, m2) -> Boolean.compare(m2.isTaken(), m1.isTaken()));
        medicamentos.setValue(items);
    }

    private void cargarContactos() {
        contactName1.setValue(prefs.getString("client_contact_name_1", ""));
        contactPhone1.setValue(prefs.getString("client_contact_phone_1", ""));
        contactName2.setValue(prefs.getString("client_contact_name_2", ""));
        contactPhone2.setValue(prefs.getString("client_contact_phone_2", ""));
    }

    public void guardarContactos(String n1, String p1, String n2, String p2) {
        prefs.edit()
            .putString("client_contact_name_1", n1)
            .putString("client_contact_phone_1", p1)
            .putString("client_contact_name_2", n2)
            .putString("client_contact_phone_2", p2)
            .apply();
        cargarContactos();
    }

    public void tomar(Medication m) {
        List<Medication> items = repo.getMeds();
        for (Medication item : items) {
            if (item.getId().equals(m.getId())) {
                item.setTaken(true);
                break;
            }
        }
        repo.saveMeds(items);
        repo.addEvent(new AlertEvent("MEDICINA", "Tomó " + m.getNombre(), "Casa", getTime()));
        refrescarMeds();
    }

    public void medirAhora() {
        int p = (int) (Math.random() * 30 + 65);
        int o = (int) (Math.random() * 5 + 95);
        String pres = (int)(Math.random()*25+110) + "/" + (int)(Math.random()*15+70);
        
        repo.addEvent(new AlertEvent("SALUD", "Control: " + p + " LPM", "Casa", getTime()));
        prefs.edit().putString("ultimos_vitals", "{\"pulso\":" + p + ",\"oxig\":" + o + ",\"pres\":\"" + pres + "\"}").apply();
        
        refrescarSignos();
        lastVitalsUpdate.setValue(new VitalsUpdate(p, o, pres));
    }

    public void vitalsMostrado() {
        lastVitalsUpdate.setValue(null);
    }

    public void switchRole() {
        prefs.edit().putString("rol", "cuidador").apply();
    }

    public void logout() {
        prefs.edit().putBoolean("isLoggedIn", false).apply();
    }

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
