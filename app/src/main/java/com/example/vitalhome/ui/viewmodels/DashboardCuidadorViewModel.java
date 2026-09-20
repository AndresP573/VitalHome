package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.ui.controladores.AppRepo;

public class DashboardCuidadorViewModel extends AndroidViewModel {
    private final AppRepo repo;
    private final SharedPreferences prefs;

    private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> nombreAdultoMayor = new MutableLiveData<>();
    private final MutableLiveData<Integer> medicinasCount = new MutableLiveData<>();

    public DashboardCuidadorViewModel(@NonNull Application application) {
        super(application);
        repo = new AppRepo(application);
        prefs = application.getSharedPreferences("VitalHomePrefs", Context.MODE_PRIVATE);
        refreshData();
    }

    public void refreshData() {
        nombreUsuario.setValue(prefs.getString("nombre", "Usuario"));
        nombreAdultoMayor.setValue(prefs.getString("nombreAdultoMayor", "Familiar"));
        medicinasCount.setValue(repo.getMeds().size());
    }

    public LiveData<String> getNombreUsuario() { return nombreUsuario; }
    public LiveData<String> getNombreAdultoMayor() { return nombreAdultoMayor; }
    public LiveData<Integer> getMedicinasCount() { return medicinasCount; }
}