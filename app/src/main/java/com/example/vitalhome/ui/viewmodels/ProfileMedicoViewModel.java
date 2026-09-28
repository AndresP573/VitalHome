package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel encargado de la información del perfil médico del usuario y sus contactos de emergencia.
 */
public class ProfileMedicoViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final SharedPreferences prefs;
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> contact1 = new MutableLiveData<>();
    private final MutableLiveData<String> contact2 = new MutableLiveData<>();

    // --- Bloque 2: Constructor e Inicialización ---
    public ProfileMedicoViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        loadProfile();
    }

    // --- Bloque 3: Acceso a LiveData ---
    public LiveData<String> getName() { return name; }
    public LiveData<String> getContact1() { return contact1; }
    public LiveData<String> getContact2() { return contact2; }

    // --- Bloque 4: Carga de datos del perfil ---
    private void loadProfile() {
        name.setValue(prefs.getString("nombreAdultoMayor", "Marta Rodríguez"));
        
        String c1Name = prefs.getString("client_contact_name_1", "Hijo — Juan");
        String c1Phone = prefs.getString("client_contact_phone_1", "1234-5678");
        String c2Name = prefs.getString("client_contact_name_2", "Vecina — Ana");
        String c2Phone = prefs.getString("client_contact_phone_2", "8765-4321");

        if (!c1Phone.trim().isEmpty()) {
            contact1.setValue((c1Name.isEmpty() ? "Contacto 1" : c1Name) + " (" + c1Phone + ")");
        } else {
            contact1.setValue("");
        }
        
        if (!c2Phone.trim().isEmpty()) {
            contact2.setValue((c2Name.isEmpty() ? "Contacto 2" : c2Name) + " (" + c2Phone + ")");
        } else {
            contact2.setValue("");
        }
    }
}
