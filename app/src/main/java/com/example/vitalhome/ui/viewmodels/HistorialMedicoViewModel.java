package com.example.vitalhome.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.vitalhome.data.modelo.MedicalRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel para gestionar y proveer la lista de registros médicos en el historial clínico.
 */
public class HistorialMedicoViewModel extends AndroidViewModel {

    // --- Bloque 1: Atributos y LiveData ---
    private final MutableLiveData<List<MedicalRecord>> records = new MutableLiveData<>();

    // --- Bloque 2: Constructor e inicialización ---
    public HistorialMedicoViewModel(@NonNull Application application) {
        super(application);
        loadRecords();
    }

    // --- Bloque 3: Métodos de acceso a LiveData ---
    public LiveData<List<MedicalRecord>> getRecords() {
        return records;
    }

    // --- Bloque 4: Carga de registros médicos de prueba ---
    private void loadRecords() {
        List<MedicalRecord> data = new ArrayList<>();
        data.add(new MedicalRecord("10/09/2026", "Chequeo Cardiológico", "Dr. Ricardo Pérez", "Electrocardiograma normal. Ritmo sinusal estable. Próximo control en 6 meses."));
        data.add(new MedicalRecord("05/08/2026", "Análisis de Sangre", "Lab Vital Central", "Niveles de glucosa controlados. Colesterol dentro de los rangos óptimos."));
        data.add(new MedicalRecord("20/07/2026", "Consulta Oftalmológica", "Dra. Lucía Méndez", "Cambio de graduación en lentes de lectura. Sin signos de cataratas."));
        data.add(new MedicalRecord("12/06/2026", "Control de Presión", "Clínica del Sol", "Presión arterial estable (120/80) tras ajuste de medicación."));
        records.setValue(data);
    }
}
