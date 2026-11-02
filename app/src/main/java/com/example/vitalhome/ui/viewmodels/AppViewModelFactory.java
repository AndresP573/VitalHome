package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.vitalhome.data.repository.AppRepo;

/**
 * Fábrica personalizada de ViewModels que permite inyectar el repositorio centralizado AppRepo
 * y el contexto de Application a los ViewModels de la aplicación.
 */
public class AppViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final AppRepo repo;

    public AppViewModelFactory(@NonNull Application application, @NonNull AppRepo repo) {
        this.application = application;
        this.repo = repo;
    }

    public AppViewModelFactory(@NonNull Application application) {
        this.application = application;
        this.repo = new AppRepo(application);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(MedicamentosViewModel.class)) {
            return (T) new MedicamentosViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(AlertasViewModel.class)) {
            return (T) new AlertasViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(DashboardClienteViewModel.class)) {
            return (T) new DashboardClienteViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(DashboardCuidadorViewModel.class)) {
            return (T) new DashboardCuidadorViewModel(application, repo);
        } else if (modelClass.isAssignableFrom(PulseraViewModel.class)) {
            return (T) new PulseraViewModel(application, repo);
        }
        throw new IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.getName());
    }
}
