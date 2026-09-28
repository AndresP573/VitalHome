package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.repository.AppRepo;

/**
 * ViewModel correspondiente al Dashboard del Cuidador.
 * Proporciona información general sobre el adulto mayor asignado y el estado de sus medicamentos.
 */
public class DashboardCuidadorViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> nombreAdultoMayor = new MutableLiveData<>();
    private final MutableLiveData<Integer> medicinasCount = new MutableLiveData<>();

    // --- Bloque 2: Constructor e inicialización ---
    public DashboardCuidadorViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
    }

    // --- Bloque 3: Carga y actualización de datos ---
    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombre", "Usuario"));
        nombreAdultoMayor.setValue(prefs.getString("nombreAdultoMayor", "Familiar"));
        medicinasCount.setValue(repo.getMeds().size());
    }

    // --- Bloque 4: Métodos Getters para LiveData ---
    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getNombreAdultoMayor() { return nombreAdultoMayor; }
    public LiveData<Integer> getMedicinasCount() { return medicinasCount; }
}
